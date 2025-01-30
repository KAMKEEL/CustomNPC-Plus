package kamkeel.npcs.network.packets.client;

import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumClientPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.Server;
import noppes.npcs.controllers.data.MarkData;
import noppes.npcs.entity.EntityNPCInterface;

import java.io.IOException;

public final class MarkDataPacket extends AbstractPacket {
    public static final String packetName = "Client|MarkData";

    public MarkDataPacket() {}

    @Override
    public Enum getType() {
        return EnumClientPacket.MARK_DATA;
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
        if(CustomNpcs.side() != Side.CLIENT)
            return;

        Entity entity = player.worldObj.getEntityByID(in.readInt());
        if (!(entity instanceof EntityNPCInterface)) return;

        EntityNPCInterface npc = (EntityNPCInterface) entity;
        NBTTagCompound nbt = Server.readNBT(in);
        MarkData data = MarkData.get(npc);
        data.setNBT(nbt);
    }
}
