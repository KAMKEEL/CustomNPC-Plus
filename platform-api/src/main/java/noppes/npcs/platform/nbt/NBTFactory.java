package noppes.npcs.platform.nbt;

/**
 * Factory for creating new NBT instances.
 * Each mc* module provides an implementation via PlatformService.
 */
public interface NBTFactory {

    /**
     * Creates a new, empty compound tag.
     */
    INBTCompound createCompound();

    /**
     * Creates a new, empty list tag.
     */
    INBTList createList();

    /**
     * Wraps a raw MC NBT compound object into an INBTCompound.
     * Used at the boundary when receiving NBT from MC code.
     *
     * @param mcNBTTagCompound the raw MC NBTTagCompound / CompoundTag
     * @return the wrapped interface
     * @throws IllegalArgumentException if the object is not a valid MC NBT compound
     */
    INBTCompound wrap(Object mcNBTTagCompound);

    /**
     * Wraps a raw MC NBT list object into an INBTList.
     *
     * @param mcNBTTagList the raw MC NBTTagList / ListTag
     * @return the wrapped interface
     * @throws IllegalArgumentException if the object is not a valid MC NBT list
     */
    INBTList wrapList(Object mcNBTTagList);
}
