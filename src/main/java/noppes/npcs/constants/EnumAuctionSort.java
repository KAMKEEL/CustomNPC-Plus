package noppes.npcs.constants;

import net.minecraft.util.StatCollector;

public enum EnumAuctionSort {
    NEWEST("auction.sort.newest"),
    ENDING_SOON("auction.sort.endingSoon"),
    PRICE_LOW("auction.sort.priceLow"),
    PRICE_HIGH("auction.sort.priceHigh"),
    MOST_BIDS("auction.sort.mostBids");

    private final String langKey;

    EnumAuctionSort(String langKey) {
        this.langKey = langKey;
    }

    public String getLangKey() {
        return langKey;
    }

    public String getDisplayName() {
        return StatCollector.translateToLocal(langKey);
    }

    public static String[] getDisplayNames() {
        String[] names = new String[values().length];
        for (int i = 0; i < values().length; i++) {
            names[i] = values()[i].getDisplayName();
        }
        return names;
    }

    public static EnumAuctionSort fromOrdinal(int ordinal) {
        if (ordinal >= 0 && ordinal < values().length) {
            return values()[ordinal];
        }
        return NEWEST;
    }
}
