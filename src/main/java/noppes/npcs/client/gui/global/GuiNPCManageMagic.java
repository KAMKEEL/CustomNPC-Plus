package noppes.npcs.client.gui.global;

import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNPCManageMagic extends GuiNPCInterface2 {

    private GuiMagicMap magicMap;

    public GuiNPCManageMagic(EntityNPCInterface npc) {
        super(npc);
    }

    @Override
    public void initGui() {
        super.initGui();
        // Position the map in a designated area (e.g., right panel)
        int mapX = guiLeft + xSize - 230 + 10;
        int mapY = guiTop + 10;
        int mapWidth = 210;
        int mapHeight = 200;
        magicMap = new GuiMagicMap(this, mapX, mapY, mapWidth, mapHeight);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (magicMap != null) {
            magicMap.drawDiagram(mouseX, mouseY);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (magicMap != null && magicMap.mouseClicked(mouseX, mouseY, mouseButton)) {
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (magicMap != null) {
            magicMap.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int state) {
        if (magicMap != null) {
            magicMap.mouseReleased(mouseX, mouseY, state);
        }
        super.mouseMovedOrUp(mouseX, mouseY, state);
    }

    @Override
    protected void actionPerformed(net.minecraft.client.gui.GuiButton button) {
        // your existing action handling
    }

    @Override
    public void keyTyped(char c, int i) {
        super.keyTyped(c, i);
    }

    @Override
    public void save() {
        // your save logic
    }
}
