package noppes.npcs.client.gui.advanced;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.ability.CustomAbilitiesGetPacket;
import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumScrollData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * Simple ability name picker with optional filter mode.
 * Shows custom, built-in, or all abilities in a scroll list with search.
 * Returns the selected ability name.
 */
public class SubGuiAbilitySelect extends SubGuiInterface implements ICustomScrollListener, IScrollData {

    /** Show all abilities (custom + built-in). */
    public static final int FILTER_ALL = 0;
    /** Show only custom abilities. */
    public static final int FILTER_CUSTOM_ONLY = 1;
    /** Show only built-in abilities. */
    public static final int FILTER_BUILTIN_ONLY = 2;

    private GuiCustomScroll scroll;
    private HashMap<String, Integer> customData = new HashMap<>();
    private HashMap<String, Integer> builtInData = new HashMap<>();
    private String selectedName = null;
    private String search = "";
    private final int filterMode;

    public SubGuiAbilitySelect() {
        this(FILTER_ALL);
    }

    public SubGuiAbilitySelect(int filterMode) {
        this.filterMode = filterMode;
        setBackground("menubg.png");
        xSize = 220;
        ySize = 216;

        PacketClient.sendClient(new CustomAbilitiesGetPacket());
    }

    @Override
    public void initGui() {
        super.initGui();

        int y = guiTop + 5;

        addLabel(new GuiNpcLabel(0, "ability.select", guiLeft + 10, y));
        y += 14;

        if (scroll == null) {
            scroll = new GuiCustomScroll(this, 0);
            scroll.setSize(200, 140);
        }
        scroll.guiLeft = guiLeft + 10;
        scroll.guiTop = y;
        scroll.setList(getFilteredList());
        if (selectedName != null) {
            scroll.setSelected(selectedName);
        }
        addScroll(scroll);

        y += 143;

        addTextField(new GuiNpcTextField(10, this, fontRendererObj, guiLeft + 10, y, 200, 20, search));

        y += 24;

        addButton(new GuiNpcButton(0, guiLeft + 10, y, 95, 20, "gui.select"));
        addButton(new GuiNpcButton(2, guiLeft + 115, y, 95, 20, "gui.cancel"));

        getButton(0).setEnabled(selectedName != null);
    }

    private List<String> getFilteredList() {
        HashMap<String, Integer> merged = new HashMap<>();
        if (filterMode != FILTER_BUILTIN_ONLY) {
            merged.putAll(customData);
        }
        if (filterMode != FILTER_CUSTOM_ONLY) {
            merged.putAll(builtInData);
        }

        if (search.isEmpty()) {
            return new ArrayList<>(merged.keySet());
        }
        List<String> list = new ArrayList<>();
        for (String name : merged.keySet()) {
            if (name.toLowerCase().contains(search.toLowerCase())) {
                list.add(name);
            }
        }
        return list;
    }

    @Override
    public void buttonEvent(GuiButton guibutton) {
        int id = guibutton.id;

        if (id == 0 && selectedName != null) {
            close();
        } else if (id == 2) {
            selectedName = null;
            close();
        }
    }

    @Override
    public void keyTyped(char c, int i) {
        super.keyTyped(c, i);
        if (getTextField(10) != null && getTextField(10).isFocused()) {
            String newSearch = getTextField(10).getText();
            if (!search.equals(newSearch)) {
                search = newSearch;
                scroll.resetScroll();
                scroll.setList(getFilteredList());
            }
        }
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll scroll) {
        if (scroll.id == 0) {
            selectedName = scroll.getSelected();
            initGui();
        }
    }

    @Override
    public void customScrollDoubleClicked(String selection, GuiCustomScroll scroll) {
        if (scroll.id == 0 && selection != null && !selection.isEmpty()) {
            selectedName = selection;
            close();
        }
    }

    @Override
    public void setData(Vector<String> list, HashMap<String, Integer> data, EnumScrollData type) {
        if (type == EnumScrollData.CUSTOM_ABILITIES) {
            customData = data;
        } else if (type == EnumScrollData.BUILTIN_ABILITIES) {
            builtInData = data;
        }
        if (scroll != null) {
            scroll.setList(getFilteredList());
            if (selectedName != null) {
                scroll.setSelected(selectedName);
            }
        }
        initGui();
    }

    @Override
    public void setSelected(String selected) {
        this.selectedName = selected;
        if (scroll != null) {
            scroll.setSelected(selected);
        }
    }

    /**
     * Get the selected ability name after the dialog closes.
     * Returns null if cancelled.
     */
    public String getSelectedName() {
        return selectedName;
    }
}
