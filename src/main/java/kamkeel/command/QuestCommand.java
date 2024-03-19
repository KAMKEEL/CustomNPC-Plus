package kamkeel.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import noppes.npcs.Server;
import noppes.npcs.api.handler.data.IQuestObjective;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumQuestCompletion;
import noppes.npcs.constants.EnumQuestRepeat;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.SyncController;
import noppes.npcs.controllers.data.Party;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.controllers.data.QuestData;
import noppes.npcs.util.ValueUtil;

import java.util.Collection;
import java.util.List;

public class QuestCommand extends CommandKamkeelBase {

	@Override
	public String getCommandName() {
		return "quest";
	}

	@Override
	public String getDescription() {
		return "Quest operations";
	}

    @SubCommand(
            desc = "Start a quest",
            usage = "<player> <quest>"
    )
    public void start(ICommandSender sender, String[] args) throws CommandException {
        String playername = args[0];
        int questid;
        try {
        	questid = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            sendError(sender, "QuestID must be an integer: " + args[1]);
            return;
        }

        List<PlayerData> data = PlayerDataController.Instance.getPlayersData(sender, playername);

        if (data.isEmpty()) {
            sendError(sender, "Unknown player: " + playername);
            return;
        }

        Quest quest = QuestController.Instance.quests.get(questid);
        if (quest == null){
        	sendError(sender, "Unknown QuestID: " + questid);
            return;
        }

        for(PlayerData playerdata : data){
	        QuestData questdata = new QuestData(quest);
	        playerdata.questData.activeQuests.put(questid, questdata);
            playerdata.save();
            if(playerdata.player != null && questdata.sendAlerts){
                Server.sendData((EntityPlayerMP)playerdata.player, EnumPacketClient.MESSAGE, "quest.newquest", quest.title);
                Server.sendData((EntityPlayerMP)playerdata.player, EnumPacketClient.CHAT, "quest.newquest", ": ", quest.title);
            }
            playerdata.updateClient = true;
            sendResult(sender, String.format("Started Quest \u00A7e%d\u00A77 for Player '\u00A7b%s\u00A77'", questid, playerdata.playername));
        }
    }

    @SubCommand(
            desc = "Finish a quest",
            usage = "<player> <quest>"
    )
    public void finish(ICommandSender sender, String args[]) throws CommandException{
        String playername=args[0];
        int questid;
        try {
        	questid = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException ex) {
        	sendError(sender, "QuestID must be an integer: " + args[1]);
            return;
        }

        List<PlayerData> data = PlayerDataController.Instance.getPlayersData(sender, playername);
        if (data.isEmpty()) {
        	sendError(sender, String.format("Unknown player '%s'", playername));
            return;
        }

        Quest quest = QuestController.Instance.quests.get(questid);
        if (quest == null){
        	sendError(sender, "Unknown QuestID: " + questid);
            return;
        }
        for(PlayerData playerdata : data){
            if(playerdata.questData.activeQuests.containsKey(questid)){
                playerdata.questData.activeQuests.remove(questid);
            }

            if(quest.repeat == EnumQuestRepeat.RLDAILY || quest.repeat == EnumQuestRepeat.RLWEEKLY)
                playerdata.questData.finishedQuests.put(quest.id, System.currentTimeMillis());
            else
                playerdata.questData.finishedQuests.put(quest.id, sender.getEntityWorld().getTotalWorldTime());

            playerdata.save();
            if(playerdata.player != null){
                Server.sendData((EntityPlayerMP)playerdata.player, EnumPacketClient.MESSAGE, "quest.completed", quest.title);
                Server.sendData((EntityPlayerMP)playerdata.player, EnumPacketClient.CHAT, "quest.completed", ": ", quest.title);
            }
            playerdata.updateClient = true;
            sendResult(sender, String.format("Finished Quest \u00A7e%d\u00A77 for Player '\u00A7b%s\u00A77'", questid, playerdata.playername));
        }
    }

