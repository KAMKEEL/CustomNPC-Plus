package noppes.npcs.controllers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import noppes.npcs.LogWriter;
import noppes.npcs.config.ConfigMarket;
import noppes.npcs.controllers.data.AuctionListing;

import java.util.*;

/**
 * Handles auction notifications for players.
 * Sends messages for outbid, won, sold, expired events.
 */
public class AuctionNotificationController {
    public static AuctionNotificationController Instance;

    // Pending notifications for offline players (UUID -> List of notifications)
    private Map<UUID, List<AuctionNotification>> pendingNotifications = new HashMap<>();

    public AuctionNotificationController() {
        Instance = this;
    }

    /**
     * Notify a player they have been outbid
     */
    public void notifyOutbid(AuctionListing listing, UUID outbidPlayerUUID,
                             String outbidPlayerName, long previousBid, long newBid) {
        String message = EnumChatFormatting.YELLOW + "[Auction] " + EnumChatFormatting.RED +
            "You have been outbid on " + EnumChatFormatting.WHITE +
            listing.item.getDisplayName() + EnumChatFormatting.RED +
            "! New bid: " + formatCurrency(newBid) +
            " (Your bid: " + formatCurrency(previousBid) + " has been refunded to claims)";

        sendOrQueueNotification(outbidPlayerUUID, NotificationType.OUTBID, message, listing.id);

        // Log the outbid event
        AuctionLogger.logOutbid(listing, outbidPlayerName, previousBid, listing.highBidderName, newBid);
    }

    /**
     * Notify a player they won an auction
     */
    public void notifyAuctionWon(AuctionListing listing) {
        if (listing.highBidderUUID == null) return;

        String message = EnumChatFormatting.YELLOW + "[Auction] " + EnumChatFormatting.GREEN +
            "Congratulations! You won the auction for " + EnumChatFormatting.WHITE +
            listing.item.getDisplayName() + EnumChatFormatting.GREEN +
            " for " + formatCurrency(listing.currentBid) + "! Visit an Auctioneer to claim your item.";

        sendOrQueueNotification(listing.highBidderUUID, NotificationType.WON, message, listing.id);

        // Log the win
        AuctionLogger.logAuctionWon(listing);
    }

    /**
     * Notify seller their auction sold
     */
    public void notifyAuctionSold(AuctionListing listing) {
        String message = EnumChatFormatting.YELLOW + "[Auction] " + EnumChatFormatting.GREEN +
            "Your " + EnumChatFormatting.WHITE + listing.item.getDisplayName() +
            EnumChatFormatting.GREEN + " sold for " + formatCurrency(listing.currentBid) +
            "! Visit an Auctioneer to claim " + formatCurrency(listing.getSellerProceeds()) + ".";

        sendOrQueueNotification(listing.sellerUUID, NotificationType.SOLD, message, listing.id);

        // Log the sale
        AuctionLogger.logAuctionSold(listing);
    }

    /**
     * Notify seller their auction expired with no bids
     */
    public void notifyAuctionExpired(AuctionListing listing) {
        String message = EnumChatFormatting.YELLOW + "[Auction] " + EnumChatFormatting.GRAY +
            "Your auction for " + EnumChatFormatting.WHITE + listing.item.getDisplayName() +
            EnumChatFormatting.GRAY + " has expired with no bids. Visit an Auctioneer to reclaim your item.";

        sendOrQueueNotification(listing.sellerUUID, NotificationType.EXPIRED, message, listing.id);

        // Log the expiration
        AuctionLogger.logAuctionExpired(listing);
    }

    /**
     * Notify player their auction was cancelled
     */
    public void notifyAuctionCancelled(AuctionListing listing, boolean byAdmin) {
        String message;
        if (byAdmin) {
            message = EnumChatFormatting.YELLOW + "[Auction] " + EnumChatFormatting.RED +
                "Your auction for " + EnumChatFormatting.WHITE + listing.item.getDisplayName() +
                EnumChatFormatting.RED + " was cancelled by an administrator.";
        } else {
            message = EnumChatFormatting.YELLOW + "[Auction] " + EnumChatFormatting.GRAY +
                "You cancelled your auction for " + EnumChatFormatting.WHITE +
                listing.item.getDisplayName() + EnumChatFormatting.GRAY + ".";
        }

        sendOrQueueNotification(listing.sellerUUID, NotificationType.CANCELLED, message, listing.id);

        // Log the cancellation
        AuctionLogger.logAuctionCancelled(listing, byAdmin);
    }

