package noppes.npcs.controllers.data;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.constants.EnumAuctionStatus;

import java.util.UUID;

public class AuctionListing {
    public String id;
    public UUID sellerUUID;
    public String sellerName;
    public ItemStack item;
    public long startingPrice;
    public long buyoutPrice;
    public long currentBid;
    public UUID highBidderUUID;
    public String highBidderName;
    public int bidCount;
    public long createdTime;
    public long endTime;
    public EnumAuctionStatus status;

    public AuctionListing() {
        this.id = UUID.randomUUID().toString();
        this.status = EnumAuctionStatus.ACTIVE;
        this.bidCount = 0;
        this.currentBid = 0;
        this.createdTime = System.currentTimeMillis();
    }

    public AuctionListing(UUID sellerUUID, String sellerName, ItemStack item, long startingPrice, long buyoutPrice, long durationMs) {
        this();
        this.sellerUUID = sellerUUID;
        this.sellerName = sellerName;
        this.item = item.copy();
        this.startingPrice = startingPrice;
        this.buyoutPrice = buyoutPrice;
        this.currentBid = 0;
        this.endTime = this.createdTime + durationMs;
    }

    public boolean hasBids() {
        return bidCount > 0 && highBidderUUID != null;
    }

    public boolean hasBuyout() {
        return buyoutPrice > 0;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= endTime;
    }

    public boolean isActive() {
        return status == EnumAuctionStatus.ACTIVE && !isExpired();
    }

    public long getTimeRemaining() {
        return Math.max(0, endTime - System.currentTimeMillis());
    }

    public long getEffectivePrice() {
        return hasBids() ? currentBid : startingPrice;
    }

    public long getMinimumBid(double minIncrementPercent) {
        if (!hasBids()) {
            return startingPrice;
        }
        long increment = (long) Math.ceil(currentBid * minIncrementPercent);
        return currentBid + Math.max(1, increment);
    }

    public boolean isSeller(UUID playerUUID) {
        return sellerUUID != null && sellerUUID.equals(playerUUID);
    }

    public boolean isHighBidder(UUID playerUUID) {
        return highBidderUUID != null && highBidderUUID.equals(playerUUID);
    }

    public void extendForSnipeProtection(int snipeProtectionMinutes) {
        long snipeProtectionMs = snipeProtectionMinutes * 60 * 1000L;
        long timeRemaining = getTimeRemaining();
        if (timeRemaining < snipeProtectionMs) {
            endTime = System.currentTimeMillis() + snipeProtectionMs;
        }
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setString("ID", id);
        compound.setString("SellerUUID", sellerUUID != null ? sellerUUID.toString() : "");
        compound.setString("SellerName", sellerName != null ? sellerName : "");

        if (item != null) {
            NBTTagCompound itemTag = new NBTTagCompound();
            item.writeToNBT(itemTag);
            compound.setTag("Item", itemTag);
        }

        compound.setLong("StartingPrice", startingPrice);
        compound.setLong("BuyoutPrice", buyoutPrice);
        compound.setLong("CurrentBid", currentBid);

        compound.setString("HighBidderUUID", highBidderUUID != null ? highBidderUUID.toString() : "");
        compound.setString("HighBidderName", highBidderName != null ? highBidderName : "");

        compound.setInteger("BidCount", bidCount);
        compound.setLong("CreatedTime", createdTime);
        compound.setLong("EndTime", endTime);
        compound.setInteger("Status", status.ordinal());

        return compound;
    }

    public void readFromNBT(NBTTagCompound compound) {
        id = compound.getString("ID");

        String sellerUUIDStr = compound.getString("SellerUUID");
        sellerUUID = sellerUUIDStr.isEmpty() ? null : UUID.fromString(sellerUUIDStr);
        sellerName = compound.getString("SellerName");

        if (compound.hasKey("Item")) {
            item = ItemStack.loadItemStackFromNBT(compound.getCompoundTag("Item"));
        }

        startingPrice = compound.getLong("StartingPrice");
        buyoutPrice = compound.getLong("BuyoutPrice");
        currentBid = compound.getLong("CurrentBid");

        String highBidderUUIDStr = compound.getString("HighBidderUUID");
        highBidderUUID = highBidderUUIDStr.isEmpty() ? null : UUID.fromString(highBidderUUIDStr);
        highBidderName = compound.getString("HighBidderName");

        bidCount = compound.getInteger("BidCount");
        createdTime = compound.getLong("CreatedTime");
        endTime = compound.getLong("EndTime");
        status = EnumAuctionStatus.fromOrdinal(compound.getInteger("Status"));
    }

    public static AuctionListing fromNBT(NBTTagCompound compound) {
        AuctionListing listing = new AuctionListing();
        listing.readFromNBT(compound);
        return listing;
    }
}
