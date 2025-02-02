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

public final class PartyInvitePacket extends AbstractPacket {
    public static final String packetName = "Request|PartyInvite";

    private String name;

    public PartyInvitePacket() {
    }

    public PartyInvitePacket(String playername) {
        this.name = playername;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.PartyInvite;
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
        EntityPlayer invitedPlayer = NoppesUtilServer.getPlayerByName(ByteBufUtils.readString(in));
        if (invitedPlayer != null) {
            PlayerData senderData = PlayerDataController.Instance.getPlayerData(player);
            PlayerData invitedData = PlayerDataController.Instance.getPlayerData(invitedPlayer);
            if (senderData.partyUUID != null && invitedData.partyUUID == null) {
                Party party = PartyController.Instance().getParty(senderData.partyUUID);
                if (!party.getIsLocked()) {
                    PartyEvent.PartyInviteEvent partyEvent = new PartyEvent.PartyInviteEvent(party, party.getQuest(), (IPlayer) NpcAPI.Instance().getIEntity(invitedPlayer));
                    EventHooks.onPartyInvite(partyEvent);
                    if (!partyEvent.isCancelled()) {
                        invitedData.inviteToParty(party);
                        sendInviteData((EntityPlayerMP) invitedPlayer);
                    }
                }
            }
        }
    }
}
