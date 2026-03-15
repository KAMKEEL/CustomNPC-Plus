/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.block
 */

/**
 * @javaFqn noppes.npcs.api.block.IBlockScripted
 */
export interface IBlockScripted extends import('../IBlock').IBlock {
    /**
     * @param item The item to be set as model
     */
    setModel(item: import('../item/IItemStack').IItemStack): import('./void').void;
    setModel(name: String): import('./void').void;
    getModel(): import('../item/IItemStack').IItemStack;
    getTimers(): import('../ITimers').ITimers;
    /**
     * @param strength Sets the strength of the redstone signal (0-15)
     */
    setRedstonePower(strength: import('./int').int): import('./void').void;
    /**
     * @return Returns the current redstone power (0-15) this block is giving off
     */
    getRedstonePower(): import('./int').int;
    setIsLadder(enabled: import('./boolean').boolean): import('./void').void;
    getIsLadder(): import('./boolean').boolean;
    /**
     * @param value Sets the light value (0-15)
     */
    setLight(value: import('./int').int): import('./void').void;
    /**
     * @return Returns the light value (0-15)
     */
    getLight(): import('./int').int;
    /**
     * @param x Scale x (0-10)
     * @param y Scale y (0-10)
     * @param z Scale z (0-10)
     */
    setScale(x: import('./float').float, y: import('./float').float, z: import('./float').float): import('./void').void;
    getScaleX(): import('./float').float;
    getScaleY(): import('./float').float;
    getScaleZ(): import('./float').float;
    /**
     * @param x Rotation x (0-359)
     * @param y Rotation y (0-359)
     * @param z Rotation z (0-359)
     */
    setRotation(x: import('./int').int, y: import('./int').int, z: import('./int').int): import('./void').void;
    getRotationX(): import('./int').int;
    getRotationY(): import('./int').int;
    getRotationZ(): import('./int').int;
    /**
     * On servers the enable-command-block option in the server.properties needs to be set to true <br>
     * Use /gamerule commandBlockOutput false/true to turn off/on command block feedback <br>
     * Setting NpcUseOpCommands to true in the CustomNPCs.cfg should allow the npc to run op commands, be warned this could be a major security risk, use at own risk <br>
     * For permission plugins the commands are run under uuid:c9c843f8-4cb1-4c82-aa61-e264291b7bd6 and name:[customnpcs]
     *
     * @param command The command to be executed
     */
    executeCommand(command: String): import('./void').void;
    /**
     * TYPO VERSION
     * @return true if entities can pass through this block
     */
    getIsPassible(): import('./boolean').boolean;
    /**
     * TYPO VERSION
     * @param bo whether entities can pass through this block
     */
    setIsPassible(bo: import('./boolean').boolean): import('./void').void;
    getIsPassable(): import('./boolean').boolean;
    setIsPassable(bo: import('./boolean').boolean): import('./void').void;
    /**
     * @return Harvesting hardness (-1 makes it unharvestable)
     */
    getHardness(): import('./float').float;
    setHardness(hardness: import('./float').float): import('./void').void;
    /**
     * @return Explosion resistance (-1 makes it unexplodable)
     */
    getResistance(): import('./float').float;
    setResistance(resistance: import('./float').float): import('./void').void;
    getTextPlane(): import('./ITextPlane').ITextPlane;
    getTextPlane2(): import('./ITextPlane').ITextPlane;
    getTextPlane3(): import('./ITextPlane').ITextPlane;
    getTextPlane4(): import('./ITextPlane').ITextPlane;
    getTextPlane5(): import('./ITextPlane').ITextPlane;
    getTextPlane6(): import('./ITextPlane').ITextPlane;
    setStoredData(key: String, value: Object): import('./void').void;
    getStoredData(key: String): Object;
    removeStoredData(key: String): import('./void').void;
    hasStoredData(key: String): import('./boolean').boolean;
    clearStoredData(): import('./void').void;
    getStoredDataKeys(): String[];
    removeTempData(key: String): import('./void').void;
    setTempData(key: String, value: Object): import('./void').void;
    hasTempData(key: String): import('./boolean').boolean;
    getTempData(key: String): Object;
    clearTempData(): import('./void').void;
    getTempDataKeys(): String[];
}
