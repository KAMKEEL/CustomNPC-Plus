package noppes.npcs.controllers;

import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
import kamkeel.npcs.util.VaultUtil;
import noppes.npcs.config.ConfigMarket;

/**
 * Server-side utility for syncing auction IConfiguration to clients.
 * Handles Vault integration for currency name resolution.
 * <p>
 * This class is SERVER-SIDE ONLY. Client reads via AuctionClientConfig.
 */
public class AuctionConfigSync {

    /**
     * Write auction config to NBT for sending to client.
     * Called on server side when building login packet.
     * <p>
     * Currency name is resolved from Vault if enabled and available,
     * otherwise falls back to ConfigMarket.CurrencyName.
     */
    public static INbt writeToNBT(INbt compound) {
        compound.setBoolean("AuctionEnabled", ConfigMarket.AuctionEnabled);
        compound.setLong("ListingFee", ConfigMarket.ListingFee);
        compound.setString("CurrencyName", getEffectiveCurrencyName());
        compound.setInteger("AuctionDurationHours", ConfigMarket.AuctionDurationHours);
        compound.setDouble("MinBidIncrement", ConfigMarket.MinBidIncrementPercent);
        compound.setInteger("MaxActiveListings", ConfigMarket.DefaultMaxTrades);
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
            if (vaultName != null && !vaultName.isEmpty()) {
                return vaultName;
            }
        }
        return ConfigMarket.CurrencyName;
    }
}
