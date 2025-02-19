package kamkeel.npcs.command.profile;

import kamkeel.npcs.controllers.ProfileController;
import kamkeel.npcs.controllers.data.Profile;
import kamkeel.npcs.controllers.data.ProfileOperation;
import kamkeel.npcs.controllers.data.Slot;
import noppes.npcs.LogWriter;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

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
        desc = "Clone a slot from one player's profile to another's.",
        usage = "<sourcePlayer> <destinationPlayer> <sourceSlot> <destinationSlot> [temp]"
    )
    public void clone(ICommandSender sender, String[] args) throws CommandException {
        if(args.length < 4) {
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
        ProfileOperation result = ProfileController.cloneSlot(sourcePlayer, sourceSlot, destinationSlot, temporary);
        switch(result) {
            case SUCCESS:
                sendResult(sender, "Successfully cloned slot %d from %s to slot %d for %s.", sourceSlot, sourcePlayer, destinationSlot, destinationPlayer);
                break;
            case NEW_SLOT_CREATED:
                sendResult(sender, "Successfully cloned slot %d from %s to NEW slot %d for %s.", sourceSlot, sourcePlayer, destinationSlot, destinationPlayer);
                break;
            case LOCKED:
                sendError(sender, "Profile is locked. Please try again later.");
                break;
            case VERIFICATION_FAILED:
                sendError(sender, "Profile verification failed. Change not permitted.");
                break;
            case PLAYER_NOT_FOUND:
                sendError(sender, "Player profile not found.");
                break;
            case ERROR:
            default:
                sendError(sender, "Error cloning slot.");
                break;
        }
    }

    @SubCommand(
        desc = "Remove a specified slot from a player's profile.",
        usage = "<targetPlayer> <slotId>"
    )
    public void remove(ICommandSender sender, String[] args) throws CommandException {
        if(args.length < 2) {
            sendError(sender, "Usage: /profile admin remove <targetPlayer> <slotId>");
            return;
        }
        String targetPlayer = args[0];
        int slotId;
        try {
            slotId = Integer.parseInt(args[1]);
        } catch(NumberFormatException ex) {
            sendError(sender, "Slot ID must be a number.");
            return;
        }
        ProfileOperation result = ProfileController.removeSlot(targetPlayer, slotId);
        switch(result) {
            case SUCCESS:
                sendResult(sender, "Successfully removed slot %d from %s.", slotId, targetPlayer);
                break;
            case LOCKED:
                sendError(sender, "Profile for %s is locked. Please try again later.", targetPlayer);
                break;
            default:
                sendError(sender, "Error removing slot %d from %s.", slotId, targetPlayer);
                break;
        }
    }

    @SubCommand(
        desc = "Change a specified player's active slot.",
        usage = "<targetPlayer> <newSlotId>"
    )
    public void change(ICommandSender sender, String[] args) throws CommandException {
        if(args.length < 2) {
            sendError(sender, "Usage: /profile admin change <targetPlayer> <newSlotId>");
            return;
        }
        String targetPlayer = args[0];
        int newSlotId;
        try {
            newSlotId = Integer.parseInt(args[1]);
        } catch(NumberFormatException ex) {
            sendError(sender, "Slot ID must be a number.");
            return;
        }
        ProfileOperation result = ProfileController.changeSlot(targetPlayer, newSlotId);
        switch(result) {
            case SUCCESS:
                sendResult(sender, "Successfully changed %s's active slot to %d.", targetPlayer, newSlotId);
                break;
            case NEW_SLOT_CREATED:
                sendResult(sender, "Switched %s to NEW slot %d (new profile created).", targetPlayer, newSlotId);
                break;
            case ALREADY_ACTIVE:
                sendError(sender, "Slot %d is already active for %s.", newSlotId, targetPlayer);
                break;
            case LOCKED:
                sendError(sender, "Profile for %s is locked. Please try again later.", targetPlayer);
                break;
            case VERIFICATION_FAILED:
                sendError(sender, "Profile verification failed for %s.", targetPlayer);
                break;
            default:
                sendError(sender, "Error changing active slot for %s.", targetPlayer);
        }
    }

    @SubCommand(
        desc = "List all slots (IDs and names) for a specified player's profile.",
        usage = "<targetPlayer>"
    )
    public void list(ICommandSender sender, String[] args) throws CommandException {
        if(args.length < 1) {
            sendError(sender, "Usage: /profile admin list <targetPlayer>");
            return;
        }
        String targetPlayer = args[0];
        Profile profile = ProfileController.getProfile(targetPlayer);
        if(profile == null) {
            sendError(sender, "Profile not found for player: " + targetPlayer);
            return;
        }
        sendMessage(sender, "Profile Slots for %s:", targetPlayer);
        for(Map.Entry<Integer, Slot> entry : profile.slots.entrySet()) {
            int id = entry.getKey();
            String name = entry.getValue().getName();
            String prefix = (id == profile.currentID) ? "* " : "- ";
            sendMessage(sender, prefix + "Slot " + id + ": " + name);
        }
    }

    @SubCommand(
        desc = "Rename a specified player's slot.",
        usage = "<targetPlayer> <slotId> <newName>"
    )
    public void rename(ICommandSender sender, String[] args) throws CommandException {
        if(args.length < 3) {
            sendError(sender, "Usage: /profile admin rename <targetPlayer> <slotId> <newName>");
            return;
        }
        String targetPlayer = args[0];
        int slotId;
        try {
            slotId = Integer.parseInt(args[1]);
        } catch(NumberFormatException ex) {
            sendError(sender, "Slot ID must be a number.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for(int i = 2; i < args.length; i++){
            if(i > 2) sb.append(" ");
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
        Profile profile = ProfileController.getProfile(targetPlayer);
        if(profile == null) {
            sendError(sender, "Profile not found for player: " + targetPlayer);
            return;
        }
        if(profile.locked) {
            sendError(sender, "Profile for %s is locked. Please try again later.", targetPlayer);
            return;
        }
        if(!profile.slots.containsKey(slotId)) {
            sendError(sender, "Slot %d not found in %s's profile.", slotId, targetPlayer);
            return;
        }
        profile.slots.get(slotId).setName(newName);
        if(profile.player != null) {
            ProfileController.save(profile.player, profile);
        } else {
            UUID uuid = ProfileController.getUUIDFromUsername(targetPlayer);
            if(uuid != null)
                ProfileController.saveOffline(profile, uuid);
        }
        sendResult(sender, "Successfully renamed slot %d for %s to '%s'.", slotId, targetPlayer, newName);
    }

    @SubCommand(
        desc = "Rollback a player's profile to one of their backups.",
        usage = "<targetPlayer> <backupFileName>"
    )
    public void rollback(ICommandSender sender, String[] args) throws CommandException {
        if(args.length < 2) {
            sendError(sender, "Usage: /profile admin rollback <targetPlayer> <backupFileName>");
            return;
        }
        String targetPlayer = args[0];
        String backupFileName = args[1] += ".dat";
        File backupDir = new File(ProfileController.getBackupDir(), ProfileController.getProfile(targetPlayer).player.getUniqueID().toString());
        File backupFile = new File(backupDir, backupFileName);
        if(!backupFile.exists()){
            sendError(sender, "Backup file %s not found for player %s.", backupFileName, targetPlayer);
            return;
        }
        boolean success = ProfileController.rollbackProfile(targetPlayer, backupFile);
        if(success) {
            sendResult(sender, "Successfully rolled back %s's profile to backup %s.", targetPlayer, backupFileName);
        } else {
            sendError(sender, "Failed to rollback %s's profile.", targetPlayer);
        }
    }
}
