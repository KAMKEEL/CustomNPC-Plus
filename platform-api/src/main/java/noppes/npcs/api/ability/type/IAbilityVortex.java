package noppes.npcs.api.ability.type;

import noppes.npcs.api.ability.IAbility;

/**
 * API interface for Vortex abilities.
 * Pulls targets toward the caster.
 */
public interface IAbilityVortex extends IAbility {

    /** @return Radius of the pull effect in blocks. */
    float getPullRadius();

    /** @param radius Pull effect radius in blocks. */
    void setPullRadius(float radius);

    /** @return Strength of the inward pull force. */
    float getPullStrength();

    /** @param strength Pull force strength. */
    void setPullStrength(float strength);

    /** @return Damage dealt on initial vortex hit. */
    float getDamage();

    /** @param damage Initial hit damage. */
    void setDamage(float damage);

    /** @return Knockback strength applied on initial hit. */
    float getKnockback();

    /** @param knockback Knockback strength. */
    void setKnockback(float knockback);

    /** @return Whether the vortex hits all entities in radius (true) or only the target (false). */
    boolean isAoe();

    /** @param aoe Whether the vortex is area-of-effect. */
    void setAoe(boolean aoe);

    /** @return Whether continuous damage is dealt while pulling entities. */
    boolean isDamageOnPull();

    /** @param damage Whether to deal damage during pull. */
    void setDamageOnPull(boolean damage);

    /** @return Damage dealt per tick while pulling entities. */
    float getPullDamage();

    /** @param damage Per-tick pull damage. */
    void setPullDamage(float damage);
}
