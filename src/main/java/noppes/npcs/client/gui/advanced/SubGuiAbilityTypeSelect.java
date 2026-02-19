package noppes.npcs.client.gui.advanced;

import kamkeel.npcs.controllers.AbilityController;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SubGui for selecting an ability type when creating a new ability.
 */
public class SubGuiAbilityTypeSelect extends SubGuiInterface implements ICustomScrollListener, ITextfieldListener {

    private GuiCustomScroll scroll;
    private final HashMap<String, String> displayNameToTypeId = new HashMap<>();
    private final HashMap<String, String> allDisplayNameToTypeId = new HashMap<>();
    private String selectedTypeId = null;
    private String search = "";

    public SubGuiAbilityTypeSelect() {
        setBackground("menubg.png");
        xSize = 200;
        ySize = 216;
    }

    @Override
    public void initGui() {
        super.initGui();

        addTextField(new GuiNpcTextField(10, this, fontRendererObj, guiLeft + 5, guiTop + 5, 190, 18, search));

        if (scroll == null) {
            scroll = new GuiCustomScroll(this, 0);
            scroll.setSize(190, 145);
        }
        scroll.guiLeft = guiLeft + 5;
        scroll.guiTop = guiTop + 26;
        addScroll(scroll);

        buildAllTypes();
        scroll.setUnsortedList(getFilteredTypeList());

        addButton(new GuiNpcButton(0, guiLeft + 5, guiTop + 188, 90, 20, "gui.add"));
        getButton(0).setEnabled(scroll.hasSelected());
        addButton(new GuiNpcButton(1, guiLeft + 105, guiTop + 188, 90, 20, "gui.cancel"));
    }

    private void buildAllTypes() {
        allDisplayNameToTypeId.clear();
        String[] types = AbilityController.Instance.getTypes();
        for (String typeId : types) {
            String displayName = I18n.format(typeId);
            if (AbilityController.Instance.isConcurrentCapableType(typeId)) {
                displayName = "\u00A7e" + displayName;
            }
            allDisplayNameToTypeId.put(displayName, typeId);
        }
    }

    private List<String> getFilteredTypeList() {
        List<String> list = new ArrayList<>();
        displayNameToTypeId.clear();
        for (Map.Entry<String, String> entry : allDisplayNameToTypeId.entrySet()) {
            String displayName = entry.getKey();
            String stripped = displayName.replaceAll("\u00A7.", "");
            if (search.isEmpty() || stripped.toLowerCase().contains(search) || entry.getValue().toLowerCase().contains(search)) {
                list.add(displayName);
                displayNameToTypeId.put(displayName, entry.getValue());
            }
        }
        Collections.sort(list, (a, b) -> String.CASE_INSENSITIVE_ORDER.compare(
            a.replaceAll("\u00A7.", ""), b.replaceAll("\u00A7.", "")));
        return list;
    }

    @Override
    public void buttonEvent(GuiButton guibutton) {
        if (guibutton.id == 0 && scroll.hasSelected()) {
            String displayName = scroll.getSelected();
            selectedTypeId = displayNameToTypeId.get(displayName);
            close();
        } else if (guibutton.id == 1) {
            selectedTypeId = null;
            close();
        }
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
        if (guiCustomScroll.id == 0) {
            getButton(0).setEnabled(scroll.hasSelected());
        }
    }

    @Override
    public void customScrollDoubleClicked(String selection, GuiCustomScroll guiCustomScroll) {
        if (guiCustomScroll.id == 0 && selection != null) {
            selectedTypeId = displayNameToTypeId.get(selection);
            close();
        }
    }

    @Override
    public void keyTyped(char c, int i) {
        super.keyTyped(c, i);
        if (getTextField(10) != null && getTextField(10).isFocused()) {
            if (!search.equals(getTextField(10).getText())) {
                search = getTextField(10).getText().toLowerCase();
                scroll.setList(getFilteredTypeList());
                scroll.resetScroll();
            }
        }
    }

    @Override
    public void unFocused(GuiNpcTextField textField) {
    }

    public String getSelectedTypeId() {
        return selectedTypeId;
    }
}
