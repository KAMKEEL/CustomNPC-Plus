package noppes.npcs.api.handler.data;

/**
 * Quest objective interface for item-collection quests.
 * Configures whether items are consumed on turn-in and matching criteria.
 */
public interface IQuestItem extends IQuestInterface {

    /** @param leaveItems true to leave items in the player's inventory on turn-in. */
    void setLeaveItems(boolean leaveItems);

    /** @return true if items remain in the player's inventory on turn-in. */
    boolean getLeaveItems();

    /** @param ignoreDamage true to ignore item damage when matching. */
    void setIgnoreDamage(boolean ignoreDamage);

    /** @return true if item damage is ignored when matching. */
    boolean getIgnoreDamage();

    /** @param ignoreNbt true to ignore NBT data when matching. */
    void setIgnoreNbt(boolean ignoreNbt);

    /** @return true if NBT data is ignored when matching. */
    boolean getIgnoreNbt();
}
