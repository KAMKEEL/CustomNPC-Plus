package noppes.npcs.client.gui.util.animation;

import net.minecraft.client.gui.FontRenderer;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiScrollWindow;
import noppes.npcs.client.gui.util.animation.keys.AnimationKeyPresets;

public class ViewportGraphEditor extends GuiScrollWindow {
    public int startX, startY, endX, endY, width, height;


    public Grid grid;
    public AnimationKeyPresets keys = new AnimationKeyPresets();
    public OverlayKeyPresetViewer presetOverlay;


    public ViewportGraphEditor(GuiNPCInterface parent, int posX, int posY, int clipWidth, int clipHeight, int maxScroll) {
        super(parent, posX, posY, clipWidth, clipHeight, maxScroll);
        drawDefaultBackground = false;


        this.grid = new Grid(this, startX, startY, endX, endY);
        presetOverlay = new OverlayKeyPresetViewer(keys);
    }

    public void initGui() {
        super.initGui();
        endX = (startX = xPos) + clipWidth;
        endY = (startY = yPos) + clipHeight;

        grid.setPos(startX, startY, endX, endY);

        presetOverlay.initGui(endX - (clipWidth / 2) - 37, startY + (clipHeight / 4), endX, endY);
        presetOverlay.viewButton.initGui(endX + 1, endY + 2);
    }

    public void updateScreen(){
        super.updateScreen();
        keys.tick();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks, int wheel) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.drawGradientRect(startX, startY, endX, endY, 0xFF303030, 0xFF303030);

        grid.draw(mouseX, mouseY, partialTicks, wheel);
        presetOverlay.draw(mouseX, mouseY, wheel);

        // int heigh = presetOverlay.list.get(0).getHeight();
        grid.parent.getFontRenderer().drawString("abovebox: " + presetOverlay.list.get(0).isMouseAboveBox(mouseX, mouseY), mouseX + 5, mouseY, 0xffffffff);
        //  grid.parent.getFontRenderer().drawString("y: " + presetOverlay.startY, mouseX, mouseY+40, 0xffffffff);
        grid.parent.getFontRenderer().drawString("mouseX: " + mouseX, mouseX, mouseY + 10, 0xffffffff);
        //   grid.parent.getFontRenderer().drawString("mY+height: " +(heigh+mouseY), mouseX, mouseY+20, 0xffffffff);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        grid.mouseClicked(mouseX, mouseY, button);
        presetOverlay.mouseClicked(mouseX, mouseY, button);
    }
    @Override
    public void keyTyped(char c, int i) {
        super.keyTyped(c,i);
        grid.keyTyped(c,i);
        presetOverlay.keyTyped(c, i);
    }

    public boolean isWithin(int mouseX, int mouseY) {
        if (parent.hasSubGui())
            return false;

        return mouseX >= startX && mouseX <= startX + clipWidth && mouseY >= startY && mouseY <= startY + clipHeight;
    }

    public void drawString(String text, int x, int y, int color) {
        drawCenteredString(fontRendererObj, text, x, y, color);
    }

    public FontRenderer getFontRenderer() {
        return fontRendererObj;
    }

    public void close() {
        if (Cursors.currentCursor != null)
            Cursors.reset();
    }
}
