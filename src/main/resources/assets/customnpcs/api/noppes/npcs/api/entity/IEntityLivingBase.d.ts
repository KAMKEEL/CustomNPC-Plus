/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity
 */

export interface IEntityLivingBase<T> extends import('./IEntity').IEntity {

    // Methods
    getHealth(): number;
    setHealth(health: number): void;
    hurt(damage: number): void;
    hurt(damage: number, source: import('./IEntity').IEntity): void;
    hurt(damage: number, damageSource: import('../IDamageSource').IDamageSource): void;
    setMaxHurtTime(time: number): void;
    getMaxHurtTime(): number;
    getMaxHealth(): number;
    getFollowRange(): number;
    getKnockbackResistance(): number;
    getSpeed(): number;
    getMeleeStrength(): number;
    setMaxHealth(health: number): void;
    setFollowRange(range: number): void;
    setKnockbackResistance(knockbackResistance: number): void;
    setSpeed(speed: number): void;
    setMeleeStrength(attackDamage: number): void;
    isAttacking(): boolean;
    setAttackTarget(living: IEntityLivingBase): void;
    getAttackTarget(): IEntityLivingBase;
    getAttackTargetTime(): number;
    setLastAttacker(p_130011_1_: import('./IEntity').IEntity): void;
    getLastAttacker(): import('./IEntity').IEntity;
    getLastAttackerTime(): number;
    canBreatheUnderwater(): boolean;
    getType(): number;
    typeOf(type: number): boolean;
    getLookVector(): import('../IPos').IPos;
    getLookingAtBlock(maxDistance: number, stopOnBlock: boolean, stopOnLiquid: boolean, stopOnCollision: boolean): import('../IBlock').IBlock;
    getLookingAtBlock(maxDistance: number): import('../IBlock').IBlock;
    getLookingAtPos(maxDistance: number, stopOnBlock: boolean, stopOnLiquid: boolean, stopOnCollision: boolean): import('../IPos').IPos;
    getLookingAtPos(maxDistance: number): import('../IPos').IPos;
    getLookingAtEntities(ignoreEntities: import('./IEntity').IEntity[], maxDistance: number, offset: number, range: number, stopOnBlock: boolean, stopOnLiquid: boolean, stopOnCollision: boolean): import('./IEntity').IEntity[];
    getLookingAtEntities(maxDistance: number, offset: number, range: number, stopOnBlock: boolean, stopOnLiquid: boolean, stopOnCollision: boolean): import('./IEntity').IEntity[];
    getLookingAtEntities(maxDistance: number, offset: number, range: number): import('./IEntity').IEntity[];
    getMCEntity(): T;
    swingHand(): void;
    addPotionEffect(effect: number, duration: number, strength: number, hideParticles: boolean): void;
    clearPotionEffects(): void;
    getPotionEffect(effect: number): number;
    getHeldItem(): import('../item/IItemStack').IItemStack;
    setHeldItem(item: import('../item/IItemStack').IItemStack): void;
    getArmor(slot: number): import('../item/IItemStack').IItemStack;
    setArmor(slot: number, item: import('../item/IItemStack').IItemStack): void;
    isChild(): boolean;
    renderBrokenItemStack(itemStack: import('../item/IItemStack').IItemStack): void;
    isOnLadder(): boolean;
    getTotalArmorValue(): number;
    getArrowCountInEntity(): number;
    setArrowCountInEntity(count: number): void;
    dismountEntity(entity: import('./IEntity').IEntity): void;
    setAIMoveSpeed(speed: number): void;
    getAIMoveSpeed(): number;
    setAbsorptionAmount(amount: number): void;
    getAbsorptionAmount(): number;

}
