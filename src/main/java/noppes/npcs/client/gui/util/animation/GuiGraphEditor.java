package noppes.npcs.client.gui.util.animation;

import net.minecraft.client.gui.FontRenderer;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiScrollWindow;

import java.awt.*;
import java.util.ArrayList;

public class GuiGraphEditor extends GuiScrollWindow {
    public int graphRight, graphBottom;
    public ArrayList<Point> points = new ArrayList<>();

    public Grid grid;

    public GuiGraphEditor(GuiNPCInterface parent, int posX, int posY, int clipWidth, int clipHeight, int maxScroll) {
        super(parent, posX, posY, clipWidth, clipHeight, maxScroll);
        points.add(new Point(50, 50));
        points.add(new Point(100, 100));
        drawDefaultBackground = false;
    }

    public void initGui() {
        super.initGui();

        graphRight = xPos + clipWidth;
        graphBottom = yPos + clipHeight;

        this.grid = new Grid(this, xPos, yPos, graphRight, graphBottom);
        grid.panY = -(clipHeight * grid.subDivisionY / 2);
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

}
