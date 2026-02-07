package kamkeel.npcs.command;

import kamkeel.npcs.network.packets.data.gui.GuiFontTestPacket;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

public class TestCommand extends CommandKamkeelBase {

    @Override
    public String getCommandName() {
        return "testfont";
    }

    @Override
    public String getDescription() {
        return "Open the client-side font test GUI";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (!(sender instanceof EntityPlayerMP)) {
            throw new CommandException("This command can only be used by a player");
        }
        GuiFontTestPacket.open((EntityPlayerMP) sender);
    }
}
