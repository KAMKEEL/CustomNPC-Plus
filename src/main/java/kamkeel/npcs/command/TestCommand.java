package kamkeel.npcs.command;

import kamkeel.npcs.network.packets.data.gui.GuiFontTestPacket;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

import static kamkeel.npcs.util.ColorUtil.sendError;
import static kamkeel.npcs.util.ColorUtil.sendResult;

public class TestCommand extends CommandKamkeelBase {

    @Override
    public String getCommandName() {
        return "testfont";
    }

    @Override
    public String getDescription() {
        return "Open the scalable font test GUI";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (!(sender instanceof EntityPlayerMP)) {
            sendError(sender, "This command can only be run by a player.");
            return;
        }
        GuiFontTestPacket.open((EntityPlayerMP) sender);
        sendResult(sender, "Opened font test GUI.");
    }
}
