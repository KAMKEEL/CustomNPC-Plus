package noppes.npcs.client.gui;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.magic.MagicCycleSavePacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.gui.global.GuiNpcManageMagic;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumDiagramLayout;
import noppes.npcs.controllers.data.MagicAssociation;
import noppes.npcs.controllers.data.MagicCycle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SubGuiMagicCycle extends SubGuiInterface implements ITextfieldListener, ISubGuiListener, ICustomScrollListener {

    // Reference to the parent GUI (for accessing magicData)
    public GuiNpcManageMagic parent;
    // Scroll for available magics (to add as associations)
    private GuiCustomScroll allMagic;
    // Scroll for magics already associated with the cycle
    private GuiCustomScroll associationScroll;

    // The MagicCycle we are editing
    private MagicCycle cycle;

    // Search string for available magics
    private String search = "";

    // Text fields for basic cycle data
    // ID 1: cycle name, ID 2: cycle display name
    // Button ID 13 will cycle through the layouts
    // IDs 91 & 92 for editing selected association’s index and priority
    private GuiNpcTextField indexField;
    private GuiNpcTextField priorityField;

    // The currently selected associated magic (by its name)
    private String selectedAssociation;
    // A mapping of magic names (from parent.magicData) to their MagicAssociation objects.
    private HashMap<String, MagicAssociation> associationMap = new HashMap<>();

    public SubGuiMagicCycle(GuiNpcManageMagic parent, MagicCycle cycle) {
        this.parent = parent;
        this.cycle = cycle;
        setBackground("menubg.png");
        xSize = 415;
        ySize = 216;
        closeOnEsc = true;
        processAssociations();
    }

    @Override
    public void initGui() {
        super.initGui();
        int y = guiTop + 6;

        // --- Basic Cycle Data ---
        addLabel(new GuiNpcLabel(1, "gui.name", guiLeft + 4, y + 5));
        addTextField(new GuiNpcTextField(1, this, fontRendererObj, guiLeft + 80, y, 200, 20, cycle.name));

        addLabel(new GuiNpcLabel(-10, "ID", guiLeft + 200 + 80 + 5, y + 2));
        addLabel(new GuiNpcLabel(-11, cycle.id + "", guiLeft + 200 + 80 + 5, y + 12));

        // Layout button – cycles through available layouts
        addLabel(new GuiNpcLabel(3, "magic.diagram", guiLeft + 307, y + 5));

        y += 25;

        addButton(new GuiNpcButton(13, guiLeft + 307, y, 100, 20, EnumDiagramLayout.names(), cycle.layout.ordinal()));
        getButton(13).setHoverText("diagram.info");

        addLabel(new GuiNpcLabel(2, "gui.displayName", guiLeft + 4, y + 5));
        addTextField(new GuiNpcTextField(2, this, fontRendererObj, guiLeft + 80, y, 200, 20, cycle.displayName));

        y += 27;

        String index = "gui.unused";
        String priority = "gui.unused";
        if (cycle.layout == EnumDiagramLayout.MANUAL) {
            index = "X";
            priority = "Y";
        } else if (cycle.layout.isManual()) {
            index = "magic.index";
            priority = "magic.priority";
        }

        addLabel(new GuiNpcLabel(50, index, guiLeft + 5, y + 5));
        indexField = new GuiNpcTextField(91, this, fontRendererObj, guiLeft + 100, y, 45, 20, "");
        indexField.setIntegersOnly();
        addTextField(indexField);

        addLabel(new GuiNpcLabel(51, priority, guiLeft + 150, y + 5));
        priorityField = new GuiNpcTextField(92, this, fontRendererObj, guiLeft + 250, y, 45, 20, "");
        priorityField.setIntegersOnly();
        addTextField(priorityField);

        indexField.enabled = false;
        priorityField.enabled = false;

        if (selectedAssociation != null && associationMap.containsKey(selectedAssociation)) {
            indexField.enabled = true;
            priorityField.enabled = true;
            indexField.setText(associationMap.get(selectedAssociation).index + "");
            priorityField.setText(associationMap.get(selectedAssociation).priority + "");
        }

        y += 27;

        // --- Association Management ---
        // Scroll for available magics to add
        if (allMagic == null) {
            allMagic = new GuiCustomScroll(this, 0);
            allMagic.setSize(150, 100);
        }
        allMagic.guiLeft = guiLeft + 5;
        allMagic.guiTop = y;
        // Set list based on parent's magicData filtered by search
        allMagic.setList(getAvailableMagicList());
        this.addScroll(allMagic);

        // Search field for available magics (ID 34)
        addTextField(new GuiNpcTextField(34, this, fontRendererObj, guiLeft + 5, y + 105, 150, 20, search));

        // Scroll for current associations
        if (associationScroll == null) {
            associationScroll = new GuiCustomScroll(this, 1);
            associationScroll.setSize(150, 125);
        }
        associationScroll.guiLeft = guiLeft + 205;
        associationScroll.guiTop = y;
        // Populate with keys from associationMap
        associationScroll.setList(new ArrayList<>(associationMap.keySet()));
        this.addScroll(associationScroll);

        // Buttons to add/remove associations (IDs 60, 61, 62, 63)
        addButton(new GuiNpcButton(60, guiLeft + 160, y + 20, 30, 20, ">"));
        addButton(new GuiNpcButton(61, guiLeft + 160, y + 45, 30, 20, "<"));
        addButton(new GuiNpcButton(62, guiLeft + 160, y + 70, 30, 20, ">>"));
        addButton(new GuiNpcButton(63, guiLeft + 160, y + 95, 30, 20, "<<"));

        // Done Button to save changes
        addButton(new GuiNpcButton(99, guiLeft + xSize - 50 - 4, guiTop + ySize - 20 - 4, 50, 20, "gui.done"));
    }

    @Override
    public void actionPerformed(GuiButton button) {
        // Layout button: update cycle layout.
        if (button.id == 13) {
            cycle.layout = EnumDiagramLayout.values()[((GuiNpcButton) button).getValue()];
            initGui();
        }
        // Add single association
        if (button.id == 60 && allMagic.hasSelected() && !associationMap.containsKey(allMagic.getSelected())
            && parent.magicData.containsKey(allMagic.getSelected())) {
            MagicAssociation assoc = new MagicAssociation();
            assoc.magicId = parent.magicData.get(allMagic.getSelected());
            assoc.index = 0;
            assoc.priority = 0;
            associationMap.put(allMagic.getSelected(), assoc);
            associationScroll.list.add(allMagic.getSelected());
        }
        // Remove single association
        if (button.id == 61 && associationScroll.hasSelected()) {
            associationMap.remove(associationScroll.getSelected());
            associationScroll.list.remove(associationScroll.selected);
            associationScroll.selected = -1;
            indexField.enabled = false;
            indexField.setText("");
            priorityField.enabled = false;
            priorityField.setText("");
        }
        // Add All available associations
        if (button.id == 62) {
            for (String name : parent.magicData.keySet()) {
                if (!associationMap.containsKey(name)) {
                    MagicAssociation assoc = new MagicAssociation();
                    assoc.magicId = parent.magicData.get(name);
                    assoc.index = 0;
                    assoc.priority = 0;
                    associationMap.put(name, assoc);
                }
            }
            associationScroll.setList(new ArrayList<>(associationMap.keySet()));
        }
        // Remove All associations
        if (button.id == 63) {
            associationMap.clear();
            associationScroll.setList(new ArrayList<>());
            associationScroll.selected = -1;
            indexField.enabled = false;
            indexField.setText("");
            priorityField.enabled = false;
            priorityField.setText("");
        }
        // Done – save changes and close
        if (button.id == 99) {
            close();
        }
    }

    @Override
    public void unFocused(GuiNpcTextField textField) {
        // Update cycle basic data.
        if (textField.id == 1) {
            cycle.name = textField.getText();
        }
        if (textField.id == 2) {
            cycle.displayName = textField.getText();
        }
        // Update association ordering values when fields lose focus.
        if (textField.id == 91 && selectedAssociation != null) {
            try {
                int idx = Integer.parseInt(textField.getText());
                associationMap.get(selectedAssociation).index = idx;
            } catch (NumberFormatException e) {
            }
        }
        if (textField.id == 92 && selectedAssociation != null) {
            try {
                int prio = Integer.parseInt(textField.getText());
                associationMap.get(selectedAssociation).priority = prio;
            } catch (NumberFormatException e) {
            }
        }
    }

    @Override
    public void keyTyped(char c, int i) {
        super.keyTyped(c, i);
        // Available magic search (text field ID 34)
        if (getTextField(34) != null && getTextField(34).isFocused()) {
            if (search.equals(getTextField(34).getText()))
                return;
            search = getTextField(34).getText().toLowerCase();
            allMagic.setList(getAvailableMagicList());
            allMagic.resetScroll();
        }
    }

    /**
     * Returns a filtered list of available magic names based on parent's magicData and the search string.
     * Only those not already in the associationMap are shown.
     */
    private List<String> getAvailableMagicList() {
        List<String> original = new ArrayList<>(parent.magicData.keySet());
        List<String> filtered = new ArrayList<>();
        for (String name : original) {
            if (!associationMap.containsKey(name) && name.toLowerCase().contains(search)) {
                filtered.add(name);
            }
        }
        return filtered;
    }

    /**
     * Populates associationMap from cycle.associations using parent's magicData.
     */
    public void processAssociations() {
        associationMap.clear();
        // For each association stored in the cycle, find the corresponding magic name.
        for (MagicAssociation assoc : cycle.associations.values()) {
            for (String name : parent.magicData.keySet()) {
                if (parent.magicData.get(name) == assoc.magicId) {
                    associationMap.put(name, assoc);
                    break;
                }
            }
        }
    }

    @Override
    public void customScrollClicked(int id, int index, int clickType, GuiCustomScroll scroll) {
        // When clicking the association scroll, update the ordering text fields.
        if (scroll.id == 1) {
            selectedAssociation = scroll.getSelected();
            if (selectedAssociation != null && associationMap.containsKey(selectedAssociation)) {
                indexField.enabled = true;
                priorityField.enabled = true;
                indexField.setText(associationMap.get(selectedAssociation).index + "");
                priorityField.setText(associationMap.get(selectedAssociation).priority + "");
            }
        }
    }

    @Override
    public void customScrollDoubleClicked(String selection, GuiCustomScroll scroll) {
    }

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
        // No subgui handling in this example.
    }

    /**
     * Called when the user clicks "Done". Updates the cycle's associations from associationMap,
     * writes the NBT data, and sends a MagicCycleSavePacket.
     */
    public void close() {
        // Clear the cycle's associations and update them from our associationMap.
        cycle.associations.clear();
        for (String name : associationMap.keySet()) {
            cycle.associations.put(associationMap.get(name).magicId, associationMap.get(name));
        }
        NBTTagCompound compound = new NBTTagCompound();
        cycle.writeNBT(compound);
        PacketClient.sendClient(new MagicCycleSavePacket(compound));
        super.close();
    }
}
