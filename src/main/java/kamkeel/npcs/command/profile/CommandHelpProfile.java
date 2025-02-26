package kamkeel.npcs.command.profile;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentTranslation;
import noppes.npcs.CustomNpcsPermissions;

import java.lang.reflect.Method;
import java.util.Map.Entry;

import static kamkeel.npcs.util.ColorUtil.sendError;
import static kamkeel.npcs.util.ColorUtil.sendMessage;

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
                sendMessage(sender, "\u00A77> " + "\u00A7e" + entry.getKey() + "\u00A78: \u00A77"
                    + entry.getValue().getCommandUsage(sender));
            }
            return;
        }

        CommandProfileBase command = parent.getCommand(args);
        if(command == null){
            sendError(sender, "Unknown command " + args[0]);
            return;
        }

        if(command.subcommands.isEmpty()){
            sendMessage(sender, (command.getCommandUsage(sender)));
            return;
        }

        Method m = null;
        if(args.length > 1){
            m = command.subcommands.get(args[1].toLowerCase());
        }
        if(m == null){
            sendMessage(sender, "\u00A78------ \u00A7a" + command.getCommandName().toUpperCase()
                + " SubCommands \u00A78------");
            sendMessage(sender, "\u00A77Usage: \u00A76" + command.getUsage());
            for(Entry<String, Method> entry : command.subcommands.entrySet()){
                SubCommand sc = entry.getValue().getAnnotation(SubCommand.class);
                // For players, only list subcommands if they have permission; for console, show all.
                if(!(sender instanceof EntityPlayer) || command.canSendCommand(sender, sc, entry.getKey())){
                    sendMessage(sender, ("\u00A77> "
                        + "\u00A7e" + entry.getKey()
                        + "\u00A78: \u00A77" + sc.desc()));
                }
            }
            // Show permission node only if sender is console or has permission.
            if(!(sender instanceof EntityPlayer) ||
                CustomNpcsPermissions.hasPermission((EntityPlayer)sender, CustomNpcsPermissions.PROFILE_ADMIN)){
                sendMessage(sender, ("\u00A78Permission:\u00A77 "
                    + CommandProfile.getCommandPermission(command.getCommandName())));
            }
        }
        else{
            sendMessage(sender, "\u00A78------ \u00A7b" + command.getCommandName().toUpperCase()
                + "." + args[1].toUpperCase() + " Command \u00A78------");
            SubCommand sc = m.getAnnotation(SubCommand.class);
            sendMessage(sender, ("\u00A77" + sc.desc()));
            if(!sc.usage().isEmpty()){
                sendMessage(sender, ("\u00A77Usage: \u00A76" + sc.usage()));
            }
            if(!(sender instanceof EntityPlayer) ||
                CustomNpcsPermissions.hasPermission((EntityPlayer)sender, CustomNpcsPermissions.PROFILE_ADMIN)){
                sendMessage(sender, ("\u00A78Permission:\u00A77 "
                    + getSubCommandPermission(args[1])));
            }
        }
    }
}
