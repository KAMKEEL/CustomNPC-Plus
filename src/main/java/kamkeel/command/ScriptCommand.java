package kamkeel.command;

import net.minecraft.command.ICommandSender;
import noppes.npcs.controllers.ScriptController;


public class ScriptCommand extends CommandKamkeelBase {

    @SubCommand(desc = "Reload scripts data and folders.")
    public Boolean reload(ICommandSender sender, String args[]) {
		ScriptController.Instance.loadCategories();

		if(ScriptController.Instance.loadPlayerScripts())
			sendResult(sender, "Reload player scripts successfully");
		else
			sendError(sender, "Failed reloading player scripts");

		if(ScriptController.Instance.loadForgeScripts())
			sendResult(sender, "Reload forge scripts successfully");
		else
			sendError(sender, "Failed reloading forge scripts");

		if(ScriptController.Instance.loadStoredData())
			sendResult(sender, "Reload stored data successfully");
		else
			sendError(sender, "Failed reloading stored data");
		return true;

	}

	@Override
	public String getCommandName() {
		return "script";
	}

	@Override
	public String getDescription() {
		return "Commands for scripts";
	}
}