    @SubCommand(
            desc = "Stop a started quest",
            usage = "<player> <quest>"
    )
    public void stop(ICommandSender sender, String[] args) throws CommandException {
        String playername=args[0];
        int questid;
        try {
        	questid = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
        	sendError(sender, "QuestID must be an integer: " + args[1]);
            return;
        }
        List<PlayerData> data = PlayerDataController.Instance.getPlayersData(sender, playername);
        if (data.isEmpty()) {
        	sendError(sender, String.format("Unknown player '%s'", playername));
            return;
        }
        Quest quest = QuestController.Instance.quests.get(questid);
        if (quest == null){
        	sendError(sender, "Unknown QuestID: " + questid);
            return;
        }
        for(PlayerData playerdata : data){
	        playerdata.questData.activeQuests.remove(questid);
            playerdata.save();
            playerdata.updateClient = true;
            sendResult(sender, String.format("Stopped Quest \u00A7e%d\u00A77 for Player '\u00A7b%s\u00A77'", questid, playerdata.playername));
        }
    }

    @SubCommand(
            desc = "Removes a quest from finished and active quests",
            usage = "<player> <quest>"
    )
    public void remove(ICommandSender sender, String[] args) throws CommandException {
        String playername=args[0];
        int questid;
        try {
        	questid = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            sendError(sender, "QuestID must be an integer: " + args[1]);
            return;
        }

        List<PlayerData> data = PlayerDataController.Instance.getPlayersData(sender, playername);
        if (data.isEmpty()) {
            sendError(sender, String.format("Unknown player '%s'", playername));
            return;
        }

        Quest quest = QuestController.Instance.quests.get(questid);
        if (quest == null){
        	sendError(sender, "Unknown QuestID");
            return;
        }

        for(PlayerData playerdata : data){
	        playerdata.questData.activeQuests.remove(questid);
	        playerdata.questData.finishedQuests.remove(questid);
            playerdata.save();
            playerdata.updateClient = true;
            sendResult(sender, String.format("Removed Quest \u00A7e%d\u00A77 for Player '\u00A7b%s\u00A77'", questid, playerdata.playername));
        }
    }

    @SubCommand(
        desc= "Get/Set objectives for quests progress",
        usage = "<player> <quest> [objective] [value]"
    )
    public void objective(ICommandSender sender, String[] args) throws CommandException {
        EntityPlayer player = CommandBase.getPlayer(sender, args[0]);
        int questid;
        try {
            questid = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            sendError(sender, "QuestID must be an integer: " + args[1]);
            return;
        }

        Quest quest = QuestController.Instance.quests.get(questid);
        if (quest == null){
            sendError(sender, "Unknown QuestID");
            return;
        }

        PlayerData data = PlayerDataController.Instance.getPlayerData(player);
        if (data == null){
            sendError(sender, "No PlayerData found for:" + player);
            return;
        }

        Party party = data.getPlayerParty();
        boolean partyValid = party != null && party.getQuest() != null && party.getQuest().getId() == quest.id;

        if(!partyValid && !data.questData.activeQuests.containsKey(quest.id)) {
            sendError(sender, "Player does not have quest active");
            return;
        }

        IQuestObjective[] objectives;
        if(partyValid)
            objectives = quest.questInterface.getPartyObjectives(party);
        else
            objectives = quest.questInterface.getObjectives(player);

        if(args.length <= 2) {
            if(partyValid)
                sendResult(sender, "For Party: ");
            for(IQuestObjective ob : objectives) {
                sendResult(sender, ob.getText());
            }
            return;
        }

        int objective;
        try {
            objective = Integer.parseInt(args[2]);
        } catch (NumberFormatException ex) {
            sendError(sender, "Objective must be an integer. Most often 0, 1 or 2");
            return;
        }

        if(objective < 0 || objective >= objectives.length) {
            sendError(sender, "Invalid objective number was given");
            return;
        }

        if(args.length <= 3) {
            sendResult(sender, objectives[objective].getText());
            return;
        }

        IQuestObjective object = objectives[objective];
        String s = args[3];
        int value;
        try {
            value = Integer.parseInt(args[3]);
        } catch (NumberFormatException ex) {
            sendError(sender, "Value must be an integer");
            return;
        }

        if(s.startsWith("-") || s.startsWith("+")) {
            value = ValueUtil.CorrectInt(object.getProgress() + value, 0, object.getMaxProgress());
        }

        if(partyValid)
            object.setPlayerProgress(player.getCommandSenderName(), value);
        else
            object.setProgress(value);
        sendResult(sender, "Successfully updated progress");
    }

