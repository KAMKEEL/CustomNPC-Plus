package noppes.npcs.api.event;

import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.ability.IChainedAbility;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IPlayer;

/**
 * Events related to chained ability execution lifecycle.
 * These fire at chain-level transitions, not per-ability.
 * Unified for both NPCs and Players.
 */
public interface IChainEvent extends ICustomNPCsEvent {

    /**
     * Get the entity executing the chain (NPC or Player).
     * @return the entity executing the chain
     */
    IEntityLivingBase getEntity();

    /**
     * Get the player executing the chain, or null if the caster is an NPC.
     * @return the player executing the chain, or null if NPC
     */
    IPlayer getPlayer();

    /**
     * Get the NPC executing the chain, or null if the caster is a player.
     * @return the NPC executing the chain, or null if player
     */
    ICustomNpc getNpc();

    /**
     * Whether the entity executing the chain is an NPC.
     * @return true if the executor is an NPC
     */
    boolean isNPC();

    /**
     * Get the chained ability being executed.
     * @return the chained ability being executed
     */
    IChainedAbility getChain();

    /**
     * Get the target of the chain, or null if no target.
     * @return the current target entity
     */
    IEntityLivingBase getTarget();

    /**
     * Get the current entry index within the chain (0-based).
     * @return the current entry index in the chain
     */
    int getEntryIndex();

    /**
     * Fired when a chained ability sequence starts executing.
     */
    interface StartEvent extends IChainEvent {}

    /**
     * Fired when the chain advances to the next entry in the sequence.
     */
    interface NextEvent extends IChainEvent {}

    /**
     * Fired when a chained ability sequence completes all entries.
     */
    interface CompleteEvent extends IChainEvent {}

    /**
     * Fired when a chained ability is interrupted by damage.
     */
    interface InterruptEvent extends IChainEvent {
        /**
         * Get the damage source that caused the interruption.
         * @return the damage source used for this hit
         */
        IDamageSource getDamageSource();

        /**
         * Get the amount of damage that caused the interruption.
         * @return the damage amount
         */
        float getDamage();
    }
}
