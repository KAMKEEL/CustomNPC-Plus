package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.constants.EnumClaimType;
import noppes.npcs.controllers.AuctionController;
import noppes.npcs.controllers.data.AuctionClaim;
import noppes.npcs.controllers.data.AuctionListing;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * Container for viewing player's auction trades.
 * Shows active listings, bids, and claims in a 9x5 grid.
 * Order: SOLD claims, OUTBID claims, WON claims, SELLING, BIDDING, EXPIRED claims
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
    private int maxTradeSlots = 8;  // Player's max trade slots (permission-aware, synced from server)

    // Slot type tracking for proper accessor methods
    private int[] slotTypes = new int[SLOT_COUNT];  // 0=empty, 1=selling, 2=bidding, 3=claim
    private int[] slotDataIndex = new int[SLOT_COUNT];  // Index into respective list

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

    /** Refresh data from controller (server-side only) */
    public void refreshData() {
        // Clear existing data
        clearData();

        // Only works on server where AuctionController exists
        if (AuctionController.Instance == null) return;

        List<AuctionListing> newListings = AuctionController.Instance.getPlayerActiveListings(player.getUniqueID());
        List<AuctionListing> newBids = AuctionController.Instance.getPlayerActiveBids(player.getUniqueID());
        List<AuctionClaim> newClaims = AuctionController.Instance.getPlayerClaims(player.getUniqueID());

        populateDisplay(newListings, newBids, newClaims);
    }

    /** Set data from NBT received from server (client-side) */
    public void setTradesData(NBTTagCompound compound) {
        // Clear existing data
        clearData();

        // Read player's max trade slots (permission-aware)
        if (compound.hasKey("MaxTradeSlots")) {
            maxTradeSlots = compound.getInteger("MaxTradeSlots");
        }

        List<AuctionListing> newListings = new ArrayList<>();
        List<AuctionListing> newBids = new ArrayList<>();
        List<AuctionClaim> newClaims = new ArrayList<>();

        // Parse active listings
        if (compound.hasKey("ActiveListings")) {
            NBTTagList listingsNBT = compound.getTagList("ActiveListings", 10);
            for (int i = 0; i < listingsNBT.tagCount(); i++) {
                newListings.add(AuctionListing.fromNBT(listingsNBT.getCompoundTagAt(i)));
            }
        }

        // Parse active bids
        if (compound.hasKey("ActiveBids")) {
            NBTTagList bidsNBT = compound.getTagList("ActiveBids", 10);
            for (int i = 0; i < bidsNBT.tagCount(); i++) {
                newBids.add(AuctionListing.fromNBT(bidsNBT.getCompoundTagAt(i)));
            }
        }

        // Parse claims
        if (compound.hasKey("Claims")) {
            NBTTagList claimsNBT = compound.getTagList("Claims", 10);
            for (int i = 0; i < claimsNBT.tagCount(); i++) {
                newClaims.add(AuctionClaim.fromNBT(claimsNBT.getCompoundTagAt(i)));
            }
        }

        populateDisplay(newListings, newBids, newClaims);
    }

    /** Clear all data and reset display */
    private void clearData() {
        activeListings.clear();
        activeBids.clear();
        claims.clear();
        displayInventory.clear();

        // Reset slot tracking
        for (int i = 0; i < SLOT_COUNT; i++) {
            slotTypes[i] = 0;
            slotDataIndex[i] = -1;
        }
    }

    /** Populate display from provided data lists */
    private void populateDisplay(List<AuctionListing> newListings, List<AuctionListing> newBids, List<AuctionClaim> allClaims) {
        activeListings = new ArrayList<>(newListings);
        activeBids = new ArrayList<>(newBids);

        // Sort claims into categories for proper display order
        List<AuctionClaim> soldClaims = new ArrayList<>();      // CURRENCY - sold items
        List<AuctionClaim> outbidClaims = new ArrayList<>();    // REFUND - outbid refunds
        List<AuctionClaim> wonClaims = new ArrayList<>();       // ITEM won (isReturned=false)
        List<AuctionClaim> expiredClaims = new ArrayList<>();   // ITEM returned (isReturned=true)

        for (AuctionClaim claim : allClaims) {
            if (claim.type == EnumClaimType.CURRENCY) {
                soldClaims.add(claim);
            } else if (claim.type == EnumClaimType.REFUND) {
                outbidClaims.add(claim);
            } else if (claim.type == EnumClaimType.ITEM) {
                if (claim.isReturned) {
                    expiredClaims.add(claim);
                } else {
                    wonClaims.add(claim);
                }
            }
        }

        // Populate display in order: SOLD, OUTBID, WON, SELLING, BIDDING, EXPIRED
        int slot = 0;

        // 1. Sold claims (green) - you have money to claim!
        for (AuctionClaim claim : soldClaims) {
            if (slot >= SLOT_COUNT) break;
            claims.add(claim);
            displayInventory.setClaimItem(slot, claim);
            slotTypes[slot] = 3;  // claim
            slotDataIndex[slot] = claims.size() - 1;
            slot++;
        }

        // 2. Outbid claims (yellow) - refunds available
        for (AuctionClaim claim : outbidClaims) {
            if (slot >= SLOT_COUNT) break;
            claims.add(claim);
            displayInventory.setClaimItem(slot, claim);
            slotTypes[slot] = 3;  // claim
            slotDataIndex[slot] = claims.size() - 1;
            slot++;
        }

        // 3. Won claims (green) - items to claim!
        for (AuctionClaim claim : wonClaims) {
            if (slot >= SLOT_COUNT) break;
            claims.add(claim);
            displayInventory.setClaimItem(slot, claim);
            slotTypes[slot] = 3;  // claim
            slotDataIndex[slot] = claims.size() - 1;
            slot++;
        }

        // 4. Active listings (blue) - items you're selling
        int listingIdx = 0;
        for (AuctionListing listing : activeListings) {
            if (slot >= SLOT_COUNT) break;
            if (listing.item != null) displayInventory.setInventorySlotContents(slot, listing.item.copy());
            slotTypes[slot] = 1;  // selling
            slotDataIndex[slot] = listingIdx;
            slot++;
            listingIdx++;
        }

        // 5. Active bids (blue) - items you're bidding on
        int bidIdx = 0;
        for (AuctionListing bid : activeBids) {
            if (slot >= SLOT_COUNT) break;
            if (bid.item != null) displayInventory.setInventorySlotContents(slot, bid.item.copy());
            slotTypes[slot] = 2;  // bidding
            slotDataIndex[slot] = bidIdx;
            slot++;
            bidIdx++;
        }

        // 6. Expired claims (red) - returned items (lowest priority)
        for (AuctionClaim claim : expiredClaims) {
            if (slot >= SLOT_COUNT) break;
            claims.add(claim);
            displayInventory.setClaimItem(slot, claim);
            slotTypes[slot] = 3;  // claim
            slotDataIndex[slot] = claims.size() - 1;
            slot++;
        }
    }

    // ========== Slot Data Accessors ==========

    /** Get listing at slot (or null if claim/empty) */
    public AuctionListing getListingAt(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) return null;
        int type = slotTypes[slot];
        int idx = slotDataIndex[slot];
        if (idx < 0) return null;

        if (type == 1 && idx < activeListings.size()) {
            return activeListings.get(idx);
        } else if (type == 2 && idx < activeBids.size()) {
            return activeBids.get(idx);
        }
        return null;
    }

    /** Get claim at slot (or null if listing/empty) */
    public AuctionClaim getClaimAt(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) return null;
        if (slotTypes[slot] == 3) {
            int idx = slotDataIndex[slot];
            if (idx >= 0 && idx < claims.size()) {
                return claims.get(idx);
            }
        }
        return null;
    }

    /** Check if slot is player's active listing */
    public boolean isSellingAt(int slot) {
        return slot >= 0 && slot < SLOT_COUNT && slotTypes[slot] == 1;
    }

    /** Check if slot is player's active bid */
    public boolean isBiddingAt(int slot) {
        return slot >= 0 && slot < SLOT_COUNT && slotTypes[slot] == 2;
    }

    /** Find listing matching item (searches both selling and bidding) */
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

    /** Find listing in selling list */
    public AuctionListing getSellingListingForItem(ItemStack stack) {
        if (stack == null) return null;
        for (AuctionListing listing : activeListings) {
            if (listing.item != null && ItemStack.areItemStacksEqual(listing.item, stack)) return listing;
        }
        return null;
    }

    /** Find listing in bidding list */
    public AuctionListing getBiddingListingForItem(ItemStack stack) {
        if (stack == null) return null;
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

    /** Restore item at slot from data using slot tracking */
    private void restoreSlotItem(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) return;

        int type = slotTypes[slot];
        int idx = slotDataIndex[slot];
        if (idx < 0) return;

        if (type == 1 && idx < activeListings.size()) {
            // Selling
            AuctionListing listing = activeListings.get(idx);
            if (listing != null && listing.item != null) {
                displayInventory.setInventorySlotContents(slot, listing.item.copy());
            }
        } else if (type == 2 && idx < activeBids.size()) {
            // Bidding
            AuctionListing bid = activeBids.get(idx);
            if (bid != null && bid.item != null) {
                displayInventory.setInventorySlotContents(slot, bid.item.copy());
            }
        } else if (type == 3 && idx < claims.size()) {
            // Claim
            displayInventory.setClaimItem(slot, claims.get(idx));
        }
    }

    public int getHiddenSlot() { return hiddenSlot; }

    /** Get player's max trade slots (synced from server based on permissions) */
    public int getMaxTradeSlots() { return maxTradeSlots; }
}
