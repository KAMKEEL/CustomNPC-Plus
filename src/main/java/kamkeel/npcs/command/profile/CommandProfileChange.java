package kamkeel.npcs.command.profile;

import kamkeel.npcs.controllers.ProfileController;
import kamkeel.npcs.controllers.data.Profile;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class CommandProfileChange extends CommandProfileBase {


	@Override
	public String getCommandName() {
		return "change";
	}

	@Override
	public String getDescription() {
		return "change profiles";
	}

	@Override
	public String getUsage() {
		return "<number>";
	}

	@Override
	public boolean runSubCommands(){
		return false;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if(!(sender instanceof EntityPlayer)){
            sendError(sender, "This command can only be sent by a player");
            return;
        }

        int slotID;
        try {
            slotID = Integer.parseInt(args[0]);
        } catch (NumberFormatException ex) {
            sendError(sender, "Slot ID must be a number: " + args[1]);
            return;
        }

        // TODO: Check if the player has permission to switch to this slot
        EntityPlayerMP player = (EntityPlayerMP) sender;

        Profile profile = ProfileController.activeProfiles.get(player.getUniqueID());
        if(profile == null){
            sendError(sender, "Could not find a valid profile file");
            return;
        }

        ProfileController.changePlayerSlot(player, slotID);
        // TODO: Add Cooldown to successful operation
        sendResult(sender, String.format("Switched to Slot \u00A7b%d\u00A77", slotID));
    }
}
