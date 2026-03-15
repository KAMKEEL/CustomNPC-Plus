/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler
 */

/**
 * Handles natural spawn configurations for custom NPCs.
  * @javaFqn noppes.npcs.api.handler.INaturalSpawnsHandler
*/
export interface INaturalSpawnsHandler {
    /**
     * Saves all spawn configurations to disk.
     */
    save(): import('./void').void;
    /**
     * Returns all registered natural spawn configurations.
     *
     * @return an array of spawn configurations.
     */
    getSpawns(): import('./data/INaturalSpawn').INaturalSpawn[];
    /**
     * Returns spawn configurations that include the specified biome.
     *
     * @param biome the biome name.
     * @return an array of matching spawn configurations.
     */
    getSpawns(biome: String): import('./data/INaturalSpawn').INaturalSpawn[];
    /**
     * Registers a new natural spawn configuration.
     *
     * @param spawn the spawn to add.
     */
    addSpawn(spawn: import('./data/INaturalSpawn').INaturalSpawn): import('./void').void;
    /**
     * Removes a natural spawn configuration.
     *
     * @param spawn the spawn to remove.
     */
    removeSpawn(spawn: import('./data/INaturalSpawn').INaturalSpawn): import('./void').void;
    /**
     * Creates a new empty natural spawn configuration.
     *
     * @return the created spawn.
     */
    createSpawn(): import('./data/INaturalSpawn').INaturalSpawn;
}
