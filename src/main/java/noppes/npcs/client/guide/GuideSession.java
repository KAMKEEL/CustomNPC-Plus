package noppes.npcs.client.guide;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import kamkeel.npcs.network.packets.data.GuideTargetPacket;
import kamkeel.npcs.network.packets.request.GuideQueryPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.ChatComponentText;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.List;
import java.util.UUID;

/**
 * 客户端引路会话，同时只有一个目标。负责驱动分帧 A*、跟踪目标位置、判定结束条件，
 * 并把算好的曲线交给 GuideRenderer 画。
 */
public class GuideSession {

    public static final GuideSession Instance = new GuideSession();

    private final Minecraft mc = Minecraft.getMinecraft();

    private boolean active;
    /**
     * 服务端那侧的 UUID。客户端拿它查不到实体（见 targetEntityId），只用来原样回传给服务端，
     * 让刷新时锁死同一个 NPC。
     */
    private UUID targetId;
    /**
     * 客户端查实体只能靠实体 ID。1.7.10 的实体生成包不带 UUID（Forge 的 EntitySpawnMessage
     * 与原版 S0FPacketSpawnMob 都没有这个字段），客户端这份 NPC 实例的 UUID 是构造时现摇的
     * 随机值，跟服务端的必然不同；实体 ID 才是两端同步一致的那个标识。
     */
    private int targetEntityId = GuideTargetPacket.NO_ENTITY_ID;
    private String targetName = "";
    private int targetDimension;
    private double targetX;
    private double targetY;
    private double targetZ;
    /** 菱形标记画在这个高度之上。目标不在加载范围时只能估，进入范围后用真实眼高覆盖。 */
    private double targetEyeY;

    private GuidePathfinder.Search search;
    private double[][] curve;
    private double[] arcLengths;

    private int ticksSinceReplan;
    private int ticksSinceRefresh;
    private int missedRefreshes;
    /** 距上一次真正拿到目标位置（实体实时位置，或服务端刷新回包）过了多少 tick。见 tick()。 */
    private int ticksSinceTargetUpdate;

    private GuideSession() {}

    // ================= 由 GuideTargetPacket 调用 =================

    public void startGuiding(UUID id, int entityId, String name, int dimension,
                             double x, double y, double z) {
        reset();
        this.active = true;
        adoptIdentity(id, entityId, name, dimension);
        setTargetPos(x, y, z, y + 1.8);
        say("\u00a7b开始引路：\u00a7e" + this.targetName);
    }

    /**
     * 服务端刷新回来的目标。除了坐标还要一并更新身份——服务端按 UUID 找不到、退回按名字命中
     * 另一个 NPC 时，客户端得跟着换到新目标，否则手上攥着的还是那个已经不在了的 UUID/实体 ID，
     * 既锁不上新目标，也认不出它其实就走在眼前。
     */
    public void refreshTarget(UUID id, int entityId, String name, int dimension,
                              double x, double y, double z) {
        if (!active) {
            return;
        }
        adoptIdentity(id, entityId, name, dimension);
        setTargetPos(x, y, z, y + 1.8);
        missedRefreshes = 0;
        ticksSinceTargetUpdate = 0;
    }

    /** 服务端刷新时报找不到。连续多次才判定跟丢——单次找不到可能只是区块暂时卸载。 */
    public void onTargetMissing() {
        if (!active) {
            return;
        }
        missedRefreshes++;
        if (missedRefreshes >= GuideRenderConfig.MAX_MISSED_REFRESHES) {
            loseTarget();
        }
    }

    /** 跟丢的唯一出口，保证文案与结束动作只有一处。 */
    private void loseTarget() {
        say("\u00a7c跟丢了『\u00a7e" + targetName + "\u00a7c』");
        reset();
    }

    private void adoptIdentity(UUID id, int entityId, String name, int dimension) {
        this.targetId = id;
        this.targetEntityId = entityId;
        this.targetName = name == null ? "" : name;
        this.targetDimension = dimension;
    }

    public void stopByCommand() {
        if (!active) {
            say("\u00a77当前没有正在进行的引路");
            return;
        }
        say("\u00a77已停止引路：\u00a7e" + targetName);
        reset();
    }

    public void printStatus() {
        if (!active) {
            say("\u00a77当前没有正在引路。用法：/guide <NPC名>  |  /guide stop");
            return;
        }
        say("\u00a7b正在引路：\u00a7e" + targetName + " \u00a77(直线 " + (int) distanceToTarget() + " 格)");
    }

    /** 渲染回调里出异常时用：静默结束，避免每帧往聊天栏刷屏。 */
    public void stopSilently() {
        reset();
    }

    // ================= 渲染侧读取 =================

    public boolean isActive() {
        return active;
    }

    public double[][] getCurve() {
        return curve;
    }

    public double[] getArcLengths() {
        return arcLengths;
    }

    /** 菱形标记该画的世界坐标；没有目标时返回 null。 */
    public double[] getMarkerPos() {
        if (!active) {
            return null;
        }
        return new double[]{targetX, targetEyeY + GuideRenderConfig.MARKER_HEIGHT_ABOVE_EYES, targetZ};
    }

    public double distanceToTarget() {
        if (mc.thePlayer == null) {
            return Double.MAX_VALUE;
        }
        double dx = targetX - mc.thePlayer.posX;
        double dy = targetY - mc.thePlayer.posY;
        double dz = targetZ - mc.thePlayer.posZ;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    // ================= tick =================

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        // 安全网：1.7.10 里 tick 回调漏出去的异常会直接崩游戏。结束当前引路而不是继续
        // 每 tick 抛，否则日志和聊天栏都会被刷爆。
        try {
            tick();
        } catch (Throwable t) {
            System.out.println("[CustomNPC+] 引路会话 tick 抛出未处理异常：");
            t.printStackTrace();
            reset();
        }
    }

