package noppes.npcs.client.gui.advanced;

import kamkeel.npcs.controllers.AbilityController;
import kamkeel.npcs.util.Register;
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

    private static final int BTN_ADD = 0;
    private static final int BTN_CANCEL = 1;
    private static final int BTN_SCROLL_TYPE = 2;
    private static final int TF_SEARCH = 10;

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

        int y = guiTop + 5;

        // Scroll type filter button
        addButton(new GuiNpcButton(BTN_SCROLL_TYPE, guiLeft + 5, y, 190, 20, GuiNPCAbilities.scrollType.toString()));
        y += 24;

        // Search bar
        addTextField(new GuiNpcTextField(TF_SEARCH, this, fontRendererObj, guiLeft + 5, y, 190, 18, search));
        y += 20;

        // Scroll list (shortened to fit filter button within original ySize)
        if (scroll == null) {
            scroll = new GuiCustomScroll(this, 0);
            scroll.setSize(190, 121);
        }
        scroll.guiLeft = guiLeft + 5;
        scroll.guiTop = y;
        addScroll(scroll);

        buildAllTypes();
        scroll.setUnsortedList(getFilteredTypeList());

        addButton(new GuiNpcButton(BTN_ADD, guiLeft + 5, guiTop + 188, 90, 20, "gui.add"));
        getButton(BTN_ADD).setEnabled(scroll.hasSelected());
        addButton(new GuiNpcButton(BTN_CANCEL, guiLeft + 105, guiTop + 188, 90, 20, "gui.cancel"));
    }

    private void buildAllTypes() {
        allDisplayNameToTypeId.clear();
        String[] types = AbilityController.Instance.getTypes();
        for (String typeId : types) {
            // Never show built-in types — they are immutable and cannot be created
            if (AbilityController.Instance.isBuiltInType(typeId))
                continue;

            // Apply scroll type filter
            if (!matchesScrollType(typeId))
                continue;

            String displayName = I18n.format(typeId);
            if (typeId.equals("ability.cnpc.custom")) {
                displayName = "\u00A7d" + displayName;
            } else if (AbilityController.Instance.isConcurrentCapableType(typeId)) {
                displayName = "\u00A7a" + displayName;
            }
            allDisplayNameToTypeId.put(displayName, typeId);
        }
    }

    private boolean matchesScrollType(String typeId) {
        GuiNPCAbilities.ScrollType type = GuiNPCAbilities.scrollType;
        if (type == GuiNPCAbilities.ScrollType.ALL) {
            return true;
        } else if (type == GuiNPCAbilities.ScrollType.CNPC) {
            return typeId.startsWith("ability.cnpc.");
        } else if (type == GuiNPCAbilities.ScrollType.MODDED) {
            if (!Register.isEmpty("ability")) {
                List<String> registerList = Register.REGISTERED_NAMESPACES.get("ability");
                if (!registerList.isEmpty()) {
                    String namespace = registerList.get(GuiNPCAbilities.modIndex);
                    return typeId.startsWith("ability." + namespace + ".");
                }
            }
            return false;
        }
        return true;
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

        // Pin Custom Ability to top of list
        String customEntry = null;
        for (String name : list) {
            String tid = displayNameToTypeId.get(name);
            if (tid != null && tid.equals("ability.cnpc.custom")) {
                customEntry = name;
                break;
            }
        }
        if (customEntry != null) {
            list.remove(customEntry);
            list.add(0, customEntry);
        }

        return list;
    }

    @Override
    public void buttonEvent(GuiButton guibutton) {
        int id = guibutton.id;

        if (id == BTN_ADD && scroll.hasSelected()) {
            String displayName = scroll.getSelected();
            selectedTypeId = displayNameToTypeId.get(displayName);
            close();
        } else if (id == BTN_CANCEL) {
            selectedTypeId = null;
            close();
        } else if (id == BTN_SCROLL_TYPE) {
            cycleScrollType();
            if (scroll != null) {
                scroll.resetScroll();
            }
            initGui();
        }
    }

    private void cycleScrollType() {
        if (GuiNPCAbilities.scrollType != GuiNPCAbilities.ScrollType.MODDED) {
            GuiNPCAbilities.ScrollType[] values = GuiNPCAbilities.ScrollType.values();
            GuiNPCAbilities.ScrollType next = values[(GuiNPCAbilities.scrollType.ordinal() + 1) % values.length];
            if (next == GuiNPCAbilities.ScrollType.MODDED && Register.isEmpty("ability")) {
                next = GuiNPCAbilities.ScrollType.ALL;
            }
            GuiNPCAbilities.scrollType = next;
        } else {
            List<String> list = Register.REGISTERED_NAMESPACES.get("ability");
            if (list != null && !list.isEmpty()) {
                if (GuiNPCAbilities.modIndex == list.size() - 1) {
                    GuiNPCAbilities.scrollType = GuiNPCAbilities.ScrollType.ALL;
                } else {
                    GuiNPCAbilities.modIndex = (GuiNPCAbilities.modIndex + 1) % list.size();
                }
            } else {
                GuiNPCAbilities.modIndex = 0;
                GuiNPCAbilities.scrollType = GuiNPCAbilities.ScrollType.ALL;
            }
        }
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
        if (guiCustomScroll.id == 0) {
            getButton(BTN_ADD).setEnabled(scroll.hasSelected());
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
        if (getTextField(TF_SEARCH) != null && getTextField(TF_SEARCH).isFocused()) {
            if (!search.equals(getTextField(TF_SEARCH).getText())) {
                search = getTextField(TF_SEARCH).getText().toLowerCase();
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
