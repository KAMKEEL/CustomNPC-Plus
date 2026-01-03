/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

export interface IQuest {

    // Methods
    getId(): number;
    getName(): string;
    setName(var1: string): void;
    getType(): number;
    setType(var1: number): void;
    getLogText(): string;
    setLogText(var1: string): void;
    getCompleteText(): string;
    setCompleteText(var1: string): void;
    getNextQuest(): import('./IQuest').IQuest;
    setNextQuest(var1: import('./IQuest').IQuest): void;
    getObjectives(var1: import('../../entity/IPlayer').IPlayer): import('./IQuestObjective').IQuestObjective[];
    getCategory(): import('./IQuestCategory').IQuestCategory;
    getRewards(): import('../../IContainer').IContainer;
    getNpcName(): string;
    setNpcName(var1: string): void;
    save(): void;
    getIsRepeatable(): boolean;
    getTimeUntilRepeat(player: import('../../entity/IPlayer').IPlayer): number;
    setRepeatType(type: number): void;
    getRepeatType(): number;
    getQuestInterface(): import('./IQuestInterface').IQuestInterface;
    getPartyOptions(): import('./IPartyOptions').IPartyOptions;
    getProfileOptions(): import('./IProfileOptions').IProfileOptions;
    getCustomCooldown(): number;
    setCustomCooldown(newCooldown: number): void;

}
