/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler
 */

export interface IPlayerQuestData {

    // Methods
    getTrackedQuest(): import('./data/IQuest').IQuest;
    startQuest(id: number): void;
    finishQuest(id: number): void;
    stopQuest(id: number): void;
    removeQuest(id: number): void;
    hasFinishedQuest(id: number): boolean;
    hasActiveQuest(id: number): boolean;
    getActiveQuests(): import('./data/IQuest').IQuest[];
    getFinishedQuests(): import('./data/IQuest').IQuest[];
    getLastCompletedTime(id: number): number;
    setLastCompletedTime(id: number, time: number): void;

}
