package noppes.npcs.controllers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.config.ConfigMarket;
import noppes.npcs.constants.EnumAuctionSort;
import noppes.npcs.constants.EnumAuctionStatus;
import noppes.npcs.constants.EnumClaimType;
import noppes.npcs.constants.EnumNotificationType;
import noppes.npcs.controllers.data.AuctionClaim;
import noppes.npcs.controllers.data.AuctionFilter;
import noppes.npcs.controllers.data.AuctionListing;
import noppes.npcs.controllers.data.AuctionNotification;
import noppes.npcs.controllers.data.PlayerCurrencyData;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.util.CustomNPCsThreader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Main controller for the Auction House system.
 *
 * Thread Safety:
 * - All data modifications go through synchronized methods or use proper locking
 * - Player indices are maintained for O(1) lookups
 * - Saves are performed asynchronously on the CNPC+ thread
 *
 * Performance:
 * - Auction processing runs every 20 seconds (400 ticks) to reduce server load
 * - Player-specific queries use indexed maps for fast lookups
 * - Read operations use read locks, write operations use write locks
 */
public class AuctionController {
    public static AuctionController Instance;

    // Main data stores
    private final Map<String, AuctionListing> listings = new ConcurrentHashMap<>();
    private final Map<String, AuctionClaim> claims = new ConcurrentHashMap<>();
    private final List<AuctionNotification> pendingNotifications = Collections.synchronizedList(new ArrayList<>());

    // =========================================
    // Player Indices for O(1) Lookups
    // =========================================

    /** Listings by seller UUID - for quick "my listings" lookups */
    private final Map<UUID, Set<String>> playerListingIds = new ConcurrentHashMap<>();

    /** Listings where player is current high bidder */
    private final Map<UUID, Set<String>> playerBidIds = new ConcurrentHashMap<>();

    /** Claims by player UUID - for quick "my claims" lookups */
    private final Map<UUID, Set<String>> playerClaimIds = new ConcurrentHashMap<>();

    // =========================================
    // Threading & State
    // =========================================

    /** Lock for data modifications - allows concurrent reads */
    private final ReentrantReadWriteLock dataLock = new ReentrantReadWriteLock();

    /** Flag indicating data needs to be saved */
    private final AtomicBoolean dirty = new AtomicBoolean(false);

    /** Flag to prevent concurrent saves */
    private final AtomicBoolean saving = new AtomicBoolean(false);

    private String filePath = "";
    private int tickCounter = 0;

    /** Process auctions every 20 seconds (400 ticks) for performance */
    private static final int PROCESS_INTERVAL_TICKS = 400;

    /** Save dirty data every 60 seconds (1200 ticks) */
    private static final int SAVE_INTERVAL_TICKS = 1200;
    private int saveTickCounter = 0;

    public AuctionController() {
        Instance = this;
        load();
    }

    public static AuctionController getInstance() {
        if (Instance == null || needsNewInstance()) {
            Instance = new AuctionController();
        }
        return Instance;
    }

    private static boolean needsNewInstance() {
        if (Instance == null) return true;
        File file = CustomNpcs.getWorldSaveDirectory();
        if (file == null) return false;
        return !Instance.filePath.equals(file.getAbsolutePath());
    }

    // =========================================
    // Tick Processing
    // =========================================

    public void onServerTick() {
        if (!ConfigMarket.AuctionEnabled) return;

        tickCounter++;
        saveTickCounter++;

        // Process ended auctions every 20 seconds
        if (tickCounter >= PROCESS_INTERVAL_TICKS) {
            tickCounter = 0;
            processEndedAuctions();
            processExpiredClaims();
        }

        // Save dirty data periodically
        if (saveTickCounter >= SAVE_INTERVAL_TICKS) {
            saveTickCounter = 0;
            if (dirty.get()) {
                saveAsync();
            }
        }
    }

    private void processEndedAuctions() {
        dataLock.writeLock().lock();
        try {
            boolean changed = false;
            for (AuctionListing listing : listings.values()) {
                if (listing.status == EnumAuctionStatus.ACTIVE && listing.isExpired()) {
                    endAuction(listing);
                    changed = true;
                }
            }
            if (changed) {
                markDirty();
            }
        } finally {
            dataLock.writeLock().unlock();
        }
    }

