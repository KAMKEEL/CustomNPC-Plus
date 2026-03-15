/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler
 */

/**
 * Provides access to all persistent data categories for a player,
 * including companion, dialog, quest, faction, transport, mail, trade, and ability data.
  * @javaFqn noppes.npcs.api.handler.IPlayerData
*/
export interface IPlayerData {
    /**
     * Sets the player's companion NPC.
     *
     * @param npc the companion NPC, or null to clear.
     */
    setCompanion(npc: import('../entity/ICustomNpc').ICustomNpc): import('./void').void;
    /**
     * Returns the player's companion NPC.
     *
     * @return the companion NPC, or null if none.
     */
    getCompanion(): import('../entity/ICustomNpc').ICustomNpc;
    /**
     * @return true if the player currently has a companion NPC.
     */
    hasCompanion(): import('./boolean').boolean;
    /**
     * @return the entity ID of the companion, or -1 if none.
     */
    getCompanionID(): import('./int').int;
    /** @return the player's dialog data. */
    getDialogData(): import('./IPlayerDialogData').IPlayerDialogData;
    /** @return the player's bank data. */
    getBankData(): import('./IPlayerBankData').IPlayerBankData;
    /** @return the player's quest data. */
    getQuestData(): import('./IPlayerQuestData').IPlayerQuestData;
    /** @return the player's transport data. */
    getTransportData(): import('./IPlayerTransportData').IPlayerTransportData;
    /** @return the player's faction data. */
    getFactionData(): import('./IPlayerFactionData').IPlayerFactionData;
    /** @return the player's item giver data. */
    getItemGiverData(): import('./IPlayerItemGiverData').IPlayerItemGiverData;
    /** @return the player's mail data. */
    getMailData(): import('./IPlayerMailData').IPlayerMailData;
    /** @return the player's trade data. */
    getTradeData(): import('./IPlayerTradeData').IPlayerTradeData;
    /** @return the player's ability data. */
    getAbilityData(): import('../ability/IPlayerAbilityData').IPlayerAbilityData;
    /**
     * Saves all player data to disk.
     */
    save(): import('./void').void;
    dialogData: import('./PlayerDialogData').PlayerDialogData;
    bankData: import('./PlayerBankData').PlayerBankData;
    questData: import('./PlayerQuestData').PlayerQuestData;
    transportData: import('./PlayerTransportData').PlayerTransportData;
    factionData: import('./PlayerFactionData').PlayerFactionData;
    itemgiverData: import('./PlayerItemGiverData').PlayerItemGiverData;
    mailData: import('./PlayerMailData').PlayerMailData;
    animationData: import('./AnimationData').AnimationData;
    effectData: import('./PlayerEffectData').PlayerEffectData;
    timers: import('./DataTimers').DataTimers;
    skinOverlays: import('./DataSkinOverlays').DataSkinOverlays;
    magicData: import('./MagicData').MagicData;
    tradeData: import('./PlayerTradeData').PlayerTradeData;
    abilityData: import('./PlayerAbilityData').PlayerAbilityData;
    hotbarData: import('./PlayerAbilityHotbarData').PlayerAbilityHotbarData;
    actionManager: import('./ActionManager').ActionManager;
    scriptData: import('./PlayerDataScript').PlayerDataScript;
    editingNpc: import('./EntityNPCInterface').EntityNPCInterface;
    cloned: import('./NBTTagCompound').NBTTagCompound;
    partyUUID: import('./UUID').UUID;
    player: import('./EntityPlayer').EntityPlayer;
    playername: String;
    uuid: String;
    companionID: import('./int').int;
    isGUIOpen: import('./boolean').boolean;
    hadInteract: import('./boolean').boolean;
    updateClient: import('./boolean').boolean;
    screenSize: import('./ScreenSize').ScreenSize;
    profileSlot: import('./int').int;
}
