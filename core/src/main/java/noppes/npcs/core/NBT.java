package noppes.npcs.core;

import noppes.npcs.platform.PlatformServiceHolder;
import noppes.npcs.platform.nbt.INBTCompound;
import noppes.npcs.platform.nbt.INBTList;

/**
 * Convenience factory for creating NBT objects in CORE code.
 * Shorthand for PlatformServiceHolder.get().nbt().createCompound(), etc.
 */
public final class NBT {

    private NBT() {}

    /**
     * Creates a new, empty NBT compound.
     */
    public static INBTCompound compound() {
        return PlatformServiceHolder.get().nbt().createCompound();
    }

    /**
     * Creates a new, empty NBT list.
     */
    public static INBTList list() {
        return PlatformServiceHolder.get().nbt().createList();
    }
}
