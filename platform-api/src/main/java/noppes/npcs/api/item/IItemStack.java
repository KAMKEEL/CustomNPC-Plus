package noppes.npcs.api.item;

import noppes.npcs.api.INbt;

/**
 * Represents an item stack with extended functionality:
 * enchantments, lore, and additional item data.
 */
public interface IItemStack {
    String getName();

    /**
     * @return Returns the stack size.
     */
    int getStackSize();

    /**
     * @return Returns whether the item has a custom name.
     */
    boolean hasCustomName();

    /**
     * Sets the custom name for the item.
     *
     * @param name The custom name.
     */
    void setCustomName(String name);

    /**
     * @return Returns the in-game displayed name. This is either the item name or the custom name if set.
     */
    String getDisplayName();

    /**
     * @return Returns the base item name, regardless of any custom name.
     */
    String getItemName();

    /**
     * Sets the stack size of the item.
     *
     * @param size The new stack size (between 1 and 64).
     */
    void setStackSize(int size);

    /**
     * @return Returns the maximum stack size for this item.
     */
    int getMaxStackSize();

    /**
     * @return Returns the item damage. For tools, this represents durability.
     */
    int getItemDamage();

    /**
     * Sets the item damage value.
     *
     * @param value The new damage value.
     */
    void setItemDamage(int value);

    /**
     * Sets an NBT tag for this item.
     *
     * @param key   The tag key.
     * @param value The value to store (Number or String).
     */
    void setTag(String key, Object value);

    /**
     * Checks if the item has an NBT tag with the specified key.
     *
     * @param key The tag key.
     * @return true if present, false otherwise.
     */
    boolean hasTag(String key);

    /**
     * Returns the value of the NBT tag with the specified key.
     *
     * @param key The tag key.
     * @return The tag value, or null if not present.
     */
    Object getTag(String key);

    /**
     * Removes all custom tags from the item and returns the removed NBT data.
     *
     * @return The removed NBT data.
     */
    INbt removeTags();

    /**
     * @return Returns whether the item is enchanted.
     */
    boolean isEnchanted();

    /**
     * Checks if the item has the specified enchantment.
     *
     * @param id The enchantment id.
     * @return true if present, false otherwise.
     */
    boolean hasEnchant(int id);

    /**
     * Adds an enchantment to the item.
     *
     * @param id       The enchantment id.
     * @param strength The enchantment strength.
     */
    void addEnchant(int id, int strength);

    // --- Default MC Attribute Handling (legacy) ---

    /**
     * Sets an attribute on the item using the default MC attribute system.
     *
     * @param name  The attribute key.
     * @param value The attribute value.
     */
    void setAttribute(String name, double value);

    /**
     * Retrieves the attribute value using the default MC attribute system.
     *
     * @param name The attribute key.
     * @return The attribute value.
     */
    double getAttribute(String name);

    /**
     * Checks if the item has an attribute (using the default MC attribute system).
     *
     * @param name The attribute key.
     * @return true if present, false otherwise.
     */
    boolean hasAttribute(String name);


    /**
     * Sets the custom attribute value for the given key.
     * The value is stored in the item's custom NBT structure.
     *
     * @param key   The custom attribute key.
     * @param value The attribute value.
     */
    void setCustomAttribute(String key, double value);


    /**
     * Checks whether the item has a custom attribute with the specified key.
     *
     * @param key The custom attribute key.
     * @return true if the attribute exists, false otherwise.
     */
    boolean hasCustomAttribute(String key);

    /**
     * Returns the custom attribute object associated with the given key.
     * This may be a raw value or a more complex structure.
     *
     * @param key The custom attribute key.
     * @return The attribute object value
     */
    float getCustomAttribute(String key);

    /**
     * Removes the custom attribute with the specified key from the item.
     *
     * @param key The custom attribute key.
     */
    void removeCustomAttribute(String key);

    /**
     * Sets the magic attribute value for the given key and magic identifier.
     * The value is stored in the item's custom NBT under "RPGCore", "Magic".
     *
     * @param key     The magic attribute key.
     * @param magicId The magic identifier.
     * @param value   The attribute value.
     */
    void setMagicAttribute(String key, int magicId, double value);


    /**
     * Checks whether the item has a magic attribute with the given key and magic identifier.
     *
     * @param key     The magic attribute key.
     * @param magicId The magic identifier.
     * @return true if the attribute exists, false otherwise.
     */
    boolean hasMagicAttribute(String key, int magicId);

