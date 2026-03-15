package noppes.npcs.api.entity;

import noppes.npcs.api.*;
import noppes.npcs.api.gui.ICustomGui;
import noppes.npcs.api.handler.IActionManager;
import noppes.npcs.api.handler.IOverlayHandler;
import noppes.npcs.api.handler.IPlayerData;
import noppes.npcs.api.handler.data.*;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.overlay.ICustomOverlay;

/**
 * Represents a player in the game with methods for managing dialogs, quests,
 * inventory, sound, and more.
 */
public interface IPlayer extends IEntityLivingBase, IAnimatable {
    /**
     * @return Returns the displayed name of the player
     */
    String getDisplayName();

    /**
     * @return Returns the player's name
     */
    String getName();

    /**
     * Kicks the player from the server with the specified reason.
     *
     * @param reason The reason for kicking the player.
     */
    void kick(String reason);

    /**
     * Teleports the player to the specified coordinates in the current dimension.
     *
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @param z The z-coordinate.
     */
    void setPosition(double x, double y, double z);

    /**
     * Teleports the player to the coordinates specified by the given position.
     *
     * @param pos The position object containing coordinates.
     */
    void setPosition(IPos pos);

    /**
     * Teleports the player to the specified coordinates in the given dimension.
     *
     * @param x           The x-coordinate.
     * @param y           The y-coordinate.
     * @param z           The z-coordinate.
     * @param dimensionId The dimension ID.
     */
    void setPosition(double x, double y, double z, int dimensionId);

    /**
     * Teleports the player to the position in the specified dimension.
     *
     * @param pos         The position object containing coordinates.
     * @param dimensionId The dimension ID.
     */
    void setPosition(IPos pos, int dimensionId);

    /**
     * Teleports the player to the specified coordinates in the dimension of the provided world.
     *
     * @param x     The x-coordinate.
     * @param y     The y-coordinate.
     * @param z     The z-coordinate.
     * @param world The world whose dimension will be used.
     */
    void setPosition(double x, double y, double z, IWorld world);

    /**
     * Teleports the player to the position specified by the given position in the dimension of the provided world.
     *
     * @param pos   The position object containing coordinates.
     * @param world The world whose dimension will be used.
     */
    void setPosition(IPos pos, IWorld world);

    /**
     * Changes the player's dimension by teleporting them to their current position in the specified dimension.
     *
     * @param dimension The dimension ID to move the player to.
     */
    void setDimension(int dimension);

    /**
     * @return Returns the player's current hunger level.
     */
    int getHunger();

    /**
     * Sets the player's hunger level.
     *
     * @param hunger The new hunger level.
     */
    void setHunger(int hunger);

    /**
     * @return Returns the player's current saturation level.
     */
    float getSaturation();

    /**
     * Sets the player's saturation level.
     *
     * @param saturation The new saturation level.
     */
    void setSaturation(float saturation);

    /**
     * Displays the specified dialog to the player.
     *
     * @param dialog The dialog to display.
     */
    void showDialog(IDialog dialog);

    /**
     * Checks whether the player has read the specified dialog.
     *
     * @param dialog The dialog to check.
     * @return True if the dialog has been read, false otherwise.
     */
    boolean hasReadDialog(IDialog dialog);

    /**
     * Marks the specified dialog as read for the player.
     *
     * @param dialog The dialog to mark as read.
     */
    void readDialog(IDialog dialog);

    /**
     * Marks the specified dialog as unread for the player.
     *
     * @param dialog The dialog to mark as unread.
     */
    void unreadDialog(IDialog dialog);

    /**
     * Displays the dialog with the specified ID to the player.
     *
     * @param id The dialog ID.
     */
    void showDialog(int id);

    /**
     * Checks whether the player has read the dialog with the specified ID.
     *
     * @param id The dialog ID.
     * @return True if the dialog has been read, false otherwise.
     */
    boolean hasReadDialog(int id);

    /**
     * Marks the dialog with the specified ID as read for the player.
     *
     * @param id The dialog ID.
     */
    void readDialog(int id);

    /**
     * Marks the dialog with the specified ID as unread for the player.
     *
     * @param id The dialog ID.
     */
    void unreadDialog(int id);

    /**
     * Checks whether the player has finished the specified quest.
     *
     * @param quest The quest to check.
     * @return True if the quest has been finished, false otherwise.
     */
    boolean hasFinishedQuest(IQuest quest);

    /**
     * Checks whether the player has an active quest matching the specified quest.
     *
     * @param quest The quest to check.
     * @return True if the quest is active, false otherwise.
     */
    boolean hasActiveQuest(IQuest quest);

