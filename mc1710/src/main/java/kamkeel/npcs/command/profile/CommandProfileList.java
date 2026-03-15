package kamkeel.npcs.command.profile;

import kamkeel.npcs.controllers.ProfileController;
import kamkeel.npcs.controllers.data.profile.Profile;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.api.handler.data.ISlot;

import java.util.Map;

import static kamkeel.npcs.util.ColorUtil.sendError;
import static kamkeel.npcs.util.ColorUtil.sendMessage;

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
        return ""; // No extra parameters.
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (!(sender instanceof EntityPlayer)) {
            sendError(sender, "This command can only be used by a player.");
            return;
        }
        EntityPlayer player = (EntityPlayer) sender;
        Profile profile = ProfileController.Instance.getProfile(player);
        if (profile == null) {
            sendError(sender, "Profile not found.");
            return;
        }
        if (profile.getSlots().isEmpty()) {
            sendMessage(sender, "No slots found. Using default slot 0.");
        }
        sendMessage(sender, "Your Profile Slots:");
        for (Map.Entry<Integer, ISlot> entry : profile.getSlots().entrySet()) {
            int id = entry.getKey();
            String name = entry.getValue().getName();
            String prefix = (id == profile.currentSlotId) ? "* " : "- ";
            sendMessage(sender, prefix + "Slot " + id + ": " + name);
        }
    }
}
