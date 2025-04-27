package noppes.npcs.client.gui.global;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.magic.*;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.gui.SubGuiMagic;
import noppes.npcs.client.gui.SubGuiMagicCycle;
import noppes.npcs.client.gui.SubGuiMagicCycleViewer;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumScrollData;
import noppes.npcs.controllers.data.Magic;
import noppes.npcs.controllers.data.MagicCycle;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import static noppes.npcs.client.gui.player.inventory.GuiCNPCInventory.specialIcons;

public class GuiNpcManageMagic extends GuiNPCInterface2 implements ISubGuiListener, ICustomScrollListener, IScrollData, IGuiData {

    // Left scroll (for cycles) and right scroll (for magics).
    public GuiCustomScroll leftScroll;   // Only used in cycle view.
    public GuiCustomScroll rightScroll;  // Displays the magics list.

    // When true, we are in global magic view (single list with all magics).
    // When false, we are in cycle view (two lists: left for cycles and right for associated magics).
    private boolean viewByCycle = true;

    // Data maps for cycles and magics. (Keys are display names)
    public HashMap<String, Integer> cycleData = new HashMap<>();
    public HashMap<String, Integer> magicData = new HashMap<>();

    // currentMagicList is the subset shown in the right scroll.
    // In global view it contains all magic names; in cycle view it contains only those associated with the selected cycle.
    private List<String> currentMagicList = new ArrayList<>();

    private MagicCycle selectedCycle;
    private Magic selectedMagic;

    // Divider/resizing variables for cycle view.
    private boolean isResizing = false;
    private int initialDragX = 0;
    private int dividerOffset = 143; // Initial offset from regionLeft.
    private final int dividerWidth = 5;
    private final int minScrollWidth = 50;
    private final int dividerLineHeight = 20;
    private final int dividerLineYOffset = 0;

    // Search strings.
    // cycleSearch filters the left scroll (cycles).
    // magicSearch filters the currentMagicList (right scroll).
    private String cycleSearch = "";
    private String magicSearch = "";

    public GuiNpcManageMagic(EntityNPCInterface npc) {
        super(npc);
        PacketClient.sendClient(new MagicGetAllPacket());
    }

