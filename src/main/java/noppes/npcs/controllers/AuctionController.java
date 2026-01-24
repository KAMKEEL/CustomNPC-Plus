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
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.LogWriter;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.IAuctionHandler;
import noppes.npcs.api.handler.data.IAuctionListing;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.config.ConfigMarket;
import noppes.npcs.constants.EnumAuctionSort;
import noppes.npcs.constants.EnumAuctionStatus;
import noppes.npcs.constants.EnumClaimType;
import noppes.npcs.constants.EnumNotificationType;
import noppes.npcs.controllers.data.AuctionClaim;
import noppes.npcs.controllers.data.AuctionFilter;
import noppes.npcs.controllers.data.AuctionListing;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerTradeData;
import noppes.npcs.util.CustomNPCsThreader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main controller for the Auction system.
 *
 * Thread Safety:
 * - Uses ConcurrentHashMap for all data stores - no locking required
 * - Claims are stored in PlayerData, not here - reduces contention
 * - Saves are performed asynchronously on the CNPC+ thread
 *
 * Performance:
 * - Auction processing runs every 30 seconds (600 ticks)
 * - Save interval is every 30 seconds (600 ticks)
 * - Player-specific queries use indexed maps for fast lookups
 */
public class AuctionController implements IAuctionHandler {
    public static AuctionController Instance;

    // Main data stores - listings only, claims are stored in PlayerData
    private final Map<String, AuctionListing> listings = new ConcurrentHashMap<>();
    // Claims are now stored in PlayerData.tradeData, not here

    // =========================================
    // Player Indices for O(1) Lookups
    // =========================================

    /** Listings by seller UUID - for quick "my listings" lookups */
    private final Map<UUID, Set<String>> playerListingIds = new ConcurrentHashMap<>();

    /** Listings where player is current high bidder */
    private final Map<UUID, Set<String>> playerBidIds = new ConcurrentHashMap<>();

    /** Cached max trade slots per player - computed on login, cleared on logout */
    private final Map<UUID, Integer> playerMaxTradesCache = new ConcurrentHashMap<>();

    // =========================================
    // Threading & State
    // =========================================

    /** Flag indicating data needs to be saved */
    private final AtomicBoolean dirty = new AtomicBoolean(false);

    /** Flag to prevent concurrent saves */
    private final AtomicBoolean saving = new AtomicBoolean(false);

    private String filePath = "";
    private int tickCounter = 0;

    /** Process auctions and save every 30 seconds (600 ticks) */
    private static final int TICK_INTERVAL = 600;

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

