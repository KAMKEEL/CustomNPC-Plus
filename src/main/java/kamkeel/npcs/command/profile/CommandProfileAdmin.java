package kamkeel.npcs.command.profile;

import kamkeel.npcs.controllers.ProfileController;
import kamkeel.npcs.controllers.data.Profile;
import kamkeel.npcs.controllers.data.ProfileOperation;
import kamkeel.npcs.controllers.data.Slot;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import noppes.npcs.LogWriter;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Map;
import java.util.UUID;

import static kamkeel.npcs.util.ColorUtil.*;

public class CommandProfileAdmin extends CommandProfileBase {

    @Override
    public String getCommandName() {
        return "admin";
    }

    @Override
    public String getDescription() {
        return "Admin profile management: clone, remove, change, and list a player's slots.";
    }

    @Override
    public String getUsage() {
        return "<subcommand>";
    }

    @SubCommand(
        desc = "Clone a slot from one player's profile to another's.",
        usage = "<sourcePlayer> <destinationPlayer> <sourceSlot> <destinationSlot> [temp]"
    )
    public void clone(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 4) {
            sendError(sender, "Usage: /profile admin clone <sourcePlayer> <destinationPlayer> <sourceSlot> <destinationSlot> [temp]");
            return;
        }
        String sourcePlayer = args[0];
        String destinationPlayer = args[1];
        int sourceSlot, destinationSlot;
        try {
            sourceSlot = Integer.parseInt(args[2]);
            destinationSlot = Integer.parseInt(args[3]);
        } catch (NumberFormatException ex) {
            sendError(sender, "Source and destination slot IDs must be numbers.");
            return;
        }
        boolean temporary = (args.length >= 5 && args[4].equalsIgnoreCase("temp"));

