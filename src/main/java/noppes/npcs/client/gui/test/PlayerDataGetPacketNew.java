package noppes.npcs.client.gui.test;

import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.io.IOException;

public final class PlayerDataGetPacketNew extends AbstractPacket {
    public static final String packetName = "Request|PlayerDataGetNew";
    private String playerName;

    public PlayerDataGetPacketNew() {}

    public PlayerDataGetPacketNew(String playerName) {
        this.playerName = playerName;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.PlayerDataGet;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeString(out, playerName);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        String playerName = ByteBufUtils.readString(in);
        NoppesUtilServerNew.sendPlayerData(playerName, (EntityPlayerMP) player);
    }
}
