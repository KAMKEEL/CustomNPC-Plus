package kamkeel.npcs.client.gui.components;

import noppes.npcs.client.gui.util.ModernColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/**
 * A scrollable panel component for containing other UI elements.
 * Uses GL scissor for clipping content that overflows the panel bounds.
 */
public class ScrollPanel extends Gui {

    // Position and size
    protected int x, y, width, height;

    // Scroll state
    protected float scrollY = 0;
    protected float targetScrollY = 0;
    protected int contentHeight = 0;
    protected float scrollSpeed = 0.3f; // Lerp speed

    // Scrollbar
    protected int scrollbarWidth = 6;
    protected boolean showScrollbar = true;
    protected boolean isDraggingScrollbar = false;
    protected int dragStartY;
    protected float dragStartScroll;

    // Content elements (managed by owner, we just track height)
    protected List<ScrollPanelElement> elements = new ArrayList<>();

    // Colors
    protected int backgroundColor = ModernColors.PANEL_BG_SOLID;
    protected int scrollbarBg = ModernColors.SCROLLBAR_BG;
    protected int scrollbarThumb = ModernColors.SCROLLBAR_THUMB;
    protected int scrollbarThumbHover = ModernColors.SCROLLBAR_THUMB_HOVER;

    // State
    protected boolean hoveringScrollbar = false;

    public ScrollPanel(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    // === Position/Size ===

    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void setContentHeight(int height) {
        this.contentHeight = height;
        // Clamp scroll if content shrunk
        float maxScroll = getMaxScroll();
        if (scrollY > maxScroll) {
            scrollY = maxScroll;
            targetScrollY = maxScroll;
        }
    }

    public int getContentHeight() {
        return contentHeight;
    }

    public int getScrollY() {
        return (int) scrollY;
    }

    public void setScrollY(float scroll) {
        this.scrollY = Math.max(0, Math.min(scroll, getMaxScroll()));
        this.targetScrollY = scrollY;
    }

    // === Drawing ===

    /**
     * Begin drawing the scroll panel. Call this before drawing content.
     * Sets up GL scissor for clipping.
     */
    public void beginDraw() {
        // Update scroll animation
        if (Math.abs(scrollY - targetScrollY) > 0.5f) {
            scrollY += (targetScrollY - scrollY) * scrollSpeed;
        } else {
            scrollY = targetScrollY;
        }

        // Draw background
        drawRect(x, y, x + width, y + height, backgroundColor);

        // Set up scissor (convert to screen coordinates)
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int scale = sr.getScaleFactor();

        int scissorX = x * scale;
        int scissorY = mc.displayHeight - (y + height) * scale;
        int scissorW = (width - (needsScrollbar() ? scrollbarWidth : 0)) * scale;
        int scissorH = height * scale;

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(scissorX, scissorY, scissorW, scissorH);

        // Translate for scroll offset
        GL11.glPushMatrix();
        GL11.glTranslatef(0, -scrollY, 0);
    }

    /**
     * End drawing the scroll panel. Call this after drawing content.
     * Disables GL scissor and draws scrollbar.
     */
    public void endDraw(int mouseX, int mouseY) {
        GL11.glPopMatrix();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // Draw scrollbar if needed
        if (needsScrollbar()) {
            drawScrollbar(mouseX, mouseY);
        }
    }

    /**
     * Draw the scrollbar.
     */
    protected void drawScrollbar(int mouseX, int mouseY) {
        int sbX = x + width - scrollbarWidth;
        int sbY = y;
        int sbH = height;

        // Track background
        drawRect(sbX, sbY, sbX + scrollbarWidth, sbY + sbH, scrollbarBg);

        // Calculate thumb size and position
        float viewRatio = (float) height / contentHeight;
        int thumbH = Math.max(20, (int) (sbH * viewRatio));
        int maxThumbY = sbH - thumbH;
        int thumbY = (int) (maxThumbY * (scrollY / getMaxScroll()));

        // Check hover
        hoveringScrollbar = mouseX >= sbX && mouseX < sbX + scrollbarWidth &&
                           mouseY >= sbY + thumbY && mouseY < sbY + thumbY + thumbH;

        // Draw thumb
        int thumbColor = (hoveringScrollbar || isDraggingScrollbar) ? scrollbarThumbHover : scrollbarThumb;
        drawRect(sbX + 1, sbY + thumbY, sbX + scrollbarWidth - 1, sbY + thumbY + thumbH, thumbColor);
    }

    // === Scrolling ===

    public boolean needsScrollbar() {
        return showScrollbar && contentHeight > height;
    }

    public float getMaxScroll() {
        return Math.max(0, contentHeight - height);
    }

    /**
     * Handle mouse scroll wheel.
     * @param delta Scroll wheel delta (positive = scroll up)
     */
    public void mouseScrolled(int delta) {
        if (!needsScrollbar()) return;

        float scrollAmount = delta * 20;
        targetScrollY = Math.max(0, Math.min(targetScrollY - scrollAmount, getMaxScroll()));
    }

    /**
     * Handle mouse click. Returns true if handled.
     */
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (button != 0) return false;

        // Check scrollbar click
        if (needsScrollbar()) {
            int sbX = x + width - scrollbarWidth;
            if (mouseX >= sbX && mouseX < sbX + scrollbarWidth &&
                mouseY >= y && mouseY < y + height) {

                // Calculate thumb position
                float viewRatio = (float) height / contentHeight;
                int thumbH = Math.max(20, (int) (height * viewRatio));
                int maxThumbY = height - thumbH;
                int thumbY = (int) (maxThumbY * (scrollY / getMaxScroll()));

                if (mouseY >= y + thumbY && mouseY < y + thumbY + thumbH) {
                    // Clicked on thumb - start dragging
                    isDraggingScrollbar = true;
                    dragStartY = mouseY;
                    dragStartScroll = scrollY;
                } else {
                    // Clicked on track - jump to position
                    float clickRatio = (float) (mouseY - y - thumbH / 2) / (height - thumbH);
                    clickRatio = Math.max(0, Math.min(1, clickRatio));
                    targetScrollY = clickRatio * getMaxScroll();
                }
                return true;
            }
        }

        return false;
    }

