/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity
 */

/**
 * Represents a player in the game with methods for managing dialogs, quests,
 * inventory, sound, and more.
  * @javaFqn noppes.npcs.api.entity.IPlayer
*/
export interface IPlayer<T extends EntityPlayerMP /* net.minecraft.entity.player.EntityPlayerMP */> extends import('./IEntityLivingBase').IEntityLivingBase {
    /**
     * @return Returns the displayed name of the player
     */
    getDisplayName(): String;
    /**
     * @return Returns the player's name
     */
    getName(): String;
    /**
     * Kicks the player from the server with the specified reason.
     *
     * @param reason The reason for kicking the player.
     */
    kick(reason: String): import('./void').void;
    /**
     * Teleports the player to the specified coordinates in the current dimension.
     *
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @param z The z-coordinate.
     */
    setPosition(x: import('./double').double, y: import('./double').double, z: import('./double').double): import('./void').void;
    /**
     * Teleports the player to the coordinates specified by the given position.
     *
     * @param pos The position object containing coordinates.
     */
    setPosition(pos: import('../IPos').IPos): import('./void').void;
    /**
     * Teleports the player to the specified coordinates in the given dimension.
     *
     * @param x           The x-coordinate.
     * @param y           The y-coordinate.
     * @param z           The z-coordinate.
     * @param dimensionId The dimension ID.
     */
    setPosition(x: import('./double').double, y: import('./double').double, z: import('./double').double, dimensionId: import('./int').int): import('./void').void;
    /**
     * Teleports the player to the position in the specified dimension.
     *
     * @param pos         The position object containing coordinates.
     * @param dimensionId The dimension ID.
     */
    setPosition(pos: import('../IPos').IPos, dimensionId: import('./int').int): import('./void').void;
    /**
     * Teleports the player to the specified coordinates in the dimension of the provided world.
     *
     * @param x     The x-coordinate.
     * @param y     The y-coordinate.
     * @param z     The z-coordinate.
     * @param world The world whose dimension will be used.
     */
    setPosition(x: import('./double').double, y: import('./double').double, z: import('./double').double, world: import('../IWorld').IWorld): import('./void').void;
    /**
     * Teleports the player to the position specified by the given position in the dimension of the provided world.
     *
     * @param pos   The position object containing coordinates.
     * @param world The world whose dimension will be used.
     */
    setPosition(pos: import('../IPos').IPos, world: import('../IWorld').IWorld): import('./void').void;
    /**
     * Changes the player's dimension by teleporting them to their current position in the specified dimension.
     *
     * @param dimension The dimension ID to move the player to.
     */
    setDimension(dimension: import('./int').int): import('./void').void;
    /**
     * @return Returns the player's current hunger level.
     */
    getHunger(): import('./int').int;
    /**
     * Sets the player's hunger level.
     *
     * @param hunger The new hunger level.
     */
    setHunger(hunger: import('./int').int): import('./void').void;
    /**
     * @return Returns the player's current saturation level.
     */
    getSaturation(): import('./float').float;
    /**
     * Sets the player's saturation level.
     *
     * @param saturation The new saturation level.
     */
    setSaturation(saturation: import('./float').float): import('./void').void;
    /**
     * Displays the specified dialog to the player.
     *
     * @param dialog The dialog to display.
     */
    showDialog(dialog: import('../handler/data/IDialog').IDialog): import('./void').void;
    /**
     * Checks whether the player has read the specified dialog.
     *
     * @param dialog The dialog to check.
     * @return True if the dialog has been read, false otherwise.
     */
    hasReadDialog(dialog: import('../handler/data/IDialog').IDialog): import('./boolean').boolean;
    /**
     * Marks the specified dialog as read for the player.
     *
     * @param dialog The dialog to mark as read.
     */
    readDialog(dialog: import('../handler/data/IDialog').IDialog): import('./void').void;
    /**
     * Marks the specified dialog as unread for the player.
     *
     * @param dialog The dialog to mark as unread.
     */
    unreadDialog(dialog: import('../handler/data/IDialog').IDialog): import('./void').void;
    /**
     * Displays the dialog with the specified ID to the player.
     *
     * @param id The dialog ID.
     */
    showDialog(id: import('./int').int): import('./void').void;
    /**
     * Checks whether the player has read the dialog with the specified ID.
     *
     * @param id The dialog ID.
     * @return True if the dialog has been read, false otherwise.
     */
    hasReadDialog(id: import('./int').int): import('./boolean').boolean;
    /**
     * Marks the dialog with the specified ID as read for the player.
     *
     * @param id The dialog ID.
     */
    readDialog(id: import('./int').int): import('./void').void;
    /**
     * Marks the dialog with the specified ID as unread for the player.
     *
     * @param id The dialog ID.
     */
    unreadDialog(id: import('./int').int): import('./void').void;
    /**
     * Checks whether the player has finished the specified quest.
     *
     * @param quest The quest to check.
     * @return True if the quest has been finished, false otherwise.
     */
    hasFinishedQuest(quest: import('../handler/data/IQuest').IQuest): import('./boolean').boolean;
    /**
     * Checks whether the player has an active quest matching the specified quest.
     *
     * @param quest The quest to check.
     * @return True if the quest is active, false otherwise.
     */
    hasActiveQuest(quest: import('../handler/data/IQuest').IQuest): import('./boolean').boolean;
    /**
     * Add the quest from active quest list.
     *
     * @param quest The quest.
     */
    startQuest(quest: import('../handler/data/IQuest').IQuest): import('./void').void;
    /**
     * Add the quest from finished quest list.
     *
     * @param quest The quest.
     */
    finishQuest(quest: import('../handler/data/IQuest').IQuest): import('./void').void;
    /**
     * Removes the quest from active quest list.
     *
     * @param quest The quest.
     */
    stopQuest(quest: import('../handler/data/IQuest').IQuest): import('./void').void;
    /**
     * Removes the quest from active and finished quest list.
     *
     * @param quest The quest.
     */
    removeQuest(quest: import('../handler/data/IQuest').IQuest): import('./void').void;
    /**
     * Checks whether the player has finished the quest with the given ID.
     *
     * @param id The quest ID.
     * @return True if the quest has been finished, false otherwise.
     */
    hasFinishedQuest(id: import('./int').int): import('./boolean').boolean;
    /**
     * Checks whether the player has an active quest with the given ID.
     *
     * @param id The quest ID.
     * @return True if the quest is active, false otherwise.
     */
    hasActiveQuest(id: import('./int').int): import('./boolean').boolean;
    /**
     * @param id The quest ID.
     */
    startQuest(id: import('./int').int): import('./void').void;
    /**
     * @param id The quest ID.
     */
    finishQuest(id: import('./int').int): import('./void').void;
    /**
     * @param id The quest ID.
     */
    stopQuest(id: import('./int').int): import('./void').void;
    /**
     * @param id The quest ID.
     */
    removeQuest(id: import('./int').int): import('./void').void;
    /**
     * Returns an array of quests that the player has finished.
     *
     * @return An array of finished quests.
     */
    getFinishedQuests(): import('../handler/data/IQuest').IQuest[];
    /**
     * Returns the entity type identifier for the player.
     *
     * @return The type as an integer.
     */
    getType(): import('./int').int;
    /**
     * Checks if the player is of the specified entity type.
     *
     * @param type The entity type to check.
     * @return True if the player is of the specified type, false otherwise.
     */
    typeOf(type: import('./int').int): import('./boolean').boolean;
    /**
     * @param faction The faction id.
     * @param points  The points to increase. Use negative values to decrease.
     */
    addFactionPoints(faction: import('./int').int, points: import('./int').int): import('./void').void;
    /**
     * @param faction The faction id.
     * @param points  The new point value for this faction.
     */
    setFactionPoints(faction: import('./int').int, points: import('./int').int): import('./void').void;
    /**
     * @param faction The faction id.
     * @return The current point total for the faction.
     */
    getFactionPoints(faction: import('./int').int): import('./int').int;
    /**
     * Sends a chat message to the player.
     *
     * @param message The message you want to send. Compatible with formatting codes.
     * @see <a href="https://static.wikia.nocookie.net/minecraft_gamepedia/images/7/7e/Minecraft_Formatting.gif/revision/latest/scale-to-width-down/200?cb=20200828001454">Minecraft formatting codes</a>
     */
    sendMessage(message: String): import('./void').void;
    /**
     * @return Returns gamemode. 0: Survival, 1: Creative, 2: Adventure.
     */
    getMode(): import('./int').int;
    /**
     * @param type The gamemode type. 0:SURVIVAL, 1:CREATIVE, 2:ADVENTURE.
     */
    setMode(type: import('./int').int): import('./void').void;
    /**
     * @return Returns an IItemStack array of size 36 representing the player's inventory.
     * @since 1.7.10d
     */
    getInventory(): import('../item/IItemStack').IItemStack[];
    /**
     * @param item         The item to be checked.
     * @param ignoreNBT    Whether the item's NBT tags will be checked for equality.
     * @param ignoreDamage Whether the item's damage will be checked for equality.
     * @return The total count of the specified item in the player's inventory.
     */
    inventoryItemCount(item: import('../item/IItemStack').IItemStack, ignoreNBT: import('./boolean').boolean, ignoreDamage: import('./boolean').boolean): import('./int').int;
    /**
     * @param id     The item's name.
     * @param damage The damage value.
     * @param amount How many items will be removed.
     * @return True if the items were removed successfully, false if the amount exceeds what the player has or the item doesn't exist.
     * @since 1.7.10c
     */
    removeItem(id: String, damage: import('./int').int, amount: import('./int').int): import('./boolean').boolean;
    /**
     * @param item         The item type to be removed.
     * @param amount       The number of items to remove.
     * @param ignoreNBT    Whether the item's NBT tags will be checked for equality.
     * @param ignoreDamage Whether the item's damage will be checked for equality.
     * @return True if the items were removed successfully, false if the removal amount exceeds the player's count.
     */
    removeItem(item: import('../item/IItemStack').IItemStack, amount: import('./int').int, ignoreNBT: import('./boolean').boolean, ignoreDamage: import('./boolean').boolean): import('./boolean').boolean;
    /**
     * @param item         The item to be removed from the player's inventory.
     * @param ignoreNBT    Whether the item's NBT tags will be checked for equality.
     * @param ignoreDamage Whether the item's damage will be checked for equality.
     * @return The number of item stacks that were removed.
     */
    removeAllItems(item: import('../item/IItemStack').IItemStack, ignoreNBT: import('./boolean').boolean, ignoreDamage: import('./boolean').boolean): import('./int').int;
    /**
     * @param item   The item to be added.
     * @param amount The number of items to add.
     * @return True if the item was given successfully, false otherwise.
     * @since 1.7.10c
     */
    giveItem(item: import('../item/IItemStack').IItemStack, amount: import('./int').int): import('./boolean').boolean;
    /**
     * @param id     The item's name.
     * @param damage The damage value.
     * @param amount The number of items to add.
     * @return True if the item was given successfully, false otherwise.
     * @since 1.7.10c
     */
    giveItem(id: String, damage: import('./int').int, amount: import('./int').int): import('./boolean').boolean;
    /**
     * Same as the /spawnpoint command.
     *
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @param z The z-coordinate.
     */
    setSpawnpoint(x: import('./int').int, y: import('./int').int, z: import('./int').int): import('./void').void;
    /**
     * Sets the player's spawnpoint to the coordinates of the provided position.
     *
     * @param pos The position containing the spawn coordinates.
     */
    setSpawnpoint(pos: import('../IPos').IPos): import('./void').void;
    /**
     * Resets the player's spawnpoint to the default spawn.
     */
    resetSpawnpoint(): import('./void').void;
    /**
     * Sets the player's rotation.
     *
     * @param rotationYaw   The horizontal rotation (yaw).
     * @param rotationPitch The vertical rotation (pitch).
     */
    setRotation(rotationYaw: import('./float').float, rotationPitch: import('./float').float): import('./void').void;
    /**
     * Disables mouse input for the specified time and buttons for the player.
     *
     * @param time      The duration for which mouse input is disabled.
     * @param buttonIds The IDs of the mouse buttons to disable.
     */
    disableMouseInput(time: import('./long').long, ...buttonIds: import('./int').int[]): import('./void').void;
    /**
     * Stops the player from using the currently active item.
     */
    stopUsingItem(): import('./void').void;
    /**
     * Clears the currently in-use item.
     */
    clearItemInUse(): import('./void').void;
    /**
     * Clears the player's entire inventory, including main and armor slots.
     */
    clearInventory(): import('./void').void;
    /**
     * Plays the specified sound at the given volume and pitch for the player.
     *
     * @param name   The sound name.
     * @param volume The volume level.
     * @param pitch  The pitch level.
     */
    playSound(name: String, volume: import('./float').float, pitch: import('./float').float): import('./void').void;
    /**
     * Plays the specified sound with an identifier for the player.
     *
     * @param id    The sound identifier.
     * @param sound The sound to play.
     */
    playSound(id: import('./int').int, sound: import('../handler/data/ISound').ISound): import('./void').void;
    /**
     * Plays the specified sound for the player.
     *
     * @param sound The sound to play.
     */
    playSound(sound: import('../handler/data/ISound').ISound): import('./void').void;
    /**
     * Stops the sound with the given identifier for the player.
     *
     * @param id The sound identifier.
     */
    stopSound(id: import('./int').int): import('./void').void;
    /**
     * Pauses all sounds currently playing for the player.
     */
    pauseSounds(): import('./void').void;
    /**
     * Resumes paused sounds for the player.
     */
    continueSounds(): import('./void').void;
    /**
     * Stops all sounds currently playing for the player.
     */
    stopSounds(): import('./void').void;
    /**
     * Mounts the specified entity onto the player.
     *
     * @param ridingEntity The entity to mount.
     */
    mountEntity(ridingEntity: import('../../../../net/minecraft/entity/Entity').Entity): import('./void').void;
    /**
     * Drops one item from the player's inventory.
     *
     * @param dropStack If true, drops the entire stack; otherwise, drops a single item.
     * @return The dropped item as an IEntity.
     */
    dropOneItem(dropStack: import('./boolean').boolean): import('./IEntity').IEntity;
    /**
     * Checks if the player can harvest the specified block.
     *
     * @param block The block to check.
     * @return True if the player can harvest the block, false otherwise.
     */
    canHarvestBlock(block: import('../IBlock').IBlock): import('./boolean').boolean;
    /**
     * Interacts with the specified entity.
     *
     * @param entity The entity to interact with.
     * @return True if the interaction was successful, false otherwise.
     */
    interactWith(entity: import('./IEntity').IEntity): import('./boolean').boolean;
    /**
     * @param achievement The achievement id. For a complete list see http://minecraft.wiki/w/Achievements
     * @return Returns whether or not the player has this achievement.
     */
    hasAchievement(achievement: String): import('./boolean').boolean;
    /**
     * @param permission Bukkit/Cauldron permission.
     * @return Returns whether or not the player has the specified permission.
     */
    hasBukkitPermission(permission: String): import('./boolean').boolean;
    /**
     * @return Returns the player's experience level.
     * @since 1.7.10c
     */
    getExpLevel(): import('./int').int;
    /**
     * @param level The new experience level to set.
     * @since 1.7.10c
     */
    setExpLevel(level: import('./int').int): import('./void').void;
    /**
     * Requires Pixelmon to be installed.
     *
     * @return Returns the player's Pixelmon data, or null if Pixelmon is not enabled.
     * @since 1.7.10d
     */
    getPixelmonData(): import('../IPixelmonPlayerData').IPixelmonPlayerData;
    /**
     * Returns the player's timers.
     *
     * @return The timers associated with the player.
     */
    getTimers(): import('../ITimers').ITimers;
    /**
     * Updates the player's inventory on the client side.
     */
    updatePlayerInventory(): import('./void').void;
    /**
     * Returns the player's DBC (database) data, if available.
     *
     * @return The DBC data, or null if not applicable.
     */
    getDBCPlayer(): import('./IDBCPlayer').IDBCPlayer;
    /**
     * Checks if the player is currently blocking.
     *
     * @return True if blocking, false otherwise.
     */
    blocking(): import('./boolean').boolean;
    /**
     * Returns the player's associated data (quest, faction, timers, etc.).
     *
     * @return The player's data.
     */
    getData(): import('../handler/IPlayerData').IPlayerData;
    /**
     * Checks if the player is flagged as a scripting developer.
     *
     * @return True if the player is a scripting developer, false otherwise.
     */
    isScriptingDev(): import('./boolean').boolean;
    /**
     * Returns an array of quests that the player is actively undertaking.
     *
     * @return An array of active quests.
     */
    getActiveQuests(): import('../handler/data/IQuest').IQuest[];
    /**
     * Returns the container (GUI) that the player currently has open, if any.
     *
     * @return The open container, or null if none is open.
     */
    getOpenContainer(): import('../IContainer').IContainer;
    /**
     * Displays a custom GUI to the player.
     *
     * @param gui The custom GUI to show.
     */
    showCustomGui(gui: import('../gui/ICustomGui').ICustomGui): import('./void').void;
    /**
     * Returns the custom GUI currently open for the player.
     *
     * @return The custom GUI, or null if none is open.
     */
    getCustomGui(): import('../gui/ICustomGui').ICustomGui;
    /**
     * Closes the currently open GUI for the player.
     */
    closeGui(): import('./void').void;
    /**
     * Displays a custom overlay on the player's screen.
     *
     * @param overlay The custom overlay to display.
     */
    showCustomOverlay(overlay: import('../overlay/ICustomOverlay').ICustomOverlay): import('./void').void;
    /**
     * Closes the custom overlay with the specified identifier.
     *
     * @param id The overlay ID.
     */
    closeOverlay(id: import('./int').int): import('./void').void;
    /**
     * Returns the player's overlay handler which manages custom overlays.
     *
     * @return The overlay handler.
     */
    getOverlays(): import('../handler/IOverlayHandler').IOverlayHandler;
    /**
     * Returns the player's animation data.
     *
     * @return The animation data.
     */
    getAnimationData(): import('../handler/data/IAnimationData').IAnimationData;
    /**
     * Sets whether the player has conquered the End dimension.
     *
     * @param conqueredEnd True if the End is conquered, false otherwise.
     */
    setConqueredEnd(conqueredEnd: import('./boolean').boolean): import('./void').void;
    /**
     * Checks whether the player has conquered the End dimension.
     *
     * @return True if the End has been conquered, false otherwise.
     */
    conqueredEnd(): import('./boolean').boolean;
    /**
     * Returns the player's screen size information.
     *
     * @return The screen size.
     */
    getScreenSize(): import('../IScreenSize').IScreenSize;
    /**
     * Returns the player's magic-related data.
     *
     * @return The magic data.
     */
    getMagicData(): import('../handler/data/IMagicData').IMagicData;
    /**
     * Returns the player's item attribute combination
     *
     * @return Player Attributes Data
     */
    getAttributes(): import('../handler/data/IPlayerAttributes').IPlayerAttributes;
    /**
     *
     * @return player's ActionManager
     */
    getActionManager(): import('../handler/IActionManager').IActionManager;
    /**
     * Returns the members of the player's current party, excluding the player.
     *
     * @return an array of party members, or an empty array if the player is not in a party.
     */
    getPartyMembers(): import('./IPlayer').IPlayer[];
    /**
     * Gets the player's current currency balance.
     * If Vault is configured and available, returns Vault balance.
     * Otherwise returns CNPC+ built-in currency balance.
     *
     * @return The player's currency balance
     */
    getCurrencyBalance(): import('./long').long;
    /**
     * Sets the player's currency balance.
     * If Vault is configured and available, sets Vault balance.
     * Otherwise sets CNPC+ built-in currency balance.
     *
     * @param amount The new balance
     */
    setCurrencyBalance(amount: import('./long').long): import('./void').void;
    /**
     * Deposits (adds) currency to the player's balance.
     * If Vault is configured and available, deposits to Vault.
     * Otherwise deposits to CNPC+ built-in currency.
     *
     * @param amount The amount to deposit
     * @return true if successful, false if would exceed max balance
     */
    depositCurrency(amount: import('./long').long): import('./boolean').boolean;
    /**
     * Withdraws (removes) currency from the player's balance.
     * If Vault is configured and available, withdraws from Vault.
     * Otherwise withdraws from CNPC+ built-in currency.
     *
     * @param amount The amount to withdraw
     * @return true if successful, false if insufficient funds
     */
    withdrawCurrency(amount: import('./long').long): import('./boolean').boolean;
    /**
     * Checks if the player can afford the specified amount.
     * If Vault is configured and available, checks Vault balance.
     * Otherwise checks CNPC+ built-in currency balance.
     *
     * @param amount The amount to check
     * @return true if the player has enough currency
     */
    canAffordCurrency(amount: import('./long').long): import('./boolean').boolean;
    /**
     * Checks if Vault is being used for currency operations.
     *
     * @return true if Vault is configured AND available, false if using CNPC+ built-in currency
     */
    isUsingVaultCurrency(): import('./boolean').boolean;
    /**
     * Gets the player's formatted currency balance for display.
     * Uses Vault formatting if Vault is active, otherwise uses CNPC+ formatting.
     *
     * @return The formatted balance string
     */
    getFormattedCurrencyBalance(): String;
    /**
     * Returns all active energy projectiles fired by this player.
     *
     * @return Array of active energy projectiles, empty array if none
     */
    getActiveEnergyProjectiles(): import('./IEnergyProjectile').IEnergyProjectile[];
}
