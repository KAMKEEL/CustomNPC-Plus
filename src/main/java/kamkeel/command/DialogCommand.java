package kamkeel.command;

import java.util.Collection;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.client.EntityUtil;
import noppes.npcs.controllers.DialogController;

import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogOption;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.entity.EntityDialogNpc;

public class DialogCommand extends CommandKamkeelBase {

	@Override
	public String getCommandName() {
		return "dialog";
	}

	@Override
	public String getDescription() {
		return "Dialog operations";
	}
    
    @SubCommand(
            desc = "force read",
            usage = "<player> <dialog>"
    )
    public void read(ICommandSender sender, String args[]) throws CommandException{
        String playername=args[0];
        int diagid;
        try {
        	diagid = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            sendError(sender, "DialogID must be an integer: " + args[1]);
            return;
        }
        List<PlayerData> data = PlayerDataController.Instance.getPlayersData(sender, playername);
        if (data.isEmpty()) {
            sendError(sender, "Unknown player: " + playername);
            return;
        }
        for(PlayerData playerdata : data){     
	        playerdata.dialogData.dialogsRead.add(diagid);
            playerdata.save();
            sendResult(sender, String.format("Forced Read for Dialog \u00A7e%d\u00A77 for Player '\u00A7b%s\u00A77'", diagid, playerdata.playername));
        }
    }
    
    @SubCommand(
            desc = "force unread dialog",
            usage = "<player> <dialog>"
    )      
    public void unread(ICommandSender sender, String args[]) throws CommandException{
        String playername=args[0];
        int diagid;
        try {
        	diagid = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            sendError(sender, "DialogID must be an integer: " + args[1]);
            return;
        }
        List<PlayerData> data = PlayerDataController.Instance.getPlayersData(sender, playername);
        if (data.isEmpty()) {
            sendError(sender, "Unknown player: " + playername);
            return;
        }
        for(PlayerData playerdata : data){  
	        playerdata.dialogData.dialogsRead.remove(diagid);
            playerdata.save();
            sendResult(sender, String.format("Forced Unread for Dialog \u00A7e%d\u00A77 for Player '\u00A7b%s\u00A77'", diagid, playerdata.playername));
        }
    }
    @SubCommand(
            desc="reload dialogs from disk",
            permission = 4
    )      
    public void reload(ICommandSender sender, String args[]){
    	new DialogController();
        sendResult(sender, "Dialogs Reloaded");
    }

    @SubCommand(
            desc="show dialog",
            usage="<player> <dialog> <name>"
    )      
    public void show(ICommandSender sender, String args[]) throws CommandException{
    	EntityPlayer player = CommandBase.getPlayer(sender, args[0]);
    	if(player == null){
            sendError(sender, "Unknown player: " + args[0]);
            return;
    	}
    		
        int diagid;
        try {
        	diagid = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            sendError(sender, "DialogID must be an integer: " + args[1]);
            return;
        }
        Dialog dialog = DialogController.instance.dialogs.get(diagid);
        if(dialog == null){
            sendError(sender, "Unknown dialog id: " + args[1]);
            return;
        }
        
    	EntityDialogNpc npc = new EntityDialogNpc(sender.getEntityWorld());
    	npc.display.setName(args[2]);
		EntityUtil.Copy(player, npc);
    	DialogOption option = new DialogOption();
    	option.dialogId = diagid;
		option.title = dialog.title;
    	npc.dialogs.put(0, option);
    	NoppesUtilServer.openDialog(player, npc, dialog, 0);
        sendResult(sender, String.format("Displayed Dialog \u00A7e%d\u00A77 to Player '\u00A7b%s\u00A77'", diagid, player.getCommandSenderName()));
    }

    @SubCommand(
            desc = "Find dialog id number by its name",
            usage = "<dialog name>"
    )
    public void id(ICommandSender sender, String args[]) throws CommandException {
        if(args.length == 0){
            sendError(sender, "Please provide a name for the dialog");
            return;
        }

        String dialogName = String.join(" ", args).toLowerCase();
        final Collection<Dialog> quests = DialogController.instance.dialogs.values();
        int count = 0;
        for(Dialog dialog : quests){
            if(dialog.getName().toLowerCase().contains(dialogName)){
                sendResult(sender, String.format("Dialog \u00A7e%d\u00A77 - \u00A7c'%s'", dialog.id, dialog.getName()));
                count++;
            }
        }
        if(count == 0){
            sendResult(sender, String.format("No Dialog found with name: \u00A7c'%s'", dialogName));
        }
    }
}
