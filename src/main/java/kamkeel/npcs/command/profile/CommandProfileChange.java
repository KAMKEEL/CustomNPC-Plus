package kamkeel.npcs.command.profile;

import kamkeel.npcs.controllers.ProfileController;
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
        switch(result) {
            case SUCCESS:
                sendResult(sender, "Successfully changed profile slot to %d.", slotId);
                break;
            case NEW_SLOT_CREATED:
                sendResult(sender, "Switched to new slot %d (new profile created).", slotId);
                break;
            case ALREADY_ACTIVE:
                sendError(sender, "Slot %d is already active.", slotId);
                break;
            case LOCKED:
                sendError(sender, "Profile is locked. Please try again later.");
                break;
            case VERIFICATION_FAILED:
                sendError(sender, "Profile verification failed. Change not permitted.");
                break;
            default:
                sendError(sender, "Error occurred while changing your profile slot.");
        }
    }
}
