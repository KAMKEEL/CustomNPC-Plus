package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleTrader;


public class ContainerNPCTrader extends ContainerNpcInterface {
    public RoleTrader role;
    private final EntityNPCInterface npc;

    // Layout constants for 256px wide GUI
    public static final int COLUMN_WIDTH = 80;
    public static final int COLUMN_START_X = 8;
    public static final int ROW_HEIGHT = 22;
    public static final int ROW_START_Y = 6;

    // Slot offsets within each column (for 18x18 slots, items render centered)
    public static final int CURRENCY1_OFFSET = 2;   // Primary currency slot
    public static final int CURRENCY2_OFFSET = 20;  // Secondary currency slot
    public static final int OUTPUT_OFFSET = 50;     // Output slot

    public ContainerNPCTrader(EntityNPCInterface npc, EntityPlayer player) {
        super(player);
        this.npc = npc;
        role = (RoleTrader) npc.roleInterface;

        // Register this player as viewing the trader (server-side only)
        // Needed for shared stock (not per-player) to sync updates to other viewers
        // Works for both linked markets (MarketRegistry) and normal traders (local viewers)
        if (player instanceof EntityPlayerMP && !role.stock.perPlayer) {
            role.registerViewer((EntityPlayerMP) player);
        }

        // Trade slots: 3 columns x 6 rows, 80px column width
        // Layout per column: [Slot1][Slot2] = [Output]
        for (int i = 0; i < 18; i++) {
            int col = i % 3;
            int row = i / 3;
            int x = COLUMN_START_X + col * COLUMN_WIDTH;
            int y = ROW_START_Y + row * ROW_HEIGHT;
            // Output slot position (slot texture is 18x18, item renders centered at +1)
            addSlotToContainer(new Slot(role.inventorySold, i, x + OUTPUT_OFFSET + 1, y + 1));
        }

        for (int i1 = 0; i1 < 3; i1++) {
            for (int l1 = 0; l1 < 9; l1++) {
                addSlotToContainer(new Slot(player.inventory, l1 + i1 * 9 + 9, 48 + l1 * 18, 137 + i1 * 18));
            }
        }

        // Player hotbar
        for (int j1 = 0; j1 < 9; j1++) {
            addSlotToContainer(new Slot(player.inventory, j1, 48 + j1 * 18, 195));
        }
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        // Unregister this player from viewer tracking (only if registered for shared stock)
        if (player instanceof EntityPlayerMP && !role.stock.perPlayer) {
            role.unregisterViewer((EntityPlayerMP) player);
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int i) {
        return null;
    }

    @Override
    public ItemStack slotClick(int i, int j, int par3, EntityPlayer entityplayer) {
        if (par3 == 6)
            par3 = 0;
        if (i < 0 || i >= 18)
            return super.slotClick(i, j, par3, entityplayer);
        if (j == 1)
            return null;
        Slot slot = (Slot) inventorySlots.get(i);
        if (slot == null || slot.getStack() == null)
            return null;
        ItemStack item = slot.getStack();
        if (!canGivePlayer(item, entityplayer))
            return null;
        if (!isSlotEnabled(i, entityplayer))
            return null;

        // Check stock availability
        String playerName = entityplayer.getCommandSenderName();
        if (!role.hasStock(i, playerName, 1)) {
            return null;  // Out of stock
        }

        // Item-based currency
        if (!canBuy(i, entityplayer))
            return null;

        // Currency cost check (additive to item costs)
        long currencyCost = role.getCurrencyCost(i);
        if (currencyCost > 0) {
            PlayerData data = PlayerData.get(entityplayer);
            if (data.currencyData.getBalance() < currencyCost) {
                return null;  // Can't afford currency cost
            }
        }

        // Consume item currency
        NoppesUtilPlayer.consumeItem(entityplayer, role.inventoryCurrency.getStackInSlot(i), role.ignoreDamage, role.ignoreNBT);
        NoppesUtilPlayer.consumeItem(entityplayer, role.inventoryCurrency.getStackInSlot(i + 18), role.ignoreDamage, role.ignoreNBT);

        // Withdraw currency cost (after item consumption to maintain order)
        if (currencyCost > 0) {
            PlayerData data = PlayerData.get(entityplayer);
            data.currencyData.withdraw(currencyCost);
        }

        // Consume stock
        role.consumeStock(i, playerName, 1);

        ItemStack soldItem = item.copy();
        givePlayer(soldItem, entityplayer);
        role.addPurchase(i, entityplayer.getDisplayName());

        // Sync updated data after purchase
        // For per-player stocks: sync only to the purchasing player
        // For shared stocks: consumeStock() handles sync (market-wide or local)
        if (entityplayer instanceof EntityPlayerMP) {
            if (role.stock.perPlayer) {
                // Per-player: only sync to purchasing player (their own stock/balance)
                role.syncToPlayer((EntityPlayerMP) entityplayer);
            }
            // Shared stock sync is already handled in consumeStock()
        }

        return soldItem;
    }

    public boolean isSlotEnabled(int slot, EntityPlayer player) {
        return role.isSlotEnabled(slot, player.getDisplayName());
    }

    public boolean canBuy(int slot, EntityPlayer player) {
        ItemStack currency = role.inventoryCurrency.getStackInSlot(slot);
        ItemStack currency2 = role.inventoryCurrency.getStackInSlot(slot + 18);
        if (currency == null && currency2 == null)
            return true;
        if (currency == null) {
            currency = currency2;
            currency2 = null;
        }
        if (NoppesUtilPlayer.compareItems(currency, currency2, role.ignoreDamage, role.ignoreNBT)) {
            currency = currency.copy();
            currency.stackSize += currency2.stackSize;
            currency2 = null;
        }
        if (currency2 == null)
            return NoppesUtilPlayer.compareItems(player, currency, role.ignoreDamage, role.ignoreNBT);
        return NoppesUtilPlayer.compareItems(player, currency, role.ignoreDamage, role.ignoreNBT) && NoppesUtilPlayer.compareItems(player, currency2, role.ignoreDamage, role.ignoreNBT);

    }

    private boolean canGivePlayer(ItemStack item, EntityPlayer entityplayer) {//check Item being held with the mouse
        ItemStack itemstack3 = entityplayer.inventory.getItemStack();
        if (itemstack3 == null) {
            return true;
        } else if (NoppesUtilPlayer.compareItems(itemstack3, item, false, false)) {
            int k1 = item.stackSize;
            if (k1 > 0 && k1 + itemstack3.stackSize <= itemstack3.getMaxStackSize()) {
                return true;
            }
        }
        return false;
    }

    private void givePlayer(ItemStack item, EntityPlayer entityplayer) {//set item bought to the held mouse item
        ItemStack itemstack3 = entityplayer.inventory.getItemStack();
        if (itemstack3 == null) {
            entityplayer.inventory.setItemStack(item);
        } else if (NoppesUtilPlayer.compareItems(itemstack3, item, false, false)) {

            int k1 = item.stackSize;
            if (k1 > 0 && k1 + itemstack3.stackSize <= itemstack3.getMaxStackSize()) {
                itemstack3.stackSize += k1;
            }
        }
    }
}
