package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import noppes.npcs.controllers.data.AuctionListing;
import noppes.npcs.entity.EntityNPCInterface;

/**
 * Container for the auction bidding/purchase view.
 * Displays a single item slot showing the listing's item.
 */
public class ContainerAuctionBidding extends ContainerAuction {
    // Display slot layout
    public static final int ITEM_SLOT_X = 75;
    public static final int ITEM_SLOT_Y = 70;
    public static final int ITEM_SLOT_INDEX = PLAYER_INV_SLOT_COUNT;

    private final IInventory displayInventory;
    private AuctionListing listing;

    public ContainerAuctionBidding(EntityNPCInterface npc, EntityPlayer player) {
        super(npc, player);
        displayInventory = new InventoryBasic("Display", false, 1);

        // Display slot (read-only)
        addSlotToContainer(new Slot(displayInventory, 0, ITEM_SLOT_X, ITEM_SLOT_Y) {
            @Override
            public boolean isItemValid(ItemStack stack) { return false; }
            @Override
            public boolean canTakeStack(EntityPlayer player) { return false; }
        });
    }

    /**
     * Set the listing to display.
     */
    public void setListing(AuctionListing listing) {
        this.listing = listing;
        if (listing != null && listing.item != null) {
            displayInventory.setInventorySlotContents(0, listing.item.copy());
        } else {
            displayInventory.setInventorySlotContents(0, null);
        }
    }

    public AuctionListing getListing() {
        return listing;
    }

    public ItemStack getDisplayItem() {
        return displayInventory.getStackInSlot(0);
    }

    /**
     * Check if this is the display slot.
     */
    public boolean isDisplaySlot(int slotIndex) {
        return slotIndex == ITEM_SLOT_INDEX;
    }
}
