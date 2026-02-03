package noppes.npcs.controllers.data;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.handler.data.IAuctionListing;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.config.ConfigMarket;
import noppes.npcs.constants.EnumAuctionStatus;
import noppes.npcs.scripted.NpcAPI;

import java.util.UUID;

public class AuctionListing implements IAuctionListing {
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

    // =========================================
    // IAuctionListing Implementation
    // =========================================

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getSellerUUID() {
        return sellerUUID != null ? sellerUUID.toString() : null;
    }

    @Override
    public String getSellerName() {
        return sellerName;
    }

    @Override
    public IItemStack getItem() {
        return item != null ? NpcAPI.Instance().getIItemStack(item) : null;
    }

    @Override
    public long getStartingPrice() {
        return startingPrice;
    }

    @Override
    public long getBuyoutPrice() {
        return buyoutPrice;
    }

    @Override
    public long getCurrentBid() {
        return currentBid;
    }

    @Override
    public String getHighBidderUUID() {
        return highBidderUUID != null ? highBidderUUID.toString() : null;
    }

    @Override
    public String getHighBidderName() {
        return highBidderName;
    }

    @Override
    public int getBidCount() {
        return bidCount;
    }

    @Override
    public long getCreatedTime() {
        return createdTime;
    }

    @Override
    public long getEndTime() {
        return endTime;
    }

    @Override
    public long getRemainingTime() {
        return getTimeRemaining();
    }

    @Override
    public int getStatus() {
        return status.ordinal();
    }

    @Override
    public long getMinimumBid() {
        return getMinimumBid(ConfigMarket.MinBidIncrementPercent);
    }

    @Override
    public boolean isSeller(String playerUUID) {
        if (playerUUID == null || sellerUUID == null) return false;
        return sellerUUID.toString().equals(playerUUID);
    }

    @Override
    public boolean isHighBidder(String playerUUID) {
        if (playerUUID == null || highBidderUUID == null) return false;
        return highBidderUUID.toString().equals(playerUUID);
    }

    @Override
    public String getRemainingTimeFormatted() {
        long remaining = getTimeRemaining();
        if (remaining <= 0) return "Ended";

        long hours = remaining / (60 * 60 * 1000);
        long minutes = (remaining % (60 * 60 * 1000)) / (60 * 1000);

        if (hours > 0) {
            return hours + "h " + minutes + "m";
        } else if (minutes > 0) {
            return minutes + "m";
        } else {
            long seconds = (remaining % (60 * 1000)) / 1000;
            return seconds + "s";
        }
    }

    // Override existing methods with @Override annotation
    @Override
    public boolean hasBids() {
        return bidCount > 0 && highBidderUUID != null;
    }

    @Override
    public boolean hasBuyout() {
        return buyoutPrice > 0;
    }

    @Override
    public boolean isExpired() {
        return System.currentTimeMillis() >= endTime;
    }

    @Override
    public boolean isActive() {
        return status == EnumAuctionStatus.ACTIVE && !isExpired();
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
