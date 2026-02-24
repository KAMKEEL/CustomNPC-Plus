package kamkeel.npcs.network.packets.data.energycharge;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.controllers.data.energycharge.EnergyChargePreviewManager;
import kamkeel.npcs.entity.EntityEnergyProjectile;
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
import java.lang.reflect.Constructor;

/**
 * Spawns a client-only charging preview entity from serialized spawn data.
 */
public final class EnergyChargeSpawnPacket extends AbstractPacket {
    public static final String packetName = "Data|EnergyChargeSpawn";

    private String instanceId;
    private String entityClassName;
    private NBTTagCompound spawnNbt;

    public EnergyChargeSpawnPacket() {
    }

    public EnergyChargeSpawnPacket(String instanceId, String entityClassName, NBTTagCompound spawnNbt) {
        this.instanceId = instanceId;
        this.entityClassName = entityClassName;
        this.spawnNbt = spawnNbt;
    }

    @Override
    public Enum getType() {
        return EnumDataPacket.ENERGY_CHARGE_SPAWN;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.DATA_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeString(out, instanceId);
        ByteBufUtils.writeString(out, entityClassName);
        ByteBufUtils.writeNBT(out, spawnNbt);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        String id = ByteBufUtils.readString(in);
        String className = ByteBufUtils.readString(in);
        NBTTagCompound nbt = ByteBufUtils.readNBT(in);

        if (id == null || className == null || nbt == null) return;
        if (EnergyChargePreviewManager.ClientInstance == null) return;

        World world = Minecraft.getMinecraft().theWorld;
        if (world == null) return;

        EntityEnergyProjectile preview = createPreviewEntity(className, world);
        if (preview == null) return;

        preview.setPreviewMode(true);
        preview.importSpawnNBT(nbt);

        // Ensure the preview follows current owner immediately on first render frame.
        Entity owner = preview.getOwnerEntity();
        if (owner instanceof net.minecraft.entity.EntityLivingBase) {
            preview.setPreviewOwner((net.minecraft.entity.EntityLivingBase) owner);
        }

        EnergyChargePreviewManager.ClientInstance.addPreview(id, preview);
    }

    @SideOnly(Side.CLIENT)
    private static EntityEnergyProjectile createPreviewEntity(String className, World world) {
        try {
            Class<?> clazz = Class.forName(className);
            if (!EntityEnergyProjectile.class.isAssignableFrom(clazz)) {
                return null;
            }

            Constructor<?> constructor = clazz.getConstructor(World.class);
            Object instance = constructor.newInstance(world);
            return (EntityEnergyProjectile) instance;
        } catch (Exception ignored) {
            return null;
        }
    }

    public static void sendToTracking(String instanceId, EntityEnergyProjectile previewEntity, Entity trackingEntity) {
        if (previewEntity == null || trackingEntity == null) return;
        String className = previewEntity.getClass().getName();
        NBTTagCompound nbt = previewEntity.exportSpawnNBT();
        PacketHandler.Instance.sendTracking(new EnergyChargeSpawnPacket(instanceId, className, nbt), trackingEntity);
    }
}
