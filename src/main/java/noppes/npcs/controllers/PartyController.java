package noppes.npcs.controllers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.Server;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.controllers.data.Party;
import noppes.npcs.controllers.data.PlayerData;

import java.util.HashMap;
import java.util.UUID;

public class PartyController {
    private static PartyController Instance;

    private HashMap<UUID, Party> parties = new HashMap<>();

    private PartyController() {
    }

    public static PartyController Instance() {
        if (Instance == null) {
            Instance = new PartyController();
        }
        return Instance;
    }

    public Party createParty() {
        Party party = new Party();
        this.parties.put(party.getPartyUUID(), party);
        return party;
    }

    public Party getParty(UUID partyUUID) {
        return this.parties.get(partyUUID);
    }

    public void disbandParty(UUID partyUUID) {
        Party party = this.parties.get(partyUUID);
        if (party != null) {
            for (UUID uuid: party.getPlayerUUIDs()) {
                EntityPlayer player = NoppesUtilServer.getPlayer(uuid);
                PlayerData playerData = PlayerDataController.Instance.getPlayerData(player);
                playerData.partyUUID = null;
            }
            this.parties.remove(partyUUID);
        }
    }

    public void sendKickMessages(Party party, EntityPlayer kickPlayer){
        if(kickPlayer == null || party == null)
            return;

        for(String name : party.getPlayerNames()){
            EntityPlayer playerMP = NoppesUtilServer.getPlayerByName(name);
            if(playerMP != null){
                Server.sendData((EntityPlayerMP) playerMP, EnumPacketClient.PARTY_MESSAGE,  "party.kickOtherAlert", kickPlayer.getCommandSenderName());
                Server.sendData((EntityPlayerMP) playerMP, EnumPacketClient.CHAT, "\u00A7c", kickPlayer.getCommandSenderName(), " \u00A7e", "party.kickOtherChat", "!");
            }
        }
        Server.sendData((EntityPlayerMP) kickPlayer, EnumPacketClient.PARTY_MESSAGE, "party.kickYouAlert", "");
        Server.sendData((EntityPlayerMP) kickPlayer, EnumPacketClient.CHAT, "\u00A7c", "party.kickYouChat", "!");
    }

    public void sendLeavingMessages(Party party, EntityPlayer leavingPlayer){
        if(leavingPlayer == null || party == null)
            return;

        for(String name : party.getPlayerNames()){
            EntityPlayer playerMP = NoppesUtilServer.getPlayerByName(name);
            if(playerMP != null){
                Server.sendData((EntityPlayerMP) playerMP, EnumPacketClient.PARTY_MESSAGE,  "party.leaveOtherAlert", leavingPlayer.getCommandSenderName());
                Server.sendData((EntityPlayerMP) playerMP, EnumPacketClient.CHAT, "\u00A7a", leavingPlayer.getCommandSenderName(), " \u00A7e", "party.leaveOtherChat", "!");
            }
        }
        Server.sendData((EntityPlayerMP) leavingPlayer, EnumPacketClient.PARTY_MESSAGE, "party.leaveYouAlert", "");
        Server.sendData((EntityPlayerMP) leavingPlayer, EnumPacketClient.CHAT, "\u00A7a", "party.leaveYouChat", "!");
    }
}
