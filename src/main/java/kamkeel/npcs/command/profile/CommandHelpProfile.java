package kamkeel.npcs.command.profile;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentTranslation;

import java.lang.reflect.Method;
import java.util.Map.Entry;

public class CommandHelpProfile extends CommandProfileBase {
	private CommandProfile parent;

	public CommandHelpProfile(CommandProfile parent){
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
		if(args.length == 0){
			sendMessage(sender, "\u00A78------ \u00A7aProfile Commands \u00A78------");
			for(Entry<String, CommandProfileBase> entry : parent.map.entrySet()){
				sendMessage(sender, "\u00A77> " + "\u00A7e" + entry.getKey() + "\u00A78: \u00A77" + entry.getValue().getCommandUsage(sender));
			}
			return;
		}

        CommandProfileBase command = parent.getCommand(args);
		if(command == null){
			sendError(sender, "Unknown command " + args[0]);
			return;
		}

		if(command.subcommands.isEmpty()){
			sender.addChatMessage(new ChatComponentTranslation(command.getCommandUsage(sender)));
			return;
		}

		Method m = null;
		if(args.length > 1){
			m = command.subcommands.get(args[1].toLowerCase());
		}
		if(m == null){
			sendMessage(sender, "\u00A78------ \u00A7a" + command.getCommandName().toUpperCase() + " SubCommands \u00A78------");
			sendMessage(sender, "\u00A77Usage: \u00A76" + command.getUsage());
			for(Entry<String, Method> entry : command.subcommands.entrySet()){
				sender.addChatMessage(new ChatComponentTranslation("\u00A77> " + "\u00A7e" + entry.getKey() + "\u00A78: \u00A77" + entry.getValue().getAnnotation(SubCommand.class).desc()));
			}
			sender.addChatMessage(new ChatComponentTranslation("\u00A78Permission:\u00A77 " + CommandProfile.getCommandPermission(command.getCommandName())));
		}
		else{
			sendMessage(sender, "\u00A78------ \u00A7b" + command.getCommandName().toUpperCase() + "." + args[1].toUpperCase() + " Command \u00A78------");
			SubCommand sc = m.getAnnotation(SubCommand.class);
			sender.addChatMessage(new ChatComponentTranslation("\u00A77" + sc.desc()));
			if(!sc.usage().isEmpty())
				sender.addChatMessage(new ChatComponentTranslation("\u00A77Usage: \u00A76" + sc.usage()));
			sender.addChatMessage(new ChatComponentTranslation("\u00A78Permission:\u00A77 " + getSubCommandPermission(args[1])));
		}
	}
}
