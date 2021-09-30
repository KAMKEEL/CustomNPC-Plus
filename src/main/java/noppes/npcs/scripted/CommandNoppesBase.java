//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.scripted;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
//import net.minecraft.util.text.TextComponentTranslation;

public abstract class CommandNoppesBase extends CommandBase {
    public Map<String, Method> subcommands = new HashMap();

    public CommandNoppesBase() {
        Method[] var1 = this.getClass().getDeclaredMethods();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            Method m = var1[var3];
            CommandNoppesBase.SubCommand sc = (CommandNoppesBase.SubCommand)m.getAnnotation(CommandNoppesBase.SubCommand.class);
            if(sc != null) {
                String name = sc.name();
                if(name.equals("")) {
                    name = m.getName();
                }

                this.subcommands.put(name.toLowerCase(), m);
            }
        }

    }

    public void func_184881_a(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
    }

    public String func_71518_a(ICommandSender sender) {
        return this.getDescription();
    }

    public abstract String getDescription();

    public String getUsage() {
        return "";
    }

    public boolean runSubCommands() {
        return !this.subcommands.isEmpty();
    }

    /*
    protected void sendMessage(ICommandSender sender, String message, Object... obs) {
        sender.func_145747_a(new TextComponentTranslation(message, obs));
    }
    */

    public void executeSub(MinecraftServer server, ICommandSender sender, String command, String[] args) throws CommandException {
        Method m = (Method)this.subcommands.get(command.toLowerCase());
        if(m == null) {
            throw new CommandException("Unknown subcommand " + command, new Object[0]);
        } else {
            CommandNoppesBase.SubCommand sc = (CommandNoppesBase.SubCommand)m.getAnnotation(CommandNoppesBase.SubCommand.class);
            if(!sender.canCommandSenderUseCommand(sc.permission(), "commands.noppes." + this.getCommandName().toLowerCase() + "." + command.toLowerCase())) {
                throw new CommandException("You are not allowed to use this command", new Object[0]);
            } else {
                this.canRun(server, sender, sc.usage(), args);

                try {
                    m.invoke(this, new Object[]{server, sender, args});
                } catch (Exception var8) {
                    if(var8.getCause() instanceof CommandException) {
                        throw (CommandException)var8.getCause();
                    }

                    var8.printStackTrace();
                }

            }
        }
    }

    public void canRun(MinecraftServer server, ICommandSender sender, String usage, String[] args) throws CommandException {
        String[] np = usage.split(" ");
        ArrayList required = new ArrayList();

        for(int i = 0; i < np.length; ++i) {
            String command = np[i];
            if(command.startsWith("<")) {
                required.add(command);
            }

            if(command.equals("<player>") && args.length > i) {
                CommandBase.getPlayer(sender, args[i]);
            }
        }

        if(args.length < required.size()) {
            throw new CommandException("Missing parameter: " + (String)required.get(args.length), new Object[0]);
        }
    }

    public int func_82362_a() {
        return 2;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public @interface SubCommand {
        String name() default "";

        String usage() default "";

        String desc();

        int permission() default 4;
    }
}
