package noppes.npcs.client.gui.player;

import kamkeel.npcs.network.packets.player.AuctionActionPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.config.ConfigMarket;
import noppes.npcs.constants.EnumAuctionCategory;
import noppes.npcs.constants.EnumAuctionSort;
import noppes.npcs.constants.EnumAuctionStatus;
import noppes.npcs.containers.ContainerAuction;
import noppes.npcs.controllers.data.AuctionFilter;
import noppes.npcs.controllers.data.AuctionListing;
import noppes.npcs.entity.EntityNPCInterface;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.ArrayList;
import java.util.List;

/**
 * Main auction house browse GUI.
 * Displays listings with search, filter, sort, and pagination.
 */
public class GuiAuction extends GuiContainerNPCInterface implements IGuiData,
        ICustomScrollListener, NoppesUtil.IAuctionData {

    private static final ResourceLocation background = new ResourceLocation("customnpcs", "textures/gui/auction.png");

    // GUI Components
    private GuiNpcTextField searchField;
    private GuiCustomScroll listingScroll;

    // Current state
    private AuctionFilter currentFilter = new AuctionFilter();
    private List<AuctionListing> listings = new ArrayList<>();
    private int totalResults = 0;
    private int currentPage = 0;
    private int totalPages = 0;
    private long playerBalance = 0;

    // Tab state (0=Browse, 1=My Listings, 2=My Bids, 3=Claims, 4=Create)
    private int currentTab = 0;

    // Selected listing for details
    private int selectedListingIndex = -1;

    // Countdown update timer
    private long lastCountdownUpdate = 0;

    public GuiAuction(EntityNPCInterface npc, ContainerAuction container) {
        super(npc, container);
        this.title = "Auction House";
        this.xSize = 256;
        this.ySize = 222;
        this.closeOnEsc = true;
        this.drawDefaultBackground = true;

        // Request initial data
        AuctionActionPacket.QueryListings(currentFilter);
    }

    @Override
    public void initGui() {
        super.initGui();

        // Tab buttons at top
        addTopButton(new GuiMenuTopButton(0, guiLeft + 4, guiTop - 17, "Browse"));
        addTopButton(new GuiMenuTopButton(1, guiLeft + 54, guiTop - 17, "My Listings"));
        addTopButton(new GuiMenuTopButton(2, guiLeft + 114, guiTop - 17, "My Bids"));
        addTopButton(new GuiMenuTopButton(3, guiLeft + 164, guiTop - 17, "Claims"));
        addTopButton(new GuiMenuTopButton(4, guiLeft + 214, guiTop - 17, "Sell"));
        getTopButton(currentTab).setEnabled(false);

        // Search field
        searchField = new GuiNpcTextField(10, this, guiLeft + 8, guiTop + 6, 120, 14, currentFilter.searchText);
        searchField.setMaxStringLength(50);
        addTextField(searchField);

        // Search button
        addButton(new GuiNpcButton(11, guiLeft + 130, guiTop + 5, 30, 16, "Search"));

        // Category buttons (simplified row)
        String[] categories = {"All", "Weapons", "Armor", "Tools", "Potions", "Blocks", "Other"};
        for (int i = 0; i < categories.length; i++) {
            GuiNpcButton btn = new GuiNpcButton(20 + i, guiLeft + 8 + i * 35, guiTop + 24, 33, 14, categories[i]);
            btn.setEnabled(currentFilter.category.ordinal() != getCategoryOrdinal(i));
            addButton(btn);
        }

        // Sort dropdown button
        addButton(new GuiNpcButton(30, guiLeft + 170, guiTop + 5, 78, 16, currentFilter.sortOrder.getDisplayName()));

        // Listing scroll area
        listingScroll = new GuiCustomScroll(this, 0);
        listingScroll.guiLeft = guiLeft + 8;
        listingScroll.guiTop = guiTop + 42;
        listingScroll.setSize(160, 130);
        addScroll(listingScroll);
        updateListingDisplay();

        // Pagination buttons
        addButton(new GuiNpcButton(40, guiLeft + 8, guiTop + 176, 40, 16, "< Prev"));
        addLabel(new GuiNpcLabel(41, getPageLabel(), guiLeft + 90, guiTop + 180, CustomNpcResourceListener.DefaultTextColor));
        addButton(new GuiNpcButton(42, guiLeft + 128, guiTop + 176, 40, 16, "Next >"));

        // Action buttons (right side)
        addButton(new GuiNpcButton(50, guiLeft + 175, guiTop + 50, 73, 20, "View Details"));
        addButton(new GuiNpcButton(51, guiLeft + 175, guiTop + 75, 73, 20, "Place Bid"));
        addButton(new GuiNpcButton(52, guiLeft + 175, guiTop + 100, 73, 20, "Buyout"));
        addButton(new GuiNpcButton(53, guiLeft + 175, guiTop + 125, 73, 20, "Refresh"));

        // Balance display
        addLabel(new GuiNpcLabel(60, "Balance: " + formatCurrency(playerBalance),
            guiLeft + 175, guiTop + 155, 0x00AA00));

        updateButtonStates();
    }

    private int getCategoryOrdinal(int buttonIndex) {
        switch (buttonIndex) {
            case 0: return EnumAuctionCategory.ALL.ordinal();
            case 1: return EnumAuctionCategory.WEAPONS.ordinal();
            case 2: return EnumAuctionCategory.ARMOR.ordinal();
            case 3: return EnumAuctionCategory.TOOLS.ordinal();
            case 4: return EnumAuctionCategory.POTIONS.ordinal();
            case 5: return EnumAuctionCategory.BLOCKS.ordinal();
            default: return EnumAuctionCategory.MISC.ordinal();
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        // Update countdowns every second
        long now = System.currentTimeMillis();
        if (now - lastCountdownUpdate >= 1000) {
            lastCountdownUpdate = now;
            updateListingDisplay();
        }
    }

    @Override
    public void actionPerformed(GuiButton button) {
        super.actionPerformed(button);
        int id = button.id;

        // Tab buttons
        if (id >= 0 && id <= 4) {
            switchTab(id);
        }
        // Search button
        else if (id == 11) {
            performSearch();
        }
        // Category buttons
        else if (id >= 20 && id <= 26) {
            setCategory(getCategoryOrdinal(id - 20));
        }
        // Sort button
        else if (id == 30) {
            cycleSortOrder();
        }
        // Pagination
        else if (id == 40) {
            previousPage();
        } else if (id == 42) {
            nextPage();
        }
        // Actions
        else if (id == 50) {
            viewDetails();
        } else if (id == 51) {
            placeBid();
        } else if (id == 52) {
            buyout();
        } else if (id == 53) {
            refresh();
        }
    }

    private void switchTab(int tab) {
        currentTab = tab;
        currentPage = 0;

        switch (tab) {
            case 0: // Browse
                AuctionActionPacket.QueryListings(currentFilter);
                break;
            case 1: // My Listings
                AuctionActionPacket.QueryMyListings();
                break;
            case 2: // My Bids
                AuctionActionPacket.QueryMyBids();
                break;
            case 3: // Claims
                AuctionActionPacket.QueryClaimable();
                break;
            case 4: // Create - open sub GUI
                setSubGui(new SubGuiAuctionCreate(this, (ContainerAuction) inventorySlots));
                break;
        }

        initGui();
    }

    private void performSearch() {
        currentFilter.searchText = searchField.getText();
        currentFilter.page = 0;
        currentPage = 0;
        AuctionActionPacket.QueryListings(currentFilter);
    }

    private void setCategory(int categoryOrdinal) {
        currentFilter.category = EnumAuctionCategory.values()[categoryOrdinal];
        currentFilter.page = 0;
        currentPage = 0;
        AuctionActionPacket.QueryListings(currentFilter);
        initGui();
    }

    private void cycleSortOrder() {
        currentFilter.sortOrder = currentFilter.sortOrder.next();
        currentFilter.page = 0;
        currentPage = 0;
        AuctionActionPacket.QueryListings(currentFilter);
        initGui();
    }

    private void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            currentFilter.page = currentPage;
            AuctionActionPacket.QueryListings(currentFilter);
        }
    }

    private void nextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            currentFilter.page = currentPage;
            AuctionActionPacket.QueryListings(currentFilter);
        }
    }

    private void viewDetails() {
        if (selectedListingIndex >= 0 && selectedListingIndex < listings.size()) {
            AuctionListing listing = listings.get(selectedListingIndex);
            setSubGui(new SubGuiAuctionDetails(this, listing));
        }
    }

    private void placeBid() {
        if (selectedListingIndex >= 0 && selectedListingIndex < listings.size()) {
            AuctionListing listing = listings.get(selectedListingIndex);
            setSubGui(new SubGuiAuctionBid(this, listing, playerBalance));
        }
    }

    private void buyout() {
        if (selectedListingIndex >= 0 && selectedListingIndex < listings.size()) {
            AuctionListing listing = listings.get(selectedListingIndex);
            if (listing.buyoutPrice > 0 && playerBalance >= listing.buyoutPrice) {
                AuctionActionPacket.Buyout(listing.id);
                refresh();
            }
        }
    }

    private void refresh() {
        switch (currentTab) {
            case 0:
                AuctionActionPacket.QueryListings(currentFilter);
                break;
            case 1:
                AuctionActionPacket.QueryMyListings();
                break;
            case 2:
                AuctionActionPacket.QueryMyBids();
                break;
            case 3:
                AuctionActionPacket.QueryClaimable();
                break;
        }
    }

    private void updateListingDisplay() {
        if (listingScroll == null) return;

        List<String> displayList = new ArrayList<>();
        for (AuctionListing listing : listings) {
            String line = formatListingLine(listing);
            displayList.add(line);
        }
        listingScroll.setList(displayList);

        if (selectedListingIndex >= 0 && selectedListingIndex < displayList.size()) {
            listingScroll.selected = selectedListingIndex;
        }
    }

    private String formatListingLine(AuctionListing listing) {
        String itemName = listing.item.getDisplayName();
        if (itemName.length() > 15) {
            itemName = itemName.substring(0, 12) + "...";
        }

        long price = listing.currentBid > 0 ? listing.currentBid : listing.startingPrice;
        String priceStr = formatCurrency(price);

        String timeStr = formatTimeRemaining(listing.getTimeRemaining());

        return String.format("%s - %s (%s)", itemName, priceStr, timeStr);
    }

    private String formatTimeRemaining(long millis) {
        if (millis <= 0) return "Ended";

        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + "d " + (hours % 24) + "h";
        } else if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m";
        } else if (minutes > 0) {
            return minutes + "m " + (seconds % 60) + "s";
        } else {
            return seconds + "s";
        }
    }

    private String formatCurrency(long amount) {
        if (amount >= 1000000) {
            return String.format("%.1fM", amount / 1000000.0);
        } else if (amount >= 1000) {
            return String.format("%.1fK", amount / 1000.0);
        }
        return String.valueOf(amount);
    }

    private String getPageLabel() {
        if (totalPages == 0) {
            return "No results";
        }
        return "Page " + (currentPage + 1) + " of " + totalPages;
    }

    private void updateButtonStates() {
        // Pagination buttons
        if (getButton(40) != null) getButton(40).setEnabled(currentPage > 0);
        if (getButton(42) != null) getButton(42).setEnabled(currentPage < totalPages - 1);

        // Action buttons based on selection
        boolean hasSelection = selectedListingIndex >= 0 && selectedListingIndex < listings.size();
        if (getButton(50) != null) getButton(50).setEnabled(hasSelection);
        if (getButton(51) != null) getButton(51).setEnabled(hasSelection);

        if (hasSelection && getButton(52) != null) {
            AuctionListing listing = listings.get(selectedListingIndex);
            getButton(52).setEnabled(listing.buyoutPrice > 0 && playerBalance >= listing.buyoutPrice);
        } else if (getButton(52) != null) {
            getButton(52).setEnabled(false);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        drawDefaultBackground();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        // Draw background (using a standard size, can be customized)
        mc.renderEngine.bindTexture(new ResourceLocation("customnpcs", "textures/gui/bgfilled.png"));
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        // Draw selected item preview
        if (selectedListingIndex >= 0 && selectedListingIndex < listings.size()) {
            AuctionListing listing = listings.get(selectedListingIndex);
            drawItemPreview(listing, guiLeft + 180, guiTop + 160);
        }

        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
    }

    private void drawItemPreview(AuctionListing listing, int x, int y) {
        if (listing == null || listing.item == null) return;

        RenderHelper.enableGUIStandardItemLighting();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glEnable(GL11.GL_LIGHTING);

        itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.renderEngine, listing.item, x, y);
        itemRender.renderItemOverlayIntoGUI(fontRendererObj, mc.renderEngine, listing.item, x, y);

        RenderHelper.disableStandardItemLighting();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        // Title
        String titleText = getTabTitle();
        fontRendererObj.drawString(titleText, (xSize - fontRendererObj.getStringWidth(titleText)) / 2, -8,
            CustomNpcResourceListener.DefaultTextColor);

        // Results count
        fontRendererObj.drawString(totalResults + " listings", 175, 42, 0x666666);
    }

    private String getTabTitle() {
        switch (currentTab) {
            case 0: return "Browse Auctions";
            case 1: return "My Listings";
            case 2: return "My Active Bids";
            case 3: return "Claim Items";
            case 4: return "Create Listing";
            default: return "Auction House";
        }
    }

    // ICustomScrollListener
    @Override
    public void scrollClicked(int scrollId, int index, boolean doubleClick, String selection) {
        selectedListingIndex = index;
        updateButtonStates();

        if (doubleClick && selectedListingIndex >= 0 && selectedListingIndex < listings.size()) {
            viewDetails();
        }
    }

    @Override
    public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
        // Handled above
    }

    // IGuiData - Receive NBT data from server
    @Override
    public void setGuiData(NBTTagCompound compound) {
        // This can be used for initial setup data
    }

    // IAuctionData - Receive auction listing data
    @Override
    public void onAuctionDataReceived(int dataType, NBTTagCompound data,
                                       int totalResults, int currentPage, int totalPages,
                                       long playerBalance) {
        this.totalResults = totalResults;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.playerBalance = playerBalance;

        // Parse listings from NBT
        listings.clear();
        NBTTagList listingList = data.getTagList("Listings", 10);
        for (int i = 0; i < listingList.tagCount(); i++) {
            NBTTagCompound listingNBT = listingList.getCompoundTagAt(i);
            AuctionListing listing = new AuctionListing();
            listing.readFromNBT(listingNBT);
            listings.add(listing);
        }

        selectedListingIndex = -1;
        updateListingDisplay();
        updateButtonStates();

        // Update labels
        if (getLabel(41) != null) {
            getLabel(41).label = getPageLabel();
        }
        if (getLabel(60) != null) {
            getLabel(60).label = "Balance: " + formatCurrency(playerBalance);
        }
    }

    @Override
    public void save() {
        // Nothing to save
    }
}
