package kamkeel.npcs.network.packets.player;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumPlayerPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.config.ConfigMarket;
import noppes.npcs.constants.EnumAuctionDuration;
import noppes.npcs.constants.EnumRoleType;
import noppes.npcs.containers.ContainerAuction;
import noppes.npcs.controllers.AuctionController;
import noppes.npcs.controllers.CurrencyController;
import noppes.npcs.controllers.data.AuctionFilter;
import noppes.npcs.controllers.data.AuctionListing;
import noppes.npcs.util.NBTJsonUtil;
import kamkeel.npcs.network.packets.data.large.AuctionDataPacket;

import java.io.IOException;

/**
 * Packet for all auction-related player actions.
 * Client -> Server for actions, Server -> Client for responses.
 */
public class AuctionActionPacket extends AbstractPacket {
    public static final String packetName = "Player|Auction";

    private Action action;
    private int listingId;
    private long amount;
    private long buyoutPrice;
    private int durationOrdinal;
    private NBTTagCompound itemData;
    private NBTTagCompound filterData;  // For query operations
    private String responseMessage;
    private boolean success;

    public AuctionActionPacket() {
    }

    private AuctionActionPacket(Action action) {
        this.action = action;
    }

    @Override
    public boolean needsNPC() {
        return true;
    }

    // ==================== Client -> Server Actions ====================

    /**
     * Create a new auction listing
     */
    @SideOnly(Side.CLIENT)
    public static void CreateListing(ItemStack item, long startingPrice, long buyoutPrice, EnumAuctionDuration duration) {
        AuctionActionPacket packet = new AuctionActionPacket(Action.CreateListing);
        packet.amount = startingPrice;
        packet.buyoutPrice = buyoutPrice;
        packet.durationOrdinal = duration.ordinal();
        packet.itemData = item.writeToNBT(new NBTTagCompound());
        PacketClient.sendClient(packet);
    }

    /**
     * Place a bid on an auction
     */
    @SideOnly(Side.CLIENT)
    public static void PlaceBid(int listingId, long bidAmount) {
        AuctionActionPacket packet = new AuctionActionPacket(Action.PlaceBid);
        packet.listingId = listingId;
        packet.amount = bidAmount;
        PacketClient.sendClient(packet);
    }

    /**
     * Execute buyout on an auction
     */
    @SideOnly(Side.CLIENT)
    public static void Buyout(int listingId) {
        AuctionActionPacket packet = new AuctionActionPacket(Action.Buyout);
        packet.listingId = listingId;
        PacketClient.sendClient(packet);
    }

    /**
     * Cancel own listing
     */
    @SideOnly(Side.CLIENT)
    public static void CancelListing(int listingId) {
        AuctionActionPacket packet = new AuctionActionPacket(Action.CancelListing);
        packet.listingId = listingId;
        PacketClient.sendClient(packet);
    }

    /**
     * Claim item from won/expired/cancelled auction
     */
    @SideOnly(Side.CLIENT)
    public static void ClaimItem(int listingId) {
        AuctionActionPacket packet = new AuctionActionPacket(Action.ClaimItem);
        packet.listingId = listingId;
        PacketClient.sendClient(packet);
    }

    /**
     * Claim proceeds from sold auction
     */
    @SideOnly(Side.CLIENT)
    public static void ClaimProceeds(int listingId) {
        AuctionActionPacket packet = new AuctionActionPacket(Action.ClaimProceeds);
        packet.listingId = listingId;
        PacketClient.sendClient(packet);
    }

    /**
     * Query filtered listings
     */
    @SideOnly(Side.CLIENT)
    public static void QueryListings(AuctionFilter filter) {
        AuctionActionPacket packet = new AuctionActionPacket(Action.QueryListings);
        packet.filterData = filter.writeToNBT(new NBTTagCompound());
        PacketClient.sendClient(packet);
    }

    /**
     * Request player's own listings
     */
    @SideOnly(Side.CLIENT)
    public static void QueryMyListings() {
        AuctionActionPacket packet = new AuctionActionPacket(Action.QueryMyListings);
        PacketClient.sendClient(packet);
    }

    /**
     * Request player's active bids
     */
    @SideOnly(Side.CLIENT)
    public static void QueryMyBids() {
        AuctionActionPacket packet = new AuctionActionPacket(Action.QueryMyBids);
        PacketClient.sendClient(packet);
    }

