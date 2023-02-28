package kamkeel;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map.Entry;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentTranslation;

public class CmdHelp extends CommandKamkeelBase{
	private CommandKamkeel parent;
	
	public CmdHelp(CommandKamkeel parent){
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
			sendMessage(sender, "------Kamkeel Commands------");
			for(Entry<String, CommandKamkeelBase> entry : parent.map.entrySet()){
				sendMessage(sender, entry.getKey() + ": " + entry.getValue().getCommandUsage(sender));
			}
			return;
		}
		
		CommandKamkeelBase command = parent.getCommand(args);
		if(command == null)
			throw new CommandException("Unknown command " + args[0]);
		
		if(command.subcommands.isEmpty()){
			sender.addChatMessage(new ChatComponentTranslation(command.getCommandUsage(sender)));
			return;
		}
		
		Method m = null;
		if(args.length > 1){
			m = command.subcommands.get(args[1].toLowerCase());
		}
		if(m == null){
			sendMessage(sender, "------" + command.getCommandName() + " SubCommands------");
			for(Entry<String, Method> entry : command.subcommands.entrySet()){
				sender.addChatMessage(new ChatComponentTranslation(entry.getKey() + ": " + entry.getValue().getAnnotation(SubCommand.class).desc()));
			}
		}
		else{
			sendMessage(sender, "------" + command.getCommandName() + "." + args[1].toLowerCase() + " Command------");
			SubCommand sc = m.getAnnotation(SubCommand.class);
			sender.addChatMessage(new ChatComponentTranslation(sc.desc()));
			if(!sc.usage().isEmpty())
				sender.addChatMessage(new ChatComponentTranslation("Usage: " + sc.usage()));
		}
	}
}
