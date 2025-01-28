package kamkeel.network.packets.client;

import io.netty.buffer.ByteBuf;
import kamkeel.network.AbstractPacket;
import kamkeel.network.PacketChannel;
import kamkeel.network.PacketHandler;
import kamkeel.network.enums.EnumClientPacket;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.client.NoppesUtil;

import java.io.IOException;

public final class SyncPlayerPacket extends AbstractPacket {
    public static final String packetName = "Client|SyncPlayer";

    public SyncPlayerPacket() {}

    @Override
    public Enum getType() {
        return EnumClientPacket.SYNC_PLAYER;
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
        boolean sync = in.readBoolean();
        NoppesUtil.handlePlayerDataEnd(sync);
    }
}
