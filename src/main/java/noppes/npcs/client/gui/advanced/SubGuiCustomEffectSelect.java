package noppes.npcs.client.gui.advanced;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.controllers.CustomEffectController;
import noppes.npcs.controllers.data.CustomEffect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SubGui for selecting a custom effect from CustomEffectController.
 */
@SideOnly(Side.CLIENT)
public class SubGuiCustomEffectSelect extends SubGuiInterface implements ICustomScrollListener, ITextfieldListener {

    private GuiCustomScroll scroll;
    private final HashMap<String, Integer> displayNameToId = new HashMap<>();
    private final HashMap<String, Integer> allDisplayNameToId = new HashMap<>();
    private int selectedEffectId = -1;
    private String search = "";
    private final int preselectedId;

    public SubGuiCustomEffectSelect(int preselectedId) {
        this.preselectedId = preselectedId;
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

        buildAllEffects();
        List<String> filtered = getFilteredList();
        scroll.setUnsortedList(filtered);

        // Pre-select current effect
        if (preselectedId >= 0 && !scroll.hasSelected()) {
            for (String name : filtered) {
                Integer id = displayNameToId.get(name);
                if (id != null && id == preselectedId) {
                    scroll.setSelected(name);
                    break;
                }
            }
        }

        addButton(new GuiNpcButton(0, guiLeft + 5, guiTop + 188, 90, 20, "gui.select"));
        getButton(0).setEnabled(scroll.hasSelected());
        addButton(new GuiNpcButton(1, guiLeft + 105, guiTop + 188, 90, 20, "gui.cancel"));
    }

    private void buildAllEffects() {
        allDisplayNameToId.clear();
        HashMap<Integer, CustomEffect> effects = CustomEffectController.getInstance().getCustomEffects();
        if (effects == null) return;
        for (CustomEffect ce : effects.values()) {
            if (ce.getName() != null && !ce.getName().isEmpty()) {
                allDisplayNameToId.put(ce.getName(), ce.id);
            }
        }
    }

    private List<String> getFilteredList() {
        List<String> list = new ArrayList<>();
        displayNameToId.clear();
        for (Map.Entry<String, Integer> entry : allDisplayNameToId.entrySet()) {
            String name = entry.getKey();
            if (search.isEmpty() || name.toLowerCase().contains(search)) {
                list.add(name);
                displayNameToId.put(name, entry.getValue());
            }
        }
        Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
        return list;
    }

    @Override
    public void buttonEvent(GuiButton guibutton) {
        if (guibutton.id == 0 && scroll.hasSelected()) {
            String name = scroll.getSelected();
            Integer id = displayNameToId.get(name);
            selectedEffectId = id != null ? id : -1;
            close();
        } else if (guibutton.id == 1) {
            selectedEffectId = -1;
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
            Integer id = displayNameToId.get(selection);
            selectedEffectId = id != null ? id : -1;
            close();
        }
    }

    @Override
    public void keyTyped(char c, int i) {
        super.keyTyped(c, i);
        if (getTextField(10) != null && getTextField(10).isFocused()) {
            String newSearch = getTextField(10).getText().toLowerCase();
            if (!search.equals(newSearch)) {
                search = newSearch;
                scroll.setUnsortedList(getFilteredList());
                scroll.resetScroll();
            }
        }
    }

    @Override
    public void unFocused(GuiNpcTextField textField) {
    }

    public int getSelectedEffectId() {
        return selectedEffectId;
    }
}
