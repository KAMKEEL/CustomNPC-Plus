package noppes.npcs.api.handler;

import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.api.handler.data.IQuestCategory;

import java.util.List;

/**
 * Handles retrieval of quests and quest categories.
 */
public interface IQuestHandler {

    /**
     * Returns all quest categories.
     *
     * @return a list of quest categories.
     */
    List<IQuestCategory> categories();

    /**
     * Returns the quest with the given ID.
     *
     * @param id the quest ID.
     * @return the quest, or null if not found.
     */
    IQuest get(int id);
}
