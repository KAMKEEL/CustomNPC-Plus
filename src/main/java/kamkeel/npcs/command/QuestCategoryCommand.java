package kamkeel.npcs.command;

import kamkeel.npcs.network.packets.data.AchievementPacket;
import kamkeel.npcs.network.packets.data.ChatAlertPacket;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.constants.EnumQuestRepeat;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.controllers.data.QuestCategory;
import noppes.npcs.controllers.data.QuestData;

import java.util.Collection;
import java.util.List;

public class QuestCategoryCommand extends CommandKamkeelBase {

	@Override
	public String getCommandName() {
		return "questcat";
	}

	@Override
	public String getDescription() {
		return "Quest Category operations";
	}

    @SubCommand(
            desc = "Find quest category id number by its name",
            usage = "<quest cat name>"
    )
    public void id(ICommandSender sender, String args[]) throws CommandException {
        if(args.length == 0){
            sendError(sender, "Please provide a name for the quest category");
            return;
        }

        String catName = String.join(" ", args).toLowerCase();
        final Collection<QuestCategory> questCats = QuestController.Instance.categories.values();
        int count = 0;
        for(QuestCategory cat : questCats){
            if(cat.getName().toLowerCase().contains(catName)){
                sendResult(sender, String.format("Quest Cat \u00A7e%d\u00A77 - \u00A7c'%s'", cat.id, cat.getName()));
                count++;
            }
        }
        if(count == 0){
            sendResult(sender, String.format("No Quest Cat found with name: \u00A7c'%s'", catName));
        }
    }


    @SubCommand(
            desc = "Finish a quest category for a player",
            usage = "<player> <questcatid>"
    )
    public void finish(ICommandSender sender, String args[]) throws CommandException{
        String playername=args[0];
        int questcatid;
        try {
        	questcatid = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException ex) {
        	sendError(sender, "QuestCatID must be an integer: " + args[1]);
            return;
        }

        List<PlayerData> data = PlayerDataController.Instance.getPlayersData(sender, playername);
        if (data.isEmpty()) {
        	sendError(sender, String.format("Unknown player '%s'", playername));
            return;
        }

        QuestCategory questCategory = QuestController.Instance.categories.get(questcatid);
        if (questCategory == null){
        	sendError(sender, "Unknown QuestCatID: " + questcatid);
            return;
        }

        int count = 0;
        for(PlayerData playerdata : data){
            for(Quest quest : questCategory.quests.values()){
                if(playerdata.questData.activeQuests.containsKey(quest.id)){
                    playerdata.questData.activeQuests.remove(quest.id);
                }

                if(quest.repeat == EnumQuestRepeat.RLDAILY || quest.repeat == EnumQuestRepeat.RLWEEKLY)
                    playerdata.questData.finishedQuests.put(quest.id, System.currentTimeMillis());
                else
                    playerdata.questData.finishedQuests.put(quest.id, sender.getEntityWorld().getTotalWorldTime());

                if(playerdata.player != null){
                    AchievementPacket.sendAchievement((EntityPlayerMP) playerdata.player, false, "quest.completed", quest.title);
                    ChatAlertPacket.sendChatAlert((EntityPlayerMP) playerdata.player, "quest.completed", ": ", quest.title);
                }
                count++;
            }

            playerdata.save();
            playerdata.updateClient = true;
            sendResult(sender, String.format("Completed Quest Cat \u00A7c'%s' \u00A7e%d\u00A77 for Player '\u00A7b%s\u00A77'", questCategory.getName(), questcatid, playerdata.playername));
            sendResult(sender, String.format("Completed a total of \u00A7b%d \u00A77quests", count));
        }
    }

