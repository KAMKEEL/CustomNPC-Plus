/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.item
 */

/**
 * @javaFqn noppes.npcs.api.item.IItemCustom
 */
export interface IItemCustom extends import('./IItemCustomizable').IItemCustomizable {
    /**
     * @return true if scripted item script is enabled
     */
    getEnabled(): import('./boolean').boolean;
    /**
     * @param enable Enable or Disable scripted item script
     */
    setEnabled(enable: import('./boolean').boolean): import('./void').void;
    /**
     * Sets the armor type for the scripted item.
     *
     * @param armorType The armor type
     */
    setArmorType(armorType: import('./int').int): import('./void').void;
    /**
     * Sets whether the scripted item is a tool.
     *
     * @param isTool True if the item is a tool, false otherwise
     */
    setIsTool(isTool: import('./boolean').boolean): import('./void').void;
    /**
     * Sets whether the scripted item is a normal item. Will disable
     * all forms of Rotation, Translation, Scale Rendering in hand and on ground
     *
     * @param normalItem True if the item is a normal item, false otherwise
     */
    setIsNormalItem(normalItem: import('./boolean').boolean): import('./void').void;
    /**
     * Sets the dig speed for the scripted item.
     *
     * @param digSpeed The dig speed
     */
    setDigSpeed(digSpeed: import('./int').int): import('./void').void;
    /**
     * Sets the maximum stack size for the scripted item.
     *
     * @param maxStackSize The maximum stack size
     */
    setMaxStackSize(maxStackSize: import('./int').int): import('./void').void;
    /**
     * Sets the current durability value for the scripted item.
     *
     * @param durabilityValue The durability value
     */
    setDurabilityValue(durabilityValue: import('./float').float): import('./void').void;
    /**
     * Sets the maximum item use duration for the scripted item.
     *
     * @param duration The maximum item use duration
     */
    setMaxItemUseDuration(duration: import('./int').int): import('./void').void;
    /**
     * Sets the item use action for the scripted item.
     *
     * @param action The item use action
     */
    setItemUseAction(action: import('./int').int): import('./void').void;
    /**
     * Sets the attack speed for the scripted item.
     * Speed is the max resistantance time between two attacks in ticks.
     *
     * @param speed The attack speed
     */
    setAttackSpeed(speed: import('./int').int): import('./void').void;
    /**
     * Sets the enchantability for the scripted item.
     *
     * @param enchantability The enchantability
     */
    setEnchantability(enchantability: import('./int').int): import('./void').void;
    scripts: import('./IScriptUnit').IScriptUnit[];
    errored: Integer[];
    scriptLanguage: String;
    enabled: import('./boolean').boolean;
    loaded: import('./boolean').boolean;
    durabilityValue: import('./double').double;
    stackSize: import('./int').int;
    maxItemUseDuration: import('./int').int;
    itemUseAction: import('./int').int;
    isNormalItem: import('./boolean').boolean;
    isTool: import('./boolean').boolean;
    digSpeed: import('./int').int;
    armorType: import('./int').int;
    enchantability: import('./int').int;
    attackSpeed: import('./int').int;
    lastInited: import('./long').long;
}