        Profile sourceProfile = ProfileController.getProfile(sourcePlayer);
        if (sourceProfile == null) {
            sendError(sender, "Source profile not found for player: " + sourcePlayer);
            return;
        }
        Profile destProfile = ProfileController.getProfile(destinationPlayer);
        if (destProfile == null) {
            sendError(sender, "Destination profile not found for player: " + destinationPlayer);
            return;
        }
        if (destProfile.locked) {
            sendError(sender, "Destination profile is locked. Please try again later.");
            return;
        }
        if (!sourceProfile.slots.containsKey(sourceSlot)) {
            sendError(sender, "Source slot " + sourceSlot + " does not exist for player: " + sourcePlayer);
            return;
        }
        if (destinationSlot == destProfile.currentID) {
            sendError(sender, "Cannot clone into the destination's active slot.");
            return;
        }
        try {
            Slot sourceSlotObj = sourceProfile.slots.get(sourceSlot);
            if (sourceSlotObj == null) {
                sendError(sender, "Source slot is invalid.");
                return;
            }
            NBTTagCompound clonedData = (NBTTagCompound) sourceSlotObj.getCompound().copy();
            Slot clonedSlot = new Slot(destinationSlot, "Admin Cloned Slot " + destinationSlot, clonedData, System.currentTimeMillis(), temporary);
            destProfile.slots.put(destinationSlot, clonedSlot);

            if (destProfile.player != null) {
                ProfileController.save(destProfile.player, destProfile);
            } else {
                UUID destUUID = ProfileController.getUUIDFromUsername(destinationPlayer);
                if (destUUID != null)
                    ProfileController.saveOffline(destProfile, destUUID);
            }
            sendResult(sender, "Successfully cloned slot %d from %s to slot %d for %s%s.",
                sourceSlot, sourcePlayer, destinationSlot, destinationPlayer, (temporary ? " (temporary)" : ""));
        } catch (Exception e) {
            LogWriter.error("Error cloning slot", e);
            sendError(sender, "An error occurred while cloning slot.");
        }
    }

    @SubCommand(
        desc = "Remove a specified slot from a player's profile.",
        usage = "<targetPlayer> <slotId>"
    )
    public void remove(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) {
            sendError(sender, "Usage: /profile admin remove <targetPlayer> <slotId>");
            return;
        }
        String targetPlayer = args[0];
        int slotId;
        try {
            slotId = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            sendError(sender, "Slot ID must be a number.");
            return;
        }
        Profile targetProfile = ProfileController.getProfile(targetPlayer);
        if (targetProfile == null) {
            sendError(sender, "Profile not found for player: " + targetPlayer);
            return;
        }
        if (targetProfile.locked) {
            sendError(sender, "Profile for " + targetPlayer + " is locked. Please try again later.");
            return;
        }
        ProfileOperation result = ProfileController.removeSlot(targetPlayer, slotId);
        switch (result) {
            case SUCCESS:
                sendResult(sender, "Successfully removed slot %d from %s.", slotId, targetPlayer);
                break;
            case LOCKED:
                sendError(sender, "Profile for " + targetPlayer + " is locked.");
                break;
            default:
                sendError(sender, "Error removing slot %d from %s.", slotId, targetPlayer);
        }
    }

    @SubCommand(
        desc = "Change a specified player's active slot.",
        usage = "<targetPlayer> <newSlotId>"
    )
    public void change(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) {
            sendError(sender, "Usage: /profile admin change <targetPlayer> <newSlotId>");
            return;
        }
        String targetPlayer = args[0];
        int newSlotId;
        try {
            newSlotId = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            sendError(sender, "Slot ID must be a number.");
            return;
        }
        Profile targetProfile = ProfileController.getProfile(targetPlayer);
        if (targetProfile == null) {
            sendError(sender, "Profile not found for player: " + targetPlayer);
            return;
        }
        if (targetProfile.locked) {
            sendError(sender, "Profile for " + targetPlayer + " is locked. Please try again later.");
            return;
        }
        ProfileOperation result = ProfileController.changeSlot(targetPlayer, newSlotId);
        switch (result) {
            case SUCCESS:
                sendResult(sender, "Successfully changed %s's active slot to %d.", targetPlayer, newSlotId);
                break;
            case LOCKED:
                sendError(sender, "Profile for " + targetPlayer + " is locked.");
                break;
            default:
                sendError(sender, "Error changing active slot for " + targetPlayer);
        }
    }

    @SubCommand(
        desc = "List all slots (IDs and names) for a specified player's profile.",
        usage = "<targetPlayer>"
    )
    public void list(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            sendError(sender, "Usage: /profile admin list <targetPlayer>");
            return;
        }
        String targetPlayer = args[0];
        Profile targetProfile = ProfileController.getProfile(targetPlayer);
        if (targetProfile == null) {
            sendError(sender, "Profile not found for player: " + targetPlayer);
            return;
        }
        sendMessage(sender, "Profile Slots for " + targetPlayer + ":");
        for (Map.Entry<Integer, Slot> entry : targetProfile.slots.entrySet()) {
            int id = entry.getKey();
            String name = entry.getValue().getName();
            String prefix = (id == targetProfile.currentID) ? "* " : "- ";
            sendMessage(sender, prefix + "Slot " + id + ": " + name);
        }
    }

    @SubCommand(
        desc = "Rename a specified player's slot.",
        usage = "<targetPlayer> <slotId> <newName>"
    )
    public void rename(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 3) {
            sendError(sender, "Usage: /profile admin rename <targetPlayer> <slotId> <newName>");
            return;
        }
        String targetPlayer = args[0];
        int slotId;
        try {
            slotId = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            sendError(sender, "Slot ID must be a number.");
            return;
        }
        // Combine remaining arguments for the new name.
        StringBuilder sb = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            if (i > 2) sb.append(" ");
            sb.append(args[i]);
        }
        String newName = sb.toString().trim();
        // Cap name at 20 characters.
        if (newName.length() > 20) {
            newName = newName.substring(0, 20);
        }
        // Validate that name contains only letters and spaces.
        if (!newName.matches("[A-Za-z ]+")) {
            sendError(sender, "Invalid name. Only alphabetic characters and spaces are allowed.");
            return;
        }
        Profile targetProfile = ProfileController.getProfile(targetPlayer);
        if (targetProfile == null) {
            sendError(sender, "Profile not found for player: " + targetPlayer);
            return;
        }
        if (targetProfile.locked) {
            sendError(sender, "Profile for " + targetPlayer + " is locked. Please try again later.");
            return;
        }
        if (!targetProfile.slots.containsKey(slotId)) {
            sendError(sender, "Slot " + slotId + " not found in " + targetPlayer + "'s profile.");
            return;
        }
        // Rename the slot.
        Slot targetSlot = targetProfile.slots.get(slotId);
        targetSlot.setName(newName);
        // Save the updated profile.
        if (targetProfile.player != null) {
            ProfileController.save(targetProfile.player, targetProfile);
        } else {
            UUID uuid = ProfileController.getUUIDFromUsername(targetPlayer);
            if (uuid != null)
                ProfileController.saveOffline(targetProfile, uuid);
        }
        sendResult(sender, "Successfully renamed slot %s for %s to '%s'.", slotId, targetPlayer, newName);
    }
}
