/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.event
 */

/**
 * Events related to chained ability execution lifecycle.
 * These fire at chain-level transitions, not per-ability.
 * Unified for both NPCs and Players.
  * @javaFqn noppes.npcs.api.event.IChainEvent
*/
export interface IChainEvent extends import('./ICustomNPCsEvent').ICustomNPCsEvent {
    /**
     * Get the entity executing the chain (NPC or Player).
     * @return the entity executing the chain
     */
    getEntity(): import('../entity/IEntityLivingBase').IEntityLivingBase;
    /**
     * Get the player executing the chain, or null if the caster is an NPC.
     * @return the player executing the chain, or null if NPC
     */
    getPlayer(): import('../entity/IPlayer').IPlayer;
    /**
     * Get the NPC executing the chain, or null if the caster is a player.
     * @return the NPC executing the chain, or null if player
     */
    getNpc(): import('../entity/ICustomNpc').ICustomNpc;
    /**
     * Whether the entity executing the chain is an NPC.
     * @return true if the executor is an NPC
     */
    isNPC(): import('./boolean').boolean;
    /**
     * Get the chained ability being executed.
     * @return the chained ability being executed
     */
    getChain(): import('../ability/IChainedAbility').IChainedAbility;
    /**
     * Get the target of the chain, or null if no target.
     * @return the current target entity
     */
    getTarget(): import('../entity/IEntityLivingBase').IEntityLivingBase;
    /**
     * Get the current entry index within the chain (0-based).
     * @return the current entry index in the chain
     */
    getEntryIndex(): import('./int').int;
    readonly player: import('../entity/IPlayer').IPlayer;
    readonly npc: import('../entity/ICustomNpc').ICustomNpc;
}

export namespace IChainEvent {
    
    
    
    
    
    
    
    /**
     * Fired when a chained ability sequence starts executing.
          * @javaFqn noppes.npcs.api.event.IChainEvent.StartEvent
*/
    export interface StartEvent extends IChainEvent {
    }
    /**
     * Fired when the chain advances to the next entry in the sequence.
          * @javaFqn noppes.npcs.api.event.IChainEvent.NextEvent
*/
    export interface NextEvent extends IChainEvent {
    }
    /**
     * Fired when a chained ability sequence completes all entries.
          * @javaFqn noppes.npcs.api.event.IChainEvent.CompleteEvent
*/
    export interface CompleteEvent extends IChainEvent {
    }
    /**
     * Fired when a chained ability is interrupted by damage.
          * @javaFqn noppes.npcs.api.event.IChainEvent.InterruptEvent
*/
    export interface InterruptEvent extends IChainEvent {
        /**
         * Get the damage source that caused the interruption.
         * @return the damage source used for this hit
         */
        getDamageSource(): import('../IDamageSource').IDamageSource;
        /**
         * Get the amount of damage that caused the interruption.
         * @return the damage amount
         */
        getDamage(): import('./float').float;
    }
}
