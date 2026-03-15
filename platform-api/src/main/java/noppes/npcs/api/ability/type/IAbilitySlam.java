package noppes.npcs.api.ability.type;

import noppes.npcs.api.ability.IAbility;

/**
 * API interface for Slam abilities.
 * Ground-pound AOE attack with leap.
 */
public interface IAbilitySlam extends IAbility {

    /** @return Damage dealt on ground impact. */
    float getDamage();

    /** @param damage Impact damage amount. */
    void setDamage(float damage);

    /** @return Radius of the impact area in blocks. */
    float getRadius();

    /** @param radius Impact radius in blocks. */
    void setRadius(float radius);

    /** @return Knockback strength applied to entities in the impact area. */
    float getKnockbackStrength();

    /** @param knockback Knockback strength. */
    void setKnockbackStrength(float knockback);

    /** @return Horizontal speed of the leap toward the target. */
    float getLeapSpeed();

    /** @param speed Leap speed. */
    void setLeapSpeed(float speed);

    /** @return Vertical height of the leap in blocks. */
    float getLeapHeight();

    /** @param height Leap height in blocks. */
    void setLeapHeight(float height);
}
