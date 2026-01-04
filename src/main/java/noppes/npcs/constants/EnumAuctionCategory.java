package noppes.npcs.constants;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;

/**
 * Categories for auction house items.
 * Used for filtering and browsing.
 */
public enum EnumAuctionCategory {
    ALL,        // Special: shows all categories
    WEAPONS,    // Swords, bows, etc.
    ARMOR,      // Helmets, chestplates, leggings, boots
    TOOLS,      // Pickaxes, axes, shovels, hoes
    FOOD,       // Edible items
    POTIONS,    // Potions and splash potions
    BLOCKS,     // Placeable blocks
    MATERIALS,  // Crafting materials (ingots, gems, etc.)
    ENCHANTED,  // Items with enchantments or enchanted books
    MISC;       // Everything else

    /**
     * Get display name for GUI
     */
    public String getDisplayName() {
        switch (this) {
            case ALL: return "All Items";
            case WEAPONS: return "Weapons";
            case ARMOR: return "Armor";
            case TOOLS: return "Tools";
            case FOOD: return "Food";
            case POTIONS: return "Potions";
            case BLOCKS: return "Blocks";
            case MATERIALS: return "Materials";
            case ENCHANTED: return "Enchanted";
            case MISC: return "Miscellaneous";
            default: return "Unknown";
        }
    }

    /**
     * Auto-categorize an item based on its type
     */
    public static EnumAuctionCategory categorize(ItemStack itemStack) {
        if (itemStack == null || itemStack.getItem() == null) {
            return MISC;
        }

        Item item = itemStack.getItem();

        // Check for enchantments first (higher priority)
        if (itemStack.isItemEnchanted() || item instanceof ItemEnchantedBook) {
            return ENCHANTED;
        }

        // Weapons
        if (item instanceof ItemSword || item instanceof ItemBow) {
            return WEAPONS;
        }

        // Armor
        if (item instanceof ItemArmor) {
            return ARMOR;
        }

        // Tools
        if (item instanceof ItemTool || item instanceof ItemPickaxe ||
            item instanceof ItemAxe || item instanceof ItemSpade ||
            item instanceof ItemHoe) {
            return TOOLS;
        }

        // Food
        if (item instanceof ItemFood) {
            return FOOD;
        }

        // Potions
        if (item instanceof ItemPotion) {
            return POTIONS;
        }

        // Blocks
        if (item instanceof ItemBlock) {
            return BLOCKS;
        }

        // Materials - common crafting items
        if (isMaterial(item)) {
            return MATERIALS;
        }

        return MISC;
    }

    /**
     * Check if an item is a common crafting material
     */
    private static boolean isMaterial(Item item) {
        // Check common material items
        return item == Items.iron_ingot ||
               item == Items.gold_ingot ||
               item == Items.diamond ||
               item == Items.emerald ||
               item == Items.coal ||
               item == Items.redstone ||
               item == Items.glowstone_dust ||
               item == Items.quartz ||
               item == Items.leather ||
               item == Items.string ||
               item == Items.feather ||
               item == Items.bone ||
               item == Items.gunpowder ||
               item == Items.slime_ball ||
               item == Items.ender_pearl ||
               item == Items.blaze_rod ||
               item == Items.ghast_tear ||
               item == Items.gold_nugget ||
               item == Items.nether_wart ||
               item == Items.spider_eye ||
               item == Items.magma_cream ||
               item == Items.nether_star;
    }

    /**
     * Get icon item for category display
     */
    public ItemStack getIconItem() {
        switch (this) {
            case ALL: return new ItemStack(Items.compass);
            case WEAPONS: return new ItemStack(Items.iron_sword);
            case ARMOR: return new ItemStack(Items.iron_chestplate);
            case TOOLS: return new ItemStack(Items.iron_pickaxe);
            case FOOD: return new ItemStack(Items.bread);
            case POTIONS: return new ItemStack(Items.potionitem);
            case BLOCKS: return new ItemStack(Item.getItemFromBlock(net.minecraft.init.Blocks.grass));
            case MATERIALS: return new ItemStack(Items.iron_ingot);
            case ENCHANTED: return new ItemStack(Items.enchanted_book);
            case MISC: return new ItemStack(Items.chest_minecart);
            default: return new ItemStack(Items.paper);
        }
    }
}
