package noppes.npcs.scripted.roles;

import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleTrader;
import noppes.npcs.scripted.ScriptItemStack;
import noppes.npcs.scripted.constants.RoleType;
import foxz.utils.Market;

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

	@Override
	public int getType(){
		return RoleType.TRADER;
	}
}
