package noppes.npcs.client.gui.advanced;

import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.conditions.AbilityCondition;
import kamkeel.npcs.controllers.data.ability.gui.AbilityFieldBuilder;
import noppes.npcs.client.gui.script.GuiScriptInterface;
import noppes.npcs.controllers.data.AbilityScript;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.gui.builder.FieldDef;
import noppes.npcs.client.gui.util.GuiMenuTopButton;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.GuiScrollWindow;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;

import java.util.ArrayList;
import java.util.List;

import static kamkeel.npcs.controllers.data.ability.conditions.AbilityCondition.MAX_CONDITIONS;

public class SubGuiAbilityConfig extends SubGuiInterface implements ITextfieldListener, ISubGuiListener {

    private static final int DECLARATIVE_ID_START = 1000;
    private static final int CLEAR_ID_START = 2000;
    private static final int LABEL_ID_START = 3000;

    // Fixed tab indices
    private static final int TAB_GENERAL = 0;
    private static final int TAB_TYPE = 1;
    private static final int TAB_TARGET = 2;
    private static final int TAB_EFFECTS = 3;
    // Custom tabs start at index 4

    // Tab name strings (matching FieldDef.tab() values)
    private static final String TAB_NAME_GENERAL = "General";
    private static final String TAB_NAME_TYPE = "Type";
    private static final String TAB_NAME_TARGET = "Target";
    private static final String TAB_NAME_EFFECTS = "Effects";

    private static final int L_LABEL_X = 5;
    private static final int ROW_H = 24;

    // Condition buttons
    private static final int COND_BASE = 50;
    private static final int COND_STRIDE = 10;
    private static final int COND_END = COND_BASE + MAX_CONDITIONS * COND_STRIDE; // 50 + N*10
    private static final int BTN_ADD_COND = COND_END;

    private final Ability ability;
    private final IAbilityConfigCallback callback;
    private int activeTab = TAB_GENERAL;

    private List<FieldDef> fieldDefs;
    private List<String> customTabNames;

    private List<AbilityCondition> conditions;
    private int editingConditionIndex = -1;

    private AbilityFieldBuilder builder;

    // Scroll position preservation per tab
    private float[] tabScrollY;

    public SubGuiAbilityConfig(Ability ability, IAbilityConfigCallback callback) {
        this.ability = ability;
        this.callback = callback;
        this.conditions = new ArrayList<>(ability.getConditions());

        this.fieldDefs = ability.getAllDefinitions();
        discoverCustomTabs();

        setBackground("menubg.png", 217);
        xSize = 356;
        ySize = 200;
    }

