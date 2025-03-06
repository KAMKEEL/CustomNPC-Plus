package kamkeel.npcs.command;

import kamkeel.npcs.controllers.data.attribute.requirement.RequirementCheckerRegistry;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import noppes.npcs.CustomNpcsPermissions;
import kamkeel.npcs.controllers.AttributeController;
import kamkeel.npcs.controllers.data.attribute.AttributeDefinition;
import noppes.npcs.config.ConfigMain;
import noppes.npcs.scripted.CustomNPCsException;

import java.lang.reflect.Method;
import java.util.*;

import static kamkeel.npcs.command.EffectCommand.getSortedEffectNames;

public class CommandKamkeel extends CommandBase{

    public Map<String, CommandKamkeelBase> map = new HashMap<String, CommandKamkeelBase>();
    public HelpCommand help = new HelpCommand(this);
    public String[] alias = {"kam"};

    public CommandKamkeel(){
        registerCommand(help);
        registerCommand(new ScriptCommand());
        registerCommand(new SlayCommand());
        registerCommand(new QuestCommand());
        registerCommand(new QuestCategoryCommand());
        registerCommand(new DialogCommand());
        registerCommand(new DialogCategoryCommand());
        registerCommand(new FactionCommand());
        registerCommand(new NpcCommand());
        registerCommand(new CloneCommand());
        registerCommand(new ConfigCommand());
        registerCommand(new AnimationCommand());
        registerCommand(new OverlayCommand());
        registerCommand(new CommandCommand());
        registerCommand(new EffectCommand());
        if(ConfigMain.AttributesEnabled)
            registerCommand(new AttributeCommand());
    }

    public void registerCommand(CommandKamkeelBase command){
        String name = command.getCommandName().toLowerCase();
        if(map.containsKey(name))
            throw new CustomNPCsException("Already a subcommand with the name: " + name);
        map.put(name, command);
    }

    @Override
    public String getCommandName() {
        return "kamkeel";
    }

    @Override
    public List getCommandAliases()
    {
        return Arrays.asList(alias);
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "Use as /kamkeel subcommand";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if(args.length == 0){
            help.processCommand(sender, args);
            return;
        }

        CommandKamkeelBase command = getCommand(args);
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
        CommandKamkeelBase command = getCommand(args);
        if(command == null)
            return null;
        if(args.length == 2 && command.runSubCommands())
            return CommandBase.getListOfStringsMatchingLastWord(args, command.subcommands.keySet().toArray(new String[command.subcommands.keySet().size()]));
        String[] useArgs = command.getUsage().split(" ");
        if(command.runSubCommands()) {
            Method m = command.subcommands.get(args[1].toLowerCase());
            if(m != null) {
                useArgs = m.getAnnotation(CommandKamkeelBase.SubCommand.class).usage().split(" ");
            }
        }
        if(args.length <= useArgs.length + 2) {
            if(args.length - 3 >= 0){
                String usage = useArgs[args.length - 3];
                if(usage.equals("<player>") || usage.equals("[player]")) {
                    return CommandBase.getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
                }
                else if(usage.equals("<attribute>")) {
                    List<String> keys = new ArrayList<>();
                    for (AttributeDefinition def : AttributeController.getAllAttributes()) {
                        keys.add(def.getKey());
                    }
                    return CommandBase.getListOfStringsMatchingLastWord(args, keys.toArray(new String[keys.size()]));
                } else if (usage.equals("<requirement>")) {
                    List<String> keys = new ArrayList<>(RequirementCheckerRegistry.getAllKeys());
                    return CommandBase.getListOfStringsMatchingLastWord(args, keys.toArray(new String[keys.size()]));
                } else if(usage.equals("<effectName>")){
                    List<String> keys = getSortedEffectNames();
                    return CommandBase.getListOfStringsMatchingLastWord(args, keys.toArray(new String[keys.size()]));
                }
            }
        }
        return command.addTabCompletionOptions(sender, Arrays.copyOfRange(args, 1, args.length));
    }

    public CommandKamkeelBase getCommand(String[] args){
        if(args.length == 0)
            return null;
        return map.get(args[0].toLowerCase());
    }

    @Override
    public int getRequiredPermissionLevel(){
        return 2;
    }

    public static String getCommandPermission(String command){
        return "cnpc.kamkeel." + command.toLowerCase();
    }

    public static String getUniversalPermission(){
        return "cnpc.kamkeel.*";
    }

    public static boolean canSendCommand(ICommandSender sender, CommandKamkeelBase command){
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

