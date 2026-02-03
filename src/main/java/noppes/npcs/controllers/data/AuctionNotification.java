package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.constants.EnumNotificationType;

import java.util.UUID;

public class AuctionNotification {
    public String id;
    public UUID playerUUID;
    public EnumNotificationType type;
    public String listingId;
    public String message;
    public long timestamp;
    public boolean sent;

    public AuctionNotification() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.sent = false;
    }

    public AuctionNotification(UUID playerUUID, EnumNotificationType type, String listingId, String message) {
        this();
        this.playerUUID = playerUUID;
        this.type = type;
        this.listingId = listingId;
        this.message = message;
    }

    public boolean isForPlayer(UUID uuid) {
        return playerUUID != null && playerUUID.equals(uuid);
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setString("ID", id);
        compound.setString("PlayerUUID", playerUUID != null ? playerUUID.toString() : "");
        compound.setInteger("Type", type.ordinal());
        compound.setString("ListingID", listingId != null ? listingId : "");
        compound.setString("Message", message != null ? message : "");
        compound.setLong("Timestamp", timestamp);
        compound.setBoolean("Sent", sent);
        return compound;
    }

    public void readFromNBT(NBTTagCompound compound) {
        id = compound.getString("ID");

        String playerUUIDStr = compound.getString("PlayerUUID");
        playerUUID = playerUUIDStr.isEmpty() ? null : UUID.fromString(playerUUIDStr);

        type = EnumNotificationType.fromOrdinal(compound.getInteger("Type"));
        listingId = compound.getString("ListingID");
        message = compound.getString("Message");
        timestamp = compound.getLong("Timestamp");
        sent = compound.getBoolean("Sent");
    }

    public static AuctionNotification fromNBT(NBTTagCompound compound) {
        AuctionNotification notification = new AuctionNotification();
        notification.readFromNBT(compound);
        return notification;
    }
}
