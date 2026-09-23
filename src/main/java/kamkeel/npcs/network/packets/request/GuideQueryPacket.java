package kamkeel.npcs.network.packets.request;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.network.packets.data.GuideTargetPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.guide.GuideNameMatcher;
import noppes.npcs.guide.GuideNpcLocator;
import noppes.npcs.guide.GuideServerTasks;

import java.io.IOException;
import java.util.UUID;

/**
 * 客户端 → 服务端：刷新超出客户端加载范围的引路目标坐标，每 5 秒一次。
 *
 * 带 UUID 是为了锁死同一个 NPC——只按名字找的话，玩家走近另一个同名 NPC 时目标会被悄悄
 * 换掉。UUID 找不到才退回按名字找（原目标可能刚好在两次刷新之间被重新生成）。
 *
 * 不覆盖 getPermission()，保持默认的 null，也就是不做权限检查——引路是面向所有玩家的功能，
 * 加了权限普通玩家的刷新会静默失效（见 PacketHandler 里 getPermission() != null 的判断）。
 * 同理还要覆盖 bypassOpsOnly()，见下面的注释。
 */
public class GuideQueryPacket extends AbstractPacket {

    public static final String packetName = "Request|GuideQuery";

    private String name = "";
    private String uuid = "";

    public GuideQueryPacket() {
    }

    @SideOnly(Side.CLIENT)
    public static void requestRefresh(String name, UUID id) {
        GuideQueryPacket packet = new GuideQueryPacket();
        packet.name = name == null ? "" : name;
        packet.uuid = id == null ? "" : id.toString();
        PacketClient.sendClient(packet);
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.GuideQuery;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    /**
     * PacketHandler 对 REQUEST 通道有一道 "Only Ops Edit NPCs" 的总闸，开了之后非 OP 发来的
     * request 包一律丢弃并打一行日志。那道闸是给编辑类的包写的，引路是面向所有玩家、明确不需要
     * OP 的功能：不豁免的话，公开服上普通玩家的 /guide 能用、START 包能到（DATA 通道不过这道
     * 闸），但之后每 5 秒的刷新全被丢，同时服务端日志每 5 秒刷一行 "tried to use CNPC+ without
     * being an op"。
     *
     * 这个包不改动任何东西，只是按名字/UUID 查一个 NPC 的坐标再发回给请求者本人，让所有玩家能用
     * 是安全的。
     */
    @Override
    public boolean bypassOpsOnly() {
        return true;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeString(out, name);
        ByteBufUtils.writeString(out, uuid);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        final String name = ByteBufUtils.readString(in);
        final String uuid = ByteBufUtils.readString(in);
        if (!(player instanceof EntityPlayerMP)) {
            return;
        }
        final EntityPlayerMP playerMP = (EntityPlayerMP) player;

        // 这里还在 netty 的 IO 线程上（1.7.10 的 ServerCustomPacketEvent 就是在那儿派发的），
        // 而查询要遍历 world.loadedEntityList，那个 ArrayList 同时正被主线程增删。目标区块刚好
        // 有实体生成/移除时就会撞上 ConcurrentModificationException，异常又会被 PacketHandler 的
        // catch(Exception) 吞掉，表现成刷新静默失效、连 MISSING 都不回。所以排到主线程再查。
        GuideServerTasks.enqueue(new Runnable() {
            @Override
            public void run() {
                lookupAndReply(playerMP, name, uuid);
            }
        });
    }

    /** 只在服务端主线程上调用，见 receiveData 里的说明。 */
    private static void lookupAndReply(EntityPlayerMP playerMP, String name, String uuid) {
        // 排队期间玩家可能已经掉线了，往一个断开的连接上发包没有意义。
        if (playerMP.playerNetServerHandler == null || playerMP.worldObj == null) {
            return;
        }

        EntityNPCInterface npc = GuideNpcLocator.findByUuid(playerMP, uuid);
        if (npc == null) {
            GuideNameMatcher.Result result = GuideNpcLocator.findInPlayerDimension(playerMP, name);
            if (result.chosen != null) {
                npc = (EntityNPCInterface) result.chosen.payload;
            }
        }
        if (npc == null) {
            GuideTargetPacket.sendMissing(playerMP);
        } else {
            GuideTargetPacket.sendRefresh(playerMP, npc);
        }
    }
}
