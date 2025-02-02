package kamkeel.npcs.network;

import kamkeel.npcs.network.enums.EnumItemPacketType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import noppes.npcs.CustomItems;
import noppes.npcs.LogWriter;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class PacketUtil {

    public static boolean verifyItemPacket(EnumItemPacketType type, EntityPlayer player) {
        if (player == null)
            return false;

        ItemStack item = player.inventory.getCurrentItem();
        if (item == null) {
            LogWriter.error(String.format("%s attempted to utilize a %s Packet without an item, they could be a hacker",
                player.getCommandSenderName(), type));
            return false;
        }

        switch (type) {
            case WAND:
                if (item.getItem() != CustomItems.wand) {
                    LogWriter.error(String.format("%s attempted to utilize a %s Packet without a Wand, they could be a hacker",
                        player.getCommandSenderName(), type));
                    return false;
                }
                break;
            case MOUNTER:
                if (item.getItem() != CustomItems.mount) {
                    LogWriter.error(String.format("%s attempted to utilize a %s Packet without a Mounter, they could be a hacker",
                        player.getCommandSenderName(), type));
                    return false;
                }
                break;
            case CLONER:
                if (item.getItem() != CustomItems.cloner) {
                    LogWriter.error(String.format("%s attempted to utilize a %s Packet without a Cloner, they could be a hacker",
                        player.getCommandSenderName(), type));
                    return false;
                }
                break;
            case TELEPORTER:
                if (item.getItem() != CustomItems.teleporter) {
                    LogWriter.error(String.format("%s attempted to utilize a %s Packet without a Teleporter, they could be a hacker",
                        player.getCommandSenderName(), type));
                    return false;
                }
                break;
            case SCRIPTER:
                if (item.getItem() != CustomItems.scripter) {
                    LogWriter.error(String.format("%s attempted to utilize a %s Packet without a Scripter, they could be a hacker",
                        player.getCommandSenderName(), type));
                    return false;
                }
                break;
            case PATHER:
                if (item.getItem() != CustomItems.moving) {
                    LogWriter.error(String.format("%s attempted to utilize a %s Packet without a Pather, they could be a hacker",
                        player.getCommandSenderName(), type));
                    return false;
                }
                break;
            case BLOCK:
                if (item.getItem() == Item.getItemFromBlock(CustomItems.waypoint)
                    || item.getItem() == Item.getItemFromBlock(CustomItems.border)
                    || item.getItem() == Item.getItemFromBlock(CustomItems.redstoneBlock)) {
                    LogWriter.error(String.format("%s attempted to utilize a %s Packet without a valid Block, they could be a hacker",
                        player.getCommandSenderName(), type));
                    return false;
                }
                break;
        }
        return true;
    }

    /**
     * New method that accepts multiple EnumItemPacketTypes.
     * It returns true if any one of the provided types is valid.
     * If none are valid, a merged error message is logged.
     */
    public static boolean verifyItemPacket(EntityPlayer player, EnumItemPacketType... types) {
        if (player == null)
            return false;

        ItemStack item = player.inventory.getCurrentItem();
        if (item == null) {
            LogWriter.error(String.format("%s attempted to utilize a Packet without an item. Expected one of: %s, they could be a hacker",
                player.getCommandSenderName(), getExpectedItemNames(types)));
            return false;
        }

        for (EnumItemPacketType type : types) {
            if (isValidItemForType(item, type)) {
                return true;
            }
        }

        // None passed; log one merged error message listing all expected items.
        LogWriter.error(String.format("%s attempted to utilize a Packet without a valid item. Expected one of: %s, they could be a hacker",
            player.getCommandSenderName(), getExpectedItemNames(types)));
        return false;
    }

    private static boolean isValidItemForType(ItemStack item, EnumItemPacketType type) {
        switch (type) {
            case WAND:
                return item.getItem() == CustomItems.wand;
            case MOUNTER:
                return item.getItem() == CustomItems.mount;
            case CLONER:
                return item.getItem() == CustomItems.cloner;
            case TELEPORTER:
                return item.getItem() == CustomItems.teleporter;
            case SCRIPTER:
                return item.getItem() == CustomItems.scripter;
            case PATHER:
                return item.getItem() == CustomItems.moving;
            case BLOCK:
                // For BLOCK, the item is valid as long as it is NOT one of these blocks.
                return !(item.getItem() == Item.getItemFromBlock(CustomItems.waypoint)
                    || item.getItem() == Item.getItemFromBlock(CustomItems.border)
                    || item.getItem() == Item.getItemFromBlock(CustomItems.redstoneBlock));
            default:
                return false;
        }
    }

    /**
     * Builds a comma‐separated list of expected item names for the given types.
     */
    private static String getExpectedItemNames(EnumItemPacketType... types) {
        Set<String> expectedNames = new LinkedHashSet<>();
        for (EnumItemPacketType type : types) {
            expectedNames.add(getExpectedItemName(type));
        }
        return String.join(", ", expectedNames);
    }

    /**
     * Returns the expected item name for the given EnumItemPacketType.
     */
    private static String getExpectedItemName(EnumItemPacketType type) {
        switch (type) {
            case WAND:
                return "Wand";
            case MOUNTER:
                return "Mounter";
            case CLONER:
                return "Cloner";
            case TELEPORTER:
                return "Teleporter";
            case SCRIPTER:
                return "Scripter";
            case PATHER:
                return "Pather";
            case BLOCK:
                return "valid Block";
            default:
                return type.toString();
        }
    }
}
