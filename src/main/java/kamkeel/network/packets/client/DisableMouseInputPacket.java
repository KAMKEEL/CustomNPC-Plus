package kamkeel.network.packets.client;

import io.netty.buffer.ByteBuf;
import kamkeel.network.AbstractPacket;
import kamkeel.network.PacketChannel;
import kamkeel.network.PacketHandler;
import kamkeel.network.enums.EnumClientPacket;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.client.ClientEventHandler;

import java.io.IOException;

public final class DisableMouseInputPacket extends AbstractPacket {
    public static final String packetName = "Client|DisableMouseInput";

    public DisableMouseInputPacket() {}

    @Override
    public Enum getType() {
        return EnumClientPacket.DISABLE_MOUSE_INPUT;
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
        long duration = in.readLong();
        String parsedButtons = Server.readString(in);

        if (parsedButtons == null || parsedButtons.isEmpty()) {
            ClientEventHandler.disabledButtonTimes.put(-1, duration);
            return;
        }

        String[] buttonIds = parsedButtons.split(";");
        for (String button : buttonIds) {
            ClientEventHandler.disabledButtonTimes.put(Integer.parseInt(button), duration);
        }
    }
}
