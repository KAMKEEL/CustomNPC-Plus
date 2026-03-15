package noppes.npcs.api.handler.data;

import noppes.npcs.api.item.IItemStack;

/**
 * Represents a linked item definition. Linked items are custom item templates
 * that can be globally updated; all existing instances inherit the new properties.
 */
public interface ILinkedItem {

    /**
     * Persists this linked item and returns the saved instance.
     *
     * @return the saved linked item.
     */
    ILinkedItem save();

    /**
     * Creates an ItemStack from this linked item definition.
     *
     * @return the created item stack.
     */
    IItemStack createStack();

    /** @return the unique ID of this linked item. */
    int getId();

    /** @param id the unique ID. */
    void setId(int id);

    /** @return the version number of this linked item. */
    int getVersion();

    /** @param version the new version number. */
    void setVersion(int version);

    /** @return the display name. */
    String getName();

    /** @param name the display name. */
    void setName(String name);

    /** @return the durability value (max damage). */
    double getDurabilityValue();

    /** @param durabilityValue the durability value. */
    void setDurabilityValue(double durabilityValue);

    /** @return the maximum stack size. */
    int getStackSize();

    /** @param stackSize the maximum stack size. */
    void setStackSize(int stackSize);

    /** @return the maximum item use duration in ticks. */
    int getMaxItemUseDuration();

    /** @param maxItemUseDuration the use duration in ticks. */
    void setMaxItemUseDuration(int maxItemUseDuration);

    /**
     * @return the item use action type.
     * @see net.minecraft.item.EnumAction
     */
    int getItemUseAction();

    /** @param itemUseAction the item use action type ordinal. */
    void setItemUseAction(int itemUseAction);

    /** @return true if this is a normal (non-tool, non-armor) item. */
    boolean isNormalItem();

    /** @param normalItem true for a normal item. */
    void setNormalItem(boolean normalItem);

    /** @return true if this item functions as a tool. */
    boolean isTool();

    /** @param tool true to mark as a tool. */
    void setTool(boolean tool);

    /** @return the dig speed for tool items. */
    int getDigSpeed();

    /** @param digSpeed the dig speed. */
    void setDigSpeed(int digSpeed);

    /**
     * @return the armor type slot.
     *         0: helmet, 1: chestplate, 2: leggings, 3: boots, -1: not armor.
     */
    int getArmorType();

    /** @param armorType the armor type slot. */
    void setArmorType(int armorType);

    /** @return the enchantability value. */
    int getEnchantability();

    /** @param enchantability the enchantability value. */
    void setEnchantability(int enchantability);

    /** @return the attack speed in ticks between attacks. */
    int getAttackSpeed();

    /** @param time attack speed in ticks. */
    void setAttackSpeed(int time);
}
