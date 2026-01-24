package noppes.npcs.client.gui.player;

import kamkeel.npcs.network.packets.player.AuctionActionPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.GuiAuctionNavButton;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumAuctionSort;
import noppes.npcs.containers.ContainerAuctionListing;
import noppes.npcs.controllers.data.AuctionFilter;
import noppes.npcs.controllers.data.AuctionListing;
import noppes.npcs.entity.EntityNPCInterface;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI for browsing auction listings.
 * Displays 9x5 grid with pagination and filter controls.
 */
public class GuiAuctionListing extends GuiAuctionInterface implements ISubGuiListener, IGuiData {
    // Small button textures (10x10)
    private static final ResourceLocation SMALL_BUTTON = new ResourceLocation("customnpcs", "textures/gui/auction/small_button.png");
    private static final ResourceLocation SMALL_BUTTON_PRESS = new ResourceLocation("customnpcs", "textures/gui/auction/small_button_press.png");

    // Arrow icons (8x8)
    private static final ResourceLocation ICON_UP = new ResourceLocation("customnpcs", "textures/gui/auction/up.png");
    private static final ResourceLocation ICON_DOWN = new ResourceLocation("customnpcs", "textures/gui/auction/down.png");

    // Grid layout - non-final for testing/positioning
    protected int gridX = 56;
    protected int gridY = 46;
    protected int cols = 9;
    protected int rows = 5;

    // Pagination positions (side by side horizontally)
    protected int pageUpX = 33;
    protected int pageDownX = 45;
    protected int pageBtnY = 128;

    // Filter button position
    protected int navYFilter = 109;

    // Button IDs
    private static final int BTN_PAGE_UP = 200;
    private static final int BTN_PAGE_DOWN = 201;

    private final ContainerAuctionListing listingContainer;
    private AuctionFilter filter;
    private int currentPage = 0;
    private int totalPages = 1;
    private int totalListings = 0;
    private boolean dataLoaded = false;

    // Buttons defined on this page
    private GuiAuctionNavButton btnFilter;
    private GuiAuctionNavButton btnPageUp;
    private GuiAuctionNavButton btnPageDown;

    public GuiAuctionListing(EntityNPCInterface npc, ContainerAuctionListing container) {
        super(npc, container);
        this.listingContainer = container;
        this.filter = new AuctionFilter();
    }

    @Override
    public void initGui() {
        super.initGui();

        // Filter button (18x18 with 16x16 icon)
        btnFilter = new GuiAuctionNavButton(BTN_NAV_FILTER, guiLeft + navX, guiTop + navYFilter,
            "auction.nav.filter", ICON_FILTER);
        addButton(btnFilter);

        // Page Up button (10x10 with 8x8 icon)
        btnPageUp = new GuiAuctionNavButton(BTN_PAGE_UP, guiLeft + pageUpX, guiTop + pageBtnY,
            10, 8, "auction.page.prev", ICON_UP, SMALL_BUTTON, SMALL_BUTTON_PRESS);
        addButton(btnPageUp);

        // Page Down button (10x10 with 8x8 icon)
        btnPageDown = new GuiAuctionNavButton(BTN_PAGE_DOWN, guiLeft + pageDownX, guiTop + pageBtnY,
            10, 8, "auction.page.next", ICON_DOWN, SMALL_BUTTON, SMALL_BUTTON_PRESS);
        addButton(btnPageDown);

        updateFilterTooltip();
        requestListings();
    }

    @Override
    protected int getCurrentPage() {
        return PAGE_LISTINGS;
    }

    /** Request listings from server */
    private void requestListings() {
        AuctionActionPacket.requestListings(filter, currentPage);
    }

    /** Update pagination button visibility based on current page state */
    private void updatePaginationVisibility() {
        if (btnPageUp != null) {
            btnPageUp.setVisible(currentPage > 0);
        }
        if (btnPageDown != null) {
            btnPageDown.setVisible(currentPage < totalPages - 1);
        }
    }

    // ========== Filter Handling ==========

    private void updateFilterTooltip() {
        if (btnFilter == null) return;

        List<String> tooltip = new ArrayList<>();
        tooltip.add(EnumChatFormatting.GOLD + StatCollector.translateToLocal("auction.nav.filter"));
        tooltip.add("");

        // Sort mode
        tooltip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("auction.filter.sort") + ":");
        tooltip.add(EnumChatFormatting.WHITE + "  " + filter.getSortBy().getDisplayName());
        tooltip.add("");

