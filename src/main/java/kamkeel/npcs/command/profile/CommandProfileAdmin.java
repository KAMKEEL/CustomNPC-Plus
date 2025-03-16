package kamkeel.npcs.command.profile;

import kamkeel.npcs.controllers.ProfileController;
import kamkeel.npcs.controllers.data.profile.EnumProfileOperation;
import kamkeel.npcs.controllers.data.profile.Profile;
import kamkeel.npcs.controllers.data.profile.ProfileOperation;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import noppes.npcs.api.handler.data.ISlot;

import java.io.File;
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
        return "Admin profile management: clone, remove, change, list, rename, and rollback profiles.";
    }

    @Override
    public String getUsage() {
        return "<subcommand>";
    }

    @SubCommand(
        desc = "Clone a slot from one player's profile to another player.",
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
            sendError(sender, "Slot IDs must be numbers.");
            return;
        }
        boolean temporary = (args.length >= 5 && args[4].equalsIgnoreCase("temp"));
        ProfileOperation result = ProfileController.Instance.cloneSlot(sourcePlayer, sourceSlot, destinationSlot, temporary);
        if (result.getResult() == EnumProfileOperation.SUCCESS) {
            sendResult(sender, "Successfully cloned slot %d from %s to slot %d for %s.", sourceSlot, sourcePlayer, destinationSlot, destinationPlayer);
        } else if (result.getResult() == EnumProfileOperation.LOCKED) {
            // Detail message for admins – note: using result.getMessage() for further details.
            sendError(sender, "Clone failed for %s: Profile is locked. Details: %s", sourcePlayer, result.getMessage());
        } else if (result.getResult() == EnumProfileOperation.ERROR) {
            sendError(sender, "Error cloning slot for %s: %s", sourcePlayer, result.getMessage());
        } else {
            sendError(sender, "Unexpected error cloning slot: %s", result.getMessage());
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
        ProfileOperation result = ProfileController.Instance.removeSlot(targetPlayer, slotId);
        if (result.getResult() == EnumProfileOperation.SUCCESS) {
            sendResult(sender, "Successfully removed slot %d from %s.", slotId, targetPlayer);
        } else if (result.getResult() == EnumProfileOperation.LOCKED) {
            sendError(sender, "Remove failed for %s: Profile is locked. Details: %s", targetPlayer, result.getMessage());
        } else if (result.getResult() == EnumProfileOperation.ERROR) {
            sendError(sender, "Error removing slot %d from %s: %s", slotId, targetPlayer, result.getMessage());
        } else {
            sendError(sender, "Unexpected error during removal: %s", result.getMessage());
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
        ProfileOperation result = ProfileController.Instance.changeSlot(targetPlayer, newSlotId);
        if (result.getResult() == EnumProfileOperation.SUCCESS) {
            sendResult(sender, "Successfully changed %s's active slot to %d.", targetPlayer, newSlotId);
        } else if (result.getResult() == EnumProfileOperation.LOCKED) {
            sendError(sender, "Change failed for %s: Profile is locked. Details: %s", targetPlayer, result.getMessage());
        } else if (result.getResult() == EnumProfileOperation.ERROR) {
            sendError(sender, "Error changing active slot for %s: %s", targetPlayer, result.getMessage());
        } else {
            sendError(sender, "Unexpected error during change: %s", result.getMessage());
        }
    }

    @SubCommand(
        desc = "Create a new slot for a specified player's profile.",
        usage = "<targetPlayer>"
    )
    public void create(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            sendError(sender, "Usage: /profile admin create <targetPlayer>");
            return;
        }
        String targetPlayer = args[0];
        Profile profile = ProfileController.Instance.getProfile(targetPlayer);
        if (profile == null) {
            sendError(sender, "Profile not found for player: %s", targetPlayer);
            return;
        }
        ProfileOperation result = ProfileController.Instance.createSlotInternal(profile);
        if (result.getResult() == EnumProfileOperation.SUCCESS) {
            sendResult(sender, "New slot created successfully for %s.", targetPlayer);
        } else if (result.getResult() == EnumProfileOperation.LOCKED) {
            sendError(sender, "Create failed for %s: Profile is locked. Details: %s", targetPlayer, result.getMessage());
        } else if (result.getResult() == EnumProfileOperation.ERROR) {
            sendError(sender, "Error creating new slot for %s: %s", targetPlayer, result.getMessage());
        } else {
            sendError(sender, "Unexpected error during slot creation: %s", result.getMessage());
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
        Profile profile = ProfileController.Instance.getProfile(targetPlayer);
        if (profile == null) {
            sendError(sender, "Profile not found for player: %s", targetPlayer);
            return;
        }
        sendMessage(sender, "Profile Slots for %s:", targetPlayer);
        for (Map.Entry<Integer, ISlot> entry : profile.getSlots().entrySet()) {
            int id = entry.getKey();
            String name = entry.getValue().getName();
            String prefix = (id == profile.currentSlotId) ? "* " : "- ";
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
        StringBuilder sb = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            if (i > 2) sb.append(" ");
            sb.append(args[i]);
        }
        String newName = sb.toString().trim();
        if (newName.length() > 20) {
            newName = newName.substring(0, 20);
        }
        if (!newName.matches("[A-Za-z ]+")) {
            sendError(sender, "Invalid name. Only alphabetic characters and spaces are allowed.");
            return;
        }
        Profile profile = ProfileController.Instance.getProfile(targetPlayer);
        if (profile == null) {
            sendError(sender, "Profile not found for player: %s", targetPlayer);
            return;
        }
        if (profile.isLocked()) {
            sendError(sender, "Profile for %s is locked. Please try again later.", targetPlayer);
            return;
        }
        if (!profile.getSlots().containsKey(slotId)) {
            sendError(sender, "Slot %d not found in %s's profile.", slotId, targetPlayer);
            return;
        }
        profile.getSlots().get(slotId).setName(newName);
        if (profile.getPlayer() != null) {
            ProfileController.Instance.save(profile.player, profile);
        } else {
            UUID uuid = ProfileController.Instance.getUUIDFromUsername(targetPlayer);
            if (uuid != null)
                ProfileController.Instance.saveOffline(profile, uuid);
        }
        sendResult(sender, "Successfully renamed slot %d for %s to '%s'.", slotId, targetPlayer, newName);
    }

    @SubCommand(
        desc = "Rollback a player's profile to one of their backups.",
        usage = "<targetPlayer> <backupFileName>"
    )
    public void rollback(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) {
            sendError(sender, "Usage: /profile admin rollback <targetPlayer> <backupFileName>");
            return;
        }
        String targetPlayer = args[0];
        String backupFileName = args[1] + ".dat";

        Profile profile = ProfileController.Instance.getProfile(targetPlayer);
        if (profile == null || profile.player == null)
            return;

        File backupDir = new File(ProfileController.getBackupDir(), profile.getPlayer().getUniqueID().toString());
        File backupFile = new File(backupDir, backupFileName);
        if (!backupFile.exists()) {
            sendError(sender, "Backup file %s not found for player %s.", backupFileName, targetPlayer);
            return;
        }
        boolean success = ProfileController.Instance.rollbackProfile(targetPlayer, backupFile);
        if (success) {
            sendResult(sender, "Successfully rolled back %s's profile to backup %s.", targetPlayer, backupFileName);
        } else {
            sendError(sender, "Failed to rollback %s's profile.", targetPlayer);
        }
    }
}
