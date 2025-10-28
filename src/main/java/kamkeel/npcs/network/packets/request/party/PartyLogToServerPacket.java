package kamkeel.npcs.network.packets.request.party;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.config.ConfigMain;
import noppes.npcs.controllers.data.PlayerData;

import java.io.IOException;

public final class PartyLogToServerPacket extends AbstractPacket {
    public static final String packetName = "Request|PartyLogToServer";

    private String key;

    public PartyLogToServerPacket() {
    }

    public PartyLogToServerPacket(String key) {
        this.key = key;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.PartyLogToServer;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.PLAYER_PACKET;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeString(out, this.key);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!ConfigMain.PartiesEnabled) {
            return;
        }

        PlayerData playerData = PlayerData.get(player);
        if (playerData.partyUUID == null) {
            return;
        }

        NoppesUtilPlayer.updatePartyQuestLogData(in, (EntityPlayerMP) player);
    }


}
