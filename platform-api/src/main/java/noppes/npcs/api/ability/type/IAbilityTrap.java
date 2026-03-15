package noppes.npcs.api.ability.type;

/**
 * API interface for Trap abilities.
 * Proximity-triggered traps placed around the caster.
 */
public interface IAbilityTrap extends IAbilityZone {

    /** @return Radius in blocks at which entities trigger the trap. */
    float getTriggerRadius();

    /** @param radius Trigger detection radius in blocks. */
    void setTriggerRadius(float radius);

    /** @return Time in ticks before the trap becomes active after placement. */
    int getArmTime();

    /** @param ticks Arming time in ticks. */
    void setArmTime(int ticks);

    /** @return Maximum number of times the trap can trigger before expiring. */
    int getMaxTriggers();

    /** @param max Maximum trigger count. */
    void setMaxTriggers(int max);

    /** @return Cooldown in ticks between consecutive triggers. */
    int getTriggerCooldown();

    /** @param cooldown Trigger cooldown in ticks. */
    void setTriggerCooldown(int cooldown);

    /** @return Damage dealt when the trap triggers. */
    float getDamage();

    /** @param damage Trigger damage amount. */
    void setDamage(float damage);

    /** @return Radius of the damage area when triggered, in blocks. */
    float getDamageRadius();

    /** @param radius Damage area radius in blocks. */
    void setDamageRadius(float radius);

    /** @return Knockback strength applied to entities caught in the trigger. */
    float getKnockback();

    /** @param knockback Knockback strength. */
    void setKnockback(float knockback);

    /** @return Whether the trap is visible to entities. */
    boolean isVisible();

    /** @param visible Whether the trap is visible. */
    void setVisible(boolean visible);
}
