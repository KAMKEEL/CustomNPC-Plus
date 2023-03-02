package kamkeel;

import net.minecraft.command.ICommandSender;
import noppes.npcs.controllers.ScriptController;


public class ScriptCommand extends CommandKamkeelBase {

    @SubCommand(desc = "Reload scripts data and folders.")
    public Boolean reload(ICommandSender sender, String args[]) {
		ScriptController.Instance.loadCategories();

		if(ScriptController.Instance.loadPlayerScripts())
			sendMessage(sender, "Reload player scripts successfully");
		else
			sendMessage(sender, "Failed reloading player scripts");

		if(ScriptController.Instance.loadForgeScripts())
			sendMessage(sender, "Reload forge scripts successfully");
		else
			sendMessage(sender, "Failed reloading forge scripts");

		if(ScriptController.Instance.loadStoredData())
			sendMessage(sender, "Reload stored data successfully");
		else
			sendMessage(sender, "Failed reloading stored data");
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
