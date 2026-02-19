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
 * Supports switching between effect maps (index 0 = custom, addon indices via toggle button).
 */
@SideOnly(Side.CLIENT)
public class SubGuiCustomEffectSelect extends SubGuiInterface implements ICustomScrollListener, ITextfieldListener {

    private GuiCustomScroll scroll;
    private final HashMap<String, Integer> displayNameToId = new HashMap<>();
    private final HashMap<String, Integer> allDisplayNameToId = new HashMap<>();
    private int selectedEffectId = -1;
    private int selectedIndex = 0;
    private String search = "";
    private final int preselectedId;
    private final int preselectedIndex;

    /** The effect map index currently being viewed */
    private int viewIndex = 0;

    /** Ordered list of available indices for toggling */
    private List<Integer> availableIndices;

    public SubGuiCustomEffectSelect(int preselectedId) {
        this(preselectedId, 0);
    }

    public SubGuiCustomEffectSelect(int preselectedId, int preselectedIndex) {
        this.preselectedId = preselectedId;
        this.preselectedIndex = preselectedIndex;
        this.viewIndex = preselectedIndex;
        setBackground("menubg.png");
        xSize = 200;
        ySize = 216;
    }

    @Override
    public void initGui() {
        super.initGui();

        buildAvailableIndices();

        addTextField(new GuiNpcTextField(10, this, fontRendererObj, guiLeft + 5, guiTop + 5, 190, 18, search));

        // Toggle button for switching between effect lists (only if addon indices exist)
        if (availableIndices.size() > 1) {
            String label = getViewLabel();
            addButton(new GuiNpcButton(2, guiLeft + 5, guiTop + 26, 190, 20, label));
        }

        int scrollTop = availableIndices.size() > 1 ? guiTop + 49 : guiTop + 26;
        int scrollHeight = availableIndices.size() > 1 ? 122 : 145;

        if (scroll == null) {
            scroll = new GuiCustomScroll(this, 0);
        }
        scroll.setSize(190, scrollHeight);
        scroll.guiLeft = guiLeft + 5;
        scroll.guiTop = scrollTop;
        addScroll(scroll);

        buildAllEffects();
        List<String> filtered = getFilteredList();
        scroll.setUnsortedList(filtered);

        // Pre-select current effect (only if viewing the same index)
        if (preselectedId >= 0 && viewIndex == preselectedIndex && !scroll.hasSelected()) {
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

    private void buildAvailableIndices() {
        availableIndices = new ArrayList<>();
        CustomEffectController controller = CustomEffectController.getInstance();

        // Always include index 0
        availableIndices.add(0);

        // Add any addon indices that have registered labels
        HashMap<Integer, String> labels = controller.getIndexLabels();
        for (int idx : labels.keySet()) {
            if (idx != 0 && controller.getEffectMap(idx) != null) {
                availableIndices.add(idx);
            }
        }
        Collections.sort(availableIndices);

        // Ensure viewIndex is valid
        if (!availableIndices.contains(viewIndex)) {
            viewIndex = 0;
        }
    }

    private String getViewLabel() {
        if (viewIndex == 0) {
            return "Custom Effects";
        }
        HashMap<Integer, String> labels = CustomEffectController.getInstance().getIndexLabels();
        String label = labels.get(viewIndex);
        return label != null ? label : "Index " + viewIndex;
    }

    private void buildAllEffects() {
        allDisplayNameToId.clear();
        HashMap<Integer, CustomEffect> effects = CustomEffectController.getInstance().getEffectMap(viewIndex);
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
            selectedIndex = viewIndex;
            close();
        } else if (guibutton.id == 1) {
            selectedEffectId = -1;
            close();
        } else if (guibutton.id == 2) {
            // Toggle to next available index
            int currentPos = availableIndices.indexOf(viewIndex);
            int nextPos = (currentPos + 1) % availableIndices.size();
            viewIndex = availableIndices.get(nextPos);
            search = "";
            scroll = null;
            initGui();
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
            selectedIndex = viewIndex;
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

    public int getSelectedIndex() {
        return selectedIndex;
    }
}