    @SubCommand(
            desc = "Stop a quest category for a players active quests",
            usage = "<player> <questcatid>"
    )
    public void stop(ICommandSender sender, String args[]) throws CommandException{
        String playername=args[0];
        int questcatid;
        try {
            questcatid = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException ex) {
            sendError(sender, "QuestCatID must be an integer: " + args[1]);
            return;
        }

        List<PlayerData> data = PlayerDataController.Instance.getPlayersData(sender, playername);
        if (data.isEmpty()) {
            sendError(sender, String.format("Unknown player '%s'", playername));
            return;
        }

        QuestCategory questCategory = QuestController.Instance.categories.get(questcatid);
        if (questCategory == null){
            sendError(sender, "Unknown QuestCatID: " + questcatid);
            return;
        }

        int count = 0;
        for(PlayerData playerdata : data){
            for(Quest quest : questCategory.quests.values()){
                if(playerdata.questData.activeQuests.containsKey(quest.id)){
                    playerdata.questData.activeQuests.remove(quest.id);
                    count++;
                }
            }

            playerdata.save();
            playerdata.updateClient = true;
            sendResult(sender, String.format("Stopped Quest Cat \u00A7c'%s' \u00A7e%d\u00A77 for Player '\u00A7b%s\u00A77'", questCategory.getName(), questcatid, playerdata.playername));
            sendResult(sender, String.format("Stopped a total of \u00A7b%d \u00A77quests", count));
        }
    }

    @SubCommand(
            desc = "Start a quest category for a player",
            usage = "<player> <questcatid>"
    )
    public void start(ICommandSender sender, String args[]) throws CommandException{
        String playername=args[0];
        int questcatid;
        try {
            questcatid = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException ex) {
            sendError(sender, "QuestCatID must be an integer: " + args[1]);
            return;
        }

        List<PlayerData> data = PlayerDataController.Instance.getPlayersData(sender, playername);
        if (data.isEmpty()) {
            sendError(sender, String.format("Unknown player '%s'", playername));
            return;
        }

        QuestCategory questCategory = QuestController.Instance.categories.get(questcatid);
        if (questCategory == null){
            sendError(sender, "Unknown QuestCatID: " + questcatid);
            return;
        }

        int count = 0;
        for(PlayerData playerdata : data){
            for(Quest quest : questCategory.quests.values()){
                if(!playerdata.questData.activeQuests.containsKey(quest.id)){
                    QuestData questData = new QuestData(quest);
                    playerdata.questData.activeQuests.put(quest.id, questData);
                    count++;

                    if(playerdata.player != null && questData.sendAlerts){
                        AchievementPacket.sendAchievement((EntityPlayerMP) playerdata.player, false, "quest.newquest", quest.title);
                        ChatAlertPacket.sendChatAlert((EntityPlayerMP) playerdata.player, "quest.newquest", ": ", quest.title);
                    }
                }
            }

            playerdata.save();
            playerdata.updateClient = true;
            sendResult(sender, String.format("Started Quest Cat \u00A7c'%s' \u00A7e%d\u00A77 for Player '\u00A7b%s\u00A77'", questCategory.getName(), questcatid, playerdata.playername));
            sendResult(sender, String.format("Started a total of \u00A7b%d \u00A77quests", count));
        }
    }


    @SubCommand(
            desc = "Remove a quest cat from active/finished",
            usage = "<player> <questcatid>"
    )
    public void remove(ICommandSender sender, String args[]) throws CommandException{
        String playername=args[0];
        int questcatid;
        try {
            questcatid = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException ex) {
            sendError(sender, "QuestCatID must be an integer: " + args[1]);
            return;
        }

        List<PlayerData> data = PlayerDataController.Instance.getPlayersData(sender, playername);
        if (data.isEmpty()) {
            sendError(sender, String.format("Unknown player '%s'", playername));
            return;
        }

        QuestCategory questCategory = QuestController.Instance.categories.get(questcatid);
        if (questCategory == null){
            sendError(sender, "Unknown QuestCatID: " + questcatid);
            return;
        }

        int count = 0;
        for(PlayerData playerdata : data){
            for(Quest quest : questCategory.quests.values()){
                playerdata.questData.activeQuests.remove(quest.id);
                playerdata.questData.finishedQuests.remove(quest.id);
                count++;
            }

            playerdata.save();
            playerdata.updateClient = true;
            sendResult(sender, String.format("Cleared Quest Cat \u00A7c'%s' \u00A7e%d\u00A77 for Player '\u00A7b%s\u00A77'", questCategory.getName(), questcatid, playerdata.playername));
            sendResult(sender, String.format("Cleared a total of \u00A7b%d \u00A77quests", count));
        }
    }
}

