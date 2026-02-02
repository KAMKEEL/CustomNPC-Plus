package kamkeel.npcs.controllers.data.ability.gui;

import kamkeel.npcs.controllers.data.ability.AbilityEffect;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcButtonYesNo;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.GuiScrollWindow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reusable builder that converts a list of {@link FieldDef} into positioned
 * GUI widgets inside a {@link GuiScrollWindow}.
 */
public class FieldDefinitionsBuilder {

    // Layout defaults
    private int contentRight = 330;
    private int colLLabel = 5;
    private int colLField = 75;
    private int colLWidth = 70;
    private int colRLabel = 178;
    private int colRField = 248;
    private int colRWidth = 70;
    private int rowHeight = 24;
    private int labelPadding = 8;

    // Dependencies
    private final GuiScreen parent;
    private final GuiScrollWindow sw;
    private final FontRenderer fontRenderer;

    // ID counters
    private int widgetId;
    private int clearId;
    private int labelId;

    // Starting Y
    private int startY = 5;

    // Output maps
    private final Map<Integer, FieldDef> buttonFieldMap = new HashMap<>();
    private final Map<Integer, FieldDef> textFieldMap = new HashMap<>();
    private final Map<Integer, FieldDef> clearFieldMap = new HashMap<>();

    // Effects list metadata: widgetId -> [effectIndex, action]
    // action: 0=type, 1=duration(text), 2=amp, 3=delete, 4=add
    private final Map<Integer, int[]> effectWidgetMeta = new HashMap<>();

    public FieldDefinitionsBuilder(GuiScreen parent, GuiScrollWindow sw, FontRenderer fontRenderer) {
        this.parent = parent;
        this.sw = sw;
        this.fontRenderer = fontRenderer;
    }

    // ═══════════════════════════════════════════════════════════════════
    // CONFIG CHAINING
    // ═══════════════════════════════════════════════════════════════════

    public FieldDefinitionsBuilder contentRight(int v) { this.contentRight = v; return this; }
    public FieldDefinitionsBuilder startIds(int widget, int clear, int label) {
        this.widgetId = widget; this.clearId = clear; this.labelId = label; return this;
    }
    public FieldDefinitionsBuilder startY(int y) { this.startY = y; return this; }
    public FieldDefinitionsBuilder rowHeight(int h) { this.rowHeight = h; return this; }

    // ═══════════════════════════════════════════════════════════════════
    // BUILD
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Processes all fields and adds widgets to the scroll window.
     * @return the final Y position after all fields
     */
    public int build(List<FieldDef> fields) {
        buttonFieldMap.clear();
        textFieldMap.clear();
        clearFieldMap.clear();
        effectWidgetMeta.clear();

        int y = startY;

        for (int i = 0; i < fields.size(); i++) {
            FieldDef def = fields.get(i);

            // Section header
            if (def.getType() == FieldType.SECTION_HEADER) {
                y += 3;
                String sectionText = StatCollector.translateToLocal(def.getLabel());
                if (def.getTooltip() != null && !def.getTooltip().isEmpty()) {
                    sectionText += " (" + StatCollector.translateToLocal(def.getTooltip()) + ")";
                }
                sw.addLabel(new GuiNpcLabel(labelId++, sectionText, colLLabel, y + 2, 0xFFFF55));
                y += 15;
                continue;
            }

            // Effects list renders its own multi-row block
            if (def.getType() == FieldType.EFFECTS_LIST) {
                y = renderEffectsList(def, y);
                continue;
            }

            // Two-column pair: LEFT followed by RIGHT
            if (def.getColumn() == ColumnHint.LEFT
                    && i + 1 < fields.size()
                    && fields.get(i + 1).getColumn() == ColumnHint.RIGHT
                    && fields.get(i + 1).getType() != FieldType.SECTION_HEADER) {
                renderFieldAt(def, colLLabel, colLField, colLWidth, y);

                i++;
                FieldDef rightDef = fields.get(i);
                renderFieldAt(rightDef, colRLabel, colRField, colRWidth, y);

                y += rowHeight;
            } else {
                // Full-width: compute fieldX from label pixel width
                int fieldW = getFullFieldWidth(def);
                String translated = StatCollector.translateToLocal(def.getLabel());
                int labelW = fontRenderer.getStringWidth(translated);
                int fieldX = colLLabel + labelW + labelPadding;

                renderFieldAt(def, colLLabel, fieldX, fieldW, y);
                y += rowHeight;
            }
        }

        return y;
    }

