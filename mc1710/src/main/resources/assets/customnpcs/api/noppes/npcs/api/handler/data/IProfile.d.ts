/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

/**
 * @javaFqn noppes.npcs.api.handler.data.IProfile
 */
export interface IProfile {
    /**
     * @return the IPlayer attached to the Profile
     */
    getPlayer(): import('../../entity/IPlayer').IPlayer;
    /**
     * @return The profiles current Slot ID
     */
    getCurrentSlotId(): import('./int').int;
    /**
     * @return Map of all Slot IDs
     */
    getSlots(): Java.java.util.Map<Integer, import('./ISlot').ISlot>;
    /**
     * @return FULL NBT of the Profile
     */
    writeToNBT(): import('../../../../../net/minecraft/nbt/NBTTagCompound').NBTTagCompound;
    player: import('./EntityPlayer').EntityPlayer;
    currentSlotId: import('./int').int;
    sharedQuestTimestamps: Java.java.util.Map<Integer, Long>;
}
