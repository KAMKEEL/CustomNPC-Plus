/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity
 */

export interface IProjectile {

    // Methods
    getItem(): import('../item/IItemStack').IItemStack;
    setItem(item: import('../item/IItemStack').IItemStack): void;
    getHasGravity(): boolean;
    setHasGravity(bo: boolean): void;
    getAccuracy(): number;
    setAccuracy(accuracy: number): void;
    setHeading(entity: import('./IEntity').IEntity): void;
    setHeading(x: number, y: number, z: number): void;
    setHeading(yaw: number, pitch: number): void;
    getThrower(): import('./IEntity').IEntity;
    enableEvents(): void;

}
