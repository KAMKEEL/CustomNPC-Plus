package noppes.npcs.api.entity;

import noppes.npcs.api.IPos;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.handler.data.IAnimationData;
import noppes.npcs.api.handler.data.IMagicData;
import noppes.npcs.api.item.IItemStack;

/**
 * Platform-api version (MC-free). The src/api shadow adds generic type parameter
 * and methods requiring MC-only types (IBlock, IContainer, ICustomGui, ISound,
 * IDialog, IQuest, ITimers, IPlayerData, etc.).
 */
public interface IPlayer extends IEntityLivingBase, IAnimatable {

    String getDisplayName();

    String getName();

    void kick(String reason);

    void setPosition(double x, double y, double z);

    void setPosition(IPos pos);

    void setPosition(double x, double y, double z, int dimensionId);

    void setPosition(IPos pos, int dimensionId);

    void setPosition(double x, double y, double z, IWorld world);

    void setPosition(IPos pos, IWorld world);

    void setDimension(int dimension);

    int getHunger();

    void setHunger(int hunger);

    float getSaturation();

    void setSaturation(float saturation);

    // Dialog methods (int-based — IDialog-based stay in shadow)

    void showDialog(int id);

    boolean hasReadDialog(int id);

    void readDialog(int id);

    void unreadDialog(int id);

    // Quest methods (int-based — IQuest-based stay in shadow)

    boolean hasFinishedQuest(int id);

    boolean hasActiveQuest(int id);

    void startQuest(int id);

    void finishQuest(int id);

    void stopQuest(int id);

    void removeQuest(int id);

    int getType();

    boolean typeOf(int type);

    void addFactionPoints(int faction, int points);

    void setFactionPoints(int faction, int points);

    int getFactionPoints(int faction);

    void sendMessage(String message);

    int getMode();

    void setMode(int type);

    IItemStack[] getInventory();

    int inventoryItemCount(IItemStack item, boolean ignoreNBT, boolean ignoreDamage);

    boolean removeItem(String id, int damage, int amount);

    boolean removeItem(IItemStack item, int amount, boolean ignoreNBT, boolean ignoreDamage);

    int removeAllItems(IItemStack item, boolean ignoreNBT, boolean ignoreDamage);

    boolean giveItem(IItemStack item, int amount);

    boolean giveItem(String id, int damage, int amount);

    void setSpawnpoint(int x, int y, int z);

    void setSpawnpoint(IPos pos);

    void resetSpawnpoint();

    void setRotation(float rotationYaw, float rotationPitch);

    void disableMouseInput(long time, int... buttonIds);

    void stopUsingItem();

    void clearItemInUse();

    void clearInventory();

    void playSound(String name, float volume, float pitch);

    // playSound(ISound) and playSound(int, ISound) stay in shadow — ISound not in platform-api

    void stopSound(int id);

    void pauseSounds();

    void continueSounds();

    void stopSounds();

    // mountEntity(Entity) stays in shadow — raw MC type

    IEntity dropOneItem(boolean dropStack);

    // canHarvestBlock(IBlock) stays in shadow — IBlock not in platform-api

    boolean interactWith(IEntity entity);

    boolean hasAchievement(String achievement);

    boolean hasBukkitPermission(String permission);

    int getExpLevel();

    void setExpLevel(int level);

    // getPixelmonData(), getTimers(), getDBCPlayer(), getData(),
    // getActionManager(), getAttributes(), getScreenSize(),
    // getOpenContainer(), showCustomGui(), getCustomGui(),
    // showCustomOverlay(), getOverlays()
    // all stay in shadow — types not in platform-api

    void updatePlayerInventory();

    boolean blocking();

    boolean isScriptingDev();

    void closeGui();

    void closeOverlay(int id);

    IAnimationData getAnimationData();

    void setConqueredEnd(boolean conqueredEnd);

    boolean conqueredEnd();

    IMagicData getMagicData();

    IPlayer[] getPartyMembers();

    // =========================================
    // Currency Methods
    // =========================================

    long getCurrencyBalance();

    void setCurrencyBalance(long amount);

    boolean depositCurrency(long amount);

    boolean withdrawCurrency(long amount);

    boolean canAffordCurrency(long amount);

    boolean isUsingVaultCurrency();

    String getFormattedCurrencyBalance();

    Object getMCEntity();
}
