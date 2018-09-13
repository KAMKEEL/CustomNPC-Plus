package foxz.command;

import static foxz.utils.Utils.getOnlinePlayer;

import java.util.List;

import net.minecraft.command.PlayerSelector;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.Server;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.PlayerData;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.Quest;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.QuestData;
import foxz.commandhelper.ChMcLogger;
import foxz.commandhelper.annotations.Command;
import foxz.commandhelper.annotations.SubCommand;
import foxz.commandhelper.permissions.OpOnly;
import foxz.commandhelper.permissions.ParamCheck;

@Command(
        name = "quest",
        usage = "help",
        desc = "Quest operations"
)
public class CmdQuest extends ChMcLogger{

    public CmdQuest(Object sender) {
        super(sender);
    }

    @SubCommand(
            desc = "Start a quest",
            usage = "<player> <quest>",
            permissions = {OpOnly.class, ParamCheck.class}
    )
    public Boolean start(String[] args) {
        String playername=args[0];
        int questid;
        try {
        	questid = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            sendmessage("QuestID must be an integer");
            return false;
        }
        List<PlayerData> data = getPlayersData(playername);
        if (data.isEmpty()) {
            sendmessage(String.format("Unknow player '%s'", playername));
            return false;
        }
        Quest quest = QuestController.instance.quests.get(questid);
        if (quest == null){
            sendmessage("Unknown QuestID");
            return false;
        }
        for(PlayerData playerdata : data){  
	        if(playerdata.questData.activeQuests.containsKey(questid))
	        	continue;
	        QuestData questdata = new QuestData(quest);    
	        playerdata.questData.activeQuests.put(questid, questdata);
	        playerdata.saveNBTData(null);
	        if(playerdata.player != null){
				Server.sendData((EntityPlayerMP)playerdata.player, EnumPacketClient.MESSAGE, "quest.newquest", quest.title);
				Server.sendData((EntityPlayerMP)playerdata.player, EnumPacketClient.CHAT, "quest.newquest", ": ", quest.title);
	        }
        }
        return true;
    }
    
    @SubCommand(
            desc = "Finish a quest",
            usage = "<player> <quest>",
            permissions = {OpOnly.class, ParamCheck.class}
    )
    public Boolean finish(String args[]){
        String playername=args[0];
        int questid;
        try {
        	questid = Integer.parseInt(args[1]);
        } 
        catch (NumberFormatException ex) {
            sendmessage("QuestID must be an integer");
            return false;
        }
        List<PlayerData> data = getPlayersData(playername);
        if (data.isEmpty()) {
            sendmessage(String.format("Unknow player '%s'", playername));
            return false;
        }
        
        Quest quest = QuestController.instance.quests.get(questid);
        if (quest == null){
            sendmessage("Unknown QuestID");
            return false;
        }             
        for(PlayerData playerdata : data){  
	        playerdata.questData.finishedQuests.put(questid, System.currentTimeMillis());    
	        playerdata.saveNBTData(null); 
        }
        return true;
    }

    @SubCommand(
            desc = "Stop a started quest",
            usage = "<player> <quest>",
            permissions = {OpOnly.class, ParamCheck.class}
    )
    public Boolean stop(String[] args) {
        String playername=args[0];
        int questid;
        try {
        	questid = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            sendmessage("QuestID must be an integer");
            return false;
        }
        List<PlayerData> data = getPlayersData(playername);
        if (data.isEmpty()) {
            sendmessage(String.format("Unknow player '%s'", playername));
            return false;
        }
        Quest quest = QuestController.instance.quests.get(questid);
        if (quest == null){
            sendmessage("Unknown QuestID");
            return false;
        }       
        for(PlayerData playerdata : data){  
	        playerdata.questData.activeQuests.remove(questid);
	        playerdata.saveNBTData(null); 
        }
        return true;
    }
    
    @SubCommand(
            desc = "Removes a quest from finished and active quests",
            usage = "<player> <quest>",
            permissions = {OpOnly.class, ParamCheck.class}
    )
    public Boolean remove(String[] args) {
        String playername=args[0];
        int questid;
        try {
        	questid = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            sendmessage("QuestID must be an integer");
            return false;
        }
        List<PlayerData> data = getPlayersData(playername);
        if (data.isEmpty()) {
            sendmessage(String.format("Unknow player '%s'", playername));
            return false;
        }
        Quest quest = QuestController.instance.quests.get(questid);
        if (quest == null){
            sendmessage("Unknown QuestID");
            return false;
        }     
        for(PlayerData playerdata : data){  
	        playerdata.questData.activeQuests.remove(questid);
	        playerdata.questData.finishedQuests.remove(questid);
	        playerdata.saveNBTData(null); 
        }
        return true;
    }
    @SubCommand(
            desc="reload quests from disk",
            permissions={OpOnly.class}
    )      
    public boolean reload(String args[]){
    	new DialogController();
    	return true;
    }
}













