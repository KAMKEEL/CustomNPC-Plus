/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.ability.type
 */

/**
 * API interface for defensive abilities (Guard, Counter, Dodge).
  * @javaFqn noppes.npcs.api.ability.type.IAbilityDefend
*/
export interface IAbilityDefend extends import('../IAbility').IAbility {
    /** @return Maximum duration of the defensive stance in ticks. */
    getDurationTicks(): import('./int').int;
    /** @param ticks Maximum duration in ticks. */
    setDurationTicks(ticks: import('./int').int): import('./void').void;
    /** @return Maximum number of hits that can be absorbed before the defense breaks. */
    getMaxHitAmount(): import('./int').int;
    /** @param amount Maximum hit count before breaking. */
    setMaxHitAmount(amount: import('./int').int): import('./void').void;
    /** @return Whether the entity is currently in a defensive stance. */
    isDefending(): import('./boolean').boolean;
}
