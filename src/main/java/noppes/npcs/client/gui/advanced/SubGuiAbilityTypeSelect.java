package noppes.npcs.client.gui.advanced;

import kamkeel.npcs.controllers.data.ability.AbilityController;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.SubGuiInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * SubGui for selecting an ability type when creating a new ability.
 */
public class SubGuiAbilityTypeSelect extends SubGuiInterface implements ICustomScrollListener {

    private GuiCustomScroll scroll;
    private final HashMap<String, String> displayNameToTypeId = new HashMap<>();
    private String selectedTypeId = null;

    public SubGuiAbilityTypeSelect() {
        setBackground("menubg.png");
        xSize = 200;
        ySize = 216;
    }

    @Override
    public void initGui() {
        super.initGui();

        addLabel(new GuiNpcLabel(0, "ability.selectType", guiLeft + 5, guiTop + 5));

        if (scroll == null) {
            scroll = new GuiCustomScroll(this, 0);
            scroll.setSize(190, 165);
        }
        scroll.guiLeft = guiLeft + 5;
        scroll.guiTop = guiTop + 16;
        addScroll(scroll);

        List<String> list = buildTypeList();
        scroll.setList(list);

        addButton(new GuiNpcButton(0, guiLeft + 5, guiTop + 188, 90, 20, "gui.add"));
        getButton(0).setEnabled(scroll.hasSelected());
        addButton(new GuiNpcButton(1, guiLeft + 105, guiTop + 188, 90, 20, "gui.cancel"));
    }

    private List<String> buildTypeList() {
        List<String> list = new ArrayList<>();
        displayNameToTypeId.clear();
        String[] types = AbilityController.Instance.getTypes();
        for (String typeId : types) {
            String displayName = I18n.format(typeId);
            list.add(displayName);
            displayNameToTypeId.put(displayName, typeId);
        }
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

    public String getSelectedTypeId() {
        return selectedTypeId;
    }
}
