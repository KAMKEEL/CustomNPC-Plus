package noppes.npcs.constants;

/**
 * Enum for trade slot types in the player's auction trades view.
 * Used by ContainerAuctionTrades to identify what each slot contains.
 */
public enum EnumTradeSlotType {
    EMPTY,
    SELLING,
    BIDDING,
    CLAIM;

    public boolean isSelling() {
        return this == SELLING;
    }

    public boolean isBidding() {
        return this == BIDDING;
    }

    public boolean isClaim() {
        return this == CLAIM;
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    public static EnumTradeSlotType fromOrdinal(int ordinal) {
        if (ordinal >= 0 && ordinal < values().length) {
            return values()[ordinal];
        }
        return EMPTY;
    }
}
