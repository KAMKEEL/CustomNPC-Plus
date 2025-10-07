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
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.config.ConfigMain;
import noppes.npcs.controllers.PartyController;
import noppes.npcs.controllers.data.Party;
import noppes.npcs.controllers.data.PlayerData;

import java.io.IOException;

public final class PartySetLeaderPacket extends AbstractPacket {
    public static final String packetName = "Request|PartySetLeader";

    private String name;

    public PartySetLeaderPacket() {
    }

    public PartySetLeaderPacket(String playername) {
        this.name = playername;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.PartySetLeader;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.PLAYER_PACKET;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeString(out, this.name);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        String newLeaderName = ByteBufUtils.readString(in);
        if (!ConfigMain.PartiesEnabled) {
            return;
        }

        PlayerData playerData = PlayerData.get(player);
        if (playerData.partyUUID == null) {
            return;
        }

        Party party = PartyController.Instance().getParty(playerData.partyUUID);
        if (!PartyPacketUtil.canManageParty(player, party) || party.getIsLocked()) {
            PartyInfoPacket.sendPartyData((EntityPlayerMP) player);
            return;
        }

        EntityPlayer newLeader = NoppesUtilServer.getPlayerByName(newLeaderName);
        if (newLeader == null || !party.hasPlayer(newLeader)) {
            return;
        }

        if (party.setLeader(newLeader)) {
            PartyController.Instance().pingPartyUpdate(party);
        }
    }
}
