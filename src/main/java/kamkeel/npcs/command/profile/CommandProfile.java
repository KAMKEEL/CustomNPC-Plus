package kamkeel.npcs.command.profile;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.scripted.CustomNPCsException;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandProfile extends CommandBase {

	public Map<String, CommandProfileBase> map = new HashMap<String, CommandProfileBase>();
	public CommandHelpProfile help = new CommandHelpProfile(this);
	public String[] alias = {"profile"};

	public CommandProfile(){
		registerCommand(help);
        registerCommand(new CommandProfileChange());
        registerCommand(new CommandProfileRemove());
        registerCommand(new CommandProfileAdmin());
        registerCommand(new CommandProfileList());
	}

	public void registerCommand(CommandProfileBase command){
		String name = command.getCommandName().toLowerCase();
		if(map.containsKey(name))
			throw new CustomNPCsException("Already a subcommand with the name: " + name);
		map.put(name, command);
	}

	@Override
	public String getCommandName() {
		return "profile";
	}

	@Override
	public List getCommandAliases()
	{
		return Arrays.asList(alias);
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "Use as /profile subcommand";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		if(args.length == 0){
			help.processCommand(sender, args);
			return;
		}

		CommandProfileBase command = getCommand(args);
		if(command == null)
			throw new CommandException("Unknown command " + args[0]);

		args = Arrays.copyOfRange(args, 1, args.length);
		if(command.subcommands.isEmpty() || !command.runSubCommands()){
			if(!canSendCommand(sender, command))
				throw new CommandException("You are not allowed to use this command: " + command);
			command.canRun(sender, command.getUsage(), args);
			command.processCommand(sender, args);
			return;
		}

		if(args.length == 0){
			help.processCommand(sender, new String[]{command.getCommandName()});
			return;
		}

		command.processSubCommand(sender, args[0], Arrays.copyOfRange(args, 1, args.length));
	}

	@Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args){
		if(args.length == 1)
			return CommandBase.getListOfStringsMatchingLastWord(args, map.keySet().toArray(new String[map.size()]));
    	CommandProfileBase command = getCommand(args);
		if(command == null)
			return null;
		if(args.length == 2 && command.runSubCommands())
			return getListOfStringsMatchingLastWord(args, command.subcommands.keySet().toArray(new String[command.subcommands.keySet().size()]));
		String[] useArgs = command.getUsage().split(" ");
		if(command.runSubCommands()) {
			Method m = command.subcommands.get(args[1].toLowerCase());
			if(m != null) {
				useArgs = m.getAnnotation(CommandProfileBase.SubCommand.class).usage().split(" ");
			}
		}
		if(args.length <= useArgs.length + 2) {
			if(args.length - 3 >= 0){
				String usage = useArgs[args.length - 3];
				if(usage.equals("<player>") || usage.equals("[player]")) {
					return CommandBase.getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
				}
			}
		}
		return command.addTabCompletionOptions(sender, Arrays.copyOfRange(args, 1, args.length));
    }

    public CommandProfileBase getCommand(String[] args){
    	if(args.length == 0)
    		return null;
    	return map.get(args[0].toLowerCase());
    }

    @Override
    public int getRequiredPermissionLevel(){
        return 0;
    }

	public static String getCommandPermission(String command){
		return "profile.kamkeel." + command.toLowerCase();
	}

	public static String getUniversalPermission(){
		return "profile.kamkeel.*";
	}

	public static boolean canSendCommand(ICommandSender sender, CommandProfileBase command){
		if(sender.canCommandSenderUseCommand(command.getRequiredPermissionLevel(), getUniversalPermission())){
			return true;
		}

		if(sender.canCommandSenderUseCommand(command.getRequiredPermissionLevel(), getCommandPermission(command.getCommandName()))){
			return true;
		}

		if(sender instanceof EntityPlayer){
			return CustomNpcsPermissions.hasCustomPermission((EntityPlayer) sender, getCommandPermission(command.getCommandName()));
		}

		return false;
	}
}

