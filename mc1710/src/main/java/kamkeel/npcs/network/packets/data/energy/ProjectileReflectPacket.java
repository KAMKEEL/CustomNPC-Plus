package kamkeel.npcs.network.packets.data.energy;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
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

/**
 * Syncs projectile reflection state to all tracking clients.
 * Sent server→client after a successful barrier reflection so the client
 * can update ownerEntityId, position, motion, colors, and type-specific
 * reflection data (beam mode, laser direction, disc boomerang, etc.).
 */
public final class ProjectileReflectPacket extends AbstractPacket {
    public static final String packetName = "Data|ProjectileReflect";

    private int entityId;
    private NBTTagCompound reflectData;

    public ProjectileReflectPacket() {
    }

    public ProjectileReflectPacket(int entityId, NBTTagCompound reflectData) {
        this.entityId = entityId;
        this.reflectData = reflectData;
    }

    @Override
    public Enum getType() {
        return EnumDataPacket.PROJECTILE_REFLECT;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.DATA_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(entityId);
        ByteBufUtils.writeNBT(out, reflectData);
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
        if (entity instanceof EntityEnergyProjectile) {
            ((EntityEnergyProjectile) entity).applyReflectionData(nbt);
        }
    }

    public static void sendToTracking(EntityEnergyProjectile projectile, NBTTagCompound reflectData) {
        if (projectile == null || reflectData == null) return;
        PacketHandler.Instance.sendTracking(
            new ProjectileReflectPacket(projectile.getEntityId(), reflectData),
            projectile
        );
    }
}
