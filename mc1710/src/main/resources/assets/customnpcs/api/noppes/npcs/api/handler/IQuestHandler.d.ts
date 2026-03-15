/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler
 */

/**
 * Handles retrieval of quests and quest categories.
  * @javaFqn noppes.npcs.api.handler.IQuestHandler
*/
export interface IQuestHandler {
    /**
     * Returns all quest categories.
     *
     * @return a list of quest categories.
     */
    categories(): import('./data/IQuestCategory').IQuestCategory[];
    /**
     * Returns the quest with the given ID.
     *
     * @param id the quest ID.
     * @return the quest, or null if not found.
     */
    get(id: import('./int').int): import('./data/IQuest').IQuest;
}
