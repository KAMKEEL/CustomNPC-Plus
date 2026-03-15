/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity
 */

/**
 * Represents a living entity (mob, NPC, etc.) with additional methods to manage health,
 * damage, targeting, vision, potion effects, equipment, and various attributes.
 * <p>
 * This interface extends {@link IEntity} and provides functionality specific to living entities.
 *
 * @param <T> the underlying Minecraft EntityLivingBase type.
  * @javaFqn noppes.npcs.api.entity.IEntityLivingBase
*/
export interface IEntityLivingBase<T extends EntityLivingBase /* net.minecraft.entity.EntityLivingBase */> extends import('./IEntity').IEntity {
    /**
     * Returns the entity's current health.
     *
     * @return the health value.
     */
    getHealth(): import('./float').float;
    /**
     * Sets the entity's health to the specified value.
     *
     * @param health the new health value.
     */
    setHealth(health: import('./float').float): import('./void').void;
    /**
     * Applies damage to this entity using a generic damage source.
     *
     * @param damage the damage amount.
     */
    hurt(damage: import('./float').float): import('./void').void;
    /**
     * Applies damage to this entity with the given source entity.
     *
     * @param damage the damage amount.
     * @param source the source entity.
     */
    hurt(damage: import('./float').float, source: import('./IEntity').IEntity): import('./void').void;
    /**
     * Applies damage to this entity using a custom damage source.
     *
     * @param damage       the damage amount.
     * @param damageSource the damage source.
     */
    hurt(damage: import('./float').float, damageSource: import('../IDamageSource').IDamageSource): import('./void').void;
    /**
     * Sets the maximum hurt time (hurt resistance time) for this entity.
     *
     * @param time the time in ticks.
     */
    setMaxHurtTime(time: import('./int').int): import('./void').void;
    /**
     * Returns the maximum hurt time (hurt resistance time) for this entity.
     *
     * @return the time in ticks.
     */
    getMaxHurtTime(): import('./int').int;
    /**
     * Returns the entity's maximum health.
     *
     * @return the maximum health.
     */
    getMaxHealth(): import('./double').double;
    /**
     * Returns the follow range attribute of the entity.
     *
     * @return the follow range.
     */
    getFollowRange(): import('./double').double;
    /**
     * Returns the knockback resistance of the entity.
     *
     * @return the knockback resistance.
     */
    getKnockbackResistance(): import('./double').double;
    /**
     * Returns the movement speed of the entity.
     *
     * @return the speed.
     */
    getSpeed(): import('./double').double;
    /**
     * Returns the melee strength (attack damage) of the entity.
     *
     * @return the melee strength.
     */
    getMeleeStrength(): import('./double').double;
    /**
     * Sets the entity's maximum health.
     *
     * @param health the new maximum health.
     */
    setMaxHealth(health: import('./double').double): import('./void').void;
    /**
     * Sets the follow range of the entity.
     *
     * @param range the new follow range.
     */
    setFollowRange(range: import('./double').double): import('./void').void;
    /**
     * Sets the knockback resistance of the entity.
     *
     * @param knockbackResistance the new knockback resistance.
     */
    setKnockbackResistance(knockbackResistance: import('./double').double): import('./void').void;
    /**
     * Sets the movement speed of the entity.
     *
     * @param speed the new movement speed.
     */
    setSpeed(speed: import('./double').double): import('./void').void;
    /**
     * Sets the melee strength (attack damage) of the entity.
     *
     * @param attackDamage the new attack damage.
     */
    setMeleeStrength(attackDamage: import('./double').double): import('./void').void;
    /**
     * @return true if this entity is currently attacking a target; false otherwise.
     */
    isAttacking(): import('./boolean').boolean;
    /**
     * Sets the attack target for this entity.
     *
     * @param living the target entity.
     */
    setAttackTarget(living: import('./IEntityLivingBase').IEntityLivingBase): import('./void').void;
    /**
     * Returns the current attack target.
     *
     * @return the target entity.
     */
    getAttackTarget(): import('./IEntityLivingBase').IEntityLivingBase;
    /**
     * Returns the time (in ticks) for which the current attack target has been active.
     *
     * @return the attack target time.
     */
    getAttackTargetTime(): import('./int').int;
    /**
     * Sets the last attacker for this entity.
     *
     * @param p_130011_1_ the last attacker.
     */
    setLastAttacker(p_130011_1_: import('./IEntity').IEntity): import('./void').void;
    /**
     * Returns the last attacker of this entity.
     *
     * @return the last attacker.
     */
    getLastAttacker(): import('./IEntity').IEntity;
    /**
     * Returns the time (in ticks) since the entity was last attacked.
     *
     * @return the last attacker time.
     */
    getLastAttackerTime(): import('./int').int;
    /**
     * Checks whether the entity can breathe underwater.
     *
     * @return true if it can breathe underwater; false otherwise.
     */
    canBreatheUnderwater(): import('./boolean').boolean;
    /**
     * Returns the EntityType (as defined in scripting constants) for this entity.
     *
     * @return the entity type.
     */
    getType(): import('./int').int;
    /**
     * Checks if this entity is of the specified type.
     *
     * @param type the type to check.
     * @return true if this entity is of that type; false otherwise.
     */
    typeOf(type: import('./int').int): import('./boolean').boolean;
    /**
     * Returns the look vector of this entity as an IPos object.
     *
     * @return the look vector.
     */
    getLookVector(): import('../IPos').IPos;
    /**
     * Returns the block that the entity is looking at, with detailed stopping conditions.
     *
     * @param maxDistance     the maximum distance to check.
     * @param stopOnBlock     whether to stop on any block.
     * @param stopOnLiquid    whether to stop on liquids.
     * @param stopOnCollision whether to stop on collisions.
     * @return the block being looked at.
     */
    getLookingAtBlock(maxDistance: import('./int').int, stopOnBlock: import('./boolean').boolean, stopOnLiquid: import('./boolean').boolean, stopOnCollision: import('./boolean').boolean): import('../IBlock').IBlock;
    /**
     * Returns the first block found by following the entity's look vector,
     * using default parameters (stop on block).
     *
     * @param maxDistance the maximum distance to check.
     * @return the block being looked at.
     */
    getLookingAtBlock(maxDistance: import('./int').int): import('../IBlock').IBlock;
    /**
     * Returns the position (IPos) corresponding to the final obstructed point along the entity's look vector,
     * with detailed stopping conditions.
     *
     * @param maxDistance     the maximum distance to check.
     * @param stopOnBlock     whether to stop on blocks.
     * @param stopOnLiquid    whether to stop on liquids.
     * @param stopOnCollision whether to stop on collisions.
     * @return the obstructed position, or the final position if unobstructed.
     */
    getLookingAtPos(maxDistance: import('./int').int, stopOnBlock: import('./boolean').boolean, stopOnLiquid: import('./boolean').boolean, stopOnCollision: import('./boolean').boolean): import('../IPos').IPos;
    /**
     * Returns the position (IPos) corresponding to the final obstructed point along the entity's look vector,
     * using default parameters (stop on block).
     *
     * @param maxDistance the maximum distance to check.
     * @return the position being looked at.
     */
    getLookingAtPos(maxDistance: import('./int').int): import('../IPos').IPos;
    /**
     * Returns an array of entities intersecting with the entity's look vector.
     * Entities are sorted first by proximity along the vector, then by closeness to the vector.
     *
     * @param ignoreEntities  an array of entities to ignore.
     * @param maxDistance     the maximum distance to check.
     * @param offset          the offset along the vector.
     * @param range           the search radius around the vector.
     * @param stopOnBlock     whether to stop on blocks.
     * @param stopOnLiquid    whether to stop on liquids.
     * @param stopOnCollision whether to stop on collisions.
     * @return an array of entities.
     */
    getLookingAtEntities(ignoreEntities: import('./IEntity').IEntity[], maxDistance: import('./int').int, offset: import('./double').double, range: import('./double').double, stopOnBlock: import('./boolean').boolean, stopOnLiquid: import('./boolean').boolean, stopOnCollision: import('./boolean').boolean): import('./IEntity').IEntity[];
    /**
     * Returns an array of entities intersecting with the entity's look vector with detailed stop conditions.
     *
     * @param maxDistance     the maximum distance.
     * @param offset          the offset.
     * @param range           the search radius.
     * @param stopOnBlock     whether to stop on blocks.
     * @param stopOnLiquid    whether to stop on liquids.
     * @param stopOnCollision whether to stop on collisions.
     * @return an array of entities.
     */
    getLookingAtEntities(maxDistance: import('./int').int, offset: import('./double').double, range: import('./double').double, stopOnBlock: import('./boolean').boolean, stopOnLiquid: import('./boolean').boolean, stopOnCollision: import('./boolean').boolean): import('./IEntity').IEntity[];
    /**
     * Returns an array of entities intersecting with the entity's look vector using default stop conditions.
     *
     * @param maxDistance the maximum distance.
     * @param offset      the offset.
     * @param range       the search radius.
     * @return an array of entities.
     */
    getLookingAtEntities(maxDistance: import('./int').int, offset: import('./double').double, range: import('./double').double): import('./IEntity').IEntity[];
    /**
     * Expert use only.
     * Returns the underlying Minecraft EntityLivingBase object.
     *
     * @return the Minecraft entity.
     */
    getMCEntity(): T;
    /**
     * Makes the entity swing its hand.
     */
    swingHand(): import('./void').void;
    /**
     * Applies a potion effect to the entity.
     * Works similarly to the /effect command.
     *
     * @param effect        the potion effect ID.
     * @param duration      the duration in seconds.
     * @param strength      the amplifier of the effect.
     * @param hideParticles whether to hide potion particles.
     */
    addPotionEffect(effect: import('./int').int, duration: import('./int').int, strength: import('./int').int, hideParticles: import('./boolean').boolean): import('./void').void;
    /**
     * Clears all active potion effects from the entity.
     */
    clearPotionEffects(): import('./void').void;
    /**
     * Returns the amplifier of the specified potion effect.
     * If the effect is not active, returns -1.
     *
     * @param effect the potion effect ID.
     * @return the amplifier, or -1 if not active.
     */
    getPotionEffect(effect: import('./int').int): import('./int').int;
    /**
     * Returns the item the entity is currently holding.
     * Note that not all living entities support this.
     *
     * @return the held item.
     */
    getHeldItem(): import('../item/IItemStack').IItemStack;
    /**
     * Sets the item the entity is holding.
     * Note that not all living entities support this.
     *
     * @param item the item to set.
     */
    setHeldItem(item: import('../item/IItemStack').IItemStack): import('./void').void;
    /**
     * Returns the armor piece in the given slot.
     * Slots: 0 - boots, 1 - pants, 2 - body, 3 - head.
     *
     * @param slot the armor slot.
     * @return the armor item.
     */
    getArmor(slot: import('./int').int): import('../item/IItemStack').IItemStack;
    /**
     * Sets the armor piece in the given slot.
     * Slots: 0 - boots, 1 - pants, 2 - body, 3 - head.
     *
     * @param slot the armor slot.
     * @param item the item to set.
     */
    setArmor(slot: import('./int').int, item: import('../item/IItemStack').IItemStack): import('./void').void;
    /**
     * @return true if this entity is a child.
     */
    isChild(): import('./boolean').boolean;
    /**
     * Renders a broken item stack effect for the given item.
     *
     * @param itemStack the item stack.
     */
    renderBrokenItemStack(itemStack: import('../item/IItemStack').IItemStack): import('./void').void;
    /**
     * @return true if the entity is on a ladder.
     */
    isOnLadder(): import('./boolean').boolean;
    /**
     * Returns the total armor value of the entity.
     *
     * @return the armor value.
     */
    getTotalArmorValue(): import('./int').int;
    /**
     * Returns the number of arrows currently embedded in the entity.
     *
     * @return the arrow count.
     */
    getArrowCountInEntity(): import('./int').int;
    /**
     * Sets the number of arrows embedded in the entity.
     *
     * @param count the new arrow count.
     */
    setArrowCountInEntity(count: import('./int').int): import('./void').void;
    /**
     * Dismounts the specified entity from this entity.
     *
     * @param entity the entity to dismount.
     */
    dismountEntity(entity: import('./IEntity').IEntity): import('./void').void;
    /**
     * Sets the AI move speed for the entity.
     *
     * @param speed the speed value.
     */
    setAIMoveSpeed(speed: import('./float').float): import('./void').void;
    /**
     * Returns the AI move speed of the entity.
     *
     * @return the speed.
     */
    getAIMoveSpeed(): import('./float').float;
    /**
     * Sets the absorption (extra health) amount.
     *
     * @param amount the absorption amount.
     */
    setAbsorptionAmount(amount: import('./float').float): import('./void').void;
    /**
     * Returns the absorption (extra health) amount.
     *
     * @return the absorption amount.
     */
    getAbsorptionAmount(): import('./float').float;
    /**
     * Sets the current hurt time (red flash duration) of the entity.
     *
     * @param time the hurt time in ticks.
     */
    setHurtTime(time: import('./int').int): import('./void').void;
    /**
     * Applies knockback to the entity, pushing it away from the source entity.
     * Uses Minecraft's knockback mechanics which respects the entity's knockback resistance.
     * A strength of 1.0 is equivalent to a standard melee hit.
     *
     * @param strength the knockback strength multiplier.
     * @param source   the source entity to knock back away from.
     */
    applyKnockback(strength: import('./float').float, source: import('./IEntity').IEntity): import('./void').void;
    /**
     * Applies knockback to the entity in the specified direction.
     * Uses Minecraft's knockback mechanics which respects the entity's knockback resistance.
     * The direction vector does not need to be normalized.
     * A strength of 1.0 is equivalent to a standard melee hit.
     *
     * @param strength the knockback strength multiplier.
     * @param dirX     the x component of the knockback direction.
     * @param dirZ     the z component of the knockback direction.
     */
    applyKnockback(strength: import('./float').float, dirX: import('./double').double, dirZ: import('./double').double): import('./void').void;
    /**
     * Forcefully applies knockback to the entity, pushing it away from the source entity.
     * This ignores the entity's knockback resistance attribute.
     * A strength of 1.0 is equivalent to a standard melee hit.
     *
     * @param strength the knockback strength multiplier.
     * @param source   the source entity to knock back away from.
     */
    forceKnockback(strength: import('./float').float, source: import('./IEntity').IEntity): import('./void').void;
    /**
     * Forcefully applies knockback to the entity in the specified direction.
     * This ignores the entity's knockback resistance attribute.
     * The direction vector does not need to be normalized.
     * A strength of 1.0 is equivalent to a standard melee hit.
     *
     * @param strength the knockback strength multiplier.
     * @param dirX     the x component of the knockback direction.
     * @param dirZ     the z component of the knockback direction.
     */
    forceKnockback(strength: import('./float').float, dirX: import('./double').double, dirZ: import('./double').double): import('./void').void;
}
