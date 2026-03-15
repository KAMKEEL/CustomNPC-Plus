package kamkeel.npcs.network.packets.data.telegraph;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.controllers.data.telegraph.TelegraphInstance;
import kamkeel.npcs.controllers.data.telegraph.TelegraphManager;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumDataPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import java.io.IOException;

/**
 * Packet to spawn a telegraph on the client.
 * Sent from server to client when a telegraph is created.
 */
public final class TelegraphSpawnPacket extends AbstractPacket {
    public static final String packetName = "Data|TelegraphSpawn";

    private NBTTagCompound telegraphNBT;

    public TelegraphSpawnPacket() {
    }

    public TelegraphSpawnPacket(TelegraphInstance instance) {
        this.telegraphNBT = instance.writeNBT();
    }

    @Override
    public Enum getType() {
        return EnumDataPacket.TELEGRAPH_SPAWN;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.DATA_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeNBT(out, telegraphNBT);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        NBTTagCompound nbt = ByteBufUtils.readNBT(in);
        if (nbt != null && TelegraphManager.ClientInstance != null) {
            TelegraphInstance instance = new TelegraphInstance();
            instance.readNBT(nbt);
            TelegraphManager.ClientInstance.addTelegraph(instance);
        }
    }

    /**
     * Send telegraph to a specific player.
     */
    public static void send(TelegraphInstance instance, EntityPlayerMP player) {
        PacketHandler.Instance.sendToPlayer(new TelegraphSpawnPacket(instance), player);
    }

    /**
     * Send telegraph to all players tracking an entity (within range).
     */
    public static void sendToTracking(TelegraphInstance instance, net.minecraft.entity.Entity entity) {
        PacketHandler.Instance.sendTracking(new TelegraphSpawnPacket(instance), entity);
    }

    /**
     * Send telegraph to all players in a dimension.
     */
    public static void sendToDimension(TelegraphInstance instance, int dimensionId) {
        PacketHandler.Instance.sendToDimension(new TelegraphSpawnPacket(instance), dimensionId);
    }

    /**
     * Send telegraph to all players on the server.
     */
    public static void sendToAll(TelegraphInstance instance) {
        PacketHandler.Instance.sendToAll(new TelegraphSpawnPacket(instance));
    }
}
