package kamkeel.npcs.command;

import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.packets.data.gui.GuiFontTestOpenPacket;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

import static kamkeel.npcs.util.ColorUtil.sendResult;

public class TestCommand extends CommandKamkeelBase {
    @Override
    public String getCommandName() {
        return "testfont";
    }

    @Override
    public String getDescription() {
        return "Open font renderer test GUI";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (!(sender instanceof EntityPlayerMP)) {
            throw new CommandException("This command can only be run by a player");
        }

        EntityPlayerMP player = (EntityPlayerMP) sender;
        PacketHandler.Instance.sendToPlayer(new GuiFontTestOpenPacket(), player);
        sendResult(sender, "Opened font test GUI.");
    }
}
