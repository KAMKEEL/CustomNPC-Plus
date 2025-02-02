package kamkeel.npcs.network.packets.data.npc;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumDataPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.entity.EntityNPCInterface;

import java.io.IOException;

public final class RolePacket extends AbstractPacket {
    public static final String packetName = "Data|Role";

    private NBTTagCompound compound;

    public RolePacket() {
    }

    public RolePacket(NBTTagCompound compound) {
        this.compound = compound;
    }

    @Override
    public Enum getType() {
        return EnumDataPacket.ROLE_NPC;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.DATA_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeNBT(out, this.compound);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        NBTTagCompound compound = ByteBufUtils.readNBT(in);
        Entity entity = Minecraft.getMinecraft().theWorld.getEntityByID(compound.getInteger("EntityId"));
        if (entity instanceof EntityNPCInterface) {
            EntityNPCInterface npc = (EntityNPCInterface) entity;
            npc.advanced.setRole(compound.getInteger("Role"));
            npc.roleInterface.readFromNBT(compound);
            NoppesUtil.setLastNpc(npc);
        }
    }
}
