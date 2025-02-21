package kamkeel.npcs.command.profile;

import kamkeel.npcs.controllers.ProfileController;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import java.util.UUID;
import static kamkeel.npcs.util.ColorUtil.sendError;
import static kamkeel.npcs.util.ColorUtil.sendResult;

public class CommandProfileRename extends CommandProfileBase {

    @Override
    public String getCommandName() {
        return "rename";
    }

    @Override
    public String getDescription() {
        return "Rename one of your profile slots. Only letters and spaces are allowed (max 20 characters).";
    }

    @Override
    public String getUsage() {
        return "<slotId> <newName>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if(!(sender instanceof EntityPlayer)) {
            sendError(sender, "This command can only be used by a player.");
            return;
        }
        if(args.length < 2) {
            sendError(sender, "Usage: " + getUsage());
            return;
        }
        int slotId;
        try {
            slotId = Integer.parseInt(args[0]);
        } catch(NumberFormatException ex) {
            sendError(sender, "Slot ID must be a number.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i < args.length; i++){
            if(i > 1) sb.append(" ");
            sb.append(args[i]);
        }
        String newName = sb.toString().trim();
        if(newName.length() > 20) {
            newName = newName.substring(0, 20);
        }
        if(!newName.matches("[A-Za-z ]+")){
            sendError(sender, "Invalid name. Only alphabetic characters and spaces are allowed.");
            return;
        }
        EntityPlayer player = (EntityPlayer)sender;
        if(ProfileController.getProfile(player) == null) {
            sendError(sender, "Profile not found.");
            return;
        }
        if(!ProfileController.getProfile(player).getSlots().containsKey(slotId)) {
            sendError(sender, "Slot %d not found in your profile.", slotId);
            return;
        }
        ProfileController.getProfile(player).getSlots().get(slotId).setName(newName);
        ProfileController.save(player, ProfileController.getProfile(player));
        sendResult(sender, "Successfully renamed slot %d to '%s'.", slotId, newName);
    }
}
