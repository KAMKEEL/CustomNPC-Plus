package noppes.npcs.client.gui.builder;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcButtonYesNo;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.GuiScrollWindow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * General-purpose builder that converts a list of {@link FieldDef} into positioned
 * GUI widgets inside a {@link GuiScrollWindow}.
 * <p>
 * Supports 1-column and 2-column layouts. Subclasses can override {@link #buildField}
 * to handle custom field types.
 */
@SideOnly(Side.CLIENT)
public class GuiFieldBuilder {

    // Layout defaults (2-column mode)
    protected int contentRight = 330;
    protected int colLLabel = 5;
    protected int colLField = 75;
    protected int colLWidth = 70;
    protected int colRLabel = 178;
    protected int colRField = 248;
    protected int colRWidth = 70;
    protected int rowHeight = 24;
    protected int labelPadding = 8;

    // Column mode
    protected int columnCount = 2;

    // Dependencies
    protected final GuiNPCInterface parent;
    protected GuiScrollWindow sw;
    protected final FontRenderer fontRenderer;

    // Scroll window ID
    protected int scrollWindowId = 0;

    // ID counters
    protected int widgetId;
    protected int clearId;
    protected int labelId;

    // Starting Y
    protected int startY = 5;
    protected int lastBuildY = 0;

    // Output maps
    protected final Map<Integer, FieldDef> buttonFieldMap = new HashMap<>();
    protected final Map<Integer, FieldDef> textFieldMap = new HashMap<>();
    protected final Map<Integer, FieldDef> clearFieldMap = new HashMap<>();

    public GuiFieldBuilder(GuiNPCInterface parent, FontRenderer fontRenderer) {
        this.parent = parent;
        this.fontRenderer = fontRenderer;
    }

    // ═══════════════════════════════════════════════════════════════════
    // CONFIG CHAINING
    // ═══════════════════════════════════════════════════════════════════

    public GuiFieldBuilder scrollWindowId(int id) { this.scrollWindowId = id; return this; }
    public GuiFieldBuilder columns(int count) { this.columnCount = Math.max(1, Math.min(2, count)); return this; }
    public GuiFieldBuilder contentRight(int v) { this.contentRight = v; return this; }
    public GuiFieldBuilder startIds(int widget, int clear, int label) {
        this.widgetId = widget; this.clearId = clear; this.labelId = label; return this;
    }
    public GuiFieldBuilder startY(int y) { this.startY = y; return this; }
    public GuiFieldBuilder rowHeight(int h) { this.rowHeight = h; return this; }

    // ═══════════════════════════════════════════════════════════════════
    // BUILD
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Creates a {@link GuiScrollWindow} at the given position/size, builds all fields into it,
     * and sets maxScrollY automatically. The returned window is ready to use.
     */
    public GuiScrollWindow buildScrollWindow(List<FieldDef> fields, int x, int y, int width, int height) {
        this.sw = new GuiScrollWindow(parent, x, y, width, height, 0);
        sw.backgroundColor = 0x88000000;
        // Register with parent first — this calls initGui() which clears widget lists,
        // so we must add widgets AFTER this point
        parent.addScrollableGui(scrollWindowId, sw);
        int finalY = build(fields);
        sw.maxScrollY = Math.max(finalY - height, 0);
        return sw;
    }

    /**
     * Processes all fields and adds widgets to the scroll window.
     * @return the final Y position after all fields
     */
    protected int build(List<FieldDef> fields) {
        buttonFieldMap.clear();
        textFieldMap.clear();
        clearFieldMap.clear();

        int y = startY;

        for (int i = 0; i < fields.size(); i++) {
            FieldDef def = fields.get(i);

            // Let subclasses handle custom types; returns -1 if not handled
            int customResult = buildField(def, y, fields, i);
            if (customResult >= 0) {
                // Subclass handled it, check if index was advanced
                int newIndex = getLastHandledIndex();
                if (newIndex > i) i = newIndex;
                y = customResult;
                continue;
            }

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

            // Two-column pair: LEFT followed by RIGHT (only in 2-column mode)
            if (columnCount == 2
                    && def.getColumn() == ColumnHint.LEFT
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

        lastBuildY = y;
        return y;
    }

    /**
     * Hook for subclasses to handle custom field types (e.g. EFFECTS_LIST).
     * Return the new Y position if handled, or -1 if not handled (base class will process it).
     * If handling advances the field index, call {@link #setLastHandledIndex(int)}.
     */
    protected int buildField(FieldDef def, int y, List<FieldDef> fields, int index) {
        return -1;
    }

    private int lastHandledIndex = -1;
    protected void setLastHandledIndex(int index) { this.lastHandledIndex = index; }
    protected int getLastHandledIndex() { return lastHandledIndex; }

    // ═══════════════════════════════════════════════════════════════════
    // FIELD RENDERING
    // ═══════════════════════════════════════════════════════════════════

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void renderFieldAt(FieldDef def, int labelX, int fieldX, int fieldW, int y) {
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
            default:
                // Unknown type — skip, incrementing IDs to stay in sync
                widgetId++;
                clearId++;
                break;
        }
    }

    protected int getFullFieldWidth(FieldDef def) {
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
    public GuiScrollWindow getScrollWindow() { return sw; }
    public int getLastBuildY() { return lastBuildY; }
    public int getNextLabelId() { return labelId; }
}
