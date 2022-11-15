package noppes.npcs.scripted.roles;

import foxz.utils.Market;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleTrader;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.scripted.constants.RoleType;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.roles.IRoleTrader;

public class ScriptRoleTrader extends ScriptRoleInterface implements IRoleTrader {
	private RoleTrader role;
	public ScriptRoleTrader(EntityNPCInterface npc) {
		super(npc);
		role = (RoleTrader) npc.roleInterface;
	}

	public void setSellOption(int slot, IItemStack currency, IItemStack currency2, IItemStack sold){
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

	public void setSellOption(int slot, IItemStack currency, IItemStack sold){
		setSellOption(slot, currency, null, sold);
	}

	public IItemStack getSellOption(int slot) {
		if (slot >= 18 || slot < 0) return null;
		if (role.inventorySold.items.get(slot) == null) return null;
		return NpcAPI.Instance().getIItemStack(role.inventoryCurrency.items.get(slot));
	}

	public IItemStack[] getCurrency(int slot) {
		if (slot >= 18 || slot < 0) return null;
		IItemStack[] currency = new IItemStack[2];
		if (role.inventoryCurrency.items.get(slot) != null) {
			currency[0] = NpcAPI.Instance().getIItemStack(role.inventoryCurrency.items.get(slot));
		}
		if (role.inventoryCurrency.items.get(slot + 18) != null) {
			currency[1] = NpcAPI.Instance().getIItemStack(role.inventoryCurrency.items.get(slot + 18));
		}
		return currency;
	}
	

	public void removeSellOption(int slot){
		if(slot >= 18 || slot < 0) return;
		role.inventoryCurrency.items.remove(slot);
		role.inventoryCurrency.items.remove(slot + 18);
		role.inventorySold.items.remove(slot);
	}

	public void setMarket(String name){
		role.marketName = name;
		Market.load(role, name);
	}

	public String getMarket(){
		return role.marketName;
	}

	public int getPurchaseNum(int slot) {
		if(slot >= 18 || slot < 0) return -1;
		return role.purchases[slot];
	}

	public int getPurchaseNum(int slot, IPlayer player) {
		if(slot >= 18 || slot < 0) return -1;
		return role.getArrayByName(player.getDisplayName(), role.playerPurchases)[slot];
	}

	public void resetPurchaseNum() {
		for (int i = 0; i < role.purchases.length; ++i) role.purchases[i] = 0;
	}

	public void resetPurchaseNum(int slot) {
		if(slot >= 18 || slot < 0) return;
		role.purchases[slot] = 0;
	}

	public void resetPurchaseNum(int slot, IPlayer player) {
		if(slot >= 18 || slot < 0) return;
		role.getArrayByName(player.getDisplayName(), role.playerPurchases)[slot] = 0;
	}

	public boolean isSlotEnabled(int slot) {
		if(slot >= 18 || slot < 0) return false;
		return role.disableSlot[slot] > 0;
	}

	public boolean isSlotEnabled(int slot, IPlayer player) {
		if(slot >= 18 || slot < 0) return false;
		return role.isSlotEnabled(slot, player.getDisplayName());
	}

	public void disableSlot(int slot) {
		if(slot >= 18 || slot < 0) return;
		role.disableSlot[slot] = 1;
	}

	public void disableSlot(int slot, IPlayer player) {
		if(slot >= 18 || slot < 0) return;
		role.getArrayByName(player.getDisplayName(), role.playerDisableSlot)[slot] = 1;
	}

	public void enableSlot(int slot) {
		if(slot >= 18 || slot < 0) return;
		role.disableSlot[slot] = 0;
	}

	public void enableSlot(int slot, IPlayer player) {
		if(slot >= 18 || slot < 0) return;
		role.getArrayByName(player.getDisplayName(), role.playerDisableSlot)[slot] = 0;
	}

	@Override
	public int getType(){
		return RoleType.TRADER;
	}
}
