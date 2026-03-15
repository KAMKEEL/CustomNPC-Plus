/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

/**
 * Quest objective interface for dialog-type quests.
 * Players must interact with specific dialogs to complete the quest.
  * @javaFqn noppes.npcs.api.handler.data.IQuestDialog
*/
export interface IQuestDialog extends import('./IQuestInterface').IQuestInterface {
    dialogs: Java.java.util.HashMap<Integer, Integer>;
}
