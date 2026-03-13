package noppes.npcs.platform.nbt;

/**
 * Version-independent abstraction over Minecraft's NBT list tag.
 *
 * Maps to:
 *  - 1.7.10 / 1.12: net.minecraft.nbt.NBTTagList
 *  - 1.16+:         net.minecraft.nbt.ListNBT / ListTag
 */
public interface INBTList {

    /**
     * @return the number of elements in this list
     */
    int size();

    /**
     * Gets the compound tag at the given index.
     * @throws ClassCastException if the element is not a compound
     */
    INBTCompound getCompound(int index);

    /**
     * Gets the string value at the given index.
     */
    String getString(int index);

    /**
     * Gets the integer value at the given index (from int array tags).
     */
    int getInt(int index);

    /**
     * Gets the double value at the given index.
     */
    double getDouble(int index);

    /**
     * Gets the float value at the given index.
     */
    float getFloat(int index);

    /**
     * Gets an int array at the given index (for lists of int arrays).
     */
    int[] getIntArray(int index);

    /**
     * @return the NBT type ID of elements in this list (see {@link noppes.npcs.constants.NBTTypes}),
     *         or 0 if the list is empty
     */
    int getElementType();

    // --- Mutators ---

    /**
     * Appends a compound tag to the end of this list.
     */
    void addCompound(INBTCompound compound);

    /**
     * Appends a string to the end of this list.
     */
    void addString(String value);

    /**
     * Appends an integer tag to the end of this list.
     */
    void addInt(int value);

    /**
     * Appends a double tag to the end of this list.
     */
    void addDouble(double value);

    /**
     * Removes and returns the element at the given index.
     */
    void remove(int index);

    // --- Interop ---

    /**
     * Returns the underlying MC NBT list object.
     * CORE code should NEVER call this — only mc* modules should.
     */
    Object getUnderlyingTag();
}
