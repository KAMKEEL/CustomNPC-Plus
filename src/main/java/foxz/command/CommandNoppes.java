package foxz.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.List;

/**
 * @deprecated The /noppes command is deprecated and no longer supported.
 * Please use the /kam command instead for all CNPC+ operations.
 */
@Deprecated
public class CommandNoppes extends CommandBase {

    public CmdNoppes noppes = new CmdNoppes(this);

    @Override
    public String getCommandName() {
        return noppes.commandHelper.name;
    }

    @Override
    public String getCommandUsage(ICommandSender var1) {
        return noppes.commandHelper.usage;
    }

    @Override
    public void processCommand(ICommandSender var1, String[] var2) {
        // Send deprecation warning
        var1.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[DEPRECATED] " +
            EnumChatFormatting.YELLOW + "The /noppes command is deprecated and no longer supported."));
        var1.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW +
            "Please use " + EnumChatFormatting.GREEN + "/kam" + EnumChatFormatting.YELLOW + " commands instead."));
        var1.addChatMessage(new ChatComponentText(EnumChatFormatting.GRAY +
            "Type " + EnumChatFormatting.WHITE + "/kam help" + EnumChatFormatting.GRAY + " for available commands."));

        // Still execute the command for backwards compatibility
        noppes.processCommand(var1, var2);
    }

    @Override
    public List addTabCompletionOptions(ICommandSender par1, String[] par2) {
        return noppes.addTabCompletion(par1, par2);
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }
}
