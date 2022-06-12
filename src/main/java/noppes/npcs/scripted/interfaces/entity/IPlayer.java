package noppes.npcs.scripted.interfaces.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumChatFormatting;
import noppes.npcs.scripted.entity.ScriptDBCPlayer;
import noppes.npcs.scripted.interfaces.handler.IOverlayHandler;
import noppes.npcs.scripted.interfaces.handler.IPlayerData;
import noppes.npcs.scripted.interfaces.handler.data.IQuest;
import noppes.npcs.scripted.interfaces.*;
import noppes.npcs.scripted.interfaces.gui.ICustomGui;
import noppes.npcs.scripted.interfaces.item.IItemStack;
import noppes.npcs.scripted.interfaces.overlay.ICustomOverlay;
import noppes.npcs.scripted.ScriptPixelmonPlayerData;

public interface IPlayer<T extends EntityPlayerMP> extends IEntityLivingBase<T> {
    /**
     * @return Returns the displayed name of the player
     */
    String getDisplayName();

    /**
     * @return Returns the players name
     */
    String getName();

    void setPosition(double x, double y, double z);

    void setPosition(double x, double y, double z, int dimensionId);

    int getHunger();

    void setHunger(int hunger);

    float getSaturation();

    void setSaturation(float saturation);

    public boolean hasReadDialog(int id);

    public void readDialog(int id);

    public void unreadDialog(int id);

    boolean hasFinishedQuest(int id);

    boolean hasActiveQuest(int id);

    /**
     * Add the quest from active quest list
     * @param id The Quest ID
     */
    void startQuest(int id);

    /**
     * Add the quest from finished quest list
     * @param id The Quest ID
     */
    void finishQuest(int id);
    /**
     * Removes the quest from active quest list
     * @param id The Quest ID
     */
    void stopQuest(int id);

    /**
     * Removes the quest from active and finished quest list
     * @param id The Quest ID
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
     * @param message The message you want to send
     */
    void sendMessage(String message);

    void sendMessage(String message, EnumChatFormatting color, boolean bold, boolean italic, boolean underlined);

    void sendMessage(String message, EnumChatFormatting color, boolean bold, boolean italic, boolean obfuscated, boolean strikethrough, boolean underlined);

    /**
     * @return Return gamemode. 0: Survival, 1: Creative, 2: Adventure
     */
    int getMode();

    /**
     * @param type The gamemode type. 0:SURVIVAL, 1:CREATIVE, 2:ADVENTURE
     */
    void setMode(int type);

    /**
     * @param item The item to be checked
     * @return How many of this item the player has
     */
    int inventoryItemCount(IItemStack item);

    /**
     * @since 1.7.10d
     * @return Returns a IItemStack array size 36
     */
    IItemStack[] getInventory();

    /**
     * @param item The Item type to be removed
     * @param amount How many will be removed
     * @return Returns true if the items were removed succesfully. Returns false incase a bigger amount than what the player has was given
     */
    boolean removeItem(IItemStack item, int amount);

    /**
     * @since 1.7.10c
     * @param id The items name
     * @param damage The damage value
     * @param amount How many will be removed
     * @return Returns true if the items were removed succesfully. Returns false incase a bigger amount than what the player has was given or item doesnt exist
     */
    boolean removeItem(String id, int damage, int amount);

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

    void resetSpawnpoint();

    /**
     * @param item The item to be removed from the players inventory
     */
    void removeAllItems(IItemStack item);

    void setRotation(float rotationYaw, float rotationPitch);

    void stopUsingItem();

    void clearItemInUse();

    public void clearInventory();

    void playSound(String name, float volume, float pitch);

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
    ScriptPixelmonPlayerData getPixelmonData();

    IBlock getLookingAtBlock(int maxDistance);

    ITimers getTimers();

    void updatePlayerInventory();

    ScriptDBCPlayer<T> getDBCPlayer();

    boolean blocking();

    public IPlayerData getData();

    IQuest[] getActiveQuests();

    IContainer getOpenContainer();

    void showCustomGui(ICustomGui gui);

    ICustomGui getCustomGui();

    void closeGui();

    void showCustomOverlay(ICustomOverlay overlay);

    void closeOverlay(int id);

    IOverlayHandler getOverlays();
}
