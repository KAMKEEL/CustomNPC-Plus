package noppes.npcs.core;

import kamkeel.npcs.platform.PlatformServiceHolder;
import noppes.npcs.api.INbt;
import noppes.npcs.api.INbtList;

/**
 * Convenience factory for creating NBT objects in CORE code.
 * Shorthand for PlatformServiceHolder.get().nbt().createCompound(), etc.
 */
public final class NBT {

    private NBT() {}

    /**
     * Creates a new, empty NBT compound.
     */
    public static INbt compound() {
        return PlatformServiceHolder.get().nbt().createCompound();
    }

    /**
     * Creates a new, empty NBT list.
     */
    public static INbtList list() {
        return PlatformServiceHolder.get().nbt().createList();
    }
}
