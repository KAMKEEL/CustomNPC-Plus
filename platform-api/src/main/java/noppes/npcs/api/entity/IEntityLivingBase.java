package noppes.npcs.api.entity;

import noppes.npcs.api.IBlock;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IPos;
import noppes.npcs.api.item.IItemStack;

/**
 * Represents a living entity (mob, NPC, etc.) with additional methods to manage health,
 * damage, targeting, vision, potion effects, equipment, and various attributes.
 * <p>
 * This interface extends {@link IEntity} and provides functionality specific to living entities.
 */
public interface IEntityLivingBase extends IEntity {

    /**
     * Returns the entity's current health.
     *
     * @return the health value.
     */
    float getHealth();

    /**
     * Sets the entity's health to the specified value.
     *
     * @param health the new health value.
     */
    void setHealth(float health);

    /**
     * Applies damage to this entity using a generic damage source.
     *
     * @param damage the damage amount.
     */
    void hurt(float damage);

    /**
     * Applies damage to this entity with the given source entity.
     *
     * @param damage the damage amount.
     * @param source the source entity.
     */
    void hurt(float damage, IEntity source);

    /**
     * Applies damage to this entity using a custom damage source.
     *
     * @param damage       the damage amount.
     * @param damageSource the damage source.
     */
    void hurt(float damage, IDamageSource damageSource);

    /**
     * Sets the maximum hurt time (hurt resistance time) for this entity.
     *
     * @param time the time in ticks.
     */
    void setMaxHurtTime(int time);

    /**
     * Returns the maximum hurt time (hurt resistance time) for this entity.
     *
     * @return the time in ticks.
     */
    int getMaxHurtTime();

    /**
     * Returns the entity's maximum health.
     *
     * @return the maximum health.
     */
    double getMaxHealth();

    /**
     * Returns the follow range attribute of the entity.
     *
     * @return the follow range.
     */
    double getFollowRange();

    /**
     * Returns the knockback resistance of the entity.
     *
     * @return the knockback resistance.
     */
    double getKnockbackResistance();

    /**
     * Returns the movement speed of the entity.
     *
     * @return the speed.
     */
    double getSpeed();

    /**
     * Returns the melee strength (attack damage) of the entity.
     *
     * @return the melee strength.
     */
    double getMeleeStrength();

    /**
     * Sets the entity's maximum health.
     *
     * @param health the new maximum health.
     */
    void setMaxHealth(double health);

    /**
     * Sets the follow range of the entity.
     *
     * @param range the new follow range.
     */
    void setFollowRange(double range);

    /**
     * Sets the knockback resistance of the entity.
     *
     * @param knockbackResistance the new knockback resistance.
     */
    void setKnockbackResistance(double knockbackResistance);

    /**
     * Sets the movement speed of the entity.
     *
     * @param speed the new movement speed.
     */
    void setSpeed(double speed);

    /**
     * Sets the melee strength (attack damage) of the entity.
     *
     * @param attackDamage the new attack damage.
     */
    void setMeleeStrength(double attackDamage);

    /**
     * @return true if this entity is currently attacking a target; false otherwise.
     */
    boolean isAttacking();

    /**
     * Sets the attack target for this entity.
     *
     * @param living the target entity.
     */
    void setAttackTarget(IEntityLivingBase living);

    /**
     * Returns the current attack target.
     *
     * @return the target entity.
     */
    IEntityLivingBase getAttackTarget();

    /**
     * Returns the time (in ticks) for which the current attack target has been active.
     *
     * @return the attack target time.
     */
    int getAttackTargetTime();

    /**
     * Sets the last attacker for this entity.
     *
     * @param p_130011_1_ the last attacker.
     */
    void setLastAttacker(IEntity p_130011_1_);

    /**
     * Returns the last attacker of this entity.
     *
     * @return the last attacker.
     */
    IEntity getLastAttacker();

    /**
     * Returns the time (in ticks) since the entity was last attacked.
     *
     * @return the last attacker time.
     */
    int getLastAttackerTime();

    /**
     * Checks whether the entity can breathe underwater.
     *
     * @return true if it can breathe underwater; false otherwise.
     */
    boolean canBreatheUnderwater();

    /**
     * Returns the EntityType (as defined in scripting constants) for this entity.
     *
     * @return the entity type.
     */
    int getType();

    /**
     * Checks if this entity is of the specified type.
     *
     * @param type the type to check.
     * @return true if this entity is of that type; false otherwise.
     */
    boolean typeOf(int type);

    /**
     * Returns the look vector of this entity as an IPos object.
     *
     * @return the look vector.
     */
    IPos getLookVector();

    /**
     * Returns the block that the entity is looking at, with detailed stopping conditions.
     *
     * @param maxDistance     the maximum distance to check.
     * @param stopOnBlock     whether to stop on any block.
     * @param stopOnLiquid    whether to stop on liquids.
     * @param stopOnCollision whether to stop on collisions.
     * @return the block being looked at.
     */
    IBlock getLookingAtBlock(int maxDistance, boolean stopOnBlock, boolean stopOnLiquid, boolean stopOnCollision);

    /**
     * Returns the first block found by following the entity's look vector,
     * using default parameters (stop on block).
     *
     * @param maxDistance the maximum distance to check.
     * @return the block being looked at.
     */
    IBlock getLookingAtBlock(int maxDistance);

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
    IPos getLookingAtPos(int maxDistance, boolean stopOnBlock, boolean stopOnLiquid, boolean stopOnCollision);

