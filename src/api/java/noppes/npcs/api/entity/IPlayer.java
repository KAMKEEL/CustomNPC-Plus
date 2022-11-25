package noppes.npcs.api.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.api.*;
import noppes.npcs.api.handler.IOverlayHandler;
import noppes.npcs.api.handler.IPlayerData;
import noppes.npcs.api.handler.data.IDialog;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.api.gui.ICustomGui;
import noppes.npcs.api.handler.data.ISound;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.overlay.ICustomOverlay;

public interface IPlayer<T extends EntityPlayerMP> extends IEntityLivingBase<T> {
    /**
     * @return Returns the displayed name of the player
     */
    String getDisplayName();

    /**
     * @return Returns the players name
     */
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

    void showDialog(IDialog dialog);

    boolean hasReadDialog(IDialog dialog);

    void readDialog(IDialog dialog);

    void unreadDialog(IDialog dialog);

    void showDialog(int id);

    boolean hasReadDialog(int id);

    void readDialog(int id);

    void unreadDialog(int id);

    boolean hasFinishedQuest(IQuest quest);

    boolean hasActiveQuest(IQuest quest);

    /**
     * Add the quest from active quest list
     * @param quest The quest
     */
    void startQuest(IQuest quest);

    /**
     * Add the quest from finished quest list
     * @param quest The quest
     */
    void finishQuest(IQuest quest);
    /**
     * Removes the quest from active quest list
     * @param quest The quest
     */
    void stopQuest(IQuest quest);

    /**
     * Removes the quest from active and finished quest list
     * @param quest The quest
     */
    void removeQuest(IQuest quest);

    boolean hasFinishedQuest(int id);

    boolean hasActiveQuest(int id);

    /**
     * @param id The quest ID
     */
    void startQuest(int id);

    /**
     * @param id The quest ID
     */
    void finishQuest(int id);
    /**
     * @param id The quest ID
     */
    void stopQuest(int id);

    /**
     * @param id The quest ID
     */
    void removeQuest(int id);

    IQuest[] getFinishedQuests();

    int getType();

    boolean typeOf(int type);
    /**
     * @param faction The faction id
     * @param points The points to increase. Use negative values to decrease
     */
    void addFactionPoints(int faction, int points);

    /**
	 * @param faction The faction id
	 * @param points The new point value for this faction
	 */
	void setFactionPoints(int faction, int points);

    /**
     * @param faction The faction id
     * @return  points
     */
    int getFactionPoints(int faction);

    /**
     * @param message The message you want to send. Compatible with formatting codes, which can be found on the attached link.
     * @see <a href="https://static.wikia.nocookie.net/minecraft_gamepedia/images/7/7e/Minecraft_Formatting.gif/revision/latest/scale-to-width-down/200?cb=20200828001454">Minecraft formatting codes</a>
     *
     */
    void sendMessage(String message);

    /**
     * @return Return gamemode. 0: Survival, 1: Creative, 2: Adventure
     */
    int getMode();

    /**
     * @param type The gamemode type. 0:SURVIVAL, 1:CREATIVE, 2:ADVENTURE
     */
    void setMode(int type);

    /**
     * @since 1.7.10d
     * @return Returns a IItemStack array size 36
     */
    IItemStack[] getInventory();

    /**
     * @param item The item to be checked
     * @param ignoreNBT Whether the item's NBT tags will be checked to be equal
     * @param ignoreDamage Whether the item's damage will be checked to be equal
     * @return How many of this item the player has
     */
    int inventoryItemCount(IItemStack item, boolean ignoreNBT, boolean ignoreDamage);

    /**
     * @since 1.7.10c
     * @param id The items name
     * @param damage The damage value
     * @param amount How many will be removed
     * @return True if the items were removed succesfully. Returns false incase a bigger amount than what the player has was given or item doesnt exist
     */
    boolean removeItem(String id, int damage, int amount);

    /**
     * @param item The Item type to be removed
     * @param amount How many will be removed
     * @param ignoreNBT Whether the item's NBT tags will be checked to be equal
     * @param ignoreDamage Whether the item's damage will be checked to be equal
     * @return True if the items were removed succesfully. Returns false incase a bigger amount than what the player has was given
     */
    boolean removeItem(IItemStack item, int amount, boolean ignoreNBT, boolean ignoreDamage);

    /**
     * @param item The item to be removed from the players inventory
     * @param ignoreNBT Whether the item's NBT tags will be checked to be equal
     * @param ignoreDamage Whether the item's damage will be checked to be equal
     * @return The amount of item stacks that were removed
     */
    int removeAllItems(IItemStack item, boolean ignoreNBT, boolean ignoreDamage);

    /**
     * @since 1.7.10c
     * @param item Item to be added
     * @param amount The amount of the item to be added
     * @return Returns whether or not it gave the item succesfully
     */
    boolean giveItem(IItemStack item, int amount);

    /**
     * @since 1.7.10c
     * @param id The items name
     * @param damage The damage value
     * @param amount The amount of the item to be added
     * @return Returns whether or not it gave the item succesfully
     */
    boolean giveItem(String id, int damage, int amount);

    /**
     * Same as the /spawnpoint command
     * @param x The x position
     * @param y The y position
     * @param z The z position
     */
    void setSpawnpoint(int x, int y, int z);
    void setSpawnpoint(IPos pos);

    void resetSpawnpoint();

    void setRotation(float rotationYaw, float rotationPitch);

    void disableMouseInput(long time, int... buttonIds);

    void stopUsingItem();

    void clearItemInUse();

    public void clearInventory();

    void playSound(String name, float volume, float pitch);

    void playSound(int id, ISound sound);

    void stopSound(int id);

    void pauseSounds();

    void continueSounds();

    void stopSounds();

    void mountEntity(Entity ridingEntity);

    IEntity dropOneItem(boolean dropStack);

    boolean canHarvestBlock(IBlock block);

    boolean interactWith(IEntity entity);

    /**
     * @param achievement The achievement id. For a complete list see http://minecraft.gamepedia.com/Achievements
     * @return Returns whether or not the player has this achievement
     */
    boolean hasAchievement(String achievement);

    /**
     * @param permission Bukkit/Cauldron permission
     * @return Returns whether or not the player has the permission
     */
    boolean hasBukkitPermission(String permission);

    /**
     * @since 1.7.10c
     * @return Returns the exp level
     */
    int getExpLevel();

    /**
     * @since 1.7.10c
     * @param level The new exp level you want to set
     */
    void setExpLevel(int level);

    /**
     * Requires pixelmon to be installed
     * @since 1.7.10d
     */
    IPixelmonPlayerData getPixelmonData();

    ITimers getTimers();

    void updatePlayerInventory();

    IDBCPlayer getDBCPlayer();

    boolean blocking();

    public IPlayerData getData();

    boolean isScriptingDev();

    IQuest[] getActiveQuests();

    IContainer getOpenContainer();

    void showCustomGui(ICustomGui gui);

    ICustomGui getCustomGui();

    void closeGui();

    void showCustomOverlay(ICustomOverlay overlay);

    void closeOverlay(int id);

    IOverlayHandler getOverlays();
}
