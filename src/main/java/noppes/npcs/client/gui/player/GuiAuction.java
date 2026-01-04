package noppes.npcs.client.gui.player;

import kamkeel.npcs.network.packets.player.AuctionActionPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.config.ConfigMarket;
import noppes.npcs.containers.ContainerAuction;
import noppes.npcs.constants.EnumAuctionDuration;
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

    private static final ResourceLocation BG_TEXTURE = new ResourceLocation("customnpcs", "textures/gui/standardbg.png");
    private static final int LEFT_PANEL_X = 8;
    private static final int LEFT_PANEL_WIDTH = 190;
    private static final int RIGHT_PANEL_X = 210;
    private static final int ACTION_BUTTON_WIDTH = 98;
    private static final int LISTING_SCROLL_HEIGHT = 100;
    private static final int LISTING_SCROLL_WIDTH = 190;
    private static final int PAGE_BUTTON_WIDTH = 60;
    private static final int SEARCH_FIELD_WIDTH = 140;
    private static final int SEARCH_BUTTON_WIDTH = 46;
    private static final int HIDDEN_SLOT_POSITION = -1000;
    private static final int INVENTORY_START_X = 8;
    private static final int INVENTORY_START_Y = 166;
    private static final int HOTBAR_Y_OFFSET = 58;

    // GUI Components
    private GuiNpcTextField searchField;
    private GuiNpcTextField startingPriceField;
    private GuiNpcTextField buyoutPriceField;
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
    private int selectedDuration = 1;

    // Selected listing for details
    private int selectedListingIndex = -1;

    // Countdown update timer
    private long lastCountdownUpdate = 0;

    public GuiAuction(EntityNPCInterface npc, ContainerAuction container) {
        super(npc, container);
        this.title = "";
        this.xSize = 319;
        this.ySize = 238;
        this.closeOnEsc = true;
        this.drawDefaultBackground = true;

        // Request initial data
        AuctionActionPacket.QueryListings(currentFilter);
    }

    @Override
    public void initGui() {
        super.initGui();

        int leftX = guiLeft + LEFT_PANEL_X;
        int rightX = guiLeft + RIGHT_PANEL_X;
        int actionY = guiTop + 44;
        boolean isCreateTab = currentTab == 4;

        updateListingSlotPosition(isCreateTab);

        // Tab buttons at top
        GuiMenuTopButton browseButton = new GuiMenuTopButton(0, guiLeft + 4, guiTop - 17, StatCollector.translateToLocal("auction.browse"));
        addTopButton(browseButton);
        GuiMenuTopButton myListingsButton = new GuiMenuTopButton(1, browseButton, StatCollector.translateToLocal("auction.myListings"));
        addTopButton(myListingsButton);
        GuiMenuTopButton myBidsButton = new GuiMenuTopButton(2, myListingsButton, StatCollector.translateToLocal("auction.myBids"));
        addTopButton(myBidsButton);
        GuiMenuTopButton claimsButton = new GuiMenuTopButton(3, myBidsButton, StatCollector.translateToLocal("auction.claims"));
        addTopButton(claimsButton);
        GuiMenuTopButton sellButton = new GuiMenuTopButton(4, claimsButton, StatCollector.translateToLocal("auction.sell"));
        addTopButton(sellButton);
        getTopButton(currentTab).setEnabled(false);

        if (isCreateTab) {
            addCreateControls(leftX, rightX);
        } else {
            // Search field
            searchField = new GuiNpcTextField(10, this, leftX, guiTop + 6, SEARCH_FIELD_WIDTH, 14, currentFilter.searchText);
            searchField.setMaxStringLength(50);
            addTextField(searchField);

            // Search button
            addButton(new GuiNpcButton(11, leftX + SEARCH_FIELD_WIDTH + 4, guiTop + 5, SEARCH_BUTTON_WIDTH, 16,
                StatCollector.translateToLocal("auction.search")));

            // Sort button
            addButton(new GuiNpcButton(30, leftX, guiTop + 24, LEFT_PANEL_WIDTH, 16, currentFilter.sortOrder.getDisplayName()));

            // Listing scroll area
            listingScroll = new GuiCustomScroll(this, 0);
            listingScroll.guiLeft = leftX;
            listingScroll.guiTop = guiTop + 44;
            listingScroll.setSize(LISTING_SCROLL_WIDTH, LISTING_SCROLL_HEIGHT);
            addScroll(listingScroll);
            updateListingDisplay();

            // Pagination buttons
            addButton(new GuiNpcButton(40, leftX, guiTop + 148, PAGE_BUTTON_WIDTH, 16,
                StatCollector.translateToLocal("auction.prevPage")));
            addLabel(new GuiNpcLabel(41, getPageLabel(), leftX + 70, guiTop + 152, CustomNpcResourceListener.DefaultTextColor));
            addButton(new GuiNpcButton(42, leftX + LEFT_PANEL_WIDTH - PAGE_BUTTON_WIDTH, guiTop + 148, PAGE_BUTTON_WIDTH, 16,
                StatCollector.translateToLocal("auction.nextPage")));

            // Action buttons (right side)
            addButton(new GuiNpcButton(50, rightX, actionY, ACTION_BUTTON_WIDTH, 20, StatCollector.translateToLocal("auction.viewDetails")));
            addButton(new GuiNpcButton(51, rightX, actionY + 24, ACTION_BUTTON_WIDTH, 20, StatCollector.translateToLocal("auction.placeBid")));
            addButton(new GuiNpcButton(52, rightX, actionY + 48, ACTION_BUTTON_WIDTH, 20, StatCollector.translateToLocal("auction.buyout")));
            addButton(new GuiNpcButton(53, rightX, actionY + 72, ACTION_BUTTON_WIDTH, 20, StatCollector.translateToLocal("auction.refresh")));

            // Balance display
            addLabel(new GuiNpcLabel(60, String.format(StatCollector.translateToLocal("auction.balance"), formatCurrency(playerBalance)),
                rightX, guiTop + 170, 0x00AA00));
        }

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

        if (currentTab == 4 && inventorySlots instanceof ContainerAuction) {
            ItemStack item = ((ContainerAuction) inventorySlots).getListingItem();
            GuiNpcLabel itemLabel = getLabel(102);
            GuiNpcButton createButton = getButton(71);
            if (item != null) {
                if (itemLabel != null) {
                    itemLabel.label = StatCollector.translateToLocal("auction.item") + " " + item.getDisplayName();
                    itemLabel.color = 0x008800;
                }
                if (createButton != null) {
                    createButton.setEnabled(true);
                }
            } else {
                if (itemLabel != null) {
                    itemLabel.label = StatCollector.translateToLocal("auction.noItemSelected");
                    itemLabel.color = 0x888888;
                }
                if (createButton != null) {
                    createButton.setEnabled(false);
                }
            }
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
        } else if (id == 70) {
            selectedDuration = (selectedDuration + 1) % EnumAuctionDuration.values().length;
            initGui();
        } else if (id == 71) {
            createListing();
        } else if (id == 72) {
            switchTab(0);
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
            case 4:
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

    private void updateListingSlotPosition(boolean showInventory) {
        if (!(inventorySlots instanceof ContainerAuction)) {
            return;
        }

        ContainerAuction container = (ContainerAuction) inventorySlots;
        if (showInventory) {
            container.listingSlot.xDisplayPosition = RIGHT_PANEL_X + 41;
            container.listingSlot.yDisplayPosition = 64;

            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 9; col++) {
                    int slotIndex = 1 + col + row * 9;
                    container.inventorySlots.get(slotIndex).xDisplayPosition = INVENTORY_START_X + col * 18;
                    container.inventorySlots.get(slotIndex).yDisplayPosition = INVENTORY_START_Y + row * 18;
                }
            }

            for (int col = 0; col < 9; col++) {
                int slotIndex = 28 + col;
                container.inventorySlots.get(slotIndex).xDisplayPosition = INVENTORY_START_X + col * 18;
                container.inventorySlots.get(slotIndex).yDisplayPosition = INVENTORY_START_Y + HOTBAR_Y_OFFSET;
            }
        } else {
            container.listingSlot.xDisplayPosition = HIDDEN_SLOT_POSITION;
            container.listingSlot.yDisplayPosition = HIDDEN_SLOT_POSITION;

            for (int slotIndex = 1; slotIndex < container.inventorySlots.size(); slotIndex++) {
                container.inventorySlots.get(slotIndex).xDisplayPosition = HIDDEN_SLOT_POSITION;
                container.inventorySlots.get(slotIndex).yDisplayPosition = HIDDEN_SLOT_POSITION;
            }
        }
    }

    private void addCreateControls(int leftX, int rightX) {
        addLabel(new GuiNpcLabel(100, StatCollector.translateToLocal("auction.createListing"), leftX, guiTop + 8, 0x404040));
        addLabel(new GuiNpcLabel(101, StatCollector.translateToLocal("auction.placeItemBelow"), rightX, guiTop + 32, 0x404040));

        ItemStack item = ((ContainerAuction) inventorySlots).getListingItem();
        if (item != null) {
            addLabel(new GuiNpcLabel(102, StatCollector.translateToLocal("auction.item") + " " + item.getDisplayName(),
                rightX, guiTop + 86, 0x008800));
        } else {
            addLabel(new GuiNpcLabel(102, StatCollector.translateToLocal("auction.noItemSelected"), rightX, guiTop + 86, 0x888888));
        }

        addLabel(new GuiNpcLabel(110, StatCollector.translateToLocal("auction.startingPrice"), leftX, guiTop + 36, 0x404040));
        startingPriceField = new GuiNpcTextField(111, this, leftX + 120, guiTop + 34, 90, 14, "100");
        startingPriceField.setIntegersOnly();
        startingPriceField.setMinMaxDefault(1, 999999999, 100);
        addTextField(startingPriceField);

        addLabel(new GuiNpcLabel(120, StatCollector.translateToLocal("auction.buyoutPrice"), leftX, guiTop + 58, 0x404040));
        buyoutPriceField = new GuiNpcTextField(121, this, leftX + 120, guiTop + 56, 90, 14, "0");
        buyoutPriceField.setIntegersOnly();
        buyoutPriceField.setMinMaxDefault(0, 999999999, 0);
        addTextField(buyoutPriceField);
        addLabel(new GuiNpcLabel(122, StatCollector.translateToLocal("auction.noBuyoutHint"), leftX + 214, guiTop + 58, 0x888888));

        addLabel(new GuiNpcLabel(130, StatCollector.translateToLocal("auction.duration"), leftX, guiTop + 80, 0x404040));
        addButton(new GuiNpcButton(70, leftX + 120, guiTop + 76, 110, 20, getDurationDisplay()));

        long fee = getListingFee();
        addLabel(new GuiNpcLabel(140, StatCollector.translateToLocal("auction.listingFee") + " " + fee + " " + ConfigMarket.CurrencyName,
            leftX, guiTop + 104, fee > 0 ? 0xAA0000 : 0x008800));

        addButton(new GuiNpcButton(71, leftX, guiTop + 126, 90, 20, StatCollector.translateToLocal("auction.create")));
        addButton(new GuiNpcButton(72, leftX + 100, guiTop + 126, 90, 20, StatCollector.translateToLocal("gui.cancel")));

        if (getButton(71) != null) {
            getButton(71).setEnabled(item != null);
        }
    }

    private String getDurationDisplay() {
        EnumAuctionDuration duration = EnumAuctionDuration.values()[selectedDuration];
        return duration.getDisplayName();
    }

    private long getListingFee() {
        EnumAuctionDuration duration = EnumAuctionDuration.values()[selectedDuration];
        return duration.getListingFee();
    }

    private void createListing() {
        ContainerAuction container = (ContainerAuction) inventorySlots;
        ItemStack item = container.getListingItem();
        if (item == null) {
            return;
        }

        long startingPrice = parsePrice(startingPriceField, 1);
        long buyoutPrice = parsePrice(buyoutPriceField, 0);

        if (startingPrice < 1) {
            startingPrice = 1;
        }

        if (buyoutPrice > 0 && buyoutPrice < startingPrice) {
            return;
        }

        EnumAuctionDuration duration = EnumAuctionDuration.values()[selectedDuration];
        AuctionActionPacket.CreateListing(item, startingPrice, buyoutPrice, duration);
        container.clearListingSlot();
        switchTab(0);
    }

    private long parsePrice(GuiNpcTextField field, long defaultValue) {
        if (field == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(field.getText());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        drawDefaultBackground();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        // Draw background
        mc.renderEngine.bindTexture(BG_TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, 252, ySize);
        drawTexturedModalRect(guiLeft + 252, guiTop, 188, 0, xSize - 252, ySize);

        // Draw selected item preview
        if (selectedListingIndex >= 0 && selectedListingIndex < listings.size()) {
            AuctionListing listing = listings.get(selectedListingIndex);
            drawItemPreview(listing, guiLeft + RIGHT_PANEL_X + 41, guiTop + 192);
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

        if (currentTab != 4) {
            String listingsLabel = String.format(StatCollector.translateToLocal("auction.listings"), totalResults);
            fontRendererObj.drawString(listingsLabel, LEFT_PANEL_X, 36, 0x666666);
        }
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
