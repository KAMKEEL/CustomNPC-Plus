package noppes.npcs.constants;

import net.minecraft.util.StatCollector;

/**
 * Sorting options for auction listings.
 */
public enum EnumAuctionSort {
    NEWEST_FIRST("auction.sort.newest"),
    OLDEST_FIRST("auction.sort.oldest"),
    PRICE_HIGH_TO_LOW("auction.sort.priceHigh"),
    PRICE_LOW_TO_HIGH("auction.sort.priceLow"),
    ENDING_SOON("auction.sort.endingSoon"),
    MOST_TIME_LEFT("auction.sort.mostTime"),
    NAME_A_TO_Z("auction.sort.nameAZ"),
    NAME_Z_TO_A("auction.sort.nameZA"),
    MOST_BIDS("auction.sort.mostBids"),
    LEAST_BIDS("auction.sort.leastBids");

    private final String langKey;

    EnumAuctionSort(String langKey) {
        this.langKey = langKey;
    }

    public String getDisplayName() {
        return StatCollector.translateToLocal(langKey);
    }

    public EnumAuctionSort next() {
        EnumAuctionSort[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }

    public EnumAuctionSort previous() {
        EnumAuctionSort[] values = values();
        return values[(this.ordinal() - 1 + values.length) % values.length];
    }
}