    private void discoverCustomTabs() {
        customTabNames = new ArrayList<>();
        for (FieldDef def : fieldDefs) {
            String tab = def.getTab();
            if (tab != null
                && !TAB_NAME_GENERAL.equals(tab)
                && !TAB_NAME_TYPE.equals(tab)
                && !TAB_NAME_TARGET.equals(tab)
                && !TAB_NAME_EFFECTS.equals(tab)
                && !customTabNames.contains(tab)) {
                customTabNames.add(tab);
            }
        }
        tabScrollY = new float[4 + customTabNames.size()];
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // INIT GUI
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public void initGui() {
        // Commit any focused text field before rebuilding
        GuiNpcTextField.unfocus();

        // Save scroll position before super clears scroll windows
        GuiScrollWindow oldSw = getScrollableGui(0);
        if (oldSw != null && activeTab < tabScrollY.length) {
            tabScrollY[activeTab] = oldSw.nextScrollY;
        }

        super.initGui();

        // Tab buttons — always show General, Type, Target, Effects
        GuiMenuTopButton generalTab = new GuiMenuTopButton(90, guiLeft + 4, guiTop - 17, "menu.general");
        generalTab.active = (activeTab == TAB_GENERAL);
        addTopButton(generalTab);
        GuiMenuTopButton lastTab = generalTab;

        GuiMenuTopButton typeTab = new GuiMenuTopButton(91, lastTab, "gui.type");
        typeTab.active = (activeTab == TAB_TYPE);
        addTopButton(typeTab);
        lastTab = typeTab;

        GuiMenuTopButton targetTab = new GuiMenuTopButton(92, lastTab, "script.target");
        targetTab.active = (activeTab == TAB_TARGET);
        addTopButton(targetTab);
        lastTab = targetTab;

        GuiMenuTopButton effectsTab = new GuiMenuTopButton(93, lastTab, "ability.tab.effects");
        effectsTab.active = (activeTab == TAB_EFFECTS);
        addTopButton(effectsTab);
        lastTab = effectsTab;

        // Custom tabs (Visual, etc.)
        for (int i = 0; i < customTabNames.size(); i++) {
            GuiMenuTopButton ct = new GuiMenuTopButton(94 + i, lastTab, customTabNames.get(i));
            ct.active = (activeTab == 4 + i);
            addTopButton(ct);
            lastTab = ct;
        }

        GuiMenuTopButton closeBtn = new GuiMenuTopButton(-1000, guiLeft + xSize - 22, guiTop - 17, "X");
        addTopButton(closeBtn);

        GuiMenuTopButton scriptBtn = new GuiMenuTopButton(-999, "script.scripts", closeBtn);
        addTopButton(scriptBtn);

        // Build scroll window dimensions
        int swX = guiLeft + 4;
        int swY = guiTop + 5;
        int swW = xSize - 8;
        int swH = ySize - 10;

        int y = 5;
        int labelCounter = LABEL_ID_START;

        // General tab: type label + optional info labels at top — reserve space
        if (activeTab == TAB_GENERAL) {
            y += ROW_H;
            if ("ability.cnpc.custom".equals(ability.getTypeId())) {
                y += 15;
            }
            if (ability.isConcurrentCapable()) {
                y += 15;
            }
        }

        // Build declarative fields into an auto-created scroll window
        List<FieldDef> tabFields = getVisibleFieldsForTab(activeTab);
        builder = new AbilityFieldBuilder(this, fontRendererObj);
        builder.startIds(DECLARATIVE_ID_START, CLEAR_ID_START, labelCounter);
        builder.startY(y);

        GuiScrollWindow sw = builder.buildScrollWindow(tabFields, swX, swY, swW, swH);

        // General tab: add type label + optional info labels at top of scroll window
        if (activeTab == TAB_GENERAL) {
            int baseId = builder.getNextLabelId();
            int infoY = 25;
            sw.addLabel(new GuiNpcLabel(baseId, "gui.type", 5, 10, 0xFFFFFF));
            sw.addLabel(new GuiNpcLabel(baseId + 1, ability.getTypeId(), 55, 10, 0xFFFFFF));
            if ("ability.cnpc.custom".equals(ability.getTypeId())) {
                sw.addLabel(new GuiNpcLabel(baseId + 2,
                    StatCollector.translateToLocal("ability.customScriptInfo"), 5, infoY, 0xFF55FF));
                infoY += 15;
            }
            if (ability.isConcurrentCapable()) {
                sw.addLabel(new GuiNpcLabel(baseId + 3,
                    StatCollector.translateToLocal("ability.concurrentInfo"), 5, infoY, 0x55FF55));
            }
        }

        // Conditions on target tab — appended after field defs
        if (activeTab == TAB_TARGET) {
            int condY = renderConditions(sw, builder.getLastBuildY(), builder.getNextLabelId());
            sw.maxScrollY = Math.max(condY - swH, 0);
        }

        // Restore scroll position
        if (activeTab < tabScrollY.length) {
            float restored = Math.min(tabScrollY[activeTab], sw.maxScrollY);
            sw.nextScrollY = restored;
            sw.scrollY = restored;
        }
    }

    private List<FieldDef> getVisibleFieldsForTab(int tabIndex) {
        List<FieldDef> result = new ArrayList<>();

        String tabName;
        if (tabIndex >= 4) {
            int customIndex = tabIndex - 4;
            if (customIndex >= 0 && customIndex < customTabNames.size()) {
                tabName = customTabNames.get(customIndex);
            } else {
                return result;
            }
        } else {
            switch (tabIndex) {
                case TAB_GENERAL:
                    tabName = TAB_NAME_GENERAL;
                    break;
                case TAB_TYPE:
                    tabName = TAB_NAME_TYPE;
                    break;
                case TAB_TARGET:
                    tabName = TAB_NAME_TARGET;
                    break;
                case TAB_EFFECTS:
                    tabName = TAB_NAME_EFFECTS;
                    break;
                default:
                    tabName = TAB_NAME_TYPE;
                    break;
            }
        }

        for (FieldDef def : fieldDefs) {
            if (tabName.equals(def.getTab()) && def.isVisible()) {
                result.add(def);
            }
        }

        return result;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CONDITIONS (Target tab)
    // ═══════════════════════════════════════════════════════════════════════════

    private int renderConditions(GuiScrollWindow sw, int y, int labelCounter) {
        y += 3;
        sw.addLabel(new GuiNpcLabel(labelCounter, "ability.conditions", L_LABEL_X, y + 2, 0xFFFF55));
        y += 15;

        for (int i = 0; i < conditions.size() && i < MAX_CONDITIONS; i++) {
            AbilityCondition cond = conditions.get(i);
            String condName = getConditionDisplayName(cond);
            GuiNpcButton condBtn = new GuiNpcButton(COND_BASE + i * COND_STRIDE, L_LABEL_X, y, 140, 20, condName);
            condBtn.hoverableText = cond.getConditionSummary();
            sw.addButton(condBtn);
            sw.addButton(new GuiNpcButton(COND_BASE + i * COND_STRIDE + 1, L_LABEL_X + 145, y,  40, 20, "gui.edit"));
            sw.addButton(new GuiNpcButton(COND_BASE + i * COND_STRIDE + 2, L_LABEL_X + 190, y,  20, 20, "X"));
            y += 22;
        }

        if (conditions.size() < MAX_CONDITIONS) {
            sw.addButton(new GuiNpcButton(BTN_ADD_COND, L_LABEL_X, y, 50, 20, "gui.add"));;
            y += ROW_H;
        }

        return y;
    }

    private String getConditionDisplayName(AbilityCondition cond) {
        if (cond == null) return "None";
        String name = cond.getName();
        return StatCollector.translateToLocal(name);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // BUTTON EVENTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public void buttonEvent(GuiButton guibutton) {
        int id = guibutton.id;

        // Tab switching — fixed tabs
        if (id == 90) {
            activeTab = TAB_GENERAL;
            initGui();
            return;
        }
        if (id == 91) {
            activeTab = TAB_TYPE;
            initGui();
            return;
        }
        if (id == 92) {
            activeTab = TAB_TARGET;
            initGui();
            return;
        }
        if (id == 93) {
            activeTab = TAB_EFFECTS;
            initGui();
            return;
        }
        // Custom tabs (94+)
        if (id >= 94 && id < 94 + customTabNames.size()) {
            activeTab = 4 + (id - 94);
            initGui();
            return;
        }
        if (id == -999) {
            GuiNpcTextField.unfocus();
            applyToAbility();
            callback.onAbilitySaved(ability);
            GuiScriptInterface.open(parent, new AbilityScript(ability.getId()));
            return;
        }
        if (id == -1000) {
            close();
            return;
        }

        // Condition buttons (50-80)
        if (handleConditionButton(id)) return;

        // Declarative field handling (effects list, booleans, enums, sub-guis, clear buttons)
        if (builder.handleButtonEvent(id, guibutton)) {
            // SUB_GUI fields are opened directly by the builder via setSubGuiWithResult().
            // Only rebuild if no sub-gui was opened (e.g. boolean toggle, enum change).
            if (!hasSubGui()) {
                initGui();
            }
            return;
        }
    }

    private boolean handleConditionButton(int id) {
        if (id >= COND_BASE && id < COND_END) {
            int condIndex = (id - COND_BASE) / COND_STRIDE;
            int action    = (id - COND_BASE) % COND_STRIDE;
            if (action == 0 || action == 1) {
                if (condIndex < conditions.size()) {
                    editingConditionIndex = condIndex;
                    setSubGui(new SubGuiConditionEdit(conditions.get(condIndex)));
                }
            } else if (action == 2) {
                if (condIndex < conditions.size()) {
                    conditions.remove(condIndex);
                    initGui();
                }
            }
            return true;
        }
        if (id == BTN_ADD_COND) {
            if (conditions.size() < MAX_CONDITIONS) {
                editingConditionIndex = conditions.size();
                setSubGui(new SubGuiConditionEdit(null));
            }
            return true;
        }
        return false;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEXT FIELD EVENTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public void unFocused(GuiNpcTextField textField) {
        if (textField.id < DECLARATIVE_ID_START) return;
        if (builder.handleTextFieldEvent(textField.id, textField)) {
            initGui();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SUB GUI CLOSED
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
        if (builder.handleSubGuiClosed(subgui)) {
            initGui();
            return;
        }

        if (subgui instanceof SubGuiConditionEdit) {
            SubGuiConditionEdit condEdit = (SubGuiConditionEdit) subgui;
            AbilityCondition result = condEdit.getResult();
            if (result != null && editingConditionIndex >= 0) {
                if (editingConditionIndex < conditions.size()) {
                    conditions.set(editingConditionIndex, result);
                } else {
                    conditions.add(result);
                }
            }
            editingConditionIndex = -1;
            initGui();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CLOSE / APPLY
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public void close() {
        GuiNpcTextField.unfocus();
        applyToAbility();
        callback.onAbilitySaved(ability);
        super.close();
    }

    protected void applyToAbility() {
        ability.getConditions().clear();
        for (AbilityCondition c : conditions) ability.addCondition(c);
    }

    public void loadAbility(Ability loadedAbility) {
        if (loadedAbility == null) return;
        NBTTagCompound nbt = loadedAbility.writeNBT(false);
        nbt.setString("typeId", ability.getTypeId());
        ability.readNBT(nbt);

        this.conditions = new ArrayList<>(ability.getConditions());
        this.fieldDefs = ability.getAllDefinitions();
        discoverCustomTabs();
        initGui();
    }

    protected Ability getAbility() {
        return ability;
    }
}
