package noppes.npcs.controllers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.config.ConfigMarket;
import noppes.npcs.constants.EnumAuctionDuration;
import noppes.npcs.constants.EnumAuctionSort;
import noppes.npcs.constants.EnumAuctionStatus;
import noppes.npcs.controllers.data.AuctionFilter;
import noppes.npcs.controllers.data.AuctionListing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Controller for the global auction house.
 * Manages all auction listings, bidding, claims, notifications, and logging.
 */
public class AuctionController {
    private static final String LOG_PREFIX = "[Auction] ";

    public static AuctionController Instance;

    // All auction listings by ID
    private Map<Integer, AuctionListing> listings = new ConcurrentHashMap<>();

    // Next available listing ID
    private int nextId = 1;

    // Indices for fast lookup
    private Map<UUID, List<Integer>> listingsBySeller = new ConcurrentHashMap<>();
    private Map<UUID, List<Integer>> listingsByBidder = new ConcurrentHashMap<>();

    // Pending notifications for offline players
    private Map<UUID, List<String>> pendingNotifications = new ConcurrentHashMap<>();

    public AuctionController() {
        Instance = this;
        load();
    }

    // ==================== Listing Management ====================

    /**
     * Create a new auction listing
     * @return The new listing, or null if creation failed
     */
    public AuctionListing createListing(EntityPlayer seller, ItemStack item,
                                         long startingPrice, long buyoutPrice,
                                         EnumAuctionDuration duration) {
        // Validate item
        String validation = AuctionListing.validateItem(item);
        if (validation != null) {
            LogWriter.info(LOG_PREFIX + "Listing validation failed: " + validation);
            return null;
        }

        // Check seller's listing limit
        int currentListings = getActiveListingsCount(seller.getUniqueID());
        int maxListings = getMaxListings(seller);
        if (currentListings >= maxListings) {
            return null;
        }

        // Calculate and charge listing fee
        AuctionListing listing = new AuctionListing(
            seller.getUniqueID(),
            seller.getCommandSenderName(),
            item,
            startingPrice,
            buyoutPrice,
            duration
        );

        long listingFee = listing.getListingFee();
        if (listingFee > 0) {
            if (!CurrencyController.Instance.canAfford(seller, listingFee)) {
                return null;
            }
            CurrencyController.Instance.withdraw(seller, listingFee);
        }

        // Assign ID and store
        listing.id = nextId++;
        listings.put(listing.id, listing);

        // Update indices
        addToSellerIndex(seller.getUniqueID(), listing.id);

        save();

        // Log to CNPC+
        logEvent("LISTING_CREATED", String.format("ID=%d, Seller=%s, Item=%s x%d, StartPrice=%d, Buyout=%s",
            listing.id, listing.sellerName, item.getDisplayName(), item.stackSize,
            listing.startingPrice, listing.buyoutPrice > 0 ? listing.buyoutPrice : "None"));

        return listing;
    }

    /**
     * Get a listing by ID
     */
    public AuctionListing getListing(int id) {
        return listings.get(id);
    }

    /**
     * Get all active listings
     */
    public List<AuctionListing> getActiveListings() {
        processExpiredAuctions();
        return listings.values().stream()
            .filter(AuctionListing::isActive)
            .collect(Collectors.toList());
    }

    /**
     * Search listings by item name
     */
    public List<AuctionListing> searchListings(String query) {
        String lowerQuery = query.toLowerCase();
        return getActiveListings().stream()
            .filter(l -> l.item != null)
            .filter(l -> l.item.getDisplayName().toLowerCase().contains(lowerQuery))
            .collect(Collectors.toList());
    }

    // ==================== Filtering & Sorting ====================

    /**
     * Get filtered and sorted auction listings.
     * @param filter The filter criteria
     * @return FilterResult containing matching listings and metadata
     */
    public FilterResult getFilteredListings(AuctionFilter filter) {
        processExpiredAuctions();

        // Get all active listings
        List<AuctionListing> results = listings.values().stream()
            .filter(AuctionListing::isActive)
            .filter(filter::matches)
            .collect(Collectors.toList());

        // Sort the results
        sortListings(results, filter.sortOrder);

        // Calculate pagination
        int totalResults = results.size();
        int totalPages = totalResults == 0
            ? 0
            : Math.max(1, (int) Math.ceil((double) totalResults / filter.pageSize));
        int currentPage = Math.max(0, filter.page);
        if (totalPages > 0 && currentPage >= totalPages) {
            currentPage = totalPages - 1;
        }
        int startIndex = currentPage * filter.pageSize;
        int endIndex = Math.min(startIndex + filter.pageSize, totalResults);

        // Get the page slice
        List<AuctionListing> pageResults;
        if (startIndex >= totalResults) {
            pageResults = new ArrayList<>();
        } else {
            pageResults = new ArrayList<>(results.subList(startIndex, endIndex));
        }

        return new FilterResult(pageResults, totalResults, currentPage, totalPages);
    }