    /**
     * Notify about a new bid placed
     */
    public void notifyNewBid(AuctionListing listing, String bidderName, long bidAmount) {
        // Notify seller about new bid
        String sellerMessage = EnumChatFormatting.YELLOW + "[Auction] " + EnumChatFormatting.AQUA +
            bidderName + " placed a bid of " + formatCurrency(bidAmount) +
            " on your " + listing.item.getDisplayName() + "!";

        sendOrQueueNotification(listing.sellerUUID, NotificationType.NEW_BID, sellerMessage, listing.id);

        // Log the bid
        AuctionLogger.logBidPlaced(listing, bidderName, bidAmount);
    }

    /**
     * Send notification immediately if player online, otherwise queue for later
     */
    private void sendOrQueueNotification(UUID playerUUID, NotificationType type, String message, int listingId) {
        EntityPlayerMP player = getOnlinePlayer(playerUUID);

        if (player != null) {
            player.addChatMessage(new ChatComponentText(message));
        } else {
            // Queue for when player logs in
            AuctionNotification notification = new AuctionNotification(type, message, listingId);

            if (!pendingNotifications.containsKey(playerUUID)) {
                pendingNotifications.put(playerUUID, new ArrayList<>());
            }
            pendingNotifications.get(playerUUID).add(notification);

            // Limit queue size per player
            List<AuctionNotification> queue = pendingNotifications.get(playerUUID);
            while (queue.size() > 50) {
                queue.remove(0);
            }
        }
    }

    /**
     * Called when a player logs in - send any pending notifications
     */
    public void onPlayerLogin(EntityPlayerMP player) {
        UUID uuid = player.getUniqueID();

        if (pendingNotifications.containsKey(uuid)) {
            List<AuctionNotification> notifications = pendingNotifications.remove(uuid);

            if (!notifications.isEmpty()) {
                player.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.YELLOW + "[Auction] " + EnumChatFormatting.WHITE +
                    "You have " + notifications.size() + " auction notification(s):"));

                for (AuctionNotification notification : notifications) {
                    player.addChatMessage(new ChatComponentText(notification.message));
                }
            }
        }

        // Also check for claimable items
        if (AuctionController.Instance != null) {
            int claimableCount = AuctionController.Instance.getClaimableListings(uuid).size();
            if (claimableCount > 0) {
                player.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.YELLOW + "[Auction] " + EnumChatFormatting.GREEN +
                    "You have " + claimableCount + " item(s) waiting to be claimed!"));
            }
        }
    }

    private EntityPlayerMP getOnlinePlayer(UUID uuid) {
        if (noppes.npcs.CustomNpcs.getServer() == null) return null;

        for (Object obj : noppes.npcs.CustomNpcs.getServer().getConfigurationManager().playerEntityList) {
            EntityPlayerMP player = (EntityPlayerMP) obj;
            if (player.getUniqueID().equals(uuid)) {
                return player;
            }
        }
        return null;
    }

    private String formatCurrency(long amount) {
        return String.format("%,d %s", amount, ConfigMarket.CurrencyName);
    }

    /**
     * Notification types for categorization
     */
    public enum NotificationType {
        OUTBID,
        WON,
        SOLD,
        EXPIRED,
        CANCELLED,
        NEW_BID
    }

    /**
     * Stored notification for offline players
     */
    private static class AuctionNotification {
        public final NotificationType type;
        public final String message;
        public final int listingId;
        public final long timestamp;

        public AuctionNotification(NotificationType type, String message, int listingId) {
            this.type = type;
            this.message = message;
            this.listingId = listingId;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
