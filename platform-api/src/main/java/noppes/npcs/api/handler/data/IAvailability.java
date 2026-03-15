package noppes.npcs.api.handler.data;

import noppes.npcs.api.entity.IPlayer;

/**
 * Defines the conditions that determine if certain NPC interactions
 * (dialogs, quests, and faction requirements) are available to a player.
 * <p>
 * Availability conditions include:
 * <ul>
 *     <li>Daytime restrictions (Always, Day, or Night)</li>
 *     <li>Dialog conditions (based on whether a dialog has been read or not)</li>
 *     <li>Quest conditions (before, after, active, not active, acceptable, etc.)</li>
 *     <li>Faction requirements (faction ID, availability type and required stance)</li>
 *     <li>Minimum player level</li>
 * </ul>
 * The method {@link #isAvailable(IPlayer)} evaluates all these criteria.
 */
public interface IAvailability {

    /**
     * Determines whether all availability conditions are met for the given player.
     * <p>
     * Conditions checked include:
     * <ul>
     *     <li>Daytime requirement – if set to Day, the player must be in daytime; if Night, in nighttime.</li>
     *     <li>Dialog conditions – all dialog slots (0–3) must match their respective availability settings.</li>
     *     <li>Quest conditions – all quest slots (0–3) must satisfy their availability criteria.</li>
     *     <li>Faction conditions – each faction requirement (slot 0 and 1) must match the specified faction availability and stance.</li>
     *     <li>Minimum player level – the player's level must be at least the required minimum.</li>
     * </ul>
     *
     * @param player the player for whom availability is being checked.
     * @return true if all conditions are met; false otherwise.
     */
    boolean isAvailable(IPlayer player);

    /**
     * Returns the daytime condition requirement as an ordinal value.
     * <p>
     * The returned value corresponds to:
     * <ul>
     *     <li>0 – Always</li>
     *     <li>1 – Day</li>
     *     <li>2 – Night</li>
     * </ul>
     *
     * @return the ordinal value representing the required daytime condition.
     */
    int getDaytime();

    /**
     * Sets the daytime condition requirement.
     * <p>
     * The provided value is clamped between 0 and 2, corresponding to:
     * <ul>
     *     <li>0 – Always</li>
     *     <li>1 – Day</li>
     *     <li>2 – Night</li>
     * </ul>
     *
     * @param daytime the ordinal value for the daytime condition.
     */
    void setDaytime(int daytime);

    /**
     * Returns the minimum player level required for the availability conditions.
     *
     * @return the minimum player level.
     */
    int getMinPlayerLevel();

    /**
     * Sets the minimum player level required for the availability conditions.
     *
     * @param level the minimum player level.
     */
    void setMinPlayerLevel(int level);

    /**
     * Retrieves the dialog ID for the specified slot.
     * <p>
     * Valid slot indices are 0 to 3. A dialog ID of -1 indicates no dialog is set.
     *
     * @param index the dialog slot index (0–3).
     * @return the dialog ID associated with the specified slot.
     */
    int getDialog(int index);

    /**
     * Sets the dialog for the specified slot with the given dialog ID and availability type.
     * <p>
     * The availability type (provided as an ordinal) determines when the dialog is available:
     * <ul>
     *     <li>0 – Always</li>
     *     <li>1 – Before (available before the dialog is read)</li>
     *     <li>2 – After (available after the dialog is read)</li>
     * </ul>
     *
     * @param index the dialog slot index (0–3).
     * @param id    the dialog ID to set.
     * @param type  the availability type as an ordinal value.
     */
    void setDialog(int index, int id, int type);

    /**
     * Removes the dialog from the specified slot, resetting it to default values.
     * <p>
     * After removal, the dialog ID is set to -1 and its availability is reset to Always.
     *
     * @param index the dialog slot index (0–3).
     */
    void removeDialog(int index);

    /**
     * Retrieves the quest ID for the specified slot.
     * <p>
     * Valid slot indices are 0 to 3. A quest ID of -1 indicates no quest is set.
     *
     * @param index the quest slot index (0–3).
     * @return the quest ID for the specified slot.
     */
    int getQuest(int index);

    /**
     * Sets the quest for the specified slot with the given quest ID and availability type.
     * <p>
     * The availability type for quests is provided as an ordinal and can represent various conditions
     * such as Always, After, Before, Active, NotActive, Acceptable, or NotAcceptable.
     *
     * @param index the quest slot index (0–3).
     * @param id    the quest ID to set.
     * @param type  the availability type as an ordinal value.
     */
    void setQuest(int index, int id, int type);

    /**
     * Removes the quest from the specified slot, resetting it to default values.
     * <p>
     * After removal, the quest ID is set to -1 and its availability is reset to Always.
     *
     * @param index the quest slot index (0–3).
     */
    void removeQuest(int index);

    /**
     * Sets the faction requirement for the specified slot with the given parameters.
     * <p>
     * The parameters are:
     * <ul>
     *     <li><code>slot</code> – the faction slot index (0 or 1).</li>
     *     <li><code>id</code> – the faction ID.</li>
     *     <li><code>type</code> – the faction availability type (clamped between 0 and 2).</li>
     *     <li><code>stance</code> – the required faction stance (clamped between 0 and 2).</li>
     * </ul>
     *
     * @param slot   the faction slot index (0 or 1).
     * @param id     the faction ID.
     * @param type   the faction availability type as an ordinal value.
     * @param stance the required faction stance as an ordinal value.
     */
    void setFaction(int slot, int id, int type, int stance);

    /**
     * Removes the faction requirement from the specified slot, resetting it to default values.
     * <p>
     * After removal, the faction ID is set to -1, the availability type is set to Always,
     * and the faction stance is reset to Friendly.
     *
     * @param slot the faction slot index (0 or 1).
     */
    void removeFaction(int slot);
}
