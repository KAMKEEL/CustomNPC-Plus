package kamkeel.npcs.command.profile;

import kamkeel.npcs.controllers.ProfileController;
import kamkeel.npcs.controllers.data.Profile;
import kamkeel.npcs.controllers.data.ProfileOperation;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import static kamkeel.npcs.util.ColorUtil.sendError;
import static kamkeel.npcs.util.ColorUtil.sendResult;

public class CommandProfileCreate extends CommandProfileBase {

    @Override
    public String getCommandName() {
        return "create";
    }

    @Override
    public String getDescription() {
        return "Create a new profile slot.";
    }

    @Override
    public String getUsage() {
        return "";
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
        EntityPlayer player = (EntityPlayer)sender;
        Profile profile = ProfileController.getProfile(player);
        if(profile == null) {
            sendError(sender, "Profile not found.");
            return;
        }
        ProfileOperation result = ProfileController.createSlotInternal(profile);
        switch(result) {
            case NEW_SLOT_CREATED:
                sendResult(sender, "New profile slot created successfully.");
                break;
            case MAX_SLOTS:
                sendError(sender, "You have reached the maximum allowed slots.");
                break;
            case LOCKED:
                sendError(sender, "Profile is locked. Please try again later.");
                break;
            case VERIFICATION_FAILED:
                sendError(sender, "Profile verification failed. Creation not permitted.");
                break;
            default:
                sendError(sender, "Error occurred while creating a new profile slot.");
        }
    }
}