    /**
     * Add the quest from active quest list.
     *
     * @param quest The quest.
     */
    void startQuest(IQuest quest);

    /**
     * Add the quest from finished quest list.
     *
     * @param quest The quest.
     */
    void finishQuest(IQuest quest);

    /**
     * Removes the quest from active quest list.
     *
     * @param quest The quest.
     */
    void stopQuest(IQuest quest);

    /**
     * Removes the quest from active and finished quest list.
     *
     * @param quest The quest.
     */
    void removeQuest(IQuest quest);

    /**
     * Checks whether the player has finished the quest with the given ID.
     *
     * @param id The quest ID.
     * @return True if the quest has been finished, false otherwise.
     */
    boolean hasFinishedQuest(int id);

    /**
     * Checks whether the player has an active quest with the given ID.
     *
     * @param id The quest ID.
     * @return True if the quest is active, false otherwise.
     */
    boolean hasActiveQuest(int id);

    /**
     * @param id The quest ID.
     */
    void startQuest(int id);

    /**
     * @param id The quest ID.
     */
    void finishQuest(int id);

    /**
     * @param id The quest ID.
     */
    void stopQuest(int id);

    /**
     * @param id The quest ID.
     */
    void removeQuest(int id);

    /**
     * Returns an array of quests that the player has finished.
     *
     * @return An array of finished quests.
     */
    IQuest[] getFinishedQuests();

    /**
     * Returns the entity type identifier for the player.
     *
     * @return The type as an integer.
     */
    int getType();

    /**
     * Checks if the player is of the specified entity type.
     *
     * @param type The entity type to check.
     * @return True if the player is of the specified type, false otherwise.
     */
    boolean typeOf(int type);

    /**
     * @param faction The faction id.
     * @param points  The points to increase. Use negative values to decrease.
     */
    void addFactionPoints(int faction, int points);

    /**
     * @param faction The faction id.
     * @param points  The new point value for this faction.
     */
    void setFactionPoints(int faction, int points);

    /**
     * @param faction The faction id.
     * @return The current point total for the faction.
     */
    int getFactionPoints(int faction);

    /**
     * Sends a chat message to the player.
     *
     * @param message The message you want to send. Compatible with formatting codes.
     * @see <a href="https://static.wikia.nocookie.net/minecraft_gamepedia/images/7/7e/Minecraft_Formatting.gif/revision/latest/scale-to-width-down/200?cb=20200828001454">Minecraft formatting codes</a>
     */
    void sendMessage(String message);

    /**
     * @return Returns gamemode. 0: Survival, 1: Creative, 2: Adventure.
     */
    int getMode();

    /**
     * @param type The gamemode type. 0:SURVIVAL, 1:CREATIVE, 2:ADVENTURE.
     */
    void setMode(int type);

    /**
     * @return Returns an IItemStack array of size 36 representing the player's inventory.
     * @since 1.7.10d
     */
    IItemStack[] getInventory();

    /**
     * @param item         The item to be checked.
     * @param ignoreNBT    Whether the item's NBT tags will be checked for equality.
     * @param ignoreDamage Whether the item's damage will be checked for equality.
     * @return The total count of the specified item in the player's inventory.
     */
    int inventoryItemCount(IItemStack item, boolean ignoreNBT, boolean ignoreDamage);

    /**
     * @param id     The item's name.
     * @param damage The damage value.
     * @param amount How many items will be removed.
     * @return True if the items were removed successfully, false if the amount exceeds what the player has or the item doesn't exist.
     * @since 1.7.10c
     */
    boolean removeItem(String id, int damage, int amount);

    /**
     * @param item         The item type to be removed.
     * @param amount       The number of items to remove.
     * @param ignoreNBT    Whether the item's NBT tags will be checked for equality.
     * @param ignoreDamage Whether the item's damage will be checked for equality.
     * @return True if the items were removed successfully, false if the removal amount exceeds the player's count.
     */
    boolean removeItem(IItemStack item, int amount, boolean ignoreNBT, boolean ignoreDamage);

    /**
     * @param item         The item to be removed from the player's inventory.
     * @param ignoreNBT    Whether the item's NBT tags will be checked for equality.
     * @param ignoreDamage Whether the item's damage will be checked for equality.
     * @return The number of item stacks that were removed.
     */
    int removeAllItems(IItemStack item, boolean ignoreNBT, boolean ignoreDamage);

    /**
     * @param item   The item to be added.
     * @param amount The number of items to add.
     * @return True if the item was given successfully, false otherwise.
     * @since 1.7.10c
     */
    boolean giveItem(IItemStack item, int amount);

