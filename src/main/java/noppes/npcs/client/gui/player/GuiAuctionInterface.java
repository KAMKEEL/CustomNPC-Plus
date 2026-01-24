package noppes.npcs.client.gui.player;

import kamkeel.npcs.network.packets.player.AuctionActionPacket;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.gui.util.GuiAuctionNavButton;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.containers.ContainerAuction;
import noppes.npcs.entity.EntityNPCInterface;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.List;

/**
 * Base GUI for all auction interfaces.
 * 256x256 texture with player inventory at bottom.
 */
public abstract class GuiAuctionInterface extends GuiContainerNPCInterface {
    // Textures
    protected static final ResourceLocation AUCTION_BACKGROUND = new ResourceLocation("customnpcs", "textures/gui/auction/auction.png");
    protected static final ResourceLocation AUCTION_SLOT = new ResourceLocation("customnpcs", "textures/gui/auction/auction_slot.png");

    // Navigation icons
    protected static final ResourceLocation ICON_LISTINGS = new ResourceLocation("customnpcs", "textures/items/npcAncientScroll.png");
    protected static final ResourceLocation ICON_SELL = new ResourceLocation("customnpcs", "textures/gui/auction/sell.png");
    protected static final ResourceLocation ICON_CLAIMS = new ResourceLocation("customnpcs", "textures/gui/auction/my_auctions.png");
    protected static final ResourceLocation ICON_FILTER = new ResourceLocation("customnpcs", "textures/gui/auction/search.png");

    // Page constants
    public static final int PAGE_LISTINGS = 0;
    public static final int PAGE_SELL = 1;
    public static final int PAGE_CLAIMS = 2;

    // Navigation button IDs
    protected static final int BTN_NAV_LISTINGS = 100;
    protected static final int BTN_NAV_SELL = 101;
    protected static final int BTN_NAV_CLAIMS = 102;
    protected static final int BTN_NAV_FILTER = 103;

    // Navigation positions - non-final for testing/positioning
    protected int navX = 35;
    protected int navYListings = 46;
    protected int navYSell = 67;
    protected int navYClaims = 88;

    protected final ContainerAuction auctionContainer;
    protected GuiAuctionNavButton btnListings;
    protected GuiAuctionNavButton btnSell;
    protected GuiAuctionNavButton btnClaims;

    public GuiAuctionInterface(EntityNPCInterface npc, ContainerAuction container) {
        super(npc, container);
        this.auctionContainer = container;
        this.xSize = 256;
        this.ySize = 256;
        this.closeOnEsc = true;
        this.title = "";
    }

    @Override
    public void initGui() {
        super.initGui();
        initNavigationButtons();
    }

    /** Initialize navigation buttons */
    protected void initNavigationButtons() {
        int page = getCurrentPage();

        btnListings = new GuiAuctionNavButton(BTN_NAV_LISTINGS, guiLeft + navX, guiTop + navYListings,
            "auction.nav.listings", ICON_LISTINGS);
        btnListings.setSelected(page == PAGE_LISTINGS);
        addButton(btnListings);

        btnSell = new GuiAuctionNavButton(BTN_NAV_SELL, guiLeft + navX, guiTop + navYSell,
            "auction.nav.sell", ICON_SELL);
        btnSell.setSelected(page == PAGE_SELL);
        addButton(btnSell);

        btnClaims = new GuiAuctionNavButton(BTN_NAV_CLAIMS, guiLeft + navX, guiTop + navYClaims,
            "auction.nav.trades", ICON_CLAIMS);
        btnClaims.setSelected(page == PAGE_CLAIMS);
        addButton(btnClaims);
    }

    /** Returns current page constant - implement in subclass */
    protected abstract int getCurrentPage();

