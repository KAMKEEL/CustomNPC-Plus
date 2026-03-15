/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.ability
 */

/**
 * API interface for chained abilities - ordered sequences of ability references
 * that execute one after another with configurable delays.
  * @javaFqn noppes.npcs.api.ability.IChainedAbility
*/
export interface IChainedAbility extends import('./IAbilityAction').IAbilityAction {
    /** @return Whether all entries wind up simultaneously before execution begins. */
    isWindUpAll(): import('./boolean').boolean;
    /** @return The number of ability entries in this chain. */
    getEntryCount(): import('./int').int;
    /**
     * @param index Entry index in the chain.
     * @return The ability reference ID at the given index.
     */
    getEntryReference(index: import('./int').int): String;
    /**
     * @param index Entry index in the chain.
     * @return The delay in ticks before executing this entry.
     */
    getEntryDelay(index: import('./int').int): import('./int').int;
    /**
     * @param index Entry index in the chain.
     * @return Whether this entry executes inline (overlapping with previous).
     */
    isEntryInline(index: import('./int').int): import('./boolean').boolean;
}
