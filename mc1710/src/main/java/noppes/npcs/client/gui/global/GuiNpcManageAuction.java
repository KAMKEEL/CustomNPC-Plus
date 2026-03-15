package noppes.npcs.client.gui.global;

import kamkeel.npcs.network.packets.request.auction.ManageAuctionPacket;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.AuctionClientConfig;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.constants.EnumAuctionSort;
import noppes.npcs.constants.EnumClaimType;
import noppes.npcs.containers.ContainerManageAuction;
import noppes.npcs.controllers.data.AuctionClaim;
import noppes.npcs.controllers.data.AuctionFilter;
import noppes.npcs.controllers.data.AuctionListing;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.AuctionFormatUtil;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class GuiNpcManageAuction extends GuiContainerNPCInterface2 implements IGuiData, ITextfieldListener {
    private static final ResourceLocation SLOT_DEFAULT = new ResourceLocation("customnpcs", "textures/gui/slot.png");
    private static final ResourceLocation SLOT_AUCTION = new ResourceLocation("customnpcs", "textures/gui/auction/auction_slot.png");
    private static final ResourceLocation ICON_X = new ResourceLocation("customnpcs", "textures/gui/auction/x_icon.png");
    private static final ResourceLocation ICON_X_CANCEL = new ResourceLocation("customnpcs", "textures/gui/auction/x_icon_blue.png");
    private static final ResourceLocation ICON_CHECK = new ResourceLocation("customnpcs", "textures/gui/auction/check_icon.png");
    private static final ResourceLocation COIN_ICON = new ResourceLocation("customnpcs", "textures/items/npcCoinGold.png");

    // Slot tints (ARGB) aligned with My Trades style.
    private static final int TINT_BLUE = 0x605080FF;
    private static final int TINT_GREEN = 0x6050FF50;
    private static final int TINT_RED = 0x60FF5050;

    public enum Tab {
        LISTINGS,
        CREATE,
        CLAIMS
    }

    private enum PendingAction {
        NONE,
        STOP,
        CANCEL,
        CLAIM_ITEM,
        CLAIM_CURRENCY
    }

    private static final int BTN_TAB_LISTINGS = 100;
    private static final int BTN_TAB_CREATE = 101;
    private static final int BTN_TAB_CLAIMS = 102;
    private static final int BTN_PAGE_PREV = 110;
    private static final int BTN_PAGE_NEXT = 111;
    private static final int BTN_SORT = 112;
    private static final int BTN_CREATE = 120;

    private static final int TXT_SEARCH = 1;
    private static final int TXT_FAKE_SELLER = 2;
    private static final int TXT_FAKE_START = 3;
    private static final int TXT_FAKE_BUYOUT = 4;
    private static final int TXT_FAKE_DURATION = 5;

    private static final int RIGHT_X = 188;
    private static final int RIGHT_WIDTH = 220;
    private static final int TAB_Y = 10;

    private static final int LISTINGS_SORT_Y = 40;
    private static final int LISTINGS_SEARCH_LABEL_Y = 66;
    private static final int LISTINGS_SEARCH_FIELD_Y = 76;
    private static final int LISTINGS_HINT_Y = 98;
    private static final int LISTINGS_HINT2_Y = 110;
    private static final int LISTINGS_HINT3_Y = 122;
    private static final int LISTINGS_HINT4_Y = 134;
    private static final int LISTINGS_DETAILS_TITLE_Y = 42;
    private static final int DETAILS_PANEL_X = RIGHT_X;
    private static final int DETAILS_PANEL_Y = 40;
    private static final int DETAILS_PANEL_W = RIGHT_WIDTH - 8;
    private static final int DETAILS_PANEL_H = 138;

    private static final int CREATE_FIELD_X = RIGHT_X + 82;
    private static final int CREATE_FIELD_WIDTH = 126;
    private static final int CREATE_SELLER_Y = 52;
    private static final int CREATE_START_Y = 78;
    private static final int CREATE_BUYOUT_Y = 104;
    private static final int CREATE_DURATION_Y = 130;
    private static final int CREATE_BUTTON_Y = 178;

    private final ContainerManageAuction manageContainer;
    private final AuctionFilter filter = new AuctionFilter();

    private Tab activeTab = Tab.LISTINGS;
    private String searchText = "";

    private String fakeSeller = "Server";
    private String fakeStartPrice = "1";
    private String fakeBuyoutPrice = "";
    private String fakeDurationHours = Integer.toString(Math.max(1, AuctionClientConfig.getAuctionDurationHours()));

    private int listingsPage = 0;
    private int listingsTotalPages = 1;
    private int listingsTotalCount = 0;

    private int claimsPage = 0;
    private int claimsTotalPages = 1;
    private int claimsTotalCount = 0;
    private int claimsListingCount = 0;
    private int claimsClaimCount = 0;

    private int pendingDisplaySlot = -1;
    private PendingAction pendingAction = PendingAction.NONE;

    private String selectedListingId = null;
    private AuctionListing selectedListing = null;
    private int selectedDisplaySlot = -1;
    private boolean listingsDetailsView = false;

    private String errorMessage = null;
    private boolean createRequestPending = false;

    public GuiNpcManageAuction(EntityNPCInterface npc, ContainerManageAuction container) {
        super(npc, container);
        this.manageContainer = container;
        this.drawDefaultBackground = false;
        this.ySize = 200;
        this.title = "";
    }

    @Override
    public void initGui() {
        super.initGui();

        addButton(new GuiNpcButton(BTN_TAB_LISTINGS, guiLeft + RIGHT_X, guiTop + TAB_Y, 70, 20, "global.auction.manage.listings"));
        addButton(new GuiNpcButton(BTN_TAB_CREATE, guiLeft + RIGHT_X + 74, guiTop + TAB_Y, 70, 20, "global.auction.manage.create"));
        addButton(new GuiNpcButton(BTN_TAB_CLAIMS, guiLeft + RIGHT_X + 148, guiTop + TAB_Y, 70, 20, "global.auction.manage.claims"));
        updateTabButtonState();

        int pageButtonY = guiTop + ySize - 24;
        addButton(new GuiNpcButton(BTN_PAGE_PREV, guiLeft + RIGHT_X, pageButtonY, 20, 20, "<"));
        addButton(new GuiNpcButton(BTN_PAGE_NEXT, guiLeft + RIGHT_X + 24, pageButtonY, 20, 20, ">"));

        if (activeTab == Tab.LISTINGS && !listingsDetailsView) {
            String sortName = filter.getSortBy().getDisplayName();
            addButton(new GuiNpcButton(BTN_SORT, guiLeft + RIGHT_X, guiTop + LISTINGS_SORT_Y, RIGHT_WIDTH - 8, 20,
                StatCollector.translateToLocal("global.auction.manage.sort") + ": " + sortName));

            addTextField(new GuiNpcTextField(TXT_SEARCH, this, fontRendererObj,
                guiLeft + RIGHT_X, guiTop + LISTINGS_SEARCH_FIELD_Y, RIGHT_WIDTH - 8, 18, searchText));
            requestListings();
        } else if (activeTab == Tab.LISTINGS) {
            manageContainer.setDisplayMode(ContainerManageAuction.DisplayMode.LISTINGS);
        } else if (activeTab == Tab.CREATE) {
            addTextField(new GuiNpcTextField(TXT_FAKE_SELLER, this, fontRendererObj,
                guiLeft + CREATE_FIELD_X, guiTop + CREATE_SELLER_Y, CREATE_FIELD_WIDTH, 18, fakeSeller));

            GuiNpcTextField startField = new GuiNpcTextField(TXT_FAKE_START, this, fontRendererObj,
                guiLeft + CREATE_FIELD_X, guiTop + CREATE_START_Y, CREATE_FIELD_WIDTH, 18, fakeStartPrice);
            startField.setIntegersOnly();
            addTextField(startField);

            GuiNpcTextField buyoutField = new GuiNpcTextField(TXT_FAKE_BUYOUT, this, fontRendererObj,
                guiLeft + CREATE_FIELD_X, guiTop + CREATE_BUYOUT_Y, CREATE_FIELD_WIDTH, 18, fakeBuyoutPrice);
            buyoutField.setIntegersOnly();
            addTextField(buyoutField);

            GuiNpcTextField durationField = new GuiNpcTextField(TXT_FAKE_DURATION, this, fontRendererObj,
                guiLeft + CREATE_FIELD_X, guiTop + CREATE_DURATION_Y, CREATE_FIELD_WIDTH, 18, fakeDurationHours);
            durationField.setIntegersOnly();
            addTextField(durationField);

            addButton(new GuiNpcButton(BTN_CREATE, guiLeft + RIGHT_X, guiTop + CREATE_BUTTON_Y, RIGHT_WIDTH - 8, 20,
                "global.auction.manage.create.submit"));
        } else if (activeTab == Tab.CLAIMS) {
            requestClaims();
        }

        syncContainerSlots();
        updatePageButtons();
    }

    private void switchTab(Tab tab) {
        if (activeTab == tab) return;
        activeTab = tab;
        listingsDetailsView = false;
        if (tab != Tab.LISTINGS) {
            selectedListing = null;
            selectedListingId = null;
            selectedDisplaySlot = -1;
        }
        clearPending();
        errorMessage = null;
        initGui();
    }

    private void updateTabButtonState() {
        GuiNpcButton listingsBtn = getButton(BTN_TAB_LISTINGS);
        GuiNpcButton createBtn = getButton(BTN_TAB_CREATE);
        GuiNpcButton claimsBtn = getButton(BTN_TAB_CLAIMS);
        if (listingsBtn != null) {
            listingsBtn.enabled = activeTab != Tab.LISTINGS || listingsDetailsView;
        }
        if (createBtn != null) {
            createBtn.enabled = activeTab != Tab.CREATE;
        }
        if (claimsBtn != null) {
            claimsBtn.enabled = activeTab != Tab.CLAIMS;
        }
    }

    private void syncContainerSlots() {
        boolean showCreate = activeTab == Tab.CREATE;
        manageContainer.setCreateSlotVisible(showCreate);

        boolean showDetail = activeTab == Tab.LISTINGS && listingsDetailsView && selectedListing != null;
        manageContainer.setDetailSlotVisible(showDetail);
        if (showDetail) {
            manageContainer.setDetailItem(selectedListing.item);
        } else {
            manageContainer.clearDetailItem();
        }
    }

    private void updatePageButtons() {
        GuiNpcButton prev = getButton(BTN_PAGE_PREV);
        GuiNpcButton next = getButton(BTN_PAGE_NEXT);
        if (prev == null || next == null) return;

        if (activeTab == Tab.CREATE) {
            prev.visible = false;
            next.visible = false;
            prev.enabled = false;
            next.enabled = false;
            return;
        }

        if (activeTab == Tab.LISTINGS && listingsDetailsView) {
            prev.visible = false;
            next.visible = false;
            prev.enabled = false;
            next.enabled = false;
            return;
        }

        prev.visible = true;
        next.visible = true;
        if (activeTab == Tab.LISTINGS) {
            prev.enabled = listingsPage > 0;
            next.enabled = listingsPage < listingsTotalPages - 1;
        } else {
            prev.enabled = claimsPage > 0;
            next.enabled = claimsPage < claimsTotalPages - 1;
        }
    }

    private void requestListings() {
        filter.setSearchText(searchText);
        manageContainer.setDisplayMode(ContainerManageAuction.DisplayMode.LISTINGS);
        ManageAuctionPacket.requestListings(filter, listingsPage);
    }

    private void requestClaims() {
        manageContainer.setDisplayMode(ContainerManageAuction.DisplayMode.CLAIMS);
        ManageAuctionPacket.requestGlobalClaims(claimsPage);
    }

    private void cycleSort() {
        EnumAuctionSort[] values = EnumAuctionSort.values();
        int next = (filter.getSortBy().ordinal() + 1) % values.length;
        filter.setSortBy(values[next]);
        listingsPage = 0;
        clearPending();
        requestListings();
        initGui();
    }

    private void createFakeAuction() {
        GuiNpcTextField sellerField = getTextField(TXT_FAKE_SELLER);
        GuiNpcTextField startField = getTextField(TXT_FAKE_START);
        GuiNpcTextField buyoutField = getTextField(TXT_FAKE_BUYOUT);
        GuiNpcTextField durationField = getTextField(TXT_FAKE_DURATION);

        if (sellerField != null) fakeSeller = sellerField.getText();
        if (startField != null) fakeStartPrice = startField.getText();
        if (buyoutField != null) fakeBuyoutPrice = buyoutField.getText();
        if (durationField != null) fakeDurationHours = durationField.getText();

        ItemStack staged = manageContainer.getCreateItem();
        if (staged == null) {
            errorMessage = StatCollector.translateToLocal("global.auction.manage.error.noItem");
            return;
        }

        long start = parseLong(fakeStartPrice, 0);
        long buyout = parseLong(fakeBuyoutPrice, 0);
        int duration = (int) parseLong(fakeDurationHours, AuctionClientConfig.getAuctionDurationHours());
        if (duration <= 0) {
            duration = Math.max(1, AuctionClientConfig.getAuctionDurationHours());
        }

        if (start <= 0) {
            errorMessage = StatCollector.translateToLocal("global.auction.manage.error.invalidStart");
            return;
        }
        if (buyout > 0 && buyout < start) {
            errorMessage = StatCollector.translateToLocal("global.auction.manage.error.invalidBuyout");
            return;
        }

        String seller = fakeSeller != null ? fakeSeller.trim() : "";
        if (seller.isEmpty()) {
            seller = "Server";
            fakeSeller = seller;
        }

        ManageAuctionPacket.createFakeListing(staged, seller, start, buyout, duration);
        createRequestPending = true;
        errorMessage = null;
    }

    private long parseLong(String value, long fallback) {
        if (value == null || value.trim().isEmpty()) return fallback;
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        super.actionPerformed(guibutton);
        if (!(guibutton instanceof GuiNpcButton)) return;

        if (guibutton.id == BTN_TAB_LISTINGS) {
            if (activeTab == Tab.LISTINGS && listingsDetailsView) {
                listingsDetailsView = false;
                selectedListing = null;
                selectedListingId = null;
                selectedDisplaySlot = -1;
                clearPending();
                initGui();
            } else {
                switchTab(Tab.LISTINGS);
            }
            return;
        }
        if (guibutton.id == BTN_TAB_CREATE) {
            switchTab(Tab.CREATE);
            return;
        }
        if (guibutton.id == BTN_TAB_CLAIMS) {
            switchTab(Tab.CLAIMS);
            return;
        }

        if (guibutton.id == BTN_PAGE_PREV) {
            if (activeTab == Tab.LISTINGS && listingsPage > 0) {
                listingsPage--;
                clearPending();
                requestListings();
                NoppesUtil.clickSound();
            } else if (activeTab == Tab.CLAIMS && claimsPage > 0) {
                claimsPage--;
                clearPending();
                requestClaims();
                NoppesUtil.clickSound();
            }
            updatePageButtons();
            return;
        }

        if (guibutton.id == BTN_PAGE_NEXT) {
            if (activeTab == Tab.LISTINGS && listingsPage < listingsTotalPages - 1) {
                listingsPage++;
                clearPending();
                requestListings();
                NoppesUtil.clickSound();
            } else if (activeTab == Tab.CLAIMS && claimsPage < claimsTotalPages - 1) {
                claimsPage++;
                clearPending();
                requestClaims();
                NoppesUtil.clickSound();
            }
            updatePageButtons();
            return;
        }

        if (guibutton.id == BTN_SORT) {
            cycleSort();
            NoppesUtil.clickSound();
            return;
        }

        if (guibutton.id == BTN_CREATE) {
            createFakeAuction();
            NoppesUtil.clickSound();
        }
    }

    @Override
    protected void handleMouseClick(Slot slot, int slotIndex, int mouseButton, int clickType) {
        if (slot == null) {
            clearPending();
            return;
        }

        if (manageContainer.isDisplaySlot(slotIndex)) {
            int displayIndex = manageContainer.toDisplayIndex(slotIndex);
            if (activeTab == Tab.LISTINGS) {
                handleListingsSlotClick(displayIndex, mouseButton);
                return;
            }
            if (activeTab == Tab.CLAIMS) {
                handleClaimsSlotClick(displayIndex, mouseButton);
                return;
            }
        }

        if (activeTab == Tab.CREATE) {
            if (manageContainer.isCreateSlot(slotIndex)) {
                if (manageContainer.getCreateItem() != null) {
                    boolean removeAll = mouseButton == 0;
                    manageContainer.removeFromCreateSlot(removeAll);
                    NoppesUtil.clickSound();
                }
                return;
            }

            if (manageContainer.isPlayerSlot(slotIndex)) {
                ItemStack stack = slot.getStack();
                if (stack != null) {
                    boolean fullStack = mouseButton == 0;
                    manageContainer.addToCreateSlot(slotIndex, fullStack);
                    NoppesUtil.clickSound();
                }
                return;
            }
        }

        clearPending();
    }

    private void handleListingsSlotClick(int displayIndex, int mouseButton) {
        AuctionListing listing = manageContainer.getListingAtDisplay(displayIndex);
        if (listing == null) {
            clearPending();
            return;
        }

        // Any non-matching input while pending cancels the operation.
        if (pendingAction != PendingAction.NONE) {
            if (displayIndex == pendingDisplaySlot && isConfirmInput(pendingAction, mouseButton)) {
                ManageAuctionPacket.manageListing(listing.id, pendingAction == PendingAction.CANCEL);
                clearPending();
                playConfirmSound();
            } else {
                clearPending();
            }
            return;
        }

        if (mouseButton == 0) {
            selectListing(displayIndex, listing);
            clearPending();
            listingsDetailsView = true;
            initGui();
            NoppesUtil.clickSound();
            return;
        }

        if (isStopStartInput(mouseButton) || isCancelStartInput(mouseButton)) {
            PendingAction action = isShiftKeyDown() ? PendingAction.CANCEL : PendingAction.STOP;
            setPending(displayIndex, action);
            NoppesUtil.clickSound();
            return;
        }

        clearPending();
    }

    private void handleClaimsSlotClick(int displayIndex, int mouseButton) {
        AuctionListing listing = manageContainer.getListingAtDisplay(displayIndex);
        if (listing != null) {
            if (pendingAction != PendingAction.NONE) {
                if (displayIndex == pendingDisplaySlot && isConfirmInput(pendingAction, mouseButton)) {
                    ManageAuctionPacket.manageListing(listing.id, pendingAction == PendingAction.CANCEL);
                    clearPending();
                    playConfirmSound();
                } else {
                    clearPending();
                }
                return;
            }

            if (isStopStartInput(mouseButton) || isCancelStartInput(mouseButton)) {
                PendingAction action = isShiftKeyDown() ? PendingAction.CANCEL : PendingAction.STOP;
                setPending(displayIndex, action);
                NoppesUtil.clickSound();
            } else {
                clearPending();
            }
            return;
        }

        AuctionClaim claim = manageContainer.getClaimAtDisplay(displayIndex);
        if (claim == null) {
            clearPending();
            return;
        }

        PendingAction action = claim.type.isItem() ? PendingAction.CLAIM_ITEM : PendingAction.CLAIM_CURRENCY;
        if (pendingAction != PendingAction.NONE) {
            if (displayIndex == pendingDisplaySlot && pendingAction == action && isConfirmInput(action, mouseButton)) {
                if (action == PendingAction.CLAIM_ITEM) {
                    ManageAuctionPacket.claimGlobalItem(claim.id);
                } else {
                    ManageAuctionPacket.claimGlobalCurrency(claim.id);
                }
                clearPending();
                playConfirmSound();
            } else {
                clearPending();
            }
            return;
        }

        if (mouseButton == 0) {
            setPending(displayIndex, action);
            NoppesUtil.clickSound();
            return;
        }

        clearPending();
    }

    private boolean isStopStartInput(int mouseButton) {
        return mouseButton == 1 && !isShiftKeyDown();
    }

    private boolean isCancelStartInput(int mouseButton) {
        return mouseButton == 1 && isShiftKeyDown();
    }

    private boolean isConfirmInput(PendingAction action, int mouseButton) {
        if (action == PendingAction.CLAIM_ITEM || action == PendingAction.CLAIM_CURRENCY) {
            return mouseButton == 0;
        }
        if (action == PendingAction.STOP) {
            return mouseButton == 1 && !isShiftKeyDown();
        }
        if (action == PendingAction.CANCEL) {
            return mouseButton == 1 && isShiftKeyDown();
        }
        return false;
    }

    private void selectListing(int displayIndex, AuctionListing listing) {
        selectedDisplaySlot = displayIndex;
        selectedListingId = listing != null ? listing.id : null;
        selectedListing = listing;
        syncContainerSlots();
        updateTabButtonState();
    }

    private void refreshSelectedListing(List<AuctionListing> pageListings) {
        selectedListing = null;
        selectedDisplaySlot = -1;

        if (selectedListingId == null || pageListings == null) {
            syncContainerSlots();
            return;
        }

        for (int i = 0; i < pageListings.size(); i++) {
            AuctionListing listing = pageListings.get(i);
            if (listing != null && selectedListingId.equals(listing.id)) {
                selectedListing = listing;
                selectedDisplaySlot = i;
                break;
            }
        }

        if (selectedListing == null) {
            selectedListingId = null;
        }

        if (selectedListing == null) {
            listingsDetailsView = false;
        }

        syncContainerSlots();
        updateTabButtonState();
    }

    private void setPending(int displaySlot, PendingAction action) {
        this.pendingDisplaySlot = displaySlot;
        this.pendingAction = action;
        manageContainer.setHiddenDisplaySlot(displaySlot);
    }

    private void clearPending() {
        manageContainer.clearHiddenDisplaySlot();
        pendingDisplaySlot = -1;
        pendingAction = PendingAction.NONE;
    }

    private void playConfirmSound() {
        mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(
            new ResourceLocation("random.orb"), 1.0F));
    }

    @Override
    public void keyTyped(char c, int i) {
        super.keyTyped(c, i);

        if (activeTab == Tab.LISTINGS) {
            GuiNpcTextField search = getTextField(TXT_SEARCH);
            if (search != null && search.isFocused()) {
                String text = search.getText();
                if (!text.equals(searchText)) {
                    searchText = text;
                    listingsPage = 0;
                    clearPending();
                    requestListings();
                }
            }
        }
    }

    @Override
    public void unFocused(GuiNpcTextField textfield) {
        if (textfield == null) return;

        if (textfield.id == TXT_SEARCH) {
            searchText = textfield.getText();
        } else if (textfield.id == TXT_FAKE_SELLER) {
            fakeSeller = textfield.getText();
        } else if (textfield.id == TXT_FAKE_START) {
            fakeStartPrice = textfield.getText();
        } else if (textfield.id == TXT_FAKE_BUYOUT) {
            fakeBuyoutPrice = textfield.getText();
        } else if (textfield.id == TXT_FAKE_DURATION) {
            fakeDurationHours = textfield.getText();
        }
    }

    @Override
    public void setGuiData(NBTTagCompound compound) {
        if (compound.hasKey("ManageAuctionListingsData")) {
            listingsPage = compound.getInteger("Page");
            listingsTotalPages = Math.max(1, compound.getInteger("TotalPages"));
            listingsTotalCount = compound.getInteger("TotalListings");

            NBTTagList list = compound.getTagList("Listings", 10);
            List<AuctionListing> listings = new ArrayList<AuctionListing>();
            for (int i = 0; i < list.tagCount(); i++) {
                listings.add(AuctionListing.fromNBT(list.getCompoundTagAt(i)));
            }
            manageContainer.setListingsPage(listings);
            refreshSelectedListing(listings);
            updatePageButtons();
        }

        if (compound.hasKey("ManageAuctionClaimsData")) {
            claimsPage = compound.getInteger("Page");
            claimsTotalPages = Math.max(1, compound.getInteger("TotalPages"));
            claimsTotalCount = compound.hasKey("TotalEntries") ? compound.getInteger("TotalEntries") : compound.getInteger("TotalClaims");
            claimsClaimCount = compound.getInteger("TotalClaims");
            claimsListingCount = compound.hasKey("TotalListings") ? compound.getInteger("TotalListings") : 0;

            NBTTagList claimList = compound.getTagList("Claims", 10);
            List<AuctionClaim> claims = new ArrayList<AuctionClaim>();
            for (int i = 0; i < claimList.tagCount(); i++) {
                claims.add(AuctionClaim.fromNBT(claimList.getCompoundTagAt(i)));
            }

            NBTTagList listingList = compound.getTagList("Listings", 10);
            List<AuctionListing> listings = new ArrayList<AuctionListing>();
            for (int i = 0; i < listingList.tagCount(); i++) {
                listings.add(AuctionListing.fromNBT(listingList.getCompoundTagAt(i)));
            }

            manageContainer.setClaimsAndListingsPage(claims, listings);
            updatePageButtons();
        }

        if (compound.getBoolean("ManageAuctionRefresh")) {
            boolean createSuccess = compound.getBoolean("ManageAuctionCreateSuccess");
            createRequestPending = false;
            if (createSuccess) {
                manageContainer.clearCreateSlot();
                listingsDetailsView = false;
                selectedListing = null;
                selectedListingId = null;
                selectedDisplaySlot = -1;
                activeTab = Tab.LISTINGS;
                clearPending();
                initGui();
                return;
            }

            clearPending();
            if (activeTab == Tab.LISTINGS) {
                requestListings();
            } else if (activeTab == Tab.CLAIMS) {
                requestClaims();
            }
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(f, mouseX, mouseY);

        drawDisplaySlotBackgrounds();
        drawPlayerInventorySlotBackgrounds();
        drawRightPanelSlotBackgrounds();
        drawClaimsTintOverlay();
        drawSelectedListingOverlay();
        drawClaimIndicators();
        drawPendingOverlay();
    }

    private void drawDisplaySlotBackgrounds() {
        for (int row = 0; row < ContainerManageAuction.DISPLAY_ROWS; row++) {
            for (int col = 0; col < ContainerManageAuction.DISPLAY_COLS; col++) {
                int x = guiLeft + ContainerManageAuction.DISPLAY_X + col * 18;
                int y = guiTop + ContainerManageAuction.DISPLAY_Y + row * 18;
                drawAuctionSlot(x, y);
            }
        }
    }

    private void drawPlayerInventorySlotBackgrounds() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int x = guiLeft + ContainerManageAuction.PLAYER_INV_X + col * 18;
                int y = guiTop + ContainerManageAuction.PLAYER_INV_Y + row * 18;
                drawDefaultSlot(x, y);
            }
        }
        for (int col = 0; col < 9; col++) {
            int x = guiLeft + ContainerManageAuction.PLAYER_INV_X + col * 18;
            int y = guiTop + ContainerManageAuction.HOTBAR_Y;
            drawDefaultSlot(x, y);
        }
    }

    private void drawRightPanelSlotBackgrounds() {
        if (activeTab == Tab.LISTINGS && listingsDetailsView) {
            drawDetailsPanelBackground();
        }
        if (activeTab == Tab.LISTINGS && listingsDetailsView && selectedListing != null) {
            drawAuctionSlot(guiLeft + ContainerManageAuction.DETAIL_SLOT_X, guiTop + ContainerManageAuction.DETAIL_SLOT_Y);
        } else if (activeTab == Tab.CREATE) {
            drawAuctionSlot(guiLeft + ContainerManageAuction.CREATE_SLOT_X, guiTop + ContainerManageAuction.CREATE_SLOT_Y);
        }
    }

    private void drawDetailsPanelBackground() {
        int x = guiLeft + DETAILS_PANEL_X;
        int y = guiTop + DETAILS_PANEL_Y;
        int x2 = x + DETAILS_PANEL_W;
        int y2 = y + DETAILS_PANEL_H;

        Gui.drawRect(x, y, x2, y2, 0xCC0F0F0F);
        Gui.drawRect(x + 1, y + 1, x2 - 1, y2 - 1, 0x88202020);
        Gui.drawRect(x, y, x2, y + 1, 0xFF3A3A3A);
        Gui.drawRect(x, y2 - 1, x2, y2, 0xFF3A3A3A);
        Gui.drawRect(x, y, x + 1, y2, 0xFF3A3A3A);
        Gui.drawRect(x2 - 1, y, x2, y2, 0xFF3A3A3A);
    }

    private void drawSelectedListingOverlay() {
        if (activeTab != Tab.LISTINGS || selectedDisplaySlot < 0 || selectedDisplaySlot >= ContainerManageAuction.DISPLAY_SLOT_COUNT) {
            return;
        }

        int x = guiLeft + ContainerManageAuction.DISPLAY_X + (selectedDisplaySlot % ContainerManageAuction.DISPLAY_COLS) * 18;
        int y = guiTop + ContainerManageAuction.DISPLAY_Y + (selectedDisplaySlot / ContainerManageAuction.DISPLAY_COLS) * 18;
        drawColoredOverlay(x, y, 0x503080FF);
        Gui.drawRect(x, y, x + 18, y + 1, 0xFF8AC4FF);
        Gui.drawRect(x, y + 17, x + 18, y + 18, 0xFF8AC4FF);
        Gui.drawRect(x, y, x + 1, y + 18, 0xFF8AC4FF);
        Gui.drawRect(x + 17, y, x + 18, y + 18, 0xFF8AC4FF);
    }

    private void drawClaimIndicators() {
        if (activeTab != Tab.CLAIMS) return;

        for (int i = 0; i < ContainerManageAuction.DISPLAY_SLOT_COUNT; i++) {
            AuctionClaim claim = manageContainer.getClaimAtDisplay(i);
            if (claim == null) continue;

            boolean drawCoin = claim.type == EnumClaimType.CURRENCY
                || (claim.type == EnumClaimType.REFUND && claim.item == null);
            if (!drawCoin) continue;

            int x = guiLeft + ContainerManageAuction.DISPLAY_X + (i % ContainerManageAuction.DISPLAY_COLS) * 18;
            int y = guiTop + ContainerManageAuction.DISPLAY_Y + (i / ContainerManageAuction.DISPLAY_COLS) * 18;
            drawIconOverlay(x, y, COIN_ICON);
        }
    }

    private void drawClaimsTintOverlay() {
        if (activeTab != Tab.CLAIMS) return;
        for (int i = 0; i < ContainerManageAuction.DISPLAY_SLOT_COUNT; i++) {
            int tint = getClaimsSlotTint(i);
            if (tint == 0) continue;

            int x = guiLeft + ContainerManageAuction.DISPLAY_X + (i % ContainerManageAuction.DISPLAY_COLS) * 18;
            int y = guiTop + ContainerManageAuction.DISPLAY_Y + (i / ContainerManageAuction.DISPLAY_COLS) * 18;
            drawColoredOverlay(x, y, tint);
        }
    }

    private int getClaimsSlotTint(int displayIndex) {
        AuctionListing listing = manageContainer.getListingAtDisplay(displayIndex);
        if (listing != null) {
            return TINT_BLUE;
        }

        AuctionClaim claim = manageContainer.getClaimAtDisplay(displayIndex);
        if (claim == null) return 0;

        if (claim.type == EnumClaimType.CURRENCY) {
            return TINT_GREEN;
        }
        if (claim.type == EnumClaimType.REFUND) {
            return TINT_RED;
        }
        if (claim.type == EnumClaimType.ITEM) {
            return claim.isReturned ? TINT_RED : TINT_GREEN;
        }
        return TINT_GREEN;
    }

    private void drawPendingOverlay() {
        if (pendingDisplaySlot < 0 || pendingAction == PendingAction.NONE) return;

        int x = guiLeft + ContainerManageAuction.DISPLAY_X + (pendingDisplaySlot % ContainerManageAuction.DISPLAY_COLS) * 18;
        int y = guiTop + ContainerManageAuction.DISPLAY_Y + (pendingDisplaySlot / ContainerManageAuction.DISPLAY_COLS) * 18;

        int color = 0x80FFFF00;
        ResourceLocation icon = ICON_X;
        float iconR = 1f;
        float iconG = 1f;
        float iconB = 1f;
        if (pendingAction == PendingAction.STOP) {
            color = 0x805080FF;
            icon = ICON_X;
        } else if (pendingAction == PendingAction.CANCEL) {
            color = 0x80A040D0;
            icon = ICON_X_CANCEL;
        } else if (pendingAction == PendingAction.CLAIM_ITEM || pendingAction == PendingAction.CLAIM_CURRENCY) {
            color = 0x8055FF55;
            icon = ICON_CHECK;
        }

        drawColoredOverlay(x, y, color);
        drawIconOverlay(x, y, icon, iconR, iconG, iconB, 1f);
    }

    private void drawAuctionSlot(int x, int y) {
        GL11.glColor4f(1f, 1f, 1f, 1f);
        mc.renderEngine.bindTexture(SLOT_AUCTION);
        Gui.func_146110_a(x, y, 0, 0, 18, 18, 18, 18);
    }

    private void drawDefaultSlot(int x, int y) {
        GL11.glColor4f(1f, 1f, 1f, 1f);
        mc.renderEngine.bindTexture(SLOT_DEFAULT);
        drawTexturedModalRect(x, y, 0, 0, 18, 18);
    }

    private void drawColoredOverlay(int x, int y, int color) {
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        Gui.drawRect(x + 1, y + 1, x + 17, y + 17, color);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glColor4f(1f, 1f, 1f, 1f);
    }

    private void drawIconOverlay(int x, int y, ResourceLocation icon) {
        drawIconOverlay(x, y, icon, 1f, 1f, 1f, 1f);
    }

    private void drawIconOverlay(int x, int y, ResourceLocation icon, float r, float g, float b, float a) {
        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(r, g, b, a);
        mc.renderEngine.bindTexture(icon);
        Gui.func_146110_a(x + 1, y + 1, 0, 0, 16, 16, 16, 16);
        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        int textColor = CustomNpcResourceListener.DefaultTextColor;

        fontRendererObj.drawString(StatCollector.translateToLocal("global.auction.manage.title"), 8, 6, textColor);

        if (activeTab == Tab.LISTINGS) {
            if (listingsDetailsView) {
                drawListingsDetails(textColor);
            } else {
                drawPageText(listingsPage, listingsTotalPages, listingsTotalCount, textColor);
                fontRendererObj.drawString(StatCollector.translateToLocal("global.auction.manage.search"), RIGHT_X, LISTINGS_SEARCH_LABEL_Y, textColor);
                fontRendererObj.drawString(StatCollector.translateToLocal("global.auction.manage.hint.listings"), RIGHT_X, LISTINGS_HINT_Y, textColor);
                fontRendererObj.drawString(StatCollector.translateToLocal("global.auction.manage.hint.stop"), RIGHT_X, LISTINGS_HINT2_Y, textColor);
                fontRendererObj.drawString(StatCollector.translateToLocal("global.auction.manage.hint.cancel"), RIGHT_X, LISTINGS_HINT3_Y, textColor);
                fontRendererObj.drawString(StatCollector.translateToLocal("global.auction.manage.selectListing"), RIGHT_X, LISTINGS_HINT4_Y, textColor);
            }
        } else if (activeTab == Tab.CLAIMS) {
            drawPageText(claimsPage, claimsTotalPages, claimsTotalCount, textColor);
            fontRendererObj.drawString(StatCollector.translateToLocal("global.auction.manage.hint.claims"), RIGHT_X, 58, textColor);
            fontRendererObj.drawString(StatCollector.translateToLocal("global.auction.manage.hint.stop"), RIGHT_X, 70, textColor);
            fontRendererObj.drawString(StatCollector.translateToLocal("global.auction.manage.hint.cancel"), RIGHT_X, 82, textColor);
            String totals = claimsClaimCount + " claims | " + claimsListingCount + " listings";
            fontRendererObj.drawString(totals, RIGHT_X, 94, textColor);
        } else {
            fontRendererObj.drawString(StatCollector.translateToLocal("global.auction.manage.seller"), RIGHT_X, CREATE_SELLER_Y + 5, textColor);
            fontRendererObj.drawString(StatCollector.translateToLocal("global.auction.manage.startPrice"), RIGHT_X, CREATE_START_Y + 5, textColor);
            fontRendererObj.drawString(StatCollector.translateToLocal("global.auction.manage.buyoutPrice"), RIGHT_X, CREATE_BUYOUT_Y + 5, textColor);
            fontRendererObj.drawString(StatCollector.translateToLocal("global.auction.manage.durationHours"), RIGHT_X, CREATE_DURATION_Y + 5, textColor);
            fontRendererObj.drawString(StatCollector.translateToLocal("global.auction.manage.stagedItem"), RIGHT_X + 22, ContainerManageAuction.CREATE_SLOT_Y + 5, textColor);

            ItemStack staged = manageContainer.getCreateItem();
            if (staged != null) {
                int available = manageContainer.countItemInPlayerInventory(staged);
                String countText = staged.stackSize + "/" + available;
                int countColor = staged.stackSize <= available ? 0x55FF55 : 0xFF5555;
                fontRendererObj.drawStringWithShadow(countText, RIGHT_X + 22, ContainerManageAuction.CREATE_SLOT_Y + 16, countColor);
            }
        }

        if (errorMessage != null && !errorMessage.isEmpty()) {
            fontRendererObj.drawString(EnumChatFormatting.RED + errorMessage, RIGHT_X, ySize - 12, 0xFFFFFF);
        }

        drawDynamicTooltip(mouseX, mouseY);
    }

    private void drawPageText(int page, int totalPages, int totalCount, int textColor) {
        String pageText = (page + 1) + "/" + totalPages + " (" + totalCount + ")";
        fontRendererObj.drawString(StatCollector.translateToLocal("global.auction.manage.page") + ": " + pageText,
            RIGHT_X + 48, ySize - 18, textColor);
    }

    private void drawListingsDetails(int textColor) {
        int panelLeft = DETAILS_PANEL_X + 6;
        int panelRight = DETAILS_PANEL_X + DETAILS_PANEL_W - 6;
        int y = LISTINGS_DETAILS_TITLE_Y;

        fontRendererObj.drawString(EnumChatFormatting.AQUA + StatCollector.translateToLocal("global.auction.manage.details"), panelLeft, y, 0xFFFFFF);
        y += 12;

        if (selectedListing == null) {
            fontRendererObj.drawString(EnumChatFormatting.GRAY + StatCollector.translateToLocal("global.auction.manage.selectListing"), panelLeft, y, 0xFFFFFF);
            return;
        }

        y = ContainerManageAuction.DETAIL_SLOT_Y + 1;
        drawDetailsLine(panelLeft + 24, panelRight, y, EnumChatFormatting.GRAY + StatCollector.translateToLocal("auction.seller").replace("%s", ""),
            EnumChatFormatting.WHITE + selectedListing.sellerName);

        y += 11;
        long currentValue = selectedListing.hasBids() ? selectedListing.currentBid : selectedListing.startingPrice;
        String currentLabel = selectedListing.hasBids()
            ? StatCollector.translateToLocal("auction.info.currentBid")
            : StatCollector.translateToLocal("auction.info.startingPrice");
        drawDetailsLine(panelLeft + 24, panelRight, y, EnumChatFormatting.YELLOW + currentLabel,
            EnumChatFormatting.GOLD + AuctionFormatUtil.formatCurrencyWithName(currentValue));

        y += 11;
        if (selectedListing.hasBuyout()) {
            drawDetailsLine(panelLeft + 24, panelRight, y, EnumChatFormatting.GREEN + StatCollector.translateToLocal("auction.info.buyoutPrice"),
                EnumChatFormatting.GREEN + AuctionFormatUtil.formatCurrencyWithName(selectedListing.buyoutPrice));
            y += 11;
        }

        drawDetailsLine(panelLeft + 24, panelRight, y, EnumChatFormatting.GRAY + StatCollector.translateToLocal("auction.info.bidCount"),
            EnumChatFormatting.WHITE + Integer.toString(selectedListing.bidCount));
        y += 11;

        long remaining = selectedListing.getTimeRemaining();
        EnumChatFormatting timeColor = AuctionFormatUtil.isTimeUrgent(remaining) ? EnumChatFormatting.RED : EnumChatFormatting.WHITE;
        drawDetailsLine(panelLeft + 24, panelRight, y, EnumChatFormatting.GRAY + StatCollector.translateToLocal("auction.info.timeLeft"),
            timeColor + AuctionFormatUtil.formatTimeRemaining(remaining));
    }

    private void drawDetailsLine(int leftX, int rightX, int y, String label, String value) {
        fontRendererObj.drawString(label, leftX, y, 0xFFFFFF);
        int width = fontRendererObj.getStringWidth(value);
        fontRendererObj.drawString(value, rightX - width, y, 0xFFFFFF);
    }

    private int getHoveredDisplaySlot(int mouseX, int mouseY) {
        int relX = mouseX - ContainerManageAuction.DISPLAY_X;
        int relY = mouseY - ContainerManageAuction.DISPLAY_Y;
        if (relX < 0 || relY < 0) return -1;

        int col = relX / 18;
        int row = relY / 18;
        if (col < 0 || col >= ContainerManageAuction.DISPLAY_COLS || row < 0 || row >= ContainerManageAuction.DISPLAY_ROWS) return -1;
        return col + row * ContainerManageAuction.DISPLAY_COLS;
    }

    private void drawDynamicTooltip(int mouseX, int mouseY) {
        if (hasSubGui()) return;

        // GuiContainer foreground receives absolute mouse coords; convert to GUI-local like auction GUIs.
        int guiMouseX = mouseX - guiLeft;
        int guiMouseY = mouseY - guiTop;
        int hovered = getHoveredDisplaySlot(guiMouseX, guiMouseY);

        List<String> tooltip = new ArrayList<String>();

        if (hovered >= 0) {
            if (hovered == pendingDisplaySlot && pendingAction != PendingAction.NONE) {
                if (pendingAction == PendingAction.STOP) {
                    tooltip.add(EnumChatFormatting.GOLD + StatCollector.translateToLocal("global.auction.manage.confirm.stop"));
                } else if (pendingAction == PendingAction.CANCEL) {
                    tooltip.add(EnumChatFormatting.RED + StatCollector.translateToLocal("global.auction.manage.confirm.cancel"));
                } else {
                    tooltip.add(EnumChatFormatting.GREEN + StatCollector.translateToLocal("global.auction.manage.confirm.claim"));
                }
                tooltip.add(EnumChatFormatting.DARK_GRAY + StatCollector.translateToLocal("auction.trades.otherClickCancel"));
            } else if (activeTab == Tab.CLAIMS) {
                AuctionClaim claim = manageContainer.getClaimAtDisplay(hovered);
                if (claim != null && claim.item == null) {
                    if (claim.type == EnumClaimType.CURRENCY) {
                        tooltip.add(EnumChatFormatting.GREEN + "Sold Auction Claim");
                        if (claim.itemName != null && !claim.itemName.isEmpty()) {
                            tooltip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("auction.claim.soldItem")
                                .replace("%s", EnumChatFormatting.WHITE + claim.itemName));
                        }
                        if (claim.otherPlayerName != null && !claim.otherPlayerName.isEmpty()) {
                            tooltip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("auction.claim.buyer")
                                .replace("%s", EnumChatFormatting.WHITE + claim.otherPlayerName));
                        }
                        tooltip.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocal("auction.claim.amount")
                            .replace("%s", EnumChatFormatting.GOLD + AuctionFormatUtil.formatCurrencyWithName(claim.currency)));
                    } else if (claim.type == EnumClaimType.REFUND) {
                        tooltip.add(EnumChatFormatting.RED + "Outbid Refund Claim");
                        if (claim.otherPlayerName != null && !claim.otherPlayerName.isEmpty()) {
                            tooltip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("auction.claim.outbidBy")
                                .replace("%s", EnumChatFormatting.WHITE + claim.otherPlayerName));
                        }
                        tooltip.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocal("auction.claim.amount")
                            .replace("%s", EnumChatFormatting.GOLD + AuctionFormatUtil.formatCurrencyWithName(claim.currency)));
                    } else if (claim.type.isCurrency()) {
                        tooltip.add(EnumChatFormatting.GOLD + String.format("%,d", claim.currency) + " " + AuctionClientConfig.getCurrencyName());
                    }
                    tooltip.add(EnumChatFormatting.GREEN + StatCollector.translateToLocal("auction.claim.clickToClaim"));
                }
            }
        }

        if (!tooltip.isEmpty()) {
            drawHoveringText(tooltip, guiMouseX, guiMouseY, fontRendererObj);
        }
    }

    public ContainerManageAuction getManageContainer() {
        return manageContainer;
    }

    public Tab getActiveTab() {
        return activeTab;
    }

    @Override
    public void save() {
    }
}
