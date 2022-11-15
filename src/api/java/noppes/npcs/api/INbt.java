//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.api;

import net.minecraft.nbt.NBTTagCompound;

/**
 * A scripted data representation of an MC NBTTagCompound object. Using these functions modifies the compound tag
 * associated with this object. Any changes made to the compound tag are made directly to its "tagMap" attribute,
 * minimizing the impact of setting/getting NBT data with this object on performance.
 *
 * Compound tags, unlike list tags, can contain multiple types of NBT tag types. Compound tags can even
 * contain compound tags inside them, and if that compound tag is accessed, an object like this one will also be returned!
 *
 * Every tag inside a compound tag has a -unique- "key". If you try to put another tag of the same key inside the
 * compound tag, it will replace the previous tag.
 *
 * The types and their type as an integer are as follows:
 * 1: Byte
 * 2: Short
 * 3: Int
 * 4: Long
 * 5: Float
 * 6: Double
 * 7: Byte array
 * 8: String
 * 9: Tag list
 * 10: Compound
 * 11: Integer array
 *
 */
public interface INbt {
    /**
     * Returns the tag with the given key from the compound tag.
     */
    void remove(String key);

    /**
     * @return True if the compound tag has a tag with the given key.
     */
    boolean has(String key);

    boolean getBoolean(String key);

    void setBoolean(String key, boolean value);

    short getShort(String key);

    void setShort(String key, short value);

    int getInteger(String key);

    void setInteger(String key, int value);

    byte getByte(String key);

    void setByte(String key, byte value);

    long getLong(String key);

    void setLong(String key, long value);

    double getDouble(String key);

    void setDouble(String key, double value);

    float getFloat(String key);

    void setFloat(String key, float value);

    String getString(String key);

    void setString(String key, String value);

    byte[] getByteArray(String key);

    void setByteArray(String key, byte[] value);

    int[] getIntegerArray(String key);

    void setIntegerArray(String key, int[] value);

    /**
     * Returns a tag list of objects with this key in the compound tag. All the objects in the list
     * will always be of the same type.
     *
     * @return The tag list of objects, depending on the tag type.
     */
    Object[] getList(String key, int value);

    /**
     * Returns the type of the tag list with this key, as an integer.
     */
    int getListType(String key);

    /**
     * Adds a new tag list to the compound tag with the given key.
     *
     * @param key The key for the list tag
     * @param value The list of objects to be in the list. The type of the first element in this list becomes the tag
     *              list's type, and if later objects are not of this type, they will not be added.
     */
    void setList(String key, Object[] value);

    INbt getCompound(String key);

    void setCompound(String key, INbt value);

    /**
     *
     * @return A list of all the compound tag's keys.
     */
    String[] getKeys();

    /**
     *
     * @return The type of the tag with the input key as an integer.
     */
    int getType(String key);

    /**
     *
     * @return An obfuscated MC NBTTagCompound object.
     */
    NBTTagCompound getMCNBT();

    /**
     *
     * @return A curly-bracket formatted JSON string of all the compound tag.
     */
    String toJsonString();

    boolean isEqual(INbt nbt);

    /**
     * Completely clears the compound tag of all tags inside it.
     */
    void clear();
}
