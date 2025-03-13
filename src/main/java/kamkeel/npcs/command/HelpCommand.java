package kamkeel.npcs.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

import java.lang.reflect.Method;
import java.util.Map.Entry;

import static kamkeel.npcs.util.ColorUtil.sendError;
import static kamkeel.npcs.util.ColorUtil.sendMessage;

public class HelpCommand extends CommandKamkeelBase {
    private final CommandKamkeel parent;

    public HelpCommand(CommandKamkeel parent) {
        this.parent = parent;
    }

    @Override
    public String getCommandName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "help [command]";
    }


    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            sendMessage(sender, "\u00A78------ \u00A7cKamkeel Commands \u00A78------");
            for (Entry<String, CommandKamkeelBase> entry : parent.map.entrySet()) {
                sendMessage(sender, "\u00A77> " + "\u00A7e" + entry.getKey() + "\u00A78: \u00A77" + entry.getValue().getCommandUsage(sender));
            }
            return;
        }

        CommandKamkeelBase command = parent.getCommand(args);
        if (command == null) {
            sendError(sender, "Unknown command " + args[0]);
            return;
        }

        if (command.subcommands.isEmpty()) {
            sendMessage(sender, (command.getCommandUsage(sender)));
            return;
        }

        Method m = null;
        if (args.length > 1) {
            m = command.subcommands.get(args[1].toLowerCase());
        }
        if (m == null) {
            sendMessage(sender, "\u00A78------ \u00A7a" + command.getCommandName().toUpperCase() + " SubCommands \u00A78------");
            sendMessage(sender, "\u00A77Usage: \u00A76" + command.getUsage());
            for (Entry<String, Method> entry : command.subcommands.entrySet()) {
                sendMessage(sender, ("\u00A77> " + "\u00A7e" + entry.getKey() + "\u00A78: \u00A77" + entry.getValue().getAnnotation(SubCommand.class).desc()));
            }
            sendMessage(sender, ("\u00A78Permission:\u00A77 " + CommandKamkeel.getCommandPermission(command.getCommandName())));
        } else {
            sendMessage(sender, "\u00A78------ \u00A7b" + command.getCommandName().toUpperCase() + "." + args[1].toUpperCase() + " Command \u00A78------");
            SubCommand sc = m.getAnnotation(SubCommand.class);
            sendMessage(sender, ("\u00A77" + sc.desc()));
            if (!sc.usage().isEmpty())
                sendMessage(sender, ("\u00A77Usage: \u00A76" + sc.usage()));
            sendMessage(sender, ("\u00A78Permission:\u00A77 " + getSubCommandPermission(args[1])));
        }
    }
}
