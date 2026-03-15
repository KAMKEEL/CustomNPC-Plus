/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

/**
 * Configures party-related options for quests, controlling membership requirements,
 * reward distribution, completion rules, and party size limits.
  * @javaFqn noppes.npcs.api.handler.data.IPartyOptions
*/
export interface IPartyOptions {
    /** @return true if party participation is allowed. */
    isAllowParty(): import('./boolean').boolean;
    /** @param allowParty true to allow party participation. */
    setAllowParty(allowParty: import('./boolean').boolean): import('./void').void;
    /** @return true if only party members can participate. */
    isOnlyParty(): import('./boolean').boolean;
    /** @param onlyParty true to require party membership. */
    setOnlyParty(onlyParty: import('./boolean').boolean): import('./void').void;
    /**
     * @return 0:Leader, 1:All, 2:Valid
     */
    getPartyRequirements(): import('./int').int;
    /**
     * @param partyRequirements 0:Leader, 1:All, 2:Valid
     */
    setPartyRequirements(partyRequirements: import('./int').int): import('./void').void;
    /**
     * @return 0:Leader, 1:All, 2:Enrolled, 3:Valid
     */
    getRewardControl(): import('./int').int;
    /**
     * @param rewardControl 0:Leader, 1:All, 2:Enrolled, 3:Valid
     */
    setRewardControl(rewardControl: import('./int').int): import('./void').void;
    /**
     * @return 0:Leader, 1:All, 2:Enrolled, 3:Valid
     */
    getCompleteFor(): import('./int').int;
    /**
     * @param completeFor 0:Leader, 1:All, 2:Enrolled, 3:Valid
     */
    setCompleteFor(completeFor: import('./int').int): import('./void').void;
    /**
     * @return 0:Leader, 1:All, 2:Enrolled, 3:Valid
     */
    getExecuteCommandFor(): import('./int').int;
    /**
     * @param commandFor 0:Leader, 1:All, 2:Enrolled, 3:Valid
     */
    setExecuteCommandFor(commandFor: import('./int').int): import('./void').void;
    /**
     * @return 0:Shard, 1:All, 2:Leader
     */
    getObjectiveRequirement(): import('./int').int;
    /**
     * @param requirement 0:Shard, 1:All, 2:Leader
     */
    setObjectiveRequirement(requirement: import('./int').int): import('./void').void;
    /** @return the minimum party size required. */
    getMinPartySize(): import('./int').int;
    /** @param newSize the minimum party size. */
    setMinPartySize(newSize: import('./int').int): import('./void').void;
    /** @return the maximum party size allowed. */
    getMaxPartySize(): import('./int').int;
    /** @param newSize the maximum party size. */
    setMaxPartySize(newSize: import('./int').int): import('./void').void;
}
