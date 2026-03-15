package noppes.npcs.api.ability.type;

import noppes.npcs.api.ability.IAbility;

/**
 * API interface for Cutter abilities.
 * Sweeping fan attack in an arc.
 */
public interface IAbilityCutter extends IAbility {

    /** @return Arc angle of the sweep in degrees. */
    float getArcAngle();

    /** @param angle Arc angle in degrees. */
    void setArcAngle(float angle);

    /** @return Range of the sweep attack in blocks. */
    float getRange();

    /** @param range Sweep range in blocks. */
    void setRange(float range);

    /** @return Damage dealt to entities hit by the sweep. */
    float getDamage();

    /** @param damage Sweep damage amount. */
    void setDamage(float damage);

    /** @return Knockback strength applied to entities hit. */
    float getKnockback();

    /** @param knockback Knockback strength. */
    void setKnockback(float knockback);

    /** @return Sweep mode ordinal (0=SWIPE, 1=SPIN). */
    int getSweepMode();

    /** @param mode Sweep mode ordinal (0=SWIPE, 1=SPIN). */
    void setSweepMode(int mode);

    /** @return Speed of the sweep animation. */
    float getSweepSpeed();

    /** @param speed Sweep animation speed. */
    void setSweepSpeed(float speed);

    /** @return Duration of the spin in ticks (only used in SPIN mode). */
    int getSpinDurationTicks();

    /** @param ticks Spin duration in ticks. */
    void setSpinDurationTicks(int ticks);

    /** @return Whether the sweep hits through multiple targets. */
    boolean isPiercing();

    /** @param piercing Whether the sweep pierces through targets. */
    void setPiercing(boolean piercing);

    /** @return Inner radius of the sweep arc in blocks (creates a donut-shaped hitbox). */
    float getInnerRadius();

    /** @param radius Inner radius in blocks. */
    void setInnerRadius(float radius);
}
