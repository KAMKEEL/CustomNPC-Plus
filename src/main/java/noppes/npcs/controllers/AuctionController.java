package noppes.npcs.controllers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomNpcs;
import noppes.npcs.config.ConfigMarket;
import noppes.npcs.constants.EnumAuctionCategory;
import noppes.npcs.constants.EnumAuctionDuration;
import noppes.npcs.constants.EnumAuctionStatus;
import noppes.npcs.controllers.data.AuctionListing;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Controller for the global auction house.
 * Manages all auction listings, bidding, and claims.
 */
public class AuctionController {
    private static final Logger logger = LogManager.getLogger(CustomNpcs.class);

    public static AuctionController Instance;

    // All auction listings by ID
    private Map<Integer, AuctionListing> listings = new ConcurrentHashMap<>();

    // Next available listing ID
    private int nextId = 1;

    // Indices for fast lookup
    private Map<UUID, List<Integer>> listingsBySeller = new ConcurrentHashMap<>();
    private Map<UUID, List<Integer>> listingsByBidder = new ConcurrentHashMap<>();

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
            logger.warn("Auction listing validation failed: " + validation);
            return null;
        }

        // Check seller's listing limit
        int currentListings = getActiveListingsCount(seller.getUniqueID());
        int maxListings = getMaxListings(seller);
        if (currentListings >= maxListings) {
            logger.info("Player {} has reached max listings ({}/{})",
                seller.getCommandSenderName(), currentListings, maxListings);
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
                logger.info("Player {} cannot afford listing fee of {}",
                    seller.getCommandSenderName(), listingFee);
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
        logger.info("Created auction listing {} by {} for {}",
            listing.id, seller.getCommandSenderName(),
            item.getDisplayName());

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
     * Get active listings by category
     */
    public List<AuctionListing> getActiveListings(EnumAuctionCategory category) {
        if (category == EnumAuctionCategory.ALL) {
            return getActiveListings();
        }
        return getActiveListings().stream()
            .filter(l -> l.category == category)
            .collect(Collectors.toList());
    }

    /**
     * Search listings by item name
     */
    public List<AuctionListing> searchListings(String query) {
        String lowerQuery = query.toLowerCase();
        return getActiveListings().stream()
            .filter(l -> l.item.getDisplayName().toLowerCase().contains(lowerQuery))
            .collect(Collectors.toList());
    }

    /**
     * Get all listings by a seller (all statuses)
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
            .filter(l -> bidderUUID.equals(l.highBidderUUID))
            .collect(Collectors.toList());
    }

    /**
     * Get listings that need claiming by a player
     * (items won, sales proceeds, cancelled/expired items)
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
     * @return true if bid was successful
     */
    public boolean placeBid(int listingId, EntityPlayer bidder, long amount) {
        AuctionListing listing = listings.get(listingId);
        if (listing == null) {
            return false;
        }

        // Check if bidder can afford
        if (!CurrencyController.Instance.canAfford(bidder, amount)) {
            return false;
        }

        // Store previous bidder for refund
        UUID previousBidder = listing.highBidderUUID;
        long previousBidAmount = listing.currentBid;

        // Try to place bid
        if (!listing.placeBid(bidder.getUniqueID(), bidder.getCommandSenderName(), amount)) {
            return false;
        }

        // Withdraw bid amount from new bidder
        CurrencyController.Instance.withdraw(bidder, amount);

        // Refund previous bidder via claims system
        if (previousBidder != null) {
            CurrencyController.Instance.addClaim(
                previousBidder,
                previousBidAmount,
                "Auction Outbid",
                String.valueOf(listingId)
            );
        }

        // Update bidder index
        addToBidderIndex(bidder.getUniqueID(), listingId);

        save();
        logger.info("Bid placed on listing {} by {} for {}",
            listingId, bidder.getCommandSenderName(), amount);

        return true;
    }

    /**
     * Execute buyout on an auction
     * @return true if buyout was successful
     */
    public boolean buyout(int listingId, EntityPlayer buyer) {
        AuctionListing listing = listings.get(listingId);
        if (listing == null || listing.buyoutPrice <= 0) {
            return false;
        }

        // Check if buyer can afford
        if (!CurrencyController.Instance.canAfford(buyer, listing.buyoutPrice)) {
            return false;
        }

        // Store previous bidder for refund
        UUID previousBidder = listing.highBidderUUID;
        long previousBidAmount = listing.currentBid;

        // Execute buyout
        if (!listing.buyout(buyer.getUniqueID(), buyer.getCommandSenderName())) {
            return false;
        }

        // Withdraw buyout amount from buyer
        CurrencyController.Instance.withdraw(buyer, listing.buyoutPrice);

        // Refund previous bidder via claims system
        if (previousBidder != null) {
            CurrencyController.Instance.addClaim(
                previousBidder,
                previousBidAmount,
                "Auction Outbid (Buyout)",
                String.valueOf(listingId)
            );
        }

        // Update bidder index
        addToBidderIndex(buyer.getUniqueID(), listingId);

        save();
        logger.info("Buyout executed on listing {} by {} for {}",
            listingId, buyer.getCommandSenderName(), listing.buyoutPrice);

        return true;
    }

    /**
     * Cancel an auction listing
     * @return true if cancellation was successful
     */
    public boolean cancelListing(int listingId, EntityPlayer requester) {
        AuctionListing listing = listings.get(listingId);
        if (listing == null) {
            return false;
        }

        if (!listing.cancel(requester.getUniqueID())) {
            return false;
        }

        save();
        logger.info("Listing {} cancelled by {}",
            listingId, requester.getCommandSenderName());

        return true;
    }

    // ==================== Claiming ====================

    /**
     * Claim seller proceeds from a sold auction
     * @return true if claim was successful
     */
    public boolean claimSellerProceeds(int listingId, EntityPlayer seller) {
        AuctionListing listing = listings.get(listingId);
        if (listing == null) {
            return false;
        }

        if (!listing.sellerUUID.equals(seller.getUniqueID())) {
            return false;  // Not the seller
        }

        if (listing.status != EnumAuctionStatus.SOLD) {
            return false;  // Not sold
        }

        if (listing.sellerClaimed) {
            return false;  // Already claimed
        }

        // Deposit proceeds to seller
        long proceeds = listing.getSellerProceeds();
        CurrencyController.Instance.deposit(seller, proceeds);

        listing.sellerClaimed = true;
        checkAndMarkClaimed(listing);
        save();

        logger.info("Seller {} claimed {} from listing {}",
            seller.getCommandSenderName(), proceeds, listingId);

        return true;
    }

    /**
     * Claim item from won auction
     * @return The item if successful, null otherwise
     */
    public ItemStack claimBuyerItem(int listingId, EntityPlayer buyer) {
        AuctionListing listing = listings.get(listingId);
        if (listing == null) {
            return null;
        }

        if (listing.highBidderUUID == null ||
            !listing.highBidderUUID.equals(buyer.getUniqueID())) {
            return null;  // Not the buyer
        }

        if (listing.status != EnumAuctionStatus.SOLD) {
            return null;  // Not sold
        }

        if (listing.buyerClaimed) {
            return null;  // Already claimed
        }

        listing.buyerClaimed = true;
        checkAndMarkClaimed(listing);
        save();

        logger.info("Buyer {} claimed item from listing {}",
            buyer.getCommandSenderName(), listingId);

        return listing.item.copy();
    }

    /**
     * Claim item from cancelled or expired auction (seller reclaiming)
     * @return The item if successful, null otherwise
     */
    public ItemStack claimExpiredItem(int listingId, EntityPlayer seller) {
        AuctionListing listing = listings.get(listingId);
        if (listing == null) {
            return null;
        }

        if (!listing.sellerUUID.equals(seller.getUniqueID())) {
            return null;  // Not the seller
        }

        if (listing.status != EnumAuctionStatus.CANCELLED &&
            listing.status != EnumAuctionStatus.EXPIRED) {
            return null;  // Not claimable
        }

        if (listing.sellerClaimed) {
            return null;  // Already claimed
        }

        listing.sellerClaimed = true;
        listing.status = EnumAuctionStatus.CLAIMED;
        save();

        logger.info("Seller {} reclaimed item from listing {}",
            seller.getCommandSenderName(), listingId);

        return listing.item.copy();
    }

    /**
     * Mark listing as fully claimed if both parties have claimed
     */
    private void checkAndMarkClaimed(AuctionListing listing) {
        if (listing.status == EnumAuctionStatus.SOLD) {
            if (listing.sellerClaimed && listing.buyerClaimed) {
                listing.status = EnumAuctionStatus.CLAIMED;
            }
        }
    }

    // ==================== Expiration Processing ====================

    /**
     * Process all expired auctions
     * Call this periodically (e.g., every minute)
     */
    public void processExpiredAuctions() {
        boolean changed = false;

        for (AuctionListing listing : listings.values()) {
            if (listing.status == EnumAuctionStatus.ACTIVE && listing.isExpired()) {
                listing.finalize();
                changed = true;

                // If sold, add proceeds to seller's claims
                if (listing.status == EnumAuctionStatus.SOLD) {
                    // Proceeds will be claimed manually, no automatic deposit
                    logger.info("Auction {} ended with sale to {} for {}",
                        listing.id, listing.highBidderName, listing.currentBid);
                } else {
                    logger.info("Auction {} expired without bids", listing.id);
                }
            }
        }

        if (changed) {
            save();
        }
    }

    /**
     * Clean up old claimed listings
     * Call this periodically (e.g., daily)
     */
    public void cleanupOldListings() {
        long claimExpirationMs = ConfigMarket.ClaimExpirationDays * 24L * 60L * 60L * 1000L;
        if (claimExpirationMs <= 0) {
            return;  // No expiration
        }

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
            logger.info("Cleaned up {} old auction listings", toRemove.size());
        }
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

    // ==================== Permission Helpers ====================

    /**
     * Get maximum listings allowed for a player
     */
    public int getMaxListings(EntityPlayer player) {
        // TODO: Check for permission-based overrides
        // e.g., CustomNpcsPermissions.hasCustomPermission(player, "cnpc.auction.slots.10")
        return ConfigMarket.DefaultMaxListings;
    }

    // ==================== Persistence ====================

    public void save() {
        File saveDir = CustomNpcs.getWorldSaveDirectory();
        if (saveDir == null) {
            return;
        }

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
                if (backup.exists()) {
                    backup.delete();
                }
                file.renameTo(backup);
            }

            file = new File(saveDir, "auctions.dat");
            FileOutputStream fos = new FileOutputStream(file);
            CompressedStreamTools.writeCompressed(compound, fos);
            fos.close();

        } catch (Exception e) {
            logger.error("Error saving auctions", e);
        }
    }

    public void load() {
        File saveDir = CustomNpcs.getWorldSaveDirectory();
        if (saveDir == null) {
            return;
        }

        listings.clear();

        try {
            File file = new File(saveDir, "auctions.dat");
            if (!file.exists()) {
                file = new File(saveDir, "auctions.dat_old");
                if (!file.exists()) {
                    return;
                }
            }

            FileInputStream fis = new FileInputStream(file);
            NBTTagCompound compound = CompressedStreamTools.readCompressed(fis);
            fis.close();

            nextId = compound.getInteger("NextId");
            if (nextId < 1) {
                nextId = 1;
            }

            NBTTagList listingList = compound.getTagList("Listings", 10);
            for (int i = 0; i < listingList.tagCount(); i++) {
                AuctionListing listing = new AuctionListing();
                listing.readFromNBT(listingList.getCompoundTagAt(i));
                listings.put(listing.id, listing);
            }

            rebuildIndices();

            logger.info("Loaded {} auction listings", listings.size());

        } catch (Exception e) {
            logger.error("Error loading auctions", e);
        }
    }

    // ==================== Statistics ====================

    /**
     * Get total number of active listings
     */
    public int getActiveListingCount() {
        return (int) listings.values().stream()
            .filter(l -> l.status == EnumAuctionStatus.ACTIVE)
            .count();
    }

    /**
     * Get total number of all listings
     */
    public int getTotalListingCount() {
        return listings.size();
    }
}
