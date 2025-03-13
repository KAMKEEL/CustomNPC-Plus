package noppes.npcs.client.gui.util.animation;

import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiUtil;
import noppes.npcs.client.gui.util.animation.panel.PanelFrameType;

public class AnimationGraphEditor extends ViewportGraphEditor {
    public PanelFrameType frameTypePanel = new PanelFrameType(this);

    public AnimationGraphEditor(GuiNPCInterface parent) {
        super(parent);
    }

    public void initGui(int startX, int startY, int endX, int endY) {
        super.initGui(startX, startY, endX, endY);
        frameTypePanel.initGui(startX - 60, startY, startX, endY);
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks, int wheel) {
        GuiUtil.drawGradientRect(startX, startY, endX, endY, 0xFF303030, 0xFF303030);

        frameTypePanel.draw();
        grid.draw(mouseX, mouseY, partialTicks, wheel);
        presetOverlay.draw(mouseX, mouseY, wheel);
    }
}
