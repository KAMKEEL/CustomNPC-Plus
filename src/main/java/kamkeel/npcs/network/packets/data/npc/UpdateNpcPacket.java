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
import noppes.npcs.entity.EntityNPCInterface;

import java.io.IOException;

public final class UpdateNpcPacket extends AbstractPacket {
    public static final String packetName = "Client|UpdateNpc";

    private NBTTagCompound npcCompound;

    public UpdateNpcPacket() {}

    public UpdateNpcPacket(NBTTagCompound npcCompound) {
        this.npcCompound = npcCompound;
    }

    @Override
    public Enum getType() {
        return EnumDataPacket.UPDATE_NPC;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.DATA_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeNBT(out, this.npcCompound);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        NBTTagCompound compound = ByteBufUtils.readNBT(in);
        Entity entity = Minecraft.getMinecraft().theWorld.getEntityByID(compound.getInteger("EntityId"));
        if (entity instanceof EntityNPCInterface) {
            ((EntityNPCInterface) entity).readSpawnData(compound);
        }
    }
}
