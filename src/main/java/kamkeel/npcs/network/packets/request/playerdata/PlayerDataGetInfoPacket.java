package kamkeel.npcs.network.packets.request.playerdata;

import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.NoppesUtilServer;

import java.io.IOException;

public final class PlayerDataGetInfoPacket extends AbstractPacket {
    public static final String packetName = "Request|PlayerDataGetNew";
    private String playerName;

    public PlayerDataGetInfoPacket() {}

    public PlayerDataGetInfoPacket(String playerName) {
        this.playerName = playerName;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.PlayerDataInfo;
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
        NoppesUtilServer.sendPlayerDataInfo(playerName, (EntityPlayerMP) player);
    }
}
