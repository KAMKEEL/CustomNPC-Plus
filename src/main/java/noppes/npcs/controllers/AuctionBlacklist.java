package noppes.npcs.controllers;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import noppes.npcs.CustomNpcs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Controller for managing auction house item blacklisting.
 * Allows administrators to block specific items, entire mods,
 * or items with certain NBT tags from being listed.
 */
public class AuctionBlacklist {
    private static final Logger logger = LogManager.getLogger(CustomNpcs.class);

    public static AuctionBlacklist Instance;

    // Blacklist by item registry name (e.g., "minecraft:diamond_sword")
    private Set<String> blacklistedItems = new HashSet<>();

    // Blacklist by mod ID (e.g., "minecraft" blocks all vanilla items)
    private Set<String> blacklistedMods = new HashSet<>();

    // Blacklist by NBT tag presence (e.g., items with specific custom data)
    private Set<String> blacklistedNBTTags = new HashSet<>();

    public AuctionBlacklist() {
        Instance = this;
        load();
    }

    // ==================== Blacklist Checking ====================

    /**
     * Check if an item is blacklisted from the auction house.
     *
     * @param item The item to check
     * @return true if the item is blacklisted
     */
    public boolean isBlacklisted(ItemStack item) {
        if (item == null || item.getItem() == null) {
            return false;
        }

        // Check item registry name
        String itemName = Item.itemRegistry.getNameForObject(item.getItem());
        if (itemName != null) {
            // Direct item blacklist
            if (blacklistedItems.contains(itemName)) {
                return true;
            }

            // Also check with metadata for specific variants
            String itemNameWithMeta = itemName + ":" + item.getItemDamage();
            if (blacklistedItems.contains(itemNameWithMeta)) {
                return true;
            }

            // Check mod ID
            String modId = getModIdFromName(itemName);
            if (modId != null && blacklistedMods.contains(modId)) {
                return true;
            }
        }

        // Check NBT tags
        if (item.hasTagCompound()) {
            NBTTagCompound tag = item.getTagCompound();
            for (String nbtTag : blacklistedNBTTags) {
                if (hasNestedTag(tag, nbtTag)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Get the reason why an item is blacklisted
     * @return Description of why blacklisted, or null if not blacklisted
     */
    public String getBlacklistReason(ItemStack item) {
        if (item == null || item.getItem() == null) {
            return null;
        }

        String itemName = Item.itemRegistry.getNameForObject(item.getItem());
        if (itemName != null) {
            if (blacklistedItems.contains(itemName)) {
                return "Item is blacklisted: " + itemName;
            }

            String itemNameWithMeta = itemName + ":" + item.getItemDamage();
            if (blacklistedItems.contains(itemNameWithMeta)) {
                return "Item variant is blacklisted: " + itemNameWithMeta;
            }

            String modId = getModIdFromName(itemName);
            if (modId != null && blacklistedMods.contains(modId)) {
                return "Mod is blacklisted: " + modId;
            }
        }

        if (item.hasTagCompound()) {
            NBTTagCompound tag = item.getTagCompound();
            for (String nbtTag : blacklistedNBTTags) {
                if (hasNestedTag(tag, nbtTag)) {
                    return "Item has blacklisted NBT tag: " + nbtTag;
                }
            }
        }

        return null;
    }

    /**
     * Extract mod ID from registry name (e.g., "minecraft" from "minecraft:stone")
     */
    private String getModIdFromName(String registryName) {
        if (registryName == null) {
            return null;
        }
        int colonIndex = registryName.indexOf(':');
        if (colonIndex > 0) {
            return registryName.substring(0, colonIndex);
        }
        return null;
    }

    /**
     * Check for nested NBT tag (supports dot notation like "display.Name")
     */
    private boolean hasNestedTag(NBTTagCompound tag, String path) {
        if (tag == null || path == null || path.isEmpty()) {
            return false;
        }

        String[] parts = path.split("\\.");
        NBTTagCompound current = tag;

        for (int i = 0; i < parts.length - 1; i++) {
            if (!current.hasKey(parts[i])) {
                return false;
            }
            if (current.hasKey(parts[i], 10)) { // 10 = Compound tag
                current = current.getCompoundTag(parts[i]);
            } else {
                return false;
            }
        }

        return current.hasKey(parts[parts.length - 1]);
    }

    // ==================== Item Blacklist Management ====================

    /**
     * Add an item to the blacklist by registry name
     * @param itemName Registry name (e.g., "minecraft:diamond_sword" or "minecraft:wool:14")
     */
    public void addItem(String itemName) {
        if (itemName != null && !itemName.isEmpty()) {
            blacklistedItems.add(itemName);
            save();
            logger.info("Added item to auction blacklist: " + itemName);
        }
    }

    /**
     * Add an item to the blacklist from an ItemStack
     */
    public void addItem(ItemStack item, boolean includeMeta) {
        if (item == null || item.getItem() == null) {
            return;
        }
        String itemName = Item.itemRegistry.getNameForObject(item.getItem());
        if (itemName != null) {
            if (includeMeta && item.getHasSubtypes()) {
                addItem(itemName + ":" + item.getItemDamage());
            } else {
                addItem(itemName);
            }
        }
    }

    /**
     * Remove an item from the blacklist
     */
    public void removeItem(String itemName) {
        if (blacklistedItems.remove(itemName)) {
            save();
            logger.info("Removed item from auction blacklist: " + itemName);
        }
    }

    /**
     * Check if a specific item is blacklisted
     */
    public boolean isItemBlacklisted(String itemName) {
        return blacklistedItems.contains(itemName);
    }

    /**
     * Get all blacklisted items
     */
    public Set<String> getBlacklistedItems() {
        return new HashSet<>(blacklistedItems);
    }

    // ==================== Mod Blacklist Management ====================

    /**
     * Add an entire mod to the blacklist
     * @param modId The mod ID (e.g., "minecraft", "customnpcs")
     */
    public void addMod(String modId) {
        if (modId != null && !modId.isEmpty()) {
            blacklistedMods.add(modId);
            save();
            logger.info("Added mod to auction blacklist: " + modId);
        }
    }

    /**
     * Remove a mod from the blacklist
     */
    public void removeMod(String modId) {
        if (blacklistedMods.remove(modId)) {
            save();
            logger.info("Removed mod from auction blacklist: " + modId);
        }
    }

    /**
     * Check if a mod is blacklisted
     */
    public boolean isModBlacklisted(String modId) {
        return blacklistedMods.contains(modId);
    }

    /**
     * Get all blacklisted mods
     */
    public Set<String> getBlacklistedMods() {
        return new HashSet<>(blacklistedMods);
    }

    // ==================== NBT Tag Blacklist Management ====================

    /**
     * Add an NBT tag to the blacklist
     * Items containing this tag will be blocked
     * @param tagPath The tag path (e.g., "display.Name" or "CustomData")
     */
    public void addNBTTag(String tagPath) {
        if (tagPath != null && !tagPath.isEmpty()) {
            blacklistedNBTTags.add(tagPath);
            save();
            logger.info("Added NBT tag to auction blacklist: " + tagPath);
        }
    }

    /**
     * Remove an NBT tag from the blacklist
     */
    public void removeNBTTag(String tagPath) {
        if (blacklistedNBTTags.remove(tagPath)) {
            save();
            logger.info("Removed NBT tag from auction blacklist: " + tagPath);
        }
    }

    /**
     * Check if an NBT tag is blacklisted
     */
    public boolean isNBTTagBlacklisted(String tagPath) {
        return blacklistedNBTTags.contains(tagPath);
    }

    /**
     * Get all blacklisted NBT tags
     */
    public Set<String> getBlacklistedNBTTags() {
        return new HashSet<>(blacklistedNBTTags);
    }

    // ==================== Persistence ====================

    public void save() {
        File saveDir = CustomNpcs.getWorldSaveDirectory();
        if (saveDir == null) {
            return;
        }

        try {
            NBTTagCompound compound = new NBTTagCompound();

            // Save blacklisted items
            NBTTagList itemList = new NBTTagList();
            for (String item : blacklistedItems) {
                itemList.appendTag(new NBTTagString(item));
            }
            compound.setTag("Items", itemList);

            // Save blacklisted mods
            NBTTagList modList = new NBTTagList();
            for (String mod : blacklistedMods) {
                modList.appendTag(new NBTTagString(mod));
            }
            compound.setTag("Mods", modList);

            // Save blacklisted NBT tags
            NBTTagList nbtList = new NBTTagList();
            for (String nbt : blacklistedNBTTags) {
                nbtList.appendTag(new NBTTagString(nbt));
            }
            compound.setTag("NBTTags", nbtList);

            // Write to file
            File file = new File(saveDir, "auction_blacklist.dat");
            File backup = new File(saveDir, "auction_blacklist.dat_old");

            if (file.exists()) {
                if (backup.exists()) {
                    backup.delete();
                }
                file.renameTo(backup);
            }

            file = new File(saveDir, "auction_blacklist.dat");
            FileOutputStream fos = new FileOutputStream(file);
            CompressedStreamTools.writeCompressed(compound, fos);
            fos.close();

        } catch (Exception e) {
            logger.error("Error saving auction blacklist", e);
        }
    }

    public void load() {
        File saveDir = CustomNpcs.getWorldSaveDirectory();
        if (saveDir == null) {
            return;
        }

        blacklistedItems.clear();
        blacklistedMods.clear();
        blacklistedNBTTags.clear();

        try {
            File file = new File(saveDir, "auction_blacklist.dat");
            if (!file.exists()) {
                // Try backup
                file = new File(saveDir, "auction_blacklist.dat_old");
                if (!file.exists()) {
                    return;
                }
            }

            FileInputStream fis = new FileInputStream(file);
            NBTTagCompound compound = CompressedStreamTools.readCompressed(fis);
            fis.close();

            // Load blacklisted items
            NBTTagList itemList = compound.getTagList("Items", 8); // 8 = String tag
            for (int i = 0; i < itemList.tagCount(); i++) {
                blacklistedItems.add(itemList.getStringTagAt(i));
            }

            // Load blacklisted mods
            NBTTagList modList = compound.getTagList("Mods", 8);
            for (int i = 0; i < modList.tagCount(); i++) {
                blacklistedMods.add(modList.getStringTagAt(i));
            }

            // Load blacklisted NBT tags
            NBTTagList nbtList = compound.getTagList("NBTTags", 8);
            for (int i = 0; i < nbtList.tagCount(); i++) {
                blacklistedNBTTags.add(nbtList.getStringTagAt(i));
            }

            logger.info("Loaded auction blacklist: {} items, {} mods, {} NBT tags",
                blacklistedItems.size(), blacklistedMods.size(), blacklistedNBTTags.size());

        } catch (Exception e) {
            logger.error("Error loading auction blacklist", e);
        }
    }

    /**
     * Clear all blacklist entries
     */
    public void clearAll() {
        blacklistedItems.clear();
        blacklistedMods.clear();
        blacklistedNBTTags.clear();
        save();
        logger.info("Cleared all auction blacklist entries");
    }

    /**
     * Get total number of blacklist entries
     */
    public int getTotalEntries() {
        return blacklistedItems.size() + blacklistedMods.size() + blacklistedNBTTags.size();
    }
}
