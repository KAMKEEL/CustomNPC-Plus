package kamkeel.npcs.network.packets.data;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumDataPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.client.guide.GuideSession;
import noppes.npcs.entity.EntityNPCInterface;

import java.io.IOException;
import java.util.UUID;

/**
 * 服务端 → 客户端的引路控制包。
 *
 * 引路状态只存在于客户端，服务端不知道谁在引路去哪，所以 /guide stop 和无参 /guide 也
 * 得转成控制包发回来由客户端自己执行。
 */
public final class GuideTargetPacket extends AbstractPacket {

    public static final String packetName = "Data|GuideTarget";

    public static final int ACTION_START = 0;
    public static final int ACTION_STOP = 1;
    public static final int ACTION_STATUS = 2;
    public static final int ACTION_REFRESH = 3;
    public static final int ACTION_MISSING = 4;

    /** 客户端拿不到实体时的实体 ID 占位值。原版实体 ID 恒为正数，-1 不会跟任何实体撞上。 */
    public static final int NO_ENTITY_ID = -1;

    private int action;
    private String name = "";
    private String uuid = "";
    private int entityId = NO_ENTITY_ID;
    private int dimension;
    private double x;
    private double y;
    private double z;

    public GuideTargetPacket() {
    }

    private GuideTargetPacket(int action) {
        this.action = action;
    }

    private static GuideTargetPacket withNpc(int action, EntityNPCInterface npc) {
        GuideTargetPacket packet = new GuideTargetPacket(action);
        packet.name = npc.display.name == null ? "" : npc.display.name;
        packet.uuid = npc.getUniqueID().toString();
        // 实体 ID 而不是 UUID 才是客户端能用来查实体的东西：1.7.10 的实体生成包不带 UUID
        // （Forge 的 EntitySpawnMessage 和原版 S0FPacketSpawnMob 里都没有这个字段，UUID 进
        // 生成包是 1.9 才有的），客户端那个 NPC 实例的 UUID 是构造时现摇的随机值，跟服务端
        // 必然不同。UUID 仍然要带上——它是服务端刷新时锁死同一个 NPC 用的同侧标识。
        packet.entityId = npc.getEntityId();
        packet.dimension = npc.dimension;
        packet.x = npc.posX;
        packet.y = npc.posY;
        packet.z = npc.posZ;
        return packet;
    }

    public static void sendStart(EntityPlayerMP player, EntityNPCInterface npc) {
        PacketHandler.Instance.sendToPlayer(withNpc(ACTION_START, npc), player);
    }

    public static void sendRefresh(EntityPlayerMP player, EntityNPCInterface npc) {
        PacketHandler.Instance.sendToPlayer(withNpc(ACTION_REFRESH, npc), player);
    }

    public static void sendStop(EntityPlayerMP player) {
        PacketHandler.Instance.sendToPlayer(new GuideTargetPacket(ACTION_STOP), player);
    }

    public static void sendStatus(EntityPlayerMP player) {
        PacketHandler.Instance.sendToPlayer(new GuideTargetPacket(ACTION_STATUS), player);
    }

    public static void sendMissing(EntityPlayerMP player) {
        PacketHandler.Instance.sendToPlayer(new GuideTargetPacket(ACTION_MISSING), player);
    }

    @Override
    public Enum getType() {
        return EnumDataPacket.GUIDE_TARGET;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.DATA_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(action);
        ByteBufUtils.writeString(out, name);
        ByteBufUtils.writeString(out, uuid);
        out.writeInt(entityId);
        out.writeInt(dimension);
        out.writeDouble(x);
        out.writeDouble(y);
        out.writeDouble(z);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        int action = in.readInt();
        String name = ByteBufUtils.readString(in);
        String uuid = ByteBufUtils.readString(in);
        int entityId = in.readInt();
        int dimension = in.readInt();
        double x = in.readDouble();
        double y = in.readDouble();
        double z = in.readDouble();

        switch (action) {
            case ACTION_STOP:
                GuideSession.Instance.stopByCommand();
                break;
            case ACTION_STATUS:
                GuideSession.Instance.printStatus();
                break;
            case ACTION_REFRESH:
                // 刷新包带的是完整身份而不只是坐标：服务端按 UUID 找不到、退回按名字命中
                // 另一个 NPC 时，客户端必须跟着换到新目标的标识，否则之后再也锁不上。
                GuideSession.Instance.refreshTarget(parseUuid(uuid), entityId, name, dimension, x, y, z);
                break;
            case ACTION_MISSING:
                GuideSession.Instance.onTargetMissing();
                break;
            case ACTION_START:
            default:
                GuideSession.Instance.startGuiding(parseUuid(uuid), entityId, name, dimension, x, y, z);
                break;
        }
    }

    /** 服务端总会填上合法 UUID，但网络来的字符串一律当不可信处理——解析失败返回 null 而不是抛。 */
    private static UUID parseUuid(String raw) {
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
