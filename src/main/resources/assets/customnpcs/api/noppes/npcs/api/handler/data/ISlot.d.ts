/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

export interface ISlot {

    // Methods
    getId(): number;
    getName(): string;
    setName(name: string): void;
    getLastLoaded(): number;
    setLastLoaded(time: number): void;
    isTemporary(): boolean;
    setTemporary(temporary: boolean): void;
    getComponents(): object<string, NBTTagCompound;
    setComponentData(key: string, data: NBTTagCompound): void;
    getComponentData(key: string): NBTTagCompound;
    toNBT(): NBTTagCompound;

}
