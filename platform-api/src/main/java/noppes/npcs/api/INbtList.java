package noppes.npcs.api;

/**
 * Version-independent abstraction over Minecraft's NBT list tag.
 *
 * Platform-api version (MC-free). The src/api shadow may add MC-typed methods.
 */
public interface INbtList {

    int size();

    INbt getCompound(int index);

    String getString(int index);

    int getInt(int index);

    double getDouble(int index);

    float getFloat(int index);

    int[] getIntArray(int index);

    /**
     * @return the NBT type ID of elements in this list, or 0 if empty
     */
    int getElementType();

    // --- Mutators ---

    void addCompound(INbt compound);

    void addString(String value);

    void addInt(int value);

    void addDouble(double value);

    void remove(int index);

    /**
     * Returns the underlying MC NBT list object.
     * CORE code should NEVER call this — only mc* modules should.
     */
    Object getMCTagList();
}
