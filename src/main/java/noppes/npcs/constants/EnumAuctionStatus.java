package noppes.npcs.constants;

/**
 * Status of an auction listing.
 */
public enum EnumAuctionStatus {
    ACTIVE,      // Currently listed and accepting bids
    SOLD,        // Successfully sold (buyout or auction end with bids)
    EXPIRED,     // Ended without any bids
    CANCELLED,   // Cancelled by seller (only allowed before first bid)
    CLAIMED;     // Item/money has been claimed by respective parties

    /**
     * Get display name for GUI
     */
    public String getDisplayName() {
        switch (this) {
            case ACTIVE: return "Active";
            case SOLD: return "Sold";
            case EXPIRED: return "Expired";
            case CANCELLED: return "Cancelled";
            case CLAIMED: return "Claimed";
            default: return "Unknown";
        }
    }

    /**
     * Get display color for GUI (MC color codes)
     */
    public char getColorCode() {
        switch (this) {
            case ACTIVE: return 'a';   // Green
            case SOLD: return '6';     // Gold
            case EXPIRED: return '7';  // Gray
            case CANCELLED: return 'c'; // Red
            case CLAIMED: return '8';  // Dark gray
            default: return 'f';       // White
        }
    }

    /**
     * Check if listing can still receive bids
     */
    public boolean canBid() {
        return this == ACTIVE;
    }

    /**
     * Check if listing needs claiming (item or money)
     */
    public boolean needsClaiming() {
        return this == SOLD || this == EXPIRED || this == CANCELLED;
    }
}
