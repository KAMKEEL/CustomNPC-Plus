package noppes.npcs.client.gui.util.animation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiUtil;
import noppes.npcs.client.gui.util.animation.keys.GraphEditorKeyPresets;

public class ViewportGraphEditor {
    public Minecraft mc = Minecraft.getMinecraft();
    public GuiNPCInterface parent;

    public int startX, startY, endX, endY, width, height;

    public GraphEditorKeyPresets keys = new GraphEditorKeyPresets();
    public OverlayKeyPresetViewer presetOverlay = new OverlayKeyPresetViewer(keys);

    public Grid grid = new Grid(this);

    public ViewportGraphEditor(GuiNPCInterface parent) {
        this.parent = parent;
    }

    public void initGui(int startX, int startY, int endX, int endY) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.width = endX - this.startX;
        this.height = endY - this.startY;

        grid.init(this.startX, this.startY, endX, endY);
        presetOverlay.initGui(endX - (width / 2) - 37, startY + (height / 4), endX, endY);
        presetOverlay.viewButton.initGui(endX + 1, endY + 2);
    }

    public void updateScreen() {
        keys.tick();
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks, int wheel) {
        GuiUtil.drawGradientRect(startX, startY, endX, endY, 0xFF303030, 0xFF303030);
        grid.draw(mouseX, mouseY, partialTicks, wheel);
        presetOverlay.draw(mouseX, mouseY, wheel);
    }

    public void mouseClicked(int mouseX, int mouseY, int button) {
        grid.mouseClicked(mouseX, mouseY, button);
        presetOverlay.mouseClicked(mouseX, mouseY, button);
    }

    public void keyTyped(char c, int i) {
        grid.keyTyped(c, i);
        presetOverlay.keyTyped(c, i);
    }

    public boolean isWithin(int mouseX, int mouseY) {
        if (parent.hasSubGui())
            return false;

        return mouseX >= startX && mouseX <= startX + width && mouseY >= startY && mouseY <= startY + height;
    }

    public void drawString(String text, int x, int y, int color) {
        parent.drawCenteredString(getFontRenderer(), text, x, y, color);
    }

    public FontRenderer getFontRenderer() {
        return parent.getFontRenderer();
    }

    public void close() {
        if (Cursors.currentCursor != null)
            Cursors.reset();
    }
}
