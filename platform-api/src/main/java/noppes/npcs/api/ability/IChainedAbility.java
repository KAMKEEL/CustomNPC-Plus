package noppes.npcs.api.ability;

/**
 * API interface for chained abilities - ordered sequences of ability references
 * that execute one after another with configurable delays.
 */
public interface IChainedAbility extends IAbilityAction {

    /** @return Whether all entries wind up simultaneously before execution begins. */
    boolean isWindUpAll();

    /** @return The number of ability entries in this chain. */
    int getEntryCount();

    /**
     * @param index Entry index in the chain.
     * @return The ability reference ID at the given index.
     */
    String getEntryReference(int index);

    /**
     * @param index Entry index in the chain.
     * @return The delay in ticks before executing this entry.
     */
    int getEntryDelay(int index);

    /**
     * @param index Entry index in the chain.
     * @return Whether this entry executes inline (overlapping with previous).
     */
    boolean isEntryInline(int index);
}
