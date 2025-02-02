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
import noppes.npcs.controllers.PartyController;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.Party;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.event.PartyEvent;

import java.io.IOException;

import static noppes.npcs.PacketHandlerServer.sendInviteData;

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
        return PacketHandler.REQUEST_PACKET;
    }


    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeString(out, this.name);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        EntityPlayer leavingPlayer = NoppesUtilServer.getPlayerByName(ByteBufUtils.readString(in));
        if (leavingPlayer != null) {
            PlayerData playerData = PlayerDataController.Instance.getPlayerData(player);
            if (playerData.partyUUID != null) {
                Party party = PartyController.Instance().getParty(playerData.partyUUID);
                boolean successful = party.removePlayer(leavingPlayer);
                if (successful) {
                    PartyEvent.PartyLeaveEvent partyEvent = new PartyEvent.PartyLeaveEvent(party, party.getQuest(), (IPlayer) NpcAPI.Instance().getIEntity(leavingPlayer));
                    EventHooks.onPartyLeave(partyEvent);
                    sendInviteData((EntityPlayerMP) leavingPlayer);
                    PartyController.Instance().pingPartyUpdate(party);
                    PartyController.Instance().sendLeavingMessages(party, leavingPlayer);
                }
            }
        }
    }
}