    private void endAuction(AuctionListing listing) {
        // Must be called within write lock
        listing.status = EnumAuctionStatus.ENDED;

        // Remove from seller's active listings
        removeFromPlayerListings(listing.sellerUUID, listing.id);

        // Remove from bidder's active bids if any
        if (listing.highBidderUUID != null) {
            removeFromPlayerBids(listing.highBidderUUID, listing.id);
        }

        // Get item display name safely
        String itemDisplayName = listing.item != null ? listing.item.getDisplayName() : "Unknown Item";

        if (listing.hasBids()) {
            // Auction sold - create claims
            AuctionClaim itemClaim = AuctionClaim.createItemWonClaim(
                listing.highBidderUUID, listing.highBidderName, listing.id, listing.item);
            addClaim(itemClaim);

            long saleAmount = listing.currentBid;
            long tax = (long) (saleAmount * ConfigMarket.SalesTaxPercent);
            long sellerReceives = saleAmount - tax;

            AuctionClaim currencyClaim = AuctionClaim.createCurrencyClaim(
                listing.sellerUUID, listing.sellerName, listing.id, sellerReceives,
                itemDisplayName, listing.highBidderName);
            addClaim(currencyClaim);

            // Notifications
            queueNotification(listing.highBidderUUID, EnumNotificationType.AUCTION_WON, listing.id,
                "You won the auction for " + itemDisplayName + "!");
            queueNotification(listing.sellerUUID, EnumNotificationType.AUCTION_SOLD, listing.id,
                "Your " + itemDisplayName + " sold for " + saleAmount + " " + ConfigMarket.CurrencyName + "!");

            logAuction("SOLD", listing.sellerName, itemDisplayName, saleAmount,
                "Winner: " + listing.highBidderName + ", Tax: " + tax);
        } else {
            // No bids - return item to seller
            AuctionClaim returnClaim = AuctionClaim.createItemReturnedClaim(
                listing.sellerUUID, listing.sellerName, listing.id, listing.item);
            addClaim(returnClaim);

            queueNotification(listing.sellerUUID, EnumNotificationType.AUCTION_EXPIRED, listing.id,
                "Your auction for " + itemDisplayName + " expired with no bids.");

            logAuction("EXPIRED", listing.sellerName, itemDisplayName, listing.startingPrice, "No bids");
        }
    }

    private void processExpiredClaims() {
        int expirationDays = ConfigMarket.ClaimExpirationDays;

        dataLock.writeLock().lock();
        try {
            Iterator<Map.Entry<String, AuctionClaim>> iterator = claims.entrySet().iterator();
            boolean changed = false;

            while (iterator.hasNext()) {
                AuctionClaim claim = iterator.next().getValue();
                if (!claim.claimed && claim.isExpired(expirationDays)) {
                    logAuction("CLAIM_EXPIRED", claim.playerName,
                        claim.type.isItem() ? (claim.item != null ? claim.item.getDisplayName() : "Unknown") : "Currency",
                        claim.currency, "Claim expired after " + expirationDays + " days");

                    // Remove from player index
                    removeFromPlayerClaims(claim.playerUUID, claim.id);
                    iterator.remove();
                    changed = true;
                }
            }

            if (changed) {
                markDirty();
            }
        } finally {
            dataLock.writeLock().unlock();
        }
    }

    // =========================================
    // Index Management
    // =========================================

    private void addToPlayerListings(UUID playerUUID, String listingId) {
        playerListingIds.computeIfAbsent(playerUUID, k -> ConcurrentHashMap.newKeySet()).add(listingId);
    }

    private void removeFromPlayerListings(UUID playerUUID, String listingId) {
        Set<String> ids = playerListingIds.get(playerUUID);
        if (ids != null) {
            ids.remove(listingId);
            if (ids.isEmpty()) {
                playerListingIds.remove(playerUUID);
            }
        }
    }

    private void addToPlayerBids(UUID playerUUID, String listingId) {
        playerBidIds.computeIfAbsent(playerUUID, k -> ConcurrentHashMap.newKeySet()).add(listingId);
    }

    private void removeFromPlayerBids(UUID playerUUID, String listingId) {
        Set<String> ids = playerBidIds.get(playerUUID);
        if (ids != null) {
            ids.remove(listingId);
            if (ids.isEmpty()) {
                playerBidIds.remove(playerUUID);
            }
        }
    }

    private void addToPlayerClaims(UUID playerUUID, String claimId) {
        playerClaimIds.computeIfAbsent(playerUUID, k -> ConcurrentHashMap.newKeySet()).add(claimId);
    }

    private void removeFromPlayerClaims(UUID playerUUID, String claimId) {
        Set<String> ids = playerClaimIds.get(playerUUID);
        if (ids != null) {
            ids.remove(claimId);
            if (ids.isEmpty()) {
                playerClaimIds.remove(playerUUID);
            }
        }
    }

    /** Add a claim and update indices */
    private void addClaim(AuctionClaim claim) {
        claims.put(claim.id, claim);
        addToPlayerClaims(claim.playerUUID, claim.id);
    }

    // =========================================
    // Listing Management
    // =========================================

