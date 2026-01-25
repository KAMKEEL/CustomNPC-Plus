package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.entity.EntityNPCInterface;

/**
 * Container for creating auction listings with a single sell slot.
 * Items are cloned from inventory, not moved. Validation happens on submit.
 */
public class ContainerAuctionSell extends ContainerAuction {
    private int sellSlotX = 59;
    private int sellSlotY = 101;
    private int sellSlotIndex = PLAYER_INV_SLOT_COUNT;
    private int maxStack = 64;

    private final IInventory sellInventory;

    public ContainerAuctionSell(EntityNPCInterface npc, EntityPlayer player) {
        super(npc, player);
        sellInventory = new InventoryBasic("Sell", false, 1);

        // Sell slot blocks direct interaction
        addSlotToContainer(new Slot(sellInventory, 0, sellSlotX, sellSlotY) {
            @Override
            public boolean isItemValid(ItemStack stack) { return false; }
            @Override
            public boolean canTakeStack(EntityPlayer player) { return false; }
        });
    }

    public ItemStack getItemToSell() {
        return sellInventory.getStackInSlot(0);
    }

    public void clearSellSlot() {
        sellInventory.setInventorySlotContents(0, null);
    }

    /**
     * Add items from inventory to sell slot.
     * Not limited by inventory - just stages items for sale. Validation on submit.
     * @param sourceSlot Inventory slot index
     * @param fullStack True=full stack, False=1 item
     */
    public void addToSellSlot(int sourceSlot, boolean fullStack) {
        if (!isPlayerInventorySlot(sourceSlot)) return;

        ItemStack source = getPlayerInventoryStack(sourceSlot);
        if (source == null) return;

        ItemStack sellStack = sellInventory.getStackInSlot(0);
        int toAdd = fullStack ? source.stackSize : 1;

        if (sellStack == null) {
            // Empty slot - create new stack
            ItemStack newStack = source.copy();
            newStack.stackSize = Math.min(toAdd, maxStack);
            sellInventory.setInventorySlotContents(0, newStack);
        } else if (itemsMatch(sellStack, source)) {
            // Same item - add to existing (capped at 64)
            int space = maxStack - sellStack.stackSize;
            int add = Math.min(toAdd, space);
            if (add > 0) {
                sellStack.stackSize += add;
            }
        } else {
            // Different item - replace
            ItemStack newStack = source.copy();
            newStack.stackSize = Math.min(toAdd, maxStack);
            sellInventory.setInventorySlotContents(0, newStack);
        }
    }

    /**
     * Remove items from sell slot.
     * @param removeAll True=clear slot, False=remove 1 item
     */
    public void removeFromSellSlot(boolean removeAll) {
        ItemStack sellStack = sellInventory.getStackInSlot(0);
        if (sellStack == null) return;

        if (removeAll || sellStack.stackSize <= 1) {
            sellInventory.setInventorySlotContents(0, null);
        } else {
            sellStack.stackSize--;
        }
    }

    /** Check if sell slot is this container slot index */
    public boolean isSellSlot(int slotIndex) {
        return slotIndex == sellSlotIndex;
    }

    /** Check if two stacks match using NoppesUtilPlayer.compareItems (same item, damage, and NBT) */
    private boolean itemsMatch(ItemStack a, ItemStack b) {
        return NoppesUtilPlayer.compareItems(a, b, false, false);
    }

    /**
     * Count total items of a specific type across player inventory.
     * Used for submit validation and UI indicator.
     */
    public int countItemInInventory(ItemStack target) {
        if (target == null) return 0;
        int total = 0;
        for (int i = 0; i < PLAYER_INV_SLOT_COUNT; i++) {
            ItemStack stack = getPlayerInventoryStack(i);
            if (stack != null && itemsMatch(stack, target)) {
                total += stack.stackSize;
            }
        }
        return total;
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        clearSellSlot();
    }
}
