package noppes.npcs.api.handler.data;

import noppes.npcs.api.IContainer;
import noppes.npcs.api.entity.IPlayer;

/**
 * Represents a quest with objectives, rewards, repeatability, and party/profile options.
 */
public interface IQuest {

    /** @return the unique quest ID. */
    int getId();

    /** @return the quest display name. */
    String getName();

    /** @param name the quest display name. */
    void setName(String name);

    /** @return the quest type ordinal (0: Item, 1: Dialog, 2: Kill, 3: Location, 4: AreaKill, 5: Manual). */
    int getType();

    /** @param type the quest type ordinal. */
    void setType(int type);

    /** @return the quest log description text. */
    String getLogText();

    /** @param text the quest log description text. */
    void setLogText(String text);

    /** @return the text displayed on quest completion. */
    String getCompleteText();

    /** @param text the completion text. */
    void setCompleteText(String text);

    /** @return the next quest in the chain, or null if none. */
    IQuest getNextQuest();

    /** @param quest the next quest in the chain, or null to clear. */
    void setNextQuest(IQuest quest);

    /**
     * Returns the quest objectives for the given player.
     *
     * @param player the player whose objectives to retrieve.
     * @return an array of quest objectives.
     */
    IQuestObjective[] getObjectives(IPlayer player);

    /** @return the category this quest belongs to. */
    IQuestCategory getCategory();

    /** @return the reward container with items given on completion. */
    IContainer getRewards();

    /** @return the NPC name associated with this quest. */
    String getNpcName();

    /** @param name the NPC name. */
    void setNpcName(String name);

    /** Saves this quest to disk. */
    void save();

    /** @return true if this quest can be repeated. */
    boolean getIsRepeatable();

    /**
     * Returns the time remaining before the quest can be repeated.
     *
     * @param player the player.
     * @return milliseconds until repeatable, or 0 if ready.
     */
    long getTimeUntilRepeat(IPlayer player);

    /**
     * Sets the repeat type.
     *
     * @param type 0: None, 1: Instant, 2: Daily, 3: Weekly, 4: Custom.
     */
    void setRepeatType(int type);

    /** @return the repeat type ordinal. */
    int getRepeatType();

    /** @return the quest interface (type-specific objective data). */
    IQuestInterface getQuestInterface();

    /** @return the party options for this quest. */
    IPartyOptions getPartyOptions();

    /** @return the profile options for this quest. */
    IProfileOptions getProfileOptions();

    /** @return the custom cooldown in milliseconds for the Custom repeat type. */
    long getCustomCooldown();

    /** @param newCooldown the custom cooldown in milliseconds. */
    void setCustomCooldown(long newCooldown);
}
