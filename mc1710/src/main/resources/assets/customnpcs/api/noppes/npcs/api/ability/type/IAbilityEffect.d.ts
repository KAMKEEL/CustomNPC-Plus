/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.ability.type
 */

/**
 * API interface for Effect abilities.
 * Applies healing, potion effects, custom effects, and mod-registered effect actions
 * to self or nearby entities.
  * @javaFqn noppes.npcs.api.ability.type.IAbilityEffect
*/
export interface IAbilityEffect extends import('../IAbility').IAbility {
    /** @return Duration of the effect in ticks. */
    getDurationTicks(): import('./int').int;
    /** @param ticks Effect duration in ticks. */
    setDurationTicks(ticks: import('./int').int): import('./void').void;
    /** @return Flat heal amount per tick (body HP). */
    getHealAmount(): import('./float').float;
    /** @param amount Flat heal amount per tick. */
    setHealAmount(amount: import('./float').float): import('./void').void;
    /** @return Heal amount as a percentage of max health per tick (0.0-100.0). */
    getHealPercent(): import('./float').float;
    /** @param percent Heal percentage per tick (0.0-100.0). */
    setHealPercent(percent: import('./float').float): import('./void').void;
    /** @return Whether the caster is also affected by the effect. */
    isIncludeSelf(): import('./boolean').boolean;
    /** @param includeSelf Whether to include the caster. */
    setIncludeSelf(includeSelf: import('./boolean').boolean): import('./void').void;
    /** @return Area-of-effect radius in blocks. */
    getRadius(): import('./float').float;
    /** @param radius AoE radius in blocks. */
    setRadius(radius: import('./float').float): import('./void').void;
    /** @return Whether healing is applied all at once instead of over time. */
    isInstantHeal(): import('./boolean').boolean;
    /** @param instant Whether healing is instant. */
    setInstantHeal(instant: import('./boolean').boolean): import('./void').void;
    /**
     * @return Target filter type ordinal: 0=ALLIES, 1=ENEMIES, 2=ALL
     */
    getTargetFilterType(): import('./int').int;
    /**
     * @param filter Target filter type ordinal: 0=ALLIES, 1=ENEMIES, 2=ALL
     */
    setTargetFilterType(filter: import('./int').int): import('./void').void;
    /**
     * @return Number of configured custom effects (from CustomEffectController)
     */
    getCustomEffectCount(): import('./int').int;
    /**
     * @return Number of configured mod-registered effect actions
     */
    getEffectActionCount(): import('./int').int;
}
