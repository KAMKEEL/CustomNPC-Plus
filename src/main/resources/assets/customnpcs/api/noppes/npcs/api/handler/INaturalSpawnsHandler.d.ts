/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler
 */

export interface INaturalSpawnsHandler {

    // Methods
    save(): void;
    getSpawns(): import('./data/INaturalSpawn').INaturalSpawn[];
    getSpawns(biome: string): import('./data/INaturalSpawn').INaturalSpawn[];
    addSpawn(spawn: import('./data/INaturalSpawn').INaturalSpawn): void;
    removeSpawn(spawn: import('./data/INaturalSpawn').INaturalSpawn): void;
    createSpawn(): import('./data/INaturalSpawn').INaturalSpawn;

}
