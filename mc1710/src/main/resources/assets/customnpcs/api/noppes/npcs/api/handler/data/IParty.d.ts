/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

/**
 * @javaFqn noppes.npcs.api.handler.data.IParty
 */
export interface IParty {
    /**
     * @return UUID of Party
     */
    getPartyUUIDString(): String;
    /**
     * @return if party is locked (quest is set)
     */
    getIsLocked(): import('./boolean').boolean;
    /**
     * Set a quest to the party (this does not validate use validateQuest())
     *
     * @param quest Quest Object
     */
    setQuest(quest: import('./IQuest').IQuest): import('./void').void;
    /**
     * Get the Quest for the Party, can be null
     *
     * @return quest
     */
    getQuest(): import('./IQuest').IQuest;
    /**
     * Get the Quest ID for Party | -1 if no quest
     *
     * @return quest id
     */
    getCurrentQuestID(): import('./int').int;
    /**
     * Get the Quest Name for Party | BLANK if no quest
     *
     * @return quest name
     */
    getCurrentQuestName(): String;
    /**
     * @param playerName Username of Player (Case Sensitive)
     * @return successful or not
     */
    addPlayer(playerName: String): import('./boolean').boolean;
    /**
     * @param playerName Username of Player (Case Sensitive)
     * @return successful or not
     */
    removePlayer(playerName: String): import('./boolean').boolean;
    /**
     * @param player IPlayer
     * @return successful or not
     */
    addPlayer(player: import('../../entity/IPlayer').IPlayer): import('./boolean').boolean;
    /**
     * @param player IPlayer
     * @return successful or not
     */
    removePlayer(player: import('../../entity/IPlayer').IPlayer): import('./boolean').boolean;
    /**
     * Check if Player is in Party with IPlayer
     *
     * @param player the player to check
     * @return true/false if player is in party
     */
    hasPlayer(player: import('../../entity/IPlayer').IPlayer): import('./boolean').boolean;
    /**
     * Check if Player is in Party with Player Name
     *
     * @param playerName the player name to check
     * @return true/false if player is in party
     */
    hasPlayer(playerName: String): import('./boolean').boolean;
    /**
     * @return Party Leader's Name
     */
    getPartyLeaderName(): String;
    /**
     * @return Collection of all Player Names in Party
     */
    getPlayerNamesList(): String[];
    /**
     * @param questID            ID for the Quest to Verify
     * @param sendLeaderMessages IF true will send ERROR message to Party Leader on why the quest could not be set
     * @return true/false if the party CAN have the quest set
     */
    validateQuest(questID: import('./int').int, sendLeaderMessages: import('./boolean').boolean): import('./boolean').boolean;
    /**
     * Toggles Friendly Fire
     */
    toggleFriendlyFire(): import('./void').void;
    /**
     * @return Current Friendly Fire settings in Party
     */
    friendlyFire(): import('./boolean').boolean;
    /**
     * Sends information for Quest Objectives to all Party Participants [Lower Data]
     */
    updateQuestObjectiveData(): import('./void').void;
    /**
     * Sends all PARTY INFORMATION to all Party Participants [Higher Data]
     */
    updatePartyData(): import('./void').void;
}
