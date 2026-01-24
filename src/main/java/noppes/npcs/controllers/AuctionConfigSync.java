package noppes.npcs.controllers;

import kamkeel.npcs.util.VaultUtil;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.config.ConfigMarket;

/**
 * Server-side utility for syncing auction configuration to clients.
 * Handles Vault integration for currency name resolution.
 *
 * This class is SERVER-SIDE ONLY. Client reads via AuctionClientConfig.
 */
public class AuctionConfigSync {

    /**
     * Write auction config to NBT for sending to client.
     * Called on server side when building login packet.
     *
     * Currency name is resolved from Vault if enabled and available,
     * otherwise falls back to ConfigMarket.CurrencyName.
     */
    public static NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setBoolean("AuctionEnabled", ConfigMarket.AuctionEnabled);
        compound.setLong("ListingFee", ConfigMarket.ListingFee);
        compound.setString("CurrencyName", getEffectiveCurrencyName());
        compound.setInteger("AuctionDurationHours", ConfigMarket.AuctionDurationHours);
        compound.setDouble("MinBidIncrement", ConfigMarket.MinBidIncrementPercent);
        compound.setInteger("MaxActiveListings", ConfigMarket.DefaultMaxListings);
        compound.setInteger("ClaimExpirationDays", ConfigMarket.ClaimExpirationDays);
        return compound;
    }

    /**
     * Get the effective currency name for display.
     * Uses Vault currency name if Vault is enabled and available,
     * otherwise uses ConfigMarket.CurrencyName.
     *
     * @return The currency name to display
     */
    public static String getEffectiveCurrencyName() {
        if (ConfigMarket.UseVault && VaultUtil.isEnabled()) {
            String vaultName = VaultUtil.getCurrencyNamePlural();
            if (vaultName != null && !vaultName.isEmpty() && !vaultName.equals("coins")) {
                return vaultName;
            }
        }
        return ConfigMarket.CurrencyName;
    }
}
