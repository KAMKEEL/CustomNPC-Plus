/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

export interface IMagicData {

    // Methods
    removeMagic(id: number): void;
    hasMagic(id: number): boolean;
    clear(): void;
    isEmpty(): boolean;
    addMagic(id: number, damage: number, split: number): void;
    getMagicDamage(id: number): number;
    getMagicSplit(id: number): number;

}
