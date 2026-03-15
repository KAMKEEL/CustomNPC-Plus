/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

/**
 * Marker interface for quest objective types (e.g., kill, item, location, dialog).
  * @javaFqn noppes.npcs.api.handler.data.IQuestInterface
*/
export interface IQuestInterface {
    questId: import('./int').int;
    writeEntityToNBT: import('./abstract void').abstract void;
    readEntityFromNBT: import('./abstract void').abstract void;
    isCompleted: import('./abstract boolean').abstract boolean;
    getQuestLogStatus: import('./abstract Vector').abstract Vector;
    getObjectives: import('./abstract IQuestObjective').abstract IQuestObjective[];
    getPartyObjectives: import('./abstract IQuestObjective').abstract IQuestObjective[];
    getPartyQuestLogStatus: import('./abstract Vector').abstract Vector;
    isPartyCompleted: import('./abstract boolean').abstract boolean;
}
