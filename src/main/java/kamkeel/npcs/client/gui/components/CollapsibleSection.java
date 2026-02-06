package kamkeel.npcs.client.gui.components;

import noppes.npcs.client.gui.util.ModernColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * A collapsible section header that can expand/collapse its content.
 * Used in the editor panel to organize settings into groups.
 */
public class CollapsibleSection extends Gui {

    // Identity
    protected int id;
    protected String title;

    // State
    protected boolean expanded = true;
    protected boolean hovered = false;

    // Layout
    protected int x, y, width;
    protected int headerHeight = 18;
    protected int contentHeight = 0; // Set by owner based on content
    protected int indent = 8;
    protected int spacing = 4;
    protected int topPadding = 4; // Padding between header and first content row

    // Remove button
    protected Runnable onRemove = null;
    protected boolean hoveringRemoveBtn = false;
    protected int removeBtnSize = 14;

    // Icon
    protected static final ResourceLocation X_ICON = new ResourceLocation("customnpcs", "textures/gui/auction/x_icon.png");

    // Colors
    protected int headerBg = ModernColors.SECTION_HEADER_BG;
    protected int headerBgHover = ModernColors.SECTION_HEADER_HOVER;
    protected int headerText = ModernColors.TEXT_LIGHT;
    protected int arrowColor = ModernColors.SECTION_ARROW;

    // Animation
    protected float expandProgress = 1.0f; // 0 = collapsed, 1 = expanded
    protected float animSpeed = 8.0f; // Units per second
    protected long lastDrawTime = 0;

