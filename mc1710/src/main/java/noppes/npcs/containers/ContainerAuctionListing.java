package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import noppes.npcs.controllers.AuctionController;
import noppes.npcs.controllers.data.AuctionFilter;
import noppes.npcs.controllers.data.AuctionListing;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.List;

/**
 * Container for the auction listing browse view.
 * Displays a 9x5 grid of auction listings (45 slots).
 */
public class ContainerAuctionListing extends ContainerAuction {
    // Listing grid layout
    public static final int LISTING_START_X = 57;  // 56 + 1 for item offset
    public static final int LISTING_START_Y = 47;  // 46 + 1 for item offset
    public static final int LISTING_COLS = 9;
    public static final int LISTING_ROWS = 5;
    public static final int LISTING_SLOT_COUNT = LISTING_COLS * LISTING_ROWS;

    public final InventoryAuctionDisplay displayInventory;
    private int currentPage = 0;
    private AuctionFilter filter = new AuctionFilter();

    public ContainerAuctionListing(EntityNPCInterface npc, EntityPlayer player) {
        super(npc, player);

        // Create display inventory for listings
        displayInventory = new InventoryAuctionDisplay(LISTING_SLOT_COUNT);

        // Add listing display slots (9x5 grid)
        for (int row = 0; row < LISTING_ROWS; row++) {
            for (int col = 0; col < LISTING_COLS; col++) {
                int slotIndex = col + row * LISTING_COLS;
                int x = LISTING_START_X + col * 18;
                int y = LISTING_START_Y + row * 18;
                addSlotToContainer(new SlotAuctionDisplay(displayInventory, slotIndex, x, y));
            }
        }

        // Load initial listings
        refreshListings();
    }

    public void refreshListings() {
        if (AuctionController.Instance == null) return;

        List<AuctionListing> listings = AuctionController.Instance.getActiveListings(
            filter, currentPage, LISTING_SLOT_COUNT);

        displayInventory.clear();
        for (int i = 0; i < listings.size() && i < LISTING_SLOT_COUNT; i++) {
            displayInventory.setListing(i, listings.get(i));
        }
    }

    public AuctionListing getListingAt(int slotIndex) {
        return displayInventory.getListing(slotIndex);
    }

    /**
     * Find a listing by matching ItemStack.
     */
    public AuctionListing getListingForItem(ItemStack stack) {
        if (stack == null) return null;

        for (int i = 0; i < LISTING_SLOT_COUNT; i++) {
            AuctionListing listing = displayInventory.getListing(i);
            if (listing != null && listing.item != null && ItemStack.areItemStacksEqual(listing.item, stack)) {
                return listing;
            }
        }
        return null;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setPage(int page) {
        this.currentPage = Math.max(0, page);
        refreshListings();
    }

    public void nextPage() {
        setPage(currentPage + 1);
    }

    public void prevPage() {
        if (currentPage > 0) {
            setPage(currentPage - 1);
        }
    }

    public int getTotalPages() {
        if (AuctionController.Instance == null) return 1;
        int totalListings = AuctionController.Instance.getTotalActiveListings(filter);
        return Math.max(1, (int) Math.ceil((double) totalListings / LISTING_SLOT_COUNT));
    }

    public int getTotalListings() {
        if (AuctionController.Instance == null) return 0;
        return AuctionController.Instance.getTotalActiveListings(filter);
    }

    public AuctionFilter getFilter() {
        return filter;
    }

    public void setFilter(AuctionFilter filter) {
        this.filter = filter;
        this.currentPage = 0;
        refreshListings();
    }

    /**
     * Check if a slot index is a display slot.
     */
    public boolean isDisplaySlot(int slotIndex) {
        int start = getDisplaySlotStart();
        return slotIndex >= start && slotIndex < start + LISTING_SLOT_COUNT;
    }

    /**
     * Get the starting slot index for display slots.
     */
    public int getDisplaySlotStart() {
        return PLAYER_INV_SLOT_COUNT;
    }
}
