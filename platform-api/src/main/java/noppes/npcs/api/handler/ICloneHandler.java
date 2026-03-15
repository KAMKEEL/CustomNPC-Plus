package noppes.npcs.api.handler;

import noppes.npcs.api.IPos;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntity;

/**
 * Handles storing and spawning cloned entities.
 * Clones are organized by numbered tabs (0-based) or named folders.
 */
public interface ICloneHandler {

    /**
     * Spawns a clone at the given coordinates.
     *
     * @param x                the x coordinate.
     * @param y                the y coordinate.
     * @param z                the z coordinate.
     * @param tab              the tab index.
     * @param name             the clone name.
     * @param world            the world to spawn in.
     * @param ignoreProtection whether to bypass spawn protection.
     * @return the spawned entity, or null on failure.
     */
    IEntity spawn(double x, double y, double z, int tab, String name, IWorld world, boolean ignoreProtection);

    /**
     * Spawns a clone at the given position.
     *
     * @param pos              the position.
     * @param tab              the tab index.
     * @param name             the clone name.
     * @param world            the world to spawn in.
     * @param ignoreProtection whether to bypass spawn protection.
     * @return the spawned entity, or null on failure.
     */
    IEntity spawn(IPos pos, int tab, String name, IWorld world, boolean ignoreProtection);

    /**
     * Spawns a clone at the given coordinates with default protection rules.
     *
     * @param x     the x coordinate.
     * @param y     the y coordinate.
     * @param z     the z coordinate.
     * @param tab   the tab index.
     * @param name  the clone name.
     * @param world the world to spawn in.
     * @return the spawned entity, or null on failure.
     */
    IEntity spawn(double x, double y, double z, int tab, String name, IWorld world);

    /**
     * Spawns a clone at the given position with default protection rules.
     *
     * @param pos   the position.
     * @param tab   the tab index.
     * @param name  the clone name.
     * @param world the world to spawn in.
     * @return the spawned entity, or null on failure.
     */
    IEntity spawn(IPos pos, int tab, String name, IWorld world);

    /**
     * Returns all clones stored in the specified tab.
     *
     * @param tab   the tab index.
     * @param world the world context for entity creation.
     * @return an array of entities in the tab.
     */
    IEntity[] getTab(int tab, IWorld world);

    /**
     * Returns a specific clone from a tab by name.
     *
     * @param tab   the tab index.
     * @param name  the clone name.
     * @param world the world context for entity creation.
     * @return the entity, or null if not found.
     */
    IEntity get(int tab, String name, IWorld world);

    /**
     * Checks whether a clone with the given name exists in the tab.
     *
     * @param tab  the tab index.
     * @param name the clone name.
     * @return true if the clone exists; false otherwise.
     */
    boolean has(int tab, String name);

    /**
     * Stores an entity as a clone in the given tab with the specified name.
     *
     * @param tab    the tab index.
     * @param name   the clone name.
     * @param entity the entity to store.
     */
    void set(int tab, String name, IEntity entity);

    /**
     * Removes a clone from the given tab.
     *
     * @param tab  the tab index.
     * @param name the clone name.
     */
    void remove(int tab, String name);

    // --- Custom Folder Methods ---

    /**
     * Returns all custom folder names.
     *
     * @return an array of folder names.
     */
    String[] getFolders();

    /**
     * Checks whether a custom folder exists.
     *
     * @param folderName the folder name.
     * @return true if the folder exists; false otherwise.
     */
    boolean hasFolder(String folderName);

    /**
     * Spawns a clone from a custom folder at the given coordinates.
     *
     * @param x                the x coordinate.
     * @param y                the y coordinate.
     * @param z                the z coordinate.
     * @param folderName       the folder name.
     * @param name             the clone name.
     * @param world            the world to spawn in.
     * @param ignoreProtection whether to bypass spawn protection.
     * @return the spawned entity, or null on failure.
     */
    IEntity spawn(double x, double y, double z, String folderName, String name, IWorld world, boolean ignoreProtection);

    /**
     * Spawns a clone from a custom folder at the given position.
     *
     * @param pos              the position.
     * @param folderName       the folder name.
     * @param name             the clone name.
     * @param world            the world to spawn in.
     * @param ignoreProtection whether to bypass spawn protection.
     * @return the spawned entity, or null on failure.
     */
    IEntity spawn(IPos pos, String folderName, String name, IWorld world, boolean ignoreProtection);

    /**
     * Spawns a clone from a custom folder with default protection rules.
     *
     * @param x          the x coordinate.
     * @param y          the y coordinate.
     * @param z          the z coordinate.
     * @param folderName the folder name.
     * @param name       the clone name.
     * @param world      the world to spawn in.
     * @return the spawned entity, or null on failure.
     */
    IEntity spawn(double x, double y, double z, String folderName, String name, IWorld world);

    /**
     * Spawns a clone from a custom folder at the given position with default protection rules.
     *
     * @param pos        the position.
     * @param folderName the folder name.
     * @param name       the clone name.
     * @param world      the world to spawn in.
     * @return the spawned entity, or null on failure.
     */
    IEntity spawn(IPos pos, String folderName, String name, IWorld world);

    /**
     * Returns all clones stored in the specified custom folder.
     *
     * @param folderName the folder name.
     * @param world      the world context for entity creation.
     * @return an array of entities in the folder.
     */
    IEntity[] getFolder(String folderName, IWorld world);

    /**
     * Returns a specific clone from a custom folder by name.
     *
     * @param folderName the folder name.
     * @param name       the clone name.
     * @param world      the world context for entity creation.
     * @return the entity, or null if not found.
     */
    IEntity get(String folderName, String name, IWorld world);

    /**
     * Checks whether a clone exists in a custom folder.
     *
     * @param folderName the folder name.
     * @param name       the clone name.
     * @return true if the clone exists; false otherwise.
     */
    boolean has(String folderName, String name);

    /**
     * Stores an entity as a clone in a custom folder.
     *
     * @param folderName the folder name.
     * @param name       the clone name.
     * @param entity     the entity to store.
     */
    void set(String folderName, String name, IEntity entity);

    /**
     * Removes a clone from a custom folder.
     *
     * @param folderName the folder name.
     * @param name       the clone name.
     */
    void remove(String folderName, String name);
}
