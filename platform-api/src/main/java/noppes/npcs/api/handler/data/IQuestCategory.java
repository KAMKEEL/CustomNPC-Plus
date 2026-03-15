package noppes.npcs.api.handler.data;

import java.util.List;

/**
 * Represents a category that groups related quests together.
 */
public interface IQuestCategory {

    /** @return all quests in this category. */
    List<IQuest> quests();

    /** @return the category display name. */
    String getName();

    /**
     * Creates a new quest in this category.
     *
     * @return the newly created quest.
     */
    IQuest create();

    /** @return the unique category ID. */
    int getId();
}
