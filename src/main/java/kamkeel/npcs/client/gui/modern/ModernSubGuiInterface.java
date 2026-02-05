package kamkeel.npcs.client.gui.modern;

import noppes.npcs.client.gui.util.ModernColors;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.SubGuiInterface;

/**
 * Base class for modern styled SubGuis.
 * Features:
 * - Semi-transparent dark overlay over parent screen
 * - Modern dark panel with accent border
 * - No texture-based background
 */
public class ModernSubGuiInterface extends SubGuiInterface {

    // Overlay color (semi-transparent black)
    protected int overlayColor = 0x80000000;

    // Panel colors
    protected int panelBg = ModernColors.PANEL_BG_SOLID;
    protected int panelBorder = ModernColors.ACCENT_BLUE;
    protected int panelBorderWidth = 1;

    // Header (optional)
    protected String headerTitle = null;
    protected int headerHeight = 20;
    protected int headerBg = ModernColors.SECTION_HEADER_BG;
    protected int headerText = ModernColors.TEXT_WHITE;

    public ModernSubGuiInterface() {
        // Disable default background drawing
        drawDefaultBackground = false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Draw semi-transparent overlay over entire screen
        Gui.drawRect(0, 0, width, height, overlayColor);

        // Draw modern panel background
        drawModernPanel();

        // Draw header if set
        if (headerTitle != null) {
            drawHeader();
        }

        // Draw content (buttons, labels, etc.)
        drawButtonsModern(mouseX, mouseY);
        drawLabelsModern();

        // Draw any custom content
        drawContent(mouseX, mouseY, partialTicks);
    }

    /**
     * Draw the modern panel background.
     */
    protected void drawModernPanel() {
        // Draw border
        Gui.drawRect(guiLeft - panelBorderWidth, guiTop - panelBorderWidth,
                    guiLeft + xSize + panelBorderWidth, guiTop + ySize + panelBorderWidth,
                    panelBorder);

        // Draw panel background
        Gui.drawRect(guiLeft, guiTop, guiLeft + xSize, guiTop + ySize, panelBg);
    }

    /**
     * Draw the header bar if headerTitle is set.
     */
    protected void drawHeader() {
        // Header background
        Gui.drawRect(guiLeft, guiTop, guiLeft + xSize, guiTop + headerHeight, headerBg);

        // Header text (centered)
        int textW = fontRendererObj.getStringWidth(headerTitle);
        int textX = guiLeft + (xSize - textW) / 2;
        int textY = guiTop + (headerHeight - fontRendererObj.FONT_HEIGHT) / 2;
        fontRendererObj.drawString(headerTitle, textX, textY, headerText);
    }

    /**
     * Draw buttons with modern styling.
     */
    protected void drawButtonsModern(int mouseX, int mouseY) {
        for (Object obj : buttonList) {
            if (obj instanceof GuiButton) {
                ((GuiButton) obj).drawButton(mc, mouseX, mouseY);
            }
        }
    }

    /**
     * Draw labels with modern styling.
     */
    protected void drawLabelsModern() {
        for (Object obj : labels.values()) {
            if (obj instanceof GuiNpcLabel) {
                ((GuiNpcLabel) obj).drawLabel(this, fontRendererObj);
            }
        }
    }

    /**
     * Override this method to draw custom content.
     * Called after panel and header, before subgui.
     */
    protected void drawContent(int mouseX, int mouseY, float partialTicks) {
        // Override in subclasses
    }

    /**
     * Override drawBackground to do nothing - we handle it in drawScreen.
     */
    @Override
    protected void drawBackground() {
        // Don't draw default textured background
    }

    // === Configuration Methods ===

    /**
     * Set the header title. Pass null to disable header.
     */
    public void setHeaderTitle(String title) {
        this.headerTitle = title;
    }

    /**
     * Set panel colors.
     */
    public void setPanelColors(int background, int border) {
        this.panelBg = background;
        this.panelBorder = border;
    }

    /**
     * Set overlay color (screen darkening).
     */
    public void setOverlayColor(int color) {
        this.overlayColor = color;
    }

    /**
     * Get content area Y start (after header if present).
     */
    protected int getContentY() {
        return guiTop + (headerTitle != null ? headerHeight : 0);
    }

    /**
     * Get content area height (excluding header if present).
     */
    protected int getContentHeight() {
        return ySize - (headerTitle != null ? headerHeight : 0);
    }
}
