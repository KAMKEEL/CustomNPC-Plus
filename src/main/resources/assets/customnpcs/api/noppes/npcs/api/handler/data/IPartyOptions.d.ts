/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

export interface IPartyOptions {

    // Methods
    isAllowParty(): boolean;
    setAllowParty(allowParty: boolean): void;
    isOnlyParty(): boolean;
    setOnlyParty(onlyParty: boolean): void;
    getPartyRequirements(): number;
    setPartyRequirements(partyRequirements: number): void;
    getRewardControl(): number;
    setRewardControl(rewardControl: number): void;
    getCompleteFor(): number;
    setCompleteFor(completeFor: number): void;
    getExecuteCommandFor(): number;
    setExecuteCommandFor(commandFor: number): void;
    getObjectiveRequirement(): number;
    setObjectiveRequirement(requirement: number): void;
    getMinPartySize(): number;
    setMinPartySize(newSize: number): void;
    getMaxPartySize(): number;
    setMaxPartySize(newSize: number): void;

}
