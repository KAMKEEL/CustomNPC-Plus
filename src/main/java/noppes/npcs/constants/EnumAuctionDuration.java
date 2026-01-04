package noppes.npcs.constants;

import noppes.npcs.config.ConfigMarket;

/**
 * Duration options for auction listings.
 * Values are configurable via ConfigMarket.
 */
public enum EnumAuctionDuration {
    SHORT,      // Default: 2 hours
    MEDIUM,     // Default: 12 hours
    LONG,       // Default: 24 hours
    VERY_LONG;  // Default: 48 hours

    /**
     * Get duration in milliseconds
     */
    public long getMillis() {
        switch (this) {
            case SHORT:
                return ConfigMarket.AuctionDurationShort * 60L * 60L * 1000L;
            case MEDIUM:
                return ConfigMarket.AuctionDurationMedium * 60L * 60L * 1000L;
            case LONG:
                return ConfigMarket.AuctionDurationLong * 60L * 60L * 1000L;
            case VERY_LONG:
                return ConfigMarket.AuctionDurationVeryLong * 60L * 60L * 1000L;
            default:
                return 24 * 60L * 60L * 1000L;
        }
    }

    /**
     * Get display name for GUI
     */
    public String getDisplayName() {
        switch (this) {
            case SHORT:
                return ConfigMarket.AuctionDurationShort + " Hours";
            case MEDIUM:
                return ConfigMarket.AuctionDurationMedium + " Hours";
            case LONG:
                return ConfigMarket.AuctionDurationLong + " Hours";
            case VERY_LONG:
                return ConfigMarket.AuctionDurationVeryLong + " Hours";
            default:
                return "Unknown";
        }
    }

    /**
     * Get listing fee for this duration
     */
    public long getListingFee() {
        switch (this) {
            case SHORT:
                return ConfigMarket.AuctionFeeShort;
            case MEDIUM:
                return ConfigMarket.AuctionFeeMedium;
            case LONG:
                return ConfigMarket.AuctionFeeLong;
            case VERY_LONG:
                return ConfigMarket.AuctionFeeVeryLong;
            default:
                return 0;
        }
    }
}
