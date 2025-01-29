package kamkeel.npcs.network.packets.client;

import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumClientPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.Server;
import noppes.npcs.entity.EntityNPCInterface;

import java.io.IOException;

public final class UpdateNpcPacket extends AbstractPacket {
    public static final String packetName = "Client|UpdateNpc";

    public UpdateNpcPacket() {}

    @Override
    public Enum getType() {
        return EnumClientPacket.UPDATE_NPC;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.CLIENT_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        // TODO: Send Packet
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        NBTTagCompound compound = Server.readNBT(in);
        Entity entity = Minecraft.getMinecraft().theWorld.getEntityByID(compound.getInteger("EntityId"));
        if (entity instanceof EntityNPCInterface) {
            ((EntityNPCInterface) entity).readSpawnData(compound);
        }
    }
}
