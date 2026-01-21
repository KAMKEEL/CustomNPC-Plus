package kamkeel.npcs.controllers.data.attribute;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.controllers.data.PlayerData;

import java.util.UUID;

/**
 * Utility class for managing item trade restrictions.
 * Checks and sets attributes that control whether items can be traded/auctioned.
 */
public class ItemTradeAttribute {

    // New untradeable attribute - prevents all trading
    public static final String TAG_UNTRADEABLE = "CNPC_Untradeable";

    // Existing requirement keys (from ProfileSlotRequirement and SoulbindRequirement)
    public static final String TAG_PROFILE_SLOT = "cnpc_profile_slot";
    public static final String TAG_SOULBIND = "cnpc_soulbind";

    // ==================== Trade Check Methods ====================

    /**
     * Check if item can be traded/auctioned by the given player.
     * An item cannot be traded if:
     * - It has the Untradeable flag
     * - It is bound to a specific profile slot (ProfileSlot Bound)
     * - It is soulbound to a different player
     *
     * @param item   The item to check
     * @param player The player trying to trade (null to check without player context)
     * @return true if the item can be traded
     */
    public static boolean canTrade(ItemStack item, EntityPlayer player) {
        if (item == null) {
            return true;
        }
        if (!item.hasTagCompound()) {
            return true;
        }

        NBTTagCompound tag = item.getTagCompound();

        // Check untradeable flag - always blocks trading
        if (tag.getBoolean(TAG_UNTRADEABLE)) {
            return false;
        }

        // Check profile slot bound - blocks trading as it's tied to a slot
        if (tag.hasKey(TAG_PROFILE_SLOT)) {
            return false;
        }

        // Check soulbound - blocks trading as it's tied to a player
        if (tag.hasKey(TAG_SOULBIND)) {
            // If we have a player context, check if they're the owner
            if (player != null) {
                String soulbindUUID = tag.getString(TAG_SOULBIND);
                if (soulbindUUID != null && !soulbindUUID.isEmpty()) {
                    // Even if bound to this player, still can't trade
                    return false;
                }
            }
            return false;
        }

        return true;
    }

    /**
     * Check if item can be traded (without player context)
     */
    public static boolean canTrade(ItemStack item) {
        return canTrade(item, null);
    }

    /**
     * Get the reason why an item cannot be traded.
     * Returns a translation key for localization.
     *
     * @param item The item to check
     * @return Translation key for the block reason, or null if tradeable
     */
    public static String getTradeBlockReason(ItemStack item) {
        if (item == null || !item.hasTagCompound()) {
            return null;
        }

        NBTTagCompound tag = item.getTagCompound();

        if (tag.getBoolean(TAG_UNTRADEABLE)) {
            return "gui.auction.untradeable";
        }
        if (tag.hasKey(TAG_PROFILE_SLOT)) {
            return "gui.auction.profileslotbound";
        }
        if (tag.hasKey(TAG_SOULBIND)) {
            return "gui.auction.soulbound";
        }

        return null;
    }

    // ==================== Untradeable Methods ====================

    /**
     * Check if item is marked as untradeable
     */
    public static boolean isUntradeable(ItemStack item) {
        if (item == null || !item.hasTagCompound()) {
            return false;
        }
        return item.getTagCompound().getBoolean(TAG_UNTRADEABLE);
    }

    /**
     * Set the untradeable flag on an item
     */
    public static void setUntradeable(ItemStack item, boolean untradeable) {
        if (item == null) {
            return;
        }
        if (!item.hasTagCompound()) {
            item.setTagCompound(new NBTTagCompound());
        }
        if (untradeable) {
            item.getTagCompound().setBoolean(TAG_UNTRADEABLE, true);
        } else {
            item.getTagCompound().removeTag(TAG_UNTRADEABLE);
        }
    }

    // ==================== Profile Slot Bound Methods ====================

    /**
     * Check if item is bound to a profile slot
     */
    public static boolean isProfileSlotBound(ItemStack item) {
        if (item == null || !item.hasTagCompound()) {
            return false;
        }
        return item.getTagCompound().hasKey(TAG_PROFILE_SLOT);
    }

    /**
     * Get the profile slot an item is bound to
     * @return The profile slot ID, or -1 if not bound
     */
    public static int getProfileSlot(ItemStack item) {
        if (item == null || !item.hasTagCompound()) {
            return -1;
        }
        if (item.getTagCompound().hasKey(TAG_PROFILE_SLOT)) {
            return item.getTagCompound().getInteger(TAG_PROFILE_SLOT);
        }
        return -1;
    }

