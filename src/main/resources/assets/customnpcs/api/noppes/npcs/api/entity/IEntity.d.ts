/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity
 */

export interface IEntity {

    // Methods
    spawnParticle(entityParticle: import('../IParticle').IParticle): void;
    getEntityId(): number;
    getUniqueID(): string;
    getYOffset(): number;
    getWidth(): number;
    getHeight(): number;
    getX(): number;
    setX(x: number): void;
    getY(): number;
    setY(y: number): void;
    getZ(): number;
    setZ(z: number): void;
    getMotionX(): number;
    setMotionX(x: number): void;
    getMotionY(): number;
    setMotionY(y: number): void;
    getMotionZ(): number;
    setMotionZ(z: number): void;
    setMotion(x: number, y: number, z: number): void;
    setMotion(pos: import('../IPos').IPos): void;
    getMotion(): import('../IPos').IPos;
    isAirborne(): boolean;
    getBlockX(): number;
    getBlockY(): number;
    getBlockZ(): number;
    setPosition(x: number, y: number, z: number): void;
    setPosition(pos: import('../IPos').IPos): void;
    getPosition(): import('../IPos').IPos;
    getDimension(): number;
    setDimension(dimensionId: number): void;
    getCollidingEntities(): import('./IEntity').IEntity[];
    getSurroundingEntities(range: number): import('./IEntity').IEntity[];
    getSurroundingEntities(range: number, type: number): import('./IEntity').IEntity[];
    isAlive(): boolean;
    getTempData(key: string): any;
    setTempData(key: string, value: any): void;
    hasTempData(key: string): boolean;
    removeTempData(key: string): void;
    clearTempData(): void;
    getTempDataKeys(): string[];
    getStoredData(key: string): any;
    setStoredData(key: string, value: any): void;
    hasStoredData(key: string): boolean;
    removeStoredData(key: string): void;
    clearStoredData(): void;
    getStoredDataKeys(): string[];
    getAge(): number;
    despawn(): void;
    inWater(): boolean;
    inLava(): boolean;
    inFire(): boolean;
    isBurning(): boolean;
    setBurning(ticks: number): void;
    extinguish(): void;
    getTypeName(): string;
    dropItem(item: import('../item/IItemStack').IItemStack): void;
    getRider(): import('./IEntity').IEntity;
    setRider(entity: import('./IEntity').IEntity): void;
    getMount(): import('./IEntity').IEntity;
    setMount(entity: import('./IEntity').IEntity): void;
    getType(): number;
    typeOf(type: number): boolean;
    setRotation(rotation: number): void;
    setRotation(rotationYaw: number, rotationPitch: number): void;
    getRotation(): number;
    setPitch(pitch: number): void;
    getPitch(): number;
    knockback(power: number, direction: number): void;
    knockback(xpower: number, ypower: number, zpower: number, direction: number): void;
    knockback(pos: import('../IPos').IPos, direction: number): void;
    setImmune(ticks: number): void;
    setInvisible(invisible: boolean): void;
    setSneaking(sneaking: boolean): void;
    setSprinting(sprinting: boolean): void;
    hasCollided(): boolean;
    hasCollidedVertically(): boolean;
    hasCollidedHorizontally(): boolean;
    capturesDrops(): boolean;
    setCapturesDrops(capture: boolean): void;
    setCapturedDrops(capturedDrops: IEntity<?[]): void;
    getCapturedDrops(): [];
    isSneaking(): boolean;
    isSprinting(): boolean;
    getMCEntity(): T;
    getNbt(): import('../INbt').INbt;
    getAllNbt(): import('../INbt').INbt;
    setNbt(nbt: import('../INbt').INbt): void;
    getNbtOptional(): import('../INbt').INbt;
    storeAsClone(tab: number, name: string): void;
    getWorld(): import('../IWorld').IWorld;
    updateEntity(): void;

}
