package kamkeel.npcs.network.packets.player;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumPlayerPacket;
import kamkeel.npcs.network.packets.data.large.GuiDataPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.config.ConfigMarket;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumRoleType;
import noppes.npcs.controllers.AuctionController;
import noppes.npcs.controllers.data.AuctionClaim;
import noppes.npcs.controllers.data.AuctionFilter;
import noppes.npcs.controllers.data.AuctionListing;

import java.io.IOException;
import java.util.List;

/**
 * Handles all auction-related client-server communication.
 */
public class AuctionActionPacket extends AbstractPacket {
    public static final String packetName = "Player|Auction";
    private static final int PAGE_SIZE = 45; // 9x5 grid

    private Action action;
    private String listingId;
    private long amount;
    private NBTTagCompound data;

    public AuctionActionPacket() {}

    private AuctionActionPacket(Action action) {
        this.action = action;
    }

    private AuctionActionPacket(Action action, String listingId) {
        this.action = action;
        this.listingId = listingId;
    }

    private AuctionActionPacket(Action action, String listingId, long amount) {
        this.action = action;
        this.listingId = listingId;
        this.amount = amount;
    }

    @Override
    public boolean needsNPC() { return false; }  // Allow opening via command without NPC

    // =========================================
    // Client-side static methods
    // =========================================

    @SideOnly(Side.CLIENT)
    public static void placeBid(String listingId, long bidAmount) {
        AuctionActionPacket packet = new AuctionActionPacket(Action.Bid, listingId, bidAmount);
        PacketClient.sendClient(packet);
    }

    @SideOnly(Side.CLIENT)
    public static void buyout(String listingId) {
        AuctionActionPacket packet = new AuctionActionPacket(Action.Buyout, listingId);
        PacketClient.sendClient(packet);
    }

    @SideOnly(Side.CLIENT)
    public static void cancelListing(String listingId) {
        AuctionActionPacket packet = new AuctionActionPacket(Action.Cancel, listingId);
        PacketClient.sendClient(packet);
    }

    @SideOnly(Side.CLIENT)
    public static void claimItem(String claimId) {
        AuctionActionPacket packet = new AuctionActionPacket(Action.ClaimItem, claimId);
        PacketClient.sendClient(packet);
    }

    @SideOnly(Side.CLIENT)
    public static void claimCurrency(String claimId) {
        AuctionActionPacket packet = new AuctionActionPacket(Action.ClaimCurrency, claimId);
        PacketClient.sendClient(packet);
    }

    @SideOnly(Side.CLIENT)
    public static void createListing(ItemStack item, long startingPrice, long buyoutPrice) {
        AuctionActionPacket packet = new AuctionActionPacket(Action.CreateListing);
        packet.amount = startingPrice;
        packet.listingId = String.valueOf(buyoutPrice);
        packet.data = new NBTTagCompound();
        item.writeToNBT(packet.data);
        PacketClient.sendClient(packet);
    }

    @SideOnly(Side.CLIENT)
    public static void openPage(int page) {
        AuctionActionPacket packet = new AuctionActionPacket(Action.OpenPage);
        packet.amount = page;
        PacketClient.sendClient(packet);
    }

    @SideOnly(Side.CLIENT)
    public static void openBidding(String listingId) {
        AuctionActionPacket packet = new AuctionActionPacket(Action.OpenBidding, listingId);
        PacketClient.sendClient(packet);
    }

    /** Request listings from server with filter and page */
    @SideOnly(Side.CLIENT)
    public static void requestListings(AuctionFilter filter, int page) {
        AuctionActionPacket packet = new AuctionActionPacket(Action.RequestListings);
        packet.amount = page;
        packet.data = filter.writeToNBT(new NBTTagCompound());
        PacketClient.sendClient(packet);
    }

    // =========================================
    // Packet Implementation
    // =========================================

    @Override
    public Enum getType() { return EnumPlayerPacket.AuctionAction; }

    @Override
    public PacketChannel getChannel() { return PacketHandler.PLAYER_PACKET; }

