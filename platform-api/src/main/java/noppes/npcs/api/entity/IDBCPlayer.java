package noppes.npcs.api.entity;

import noppes.npcs.api.item.IItemStack;

/**
 * Extends IPlayer with additional methods specific to DBC (Dragon Ball C) players.
 * These methods manage stats, bonus attributes, form configurations, inventory, and more.
 */
public interface IDBCPlayer extends IPlayer {

    /**
     * Sets the specified stat to the given value.
     *
     * @param stat  the stat name (e.g. "str", "dex", "con", "wil", "mnd", "spi").
     * @param value the new value.
     */
    void setStat(String stat, int value);

    /**
     * Retrieves the value of the specified stat.
     *
     * @param stat the stat name.
     * @return the stat value.
     */
    int getStat(String stat);

    /**
     * Adds a bonus attribute for the specified stat.
     *
     * @param stat           the stat name.
     * @param bonusID        the identifier for the bonus.
     * @param operation      the arithmetic operation to apply.
     * @param attributeValue the attribute value.
     */
    void addBonusAttribute(String stat, String bonusID, String operation, double attributeValue);

    /**
     * Adds a bonus attribute for the specified stat.
     *
     * @param stat           the stat name.
     * @param bonusID        the identifier for the bonus.
     * @param operation      the arithmetic operation to apply.
     * @param attributeValue the attribute value.
     * @param endOfTheList   whether to add the bonus at the end of the list.
     */
    void addBonusAttribute(String stat, String bonusID, String operation, double attributeValue, boolean endOfTheList);

    /**
     * Increases an existing bonus attribute for the specified stat.
     *
     * @param stat           the stat name.
     * @param bonusID        the identifier for the bonus.
     * @param operation      the arithmetic operation.
     * @param attributeValue the value to add.
     */
    void addToBonusAttribute(String stat, String bonusID, String operation, double attributeValue);

    /**
     * Sets a bonus attribute for the specified stat.
     *
     * @param stat           the stat name.
     * @param bonusID        the identifier for the bonus.
     * @param operation      the arithmetic operation.
     * @param attributeValue the value to set.
     */
    void setBonusAttribute(String stat, String bonusID, String operation, double attributeValue);

    /**
     * Retrieves a bonus attribute for the specified stat.
     *
     * @param stat    the stat name.
     * @param bonusID the identifier for the bonus.
     */
    void getBonusAttribute(String stat, String bonusID);

    /**
     * Removes the bonus attribute with the given identifier for the specified stat.
     *
     * @param stat    the stat name.
     * @param bonusID the bonus identifier.
     */
    void removeBonusAttribute(String stat, String bonusID);

    /**
     * Clears all bonus attributes for the specified stat.
     *
     * @param stat the stat name.
     */
    void clearBonusAttribute(String stat);

    /**
     * Retrieves a bonus attribute string based on an action.
     *
     * @param action  the action (get, remove, or clear).
     * @param stat    the stat name.
     * @param bonusID the bonus identifier.
     * @return the bonus attribute string.
     */
    String bonusAttribute(String action, String stat, String bonusID);

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
    String bonusAttribute(String action, String stat, String bonusID, String operation, double attributeValue, boolean endOfTheList);

    /**
     * Sets the release state.
     *
     * @param release the release value.
     */
    void setRelease(byte release);

    /**
     * Returns the current release state.
     *
     * @return the release value.
     */
    byte getRelease();

    /**
     * Sets the body value. Also HP.
     *
     * @param body the body value.
     */
    void setBody(int body);

    /**
     * Returns the body value. Also HP.
     *
     * @return the body.
     */
    int getBody();

    /**
     * Sets the HP value.
     *
     * @param hp the HP value.
     */
    void setHP(int hp);

    /**
     * Returns the HP value.
     *
     * @return the HP.
     */
    int getHP();

    /**
     * Sets the stamina value.
     *
     * @param stamina the stamina value.
     */
    void setStamina(int stamina);

    /**
     * Returns the stamina value.
     *
     * @return the stamina.
     */
    int getStamina();

    /**
     * Sets the Ki value.
     *
     * @param ki Ki value.
     */
    void setKi(int ki);

    /**
     * Returns the Ki value.
     *
     * @return Ki of Player.
     */
    int getKi();

    /**
     * Sets the TP value.
     *
     * @param tp Set TP Amount
     */
    void setTP(int tp);

    /**
     * Returns the TP value.
     *
     * @return Player TP Amount
     */
    int getTP();

    /**
     * Sets the gravity for the player.
     *
     * @param gravity the gravity value.
     */
    void setGravity(float gravity);

    /**
     * Returns the current gravity value.
     *
     * @return the gravity.
     */
    float getGravity();

    /**
     * Checks if the player is blocking.
     *
     * @return true if blocking; false otherwise.
     */
    boolean isBlocking();

