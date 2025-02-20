package noppes.npcs.client.gui.test;

import kamkeel.npcs.controllers.SyncController;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import kamkeel.npcs.network.PacketHandler;
import noppes.npcs.controllers.*;
import noppes.npcs.controllers.data.*;
import noppes.npcs.config.ConfigMain;
import net.minecraft.nbt.NBTTagCompound;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class NoppesUtilServerNew {

    /**
     * Retrieves the player's data from the existing PlayerDataController and sends
     * a large packet to the client containing all the maps, including categories.
     */
    public static void sendPlayerData(String playerName, EntityPlayerMP player) {
        PlayerData playerdata;
        EntityPlayer pl = MinecraftServer.getServer().getConfigurationManager().func_152612_a(playerName);
        if(pl == null)
            playerdata = PlayerDataController.Instance.getDataFromUsername(playerName);
        else
            playerdata = PlayerDataController.Instance.getPlayerData(pl);

        // Assemble quest maps:
        Map<String, Integer> questActiveMap = new HashMap<>();
        Map<String, Integer> questFinishedMap = new HashMap<>();
        Map<String, Integer> questCategoriesMap = new HashMap<>();
        QuestController qc = QuestController.Instance;
        PlayerQuestData qData = playerdata.questData;
        for (Integer questId : qData.activeQuests.keySet()) {
            if(!qc.quests.containsKey(questId))
                continue;
            Quest quest = qc.quests.get(questId);
            questActiveMap.put(quest.category.title + ": " + quest.title, questId);
            if (!questCategoriesMap.containsKey(quest.category.title))
                questCategoriesMap.put(quest.category.title, quest.category.id);
        }
        for (Integer questId : qData.finishedQuests.keySet()) {
            if(!qc.quests.containsKey(questId))
                continue;
            Quest quest = qc.quests.get(questId);
            questFinishedMap.put(quest.category.title + ": " + quest.title, questId);
            if (!questCategoriesMap.containsKey(quest.category.title))
                questCategoriesMap.put(quest.category.title, quest.category.id);
        }

        // Assemble dialog maps:
        Map<String, Integer> dialogReadMap = new HashMap<>();
        Map<String, Integer> dialogCategoriesMap = new HashMap<>();
        PlayerDialogData dData = playerdata.dialogData;
        for (Integer dialogId : dData.dialogsRead) {
            if(!DialogController.Instance.dialogs.containsKey(dialogId))
                continue;
            Dialog dialog = DialogController.Instance.dialogs.get(dialogId);
            dialogReadMap.put(dialog.category.title + ": " + dialog.title, dialogId);
            if (!dialogCategoriesMap.containsKey(dialog.category.title))
                dialogCategoriesMap.put(dialog.category.title, dialog.category.id);
        }

        // Assemble transport maps:
        Map<String, Integer> transportLocationsMap = new HashMap<>();
        Map<String, Integer> transportCategoriesMap = new HashMap<>();
        PlayerTransportData tData = playerdata.transportData;
        TransportController tc = TransportController.getInstance();
        for (Integer id : tData.transports) {
            TransportLocation location = tc.getTransport(id);
            if(location == null)
                continue;
            transportLocationsMap.put(location.category.title + ": " + location.name, id);
            if (!transportCategoriesMap.containsKey(location.category.title))
                transportCategoriesMap.put(location.category.title, location.category.id);
        }

        // Assemble bank map:
        Map<String, Integer> bankMap = new HashMap<>();
        PlayerBankData bData = playerdata.bankData;
        BankController bc = BankController.getInstance();
        for (Integer bankId : bData.banks.keySet()) {
            if(!bc.banks.containsKey(bankId))
                continue;
            Bank bank = bc.banks.get(bankId);
            bankMap.put(bank.name, bankId);
        }

        // Assemble faction map:
        Map<String, Integer> factionMap = new HashMap<>();
        PlayerFactionData fData = playerdata.factionData;
        FactionController fc = FactionController.getInstance();
        for (Integer factionId : fData.factionData.keySet()) {
            if(!fc.factions.containsKey(factionId))
                continue;
            Faction faction = fc.factions.get(factionId);
            factionMap.put(faction.name + "(" + fData.getFactionPoints(factionId) + ")", factionId);
        }

        // Send the full packet with categories, active/finished data, dialogs, transports, banks, and factions.
        PlayerDataSendPacketNew packet = new PlayerDataSendPacketNew(playerName,
            questCategoriesMap, questActiveMap, questFinishedMap,
            dialogCategoriesMap, dialogReadMap,
            transportCategoriesMap, transportLocationsMap,
            bankMap, factionMap);
        PacketHandler.Instance.sendToPlayer(packet, player);
    }

    /**
     * Removes a specific data entry from the player's data.
     *
     * The tabType parameter is used as follows:
     *   0: Quest – remove from both activeQuests and finishedQuests.
     *   1: Dialog – remove from dialogsRead.
     *   2: Transport – remove from transports.
     *   3: Bank – remove from banks.
     *   4: Factions – remove from factionData.
     *
     * The selectedKey is expected to be the string representation of the entry's ID.
     */
    public static void removePlayerData(String playerName, int tabType, int value, EntityPlayerMP player) {
        EntityPlayer pl = MinecraftServer.getServer().getConfigurationManager().func_152612_a(playerName);
        PlayerData playerdata;
        if(pl == null)
            playerdata = PlayerDataController.Instance.getDataFromUsername(playerName);
        else
            playerdata = PlayerDataController.Instance.getPlayerData(pl);

        if(tabType == 0) { // Quest removal
            playerdata.questData.activeQuests.remove(value);
            playerdata.questData.finishedQuests.remove(value);
        }
        if(tabType == 1) { // Dialog removal
            playerdata.dialogData.dialogsRead.remove(value);
        }
        if(tabType == 2) { // Transport removal
            playerdata.transportData.transports.remove(value);
        }
        if(tabType == 3) { // Bank removal
            playerdata.bankData.banks.remove(value);
        }
        if(tabType == 4) { // Faction removal
            playerdata.factionData.factionData.remove(value);
        }
        playerdata.save();

        if(pl != null) {
            SyncController.syncPlayer((EntityPlayerMP) pl);
        }
        // Resend the updated data to the client.
        sendPlayerData(playerName, player);
    }
}
