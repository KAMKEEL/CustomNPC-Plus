package foxz.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import noppes.npcs.api.ICommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScriptedCommand extends CommandBase implements ICommand {
    private String commandName;
    private int permissionLevel;
    private String commandUsage;
    private final ArrayList<String> aliases;

    public ScriptedCommand(String commandName, int permissionLevel) {
        this.commandName = commandName;
        this.permissionLevel = permissionLevel;
        this.commandUsage = "";
        this.aliases = new ArrayList<>();
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public void setCommandUsage(String commandUsage) {
        this.commandUsage = commandUsage;
    }

    public void setPermissionLevel(int permissionLevel) {
        this.permissionLevel = permissionLevel;
    }

    @Override
    public String getCommandName() {
        return this.commandName;
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_) {
        return this.commandUsage;
    }

    public String getCommandUsage() {
        return this.commandUsage;
    }

    @Override
    public void processCommand(ICommandSender p_71515_1_, String[] p_71515_2_) {
    }

    public int getRequiredPermissionLevel() {
        return this.permissionLevel;
    }

    public int getPermissionLevel() {
        return this.getRequiredPermissionLevel();
    }

    public List<?> getCommandAliases() {
        return this.aliases;
    }

    public String[] getAliases() {
        return this.aliases.toArray(new String[0]);
    }

    public void addAliases(String... aliases) {
        this.aliases.addAll(Arrays.asList(aliases));
    }

    public boolean hasAlias(String alias) {
        return this.aliases.contains(alias);
    }

    public void removeAlias(String alias) {
        this.aliases.remove(alias);
    }
}
