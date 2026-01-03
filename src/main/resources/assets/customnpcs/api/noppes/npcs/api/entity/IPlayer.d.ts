/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity
 */

export interface IPlayer extends IEntityLivingBase<T>, IAnimatable {

    // Methods
    getDisplayName(): string;
    getName(): string;
    kick(reason: string): void;
    setPosition(x: number, y: number, z: number): void;
    setPosition(pos: import('../IPos').IPos): void;
    setPosition(x: number, y: number, z: number, dimensionId: number): void;
    setPosition(pos: import('../IPos').IPos, dimensionId: number): void;
    setPosition(x: number, y: number, z: number, world: import('../IWorld').IWorld): void;
    setPosition(pos: import('../IPos').IPos, world: import('../IWorld').IWorld): void;
    setDimension(dimension: number): void;
    getHunger(): number;
    setHunger(hunger: number): void;
    getSaturation(): number;
    setSaturation(saturation: number): void;
    showDialog(dialog: import('../handler/data/IDialog').IDialog): void;
    hasReadDialog(dialog: import('../handler/data/IDialog').IDialog): boolean;
    readDialog(dialog: import('../handler/data/IDialog').IDialog): void;
    unreadDialog(dialog: import('../handler/data/IDialog').IDialog): void;
    showDialog(id: number): void;
    hasReadDialog(id: number): boolean;
    readDialog(id: number): void;
    unreadDialog(id: number): void;
    hasFinishedQuest(quest: import('../handler/data/IQuest').IQuest): boolean;
    hasActiveQuest(quest: import('../handler/data/IQuest').IQuest): boolean;
    startQuest(quest: import('../handler/data/IQuest').IQuest): void;
    finishQuest(quest: import('../handler/data/IQuest').IQuest): void;
    stopQuest(quest: import('../handler/data/IQuest').IQuest): void;
    removeQuest(quest: import('../handler/data/IQuest').IQuest): void;
    hasFinishedQuest(id: number): boolean;
    hasActiveQuest(id: number): boolean;
    startQuest(id: number): void;
    finishQuest(id: number): void;
    stopQuest(id: number): void;
    removeQuest(id: number): void;
    getFinishedQuests(): import('../handler/data/IQuest').IQuest[];
    getType(): number;
    typeOf(type: number): boolean;
    addFactionPoints(faction: number, points: number): void;
    setFactionPoints(faction: number, points: number): void;
    getFactionPoints(faction: number): number;
    sendMessage(message: string): void;
    getMode(): number;
    setMode(type: number): void;
    getInventory(): import('../item/IItemStack').IItemStack[];
    inventoryItemCount(item: import('../item/IItemStack').IItemStack, ignoreNBT: boolean, ignoreDamage: boolean): number;
    removeItem(id: string, damage: number, amount: number): boolean;
    removeItem(item: import('../item/IItemStack').IItemStack, amount: number, ignoreNBT: boolean, ignoreDamage: boolean): boolean;
    removeAllItems(item: import('../item/IItemStack').IItemStack, ignoreNBT: boolean, ignoreDamage: boolean): number;
    giveItem(item: import('../item/IItemStack').IItemStack, amount: number): boolean;
    giveItem(id: string, damage: number, amount: number): boolean;
    setSpawnpoint(x: number, y: number, z: number): void;
    setSpawnpoint(pos: import('../IPos').IPos): void;
    resetSpawnpoint(): void;
    setRotation(rotationYaw: number, rotationPitch: number): void;
    disableMouseInput(time: number, buttonIds: ): void;
    stopUsingItem(): void;
    clearItemInUse(): void;
    clearInventory(): void;
    playSound(name: string, volume: number, pitch: number): void;
    playSound(id: number, sound: import('../handler/data/ISound').ISound): void;
    playSound(sound: import('../handler/data/ISound').ISound): void;
    stopSound(id: number): void;
    pauseSounds(): void;
    continueSounds(): void;
    stopSounds(): void;
    mountEntity(ridingEntity: Entity): void;
    dropOneItem(dropStack: boolean): import('./IEntity').IEntity;
    canHarvestBlock(block: import('../IBlock').IBlock): boolean;
    interactWith(entity: import('./IEntity').IEntity): boolean;
    hasAchievement(achievement: string): boolean;
    hasBukkitPermission(permission: string): boolean;
    getExpLevel(): number;
    setExpLevel(level: number): void;
    getPixelmonData(): import('../IPixelmonPlayerData').IPixelmonPlayerData;
    getTimers(): import('../ITimers').ITimers;
    updatePlayerInventory(): void;
    getDBCPlayer(): import('../../kamkeel/npcdbc/api/IDBCAddon').IDBCAddon;
    blocking(): boolean;
    getData(): import('../handler/IPlayerData').IPlayerData;
    isScriptingDev(): boolean;
    getActiveQuests(): import('../handler/data/IQuest').IQuest[];
    getOpenContainer(): import('../IContainer').IContainer;
    showCustomGui(gui: import('../gui/ICustomGui').ICustomGui): void;
    getCustomGui(): import('../gui/ICustomGui').ICustomGui;
    closeGui(): void;
    showCustomOverlay(overlay: import('../overlay/ICustomOverlay').ICustomOverlay): void;
    closeOverlay(id: number): void;
    getOverlays(): import('../handler/IOverlayHandler').IOverlayHandler;
    getAnimationData(): import('../handler/data/IAnimationData').IAnimationData;
    setConqueredEnd(conqueredEnd: boolean): void;
    conqueredEnd(): boolean;
    getScreenSize(): import('../IScreenSize').IScreenSize;
    getMagicData(): import('../handler/data/IMagicData').IMagicData;
    getAttributes(): import('../handler/data/IPlayerAttributes').IPlayerAttributes;
    getActionManager(): import('../handler/IActionManager').IActionManager;
    getPartyMembers(): import('./IPlayer').IPlayer[];

}