    @Override
    public void initGui() {
        super.initGui();
        int y = guiTop + 5;

        // Toggle button to switch between global and cycle views.
        addButton(new GuiToggleButton(50, guiLeft + 368, guiTop + ySize - 60, viewByCycle));
        ((GuiToggleButton) getButton(50)).setTextureOff(specialIcons).setTextureOffPos(16, 0);
        getButton(50).setIconTexture(specialIcons).setIconPos(16, 16, 16, 0);

        if (!viewByCycle) {
            // --- Cycle View: Two Scrolls (cycles on left, magics on right) ---
            int regionLeft = guiLeft + 64;
            int regionRight = guiLeft + 355;
            int dividerX = regionLeft + dividerOffset;

            // Left scroll for cycles.
            if (leftScroll == null) {
                leftScroll = new GuiCustomScroll(this, 0, 0);
            }
            leftScroll.guiLeft = regionLeft;
            leftScroll.guiTop = y;
            leftScroll.setSize(dividerX - regionLeft, 185);
            // Populate the left scroll with cycle names.
            leftScroll.setList(new ArrayList<>(cycleData.keySet()));
            this.addScroll(leftScroll);

            // Right scroll for magics.
            if (rightScroll == null) {
                rightScroll = new GuiCustomScroll(this, 1, 0);
            }
            rightScroll.guiLeft = dividerX + dividerWidth;
            rightScroll.guiTop = y;
            rightScroll.setSize(regionRight - (dividerX + dividerWidth), 185);
            this.addScroll(rightScroll);

            // Add search bars below each scroll.
            // ID 55: cycle search; ID 66: magic search.
            addTextField(new GuiNpcTextField(55, this, fontRendererObj, regionLeft, y + 185 + 3, dividerX - regionLeft, 20, cycleSearch));
            addTextField(new GuiNpcTextField(66, this, fontRendererObj, dividerX + dividerWidth, y + 185 + 3, regionRight - (dividerX + dividerWidth), 20, magicSearch));

            // Left side cycle management buttons.
            addButton(new GuiNpcButton(10, guiLeft + 3, guiTop + 8, 58, 20, "menu.cycles"));
            getButton(10).setEnabled(false);
            addButton(new GuiNpcButton(4, guiLeft + 3, guiTop + 38, 58, 20, "gui.add"));
            addButton(new GuiNpcButton(5, guiLeft + 3, guiTop + 61, 58, 20, "gui.remove"));
            addButton(new GuiNpcButton(6, guiLeft + 3, guiTop + 94, 58, 20, "gui.edit"));
            addButton(new GuiNpcButton(20, guiLeft + 3, guiTop + 117, 58, 20, "gui.view"));

            // Right side magic management buttons.
            addButton(new GuiNpcButton(33, guiLeft + 358, guiTop + 8, 58, 20, "menu.magics"));
            getButton(33).setEnabled(false);

            addButton(new GuiNpcButton(0, guiLeft + 358, guiTop + 38, 58, 20, "gui.add"));
            getButton(0).setEnabled(false);
            addButton(new GuiNpcButton(1, guiLeft + 358, guiTop + 61, 58, 20, "gui.remove"));
            getButton(1).setEnabled(false);
            addButton(new GuiNpcButton(2, guiLeft + 358, guiTop + 94, 58, 20, "gui.edit"));

            addLabel(new GuiNpcLabel(200, "ID", guiLeft + 4, guiTop + 4 + 3 + 185));
            addLabel(new GuiNpcLabel(201, "", guiLeft + 4, guiTop + 4 + 3 + 195));
        } else {
            // --- Global Magic View: Single Scroll (all magics) ---
            if (rightScroll == null) {
                rightScroll = new GuiCustomScroll(this, 1, 0);
            }
            rightScroll.guiLeft = guiLeft + 10;
            rightScroll.guiTop = y;
            rightScroll.setSize(xSize - 75, 185);
            this.addScroll(rightScroll);
            addTextField(new GuiNpcTextField(66, this, fontRendererObj, guiLeft + 10, y + 185 + 3, xSize - 75, 20, magicSearch));

            addButton(new GuiNpcButton(33, guiLeft + 358, guiTop + 8, 58, 20, "menu.magics"));
            getButton(33).setEnabled(false);

            // Global magic management buttons.
            addButton(new GuiNpcButton(0, guiLeft + 358, guiTop + 38, 58, 20, "gui.add"));
            addButton(new GuiNpcButton(1, guiLeft + 358, guiTop + 61, 58, 20, "gui.remove"));
            addButton(new GuiNpcButton(2, guiLeft + 358, guiTop + 94, 58, 20, "gui.edit"));
        }

        // --- Update Right Scroll List Based on View Mode ---
        if (viewByCycle) {
            // Global view: currentMagicList contains all magic names.
            currentMagicList = new ArrayList<>(magicData.keySet());
            if (rightScroll != null)
                rightScroll.setList(applyMagicSearchFilter(currentMagicList));
        } else {
            // Cycle view: if a cycle is selected update the list, else clear it.
            if (selectedCycle != null) {
                updateMagicList();
            } else {
                currentMagicList.clear();
                if (rightScroll != null)
                    rightScroll.setList(applyMagicSearchFilter(currentMagicList));
            }
        }

        addLabel(new GuiNpcLabel(100, "ID", guiLeft + 358, guiTop + 4 + 3 + 185));
        addLabel(new GuiNpcLabel(101, "", guiLeft + 358, guiTop + 4 + 3 + 195));

        updateButtons();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (!viewByCycle && !hasSubGui()) {
            int regionLeft = guiLeft + 64;
            int dividerX = regionLeft + dividerOffset;
            int regionTop = guiTop + 30;
            int regionHeight = 185;
            int handleTop = regionTop + (regionHeight - dividerLineHeight) / 2 + dividerLineYOffset;
            drawRect(dividerX + 1, handleTop, dividerX + dividerWidth - 1, handleTop + dividerLineHeight, 0xFF707070);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!viewByCycle && !hasSubGui()) {
            int regionLeft = guiLeft + 64;
            int dividerX = regionLeft + dividerOffset;
            int regionTop = guiTop + 30;
            int regionHeight = 185;
            int handleTop = regionTop + (regionHeight - dividerLineHeight) / 2 + dividerLineYOffset;
            int handleBottom = handleTop + dividerLineHeight;
            // Start resizing if clicking on the divider.
            if (mouseX >= dividerX && mouseX <= dividerX + dividerWidth &&
                mouseY >= handleTop && mouseY <= handleBottom) {
                isResizing = true;
                resizingActive = true;
                initialDragX = mouseX;
                return;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);


    }

    public void updateButtons() {
        // Magic Buttons
        GuiButton button = getButton(2);
        if (button != null) {
            button.enabled = selectedMagic != null;
        }

        if (viewByCycle) {
            button = getButton(1);
            if (button != null) {
                button.enabled = selectedMagic != null;
            }
        }

        button = getButton(5);
        if (button != null) {
            button.enabled = selectedCycle != null;
        }

        button = getButton(6);
        if (button != null) {
            button.enabled = selectedCycle != null;
        }

        button = getButton(20);
        if (button != null) {
            button.enabled = selectedCycle != null;
        }

        if (getLabel(201) != null) {
            if (selectedCycle != null) {
                getLabel(201).label = selectedCycle.id + "";
            } else {
                getLabel(201).label = "";
            }
        }

        if (getLabel(101) != null) {
            if (selectedMagic != null) {
                getLabel(101).label = selectedMagic.id + "";
            } else {
                getLabel(101).label = "";
            }
        }
    }

    @Override
    public void save() {
    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (!viewByCycle && isResizing) {
            int dx = mouseX - initialDragX;
            initialDragX = mouseX;
            dividerOffset += dx;
            int regionLeft = guiLeft + 64;
            int regionRight = guiLeft + 355;
            int minOffset = minScrollWidth;
            int maxOffset = (regionRight - regionLeft) - dividerWidth - minScrollWidth;
            if (dividerOffset < minOffset) {
                dividerOffset = minOffset;
            }
            if (dividerOffset > maxOffset) {
                dividerOffset = maxOffset;
            }
            int dividerX = regionLeft + dividerOffset;
            leftScroll.setSize(dividerX - regionLeft, 185);
            rightScroll.guiLeft = dividerX + dividerWidth;
            rightScroll.setSize(regionRight - (dividerX + dividerWidth), 185);
            // Adjust search field sizes.
            if (getTextField(55) != null) {
                getTextField(55).width = dividerX - regionLeft;
            }
            if (getTextField(66) != null) {
                getTextField(66).width = regionRight - (dividerX + dividerWidth);
                getTextField(66).xPosition = dividerX + dividerWidth;
            }
            return;
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int state) {
        if (isResizing) {
            isResizing = false;
            resizingActive = false;
            return;
        }
        super.mouseMovedOrUp(mouseX, mouseY, state);
    }

    @Override
    public void keyTyped(char c, int i) {
        super.keyTyped(c, i);
        // --- Cycle Search (only in cycle view) ---
        if (!viewByCycle) {
            if (getTextField(55) != null && getTextField(55).isFocused()) {
                if (!cycleSearch.equals(getTextField(55).getText())) {
                    cycleSearch = getTextField(55).getText().toLowerCase();
                    leftScroll.resetScroll();
                    leftScroll.setList(getCycleSearch());
                }
            }
        }
        // --- Magic Search: filter the currentMagicList (right scroll) ---
        if (getTextField(66) != null && getTextField(66).isFocused()) {
            if (!magicSearch.equals(getTextField(66).getText())) {
                magicSearch = getTextField(66).getText().toLowerCase();
                rightScroll.resetScroll();
                rightScroll.setList(applyMagicSearchFilter(currentMagicList));
            }
        }
    }

    // Returns a list of cycles filtered by the cycleSearch string.
    private List<String> getCycleSearch() {
        if (cycleSearch.isEmpty()) {
            return new ArrayList<>(cycleData.keySet());
        }
        List<String> list = new ArrayList<>();
        for (String name : cycleData.keySet()) {
            if (name.toLowerCase().contains(cycleSearch))
                list.add(name);
        }
        return list;
    }

    /**
     * Applies the magic search filter to the provided list.
     * Returns a new list containing only magic names that match the magicSearch string.
     */
    private List<String> applyMagicSearchFilter(List<String> list) {
        if (magicSearch.isEmpty()) {
            return new ArrayList<>(list);
        }
        List<String> filtered = new ArrayList<>();
        for (String name : list) {
            if (name.toLowerCase().contains(magicSearch)) {
                filtered.add(name);
            }
        }
        return filtered;
    }

    @Override
    public void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 50:
                // Toggle view mode.
                viewByCycle = !viewByCycle;
                selectedMagic = null;
                selectedCycle = null;
                // Reset search strings.
                cycleSearch = "";
                magicSearch = "";
                // Update currentMagicList based on the new mode.
                if (viewByCycle) {
                    // Global view: all magics.
                    currentMagicList = new ArrayList<>(magicData.keySet());
                } else {
                    // Cycle view: start with an empty magic list.
                    currentMagicList.clear();
                }

                if (rightScroll != null)
                    rightScroll.selected = -1;

                if (leftScroll != null)
                    leftScroll.selected = -1;

                initGui();
                return;
            // --- Cycle Management Buttons ---
            case 4: // Add Cycle.
            {
                MagicCycle magicCycle = new MagicCycle();
                magicCycle.name = "New";
                magicCycle.displayName = "New";
                NBTTagCompound compound = new NBTTagCompound();
                magicCycle.writeNBT(compound);
                PacketClient.sendClient(new MagicCycleSavePacket(compound));
            }
            break;
            case 5: // Remove Cycle.
                if (selectedCycle != null) {
                    PacketClient.sendClient(new MagicCycleRemovePacket(selectedCycle.id));
                    selectedCycle = null;
                    if (leftScroll != null)
                        leftScroll.selected = -1;
                }
                break;
            case 6: // Edit Cycle.
                if (selectedCycle != null) {
                    setSubGui(new SubGuiMagicCycle(this, selectedCycle));
                }
                break;
            // --- Magic Management Buttons ---
            case 0: // Add Magic.
                Magic magic = new Magic();
                magic.name = "New";
                magic.displayName = "New";
                NBTTagCompound compound = new NBTTagCompound();
                magic.writeNBT(compound);
                PacketClient.sendClient(new MagicSavePacket(compound));
                if (selectedMagic != null) {
                    selectedMagic = null;
                    if (rightScroll != null)
                        rightScroll.selected = -1;
                }
                break;
            case 1: // Remove Magic.
                if (selectedMagic != null) {
                    PacketClient.sendClient(new MagicRemovePacket(selectedMagic.id));
                    selectedMagic = null;
                    if (rightScroll != null)
                        rightScroll.selected = -1;
                }
                break;
            case 2: // Edit Magic.
                if (selectedMagic != null) {
                    setSubGui(new SubGuiMagic(this, selectedMagic));
                }
                break;
            case 20:
                if (selectedCycle != null) {
                    setSubGui(new SubGuiMagicCycleViewer(selectedCycle));
                }
                break;
        }

        updateButtons();
    }