    /**
     * Handle mouse drag.
     */
    public void mouseDragged(int mouseX, int mouseY) {
        if (isDraggingScrollbar) {
            int deltaY = mouseY - dragStartY;
            float viewRatio = (float) height / contentHeight;
            int thumbH = Math.max(20, (int) (height * viewRatio));
            int maxThumbY = height - thumbH;

            float scrollDelta = (deltaY / (float) maxThumbY) * getMaxScroll();
            scrollY = Math.max(0, Math.min(dragStartScroll + scrollDelta, getMaxScroll()));
            targetScrollY = scrollY;
        }
    }

    /**
     * Handle mouse release.
     */
    public void mouseReleased(int mouseX, int mouseY) {
        isDraggingScrollbar = false;
    }

    /**
     * Check if a point is inside the panel content area.
     */
    public boolean isInside(int mouseX, int mouseY) {
        return mouseX >= x && mouseX < x + width &&
               mouseY >= y && mouseY < y + height;
    }

    /**
     * Check if a point is inside the content area (excluding scrollbar).
     */
    public boolean isInsideContent(int mouseX, int mouseY) {
        int contentW = width - (needsScrollbar() ? scrollbarWidth : 0);
        return mouseX >= x && mouseX < x + contentW &&
               mouseY >= y && mouseY < y + height;
    }

    /**
     * Transform a screen Y coordinate to content Y coordinate.
     */
    public int toContentY(int screenY) {
        return (int) (screenY + scrollY);
    }

    /**
     * Transform a content Y coordinate to screen Y coordinate.
     */
    public int toScreenY(int contentY) {
        return (int) (contentY - scrollY);
    }

    // === Getters ===

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getContentWidth() { return width - (needsScrollbar() ? scrollbarWidth : 0); }

    // === Simple element interface ===

    public interface ScrollPanelElement {
        int getHeight();
        void draw(int x, int y, int width, int mouseX, int mouseY);
    }
}