        // Process auctions and save every 30 seconds
        if (tickCounter >= TICK_INTERVAL) {
            tickCounter = 0;
            processEndedAuctions();
            if (dirty.get()) {
                saveAsync();
            }
        }
    }

    private void processEndedAuctions() {
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
    }

    private void endAuction(AuctionListing listing) {
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
            addClaimToPlayer(listing.highBidderUUID, itemClaim);

            long saleAmount = listing.currentBid;
            long tax = (long) (saleAmount * ConfigMarket.SalesTaxPercent);
            long sellerReceives = saleAmount - tax;

            AuctionClaim currencyClaim = AuctionClaim.createCurrencyClaim(
                listing.sellerUUID, listing.sellerName, listing.id, sellerReceives,
                itemDisplayName, listing.highBidderName);
            addClaimToPlayer(listing.sellerUUID, currencyClaim);

            // Notifications
            sendNotificationToPlayer(listing.highBidderUUID, EnumNotificationType.AUCTION_WON, listing.id,
                "You won the auction for " + itemDisplayName + "!");
            sendNotificationToPlayer(listing.sellerUUID, EnumNotificationType.AUCTION_SOLD, listing.id,
                "Your " + itemDisplayName + " sold for " + saleAmount + " " + ConfigMarket.CurrencyName + "!");

            logAuction("SOLD", listing.sellerName, itemDisplayName, saleAmount,
                "Winner: " + listing.highBidderName + ", Tax: " + tax);
        } else {
            // No bids - return item to seller
            AuctionClaim returnClaim = AuctionClaim.createItemReturnedClaim(
                listing.sellerUUID, listing.sellerName, listing.id, listing.item);
            addClaimToPlayer(listing.sellerUUID, returnClaim);

            sendNotificationToPlayer(listing.sellerUUID, EnumNotificationType.AUCTION_EXPIRED, listing.id,
                "Your auction for " + itemDisplayName + " expired with no bids.");

            logAuction("EXPIRED", listing.sellerName, itemDisplayName, listing.startingPrice, "No bids");
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

    /**
     * Add a claim to a player's trade data.
     * If player is online, adds directly. If offline, loads their data file and updates it.
     */
    private void addClaimToPlayer(UUID playerUUID, AuctionClaim claim) {
        if (playerUUID == null || claim == null) return;

        PlayerData data = PlayerDataController.Instance.getData(playerUUID);
        if (data != null) {
            data.tradeData.addClaim(claim);
            data.updateClient = true;
            data.save();
        }
    }

    // =========================================
    // Listing Management
    // =========================================

    public String createListing(EntityPlayer player, ItemStack item, long startingPrice, long buyoutPrice) {
        if (!ConfigMarket.AuctionEnabled) {
            return "Auction is disabled.";
        }

        // Validate minimum price
        if (startingPrice < ConfigMarket.MinimumListingPrice) {
            return "Starting price must be at least " + ConfigMarket.MinimumListingPrice + " " + ConfigMarket.CurrencyName + ".";
        }

        // Validate buyout price if set
        if (buyoutPrice > 0 && buyoutPrice < startingPrice) {
            return "Buyout price must be higher than starting price.";
        }

        UUID playerUUID = player.getUniqueID();
        String playerName = player.getCommandSenderName();

        // Check trade slot limit (listings + bids + claims)
        int currentTrades = getPlayerTradeSlotCount(player);
        int maxTrades = getMaxTradesForPlayer(player);
        if (currentTrades >= maxTrades) {
            return "You have reached your maximum trade slots (" + maxTrades + ").";
        }

        // Check listing fee
        PlayerData playerData = PlayerData.get(player);
        if (playerData == null) {
            return "Could not access player data.";
        }

        PlayerTradeData currency = playerData.tradeData;
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
    }

    public String cancelListing(String listingId, EntityPlayer player, boolean isAdmin) {
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

            // Refund bidder (no item - can't rebid on cancelled auction)
            AuctionClaim refundClaim = AuctionClaim.createRefundClaim(
                listing.highBidderUUID, listing.highBidderName, listing.id, listing.currentBid,
                listing.item.getDisplayName(), listing.sellerName, null);
            addClaimToPlayer(listing.highBidderUUID, refundClaim);

            // Remove from bidder's active bids
            removeFromPlayerBids(listing.highBidderUUID, listing.id);

            sendNotificationToPlayer(listing.highBidderUUID, EnumNotificationType.AUCTION_OUTBID, listing.id,
                "The auction for " + listing.item.getDisplayName() + " was cancelled. Your bid has been refunded.");

            // Return item to seller
            AuctionClaim itemClaim = AuctionClaim.createItemReturnedClaim(
                listing.sellerUUID, listing.sellerName, listing.id, listing.item);
            addClaimToPlayer(listing.sellerUUID, itemClaim);

            logAuction("CANCELLED", listing.sellerName, listing.item.getDisplayName(), listing.currentBid,
                "Penalty: " + penalty + ", Bidder refunded: " + listing.highBidderName +
                (isAdmin ? ", Cancelled by admin: " + player.getCommandSenderName() : ""));
        } else {
            // No bids - just return item
            AuctionClaim itemClaim = AuctionClaim.createItemReturnedClaim(
                listing.sellerUUID, listing.sellerName, listing.id, listing.item);
            addClaimToPlayer(listing.sellerUUID, itemClaim);

            logAuction("CANCELLED", listing.sellerName, listing.item.getDisplayName(), listing.startingPrice,
                "No bids" + (isAdmin ? ", Cancelled by admin: " + player.getCommandSenderName() : ""));
        }

        // Remove from seller's active listings
        removeFromPlayerListings(listing.sellerUUID, listing.id);

        listing.status = EnumAuctionStatus.CANCELLED;
        markDirty();

        return null; // Success
    }

    // =========================================
    // Bidding
    // =========================================

    public String placeBid(String listingId, EntityPlayer player, long bidAmount) {
        if (!ConfigMarket.AuctionEnabled) {
            return "Auction is disabled.";
        }

        UUID playerUUID = player.getUniqueID();
        String playerName = player.getCommandSenderName();

        AuctionListing listing = listings.get(listingId);
        if (listing == null) {
            return "Listing not found.";
        }

        // Check status and expiry
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

        // Prevent rebidding if already highest bidder
        if (listing.isHighBidder(playerUUID)) {
            return "You are already the highest bidder. Use buyout if you want to purchase immediately.";
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

        PlayerTradeData currency = playerData.tradeData;

        // Store previous bidder info
        UUID previousBidder = listing.highBidderUUID;
        long previousBid = listing.currentBid;

        // Full bid amount charged since we don't allow rebidding
        long amountToCharge = bidAmount;

        if (!currency.canAfford(amountToCharge)) {
            return "You cannot afford this bid.";
        }

        // Deduct bid from new bidder FIRST
        if (!currency.withdraw(amountToCharge)) {
            return "Failed to deduct bid amount.";
        }

        // Refund previous bidder (include item for rebid option)
        if (listing.hasBids() && previousBidder != null) {
            AuctionClaim refundClaim = AuctionClaim.createRefundClaim(
                previousBidder, listing.highBidderName, listing.id, previousBid,
                listing.item.getDisplayName(), playerName, listing.item);
            addClaimToPlayer(previousBidder, refundClaim);

            // Remove from previous bidder's active bids
            removeFromPlayerBids(previousBidder, listing.id);

            sendNotificationToPlayer(previousBidder, EnumNotificationType.AUCTION_OUTBID, listing.id,
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
    }

    public String buyout(String listingId, EntityPlayer player) {
        if (!ConfigMarket.AuctionEnabled) {
            return "Auction is disabled.";
        }

        UUID playerUUID = player.getUniqueID();
        String playerName = player.getCommandSenderName();

        AuctionListing listing = listings.get(listingId);
        if (listing == null) {
            return "Listing not found.";
        }

        // Check status and expiry
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

        PlayerTradeData currency = playerData.tradeData;

        // If buyer is already the high bidder, only charge the difference
        boolean isCurrentBidder = listing.isHighBidder(playerUUID);
        long amountToCharge = isCurrentBidder
            ? (listing.buyoutPrice - listing.currentBid)
            : listing.buyoutPrice;

        if (!currency.canAfford(amountToCharge)) {
            if (isCurrentBidder) {
                return "You cannot afford the remaining " + amountToCharge + " " + ConfigMarket.CurrencyName + " for buyout.";
            }
            return "You cannot afford the buyout price.";
        }

        // Deduct buyout amount
        if (!currency.withdraw(amountToCharge)) {
            return "Failed to deduct buyout amount.";
        }

        // Refund previous bidder if any AND they're not the buyer (no item - auction ended via buyout)
        if (listing.hasBids() && listing.highBidderUUID != null && !isCurrentBidder) {
            AuctionClaim refundClaim = AuctionClaim.createRefundClaim(
                listing.highBidderUUID, listing.highBidderName, listing.id, listing.currentBid,
                listing.item.getDisplayName(), playerName, null);
            addClaimToPlayer(listing.highBidderUUID, refundClaim);

            // Remove from previous bidder's active bids
            removeFromPlayerBids(listing.highBidderUUID, listing.id);

            sendNotificationToPlayer(listing.highBidderUUID, EnumNotificationType.AUCTION_OUTBID, listing.id,
                "The auction for " + listing.item.getDisplayName() + " was bought out. Your bid has been refunded.");
        } else if (isCurrentBidder) {
            // Current bidder is buying out - just remove from their active bids
            removeFromPlayerBids(playerUUID, listing.id);
        }

        // Create claims
        AuctionClaim itemClaim = AuctionClaim.createItemWonClaim(playerUUID, playerName, listing.id, listing.item);
        addClaimToPlayer(playerUUID, itemClaim);

        long saleAmount = listing.buyoutPrice;
        long tax = (long) (saleAmount * ConfigMarket.SalesTaxPercent);
        long sellerReceives = saleAmount - tax;

        AuctionClaim currencyClaim = AuctionClaim.createCurrencyClaim(
            listing.sellerUUID, listing.sellerName, listing.id, sellerReceives,
            listing.item.getDisplayName(), playerName);
        addClaimToPlayer(listing.sellerUUID, currencyClaim);

        // Notifications
        sendNotificationToPlayer(listing.sellerUUID, EnumNotificationType.AUCTION_SOLD, listing.id,
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
    }

    // =========================================
    // Claims - Now stored in PlayerData
    // =========================================

    /**
     * Get player claims from PlayerData.
     */
    public List<AuctionClaim> getPlayerClaims(EntityPlayer player) {
        PlayerData playerData = PlayerData.get(player);
        if (playerData == null) {
            return new ArrayList<>();
        }
        return playerData.tradeData.getClaimsList();
    }

    /**
     * Get player claims by UUID (for login notification).
     */
    public List<AuctionClaim> getPlayerClaims(UUID playerUUID) {
        EntityPlayerMP onlinePlayer = (EntityPlayerMP) PlayerDataController.getPlayerFromUUID(playerUUID);
        if (onlinePlayer != null) {
            return getPlayerClaims(onlinePlayer);
        }
        return new ArrayList<>();
    }

    public String claimItem(String claimId, EntityPlayer player) {
        PlayerData playerData = PlayerData.get(player);
        if (playerData == null) {
            return "Could not access player data.";
        }

        AuctionClaim claim = playerData.tradeData.getClaimInternal(claimId);
        if (claim == null) {
            return "Claim not found.";
        }

        if (claim.claimed) {
            return "Already claimed.";
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

        // Remove claim from player's auction data
        playerData.tradeData.claimAndRemove(claimId);
        playerData.updateClient = true;
        playerData.save();

        // Cleanup listing if no more claims
        cleanupListingIfComplete(claim.listingId);

        logAuction("CLAIMED", player.getCommandSenderName(), claim.item.getDisplayName(), 0, "Item claimed");

        return null; // Success
    }

    public String claimCurrency(String claimId, EntityPlayer player) {
        PlayerData playerData = PlayerData.get(player);
        if (playerData == null) {
            return "Could not access player data.";
        }

        AuctionClaim claim = playerData.tradeData.getClaimInternal(claimId);
        if (claim == null) {
            return "Claim not found.";
        }

        if (claim.claimed) {
            return "Already claimed.";
        }

        if (!claim.type.isCurrency()) {
            return "This is not a currency claim.";
        }

        if (!playerData.tradeData.deposit(claim.currency)) {
            return "Failed to deposit currency.";
        }

        // Remove claim from player's auction data
        playerData.tradeData.claimAndRemove(claimId);
        playerData.updateClient = true;
        playerData.save();

        // Cleanup listing if no more claims
        cleanupListingIfComplete(claim.listingId);

        logAuction("CLAIMED", player.getCommandSenderName(), ConfigMarket.CurrencyName, claim.currency,
            claim.type == EnumClaimType.REFUND ? "Refund claimed" : "Sale proceeds claimed");

        return null; // Success
    }

    /**
     * Clean up a listing if it's ended and no longer needed.
     * Now just removes ended listings after some delay since claims are in PlayerData.
     */
    private void cleanupListingIfComplete(String listingId) {
        if (listingId == null || listingId.isEmpty()) return;

        AuctionListing listing = listings.get(listingId);
        if (listing == null) return;

        // Only cleanup ended/cancelled listings
        if (listing.status == EnumAuctionStatus.ACTIVE) return;

        // Remove ended listings (claims are now in PlayerData, not here)
        listings.remove(listingId);
        markDirty();
    }

    public int claimAll(EntityPlayer player) {
        int claimedCount = 0;
        List<AuctionClaim> playerClaims = getPlayerClaims(player);

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

        for (AuctionListing listing : listings.values()) {
            if (listing.status != EnumAuctionStatus.ACTIVE) continue;

            // Apply search filter (searches item name AND seller name)
            if (filter.hasSearchText()) {
                String itemName = listing.item != null ? listing.item.getDisplayName() : "";
                String sellerName = listing.sellerName != null ? listing.sellerName : "";
                if (!filter.matchesSearch(itemName, sellerName)) continue;
            }

            result.add(listing);
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
        for (AuctionListing listing : listings.values()) {
            if (listing.status != EnumAuctionStatus.ACTIVE) continue;
            if (filter.hasSearchText()) {
                String itemName = listing.item != null ? listing.item.getDisplayName() : "";
                String sellerName = listing.sellerName != null ? listing.sellerName : "";
                if (!filter.matchesSearch(itemName, sellerName)) continue;
            }
            count++;
        }
        return count;
    }

    @Override
    public AuctionListing getListing(String listingId) {
        return listings.get(listingId);
    }

    /**
     * Get a player's current currency balance.
     */
    public long getPlayerBalance(EntityPlayer player) {
        PlayerData playerData = PlayerData.get(player);
        if (playerData == null || playerData.tradeData == null) {
            return 0;
        }
        return playerData.tradeData.getBalance();
    }

    /**
     * Get player listing count using index
     */
    public int getPlayerListingCount(UUID playerUUID) {
        Set<String> ids = playerListingIds.get(playerUUID);
        return ids != null ? ids.size() : 0;
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
     * Includes active listings, active bids, and pending claims.
     */
    public int getPlayerTradeSlotCount(EntityPlayer player) {
        UUID playerUUID = player.getUniqueID();
        int count = 0;
        // Active listings
        Set<String> listingIds = playerListingIds.get(playerUUID);
        if (listingIds != null) count += listingIds.size();
        // Active bids
        Set<String> bidIds = playerBidIds.get(playerUUID);
        if (bidIds != null) count += bidIds.size();
        // Pending claims (from PlayerData)
        PlayerData playerData = PlayerData.get(player);
        if (playerData != null) {
            count += playerData.tradeData.getClaimCount();
        }
        return count;
    }

    /**
     * Get the total number of trade slots used by a player (UUID version).
     * For offline players, only returns listings + bids (claims are in their PlayerData).
     */
    public int getPlayerTradeSlotCount(UUID playerUUID) {
        int count = 0;
        // Active listings
        Set<String> listingIds = playerListingIds.get(playerUUID);
        if (listingIds != null) count += listingIds.size();
        // Active bids
        Set<String> bidIds = playerBidIds.get(playerUUID);
        if (bidIds != null) count += bidIds.size();
        // Can't get claims for offline player without loading their data
        return count;
    }

    /**
     * Get the maximum trade slots for a player.
     * Uses cached value if available, otherwise computes from permissions.
     */
    public int getMaxTradesForPlayer(EntityPlayer player) {
        UUID playerUUID = player.getUniqueID();

        // Check cache first
        Integer cached = playerMaxTradesCache.get(playerUUID);
        if (cached != null) {
            return cached;
        }

        // Compute and cache
        int maxTrades = computeMaxTradesForPlayer(player);
        playerMaxTradesCache.put(playerUUID, maxTrades);
        return maxTrades;
    }

    /**
     * Compute max trade slots from permissions.
     * Checks for customnpcs.auction.trades.X permissions where X is the slot count.
     * Uses the highest permission found, or DefaultMaxTrades if no permissions or lower.
     * Capped at MAX_TRADE_SLOTS (45).
     */
    private int computeMaxTradesForPlayer(EntityPlayer player) {
        // Check for unlimited permission
        if (CustomNpcsPermissions.hasCustomPermission(player, "customnpcs.auction.trades.*")) {
            return ConfigMarket.MAX_TRADE_SLOTS;
        }

        // Find highest allowed slot count from permissions
        int highestAllowed = 0;
        for (int i = 1; i <= ConfigMarket.MAX_TRADE_SLOTS; i++) {
            String perm = "customnpcs.auction.trades." + i;
            if (CustomNpcsPermissions.hasCustomPermission(player, perm)) {
                highestAllowed = i;
            }
        }

        // Use default if no permissions found or permission is lower than default
        if (highestAllowed == 0 || highestAllowed < ConfigMarket.DefaultMaxTrades) {
            highestAllowed = ConfigMarket.DefaultMaxTrades;
        }

        // Cap at maximum
        return Math.min(highestAllowed, ConfigMarket.MAX_TRADE_SLOTS);
    }

    /**
     * Refresh a player's cached max trades (call when permissions change).
     */
    public void refreshPlayerMaxTrades(EntityPlayer player) {
        UUID playerUUID = player.getUniqueID();
        int maxTrades = computeMaxTradesForPlayer(player);
        playerMaxTradesCache.put(playerUUID, maxTrades);
    }

    /**
     * Clear a player's cached max trades (call on logout).
     */
    public void clearPlayerMaxTradesCache(UUID playerUUID) {
        playerMaxTradesCache.remove(playerUUID);
    }

    // =========================================
    // Notifications
    // =========================================

    /**
     * Send a notification to a player.
     * If player is online, sends immediately. Otherwise, discards (claims will notify on login).
     */
    private void sendNotificationToPlayer(UUID playerUUID, EnumNotificationType type, String listingId, String message) {
        // Try to send immediately if player is online
        EntityPlayerMP player = (EntityPlayerMP) PlayerDataController.getPlayerFromUUID(playerUUID);
        if (player != null) {
            sendNotificationMessage(player, type, message);
        }
        // If offline, no need to store - they'll get notified about claims on login
    }

    /**
     * Send a notification chat message to a player.
     */
    private void sendNotificationMessage(EntityPlayerMP player, EnumNotificationType type, String message) {
        EnumChatFormatting color;
        switch (type) {
            case AUCTION_WON:
            case AUCTION_SOLD:
                color = EnumChatFormatting.GREEN;
                break;
            case AUCTION_OUTBID:
                color = EnumChatFormatting.YELLOW;
                break;
            case AUCTION_EXPIRED:
                color = EnumChatFormatting.RED;
                break;
            default:
                color = EnumChatFormatting.GRAY;
        }
        player.addChatMessage(new ChatComponentText(
            EnumChatFormatting.GOLD + "[Auction] " + color + message));
    }

    public void onPlayerLogin(EntityPlayer player) {
        // Cache player's max trades permission
        refreshPlayerMaxTrades(player);

        // Process expired claims for this player
        PlayerData playerData = PlayerData.get(player);
        if (playerData != null) {
            int expired = playerData.tradeData.processExpiredClaims();
            if (expired > 0) {
                playerData.updateClient = true;
                playerData.save();
                logAuction("CLAIM_EXPIRED", player.getCommandSenderName(), "Multiple", expired,
                    expired + " claims expired on login");
            }
        }

        // Check for pending claims and notify
        List<AuctionClaim> claims = getPlayerClaims(player);
        if (!claims.isEmpty()) {
            int itemClaims = 0;
            int currencyClaims = 0;
            long totalCurrency = 0;

            for (AuctionClaim claim : claims) {
                if (claim.type.isItem()) {
                    itemClaims++;
                } else {
                    currencyClaims++;
                    totalCurrency += claim.currency;
                }
            }

            StringBuilder msg = new StringBuilder();
            msg.append(EnumChatFormatting.GOLD).append("[Auction] ");
            msg.append(EnumChatFormatting.YELLOW).append("You have ");

            if (itemClaims > 0 && currencyClaims > 0) {
                msg.append(itemClaims).append(" item(s) and ");
                msg.append(String.format("%,d", totalCurrency)).append(" ").append(ConfigMarket.CurrencyName);
            } else if (itemClaims > 0) {
                msg.append(itemClaims).append(" item(s)");
            } else {
                msg.append(String.format("%,d", totalCurrency)).append(" ").append(ConfigMarket.CurrencyName);
            }
            msg.append(" to claim!");

            player.addChatMessage(new ChatComponentText(msg.toString()));
        }
    }

    public void onPlayerLogout(UUID playerUUID) {
        // Clear cached permission
        clearPlayerMaxTradesCache(playerUUID);
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
        final NBTTagCompound compound = writeToNBT(new NBTTagCompound());

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
        try {
            NBTTagCompound compound = writeToNBT(new NBTTagCompound());
            saveToFile(compound);
        } catch (Exception e) {
            LogWriter.error("Error saving auction data", e);
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
        playerListingIds.clear();
        playerBidIds.clear();

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

        // Index listings
        for (AuctionListing listing : listings.values()) {
            if (listing.status == EnumAuctionStatus.ACTIVE) {
                addToPlayerListings(listing.sellerUUID, listing.id);
                if (listing.highBidderUUID != null) {
                    addToPlayerBids(listing.highBidderUUID, listing.id);
                }
            }
        }

        // Claims are now stored in PlayerData, not indexed here
        LogWriter.info("Rebuilt auction indices: " + playerListingIds.size() + " sellers, " +
            playerBidIds.size() + " bidders");
    }

    private void loadFromFile(File file) throws Exception {
        NBTTagCompound compound = CompressedStreamTools.readCompressed(new FileInputStream(file));
        readFromNBT(compound);
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        // Listings only - claims are stored in PlayerData
        NBTTagList listingsList = new NBTTagList();
        for (AuctionListing listing : listings.values()) {
            listingsList.appendTag(listing.writeToNBT(new NBTTagCompound()));
        }
        compound.setTag("Listings", listingsList);

        return compound;
    }

    public void readFromNBT(NBTTagCompound compound) {
        NBTTagList listingsList = compound.getTagList("Listings", 10);
        for (int i = 0; i < listingsList.tagCount(); i++) {
            AuctionListing listing = AuctionListing.fromNBT(listingsList.getCompoundTagAt(i));
            listings.put(listing.id, listing);
        }
    }

    // =========================================
    // IAuctionHandler Implementation
    // =========================================

    @Override
    public boolean isEnabled() {
        return ConfigMarket.AuctionEnabled;
    }

    @Override
    public IAuctionListing[] getActiveListings() {
        List<AuctionListing> active = new ArrayList<>();
        for (AuctionListing listing : listings.values()) {
            if (listing.status == EnumAuctionStatus.ACTIVE && !listing.isExpired()) {
                active.add(listing);
            }
        }
        return active.toArray(new IAuctionListing[0]);
    }

    @Override
    public IAuctionListing[] getListingsBySeller(String sellerUUID) {
        if (sellerUUID == null) return new IAuctionListing[0];
        try {
            UUID uuid = UUID.fromString(sellerUUID);
            List<AuctionListing> result = getPlayerActiveListings(uuid);
            return result.toArray(new IAuctionListing[0]);
        } catch (IllegalArgumentException e) {
            return new IAuctionListing[0];
        }
    }

    @Override
    public IAuctionListing[] getListingsByBidder(String bidderUUID) {
        if (bidderUUID == null) return new IAuctionListing[0];
        try {
            UUID uuid = UUID.fromString(bidderUUID);
            List<AuctionListing> result = getPlayerActiveBids(uuid);
            return result.toArray(new IAuctionListing[0]);
        } catch (IllegalArgumentException e) {
            return new IAuctionListing[0];
        }
    }

    @Override
    public int getActiveListingCount() {
        int count = 0;
        for (AuctionListing listing : listings.values()) {
            if (listing.status == EnumAuctionStatus.ACTIVE && !listing.isExpired()) {
                count++;
            }
        }
        return count;
    }

    @Override
    public IAuctionListing createListing(IPlayer<?> player, IItemStack item, long startingPrice, long buyoutPrice) {
        if (player == null || item == null) return null;
        EntityPlayer entityPlayer = (EntityPlayer) player.getMCEntity();
        ItemStack mcItem = item.getMCItemStack();
        String result = createListing(entityPlayer, mcItem, startingPrice, buyoutPrice);
        if (result != null) {
            return null; // Failed
        }
        // Find the just-created listing
        Set<String> playerListings = playerListingIds.get(entityPlayer.getUniqueID());
        if (playerListings != null && !playerListings.isEmpty()) {
            // Get the most recent one
            long latestTime = 0;
            AuctionListing latestListing = null;
            for (String id : playerListings) {
                AuctionListing listing = listings.get(id);
                if (listing != null && listing.createdTime > latestTime) {
                    latestTime = listing.createdTime;
                    latestListing = listing;
                }
            }
            return latestListing;
        }
        return null;
    }

    @Override
    public String placeBid(String listingId, IPlayer<?> player, long amount) {
        if (player == null) return "Player is null";
        EntityPlayer entityPlayer = (EntityPlayer) player.getMCEntity();
        return placeBid(listingId, entityPlayer, amount);
    }

    @Override
    public String buyout(String listingId, IPlayer<?> player) {
        if (player == null) return "Player is null";
        EntityPlayer entityPlayer = (EntityPlayer) player.getMCEntity();
        return buyout(listingId, entityPlayer);
    }

    @Override
    public String cancelListing(String listingId, IPlayer<?> player, boolean isAdmin) {
        if (player == null) return "Player is null";
        EntityPlayer entityPlayer = (EntityPlayer) player.getMCEntity();
        return cancelListing(listingId, entityPlayer, isAdmin);
    }

    @Override
    public long getListingFee() {
        return ConfigMarket.ListingFee;
    }

    @Override
    public double getSalesTaxPercent() {
        return ConfigMarket.SalesTaxPercent;
    }

    @Override
    public double getMinBidIncrementPercent() {
        return ConfigMarket.MinBidIncrementPercent;
    }

    @Override
    public int getAuctionDurationHours() {
        return ConfigMarket.AuctionDurationHours;
    }

    @Override
    public int getSnipeProtectionMinutes() {
        return ConfigMarket.SnipeProtectionMinutes;
    }

    @Override
    public String getCurrencyName() {
        return ConfigMarket.CurrencyName;
    }

    @Override
    public long getMinimumListingPrice() {
        return ConfigMarket.MinimumListingPrice;
    }

    @Override
    public IAuctionListing[] searchListings(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return getActiveListings();
        }
        String search = searchText.toLowerCase().trim();
        List<AuctionListing> result = new ArrayList<>();
        for (AuctionListing listing : listings.values()) {
            if (listing.status != EnumAuctionStatus.ACTIVE || listing.isExpired()) continue;
            String itemName = listing.item != null ? listing.item.getDisplayName().toLowerCase() : "";
            String sellerName = listing.sellerName != null ? listing.sellerName.toLowerCase() : "";
            if (itemName.contains(search) || sellerName.contains(search)) {
                result.add(listing);
            }
        }
        return result.toArray(new IAuctionListing[0]);
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

    public void clearEndedListings() {
        // Remove ended/cancelled listings (claims are in PlayerData now)
        listings.entrySet().removeIf(entry ->
            entry.getValue().status == EnumAuctionStatus.CLAIMED ||
            entry.getValue().status == EnumAuctionStatus.ENDED ||
            entry.getValue().status == EnumAuctionStatus.CANCELLED);

        // Rebuild indices
        rebuildIndices();
        markDirty();
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
