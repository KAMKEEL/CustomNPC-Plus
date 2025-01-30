package foxz.command;

import foxz.commandhelper.ChMcLogger;
import foxz.commandhelper.annotations.Command;
import foxz.commandhelper.annotations.SubCommand;
import foxz.commandhelper.permissions.OpOnly;
import foxz.commandhelper.permissions.ParamCheck;
import kamkeel.npcs.controllers.SyncController;
import kamkeel.npcs.network.packets.data.AchievementPacket;
import kamkeel.npcs.network.packets.data.ChatAlertPacket;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.controllers.data.QuestData;

import java.util.List;

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
        Quest quest = QuestController.Instance.quests.get(questid);
        if (quest == null){
            sendmessage("Unknown QuestID");
            return false;
        }
        for(PlayerData playerdata : data){
	        if(playerdata.questData.activeQuests.containsKey(questid))
	        	continue;
	        QuestData questdata = new QuestData(quest);
	        playerdata.questData.activeQuests.put(questid, questdata);
	        playerdata.save();
	        if(playerdata.player != null){
                AchievementPacket.sendAchievement((EntityPlayerMP) playerdata.player, false, "quest.newquest", quest.title);
                ChatAlertPacket.sendChatAlert((EntityPlayerMP) playerdata.player, "quest.newquest", ": ", quest.title);
	        }
            playerdata.updateClient = true;
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

        Quest quest = QuestController.Instance.quests.get(questid);
        if (quest == null){
            sendmessage("Unknown QuestID");
            return false;
        }
        for(PlayerData playerdata : data){
	        playerdata.questData.finishedQuests.put(questid, System.currentTimeMillis());
	        playerdata.save();
            playerdata.updateClient = true;
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
        Quest quest = QuestController.Instance.quests.get(questid);
        if (quest == null){
            sendmessage("Unknown QuestID");
            return false;
        }
        for(PlayerData playerdata : data){
	        playerdata.questData.activeQuests.remove(questid);
	        playerdata.save();
            playerdata.updateClient = true;
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
        Quest quest = QuestController.Instance.quests.get(questid);
        if (quest == null){
            sendmessage("Unknown QuestID");
            return false;
        }
        for(PlayerData playerdata : data){
	        playerdata.questData.activeQuests.remove(questid);
	        playerdata.questData.finishedQuests.remove(questid);
	        playerdata.save();
            playerdata.updateClient = true;
        }
        return true;
    }
    @SubCommand(
            desc="reload quests from disk",
            permissions={OpOnly.class}
    )
    public boolean reload(String args[]){
    	new QuestController().load();
        SyncController.syncAllQuests();
    	return true;
    }
}













