package noppes.npcs.constants;

import net.minecraft.util.StatCollector;
import noppes.npcs.config.ConfigMarket;

/**
 * Duration options for auction listings.
 * Values are configurable via ConfigMarket.
 */
public enum EnumAuctionDuration {
    SHORT("auction.duration.short"),
    MEDIUM("auction.duration.medium"),
    LONG("auction.duration.long"),
    VERY_LONG("auction.duration.veryLong");

    private final String langKey;

    EnumAuctionDuration(String langKey) {
        this.langKey = langKey;
    }

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
     * Get duration in hours
     */
    public int getHours() {
        switch (this) {
            case SHORT:
                return ConfigMarket.AuctionDurationShort;
            case MEDIUM:
                return ConfigMarket.AuctionDurationMedium;
            case LONG:
                return ConfigMarket.AuctionDurationLong;
            case VERY_LONG:
                return ConfigMarket.AuctionDurationVeryLong;
            default:
                return 24;
        }
    }

    /**
     * Get display name for GUI
     */
    public String getDisplayName() {
        return StatCollector.translateToLocal(langKey);
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
