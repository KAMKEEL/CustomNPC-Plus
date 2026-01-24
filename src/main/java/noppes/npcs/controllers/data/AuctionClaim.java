package noppes.npcs.controllers.data;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.constants.EnumClaimType;

import java.util.UUID;

public class AuctionClaim {
    public String id;
    public UUID playerUUID;
    public String playerName;
    public String listingId;
    public EnumClaimType type;
    public ItemStack item;
    public long currency;
    public long createdTime;
    public boolean claimed;

    public AuctionClaim() {
        this.id = UUID.randomUUID().toString();
        this.createdTime = System.currentTimeMillis();
        this.claimed = false;
    }

    public static AuctionClaim createItemClaim(UUID playerUUID, String playerName, String listingId, ItemStack item) {
        AuctionClaim claim = new AuctionClaim();
        claim.playerUUID = playerUUID;
        claim.playerName = playerName;
        claim.listingId = listingId;
        claim.type = EnumClaimType.ITEM;
        claim.item = item != null ? item.copy() : null;
        claim.currency = 0;
        return claim;
    }

    public static AuctionClaim createCurrencyClaim(UUID playerUUID, String playerName, String listingId, long currency) {
        AuctionClaim claim = new AuctionClaim();
        claim.playerUUID = playerUUID;
        claim.playerName = playerName;
        claim.listingId = listingId;
        claim.type = EnumClaimType.CURRENCY;
        claim.item = null;
        claim.currency = currency;
        return claim;
    }

    public static AuctionClaim createRefundClaim(UUID playerUUID, String playerName, String listingId, long currency) {
        AuctionClaim claim = new AuctionClaim();
        claim.playerUUID = playerUUID;
        claim.playerName = playerName;
        claim.listingId = listingId;
        claim.type = EnumClaimType.REFUND;
        claim.item = null;
        claim.currency = currency;
        return claim;
    }

    public boolean isExpired(int expirationDays) {
        long expirationMs = expirationDays * 24L * 60L * 60L * 1000L;
        return System.currentTimeMillis() - createdTime > expirationMs;
    }

    public long getDaysUntilExpiration(int expirationDays) {
        long expirationMs = expirationDays * 24L * 60L * 60L * 1000L;
        long remaining = (createdTime + expirationMs) - System.currentTimeMillis();
        return Math.max(0, remaining / (24L * 60L * 60L * 1000L));
    }

    public boolean isForPlayer(UUID uuid) {
        return playerUUID != null && playerUUID.equals(uuid);
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setString("ID", id);
        compound.setString("PlayerUUID", playerUUID != null ? playerUUID.toString() : "");
        compound.setString("PlayerName", playerName != null ? playerName : "");
        compound.setString("ListingID", listingId != null ? listingId : "");
        compound.setInteger("Type", type.ordinal());

        if (item != null) {
            NBTTagCompound itemTag = new NBTTagCompound();
            item.writeToNBT(itemTag);
            compound.setTag("Item", itemTag);
        }

        compound.setLong("Currency", currency);
        compound.setLong("CreatedTime", createdTime);
        compound.setBoolean("Claimed", claimed);

        return compound;
    }

    public void readFromNBT(NBTTagCompound compound) {
        id = compound.getString("ID");

        String playerUUIDStr = compound.getString("PlayerUUID");
        playerUUID = playerUUIDStr.isEmpty() ? null : UUID.fromString(playerUUIDStr);
        playerName = compound.getString("PlayerName");

        listingId = compound.getString("ListingID");
        type = EnumClaimType.fromOrdinal(compound.getInteger("Type"));

        if (compound.hasKey("Item")) {
            item = ItemStack.loadItemStackFromNBT(compound.getCompoundTag("Item"));
        } else {
            item = null;
        }

        currency = compound.getLong("Currency");
        createdTime = compound.getLong("CreatedTime");
        claimed = compound.getBoolean("Claimed");
    }

    public static AuctionClaim fromNBT(NBTTagCompound compound) {
        AuctionClaim claim = new AuctionClaim();
        claim.readFromNBT(compound);
        return claim;
    }
}
