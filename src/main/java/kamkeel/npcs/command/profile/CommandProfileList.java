package kamkeel.npcs.command.profile;

import kamkeel.npcs.controllers.ProfileController;
import kamkeel.npcs.controllers.data.Profile;
import kamkeel.npcs.controllers.data.Slot;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Map;

public class CommandProfileList extends CommandProfileBase {

    @Override
    public String getCommandName() {
        return "list";
    }

    @Override
    public String getDescription() {
        return "List all your current profile slots (IDs and names).";
    }

    @Override
    public String getUsage() {
        return ""; // No extra parameters
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (!(sender instanceof EntityPlayer)) {
            sendError(sender, "This command can only be used by a player.");
            return;
        }
        EntityPlayer player = (EntityPlayer) sender;
        Profile profile = ProfileController.getProfile(player);
        if (profile == null) {
            sendError(sender, "Profile not found.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Your Profile Slots:\n");
        for (Map.Entry<Integer, Slot> entry : profile.slots.entrySet()) {
            int id = entry.getKey();
            String name = entry.getValue().getName();
            if (id == profile.currentID) {
                sb.append(" * "); // mark active slot
            } else {
                sb.append(" - ");
            }
            sb.append("Slot ").append(id).append(": ").append(name).append("\n");
        }
        sendMessage(sender, sb.toString());
    }
}
