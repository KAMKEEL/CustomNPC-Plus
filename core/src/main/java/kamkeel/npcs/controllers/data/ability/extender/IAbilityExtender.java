package kamkeel.npcs.controllers.data.ability.extender;

import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.conditions.AbilityCondition;
import kamkeel.npcs.controllers.data.ability.enums.AbilityPhase;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.INbt;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.IWorld;

/**
 * General-purpose extension point for abilities. Multiple extenders can be registered.
 * Covers lifecycle hooks (start/tick/complete) and damage handling.
 * <p>
 * All methods have no-op defaults so extenders only override what they need.
 */
public interface IAbilityExtender {

    /**
     * Called before an ability starts executing.
     * Return false to cancel the ability (e.g., insufficient resources).
     *
     * @param ability The ability about to start
     * @param caster  The IEntity executing the ability (NPC or Player)
     * @param target  The target IEntity (may be null for player self-cast)
     * @return true to allow, false to cancel
     */
    default boolean onAbilityStart(Ability ability, IEntityLivingBase caster, IEntityLivingBase target) {
        return true;
    }

    /**
     * Called each tick during ability execution.
     * Return false to interrupt the ability (e.g., resource depleted).
     *
     * @param ability The executing ability
     * @param caster  The IEntity executing the ability
     * @param target  The target IEntity (may be null)
     * @param phase   The current ability phase (WINDUP, ACTIVE, DAZED, BURST_DELAY)
     * @param tick    The current tick within the phase
     * @return true to continue, false to interrupt
     */
    default boolean onAbilityTick(Ability ability, IEntityLivingBase caster, IEntityLivingBase target,
                                  AbilityPhase phase, int tick) {
        return true;
    }

    /**
     * Called when an ability completes (normally or via interruption).
     *
     * @param ability     The completed ability
     * @param caster      The IEntity that was executing the ability
     * @param target      The target IEntity (may be null)
     * @param interrupted true if the ability was interrupted, false if it completed normally
     */
    default void onAbilityComplete(Ability ability, IEntityLivingBase caster, IEntityLivingBase target,
                                   boolean interrupted) {
    }

    /**
     * Called when an ability deals damage, AFTER script events but BEFORE default attackIEntityFrom.
     * Return true to handle the damage (skips default damage application).
     * <p>
     * Chain of responsibility: extenders are called in registration order.
     * The first extender returning true claims the damage; remaining extenders are skipped.
     *
     * @param ability       The ability dealing damage
     * @param caster        The IEntity executing the ability
     * @param target        The IEntity being hit
     * @param damage        The damage amount (after script event modifications)
     * @param knockback     The horizontal knockback
     * @param knockbackUp   The vertical knockback
     * @param knockbackDirX The X component of knockback direction
     * @param knockbackDirZ The Z component of knockback direction
     * @param damageMultiplier Accumulated proportional damage multiplier (0.0-1.0).
     *                        1.0 means full damage (no reduction). Extenders that recalculate
     *                        damage from stats should multiply by this value.
     * @return true if damage was handled (skip default), false to pass to next extender or default
     */
    default boolean onAbilityDamage(Ability ability, IEntityLivingBase caster, IEntityLivingBase target,
                                    float damage, float knockback, float knockbackUp,
                                    double knockbackDirX, double knockbackDirZ,
                                    float damageMultiplier) {
        return false;
    }

    /**
     * Called to modify outgoing projectile damage before it hits a barrier or is used in other contexts.
     * Unlike onAbilityDamage (chain of responsibility), this is cumulative — all extenders apply their
     * modifications in sequence, each receiving the previous extender's output.
     *
     * @param ability    The source ability of the projectile
     * @param caster     The IEntity that fired the projectile
     * @param baseDamage The base damage from the ability IConfiguration
     * @return The modified damage value
     */
    default float modifyProjectileDamage(Ability ability, IEntityLivingBase caster, float baseDamage) {
        return baseDamage;
    }

    /**
     * Called when a barrier IEntity is about to be created, allowing extenders to modify its max health.
     * Cumulative: all extenders apply in sequence, each receiving the previous output.
     *
     * @param ability    The barrier ability being executed
     * @param caster     The IEntity creating the barrier
     * @param baseHealth The base max health from EnergyBarrierData.maxHealth
     * @return The modified max health value
     */
    default float modifyBarrierHealth(Ability ability, IEntityLivingBase caster, float baseHealth) {
        return baseHealth;
    }

    /**
     * Called when an ability is about to heal an IEntity.
     * Return true to handle the healing (skip default IEntity.heal()).
     * Chain of responsibility: first returning true wins.
     *
     * @param ability    The ability performing the heal
     * @param caster     The IEntity casting the ability
     * @param target     The IEntity being healed
     * @param healAmount The heal amount calculated by the ability
     * @return true if healing was handled (skip default), false to pass to next extender or default
     */
    default boolean onAbilityHeal(Ability ability, IEntityLivingBase caster, IEntityLivingBase target, float healAmount) {
        return false;
    }

    default Boolean onCheckCondition(AbilityCondition condition, IEntityLivingBase caster, IEntityLivingBase target) {
        return null;
    }

    default Boolean onCheckConditionForPlayer(AbilityCondition condition, IEntityLivingBase player) {
        return null;
    }
}
