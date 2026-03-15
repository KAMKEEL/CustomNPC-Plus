/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

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
  * @javaFqn noppes.npcs.api.handler.data.IAvailability
*/
export interface IAvailability {
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
    isAvailable(player: import('../../entity/IPlayer').IPlayer): import('./boolean').boolean;
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
    getDaytime(): import('./int').int;
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
    setDaytime(daytime: import('./int').int): import('./void').void;
    /**
     * Returns the minimum player level required for the availability conditions.
     *
     * @return the minimum player level.
     */
    getMinPlayerLevel(): import('./int').int;
    /**
     * Sets the minimum player level required for the availability conditions.
     *
     * @param level the minimum player level.
     */
    setMinPlayerLevel(level: import('./int').int): import('./void').void;
    /**
     * Retrieves the dialog ID for the specified slot.
     * <p>
     * Valid slot indices are 0 to 3. A dialog ID of -1 indicates no dialog is set.
     *
     * @param index the dialog slot index (0–3).
     * @return the dialog ID associated with the specified slot.
     */
    getDialog(index: import('./int').int): import('./int').int;
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
    setDialog(index: import('./int').int, id: import('./int').int, type: import('./int').int): import('./void').void;
    /**
     * Removes the dialog from the specified slot, resetting it to default values.
     * <p>
     * After removal, the dialog ID is set to -1 and its availability is reset to Always.
     *
     * @param index the dialog slot index (0–3).
     */
    removeDialog(index: import('./int').int): import('./void').void;
    /**
     * Retrieves the quest ID for the specified slot.
     * <p>
     * Valid slot indices are 0 to 3. A quest ID of -1 indicates no quest is set.
     *
     * @param index the quest slot index (0–3).
     * @return the quest ID for the specified slot.
     */
    getQuest(index: import('./int').int): import('./int').int;
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
    setQuest(index: import('./int').int, id: import('./int').int, type: import('./int').int): import('./void').void;
    /**
     * Removes the quest from the specified slot, resetting it to default values.
     * <p>
     * After removal, the quest ID is set to -1 and its availability is reset to Always.
     *
     * @param index the quest slot index (0–3).
     */
    removeQuest(index: import('./int').int): import('./void').void;
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
    setFaction(slot: import('./int').int, id: import('./int').int, type: import('./int').int, stance: import('./int').int): import('./void').void;
    /**
     * Removes the faction requirement from the specified slot, resetting it to default values.
     * <p>
     * After removal, the faction ID is set to -1, the availability type is set to Always,
     * and the faction stance is reset to Friendly.
     *
     * @param slot the faction slot index (0 or 1).
     */
    removeFaction(slot: import('./int').int): import('./void').void;
    version: import('./int').int;
    dialogAvailable: import('./EnumAvailabilityDialog').EnumAvailabilityDialog;
    dialog2Available: import('./EnumAvailabilityDialog').EnumAvailabilityDialog;
    dialog3Available: import('./EnumAvailabilityDialog').EnumAvailabilityDialog;
    dialog4Available: import('./EnumAvailabilityDialog').EnumAvailabilityDialog;
    dialogId: import('./int').int;
    dialog2Id: import('./int').int;
    dialog3Id: import('./int').int;
    dialog4Id: import('./int').int;
    questAvailable: import('./EnumAvailabilityQuest').EnumAvailabilityQuest;
    quest2Available: import('./EnumAvailabilityQuest').EnumAvailabilityQuest;
    quest3Available: import('./EnumAvailabilityQuest').EnumAvailabilityQuest;
    quest4Available: import('./EnumAvailabilityQuest').EnumAvailabilityQuest;
    questId: import('./int').int;
    quest2Id: import('./int').int;
    quest3Id: import('./int').int;
    quest4Id: import('./int').int;
    daytime: import('./EnumDayTime').EnumDayTime;
    factionId: import('./int').int;
    faction2Id: import('./int').int;
    factionAvailable: import('./EnumAvailabilityFactionType').EnumAvailabilityFactionType;
    faction2Available: import('./EnumAvailabilityFactionType').EnumAvailabilityFactionType;
    factionStance: import('./EnumAvailabilityFaction').EnumAvailabilityFaction;
    faction2Stance: import('./EnumAvailabilityFaction').EnumAvailabilityFaction;
    minPlayerLevel: import('./int').int;
}
