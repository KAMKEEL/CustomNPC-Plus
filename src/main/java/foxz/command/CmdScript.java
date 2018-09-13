package foxz.command;

import noppes.npcs.controllers.ScriptController;
import foxz.commandhelper.ChMcLogger;
import foxz.commandhelper.annotations.Command;
import foxz.commandhelper.annotations.SubCommand;
import foxz.commandhelper.permissions.OpOnly;

@Command(
        name = "script",
        desc = "Script operation"
)
public class CmdScript extends ChMcLogger {

    public CmdScript(Object sender) {
        super(sender);
    }

    @SubCommand(
            desc = "Reload scripts and saved data from disks script folder.",
            permissions={OpOnly.class}
    )
    public Boolean reload(String args[]) {
    	if(ScriptController.Instance.loadStoredData())
    		sendmessage("Reload succesful");
    	else
    		sendmessage("Failed reloading stored data");
        return true;
    }
}