    /**
     * Returns the magic attribute object (typically a Float value) associated with the given key and magic identifier.
     *
     * @param key     The magic attribute key.
     * @param magicId The magic identifier.
     * @return The attribute object, or null if not present.
     */
    float getMagicAttribute(String key, int magicId);

    /**
     * Removes the magic attribute with the specified key and magic identifier from the item.
     *
     * @param key     The magic attribute key.
     * @param magicId The magic identifier.
     */
    void removeMagicAttribute(String key, int magicId);

    /**
     * Sets (or applies) a requirement for the item using the given requirement key and value.
     * The requirement data is stored under "RPGCore" → "Requirements" in the item's NBT.
     *
     * @param reqKey The requirement key.
     * @param value  The requirement value (Number or String).
     */
    void setRequirement(String reqKey, Object value);

    /**
     * Checks whether the item has a requirement with the specified key.
     *
     * @param reqKey The requirement key.
     * @return true if the requirement exists, false otherwise.
     */
    boolean hasRequirement(String reqKey);

    /**
     * Retrieves the requirement value for the specified key.
     *
     * @param reqKey The requirement key.
     * @return The requirement value as an Object (or null if not present).
     */
    Object getRequirement(String reqKey);

    /**
     * Removes the requirement with the specified key from the item.
     *
     * @param reqKey The requirement key.
     */
    void removeRequirement(String reqKey);

    /**
     * Returns an array of all custom attribute keys stored on the item.
     *
     * @return an array of custom attribute keys.
     */
    String[] getCustomAttributeKeys();

    /**
     * Returns an array of all magic attribute keys (as strings) for the specified magic attribute key.
     *
     * @param key the magic attribute key (e.g. "magic_damage").
     * @return an array of magic identifier keys as strings.
     */
    String[] getMagicAttributeKeys(String key);

    /**
     * Returns an array of all requirement keys stored on the item.
     *
     * @return an array of requirement keys.
     */
    String[] getRequirementKeys();

    /**
     * @return Returns the lore (descriptive text) for the item.
     */
    String[] getLore();

    /**
     * @return Returns whether the item has lore.
     */
    boolean hasLore();

    /**
     * Sets the lore for the item.
     *
     * @param lore An array of lore strings.
     */
    void setLore(String[] lore);

    /**
     * Creates a deep copy of this item stack.
     *
     * @return A copy of the item stack.
     */
    IItemStack copy();

    /**
     * @return Returns the maximum damage the item can sustain.
     */
    int getMaxItemDamage();

    /**
     * @return Returns whether the item is a written book.
     */
    boolean isWrittenBook();

    /**
     * @return Returns the book's title.
     */
    String getBookTitle();

    /**
     * @return Returns the book's author.
     */
    String getBookAuthor();

    /**
     * @return If the item is a book, returns the book pages.
     */
    String[] getBookText();

    /**
     * @return Returns whether the item represents a block.
     */
    boolean isBlock();

    /**
     * Returns an INbt instance representing the item's custom NBT data.
     *
     * @return The custom NBT data.
     */
    INbt getNbt();

    /**
     * Returns an INbt instance representing the full NBT data of the item.
     *
     * @return The complete NBT data.
     */
    INbt getItemNbt();

    /**
     * Returns the underlying Minecraft ItemStack.
     * <p>No support is given for this method. Use with caution.</p>
     *
     * @return The Minecraft ItemStack.
     */
    Object getMCItemStack();

    /**
     * Returns a hash value for the item.
     *
     * @return The item's hash code.
     */
    int itemHash();

    /**
     * Returns the underlying Minecraft NBTTagCompound for the item.
     *
     * @return The NBTTagCompound.
     */
    Object getMCNbt();

    /**
     * Sets the underlying Minecraft NBTTagCompound for the item.
     *
     * @param compound The NBTTagCompound to set.
     */
    void setMCNbt(Object compound);

    /**
     * Compares this item with another item, with an option to ignore NBT data.
     *
     * @param item      The item to compare.
     * @param ignoreNBT true to ignore NBT data.
     * @return true if the items are considered equal, false otherwise.
     */
    boolean compare(IItemStack item, boolean ignoreNBT);

    /**
     * Compares this item with another item, with options to ignore damage and/or NBT data.
     *
     * @param item         The item to compare.
     * @param ignoreDamage true to ignore damage values.
     * @param ignoreNBT    true to ignore NBT data.
     * @return true if the items are considered equal, false otherwise.
     */
    boolean compare(IItemStack item, boolean ignoreDamage, boolean ignoreNBT);
}
