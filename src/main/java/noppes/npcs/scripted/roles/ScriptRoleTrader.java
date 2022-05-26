package noppes.npcs.scripted.roles;

import foxz.utils.Market;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleTrader;
import noppes.npcs.scripted.ScriptItemStack;
import noppes.npcs.scripted.constants.RoleType;
import noppes.npcs.scripted.interfaces.IPlayer;

public class ScriptRoleTrader extends ScriptRoleInterface{
	private RoleTrader role;
	public ScriptRoleTrader(EntityNPCInterface npc) {
		super(npc);
		role = (RoleTrader) npc.roleInterface;
	}
	
	/**
	 * @param slot Slot number 0-17
	 * @param currency Currency item
	 * @param currency2 Currency item number two
	 * @param sold Item to be sold by this npc
	 */
	public void setSellOption(int slot, ScriptItemStack currency, ScriptItemStack currency2, ScriptItemStack sold){
		if(sold == null || slot >= 18 || slot < 0)
			return;
		if(currency == null)
			currency = currency2;

		if(currency != null)
			role.inventoryCurrency.items.put(slot, currency.getMCItemStack());
		else
			role.inventoryCurrency.items.remove(slot);
		if(currency2 != null)
			role.inventoryCurrency.items.put(slot + 18, currency2.getMCItemStack());
		else
			role.inventoryCurrency.items.remove(slot + 18);
		
		role.inventorySold.items.put(slot, sold.getMCItemStack());
	}

	/**
	 * @param slot Slot number 0-17
	 * @param currency Currency item
	 * @param sold Item to be sold by this npc
	 */
	public void setSellOption(int slot, ScriptItemStack currency, ScriptItemStack sold){
		setSellOption(slot, currency, null, sold);
	}
	
	/**
	 * @param slot
	 * @return The item being sold in this slot.
	 */
	public ScriptItemStack getSellOption(int slot) {
		if (slot >= 18 || slot < 0) return null;
		if (role.inventorySold.items.get(slot) == null) return null;
		return new ScriptItemStack(role.inventorySold.items.get(slot));
	}
	
	/**
	 * @param slot
	 * @return a ScriptItemStack array of size 2 which contains the currency of this trade
	 */
	public ScriptItemStack[] getCurrency(int slot) {
		if (slot >= 18 || slot < 0) return null;
		ScriptItemStack[] currency = new ScriptItemStack[2];
		if (role.inventoryCurrency.items.get(slot) != null) {
			currency[0] = new ScriptItemStack(role.inventoryCurrency.items.get(slot));
		}
		if (role.inventoryCurrency.items.get(slot + 18) != null) {
			currency[1] = new ScriptItemStack(role.inventoryCurrency.items.get(slot + 18));
		}
		return currency;
	}
	
	/**
	 * @param slot Slot number 0-17
	 */
	public void removeSellOption(int slot){
		if(slot >= 18 || slot < 0)
			return;
		role.inventoryCurrency.items.remove(slot);
		role.inventoryCurrency.items.remove(slot + 18);
		role.inventorySold.items.remove(slot);
	}
	/**
	 * @param name The trader Linked Market name
	 */
	public void setMarket(String name){
		role.marketName = name;
		Market.load(role, name);
	}
	
	/**
	 * @return Get the currently set Linked Market name
	 */
	public String getMarket(){
		return role.marketName;
	}
	
	/**
	 * @param slot
	 * @return the number of times an item has been sold on that slot
	 */
	public int getPurchaseNum(int slot) {
		if(slot >= 18 || slot < 0) return -1;
		return role.purchases[slot];
	}
	
	public int getPurchaseNum(int slot, IPlayer player) {
		// TODO get purchase num by player
		return -1;
	}
	
	/**
	 * Sets the purchase count of all slots to 0
	 */
	public void resetPurchaseNum() {
		for (int i = 0; i < role.purchases.length; ++i) role.purchases[i] = 0;
	}
	
	/**
	 * sets the purchase num for that slot to 0
	 * @param slot
	 */
	public void resetPurchaseNum(int slot) {
		role.purchases[slot] = 0;
	}
	
	/**
	 * prevent an item from being sold on that slot
	 * @param slot
	 */
	public void disableSlot(int slot) {
		if(slot >= 18 || slot < 0) return;
		role.disableSlot[slot] = 1;
	}
	
	/**
	 * allow an item to be sold on that slot
	 * @param slot
	 */
	public void enableSlot(int slot) {
		if(slot >= 18 || slot < 0) return;
		role.disableSlot[slot] = 0;
	}

	@Override
	public int getType(){
		return RoleType.TRADER;
	}
}
