package kamkeel.npcs.command.profile;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import noppes.npcs.config.ConfigMain;

import java.util.ArrayList;
import java.util.List;

import static kamkeel.npcs.util.ColorUtil.sendError;
import static kamkeel.npcs.util.ColorUtil.sendMessage;
import static kamkeel.npcs.util.ColorUtil.sendResult;

public class CommandProfileRegion extends CommandProfileBase {

    @Override
    public String getCommandName() {
        return "region";
    }

    @Override
    public String getDescription() {
        return "Manage profile region switching and restricted regions";
    }

    @Override
    public String getUsage() {
        return "<subcommand>";
    }

    @SubCommand(
        desc = "Enable region-based profile switching",
        usage = ""
    )
    public void enable(ICommandSender sender, String[] args) throws CommandException {
        ConfigMain.RegionProfileSwitching = true;
        ConfigMain.RegionProfileSwitchingProperty.set(true);
        if (ConfigMain.config.hasChanged()) {
            ConfigMain.config.save();
        }
        sendResult(sender, "Profile region switching enabled.");
    }

    @SubCommand(
        desc = "Disable region-based profile switching",
        usage = ""
    )
    public void disable(ICommandSender sender, String[] args) throws CommandException {
        ConfigMain.RegionProfileSwitching = false;
        ConfigMain.RegionProfileSwitchingProperty.set(false);
        if (ConfigMain.config.hasChanged()) {
            ConfigMain.config.save();
        }
        sendResult(sender, "Profile region switching disabled.");
    }

    @SubCommand(
        desc = "Add a new region. Format: DIM X1 Y1 Z1 X2 Y2 Z2",
        usage = "<dim> <x1> <y1> <z1> <x2> <y2> <z2>"
    )
    public void add(ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 7) {
            sendError(sender, "Usage: /profile region add <dim> <x1> <y1> <z1> <x2> <y2> <z2>");
            return;
        }
        List<Integer> regionList = new ArrayList<>();
        try {
            for (String arg : args) {
                regionList.add(Integer.parseInt(arg));
            }
        } catch (NumberFormatException e) {
            sendError(sender, "Invalid number format in region data.");
            return;
        }
        ConfigMain.RestrictedProfileRegions.add(regionList);
        // Update config property value
        List<String> regionStrings = new ArrayList<>();
        for (List<Integer> region : ConfigMain.RestrictedProfileRegions) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < region.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(region.get(i));
            }
            regionStrings.add(sb.toString());
        }
        ConfigMain.RestrictedProfileRegionsProperty.set(regionStrings.toArray(new String[0]));
        if (ConfigMain.config.hasChanged()) {
            ConfigMain.config.save();
        }
        sendResult(sender, "Region added successfully.");
    }

    @SubCommand(
        desc = "Remove a region by its index (1-based).",
        usage = "<index>"
    )
    public void remove(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            sendError(sender, "Usage: /profile region remove <index>");
            return;
        }
        int index;
        try {
            index = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            sendError(sender, "Index must be a number.");
            return;
        }
        index = index - 1; // Convert to 0-based index
        if (index < 0 || index >= ConfigMain.RestrictedProfileRegions.size()) {
            sendError(sender, "Invalid index. Must be between 1 and " + ConfigMain.RestrictedProfileRegions.size());
            return;
        }
        ConfigMain.RestrictedProfileRegions.remove(index);
        // Update config property value
        List<String> regionStrings = new ArrayList<>();
        for (List<Integer> region : ConfigMain.RestrictedProfileRegions) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < region.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(region.get(i));
            }
            regionStrings.add(sb.toString());
        }
        ConfigMain.RestrictedProfileRegionsProperty.set(regionStrings.toArray(new String[0]));
        if (ConfigMain.config.hasChanged()) {
            ConfigMain.config.save();
        }
        sendResult(sender, "Region removed successfully.");
    }

    @SubCommand(
        desc = "List all restricted profile regions.",
        usage = ""
    )
    public void list(ICommandSender sender, String[] args) throws CommandException {
        if (ConfigMain.RestrictedProfileRegions.isEmpty()) {
            sendMessage(sender, "No restricted profile regions configured.");
            return;
        }
        sendMessage(sender, "Restricted Profile Regions:");
        int i = 1;
        for (List<Integer> region : ConfigMain.RestrictedProfileRegions) {
            StringBuilder sb = new StringBuilder();
            sb.append(i).append(": ");
            for (int j = 0; j < region.size(); j++) {
                if (j > 0) sb.append(", ");
                sb.append(region.get(j));
            }
            sendMessage(sender, sb.toString());
            i++;
        }
    }
}
