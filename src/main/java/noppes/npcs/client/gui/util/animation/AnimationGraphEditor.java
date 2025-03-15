package noppes.npcs.client.gui.util.animation;

import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiUtil;
import noppes.npcs.client.gui.util.animation.panel.PanelFrameType;
import noppes.npcs.constants.animation.EnumFrameType;

public class AnimationGraphEditor extends ViewportGraphEditor {

    public GridPointManager pointManager;
    public PanelFrameType frameTypePanel = new PanelFrameType(this);

    public AnimationGraphEditor(GuiNPCInterface parent) {
        super(parent);
        pointManager = new GridPointManager(this);

    }

    public void initGui(int startX, int startY, int endX, int endY) {
        super.initGui(startX, startY, endX, endY);
        frameTypePanel.initGui(startX - 70, startY, startX, endY);
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks, int wheel) {
        GuiUtil.drawGradientRect(startX, startY, endX, endY, 0xFF303030, 0xFF303030);

        frameTypePanel.draw();
        grid.draw(mouseX, mouseY, partialTicks, wheel);
        pointManager.draw(mouseX, mouseY, partialTicks);
        presetOverlay.draw(mouseX, mouseY, wheel);
    }

    public void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        frameTypePanel.mouseClicked(mouseX, mouseY, button);
        pointManager.mouseClicked(mouseX, mouseY, button);
    }

    public void keyTyped(char c, int i) {
        super.keyTyped(c, i);
        frameTypePanel.keyTyped(c, i);
        pointManager.keyTyped(c, i);
    }

    public EnumFrameType getSelectedType() {
        return frameTypePanel.selectedElement.type;
    }
    public void setSelectedType(EnumFrameType type) {
        frameTypePanel.list.forEach(element -> {
            if(element.type == type)
                frameTypePanel.selectedElement = element;
        });
    }

}
