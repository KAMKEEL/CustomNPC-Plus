/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler
 */

export interface IMagicHandler {

    // Methods
    getMagic(magicId: number): import('./data/IMagic').IMagic;
    getCycle(cycleID: number): import('./data/IMagicCycle').IMagicCycle;
    addMagicToCycle(magicId: number, cycleId: number, index: number, priority: number): void;
    removeMagicFromCycle(magicId: number, cycleId: number): void;

}
