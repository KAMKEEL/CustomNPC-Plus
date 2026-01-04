package noppes.npcs.controllers.data;

import kamkeel.npcs.controllers.data.attribute.ItemTradeAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.config.ConfigMarket;
import noppes.npcs.constants.EnumAuctionCategory;
import noppes.npcs.constants.EnumAuctionDuration;
import noppes.npcs.constants.EnumAuctionStatus;
import noppes.npcs.controllers.AuctionBlacklist;

import java.util.UUID;

/**
 * Represents a single auction listing.
 * Contains all data about the item, seller, bidding, and timing.
 */
public class AuctionListing {
    // Unique identifier
    public int id;

    // Seller info
    public UUID sellerUUID;
    public String sellerName;

    // Item being sold
    public ItemStack item;

    // Pricing
    public long startingPrice;
    public long buyoutPrice;  // 0 = no buyout option
    public long currentBid;

    // Current high bidder
    public UUID highBidderUUID;
    public String highBidderName;

    // Previous bidder (for refund notifications)
    public UUID previousBidderUUID;
    public String previousBidderName;
    public long previousBidAmount;

    // Timing
    public long listingTime;      // When the auction was created
    public long expirationTime;   // When the auction ends
    public EnumAuctionDuration duration;

    // Status
    public EnumAuctionStatus status = EnumAuctionStatus.ACTIVE;

    // Category for filtering
    public EnumAuctionCategory category = EnumAuctionCategory.MISC;

    // Claim tracking
    public boolean sellerClaimed = false;   // Has seller claimed their proceeds?
    public boolean buyerClaimed = false;    // Has buyer claimed their item?

    public AuctionListing() {
    }

    /**
     * Create a new auction listing
     */
    public AuctionListing(UUID seller, String sellerName, ItemStack item,
                          long startPrice, long buyout, EnumAuctionDuration duration) {
        this.sellerUUID = seller;
        this.sellerName = sellerName;
        this.item = item.copy();
        this.startingPrice = startPrice;
        this.buyoutPrice = buyout;
        this.currentBid = 0;  // No bids yet
        this.duration = duration;
        this.listingTime = System.currentTimeMillis();
        this.expirationTime = listingTime + duration.getMillis();
        this.category = EnumAuctionCategory.categorize(item);
        this.status = EnumAuctionStatus.ACTIVE;
    }

    // ==================== Status Checks ====================

    /**
     * Check if auction has expired
     */
    public boolean isExpired() {
        return System.currentTimeMillis() >= expirationTime;
    }

    /**
     * Check if auction is still active
     */
    public boolean isActive() {
        return status == EnumAuctionStatus.ACTIVE && !isExpired();
    }

    /**
     * Get time remaining in milliseconds
     */
    public long getTimeRemaining() {
        return Math.max(0, expirationTime - System.currentTimeMillis());
    }

    /**
     * Get formatted time remaining
     */
    public String getTimeRemainingFormatted() {
        long remaining = getTimeRemaining();

        if (remaining <= 0) {
            return "Ended";
        }

        long hours = remaining / (60L * 60L * 1000L);
        long minutes = (remaining % (60L * 60L * 1000L)) / (60L * 1000L);

        if (hours > 24) {
            long days = hours / 24;
            hours = hours % 24;
            return days + "d " + hours + "h";
        } else if (hours > 0) {
            return hours + "h " + minutes + "m";
        } else {
            long seconds = (remaining % (60L * 1000L)) / 1000L;
            return minutes + "m " + seconds + "s";
        }
    }

    /**
     * Check if auction has any bids
     */
    public boolean hasBids() {
        return highBidderUUID != null;
    }

    // ==================== Bidding Methods ====================

    /**
     * Calculate minimum valid bid amount
     */
    public long getMinimumBid() {
        if (!hasBids()) {
            return startingPrice;
        }
        // Minimum increment is configured percent or 1, whichever is greater
        long minIncrement = Math.max(1, (long) (currentBid * ConfigMarket.MinBidIncrementPercent));
        return currentBid + minIncrement;
    }