    private void tick() {
        if (!active) {
            return;
        }
        if (mc.thePlayer == null || mc.theWorld == null) {
            reset();
            return;
        }
        if (mc.thePlayer.dimension != targetDimension) {
            say("\u00a77已切换维度，引路结束");
            reset();
            return;
        }

        ticksSinceTargetUpdate++;
        updateTargetPosition();

        // 纯本地的跟丢兜底：只认服务端主动回的 MISSING 是不够的——刷新请求被权限闸丢掉、
        // 服务端那侧抛异常被吞、网络丢包，任何一种都会让那条回包永远不来，于是屏幕上挂着
        // 一条指向陈旧坐标的路，永远不会自己消失。这里只看"多久没拿到过新位置"，不依赖
        // 对端有没有回话。
        if (ticksSinceTargetUpdate >= GuideRenderConfig.TARGET_STALE_TIMEOUT_TICKS) {
            loseTarget();
            return;
        }

        if (distanceToTarget() <= GuideRenderConfig.ARRIVAL_DISTANCE) {
            say("\u00a7a已抵达『\u00a7e" + targetName + "\u00a7a』");
            reset();
            return;
        }

        advanceSearch();
    }

    /** 目标在加载范围内就用实体实时位置（NPC 走动时路径跟着变），否则定期向服务端要一次。 */
    private void updateTargetPosition() {
        Entity found = findTargetEntity();
        if (found != null) {
            setTargetPos(found.posX, found.posY, found.posZ, found.posY + found.getEyeHeight());
            missedRefreshes = 0;
            ticksSinceRefresh = 0;
            ticksSinceTargetUpdate = 0;
            return;
        }
        ticksSinceRefresh++;
        if (ticksSinceRefresh >= GuideRenderConfig.REFRESH_INTERVAL_TICKS) {
            ticksSinceRefresh = 0;
            GuideQueryPacket.requestRefresh(targetName, targetId);
        }
    }

    /**
     * 按实体 ID 查（不是 UUID，原因见 targetEntityId 的注释）。实体 ID 在实体移除后会被回收
     * 复用，所以还要确认查到的确实是个 NPC，免得目标消失之后引路指向一头刚生成的猪。
     */
    private Entity findTargetEntity() {
        if (targetEntityId == GuideTargetPacket.NO_ENTITY_ID) {
            return null;
        }
        Entity e = mc.theWorld.getEntityByID(targetEntityId);
        return e instanceof EntityNPCInterface ? e : null;
    }

    /**
     * 每秒起一次新搜索，每 tick 推进一小段。新路径算完之前继续显示旧的，否则每秒会闪一下。
     *
     * 上一次没算完就到了下个刷新点时，先把它「目前找到的最优候选路线」取出来用掉再丢。不这么
     * 做的话，只要一轮搜索在一个 replan 周期内算不完（远距离 + 终点不可达时 A* 会退化成对整片
     * 已加载区域的穷举，慢机或 GC 抖动很容易超），adoptResult() 就永远轮不到调用，屏幕上一条线
     * 都不会出现，同时每 tick 还稳定烧掉一整个时间片。用部分路径的效果恰好就是设计里想要的分段
     * 引路：先给一条朝目标方向的路，玩家走一段，起点前移，下一秒再算一段。
     */
    private void advanceSearch() {
        ticksSinceReplan++;
        if (search == null || ticksSinceReplan >= GuideRenderConfig.REPLAN_INTERVAL_TICKS) {
            if (search != null && !search.isFinished()) {
                adoptResult(search.getBestPathSoFar());
            }
            search = GuidePathfinder.startSearch(mc.theWorld,
                (int) Math.floor(mc.thePlayer.posX),
                (int) Math.floor(mc.thePlayer.posY),
                (int) Math.floor(mc.thePlayer.posZ),
                (int) Math.floor(targetX),
                (int) Math.floor(targetY),
                (int) Math.floor(targetZ));
            ticksSinceReplan = 0;
        }
        if (search.isFinished()) {
            return;
        }
        search.step(System.nanoTime() + GuideRenderConfig.STEP_BUDGET_NANOS);
        if (search.isFinished()) {
            adoptResult(search.getResult());
        }
    }

    /** 算出空结果时保留上一条路径，不清空——宁可显示一条稍旧的路，也不要突然什么都没有。 */
    private void adoptResult(List<int[]> path) {
        if (path == null || path.size() < 2) {
            return;
        }
        double[][] raw = new double[path.size()][];
        for (int i = 0; i < path.size(); i++) {
            int[] p = path.get(i);
            raw[i] = new double[]{
                p[0] + 0.5,
                p[1] + GuideRenderConfig.BAND_GROUND_OFFSET,
                p[2] + 0.5
            };
        }
        double[][] smoothed = GuideCurve.subdivide(raw, GuideRenderConfig.CHAIKIN_ROUNDS);
        this.curve = smoothed;
        this.arcLengths = GuideCurve.cumulativeLength(smoothed);
    }

    private void setTargetPos(double x, double y, double z, double eyeY) {
        this.targetX = x;
        this.targetY = y;
        this.targetZ = z;
        this.targetEyeY = eyeY;
    }

    private void reset() {
        active = false;
        targetId = null;
        targetEntityId = GuideTargetPacket.NO_ENTITY_ID;
        targetName = "";
        targetDimension = 0;
        search = null;
        curve = null;
        arcLengths = null;
        ticksSinceReplan = 0;
        ticksSinceRefresh = 0;
        missedRefreshes = 0;
        ticksSinceTargetUpdate = 0;
    }

    private void say(String text) {
        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText(text));
        }
    }
}
