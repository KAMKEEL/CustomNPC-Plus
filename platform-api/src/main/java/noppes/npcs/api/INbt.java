package noppes.npcs.api;

import java.util.Set;

/**
 * Unified NBT compound interface — used by both CORE logic and scripting.
 *
 * Platform-api version (MC-free). The src/api shadow adds typed MC methods
 * (e.g., {@code NBTTagCompound getMCNBT()}).
 *
 * <p>Compound tags contain uniquely-keyed tags of various types:
 * <pre>
 * 1: Byte    2: Short   3: Int      4: Long
 * 5: Float   6: Double  7: Byte[]   8: String
 * 9: List   10: Compound 11: Int[]
 * </pre>
 */
public interface INbt {

    // ======================== Setters ========================

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

    /**
     * Alias for {@link #setIntArray(String, int[])}.
     */
    default void setIntegerArray(String key, int[] value) {
        setIntArray(key, value);
    }

    void setCompound(String key, INbt compound);

    /**
     * Alias for {@link #setCompound(String, INbt)}.
     */
    default void setTag(String key, INbt compound) {
        setCompound(key, compound);
    }

    /**
     * Sets a list from an Object array. Elements can be: INbt, String, Double, Float, Integer, int[].
     */
    void setList(String key, Object[] value);

    /**
     * Sets a typed tag list on this compound.
     */
    void setTagList(String key, INbtList list);

    // ======================== Getters ========================

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

    /**
     * Alias for {@link #getIntArray(String)}.
     */
    default int[] getIntegerArray(String key) {
        return getIntArray(key);
    }

    INbt getCompound(String key);

    /**
     * Returns a tag list as an Object array. Elements are typed based on list type:
     * INbt (compounds), String, Double, Float, Integer, int[].
     */
    Object[] getList(String key, int type);

    /**
     * Gets a typed tag list from this compound.
     *
     * @param key  the key
     * @param type the element type ID (e.g. 10 for compounds, 8 for strings)
     * @return the list, or an empty list if the key doesn't exist
     */
    INbtList getTagList(String key, int type);

    /**
     * Returns the type of the list tag with the given key.
     *
     * @return the element type ID, or 0 if not a list tag
     */
    int getListType(String key);

    // ======================== Query ========================

    /**
     * @return true if this compound has a tag with the given key
     */
    boolean hasKey(String key);

    /**
     * Alias for {@link #hasKey(String)}.
     */
    default boolean has(String key) {
        return hasKey(key);
    }

    /**
     * Checks if this compound has a key of a specific NBT type.
     */
    boolean hasKey(String key, int type);

    /**
     * @return a set of all keys in this compound
     */
    Set<String> getKeySet();

    /**
     * @return all keys as a String array
     */
    default String[] getKeys() {
        return getKeySet().toArray(new String[0]);
    }

    /**
     * Removes the tag with the given key.
     */
    void removeTag(String key);

    /**
     * Alias for {@link #removeTag(String)}.
     */
    default void remove(String key) {
        removeTag(key);
    }

    /**
     * Returns the NBT type ID for the tag with the given key.
     *
     * @return the type ID, or 0 if not present
     */
    int getTagType(String key);

    /**
     * Alias for {@link #getTagType(String)}.
     */
    default int getType(String key) {
        return getTagType(key);
    }

    /**
     * @return true if this compound has no keys
     */
    boolean isEmpty();

    // ======================== Interop ========================

    /**
     * Merges all tags from the given compound into this one.
     */
    void merge(INbt other);

    /**
     * Creates a deep copy of this compound.
     */
    INbt copy();

    /**
     * Returns the underlying MC NBT object.
     * Returns Object in platform-api; the src/api shadow narrows to the MC type.
     * CORE code should NEVER call this — only mc* modules should.
     */
    Object getMCNBT();

    // ======================== Convenience ========================

    /**
     * @return a JSON string representation of this compound
     */
    String toJsonString();

    /**
     * @return true if this compound has the same data as the given compound
     */
    boolean isEqual(INbt nbt);

    /**
     * Removes all tags from this compound.
     */
    void clear();
}
