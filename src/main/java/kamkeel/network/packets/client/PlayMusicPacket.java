package kamkeel.network.packets.client;

import io.netty.buffer.ByteBuf;
import kamkeel.network.AbstractPacket;
import kamkeel.network.PacketChannel;
import kamkeel.network.PacketHandler;
import kamkeel.network.enums.EnumClientPacket;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.Server;
import noppes.npcs.client.controllers.MusicController;

import java.io.IOException;

public final class PlayMusicPacket extends AbstractPacket {
    public static final String packetName = "Client|PlayMusic";

    public PlayMusicPacket() {}

    @Override
    public Enum getType() {
        return EnumClientPacket.PLAY_MUSIC;
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
        String musicName = Server.readString(in);
        MusicController.Instance.playMusicBackground(musicName, player, Integer.MAX_VALUE);
    }
}
