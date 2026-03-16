package common.minecraft.nbt;

/**
 * Platform abstraction for Minecraft's NBT base tag type.
 * MC 1.7.10: NBTBase
 * MC 1.12+: NBTBase / INBT (1.16+)
 */
public interface INbtBase {
    byte getId();
    INbtBase copy();
    String toString();
}
