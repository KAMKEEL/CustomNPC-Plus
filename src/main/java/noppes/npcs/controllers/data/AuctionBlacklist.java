package noppes.npcs.controllers.data;

import cpw.mods.fml.common.registry.GameRegistry;
import kamkeel.npcs.util.AttributeItemUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.config.ConfigMarket;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.Iterator;

public class AuctionBlacklist {
    private static volatile Set<String> blacklistedItems = new HashSet<>();
    private static volatile Set<Pattern> wildcardPatterns = new HashSet<>();
    private static volatile Set<String> blacklistedMods = new HashSet<>();
    private static volatile Set<String> blacklistedNBTTags = new HashSet<>();

    // Built-in bound item requirement keys (always blocked)
    private static final String SOULBIND_KEY = "cnpc_soulbind";
    private static final String PROFILE_SLOT_KEY = "cnpc_profile_slot";

    // Bypass permission
    private static final String BYPASS_PERMISSION = "customnpcs.auction.blacklist.bypass";

    public static void reload() {
        Set<String> newItems = new HashSet<>();
        Set<Pattern> newWildcards = new HashSet<>();
        Set<String> newMods = new HashSet<>();
        Set<String> newNBTTags = new HashSet<>();

        // Load blacklisted items
        for (String item : ConfigMarket.BlacklistedItems) {
            if (item == null || item.isEmpty()) continue;
            item = item.toLowerCase().trim();

            if (item.contains("*")) {
                String regex = "^" + item.replace(".", "\\.").replace("*", ".*") + "$";
                newWildcards.add(Pattern.compile(regex));
            } else {
                newItems.add(item);
            }
        }

        // Load blacklisted mods
        for (String mod : ConfigMarket.BlacklistedMods) {
            if (mod == null || mod.isEmpty()) continue;
            newMods.add(mod.toLowerCase().trim());
        }

        // Load blacklisted NBT tags
        for (String tag : ConfigMarket.BlacklistedNBTTags) {
            if (tag == null || tag.isEmpty()) continue;
            newNBTTags.add(tag.trim());
        }

        // Atomic swap
        blacklistedItems = newItems;
        wildcardPatterns = newWildcards;
        blacklistedMods = newMods;
        blacklistedNBTTags = newNBTTags;
    }

    /**
     * Check if player can bypass the blacklist (OP or has permission)
     */
    public static boolean canBypass(EntityPlayer player) {
        if (player == null) return false;

        // Check OP status
        if (player.canCommandSenderUseCommand(2, BYPASS_PERMISSION)) {
            return true;
        }

        // Check custom permission
        return CustomNpcsPermissions.hasCustomPermission(player, BYPASS_PERMISSION);
    }

