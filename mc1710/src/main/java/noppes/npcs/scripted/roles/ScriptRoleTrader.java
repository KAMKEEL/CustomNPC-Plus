package noppes.npcs.scripted.roles;

import foxz.utils.Market;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.roles.IRoleTrader;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleTrader;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.constants.RoleType;

public class ScriptRoleTrader extends ScriptRoleInterface implements IRoleTrader {
    private RoleTrader role;

    public ScriptRoleTrader(EntityNPCInterface npc) {
        super(npc);
        role = (RoleTrader) npc.roleInterface;
    }

    public void setSellOption(int slot, IItemStack currency, IItemStack currency2, IItemStack sold) {
        if (sold == null || slot >= 18 || slot < 0)
            return;
        if (currency == null)
            currency = currency2;

        if (currency != null)
            role.inventoryCurrency.items.put(slot, currency.getMCItemStack());
        else
            role.inventoryCurrency.items.remove(slot);
        if (currency2 != null)
            role.inventoryCurrency.items.put(slot + 18, currency2.getMCItemStack());
        else
            role.inventoryCurrency.items.remove(slot + 18);

        role.inventorySold.items.put(slot, sold.getMCItemStack());
    }

    public void setSellOption(int slot, IItemStack currency, IItemStack sold) {
        setSellOption(slot, currency, null, sold);
    }

