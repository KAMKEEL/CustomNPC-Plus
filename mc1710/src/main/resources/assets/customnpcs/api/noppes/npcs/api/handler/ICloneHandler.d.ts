/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler
 */

/**
 * Handles storing and spawning cloned entities.
 * Clones are organized by numbered tabs (0-based) or named folders.
  * @javaFqn noppes.npcs.api.handler.ICloneHandler
*/
export interface ICloneHandler {
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
    spawn(x: import('./double').double, y: import('./double').double, z: import('./double').double, tab: import('./int').int, name: String, world: import('../IWorld').IWorld, ignoreProtection: import('./boolean').boolean): import('../entity/IEntity').IEntity;
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
    spawn(pos: import('../IPos').IPos, tab: import('./int').int, name: String, world: import('../IWorld').IWorld, ignoreProtection: import('./boolean').boolean): import('../entity/IEntity').IEntity;
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
    spawn(x: import('./double').double, y: import('./double').double, z: import('./double').double, tab: import('./int').int, name: String, world: import('../IWorld').IWorld): import('../entity/IEntity').IEntity;
    /**
     * Spawns a clone at the given position with default protection rules.
     *
     * @param pos   the position.
     * @param tab   the tab index.
     * @param name  the clone name.
     * @param world the world to spawn in.
     * @return the spawned entity, or null on failure.
     */
    spawn(pos: import('../IPos').IPos, tab: import('./int').int, name: String, world: import('../IWorld').IWorld): import('../entity/IEntity').IEntity;
    /**
     * Returns all clones stored in the specified tab.
     *
     * @param tab   the tab index.
     * @param world the world context for entity creation.
     * @return an array of entities in the tab.
     */
    getTab(tab: import('./int').int, world: import('../IWorld').IWorld): import('../entity/IEntity').IEntity[];
    /**
     * Returns a specific clone from a tab by name.
     *
     * @param tab   the tab index.
     * @param name  the clone name.
     * @param world the world context for entity creation.
     * @return the entity, or null if not found.
     */
    get(tab: import('./int').int, name: String, world: import('../IWorld').IWorld): import('../entity/IEntity').IEntity;
    /**
     * Checks whether a clone with the given name exists in the tab.
     *
     * @param tab  the tab index.
     * @param name the clone name.
     * @return true if the clone exists; false otherwise.
     */
    has(tab: import('./int').int, name: String): import('./boolean').boolean;
    /**
     * Stores an entity as a clone in the given tab with the specified name.
     *
     * @param tab    the tab index.
     * @param name   the clone name.
     * @param entity the entity to store.
     */
    set(tab: import('./int').int, name: String, entity: import('../entity/IEntity').IEntity): import('./void').void;
    /**
     * Removes a clone from the given tab.
     *
     * @param tab  the tab index.
     * @param name the clone name.
     */
    remove(tab: import('./int').int, name: String): import('./void').void;
    /**
     * Returns all custom folder names.
     *
     * @return an array of folder names.
     */
    getFolders(): String[];
    /**
     * Checks whether a custom folder exists.
     *
     * @param folderName the folder name.
     * @return true if the folder exists; false otherwise.
     */
    hasFolder(folderName: String): import('./boolean').boolean;
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
    spawn(x: import('./double').double, y: import('./double').double, z: import('./double').double, folderName: String, name: String, world: import('../IWorld').IWorld, ignoreProtection: import('./boolean').boolean): import('../entity/IEntity').IEntity;
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
    spawn(pos: import('../IPos').IPos, folderName: String, name: String, world: import('../IWorld').IWorld, ignoreProtection: import('./boolean').boolean): import('../entity/IEntity').IEntity;
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
    spawn(x: import('./double').double, y: import('./double').double, z: import('./double').double, folderName: String, name: String, world: import('../IWorld').IWorld): import('../entity/IEntity').IEntity;
    /**
     * Spawns a clone from a custom folder at the given position with default protection rules.
     *
     * @param pos        the position.
     * @param folderName the folder name.
     * @param name       the clone name.
     * @param world      the world to spawn in.
     * @return the spawned entity, or null on failure.
     */
    spawn(pos: import('../IPos').IPos, folderName: String, name: String, world: import('../IWorld').IWorld): import('../entity/IEntity').IEntity;
    /**
     * Returns all clones stored in the specified custom folder.
     *
     * @param folderName the folder name.
     * @param world      the world context for entity creation.
     * @return an array of entities in the folder.
     */
    getFolder(folderName: String, world: import('../IWorld').IWorld): import('../entity/IEntity').IEntity[];
    /**
     * Returns a specific clone from a custom folder by name.
     *
     * @param folderName the folder name.
     * @param name       the clone name.
     * @param world      the world context for entity creation.
     * @return the entity, or null if not found.
     */
    get(folderName: String, name: String, world: import('../IWorld').IWorld): import('../entity/IEntity').IEntity;
    /**
     * Checks whether a clone exists in a custom folder.
     *
     * @param folderName the folder name.
     * @param name       the clone name.
     * @return true if the clone exists; false otherwise.
     */
    has(folderName: String, name: String): import('./boolean').boolean;
    /**
     * Stores an entity as a clone in a custom folder.
     *
     * @param folderName the folder name.
     * @param name       the clone name.
     * @param entity     the entity to store.
     */
    set(folderName: String, name: String, entity: import('../entity/IEntity').IEntity): import('./void').void;
    /**
     * Removes a clone from a custom folder.
     *
     * @param folderName the folder name.
     * @param name       the clone name.
     */
    remove(folderName: String, name: String): import('./void').void;
}
