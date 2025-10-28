package kamkeel.npcs.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.EventHooks;
import noppes.npcs.api.IPos;
import noppes.npcs.api.IWorld;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.event.CustomNPCsEvent;

import static kamkeel.npcs.util.ColorUtil.sendError;
import static kamkeel.npcs.util.ColorUtil.sendMessage;

public class CommandCommand extends CommandKamkeelBase {

    @Override
    public String getCommandName() {
        return "command";
    }

    @Override
    public String getDescription() {
        return "command CommandName [arg1 arg2 ... argn]";
    }


    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            sendError(sender, "You must specify the command name");
            return;
        }

        String commandId = args[0];
        String[] commandArgs = new String[args.length - 1];
        System.arraycopy(args, 1, commandArgs, 0, args.length - 1);

        IWorld senderWorld = NpcAPI.Instance().getIWorld(sender.getEntityWorld());

        ChunkCoordinates senderCoords = sender.getPlayerCoordinates();
        IPos senderPos = NpcAPI.Instance().getIPos(new BlockPos(senderCoords.posX, senderCoords.posY, senderCoords.posZ));

        CustomNPCsEvent.ScriptedCommandEvent event = new CustomNPCsEvent.ScriptedCommandEvent(senderWorld, senderPos, sender.getCommandSenderName(), commandId, commandArgs);
        EventHooks.onScriptedCommand(sender, event);

        if (!event.replyMessage.isEmpty()) {
            sendMessage(sender, event.replyMessage);
        }
    }
}
