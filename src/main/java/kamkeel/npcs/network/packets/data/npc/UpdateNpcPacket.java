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
import noppes.npcs.LogWriter;

import java.io.IOException;

public final class UpdateNpcPacket extends AbstractPacket {
    public static final String packetName = "Data|UpdateNpc";

    private NBTTagCompound npcCompound;

    public UpdateNpcPacket() {
    }

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
        try {
            ByteBufUtils.writeNBT(out, this.npcCompound);
        } catch (IOException e) {
            LogWriter.error("Failed to encode NPC update packet", e);
            throw e;
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        try {
            NBTTagCompound compound = ByteBufUtils.readNBT(in);
            Entity entity = Minecraft.getMinecraft().theWorld.getEntityByID(compound.getInteger("EntityId"));
            if (entity instanceof EntityNPCInterface) {
                ((EntityNPCInterface) entity).readSpawnData(compound);
            } else {
                LogWriter.error(String.format("Received NPC update for unknown entity id %d", compound.getInteger("EntityId")));
            }
        } catch (IOException e) {
            LogWriter.error("Failed to decode NPC update packet", e);
            throw e;
        } catch (Exception e) {
            IOException wrapped = new IOException(e);
            LogWriter.error("Unexpected error while handling NPC update packet", wrapped);
            throw wrapped;
        }
    }
}
