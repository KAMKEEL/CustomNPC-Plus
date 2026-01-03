/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api
 */

export interface INbt {

    // Methods
    remove(key: string): void;
    has(key: string): boolean;
    getBoolean(key: string): boolean;
    setBoolean(key: string, value: boolean): void;
    getShort(key: string): short;
    setShort(key: string, value: short): void;
    getInteger(key: string): number;
    setInteger(key: string, value: number): void;
    getByte(key: string): byte;
    setByte(key: string, value: byte): void;
    getLong(key: string): number;
    setLong(key: string, value: number): void;
    getDouble(key: string): number;
    setDouble(key: string, value: number): void;
    getFloat(key: string): number;
    setFloat(key: string, value: number): void;
    getString(key: string): string;
    setString(key: string, value: string): void;
    getByteArray(key: string): byte[];
    setByteArray(key: string, value: byte[]): void;
    getIntegerArray(key: string): number[];
    setIntegerArray(key: string, value: number[]): void;
    getList(key: string, value: number): any[];
    getListType(key: string): number;
    setList(key: string, value: any[]): void;
    getCompound(key: string): import('./INbt').INbt;
    setCompound(key: string, value: import('./INbt').INbt): void;
    getKeys(): string[];
    getType(key: string): number;
    getMCNBT(): NBTTagCompound;
    toJsonString(): string;
    isEqual(nbt: import('./INbt').INbt): boolean;
    clear(): void;

}
