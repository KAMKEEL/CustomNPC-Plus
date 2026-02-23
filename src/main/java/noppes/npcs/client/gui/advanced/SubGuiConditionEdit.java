package noppes.npcs.client.gui.advanced;

import kamkeel.npcs.controllers.AbilityController;
import kamkeel.npcs.controllers.data.ability.conditions.AbilityCondition;
import kamkeel.npcs.util.Register;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import noppes.npcs.client.gui.builder.FieldDef;
import noppes.npcs.client.gui.util.*;
import kamkeel.npcs.controllers.data.ability.gui.AbilityFieldBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * SubGui for editing a single AbilityCondition.
 *
 * Split-panel layout:
 *   Left  – Mod namespace filter + scrollable condition type list
 *   Right – FieldDef configuration for the selected condition
 */
public class SubGuiConditionEdit extends SubGuiInterface implements ITextfieldListener, ISubGuiListener, ICustomScrollListener {

    private static final int DECLARATIVE_ID_START = 1000;
    private static final int CLEAR_ID_START = 2000;
    private static final int LABEL_ID_START = 3000;

    private static final int BTN_NAMESPACE = 1;
    private static final int BTN_CANCEL = 10;
    private static final int BTN_DONE = 11;
    private static final int TF_SEARCH = 12;

    // Left panel
    private static final int LEFT_WIDTH = 110;
    private static final int LEFT_MARGIN = 5;

    // Right panel
    private static final int RIGHT_GAP = 5;

    private String searchText = "";

    private AbilityCondition condition;
    private AbilityCondition result;
    private String selectedTypeId;

    // Namespace filter
    private int namespaceFilter = 0; // 0 = ALL, 1 = cnpc, 2+ = modded namespaces
    private List<String> modNamespaces;

    // Type list
    private GuiCustomScroll scroll;
    private final HashMap<String, String> displayNameToTypeId = new HashMap<>();
    private String[] filteredTypeIds;

    private AbilityFieldBuilder builder;

    public SubGuiConditionEdit(AbilityCondition existing) {
        this.result = null;
        this.modNamespaces = getModNamespaces();

        if (existing != null) {
            this.condition = existing;
            this.selectedTypeId = existing.getTypeId();
            // Set namespace filter to match existing condition
            setNamespaceForTypeId(selectedTypeId);
        } else {
            // Default to first available condition
            String[] allTypes = AbilityController.Instance.getConditionTypes();
            if (allTypes.length > 0) {
                this.selectedTypeId = allTypes[0];
                this.condition = spawnCondition(selectedTypeId);
            }
        }

        setBackground("menubg.png", 217);
        xSize = 356;
        ySize = 200;
    }

    private List<String> getModNamespaces() {
        List<String> result = new ArrayList<>();
        List<String> registered = Register.REGISTERED_NAMESPACES.get("condition");
        if (registered != null) {
            result.addAll(registered);
        }
        return result;
    }

    private void setNamespaceForTypeId(String typeId) {
        if (typeId == null) return;
        String[] parts = typeId.split("\\.", 3);
        if (parts.length < 2) return;
        String ns = parts[1];

        if ("cnpc".equals(ns)) {
            namespaceFilter = 1;
        } else {
            for (int i = 0; i < modNamespaces.size(); i++) {
                if (modNamespaces.get(i).equals(ns)) {
                    namespaceFilter = 2 + i;
                    return;
                }
            }
            namespaceFilter = 0; // Fallback to ALL
        }
    }

    private String getNamespaceFilterLabel() {
        if (namespaceFilter == 0) return "ALL";
        if (namespaceFilter == 1) return "CNPC";
        int modIdx = namespaceFilter - 2;
        if (modIdx >= 0 && modIdx < modNamespaces.size()) {
            String ns = modNamespaces.get(modIdx);
            String display = Register.NAMESPACE_DISPLAY_NAMES.get(ns);
            return display != null ? display : ns.toUpperCase();
        }
        return "ALL";
    }

    private void cycleNamespaceFilter() {
        namespaceFilter++;
        // 0=ALL, 1=CNPC, 2..2+N-1=mod namespaces, then wrap
        int max = 2 + modNamespaces.size();
        if (namespaceFilter >= max) {
            namespaceFilter = 0;
        }
        // Skip empty mod namespace slots
        if (namespaceFilter >= 2) {
            int modIdx = namespaceFilter - 2;
            if (modIdx >= modNamespaces.size()) {
                namespaceFilter = 0;
            }
        }
    }

    private String[] getFilteredTypes() {
        if (namespaceFilter == 0) {
            return AbilityController.Instance.getConditionTypes();
        } else if (namespaceFilter == 1) {
            return AbilityController.Instance.getConditionTypesByNamespace("cnpc");
        } else {
            int modIdx = namespaceFilter - 2;
            if (modIdx >= 0 && modIdx < modNamespaces.size()) {
                return AbilityController.Instance.getConditionTypesByNamespace(modNamespaces.get(modIdx));
            }
            return AbilityController.Instance.getConditionTypes();
        }
    }

    private AbilityCondition spawnCondition(String typeId) {
        if (typeId == null) return null;
        Supplier<AbilityCondition> factory = AbilityController.Instance.getConditionType(typeId);
        return factory != null ? factory.get() : null;
    }