    // ═══════════════════════════════════════════════════════════════════
    // FIELD RENDERING
    // ═══════════════════════════════════════════════════════════════════

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void renderFieldAt(FieldDef def, int labelX, int fieldX, int fieldW, int y) {
        sw.addLabel(new GuiNpcLabel(labelId++, def.getLabel(), labelX, y + 5, 0xFFFFFF));

        String hover = def.getHoverText() != null ? def.getHoverText() : def.getTooltip();

        switch (def.getType()) {
            case FLOAT: {
                float fVal = def.getValue() instanceof Number ? ((Number) def.getValue()).floatValue() : 0f;
                GuiNpcTextField tf = new GuiNpcTextField(widgetId, parent, fontRenderer, fieldX, y, fieldW, 20, String.valueOf(fVal));
                tf.setFloatsOnly();
                if (def.hasRange()) tf.setMinMaxDefaultFloat(def.getMin(), def.getMax(), fVal);
                if (!def.isEnabled()) tf.setEnabled(false);
                if (hover != null) tf.setHoverText(hover);
                sw.addTextField(tf);
                textFieldMap.put(widgetId, def);
                widgetId++;
                clearId++;
                break;
            }
            case INT: {
                int iVal = def.getValue() instanceof Number ? ((Number) def.getValue()).intValue() : 0;
                GuiNpcTextField tf = new GuiNpcTextField(widgetId, parent, fontRenderer, fieldX, y, fieldW, 20, String.valueOf(iVal));
                tf.setIntegersOnly();
                if (def.hasRange()) tf.setMinMaxDefault((int) def.getMin(), (int) def.getMax(), iVal);
                if (!def.isEnabled()) tf.setEnabled(false);
                if (hover != null) tf.setHoverText(hover);
                sw.addTextField(tf);
                textFieldMap.put(widgetId, def);
                widgetId++;
                clearId++;
                break;
            }
            case STRING: {
                String sVal = def.getValue() != null ? def.getValue().toString() : "";
                GuiNpcTextField tf = new GuiNpcTextField(widgetId, parent, fontRenderer, fieldX, y, fieldW, 20, sVal);
                if (!def.isEnabled()) tf.setEnabled(false);
                if (hover != null) tf.setHoverText(hover);
                sw.addTextField(tf);
                textFieldMap.put(widgetId, def);
                widgetId++;
                clearId++;
                break;
            }
            case LABEL: {
                String text = def.getValue() != null ? def.getValue().toString() : "";
                sw.addLabel(new GuiNpcLabel(labelId++, text, fieldX, y + 5, 0xAAAAAA));
                widgetId++;
                clearId++;
                break;
            }
            case BOOLEAN: {
                boolean bVal = def.getValue() instanceof Boolean ? (Boolean) def.getValue() : false;
                GuiNpcButtonYesNo btn = new GuiNpcButtonYesNo(widgetId, fieldX, y, fieldW, 20, bVal);
                if ("gui.enabled".equals(def.getLabel()) && def.getTab() == TabTarget.GENERAL) {
                    btn.setTextColor(bVal ? 0x00FF00 : 0xFF0000);
                }
                if (!def.isEnabled()) btn.setEnabled(false);
                if (hover != null) btn.setHoverText(hover);
                sw.addButton(btn);
                buttonFieldMap.put(widgetId, def);
                widgetId++;
                clearId++;
                break;
            }
            case ENUM: {
                Class<? extends Enum<?>> enumClass = def.getEnumClass();
                if (enumClass != null) {
                    Enum<?>[] constants = enumClass.getEnumConstants();
                    String[] names = new String[constants.length];
                    for (int i = 0; i < constants.length; i++) names[i] = constants[i].toString();
                    int selected = def.getValue() instanceof Enum ? ((Enum<?>) def.getValue()).ordinal() : 0;
                    GuiNpcButton btn = new GuiNpcButton(widgetId, fieldX, y, fieldW, 20, names, selected);
                    if (!def.isEnabled()) btn.setEnabled(false);
                    if (hover != null) btn.setHoverText(hover);
                    sw.addButton(btn);
                    buttonFieldMap.put(widgetId, def);
                }
                widgetId++;
                clearId++;
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
                    GuiNpcButton btn = new GuiNpcButton(widgetId, fieldX, y, fieldW, 20, values, selected);
                    if (!def.isEnabled()) btn.setEnabled(false);
                    if (hover != null) btn.setHoverText(hover);
                    sw.addButton(btn);
                    buttonFieldMap.put(widgetId, def);
                }
                widgetId++;
                clearId++;
                break;
            }
            case SUB_GUI: {
                String btnText = def.getButtonLabel();
                int btnW;
                if (def.hasClearAction()) {
                    btnW = contentRight - 22 - fieldX;
                    GuiNpcButton clearBtn = new GuiNpcButton(clearId, contentRight - 20, y, 20, 20, "X");
                    sw.addButton(clearBtn);
                    clearFieldMap.put(clearId, def);
                } else {
                    btnW = contentRight - fieldX;
                }
                GuiNpcButton btn = new GuiNpcButton(widgetId, fieldX, y, btnW, 20, btnText);
                Integer textColor = def.getButtonTextColor();
                if (textColor != null) btn.setTextColor(textColor);
                if (!def.isEnabled()) btn.setEnabled(false);
                if (hover != null) btn.setHoverText(hover);
                sw.addButton(btn);
                buttonFieldMap.put(widgetId, def);
                widgetId++;
                clearId++;
                break;
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // EFFECTS LIST RENDERING
    // ═══════════════════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    private int renderEffectsList(FieldDef def, int y) {
        // Section header
        y += 3;
        sw.addLabel(new GuiNpcLabel(labelId++, def.getLabel(), colLLabel, y + 2, 0xFFFF55));
        y += 15;

        List<AbilityEffect> effects = (List<AbilityEffect>) def.getValue();
        if (effects == null) effects = new ArrayList<>();

        String[] typeNames = AbilityEffect.EffectType.getLangKeys();
        String[] ampValues = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};

        for (int e = 0; e < effects.size() && e < 5; e++) {
            AbilityEffect effect = effects.get(e);

            // Type selector button
            int typeIdx = effect.getType().ordinal();
            GuiNpcButton typeBtn = new GuiNpcButton(widgetId, colLLabel, y, 100, 20, typeNames, typeIdx);
            sw.addButton(typeBtn);
            buttonFieldMap.put(widgetId, def);
            effectWidgetMeta.put(widgetId, new int[]{e, 0});
            widgetId++;

            // Duration text field
            GuiNpcTextField durField = new GuiNpcTextField(widgetId, parent, fontRenderer,
                colLLabel + 104, y, 50, 20, String.valueOf(effect.getDurationTicks()));
            durField.setIntegersOnly();
            durField.setMinMaxDefault(1, 12000, 60);
            sw.addTextField(durField);
            textFieldMap.put(widgetId, def);
            effectWidgetMeta.put(widgetId, new int[]{e, 1});
            widgetId++;

            // Amplifier selector (0-10)
            GuiNpcButton ampBtn = new GuiNpcButton(widgetId, colLLabel + 158, y, 40, 20, ampValues, effect.getAmplifier());
            sw.addButton(ampBtn);
            buttonFieldMap.put(widgetId, def);
            effectWidgetMeta.put(widgetId, new int[]{e, 2});
            widgetId++;

            // Delete button
            GuiNpcButton delBtn = new GuiNpcButton(clearId, colLLabel + 202, y, 20, 20, "X");
            sw.addButton(delBtn);
            clearFieldMap.put(clearId, def);
            effectWidgetMeta.put(clearId, new int[]{e, 3});
            clearId++;

            y += rowHeight;
        }

        // Add button (if < 5 effects)
        if (effects.size() < 5) {
            GuiNpcButton addBtn = new GuiNpcButton(widgetId, colLLabel, y, 50, 20, "gui.add");
            sw.addButton(addBtn);
            buttonFieldMap.put(widgetId, def);
            effectWidgetMeta.put(widgetId, new int[]{effects.size(), 4});
            widgetId++;
            clearId++;
            y += rowHeight;
        }

        return y;
    }

    private int getFullFieldWidth(FieldDef def) {
        switch (def.getType()) {
            case FLOAT:
            case INT:         return 60;
            case STRING:      return 200;
            case BOOLEAN:     return 50;
            case LABEL:       return 60;
            case ENUM:
            case STRING_ENUM: return 120;
            case SUB_GUI:     return def.hasClearAction() ? 175 : 200;
            default:          return 80;
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════════════

    public Map<Integer, FieldDef> getButtonFieldMap() { return buttonFieldMap; }
    public Map<Integer, FieldDef> getTextFieldMap() { return textFieldMap; }
    public Map<Integer, FieldDef> getClearFieldMap() { return clearFieldMap; }
    public Map<Integer, int[]> getEffectWidgetMeta() { return effectWidgetMeta; }
    public int getNextLabelId() { return labelId; }
}
