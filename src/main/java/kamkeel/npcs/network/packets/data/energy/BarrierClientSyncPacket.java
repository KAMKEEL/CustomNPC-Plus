package kamkeel.npcs.network.packets.data.energy;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.entity.EntityEnergyBarrier;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumDataPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.io.IOException;

/**
 * Syncs all barrier client-relevant state to tracking clients.
 * Includes visual properties and barrier-specific data (dome radius, panel dimensions, etc.).
 * Sent server->client when scripts call syncClient() on an already-spawned energy barrier.
 */
public final class BarrierClientSyncPacket extends AbstractPacket {

    private int entityId;
    private NBTTagCompound syncData;

    public BarrierClientSyncPacket() {
    }

    public BarrierClientSyncPacket(int entityId, NBTTagCompound syncData) {
        this.entityId = entityId;
        this.syncData = syncData;
    }

    @Override
    public Enum getType() {
        return EnumDataPacket.BARRIER_CLIENT_SYNC;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.DATA_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(entityId);
        ByteBufUtils.writeNBT(out, syncData);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        int id = in.readInt();
        NBTTagCompound nbt = ByteBufUtils.readNBT(in);
        if (nbt == null) return;

        World world = Minecraft.getMinecraft().theWorld;
        if (world == null) return;

        Entity entity = world.getEntityByID(id);
        if (entity instanceof EntityEnergyBarrier) {
            ((EntityEnergyBarrier) entity).applyClientSyncData(nbt);
        }
    }

    public static void sendToTracking(EntityEnergyBarrier barrier, NBTTagCompound syncData) {
        if (barrier == null || syncData == null) return;
        PacketHandler.Instance.sendTracking(
            new BarrierClientSyncPacket(barrier.getEntityId(), syncData),
            barrier
        );
    }
}
