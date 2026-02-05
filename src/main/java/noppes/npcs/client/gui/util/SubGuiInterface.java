package noppes.npcs.client.gui.util;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;

public class SubGuiInterface extends GuiNPCInterface {
    public GuiScreen parent;

    // === Modern Style Option ===
    protected boolean useModernStyle = false;
    protected int modernOverlayColor = 0x80000000;
    protected int modernPanelBg = 0xFF1E1E2E;
    protected int modernPanelBorder = 0xFF4488CC;
    protected int modernHeaderBg = 0xFF2A2A3A;
    protected int modernHeaderText = 0xFFFFFF;
    protected String modernHeaderTitle = null;
    protected int modernHeaderHeight = 20;

    @Override
    public void save() {

    }

    @Override
    public void close() {
        save();

        if (parent instanceof ISubGuiListener)
            ((ISubGuiListener) parent).subGuiClosed(this);

        if (parent instanceof GuiNPCInterface)
            ((GuiNPCInterface) parent).closeSubGui(this);
        else if (parent instanceof GuiContainerNPCInterface)
            ((GuiContainerNPCInterface) parent).closeSubGui(this);
        else
            super.close();

    }

    public GuiScreen getParent() {
        if (parent instanceof SubGuiInterface)
            return ((SubGuiInterface) parent).getParent();
        return parent;
    }

    @Override
    public void drawScreen(int i, int j, float f) {
        if (useModernStyle) {
            drawModernStyle(i, j, f);
        } else {
            super.drawScreen(i, j, f);
        }
    }

    /**
     * Draw with modern styling - dark overlay, modern panel, accent border.
     */
    protected void drawModernStyle(int mouseX, int mouseY, float partialTicks) {
        // Draw semi-transparent overlay behind panel
        int margin = 20;
        int overlayLeft = Math.max(0, guiLeft - margin);
        int overlayTop = Math.max(0, guiTop - margin);
        int overlayRight = Math.min(width, guiLeft + xSize + margin);
        int overlayBottom = Math.min(height, guiTop + ySize + margin);
        Gui.drawRect(overlayLeft, overlayTop, overlayRight, overlayBottom, modernOverlayColor);

        // Draw panel border
        Gui.drawRect(guiLeft - 1, guiTop - 1, guiLeft + xSize + 1, guiTop + ySize + 1, modernPanelBorder);

        // Draw panel background
        Gui.drawRect(guiLeft, guiTop, guiLeft + xSize, guiTop + ySize, modernPanelBg);

        // Draw header if set
        if (modernHeaderTitle != null) {
            Gui.drawRect(guiLeft, guiTop, guiLeft + xSize, guiTop + modernHeaderHeight, modernHeaderBg);
            int textW = fontRendererObj.getStringWidth(modernHeaderTitle);
            int textX = guiLeft + (xSize - textW) / 2;
            int textY = guiTop + (modernHeaderHeight - fontRendererObj.FONT_HEIGHT) / 2;
            fontRendererObj.drawString(modernHeaderTitle, textX, textY, modernHeaderText);
        }

        // Draw buttons, labels, textfields, scrolls from parent
        for (Object obj : buttonList) {
            if (obj instanceof net.minecraft.client.gui.GuiButton) {
                ((net.minecraft.client.gui.GuiButton) obj).drawButton(mc, mouseX, mouseY);
            }
        }
        for (GuiNpcLabel label : labels.values()) {
            label.drawLabel(this, fontRendererObj);
        }
        for (GuiNpcTextField tf : textfields.values()) {
            tf.drawTextBox(mouseX, mouseY);
        }
        for (GuiCustomScroll scroll : scrolls.values()) {
            scroll.drawScreen(mouseX, mouseY, 0, scroll.isMouseOver(mouseX, mouseY) ? org.lwjgl.input.Mouse.getDWheel() : 0);
        }

        // Allow subclasses to draw custom content
        drawModernContent(mouseX, mouseY, partialTicks);
    }

    /**
     * Override to draw custom content in modern style.
     */
    protected void drawModernContent(int mouseX, int mouseY, float partialTicks) {
        // Override in subclasses
    }

    // === Modern Style Configuration ===

    /**
     * Enable modern styling for this SubGui.
     */
    public void setUseModernStyle(boolean modern) {
        this.useModernStyle = modern;
        if (modern) {
            drawDefaultBackground = false;
        }
    }

    /**
     * Set the header title for modern style. Pass null to disable header.
     */
    public void setModernHeaderTitle(String title) {
        this.modernHeaderTitle = title;
    }

    /**
     * Set panel colors for modern style.
     */
    public void setModernPanelColors(int background, int border) {
        this.modernPanelBg = background;
        this.modernPanelBorder = border;
    }

    /**
     * Get content area Y start (after header if present).
     */
    protected int getModernContentY() {
        return guiTop + (modernHeaderTitle != null ? modernHeaderHeight : 0);
    }

    /**
     * Get content area height (excluding header if present).
     */
    protected int getModernContentHeight() {
        return ySize - (modernHeaderTitle != null ? modernHeaderHeight : 0);
    }
}
