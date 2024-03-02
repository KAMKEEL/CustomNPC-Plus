package noppes.npcs.controllers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.Server;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumQuestCompletion;
import noppes.npcs.controllers.data.Party;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.Quest;

import java.util.HashMap;
import java.util.UUID;
import java.util.Vector;

import static noppes.npcs.PacketHandlerServer.sendInviteData;

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
                PlayerData playerData;
                if(player != null){
                    playerData = PlayerDataController.Instance.getPlayerData(player);
                }
                else {
                    playerData = PlayerDataController.Instance.getPlayerDataCache(uuid.toString());
                }
                if(playerData != null){
                    playerData.partyUUID = null;
                    if(player != null){
                        sendInviteData((EntityPlayerMP) player);
                        Server.sendData((EntityPlayerMP) player, EnumPacketClient.PARTY_MESSAGE,  "party.disbandAlert");
                        Server.sendData((EntityPlayerMP) player, EnumPacketClient.CHAT, "\u00A7c", "party.disbandMessage", "!");
                    }
                }
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
                Server.sendData((EntityPlayerMP) playerMP, EnumPacketClient.CHAT, "\u00A7e", kickPlayer.getCommandSenderName(), " \u00A74", "party.kickOtherChat", "!");
            }
        }
        Server.sendData((EntityPlayerMP) kickPlayer, EnumPacketClient.PARTY_MESSAGE, "party.kickYouAlert", "");
        Server.sendData((EntityPlayerMP) kickPlayer, EnumPacketClient.CHAT, "\u00A74", "party.kickYouChat", "!");
    }

    public void sendLeavingMessages(Party party, EntityPlayer leavingPlayer){
        if(leavingPlayer == null || party == null)
            return;

        for(String name : party.getPlayerNames()){
            EntityPlayer playerMP = NoppesUtilServer.getPlayerByName(name);
            if(playerMP != null){
                Server.sendData((EntityPlayerMP) playerMP, EnumPacketClient.PARTY_MESSAGE,  "party.leaveOtherAlert", leavingPlayer.getCommandSenderName());
                Server.sendData((EntityPlayerMP) playerMP, EnumPacketClient.CHAT, "\u00A7e", leavingPlayer.getCommandSenderName(), " \u00A7c", "party.leaveOtherChat", "!");
            }
        }
        Server.sendData((EntityPlayerMP) leavingPlayer, EnumPacketClient.PARTY_MESSAGE, "party.leaveYouAlert", "");
        Server.sendData((EntityPlayerMP) leavingPlayer, EnumPacketClient.CHAT, "\u00A7c", "party.leaveYouChat", "!");
    }

    public void pingPartyUpdate(Party party){
        if(party == null)
            return;

        NBTTagCompound compound = party.writeToNBT();
        if (party.getQuest() != null) {
            Quest quest = (Quest) party.getQuest();
            Vector<String> vector = quest.questInterface.getPartyQuestLogStatus(party);
            NBTTagList list = new NBTTagList();
            for (String s : vector) {
                list.appendTag(new NBTTagString(s));
            }
            compound.setTag("QuestProgress", list);
            if(quest.completion == EnumQuestCompletion.Npc && quest.questInterface.isPartyCompleted(party)) {
                compound.setString("QuestCompleteWith", quest.completerNpc);
            }
        }

        for(String name : party.getPlayerNames()){
            EntityPlayer playerMP = NoppesUtilServer.getPlayerByName(name);
            if(playerMP != null){
                PlayerData playerData = PlayerDataController.Instance.getPlayerData(playerMP);
                if(playerData != null){
                    Server.sendData((EntityPlayerMP) playerMP, EnumPacketClient.PARTY_DATA, compound);
                }
            }
        }
    }
}
