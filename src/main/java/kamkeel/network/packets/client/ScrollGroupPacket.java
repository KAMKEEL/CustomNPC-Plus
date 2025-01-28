package kamkeel.network.packets.client;

import io.netty.buffer.ByteBuf;
import kamkeel.network.AbstractPacket;
import kamkeel.network.PacketChannel;
import kamkeel.network.PacketHandler;
import kamkeel.network.enums.EnumClientPacket;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.NoppesUtil;

import java.io.IOException;

public final class ScrollGroupPacket extends AbstractPacket {
    public static final String packetName = "Client|ScrollGroup";

    public ScrollGroupPacket() {}

    @Override
    public Enum getType() {
        return EnumClientPacket.SCROLL_GROUP;
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
        NoppesUtil.setScrollGroup(in);
    }
}
