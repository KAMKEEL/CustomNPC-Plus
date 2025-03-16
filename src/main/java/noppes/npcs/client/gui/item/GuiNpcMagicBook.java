package noppes.npcs.client.gui.item;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.player.item.MagicCyclesPacket;
import kamkeel.npcs.network.packets.request.magic.MagicGetAllPacket;
import kamkeel.npcs.network.packets.request.magic.MagicGetPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.gui.GuiMagicCycleMap;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumScrollData;
import noppes.npcs.controllers.data.Magic;
import noppes.npcs.controllers.data.MagicCycle;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class GuiNpcMagicBook extends GuiNPCInterface implements ICustomScrollListener, IScrollData, IGuiData {

    private MagicCycle magicCycle;
    private GuiMagicCycleMap magicMap;
    private GuiCustomScroll leftScroll;
    public HashMap<String, Integer> cycleData = new HashMap<>();

    public GuiNpcMagicBook() {
        super();
        xSize = 350;
        setBackground("menubg.png");
        closeOnEsc = true;
        MagicCyclesPacket.GetAll();
    }

    @Override
    public void initGui() {
        super.initGui();

        int spacing = 10;
        int scrollWidth = 120;

        addLabel(new GuiNpcLabel(0, "menu.cycles", guiLeft + 14, guiTop + 10, 0xFFFFFF));

        // --- Left Scroll List ---
        leftScroll = new GuiCustomScroll(this, 0, 0);
        leftScroll.guiLeft = guiLeft + spacing;
        leftScroll.guiTop = guiTop + 22;
        leftScroll.setSize(scrollWidth, ySize - 32);
        leftScroll.setList(new ArrayList<>(cycleData.keySet()));
        addScroll(leftScroll);

        // --- Magic Map (Diagram) on the Right ---
        if(magicCycle == null)
            return;

        String displayName = magicCycle.getDisplayName().replace("&", "\u00A7");
        leftScroll.setSelected(displayName);


        int mapX = guiLeft + scrollWidth + 2 * spacing;
        int mapY = guiTop + 22;
        int mapWidth = xSize - (scrollWidth + 3 * spacing);
        int mapHeight = ySize - 32;
        magicMap = new GuiMagicCycleMap(this, mapX, mapY, mapWidth, mapHeight, magicCycle);
        addDiagram(0, magicMap);

        int labelY = guiTop + 10;
        int textWidth = fontRendererObj.getStringWidth(displayName);
        int labelX = mapX + (mapWidth / 2) - (textWidth / 2);
        GuiNpcLabel magicLabel = new GuiNpcLabel(1, displayName, labelX, labelY, CustomNpcResourceListener.DefaultTextColor);
        addLabel(magicLabel);
    }

    @Override
    public void keyTyped(char c, int i) {
        super.keyTyped(c, i);
        if (i == 1) {
            close();
        }
    }

    @Override
    public void actionPerformed(GuiButton guibutton) {}

    @Override
    public void drawScreen(int i, int j, float f) {
        super.drawScreen(i, j, f);
    }

    @Override
    protected void drawBackground(){
        super.drawBackground();

        int spacing = 10;
        int scrollWidth = 120;
        int barHeight = 16; // Same height as the magic map bar
        int barY = guiTop + 22 - barHeight - 1;

        // Draw dark rectangle above the left scroll (same width as left scroll)
        if (leftScroll != null) {
            int leftRectX = guiLeft + spacing;
            drawRect(leftRectX, barY, leftRectX + scrollWidth, barY + barHeight, 0xFF333333);
        }

        // Draw dark rectangle above the magic map
        if (magicMap != null) {
            int mapX = guiLeft + scrollWidth + 2 * spacing;
            int mapWidth = xSize - (scrollWidth + 3 * spacing);
            drawRect(mapX, barY, mapX + mapWidth, barY + barHeight, 0xFF333333);
        }
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

    @Override
    public void save() {
        // Implement save logic if needed.
    }

    // --- ICustomScrollListener Methods ---
    @Override
    public void customScrollClicked(int id, int index, int clickType, GuiCustomScroll scroll) {
        if (scroll == leftScroll) {
            String selectedName = scroll.getSelected();
            MagicCyclesPacket.GetCycle(cycleData.get(selectedName));
        }
    }

    @Override
    public void customScrollDoubleClicked(String selection, GuiCustomScroll scroll) {
        // Handle double-click if necessary.
    }

    @Override
    public void setData(Vector<String> list, HashMap<String, Integer> data, EnumScrollData type) {
        if(type == EnumScrollData.MAGIC_CYCLES){
            cycleData.clear();
            cycleData.putAll(data);
            if (leftScroll != null) {
                leftScroll.setList(new ArrayList<>(cycleData.keySet()));
            }
            initGui();
        }
    }

    @Override
    public void setSelected(String selected) {

    }

    @Override
    public void setGuiData(NBTTagCompound compound) {
        if (compound.hasKey("MagicCycle")) {
            magicCycle = new MagicCycle();
            magicCycle.readNBT(compound.getCompoundTag("MagicCycle"));
            initGui();
        }
    }
}
