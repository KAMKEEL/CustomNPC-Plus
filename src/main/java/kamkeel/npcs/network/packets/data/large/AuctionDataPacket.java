package kamkeel.npcs.network.packets.data.large;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import kamkeel.npcs.network.LargeAbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumDataPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.controllers.AuctionController;
import noppes.npcs.controllers.data.AuctionFilter;
import noppes.npcs.controllers.data.AuctionListing;

import java.io.IOException;
import java.util.List;

/**
 * Large packet for sending auction listing data from server to client.
 * Used for initial load and search/filter queries.
 */
public final class AuctionDataPacket extends LargeAbstractPacket {
    public static final String packetName = "Large|AuctionData";

    private DataType dataType;
    private List<AuctionListing> listings;
    private int totalResults;
    private int currentPage;
    private int totalPages;
    private long playerBalance;

    public AuctionDataPacket() {
    }

    private AuctionDataPacket(DataType type, List<AuctionListing> listings,
                              int totalResults, int currentPage, int totalPages,
                              long playerBalance) {
        this.dataType = type;
        this.listings = listings;
        this.totalResults = totalResults;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.playerBalance = playerBalance;
    }

    /**
     * Send filtered listing results to client
     */
    public static void sendFilteredListings(EntityPlayerMP player, AuctionFilter filter) {
        if (AuctionController.Instance == null) {
            return;
        }

        AuctionController.FilterResult result = AuctionController.Instance.getFilteredListings(filter);
        long balance = 0;
        if (noppes.npcs.controllers.CurrencyController.Instance != null) {
            balance = noppes.npcs.controllers.CurrencyController.Instance.getBalance(player);
        }

        AuctionDataPacket packet = new AuctionDataPacket(
            DataType.FILTERED_LISTINGS,
            result.listings,
            result.totalResults,
            result.currentPage,
            result.totalPages,
            balance
        );
        PacketHandler.Instance.sendToPlayer(packet, player);
    }

    /**
     * Send player's own listings
     */
    public static void sendMyListings(EntityPlayerMP player) {
        if (AuctionController.Instance == null) {
            return;
        }

        List<AuctionListing> myListings = AuctionController.Instance
            .getListingsBySeller(player.getUniqueID());

        long balance = 0;
        if (noppes.npcs.controllers.CurrencyController.Instance != null) {
            balance = noppes.npcs.controllers.CurrencyController.Instance.getBalance(player);
        }

        AuctionDataPacket packet = new AuctionDataPacket(
            DataType.MY_LISTINGS,
            myListings,
            myListings.size(),
            0,
            1,
            balance
        );
        PacketHandler.Instance.sendToPlayer(packet, player);
    }

    /**
     * Send player's active bids
     */
    public static void sendMyBids(EntityPlayerMP player) {
        if (AuctionController.Instance == null) {
            return;
        }

        List<AuctionListing> myBids = AuctionController.Instance
            .getListingsAsBidder(player.getUniqueID());

        long balance = 0;
        if (noppes.npcs.controllers.CurrencyController.Instance != null) {
            balance = noppes.npcs.controllers.CurrencyController.Instance.getBalance(player);
        }

        AuctionDataPacket packet = new AuctionDataPacket(
            DataType.MY_BIDS,
            myBids,
            myBids.size(),
            0,
            1,
            balance
        );
        PacketHandler.Instance.sendToPlayer(packet, player);
    }

    /**
     * Send player's claimable items
     */
    public static void sendClaimable(EntityPlayerMP player) {
        if (AuctionController.Instance == null) {
            return;
        }

        List<AuctionListing> claimable = AuctionController.Instance
            .getClaimableListings(player.getUniqueID());

        long balance = 0;
        if (noppes.npcs.controllers.CurrencyController.Instance != null) {
            balance = noppes.npcs.controllers.CurrencyController.Instance.getBalance(player);
        }

        AuctionDataPacket packet = new AuctionDataPacket(
            DataType.CLAIMABLE,
            claimable,
            claimable.size(),
            0,
            1,
            balance
        );
        PacketHandler.Instance.sendToPlayer(packet, player);
    }

    @Override
    public Enum getType() {
        return EnumDataPacket.AUCTION_DATA;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.DATA_PACKET;
    }

    @Override
    protected byte[] getData() throws IOException {
        ByteBuf buffer = Unpooled.buffer();

        buffer.writeInt(dataType.ordinal());
        buffer.writeInt(totalResults);
        buffer.writeInt(currentPage);
        buffer.writeInt(totalPages);
        buffer.writeLong(playerBalance);

        // Write listings as NBT
        NBTTagList listingList = new NBTTagList();
        if (listings != null) {
            for (AuctionListing listing : listings) {
                listingList.appendTag(listing.writeToNBT(new NBTTagCompound()));
            }
        }

        NBTTagCompound compound = new NBTTagCompound();
        compound.setTag("Listings", listingList);
        ByteBufUtils.writeNBT(buffer, compound);

        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        return bytes;
    }

    @Override
    protected void handleCompleteData(ByteBuf data, EntityPlayer player) throws IOException {
        // Client-side handling will be done in a client util class
        // Similar to NoppesUtil.setScrollData()
        int typeOrdinal = data.readInt();
        int totalResults = data.readInt();
        int currentPage = data.readInt();
        int totalPages = data.readInt();
        long playerBalance = data.readLong();

        NBTTagCompound compound = ByteBufUtils.readNBT(data);

        DataType type = DataType.values()[typeOrdinal];

        // Store data for client GUI to access
        // This will be implemented in the client-side GUI handler
        noppes.npcs.client.NoppesUtil.setAuctionData(type.ordinal(), compound,
            totalResults, currentPage, totalPages, playerBalance);
    }

    public enum DataType {
        FILTERED_LISTINGS,
        MY_LISTINGS,
        MY_BIDS,
        CLAIMABLE
    }
}
