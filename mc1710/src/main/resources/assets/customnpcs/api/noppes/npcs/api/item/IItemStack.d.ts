/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.item
 */

/**
 * Represents an item stack with extended functionality:
 * enchantments, lore, and additional item data.
  * @javaFqn noppes.npcs.api.item.IItemStack
*/
export interface IItemStack {
    getName(): String;
    /**
     * @return Returns the stack size.
     */
    getStackSize(): import('./int').int;
    /**
     * @return Returns whether the item has a custom name.
     */
    hasCustomName(): import('./boolean').boolean;
    /**
     * Sets the custom name for the item.
     *
     * @param name The custom name.
     */
    setCustomName(name: String): import('./void').void;
    /**
     * @return Returns the in-game displayed name. This is either the item name or the custom name if set.
     */
    getDisplayName(): String;
    /**
     * @return Returns the base item name, regardless of any custom name.
     */
    getItemName(): String;
    /**
     * Sets the stack size of the item.
     *
     * @param size The new stack size (between 1 and 64).
     */
    setStackSize(size: import('./int').int): import('./void').void;
    /**
     * @return Returns the maximum stack size for this item.
     */
    getMaxStackSize(): import('./int').int;
    /**
     * @return Returns the item damage. For tools, this represents durability.
     */
    getItemDamage(): import('./int').int;
    /**
     * Sets the item damage value.
     *
     * @param value The new damage value.
     */
    setItemDamage(value: import('./int').int): import('./void').void;
    /**
     * Sets an NBT tag for this item.
     *
     * @param key   The tag key.
     * @param value The value to store (Number or String).
     */
    setTag(key: String, value: Object): import('./void').void;
    /**
     * Checks if the item has an NBT tag with the specified key.
     *
     * @param key The tag key.
     * @return true if present, false otherwise.
     */
    hasTag(key: String): import('./boolean').boolean;
    /**
     * Returns the value of the NBT tag with the specified key.
     *
     * @param key The tag key.
     * @return The tag value, or null if not present.
     */
    getTag(key: String): Object;
    /**
     * Removes all custom tags from the item and returns the removed NBT data.
     *
     * @return The removed NBT data.
     */
    removeTags(): import('../INbt').INbt;
    /**
     * @return Returns whether the item is enchanted.
     */
    isEnchanted(): import('./boolean').boolean;
    /**
     * Checks if the item has the specified enchantment.
     *
     * @param id The enchantment id.
     * @return true if present, false otherwise.
     */
    hasEnchant(id: import('./int').int): import('./boolean').boolean;
    /**
     * Adds an enchantment to the item.
     *
     * @param id       The enchantment id.
     * @param strength The enchantment strength.
     */
    addEnchant(id: import('./int').int, strength: import('./int').int): import('./void').void;
    /**
     * Sets an attribute on the item using the default MC attribute system.
     *
     * @param name  The attribute key.
     * @param value The attribute value.
     */
    setAttribute(name: String, value: import('./double').double): import('./void').void;
    /**
     * Retrieves the attribute value using the default MC attribute system.
     *
     * @param name The attribute key.
     * @return The attribute value.
     */
    getAttribute(name: String): import('./double').double;
    /**
     * Checks if the item has an attribute (using the default MC attribute system).
     *
     * @param name The attribute key.
     * @return true if present, false otherwise.
     */
    hasAttribute(name: String): import('./boolean').boolean;
    /**
     * Sets the custom attribute value for the given key.
     * The value is stored in the item's custom NBT structure.
     *
     * @param key   The custom attribute key.
     * @param value The attribute value.
     */
    setCustomAttribute(key: String, value: import('./double').double): import('./void').void;
    /**
     * Checks whether the item has a custom attribute with the specified key.
     *
     * @param key The custom attribute key.
     * @return true if the attribute exists, false otherwise.
     */
    hasCustomAttribute(key: String): import('./boolean').boolean;
    /**
     * Returns the custom attribute object associated with the given key.
     * This may be a raw value or a more complex structure.
     *
     * @param key The custom attribute key.
     * @return The attribute object value
     */
    getCustomAttribute(key: String): import('./float').float;
    /**
     * Removes the custom attribute with the specified key from the item.
     *
     * @param key The custom attribute key.
     */
    removeCustomAttribute(key: String): import('./void').void;
    /**
     * Sets the magic attribute value for the given key and magic identifier.
     * The value is stored in the item's custom NBT under "RPGCore", "Magic".
     *
     * @param key     The magic attribute key.
     * @param magicId The magic identifier.
     * @param value   The attribute value.
     */
    setMagicAttribute(key: String, magicId: import('./int').int, value: import('./double').double): import('./void').void;
    /**
     * Checks whether the item has a magic attribute with the given key and magic identifier.
     *
     * @param key     The magic attribute key.
     * @param magicId The magic identifier.
     * @return true if the attribute exists, false otherwise.
     */
    hasMagicAttribute(key: String, magicId: import('./int').int): import('./boolean').boolean;
    /**
     * Returns the magic attribute object (typically a Float value) associated with the given key and magic identifier.
     *
     * @param key     The magic attribute key.
     * @param magicId The magic identifier.
     * @return The attribute object, or null if not present.
     */
    getMagicAttribute(key: String, magicId: import('./int').int): import('./float').float;
    /**
     * Removes the magic attribute with the specified key and magic identifier from the item.
     *
     * @param key     The magic attribute key.
     * @param magicId The magic identifier.
     */
    removeMagicAttribute(key: String, magicId: import('./int').int): import('./void').void;
    /**
     * Sets (or applies) a requirement for the item using the given requirement key and value.
     * The requirement data is stored under "RPGCore" → "Requirements" in the item's NBT.
     *
     * @param reqKey The requirement key.
     * @param value  The requirement value (Number or String).
     */
    setRequirement(reqKey: String, value: Object): import('./void').void;
    /**
     * Checks whether the item has a requirement with the specified key.
     *
     * @param reqKey The requirement key.
     * @return true if the requirement exists, false otherwise.
     */
    hasRequirement(reqKey: String): import('./boolean').boolean;
    /**
     * Retrieves the requirement value for the specified key.
     *
     * @param reqKey The requirement key.
     * @return The requirement value as an Object (or null if not present).
     */
    getRequirement(reqKey: String): Object;
    /**
     * Removes the requirement with the specified key from the item.
     *
     * @param reqKey The requirement key.
     */
    removeRequirement(reqKey: String): import('./void').void;
    /**
     * Returns an array of all custom attribute keys stored on the item.
     *
     * @return an array of custom attribute keys.
     */
    getCustomAttributeKeys(): String[];
    /**
     * Returns an array of all magic attribute keys (as strings) for the specified magic attribute key.
     *
     * @param key the magic attribute key (e.g. "magic_damage").
     * @return an array of magic identifier keys as strings.
     */
    getMagicAttributeKeys(key: String): String[];
    /**
     * Returns an array of all requirement keys stored on the item.
     *
     * @return an array of requirement keys.
     */
    getRequirementKeys(): String[];
    /**
     * @return Returns the lore (descriptive text) for the item.
     */
    getLore(): String[];
    /**
     * @return Returns whether the item has lore.
     */
    hasLore(): import('./boolean').boolean;
    /**
     * Sets the lore for the item.
     *
     * @param lore An array of lore strings.
     */
    setLore(lore: String[]): import('./void').void;
    /**
     * Creates a deep copy of this item stack.
     *
     * @return A copy of the item stack.
     */
    copy(): import('./IItemStack').IItemStack;
    /**
     * @return Returns the maximum damage the item can sustain.
     */
    getMaxItemDamage(): import('./int').int;
    /**
     * @return Returns whether the item is a written book.
     */
    isWrittenBook(): import('./boolean').boolean;
    /**
     * @return Returns the book's title.
     */
    getBookTitle(): String;
    /**
     * @return Returns the book's author.
     */
    getBookAuthor(): String;
    /**
     * @return If the item is a book, returns the book pages.
     */
    getBookText(): String[];
    /**
     * @return Returns whether the item represents a block.
     */
    isBlock(): import('./boolean').boolean;
    /**
     * Returns an INbt instance representing the item's custom NBT data.
     *
     * @return The custom NBT data.
     */
    getNbt(): import('../INbt').INbt;
    /**
     * Returns an INbt instance representing the full NBT data of the item.
     *
     * @return The complete NBT data.
     */
    getItemNbt(): import('../INbt').INbt;
    /**
     * Returns the underlying Minecraft ItemStack.
     * <p>No support is given for this method. Use with caution.</p>
     *
     * @return The Minecraft ItemStack.
     */
    getMCItemStack(): import('../../../../net/minecraft/item/ItemStack').ItemStack;
    /**
     * Returns a hash value for the item.
     *
     * @return The item's hash code.
     */
    itemHash(): import('./int').int;
    /**
     * Returns the underlying Minecraft NBTTagCompound for the item.
     *
     * @return The NBTTagCompound.
     */
    getMCNbt(): import('../../../../net/minecraft/nbt/NBTTagCompound').NBTTagCompound;
    /**
     * Sets the underlying Minecraft NBTTagCompound for the item.
     *
     * @param compound The NBTTagCompound to set.
     */
    setMCNbt(compound: import('../../../../net/minecraft/nbt/NBTTagCompound').NBTTagCompound): import('./void').void;
    /**
     * Compares this item with another item, with an option to ignore NBT data.
     *
     * @param item      The item to compare.
     * @param ignoreNBT true to ignore NBT data.
     * @return true if the items are considered equal, false otherwise.
     */
    compare(item: import('./IItemStack').IItemStack, ignoreNBT: import('./boolean').boolean): import('./boolean').boolean;
    /**
     * Compares this item with another item, with options to ignore damage and/or NBT data.
     *
     * @param item         The item to compare.
     * @param ignoreDamage true to ignore damage values.
     * @param ignoreNBT    true to ignore NBT data.
     * @return true if the items are considered equal, false otherwise.
     */
    compare(item: import('./IItemStack').IItemStack, ignoreDamage: import('./boolean').boolean, ignoreNBT: import('./boolean').boolean): import('./boolean').boolean;
    item: import('../../../../net/minecraft/item/ItemStack').ItemStack;
}
