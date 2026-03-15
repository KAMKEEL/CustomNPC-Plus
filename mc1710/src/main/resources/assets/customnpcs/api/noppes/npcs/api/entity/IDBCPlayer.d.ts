/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity
 */

/**
 * Extends IPlayer with additional methods specific to DBC (Dragon Ball C) players.
 * These methods manage stats, bonus attributes, form configurations, inventory, and more.
  * @javaFqn noppes.npcs.api.entity.IDBCPlayer
*/
export interface IDBCPlayer extends import('./IPlayer').IPlayer {
    /**
     * Sets the specified stat to the given value.
     *
     * @param stat  the stat name (e.g. "str", "dex", "con", "wil", "mnd", "spi").
     * @param value the new value.
     */
    setStat(stat: String, value: import('./int').int): import('./void').void;
    /**
     * Retrieves the value of the specified stat.
     *
     * @param stat the stat name.
     * @return the stat value.
     */
    getStat(stat: String): import('./int').int;
    /**
     * Adds a bonus attribute for the specified stat.
     *
     * @param stat           the stat name.
     * @param bonusID        the identifier for the bonus.
     * @param operation      the arithmetic operation to apply.
     * @param attributeValue the attribute value.
     */
    addBonusAttribute(stat: String, bonusID: String, operation: String, attributeValue: import('./double').double): import('./void').void;
    /**
     * Adds a bonus attribute for the specified stat.
     *
     * @param stat           the stat name.
     * @param bonusID        the identifier for the bonus.
     * @param operation      the arithmetic operation to apply.
     * @param attributeValue the attribute value.
     * @param endOfTheList   whether to add the bonus at the end of the list.
     */
    addBonusAttribute(stat: String, bonusID: String, operation: String, attributeValue: import('./double').double, endOfTheList: import('./boolean').boolean): import('./void').void;
    /**
     * Increases an existing bonus attribute for the specified stat.
     *
     * @param stat           the stat name.
     * @param bonusID        the identifier for the bonus.
     * @param operation      the arithmetic operation.
     * @param attributeValue the value to add.
     */
    addToBonusAttribute(stat: String, bonusID: String, operation: String, attributeValue: import('./double').double): import('./void').void;
    /**
     * Sets a bonus attribute for the specified stat.
     *
     * @param stat           the stat name.
     * @param bonusID        the identifier for the bonus.
     * @param operation      the arithmetic operation.
     * @param attributeValue the value to set.
     */
    setBonusAttribute(stat: String, bonusID: String, operation: String, attributeValue: import('./double').double): import('./void').void;
    /**
     * Retrieves a bonus attribute for the specified stat.
     *
     * @param stat    the stat name.
     * @param bonusID the identifier for the bonus.
     */
    getBonusAttribute(stat: String, bonusID: String): import('./void').void;
    /**
     * Removes the bonus attribute with the given identifier for the specified stat.
     *
     * @param stat    the stat name.
     * @param bonusID the bonus identifier.
     */
    removeBonusAttribute(stat: String, bonusID: String): import('./void').void;
    /**
     * Clears all bonus attributes for the specified stat.
     *
     * @param stat the stat name.
     */
    clearBonusAttribute(stat: String): import('./void').void;
    /**
     * Retrieves a bonus attribute string based on an action.
     *
     * @param action  the action (get, remove, or clear).
     * @param stat    the stat name.
     * @param bonusID the bonus identifier.
     * @return the bonus attribute string.
     */
    bonusAttribute(action: String, stat: String, bonusID: String): String;
    /**
     * Retrieves a bonus attribute string with full parameters.
     *
     * @param action         the action (add, addto, set, get, remove, or clear).
     * @param stat           the stat name.
     * @param bonusID        the bonus identifier.
     * @param operation      the arithmetic operation.
     * @param attributeValue the attribute value.
     * @param endOfTheList   whether to add at the end of the list.
     * @return the bonus attribute string.
     */
    bonusAttribute(action: String, stat: String, bonusID: String, operation: String, attributeValue: import('./double').double, endOfTheList: import('./boolean').boolean): String;
    /**
     * Sets the release state.
     *
     * @param release the release value.
     */
    setRelease(release: import('./byte').byte): import('./void').void;
    /**
     * Returns the current release state.
     *
     * @return the release value.
     */
    getRelease(): import('./byte').byte;
    /**
     * Sets the body value. Also HP.
     *
     * @param body the body value.
     */
    setBody(body: import('./int').int): import('./void').void;
    /**
     * Returns the body value. Also HP.
     *
     * @return the body.
     */
    getBody(): import('./int').int;
    /**
     * Sets the HP value.
     *
     * @param hp the HP value.
     */
    setHP(hp: import('./int').int): import('./void').void;
    /**
     * Returns the HP value.
     *
     * @return the HP.
     */
    getHP(): import('./int').int;
    /**
     * Sets the stamina value.
     *
     * @param stamina the stamina value.
     */
    setStamina(stamina: import('./int').int): import('./void').void;
    /**
     * Returns the stamina value.
     *
     * @return the stamina.
     */
    getStamina(): import('./int').int;
    /**
     * Sets the Ki value.
     *
     * @param ki Ki value.
     */
    setKi(ki: import('./int').int): import('./void').void;
    /**
     * Returns the Ki value.
     *
     * @return Ki of Player.
     */
    getKi(): import('./int').int;
    /**
     * Sets the TP value.
     *
     * @param tp Set TP Amount
     */
    setTP(tp: import('./int').int): import('./void').void;
    /**
     * Returns the TP value.
     *
     * @return Player TP Amount
     */
    getTP(): import('./int').int;
    /**
     * Sets the gravity for the player.
     *
     * @param gravity the gravity value.
     */
    setGravity(gravity: import('./float').float): import('./void').void;
    /**
     * Returns the current gravity value.
     *
     * @return the gravity.
     */
    getGravity(): import('./float').float;
    /**
     * Checks if the player is blocking.
     *
     * @return true if blocking; false otherwise.
     */
    isBlocking(): import('./boolean').boolean;
    /**
     * Sets the hair code used to define the player's hairstyle.
     *
     * @param hairCode the hair code string.
     */
    setHairCode(hairCode: String): import('./void').void;
    /**
     * Returns the player's hair code.
     *
     * @return the hair code string.
     */
    getHairCode(): String;
    /**
     * Sets the extra code used for additional customization.
     *
     * @param extraCode the extra code string.
     */
    setExtraCode(extraCode: String): import('./void').void;
    /**
     * Returns the player's extra code.
     *
     * @return the extra code string.
     */
    getExtraCode(): String;
    /**
     * Sets an item in the DBC extra inventory slot.
     * <p>
     * Slot definitions:
     * <ul>
     *   <li>0 - Weight</li>
     *   <li>1 - Body</li>
     *   <li>2 - Head</li>
     *   <li>3 - 4th Vanity Slot Down to the left</li>
     *   <li>4 - 3rd Vanity Slot Down to the left</li>
     *   <li>5 - 2nd Vanity Slot Down to the left</li>
     *   <li>6 - 1st Vanity Slot Down to the left</li>
     *   <li>7 - 4th Vanity Slot Down to the right</li>
     *   <li>8 - 3rd Vanity Slot Down to the right</li>
     *   <li>9 - 2nd Vanity Slot Down to the right</li>
     *   <li>10 - 1st Vanity Slot Down to the right</li>
     * </ul>
     *
     * @param itemStack the item to set (or null to remove).
     * @param slot      the slot index.
     * @param vanity    whether the slot is in the vanity inventory.
     */
    setItem(itemStack: import('../item/IItemStack').IItemStack, slot: import('./byte').byte, vanity: import('./boolean').boolean): import('./void').void;
    /**
     * Retrieves the item from the specified DBC inventory slot.
     *
     * @param slot   the slot index.
     * @param vanity whether the slot is in the vanity inventory.
     * @return the item stack, or null if empty.
     */
    getItem(slot: import('./byte').byte, vanity: import('./boolean').boolean): import('../item/IItemStack').IItemStack;
    /**
     * Returns the entire DBC extra inventory.
     *
     * @return an array of item stacks.
     */
    getInventory(): import('../item/IItemStack').IItemStack[];
    /**
     * Sets the player's form.
     *
     * @param form the form value.
     */
    setForm(form: import('./byte').byte): import('./void').void;
    /**
     * Returns the player's current form.
     *
     * @return the form value.
     */
    getForm(): import('./byte').byte;
    /**
     * Sets the player's secondary form.
     *
     * @param form2 the secondary form value.
     */
    setForm2(form2: import('./byte').byte): import('./void').void;
    /**
     * Returns the player's secondary form.
     *
     * @return the secondary form value.
     */
    getForm2(): import('./byte').byte;
    /**
     * Returns the mastery value for a specific racial form.
     *
     * @param form the form index.
     * @return the mastery value.
     */
    getRacialFormMastery(form: import('./byte').byte): import('./double').double;
    /**
     * Sets the mastery value for a specific racial form.
     *
     * @param form  the form index.
     * @param value the new mastery value.
     */
    setRacialFormMastery(form: import('./byte').byte, value: import('./double').double): import('./void').void;
    /**
     * Adds to the mastery value for a specific racial form.
     *
     * @param form  the form index.
     * @param value the value to add.
     */
    addRacialFormMastery(form: import('./byte').byte, value: import('./double').double): import('./void').void;
    /**
     * Returns the mastery value for a non-racial (other) form.
     *
     * @param formName the name of the form.
     * @return the mastery value.
     */
    getOtherFormMastery(formName: String): import('./double').double;
    /**
     * Sets the mastery value for a non-racial (other) form.
     *
     * @param formName the name of the form.
     * @param value    the new mastery value.
     */
    setOtherFormMastery(formName: String, value: import('./double').double): import('./void').void;
    /**
     * Adds to the mastery value for a non-racial (other) form.
     *
     * @param formName the name of the form.
     * @param value    the value to add.
     */
    addOtherFormMastery(formName: String, value: import('./double').double): import('./void').void;
    /**
     * Sets the player's power points.
     *
     * @param points the power points.
     */
    setPowerPoints(points: import('./int').int): import('./void').void;
    /**
     * Returns the player's power points.
     *
     * @return the power points.
     */
    getPowerPoints(): import('./int').int;
    /**
     * Sets the player's aura color.
     *
     * @param color the aura color.
     */
    setAuraColor(color: import('./int').int): import('./void').void;
    /**
     * Returns the player's aura color.
     *
     * @return the aura color.
     */
    getAuraColor(): import('./int').int;
    /**
     * Sets the player's form level.
     *
     * @param level the form level.
     */
    setFormLevel(level: import('./int').int): import('./void').void;
    /**
     * Returns the player's form level.
     *
     * @return the form level.
     */
    getFormLevel(): import('./int').int;
    /**
     * Sets the player's skills.
     *
     * @param skills a string representing the player's skills.
     */
    setSkills(skills: String): import('./void').void;
    /**
     * Returns the player's skills.
     *
     * @return a string representing the player's skills.
     */
    getSkills(): String;
    /**
     * Sets the player's status effects.
     *
     * @param statusEffects a string representing status effects.
     */
    setJRMCSE(statusEffects: String): import('./void').void;
    /**
     * Returns the player's status effects.
     *
     * @return a string representing the player's status effects.
     */
    getJRMCSE(): String;
    /**
     * Sets the player's race.
     *
     * @param race the race value.
     */
    setRace(race: import('./byte').byte): import('./void').void;
    /**
     * Returns the player's race.
     *
     * @return the race value.
     */
    getRace(): import('./int').int;
    /**
     * Sets the player's DBC class.
     *
     * @param dbcClass the DBC class value.
     */
    setDBCClass(dbcClass: import('./byte').byte): import('./void').void;
    /**
     * Returns the player's DBC class.
     *
     * @return the DBC class.
     */
    getDBCClass(): import('./byte').byte;
    /**
     * Sets the player's power type.
     *
     * @param powerType the power type.
     */
    setPowerType(powerType: import('./byte').byte): import('./void').void;
    /**
     * Returns the player's power type.
     *
     * @return the power type.
     */
    getPowerType(): import('./int').int;
    /**
     * Returns the player's kill count for a given type.
     *
     * @param type the kill count type ("evil", "good", "neutral", or "all").
     * @return the kill count.
     */
    getKillCount(type: String): import('./int').int;
    /**
     * Returns the fusion string for the player.
     *
     * @return the fusion string.
     */
    getFusionString(): String;
}
