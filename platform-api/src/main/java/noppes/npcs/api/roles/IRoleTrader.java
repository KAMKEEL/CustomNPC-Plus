package noppes.npcs.api.roles;

import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.item.IItemStack;

public interface IRoleTrader extends IRole {

    /**
     * @param slot      Slot number 0-17
     * @param currency  Currency item
     * @param currency2 Currency item number two
     * @param sold      Item to be sold by this npc
     */
    void setSellOption(int slot, IItemStack currency, IItemStack currency2, IItemStack sold);

    /**
     * @param slot     Slot number 0-17
     * @param currency Currency item
     * @param sold     Item to be sold by this npc
     */
    void setSellOption(int slot, IItemStack currency, IItemStack sold);

    /**
     * @param slot
     * @return The item being sold in this slot.
     */
    IItemStack getSellOption(int slot);

    /**
     * @param slot
     * @return a ScriptItemStack array of size 2 which contains the currency of this trade
     */
    IItemStack[] getCurrency(int slot);

    /**
     * @param slot Slot number 0-17
     */
    void removeSellOption(int slot);

    /**
     * @param name The trader Linked Market name
     */
    void setMarket(String name);

    /**
     * @return Get the currently set Linked Market name
     */
    String getMarket();

    /**
     * @param slot
     * @return the number of times an item has been sold on that slot
     */
    int getPurchaseNum(int slot);

    /**
     * @param slot
     * @param player
     * @return the number of times this player has purchased from this trader
     */
    int getPurchaseNum(int slot, IPlayer player);

    /**
     * Sets the purchase count of all slots to 0
     */
    void resetPurchaseNum();

    /**
     * sets the purchase num for that slot to 0
     *
     * @param slot
     */
    void resetPurchaseNum(int slot);

    /**
     * sets the purchase num for that slot and player to 0
     *
     * @param slot
     * @param player
     */
    void resetPurchaseNum(int slot, IPlayer player);

    /**
     * @param slot
     * @return if this slot is enabled
     */
    boolean isSlotEnabled(int slot);

    /**
     * @param slot
     * @param player
     * @return if this slot is enabled for this player
     */
    boolean isSlotEnabled(int slot, IPlayer player);

    /**
     * prevent an item from being sold on that slot
     *
     * @param slot
     */
    void disableSlot(int slot);

    /**
     * disables the slot for this player
     *
     * @param slot
     * @param player
     */
    void disableSlot(int slot, IPlayer player);

    /**
     * allow an item to be sold on that slot
     *
     * @param slot
     */
    void enableSlot(int slot);

    /**
     * enables the slot for this player
     *
     * @param slot
     * @param player
     */
    void enableSlot(int slot, IPlayer player);

    // ==================== Stock System ====================

    /**
     * @return Whether stock system is enabled
     */
    boolean isStockEnabled();

    /**
     * Enable or disable stock system
     */
    void setStockEnabled(boolean enabled);

    /**
     * @return Whether stock is tracked per-player (true) or globally (false)
     */
    boolean isPerPlayerStock();

    /**
     * Set whether stock is per-player or global
     */
    void setPerPlayerStock(boolean perPlayer);

    /**
     * Get the stock reset type
     *
     * @return 0=NONE, 1=MCDAILY, 2=MCWEEKLY, 3=MCCUSTOM, 4=RLDAILY, 5=RLWEEKLY, 6=RLCUSTOM
     */
    int getStockResetType();

    /**
     * Set stock reset type
     *
     * @param type 0=NONE, 1=MCDAILY, 2=MCWEEKLY, 3=MCCUSTOM, 4=RLDAILY, 5=RLWEEKLY, 6=RLCUSTOM
     */
    void setStockResetType(int type);

    /**
     * Get custom reset time (only used for MCCUSTOM/RLCUSTOM)
     *
     * @return Custom time in ticks (MC) or milliseconds (RL)
     */
    long getCustomResetTime();

    /**
     * Set custom reset time
     *
     * @param time Time in ticks (MC) or milliseconds (RL)
     */
    void setCustomResetTime(long time);

    /**
     * Get max stock for a slot
     *
     * @param slot Slot number 0-17
     * @return Max stock, -1 = unlimited
     */
    int getMaxStock(int slot);

    /**
     * Set max stock for a slot
     *
     * @param slot   Slot number 0-17
     * @param amount Max stock, -1 = unlimited
     */
    void setMaxStock(int slot, int amount);

    /**
     * Get available stock for a slot (global mode)
     *
     * @param slot Slot number 0-17
     * @return Available stock, Integer.MAX_VALUE if unlimited
     */
    int getAvailableStock(int slot);

    /**
     * Get available stock for a slot for a specific player (per-player mode)
     *
     * @param slot   Slot number 0-17
     * @param player The player
     * @return Available stock, Integer.MAX_VALUE if unlimited
     */
    int getAvailableStock(int slot, IPlayer player);

    /**
     * Reset all stock to max values
     */
    void resetStock();

    /**
     * Reset the cooldown timer without resetting stock values
     * Stock will reset on next trigger after this is called
     */
    void resetCooldown();

    /**
     * Get current stock for a slot (global mode)
     *
     * @param slot Slot number 0-17
     * @return Current stock, -1 if not initialized
     */
    int getCurrentStock(int slot);

    /**
     * Set current stock for a slot (global mode only)
     *
     * @param slot   Slot number 0-17
     * @param amount Current stock amount
     */
    void setCurrentStock(int slot, int amount);

    /**
     * Get purchased amount for a player in a slot (per-player mode)
     *
     * @param slot   Slot number 0-17
     * @param player The player
     * @return Purchased amount (stock available = maxStock - purchasedAmount)
     */
    int getPlayerPurchased(int slot, IPlayer player);

    /**
     * Set purchased amount for a player in a slot (per-player mode)
     *
     * @param slot   Slot number 0-17
     * @param player The player
     * @param amount Purchased amount
     */
    void setPlayerPurchased(int slot, IPlayer player, int amount);

    /**
     * Get the timestamp of the last stock reset
     *
     * @return Time in ticks (MC) or milliseconds (RL), depending on reset type
     */
    long getLastResetTime();

    /**
     * Get time remaining until next stock reset
     *
     * @return Time remaining in ticks (MC) or milliseconds (RL), -1 if no reset scheduled
     */
    long getTimeUntilReset();

    // ==================== Currency Cost System ====================

    /**
     * Get the currency cost for a slot (additive to item costs)
     *
     * @param slot Slot number 0-17
     * @return Currency cost, 0 = no cost
     */
    long getCurrencyCost(int slot);

    /**
     * Set the currency cost for a slot (additive to item costs)
     *
     * @param slot Slot number 0-17
     * @param cost Currency cost, 0 = no cost
     */
    void setCurrencyCost(int slot, long cost);

    /**
     * @param slot Slot number 0-17
     * @return Whether this slot has a currency cost &gt; 0
     */
    boolean hasCurrencyCost(int slot);
}
