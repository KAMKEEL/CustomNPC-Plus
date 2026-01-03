/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

export interface IParty {

    // Methods
    getPartyUUIDString(): string;
    getIsLocked(): boolean;
    setQuest(quest: import('./IQuest').IQuest): void;
    getQuest(): import('./IQuest').IQuest;
    getCurrentQuestID(): number;
    getCurrentQuestName(): string;
    addPlayer(playerName: string): boolean;
    removePlayer(playerName: string): boolean;
    addPlayer(player: import('../../entity/IPlayer').IPlayer): boolean;
    removePlayer(player: import('../../entity/IPlayer').IPlayer): boolean;
    hasPlayer(player: import('../../entity/IPlayer').IPlayer): boolean;
    hasPlayer(playerName: string): boolean;
    getPartyLeaderName(): string;
    getPlayerNamesList(): Array<string;
    validateQuest(questID: number, sendLeaderMessages: boolean): boolean;
    toggleFriendlyFire(): void;
    friendlyFire(): boolean;
    updateQuestObjectiveData(): void;
    updatePartyData(): void;

}
