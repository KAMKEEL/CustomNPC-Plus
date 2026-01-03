/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

export interface IQuestCategory {

    // Methods
    quests(): Array<IQuest;
    getName(): string;
    create(): import('./IQuest').IQuest;
    getId(): number;

}
