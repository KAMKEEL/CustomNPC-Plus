/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api
 */

export class AbstractNpcAPI {

    // Fields
    instance: import('./AbstractNpcAPI').AbstractNpcAPI;
    instance: any;
    null: any;
    c: any;
    instance: any;

    // Methods
    getTempData(key: string): any;
    setTempData(key: string, value: any): void;
    hasTempData(key: string): boolean;
    removeTempData(key: string): void;
    clearTempData(): void;
    getTempDataKeys(): string[];
    getStoredData(key: string): any;
    setStoredData(key: string, value: any): void;
    hasStoredData(key: string): boolean;
    removeStoredData(key: string): void;
    clearStoredData(): void;
    getStoredDataKeys(): string[];
    registerICommand(command: import('./ICommand').ICommand): void;
    getICommand(commandName: string, priorityLevel: number): import('./ICommand').ICommand;
    addGlobalObject(key: string, obj: any): void;
    removeGlobalObject(key: string): void;
    hasGlobalObject(key: string): boolean;
    getEngineObjects(): object<string,any;
    sizeOfObject(obj: any): number;
    stopServer(): void;
    getCurrentPlayerCount(): number;
    getMaxPlayers(): number;
    kickAllPlayers(): void;
    isHardcore(): boolean;
    getFile(path: string): File;
    getServerOwner(): string;
    getFactions(): import('./handler/IFactionHandler').IFactionHandler;
    getRecipes(): import('./handler/IRecipeHandler').IRecipeHandler;
    getQuests(): import('./handler/IQuestHandler').IQuestHandler;
    getDialogs(): import('./handler/IDialogHandler').IDialogHandler;
    getClones(): import('./handler/ICloneHandler').ICloneHandler;
    getNaturalSpawns(): import('./handler/INaturalSpawnsHandler').INaturalSpawnsHandler;
    getProfileHandler(): import('./handler/IProfileHandler').IProfileHandler;
    getCustomEffectHandler(): import('./handler/ICustomEffectHandler').ICustomEffectHandler;
    getMagicHandler(): import('./handler/IMagicHandler').IMagicHandler;
    getPartyHandler(): import('./handler/IPartyHandler').IPartyHandler;
    getLocations(): import('./handler/ITransportHandler').ITransportHandler;
    getAnimations(): import('./handler/IAnimationHandler').IAnimationHandler;
    getAllBiomeNames(): string[];
    getChunkLoadingNPCs(): INpc[];
    getLoadedEntities(): [];
    getIBlock(world: import('./IWorld').IWorld, x: number, y: number, z: number): import('./IBlock').IBlock;
    getIBlock(world: import('./IWorld').IWorld, pos: import('./IPos').IPos): import('./IBlock').IBlock;
    getITileEntity(world: import('./IWorld').IWorld, pos: import('./IPos').IPos): import('./ITileEntity').ITileEntity;
    getITileEntity(world: import('./IWorld').IWorld, x: number, y: number, z: number): import('./ITileEntity').ITileEntity;
    getITileEntity(tileEntity: TileEntity): import('./ITileEntity').ITileEntity;
    getIPos(pos: import('../../../net/minecraft/util/math/BlockPos').BlockPos): import('./IPos').IPos;
    getIPos(x: number, y: number, z: number): import('./IPos').IPos;
    getIPos(x: number, y: number, z: number): import('./IPos').IPos;
    getIPos(x: number, y: number, z: number): import('./IPos').IPos;
    getIPos(serializedPos: number): import('./IPos').IPos;
    getAllInBox(from: import('./IPos').IPos, to: import('./IPos').IPos, sortByDistance: boolean): import('./IPos').IPos[];
    getAllInBox(from: import('./IPos').IPos, to: import('./IPos').IPos): import('./IPos').IPos[];
    getIContainer(var1: IInventory): import('./IContainer').IContainer;
    getIContainer(var1: Container): import('./IContainer').IContainer;
    getIItemStack(var1: ItemStack): import('./item/IItemStack').IItemStack;
    getIWorld(var1: World): import('./IWorld').IWorld;
    getIWorld(var1: number): import('./IWorld').IWorld;
    getIWorldLoad(var1: number): import('./IWorld').IWorld;
    getActionManager(): import('./handler/IActionManager').IActionManager;
    getIWorlds(): import('./IWorld').IWorld[];
    getIDamageSource(var1: DamageSource): import('./IDamageSource').IDamageSource;
    getIDamageSource(entity: IEntity<?): import('./IDamageSource').IDamageSource;
    events(): EventBus;
    getGlobalDir(): File;
    getWorldDir(): File;
    executeCommand(var1: import('./IWorld').IWorld, var2: string): void;
    getRandomName(dictionary: number, gender: number): string;
    getINbt(nbtTagCompound: NBTTagCompound): import('./INbt').INbt;
    stringToNbt(str: string): import('./INbt').INbt;
    getAllServerPlayers(): [];
    getPlayerNames(): string[];
    createItemFromNBT(nbt: import('./INbt').INbt): import('./item/IItemStack').IItemStack;
    createItem(id: string, damage: number, size: number): import('./item/IItemStack').IItemStack;
    playSoundAtEntity(entity: IEntity<?, sound: string, volume: number, pitch: number): void;
    playSoundToNearExcept(player: IPlayer<?, sound: string, volume: number, pitch: number): void;
    getMOTD(): string;
    setMOTD(motd: string): void;
    createParticle(directory: string): import('./IParticle').IParticle;
    createEntityParticle(directory: string): import('./IParticle').IParticle;
    createSound(directory: string): import('./handler/data/ISound').ISound;
    playSound(id: number, sound: import('./handler/data/ISound').ISound): void;
    playSound(sound: import('./handler/data/ISound').ISound): void;
    stopSound(id: number): void;
    pauseSounds(): void;
    continueSounds(): void;
    stopSounds(): void;
    getServerTime(): number;
    arePlayerScriptsEnabled(): boolean;
    areForgeScriptsEnabled(): boolean;
    areGlobalNPCScriptsEnabled(): boolean;
    enablePlayerScripts(enable: boolean): void;
    enableForgeScripts(enable: boolean): void;
    enableGlobalNPCScripts(enable: boolean): void;
    createCustomGui(id: number, width: number, height: number, pauseGame: boolean): import('./gui/ICustomGui').ICustomGui;
    createCustomOverlay(id: number): import('./overlay/ICustomOverlay').ICustomOverlay;
    createSkinOverlay(texture: string): import('./ISkinOverlay').ISkinOverlay;
    millisToTime(millis: number): string;
    ticksToTime(ticks: number): string;
    createAnimation(name: string): import('./handler/data/IAnimation').IAnimation;
    createAnimation(name: string, speed: number, smooth: byte): import('./handler/data/IAnimation').IAnimation;
    createFrame(duration: number): import('./handler/data/IFrame').IFrame;
    createFrame(duration: number, speed: number, smooth: byte): import('./handler/data/IFrame').IFrame;
    createPart(name: string): import('./handler/data/IFramePart').IFramePart;
    createPart(name: string, rotation: number[], pivot: number[]): import('./handler/data/IFramePart').IFramePart;
    createPart(name: string, rotation: number[], pivot: number[], speed: number, smooth: byte): import('./handler/data/IFramePart').IFramePart;
    createPart(partId: number): import('./handler/data/IFramePart').IFramePart;
    createPart(partId: number, rotation: number[], pivot: number[]): import('./handler/data/IFramePart').IFramePart;
    createPart(partId: number, rotation: number[], pivot: number[], speed: number, smooth: byte): import('./handler/data/IFramePart').IFramePart;

}