    @SubCommand(
            desc = "Find quest id number by its name",
            usage = "<questName>"
    )
    public void id(ICommandSender sender, String args[]) throws CommandException {
        if(args.length == 0){
            sendError(sender, "Please provide a name for the quest");
            return;
        }

        String catName = String.join(" ", args).toLowerCase();
        final Collection<Quest> quests = QuestController.Instance.quests.values();
        int count = 0;
        for(Quest quest : quests){
            if(quest.getName().toLowerCase().contains(catName)){
                sendResult(sender, String.format("Quest \u00A7e%d\u00A77 - \u00A7c'%s'", quest.id, quest.getName()));
                count++;
            }
        }
        if(count == 0){
            sendResult(sender, String.format("No Quest found with name: \u00A7c'%s'", catName));
        }
    }

    @SubCommand(
            desc = "Find prerequisite quests for an id",
            usage = "<questId>"
    )
    public void prereq(ICommandSender sender, String args[]) throws CommandException {
        if(args.length == 0){
            sendError(sender, "Please provide an id for the quest");
            return;
        }

        int questid;
        try {
            questid = Integer.parseInt(args[0]);
        } catch (NumberFormatException ex) {
            sendError(sender, "QuestID must be an integer: " + args[1]);
            return;
        }

        Quest quest = QuestController.Instance.quests.get(questid);
        if (quest == null){
            sendError(sender, "Unknown QuestID");
            return;
        }
        final Collection<Quest> quests = QuestController.Instance.quests.values();
        sendResult(sender, "Prerequisites:");
        sendResult(sender, "--------------------");
        boolean foundOne = false;
        for(Quest allQuest : quests){
            if(allQuest.nextQuestid == questid){
                foundOne = true;
                sendResult(sender, String.format("Quest %d: \u00A7a'%s'", allQuest.id, allQuest.title));
            }
        }

        if(!foundOne){
            sendResult(sender, String.format("No Prerequisites found for Quest \u00A7c%d", questid));
        }
        sendResult(sender, "--------------------");
    }

    @SubCommand(
            desc = "Quick info on a quest",
            usage = "<questId>"
    )
    public void info(ICommandSender sender, String args[]) throws CommandException {
        if(args.length == 0){
            sendError(sender, "Please provide an id for the quest");
            return;
        }

        int questid;
        try {
            questid = Integer.parseInt(args[0]);
        } catch (NumberFormatException ex) {
            sendError(sender, "QuestID must be an integer: " + args[1]);
            return;
        }

        Quest quest = QuestController.Instance.quests.get(questid);
        if (quest == null){
            sendError(sender, "Unknown QuestID");
            return;
        }

        sendResult(sender, "--------------------");
        sendResult(sender, String.format("\u00A7e%d\u00A77: \u00A7a%s", quest.id, quest.title));
        sendResult(sender, String.format("Category: \u00A7b%s", quest.category.getName()));
        sendResult(sender, String.format("Type: \u00A76%s", quest.type.toString()));
        sendResult(sender, String.format("Repeatable: \u00A76%s", quest.repeat.toString()));
        sendResult(sender, String.format("Complete: \u00A76%s", quest.completion.toString()));
        if(quest.completion == EnumQuestCompletion.Npc){
            sendResult(sender, String.format("NPC: \u00A76%s", quest.completerNpc));
        }
        if(quest.nextQuestid != -1){
            sendResult(sender, String.format("Next Quest ID: \u00A7c%d", quest.nextQuestid));
        }
        sendResult(sender, "--------------------");
    }

    @SubCommand(
            desc="reload quests from disk",
            permission = 4
    )
    public void reload(ICommandSender sender, String args[]){
    	new QuestController().load();
        SyncController.syncAllQuests();
        sendResult(sender, "Quests Reloaded");
    }
}

