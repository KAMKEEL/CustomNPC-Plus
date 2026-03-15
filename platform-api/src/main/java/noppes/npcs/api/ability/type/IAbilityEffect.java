package noppes.npcs.api.ability.type;

import noppes.npcs.api.ability.IAbility;

/**
 * API interface for Effect abilities.
 * Applies healing, potion effects, custom effects, and mod-registered effect actions
 * to self or nearby entities.
 */
public interface IAbilityEffect extends IAbility {

    /** @return Duration of the effect in ticks. */
    int getDurationTicks();
    /** @param ticks Effect duration in ticks. */
    void setDurationTicks(int ticks);

    /** @return Flat heal amount per tick (body HP). */
    float getHealAmount();
    /** @param amount Flat heal amount per tick. */
    void setHealAmount(float amount);

    /** @return Heal amount as a percentage of max health per tick (0.0-100.0). */
    float getHealPercent();
    /** @param percent Heal percentage per tick (0.0-100.0). */
    void setHealPercent(float percent);

    /** @return Whether the caster is also affected by the effect. */
    boolean isIncludeSelf();
    /** @param includeSelf Whether to include the caster. */
    void setIncludeSelf(boolean includeSelf);

    /** @return Area-of-effect radius in blocks. */
    float getRadius();
    /** @param radius AoE radius in blocks. */
    void setRadius(float radius);

    /** @return Whether healing is applied all at once instead of over time. */
    boolean isInstantHeal();
    /** @param instant Whether healing is instant. */
    void setInstantHeal(boolean instant);

    /**
     * @return Target filter type ordinal: 0=ALLIES, 1=ENEMIES, 2=ALL
     */
    int getTargetFilterType();

    /**
     * @param filter Target filter type ordinal: 0=ALLIES, 1=ENEMIES, 2=ALL
     */
    void setTargetFilterType(int filter);

    /**
     * @return Number of configured custom effects (from CustomEffectController)
     */
    int getCustomEffectCount();

    /**
     * @return Number of configured mod-registered effect actions
     */
    int getEffectActionCount();
}
