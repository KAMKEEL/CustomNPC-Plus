package noppes.npcs.client.gui;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.magic.MagicSavePacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.gui.global.GuiNpcManageMagic;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumTextureType;
import noppes.npcs.controllers.MagicController;
import noppes.npcs.controllers.data.Magic;
import noppes.npcs.controllers.data.MagicCycle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SubGuiMagicCycleViewer extends SubGuiInterface {

    private final MagicCycle magicCycle;
    private GuiMagicCycleMap magicMap;

    public SubGuiMagicCycleViewer(MagicCycle cycle) {
        this.magicCycle = cycle;
        setBackground("menubg.png");
        xSize = 256;
        ySize = 222;

        closeOnEsc = true;
    }

    public SubGuiMagicCycleViewer(int cycleID) {
        this(MagicController.getInstance().getCycle(cycleID));
    }

    @Override
    public void initGui() {
        super.initGui();

        if(magicCycle == null)
            return;

        // Position the map in a designated area (e.g., right panel)
        int mapX = guiLeft + 6;
        int mapY = guiTop + 6;
        int mapWidth = xSize - 13;
        int mapHeight = ySize - 17;

        magicMap = new GuiMagicCycleMap(this, mapX, mapY, mapWidth, mapHeight, magicCycle);
        addDiagram(0, magicMap);
    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
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
}