    /**
     * Sort listings according to the specified sort order
     */
    public void sortListings(List<AuctionListing> listings, EnumAuctionSort sortOrder) {
        if (listings == null || listings.isEmpty()) {
            return;
        }

        Comparator<AuctionListing> comparator;

        switch (sortOrder) {
            case NEWEST_FIRST:
                comparator = Comparator.comparingLong(l -> -l.listingTime);
                break;
            case OLDEST_FIRST:
                comparator = Comparator.comparingLong(l -> l.listingTime);
                break;
            case PRICE_HIGH_TO_LOW:
                comparator = Comparator.comparingLong((AuctionListing l) ->
                    -(l.currentBid > 0 ? l.currentBid : l.startingPrice));
                break;
            case PRICE_LOW_TO_HIGH:
                comparator = Comparator.comparingLong(l ->
                    l.currentBid > 0 ? l.currentBid : l.startingPrice);
                break;
            case ENDING_SOON:
                comparator = Comparator.comparingLong(l -> l.expirationTime);
                break;
            case MOST_TIME_LEFT:
                comparator = Comparator.comparingLong(l -> -l.expirationTime);
                break;
            case NAME_A_TO_Z:
                comparator = Comparator.comparing(l -> getListingName(l).toLowerCase());
                break;
            case NAME_Z_TO_A:
                comparator = Comparator.comparing((AuctionListing l) ->
                    getListingName(l).toLowerCase()).reversed();
                break;
            case MOST_BIDS:
                comparator = Comparator.comparingInt(l -> -l.bidCount);
                break;
            case LEAST_BIDS:
                comparator = Comparator.comparingInt(l -> l.bidCount);
                break;
            default:
                comparator = Comparator.comparingLong(l -> l.expirationTime);
        }

        listings.sort(comparator);
    }

    /**
     * Result container for filtered queries
     */
    public static class FilterResult {
        public final List<AuctionListing> listings;
        public final int totalResults;
        public final int currentPage;
        public final int totalPages;

        public FilterResult(List<AuctionListing> listings, int totalResults,
                           int currentPage, int totalPages) {
            this.listings = listings;
            this.totalResults = totalResults;
            this.currentPage = currentPage;
            this.totalPages = totalPages;
        }

        public boolean hasNextPage() {
            return currentPage < totalPages - 1;
        }

        public boolean hasPreviousPage() {
            return currentPage > 0;
        }

        public boolean isEmpty() {
            return listings == null || listings.isEmpty();
        }
    }

