package kamkeel.npcs.network.packets.data.energyexplosion;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.entity.EntityEnergyExplosion;
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
 * Spawns a client-side explosion visual entity from serialized spawn data.
 */
public final class EnergyExplosionSpawnPacket extends AbstractPacket {
    public static final String packetName = "Data|EnergyExplosionSpawn";

    private String instanceId;
    private NBTTagCompound spawnNbt;

    public EnergyExplosionSpawnPacket() {
    }

    public EnergyExplosionSpawnPacket(String instanceId, NBTTagCompound spawnNbt) {
        this.instanceId = instanceId;
        this.spawnNbt = spawnNbt;
    }

    @Override
    public Enum getType() {
        return EnumDataPacket.ENERGY_EXPLOSION_SPAWN;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.DATA_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeString(out, instanceId);
        ByteBufUtils.writeNBT(out, spawnNbt);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        ByteBufUtils.readString(in); // Keep wire compatibility for future instance tracking.
        NBTTagCompound nbt = ByteBufUtils.readNBT(in);

        if (nbt == null) return;

        World world = Minecraft.getMinecraft().theWorld;
        if (world == null) return;

        EntityEnergyExplosion preview = new EntityEnergyExplosion(world);
        preview.importSpawnNBT(nbt);
        world.spawnEntityInWorld(preview);
    }

    public static void sendToTracking(String instanceId, EntityEnergyExplosion previewEntity, Entity trackingEntity) {
        if (instanceId == null || previewEntity == null || trackingEntity == null) return;
        NBTTagCompound nbt = previewEntity.exportSpawnNBT();
        PacketHandler.Instance.sendTracking(new EnergyExplosionSpawnPacket(instanceId, nbt), trackingEntity);
    }
}
