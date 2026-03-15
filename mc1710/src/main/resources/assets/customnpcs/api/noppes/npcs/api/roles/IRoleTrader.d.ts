/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.roles
 */

/**
 * @javaFqn noppes.npcs.api.roles.IRoleTrader
 */
export interface IRoleTrader extends import('./IRole').IRole {
    /**
     * @param slot      Slot number 0-17
     * @param currency  Currency item
     * @param currency2 Currency item number two
     * @param sold      Item to be sold by this npc
     */
    setSellOption(slot: import('./int').int, currency: import('../item/IItemStack').IItemStack, currency2: import('../item/IItemStack').IItemStack, sold: import('../item/IItemStack').IItemStack): import('./void').void;
    /**
     * @param slot     Slot number 0-17
     * @param currency Currency item
     * @param sold     Item to be sold by this npc
     */
    setSellOption(slot: import('./int').int, currency: import('../item/IItemStack').IItemStack, sold: import('../item/IItemStack').IItemStack): import('./void').void;
    /**
     * @param slot
     * @return The item being sold in this slot.
     */
    getSellOption(slot: import('./int').int): import('../item/IItemStack').IItemStack;
    /**
     * @param slot
     * @return a ScriptItemStack array of size 2 which contains the currency of this trade
     */
    getCurrency(slot: import('./int').int): import('../item/IItemStack').IItemStack[];
    /**
     * @param slot Slot number 0-17
     */
    removeSellOption(slot: import('./int').int): import('./void').void;
    /**
     * @param name The trader Linked Market name
     */
    setMarket(name: String): import('./void').void;
    /**
     * @return Get the currently set Linked Market name
     */
    getMarket(): String;
    /**
     * @param slot
     * @return the number of times an item has been sold on that slot
     */
    getPurchaseNum(slot: import('./int').int): import('./int').int;
    /**
     * @param slot
     * @param player
     * @return the number of times this player has purchased from this trader
     */
    getPurchaseNum(slot: import('./int').int, player: import('../entity/IPlayer').IPlayer): import('./int').int;
    /**
     * Sets the purchase count of all slots to 0
     */
    resetPurchaseNum(): import('./void').void;
    /**
     * sets the purchase num for that slot to 0
     *
     * @param slot
     */
    resetPurchaseNum(slot: import('./int').int): import('./void').void;
    /**
     * sets the purchase num for that slot and player to 0
     *
     * @param slot
     * @param player
     */
    resetPurchaseNum(slot: import('./int').int, player: import('../entity/IPlayer').IPlayer): import('./void').void;
    /**
     * @param slot
     * @return if this slot is enabled
     */
    isSlotEnabled(slot: import('./int').int): import('./boolean').boolean;
    /**
     * @param slot
     * @param player
     * @return if this slot is enabled for this player
     */
    isSlotEnabled(slot: import('./int').int, player: import('../entity/IPlayer').IPlayer): import('./boolean').boolean;
    /**
     * prevent an item from being sold on that slot
     *
     * @param slot
     */
    disableSlot(slot: import('./int').int): import('./void').void;
    /**
     * disables the slot for this player
     *
     * @param slot
     * @param player
     */
    disableSlot(slot: import('./int').int, player: import('../entity/IPlayer').IPlayer): import('./void').void;
    /**
     * allow an item to be sold on that slot
     *
     * @param slot
     */
    enableSlot(slot: import('./int').int): import('./void').void;
    /**
     * enables the slot for this player
     *
     * @param slot
     * @param player
     */
    enableSlot(slot: import('./int').int, player: import('../entity/IPlayer').IPlayer): import('./void').void;
    /**
     * @return Whether stock system is enabled
     */
    isStockEnabled(): import('./boolean').boolean;
    /**
     * Enable or disable stock system
     */
    setStockEnabled(enabled: import('./boolean').boolean): import('./void').void;
    /**
     * @return Whether stock is tracked per-player (true) or globally (false)
     */
    isPerPlayerStock(): import('./boolean').boolean;
    /**
     * Set whether stock is per-player or global
     */
    setPerPlayerStock(perPlayer: import('./boolean').boolean): import('./void').void;
    /**
     * Get the stock reset type
     *
     * @return 0=NONE, 1=MCDAILY, 2=MCWEEKLY, 3=MCCUSTOM, 4=RLDAILY, 5=RLWEEKLY, 6=RLCUSTOM
     */
    getStockResetType(): import('./int').int;
    /**
     * Set stock reset type
     *
     * @param type 0=NONE, 1=MCDAILY, 2=MCWEEKLY, 3=MCCUSTOM, 4=RLDAILY, 5=RLWEEKLY, 6=RLCUSTOM
     */
    setStockResetType(type: import('./int').int): import('./void').void;
    /**
     * Get custom reset time (only used for MCCUSTOM/RLCUSTOM)
     *
     * @return Custom time in ticks (MC) or milliseconds (RL)
     */
    getCustomResetTime(): import('./long').long;
    /**
     * Set custom reset time
     *
     * @param time Time in ticks (MC) or milliseconds (RL)
     */
    setCustomResetTime(time: import('./long').long): import('./void').void;
    /**
     * Get max stock for a slot
     *
     * @param slot Slot number 0-17
     * @return Max stock, -1 = unlimited
     */
    getMaxStock(slot: import('./int').int): import('./int').int;
    /**
     * Set max stock for a slot
     *
     * @param slot   Slot number 0-17
     * @param amount Max stock, -1 = unlimited
     */
    setMaxStock(slot: import('./int').int, amount: import('./int').int): import('./void').void;
    /**
     * Get available stock for a slot (global mode)
     *
     * @param slot Slot number 0-17
     * @return Available stock, Integer.MAX_VALUE if unlimited
     */
    getAvailableStock(slot: import('./int').int): import('./int').int;
    /**
     * Get available stock for a slot for a specific player (per-player mode)
     *
     * @param slot   Slot number 0-17
     * @param player The player
     * @return Available stock, Integer.MAX_VALUE if unlimited
     */
    getAvailableStock(slot: import('./int').int, player: import('../entity/IPlayer').IPlayer): import('./int').int;
    /**
     * Reset all stock to max values
     */
    resetStock(): import('./void').void;
    /**
     * Reset the cooldown timer without resetting stock values
     * Stock will reset on next trigger after this is called
     */
    resetCooldown(): import('./void').void;
    /**
     * Get current stock for a slot (global mode)
     *
     * @param slot Slot number 0-17
     * @return Current stock, -1 if not initialized
     */
    getCurrentStock(slot: import('./int').int): import('./int').int;
    /**
     * Set current stock for a slot (global mode only)
     *
     * @param slot   Slot number 0-17
     * @param amount Current stock amount
     */
    setCurrentStock(slot: import('./int').int, amount: import('./int').int): import('./void').void;
    /**
     * Get purchased amount for a player in a slot (per-player mode)
     *
     * @param slot   Slot number 0-17
     * @param player The player
     * @return Purchased amount (stock available = maxStock - purchasedAmount)
     */
    getPlayerPurchased(slot: import('./int').int, player: import('../entity/IPlayer').IPlayer): import('./int').int;
    /**
     * Set purchased amount for a player in a slot (per-player mode)
     *
     * @param slot   Slot number 0-17
     * @param player The player
     * @param amount Purchased amount
     */
    setPlayerPurchased(slot: import('./int').int, player: import('../entity/IPlayer').IPlayer, amount: import('./int').int): import('./void').void;
    /**
     * Get the timestamp of the last stock reset
     *
     * @return Time in ticks (MC) or milliseconds (RL), depending on reset type
     */
    getLastResetTime(): import('./long').long;
    /**
     * Get time remaining until next stock reset
     *
     * @return Time remaining in ticks (MC) or milliseconds (RL), -1 if no reset scheduled
     */
    getTimeUntilReset(): import('./long').long;
    /**
     * Get the currency cost for a slot (additive to item costs)
     *
     * @param slot Slot number 0-17
     * @return Currency cost, 0 = no cost
     */
    getCurrencyCost(slot: import('./int').int): import('./long').long;
    /**
     * Set the currency cost for a slot (additive to item costs)
     *
     * @param slot Slot number 0-17
     * @param cost Currency cost, 0 = no cost
     */
    setCurrencyCost(slot: import('./int').int, cost: import('./long').long): import('./void').void;
    /**
     * @param slot Slot number 0-17
     * @return Whether this slot has a currency cost &gt; 0
     */
    hasCurrencyCost(slot: import('./int').int): import('./boolean').boolean;
}