    /**
     * Returns the position (IPos) corresponding to the final obstructed point along the entity's look vector,
     * using default parameters (stop on block).
     *
     * @param maxDistance the maximum distance to check.
     * @return the position being looked at.
     */
    IPos getLookingAtPos(int maxDistance);

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
    IEntity[] getLookingAtEntities(IEntity[] ignoreEntities, int maxDistance, double offset, double range, boolean stopOnBlock, boolean stopOnLiquid, boolean stopOnCollision);

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
    IEntity[] getLookingAtEntities(int maxDistance, double offset, double range, boolean stopOnBlock, boolean stopOnLiquid, boolean stopOnCollision);

    /**
     * Returns an array of entities intersecting with the entity's look vector using default stop conditions.
     *
     * @param maxDistance the maximum distance.
     * @param offset      the offset.
     * @param range       the search radius.
     * @return an array of entities.
     */
    IEntity[] getLookingAtEntities(int maxDistance, double offset, double range);

    /**
     * Expert use only.
     * Returns the underlying Minecraft EntityLivingBase object.
     *
     * @return the Minecraft entity.
     */
    Object getMCEntity();

    /**
     * Makes the entity swing its hand.
     */
    void swingHand();

    /**
     * Applies a potion effect to the entity.
     * Works similarly to the /effect command.
     *
     * @param effect        the potion effect ID.
     * @param duration      the duration in seconds.
     * @param strength      the amplifier of the effect.
     * @param hideParticles whether to hide potion particles.
     */
    void addPotionEffect(int effect, int duration, int strength, boolean hideParticles);

    /**
     * Clears all active potion effects from the entity.
     */
    void clearPotionEffects();

    /**
     * Returns the amplifier of the specified potion effect.
     * If the effect is not active, returns -1.
     *
     * @param effect the potion effect ID.
     * @return the amplifier, or -1 if not active.
     */
    int getPotionEffect(int effect);

    /**
     * Returns the item the entity is currently holding.
     * Note that not all living entities support this.
     *
     * @return the held item.
     */
    IItemStack getHeldItem();

    /**
     * Sets the item the entity is holding.
     * Note that not all living entities support this.
     *
     * @param item the item to set.
     */
    void setHeldItem(IItemStack item);

    /**
     * Returns the armor piece in the given slot.
     * Slots: 0 - boots, 1 - pants, 2 - body, 3 - head.
     *
     * @param slot the armor slot.
     * @return the armor item.
     */
    IItemStack getArmor(int slot);

    /**
     * Sets the armor piece in the given slot.
     * Slots: 0 - boots, 1 - pants, 2 - body, 3 - head.
     *
     * @param slot the armor slot.
     * @param item the item to set.
     */
    void setArmor(int slot, IItemStack item);

    /**
     * @return true if this entity is a child.
     */
    boolean isChild();

    /**
     * Renders a broken item stack effect for the given item.
     *
     * @param itemStack the item stack.
     */
    void renderBrokenItemStack(IItemStack itemStack);

    /**
     * @return true if the entity is on a ladder.
     */
    boolean isOnLadder();

    /**
     * Returns the total armor value of the entity.
     *
     * @return the armor value.
     */
    int getTotalArmorValue();

    /**
     * Returns the number of arrows currently embedded in the entity.
     *
     * @return the arrow count.
     */
    int getArrowCountInEntity();

    /**
     * Sets the number of arrows embedded in the entity.
     *
     * @param count the new arrow count.
     */
    void setArrowCountInEntity(int count);

    /**
     * Dismounts the specified entity from this entity.
     *
     * @param entity the entity to dismount.
     */
    void dismountEntity(IEntity entity);

    /**
     * Sets the AI move speed for the entity.
     *
     * @param speed the speed value.
     */
    void setAIMoveSpeed(float speed);

    /**
     * Returns the AI move speed of the entity.
     *
     * @return the speed.
     */
    float getAIMoveSpeed();

    /**
     * Sets the absorption (extra health) amount.
     *
     * @param amount the absorption amount.
     */
    void setAbsorptionAmount(float amount);

    /**
     * Returns the absorption (extra health) amount.
     *
     * @return the absorption amount.
     */
    float getAbsorptionAmount();

    /**
     * Sets the current hurt time (red flash duration) of the entity.
     *
     * @param time the hurt time in ticks.
     */
    void setHurtTime(int time);

    /**
     * Applies knockback to the entity, pushing it away from the source entity.
     * Uses Minecraft's knockback mechanics which respects the entity's knockback resistance.
     * A strength of 1.0 is equivalent to a standard melee hit.
     *
     * @param strength the knockback strength multiplier.
     * @param source   the source entity to knock back away from.
     */
    void applyKnockback(float strength, IEntity source);

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
    void applyKnockback(float strength, double dirX, double dirZ);

    /**
     * Forcefully applies knockback to the entity, pushing it away from the source entity.
     * This ignores the entity's knockback resistance attribute.
     * A strength of 1.0 is equivalent to a standard melee hit.
     *
     * @param strength the knockback strength multiplier.
     * @param source   the source entity to knock back away from.
     */
    void forceKnockback(float strength, IEntity source);

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
    void forceKnockback(float strength, double dirX, double dirZ);
}