    @Override
    @SideOnly(Side.CLIENT)
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(action.ordinal());

        switch (action) {
            case Bid:
                ByteBufUtils.writeString(out, listingId);
                out.writeLong(amount);
                break;
            case Buyout:
            case Cancel:
            case ClaimItem:
            case ClaimCurrency:
                ByteBufUtils.writeString(out, listingId);
                break;
            case CreateListing:
                out.writeLong(amount);
                ByteBufUtils.writeString(out, listingId);
                ByteBufUtils.writeNBT(out, data);
                break;
            case OpenPage:
                out.writeInt((int) amount);
                break;
            case OpenBidding:
                ByteBufUtils.writeString(out, listingId);
                break;
            case RequestListings:
                out.writeInt((int) amount);
                ByteBufUtils.writeNBT(out, data);
                break;
        }
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP)) return;
        EntityPlayerMP playerMP = (EntityPlayerMP) player;

        // Skip role check if NPC is null (opened via command/script)
        // Otherwise require Auctioneer role
        if (npc != null && npc.advanced.role != EnumRoleType.Auctioneer) return;
        if (!ConfigMarket.AuctionEnabled) return;

        int actionOrdinal = in.readInt();
        if (actionOrdinal < 0 || actionOrdinal >= Action.values().length) {
            return; // Invalid action
        }
        Action requestedAction = Action.values()[actionOrdinal];

        AuctionController controller = AuctionController.getInstance();
        if (controller == null) {
            sendError(playerMP, "Auction system is not available.");
            return;
        }

        String result = null;

        switch (requestedAction) {
            case Bid:
                result = handleBid(in, playerMP, controller);
                break;

            case Buyout:
                result = handleBuyout(in, playerMP, controller);
                break;

            case Cancel:
                result = handleCancel(in, playerMP, controller);
                break;

            case ClaimItem:
                result = handleClaimItem(in, playerMP, controller);
                break;

            case ClaimCurrency:
                result = handleClaimCurrency(in, playerMP, controller);
                break;

            case CreateListing:
                result = handleCreateListing(in, playerMP, controller);
                break;

            case OpenPage:
                handleOpenPage(in, playerMP);
                break;

            case OpenBidding:
                handleOpenBidding(in, playerMP, controller);
                break;

            case RequestListings:
                handleRequestListings(in, playerMP, controller);
                break;
        }

        if (result != null) {
            sendError(playerMP, result);
        }
    }

    // =========================================
    // Action Handlers
    // =========================================

    private String handleBid(ByteBuf in, EntityPlayerMP player, AuctionController controller) throws IOException {
        String id = ByteBufUtils.readString(in);
        long amount = in.readLong();
        String result = controller.placeBid(id, player, amount);
        if (result == null) {
            sendListingsUpdate(player);
        }
        return result;
    }

    private String handleBuyout(ByteBuf in, EntityPlayerMP player, AuctionController controller) throws IOException {
        String id = ByteBufUtils.readString(in);
        String result = controller.buyout(id, player);
        if (result == null) {
            sendListingsUpdate(player);
        }
        return result;
    }

    private String handleCancel(ByteBuf in, EntityPlayerMP player, AuctionController controller) throws IOException {
        String id = ByteBufUtils.readString(in);
        String result = controller.cancelListing(id, player, false);
        if (result == null) {
            sendTradesUpdate(player);
        }
        return result;
    }

    private String handleClaimItem(ByteBuf in, EntityPlayerMP player, AuctionController controller) throws IOException {
        String id = ByteBufUtils.readString(in);
        String result = controller.claimItem(id, player);
        if (result == null) {
            sendTradesUpdate(player);
        }
        return result;
    }

    private String handleClaimCurrency(ByteBuf in, EntityPlayerMP player, AuctionController controller) throws IOException {
        String id = ByteBufUtils.readString(in);
        String result = controller.claimCurrency(id, player);
        if (result == null) {
            sendTradesUpdate(player);
        }
        return result;
    }

    private String handleCreateListing(ByteBuf in, EntityPlayerMP player, AuctionController controller) throws IOException {
        long startingPrice = in.readLong();
        String buyoutStr = ByteBufUtils.readString(in);
        long buyoutPrice = 0;
        try { buyoutPrice = Long.parseLong(buyoutStr); } catch (NumberFormatException ignored) {}

        NBTTagCompound itemNBT = ByteBufUtils.readNBT(in);
        if (itemNBT == null) return "No item data received.";

        ItemStack item = ItemStack.loadItemStackFromNBT(itemNBT);
        if (item == null) return "Invalid item data.";

        // Remove item from inventory first
        if (!removeItemFromInventory(player, item)) {
            return "Item not found in inventory.";
        }

        String result = controller.createListing(player, item, startingPrice, buyoutPrice);
        if (result != null) {
            // Failed - return item (drop if inventory full to prevent item loss)
            if (!player.inventory.addItemStackToInventory(item)) {
                player.entityDropItem(item, 0.5f);
            }
        }
        return result;
    }

    private void handleOpenPage(ByteBuf in, EntityPlayerMP player) {
        int page = in.readInt();
        EnumGuiType guiType;
        switch (page) {
            case 0: guiType = EnumGuiType.PlayerAuction; break;
            case 1: guiType = EnumGuiType.PlayerAuctionSell; break;
            case 2: guiType = EnumGuiType.PlayerAuctionTrades; break;
            default: guiType = EnumGuiType.PlayerAuction; break;
        }
        NoppesUtilServer.sendOpenGui(player, guiType, npc);

        // Send trades data when opening the trades page
        if (page == 2) {
            AuctionController controller = AuctionController.getInstance();
            if (controller != null) {
                sendTradesData(player, controller);
            }
        }
    }

    /** Send full trades data to player (listings, bids, claims) */
    private void sendTradesData(EntityPlayerMP player, AuctionController controller) {
        NBTTagCompound response = new NBTTagCompound();
        response.setBoolean("TradesData", true);

        // Active listings (items player is selling)
        List<AuctionListing> listings = controller.getPlayerActiveListings(player.getUniqueID());
        NBTTagList listingsNBT = new NBTTagList();
        for (AuctionListing listing : listings) {
            listingsNBT.appendTag(listing.writeToNBT(new NBTTagCompound()));
        }
        response.setTag("ActiveListings", listingsNBT);

        // Active bids (items player is bidding on)
        List<AuctionListing> bids = controller.getPlayerActiveBids(player.getUniqueID());
        NBTTagList bidsNBT = new NBTTagList();
        for (AuctionListing bid : bids) {
            bidsNBT.appendTag(bid.writeToNBT(new NBTTagCompound()));
        }
        response.setTag("ActiveBids", bidsNBT);

        // Claims (items/currency to claim)
        List<AuctionClaim> claims = controller.getPlayerClaims(player.getUniqueID());
        NBTTagList claimsNBT = new NBTTagList();
        for (AuctionClaim claim : claims) {
            claimsNBT.appendTag(claim.writeToNBT(new NBTTagCompound()));
        }
        response.setTag("Claims", claimsNBT);

        GuiDataPacket.sendGuiData(player, response);
    }

    private void handleOpenBidding(ByteBuf in, EntityPlayerMP player, AuctionController controller) throws IOException {
        String listingId = ByteBufUtils.readString(in);
        AuctionListing listing = controller.getListing(listingId);

        if (listing == null || !listing.isActive()) {
            sendError(player, "Listing not found or has ended.");
            return;
        }

        // Open bidding GUI
        NoppesUtilServer.sendOpenGui(player, EnumGuiType.PlayerAuctionBidding, npc);

        // Check if this is the player's own listing
        boolean isOwnListing = listing.isSeller(player.getUniqueID());

        // Send listing data
        NBTTagCompound response = new NBTTagCompound();
        response.setBoolean("BiddingData", true);
        response.setTag("Listing", listing.writeToNBT(new NBTTagCompound()));
        response.setLong("Balance", controller.getPlayerBalance(player));
        response.setBoolean("IsOwnListing", isOwnListing);
        GuiDataPacket.sendGuiData(player, response);
    }

    private void handleRequestListings(ByteBuf in, EntityPlayerMP player, AuctionController controller) throws IOException {
        int page = in.readInt();
        NBTTagCompound filterNBT = ByteBufUtils.readNBT(in);

        AuctionFilter filter = new AuctionFilter();
        if (filterNBT != null) {
            filter.readFromNBT(filterNBT);
        }

        // Get listings for this page
        List<AuctionListing> listings = controller.getActiveListings(filter, page, PAGE_SIZE);
        int totalListings = controller.getTotalActiveListings(filter);
        int totalPages = Math.max(1, (int) Math.ceil((double) totalListings / PAGE_SIZE));

        // Build response
        NBTTagCompound response = new NBTTagCompound();
        response.setBoolean("ListingsData", true);
        response.setInteger("Page", page);
        response.setInteger("TotalPages", totalPages);
        response.setInteger("TotalListings", totalListings);

        NBTTagList listingsList = new NBTTagList();
        for (AuctionListing listing : listings) {
            listingsList.appendTag(listing.writeToNBT(new NBTTagCompound()));
        }
        response.setTag("Listings", listingsList);

        GuiDataPacket.sendGuiData(player, response);
    }

    // =========================================
    // Helpers
    // =========================================

    /**
     * Checks if two ItemStacks are the same type (item, damage, NBT) ignoring stack size.
     */
    private boolean areItemsSameType(ItemStack item1, ItemStack item2) {
        if (item1 == null || item2 == null)
            return item1 == item2;
        if (item1.getItem() != item2.getItem())
            return false;
        if (item1.getItemDamage() != item2.getItemDamage())
            return false;
        return ItemStack.areItemStackTagsEqual(item1, item2);
    }

    /**
     * Counts total matching items across all inventory stacks.
     */
    private int countItemsInInventory(EntityPlayerMP player, ItemStack target) {
        int count = 0;
        for (int i = 0; i < player.inventory.mainInventory.length; i++) {
            ItemStack stack = player.inventory.mainInventory[i];
            if (stack != null && areItemsSameType(stack, target)) {
                count += stack.stackSize;
            }
        }
        return count;
    }

    /**
     * Removes items from inventory, consuming from multiple stacks if needed.
     * Returns true if successful, false if not enough items.
     */
    private boolean removeItemFromInventory(EntityPlayerMP player, ItemStack target) {
        if (target == null) return false;

        int needed = target.stackSize;

        // First verify player has enough total items
        int available = countItemsInInventory(player, target);
        if (available < needed) {
            return false;
        }

        // Consume items from matching stacks
        for (int i = 0; i < player.inventory.mainInventory.length && needed > 0; i++) {
            ItemStack stack = player.inventory.mainInventory[i];
            if (stack == null || !areItemsSameType(stack, target))
                continue;

            if (needed >= stack.stackSize) {
                // Need entire stack
                needed -= stack.stackSize;
                player.inventory.mainInventory[i] = null;
            } else {
                // Need only part of stack
                stack.splitStack(needed);
                needed = 0;
            }
        }

        player.inventory.markDirty();
        return true;
    }

    private void sendError(EntityPlayerMP player, String message) {
        player.addChatMessage(new net.minecraft.util.ChatComponentText(
            net.minecraft.util.EnumChatFormatting.RED + "[Auction House] " + message));
    }

    private void sendTradesUpdate(EntityPlayerMP player) {
        // Send full trades data to refresh the client
        AuctionController controller = AuctionController.getInstance();
        if (controller != null) {
            sendTradesData(player, controller);
        }
    }

    private void sendListingsUpdate(EntityPlayerMP player) {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setBoolean("ListingsRefresh", true);
        GuiDataPacket.sendGuiData(player, compound);
    }

    private enum Action {
        Bid,
        Buyout,
        Cancel,
        ClaimItem,
        ClaimCurrency,
        CreateListing,
        OpenPage,
        OpenBidding,
        RequestListings
    }
}
