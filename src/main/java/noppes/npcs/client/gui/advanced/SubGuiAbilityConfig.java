package noppes.npcs.client.gui.advanced;

import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.Condition;
import kamkeel.npcs.controllers.data.ability.gui.FieldDef;
import kamkeel.npcs.controllers.data.ability.gui.FieldType;
import kamkeel.npcs.controllers.data.ability.gui.TabTarget;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubGuiAbilityConfig extends SubGuiInterface implements ITextfieldListener, ISubGuiListener {

    private static final int DECLARATIVE_ID_START = 1000;
    private static final int CLEAR_ID_START = 2000;

    private static final int TAB_GENERAL = 0;
    private static final int TAB_TYPE = 1;
    private static final int TAB_TARGET = 2;
    private static final int TAB_EFFECTS = 3;
    private static final int TAB_VISUAL = 4;

    private final Ability ability;
    private final IAbilityConfigCallback callback;
    private int activeTab = TAB_GENERAL;

    private List<FieldDef> fieldDefs;
    private boolean hasTypeFields;
    private boolean hasVisualFields;

    // Conditions are complex (list with add/edit/remove) - cached separately
    private List<Condition> conditions;
    private int editingConditionIndex = -1;

    // Event routing
    private final Map<Integer, FieldDef> buttonFieldMap = new HashMap<>();
    private final Map<Integer, FieldDef> textFieldMap = new HashMap<>();
    private final Map<Integer, FieldDef> clearFieldMap = new HashMap<>();
    private FieldDef activeSubGuiField = null;

    public SubGuiAbilityConfig(Ability ability, IAbilityConfigCallback callback) {
        this.ability = ability;
        this.callback = callback;
        this.conditions = new ArrayList<>(ability.getConditions());

        this.fieldDefs = ability.getAllFieldDefinitions();
        this.hasTypeFields = hasFieldsForTab(TabTarget.TYPE);
        this.hasVisualFields = hasFieldsForTab(TabTarget.VISUAL);

        setBackground("menubg.png", 217);
        xSize = 356;
        ySize = 200;
    }

    private boolean hasFieldsForTab(TabTarget target) {
        for (FieldDef def : fieldDefs) {
            if (def.getTab() == target) return true;
        }
        return false;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // INIT GUI
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public void initGui() {
        super.initGui();
        buttonFieldMap.clear();
        textFieldMap.clear();
        clearFieldMap.clear();

        // Tab buttons
        GuiMenuTopButton generalTab = new GuiMenuTopButton(90, guiLeft + 4, guiTop - 17, "menu.general");
        generalTab.active = (activeTab == TAB_GENERAL);
        addTopButton(generalTab);
        GuiMenuTopButton lastTab = generalTab;

        if (hasTypeFields) {
            GuiMenuTopButton typeTab = new GuiMenuTopButton(91, lastTab, "gui.type");
            typeTab.active = (activeTab == TAB_TYPE);
            addTopButton(typeTab);
            lastTab = typeTab;
        }

        GuiMenuTopButton targetTab = new GuiMenuTopButton(92, lastTab, "script.target");
        targetTab.active = (activeTab == TAB_TARGET);
        addTopButton(targetTab);
        lastTab = targetTab;

        GuiMenuTopButton effectsTab = new GuiMenuTopButton(93, lastTab, "ability.tab.effects");
        effectsTab.active = (activeTab == TAB_EFFECTS);
        addTopButton(effectsTab);
        lastTab = effectsTab;

        if (hasVisualFields) {
            GuiMenuTopButton visualTab = new GuiMenuTopButton(94, lastTab, "ability.tab.visual");
            visualTab.active = (activeTab == TAB_VISUAL);
            addTopButton(visualTab);
        }

        GuiMenuTopButton closeBtn = new GuiMenuTopButton(-1000, guiLeft + xSize - 22, guiTop - 17, "X");
        addTopButton(closeBtn);

        // Scroll window for tab content
        int swX = guiLeft + 4;
        int swY = guiTop + 5;
        int swW = xSize - 8;
        int swH = ySize - 10;

        GuiScrollWindow sw = new GuiScrollWindow(this, swX, swY, swW, swH, 0);

        TabTarget tabTarget = tabTargetForIndex(activeTab);
        int y = 5;
        int idCounter = DECLARATIVE_ID_START;
        int clearCounter = CLEAR_ID_START;

        // General tab: show type label at top
        if (activeTab == TAB_GENERAL) {
            sw.addLabel(new GuiNpcLabel(1, "gui.type", 5, y + 5));
            sw.addLabel(new GuiNpcLabel(2, ability.getTypeId(), 50, y + 5));
            y += 24;
        }

        // Render declarative fields for this tab
        List<FieldDef> tabFields = getVisibleFieldsForTab(tabTarget);
        for (FieldDef def : tabFields) {
            int widgetId = idCounter++;
            int clearId = clearCounter++;
            y = renderField(sw, def, widgetId, clearId, y);
        }

        // Target tab: append conditions list
        if (activeTab == TAB_TARGET) {
            y = renderConditions(sw, y);
        }

        sw.maxScrollY = Math.max(y - swH, 0);
        addScrollableGui(0, sw);
    }

    private TabTarget tabTargetForIndex(int tab) {
        switch (tab) {
            case TAB_GENERAL: return TabTarget.GENERAL;
            case TAB_TYPE:    return TabTarget.TYPE;
            case TAB_TARGET:  return TabTarget.TARGET;
            case TAB_EFFECTS: return TabTarget.EFFECTS;
            case TAB_VISUAL:  return TabTarget.VISUAL;
            default:          return TabTarget.TYPE;
        }
    }

    private List<FieldDef> getVisibleFieldsForTab(TabTarget target) {
        List<FieldDef> result = new ArrayList<>();
        for (FieldDef def : fieldDefs) {
            if (def.getTab() == target && def.isVisible()) {
                result.add(def);
            }
        }
        return result;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // FIELD RENDERING
    // ═══════════════════════════════════════════════════════════════════════════

    @SuppressWarnings({"unchecked", "rawtypes"})
    private int renderField(GuiScrollWindow sw, FieldDef def, int widgetId, int clearId, int y) {
        int labelX = 5;
        int fieldX = 100;
        int fieldW = 60;

        sw.addLabel(new GuiNpcLabel(widgetId, def.getLabel(), labelX, y + 5));

        switch (def.getType()) {
            case FLOAT: {
                float fVal = def.getValue() instanceof Number ? ((Number) def.getValue()).floatValue() : 0f;
                GuiNpcTextField tf = new GuiNpcTextField(widgetId, this, fontRendererObj, fieldX, y, fieldW, 20, String.format("%.2f", fVal));
                tf.setFloatsOnly();
                if (def.hasRange()) tf.setMinMaxDefaultFloat(def.getMin(), def.getMax(), fVal);
                if (def.getTooltip() != null) tf.setHoverText(def.getTooltip());
                sw.addTextField(tf);
                textFieldMap.put(widgetId, def);
                break;
            }
            case INT: {
                int iVal = def.getValue() instanceof Number ? ((Number) def.getValue()).intValue() : 0;
                GuiNpcTextField tf = new GuiNpcTextField(widgetId, this, fontRendererObj, fieldX, y, fieldW, 20, String.valueOf(iVal));
                tf.setIntegersOnly();
                if (def.hasRange()) tf.setMinMaxDefault((int) def.getMin(), (int) def.getMax(), iVal);
                if (def.getTooltip() != null) tf.setHoverText(def.getTooltip());
                sw.addTextField(tf);
                textFieldMap.put(widgetId, def);
                break;
            }
            case STRING: {
                String sVal = def.getValue() != null ? def.getValue().toString() : "";
                GuiNpcTextField tf = new GuiNpcTextField(widgetId, this, fontRendererObj, fieldX, y, fieldW + 40, 20, sVal);
                if (def.getTooltip() != null) tf.setHoverText(def.getTooltip());
                sw.addTextField(tf);
                textFieldMap.put(widgetId, def);
                break;
            }
            case BOOLEAN: {
                boolean bVal = def.getValue() instanceof Boolean ? (Boolean) def.getValue() : false;
                GuiNpcButton btn = new GuiNpcButton(widgetId, fieldX, y, 40, 20, new String[]{"gui.no", "gui.yes"}, bVal ? 1 : 0);
                if (def.getHoverText() != null) btn.setHoverText(def.getHoverText());
                sw.addButton(btn);
                buttonFieldMap.put(widgetId, def);
                break;
            }
            case ENUM: {
                Class<? extends Enum<?>> enumClass = def.getEnumClass();
                if (enumClass != null) {
                    Enum<?>[] constants = enumClass.getEnumConstants();
                    String[] names = new String[constants.length];
                    for (int i = 0; i < constants.length; i++) names[i] = constants[i].toString();
                    int selected = def.getValue() instanceof Enum ? ((Enum<?>) def.getValue()).ordinal() : 0;
                    GuiNpcButton btn = new GuiNpcButton(widgetId, fieldX, y, 80, 20, names, selected);
                    if (def.getHoverText() != null) btn.setHoverText(def.getHoverText());
                    sw.addButton(btn);
                    buttonFieldMap.put(widgetId, def);
                }
                break;
            }
            case STRING_ENUM: {
                String[] values = def.getStringEnumValues();
                if (values != null && values.length > 0) {
                    String curVal = def.getStringEnumValue();
                    int selected = 0;
                    for (int i = 0; i < values.length; i++) {
                        if (values[i].equals(curVal)) { selected = i; break; }
                    }
                    GuiNpcButton btn = new GuiNpcButton(widgetId, fieldX, y, 80, 20, values, selected);
                    if (def.getHoverText() != null) btn.setHoverText(def.getHoverText());
                    sw.addButton(btn);
                    buttonFieldMap.put(widgetId, def);
                }
                break;
            }
            case SUB_GUI: {
                String btnText = def.getButtonLabel();
                int btnW = def.hasClearAction() ? 60 : 80;
                GuiNpcButton btn = new GuiNpcButton(widgetId, fieldX, y, btnW, 20, btnText);
                Integer textColor = def.getButtonTextColor();
                if (textColor != null) btn.setTextColor(textColor);
                if (def.getHoverText() != null) btn.setHoverText(def.getHoverText());
                sw.addButton(btn);
                buttonFieldMap.put(widgetId, def);

                if (def.hasClearAction()) {
                    GuiNpcButton clearBtn = new GuiNpcButton(clearId, fieldX + btnW + 5, y, 20, 20, "X");
                    sw.addButton(clearBtn);
                    clearFieldMap.put(clearId, def);
                }
                break;
            }
        }
        return y + 24;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CONDITIONS (Target tab special handling)
    // ═══════════════════════════════════════════════════════════════════════════

    private int renderConditions(GuiScrollWindow sw, int y) {
        int x = 5;

        sw.addLabel(new GuiNpcLabel(50, "ability.conditions", x, y));
        y += 14;

        for (int i = 0; i < conditions.size() && i < 3; i++) {
            Condition cond = conditions.get(i);
            String condName = getConditionDisplayName(cond);
            sw.addButton(new GuiNpcButton(50 + i * 10, x, y, 140, 20, condName));
            sw.addButton(new GuiNpcButton(51 + i * 10, x + 145, y, 40, 20, "gui.edit"));
            sw.addButton(new GuiNpcButton(52 + i * 10, x + 190, y, 20, 20, "X"));
            y += 22;
        }

        if (conditions.size() < 3) {
            sw.addButton(new GuiNpcButton(80, x, y, 50, 20, "gui.add"));
            y += 24;
        }

        return y;
    }

    private String getConditionDisplayName(Condition cond) {
        if (cond == null) return "None";
        String typeId = cond.getTypeId();
        switch (typeId) {
            case "hp_above":
                return StatCollector.translateToLocal("condition.hp_above") + " " + (int) (getConditionThreshold(cond) * 100) + "%";
            case "hp_below":
                return StatCollector.translateToLocal("condition.hp_below") + " " + (int) (getConditionThreshold(cond) * 100) + "%";
            case "target_hp_above":
                return StatCollector.translateToLocal("condition.target_hp_above") + " " + (int) (getConditionThreshold(cond) * 100) + "%";
            case "target_hp_below":
                return StatCollector.translateToLocal("condition.target_hp_below") + " " + (int) (getConditionThreshold(cond) * 100) + "%";
            case "hit_count":
                Condition.ConditionHitCount hc = (Condition.ConditionHitCount) cond;
                return StatCollector.translateToLocal("condition.hit_count") + ": " + hc.getRequiredHits() + "/" + hc.getWithinTicks() + "t";
            default:
                return typeId;
        }
    }

    private float getConditionThreshold(Condition cond) {
        NBTTagCompound nbt = cond.writeNBT();
        return nbt.hasKey("threshold") ? nbt.getFloat("threshold") : 0.5f;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // BUTTON EVENTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void buttonEvent(GuiButton guibutton) {
        int id = guibutton.id;

        // Tab switching
        if (id == 90) { activeTab = TAB_GENERAL; initGui(); return; }
        if (id == 91) { activeTab = TAB_TYPE; initGui(); return; }
        if (id == 92) { activeTab = TAB_TARGET; initGui(); return; }
        if (id == 93) { activeTab = TAB_EFFECTS; initGui(); return; }
        if (id == 94) { activeTab = TAB_VISUAL; initGui(); return; }
        if (id == -1000) { close(); return; }

        // Condition buttons (50-80)
        if (id >= 50 && id < 80) {
            int condIndex = (id - 50) / 10;
            int action = (id - 50) % 10;
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
            return;
        }
        if (id == 80) {
            if (conditions.size() < 3) {
                editingConditionIndex = conditions.size();
                setSubGui(new SubGuiConditionEdit(null));
            }
            return;
        }

        // Clear buttons
        if (id >= CLEAR_ID_START) {
            FieldDef def = clearFieldMap.get(id);
            if (def != null && def.hasClearAction()) {
                def.getClearAction().run();
                initGui();
            }
            return;
        }

        // Declarative field buttons
        if (id >= DECLARATIVE_ID_START) {
            FieldDef def = buttonFieldMap.get(id);
            if (def == null) return;

            switch (def.getType()) {
                case BOOLEAN:
                    def.setValue(((GuiNpcButton) guibutton).getValue() == 1);
                    initGui();
                    break;
                case ENUM: {
                    int idx = ((GuiNpcButton) guibutton).getValue();
                    Class<? extends Enum<?>> ec = def.getEnumClass();
                    if (ec != null) {
                        Enum<?>[] constants = ec.getEnumConstants();
                        if (idx >= 0 && idx < constants.length) def.setValue(constants[idx]);
                    }
                    initGui();
                    break;
                }
                case STRING_ENUM: {
                    int idx = ((GuiNpcButton) guibutton).getValue();
                    String[] values = def.getStringEnumValues();
                    if (values != null && idx >= 0 && idx < values.length) def.setStringEnumValue(values[idx]);
                    initGui();
                    break;
                }
                case SUB_GUI:
                    activeSubGuiField = def;
                    if (def.getSubGuiFactory() != null) setSubGui(def.getSubGuiFactory().get());
                    break;
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEXT FIELD EVENTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public void unFocused(GuiNpcTextField textField) {
        int id = textField.id;
        if (id < DECLARATIVE_ID_START) return;

        FieldDef def = textFieldMap.get(id);
        if (def == null) return;

        switch (def.getType()) {
            case FLOAT:
                try { def.setValue(Float.parseFloat(textField.getText())); } catch (NumberFormatException ignored) {}
                break;
            case INT:
                try { def.setValue(Integer.parseInt(textField.getText())); } catch (NumberFormatException ignored) {}
                break;
            case STRING:
                def.setValue(textField.getText());
                break;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SUB GUI CLOSED
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
        if (activeSubGuiField != null) {
            FieldDef def = activeSubGuiField;
            activeSubGuiField = null;
            if (def.getSubGuiResultHandler() != null) {
                def.getSubGuiResultHandler().accept(subgui);
            }
            initGui();
            return;
        }

        if (subgui instanceof SubGuiConditionEdit) {
            SubGuiConditionEdit condEdit = (SubGuiConditionEdit) subgui;
            Condition result = condEdit.getResult();
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
        applyToAbility();
        callback.onAbilitySaved(ability);
        super.close();
    }

    protected void applyToAbility() {
        // Conditions are the only cached values - write them back
        ability.getConditions().clear();
        for (Condition c : conditions) ability.addCondition(c);
        // All other fields write directly to ability via FieldDef lambdas
    }

    public void loadAbility(Ability loadedAbility) {
        if (loadedAbility == null) return;
        NBTTagCompound nbt = loadedAbility.writeNBT();
        nbt.setString("typeId", ability.getTypeId());
        ability.readNBT(nbt);

        this.conditions = new ArrayList<>(ability.getConditions());
        this.fieldDefs = ability.getAllFieldDefinitions();
        this.hasTypeFields = hasFieldsForTab(TabTarget.TYPE);
        this.hasVisualFields = hasFieldsForTab(TabTarget.VISUAL);
        initGui();
    }

    protected Ability getAbility() {
        return ability;
    }
}