    @Override
    public void initGui() {
        GuiNpcTextField.unfocus();
        super.initGui();

        int leftX = guiLeft + LEFT_MARGIN;
        int btnY = guiTop + ySize - 26;

        // Namespace filter button (above left scroll only)
        addButton(new GuiNpcButton(BTN_NAMESPACE, leftX, guiTop + 5, LEFT_WIDTH, 20, getNamespaceFilterLabel()));

        // Build filtered type list
        filteredTypeIds = getFilteredTypes();
        displayNameToTypeId.clear();
        List<String> scrollList = new ArrayList<>();
        for (String typeId : filteredTypeIds) {
            Supplier<AbilityCondition> factory = AbilityController.Instance.getConditionType(typeId);
            String displayName = factory != null ? I18n.format(factory.get().getName()) : typeId;
            if (!searchText.isEmpty() && !displayName.toLowerCase().contains(searchText.toLowerCase())) continue;
            scrollList.add(displayName);
            displayNameToTypeId.put(displayName, typeId);
        }

        // Left panel: condition type scroll list (between namespace button and search bar)
        int scrollTop = guiTop + 29;
        int scrollBottom = btnY - 24;
        int scrollHeight = scrollBottom - scrollTop;
        if (scroll == null) {
            scroll = new GuiCustomScroll(this, 0);
        }
        scroll.guiLeft = leftX;
        scroll.guiTop = scrollTop;
        scroll.setSize(LEFT_WIDTH, scrollHeight);
        scroll.setUnsortedList(scrollList);

        // Pre-select current condition type in scroll
        if (selectedTypeId != null) {
            for (int i = 0; i < scrollList.size(); i++) {
                String typeId = displayNameToTypeId.get(scrollList.get(i));
                if (typeId != null && typeId.equals(selectedTypeId)) {
                    scroll.selected = i;
                    break;
                }
            }
        }
        addScroll(scroll);

        // Search text field (below left scroll)
        GuiNpcTextField searchField = new GuiNpcTextField(TF_SEARCH, this, fontRendererObj, leftX, scrollBottom + 2, LEFT_WIDTH, 20, searchText);
        searchField.setHoverText("gui.search");
        addTextField(searchField);

        // Right panel: condition config via FieldDef (full height from top to buttons)
        int rightX = leftX + LEFT_WIDTH + RIGHT_GAP;
        int rightW = xSize - LEFT_MARGIN * 2 - LEFT_WIDTH - RIGHT_GAP;
        int rightTop = guiTop + 5;
        int rightHeight = btnY - rightTop - 4;

        List<FieldDef> fields = new ArrayList<>();
        if (condition != null) {
            fields = condition.getAllDefinitions();
        }

        builder = new AbilityFieldBuilder(this, fontRendererObj);
        builder.startIds(DECLARATIVE_ID_START, CLEAR_ID_START, LABEL_ID_START);
        builder.contentRight(rightW);
        builder.startY(5);
        builder.buildScrollWindow(fields, rightX, rightTop, rightW, rightHeight);

        // Bottom buttons
        addButton(new GuiNpcButton(BTN_CANCEL, leftX, btnY, 60, 20, "gui.cancel"));
        GuiNpcButton doneBtn = new GuiNpcButton(BTN_DONE, guiLeft + xSize - LEFT_MARGIN - 60, btnY, 60, 20, "gui.done");
        if (condition == null || !condition.isConfigured()) {
            doneBtn.setEnabled(false);
        }
        addButton(doneBtn);
    }

    @Override
    public void buttonEvent(GuiButton guibutton) {
        int id = guibutton.id;

        if (id == BTN_NAMESPACE) {
            cycleNamespaceFilter();
            initGui();
            return;
        }

        if (id == BTN_CANCEL) {
            result = null;
            close();
            return;
        }

        if (id == BTN_DONE) {
            result = condition;
            close();
            return;
        }

        if (builder != null && builder.handleButtonEvent(id, guibutton)) {
            if (!hasSubGui()) {
                initGui();
            }
        }
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
        if (guiCustomScroll.id == 0 && scroll.hasSelected()) {
            String displayName = scroll.getSelected();
            String typeId = displayNameToTypeId.get(displayName);
            if (typeId != null && !typeId.equals(selectedTypeId)) {
                selectedTypeId = typeId;
                condition = spawnCondition(selectedTypeId);
                initGui();
            }
        }
    }

    @Override
    public void customScrollDoubleClicked(String selection, GuiCustomScroll guiCustomScroll) {
        // Same as single click — user still needs to configure fields
    }

    @Override
    public void unFocused(GuiNpcTextField textField) {
        if (textField.id == TF_SEARCH) {
            String newText = textField.getText();
            if (!newText.equals(searchText)) {
                searchText = newText;
                initGui();
            }
            return;
        }
        if (textField.id < DECLARATIVE_ID_START) return;
        if (builder != null && builder.handleTextFieldEvent(textField.id, textField)) {
            initGui();
        }
    }

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
        if (builder != null && builder.handleSubGuiClosed(subgui)) {
            initGui();
        }
    }

    public AbilityCondition getResult() {
        return result;
    }
}
