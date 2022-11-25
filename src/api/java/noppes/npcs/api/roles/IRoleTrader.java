package noppes.npcs.api.roles;

import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.item.IItemStack;

public interface IRoleTrader extends IRole {

    /**
     * @param slot Slot number 0-17
     * @param currency Currency item
     * @param currency2 Currency item number two
     * @param sold Item to be sold by this npc
     */
    void setSellOption(int slot, IItemStack currency, IItemStack currency2, IItemStack sold);

    /**
     * @param slot Slot number 0-17
     * @param currency Currency item
     * @param sold Item to be sold by this npc
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
     * @param slot
     */
    void resetPurchaseNum(int slot);

    /**
     * sets the purchase num for that slot and player to 0
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
     * @param slot
     */
    void disableSlot(int slot);

    /**
     * disables the slot for this player
     * @param slot
     * @param player
     */
    void disableSlot(int slot, IPlayer player);

    /**
     * allow an item to be sold on that slot
     * @param slot
     */
    void enableSlot(int slot);

    /**
     * enables the slot for this player
     * @param slot
     * @param player
     */
    void enableSlot(int slot, IPlayer player);
}
