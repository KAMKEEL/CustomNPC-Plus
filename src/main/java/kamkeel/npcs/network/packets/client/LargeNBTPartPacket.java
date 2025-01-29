package kamkeel.npcs.network.packets.client;

import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumClientPacket;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.client.NoppesUtil;

import java.io.IOException;

public final class LargeNBTPartPacket extends AbstractPacket {
    public static final String packetName = "Client|LargeNBTPart";

    public LargeNBTPartPacket() {}

    @Override
    public Enum getType() {
        return EnumClientPacket.LARGE_NBT_PART;
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
        byte[] chunk = new byte[in.readShort()];
        in.readBytes(chunk);
        NoppesUtil.handleLargeData(chunk);
    }
}