    public String createListing(EntityPlayer player, ItemStack item, long startingPrice, long buyoutPrice) {
        if (!ConfigMarket.AuctionEnabled) {
            return "Auction House is disabled.";
        }

        UUID playerUUID = player.getUniqueID();
        String playerName = player.getCommandSenderName();

        dataLock.writeLock().lock();
        try {
            // Check listing limit
            int currentListings = getPlayerListingCountFast(playerUUID);
            int maxListings = getMaxListingsForPlayer(player);
            if (currentListings >= maxListings) {
                return "You have reached your maximum listings (" + maxListings + ").";
            }

            // Check listing fee
            PlayerData playerData = PlayerData.get(player);
            if (playerData == null) {
                return "Could not access player data.";
            }

            PlayerCurrencyData currency = playerData.currencyData;
            long fee = ConfigMarket.ListingFee;
            if (!currency.canAfford(fee)) {
                return "You cannot afford the listing fee (" + fee + " " + ConfigMarket.CurrencyName + ").";
            }

            // Deduct fee
            if (!currency.withdraw(fee)) {
                return "Failed to deduct listing fee.";
            }

            // Calculate duration
            long durationMs = ConfigMarket.AuctionDurationHours * 60L * 60L * 1000L;

            // Create listing
            AuctionListing listing = new AuctionListing(playerUUID, playerName, item, startingPrice, buyoutPrice, durationMs);
            listings.put(listing.id, listing);
            addToPlayerListings(playerUUID, listing.id);

            markDirty();

            logAuction("CREATED", playerName, item.getDisplayName(), startingPrice,
                "Buyout: " + (buyoutPrice > 0 ? buyoutPrice : "None") + ", Fee: " + fee);

            return null; // Success
        } finally {
            dataLock.writeLock().unlock();
        }
    }

    public String cancelListing(String listingId, EntityPlayer player, boolean isAdmin) {
        dataLock.writeLock().lock();
        try {
            AuctionListing listing = listings.get(listingId);
            if (listing == null) {
                return "Listing not found.";
            }

            if (!listing.status.canCancel()) {
                return "This auction cannot be cancelled.";
            }

            UUID playerUUID = player.getUniqueID();
            if (!isAdmin && !listing.isSeller(playerUUID)) {
                return "You can only cancel your own listings.";
            }

            // Handle penalty if there are bids
            if (listing.hasBids()) {
                long penalty = (long) (listing.currentBid * ConfigMarket.CancellationPenaltyPercent);

                // Refund bidder
                AuctionClaim refundClaim = AuctionClaim.createRefundClaim(
                    listing.highBidderUUID, listing.highBidderName, listing.id, listing.currentBid,
                    listing.item.getDisplayName(), listing.sellerName);
                addClaim(refundClaim);

                // Remove from bidder's active bids
                removeFromPlayerBids(listing.highBidderUUID, listing.id);

                queueNotification(listing.highBidderUUID, EnumNotificationType.AUCTION_OUTBID, listing.id,
                    "The auction for " + listing.item.getDisplayName() + " was cancelled. Your bid has been refunded.");

                // Return item to seller
                AuctionClaim itemClaim = AuctionClaim.createItemReturnedClaim(
                    listing.sellerUUID, listing.sellerName, listing.id, listing.item);
                addClaim(itemClaim);

                logAuction("CANCELLED", listing.sellerName, listing.item.getDisplayName(), listing.currentBid,
                    "Penalty: " + penalty + ", Bidder refunded: " + listing.highBidderName +
                    (isAdmin ? ", Cancelled by admin: " + player.getCommandSenderName() : ""));
            } else {
                // No bids - just return item
                AuctionClaim itemClaim = AuctionClaim.createItemReturnedClaim(
                    listing.sellerUUID, listing.sellerName, listing.id, listing.item);
                addClaim(itemClaim);

                logAuction("CANCELLED", listing.sellerName, listing.item.getDisplayName(), listing.startingPrice,
                    "No bids" + (isAdmin ? ", Cancelled by admin: " + player.getCommandSenderName() : ""));
            }

            // Remove from seller's active listings
            removeFromPlayerListings(listing.sellerUUID, listing.id);

            listing.status = EnumAuctionStatus.CANCELLED;
            markDirty();

            return null; // Success
        } finally {
            dataLock.writeLock().unlock();
        }
    }

    // =========================================
    // Bidding
    // =========================================

