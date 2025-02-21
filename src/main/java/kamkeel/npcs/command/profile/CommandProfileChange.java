package kamkeel.npcs.command.profile;

import kamkeel.npcs.controllers.ProfileController;
import kamkeel.npcs.controllers.data.EnumProfileOperation;
import kamkeel.npcs.controllers.data.ProfileOperation;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

import static kamkeel.npcs.util.ColorUtil.sendError;
import static kamkeel.npcs.util.ColorUtil.sendResult;

public class CommandProfileChange extends CommandProfileBase {

    @Override
    public String getCommandName() {
        return "change";
    }

    @Override
    public String getDescription() {
        return "Change your active profile slot.";
    }

    @Override
    public String getUsage() {
        return "<slotId>";
    }

    @Override
    public boolean runSubCommands() {
        return false;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if(!(sender instanceof EntityPlayer)) {
            sendError(sender, "This command can only be used by a player.");
            return;
        }
        if(args.length < 1) {
            sendError(sender, "Usage: " + getUsage());
            return;
        }
        int slotId;
        try {
            slotId = Integer.parseInt(args[0]);
        } catch(NumberFormatException ex) {
            sendError(sender, "Slot ID must be a number: " + args[0]);
            return;
        }
        EntityPlayer player = (EntityPlayer)sender;
        ProfileOperation result = ProfileController.changeSlot(player, slotId);
        if(result.getResult() == EnumProfileOperation.SUCCESS) {
            sendResult(sender, "Successfully changed profile slot to %d.", slotId);
        } else if(result.getResult() == EnumProfileOperation.LOCKED) {
            sendError(sender, "Profile is locked. Details: %s", result.getMessage());
        } else if(result.getResult() == EnumProfileOperation.ERROR) {
            sendError(sender, "Error changing your profile slot: %s", result.getMessage());
        } else {
            sendError(sender, "Unexpected error: %s", result.getMessage());
        }
    }
}
