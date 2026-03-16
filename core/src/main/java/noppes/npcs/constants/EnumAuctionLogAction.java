package noppes.npcs.constants;

import noppes.npcs.config.ConfigMarket;

/**
 * Enum for auction log action types.
 * Used by AuctionController.logAuction() for type-safe logging.
 */
public enum EnumAuctionLogAction {
    CREATED,
    BID,
    BUYOUT,
    SOLD,
    EXPIRED,
    CANCELLED,
    CLAIMED,
    CLAIM_EXPIRED;

    /**
     * Check if this action should be logged based on config settings
     */
    public boolean shouldLog() {
        if (!ConfigMarket.AuctionLoggingEnabled) return false;
        switch (this) {
            case CREATED:
                return ConfigMarket.LogAuctionCreated;
            case BID:
                return ConfigMarket.LogAuctionBid;
            case BUYOUT:
                return ConfigMarket.LogAuctionBuyout;
            case SOLD:
                return ConfigMarket.LogAuctionSold;
            case EXPIRED:
                return ConfigMarket.LogAuctionExpired;
            case CANCELLED:
                return ConfigMarket.LogAuctionCancelled;
            case CLAIMED:
            case CLAIM_EXPIRED:
                return ConfigMarket.LogAuctionClaimed;
            default:
                return false;
        }
    }
}
