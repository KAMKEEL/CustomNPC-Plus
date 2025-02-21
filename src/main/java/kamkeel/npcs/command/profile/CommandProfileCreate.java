package kamkeel.npcs.command.profile;

import kamkeel.npcs.controllers.ProfileController;
import kamkeel.npcs.controllers.data.EnumProfileOperation;
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
        if(ProfileController.Instance.getProfile(player) == null) {
            sendError(sender, "Profile not found.");
            return;
        }
        ProfileOperation result = ProfileController.Instance.createSlotInternal(ProfileController.Instance.getProfile(player));
        if(result.getResult() == EnumProfileOperation.SUCCESS) {
            sendResult(sender, "New profile slot created successfully.");
        } else if(result.getResult() == EnumProfileOperation.LOCKED) {
            sendError(sender, "Profile is locked. Details: %s", result.getMessage());
        } else if(result.getResult() == EnumProfileOperation.ERROR) {
            sendError(sender, "Error creating new profile slot: %s", result.getMessage());
        } else {
            sendError(sender, "Unexpected error: %s", result.getMessage());
        }
    }
}
