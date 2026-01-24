package noppes.npcs.client;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Client-side cache for auction configuration synced from server.
 * Prevents client from using local ConfigMarket which may differ from server settings.
 *
 * IMPORTANT: This class is CLIENT-ONLY. Server-side NBT writing is done in
 * AuctionConfigSync to avoid @SideOnly crashes on dedicated servers.
 */
@SideOnly(Side.CLIENT)
public class AuctionClientConfig {
    // Cached values from server
    private static long listingFee = 0;
    private static String currencyName = "Coins";
    private static int auctionDurationHours = 24;
    private static double minBidIncrement = 0.05;
    private static int maxActiveListings = 8;
    private static int claimExpirationDays = 20;
    private static boolean auctionEnabled = false;

    /**
     * Update cached config from server NBT data.
     * Called when receiving login packet.
     */
    public static void readFromNBT(NBTTagCompound compound) {
        if (compound == null) return;

        auctionEnabled = compound.getBoolean("AuctionEnabled");
        listingFee = compound.getLong("ListingFee");
        currencyName = compound.getString("CurrencyName");
        auctionDurationHours = compound.getInteger("AuctionDurationHours");
        minBidIncrement = compound.getDouble("MinBidIncrement");
        maxActiveListings = compound.getInteger("MaxActiveListings");
        claimExpirationDays = compound.getInteger("ClaimExpirationDays");

        if (currencyName.isEmpty()) {
            currencyName = "Gold";
        }
    }

    /**
     * Reset to defaults (called on disconnect).
     */
    public static void reset() {
        listingFee = 0;
        currencyName = "Coins";
        auctionDurationHours = 24;
        minBidIncrement = 0.05;
        maxActiveListings = 8;
        claimExpirationDays = 20;
        auctionEnabled = false;
    }

    // Getters
    public static boolean isAuctionEnabled() { return auctionEnabled; }
    public static long getListingFee() { return listingFee; }
    public static String getCurrencyName() { return currencyName; }
    public static int getAuctionDurationHours() { return auctionDurationHours; }
    public static double getMinBidIncrement() { return minBidIncrement; }
    public static int getMaxActiveListings() { return maxActiveListings; }
    public static int getClaimExpirationDays() { return claimExpirationDays; }
}
