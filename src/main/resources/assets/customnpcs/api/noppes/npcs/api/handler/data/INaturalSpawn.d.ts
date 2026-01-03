/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

export interface INaturalSpawn {

    // Methods
    setName(name: string): void;
    getName(): string;
    setEntity(entity: import('../../entity/IEntity').IEntity, slot: number): void;
    getEntity(world: import('../../IWorld').IWorld, slot: number): import('../../entity/IEntity').IEntity;
    getSlots(): Integer[];
    setWeight(weight: number): void;
    getWeight(): number;
    setMinHeight(height: number): void;
    getMinHeight(): number;
    setMaxHeight(height: number): void;
    getMaxHeight(): number;
    spawnsLikeAnimal(spawns: boolean): void;
    spawnsLikeAnimal(): boolean;
    spawnsLikeMonster(spawns: boolean): void;
    spawnsLikeMonster(): boolean;
    spawnsInLiquid(spawns: boolean): void;
    spawnsInLiquid(): boolean;
    spawnsInAir(spawns: boolean): void;
    spawnsInAir(): boolean;
    getBiomes(): string[];
    setBiomes(biomes: string[]): void;

}