    /**
     * Get all listings by a seller
     */
    public List<AuctionListing> getListingsBySeller(UUID sellerUUID) {
        List<Integer> ids = listingsBySeller.get(sellerUUID);
        if (ids == null) {
            return new ArrayList<>();
        }
        return ids.stream()
            .map(listings::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * Get active listing count for a seller
     */
    public int getActiveListingsCount(UUID sellerUUID) {
        return (int) getListingsBySeller(sellerUUID).stream()
            .filter(l -> l.status == EnumAuctionStatus.ACTIVE)
            .count();
    }

    /**
     * Get listings where player is the high bidder
     */
    public List<AuctionListing> getListingsAsBidder(UUID bidderUUID) {
        List<Integer> ids = listingsByBidder.get(bidderUUID);
        if (ids == null) {
            return new ArrayList<>();
        }
        return ids.stream()
            .map(listings::get)
            .filter(Objects::nonNull)
            .filter(AuctionListing::isActive)
            .filter(l -> bidderUUID.equals(l.highBidderUUID))
            .collect(Collectors.toList());
    }

    /**
     * Get listings that need claiming by a player
     */
    public List<AuctionListing> getClaimableListings(UUID playerUUID) {
        List<AuctionListing> claimable = new ArrayList<>();

        for (AuctionListing listing : listings.values()) {
            // Seller can claim proceeds from sold items
            if (listing.sellerUUID.equals(playerUUID) &&
                listing.status == EnumAuctionStatus.SOLD &&
                !listing.sellerClaimed) {
                claimable.add(listing);
            }

            // Seller can claim back cancelled/expired items
            if (listing.sellerUUID.equals(playerUUID) &&
                (listing.status == EnumAuctionStatus.CANCELLED ||
                 listing.status == EnumAuctionStatus.EXPIRED) &&
                !listing.sellerClaimed) {
                claimable.add(listing);
            }

            // Buyer can claim won items
            if (listing.highBidderUUID != null &&
                listing.highBidderUUID.equals(playerUUID) &&
                listing.status == EnumAuctionStatus.SOLD &&
                !listing.buyerClaimed) {
                claimable.add(listing);
            }
        }

        return claimable;
    }

    // ==================== Bidding ====================

    /**
     * Place a bid on an auction
     */
    public boolean placeBid(int listingId, EntityPlayer bidder, long amount) {
        AuctionListing listing = listings.get(listingId);
        if (listing == null) {
            return false;
        }

        if (!CurrencyController.Instance.canAfford(bidder, amount)) {
            return false;
        }

        // Store previous bidder for refund
        UUID previousBidder = listing.highBidderUUID;
        String previousBidderName = listing.highBidderName;
        long previousBidAmount = listing.currentBid;

        if (!listing.placeBid(bidder.getUniqueID(), bidder.getCommandSenderName(), amount)) {
            return false;
        }

        CurrencyController.Instance.withdraw(bidder, amount);

        // Refund previous bidder
        if (previousBidder != null) {
            CurrencyController.Instance.addClaim(previousBidder, previousBidAmount,
                "Auction Outbid", String.valueOf(listingId));

            // Notify outbid player
            notifyPlayer(previousBidder, EnumChatFormatting.RED +
                "You have been outbid on " + EnumChatFormatting.WHITE + listing.item.getDisplayName() +
                EnumChatFormatting.RED + "! New bid: " + formatCurrency(amount));

            logEvent("OUTBID", String.format("ID=%d, OutbidPlayer=%s, PrevBid=%d, NewBid=%d",
                listingId, previousBidderName, previousBidAmount, amount));
        }

        // Notify seller
        notifyPlayer(listing.sellerUUID, EnumChatFormatting.AQUA +
            bidder.getCommandSenderName() + " placed a bid of " + formatCurrency(amount) +
            " on your " + listing.item.getDisplayName() + "!");

        addToBidderIndex(bidder.getUniqueID(), listingId);
        save();

        logEvent("BID_PLACED", String.format("ID=%d, Bidder=%s, Amount=%d",
            listingId, bidder.getCommandSenderName(), amount));

        return true;
    }

    /**
     * Execute buyout on an auction
     */
    public boolean buyout(int listingId, EntityPlayer buyer) {
        AuctionListing listing = listings.get(listingId);
        if (listing == null || listing.buyoutPrice <= 0) {
            return false;
        }

        if (!CurrencyController.Instance.canAfford(buyer, listing.buyoutPrice)) {
            return false;
        }

        UUID previousBidder = listing.highBidderUUID;
        String previousBidderName = listing.highBidderName;
        long previousBidAmount = listing.currentBid;

        if (!listing.buyout(buyer.getUniqueID(), buyer.getCommandSenderName())) {
            return false;
        }

        CurrencyController.Instance.withdraw(buyer, listing.buyoutPrice);

        // Refund previous bidder
        if (previousBidder != null) {
            CurrencyController.Instance.addClaim(previousBidder, previousBidAmount,
                "Auction Outbid (Buyout)", String.valueOf(listingId));

            notifyPlayer(previousBidder, EnumChatFormatting.RED +
                "Your bid on " + EnumChatFormatting.WHITE + listing.item.getDisplayName() +
                EnumChatFormatting.RED + " was outbid by a buyout. Your " +
                formatCurrency(previousBidAmount) + " has been refunded.");
        }

        addToBidderIndex(buyer.getUniqueID(), listingId);
        save();

        // Notify buyer and seller
        notifyPlayer(buyer.getUniqueID(), EnumChatFormatting.GREEN +
            "Buyout successful! Claim your " + listing.item.getDisplayName() + " from an Auctioneer.");

        notifyPlayer(listing.sellerUUID, EnumChatFormatting.GREEN +
            "Your " + listing.item.getDisplayName() + " was bought out for " +
            formatCurrency(listing.buyoutPrice) + "! Claim your proceeds.");

        logEvent("BUYOUT", String.format("ID=%d, Buyer=%s, Price=%d, Seller=%s",
            listingId, buyer.getCommandSenderName(), listing.buyoutPrice, listing.sellerName));

        return true;
    }

    /**
     * Cancel an auction listing
     */
    public boolean cancelListing(int listingId, EntityPlayer requester, boolean forceCancel) {
        AuctionListing listing = listings.get(listingId);
        if (listing == null) {
            return false;
        }

        boolean byAdmin = !listing.sellerUUID.equals(requester.getUniqueID());
        if (byAdmin && !forceCancel) {
            return false;
        }
        if (!forceCancel && listing.hasBids()) {
            return false;
        }
        if (listing.status != EnumAuctionStatus.ACTIVE) {
            return false;
        }

        if (forceCancel && listing.hasBids() && listing.highBidderUUID != null) {
            CurrencyController.Instance.addClaim(listing.highBidderUUID, listing.currentBid,
                "Auction Cancelled", String.valueOf(listingId));
            notifyPlayer(listing.highBidderUUID, EnumChatFormatting.RED +
                "Your bid on " + EnumChatFormatting.WHITE + listing.item.getDisplayName() +
                EnumChatFormatting.RED + " was cancelled. Your " +
                formatCurrency(listing.currentBid) + " has been refunded.");
        }

        listing.status = EnumAuctionStatus.CANCELLED;

        save();

        if (byAdmin) {
            notifyPlayer(listing.sellerUUID, EnumChatFormatting.RED +
                "Your auction for " + listing.item.getDisplayName() +
                " was cancelled by an administrator.");
        }

        logEvent("CANCELLED", String.format("ID=%d, CancelledBy=%s, ByAdmin=%b",
            listingId, requester.getCommandSenderName(), byAdmin));

        return true;
    }

    // ==================== Claiming ====================

    /**
     * Claim seller proceeds from a sold auction
     */
    public boolean claimSellerProceeds(int listingId, EntityPlayer seller) {
        AuctionListing listing = listings.get(listingId);
        if (listing == null || !listing.sellerUUID.equals(seller.getUniqueID()) ||
            listing.status != EnumAuctionStatus.SOLD || listing.sellerClaimed) {
            return false;
        }

        long proceeds = listing.getSellerProceeds();
        CurrencyController.Instance.deposit(seller, proceeds);

        listing.sellerClaimed = true;
        checkAndMarkClaimed(listing);
        save();

        logEvent("PROCEEDS_CLAIMED", String.format("ID=%d, Seller=%s, Amount=%d",
            listingId, seller.getCommandSenderName(), proceeds));

        return true;
    }

    /**
     * Claim item from won auction
     */
    public ItemStack claimBuyerItem(int listingId, EntityPlayer buyer) {
        AuctionListing listing = listings.get(listingId);
        if (listing == null || listing.highBidderUUID == null ||
            !listing.highBidderUUID.equals(buyer.getUniqueID()) ||
            listing.status != EnumAuctionStatus.SOLD || listing.buyerClaimed) {
            return null;
        }

        listing.buyerClaimed = true;
        checkAndMarkClaimed(listing);
        save();

        logEvent("ITEM_CLAIMED", String.format("ID=%d, Buyer=%s, Item=%s",
            listingId, buyer.getCommandSenderName(), listing.item.getDisplayName()));

        return listing.item.copy();
    }

    /**
     * Claim item from cancelled or expired auction
     */
    public ItemStack claimExpiredItem(int listingId, EntityPlayer seller) {
        AuctionListing listing = listings.get(listingId);
        if (listing == null || !listing.sellerUUID.equals(seller.getUniqueID()) ||
            (listing.status != EnumAuctionStatus.CANCELLED && listing.status != EnumAuctionStatus.EXPIRED) ||
            listing.sellerClaimed) {
            return null;
        }

        listing.sellerClaimed = true;
        listing.status = EnumAuctionStatus.CLAIMED;
        save();

        logEvent("ITEM_RECLAIMED", String.format("ID=%d, Seller=%s, Item=%s",
            listingId, seller.getCommandSenderName(), listing.item.getDisplayName()));

        return listing.item.copy();
    }

    private void checkAndMarkClaimed(AuctionListing listing) {
        if (listing.status == EnumAuctionStatus.SOLD &&
            listing.sellerClaimed && listing.buyerClaimed) {
            listing.status = EnumAuctionStatus.CLAIMED;
        }
    }

    // ==================== Expiration Processing ====================

    /**
     * Process all expired auctions
     */
    public void processExpiredAuctions() {
        boolean changed = false;

        for (AuctionListing listing : listings.values()) {
            if (listing.status == EnumAuctionStatus.ACTIVE && listing.isExpired()) {
                listing.finalize();
                changed = true;

                if (listing.status == EnumAuctionStatus.SOLD) {
                    // Notify winner and seller
                    notifyPlayer(listing.highBidderUUID, EnumChatFormatting.GREEN +
                        "You won the auction for " + listing.item.getDisplayName() +
                        " for " + formatCurrency(listing.currentBid) + "! Claim your item.");

                    notifyPlayer(listing.sellerUUID, EnumChatFormatting.GREEN +
                        "Your " + listing.item.getDisplayName() + " sold for " +
                        formatCurrency(listing.currentBid) + "! Claim your proceeds.");

                    logEvent("AUCTION_SOLD", String.format("ID=%d, Winner=%s, Price=%d, Seller=%s",
                        listing.id, listing.highBidderName, listing.currentBid, listing.sellerName));
                } else {
                    notifyPlayer(listing.sellerUUID, EnumChatFormatting.GRAY +
                        "Your auction for " + listing.item.getDisplayName() +
                        " has expired with no bids. Claim your item.");

                    logEvent("AUCTION_EXPIRED", String.format("ID=%d, Item=%s, Seller=%s",
                        listing.id, listing.item.getDisplayName(), listing.sellerName));
                }
            }
        }

        if (changed) {
            save();
        }
    }

    /**
     * Clean up old claimed listings
     */
    public void cleanupOldListings() {
        long claimExpirationMs = ConfigMarket.ClaimExpirationDays * 24L * 60L * 60L * 1000L;
        if (claimExpirationMs <= 0) return;

        long cutoffTime = System.currentTimeMillis() - claimExpirationMs;
        List<Integer> toRemove = new ArrayList<>();

        for (AuctionListing listing : listings.values()) {
            if (listing.status == EnumAuctionStatus.CLAIMED &&
                listing.expirationTime < cutoffTime) {
                toRemove.add(listing.id);
            }
        }

        for (int id : toRemove) {
            AuctionListing removed = listings.remove(id);
            if (removed != null) {
                removeFromSellerIndex(removed.sellerUUID, id);
                if (removed.highBidderUUID != null) {
                    removeFromBidderIndex(removed.highBidderUUID, id);
                }
            }
        }

        if (!toRemove.isEmpty()) {
            save();
            logEvent("CLEANUP", "Removed " + toRemove.size() + " old listings");
        }
    }

    // ==================== Notifications ====================

    /**
     * Send notification to player (or queue if offline)
     */
    private void notifyPlayer(UUID playerUUID, String message) {
        if (playerUUID == null) return;

        EntityPlayerMP player = getOnlinePlayer(playerUUID);
        String fullMessage = EnumChatFormatting.YELLOW + "[Auction] " + message;

        if (player != null) {
            player.addChatMessage(new ChatComponentText(fullMessage));
        } else {
            pendingNotifications.computeIfAbsent(playerUUID, k -> new ArrayList<>()).add(fullMessage);
            // Limit queue size
            List<String> queue = pendingNotifications.get(playerUUID);
            while (queue.size() > 50) {
                queue.remove(0);
            }
        }
    }

    /**
     * Send pending notifications on player login
     */
    public void onPlayerLogin(EntityPlayerMP player) {
        UUID uuid = player.getUniqueID();

        List<String> notifications = pendingNotifications.remove(uuid);
        if (notifications != null && !notifications.isEmpty()) {
            player.addChatMessage(new ChatComponentText(
                EnumChatFormatting.YELLOW + "[Auction] " + EnumChatFormatting.WHITE +
                "You have " + notifications.size() + " auction notification(s):"));

            for (String msg : notifications) {
                player.addChatMessage(new ChatComponentText(msg));
            }
        }

        // Check claimable items
        int claimableCount = getClaimableListings(uuid).size();
        if (claimableCount > 0) {
            player.addChatMessage(new ChatComponentText(
                EnumChatFormatting.YELLOW + "[Auction] " + EnumChatFormatting.GREEN +
                "You have " + claimableCount + " item(s) waiting to be claimed!"));
        }
    }

    private EntityPlayerMP getOnlinePlayer(UUID uuid) {
        if (CustomNpcs.getServer() == null) return null;

        for (Object obj : CustomNpcs.getServer().getConfigurationManager().playerEntityList) {
            EntityPlayerMP player = (EntityPlayerMP) obj;
            if (player.getUniqueID().equals(uuid)) {
                return player;
            }
        }
        return null;
    }

    // ==================== Index Management ====================

    private void addToSellerIndex(UUID seller, int listingId) {
        listingsBySeller.computeIfAbsent(seller, k -> new ArrayList<>()).add(listingId);
    }

    private void removeFromSellerIndex(UUID seller, int listingId) {
        List<Integer> ids = listingsBySeller.get(seller);
        if (ids != null) {
            ids.remove(Integer.valueOf(listingId));
        }
    }

    private void addToBidderIndex(UUID bidder, int listingId) {
        listingsByBidder.computeIfAbsent(bidder, k -> new ArrayList<>()).add(listingId);
    }

    private void removeFromBidderIndex(UUID bidder, int listingId) {
        List<Integer> ids = listingsByBidder.get(bidder);
        if (ids != null) {
            ids.remove(Integer.valueOf(listingId));
        }
    }

    private void rebuildIndices() {
        listingsBySeller.clear();
        listingsByBidder.clear();

        for (AuctionListing listing : listings.values()) {
            addToSellerIndex(listing.sellerUUID, listing.id);
            if (listing.highBidderUUID != null) {
                addToBidderIndex(listing.highBidderUUID, listing.id);
            }
        }
    }

    // ==================== Helpers ====================

    public int getMaxListings(EntityPlayer player) {
        return ConfigMarket.DefaultMaxListings;
    }

    private String formatCurrency(long amount) {
        return String.format("%,d %s", amount, ConfigMarket.CurrencyName);
    }

    private void logEvent(String event, String details) {
        LogWriter.info(LOG_PREFIX + event + ": " + details);
    }

    private String getListingName(AuctionListing listing) {
        if (listing == null || listing.item == null) {
            return "";
        }
        return listing.item.getDisplayName();
    }

    // ==================== Persistence ====================

    public void save() {
        File saveDir = CustomNpcs.getWorldSaveDirectory();
        if (saveDir == null) return;

        try {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setInteger("NextId", nextId);

            NBTTagList listingList = new NBTTagList();
            for (AuctionListing listing : listings.values()) {
                listingList.appendTag(listing.writeToNBT(new NBTTagCompound()));
            }
            compound.setTag("Listings", listingList);

            File file = new File(saveDir, "auctions.dat");
            File backup = new File(saveDir, "auctions.dat_old");

            if (file.exists()) {
                if (backup.exists()) backup.delete();
                file.renameTo(backup);
            }

            file = new File(saveDir, "auctions.dat");
            FileOutputStream fos = new FileOutputStream(file);
            CompressedStreamTools.writeCompressed(compound, fos);
            fos.close();

        } catch (Exception e) {
            LogWriter.error(LOG_PREFIX + "Error saving auctions", e);
        }
    }

    public void load() {
        File saveDir = CustomNpcs.getWorldSaveDirectory();
        if (saveDir == null) return;

        listings.clear();

        try {
            File file = new File(saveDir, "auctions.dat");
            if (!file.exists()) {
                file = new File(saveDir, "auctions.dat_old");
                if (!file.exists()) return;
            }

            FileInputStream fis = new FileInputStream(file);
            NBTTagCompound compound = CompressedStreamTools.readCompressed(fis);
            fis.close();

            nextId = compound.getInteger("NextId");
            if (nextId < 1) nextId = 1;

            NBTTagList listingList = compound.getTagList("Listings", 10);
            for (int i = 0; i < listingList.tagCount(); i++) {
                AuctionListing listing = new AuctionListing();
                listing.readFromNBT(listingList.getCompoundTagAt(i));
                listings.put(listing.id, listing);
            }

            rebuildIndices();

            logEvent("SYSTEM_STARTUP", String.format("Loaded %d listings, %d active",
                listings.size(), getActiveListingCount()));

        } catch (Exception e) {
            LogWriter.error(LOG_PREFIX + "Error loading auctions", e);
        }
    }

    // ==================== Statistics ====================

    public int getActiveListingCount() {
        return (int) listings.values().stream()
            .filter(l -> l.status == EnumAuctionStatus.ACTIVE)
            .count();
    }

    public int getTotalListingCount() {
        return listings.size();
    }
}
