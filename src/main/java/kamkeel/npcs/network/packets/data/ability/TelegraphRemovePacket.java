package kamkeel.npcs.network.packets.data.ability;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.controllers.data.ability.telegraph.TelegraphManager;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumDataPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.io.IOException;

/**
 * Packet to remove a telegraph on the client.
 * Sent from server to client when an ability's telegraph should be removed early.
 */
public final class TelegraphRemovePacket extends AbstractPacket {
    public static final String packetName = "Data|TelegraphRemove";

    private String instanceId;

    public TelegraphRemovePacket() {
    }

    public TelegraphRemovePacket(String instanceId) {
        this.instanceId = instanceId;
    }

    @Override
    public Enum getType() {
        return EnumDataPacket.TELEGRAPH_REMOVE;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.DATA_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeString(out, instanceId);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        String id = ByteBufUtils.readString(in);
        if (id != null && TelegraphManager.ClientInstance != null) {
            TelegraphManager.ClientInstance.removeTelegraph(id);
        }
    }

    /**
     * Send remove to a specific player.
     */
    public static void send(String instanceId, EntityPlayerMP player) {
        PacketHandler.Instance.sendToPlayer(new TelegraphRemovePacket(instanceId), player);
    }

    /**
     * Send remove to all players tracking an entity (within range).
     */
    public static void sendToTracking(String instanceId, net.minecraft.entity.Entity entity) {
        PacketHandler.Instance.sendTracking(new TelegraphRemovePacket(instanceId), entity);
    }
}