    /**
     * @param id     The item's name.
     * @param damage The damage value.
     * @param amount The number of items to add.
     * @return True if the item was given successfully, false otherwise.
     * @since 1.7.10c
     */
    boolean giveItem(String id, int damage, int amount);

    /**
     * Same as the /spawnpoint command.
     *
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @param z The z-coordinate.
     */
    void setSpawnpoint(int x, int y, int z);

    /**
     * Sets the player's spawnpoint to the coordinates of the provided position.
     *
     * @param pos The position containing the spawn coordinates.
     */
    void setSpawnpoint(IPos pos);

    /**
     * Resets the player's spawnpoint to the default spawn.
     */
    void resetSpawnpoint();

    /**
     * Sets the player's rotation.
     *
     * @param rotationYaw   The horizontal rotation (yaw).
     * @param rotationPitch The vertical rotation (pitch).
     */
    void setRotation(float rotationYaw, float rotationPitch);

    /**
     * Disables mouse input for the specified time and buttons for the player.
     *
     * @param time      The duration for which mouse input is disabled.
     * @param buttonIds The IDs of the mouse buttons to disable.
     */
    void disableMouseInput(long time, int... buttonIds);

    /**
     * Stops the player from using the currently active item.
     */
    void stopUsingItem();

    /**
     * Clears the currently in-use item.
     */
    void clearItemInUse();

    /**
     * Clears the player's entire inventory, including main and armor slots.
     */
    void clearInventory();

    /**
     * Plays the specified sound at the given volume and pitch for the player.
     *
     * @param name   The sound name.
     * @param volume The volume level.
     * @param pitch  The pitch level.
     */
    void playSound(String name, float volume, float pitch);

    /**
     * Plays the specified sound with an identifier for the player.
     *
     * @param id    The sound identifier.
     * @param sound The sound to play.
     */
    void playSound(int id, ISound sound);

    /**
     * Plays the specified sound for the player.
     *
     * @param sound The sound to play.
     */
    void playSound(ISound sound);

    /**
     * Stops the sound with the given identifier for the player.
     *
     * @param id The sound identifier.
     */
    void stopSound(int id);

    /**
     * Pauses all sounds currently playing for the player.
     */
    void pauseSounds();

    /**
     * Resumes paused sounds for the player.
     */
    void continueSounds();

    /**
     * Stops all sounds currently playing for the player.
     */
    void stopSounds();

    /**
     * Mounts the specified entity onto the player.
     *
     * @param ridingEntity The entity to mount.
     */
    void mountEntity(Object ridingEntity);

    /**
     * Drops one item from the player's inventory.
     *
     * @param dropStack If true, drops the entire stack; otherwise, drops a single item.
     * @return The dropped item as an IEntity.
     */
    IEntity dropOneItem(boolean dropStack);

    /**
     * Checks if the player can harvest the specified block.
     *
     * @param block The block to check.
     * @return True if the player can harvest the block, false otherwise.
     */
    boolean canHarvestBlock(IBlock block);

    /**
     * Interacts with the specified entity.
     *
     * @param entity The entity to interact with.
     * @return True if the interaction was successful, false otherwise.
     */
    boolean interactWith(IEntity entity);

    /**
     * @param achievement The achievement id. For a complete list see http://minecraft.wiki/w/Achievements
     * @return Returns whether or not the player has this achievement.
     */
    boolean hasAchievement(String achievement);

    /**
     * @param permission Bukkit/Cauldron permission.
     * @return Returns whether or not the player has the specified permission.
     */
    boolean hasBukkitPermission(String permission);

    /**
     * @return Returns the player's experience level.
     * @since 1.7.10c
     */
    int getExpLevel();

    /**
     * @param level The new experience level to set.
     * @since 1.7.10c
     */
    void setExpLevel(int level);

    /**
     * Requires Pixelmon to be installed.
     *
     * @return Returns the player's Pixelmon data, or null if Pixelmon is not enabled.
     * @since 1.7.10d
     */
    IPixelmonPlayerData getPixelmonData();

    /**
     * Returns the player's timers.
     *
     * @return The timers associated with the player.
     */
    ITimers getTimers();

    /**
     * Updates the player's inventory on the client side.
     */
    void updatePlayerInventory();

    /**
     * Returns the player's DBC (database) data, if available.
     *
     * @return The DBC data, or null if not applicable.
     */
    IDBCPlayer getDBCPlayer();

    /**
     * Checks if the player is currently blocking.
     *
     * @return True if blocking, false otherwise.
     */
    boolean blocking();

    /**
     * Returns the player's associated data (quest, faction, timers, etc.).
     *
     * @return The player's data.
     */
    IPlayerData getData();

