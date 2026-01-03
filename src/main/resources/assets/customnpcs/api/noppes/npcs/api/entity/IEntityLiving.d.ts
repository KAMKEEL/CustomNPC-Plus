/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity
 */

export interface IEntityLiving extends IEntityLivingBase<T> {

    // Methods
    isNavigating(): boolean;
    clearNavigation(): void;
    navigateTo(x: number, y: number, z: number, speed: number): void;
    getMCEntity(): T;
    playLivingSound(): void;
    spawnExplosionParticle(): void;
    setMoveForward(speed: number): void;
    faceEntity(entity: import('./IEntity').IEntity, pitch: number, yaw: number): void;
    canPickUpLoot(): boolean;
    setCanPickUpLoot(pickUp: boolean): void;
    isPersistent(): boolean;
    enablePersistence(): void;
    setCustomNameTag(text: string): void;
    getCustomNameTag(): string;
    hasCustomNameTag(): boolean;
    setAlwaysRenderNameTag(alwaysRender: boolean): void;
    getAlwaysRenderNameTag(): boolean;
    clearLeashed(sendPacket: boolean, dropLeash: boolean): void;
    allowLeashing(): boolean;
    getLeashed(): boolean;
    getLeashedTo(): import('./IEntity').IEntity;
    setLeashedTo(entity: import('./IEntity').IEntity, sendPacket: boolean): void;
    canBeSteered(): boolean;

}
