package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import noppes.npcs.entity.EntityNPCInterface;

/**
 * Container for auction house GUI.
 * Handles player inventory and item slot for creating listings.
 */
public class ContainerAuction extends Container {
    public EntityNPCInterface npc;
    public EntityPlayer player;

    // Slot for item being listed
    public SlotAuctionItem listingSlot;
    private IInventory listingInventory;

    public ContainerAuction(EntityNPCInterface npc, EntityPlayer player) {
        this.npc = npc;
        this.player = player;

        // Create a single-slot inventory for the item being listed
        listingInventory = new InventoryAuctionListing();
        listingSlot = new SlotAuctionItem(listingInventory, 0, 80, 35);
        addSlotToContainer(listingSlot);

        // Player inventory slots (standard positioning)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(player.inventory, col + row * 9 + 9,
                    8 + col * 18, 140 + row * 18));
            }
        }

        // Player hotbar
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(player.inventory, col, 8 + col * 18, 198));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return npc != null && npc.isEntityAlive() &&
               player.getDistanceSqToEntity(npc) < 64.0D;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
        ItemStack itemstack = null;
        Slot slot = (Slot) inventorySlots.get(slotIndex);

        if (slot != null && slot.getHasStack()) {
            ItemStack stackInSlot = slot.getStack();
            itemstack = stackInSlot.copy();

            // If from listing slot, move to player inventory
            if (slotIndex == 0) {
                if (!mergeItemStack(stackInSlot, 1, 37, true)) {
                    return null;
                }
            }
            // If from player inventory, move to listing slot
            else {
                if (!mergeItemStack(stackInSlot, 0, 1, false)) {
                    return null;
                }
            }

            if (stackInSlot.stackSize == 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }
        }

        return itemstack;
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);

        // Return any item in the listing slot to player
        ItemStack stack = listingInventory.getStackInSlot(0);
        if (stack != null) {
            if (!player.inventory.addItemStackToInventory(stack)) {
                player.entityDropItem(stack, 0.5f);
            }
        }
    }

    /**
     * Get the item currently in the listing slot
     */
    public ItemStack getListingItem() {
        return listingInventory.getStackInSlot(0);
    }

    /**
     * Clear the listing slot (after successful listing creation)
     */
    public void clearListingSlot() {
        listingInventory.setInventorySlotContents(0, null);
    }

    /**
     * Simple inventory for the listing slot
     */
    private class InventoryAuctionListing implements IInventory {
        private ItemStack item;

        @Override
        public int getSizeInventory() { return 1; }

        @Override
        public ItemStack getStackInSlot(int slot) { return item; }

        @Override
        public ItemStack decrStackSize(int slot, int amount) {
            if (item != null) {
                ItemStack result;
                if (item.stackSize <= amount) {
                    result = item;
                    item = null;
                    return result;
                } else {
                    result = item.splitStack(amount);
                    if (item.stackSize == 0) {
                        item = null;
                    }
                    return result;
                }
            }
            return null;
        }

        @Override
        public ItemStack getStackInSlotOnClosing(int slot) {
            ItemStack result = item;
            item = null;
            return result;
        }

        @Override
        public void setInventorySlotContents(int slot, ItemStack stack) {
            item = stack;
        }

        @Override
        public String getInventoryName() { return "AuctionListing"; }

        @Override
        public boolean hasCustomInventoryName() { return false; }

        @Override
        public int getInventoryStackLimit() { return 64; }

        @Override
        public void markDirty() {}

        @Override
        public boolean isUseableByPlayer(EntityPlayer player) { return true; }

        @Override
        public void openInventory() {}

        @Override
        public void closeInventory() {}

        @Override
        public boolean isItemValidForSlot(int slot, ItemStack stack) { return true; }
    }

    /**
     * Custom slot for auction listings
     */
    public class SlotAuctionItem extends Slot {
        public SlotAuctionItem(IInventory inv, int index, int x, int y) {
            super(inv, index, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            // Check if item can be traded (not untradeable, soulbound, etc.)
            return kamkeel.npcs.controllers.data.attribute.ItemTradeAttribute.canTrade(stack);
        }
    }
}
