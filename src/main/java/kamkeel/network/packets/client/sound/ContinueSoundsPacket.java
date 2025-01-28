package kamkeel.network.packets.client.sound;

import io.netty.buffer.ByteBuf;
import kamkeel.network.AbstractPacket;
import kamkeel.network.PacketChannel;
import kamkeel.network.PacketHandler;
import kamkeel.network.enums.EnumClientPacket;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.client.controllers.ScriptSoundController;

import java.io.IOException;

public final class ContinueSoundsPacket extends AbstractPacket {
    public static final String packetName = "Client|ContinueSounds";

    public ContinueSoundsPacket() {}

    @Override
    public Enum getType() {
        return EnumClientPacket.CONTINUE_SOUNDS;
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
        ScriptSoundController.Instance.continueAllSounds();
    }
}
