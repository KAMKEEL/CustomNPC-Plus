package kamkeel.npcs.controllers.data.ability;

import net.minecraft.entity.EntityLivingBase;

/**
 * External damage handler for abilities.
 * When registered, called AFTER script events but BEFORE default attackEntityFrom.
 * If handleDamage returns true, default damage is skipped.
 */
public interface IAbilityDamageHandler {
    /**
     * Handle damage from an ability.
     *
     * @param ability        The ability dealing damage
     * @param caster         The entity executing the ability
     * @param target         The entity being hit
     * @param damage         The damage amount (after script event modifications)
     * @param knockback      The horizontal knockback
     * @param knockbackUp    The vertical knockback
     * @param knockbackDirX  The X component of knockback direction
     * @param knockbackDirZ  The Z component of knockback direction
     * @return true if damage was handled (skip default), false to use default damage
     */
    boolean handleDamage(Ability ability, EntityLivingBase caster, EntityLivingBase target,
                         float damage, float knockback, float knockbackUp,
                         double knockbackDirX, double knockbackDirZ);
}