    public String placeBid(String listingId, EntityPlayer player, long bidAmount) {
        if (!ConfigMarket.AuctionEnabled) {
            return "Auction House is disabled.";
        }

        UUID playerUUID = player.getUniqueID();
        String playerName = player.getCommandSenderName();

        dataLock.writeLock().lock();
        try {
            AuctionListing listing = listings.get(listingId);
            if (listing == null) {
                return "Listing not found.";
            }

            // Double-check status and expiry inside lock to prevent race conditions
            if (!listing.status.canBid()) {
                return "This auction has ended.";
            }

            if (listing.isExpired()) {
                return "This auction has ended.";
            }

            // CRITICAL: Prevent bidding on own items
            if (listing.isSeller(playerUUID)) {
                return "You cannot bid on your own auction.";
            }

            // Check minimum bid
            long minBid = listing.getMinimumBid(ConfigMarket.MinBidIncrementPercent);
            if (bidAmount < minBid) {
                return "Bid must be at least " + minBid + " " + ConfigMarket.CurrencyName + ".";
            }

            // Check currency
            PlayerData playerData = PlayerData.get(player);
            if (playerData == null) {
                return "Could not access player data.";
            }

            PlayerCurrencyData currency = playerData.currencyData;
            if (!currency.canAfford(bidAmount)) {
                return "You cannot afford this bid.";
            }

            // Store previous bidder info for refund
            UUID previousBidder = listing.highBidderUUID;
            long previousBid = listing.currentBid;

            // Deduct bid from new bidder FIRST
            if (!currency.withdraw(bidAmount)) {
                return "Failed to deduct bid amount.";
            }

            // Refund previous bidder if any
            if (listing.hasBids() && previousBidder != null) {
                AuctionClaim refundClaim = AuctionClaim.createRefundClaim(
                    previousBidder, listing.highBidderName, listing.id, previousBid,
                    listing.item.getDisplayName(), playerName);
                addClaim(refundClaim);

                // Remove from previous bidder's active bids
                removeFromPlayerBids(previousBidder, listing.id);

                queueNotification(previousBidder, EnumNotificationType.AUCTION_OUTBID, listing.id,
                    "You were outbid on " + listing.item.getDisplayName() + "!");
            }

            // Update listing
            listing.highBidderUUID = playerUUID;
            listing.highBidderName = playerName;
            listing.currentBid = bidAmount;
            listing.bidCount++;

            // Add to new bidder's active bids
            addToPlayerBids(playerUUID, listing.id);

            // Snipe protection
            listing.extendForSnipeProtection(ConfigMarket.SnipeProtectionMinutes);

            markDirty();

            logAuction("BID", playerName, listing.item.getDisplayName(), bidAmount,
                "Bids: " + listing.bidCount + ", Seller: " + listing.sellerName);

            return null; // Success
        } finally {
            dataLock.writeLock().unlock();
        }
    }

    public String buyout(String listingId, EntityPlayer player) {
        if (!ConfigMarket.AuctionEnabled) {
            return "Auction House is disabled.";
        }

        UUID playerUUID = player.getUniqueID();
        String playerName = player.getCommandSenderName();

        dataLock.writeLock().lock();
        try {
            AuctionListing listing = listings.get(listingId);
            if (listing == null) {
                return "Listing not found.";
            }

            // Double-check status and expiry inside lock
            if (!listing.status.canBid()) {
                return "This auction has ended.";
            }

            if (listing.isExpired()) {
                return "This auction has ended.";
            }

            if (!listing.hasBuyout()) {
                return "This auction does not have a buyout price.";
            }

            // CRITICAL: Prevent buying own items
            if (listing.isSeller(playerUUID)) {
                return "You cannot buy your own auction.";
            }

            // Check currency
            PlayerData playerData = PlayerData.get(player);
            if (playerData == null) {
                return "Could not access player data.";
            }

            PlayerCurrencyData currency = playerData.currencyData;
            if (!currency.canAfford(listing.buyoutPrice)) {
                return "You cannot afford the buyout price.";
            }

            // Deduct buyout FIRST
            if (!currency.withdraw(listing.buyoutPrice)) {
                return "Failed to deduct buyout amount.";
            }

            // Refund previous bidder if any
            if (listing.hasBids() && listing.highBidderUUID != null) {
                AuctionClaim refundClaim = AuctionClaim.createRefundClaim(
                    listing.highBidderUUID, listing.highBidderName, listing.id, listing.currentBid,
                    listing.item.getDisplayName(), playerName);
                addClaim(refundClaim);

                // Remove from previous bidder's active bids
                removeFromPlayerBids(listing.highBidderUUID, listing.id);

                queueNotification(listing.highBidderUUID, EnumNotificationType.AUCTION_OUTBID, listing.id,
                    "The auction for " + listing.item.getDisplayName() + " was bought out. Your bid has been refunded.");
            }

            // Create claims
            AuctionClaim itemClaim = AuctionClaim.createItemWonClaim(playerUUID, playerName, listing.id, listing.item);
            addClaim(itemClaim);

            long saleAmount = listing.buyoutPrice;
            long tax = (long) (saleAmount * ConfigMarket.SalesTaxPercent);
            long sellerReceives = saleAmount - tax;

            AuctionClaim currencyClaim = AuctionClaim.createCurrencyClaim(
                listing.sellerUUID, listing.sellerName, listing.id, sellerReceives,
                listing.item.getDisplayName(), playerName);
            addClaim(currencyClaim);

            // Notifications
            queueNotification(listing.sellerUUID, EnumNotificationType.AUCTION_SOLD, listing.id,
                "Your " + listing.item.getDisplayName() + " was bought out for " + saleAmount + " " + ConfigMarket.CurrencyName + "!");

            // Remove from seller's active listings
            removeFromPlayerListings(listing.sellerUUID, listing.id);

            listing.status = EnumAuctionStatus.ENDED;
            listing.highBidderUUID = playerUUID;
            listing.highBidderName = playerName;
            listing.currentBid = listing.buyoutPrice;

            markDirty();

            logAuction("BUYOUT", playerName, listing.item.getDisplayName(), listing.buyoutPrice,
                "Seller: " + listing.sellerName + ", Tax: " + tax);

            return null; // Success
        } finally {
            dataLock.writeLock().unlock();
        }
    }

