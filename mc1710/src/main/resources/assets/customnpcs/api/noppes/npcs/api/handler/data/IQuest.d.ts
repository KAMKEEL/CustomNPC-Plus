/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

/**
 * Represents a quest with objectives, rewards, repeatability, and party/profile options.
  * @javaFqn noppes.npcs.api.handler.data.IQuest
*/
export interface IQuest {
    /** @return the unique quest ID. */
    getId(): import('./int').int;
    /** @return the quest display name. */
    getName(): String;
    /** @param name the quest display name. */
    setName(name: String): import('./void').void;
    /** @return the quest type ordinal (0: Item, 1: Dialog, 2: Kill, 3: Location, 4: AreaKill, 5: Manual). */
    getType(): import('./int').int;
    /** @param type the quest type ordinal. */
    setType(type: import('./int').int): import('./void').void;
    /** @return the quest log description text. */
    getLogText(): String;
    /** @param text the quest log description text. */
    setLogText(text: String): import('./void').void;
    /** @return the text displayed on quest completion. */
    getCompleteText(): String;
    /** @param text the completion text. */
    setCompleteText(text: String): import('./void').void;
    /** @return the next quest in the chain, or null if none. */
    getNextQuest(): import('./IQuest').IQuest;
    /** @param quest the next quest in the chain, or null to clear. */
    setNextQuest(quest: import('./IQuest').IQuest): import('./void').void;
    /**
     * Returns the quest objectives for the given player.
     *
     * @param player the player whose objectives to retrieve.
     * @return an array of quest objectives.
     */
    getObjectives(player: import('../../entity/IPlayer').IPlayer): import('./IQuestObjective').IQuestObjective[];
    /** @return the category this quest belongs to. */
    getCategory(): import('./IQuestCategory').IQuestCategory;
    /** @return the reward container with items given on completion. */
    getRewards(): import('../../IContainer').IContainer;
    /** @return the NPC name associated with this quest. */
    getNpcName(): String;
    /** @param name the NPC name. */
    setNpcName(name: String): import('./void').void;
    /** Saves this quest to disk. */
    save(): import('./void').void;
    /** @return true if this quest can be repeated. */
    getIsRepeatable(): import('./boolean').boolean;
    /**
     * Returns the time remaining before the quest can be repeated.
     *
     * @param player the player.
     * @return milliseconds until repeatable, or 0 if ready.
     */
    getTimeUntilRepeat(player: import('../../entity/IPlayer').IPlayer): import('./long').long;
    /**
     * Sets the repeat type.
     *
     * @param type 0: None, 1: Instant, 2: Daily, 3: Weekly, 4: Custom.
     */
    setRepeatType(type: import('./int').int): import('./void').void;
    /** @return the repeat type ordinal. */
    getRepeatType(): import('./int').int;
    /** @return the quest interface (type-specific objective data). */
    getQuestInterface(): import('./IQuestInterface').IQuestInterface;
    /** @return the party options for this quest. */
    getPartyOptions(): import('./IPartyOptions').IPartyOptions;
    /** @return the profile options for this quest. */
    getProfileOptions(): import('./IProfileOptions').IProfileOptions;
    /** @return the custom cooldown in milliseconds for the Custom repeat type. */
    getCustomCooldown(): import('./long').long;
    /** @param newCooldown the custom cooldown in milliseconds. */
    setCustomCooldown(newCooldown: import('./long').long): import('./void').void;
    version: import('./int').int;
    id: import('./int').int;
    type: import('./EnumQuestType').EnumQuestType;
    repeat: import('./EnumQuestRepeat').EnumQuestRepeat;
    completion: import('./EnumQuestCompletion').EnumQuestCompletion;
    title: String;
    category: import('./QuestCategory').QuestCategory;
    logText: String;
    completeText: String;
    completerNpc: String;
    nextQuestid: import('./int').int;
    nextQuestTitle: String;
    mail: import('./PlayerMail').PlayerMail;
    command: String;
    questInterface: import('./QuestInterface').QuestInterface;
    customCooldown: import('./long').long;
    rewardExp: import('./int').int;
    rewardItems: import('./NpcMiscInventory').NpcMiscInventory;
    randomReward: import('./boolean').boolean;
    factionOptions: import('./FactionOptions').FactionOptions;
    partyOptions: import('./PartyOptions').PartyOptions;
    profileOptions: import('./ProfileOptions').ProfileOptions;
}