    /**
     * Sets the hair code used to define the player's hairstyle.
     *
     * @param hairCode the hair code string.
     */
    void setHairCode(String hairCode);

    /**
     * Returns the player's hair code.
     *
     * @return the hair code string.
     */
    String getHairCode();

    /**
     * Sets the extra code used for additional customization.
     *
     * @param extraCode the extra code string.
     */
    void setExtraCode(String extraCode);

    /**
     * Returns the player's extra code.
     *
     * @return the extra code string.
     */
    String getExtraCode();

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
    void setItem(IItemStack itemStack, byte slot, boolean vanity);

    /**
     * Retrieves the item from the specified DBC inventory slot.
     *
     * @param slot   the slot index.
     * @param vanity whether the slot is in the vanity inventory.
     * @return the item stack, or null if empty.
     */
    IItemStack getItem(byte slot, boolean vanity);

    /**
     * Returns the entire DBC extra inventory.
     *
     * @return an array of item stacks.
     */
    IItemStack[] getInventory();

    /**
     * Sets the player's form.
     *
     * @param form the form value.
     */
    void setForm(byte form);

    /**
     * Returns the player's current form.
     *
     * @return the form value.
     */
    byte getForm();

    /**
     * Sets the player's secondary form.
     *
     * @param form2 the secondary form value.
     */
    void setForm2(byte form2);

    /**
     * Returns the player's secondary form.
     *
     * @return the secondary form value.
     */
    byte getForm2();

    /**
     * Returns the mastery value for a specific racial form.
     *
     * @param form the form index.
     * @return the mastery value.
     */
    double getRacialFormMastery(byte form);

    /**
     * Sets the mastery value for a specific racial form.
     *
     * @param form  the form index.
     * @param value the new mastery value.
     */
    void setRacialFormMastery(byte form, double value);

    /**
     * Adds to the mastery value for a specific racial form.
     *
     * @param form  the form index.
     * @param value the value to add.
     */
    void addRacialFormMastery(byte form, double value);

    /**
     * Returns the mastery value for a non-racial (other) form.
     *
     * @param formName the name of the form.
     * @return the mastery value.
     */
    double getOtherFormMastery(String formName);

    /**
     * Sets the mastery value for a non-racial (other) form.
     *
     * @param formName the name of the form.
     * @param value    the new mastery value.
     */
    void setOtherFormMastery(String formName, double value);

    /**
     * Adds to the mastery value for a non-racial (other) form.
     *
     * @param formName the name of the form.
     * @param value    the value to add.
     */
    void addOtherFormMastery(String formName, double value);

    /**
     * Sets the player's power points.
     *
     * @param points the power points.
     */
    void setPowerPoints(int points);

    /**
     * Returns the player's power points.
     *
     * @return the power points.
     */
    int getPowerPoints();

    /**
     * Sets the player's aura color.
     *
     * @param color the aura color.
     */
    void setAuraColor(int color);

    /**
     * Returns the player's aura color.
     *
     * @return the aura color.
     */
    int getAuraColor();

    /**
     * Sets the player's form level.
     *
     * @param level the form level.
     */
    void setFormLevel(int level);

    /**
     * Returns the player's form level.
     *
     * @return the form level.
     */
    int getFormLevel();

    /**
     * Sets the player's skills.
     *
     * @param skills a string representing the player's skills.
     */
    void setSkills(String skills);

    /**
     * Returns the player's skills.
     *
     * @return a string representing the player's skills.
     */
    String getSkills();

    /**
     * Sets the player's status effects.
     *
     * @param statusEffects a string representing status effects.
     */
    void setJRMCSE(String statusEffects);

    /**
     * Returns the player's status effects.
     *
     * @return a string representing the player's status effects.
     */
    String getJRMCSE();

    /**
     * Sets the player's race.
     *
     * @param race the race value.
     */
    void setRace(byte race);

    /**
     * Returns the player's race.
     *
     * @return the race value.
     */
    int getRace();

    /**
     * Sets the player's DBC class.
     *
     * @param dbcClass the DBC class value.
     */
    void setDBCClass(byte dbcClass);

    /**
     * Returns the player's DBC class.
     *
     * @return the DBC class.
     */
    byte getDBCClass();

    /**
     * Sets the player's power type.
     *
     * @param powerType the power type.
     */
    void setPowerType(byte powerType);

    /**
     * Returns the player's power type.
     *
     * @return the power type.
     */
    int getPowerType();

    /**
     * Returns the player's kill count for a given type.
     *
     * @param type the kill count type ("evil", "good", "neutral", or "all").
     * @return the kill count.
     */
    int getKillCount(String type);

    /**
     * Returns the fusion string for the player.
     *
     * @return the fusion string.
     */
    String getFusionString();
}
