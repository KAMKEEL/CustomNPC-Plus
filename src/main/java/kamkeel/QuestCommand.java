package kamkeel;

import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.Server;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.Quest;
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
            throw new CommandException("QuestID must be an integer: " + args[1]);
        }
        
        List<PlayerData> data = PlayerDataController.instance.getPlayersData(sender, playername);
        
        if (data.isEmpty()) {
            throw new CommandException("Unknown player: " + playername);
        }
        
        Quest quest = QuestController.instance.quests.get(questid);
        if (quest == null){
        	throw new CommandException("Unknown QuestID: " + questid);
        }
        
        for(PlayerData playerdata : data){  
	        QuestData questdata = new QuestData(quest);
	        playerdata.questData.activeQuests.put(questid, questdata);
            playerdata.savePlayerDataOnFile();
            if(playerdata.player != null){
                Server.sendData((EntityPlayerMP)playerdata.player, EnumPacketClient.MESSAGE, "quest.newquest", quest.title);
                Server.sendData((EntityPlayerMP)playerdata.player, EnumPacketClient.CHAT, "quest.newquest", ": ", quest.title);
            }
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
        	throw new CommandException("QuestID must be an integer: " + args[1]);
        }
        
        List<PlayerData> data = PlayerDataController.instance.getPlayersData(sender, playername);
        if (data.isEmpty()) {
        	throw new CommandException(String.format("Unknow player '%s'", playername));
        }
        
        Quest quest = QuestController.instance.quests.get(questid);
        if (quest == null){
        	throw new CommandException("Unknown QuestID: " + questid);
        }             
        for(PlayerData playerdata : data){
            if(playerdata.questData.activeQuests.containsKey(questid)){
                playerdata.questData.activeQuests.remove(questid);
            }
            
	        playerdata.questData.finishedQuests.put(questid, System.currentTimeMillis());
            playerdata.savePlayerDataOnFile();
            if(playerdata.player != null){
                Server.sendData((EntityPlayerMP)playerdata.player, EnumPacketClient.MESSAGE, "quest.completed", quest.title);
                Server.sendData((EntityPlayerMP)playerdata.player, EnumPacketClient.CHAT, "quest.completed", ": ", quest.title);
            }
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
        	throw new CommandException("QuestID must be an integer: " + args[1]);
        }
        List<PlayerData> data = PlayerDataController.instance.getPlayersData(sender, playername);
        if (data.isEmpty()) {
        	throw new CommandException(String.format("Unknow player '%s'", playername));
        }
        Quest quest = QuestController.instance.quests.get(questid);
        if (quest == null){
        	throw new CommandException("Unknown QuestID: " + questid);
        }       
        for(PlayerData playerdata : data){  
	        playerdata.questData.activeQuests.remove(questid);
            playerdata.savePlayerDataOnFile();
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
        	throw new CommandException("QuestID must be an integer: " + args[1]);
        }
        
        List<PlayerData> data = PlayerDataController.instance.getPlayersData(sender, playername);
        if (data.isEmpty()) {
        	throw new CommandException(String.format("Unknown player '%s'", playername));
        }
        
        Quest quest = QuestController.instance.quests.get(questid);
        if (quest == null){
        	throw new CommandException(String.format("Unknown QuestID"));
        }     
        
        for(PlayerData playerdata : data){  
	        playerdata.questData.activeQuests.remove(questid);
	        playerdata.questData.finishedQuests.remove(questid);
            playerdata.savePlayerDataOnFile();
        }
    }
    
    @SubCommand(
            desc="reload quests from disk",
            permission = 4
    )      
    public void reload(ICommandSender sender, String args[]){
    	new DialogController();
    }
}

