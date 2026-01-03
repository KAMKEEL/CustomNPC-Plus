/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

export interface IQuestObjective {

    // Methods
    getProgress(): number;
    setProgress(progress: number): void;
    setPlayerProgress(playerName: string, progress: number): void;
    getMaxProgress(): number;
    isCompleted(): boolean;
    getText(): string;
    getAdditionalText(): string;

}
