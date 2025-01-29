package kamkeel.npcs.network.packets.client.sync;

import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumClientPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.Server;
import noppes.npcs.controllers.SyncController;

import java.io.IOException;

public final class SyncEndPacket extends AbstractPacket {
    public static final String packetName = "Client|SyncEnd";

    public SyncEndPacket() {}

    @Override
    public Enum getType() {
        return EnumClientPacket.SYNC_END;
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
        int synctype = in.readInt();
        NBTTagCompound compound = Server.readNBT(in);
        SyncController.clientSync(synctype, compound, true);
    }
}
