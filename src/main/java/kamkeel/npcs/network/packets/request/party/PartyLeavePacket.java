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
import noppes.npcs.EventHooks;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.config.ConfigMain;
import noppes.npcs.controllers.PartyController;
import noppes.npcs.controllers.data.Party;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.event.PartyEvent;

import java.io.IOException;

import static kamkeel.npcs.network.packets.request.party.PartyInvitePacket.sendInviteData;

public final class PartyLeavePacket extends AbstractPacket {
    public static final String packetName = "Request|PartyLeave";

    private String name;

    public PartyLeavePacket() {
    }

    public PartyLeavePacket(String playername) {
        this.name = playername;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.PartyLeave;
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
        String leavingName = ByteBufUtils.readString(in);
        if (!ConfigMain.PartiesEnabled) {
            return;
        }

        EntityPlayer leavingPlayer = NoppesUtilServer.getPlayerByName(leavingName);
        if (leavingPlayer == null) {
            return;
        }

        if (!leavingPlayer.getUniqueID().equals(player.getUniqueID()) && !NoppesUtilServer.isOp(player)) {
            return;
        }

        PlayerData playerData = PlayerData.get(player);
        if (playerData.partyUUID == null) {
            return;
        }

        Party party = PartyController.Instance().getParty(playerData.partyUUID);
        if (party == null) {
            return;
        }

        boolean successful = party.removePlayer(leavingPlayer);
        if (successful) {
            PartyEvent.PartyLeaveEvent partyEvent = new PartyEvent.PartyLeaveEvent(party, party.getQuest(), (IPlayer) NpcAPI.Instance().getIEntity(leavingPlayer));
            EventHooks.onPartyLeave(party, partyEvent);
            sendInviteData((EntityPlayerMP) leavingPlayer);
            PartyController.Instance().pingPartyUpdate(party);
            PartyController.Instance().sendLeavingMessages(party, leavingPlayer);
        }
    }
}
