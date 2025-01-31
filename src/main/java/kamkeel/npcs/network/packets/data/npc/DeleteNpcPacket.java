package kamkeel.npcs.network.packets.data.npc;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumDataPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.entity.EntityNPCInterface;

import java.io.IOException;

public final class DeleteNpcPacket extends AbstractPacket {
    public static final String packetName = "Data|DeleteNpc";

    private int entityId;

    public DeleteNpcPacket() {}

    public DeleteNpcPacket(int id){
        this.entityId = id;
    }

    @Override
    public Enum getType() {
        return EnumDataPacket.DELETE_NPC;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.DATA_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(this.entityId);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        Entity entity = Minecraft.getMinecraft().theWorld.getEntityByID(in.readInt());
        if (entity instanceof EntityNPCInterface) {
            ((EntityNPCInterface) entity).delete();
        }
    }
}
