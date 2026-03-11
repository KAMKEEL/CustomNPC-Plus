package kamkeel.npcs.command.auction;

import kamkeel.npcs.command.CommandKamkeelBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import noppes.npcs.controllers.data.AuctionBlacklist;

import java.util.List;

import static kamkeel.npcs.util.ColorUtil.sendError;
import static kamkeel.npcs.util.ColorUtil.sendMessage;
import static kamkeel.npcs.util.ColorUtil.sendResult;

public class BlacklistSubCommand extends CommandKamkeelBase {

    @Override
    public String getCommandName() {
        return "blacklist";
    }

    @Override
    public String getDescription() {
        return "Manage auction item blacklist";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        // Show help when called with no subcommand
        sendHelp(sender);
    }

    private void sendHelp(ICommandSender sender) {
        sendMessage(sender, "\u00A76=== Auction Blacklist Commands ===");
        sendMessage(sender, "\u00A7e/cnpc auction blacklist add <item|mod|nbt> <value>");
        sendMessage(sender, "\u00A7e/cnpc auction blacklist remove <item|mod|nbt> <value>");
        sendMessage(sender, "\u00A7e/cnpc auction blacklist list [item|mod|nbt]");
        sendMessage(sender, "\u00A7e/cnpc auction blacklist reload");
        sendMessage(sender, "\u00A7e/cnpc auction blacklist check \u00A77(checks held item)");
    }

    @SubCommand(
        desc = "Add item, mod, or NBT tag to blacklist",
        usage = "<item|mod|nbt> <value>"
    )
    public void add(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) {
            sendError(sender, "Usage: /cnpc auction blacklist add <item|mod|nbt> <value>");
            return;
        }

        String type = args[0].toLowerCase();
        String value = args[1];

        switch (type) {
            case "item":
                if (AuctionBlacklist.addItem(value)) {
                    sendResult(sender, "Added item to blacklist: " + value);
                } else {
                    sendError(sender, "Failed to add item to blacklist");
                }
                break;
            case "mod":
                if (AuctionBlacklist.addMod(value)) {
                    sendResult(sender, "Added mod to blacklist: " + value);
                } else {
                    sendError(sender, "Failed to add mod to blacklist");
                }
                break;
            case "nbt":
                if (AuctionBlacklist.addNBTTag(value)) {
                    sendResult(sender, "Added NBT tag to blacklist: " + value);
                } else {
                    sendError(sender, "Failed to add NBT tag to blacklist");
                }
                break;
            default:
                sendError(sender, "Unknown type: " + type + ". Use: item, mod, or nbt");
        }
    }

    @SubCommand(
        desc = "Remove item, mod, or NBT tag from blacklist",
        usage = "<item|mod|nbt> <value>"
    )
    public void remove(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) {
            sendError(sender, "Usage: /cnpc auction blacklist remove <item|mod|nbt> <value>");
            return;
        }

        String type = args[0].toLowerCase();
        String value = args[1];

        switch (type) {
            case "item":
                if (AuctionBlacklist.removeItem(value)) {
                    sendResult(sender, "Removed item from blacklist: " + value);
                } else {
                    sendError(sender, "Item not found in blacklist: " + value);
                }
                break;
            case "mod":
                if (AuctionBlacklist.removeMod(value)) {
                    sendResult(sender, "Removed mod from blacklist: " + value);
                } else {
                    sendError(sender, "Mod not found in blacklist: " + value);
                }
                break;
            case "nbt":
                if (AuctionBlacklist.removeNBTTag(value)) {
                    sendResult(sender, "Removed NBT tag from blacklist: " + value);
                } else {
                    sendError(sender, "NBT tag not found in blacklist: " + value);
                }
                break;
            default:
                sendError(sender, "Unknown type: " + type + ". Use: item, mod, or nbt");
        }
    }

    @SubCommand(
        desc = "List blacklisted items, mods, or NBT tags",
        usage = "[item|mod|nbt]"
    )
    public void list(ICommandSender sender, String[] args) throws CommandException {
        String filter = args.length > 0 ? args[0].toLowerCase() : "all";

        if (filter.equals("all") || filter.equals("item")) {
            List<String> items = AuctionBlacklist.getBlacklistedItems();
            sendMessage(sender, "\u00A76=== Blacklisted Items (" + items.size() + ") ===");
            if (items.isEmpty()) {
                sendMessage(sender, "\u00A77  (none)");
            } else {
                for (String item : items) {
                    sendMessage(sender, "\u00A7e  " + item);
                }
            }
        }

        if (filter.equals("all") || filter.equals("mod")) {
            List<String> mods = AuctionBlacklist.getBlacklistedMods();
            sendMessage(sender, "\u00A76=== Blacklisted Mods (" + mods.size() + ") ===");
            if (mods.isEmpty()) {
                sendMessage(sender, "\u00A77  (none)");
            } else {
                for (String mod : mods) {
                    sendMessage(sender, "\u00A7e  " + mod);
                }
            }
        }

        if (filter.equals("all") || filter.equals("nbt")) {
            List<String> tags = AuctionBlacklist.getBlacklistedNBTTags();
            sendMessage(sender, "\u00A76=== Blacklisted NBT Tags (" + tags.size() + ") ===");
            if (tags.isEmpty()) {
                sendMessage(sender, "\u00A77  (none)");
            } else {
                for (String tag : tags) {
                    sendMessage(sender, "\u00A7e  " + tag);
                }
            }
        }

        sendMessage(sender, "\u00A77Note: Soulbound and Profile-Slotbound items are always blocked.");
    }

    @SubCommand(
        desc = "Reload blacklist from config"
    )
    public void reload(ICommandSender sender, String[] args) throws CommandException {
        AuctionBlacklist.reload();
        sendResult(sender, "Blacklist reloaded from config");
    }

    @SubCommand(
        desc = "Check if held item is blacklisted",
        permission = 0
    )
    public void check(ICommandSender sender, String[] args) throws CommandException {
        if (!(sender instanceof EntityPlayer)) {
            sendError(sender, "This command can only be used by players");
            return;
        }

        EntityPlayer player = (EntityPlayer) sender;
        ItemStack heldItem = player.getHeldItem();

        if (heldItem == null) {
            sendError(sender, "You are not holding an item");
            return;
        }

        String registryName = AuctionBlacklist.getRegistryName(heldItem);
        sendMessage(sender, "\u00A76=== Blacklist Check ===");
        sendMessage(sender, "\u00A77Item: \u00A7e" + heldItem.getDisplayName());
        sendMessage(sender, "\u00A77Registry: \u00A7e" + (registryName != null ? registryName : "unknown"));

        boolean isBlacklisted = AuctionBlacklist.isBlacklisted(heldItem);
        boolean canBypass = AuctionBlacklist.canBypass(player);

        if (isBlacklisted) {
            sendMessage(sender, "\u00A7cStatus: BLACKLISTED");
            if (canBypass) {
                sendMessage(sender, "\u00A7aYou have bypass permission");
            }
        } else {
            sendMessage(sender, "\u00A7aStatus: ALLOWED");
        }
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, getAllSubCommandNames());
        }
        if (args.length == 2) {
            String subCmd = args[0].toLowerCase();
            if (subCmd.equals("add") || subCmd.equals("remove") || subCmd.equals("list")) {
                return getListOfStringsMatchingLastWord(args, new String[]{"item", "mod", "nbt"});
            }
        }
        return null;
    }
}
