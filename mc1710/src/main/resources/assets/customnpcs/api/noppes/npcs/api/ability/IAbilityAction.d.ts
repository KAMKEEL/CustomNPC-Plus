/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.ability
 */

/**
 * Common API interface for all combat actions (abilities and chained abilities).
 * Both {@link IAbility} and {@link IChainedAbility} extend this interface.
  * @javaFqn noppes.npcs.api.ability.IAbilityAction
*/
export interface IAbilityAction {
    /** @return The unique identifier/name of this action. */
    getName(): String;
    /** @return Whether this action is enabled. */
    isEnabled(): import('./boolean').boolean;
    /** @return Selection weight for random ability selection (higher = more likely). */
    getWeight(): import('./int').int;
    /** @return Cooldown duration in ticks after this action is used. */
    getCooldownTicks(): import('./int').int;
    /** @return Minimum range in blocks at which this action can be used. */
    getMinRange(): import('./float').float;
    /** @return Maximum range in blocks at which this action can be used. */
    getMaxRange(): import('./float').float;
    /** @return Whether this action is a chained ability (sequence) vs an individual ability. */
    isChain(): import('./boolean').boolean;
}
