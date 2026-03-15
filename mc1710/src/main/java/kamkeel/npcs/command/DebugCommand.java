package kamkeel.npcs.command;

import kamkeel.npcs.util.CNPCDebug;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

import static kamkeel.npcs.util.ColorUtil.sendResult;

public class DebugCommand extends CommandKamkeelBase {

    @Override
    public String getCommandName() {
        return "debug";
    }

    @Override
    public String getDescription() {
        return "Toggle debug logging types";
    }

    @Override
    public String getUsage() {
        return "<type>";
    }

    @Override
    public boolean runSubCommands() {
        return false;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            throw new CommandException("Usage: /kam debug <type> (e.g. energy)");
        }

        String type = args[0].toLowerCase();
        boolean newState = CNPCDebug.toggleServer(type);
        sendResult(sender, "Server debug '" + type + "' " + (newState ? "\u00A7aENABLED" : "\u00A7cDISABLED"));
    }
}
