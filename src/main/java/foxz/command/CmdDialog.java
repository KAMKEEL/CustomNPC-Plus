package foxz.command;

import java.util.Arrays;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.client.EntityUtil;
import noppes.npcs.controllers.Dialog;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.DialogOption;
import noppes.npcs.controllers.PlayerData;
import noppes.npcs.entity.EntityDialogNpc;
import foxz.commandhelper.ChMcLogger;
import foxz.commandhelper.annotations.Command;
import foxz.commandhelper.annotations.SubCommand;
import foxz.commandhelper.permissions.OpOnly;
import foxz.commandhelper.permissions.ParamCheck;

@Command(
        name="dialog",
        desc="dialog operations",
        usage="help"
)
public class CmdDialog extends ChMcLogger {

    public CmdDialog(Object sender) {
        super(sender);
    }
    
    
    @SubCommand(
            desc="force read",
            usage="<player> <dialog>",
            permissions={OpOnly.class, ParamCheck.class}
    )
    public boolean read(String args[]){
        String playername=args[0];
        int diagid;
        try {
        	diagid = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            sendmessage("DialogID must be an integer");
            return false;
        }
        List<PlayerData> data = getPlayersData(playername);
        if (data.isEmpty()) {
            sendmessage(String.format("Unknow player '%s'", playername));
            return false;
        }
        for(PlayerData playerdata : data){     
	        playerdata.dialogData.dialogsRead.add(diagid);
	        playerdata.saveNBTData(null);
        }
        return true;
    }
    
    @SubCommand(
            desc="force unread dialog",
            usage="<player> <dialog>",
            permissions={OpOnly.class, ParamCheck.class}
    )      
    public boolean unread(String args[]){
        String playername=args[0];
        int diagid;
        try {
        	diagid = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            sendmessage("DialogID must be an integer");
            return false;
        }
        List<PlayerData> data = getPlayersData(playername);
        if (data.isEmpty()) {
            sendmessage(String.format("Unknow player '%s'", playername));
            return false;
        }
        for(PlayerData playerdata : data){  
	        playerdata.dialogData.dialogsRead.remove(diagid);
	        playerdata.saveNBTData(null);
        }
        return true;
    }
    @SubCommand(
            desc="reload dialogs from disk",
            permissions={OpOnly.class}
    )      
    public boolean reload(String args[]){
    	new DialogController();
    	return true;
    }
    

    @SubCommand(
            desc="show dialog",
            usage="<player> <dialog> <name>",
            permissions={OpOnly.class}
    )      
    public void show(String args[]){
    	EntityPlayer player = CommandBase.getPlayer(pcParam, args[0]);
    	if(player == null){
            sendmessage(String.format("Unknow player '%s'", args[0]));
            return;
    	}
    		
        int diagid;
        try {
        	diagid = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            sendmessage("DialogID must be an integer: " + args[1]);
            return;
        }
        Dialog dialog = DialogController.instance.dialogs.get(diagid);
        if(dialog == null){
            sendmessage("Unknown dialog id: " + args[1]);
            return;
        }
        
    	EntityDialogNpc npc = new EntityDialogNpc(this.pcParam.getEntityWorld());
    	npc.display.name = args[2];
		EntityUtil.Copy(player, npc);
    	DialogOption option = new DialogOption();
    	option.dialogId = diagid;
		option.title = dialog.title;
    	npc.dialogs.put(0, option);
    	NoppesUtilServer.openDialog(player, npc, dialog);
    }
}
