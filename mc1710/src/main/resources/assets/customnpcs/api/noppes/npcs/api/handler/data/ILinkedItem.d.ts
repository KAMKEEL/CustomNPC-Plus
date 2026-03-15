/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

/**
 * Represents a linked item definition. Linked items are custom item templates
 * that can be globally updated; all existing instances inherit the new properties.
  * @javaFqn noppes.npcs.api.handler.data.ILinkedItem
*/
export interface ILinkedItem {
    /**
     * Persists this linked item and returns the saved instance.
     *
     * @return the saved linked item.
     */
    save(): import('./ILinkedItem').ILinkedItem;
    /**
     * Creates an ItemStack from this linked item definition.
     *
     * @return the created item stack.
     */
    createStack(): import('../../item/IItemStack').IItemStack;
    /** @return the unique ID of this linked item. */
    getId(): import('./int').int;
    /** @param id the unique ID. */
    setId(id: import('./int').int): import('./void').void;
    /** @return the version number of this linked item. */
    getVersion(): import('./int').int;
    /** @param version the new version number. */
    setVersion(version: import('./int').int): import('./void').void;
    /** @return the display name. */
    getName(): String;
    /** @param name the display name. */
    setName(name: String): import('./void').void;
    /** @return the durability value (max damage). */
    getDurabilityValue(): import('./double').double;
    /** @param durabilityValue the durability value. */
    setDurabilityValue(durabilityValue: import('./double').double): import('./void').void;
    /** @return the maximum stack size. */
    getStackSize(): import('./int').int;
    /** @param stackSize the maximum stack size. */
    setStackSize(stackSize: import('./int').int): import('./void').void;
    /** @return the maximum item use duration in ticks. */
    getMaxItemUseDuration(): import('./int').int;
    /** @param maxItemUseDuration the use duration in ticks. */
    setMaxItemUseDuration(maxItemUseDuration: import('./int').int): import('./void').void;
    /**
     * @return the item use action type.
     * @see net.minecraft.item.EnumAction
     */
    getItemUseAction(): import('./int').int;
    /** @param itemUseAction the item use action type ordinal. */
    setItemUseAction(itemUseAction: import('./int').int): import('./void').void;
    /** @return true if this is a normal (non-tool, non-armor) item. */
    isNormalItem(): import('./boolean').boolean;
    /** @param normalItem true for a normal item. */
    setNormalItem(normalItem: import('./boolean').boolean): import('./void').void;
    /** @return true if this item functions as a tool. */
    isTool(): import('./boolean').boolean;
    /** @param tool true to mark as a tool. */
    setTool(tool: import('./boolean').boolean): import('./void').void;
    /** @return the dig speed for tool items. */
    getDigSpeed(): import('./int').int;
    /** @param digSpeed the dig speed. */
    setDigSpeed(digSpeed: import('./int').int): import('./void').void;
    /**
     * @return the armor type slot.
     *         0: helmet, 1: chestplate, 2: leggings, 3: boots, -1: not armor.
     */
    getArmorType(): import('./int').int;
    /** @param armorType the armor type slot. */
    setArmorType(armorType: import('./int').int): import('./void').void;
    /** @return the enchantability value. */
    getEnchantability(): import('./int').int;
    /** @param enchantability the enchantability value. */
    setEnchantability(enchantability: import('./int').int): import('./void').void;
    /** @return the attack speed in ticks between attacks. */
    getAttackSpeed(): import('./int').int;
    /** @param time attack speed in ticks. */
    setAttackSpeed(time: import('./int').int): import('./void').void;
    id: import('./int').int;
    version: import('./int').int;
    name: String;
    readonly display: import('./ItemDisplayData').ItemDisplayData;
    durabilityValue: import('./double').double;
    stackSize: import('./int').int;
    maxItemUseDuration: import('./int').int;
    itemUseAction: import('./int').int;
    isNormalItem: import('./boolean').boolean;
    isTool: import('./boolean').boolean;
    digSpeed: import('./int').int;
    attackSpeed: import('./int').int;
    armorType: import('./int').int;
    enchantability: import('./int').int;
    tagUUIDs: import('./UUID').UUID[];
}
