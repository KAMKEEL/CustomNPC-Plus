package noppes.npcs.api.ability.type;

import noppes.npcs.api.ability.IAbility;

/**
 * API interface for Shockwave abilities.
 * Pushes targets away from the caster.
 */
public interface IAbilityShockwave extends IAbility {

    /** @return Radius of the push effect in blocks. */
    float getPushRadius();

    /** @param radius Push radius in blocks. */
    void setPushRadius(float radius);

    /** @return Strength of the outward push force. */
    float getPushStrength();

    /** @param strength Push force strength. */
    void setPushStrength(float strength);

    /** @return Damage dealt to entities caught in the shockwave. */
    float getDamage();

    /** @param damage Shockwave damage. */
    void setDamage(float damage);

    /** @return Whether the shockwave hits all entities in radius (true) or only the target (false). */
    boolean isAoe();

    /** @param aoe Whether the shockwave is area-of-effect. */
    void setAoe(boolean aoe);
}
