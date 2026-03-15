package noppes.npcs.api.ability.type;

import noppes.npcs.api.ability.IAbility;

/**
 * API interface for Charge abilities.
 * Rush forward attack.
 */
public interface IAbilityCharge extends IAbility {

    /** @return Movement speed during the charge in blocks per tick. */
    float getChargeSpeed();

    /** @param speed Movement speed in blocks per tick. */
    void setChargeSpeed(float speed);

    /** @return Damage dealt to entities hit during the charge. */
    float getDamage();

    /** @param damage Damage on charge impact. */
    void setDamage(float damage);

    /** @return Knockback strength applied to entities hit during the charge. */
    float getKnockback();

    /** @param knockback Knockback strength on impact. */
    void setKnockback(float knockback);

    /** @return Width of the charge hit area in blocks. */
    float getHitWidth();

    /** @param width Hit area width in blocks. */
    void setHitWidth(float width);
}
