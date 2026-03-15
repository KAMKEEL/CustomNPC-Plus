/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.ability.type
 */

/**
 * API interface for Counter abilities.
 * Absorbs incoming damage and counter-attacks the attacker.
  * @javaFqn noppes.npcs.api.ability.type.IAbilityCounter
*/
export interface IAbilityCounter extends import('./IAbilityDefend').IAbilityDefend {
    /** @return Counter-attack type ordinal (determines how counter damage is calculated). */
    getCounterType(): import('./int').int;
    /** @param type Counter-attack type ordinal. */
    setCounterType(type: import('./int').int): import('./void').void;
    /** @return Counter-attack value (meaning depends on counter type, e.g. flat damage or multiplier). */
    getCounterValue(): import('./float').float;
    /** @param value Counter-attack value (flat damage or multiplier). */
    setCounterValue(value: import('./float').float): import('./void').void;
    /** @return Animation ID played when the counter-attack triggers, or -1 for none. */
    getCounterAnimationId(): import('./int').int;
    /** @param animationId Animation ID for the counter-attack, or -1 for none. */
    setCounterAnimationId(animationId: import('./int').int): import('./void').void;
}
