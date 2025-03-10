package noppes.npcs.client.gui.util.animation;

import net.minecraft.client.gui.FontRenderer;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiScrollWindow;
import noppes.npcs.client.gui.util.animation.keys.AnimationKeyPresets;

public class GuiGraphEditor extends GuiScrollWindow {
    public int graphRight, graphBottom;

    public Grid grid;
    public AnimationKeyPresets keys = new AnimationKeyPresets();


    public GuiGraphEditor(GuiNPCInterface parent, int posX, int posY, int clipWidth, int clipHeight, int maxScroll) {
        super(parent, posX, posY, clipWidth, clipHeight, maxScroll);
        drawDefaultBackground = false;

        this.grid = new Grid(this, xPos, yPos, graphRight, graphBottom);
    }

    public void initGui() {
        super.initGui();
        graphRight = xPos + clipWidth;
        graphBottom = yPos + clipHeight;

        grid.setPos(xPos, yPos, graphRight, graphBottom);
    }
    public void updateScreen(){
        super.updateScreen();
        keys.tick();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks, int wheel) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.drawGradientRect(xPos,yPos,graphRight,graphBottom, 0xFF303030, 0xFF303030);
        grid.draw(mouseX, mouseY, partialTicks, wheel);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        grid.mouseClicked(mouseX, mouseY, button);
    }
    @Override
    public void keyTyped(char c, int i) {
        super.keyTyped(c,i);
        grid.keyTyped(c,i);
    }

    public boolean isWithin(int mouseX, int mouseY) {
        if (parent.hasSubGui())
            return false;

        return mouseX >= xPos && mouseX <= xPos + clipWidth && mouseY >= yPos && mouseY <= yPos + clipHeight;
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
