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
import java.util.UUID;

import static kamkeel.npcs.network.packets.request.party.PartyInvitePacket.sendInviteData;

public final class PartyKickPacket extends AbstractPacket {
    public static final String packetName = "Request|PartyKick";

    private String name;

    public PartyKickPacket() {
    }

    public PartyKickPacket(String playername) {
        this.name = playername;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.PartyKick;
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
        String kickPlayerName = ByteBufUtils.readString(in);
        EntityPlayer kickPlayer = NoppesUtilServer.getPlayerByName(kickPlayerName);
        PlayerData playerData = null;
        UUID playerUUID = null;
        if (kickPlayer != null) {
            playerData = PlayerDataController.Instance.getPlayerData(player);
        } else {
            String uuid = PlayerDataController.Instance.getPlayerUUIDFromName(kickPlayerName);
            if (!uuid.isEmpty()) {
                playerData = PlayerDataController.Instance.getPlayerDataCache(uuid);
                playerUUID = UUID.fromString(uuid);
            }
        }

        if (playerData != null) {
            if (playerData.partyUUID != null) {
                Party party = PartyController.Instance().getParty(playerData.partyUUID);
                if (!party.getIsLocked()) {
                    PartyEvent.PartyKickEvent partyEvent = new PartyEvent.PartyKickEvent(party, party.getQuest(), (IPlayer) NpcAPI.Instance().getIEntity(kickPlayer));
                    EventHooks.onPartyKick(partyEvent);
                    if (!partyEvent.isCancelled()) {
                        boolean successful = party.removePlayer(kickPlayer);
                        if (!successful)
                            successful = party.removePlayer(playerUUID);

                        if (successful) {
                            if (kickPlayer != null) {
                                sendInviteData((EntityPlayerMP) kickPlayer);
                            }
                            PartyController.Instance().pingPartyUpdate(party);
                            PartyController.Instance().sendKickMessages(party, kickPlayer, kickPlayerName);
                        }
                    }
                }
            }
        }
    }
}
