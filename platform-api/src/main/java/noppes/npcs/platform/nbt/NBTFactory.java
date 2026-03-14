package noppes.npcs.platform.nbt;

import noppes.npcs.api.INbt;
import noppes.npcs.api.INbtList;

/**
 * Factory for creating new NBT instances.
 * Each mc* module provides an implementation via PlatformService.
 */
public interface NBTFactory {

    /**
     * Creates a new, empty compound tag.
     */
    INbt createCompound();

    /**
     * Creates a new, empty list tag.
     */
    INbtList createList();

    /**
     * Wraps a raw MC NBT compound object into an INbt.
     *
     * @param mcNBTTagCompound the raw MC NBTTagCompound / CompoundTag
     * @return the wrapped interface
     * @throws IllegalArgumentException if the object is not a valid MC NBT compound
     */
    INbt wrap(Object mcNBTTagCompound);

    /**
     * Wraps a raw MC NBT list object into an INbtList.
     *
     * @param mcNBTTagList the raw MC NBTTagList / ListTag
     * @return the wrapped interface
     * @throws IllegalArgumentException if the object is not a valid MC NBT list
     */
    INbtList wrapList(Object mcNBTTagList);
}
