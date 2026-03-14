package noppes.npcs.constants;

/**
 * NBT type ID constants, matching Minecraft's internal NBT type numbering.
 * These are stable across all MC versions.
 *
 * Use these instead of magic numbers when calling INbt.hasKey(key, type)
 * or INbt.getTagList(key, type).
 */
public final class NBTTypes {

    public static final int TAG_END = 0;
    public static final int TAG_BYTE = 1;
    public static final int TAG_SHORT = 2;
    public static final int TAG_INT = 3;
    public static final int TAG_LONG = 4;
    public static final int TAG_FLOAT = 5;
    public static final int TAG_DOUBLE = 6;
    public static final int TAG_BYTE_ARRAY = 7;
    public static final int TAG_STRING = 8;
    public static final int TAG_LIST = 9;
    public static final int TAG_COMPOUND = 10;
    public static final int TAG_INT_ARRAY = 11;

    private NBTTypes() {}
}
