package noppes.npcs.api.handler;

import noppes.npcs.api.handler.data.IMagic;
import noppes.npcs.api.handler.data.IMagicCycle;

/**
 * Provides methods to access and modify magic and magic cycles.
 * Implementations of this interface are responsible for managing the lifecycle,
 * storage, and associations between individual magics and cycles.
 */
public interface IMagicHandler {

    /**
     * Retrieves a magic instance by its unique ID.
     *
     * @param magicId the unique identifier of the magic
     * @return the magic corresponding to the given ID, or null if not found
     */
    IMagic getMagic(int magicId);

    /**
     * Retrieves a magic cycle (or category) by its unique ID.
     *
     * @param cycleID the unique identifier of the magic cycle
     * @return the magic cycle corresponding to the given ID, or null if not found
     */
    IMagicCycle getCycle(int cycleID);

    /**
     * Adds a magic to a specific cycle with ordering details.
     * The provided index and priority help determine the magic's position in the cycle.
     *
     * @param magicId  the unique identifier of the magic to add
     * @param cycleId  the unique identifier of the cycle to add the magic to
     * @param index    the index position within the cycle
     * @param priority the priority value within the cycle
     */
    void addMagicToCycle(int magicId, int cycleId, int index, int priority);

    /**
     * Removes a magic from a specified cycle.
     *
     * @param magicId the unique identifier of the magic to remove
     * @param cycleId the unique identifier of the cycle from which the magic is removed
     */
    void removeMagicFromCycle(int magicId, int cycleId);
}
