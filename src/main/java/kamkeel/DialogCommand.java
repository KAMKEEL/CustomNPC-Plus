package kamkeel;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.client.EntityUtil;
import noppes.npcs.controllers.DialogController;

import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogOption;
import noppes.npcs.controllers.data.PlayerData;
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
            throw new CommandException("DialogID must be an integer: " + args[1]);
        }
        List<PlayerData> data = PlayerDataController.instance.getPlayersData(sender, playername);
        if (data.isEmpty()) {
        	throw new CommandException("Unknown player: " + playername);
        }
        for(PlayerData playerdata : data){     
	        playerdata.dialogData.dialogsRead.add(diagid);
            playerdata.savePlayerDataOnFile();
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
            throw new CommandException("DialogID must be an integer: " + args[1]);
        }
        List<PlayerData> data = PlayerDataController.instance.getPlayersData(sender, playername);
        if (data.isEmpty()) {
            throw new CommandException("Unknown player: " + playername);
        }
        for(PlayerData playerdata : data){  
	        playerdata.dialogData.dialogsRead.remove(diagid);
            playerdata.savePlayerDataOnFile();
        }
    }
    @SubCommand(
            desc="reload dialogs from disk",
            permission = 4
    )      
    public void reload(ICommandSender sender, String args[]){
    	new DialogController();
    }

    @SubCommand(
            desc="show dialog",
            usage="<player> <dialog> <name>"
    )      
    public void show(ICommandSender sender, String args[]) throws CommandException{
    	EntityPlayer player = CommandBase.getPlayer(sender, args[0]);
    	if(player == null){
            throw new CommandException("Unknown player: " + args[0]);
    	}
    		
        int diagid;
        try {
        	diagid = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
        	throw new CommandException("DialogID must be an integer: " + args[1]);
        }
        Dialog dialog = DialogController.instance.dialogs.get(diagid);
        if(dialog == null){
        	throw new CommandException("Unknown dialog id: " + args[1]);
        }
        
    	EntityDialogNpc npc = new EntityDialogNpc(sender.getEntityWorld());
    	npc.display.setName(args[2]);
		EntityUtil.Copy(player, npc);
    	DialogOption option = new DialogOption();
    	option.dialogId = diagid;
		option.title = dialog.title;
    	npc.dialogs.put(0, option);
    	NoppesUtilServer.openDialog(player, npc, dialog, 0);
    }
}
