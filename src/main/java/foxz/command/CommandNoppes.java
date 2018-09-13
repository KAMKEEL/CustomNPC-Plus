package foxz.command;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

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
        noppes.processCommand(var1, var2);
    }

    @Override
    public List addTabCompletionOptions(ICommandSender par1, String[] par2){
        return noppes.addTabCompletion(par1, par2);
    }

    @Override
    public int getRequiredPermissionLevel(){
        return 2;
    }
}