    public CollapsibleSection(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public CollapsibleSection(int id, String title, boolean expanded) {
        this.id = id;
        this.title = title;
        this.expanded = expanded;
        this.expandProgress = expanded ? 1.0f : 0.0f;
    }

    // === Layout ===

    public void setPosition(int x, int y, int width) {
        this.x = x;
        this.y = y;
        this.width = width;
    }

    public void setContentHeight(int height) {
        this.contentHeight = height;
    }

    /**
     * Get total height of this section (header + content if expanded).
     */
    public int getTotalHeight() {
        if (!expanded && expandProgress <= 0.01f) {
            return headerHeight;
        }
        return headerHeight + topPadding + (int)(contentHeight * expandProgress) + spacing;
    }

    /**
     * Get the Y position where content should start (after header + padding).
     */
    public int getContentY() {
        return y + headerHeight + topPadding;
    }

    /**
     * Get the X position for indented content.
     */
    public int getContentX() {
        return x + indent;
    }

    /**
     * Get the width available for content.
     */
    public int getContentWidth() {
        return width - indent;
    }

    // === Drawing ===

    /**
     * Draw the section header. Content is drawn by the owner.
     * @return true if content should be drawn (expanded)
     */
    public boolean draw(int mouseX, int mouseY) {
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;

        // Update animation with proper delta time
        long currentTime = System.currentTimeMillis();
        float deltaTime = lastDrawTime == 0 ? 0.016f : (currentTime - lastDrawTime) / 1000.0f;
        lastDrawTime = currentTime;

        float targetProgress = expanded ? 1.0f : 0.0f;
        if (Math.abs(expandProgress - targetProgress) > 0.001f) {
            float step = animSpeed * deltaTime;
            if (expandProgress < targetProgress) {
                expandProgress = Math.min(targetProgress, expandProgress + step);
            } else {
                expandProgress = Math.max(targetProgress, expandProgress - step);
            }
        } else {
            expandProgress = targetProgress;
        }

        // Check hover
        hovered = mouseX >= x && mouseX < x + width &&
                  mouseY >= y && mouseY < y + headerHeight;

        // Draw header background
        int bgColor = hovered ? headerBgHover : headerBg;
        drawRect(x, y, x + width, y + headerHeight, bgColor);

        // Draw expand/collapse arrow
        String arrow = expanded ? "\u25BC" : "\u25B6"; // ▼ or ▶
        fr.drawString(arrow, x + 4, y + (headerHeight - fr.FONT_HEIGHT) / 2, arrowColor);

        // Draw title
        int titleX = x + 14;
        int titleEndX = x + width;

        // Draw remove button if present
        if (onRemove != null) {
            int btnX = x + width - removeBtnSize - 4;
            int btnY = y + (headerHeight - removeBtnSize) / 2;
            titleEndX = btnX - 4;

            hoveringRemoveBtn = mouseX >= btnX && mouseX < btnX + removeBtnSize &&
                               mouseY >= btnY && mouseY < btnY + removeBtnSize;

            int btnBg = hoveringRemoveBtn ? 0xFFCC4444 : 0xFFAA3333;
            drawRect(btnX, btnY, btnX + removeBtnSize, btnY + removeBtnSize, btnBg);
            Minecraft.getMinecraft().getTextureManager().bindTexture(X_ICON);
            GL11.glColor4f(1f, 1f, 1f, 1f);
            GL11.glEnable(GL11.GL_BLEND);
            int iconSize = removeBtnSize - 4;
            int ix = btnX + 2;
            int iy = btnY + 2;
            Tessellator t = Tessellator.instance;
            t.startDrawingQuads();
            t.addVertexWithUV(ix, iy + iconSize, zLevel, 0, 1);
            t.addVertexWithUV(ix + iconSize, iy + iconSize, zLevel, 1, 1);
            t.addVertexWithUV(ix + iconSize, iy, zLevel, 1, 0);
            t.addVertexWithUV(ix, iy, zLevel, 0, 0);
            t.draw();
            GL11.glDisable(GL11.GL_BLEND);
        }

        // Draw title (truncated if needed)
        String displayTitle = fr.trimStringToWidth(title, titleEndX - titleX);
        fr.drawString(displayTitle, titleX, y + (headerHeight - fr.FONT_HEIGHT) / 2, headerText);

        // Return whether content should be drawn
        return expanded || expandProgress > 0.01f;
    }

    /**
     * Get the current expand progress for animation purposes.
     * Can be used to clip content drawing during collapse animation.
     */
    public float getExpandProgress() {
        return expandProgress;
    }

    // === Interaction ===

    /**
     * Handle mouse click. Returns true if handled.
     */
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (button != 0) return false;

        // Check if header was clicked
        if (mouseX >= x && mouseX < x + width &&
            mouseY >= y && mouseY < y + headerHeight) {

            // Check remove button first
            if (onRemove != null && hoveringRemoveBtn) {
                onRemove.run();
                return true;
            }

            // Otherwise toggle expand
            toggle();
            return true;
        }

        return false;
    }

    /**
     * Toggle expanded state.
     */
    public void toggle() {
        expanded = !expanded;
    }

    /**
     * Set expanded state.
     */
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
        // Instant state change for programmatic changes
        expandProgress = expanded ? 1.0f : 0.0f;
    }

    // === Getters/Setters ===

    public int getId() { return id; }
    public String getTitle() { return title; }
    public boolean isExpanded() { return expanded; }
    public boolean isHovered() { return hovered; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeaderHeight() { return headerHeight; }
    public int getIndent() { return indent; }

    public CollapsibleSection setTitle(String title) {
        this.title = title;
        return this;
    }

    public CollapsibleSection setHeaderHeight(int height) {
        this.headerHeight = height;
        return this;
    }

    public CollapsibleSection setIndent(int indent) {
        this.indent = indent;
        return this;
    }

    public CollapsibleSection setOnRemove(Runnable action) {
        this.onRemove = action;
        return this;
    }

    public CollapsibleSection setColors(int headerBg, int headerText, int arrow) {
        this.headerBg = headerBg;
        this.headerBgHover = ModernColors.lighten(headerBg, 0.1f);
        this.headerText = headerText;
        this.arrowColor = arrow;
        return this;
    }
}