    /**
     * Place a bid
     * @return true if bid was successful
     */
    public boolean placeBid(UUID bidder, String bidderName, long amount) {
        if (status != EnumAuctionStatus.ACTIVE) {
            return false;
        }
        if (isExpired()) {
            return false;
        }
        if (amount < getMinimumBid()) {
            return false;
        }
        if (bidder.equals(sellerUUID)) {
            return false;  // Can't bid on own item
        }

        // Store previous bidder for refund notification
        if (highBidderUUID != null) {
            previousBidderUUID = highBidderUUID;
            previousBidderName = highBidderName;
            previousBidAmount = currentBid;
        }

        // Update high bidder
        highBidderUUID = bidder;
        highBidderName = bidderName;
        currentBid = amount;

        // Snipe protection - extend if bid placed in final minutes
        if (ConfigMarket.SnipeProtectionMinutes > 0) {
            long timeRemaining = getTimeRemaining();
            long thresholdMs = ConfigMarket.SnipeProtectionThreshold * 60L * 1000L;

            if (timeRemaining < thresholdMs) {
                // Extend TO snipe protection time, not by it
                long newExpiration = System.currentTimeMillis() +
                    (ConfigMarket.SnipeProtectionMinutes * 60L * 1000L);
                // Only extend if it would actually extend the auction
                if (newExpiration > expirationTime) {
                    expirationTime = newExpiration;
                }
            }
        }

        return true;
    }

    /**
     * Execute buyout (instant purchase at buyout price)
     * @return true if buyout was successful
     */
    public boolean buyout(UUID buyer, String buyerName) {
        if (status != EnumAuctionStatus.ACTIVE) {
            return false;
        }
        if (isExpired()) {
            return false;
        }
        if (buyoutPrice <= 0) {
            return false;  // No buyout available
        }
        if (buyer.equals(sellerUUID)) {
            return false;  // Can't buy own item
        }

        // Store previous bidder for refund
        if (highBidderUUID != null) {
            previousBidderUUID = highBidderUUID;
            previousBidderName = highBidderName;
            previousBidAmount = currentBid;
        }

        highBidderUUID = buyer;
        highBidderName = buyerName;
        currentBid = buyoutPrice;
        status = EnumAuctionStatus.SOLD;

        return true;
    }

    /**
     * Cancel listing (seller only, before any bids)
     * @return true if cancellation was successful
     */
    public boolean cancel(UUID requester) {
        if (!requester.equals(sellerUUID)) {
            return false;  // Only seller can cancel
        }
        if (hasBids()) {
            return false;  // Can't cancel with active bids
        }
        if (status != EnumAuctionStatus.ACTIVE) {
            return false;
        }

        status = EnumAuctionStatus.CANCELLED;
        return true;
    }

    /**
     * Finalize auction when it expires
     * Call this when auction timer runs out
     */
    public void finalize() {
        if (status != EnumAuctionStatus.ACTIVE) {
            return;
        }

        if (hasBids()) {
            status = EnumAuctionStatus.SOLD;
        } else {
            status = EnumAuctionStatus.EXPIRED;
        }
    }

    // ==================== Validation ====================

    /**
     * Validate that the item can be listed
     * @return null if valid, error message if invalid
     */
    public static String validateItem(ItemStack item) {
        if (item == null || item.getItem() == null) {
            return "Invalid item";
        }

        // Check trade restrictions (Untradeable, ProfileSlot Bound, Soulbound)
        if (!ItemTradeAttribute.canTrade(item)) {
            String reason = ItemTradeAttribute.getTradeBlockReason(item);
            return reason != null ? reason : "Item cannot be traded";
        }

        // Check blacklist
        if (AuctionBlacklist.Instance != null && AuctionBlacklist.Instance.isBlacklisted(item)) {
            return "Item is blacklisted from auction";
        }

        return null;  // Valid
    }

