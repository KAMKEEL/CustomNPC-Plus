package noppes.npcs.api.ability.type;

import noppes.npcs.api.ability.IAbility;

/**
 * API interface for Heavy Hit abilities.
 * AOE rectangle melee attack in front of the caster.
 */
public interface IAbilityHeavyHit extends IAbility {

    /** @return Damage dealt to entities in the hit area. */
    float getDamage();

    /** @param damage Damage amount. */
    void setDamage(float damage);

    /** @return Knockback strength applied to entities hit. */
    float getKnockback();

    /** @param knockback Knockback strength. */
    void setKnockback(float knockback);

    /** @return Length of the rectangular hit area in blocks (forward from caster). */
    float getHitLength();

    /** @param hitLength Hit area length in blocks. */
    void setHitLength(float hitLength);

    /** @return Width of the rectangular hit area in blocks. */
    float getHitWidth();

    /** @param hitWidth Hit area width in blocks. */
    void setHitWidth(float hitWidth);
}
