/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.ability.type
 */

/**
 * API interface for Dodge abilities.
 * Cancels the incoming attack event entirely and plays a random dodge animation.
  * @javaFqn noppes.npcs.api.ability.type.IAbilityDodge
*/
export interface IAbilityDodge extends import('./IAbilityDefend').IAbilityDefend {
    /** @return Animation ID for dodge variant 1, or -1 for none. */
    getDodgeAnimation1Id(): import('./int').int;
    /** @param animationId Animation ID, or -1 for none. */
    setDodgeAnimation1Id(animationId: import('./int').int): import('./void').void;
    /** @return Animation ID for dodge variant 2, or -1 for none. */
    getDodgeAnimation2Id(): import('./int').int;
    /** @param animationId Animation ID, or -1 for none. */
    setDodgeAnimation2Id(animationId: import('./int').int): import('./void').void;
    /** @return Animation ID for dodge variant 3, or -1 for none. */
    getDodgeAnimation3Id(): import('./int').int;
    /** @param animationId Animation ID, or -1 for none. */
    setDodgeAnimation3Id(animationId: import('./int').int): import('./void').void;
}
