package kamkeel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentTranslation;

public abstract class CommandKamkeelBase extends CommandBase{
	public Map<String, Method> subcommands = new HashMap<String, Method>();
	
	public CommandKamkeelBase(){
        for (Method m : this.getClass().getDeclaredMethods()) {
            SubCommand sc = m.getAnnotation(SubCommand.class);
            if (sc != null) {
                String name = sc.name();
                if (name.equals("")) 
                    name = m.getName();
                subcommands.put(name.toLowerCase(), m);
            }
        }
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {

	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return getDescription();
	}
	
	public abstract String getDescription();

	/**
	 * @return Should return a string in the format of <arg> <arg2> <arg3> [arg4] where <> is a required parameter and [] optional
	 */
	public String getUsage(){
		return "";
	}

	public boolean runSubCommands(){
		return !subcommands.isEmpty();
	}

	protected void sendMessage(ICommandSender sender, String message, Object... obs) {
		sender.addChatMessage(new ChatComponentTranslation(message, obs));
	}

	@Retention(value = RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface SubCommand {
	
	    String name() default "";
	
	    /**
	     * @return Should return a string in the format of <arg> <arg2> <arg3> [arg4] where <> is a required parameter and [] optional
	     */
	    String usage() default "";
	
	    String desc();

		int permission() default 2;
	}

	public void processSubCommand(ICommandSender sender, String command, String[] args) throws CommandException {
		Method m = subcommands.get(command.toLowerCase());
		if(m == null)
			throw new CommandException("Unknown subcommand " + command);

		SubCommand sc = m.getAnnotation(SubCommand.class);
		if(!sender.canCommandSenderUseCommand(sc.permission(), "commands.kamkeel." + getCommandName().toLowerCase() + "." + command.toLowerCase()))
			throw new CommandException("You are not allowed to use this command: " + command);

		canRun(sender, sc.usage(), args);
		try {
			m.invoke(this, sender, args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void canRun(ICommandSender sender, String usage, String[] args) throws CommandException{
		String[] np = usage.split(" ");
		List<String> required = new ArrayList<String>();
		for(int i = 0; i < np.length; i++){
			String command = np[i];
			if(command.startsWith("<")){
				required.add(command);
			}
			if(command.equals("<player>") && args.length > i){
				CommandBase.getPlayer(sender, args[i]); //throws PlayerNotFoundException if no player is found
			}

		}
		if (args.length < required.size()) {
			throw new CommandException("Missing parameter: " + required.get(args.length));
		}
	}

    public int getRequiredPermissionLevel(){
        return 2;
    }
}