    @Override
    public void actionPerformed(GuiButton button) {
        super.actionPerformed(button);
        int page = getCurrentPage();
        if (button.id == BTN_NAV_LISTINGS && page != PAGE_LISTINGS) {
            AuctionActionPacket.openPage(PAGE_LISTINGS);
        } else if (button.id == BTN_NAV_SELL && page != PAGE_SELL) {
            AuctionActionPacket.openPage(PAGE_SELL);
        } else if (button.id == BTN_NAV_CLAIMS && page != PAGE_CLAIMS) {
            AuctionActionPacket.openPage(PAGE_CLAIMS);
        }
    }

    // ========== Background Drawing ==========

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        drawWorldBackground(0);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(AUCTION_BACKGROUND);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        RenderHelper.enableGUIStandardItemLighting();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glEnable(GL11.GL_LIGHTING);

        drawAuctionContent(partialTicks, mouseX, mouseY);

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        RenderHelper.enableStandardItemLighting();

        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        if (!hasSubGui()) {
            drawNavButtonTooltips(mouseX, mouseY);
        }
    }

    /** Draw navigation button tooltips */
    protected void drawNavButtonTooltips(int mouseX, int mouseY) {
        List<String> tooltip = null;
        if (btnListings != null && btnListings.isHovered()) tooltip = btnListings.getTooltipLines();
        else if (btnSell != null && btnSell.isHovered()) tooltip = btnSell.getTooltipLines();
        else if (btnClaims != null && btnClaims.isHovered()) tooltip = btnClaims.getTooltipLines();

        if (tooltip != null) {
            GL11.glPushMatrix();
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            drawHoveringText(tooltip, mouseX - guiLeft, mouseY - guiTop, fontRendererObj);
            GL11.glPopAttrib();
            GL11.glPopMatrix();
        }
    }

    // ========== Slot Drawing Utilities ==========

    /** Draw auction slot background (18x18) */
    protected void drawAuctionSlot(int x, int y) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(AUCTION_SLOT);
        Gui.func_146110_a(x, y, 0, 0, 18, 18, 18, 18);
    }

    /** Draw colored overlay on slot (ARGB format) */
    protected void drawColoredOverlay(int x, int y, int color) {
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        Gui.drawRect(x + 1, y + 1, x + 17, y + 17, color);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_LIGHTING);
    }

    /** Draw dark overlay for unavailable slots */
    protected void drawDarkenedOverlay(int x, int y) {
        drawColoredOverlay(x, y, 0x80000000);
    }

    /** Draw icon texture on slot (16x16) */
    protected void drawIconOverlay(int x, int y, ResourceLocation icon) {
        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(icon);
        Gui.func_146110_a(x + 1, y + 1, 0, 0, 16, 16, 16, 16);
        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }

    /** Subclasses implement this to draw specific content */
    protected abstract void drawAuctionContent(float partialTicks, int mouseX, int mouseY);

    // ========== Formatting Utilities ==========

    /** Format currency with commas (e.g., 1000 -> "1,000") */
    protected String formatCurrency(long amount) {
        if (amount < 1000) return String.valueOf(amount);
        StringBuilder sb = new StringBuilder();
        String str = String.valueOf(amount);
        int count = 0;
        for (int i = str.length() - 1; i >= 0; i--) {
            if (count > 0 && count % 3 == 0) sb.insert(0, ',');
            sb.insert(0, str.charAt(i));
            count++;
        }
        return sb.toString();
    }

    /** Format time remaining (e.g., "2d 5h 30m") */
    protected String formatTimeRemaining(long ms) {
        if (ms <= 0) return "Ended";

        long seconds = (ms / 1000) % 60;
        long minutes = (ms / 60000) % 60;
        long hours = (ms / 3600000) % 24;
        long days = ms / 86400000;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0 || days > 0) sb.append(hours).append("h ");
        if (minutes > 0 || hours > 0 || days > 0) sb.append(minutes).append("m");
        else sb.append(seconds).append("s");
        return sb.toString();
    }

    @Override
    public void save() {
        // Override in subclasses if needed
    }
}
