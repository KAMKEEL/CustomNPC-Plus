package noppes.npcs.client.gui.advanced;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.controllers.MagicController;
import noppes.npcs.controllers.data.Magic;
import noppes.npcs.controllers.data.MagicData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sub-GUI for editing magic splits on an Ability.
 * Dual-scroll layout: available magics on left, assigned magics on right.
 * Only split values (no flat damage) are editable.
 */
public class SubGuiAbilityMagic extends SubGuiInterface implements ICustomScrollListener, ITextfieldListener {

    public MagicData magicData;

    private GuiCustomScroll availableScroll;
    private GuiCustomScroll assignedScroll;

    /** Maps display name → magic ID for all available magics. */
    private final HashMap<String, Integer> allMagic = new HashMap<>();
    /** Maps formatted display string (right scroll) → base display name. */
    private final HashMap<String, String> assignedDisplayToName = new HashMap<>();
    private String search = "";

    private GuiNpcTextField splitField;

    public SubGuiAbilityMagic(MagicData data) {
        this.magicData = data;
        setBackground("menubg.png");
        xSize = 420;
        ySize = 222;

        // Load all available magics from the controller
        MagicController controller = MagicController.getInstance();
        for (Map.Entry<Integer, Magic> entry : controller.magics.entrySet()) {
            allMagic.put(entry.getValue().getDisplayName(), entry.getKey());
        }
    }

    @Override
    public void initGui() {
        super.initGui();

        int scrollW = 150;
        int scrollH = 140;
        int leftX = guiLeft + 10;
        int rightX = guiLeft + 250;
        int topY = guiTop + 20;

        // Title
        addLabel(new GuiNpcLabel(0, "ability.magic.editor", guiLeft + 10, guiTop + 5));

        // Left scroll: available magics
        if (availableScroll == null) {
            availableScroll = new GuiCustomScroll(this, 0);
            availableScroll.setSize(scrollW, scrollH);
        }
        availableScroll.guiLeft = leftX;
        availableScroll.guiTop = topY;
        availableScroll.setList(getSearchList());
        addScroll(availableScroll);

        // Search field below left scroll
        addTextField(new GuiNpcTextField(4, this, fontRendererObj, leftX, topY + scrollH + 5, scrollW, 20, search));

        // Right scroll: assigned magics
        if (assignedScroll == null) {
            assignedScroll = new GuiCustomScroll(this, 1);
            assignedScroll.setSize(scrollW, scrollH);
        }
        assignedScroll.guiLeft = rightX;
        assignedScroll.guiTop = topY;
        updateAssignedList();
        addScroll(assignedScroll);

        // Arrow buttons between scrolls
        int midX = guiLeft + 175;
        addButton(new GuiNpcButton(70, midX, topY + 40, 55, 20, ">"));
        addButton(new GuiNpcButton(71, midX, topY + 62, 55, 20, "<"));

        // Standardize button
        addButton(new GuiNpcButton(72, midX, topY + 90, 55, 20, "magic.dist"));
        getButton(72).setHoverText("magic.distInfo");

        // Split field below right scroll
        int tfY = topY + scrollH + 5;
        addLabel(new GuiNpcLabel(5002, "magic.split", rightX - 57, tfY + 5));
        splitField = new GuiNpcTextField(73, this, fontRendererObj, rightX, tfY, 60, 20, "");
        splitField.setFloatsOnly();
        splitField.setMinMaxDefaultFloat(0, 1, 0);
        splitField.enabled = false;
        addTextField(splitField);

        // Done button
        addButton(new GuiNpcButton(80, guiLeft + xSize / 2 - 40, guiTop + ySize - 34, 80, 20, "gui.done"));
    }

    @Override
    public void buttonEvent(GuiButton guibutton) {
        // Add magic
        if (guibutton.id == 70) {
            if (availableScroll.hasSelected()) {
                String selected = availableScroll.getSelected();
                Integer id = allMagic.get(selected);
                if (id != null && !magicData.hasMagic(id)) {
                    magicData.addMagic(id, 0, 0);
                }
            }
            updateAssignedList();
            return;
        }
        // Remove magic
        if (guibutton.id == 71) {
            if (assignedScroll.hasSelected()) {
                Integer id = resolveAssignedId(assignedScroll.getSelected());
                if (id != null && magicData.hasMagic(id)) {
                    magicData.removeMagic(id);
                }
            }
            updateAssignedList();
            if (splitField != null) {
                splitField.setText("");
                splitField.enabled = false;
            }
            return;
        }
        // Standardize splits
        if (guibutton.id == 72) {
            if (magicData.getMagics().size() > 0) {
                int count = magicData.getMagics().size();
                float stdSplit = 1.0f / count;
                for (Integer key : magicData.getMagics().keySet()) {
                    magicData.getMagic(key).split = stdSplit;
                }
                // Update field if something is selected
                if (assignedScroll.getSelected() != null && splitField != null) {
                    Integer id = resolveAssignedId(assignedScroll.getSelected());
                    if (id != null && magicData.hasMagic(id)) {
                        splitField.setText(magicData.getMagic(id).split + "");
                    }
                }
                updateAssignedList();
            }
            return;
        }
        // Done
        if (guibutton.id == 80) {
            close();
            return;
        }
    }

    @Override
    public void keyTyped(char c, int i) {
        super.keyTyped(c, i);
        if (getTextField(4) != null && getTextField(4).isFocused()) {
            if (search.equals(getTextField(4).getText()))
                return;
            search = getTextField(4).getText().toLowerCase();
            availableScroll.setList(getSearchList());
            availableScroll.resetScroll();
        }
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
        if (guiCustomScroll == assignedScroll) {
            if (assignedScroll.getSelected() != null) {
                Integer id = resolveAssignedId(assignedScroll.getSelected());
                if (id != null && magicData.hasMagic(id)) {
                    if (splitField != null) {
                        splitField.setText(magicData.getMagic(id).split + "");
                        splitField.enabled = true;
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
        if (textField.id == 73) {
            if (assignedScroll != null && assignedScroll.getSelected() != null) {
                Integer id = resolveAssignedId(assignedScroll.getSelected());
                if (id != null && magicData.hasMagic(id)) {
                    try {
                        float split = Float.parseFloat(textField.getText());
                        magicData.getMagic(id).split = split;
                        updateAssignedList();
                    } catch (NumberFormatException e) {
                    }
                }
            }
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

    /**
     * Resolves the magic ID from a right-scroll selection (which may be formatted as "Name - XX%").
     */
    private Integer resolveAssignedId(String selection) {
        if (selection == null) return null;
        String baseName = assignedDisplayToName.get(selection);
        if (baseName == null) baseName = selection;
        return allMagic.get(baseName);
    }

    private void updateAssignedList() {
        assignedDisplayToName.clear();
        List<String> selected = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : allMagic.entrySet()) {
            if (magicData.hasMagic(entry.getValue())) {
                float split = magicData.getMagic(entry.getValue()).split;
                String display = entry.getKey() + " - " + Math.round(split * 100) + "%";
                assignedDisplayToName.put(display, entry.getKey());
                selected.add(display);
            }
        }
        if (assignedScroll != null) {
            assignedScroll.setList(selected);
        }
    }
}
