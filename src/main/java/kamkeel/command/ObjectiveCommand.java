package kamkeel.command;

import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.api.handler.data.IQuestInterface;
import noppes.npcs.api.handler.data.IQuestObjective;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.controllers.data.QuestData;
import noppes.npcs.quests.QuestInterface;

public class ObjectiveCommand extends CommandKamkeelBase {

    @FunctionalInterface
    interface ProcessProgressChnageFunction {
        public void apply(IQuestObjective objective, Integer progress);
    }

    @Override
    public String getCommandName() {
        return "objective";
    }

    @Override
    public String getDescription() {
        return "Objective operations";
    }

    @SubCommand(
        desc = "Sets a value for an objective",
        usage = "<player> <quest> <objective> <value>"
    )
    public void set(ICommandSender sender, String[] args) throws CommandException {
        processProgressChangeCommand(sender, args,
            (objective, progress) -> objective.setProgress(progress));
    }

    @SubCommand(
        desc = "Add a value for an objective to the existing value.",
        usage = "<player> <quest> <objective> <value>"
    )
    public void add(ICommandSender sender, String[] args) throws CommandException {
        processProgressChangeCommand(sender, args,
            (objective, progress) -> objective.setProgress(objective.getProgress() + progress));
    }

    @SubCommand(
        desc = "Substracts a value for an objective from its existing value.",
        usage = "<player> <quest> <objective> <value>"
    )
    public void sub(ICommandSender sender, String[] args) throws CommandException {
        processProgressChangeCommand(sender, args,
            (objective, progress) -> objective.setProgress(objective.getProgress() - progress));
    }

    public void processProgressChangeCommand(ICommandSender sender, String[] args, ProcessProgressChnageFunction changeMethod) {
        if (!(sender instanceof EntityPlayer)) {
            return;
        }
        
        String playername = args[0];
        int questid;
        try {
            questid = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            sendError(sender, "QuestID must be an integer: " + args[1]);
            return;
        }
        
        int objectivenumber;
        try {
            objectivenumber = Integer.parseInt(args[2]);
        } catch (NumberFormatException ex) {
            sendError(sender, "Objective must be an integer: " + args[2]);
            return;
        }
        
        int progress;
        try {
            progress = Integer.parseInt(args[3]);
        } catch (NumberFormatException ex) {
            sendError(sender, "Objective must be an integer: " + args[3]);
            return;
        }

        List<PlayerData> data = PlayerDataController.Instance.getPlayersData(sender, playername);
        if (data.isEmpty()) {
            sendError(sender, "Unknown player: " + playername);
            return;
        }

        Quest quest = QuestController.Instance.quests.get(questid);
        if (quest == null) {
        	sendError(sender, "Unknown QuestID: " + questid);
            return;
        }

        for(PlayerData playerdata : data) {
            QuestData questdata = playerdata.questData.activeQuests.get(questid);
            IQuestInterface iQuestInterface = questdata.quest.getQuestInterface();
            if (iQuestInterface instanceof QuestInterface) {
                QuestInterface questInterface = (QuestInterface)iQuestInterface;
                IQuestObjective objective = questInterface.getObjectives((EntityPlayer)sender)[objectivenumber];
                changeMethod.apply(objective, progress);
                sendResult(sender, String.format("Objective current value for Quest \u00A7e%d\u00A77 for Player '\u00A7b%s\u00A77' is %d of %d" , questid, playerdata.playername, objectivenumber, objective.getProgress(), objective.getMaxProgress()));
                return;
            }
        }
            
        sendError(sender, "Quest is not objectivable: " + questid);
    }

    @SubCommand(
        desc = "Gets the value of an objective",
        usage = "<player> <quest> <objective>"
    )
    public void get(ICommandSender sender, String[] args) throws CommandException {
        if (!(sender instanceof EntityPlayer)) {
            return;
        }
        
        String playername = args[0];
        int questid;
        try {
            questid = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            sendError(sender, "QuestID must be an integer: " + args[1]);
            return;
        }
        
        int objectivenumber;
        try {
            objectivenumber = Integer.parseInt(args[2]);
        } catch (NumberFormatException ex) {
            sendError(sender, "Objective must be an integer: " + args[2]);
            return;
        }

        List<PlayerData> data = PlayerDataController.Instance.getPlayersData(sender, playername);
        if (data.isEmpty()) {
            sendError(sender, "Unknown player: " + playername);
            return;
        }

        Quest quest = QuestController.Instance.quests.get(questid);
        if (quest == null) {
        	sendError(sender, "Unknown QuestID: " + questid);
            return;
        }

        for(PlayerData playerdata : data) {
            QuestData questdata = playerdata.questData.activeQuests.get(questid);
            IQuestInterface iQuestInterface = questdata.quest.getQuestInterface();
            if (iQuestInterface instanceof QuestInterface) {
                QuestInterface questInterface = (QuestInterface)iQuestInterface;
                IQuestObjective objective = questInterface.getObjectives((EntityPlayer)sender)[objectivenumber];
                sendResult(sender, String.format("Objective current value for Quest \u00A7e%d\u00A77 for Player '\u00A7b%s\u00A77' is %d of %d" , questid, playerdata.playername, objectivenumber, objective.getProgress(), objective.getMaxProgress()));
                return;
            }
        }
            
        sendError(sender, "Quest is not objectivable: " + questid);
    }
}
