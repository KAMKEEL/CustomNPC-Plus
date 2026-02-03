package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.ArrayList;
import java.util.List;

/**
 * Configurable textured button for auction GUI.
 * Supports custom button sizes, icon sizes, and textures.
 */
public class GuiAuctionNavButton extends GuiNpcButton {
    // Default auction button textures (18x18)
    private static final ResourceLocation DEFAULT_NORMAL = new ResourceLocation("customnpcs", "textures/gui/auction/button.png");
    private static final ResourceLocation DEFAULT_PRESSED = new ResourceLocation("customnpcs", "textures/gui/auction/button_press.png");

    // Textures
    private ResourceLocation textureNormal;
    private ResourceLocation texturePressed;
    private ResourceLocation iconTexture;
    private ItemStack iconItem;

    // Dimensions
    private int buttonSize;
    private int iconSize;

    // State
    private boolean selected;
    private boolean isToggle; // Toggle buttons can be clicked when selected, nav buttons cannot
    private final String tooltipKey;
    private List<String> customTooltip;

    /**
     * Create a button with default 18x18 size and 16x16 icon using default textures.
     */
    public GuiAuctionNavButton(int id, int x, int y, String tooltipKey, ResourceLocation iconTexture) {
        this(id, x, y, 18, 16, tooltipKey, iconTexture, DEFAULT_NORMAL, DEFAULT_PRESSED);
    }

    /**
     * Create a button with default 18x18 size and 16x16 icon with an item icon.
     */
    public GuiAuctionNavButton(int id, int x, int y, String tooltipKey, ItemStack iconItem) {
        super(id, x, y, 18, 18, "");
        this.tooltipKey = tooltipKey;
        this.iconTexture = null;
        this.iconItem = iconItem;
        this.buttonSize = 18;
        this.iconSize = 16;
        this.textureNormal = DEFAULT_NORMAL;
        this.texturePressed = DEFAULT_PRESSED;
        this.selected = false;
        this.customTooltip = null;
    }

    /**
     * Create a fully customizable button.
     *
     * @param id Button ID
     * @param x X position
     * @param y Y position
     * @param buttonSize Size of the button background texture (e.g., 18 for 18x18, 10 for 10x10)
     * @param iconSize Size of the icon texture (e.g., 16 for 16x16, 8 for 8x8)
     * @param tooltipKey Lang key for tooltip
     * @param iconTexture Icon texture to draw inside the button
     * @param textureNormal Background texture for normal state
     * @param texturePressed Background texture for pressed/hover state
     */
    public GuiAuctionNavButton(int id, int x, int y, int buttonSize, int iconSize, String tooltipKey,
                               ResourceLocation iconTexture, ResourceLocation textureNormal, ResourceLocation texturePressed) {
        super(id, x, y, buttonSize, buttonSize, "");
        this.tooltipKey = tooltipKey;
        this.iconTexture = iconTexture;
        this.iconItem = null;
        this.buttonSize = buttonSize;
        this.iconSize = iconSize;
        this.textureNormal = textureNormal;
        this.texturePressed = texturePressed;
        this.selected = false;
        this.customTooltip = null;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    /**
     * Set whether this is a toggle button.
     * Toggle buttons can be clicked when selected (to toggle off).
     * Nav buttons cannot be clicked when selected.
     */
    public GuiAuctionNavButton setToggle(boolean toggle) {
        this.isToggle = toggle;
        return this;
    }

    public boolean isHovered() {
        return field_146123_n;
    }

    /**
     * Set a custom multi-line tooltip. If null, uses the default tooltipKey translation.
     */
    public void setCustomTooltip(List<String> tooltip) {
        this.customTooltip = tooltip;
    }

    /**
     * Get the tooltip lines for display.
     */
    public List<String> getTooltipLines() {
        if (customTooltip != null && !customTooltip.isEmpty()) {
            return customTooltip;
        }
        List<String> lines = new ArrayList<>();
        if (tooltipKey != null && !tooltipKey.isEmpty()) {
            lines.add(StatCollector.translateToLocal(tooltipKey));
        }
        return lines;
    }

    public String getTooltipText() {
        return tooltipKey != null ? StatCollector.translateToLocal(tooltipKey) : "";
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (!this.visible) {
            return;
        }

        // Check if mouse is hovering
        boolean hovered = mouseX >= xPosition && mouseX < xPosition + width
            && mouseY >= yPosition && mouseY < yPosition + height;
        field_146123_n = hovered;

        // Draw button background
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        if (selected || hovered) {
            mc.getTextureManager().bindTexture(texturePressed);
        } else {
            mc.getTextureManager().bindTexture(textureNormal);
        }
        Gui.func_146110_a(xPosition, yPosition, 0, 0, buttonSize, buttonSize, buttonSize, buttonSize);

        // Calculate icon offset to center it
        int iconOffset = (buttonSize - iconSize) / 2;
        int iconX = xPosition + iconOffset;
        int iconY = yPosition + iconOffset;

        if (iconItem != null) {
            // Render item
            RenderHelper.enableGUIStandardItemLighting();
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            RenderItem itemRenderer = RenderItem.getInstance();
            itemRenderer.renderItemAndEffectIntoGUI(mc.fontRenderer, mc.renderEngine, iconItem, iconX, iconY);
            GL11.glDisable(GL11.GL_LIGHTING);
            RenderHelper.disableStandardItemLighting();
        } else if (iconTexture != null) {
            // Render texture icon
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            mc.getTextureManager().bindTexture(iconTexture);
            Gui.func_146110_a(iconX, iconY, 0, 0, iconSize, iconSize, iconSize, iconSize);
        }
    }

    @Override
    public boolean mousePressed(Minecraft minecraft, int mouseX, int mouseY) {
        // Don't allow clicking if selected (for nav buttons, not toggle buttons)
        if (selected && !isToggle) {
            return false;
        }
        return enabled && visible && mouseX >= xPosition && mouseX < xPosition + width
            && mouseY >= yPosition && mouseY < yPosition + height;
    }

    /**
     * Check if this button contains the given mouse coordinates.
     */
    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= xPosition && mouseX < xPosition + width
            && mouseY >= yPosition && mouseY < yPosition + height;
    }
}
