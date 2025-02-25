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
import noppes.npcs.constants.EnumPlayerData;

import java.io.IOException;

public final class PlayerDataRemoveInfoPacket extends AbstractPacket {
    public static final String packetName = "Request|PlayerDataRemoveNew";

    private String playerName;
    private EnumPlayerData tabType;
    private int value;

    public PlayerDataRemoveInfoPacket() {}

    public PlayerDataRemoveInfoPacket(String playerName, EnumPlayerData tabType, int selectedKey) {
        this.playerName = playerName;
        this.tabType = tabType;
        this.value = selectedKey;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.PlayerDataDelete;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeString(out, playerName);
        out.writeInt(tabType.ordinal());
        out.writeInt(this.value);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        String playerName = ByteBufUtils.readString(in);
        int tabType = in.readInt();
        int value = in.readInt();
        NoppesUtilServer.removePlayerDataInfo(playerName, tabType, value, (EntityPlayerMP) player);
    }
}
