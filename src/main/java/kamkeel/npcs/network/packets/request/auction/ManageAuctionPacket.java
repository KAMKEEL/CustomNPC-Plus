package kamkeel.npcs.network.packets.request.auction;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.network.packets.data.large.GuiDataPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.config.ConfigMarket;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumClaimType;
import noppes.npcs.controllers.AuctionController;
import noppes.npcs.controllers.data.AuctionClaim;
import noppes.npcs.controllers.data.AuctionFilter;
import noppes.npcs.controllers.data.AuctionListing;
import noppes.npcs.wrapper.nbt.MC1710NBTCompound;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Admin-only global auction management packet.
 */
public class ManageAuctionPacket extends AbstractPacket {
    private static final int PAGE_SIZE = 45;

    private Action action;
    private int page;
    private boolean flag;
    private String text;
    private long amount;
    private long amount2;
    private int amountInt;
    private NBTTagCompound data;

    public ManageAuctionPacket() {
    }

    private ManageAuctionPacket(Action action) {
        this.action = action;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.ManageAuctionAction;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.GLOBAL_AUCTION;
    }

    @Override
    public boolean needsNPC() {
        return false;
    }

    // =========================================
    // Client Send Helpers
    // =========================================

    @SideOnly(Side.CLIENT)
    public static void requestListings(AuctionFilter filter, int page) {
        ManageAuctionPacket packet = new ManageAuctionPacket(Action.REQUEST_LISTINGS);
        packet.page = page;
        packet.data = filter != null ? ((MC1710NBTCompound) filter.writeToNBT(new MC1710NBTCompound(new NBTTagCompound()))).getMCTag() : new NBTTagCompound();
        PacketClient.sendClient(packet);
    }

    @SideOnly(Side.CLIENT)
    public static void requestGlobalClaims(int page) {
        ManageAuctionPacket packet = new ManageAuctionPacket(Action.REQUEST_CLAIMS);
        packet.page = page;
        PacketClient.sendClient(packet);
    }

    @SideOnly(Side.CLIENT)
    public static void createFakeListing(ItemStack item, String sellerName, long startingPrice, long buyoutPrice, int durationHours) {
        if (item == null) return;

        ManageAuctionPacket packet = new ManageAuctionPacket(Action.CREATE_FAKE);
        packet.text = sellerName != null ? sellerName : "";
        packet.amount = startingPrice;
        packet.amount2 = buyoutPrice;
        packet.amountInt = durationHours;
        packet.data = new NBTTagCompound();
        item.writeToNBT(packet.data);
        PacketClient.sendClient(packet);
    }

    @SideOnly(Side.CLIENT)
    public static void manageListing(String listingId, boolean cancelCompletely) {
        ManageAuctionPacket packet = new ManageAuctionPacket(Action.MANAGE_LISTING);
        packet.text = listingId;
        packet.flag = cancelCompletely;
        PacketClient.sendClient(packet);
    }

    @SideOnly(Side.CLIENT)
    public static void claimGlobalItem(String claimId) {
        ManageAuctionPacket packet = new ManageAuctionPacket(Action.CLAIM_ITEM);
        packet.text = claimId;
        PacketClient.sendClient(packet);
    }

    @SideOnly(Side.CLIENT)
    public static void claimGlobalCurrency(String claimId) {
        ManageAuctionPacket packet = new ManageAuctionPacket(Action.CLAIM_CURRENCY);
        packet.text = claimId;
        PacketClient.sendClient(packet);
    }

    @SideOnly(Side.CLIENT)
    public static void openBidding(String listingId) {
        ManageAuctionPacket packet = new ManageAuctionPacket(Action.OPEN_BIDDING);
        packet.text = listingId;
        PacketClient.sendClient(packet);
    }

    // =========================================
    // Serialization
    // =========================================

    @Override
    @SideOnly(Side.CLIENT)
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(action.ordinal());

