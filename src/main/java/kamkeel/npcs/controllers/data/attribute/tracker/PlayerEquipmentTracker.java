package kamkeel.npcs.controllers.data.attribute.tracker;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import kamkeel.npcs.util.AttributeItemUtil;

public class PlayerEquipmentTracker {
    public ItemStack heldItem;
    public ItemStack helmet;
    public ItemStack chestplate;
    public ItemStack leggings;
    public ItemStack boots;

    /**
     * Updates the stored equipment based on the player's current held item and armor.
     */
    public void updateFrom(EntityPlayer player) {
        heldItem = copyIfNotNull(player.getHeldItem());
        ItemStack[] armor = player.inventory.armorInventory;
        // Armor inventory order: 0=boots, 1=leggings, 2=chestplate, 3=helmet.
        boots = copyIfNotNull(armor[0]);
        leggings = copyIfNotNull(armor[1]);
        chestplate = copyIfNotNull(armor[2]);
        helmet = copyIfNotNull(armor[3]);
    }

    /**
     * Compares the stored equipment with the player's current equipment.
     * Ignores changes like durability by comparing the item and the CNPCAttributes NBT only.
     */
    public boolean equals(EntityPlayer player) {
        if (player == null) return false;
        if (!areItemStacksEquivalent(heldItem, player.getHeldItem())) return false;
        ItemStack[] armor = player.inventory.armorInventory;
        if (!areItemStacksEquivalent(boots, armor[0])) return false;
        if (!areItemStacksEquivalent(leggings, armor[1])) return false;
        if (!areItemStacksEquivalent(chestplate, armor[2])) return false;
        if (!areItemStacksEquivalent(helmet, armor[3])) return false;
        return true;
    }

    /**
     * Returns a copy of the given ItemStack, or null if it is null.
     */
    private ItemStack copyIfNotNull(ItemStack stack) {
        return (stack == null) ? null : stack.copy();
    }

    /**
     * Compares two ItemStacks for equivalence, ignoring durability differences.
     * It checks if the items are the same and if the "CNPCAttributes" NBT compound is equal.
     */
    private boolean areItemStacksEquivalent(ItemStack a, ItemStack b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        if (a.getItem() != b.getItem()) return false;
        NBTTagCompound tagA = a.stackTagCompound;
        NBTTagCompound tagB = b.stackTagCompound;
        String attrA = (tagA != null && tagA.hasKey(AttributeItemUtil.RPGItemAttributes))
            ? tagA.getCompoundTag(AttributeItemUtil.RPGItemAttributes).toString() : "";
        String attrB = (tagB != null && tagB.hasKey(AttributeItemUtil.RPGItemAttributes))
            ? tagB.getCompoundTag(AttributeItemUtil.RPGItemAttributes).toString() : "";
        return attrA.equals(attrB);
    }
}
