package noppes.npcs.controllers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import noppes.npcs.EventHooks;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.Server;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumQuestCompletion;
import noppes.npcs.constants.EnumQuestType;
import noppes.npcs.controllers.data.Party;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.controllers.data.QuestData;
import noppes.npcs.quests.QuestInterface;
import noppes.npcs.quests.QuestItem;
import noppes.npcs.scripted.event.PartyEvent;

import java.util.HashMap;
import java.util.UUID;
import java.util.Vector;

import static noppes.npcs.PacketHandlerServer.sendInviteData;

public class PartyController {
    private static PartyController Instance;

    private HashMap<UUID, Party> parties = new HashMap<>();

    private PartyController() {}

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
            PartyEvent.PartyDisbandEvent partyEvent = new PartyEvent.PartyDisbandEvent(party, party.getQuest());
            EventHooks.onPartyDisband(partyEvent);
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

    public void sendKickMessages(Party party, EntityPlayer kickPlayer, String kickPlayerName){
        if(party == null)
            return;

        for(String name : party.getPlayerNames()){
            EntityPlayer playerMP = NoppesUtilServer.getPlayerByName(name);
            if(playerMP != null){
                Server.sendData((EntityPlayerMP) playerMP, EnumPacketClient.PARTY_MESSAGE,  "party.kickOtherAlert", kickPlayerName);
                Server.sendData((EntityPlayerMP) playerMP, EnumPacketClient.CHAT, "\u00A7e", kickPlayerName, " \u00A74", "party.kickOtherChat", "!");
            }
        }

        if(kickPlayer != null){
            Server.sendData((EntityPlayerMP) kickPlayer, EnumPacketClient.PARTY_MESSAGE, "party.kickYouAlert", "");
            Server.sendData((EntityPlayerMP) kickPlayer, EnumPacketClient.CHAT, "\u00A74", "party.kickYouChat", "!");
        }
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
            compound.setString("QuestName", quest.getCategory().getName() + ":" + quest.getName());
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

    public void pingPartyQuestObjectiveUpdate(Party party){
        if(party == null)
            return;

        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("QuestPing", 0);
        if (party.getQuest() != null) {
            Quest quest = (Quest) party.getQuest();
            Vector<String> vector = quest.questInterface.getPartyQuestLogStatus(party);
            NBTTagList list = new NBTTagList();
            compound.setString("QuestName", quest.getCategory().getName() + ":" + quest.getName());
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

    public void sendQuestChat(Party party, String... chatAlerts) {
        if (party == null)
            return;

        for (String name : party.getPlayerNames()) {
            EntityPlayer playerMP = NoppesUtilServer.getPlayerByName(name);
            if (playerMP != null) {
                Object[] args = new Object[2 + chatAlerts.length];
                args[0] = "\u00A7a";

                System.arraycopy(chatAlerts, 0, args, 1, chatAlerts.length);
                args[args.length - 1] = "!"; // Add "!" at the end

                // Pass args to Server.sendData using spread operator
                Server.sendData((EntityPlayerMP) playerMP, EnumPacketClient.CHAT, args);
            }
        }
    }

    public boolean checkQuestCompletion(Party party, EnumQuestType type) {
        QuestData questData = party.getQuestData();
        if(questData != null){
            if(questData.quest.type == type || type == null){
                QuestInterface inter =  questData.quest.questInterface;
                if(inter.isPartyCompleted(party)){
                    if((!questData.isCompleted && questData.quest.completion == EnumQuestCompletion.Npc) || questData.quest.instantPartyComplete(party)){
                        questData.isCompleted = true;
                        if (questData.quest.completion == EnumQuestCompletion.Npc) {
                            EventHooks.onPartyFinished(party, questData.quest);
                            PartyController.Instance().sendQuestChat(party, "party.turnedInChat");
                        } else {
                            PartyController.Instance().sendQuestChat(party, "party.completeChat");
                        }
                        PartyController.Instance().pingPartyUpdate(party);
                    }
                } else {
                    questData.isCompleted = false;
                }

                for (String name : party.getPlayerNames()) {
                    EntityPlayer playerMP = NoppesUtilServer.getPlayerByName(name);
                    if (playerMP != null) {
                        PlayerData playerData = PlayerDataController.Instance.getPlayerData(playerMP);
                        if(playerData != null){
                            if (playerData.questData.getTrackedQuest() != null && questData.quest.getId() == playerData.questData.getTrackedQuest().getId()) {
                                NoppesUtilPlayer.sendPartyTrackedQuestData((EntityPlayerMP) playerMP, party);
                            }
                        }
                    }
                }
            }
        }

        QuestItem.pickedUpParty = null;
        QuestItem.pickedUpPlayer = null;
        return true;
    }
}