    /**
     * Checks if the player is flagged as a scripting developer.
     *
     * @return True if the player is a scripting developer, false otherwise.
     */
    boolean isScriptingDev();

    /**
     * Returns an array of quests that the player is actively undertaking.
     *
     * @return An array of active quests.
     */
    IQuest[] getActiveQuests();

    /**
     * Returns the container (GUI) that the player currently has open, if any.
     *
     * @return The open container, or null if none is open.
     */
    IContainer getOpenContainer();

    /**
     * Displays a custom GUI to the player.
     *
     * @param gui The custom GUI to show.
     */
    void showCustomGui(ICustomGui gui);

    /**
     * Returns the custom GUI currently open for the player.
     *
     * @return The custom GUI, or null if none is open.
     */
    ICustomGui getCustomGui();

    /**
     * Closes the currently open GUI for the player.
     */
    void closeGui();

    /**
     * Displays a custom overlay on the player's screen.
     *
     * @param overlay The custom overlay to display.
     */
    void showCustomOverlay(ICustomOverlay overlay);

    /**
     * Closes the custom overlay with the specified identifier.
     *
     * @param id The overlay ID.
     */
    void closeOverlay(int id);

    /**
     * Returns the player's overlay handler which manages custom overlays.
     *
     * @return The overlay handler.
     */
    IOverlayHandler getOverlays();

    /**
     * Returns the player's animation data.
     *
     * @return The animation data.
     */
    IAnimationData getAnimationData();

    /**
     * Sets whether the player has conquered the End dimension.
     *
     * @param conqueredEnd True if the End is conquered, false otherwise.
     */
    void setConqueredEnd(boolean conqueredEnd);

    /**
     * Checks whether the player has conquered the End dimension.
     *
     * @return True if the End has been conquered, false otherwise.
     */
    boolean conqueredEnd();

    /**
     * Returns the player's screen size information.
     *
     * @return The screen size.
     */
    IScreenSize getScreenSize();

    /**
     * Returns the player's magic-related data.
     *
     * @return The magic data.
     */
    IMagicData getMagicData();

    /**
     * Returns the player's item attribute combination
     *
     * @return Player Attributes Data
     */
    IPlayerAttributes getAttributes();

    /**
     *
     * @return player's ActionManager
     */
    IActionManager getActionManager();


    /**
     * Returns the members of the player's current party, excluding the player.
     *
     * @return an array of party members, or an empty array if the player is not in a party.
     */
    IPlayer[] getPartyMembers();

    // =========================================
    // Currency Methods
    // =========================================

    /**
     * Gets the player's current currency balance.
     * If Vault is configured and available, returns Vault balance.
     * Otherwise returns CNPC+ built-in currency balance.
     *
     * @return The player's currency balance
     */
    long getCurrencyBalance();

    /**
     * Sets the player's currency balance.
     * If Vault is configured and available, sets Vault balance.
     * Otherwise sets CNPC+ built-in currency balance.
     *
     * @param amount The new balance
     */
    void setCurrencyBalance(long amount);

    /**
     * Deposits (adds) currency to the player's balance.
     * If Vault is configured and available, deposits to Vault.
     * Otherwise deposits to CNPC+ built-in currency.
     *
     * @param amount The amount to deposit
     * @return true if successful, false if would exceed max balance
     */
    boolean depositCurrency(long amount);

    /**
     * Withdraws (removes) currency from the player's balance.
     * If Vault is configured and available, withdraws from Vault.
     * Otherwise withdraws from CNPC+ built-in currency.
     *
     * @param amount The amount to withdraw
     * @return true if successful, false if insufficient funds
     */
    boolean withdrawCurrency(long amount);

    /**
     * Checks if the player can afford the specified amount.
     * If Vault is configured and available, checks Vault balance.
     * Otherwise checks CNPC+ built-in currency balance.
     *
     * @param amount The amount to check
     * @return true if the player has enough currency
     */
    boolean canAffordCurrency(long amount);

    /**
     * Checks if Vault is being used for currency operations.
     *
     * @return true if Vault is configured AND available, false if using CNPC+ built-in currency
     */
    boolean isUsingVaultCurrency();

    /**
     * Gets the player's formatted currency balance for display.
     * Uses Vault formatting if Vault is active, otherwise uses CNPC+ formatting.
     *
     * @return The formatted balance string
     */
    String getFormattedCurrencyBalance();

    /**
     * Returns all active energy projectiles fired by this player.
     *
     * @return Array of active energy projectiles, empty array if none
     */
    IEnergyProjectile[] getActiveEnergyProjectiles();
}
