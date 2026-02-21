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

    // Left panel
    private static final int LEFT_WIDTH = 110;
    private static final int LEFT_MARGIN = 5;

    // Right panel
    private static final int RIGHT_GAP = 5;

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

        int y = guiTop + 5;

        // Namespace filter button (full width at top)
        addButton(new GuiNpcButton(BTN_NAMESPACE, guiLeft + LEFT_MARGIN, y, xSize - LEFT_MARGIN * 2, 20, getNamespaceFilterLabel()));
        y += 24;

        // Build filtered type list
        filteredTypeIds = getFilteredTypes();
        displayNameToTypeId.clear();
        List<String> scrollList = new ArrayList<>();
        for (String typeId : filteredTypeIds) {
            Supplier<AbilityCondition> factory = AbilityController.Instance.getConditionType(typeId);
            String displayName = factory != null ? I18n.format(factory.get().getName()) : typeId;
            scrollList.add(displayName);
            displayNameToTypeId.put(displayName, typeId);
        }

        // Left panel: condition type scroll list
        int scrollHeight = ySize - 58;
        scroll = new GuiCustomScroll(this, 0);
        scroll.guiLeft = guiLeft + LEFT_MARGIN;
        scroll.guiTop = y;
        scroll.setSize(LEFT_WIDTH, scrollHeight);
        scroll.setUnsortedList(scrollList);

        // Pre-select current condition type in scroll
        if (selectedTypeId != null) {
            for (int i = 0; i < filteredTypeIds.length; i++) {
                if (filteredTypeIds[i].equals(selectedTypeId)) {
                    scroll.selected = i;
                    break;
                }
            }
        }
        addScroll(scroll);

        // Right panel: condition config via FieldDef
        int rightX = guiLeft + LEFT_MARGIN + LEFT_WIDTH + RIGHT_GAP;
        int rightW = xSize - LEFT_MARGIN * 2 - LEFT_WIDTH - RIGHT_GAP;

        List<FieldDef> fields = new ArrayList<>();
        if (condition != null) {
            fields = condition.getAllDefinitions();
        }

        builder = new AbilityFieldBuilder(this, fontRendererObj);
        builder.startIds(DECLARATIVE_ID_START, CLEAR_ID_START, LABEL_ID_START);
        builder.startY(5);
        builder.buildScrollWindow(fields, rightX, y, rightW, scrollHeight);

        // Bottom buttons
        int btnY = guiTop + ySize - 26;
        addButton(new GuiNpcButton(BTN_CANCEL, guiLeft + LEFT_MARGIN, btnY, 60, 20, "gui.cancel"));
        addButton(new GuiNpcButton(BTN_DONE, guiLeft + xSize - LEFT_MARGIN - 60, btnY, 60, 20, "gui.done"));
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
