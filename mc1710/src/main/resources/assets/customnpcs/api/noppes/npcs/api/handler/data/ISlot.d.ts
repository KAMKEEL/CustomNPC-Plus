/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

/**
 * @javaFqn noppes.npcs.api.handler.data.ISlot
 */
export interface ISlot {
    /**
     * @return id of slot
     */
    getId(): import('./int').int;
    /**
     * @return Name of Slot
     */
    getName(): String;
    /**
     * @param name - New name of slot
     */
    setName(name: String): import('./void').void;
    /**
     * @return Last time Slot was Saved
     */
    getLastLoaded(): import('./long').long;
    /**
     * @param time - Long time for when it was last saved
     */
    setLastLoaded(time: import('./long').long): import('./void').void;
    /**
     * @return if the slot is temporary
     */
    isTemporary(): import('./boolean').boolean;
    /**
     * @param temporary - Setting a slot to temporary won't save it to Profile
     */
    setTemporary(temporary: import('./boolean').boolean): import('./void').void;
    /**
     * @return A map of all the NBTs within a slot
     */
    getComponents(): Java.java.util.Map<String, import('../../../../../net/minecraft/nbt/NBTTagCompound').NBTTagCompound>;
    /**
     * Sets the NBT data for a specific component key.
     *
     * @param key  the component key (e.g. "CNPC+", "DBC").
     * @param data the NBT data to store.
     */
    setComponentData(key: String, data: import('../../../../../net/minecraft/nbt/NBTTagCompound').NBTTagCompound): import('./void').void;
    /**
     * @param key - The KEY of the NBT for the Slot: [CNPC+, DBC... etc]
     * @return NBT for that that key
     */
    getComponentData(key: String): import('../../../../../net/minecraft/nbt/NBTTagCompound').NBTTagCompound;
    /**
     * @return The full NBT of the Slot
     */
    toNBT(): import('../../../../../net/minecraft/nbt/NBTTagCompound').NBTTagCompound;
}