        switch (action) {
            case REQUEST_LISTINGS:
                out.writeInt(page);
                ByteBufUtils.writeNBT(out, data);
                break;
            case REQUEST_CLAIMS:
                out.writeInt(page);
                break;
            case CREATE_FAKE:
                ByteBufUtils.writeString(out, text);
                out.writeLong(amount);
                out.writeLong(amount2);
                out.writeInt(amountInt);
                ByteBufUtils.writeNBT(out, data);
                break;
            case MANAGE_LISTING:
                ByteBufUtils.writeString(out, text);
                out.writeBoolean(flag);
                break;
            case CLAIM_ITEM:
            case CLAIM_CURRENCY:
            case OPEN_BIDDING:
                ByteBufUtils.writeString(out, text);
                break;
        }
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP)) return;
        EntityPlayerMP playerMP = (EntityPlayerMP) player;

        if (!CustomNpcsPermissions.hasCustomPermission(playerMP, "customnpcs.global.auction")) {
            sendError(playerMP, "Missing permission: customnpcs.global.auction");
            return;
        }

        int actionOrdinal = in.readInt();
        if (actionOrdinal < 0 || actionOrdinal >= Action.values().length) return;
        Action requestedAction = Action.values()[actionOrdinal];

        AuctionController controller = AuctionController.getInstance();
        if (controller == null) {
            sendError(playerMP, "Auction system is not available.");
            return;
        }

        String result = null;
        switch (requestedAction) {
            case REQUEST_LISTINGS:
                handleRequestListings(in, playerMP, controller);
                break;
            case REQUEST_CLAIMS:
                handleRequestClaims(in, playerMP, controller);
                break;
            case CREATE_FAKE:
                result = handleCreateFake(in, playerMP, controller);
                break;
            case MANAGE_LISTING:
                result = handleManageListing(in, playerMP, controller);
                break;
            case CLAIM_ITEM:
                result = handleClaimItem(in, playerMP, controller);
                break;
            case CLAIM_CURRENCY:
                result = handleClaimCurrency(in, playerMP, controller);
                break;
            case OPEN_BIDDING:
                handleOpenBidding(in, playerMP, controller);
                break;
        }

        if (result != null) {
            sendError(playerMP, result);
        }
    }

    // =========================================
    // Handlers
    // =========================================

    private void handleRequestListings(ByteBuf in, EntityPlayerMP player, AuctionController controller) throws IOException {
        int requestPage = Math.max(0, in.readInt());
        NBTTagCompound filterNbt = ByteBufUtils.readNBT(in);

        AuctionFilter filter = new AuctionFilter();
        if (filterNbt != null) {
            filter.readFromNBT(new MC1710NBTCompound(filterNbt));
        }

        int totalListings = controller.getTotalActiveListings(filter);
        int totalPages = Math.max(1, (int) Math.ceil((double) totalListings / PAGE_SIZE));
        int safePage = Math.min(requestPage, totalPages - 1);
        List<AuctionListing> listings = controller.getActiveListings(filter, safePage, PAGE_SIZE);

        NBTTagCompound response = new NBTTagCompound();
        response.setBoolean("ManageAuctionListingsData", true);
        response.setInteger("Page", safePage);
        response.setInteger("TotalPages", totalPages);
        response.setInteger("TotalListings", totalListings);

        NBTTagList list = new NBTTagList();
        for (AuctionListing listing : listings) {
            list.appendTag(listing.writeToNBT(new NBTTagCompound()));
        }
        response.setTag("Listings", list);
        GuiDataPacket.sendGuiData(player, response);
    }

    private void handleRequestClaims(ByteBuf in, EntityPlayerMP player, AuctionController controller) {
        int requestPage = Math.max(0, in.readInt());

        // Global "trades" page contains global claims + active global listings,
        // ordered like My Trades: sold claims, outbid claims, won claims, active listings, expired claims.
        List<AuctionClaim> allClaims = controller.getAllGlobalClaims();
        List<AuctionListing> allListings = controller.getActiveGlobalListings();
        int totalClaims = allClaims.size();
        int totalListings = allListings.size();

        List<AuctionClaim> soldClaims = new ArrayList<AuctionClaim>();
        List<AuctionClaim> outbidClaims = new ArrayList<AuctionClaim>();
        List<AuctionClaim> wonClaims = new ArrayList<AuctionClaim>();
        List<AuctionClaim> expiredClaims = new ArrayList<AuctionClaim>();
        for (AuctionClaim claim : allClaims) {
            if (claim == null) continue;
            if (claim.type == EnumClaimType.CURRENCY) {
                soldClaims.add(claim);
            } else if (claim.type == EnumClaimType.REFUND) {
                outbidClaims.add(claim);
            } else if (claim.type == EnumClaimType.ITEM) {
                if (claim.isReturned) {
                    expiredClaims.add(claim);
                } else {
                    wonClaims.add(claim);
                }
            } else {
                wonClaims.add(claim);
            }
        }

        List<Entry> ordered = new ArrayList<Entry>();
        appendClaims(ordered, soldClaims);
        appendClaims(ordered, outbidClaims);
        appendClaims(ordered, wonClaims);
        for (AuctionListing listing : allListings) {
            ordered.add(Entry.forListing(listing));
        }
        appendClaims(ordered, expiredClaims);

        int totalEntries = ordered.size();

        int totalPages = Math.max(1, (int) Math.ceil((double) totalEntries / PAGE_SIZE));
        int safePage = Math.min(requestPage, totalPages - 1);

        int start = safePage * PAGE_SIZE;
        int end = Math.min(totalEntries, start + PAGE_SIZE);
        List<AuctionClaim> pageClaims = new ArrayList<AuctionClaim>();
        List<AuctionListing> pageListings = new ArrayList<AuctionListing>();

        for (int i = start; i < end; i++) {
            Entry entry = ordered.get(i);
            if (entry.claim != null) {
                pageClaims.add(entry.claim);
            } else if (entry.listing != null) {
                pageListings.add(entry.listing);
            }
        }

        NBTTagCompound response = new NBTTagCompound();
        response.setBoolean("ManageAuctionClaimsData", true);
        response.setInteger("Page", safePage);
        response.setInteger("TotalPages", totalPages);
        response.setInteger("TotalClaims", totalClaims);
        response.setInteger("TotalListings", totalListings);
        response.setInteger("TotalEntries", totalEntries);

        NBTTagList claimList = new NBTTagList();
        for (AuctionClaim claim : pageClaims) {
            claimList.appendTag(claim.writeToNBT(new NBTTagCompound()));
        }
        response.setTag("Claims", claimList);

        NBTTagList listingList = new NBTTagList();
        for (AuctionListing listing : pageListings) {
            listingList.appendTag(listing.writeToNBT(new NBTTagCompound()));
        }
        response.setTag("Listings", listingList);
        GuiDataPacket.sendGuiData(player, response);
    }

    private static void appendClaims(List<Entry> out, List<AuctionClaim> claims) {
        if (claims == null || claims.isEmpty()) return;
        for (AuctionClaim claim : claims) {
            out.add(Entry.forClaim(claim));
        }
    }

    private String handleCreateFake(ByteBuf in, EntityPlayerMP player, AuctionController controller) throws IOException {
        if (!ConfigMarket.AuctionEnabled) {
            return "Auction is disabled.";
        }

        String sellerName = ByteBufUtils.readString(in);
        long startingPrice = in.readLong();
        long buyoutPrice = in.readLong();
        int durationHours = in.readInt();
        NBTTagCompound itemNbt = ByteBufUtils.readNBT(in);

        if (itemNbt == null) {
            return "No item data received.";
        }
        ItemStack item = ItemStack.loadItemStackFromNBT(itemNbt);
        if (item == null) {
            return "Invalid item data.";
        }

        // Consume staged item from admin inventory like normal auction sell flow.
        if (!removeItemFromInventory(player, item)) {
            return "Item not found in inventory.";
        }

        String result = controller.createGlobalListing(player, sellerName, item, startingPrice, buyoutPrice, durationHours);
        if (result != null) {
            // Failed create, return item (or drop to avoid loss).
            // Use same reference so partial add mutates stackSize, then drop only the remainder.
            if (!player.inventory.addItemStackToInventory(item)) {
                player.entityDropItem(item, 0.5f);
            }
        }
        if (result == null) {
            sendRefresh(player, true);
        }
        return result;
    }

    private String handleManageListing(ByteBuf in, EntityPlayerMP player, AuctionController controller) throws IOException {
        String listingId = ByteBufUtils.readString(in);
        boolean cancelCompletely = in.readBoolean();
        String result = controller.adminStopListingToGlobal(listingId, player, cancelCompletely);
        if (result == null) {
            sendRefresh(player, false);
        }
        return result;
    }

    private String handleClaimItem(ByteBuf in, EntityPlayerMP player, AuctionController controller) throws IOException {
        String claimId = ByteBufUtils.readString(in);
        String result = controller.claimGlobalItem(claimId, player);
        if (result == null) {
            sendRefresh(player, false);
        }
        return result;
    }

    private String handleClaimCurrency(ByteBuf in, EntityPlayerMP player, AuctionController controller) throws IOException {
        String claimId = ByteBufUtils.readString(in);
        String result = controller.claimGlobalCurrency(claimId, player);
        if (result == null) {
            sendRefresh(player, false);
        }
        return result;
    }

    private void handleOpenBidding(ByteBuf in, EntityPlayerMP player, AuctionController controller) throws IOException {
        String listingId = ByteBufUtils.readString(in);
        AuctionListing listing = controller.getListing(listingId);
        if (listing == null || !listing.isActive()) {
            sendError(player, "Listing not found or has ended.");
            return;
        }

        NoppesUtilServer.sendOpenGui(player, EnumGuiType.PlayerAuctionBidding, npc);

        NBTTagCompound response = new NBTTagCompound();
        response.setBoolean("BiddingData", true);
        response.setTag("Listing", listing.writeToNBT(new NBTTagCompound()));
        response.setLong("Balance", controller.getPlayerBalance(player));
        response.setBoolean("IsOwnListing", listing.isSeller(player.getUniqueID()));
        GuiDataPacket.sendGuiData(player, response);
    }

    private void sendError(EntityPlayerMP player, String message) {
        player.addChatMessage(new net.minecraft.util.ChatComponentText(
            net.minecraft.util.EnumChatFormatting.RED + "[Global Auction] " + message));
    }

    private void sendRefresh(EntityPlayerMP player, boolean created) {
        NBTTagCompound response = new NBTTagCompound();
        response.setBoolean("ManageAuctionRefresh", true);
        response.setBoolean("ManageAuctionCreateSuccess", created);
        GuiDataPacket.sendGuiData(player, response);
    }

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
        int available = countItemsInInventory(player, target);
        if (available < needed) {
            return false;
        }

        for (int i = 0; i < player.inventory.mainInventory.length && needed > 0; i++) {
            ItemStack stack = player.inventory.mainInventory[i];
            if (stack == null || !areItemsSameType(stack, target))
                continue;

            if (needed >= stack.stackSize) {
                needed -= stack.stackSize;
                player.inventory.mainInventory[i] = null;
            } else {
                stack.splitStack(needed);
                if (stack.stackSize <= 0) {
                    player.inventory.setInventorySlotContents(i, null);
                }
                needed = 0;
            }
        }

        player.inventory.markDirty();
        return true;
    }

    private static class Entry {
        final AuctionClaim claim;
        final AuctionListing listing;

        private Entry(AuctionClaim claim, AuctionListing listing) {
            this.claim = claim;
            this.listing = listing;
        }

        static Entry forClaim(AuctionClaim claim) {
            return new Entry(claim, null);
        }

        static Entry forListing(AuctionListing listing) {
            return new Entry(null, listing);
        }
    }

    private enum Action {
        REQUEST_LISTINGS,
        REQUEST_CLAIMS,
        CREATE_FAKE,
        MANAGE_LISTING,
        CLAIM_ITEM,
        CLAIM_CURRENCY,
        OPEN_BIDDING
    }
}
