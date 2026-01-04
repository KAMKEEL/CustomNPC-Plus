package noppes.npcs.client.gui.player;

import kamkeel.npcs.network.packets.player.AuctionActionPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.*;
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
 * Displays listings with search, sort, and pagination.
 */
public class GuiAuction extends GuiContainerNPCInterface implements IGuiData,
        ICustomScrollListener, NoppesUtil.IAuctionData {

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
        this.title = "";
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
        addTopButton(new GuiMenuTopButton(0, guiLeft + 4, guiTop - 17, StatCollector.translateToLocal("auction.browse")));
        addTopButton(new GuiMenuTopButton(1, guiLeft + 54, guiTop - 17, StatCollector.translateToLocal("auction.myListings")));
        addTopButton(new GuiMenuTopButton(2, guiLeft + 114, guiTop - 17, StatCollector.translateToLocal("auction.myBids")));
        addTopButton(new GuiMenuTopButton(3, guiLeft + 164, guiTop - 17, StatCollector.translateToLocal("auction.claims")));
        addTopButton(new GuiMenuTopButton(4, guiLeft + 214, guiTop - 17, StatCollector.translateToLocal("auction.sell")));
        getTopButton(currentTab).setEnabled(false);

        // Search field
        searchField = new GuiNpcTextField(10, this, guiLeft + 8, guiTop + 6, 140, 14, currentFilter.searchText);
        searchField.setMaxStringLength(50);
        addTextField(searchField);

        // Search button
        addButton(new GuiNpcButton(11, guiLeft + 150, guiTop + 5, 40, 16, StatCollector.translateToLocal("auction.search")));

        // Sort button
        addButton(new GuiNpcButton(30, guiLeft + 8, guiTop + 24, 120, 16, currentFilter.sortOrder.getDisplayName()));

        // Listing scroll area
        listingScroll = new GuiCustomScroll(this, 0);
        listingScroll.guiLeft = guiLeft + 8;
        listingScroll.guiTop = guiTop + 44;
        listingScroll.setSize(160, 128);
        addScroll(listingScroll);
        updateListingDisplay();

        // Pagination buttons
        addButton(new GuiNpcButton(40, guiLeft + 8, guiTop + 176, 40, 16, StatCollector.translateToLocal("auction.prevPage")));
        addLabel(new GuiNpcLabel(41, getPageLabel(), guiLeft + 90, guiTop + 180, CustomNpcResourceListener.DefaultTextColor));
        addButton(new GuiNpcButton(42, guiLeft + 128, guiTop + 176, 40, 16, StatCollector.translateToLocal("auction.nextPage")));

        // Action buttons (right side)
        addButton(new GuiNpcButton(50, guiLeft + 175, guiTop + 50, 73, 20, StatCollector.translateToLocal("auction.viewDetails")));
        addButton(new GuiNpcButton(51, guiLeft + 175, guiTop + 75, 73, 20, StatCollector.translateToLocal("auction.placeBid")));
        addButton(new GuiNpcButton(52, guiLeft + 175, guiTop + 100, 73, 20, StatCollector.translateToLocal("auction.buyout")));
        addButton(new GuiNpcButton(53, guiLeft + 175, guiTop + 125, 73, 20, StatCollector.translateToLocal("auction.refresh")));

        // Balance display
        addLabel(new GuiNpcLabel(60, String.format(StatCollector.translateToLocal("auction.balance"), formatCurrency(playerBalance)),
            guiLeft + 175, guiTop + 155, 0x00AA00));

        updateButtonStates();
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
            if (listing.item == null) {
                return;
            }
            setSubGui(new SubGuiAuctionDetails(this, listing));
        }
    }

    private void placeBid() {
        if (selectedListingIndex >= 0 && selectedListingIndex < listings.size()) {
            AuctionListing listing = listings.get(selectedListingIndex);
            if (listing.item == null) {
                return;
            }
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
        listingScroll.setList(displayList, true, false);

        if (selectedListingIndex >= 0 && selectedListingIndex < displayList.size()) {
            listingScroll.selected = selectedListingIndex;
        }
    }

    private String formatListingLine(AuctionListing listing) {
        String itemName = listing.item == null
            ? "Unknown"
            : listing.item.getDisplayName();
        if (itemName.length() > 15) {
            itemName = itemName.substring(0, 12) + "...";
        }

        long price = listing.currentBid > 0 ? listing.currentBid : listing.startingPrice;
        String priceStr = formatCurrency(price);

        String timeStr = formatTimeRemaining(listing.getTimeRemaining());

        return String.format("%s - %s (%s)", itemName, priceStr, timeStr);
    }

    private String formatTimeRemaining(long millis) {
        if (millis <= 0) return StatCollector.translateToLocal("auction.ended");

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
            return StatCollector.translateToLocal("auction.noResults");
        }
        return String.format(StatCollector.translateToLocal("auction.page"), currentPage + 1, totalPages);
    }

    private void updateButtonStates() {
        // Pagination buttons
        if (getButton(40) != null) getButton(40).setEnabled(currentPage > 0);
        if (getButton(42) != null) getButton(42).setEnabled(currentPage < totalPages - 1);

        // Action buttons based on selection
        boolean hasSelection = selectedListingIndex >= 0 && selectedListingIndex < listings.size();
        if (hasSelection && listings.get(selectedListingIndex).item == null) {
            hasSelection = false;
        }
        if (getButton(50) != null) getButton(50).setEnabled(hasSelection);
        if (getButton(51) != null) getButton(51).setEnabled(hasSelection);

        if (hasSelection && getButton(52) != null) {
            AuctionListing listing = listings.get(selectedListingIndex);
            getButton(52).setEnabled(listing.item != null &&
                listing.buyoutPrice > 0 &&
                playerBalance >= listing.buyoutPrice);
        } else if (getButton(52) != null) {
            getButton(52).setEnabled(false);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        drawDefaultBackground();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        // Draw background
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
        String listingsLabel = String.format(StatCollector.translateToLocal("auction.listings"), totalResults);
        fontRendererObj.drawString(listingsLabel, 175, 42, 0x666666);
    }

    private String getTabTitle() {
        switch (currentTab) {
            case 0: return StatCollector.translateToLocal("auction.browseTitle");
            case 1: return StatCollector.translateToLocal("auction.myListingsTitle");
            case 2: return StatCollector.translateToLocal("auction.myBidsTitle");
            case 3: return StatCollector.translateToLocal("auction.claimsTitle");
            case 4: return StatCollector.translateToLocal("auction.sellTitle");
            default: return StatCollector.translateToLocal("auction.title");
        }
    }

    // ICustomScrollListener
    @Override
    public void customScrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
        int index = scroll.selected;
        selectedListingIndex = index;
        updateButtonStates();
    }

    @Override
    public void customScrollDoubleClicked(String selection, GuiCustomScroll scroll) {
        if (selectedListingIndex >= 0 && selectedListingIndex < listings.size()) {
            viewDetails();
        }
    }

    // IGuiData
    @Override
    public void setGuiData(NBTTagCompound compound) {
        // This can be used for initial setup data
    }

    // IAuctionData
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
            getLabel(60).label = String.format(StatCollector.translateToLocal("auction.balance"), formatCurrency(playerBalance));
        }
    }

    @Override
    public void save() {
        // Nothing to save
    }
}
