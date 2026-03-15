/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api
 */

/**
 * This object stores functions available to all scripting handlers through the "API" keyword.
 *
  * @javaFqn noppes.npcs.api.AbstractNpcAPI
*/
export class AbstractNpcAPI {
    /**
     * @param key Get temp data for this key
     * @return Returns the stored temp data
     */
    getTempData(key: String): Object;
    /**
     * Tempdata gets cleared when the server restarts. All worlds share the same temp data.
     *
     * @param key   The key for the data stored
     * @param value The data stored
     */
    setTempData(key: String, value: Object): import('./void').void;
    /**
     * @param key The key thats going to be tested against the temp data
     * @return Whether or not temp data containes the key
     */
    hasTempData(key: String): import('./boolean').boolean;
    /**
     * @param key The key for the temp data to be removed
     */
    removeTempData(key: String): import('./void').void;
    /**
     * Removes all tempdata
     */
    clearTempData(): import('./void').void;
    getTempDataKeys(): String[];
    /**
     * @param key The key of the data to be returned
     * @return Returns the stored data
     */
    getStoredData(key: String): Object;
    /**
     * Stored data persists through world restart. Unlike tempdata only Strings and Numbers can be saved
     *
     * @param key   The key for the data stored
     * @param value The data stored. This data can be either a Number or a String. Other data is not stored
     */
    setStoredData(key: String, value: Object): import('./void').void;
    /**
     * @param key The key of the data to be checked
     * @return Returns whether or not the stored data contains the key
     */
    hasStoredData(key: String): import('./boolean').boolean;
    /**
     * @param key The key of the data to be removed
     */
    removeStoredData(key: String): import('./void').void;
    /**
     * Remove all stored data
     */
    clearStoredData(): import('./void').void;
    getStoredDataKeys(): String[];
    registerICommand(command: import('./ICommand').ICommand): import('./void').void;
    getICommand(commandName: String, priorityLevel: import('./int').int): import('./ICommand').ICommand;
    addGlobalObject(key: String, obj: Object): import('./void').void;
    removeGlobalObject(key: String): import('./void').void;
    hasGlobalObject(key: String): import('./boolean').boolean;
    getEngineObjects(): Java.java.util.HashMap<String, Object>;
    sizeOfObject(obj: Object): import('./long').long;
    stopServer(): import('./void').void;
    getCurrentPlayerCount(): import('./int').int;
    getMaxPlayers(): import('./int').int;
    kickAllPlayers(): import('./void').void;
    isHardcore(): import('./boolean').boolean;
    getFile(path: String): Java.java.io.File;
    getServerOwner(): String;
    getFactions(): import('./IFactionHandler').IFactionHandler;
    getRecipes(): import('./IRecipeHandler').IRecipeHandler;
    getQuests(): import('./IQuestHandler').IQuestHandler;
    getDialogs(): import('./IDialogHandler').IDialogHandler;
    getClones(): import('./ICloneHandler').ICloneHandler;
    getNaturalSpawns(): import('./INaturalSpawnsHandler').INaturalSpawnsHandler;
    getProfileHandler(): import('./IProfileHandler').IProfileHandler;
    getCustomEffectHandler(): import('./ICustomEffectHandler').ICustomEffectHandler;
    getMagicHandler(): import('./IMagicHandler').IMagicHandler;
    getPartyHandler(): import('./IPartyHandler').IPartyHandler;
    getLocations(): import('./ITransportHandler').ITransportHandler;
    getAnimations(): import('./IAnimationHandler').IAnimationHandler;
    getLinkedItems(): import('./ILinkedItemHandler').ILinkedItemHandler;
    /**
     * Get the script hook handler for registering custom hooks.
     * Addon mods can use this to register hooks that will appear in script editor GUIs.
     *
     * @return The script hook handler
     */
    getScriptHooks(): import('./IScriptHookHandler').IScriptHookHandler;
    getAbilities(): import('./IAbilityHandler').IAbilityHandler;
    getTelegraphs(): import('./ITelegraphHandler').ITelegraphHandler;
    /**
     * Get the auction handler for managing auctions via scripts.
     *
     * @return The auction handler, or null if auctions are disabled
     */
    getAuctions(): import('./IAuctionHandler').IAuctionHandler;
    /**
     * Create a telegraph directly.
     * Convenience method equivalent to getTelegraphs().create(type).
     *
     * @param type Type name: "circle", "ring", "line", "cone", "point"
     * @return A new telegraph configuration
     */
    createTelegraph(type: String): import('./ITelegraph').ITelegraph;
    getAllBiomeNames(): String[];
    createNPC(var1: import('./IWorld').IWorld): import('./entity/ICustomNpc').ICustomNpc;
    /**
     *
     * Spawns a new NPC in the world at the given coordinates and returns an ICustomNpc object of it.
     *
     * @param var1 the world
     * @param var2 X position
     * @param var3 Y position
     * @param var4 Z position
     * @return the spawned NPC
     */
    spawnNPC(var1: import('./IWorld').IWorld, var2: import('./int').int, var3: import('./int').int, var4: import('./int').int): import('./entity/ICustomNpc').ICustomNpc;
    spawnNPC(world: import('./IWorld').IWorld, pos: import('./IPos').IPos): import('./entity/ICustomNpc').ICustomNpc;
    getIEntity(var1: import('../../../net/minecraft/entity/Entity').Entity): import('./entity/IEntity').IEntity;
    getPlayer(username: String): import('./entity/IPlayer').IPlayer;
    getChunkLoadingNPCs(): import('../../../net/minecraft/entity/INpc').INpc[];
    getLoadedEntities(): import('./entity/IEntity').IEntity[];
    getIBlock(world: import('./IWorld').IWorld, x: import('./int').int, y: import('./int').int, z: import('./int').int): import('./IBlock').IBlock;
    getIBlock(world: import('./IWorld').IWorld, pos: import('./IPos').IPos): import('./IBlock').IBlock;
    getITileEntity(world: import('./IWorld').IWorld, pos: import('./IPos').IPos): import('./ITileEntity').ITileEntity;
    getITileEntity(world: import('./IWorld').IWorld, x: import('./int').int, y: import('./int').int, z: import('./int').int): import('./ITileEntity').ITileEntity;
    getITileEntity(tileEntity: import('../../../net/minecraft/tileentity/TileEntity').TileEntity): import('./ITileEntity').ITileEntity;
    getIPos(pos: import('../../../net/minecraft/util/math/BlockPos').BlockPos): import('./IPos').IPos;
    getIPos(x: import('./int').int, y: import('./int').int, z: import('./int').int): import('./IPos').IPos;
    getIPos(x: import('./double').double, y: import('./double').double, z: import('./double').double): import('./IPos').IPos;
    getIPos(x: import('./float').float, y: import('./float').float, z: import('./float').float): import('./IPos').IPos;
    getIPos(serializedPos: import('./long').long): import('./IPos').IPos;
    /**
     * Forms a box with corners as the input IPos parameters, and returns all
     * points inside the box as a list of IPos vectors.
     *
     * @param from           The starting IPos vector, first corner of the box.
     * @param to             The ending IPos vector, opposite corner of the box.
     * @param sortByDistance Sorts the list by distance from the "from" IPos parameter.
     * @return The list of all IPos vectors inside the box.
     */
    getAllInBox(from: import('./IPos').IPos, to: import('./IPos').IPos, sortByDistance: import('./boolean').boolean): import('./IPos').IPos[];
    getAllInBox(from: import('./IPos').IPos, to: import('./IPos').IPos): import('./IPos').IPos[];
    getIContainer(var1: import('../../../net/minecraft/inventory/IInventory').IInventory): import('./IContainer').IContainer;
    getIContainer(var1: import('../../../net/minecraft/inventory/Container').Container): import('./IContainer').IContainer;
    getIItemStack(var1: import('../../../net/minecraft/item/ItemStack').ItemStack): import('./item/IItemStack').IItemStack;
    /**
     * @param var1 the Minecraft world
     * @return A single IWorld from Loaded IWorlds
     */
    getIWorld(var1: import('../../../net/minecraft/world/World').World): import('./IWorld').IWorld;
    /**
     * @param var1 the dimension ID
     * @return A single IWorld from Loaded IWorlds
     */
    getIWorld(var1: import('./int').int): import('./IWorld').IWorld;
    /**
     * This will forcefully load the dimension if it is not loaded
     * Forge sometimes automatically, unloads the End when all players
     * are no longer present.
     *
     * @param var1 the dimension ID
     * @return A single IWorld from Loaded/Unloaded IWorlds
     */
    getIWorldLoad(var1: import('./int').int): import('./IWorld').IWorld;
    /**
     *
     * @return The global IActionManager for the server
     */
    getActionManager(): import('./IActionManager').IActionManager;
    /**
     * @return The list of all LOADED IWorlds
     */
    getIWorlds(): import('./IWorld').IWorld[];
    getIDamageSource(var1: import('../../../net/minecraft/util/DamageSource').DamageSource): import('./IDamageSource').IDamageSource;
    getIDamageSource(entity: import('./entity/IEntity').IEntity): import('./IDamageSource').IDamageSource;
    getEnergyHandler(): import('./IEnergyHandler').IEnergyHandler;
    events(): EventBus;
    getGlobalDir(): Java.java.io.File;
    getWorldDir(): Java.java.io.File;
    IsAvailable(): import('./boolean').boolean;
    Instance(): import('./AbstractNpcAPI').AbstractNpcAPI;
    executeCommand(var1: import('./IWorld').IWorld, var2: String): import('./void').void;
    /**
     * Generates a new name as a String using the Markov name generator.
     *
     * @param dictionary An integer representing which dictionary to use:
     *                   0: Roman
     *                   1: Japanese
     *                   2: Slavic
     *                   3: Welsh
     *                   4: Saami
     *                   5: Old Norse
     *                   6: Ancient Greek
     *                   7: Aztec
     *                   8: CustomNPCs Classic
     *                   9: Spanish
     * @param gender     The gender of the name:
     *                   0: Random
     *                   1: Male
     *                   2: Female
     * @return a random name
     */
    getRandomName(dictionary: import('./int').int, gender: import('./int').int): String;
    getINbt(nbtTagCompound: import('../../../net/minecraft/nbt/NBTTagCompound').NBTTagCompound): import('./INbt').INbt;
    stringToNbt(str: String): import('./INbt').INbt;
    getAllServerPlayers(): import('./entity/IPlayer').IPlayer[];
    getPlayerNames(): String[];
    createItemFromNBT(nbt: import('./INbt').INbt): import('./item/IItemStack').IItemStack;
    createItem(id: String, damage: import('./int').int, size: import('./int').int): import('./item/IItemStack').IItemStack;
    playSoundAtEntity(entity: import('./entity/IEntity').IEntity, sound: String, volume: import('./float').float, pitch: import('./float').float): import('./void').void;
    playSoundToNearExcept(player: import('./entity/IPlayer').IPlayer, sound: String, volume: import('./float').float, pitch: import('./float').float): import('./void').void;
    /**
     *
     * @return Returns the server's Message of The Day.
     */
    getMOTD(): String;
    /**
     * @param motd The server's new Message of The Day.
     */
    setMOTD(motd: String): import('./void').void;
    /**
     *
     * @param directory the particle texture directory
     * @return A new IParticle object initialized with the given texture.
     */
    createParticle(directory: String): import('./IParticle').IParticle;
    createEntityParticle(directory: String): import('./IParticle').IParticle;
    createSound(directory: String): import('./handler/data/ISound').ISound;
    playSound(id: import('./int').int, sound: import('./handler/data/ISound').ISound): import('./void').void;
    playSound(sound: import('./handler/data/ISound').ISound): import('./void').void;
    stopSound(id: import('./int').int): import('./void').void;
    pauseSounds(): import('./void').void;
    continueSounds(): import('./void').void;
    stopSounds(): import('./void').void;
    /**
     *
     * @return The uptime of the server in MC ticks.
     */
    getServerTime(): import('./int').int;
    arePlayerScriptsEnabled(): import('./boolean').boolean;
    areForgeScriptsEnabled(): import('./boolean').boolean;
    areGlobalNPCScriptsEnabled(): import('./boolean').boolean;
    enablePlayerScripts(enable: import('./boolean').boolean): import('./void').void;
    enableForgeScripts(enable: import('./boolean').boolean): import('./void').void;
    enableGlobalNPCScripts(enable: import('./boolean').boolean): import('./void').void;
    /**
     *
     * @param id        The id of the custom GUI.
     * @param width     The width of the GUI in pixels.
     * @param height    The height of the GUI in pixels.
     * @param pauseGame Whether the GUI pauses the game or not.
     * @return A new ICustomGui object with the given attributes.
     */
    createCustomGui(id: import('./int').int, width: import('./int').int, height: import('./int').int, pauseGame: import('./boolean').boolean): import('./gui/ICustomGui').ICustomGui;
    /**
     *
     * @param id the overlay ID
     * @return A new ICustomOverlay overlay object with the given ID.
     */
    createCustomOverlay(id: import('./int').int): import('./overlay/ICustomOverlay').ICustomOverlay;
    /**
     *
     * @param texture the texture path
     * @return A new ISkinOverlay object initialized with the given texture.
     */
    createSkinOverlay(texture: String): import('./ISkinOverlay').ISkinOverlay;
    millisToTime(millis: import('./long').long): String;
    ticksToTime(ticks: import('./long').long): String;
    createAnimation(name: String): import('./handler/data/IAnimation').IAnimation;
    createAnimation(name: String, speed: import('./float').float, smooth: import('./byte').byte): import('./handler/data/IAnimation').IAnimation;
    createFrame(duration: import('./int').int): import('./handler/data/IFrame').IFrame;
    createFrame(duration: import('./int').int, speed: import('./float').float, smooth: import('./byte').byte): import('./handler/data/IFrame').IFrame;
    createPart(name: String): import('./handler/data/IFramePart').IFramePart;
    createPart(name: String, rotation: import('./float').float[], pivot: import('./float').float[]): import('./handler/data/IFramePart').IFramePart;
    createPart(name: String, rotation: import('./float').float[], pivot: import('./float').float[], speed: import('./float').float, smooth: import('./byte').byte): import('./handler/data/IFramePart').IFramePart;
    createPart(partId: import('./int').int): import('./handler/data/IFramePart').IFramePart;
    createPart(partId: import('./int').int, rotation: import('./float').float[], pivot: import('./float').float[]): import('./handler/data/IFramePart').IFramePart;
    createPart(partId: import('./int').int, rotation: import('./float').float[], pivot: import('./float').float[], speed: import('./float').float, smooth: import('./byte').byte): import('./handler/data/IFramePart').IFramePart;
    instance: import('./AbstractNpcAPI').AbstractNpcAPI;
}
