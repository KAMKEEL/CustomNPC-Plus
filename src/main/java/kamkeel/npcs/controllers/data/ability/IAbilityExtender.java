package kamkeel.npcs.controllers.data.ability;

import net.minecraft.entity.EntityLivingBase;

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
     * @param caster  The entity executing the ability (NPC or Player)
     * @param target  The target entity (may be null for player self-cast)
     * @return true to allow, false to cancel
     */
    default boolean onAbilityStart(Ability ability, EntityLivingBase caster, EntityLivingBase target) {
        return true;
    }

    /**
     * Called each tick during ability execution.
     * Return false to interrupt the ability (e.g., resource depleted).
     *
     * @param ability The executing ability
     * @param caster  The entity executing the ability
     * @param target  The target entity (may be null)
     * @param phase   The current ability phase (WINDUP, ACTIVE, DAZED, BURST_DELAY)
     * @param tick    The current tick within the phase
     * @return true to continue, false to interrupt
     */
    default boolean onAbilityTick(Ability ability, EntityLivingBase caster, EntityLivingBase target,
                                  AbilityPhase phase, int tick) {
        return true;
    }

    /**
     * Called when an ability completes (normally or via interruption).
     *
     * @param ability     The completed ability
     * @param caster      The entity that was executing the ability
     * @param target      The target entity (may be null)
     * @param interrupted true if the ability was interrupted, false if it completed normally
     */
    default void onAbilityComplete(Ability ability, EntityLivingBase caster, EntityLivingBase target,
                                   boolean interrupted) {
    }

    /**
     * Called when an ability deals damage, AFTER script events but BEFORE default attackEntityFrom.
     * Return true to handle the damage (skips default damage application).
     * <p>
     * Chain of responsibility: extenders are called in registration order.
     * The first extender returning true claims the damage; remaining extenders are skipped.
     *
     * @param ability       The ability dealing damage
     * @param caster        The entity executing the ability
     * @param target        The entity being hit
     * @param damage        The damage amount (after script event modifications)
     * @param knockback     The horizontal knockback
     * @param knockbackUp   The vertical knockback
     * @param knockbackDirX The X component of knockback direction
     * @param knockbackDirZ The Z component of knockback direction
     * @return true if damage was handled (skip default), false to pass to next extender or default
     */
    default boolean onAbilityDamage(Ability ability, EntityLivingBase caster, EntityLivingBase target,
                                    float damage, float knockback, float knockbackUp,
                                    double knockbackDirX, double knockbackDirZ) {
        return false;
    }

    /**
     * Called to modify outgoing projectile damage before it hits a barrier or is used in other contexts.
     * Unlike onAbilityDamage (chain of responsibility), this is cumulative — all extenders apply their
     * modifications in sequence, each receiving the previous extender's output.
     *
     * @param ability    The source ability of the projectile
     * @param caster     The entity that fired the projectile
     * @param baseDamage The base damage from the ability configuration
     * @return The modified damage value
     */
    default float modifyProjectileDamage(Ability ability, EntityLivingBase caster, float baseDamage) {
        return baseDamage;
    }
}
