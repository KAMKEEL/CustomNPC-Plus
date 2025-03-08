package noppes.npcs.client.gui.advanced;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.magic.MagicGetAllPacket;
import kamkeel.npcs.network.packets.request.magic.MagicNpcGetPacket;
import kamkeel.npcs.network.packets.request.magic.MagicNpcSavePacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumScrollData;
import noppes.npcs.controllers.data.MagicData;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class GuiNPCMagic extends GuiNPCInterface2 implements IScrollData, ICustomScrollListener, IGuiData, ITextfieldListener {

    private GuiCustomScroll allMagicScroll;
    private GuiCustomScroll npcMagicScroll;
    private final HashMap<String, Integer> allMagic = new HashMap<>();
    // Initialize npcMagicData here so we don't need to check for null.
    private MagicData npcMagicData = new MagicData();
    private String search = "";

    private GuiNpcTextField splitField, damageField;

    public GuiNPCMagic(EntityNPCInterface npc) {
        super(npc);
        PacketClient.sendClient(new MagicGetAllPacket());
        PacketClient.sendClient(new MagicNpcGetPacket());
    }

    @Override
    public void initGui() {
        super.initGui();

        // Left scroll: display all available magic.
        if (allMagicScroll == null) {
            allMagicScroll = new GuiCustomScroll(this, 0);
            allMagicScroll.setSize(150, 150);
        }
        allMagicScroll.guiLeft = guiLeft + 20;
        allMagicScroll.guiTop = guiTop + 24;
        allMagicScroll.setList(new ArrayList<>(allMagic.keySet()));
        this.addScroll(allMagicScroll);

        // Search bar to filter available magic (text field id 4).
        addTextField(new GuiNpcTextField(4, this, fontRendererObj, guiLeft + 20, guiTop + 24 + 155, 150, 20, search));

        // Right scroll: display currently selected magic.
        if (npcMagicScroll == null) {
            npcMagicScroll = new GuiCustomScroll(this, 1);
            npcMagicScroll.setSize(150, 150);
        }
        npcMagicScroll.guiLeft = guiLeft + 250;
        npcMagicScroll.guiTop = guiTop + 24;
        List<String> selectedList = new ArrayList<>();
        for (String name : allMagic.keySet()) {
            int id = allMagic.get(name);
            if (npcMagicData.hasMagic(id)) {
                selectedList.add(name);
            }
        }
        npcMagicScroll.setList(selectedList);
        this.addScroll(npcMagicScroll);

        // Arrow buttons for adding and removing magic entries.
        addButton(new GuiNpcButton(70, guiLeft + 185, guiTop + 90, 55, 20, ">")); // Add
        addButton(new GuiNpcButton(71, guiLeft + 185, guiTop + 112, 55, 20, "<")); // Remove

        // Standardize button: evenly distribute split value among all magic entries.
        addButton(new GuiNpcButton(72, guiLeft + 185, guiTop + 140, 55, 20, "Std"));

        // Add Split and Bonus Damage text fields.
        int tfY = guiTop + 24 + 155;
        addLabel(new GuiNpcLabel(5002, "magic.split", guiLeft + 193, tfY + 5));
        splitField = new GuiNpcTextField(73, this, fontRendererObj, guiLeft + 250, tfY, 45, 20, "");
        splitField.setFloatsOnly();
        splitField.setMinMaxDefaultFloat(0, 1000000000, 0);
        splitField.enabled = false;
        addTextField(splitField);

        addLabel(new GuiNpcLabel(5003, "magic.bonus", guiLeft + 300, tfY + 5));
        damageField = new GuiNpcTextField(74, this, fontRendererObj, guiLeft + 400 - 45, tfY, 45, 20, "");
        damageField.setFloatsOnly();
        damageField.setMinMaxDefaultFloat(0, 1000000000, 0);
        damageField.enabled = false;
        addTextField(damageField);
    }

    @Override
    public void buttonEvent(GuiButton guibutton) {
        // Add magic from available list.
        if (guibutton.id == 70) {
            if (allMagicScroll.hasSelected()) {
                String selected = allMagicScroll.getSelected();
                int id = allMagic.get(selected);
                if (!npcMagicData.hasMagic(id)) {
                    npcMagicData.addMagic(id, 0, 0);
                }
            }
            updateNpcMagicSelectedList();
            save();
            return;
        }
        // Remove magic from NPC's magic list.
        if (guibutton.id == 71) {
            if (npcMagicScroll.hasSelected()) {
                String selected = npcMagicScroll.getSelected();
                int id = allMagic.get(selected);
                if (npcMagicData.hasMagic(id)) {
                    npcMagicData.removeMagic(id);
                }
            }
            updateNpcMagicSelectedList();
            // Clear text fields when removal.
            if (splitField != null) {
                splitField.setText("");
                splitField.enabled = false;
            }
            if (damageField != null) {
                damageField.setText("");
                damageField.enabled = false;
            }
            save();
            return;
        }
        // Standardize: evenly distribute 1.0 split value among all magic entries.
        if (guibutton.id == 72) {
            if (npcMagicData.getMagics().size() > 0) {
                int count = npcMagicData.getMagics().size();
                float stdSplit = 1.0f / count;
                for (Integer key : npcMagicData.getMagics().keySet()) {
                    npcMagicData.getMagic(key).split = stdSplit;
                }
                // Update the split field for the currently selected magic.
                if (npcMagicScroll.getSelected() != null && splitField != null) {
                    String name = npcMagicScroll.getSelected();
                    int id = allMagic.get(name);
                    if (npcMagicData.hasMagic(id)) {
                        splitField.setText(npcMagicData.getMagic(id).split + "");
                    }
                }
            }
            save();
            return;
        }
        initGui();
    }

    @Override
    public void setData(Vector<String> list, HashMap<String, Integer> data, EnumScrollData type) {
        if (type == EnumScrollData.MAGIC) {
            allMagic.clear();
            allMagic.putAll(data);
            if (allMagicScroll != null) {
                allMagicScroll.setList(new ArrayList<>(allMagic.keySet()));
            }
        }
        initGui();
    }

    @Override
    public void setGuiData(NBTTagCompound compound) {
        npcMagicData.readToNBT(compound);
        initGui();
    }

    @Override
    public void mouseClicked(int i, int j, int k) {
        super.mouseClicked(i, j, k);
        if (k == 0 && allMagicScroll != null)
            allMagicScroll.mouseClicked(i, j, k);
    }

    @Override
    public void keyTyped(char c, int i) {
        super.keyTyped(c, i);
        if (getTextField(4) != null && getTextField(4).isFocused()) {
            if (search.equals(getTextField(4).getText()))
                return;
            search = getTextField(4).getText().toLowerCase();
            allMagicScroll.setList(getSearchList());
            allMagicScroll.resetScroll();
        }
    }

    private List<String> getSearchList() {
        if (search.isEmpty()) {
            return new ArrayList<>(allMagic.keySet());
        }
        List<String> list = new ArrayList<>();
        for (String name : allMagic.keySet()) {
            if (name.toLowerCase().contains(search))
                list.add(name);
        }
        return list;
    }

    private void updateNpcMagicSelectedList() {
        List<String> selected = new ArrayList<>();
        for (String name : allMagic.keySet()) {
            int id = allMagic.get(name);
            if (npcMagicData.hasMagic(id)) {
                selected.add(name);
            }
        }
        if (npcMagicScroll != null)
            npcMagicScroll.setList(selected);
    }

    public void save() {
        PacketClient.sendClient(new MagicNpcSavePacket(npcMagicData));
    }

    @Override
    public void setSelected(String selected) {
        if (allMagicScroll != null)
            allMagicScroll.setSelected(selected);
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
        // When the right scroll is clicked, update the text fields with the selected magic's values.
        if (guiCustomScroll == npcMagicScroll) {
            if (npcMagicScroll.getSelected() != null) {
                String name = npcMagicScroll.getSelected();
                int id = allMagic.get(name);
                if (npcMagicData.hasMagic(id)) {
                    if (splitField != null) {
                        splitField.setText(npcMagicData.getMagic(id).split + "");
                        splitField.enabled = true;
                    }
                    if (damageField != null) {
                        damageField.setText(npcMagicData.getMagic(id).damage + "");
                        damageField.enabled = true;
                    }
                }
            }
        }
    }

    @Override
    public void customScrollDoubleClicked(String selection, GuiCustomScroll scroll) {
    }

    @Override
    public void unFocused(GuiNpcTextField textField) {
        // When the split (id 73) or bonus damage (id 74) text field loses focus,
        // update the corresponding magic entry.
        if (textField.id == 73 || textField.id == 74) {
            if (npcMagicScroll != null && npcMagicScroll.getSelected() != null) {
                String name = npcMagicScroll.getSelected();
                int id = allMagic.get(name);
                if (npcMagicData.hasMagic(id)) {
                    if (textField.id == 73) {
                        try {
                            float split = Float.parseFloat(textField.getText());
                            npcMagicData.getMagic(id).split = split;
                        } catch (NumberFormatException e) { }
                    } else if (textField.id == 74) {
                        try {
                            float bonus = Float.parseFloat(textField.getText());
                            npcMagicData.getMagic(id).damage = bonus;
                        } catch (NumberFormatException e) { }
                    }
                    save();
                }
            }
        }
    }
}
