package noppes.npcs.api.item;

public interface IItemCustom extends IItemCustomizable {

    /**
     * @return true if scripted item script is enabled
     */
    boolean getEnabled();

    /**
     * @param enable Enable or Disable scripted item script
     */
    void setEnabled(boolean enable);

    /**
     * Sets the armor type for the scripted item.
     *
     * @param armorType The armor type
     */
    void setArmorType(int armorType);

    /**
     * Sets whether the scripted item is a tool.
     *
     * @param isTool True if the item is a tool, false otherwise
     */
    void setIsTool(boolean isTool);

    /**
     * Sets whether the scripted item is a normal item. Will disable
     * all forms of Rotation, Translation, Scale Rendering in hand and on ground
     *
     * @param normalItem True if the item is a normal item, false otherwise
     */
    void setIsNormalItem(boolean normalItem);

    /**
     * Sets the dig speed for the scripted item.
     *
     * @param digSpeed The dig speed
     */
    void setDigSpeed(int digSpeed);

    /**
     * Sets the maximum stack size for the scripted item.
     *
     * @param maxStackSize The maximum stack size
     */
    void setMaxStackSize(int maxStackSize);

    /**
     * Sets the current durability value for the scripted item.
     *
     * @param durabilityValue The durability value
     */
    void setDurabilityValue(float durabilityValue);

    /**
     * Sets the maximum item use duration for the scripted item.
     *
     * @param duration The maximum item use duration
     */
    void setMaxItemUseDuration(int duration);

    /**
     * Sets the item use action for the scripted item.
     *
     * @param action The item use action
     */
    void setItemUseAction(int action);

    /**
     * Sets the attack speed for the scripted item.
     * Speed is the max resistantance time between two attacks in ticks.
     *
     * @param speed The attack speed
     */
    void setAttackSpeed(int speed);

    /**
     * Sets the enchantability for the scripted item.
     *
     * @param enchantability The enchantability
     */
    void setEnchantability(int enchantability);
}