    public IItemStack getSellOption(int slot) {
        if (slot >= 18 || slot < 0) return null;
        if (role.inventorySold.items.get(slot) == null) return null;
        return NpcAPI.Instance().getIItemStack(role.inventorySold.items.get(slot));
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


    public void removeSellOption(int slot) {
        if (slot >= 18 || slot < 0) return;
        role.inventoryCurrency.items.remove(slot);
        role.inventoryCurrency.items.remove(slot + 18);
        role.inventorySold.items.remove(slot);
    }

    public void setMarket(String name) {
        role.marketName = name;
        Market.getMarket(role, name);
    }

    public String getMarket() {
        return role.marketName;
    }

    public int getPurchaseNum(int slot) {
        if (slot >= 18 || slot < 0) return -1;
        return role.purchases[slot];
    }

    public int getPurchaseNum(int slot, IPlayer player) {
        if (slot >= 18 || slot < 0) return -1;
        return role.getArrayByName(player.getDisplayName(), role.playerPurchases)[slot];
    }

    public void resetPurchaseNum() {
        for (int i = 0; i < role.purchases.length; ++i) role.purchases[i] = 0;
    }

    public void resetPurchaseNum(int slot) {
        if (slot >= 18 || slot < 0) return;
        role.purchases[slot] = 0;
    }

    public void resetPurchaseNum(int slot, IPlayer player) {
        if (slot >= 18 || slot < 0) return;
        role.getArrayByName(player.getDisplayName(), role.playerPurchases)[slot] = 0;
    }

    public boolean isSlotEnabled(int slot) {
        if (slot >= 18 || slot < 0) return false;
        return role.disableSlot[slot] <= 0;  // enabled when NOT disabled
    }

    public boolean isSlotEnabled(int slot, IPlayer player) {
        if (slot >= 18 || slot < 0) return false;
        return role.isSlotEnabled(slot, player.getDisplayName());
    }

    public void disableSlot(int slot) {
        if (slot >= 18 || slot < 0) return;
        role.disableSlot[slot] = 1;
    }

    public void disableSlot(int slot, IPlayer player) {
        if (slot >= 18 || slot < 0) return;
        role.getArrayByName(player.getDisplayName(), role.playerDisableSlot)[slot] = 1;
    }

    public void enableSlot(int slot) {
        if (slot >= 18 || slot < 0) return;
        role.disableSlot[slot] = 0;
    }

    public void enableSlot(int slot, IPlayer player) {
        if (slot >= 18 || slot < 0) return;
        role.getArrayByName(player.getDisplayName(), role.playerDisableSlot)[slot] = 0;
    }

    // ==================== Stock System ====================

    public boolean isStockEnabled() {
        return role.stock.enableStock;
    }

    public void setStockEnabled(boolean enabled) {
        role.stock.enableStock = enabled;
    }

    public boolean isPerPlayerStock() {
        return role.stock.perPlayer;
    }

    public void setPerPlayerStock(boolean perPlayer) {
        role.stock.perPlayer = perPlayer;
    }

    public int getStockResetType() {
        return role.stock.resetType.ordinal();
    }

    public void setStockResetType(int type) {
        noppes.npcs.constants.EnumStockReset[] values = noppes.npcs.constants.EnumStockReset.values();
        if (type >= 0 && type < values.length) {
            role.stock.resetType = values[type];
        }
    }

    public long getCustomResetTime() {
        return role.stock.customResetTime;
    }

    public void setCustomResetTime(long time) {
        role.stock.customResetTime = Math.max(0, time);
    }

    public int getMaxStock(int slot) {
        if (slot < 0 || slot >= 18) return -1;
        return role.stock.maxStock[slot];
    }

    public void setMaxStock(int slot, int amount) {
        if (slot >= 0 && slot < 18) {
            role.stock.setMaxStock(slot, amount);
        }
    }

    public int getAvailableStock(int slot) {
        if (slot < 0 || slot >= 18) return 0;
        return role.stock.getAvailableStock(slot, "");
    }

    public int getAvailableStock(int slot, IPlayer player) {
        if (slot < 0 || slot >= 18) return 0;
        return role.stock.getAvailableStock(slot, player.getDisplayName());
    }

    public void resetStock() {
        long currentTime = role.stock.resetType.isRealTime()
            ? System.currentTimeMillis()
            : (npc.worldObj != null ? npc.worldObj.getTotalWorldTime() : 0);
        role.stock.resetStock(currentTime);
    }

    public void resetCooldown() {
        long currentTime = role.stock.resetType.isRealTime()
            ? System.currentTimeMillis()
            : (npc.worldObj != null ? npc.worldObj.getTotalWorldTime() : 0);
        role.stock.lastResetTime = currentTime;
    }

    public int getCurrentStock(int slot) {
        if (slot < 0 || slot >= 18) return -1;
        return role.stock.currentStock[slot];
    }

    public void setCurrentStock(int slot, int amount) {
        if (slot >= 0 && slot < 18) {
            role.stock.currentStock[slot] = amount;
        }
    }

    public int getPlayerPurchased(int slot, IPlayer player) {
        if (slot < 0 || slot >= 18) return 0;
        noppes.npcs.controllers.data.TraderStock.PlayerTraderStock pStock =
            role.stock.playerStock.get(player.getDisplayName());
        if (pStock == null) return 0;
        return pStock.purchasedAmounts[slot];
    }

    public void setPlayerPurchased(int slot, IPlayer player, int amount) {
        if (slot < 0 || slot >= 18) return;
        noppes.npcs.controllers.data.TraderStock.PlayerTraderStock pStock =
            role.stock.playerStock.computeIfAbsent(
                player.getDisplayName(),
                k -> new noppes.npcs.controllers.data.TraderStock.PlayerTraderStock()
            );
        pStock.purchasedAmounts[slot] = Math.max(0, amount);
    }

    public long getLastResetTime() {
        return role.stock.lastResetTime;
    }

    public long getTimeUntilReset() {
        long currentTime = role.stock.resetType.isRealTime()
            ? System.currentTimeMillis()
            : (npc.worldObj != null ? npc.worldObj.getTotalWorldTime() : 0);
        return role.stock.getTimeUntilReset(currentTime);
    }

    // ==================== Currency Cost System ====================

    public long getCurrencyCost(int slot) {
        return role.getCurrencyCost(slot);
    }

    public void setCurrencyCost(int slot, long cost) {
        role.setCurrencyCost(slot, cost);
    }

    public boolean hasCurrencyCost(int slot) {
        return role.hasCurrencyCost(slot);
    }

    @Override
    public int getType() {
        return RoleType.TRADER;
    }
}
