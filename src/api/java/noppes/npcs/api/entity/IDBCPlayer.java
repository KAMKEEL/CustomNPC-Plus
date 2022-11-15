package noppes.npcs.api.entity;

import noppes.npcs.api.item.IItemStack;

public interface IDBCPlayer extends IPlayer {

    void setStat(String stat, int value);
    int getStat(String stat);

    void addBonusAttribute(String stat, String bonusID, String operation, double attributeValue);
    void addBonusAttribute(String stat, String bonusID, String operation, double attributeValue, boolean endOfTheList);
    void addToBonusAttribute(String stat, String bonusID, String operation, double attributeValue);
    void setBonusAttribute(String stat, String bonusID, String operation, double attributeValue);
    void getBonusAttribute(String stat, String bonusID);
    void removeBonusAttribute(String stat, String bonusID);
    void clearBonusAttribute(String stat);

    String bonusAttribute(String action, String stat, String bonusID);

    String bonusAttribute(String action, String stat, String bonusID, String operation, double attributeValue, boolean endOfTheList);

    void setRelease(byte release);
    byte getRelease();

    void setBody(int body);
    int getBody();

    void setStamina(int stamina);
    int getStamina();

    void setKi(int ki);
    int getKi();

    void setTP(int tp);
    int getTP();

    void setGravity(float gravity);
    float getGravity();

    boolean isBlocking();

    void setHairCode(String hairCode);
    String getHairCode();

    void setExtraCode(String extraCode);
    String getExtraCode();

    /**
     * @param slot The DBC extra inventory slot the item stack parameter will go into:
     *             0 - Weight
     *             1 - Body
     *             2 - Head
     *             3 - 4th Vanity Slot Down to the left
     *             4 - 3rd Vanity Slot Down to the left
     *             5 - 2nd Vanity Slot Down to the left
     *             6 - 1st Vanity Slot Down to the left
     *             7 - 4th Vanity Slot Down to the right
     *             8 - 3rd Vanity Slot Down to the right
     *             9 - 2nd Vanity Slot Down to the right
     *             10 - 1st Vanity Slot Down to the right
     * @param itemStack The item stack to be set into the slot. Set to null to remove the stack in that slot.
     */
    void setItem(IItemStack itemStack, byte slot, boolean vanity);
    IItemStack getItem(byte slot, boolean vanity);
    IItemStack[] getInventory();

    void setForm(byte form);
    byte getForm();
    void setForm2(byte form2);
    byte getForm2();

    void setPowerPoints(int points);
    int getPowerPoints();

    void setAuraColor(int color);
    int getAuraColor();

    void setFormLevel(int level);
    int getFormLevel();

    void setSkills(String skills);
    String getSkills();

    void setJRMCSE(String statusEffects);
    String getJRMCSE();

    void setRace(byte race);
    int getRace();

    void setDBCClass(byte dbcClass);
    byte getDBCClass();

    void setPowerType(byte powerType);
    int getPowerType();

    int getKillCount(String type);
}