    /**
     * Request player's claimable items
     */
    @SideOnly(Side.CLIENT)
    public static void QueryClaimable() {
        AuctionActionPacket packet = new AuctionActionPacket(Action.QueryClaimable);
        PacketClient.sendClient(packet);
    }

    @Override
    public Enum getType() {
        return EnumPlayerPacket.AuctionAction;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.PLAYER_PACKET;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(action.ordinal());

        switch (action) {
            case CreateListing:
                out.writeLong(amount);
                out.writeLong(buyoutPrice);
                out.writeInt(durationOrdinal);
                String itemJson = NBTJsonUtil.Convert(itemData);
                byte[] itemBytes = itemJson.getBytes("UTF-8");
                out.writeInt(itemBytes.length);
                out.writeBytes(itemBytes);
                break;

            case PlaceBid:
                out.writeInt(listingId);
                out.writeLong(amount);
                break;

            case Buyout:
            case CancelListing:
            case ClaimItem:
            case ClaimProceeds:
                out.writeInt(listingId);
                break;

            case QueryListings:
                String filterJson = NBTJsonUtil.Convert(filterData);
                byte[] filterBytes = filterJson.getBytes("UTF-8");
                out.writeInt(filterBytes.length);
                out.writeBytes(filterBytes);
                break;

            case QueryMyListings:
            case QueryMyBids:
            case QueryClaimable:
                // No additional data needed
                break;
        }
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        Action requestedAction = Action.values()[in.readInt()];

        if (!(player instanceof EntityPlayerMP)) {
            return;
        }

        EntityPlayerMP playerMP = (EntityPlayerMP) player;

        // Verify NPC is an Auctioneer
        if (npc == null || npc.advanced.role != EnumRoleType.Auctioneer) {
            return;
        }

        // Check if auction system is enabled
        if (!ConfigMarket.AuctionEnabled) {
            sendResponse(playerMP, false, "Auction system is disabled");
            return;
        }

        // Check controller exists
        if (AuctionController.Instance == null) {
            sendResponse(playerMP, false, "Auction controller not initialized");
            return;
        }

        switch (requestedAction) {
            case CreateListing:
                handleCreateListing(in, playerMP);
                break;
            case PlaceBid:
                handlePlaceBid(in, playerMP);
                break;
            case Buyout:
                handleBuyout(in, playerMP);
                break;
            case CancelListing:
                handleCancelListing(in, playerMP);
                break;
            case ClaimItem:
                handleClaimItem(in, playerMP);
                break;
            case ClaimProceeds:
                handleClaimProceeds(in, playerMP);
                break;
            case QueryListings:
                handleQueryListings(in, playerMP);
                break;
            case QueryMyListings:
                handleQueryMyListings(playerMP);
                break;
            case QueryMyBids:
                handleQueryMyBids(playerMP);
                break;
            case QueryClaimable:
                handleQueryClaimable(playerMP);
                break;
        }
    }

    private void handleCreateListing(ByteBuf in, EntityPlayerMP player) throws IOException {
        // Check permission
        if (!CustomNpcsPermissions.hasPermission(player, CustomNpcsPermissions.AUCTION_LIST)) {
            sendResponse(player, false, "You don't have permission to create listings");
            return;
        }

        long startingPrice = in.readLong();
        long buyoutPrice = in.readLong();
        int durationOrdinal = in.readInt();

        int itemBytesLength = in.readInt();
        byte[] itemBytes = new byte[itemBytesLength];
        in.readBytes(itemBytes);
        String itemJson = new String(itemBytes, "UTF-8");

        try {
            NBTTagCompound itemNBT = NBTJsonUtil.Convert(itemJson);
            ItemStack requestedItem = ItemStack.loadItemStackFromNBT(itemNBT);

            if (!(player.openContainer instanceof ContainerAuction)) {
                sendResponse(player, false, "Open the auction listing screen to create a listing");
                return;
            }

            ContainerAuction container = (ContainerAuction) player.openContainer;
            ItemStack slotItem = container.getListingItem();

            if (requestedItem == null || slotItem == null) {
                sendResponse(player, false, "Place an item in the listing slot");
                return;
            }

            if (!ItemStack.areItemStacksEqual(slotItem, requestedItem) || slotItem.stackSize != requestedItem.stackSize) {
                sendResponse(player, false, "Listing item mismatch");
                return;
            }

            // Validate duration
            EnumAuctionDuration duration;
            if (durationOrdinal >= 0 && durationOrdinal < EnumAuctionDuration.values().length) {
                duration = EnumAuctionDuration.values()[durationOrdinal];
            } else {
                duration = EnumAuctionDuration.MEDIUM;
            }

            // Validate prices
            if (startingPrice < 1) {
                sendResponse(player, false, "Starting price must be at least 1");
                return;
            }

            if (buyoutPrice > 0 && buyoutPrice < startingPrice) {
                sendResponse(player, false, "Buyout price must be higher than starting price");
                return;
            }

            // Create the listing
            AuctionListing listing = AuctionController.Instance.createListing(
                player, slotItem, startingPrice, buyoutPrice, duration);

            if (listing != null) {
                container.clearListingSlot();
                sendResponse(player, true, "Listing created successfully");
            } else {
                sendResponse(player, false, "Failed to create listing. Check your balance and listing limit.");
            }
        } catch (Exception e) {
            sendResponse(player, false, "Error creating listing: " + e.getMessage());
        }
    }

