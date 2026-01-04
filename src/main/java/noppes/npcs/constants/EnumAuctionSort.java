package noppes.npcs.constants;

/**
 * Sorting options for auction listings.
 */
public enum EnumAuctionSort {
    /** Default - newest listings first */
    NEWEST_FIRST("Newest First"),

    /** Oldest listings first */
    OLDEST_FIRST("Oldest First"),

    /** Highest current price first */
    PRICE_HIGH_TO_LOW("Most Expensive"),

    /** Lowest current price first */
    PRICE_LOW_TO_HIGH("Least Expensive"),

    /** Buyout price high to low (listings without buyout at end) */
    BUYOUT_HIGH_TO_LOW("Buyout: High to Low"),

    /** Buyout price low to high (listings without buyout at end) */
    BUYOUT_LOW_TO_HIGH("Buyout: Low to High"),

    /** Ending soonest first */
    ENDING_SOON("Ending Soon"),

    /** Most time remaining first */
    MOST_TIME_LEFT("Most Time Left"),

    /** Alphabetical by item name A-Z */
    NAME_A_TO_Z("Name: A to Z"),

    /** Alphabetical by item name Z-A */
    NAME_Z_TO_A("Name: Z to A"),

    /** Most bids first */
    MOST_BIDS("Most Bids"),

    /** Least bids first */
    LEAST_BIDS("Least Bids");

    private final String displayName;

    EnumAuctionSort(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get next sort option (for cycling through in GUI)
     */
    public EnumAuctionSort next() {
        EnumAuctionSort[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }

    /**
     * Get previous sort option
     */
    public EnumAuctionSort previous() {
        EnumAuctionSort[] values = values();
        return values[(this.ordinal() - 1 + values.length) % values.length];
    }
}
