package kamkeel.npcs.controllers.data.attribute.tracker;

import kamkeel.npcs.util.AttributeItemUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Objects;

import static kamkeel.npcs.util.AttributeItemUtil.TAG_RPGCORE;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerEquipmentTracker)) return false;
        PlayerEquipmentTracker other = (PlayerEquipmentTracker)o;
        return areItemStacksEquivalent(heldItem,   other.heldItem)
            && areItemStacksEquivalent(boots,       other.boots)
            && areItemStacksEquivalent(leggings,    other.leggings)
            && areItemStacksEquivalent(chestplate,  other.chestplate)
            && areItemStacksEquivalent(helmet,      other.helmet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            itemHash(heldItem),
            itemHash(boots),
            itemHash(leggings),
            itemHash(chestplate),
            itemHash(helmet)
        );
    }

    private int itemHash(ItemStack s) {
        if (s == null) return 0;
        NBTTagCompound root = s.stackTagCompound;
        if (root == null || !root.hasKey(TAG_RPGCORE)) return 0;
        NBTTagCompound tag = root.getCompoundTag(TAG_RPGCORE);
        return Objects.hash(s.getItem(), tag);
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

        NBTTagCompound tagA = a.stackTagCompound != null
            ? a.stackTagCompound.getCompoundTag(TAG_RPGCORE)
            : null;
        NBTTagCompound tagB = b.stackTagCompound != null
            ? b.stackTagCompound.getCompoundTag(TAG_RPGCORE)
            : null;
        if (tagA == null ^ tagB == null) return false;
        if (tagA != null && !tagA.equals(tagB)) return false;

        return true;
    }
}
