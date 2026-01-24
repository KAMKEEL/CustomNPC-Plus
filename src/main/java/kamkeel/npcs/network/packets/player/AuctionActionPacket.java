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
    public boolean needsNPC() { return true; }

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

        if (npc.advanced.role != EnumRoleType.Auctioneer) return;
        if (!ConfigMarket.AuctionEnabled) return;

        Action requestedAction = Action.values()[in.readInt()];
        AuctionController controller = AuctionController.getInstance();
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
            // Failed - return item
            player.inventory.addItemStackToInventory(item);
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

    private boolean removeItemFromInventory(EntityPlayerMP player, ItemStack target) {
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (stack != null && ItemStack.areItemStacksEqual(stack, target)) {
                player.inventory.setInventorySlotContents(i, null);
                return true;
            }
        }
        return false;
    }

    private void sendError(EntityPlayerMP player, String message) {
        player.addChatMessage(new net.minecraft.util.ChatComponentText(
            net.minecraft.util.EnumChatFormatting.RED + "[Auction House] " + message));
    }

    private void sendTradesUpdate(EntityPlayerMP player) {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setBoolean("TradesUpdate", true);
        GuiDataPacket.sendGuiData(player, compound);
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