    /**
     * Check if item is blacklisted (without bypass check)
     */
    public static boolean isBlacklisted(ItemStack item) {
        if (item == null || item.getItem() == null) return true;

        // Check bound items (always blocked, even if blacklist is "disabled")
        if (hasBoundRequirement(item)) {
            return true;
        }

        if (!ConfigMarket.BlacklistEnabled) return false;

        // Get registry name
        GameRegistry.UniqueIdentifier uid = GameRegistry.findUniqueIdentifierFor(item.getItem());
        if (uid == null) return true;

        String registryName = (uid.modId + ":" + uid.name).toLowerCase();
        String modId = uid.modId.toLowerCase();

        // Check exact item match
        if (blacklistedItems.contains(registryName)) {
            return true;
        }

        // Check wildcard patterns
        for (Pattern pattern : wildcardPatterns) {
            if (pattern.matcher(registryName).matches()) {
                return true;
            }
        }

        // Check mod blacklist
        if (blacklistedMods.contains(modId)) {
            return true;
        }

        // Check NBT tags
        if (item.hasTagCompound() && !blacklistedNBTTags.isEmpty()) {
            NBTTagCompound tag = item.getTagCompound();
            for (String nbtKey : blacklistedNBTTags) {
                if (tag.hasKey(nbtKey)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Check if item is blacklisted for a specific player (with bypass check)
     */
    public static boolean isBlacklistedForPlayer(ItemStack item, EntityPlayer player) {
        if (canBypass(player)) return false;
        return isBlacklisted(item);
    }

    /**
     * Get the registry name of an item
     */
    public static String getRegistryName(ItemStack item) {
        if (item == null || item.getItem() == null) return null;
        GameRegistry.UniqueIdentifier uid = GameRegistry.findUniqueIdentifierFor(item.getItem());
        if (uid == null) return null;
        return uid.modId + ":" + uid.name;
    }

    // =========================================
    // Bound Item Checking (RPGCore/Requirements)
    // =========================================

    /**
     * Check if item has soulbind or profile slot requirement.
     * Requirements are stored under: item.stackTagCompound/RPGCore/Requirements
     */
    private static boolean hasBoundRequirement(ItemStack item) {
        if (item == null || !item.hasTagCompound()) return false;

        NBTTagCompound root = item.getTagCompound();
        if (!root.hasKey(AttributeItemUtil.TAG_RPGCORE)) return false;

        NBTTagCompound rpgCore = root.getCompoundTag(AttributeItemUtil.TAG_RPGCORE);
        if (!rpgCore.hasKey(AttributeItemUtil.TAG_REQUIREMENTS)) return false;

        NBTTagCompound requirements = rpgCore.getCompoundTag(AttributeItemUtil.TAG_REQUIREMENTS);
        return requirements.hasKey(SOULBIND_KEY) || requirements.hasKey(PROFILE_SLOT_KEY);
    }

    // =========================================
    // Runtime Blacklist Management
    // =========================================

    public static boolean addItem(String registryName) {
        if (registryName == null || registryName.isEmpty()) return false;
        registryName = registryName.toLowerCase().trim();

        if (registryName.contains("*")) {
            String regex = "^" + registryName.replace(".", "\\.").replace("*", ".*") + "$";
            Set<Pattern> newWildcards = new HashSet<>(wildcardPatterns);
            newWildcards.add(Pattern.compile(regex));
            wildcardPatterns = newWildcards;
        } else {
            Set<String> newItems = new HashSet<>(blacklistedItems);
            newItems.add(registryName);
            blacklistedItems = newItems;
        }
        return true;
    }

    public static boolean removeItem(String registryName) {
        if (registryName == null || registryName.isEmpty()) return false;
        registryName = registryName.toLowerCase().trim();

        if (registryName.contains("*")) {
            String regex = "^" + registryName.replace(".", "\\.").replace("*", ".*") + "$";
            Set<Pattern> newWildcards = new HashSet<>(wildcardPatterns);
            boolean removed = false;
            Iterator<Pattern> it = newWildcards.iterator();
            while (it.hasNext()) {
                if (it.next().pattern().equals(regex)) {
                    it.remove();
                    removed = true;
                }
            }
            wildcardPatterns = newWildcards;
            return removed;
        } else {
            Set<String> newItems = new HashSet<>(blacklistedItems);
            boolean removed = newItems.remove(registryName);
            blacklistedItems = newItems;
            return removed;
        }
    }

    public static boolean addMod(String modId) {
        if (modId == null || modId.isEmpty()) return false;
        Set<String> newMods = new HashSet<>(blacklistedMods);
        boolean added = newMods.add(modId.toLowerCase().trim());
        blacklistedMods = newMods;
        return added;
    }

    public static boolean removeMod(String modId) {
        if (modId == null || modId.isEmpty()) return false;
        Set<String> newMods = new HashSet<>(blacklistedMods);
        boolean removed = newMods.remove(modId.toLowerCase().trim());
        blacklistedMods = newMods;
        return removed;
    }

    public static boolean addNBTTag(String tag) {
        if (tag == null || tag.isEmpty()) return false;
        Set<String> newTags = new HashSet<>(blacklistedNBTTags);
        boolean added = newTags.add(tag.trim());
        blacklistedNBTTags = newTags;
        return added;
    }

    public static boolean removeNBTTag(String tag) {
        if (tag == null || tag.isEmpty()) return false;
        Set<String> newTags = new HashSet<>(blacklistedNBTTags);
        boolean removed = newTags.remove(tag.trim());
        blacklistedNBTTags = newTags;
        return removed;
    }

    // =========================================
    // Getters for listing
    // =========================================

    public static List<String> getBlacklistedItems() {
        List<String> result = new ArrayList<>(blacklistedItems);
        for (Pattern p : wildcardPatterns) {
            // Convert regex back to wildcard format
            String pattern = p.pattern();
            pattern = pattern.substring(1, pattern.length() - 1); // Remove ^ and $
            pattern = pattern.replace("\\.", ".").replace(".*", "*");
            result.add(pattern);
        }
        return result;
    }

    public static List<String> getBlacklistedMods() {
        return new ArrayList<>(blacklistedMods);
    }

    public static List<String> getBlacklistedNBTTags() {
        return new ArrayList<>(blacklistedNBTTags);
    }
}
