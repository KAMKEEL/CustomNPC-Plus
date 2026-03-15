package noppes.npcs.api.handler;

import noppes.npcs.api.handler.data.INaturalSpawn;

/**
 * Handles natural spawn configurations for custom NPCs.
 */
public interface INaturalSpawnsHandler {

    /**
     * Saves all spawn configurations to disk.
     */
    void save();

    /**
     * Returns all registered natural spawn configurations.
     *
     * @return an array of spawn configurations.
     */
    INaturalSpawn[] getSpawns();

    /**
     * Returns spawn configurations that include the specified biome.
     *
     * @param biome the biome name.
     * @return an array of matching spawn configurations.
     */
    INaturalSpawn[] getSpawns(String biome);

    /**
     * Registers a new natural spawn configuration.
     *
     * @param spawn the spawn to add.
     */
    void addSpawn(INaturalSpawn spawn);

    /**
     * Removes a natural spawn configuration.
     *
     * @param spawn the spawn to remove.
     */
    void removeSpawn(INaturalSpawn spawn);

    /**
     * Creates a new empty natural spawn configuration.
     *
     * @return the created spawn.
     */
    INaturalSpawn createSpawn();
}
