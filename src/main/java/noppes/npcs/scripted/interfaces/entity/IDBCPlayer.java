package noppes.npcs.scripted.interfaces.entity;

import noppes.npcs.scripted.interfaces.item.IItemStack;

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
