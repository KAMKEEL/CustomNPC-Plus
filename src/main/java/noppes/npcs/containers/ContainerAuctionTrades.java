package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import noppes.npcs.controllers.AuctionController;
import noppes.npcs.controllers.data.AuctionClaim;
import noppes.npcs.controllers.data.AuctionListing;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * Container for viewing player's auction trades.
 * Shows active listings, bids, and claims in a 9x5 grid.
 */
public class ContainerAuctionTrades extends ContainerAuction {
    // Grid layout
    public static final int GRID_X = 57;
    public static final int GRID_Y = 47;
    public static final int COLS = 9;
    public static final int ROWS = 5;
    public static final int SLOT_COUNT = COLS * ROWS;

    public final InventoryAuctionDisplay displayInventory;
    private List<AuctionListing> activeListings = new ArrayList<>();
    private List<AuctionListing> activeBids = new ArrayList<>();
    private List<AuctionClaim> claims = new ArrayList<>();
    private int hiddenSlot = -1;

    public ContainerAuctionTrades(EntityNPCInterface npc, EntityPlayer player) {
        super(npc, player);
        displayInventory = new InventoryAuctionDisplay(SLOT_COUNT);

        // Add display slots (9x5 grid)
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int idx = col + row * COLS;
                int x = GRID_X + col * 18;
                int y = GRID_Y + row * 18;
                addSlotToContainer(new SlotAuctionDisplay(displayInventory, idx, x, y));
            }
        }
        refreshData();
    }

    /** Refresh data from controller */
    public void refreshData() {
        activeListings.clear();
        activeBids.clear();
        claims.clear();
        displayInventory.clear();

        if (AuctionController.Instance == null) return;

        activeListings = AuctionController.Instance.getPlayerActiveListings(player.getUniqueID());
        activeBids = AuctionController.Instance.getPlayerActiveBids(player.getUniqueID());
        claims = AuctionController.Instance.getPlayerClaims(player.getUniqueID());

        // Populate display: listings first, then bids, then claims
        int slot = 0;
        for (AuctionListing listing : activeListings) {
            if (slot >= SLOT_COUNT) break;
            if (listing.item != null) displayInventory.setInventorySlotContents(slot, listing.item.copy());
            slot++;
        }
        for (AuctionListing bid : activeBids) {
            if (slot >= SLOT_COUNT) break;
            if (bid.item != null) displayInventory.setInventorySlotContents(slot, bid.item.copy());
            slot++;
        }
        for (AuctionClaim claim : claims) {
            if (slot >= SLOT_COUNT) break;
            displayInventory.setClaimItem(slot, claim);
            slot++;
        }
    }

    // ========== Slot Data Accessors ==========

    /** Get listing at slot (or null if claim/empty) */
    public AuctionListing getListingAt(int slot) {
        if (slot >= 0 && slot < activeListings.size()) {
            return activeListings.get(slot);
        }
        int bidIdx = slot - activeListings.size();
        if (bidIdx >= 0 && bidIdx < activeBids.size()) {
            return activeBids.get(bidIdx);
        }
        return null;
    }

    /** Get claim at slot (or null if listing/empty) */
    public AuctionClaim getClaimAt(int slot) {
        int claimIdx = slot - activeListings.size() - activeBids.size();
        if (claimIdx >= 0 && claimIdx < claims.size()) {
            return claims.get(claimIdx);
        }
        return null;
    }

    /** Check if slot is player's active listing */
    public boolean isSellingAt(int slot) {
        return slot >= 0 && slot < activeListings.size();
    }

    /** Check if slot is player's active bid */
    public boolean isBiddingAt(int slot) {
        int bidIdx = slot - activeListings.size();
        return bidIdx >= 0 && bidIdx < activeBids.size();
    }

    /** Find listing matching item */
    public AuctionListing getListingForItem(ItemStack stack) {
        if (stack == null) return null;
        for (AuctionListing listing : activeListings) {
            if (listing.item != null && ItemStack.areItemStacksEqual(listing.item, stack)) return listing;
        }
        for (AuctionListing bid : activeBids) {
            if (bid.item != null && ItemStack.areItemStacksEqual(bid.item, stack)) return bid;
        }
        return null;
    }

    /** Find claim matching item */
    public AuctionClaim getClaimForItem(ItemStack stack) {
        if (stack == null) return null;
        for (AuctionClaim claim : claims) {
            if (claim.item != null && ItemStack.areItemStacksEqual(claim.item, stack)) return claim;
        }
        return null;
    }

    // ========== List Getters ==========

    public List<AuctionListing> getActiveListings() { return activeListings; }
    public List<AuctionListing> getActiveBids() { return activeBids; }
    public List<AuctionClaim> getClaims() { return claims; }
    public int getTotalTradeCount() { return activeListings.size() + activeBids.size() + claims.size(); }

    // ========== Slot Hiding (for pending operations) ==========

    /** Hide slot for pending operation display */
    public void setHiddenSlot(int slot) {
        if (hiddenSlot >= 0 && hiddenSlot < SLOT_COUNT) {
            restoreSlotItem(hiddenSlot);
        }
        hiddenSlot = slot;
        if (hiddenSlot >= 0 && hiddenSlot < SLOT_COUNT) {
            displayInventory.setInventorySlotContents(hiddenSlot, null);
        }
    }

    /** Restore hidden slot */
    public void clearHiddenSlot() {
        if (hiddenSlot >= 0 && hiddenSlot < SLOT_COUNT) {
            restoreSlotItem(hiddenSlot);
        }
        hiddenSlot = -1;
    }

    /** Restore item at slot from data */
    private void restoreSlotItem(int slot) {
        if (slot < activeListings.size()) {
            AuctionListing listing = activeListings.get(slot);
            if (listing != null && listing.item != null) {
                displayInventory.setInventorySlotContents(slot, listing.item.copy());
            }
        } else if (slot < activeListings.size() + activeBids.size()) {
            int bidIdx = slot - activeListings.size();
            AuctionListing bid = activeBids.get(bidIdx);
            if (bid != null && bid.item != null) {
                displayInventory.setInventorySlotContents(slot, bid.item.copy());
            }
        } else {
            int claimIdx = slot - activeListings.size() - activeBids.size();
            if (claimIdx < claims.size()) {
                displayInventory.setClaimItem(slot, claims.get(claimIdx));
            }
        }
    }

    public int getHiddenSlot() { return hiddenSlot; }
}
