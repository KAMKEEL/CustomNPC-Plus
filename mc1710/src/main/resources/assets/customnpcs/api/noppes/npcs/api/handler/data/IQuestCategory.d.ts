/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

/**
 * Represents a category that groups related quests together.
  * @javaFqn noppes.npcs.api.handler.data.IQuestCategory
*/
export interface IQuestCategory {
    /** @return all quests in this category. */
    quests(): import('./IQuest').IQuest[];
    /** @return the category display name. */
    getName(): String;
    /**
     * Creates a new quest in this category.
     *
     * @return the newly created quest.
     */
    create(): import('./IQuest').IQuest;
    /** @return the unique category ID. */
    getId(): import('./int').int;
    quests: Java.java.util.HashMap<Integer, import('./Quest').Quest>;
    id: import('./int').int;
    title: String;
}
