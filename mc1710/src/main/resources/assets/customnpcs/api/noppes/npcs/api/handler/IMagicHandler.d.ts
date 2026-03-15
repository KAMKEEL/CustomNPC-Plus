/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler
 */

/**
 * Provides methods to access and modify magic and magic cycles.
 * Implementations of this interface are responsible for managing the lifecycle,
 * storage, and associations between individual magics and cycles.
  * @javaFqn noppes.npcs.api.handler.IMagicHandler
*/
export interface IMagicHandler {
    /**
     * Retrieves a magic instance by its unique ID.
     *
     * @param magicId the unique identifier of the magic
     * @return the magic corresponding to the given ID, or null if not found
     */
    getMagic(magicId: import('./int').int): import('./data/IMagic').IMagic;
    /**
     * Retrieves a magic cycle (or category) by its unique ID.
     *
     * @param cycleID the unique identifier of the magic cycle
     * @return the magic cycle corresponding to the given ID, or null if not found
     */
    getCycle(cycleID: import('./int').int): import('./data/IMagicCycle').IMagicCycle;
    /**
     * Adds a magic to a specific cycle with ordering details.
     * The provided index and priority help determine the magic's position in the cycle.
     *
     * @param magicId  the unique identifier of the magic to add
     * @param cycleId  the unique identifier of the cycle to add the magic to
     * @param index    the index position within the cycle
     * @param priority the priority value within the cycle
     */
    addMagicToCycle(magicId: import('./int').int, cycleId: import('./int').int, index: import('./int').int, priority: import('./int').int): import('./void').void;
    /**
     * Removes a magic from a specified cycle.
     *
     * @param magicId the unique identifier of the magic to remove
     * @param cycleId the unique identifier of the cycle from which the magic is removed
     */
    removeMagicFromCycle(magicId: import('./int').int, cycleId: import('./int').int): import('./void').void;
}
