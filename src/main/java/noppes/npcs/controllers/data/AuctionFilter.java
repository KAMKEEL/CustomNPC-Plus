package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.constants.EnumAuctionCategory;
import noppes.npcs.constants.EnumAuctionSort;

/**
 * Filter/search criteria for auction listings.
 * Used to query the auction controller with specific criteria.
 */
public class AuctionFilter {

    /** Search text to match against item names */
    public String searchText = "";

    /** Category to filter by (ALL for no category filter) */
    public EnumAuctionCategory category = EnumAuctionCategory.ALL;

    /** How to sort results */
    public EnumAuctionSort sortOrder = EnumAuctionSort.ENDING_SOON;

    /** Minimum current bid/price filter */
    public long minPrice = 0;

    /** Maximum current bid/price filter (0 = no max) */
    public long maxPrice = 0;

    /** Only show listings with buyout option */
    public boolean buyoutOnly = false;

    /** Only show listings without any bids yet */
    public boolean noBidsOnly = false;

    /** Page number for pagination (0-indexed) */
    public int page = 0;

    /** Items per page */
    public int pageSize = 20;

    /** Filter by specific seller UUID string (empty = all sellers) */
    public String sellerUUID = "";

    /** Filter by specific seller name (partial match, empty = all) */
    public String sellerName = "";

    public AuctionFilter() {
    }

    /**
     * Create a filter with search text
     */
    public static AuctionFilter search(String text) {
        AuctionFilter filter = new AuctionFilter();
        filter.searchText = text != null ? text : "";
        return filter;
    }

    /**
     * Create a filter with category
     */
    public static AuctionFilter category(EnumAuctionCategory category) {
        AuctionFilter filter = new AuctionFilter();
        filter.category = category;
        return filter;
    }

    /**
     * Create a filter for a specific seller
     */
    public static AuctionFilter seller(String sellerUUID) {
        AuctionFilter filter = new AuctionFilter();
        filter.sellerUUID = sellerUUID != null ? sellerUUID : "";
        return filter;
    }

    // Builder-style methods for chaining

    public AuctionFilter withSearch(String text) {
        this.searchText = text != null ? text : "";
        return this;
    }

    public AuctionFilter withCategory(EnumAuctionCategory category) {
        this.category = category;
        return this;
    }

    public AuctionFilter withSort(EnumAuctionSort sort) {
        this.sortOrder = sort;
        return this;
    }

    public AuctionFilter withPriceRange(long min, long max) {
        this.minPrice = min;
        this.maxPrice = max;
        return this;
    }

    public AuctionFilter withBuyoutOnly(boolean buyoutOnly) {
        this.buyoutOnly = buyoutOnly;
        return this;
    }

    public AuctionFilter withNoBidsOnly(boolean noBidsOnly) {
        this.noBidsOnly = noBidsOnly;
        return this;
    }

    public AuctionFilter withPage(int page, int pageSize) {
        this.page = Math.max(0, page);
        this.pageSize = Math.max(1, Math.min(100, pageSize));
        return this;
    }

    public AuctionFilter withSeller(String sellerUUID, String sellerName) {
        this.sellerUUID = sellerUUID != null ? sellerUUID : "";
        this.sellerName = sellerName != null ? sellerName : "";
        return this;
    }

    /**
     * Check if a listing matches this filter's criteria
     */
    public boolean matches(AuctionListing listing) {
        if (listing == null) {
            return false;
        }

        // Search text filter (case-insensitive)
        if (!searchText.isEmpty()) {
            String lowerSearch = searchText.toLowerCase();
            String itemName = listing.item.getDisplayName().toLowerCase();
            if (!itemName.contains(lowerSearch)) {
                return false;
            }
        }

        // Category filter
        if (category != EnumAuctionCategory.ALL && listing.category != category) {
            return false;
        }

        // Price range filter (uses current bid or starting price)
        long price = listing.currentBid > 0 ? listing.currentBid : listing.startingPrice;
        if (minPrice > 0 && price < minPrice) {
            return false;
        }
        if (maxPrice > 0 && price > maxPrice) {
            return false;
        }

        // Buyout only filter
        if (buyoutOnly && listing.buyoutPrice <= 0) {
            return false;
        }

        // No bids only filter
        if (noBidsOnly && listing.hasBids()) {
            return false;
        }

        // Seller UUID filter
        if (!sellerUUID.isEmpty() && !listing.sellerUUID.toString().equals(sellerUUID)) {
            return false;
        }

        // Seller name filter (partial match)
        if (!sellerName.isEmpty()) {
            String lowerName = sellerName.toLowerCase();
            if (!listing.sellerName.toLowerCase().contains(lowerName)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Reset all filters to defaults
     */
    public void reset() {
        searchText = "";
        category = EnumAuctionCategory.ALL;
        sortOrder = EnumAuctionSort.ENDING_SOON;
        minPrice = 0;
        maxPrice = 0;
        buyoutOnly = false;
        noBidsOnly = false;
        page = 0;
        sellerUUID = "";
        sellerName = "";
    }

    /**
     * Write filter to NBT for network transmission
     */
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setString("SearchText", searchText);
        compound.setInteger("Category", category.ordinal());
        compound.setInteger("SortOrder", sortOrder.ordinal());
        compound.setLong("MinPrice", minPrice);
        compound.setLong("MaxPrice", maxPrice);
        compound.setBoolean("BuyoutOnly", buyoutOnly);
        compound.setBoolean("NoBidsOnly", noBidsOnly);
        compound.setInteger("Page", page);
        compound.setInteger("PageSize", pageSize);
        compound.setString("SellerUUID", sellerUUID);
        compound.setString("SellerName", sellerName);
        return compound;
    }

    /**
     * Read filter from NBT
     */
    public void readFromNBT(NBTTagCompound compound) {
        searchText = compound.getString("SearchText");

        int catOrdinal = compound.getInteger("Category");
        if (catOrdinal >= 0 && catOrdinal < EnumAuctionCategory.values().length) {
            category = EnumAuctionCategory.values()[catOrdinal];
        }

        int sortOrdinal = compound.getInteger("SortOrder");
        if (sortOrdinal >= 0 && sortOrdinal < EnumAuctionSort.values().length) {
            sortOrder = EnumAuctionSort.values()[sortOrdinal];
        }

        minPrice = compound.getLong("MinPrice");
        maxPrice = compound.getLong("MaxPrice");
        buyoutOnly = compound.getBoolean("BuyoutOnly");
        noBidsOnly = compound.getBoolean("NoBidsOnly");
        page = compound.getInteger("Page");
        pageSize = compound.getInteger("PageSize");
        if (pageSize <= 0) pageSize = 20;
        sellerUUID = compound.getString("SellerUUID");
        sellerName = compound.getString("SellerName");
    }

    @Override
    public String toString() {
        return "AuctionFilter{" +
            "search='" + searchText + '\'' +
            ", category=" + category +
            ", sort=" + sortOrder +
            ", price=" + minPrice + "-" + maxPrice +
            ", buyoutOnly=" + buyoutOnly +
            ", page=" + page +
            '}';
    }
}
