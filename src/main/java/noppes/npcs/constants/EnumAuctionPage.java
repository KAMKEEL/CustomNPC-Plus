package noppes.npcs.constants;

import noppes.npcs.constants.EnumGuiType;

/**
 * Enum for auction GUI page navigation.
 * Maps page indices to GUI types for packet-based navigation.
 */
public enum EnumAuctionPage {
    LISTINGS(EnumGuiType.PlayerAuction),
    SELL(EnumGuiType.PlayerAuctionSell),
    TRADES(EnumGuiType.PlayerAuctionTrades);

    private final EnumGuiType guiType;

    EnumAuctionPage(EnumGuiType guiType) {
        this.guiType = guiType;
    }

    public EnumGuiType getGuiType() {
        return guiType;
    }

    public static EnumAuctionPage fromOrdinal(int ordinal) {
        if (ordinal >= 0 && ordinal < values().length) {
            return values()[ordinal];
        }
        return LISTINGS;
    }
}