    @Override
    public void customScrollClicked(int id, int index, int clickType, GuiCustomScroll scroll) {
        if (!viewByCycle) {
            if (scroll.id == 0) { // Left scroll: cycles.
                selectedCycle = null;
                String cycleName = scroll.getSelected();
                if (cycleData.containsKey(cycleName)) {
                    MagicGetPacket.GetCycle(cycleData.get(cycleName));
                }
            } else if (scroll.id == 1) { // Right scroll: magics.
                selectedMagic = null;
                String magicName = scroll.getSelected();
                if (magicData.containsKey(magicName)) {
                    MagicGetPacket.GetMagic(magicData.get(magicName));
                }
            }
        } else {
            // Global view.
            if (scroll.id == 1) {
                selectedMagic = null;
                String magicName = scroll.getSelected();
                if (magicData.containsKey(magicName)) {
                    MagicGetPacket.GetMagic(magicData.get(magicName));
                }
            }
        }
    }

    @Override
    public void customScrollDoubleClicked(String selection, GuiCustomScroll scroll) {
    }

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
        PacketClient.sendClient(new MagicGetAllPacket());
        initGui();
    }

    @Override
    public void setData(Vector<String> list, HashMap<String, Integer> data, EnumScrollData type) {
        if (type == EnumScrollData.MAGIC_CYCLES) {
            // Update cycle data and, in cycle view, update the left scroll.
            cycleData.clear();
            cycleData.putAll(data);
            if (!viewByCycle) {
                leftScroll.setList(new ArrayList<>(cycleData.keySet()));
            }
        } else if (type == EnumScrollData.MAGIC) {
            // Update magic data (all magics remain in magicData).
            magicData.clear();
            magicData.putAll(data);
            if (!viewByCycle) {
                // In cycle view, update currentMagicList based on the selected cycle.
                updateMagicList();
            } else {
                // In global view, currentMagicList is all magics.
                currentMagicList = new ArrayList<>(magicData.keySet());
                if (rightScroll != null)
                    rightScroll.setList(applyMagicSearchFilter(currentMagicList));
            }
        }
        initGui();
    }

    /**
     * In cycle view, updates the currentMagicList based on the selected cycle’s associations.
     * Then applies the magic search filter to update the right scroll list.
     */
    public void updateMagicList() {
        currentMagicList.clear();
        if (selectedCycle != null) {
            // For each magic ID associated with the cycle, add its name from magicData.
            for (int magicId : selectedCycle.associations.keySet()) {
                for (String magicName : magicData.keySet()) {
                    if (magicData.get(magicName) == magicId) {
                        currentMagicList.add(magicName);
                        break;
                    }
                }
            }
        }
        if (rightScroll != null)
            rightScroll.setList(applyMagicSearchFilter(currentMagicList));
    }

    @Override
    public void setSelected(String selected) {
    }

    @Override
    public void setGuiData(NBTTagCompound compound) {
        if (compound.hasKey("Magic")) {
            selectedMagic = new Magic();
            selectedMagic.readNBT(compound.getCompoundTag("Magic"));
        } else if (compound.hasKey("MagicCycle")) {
            selectedCycle = new MagicCycle();
            selectedCycle.readNBT(compound.getCompoundTag("MagicCycle"));
            // Refresh the magic list based on the new cycle.
            updateMagicList();
            selectedMagic = null;
            if (rightScroll != null)
                rightScroll.selected = -1;
        }

        updateButtons();
    }
}
