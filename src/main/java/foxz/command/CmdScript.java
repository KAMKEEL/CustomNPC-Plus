package foxz.command;

import foxz.commandhelper.ChMcLogger;
import foxz.commandhelper.annotations.Command;
import foxz.commandhelper.annotations.SubCommand;
import foxz.commandhelper.permissions.OpOnly;
import noppes.npcs.EventHooks;
import noppes.npcs.api.IWorld;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.event.WorldEvent;

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

    @SubCommand(
        desc = "Run scriopt command event.",
        permissions={OpOnly.class}
    )
    public Boolean run(String args[]) {
        IWorld world = NpcAPI.Instance().getIWorld(pcParam.getEntityWorld());
        WorldEvent.ScriptCommandEvent event = new WorldEvent.ScriptCommandEvent(world, null, args);
        EventHooks.onWorldScriptEvent(event);
        return true;
    }
}