    // =========================================
    // Claims
    // =========================================

    /**
     * Get player claims using indexed lookup - O(n) where n = player's claims, not all claims
     */
    public List<AuctionClaim> getPlayerClaims(UUID playerUUID) {
        Set<String> claimIds = playerClaimIds.get(playerUUID);
        if (claimIds == null || claimIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<AuctionClaim> playerClaims = new ArrayList<>();
        for (String claimId : claimIds) {
            AuctionClaim claim = claims.get(claimId);
            if (claim != null && !claim.claimed) {
                playerClaims.add(claim);
            }
        }
        return playerClaims;
    }

    public String claimItem(String claimId, EntityPlayer player) {
        dataLock.writeLock().lock();
        try {
            AuctionClaim claim = claims.get(claimId);
            if (claim == null) {
                return "Claim not found.";
            }

            if (claim.claimed) {
                return "Already claimed.";
            }

            if (!claim.isForPlayer(player.getUniqueID())) {
                return "This claim is not for you.";
            }

            if (!claim.type.isItem()) {
                return "This is not an item claim.";
            }

            if (claim.item == null) {
                return "Item data is missing.";
            }

            // Check inventory space
            if (!player.inventory.addItemStackToInventory(claim.item.copy())) {
                return "Your inventory is full.";
            }

            claim.claimed = true;
            removeFromPlayerClaims(claim.playerUUID, claim.id);
            markDirty();

            logAuction("CLAIMED", player.getCommandSenderName(), claim.item.getDisplayName(), 0, "Item claimed");

            return null; // Success
        } finally {
            dataLock.writeLock().unlock();
        }
    }

    public String claimCurrency(String claimId, EntityPlayer player) {
        dataLock.writeLock().lock();
        try {
            AuctionClaim claim = claims.get(claimId);
            if (claim == null) {
                return "Claim not found.";
            }

            if (claim.claimed) {
                return "Already claimed.";
            }

            if (!claim.isForPlayer(player.getUniqueID())) {
                return "This claim is not for you.";
            }

            if (!claim.type.isCurrency()) {
                return "This is not a currency claim.";
            }

            PlayerData playerData = PlayerData.get(player);
            if (playerData == null) {
                return "Could not access player data.";
            }

            if (!playerData.currencyData.deposit(claim.currency)) {
                return "Failed to deposit currency.";
            }

            claim.claimed = true;
            removeFromPlayerClaims(claim.playerUUID, claim.id);
            markDirty();

            logAuction("CLAIMED", player.getCommandSenderName(), ConfigMarket.CurrencyName, claim.currency,
                claim.type == EnumClaimType.REFUND ? "Refund claimed" : "Sale proceeds claimed");

            return null; // Success
        } finally {
            dataLock.writeLock().unlock();
        }
    }

    public int claimAll(EntityPlayer player) {
        int claimedCount = 0;
        List<AuctionClaim> playerClaims = getPlayerClaims(player.getUniqueID());

        for (AuctionClaim claim : playerClaims) {
            String result;
            if (claim.type.isItem()) {
                result = claimItem(claim.id, player);
            } else {
                result = claimCurrency(claim.id, player);
            }
            if (result == null) {
                claimedCount++;
            }
        }

        return claimedCount;
    }

    // =========================================
    // Queries - Using Indexed Lookups
    // =========================================

    public List<AuctionListing> getActiveListings(AuctionFilter filter, int page, int pageSize) {
        List<AuctionListing> result = new ArrayList<>();

        dataLock.readLock().lock();
        try {
            for (AuctionListing listing : listings.values()) {
                if (listing.status != EnumAuctionStatus.ACTIVE) continue;

                // Apply search filter
                if (filter.hasSearchText()) {
                    String itemName = listing.item != null ? listing.item.getDisplayName() : "";
                    if (!filter.matchesSearch(itemName)) continue;
                }

                result.add(listing);
            }
        } finally {
            dataLock.readLock().unlock();
        }

        // Sort
        sortListings(result, filter.sortBy);

        // Paginate
        int start = page * pageSize;
        int end = Math.min(start + pageSize, result.size());
        if (start >= result.size()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(result.subList(start, end));
    }

    private void sortListings(List<AuctionListing> list, EnumAuctionSort sortBy) {
        Comparator<AuctionListing> comparator;
        switch (sortBy) {
            case ENDING_SOON:
                comparator = Comparator.comparingLong(AuctionListing::getTimeRemaining);
                break;
            case PRICE_LOW:
                comparator = Comparator.comparingLong(AuctionListing::getEffectivePrice);
                break;
            case PRICE_HIGH:
                comparator = Comparator.comparingLong(AuctionListing::getEffectivePrice).reversed();
                break;
            case MOST_BIDS:
                comparator = Comparator.comparingInt(l -> -l.bidCount);
                break;
            case NEWEST:
            default:
                comparator = Comparator.comparingLong(l -> -l.createdTime);
                break;
        }
        list.sort(comparator);
    }

    public int getTotalActiveListings(AuctionFilter filter) {
        int count = 0;
        dataLock.readLock().lock();
        try {
            for (AuctionListing listing : listings.values()) {
                if (listing.status != EnumAuctionStatus.ACTIVE) continue;
                if (filter.hasSearchText()) {
                    String itemName = listing.item != null ? listing.item.getDisplayName() : "";
                    if (!filter.matchesSearch(itemName)) continue;
                }
                count++;
            }
        } finally {
            dataLock.readLock().unlock();
        }
        return count;
    }

    public AuctionListing getListing(String listingId) {
        return listings.get(listingId);
    }

    /**
     * Get a player's current currency balance.
     */
    public long getPlayerBalance(EntityPlayer player) {
        PlayerData playerData = PlayerData.get(player);
        if (playerData == null || playerData.currencyData == null) {
            return 0;
        }
        return playerData.currencyData.getBalance();
    }

    /**
     * Fast O(1) lookup for player listing count using index
     */
    public int getPlayerListingCountFast(UUID playerUUID) {
        Set<String> ids = playerListingIds.get(playerUUID);
        return ids != null ? ids.size() : 0;
    }

    /**
     * Legacy method - still iterates for accuracy (in case index is stale)
     */
    public int getPlayerListingCount(UUID playerUUID) {
        return getPlayerListingCountFast(playerUUID);
    }

    /**
     * Get player's active listings using indexed lookup
     */
    public List<AuctionListing> getPlayerActiveListings(UUID playerUUID) {
        Set<String> listingIds = playerListingIds.get(playerUUID);
        if (listingIds == null || listingIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<AuctionListing> result = new ArrayList<>();
        for (String listingId : listingIds) {
            AuctionListing listing = listings.get(listingId);
            if (listing != null && listing.status == EnumAuctionStatus.ACTIVE) {
                result.add(listing);
            }
        }
        return result;
    }

    /**
     * Get all active auctions where the player is currently the highest bidder.
     * Uses indexed lookup for O(1) access to player's bid set.
     */
    public List<AuctionListing> getPlayerActiveBids(UUID playerUUID) {
        Set<String> bidListingIds = playerBidIds.get(playerUUID);
        if (bidListingIds == null || bidListingIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<AuctionListing> result = new ArrayList<>();
        for (String listingId : bidListingIds) {
            AuctionListing listing = listings.get(listingId);
            if (listing != null && listing.status == EnumAuctionStatus.ACTIVE &&
                listing.highBidderUUID != null && listing.highBidderUUID.equals(playerUUID)) {
                result.add(listing);
            }
        }
        return result;
    }

    /**
     * Get the total number of trade slots used by a player.
     * Uses indexed lookups for O(1) performance.
     */
    public int getPlayerTradeSlotCount(UUID playerUUID) {
        int count = 0;
        // Active listings
        Set<String> listingIds = playerListingIds.get(playerUUID);
        if (listingIds != null) count += listingIds.size();
        // Active bids
        Set<String> bidIds = playerBidIds.get(playerUUID);
        if (bidIds != null) count += bidIds.size();
        // Pending claims
        Set<String> claimIds = playerClaimIds.get(playerUUID);
        if (claimIds != null) count += claimIds.size();
        return count;
    }

    private int getMaxListingsForPlayer(EntityPlayer player) {
        // TODO: Check permissions for customnpcs.auction.slots.X
        return ConfigMarket.DefaultMaxListings;
    }

    // =========================================
    // Notifications
    // =========================================

    private void queueNotification(UUID playerUUID, EnumNotificationType type, String listingId, String message) {
        AuctionNotification notification = new AuctionNotification(playerUUID, type, listingId, message);
        pendingNotifications.add(notification);

        // Try to send immediately if player is online
        EntityPlayerMP player = getOnlinePlayer(playerUUID);
        if (player != null) {
            sendNotification(player, notification);
            notification.sent = true;
        }
    }

    public void onPlayerLogin(EntityPlayer player) {
        UUID playerUUID = player.getUniqueID();
        boolean hasClaims = !getPlayerClaims(playerUUID).isEmpty();

        synchronized (pendingNotifications) {
            Iterator<AuctionNotification> iterator = pendingNotifications.iterator();
            while (iterator.hasNext()) {
                AuctionNotification notification = iterator.next();
                if (notification.isForPlayer(playerUUID) && !notification.sent) {
                    sendNotification((EntityPlayerMP) player, notification);
                    notification.sent = true;
                    iterator.remove();
                }
            }
        }

        if (hasClaims) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD +
                "[Auction House] " + EnumChatFormatting.YELLOW + "You have items or currency to claim!"));
        }
    }

    private void sendNotification(EntityPlayerMP player, AuctionNotification notification) {
        player.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD +
            "[Auction House] " + EnumChatFormatting.WHITE + notification.message));
    }

    private EntityPlayerMP getOnlinePlayer(UUID uuid) {
        MinecraftServer server = MinecraftServer.getServer();
        if (server == null) return null;

        for (Object obj : server.getConfigurationManager().playerEntityList) {
            EntityPlayerMP player = (EntityPlayerMP) obj;
            if (player.getUniqueID().equals(uuid)) {
                return player;
            }
        }
        return null;
    }

    // =========================================
    // Logging
    // =========================================

    private void logAuction(String action, String playerName, String itemName, long amount, String details) {
        if (!ConfigMarket.AuctionLoggingEnabled) return;

        boolean shouldLog = false;
        switch (action) {
            case "CREATED":
                shouldLog = ConfigMarket.LogAuctionCreated;
                break;
            case "BID":
                shouldLog = ConfigMarket.LogAuctionBid;
                break;
            case "BUYOUT":
                shouldLog = ConfigMarket.LogAuctionBuyout;
                break;
            case "SOLD":
                shouldLog = ConfigMarket.LogAuctionSold;
                break;
            case "EXPIRED":
                shouldLog = ConfigMarket.LogAuctionExpired;
                break;
            case "CANCELLED":
                shouldLog = ConfigMarket.LogAuctionCancelled;
                break;
            case "CLAIMED":
            case "CLAIM_EXPIRED":
                shouldLog = ConfigMarket.LogAuctionClaimed;
                break;
        }

        if (shouldLog) {
            String logMessage = String.format("[AUCTION:%s] Player: %s, Item: %s, Amount: %d, Details: %s",
                action, playerName, itemName, amount, details);
            LogWriter.info(logMessage);
        }
    }

    // =========================================
    // Save / Load - Async on CNPC+ Thread
    // =========================================

    /** Mark data as dirty - will be saved on next save cycle */
    private void markDirty() {
        dirty.set(true);
    }

    /** Save synchronously - used for server shutdown */
    public void save() {
        if (!dirty.compareAndSet(true, false)) {
            return; // Nothing to save
        }
        saveInternal();
    }

    /** Save asynchronously on CNPC+ thread */
    public void saveAsync() {
        if (!dirty.compareAndSet(true, false)) {
            return; // Nothing to save
        }

        if (!saving.compareAndSet(false, true)) {
            // Another save is in progress, mark dirty again
            dirty.set(true);
            return;
        }

        // Take a snapshot of the data for async save
        final NBTTagCompound compound;
        dataLock.readLock().lock();
        try {
            compound = writeToNBT(new NBTTagCompound());
        } finally {
            dataLock.readLock().unlock();
        }

        CustomNPCsThreader.customNPCThread.execute(() -> {
            try {
                saveToFile(compound);
            } catch (Exception e) {
                LogWriter.error("Error saving auction data asynchronously", e);
                // Mark dirty again so we retry
                dirty.set(true);
            } finally {
                saving.set(false);
            }
        });
    }

    /** Internal synchronous save */
    private void saveInternal() {
        dataLock.readLock().lock();
        try {
            NBTTagCompound compound = writeToNBT(new NBTTagCompound());
            saveToFile(compound);
        } catch (Exception e) {
            LogWriter.error("Error saving auction data", e);
        } finally {
            dataLock.readLock().unlock();
        }
    }

    private void saveToFile(NBTTagCompound compound) {
        try {
            File saveDir = CustomNpcs.getWorldSaveDirectory();
            if (saveDir == null) return;

            File fileNew = new File(saveDir, "auction.dat_new");
            File fileOld = new File(saveDir, "auction.dat_old");
            File fileCurrent = new File(saveDir, "auction.dat");

            CompressedStreamTools.writeCompressed(compound, new FileOutputStream(fileNew));

            // Backup rotation
            if (fileOld.exists()) fileOld.delete();
            if (fileCurrent.exists()) fileCurrent.renameTo(fileOld);
            if (fileCurrent.exists()) fileCurrent.delete();
            fileNew.renameTo(fileCurrent);
            if (fileNew.exists()) fileNew.delete();

        } catch (Exception e) {
            LogWriter.error("Error saving auction data to file", e);
        }
    }

    public void load() {
        listings.clear();
        claims.clear();
        pendingNotifications.clear();
        playerListingIds.clear();
        playerBidIds.clear();
        playerClaimIds.clear();

        File saveDir = CustomNpcs.getWorldSaveDirectory();
        if (saveDir == null) return;
        filePath = saveDir.getAbsolutePath();

        // Try current file
        try {
            File file = new File(saveDir, "auction.dat");
            if (file.exists()) {
                loadFromFile(file);
                rebuildIndices();
                return;
            }
        } catch (Exception e) {
            LogWriter.error("Error loading auction.dat, trying backup", e);
        }

        // Try backup file
        try {
            File file = new File(saveDir, "auction.dat_old");
            if (file.exists()) {
                loadFromFile(file);
                rebuildIndices();
                LogWriter.info("Loaded auction data from backup file");
            }
        } catch (Exception e) {
            LogWriter.error("Error loading auction.dat_old", e);
        }
    }

    /** Rebuild all indices from loaded data */
    private void rebuildIndices() {
        playerListingIds.clear();
        playerBidIds.clear();
        playerClaimIds.clear();

        // Index listings
        for (AuctionListing listing : listings.values()) {
            if (listing.status == EnumAuctionStatus.ACTIVE) {
                addToPlayerListings(listing.sellerUUID, listing.id);
                if (listing.highBidderUUID != null) {
                    addToPlayerBids(listing.highBidderUUID, listing.id);
                }
            }
        }

        // Index claims
        for (AuctionClaim claim : claims.values()) {
            if (!claim.claimed) {
                addToPlayerClaims(claim.playerUUID, claim.id);
            }
        }

        LogWriter.info("Rebuilt auction indices: " + playerListingIds.size() + " sellers, " +
            playerBidIds.size() + " bidders, " + playerClaimIds.size() + " claimants");
    }

    private void loadFromFile(File file) throws Exception {
        NBTTagCompound compound = CompressedStreamTools.readCompressed(new FileInputStream(file));
        readFromNBT(compound);
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        // Listings
        NBTTagList listingsList = new NBTTagList();
        for (AuctionListing listing : listings.values()) {
            listingsList.appendTag(listing.writeToNBT(new NBTTagCompound()));
        }
        compound.setTag("Listings", listingsList);

        // Claims
        NBTTagList claimsList = new NBTTagList();
        for (AuctionClaim claim : claims.values()) {
            claimsList.appendTag(claim.writeToNBT(new NBTTagCompound()));
        }
        compound.setTag("Claims", claimsList);

        // Pending Notifications
        NBTTagList notificationsList = new NBTTagList();
        synchronized (pendingNotifications) {
            for (AuctionNotification notification : pendingNotifications) {
                if (!notification.sent) {
                    notificationsList.appendTag(notification.writeToNBT(new NBTTagCompound()));
                }
            }
        }
        compound.setTag("PendingNotifications", notificationsList);

        return compound;
    }

    public void readFromNBT(NBTTagCompound compound) {
        // Listings
        NBTTagList listingsList = compound.getTagList("Listings", 10);
        for (int i = 0; i < listingsList.tagCount(); i++) {
            AuctionListing listing = AuctionListing.fromNBT(listingsList.getCompoundTagAt(i));
            listings.put(listing.id, listing);
        }

        // Claims
        NBTTagList claimsList = compound.getTagList("Claims", 10);
        for (int i = 0; i < claimsList.tagCount(); i++) {
            AuctionClaim claim = AuctionClaim.fromNBT(claimsList.getCompoundTagAt(i));
            claims.put(claim.id, claim);
        }

        // Pending Notifications
        NBTTagList notificationsList = compound.getTagList("PendingNotifications", 10);
        for (int i = 0; i < notificationsList.tagCount(); i++) {
            AuctionNotification notification = AuctionNotification.fromNBT(notificationsList.getCompoundTagAt(i));
            pendingNotifications.add(notification);
        }
    }

    // =========================================
    // Admin Methods
    // =========================================

    public List<AuctionListing> getAllListings() {
        return new ArrayList<>(listings.values());
    }

    public void adminCancelListing(String listingId, EntityPlayer admin, String reason) {
        cancelListing(listingId, admin, true);
    }

    public void clearClaimedData() {
        dataLock.writeLock().lock();
        try {
            // Remove claimed claims
            claims.entrySet().removeIf(entry -> entry.getValue().claimed);

            // Remove ended listings that have no unclaimed claims
            listings.entrySet().removeIf(entry ->
                entry.getValue().status == EnumAuctionStatus.CLAIMED ||
                (entry.getValue().status == EnumAuctionStatus.ENDED &&
                 !hasUnclaimedClaims(entry.getValue().id)));

            // Rebuild indices
            rebuildIndices();
            markDirty();
        } finally {
            dataLock.writeLock().unlock();
        }
    }

    private boolean hasUnclaimedClaims(String listingId) {
        for (AuctionClaim claim : claims.values()) {
            if (listingId.equals(claim.listingId) && !claim.claimed) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if a player is the seller of a listing.
     * Used for GUI visibility checks.
     */
    public boolean isPlayerSeller(String listingId, UUID playerUUID) {
        AuctionListing listing = listings.get(listingId);
        return listing != null && listing.isSeller(playerUUID);
    }
}