    private void handlePlaceBid(ByteBuf in, EntityPlayerMP player) throws IOException {
        // Check permission
        if (!CustomNpcsPermissions.hasPermission(player, CustomNpcsPermissions.AUCTION_BID)) {
            sendResponse(player, false, "You don't have permission to bid");
            return;
        }

        int listingId = in.readInt();
        long bidAmount = in.readLong();

        AuctionListing listing = AuctionController.Instance.getListing(listingId);
        if (listing == null) {
            sendResponse(player, false, "Listing not found");
            return;
        }

        if (!listing.isActive()) {
            sendResponse(player, false, "This auction has ended");
            return;
        }

        if (bidAmount < listing.getMinimumBid()) {
            sendResponse(player, false, "Bid must be at least " + listing.getMinimumBid());
            return;
        }

        if (!CurrencyController.Instance.canAfford(player, bidAmount)) {
            sendResponse(player, false, "You cannot afford this bid");
            return;
        }

        if (AuctionController.Instance.placeBid(listingId, player, bidAmount)) {
            sendResponse(player, true, "Bid placed successfully");
        } else {
            sendResponse(player, false, "Failed to place bid");
        }
    }

    private void handleBuyout(ByteBuf in, EntityPlayerMP player) throws IOException {
        // Check permission
        if (!CustomNpcsPermissions.hasPermission(player, CustomNpcsPermissions.AUCTION_BUYOUT)) {
            sendResponse(player, false, "You don't have permission to use buyout");
            return;
        }

        int listingId = in.readInt();

        AuctionListing listing = AuctionController.Instance.getListing(listingId);
        if (listing == null) {
            sendResponse(player, false, "Listing not found");
            return;
        }

        if (!listing.isActive()) {
            sendResponse(player, false, "This auction has ended");
            return;
        }

        if (listing.buyoutPrice <= 0) {
            sendResponse(player, false, "This listing has no buyout option");
            return;
        }

        if (!CurrencyController.Instance.canAfford(player, listing.buyoutPrice)) {
            sendResponse(player, false, "You cannot afford the buyout price");
            return;
        }

        if (AuctionController.Instance.buyout(listingId, player)) {
            sendResponse(player, true, "Buyout successful! Claim your item.");
        } else {
            sendResponse(player, false, "Failed to execute buyout");
        }
    }

    private void handleCancelListing(ByteBuf in, EntityPlayerMP player) throws IOException {
        int listingId = in.readInt();

        AuctionListing listing = AuctionController.Instance.getListing(listingId);
        if (listing == null) {
            sendResponse(player, false, "Listing not found");
            return;
        }

        // Admin can cancel any listing
        boolean isAdmin = CustomNpcsPermissions.hasPermission(player, CustomNpcsPermissions.AUCTION_ADMIN);

        if (!listing.sellerUUID.equals(player.getUniqueID()) && !isAdmin) {
            sendResponse(player, false, "You can only cancel your own listings");
            return;
        }

        if (listing.hasBids() && !isAdmin) {
            sendResponse(player, false, "Cannot cancel listing with active bids");
            return;
        }

        if (AuctionController.Instance.cancelListing(listingId, player, isAdmin)) {
            sendResponse(player, true, "Listing cancelled. Claim your item.");
        } else {
            sendResponse(player, false, "Failed to cancel listing");
        }
    }

