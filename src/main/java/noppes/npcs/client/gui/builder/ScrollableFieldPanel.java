package noppes.npcs.client.gui.builder;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.client.gui.components.ModernDropdown;
import kamkeel.npcs.client.gui.components.ScrollPanel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import java.util.List;

/**
 * Self-contained scrollable field panel that wraps {@link ScrollPanel} + {@link ModernFieldPanel}.
 * <p>
 * Handles scissor clipping, scroll coordinate translation, dropdown z-order,
 * and text area mouse delegation automatically.
 * <p>
 * Usage:
 * <pre>
 * ScrollableFieldPanel panel = new ScrollableFieldPanel(x, y, width, height);
 * panel.setFields(fieldDefs);
 * panel.setListener(listener);
 *
 * // In draw:
 * panel.draw(mouseX, mouseY);
 *
 * // Events:
 * panel.mouseClicked(mouseX, mouseY, button);
 * panel.keyTyped(c, keyCode);
 * panel.mouseScrolled(delta);
 * panel.mouseDragged(mouseX, mouseY);
 * panel.mouseReleased(mouseX, mouseY);
 * panel.updateScreen();
 * </pre>
 */
@SideOnly(Side.CLIENT)
public class ScrollableFieldPanel {

    protected ScrollPanel scrollPanel;
    protected ModernFieldPanel fieldPanel;

    // Layout
    protected int x, y, width, height;
    protected int padding = 4;

    public ScrollableFieldPanel(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.scrollPanel = new ScrollPanel(x, y, width, height);
        this.scrollPanel.setBackgroundColor(0); // Transparent
        this.fieldPanel = new ModernFieldPanel();
    }

    // === Setup ===

    public void setFields(List<FieldDef> fields) {
        fieldPanel.setFields(fields);
    }

    public void setListener(ModernFieldPanelListener listener) {
        fieldPanel.setListener(listener);
    }

    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        scrollPanel.setBounds(x, y, width, height);
    }

    public void resetScroll() {
        scrollPanel.resetScroll();
    }

    /**
     * Set available height for fillHeight() text areas.
     */
    public void setAvailableHeight(int height) {
        fieldPanel.setAvailableHeight(height);
    }

    // === Drawing ===

    public void draw(int mouseX, int mouseY) {
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;

        // Sync dropdown screen positions BEFORE scissor
        updateDropdownScreenPositions();

        // Begin scissor + scroll translation
        scrollPanel.beginDraw();

        // Draw fields
        int contentX = x + padding;
        int contentW = width - padding * 2 - getScrollbarWidth();
        int startY = y + padding;
        int contentMouseY = scrollPanel.toContentY(mouseY);

        int contentHeight = fieldPanel.draw(contentX, contentW, startY, mouseX, contentMouseY, fr);
        scrollPanel.setContentHeight(contentHeight + padding);

        // End scissor, draw scrollbar
        scrollPanel.endDraw(mouseX, mouseY);

        // Draw expanded dropdowns in screen space (above scissor)
        drawExpandedDropdownsScreenSpace(mouseX, mouseY);
    }

    protected void updateDropdownScreenPositions() {
        for (ModernDropdown dd : fieldPanel.getDropdowns()) {
            int screenY = scrollPanel.toScreenY(dd.getY());
            dd.setScreenPosition(dd.getX(), screenY);
        }
    }

    protected void drawExpandedDropdownsScreenSpace(int mouseX, int mouseY) {
        for (ModernDropdown dd : fieldPanel.getDropdowns()) {
            if (dd.isExpanded()) {
                dd.drawOverlayScreenSpace(mouseX, mouseY);
            }
        }
    }

    protected int getScrollbarWidth() {
        return scrollPanel.needsScrollbar() ? 6 : 0;
    }

    // === Event Handling ===

    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        // Check expanded dropdowns in SCREEN space first - with data write-back
        if (fieldPanel.handleExpandedDropdownScreenClick(mouseX, mouseY, button)) {
            return true;
        }

        // Check scrollbar
        if (scrollPanel.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        // Delegate to field panel with scroll-adjusted Y
        int adjMouseY = scrollPanel.toContentY(mouseY);
        return fieldPanel.mouseClicked(mouseX, adjMouseY, button);
    }

    public boolean keyTyped(char c, int keyCode) {
        return fieldPanel.keyTyped(c, keyCode);
    }

    public void mouseScrolled(int delta) {
        scrollPanel.mouseScrolled(delta);
    }

    public void mouseDragged(int mouseX, int mouseY) {
        scrollPanel.mouseDragged(mouseX, mouseY);
    }

    public void mouseReleased(int mouseX, int mouseY) {
        scrollPanel.mouseReleased(mouseX, mouseY);
    }

    public void updateScreen() {
        fieldPanel.updateScreen();
    }

    // === Accessors ===

    public ModernFieldPanel getFieldPanel() {
        return fieldPanel;
    }

    public ScrollPanel getScrollPanel() {
        return scrollPanel;
    }

    public List<ModernDropdown> getDropdowns() {
        return fieldPanel.getDropdowns();
    }

    public void refresh() {
        fieldPanel.refresh();
    }

    public boolean isInside(int mouseX, int mouseY) {
        return scrollPanel.isInside(mouseX, mouseY);
    }
}
