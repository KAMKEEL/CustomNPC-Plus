package kamkeel.command;

import java.util.Collection;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.Server;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumQuestRepeat;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.controllers.data.QuestCategory;
import noppes.npcs.controllers.data.QuestData;

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
        
        Quest quest = QuestController.instance.quests.get(questid);
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
        
        Quest quest = QuestController.instance.quests.get(questid);
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
        Quest quest = QuestController.instance.quests.get(questid);
        if (quest == null){
        	sendError(sender, "Unknown QuestID: " + questid);
            return;
        }       
        for(PlayerData playerdata : data){  
	        playerdata.questData.activeQuests.remove(questid);
            playerdata.save();
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
        
        Quest quest = QuestController.instance.quests.get(questid);
        if (quest == null){
        	sendError(sender, String.format("Unknown QuestID"));
            return;
        }     
        
        for(PlayerData playerdata : data){  
	        playerdata.questData.activeQuests.remove(questid);
	        playerdata.questData.finishedQuests.remove(questid);
            playerdata.save();
            sendResult(sender, String.format("Removed Quest \u00A7e%d\u00A77 for Player '\u00A7b%s\u00A77'", questid, playerdata.playername));
        }
    }

    @SubCommand(
            desc = "Find quest id number by its name",
            usage = "<quest name>"
    )
    public void id(ICommandSender sender, String args[]) throws CommandException {
        if(args.length == 0){
            sendError(sender, "Please provide a name for the quest");
            return;
        }

        String catName = String.join(" ", args).toLowerCase();
        final Collection<Quest> quests = QuestController.instance.quests.values();
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
            desc="reload quests from disk",
            permission = 4
    )      
    public void reload(ICommandSender sender, String args[]){
    	new QuestController();
        QuestController.instance.load();
        sendResult(sender, "Quests Reloaded");
    }
}