    private void handleClaimItem(ByteBuf in, EntityPlayerMP player) throws IOException {
        int listingId = in.readInt();

        AuctionListing listing = AuctionController.Instance.getListing(listingId);
        if (listing == null) {
            sendResponse(player, false, "Listing not found");
            return;
        }

        ItemStack claimed = null;

        // Try buyer claim first
        if (listing.highBidderUUID != null && listing.highBidderUUID.equals(player.getUniqueID())) {
            claimed = AuctionController.Instance.claimBuyerItem(listingId, player);
        }
        // Then try seller reclaim (for expired/cancelled)
        else if (listing.sellerUUID.equals(player.getUniqueID())) {
            claimed = AuctionController.Instance.claimExpiredItem(listingId, player);
        }

        if (claimed != null) {
            // Give item to player
            if (!player.inventory.addItemStackToInventory(claimed)) {
                // Drop if inventory full
                player.entityDropItem(claimed, 0.5f);
            }
            sendResponse(player, true, "Item claimed");
        } else {
            sendResponse(player, false, "Cannot claim item");
        }
    }

    private void handleClaimProceeds(ByteBuf in, EntityPlayerMP player) throws IOException {
        int listingId = in.readInt();

        AuctionListing listing = AuctionController.Instance.getListing(listingId);
        if (listing == null) {
            sendResponse(player, false, "Listing not found");
            return;
        }

        if (AuctionController.Instance.claimSellerProceeds(listingId, player)) {
            long proceeds = listing.getSellerProceeds();
            sendResponse(player, true, "Claimed " + proceeds + " " + ConfigMarket.CurrencyName);
        } else {
            sendResponse(player, false, "Cannot claim proceeds");
        }
    }

    // ==================== Query Handlers ====================

    private void handleQueryListings(ByteBuf in, EntityPlayerMP player) throws IOException {
        // Check permission
        if (!CustomNpcsPermissions.hasPermission(player, CustomNpcsPermissions.AUCTION_USE)) {
            sendResponse(player, false, "You don't have permission to view auctions");
            return;
        }

        int filterBytesLength = in.readInt();
        byte[] filterBytes = new byte[filterBytesLength];
        in.readBytes(filterBytes);
        String filterJson = new String(filterBytes, "UTF-8");

        try {
            NBTTagCompound filterNBT = NBTJsonUtil.Convert(filterJson);
            AuctionFilter filter = new AuctionFilter();
            filter.readFromNBT(filterNBT);

            // Send filtered listings to client
            AuctionDataPacket.sendFilteredListings(player, filter);
        } catch (Exception e) {
            sendResponse(player, false, "Error parsing filter");
        }
    }

    private void handleQueryMyListings(EntityPlayerMP player) {
        // Check permission
        if (!CustomNpcsPermissions.hasPermission(player, CustomNpcsPermissions.AUCTION_USE)) {
            sendResponse(player, false, "You don't have permission to view auctions");
            return;
        }

        AuctionDataPacket.sendMyListings(player);
    }

    private void handleQueryMyBids(EntityPlayerMP player) {
        // Check permission
        if (!CustomNpcsPermissions.hasPermission(player, CustomNpcsPermissions.AUCTION_USE)) {
            sendResponse(player, false, "You don't have permission to view auctions");
            return;
        }

        AuctionDataPacket.sendMyBids(player);
    }

    private void handleQueryClaimable(EntityPlayerMP player) {
        // Check permission
        if (!CustomNpcsPermissions.hasPermission(player, CustomNpcsPermissions.AUCTION_USE)) {
            sendResponse(player, false, "You don't have permission to view auctions");
            return;
        }

        AuctionDataPacket.sendClaimable(player);
    }

    /**
     * Send a response message to the player
     */
    private void sendResponse(EntityPlayerMP player, boolean success, String message) {
        // Use chat message for now; can be extended to use GUI updates
        if (message != null && !message.isEmpty()) {
            player.addChatMessage(new ChatComponentText("[Auction] " + message));
        }
    }

    private enum Action {
        CreateListing,
        PlaceBid,
        Buyout,
        CancelListing,
        ClaimItem,
        ClaimProceeds,
        QueryListings,
        QueryMyListings,
        QueryMyBids,
        QueryClaimable
    }
}
