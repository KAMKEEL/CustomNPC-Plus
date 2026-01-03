/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.block
 */

export interface IBlockScripted extends IBlock {

    // Methods
    setModel(item: import('../item/IItemStack').IItemStack): void;
    setModel(name: string): void;
    getModel(): import('../item/IItemStack').IItemStack;
    getTimers(): import('../ITimers').ITimers;
    setRedstonePower(strength: number): void;
    getRedstonePower(): number;
    setIsLadder(enabled: boolean): void;
    getIsLadder(): boolean;
    setLight(value: number): void;
    getLight(): number;
    setScale(x: number, y: number, z: number): void;
    getScaleX(): number;
    getScaleY(): number;
    getScaleZ(): number;
    setRotation(x: number, y: number, z: number): void;
    getRotationX(): number;
    getRotationY(): number;
    getRotationZ(): number;
    executeCommand(command: string): void;
    getIsPassible(): boolean;
    setIsPassible(bo: boolean): void;
    getIsPassable(): boolean;
    setIsPassable(bo: boolean): void;
    getHardness(): number;
    setHardness(hardness: number): void;
    getResistance(): number;
    setResistance(resistance: number): void;
    getTextPlane(): import('./ITextPlane').ITextPlane;
    getTextPlane2(): import('./ITextPlane').ITextPlane;
    getTextPlane3(): import('./ITextPlane').ITextPlane;
    getTextPlane4(): import('./ITextPlane').ITextPlane;
    getTextPlane5(): import('./ITextPlane').ITextPlane;
    getTextPlane6(): import('./ITextPlane').ITextPlane;
    setStoredData(key: string, value: any): void;
    getStoredData(key: string): any;
    removeStoredData(key: string): void;
    hasStoredData(key: string): boolean;
    clearStoredData(): void;
    getStoredDataKeys(): string[];
    removeTempData(key: string): void;
    setTempData(key: string, value: any): void;
    hasTempData(key: string): boolean;
    getTempData(key: string): any;
    clearTempData(): void;
    getTempDataKeys(): string[];

}
