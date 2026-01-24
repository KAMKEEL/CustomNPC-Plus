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
    public ItemStack item;          // The item (for ITEM claims only)
    public String itemName;         // Display name of item (for CURRENCY/REFUND tooltip)
    public String otherPlayerName;  // Buyer name (CURRENCY) or who outbid (REFUND)
    public long currency;
    public long createdTime;
    public boolean claimed;
    public boolean isReturned;      // For ITEM claims: true if returned to seller, false if won by buyer

    public AuctionClaim() {
        this.id = UUID.randomUUID().toString();
        this.createdTime = System.currentTimeMillis();
        this.claimed = false;
        this.isReturned = false;
        this.itemName = "";
        this.otherPlayerName = "";
    }

    /** Create claim for winning an item (buyer won auction or buyout) */
    public static AuctionClaim createItemWonClaim(UUID playerUUID, String playerName, String listingId, ItemStack item) {
        AuctionClaim claim = new AuctionClaim();
        claim.playerUUID = playerUUID;
        claim.playerName = playerName;
        claim.listingId = listingId;
        claim.type = EnumClaimType.ITEM;
        claim.item = item != null ? item.copy() : null;
        claim.currency = 0;
        claim.isReturned = false;
        return claim;
    }

    /** Create claim for returned item (seller's expired/cancelled listing) */
    public static AuctionClaim createItemReturnedClaim(UUID playerUUID, String playerName, String listingId, ItemStack item) {
        AuctionClaim claim = new AuctionClaim();
        claim.playerUUID = playerUUID;
        claim.playerName = playerName;
        claim.listingId = listingId;
        claim.type = EnumClaimType.ITEM;
        claim.item = item != null ? item.copy() : null;
        claim.currency = 0;
        claim.isReturned = true;
        return claim;
    }

    /** Create claim for seller's proceeds (stores item name for tooltip, not full item) */
    public static AuctionClaim createCurrencyClaim(UUID playerUUID, String playerName, String listingId, long currency, String itemName, String buyerName) {
        AuctionClaim claim = new AuctionClaim();
        claim.playerUUID = playerUUID;
        claim.playerName = playerName;
        claim.listingId = listingId;
        claim.type = EnumClaimType.CURRENCY;
        claim.item = null;  // No item stored for currency claims
        claim.itemName = itemName != null ? itemName : "";
        claim.otherPlayerName = buyerName != null ? buyerName : "";
        claim.currency = currency;
        claim.isReturned = false;
        return claim;
    }

    /** Create claim for outbid refund (stores item for display and rebid option) */
    public static AuctionClaim createRefundClaim(UUID playerUUID, String playerName, String listingId, long currency, String itemName, String outbidderName, ItemStack item) {
        AuctionClaim claim = new AuctionClaim();
        claim.playerUUID = playerUUID;
        claim.playerName = playerName;
        claim.listingId = listingId;
        claim.type = EnumClaimType.REFUND;
        claim.item = item != null ? item.copy() : null;  // Store item for display and rebid
        claim.itemName = itemName != null ? itemName : "";
        claim.otherPlayerName = outbidderName != null ? outbidderName : "";
        claim.currency = currency;
        claim.isReturned = false;
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

        compound.setString("ItemName", itemName != null ? itemName : "");
        compound.setString("OtherPlayerName", otherPlayerName != null ? otherPlayerName : "");
        compound.setLong("Currency", currency);
        compound.setLong("CreatedTime", createdTime);
        compound.setBoolean("Claimed", claimed);
        compound.setBoolean("IsReturned", isReturned);

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

        itemName = compound.getString("ItemName");
        otherPlayerName = compound.getString("OtherPlayerName");
        currency = compound.getLong("Currency");
        createdTime = compound.getLong("CreatedTime");
        claimed = compound.getBoolean("Claimed");
        isReturned = compound.getBoolean("IsReturned");
    }

    public static AuctionClaim fromNBT(NBTTagCompound compound) {
        AuctionClaim claim = new AuctionClaim();
        claim.readFromNBT(compound);
        return claim;
    }
}
