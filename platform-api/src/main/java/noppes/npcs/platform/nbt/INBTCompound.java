package noppes.npcs.platform.nbt;

/**
 * Version-independent abstraction over Minecraft's NBT compound tag.
 *
 * Maps to:
 *  - 1.7.10 / 1.12: net.minecraft.nbt.NBTTagCompound
 *  - 1.16+:         net.minecraft.nbt.CompoundNBT / CompoundTag
 *
 * Each mc* module provides a thin wrapper implementation that delegates
 * to the real MC class. CORE code uses this interface exclusively.
 */
public interface INBTCompound {

    // --- Setters ---

    void setString(String key, String value);

    void setInteger(String key, int value);

    void setBoolean(String key, boolean value);

    void setDouble(String key, double value);

    void setFloat(String key, float value);

    void setLong(String key, long value);

    void setShort(String key, short value);

    void setByte(String key, byte value);

    void setByteArray(String key, byte[] value);

    void setIntArray(String key, int[] value);

    void setCompound(String key, INBTCompound compound);

    void setTag(String key, INBTCompound compound);

    void setList(String key, INBTList list);

    // --- Getters ---

    String getString(String key);

    int getInteger(String key);

    boolean getBoolean(String key);

    double getDouble(String key);

    float getFloat(String key);

    long getLong(String key);

    short getShort(String key);

    byte getByte(String key);

    byte[] getByteArray(String key);

    int[] getIntArray(String key);

    INBTCompound getCompound(String key);

    /**
     * Gets a tag list from this compound.
     * @param key  the key
     * @param type the element type ID (e.g. 10 for compounds, 8 for strings)
     * @return the list, or an empty list if the key doesn't exist
     */
    INBTList getList(String key, int type);

    // --- Query ---

    boolean hasKey(String key);

    /**
     * Checks if this compound has a key of a specific NBT type.
     * @param key  the key
     * @param type the NBT type ID
     */
    boolean hasKey(String key, int type);

    /**
     * @return a set of all keys in this compound
     */
    java.util.Set<String> getKeySet();

    /**
     * Removes a key from this compound.
     */
    void removeTag(String key);

    /**
     * Returns the NBT type ID for the tag with the given key.
     * @return the type ID (see {@link noppes.npcs.constants.NBTTypes}), or 0 if not present
     */
    int getTagType(String key);

    /**
     * @return true if this compound has no keys
     */
    boolean isEmpty();

    // --- Interop ---

    /**
     * Merges all tags from the given compound into this one.
     */
    void merge(INBTCompound other);

    /**
     * Creates a deep copy of this compound.
     */
    INBTCompound copy();

    /**
     * Returns the underlying MC NBT object.
     * Used at the boundary between CORE and version-specific code.
     * CORE code should NEVER call this — only mc* modules should.
     */
    Object getUnderlyingTag();
}
