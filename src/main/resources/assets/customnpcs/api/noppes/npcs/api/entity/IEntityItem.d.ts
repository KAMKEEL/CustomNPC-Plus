/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity
 */

export interface IEntityItem extends IEntity<T> {

    // Methods
    getOwner(): string;
    setOwner(name: string): void;
    getThrower(): string;
    setThrower(name: string): void;
    getPickupDelay(): number;
    setPickupDelay(delay: number): void;
    getAge(): number;
    setAge(age: number): void;
    getLifeSpawn(): number;
    setLifeSpawn(age: number): void;
    getItem(): import('../item/IItemStack').IItemStack;
    setItem(item: import('../item/IItemStack').IItemStack): void;

}