    /**
     * Bind an item to a specific profile slot
     */
    public static void setProfileSlotBound(ItemStack item, int slot) {
        if (item == null) {
            return;
        }
        if (!item.hasTagCompound()) {
            item.setTagCompound(new NBTTagCompound());
        }
        if (slot >= 0) {
            item.getTagCompound().setInteger(TAG_PROFILE_SLOT, slot);
        } else {
            item.getTagCompound().removeTag(TAG_PROFILE_SLOT);
        }
    }

    /**
     * Bind an item to the player's current profile slot
     */
    public static void bindToCurrentSlot(ItemStack item, EntityPlayer player) {
        if (item == null || player == null) {
            return;
        }
        PlayerData data = PlayerData.get(player);
        if (data != null) {
            setProfileSlotBound(item, data.profileSlot);
        }
    }

    // ==================== Soulbind Methods ====================

    /**
     * Check if item is soulbound to any player
     */
    public static boolean isSoulbound(ItemStack item) {
        if (item == null || !item.hasTagCompound()) {
            return false;
        }
        return item.getTagCompound().hasKey(TAG_SOULBIND);
    }

    /**
     * Check if item is soulbound to a specific player
     */
    public static boolean isSoulboundTo(ItemStack item, EntityPlayer player) {
        if (item == null || player == null || !item.hasTagCompound()) {
            return false;
        }
        if (!item.getTagCompound().hasKey(TAG_SOULBIND)) {
            return false;
        }
        String soulbindUUID = item.getTagCompound().getString(TAG_SOULBIND);
        return player.getUniqueID().toString().equals(soulbindUUID);
    }

    /**
     * Get the UUID of the player the item is soulbound to
     * @return The UUID string, or null if not soulbound
     */
    public static String getSoulbindOwner(ItemStack item) {
        if (item == null || !item.hasTagCompound()) {
            return null;
        }
        if (item.getTagCompound().hasKey(TAG_SOULBIND)) {
            return item.getTagCompound().getString(TAG_SOULBIND);
        }
        return null;
    }

    /**
     * Soulbind an item to a player
     */
    public static void setSoulbound(ItemStack item, EntityPlayer player) {
        if (item == null || player == null) {
            return;
        }
        if (!item.hasTagCompound()) {
            item.setTagCompound(new NBTTagCompound());
        }
        item.getTagCompound().setString(TAG_SOULBIND, player.getUniqueID().toString());
    }

    /**
     * Soulbind an item to a player by UUID
     */
    public static void setSoulbound(ItemStack item, UUID playerUUID) {
        if (item == null || playerUUID == null) {
            return;
        }
        if (!item.hasTagCompound()) {
            item.setTagCompound(new NBTTagCompound());
        }
        item.getTagCompound().setString(TAG_SOULBIND, playerUUID.toString());
    }

    /**
     * Remove soulbind from an item
     */
    public static void removeSoulbind(ItemStack item) {
        if (item == null || !item.hasTagCompound()) {
            return;
        }
        item.getTagCompound().removeTag(TAG_SOULBIND);
    }

    // ==================== Utility Methods ====================

    /**
     * Clear all trade restriction attributes from an item
     */
    public static void clearAllRestrictions(ItemStack item) {
        if (item == null || !item.hasTagCompound()) {
            return;
        }
        NBTTagCompound tag = item.getTagCompound();
        tag.removeTag(TAG_UNTRADEABLE);
        tag.removeTag(TAG_PROFILE_SLOT);
        tag.removeTag(TAG_SOULBIND);
    }

    /**
     * Get a display string describing all trade restrictions on an item
     * @return Human-readable string of restrictions, or empty if none
     */
    public static String getRestrictionSummary(ItemStack item) {
        if (item == null || !item.hasTagCompound()) {
            return "";
        }

        StringBuilder summary = new StringBuilder();
        NBTTagCompound tag = item.getTagCompound();

        if (tag.getBoolean(TAG_UNTRADEABLE)) {
            summary.append("Untradeable");
        }
        if (tag.hasKey(TAG_PROFILE_SLOT)) {
            if (summary.length() > 0) summary.append(", ");
            summary.append("Profile Slot Bound (Slot ").append(tag.getInteger(TAG_PROFILE_SLOT)).append(")");
        }
        if (tag.hasKey(TAG_SOULBIND)) {
            if (summary.length() > 0) summary.append(", ");
            summary.append("Soulbound");
        }

        return summary.toString();
    }
}
