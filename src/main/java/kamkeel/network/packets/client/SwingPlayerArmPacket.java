package kamkeel.network.packets.client;

import io.netty.buffer.ByteBuf;
import kamkeel.network.AbstractPacket;
import kamkeel.network.PacketChannel;
import kamkeel.network.PacketHandler;
import kamkeel.network.enums.EnumClientPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import java.io.IOException;

public final class SwingPlayerArmPacket extends AbstractPacket {
    public static final String packetName = "Client|SwingPlayerArm";

    public SwingPlayerArmPacket() {}

    @Override
    public Enum getType() {
        return EnumClientPacket.SWING_PLAYER_ARM;
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
        Minecraft.getMinecraft().thePlayer.swingItem();
    }
}
