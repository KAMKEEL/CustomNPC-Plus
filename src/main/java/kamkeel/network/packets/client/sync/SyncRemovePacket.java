package kamkeel.network.packets.client.sync;

import io.netty.buffer.ByteBuf;
import kamkeel.network.AbstractPacket;
import kamkeel.network.PacketChannel;
import kamkeel.network.PacketHandler;
import kamkeel.network.enums.EnumClientPacket;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.controllers.SyncController;

import java.io.IOException;

public final class SyncRemovePacket extends AbstractPacket {
    public static final String packetName = "Client|SyncRemove";

    public SyncRemovePacket() {}

    @Override
    public Enum getType() {
        return EnumClientPacket.SYNC_REMOVE;
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
        int id = in.readInt();
        SyncController.clientSyncRemove(synctype, id);
    }
}
