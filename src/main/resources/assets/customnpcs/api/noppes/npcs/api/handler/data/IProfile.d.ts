/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

export interface IProfile {

    // Methods
    getPlayer(): import('../../entity/IPlayer').IPlayer;
    getCurrentSlotId(): number;
    getSlots(): object<Integer, ISlot;
    writeToNBT(): NBTTagCompound;

}