    // ==================== Financial Calculations ====================

    /**
     * Get listing fee for this auction
     */
    public long getListingFee() {
        long flatFee = duration.getListingFee();
        long percentFee = (long) (startingPrice * ConfigMarket.ListingFeePercent);
        return flatFee + percentFee;
    }

    /**
     * Get seller's proceeds after sales tax
     */
    public long getSellerProceeds() {
        if (status != EnumAuctionStatus.SOLD) {
            return 0;
        }
        long tax = (long) (currentBid * ConfigMarket.SalesTaxPercent);
        return currentBid - tax;
    }

    // ==================== NBT Serialization ====================

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger("Id", id);
        compound.setString("SellerUUID", sellerUUID.toString());
        compound.setString("SellerName", sellerName);
        compound.setTag("Item", item.writeToNBT(new NBTTagCompound()));
        compound.setLong("StartPrice", startingPrice);
        compound.setLong("BuyoutPrice", buyoutPrice);
        compound.setLong("CurrentBid", currentBid);

        if (highBidderUUID != null) {
            compound.setString("BidderUUID", highBidderUUID.toString());
            compound.setString("BidderName", highBidderName);
        }

        if (previousBidderUUID != null) {
            compound.setString("PrevBidderUUID", previousBidderUUID.toString());
            compound.setString("PrevBidderName", previousBidderName);
            compound.setLong("PrevBidAmount", previousBidAmount);
        }

        compound.setLong("ListingTime", listingTime);
        compound.setLong("ExpirationTime", expirationTime);
        compound.setInteger("Duration", duration.ordinal());
        compound.setInteger("Status", status.ordinal());
        compound.setInteger("Category", category.ordinal());
        compound.setBoolean("SellerClaimed", sellerClaimed);
        compound.setBoolean("BuyerClaimed", buyerClaimed);

        return compound;
    }

    public void readFromNBT(NBTTagCompound compound) {
        id = compound.getInteger("Id");
        sellerUUID = UUID.fromString(compound.getString("SellerUUID"));
        sellerName = compound.getString("SellerName");
        item = ItemStack.loadItemStackFromNBT(compound.getCompoundTag("Item"));
        startingPrice = compound.getLong("StartPrice");
        buyoutPrice = compound.getLong("BuyoutPrice");
        currentBid = compound.getLong("CurrentBid");

        if (compound.hasKey("BidderUUID")) {
            highBidderUUID = UUID.fromString(compound.getString("BidderUUID"));
            highBidderName = compound.getString("BidderName");
        }

        if (compound.hasKey("PrevBidderUUID")) {
            previousBidderUUID = UUID.fromString(compound.getString("PrevBidderUUID"));
            previousBidderName = compound.getString("PrevBidderName");
            previousBidAmount = compound.getLong("PrevBidAmount");
        }

        listingTime = compound.getLong("ListingTime");
        expirationTime = compound.getLong("ExpirationTime");

        int durationOrdinal = compound.getInteger("Duration");
        if (durationOrdinal >= 0 && durationOrdinal < EnumAuctionDuration.values().length) {
            duration = EnumAuctionDuration.values()[durationOrdinal];
        } else {
            duration = EnumAuctionDuration.LONG;
        }

        int statusOrdinal = compound.getInteger("Status");
        if (statusOrdinal >= 0 && statusOrdinal < EnumAuctionStatus.values().length) {
            status = EnumAuctionStatus.values()[statusOrdinal];
        } else {
            status = EnumAuctionStatus.ACTIVE;
        }

        int categoryOrdinal = compound.getInteger("Category");
        if (categoryOrdinal >= 0 && categoryOrdinal < EnumAuctionCategory.values().length) {
            category = EnumAuctionCategory.values()[categoryOrdinal];
        } else {
            category = EnumAuctionCategory.MISC;
        }

        sellerClaimed = compound.getBoolean("SellerClaimed");
        buyerClaimed = compound.getBoolean("BuyerClaimed");
    }
}
