package kamkeel.npcs.platform.entity;

import noppes.npcs.platform.nbt.INBTCompound;

/**
 * Platform-independent item stack abstraction.
 */
public interface IPlatformStack {

    /**
     * @return the item's registry ID (e.g., "minecraft:stone")
     */
    String getItemId();

    int getCount();

    void setCount(int count);

    /**
     * @return the item's damage/metadata value
     */
    int getDamage();

    void setDamage(int dmg);

    /**
     * @return the stack's NBT tag compound, or null if none
     */
    INBTCompound getTag();

    void setTag(INBTCompound tag);

    /**
     * @return true if this stack is empty (null item or count <= 0)
     */
    boolean isEmpty();

    /**
     * @return a deep copy of this stack
     */
    IPlatformStack copy();

    /**
     * Returns the underlying MC ItemStack object.
     * Core code should NEVER call this.
     *
     * @return the raw MC ItemStack
     */
    Object getHandle();
}
