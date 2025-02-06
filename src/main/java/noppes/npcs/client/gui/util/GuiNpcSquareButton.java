package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderHelper;
import org.lwjgl.opengl.GL11;

import static noppes.npcs.client.gui.player.inventory.GuiCNPCInventory.specialIcons;

public class GuiNpcSquareButton extends GuiNpcButton {

    // Fixed height for the text button portion.
    public static final int TEXT_BUTTON_HEIGHT = 20;
    // Overall square side length (set via layout).
    public int size;
    // The icon area height is the overall size minus TEXT_BUTTON_HEIGHT.
    public int iconAreaHeight;
    // Background color (ARGB) for the icon area.
    protected int backgroundColor;
    // The child text button.
    public GuiNpcButton textButton;

    /**
     * Constructor.
     *
     * @param id              Button id.
     * @param x               Initial x (placeholder; layout will override).
     * @param y               Initial y (placeholder; layout will override).
     * @param size            Overall square side (placeholder; layout will override).
     * @param label           Button text.
     * @param backgroundColor ARGB color for the icon area.
     */
    public GuiNpcSquareButton(int id, int x, int y, int size, String label, int backgroundColor) {
        // The composite button’s width and height are both "size".
        super(id, x, y, size, size, label);
        this.size = size;
        this.iconAreaHeight = size - TEXT_BUTTON_HEIGHT;
        this.backgroundColor = backgroundColor;
        // Create the child text button.
        this.textButton = new GuiNpcButton(id, x, y + iconAreaHeight, size, TEXT_BUTTON_HEIGHT, label);
    }

    /**
     * Update the composite button’s position and overall size.
     */
    public void updatePositionAndSize(int x, int y, int newSize) {
        this.xPosition = x;
        this.yPosition = y;
        this.size = newSize;
        this.width = newSize;
        this.height = newSize;
        this.iconAreaHeight = newSize - TEXT_BUTTON_HEIGHT;
        this.textButton.xPosition = x;
        this.textButton.yPosition = y + this.iconAreaHeight;
        this.textButton.width = newSize;
        this.textButton.height = TEXT_BUTTON_HEIGHT;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (!this.visible)
            return;

        // --- Draw the icon area background ---
        Gui.drawRect(xPosition, yPosition, xPosition + size, yPosition + iconAreaHeight, backgroundColor);

        // --- Draw the icon ---
        if (iconTexture != null) {
            mc.getTextureManager().bindTexture(iconTexture);
            GL11.glPushMatrix();
            // Disable lighting so the icon is drawn at full brightness.
            RenderHelper.disableStandardItemLighting();
            GL11.glColor4f(1F, 1F, 1F, 1F);
            // Compute center of icon area.
            float centerX = xPosition + size / 2.0F;
            float centerY = yPosition + iconAreaHeight / 2.0F;
            GL11.glTranslatef(centerX, centerY, 0F);
            // Scale so that original icon height maps to the icon area height.
            float scale = (float) iconAreaHeight / (float) iconHeight; // iconHeight should be 24 now.
            GL11.glScalef(scale, scale, 1F);
            GL11.glTranslatef(-iconWidth / 2.0F, -iconHeight / 2.0F, 0F);
            this.drawTexturedModalRect(0, 0, iconPosX, iconPosY, iconWidth, iconHeight);
            GL11.glPopMatrix();
        }

        // --- Draw the child text button ---
        textButton.drawButton(mc, mouseX, mouseY);
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (!this.visible)
            return false;

        // If click is in the text button area:
        if (mouseX >= xPosition && mouseX < xPosition + size &&
            mouseY >= yPosition + iconAreaHeight && mouseY < yPosition + size) {
            return textButton.mousePressed(mc, mouseX, mouseY);
        }
        // Or if click is in the icon area, we also treat it as a click.
        if (mouseX >= xPosition && mouseX < xPosition + size &&
            mouseY >= yPosition && mouseY < yPosition + iconAreaHeight) {
            return true;
        }
        return false;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY) {
        textButton.mouseReleased(mouseX, mouseY);
    }
}