        // Search term
        tooltip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("auction.filter.search") + ":");
        if (filter.hasSearchText()) {
            tooltip.add(EnumChatFormatting.GREEN + "  \"" + filter.getSearchText() + "\"");
        } else {
            tooltip.add(EnumChatFormatting.DARK_GRAY + "  " + StatCollector.translateToLocal("auction.filter.none"));
        }
        tooltip.add("");

        // Instructions
        tooltip.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocal("auction.filter.leftClick"));
        tooltip.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocal("auction.filter.rightClick"));

        btnFilter.setCustomTooltip(tooltip);
    }

    @Override
    public void mouseEvent(int mouseX, int mouseY, int mouseButton) {
        if (btnFilter != null && btnFilter.isMouseOver(mouseX, mouseY) && mouseButton == 1) {
            cycleSortMode();
        }
    }

    @Override
    public void actionPerformed(GuiButton button) {
        super.actionPerformed(button);

        switch (button.id) {
            case BTN_NAV_FILTER:
                setSubGui(new SubGuiAuctionSearch(filter.getSearchText()));
                break;
            case BTN_PAGE_UP:
                if (currentPage > 0) {
                    currentPage--;
                    requestListings();
                    NoppesUtil.clickSound();
                }
                break;
            case BTN_PAGE_DOWN:
                if (currentPage < totalPages - 1) {
                    currentPage++;
                    requestListings();
                    NoppesUtil.clickSound();
                }
                break;
        }
    }

    private void cycleSortMode() {
        EnumAuctionSort[] values = EnumAuctionSort.values();
        int next = (filter.getSortBy().ordinal() + 1) % values.length;
        filter.setSortBy(values[next]);
        updateFilterTooltip();
        currentPage = 0;
        requestListings();
        NoppesUtil.clickSound();
    }

    public void onSearchSubmit(String searchText) {
        filter.setSearchText(searchText);
        updateFilterTooltip();
        currentPage = 0;
        requestListings();
    }

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {}

    // ========== Data Handling ==========

    @Override
    public void setGuiData(NBTTagCompound compound) {
        if (compound.hasKey("ListingsData")) {
            currentPage = compound.getInteger("Page");
            totalPages = compound.getInteger("TotalPages");
            totalListings = compound.getInteger("TotalListings");

            // Load listings
            listingContainer.displayInventory.clear();
            NBTTagList list = compound.getTagList("Listings", 10);
            for (int i = 0; i < list.tagCount() && i < 45; i++) {
                AuctionListing listing = AuctionListing.fromNBT(list.getCompoundTagAt(i));
                listingContainer.displayInventory.setListing(i, listing);
            }
            dataLoaded = true;
        } else if (compound.hasKey("ListingsRefresh")) {
            requestListings();
        }
    }

    // ========== Drawing ==========

    @Override
    protected void drawAuctionContent(float partialTicks, int mouseX, int mouseY) {
        // Draw slot backgrounds
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int x = guiLeft + gridX + col * 18;
                int y = guiTop + gridY + row * 18;
                drawAuctionSlot(x, y);
            }
        }

        // Draw page indicator below pagination buttons
        drawPageIndicator();
    }

    private void drawPageIndicator() {
        if (totalPages > 1) {
            String text = (currentPage + 1) + "/" + totalPages;
            // Center between the two buttons (horizontally centered), below buttons
            int centerX = guiLeft + pageUpX + 11;
            int x = centerX - fontRendererObj.getStringWidth(text) / 2;
            fontRendererObj.drawString(text, x, guiTop + pageBtnY + 12, 0xFFFFFF);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        if (hasSubGui()) return;

        // Draw tooltips for buttons on this page
        drawListingPageTooltips(mouseX, mouseY);
    }

    private void drawListingPageTooltips(int mouseX, int mouseY) {
        List<String> tooltip = null;

        // Filter button tooltip
        if (btnFilter != null && btnFilter.isHovered()) {
            tooltip = btnFilter.getTooltipLines();
        }
        // Page up tooltip
        else if (btnPageUp != null && btnPageUp.visible && btnPageUp.isHovered()) {
            tooltip = btnPageUp.getTooltipLines();
        }
        // Page down tooltip
        else if (btnPageDown != null && btnPageDown.visible && btnPageDown.isHovered()) {
            tooltip = btnPageDown.getTooltipLines();
        }

        if (tooltip != null && !tooltip.isEmpty()) {
            GL11.glPushMatrix();
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            drawHoveringText(tooltip, mouseX - guiLeft, mouseY - guiTop, fontRendererObj);
            GL11.glPopAttrib();
            GL11.glPopMatrix();
        }
    }

    // ========== Slot Handling ==========

    @Override
    protected void handleMouseClick(Slot slot, int slotIndex, int mouseButton, int clickType) {
        // Check if clicking on a display slot with a listing
        if (slot != null && listingContainer.isDisplaySlot(slot.slotNumber)) {
            int listingIndex = slot.slotNumber - listingContainer.getDisplaySlotStart();
            AuctionListing listing = listingContainer.displayInventory.getListing(listingIndex);
            if (listing != null) {
                // Open bidding page for this listing
                AuctionActionPacket.openBidding(listing.id);
                NoppesUtil.clickSound();
            }
        }
        // Block other slot interactions
    }

    public AuctionFilter getFilter() { return filter; }
    public ContainerAuctionListing getListingContainer() { return listingContainer; }
}
