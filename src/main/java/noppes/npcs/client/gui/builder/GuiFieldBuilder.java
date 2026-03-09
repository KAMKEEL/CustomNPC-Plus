package noppes.npcs.client.gui.builder;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcButtonYesNo;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.GuiScrollWindow;
import noppes.npcs.client.gui.util.SubGuiInterface;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * General-purpose builder that converts a list of {@link FieldDef} into positioned
 * GUI widgets inside a {@link GuiScrollWindow}.
 * <p>
 * Uses {@link FieldDef#row(FieldDef, FieldDef)} for two-column layouts.
 * Subclasses can override {@link #buildField} to handle custom field types.
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

    public GuiFieldBuilder scrollWindowId(int id) {
        this.scrollWindowId = id;
        return this;
    }

    public GuiFieldBuilder contentRight(int v) {
        this.contentRight = v;
        return this;
    }

    public GuiFieldBuilder startIds(int widget, int clear, int label) {
        this.widgetId = widget;
        this.clearId = clear;
        this.labelId = label;
        return this;
    }

    public GuiFieldBuilder startY(int y) {
        this.startY = y;
        return this;
    }

    public GuiFieldBuilder rowHeight(int h) {
        this.rowHeight = h;
        return this;
    }

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
     *
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

            // Skip invisible fields
            if (!def.isVisible()) continue;

            // Section header
            if (def.getType() == FieldType.SECTION_HEADER) {
                y += 3;
                GuiNpcLabel sectionLabel = new GuiNpcLabel(labelId++, def.getLabel(), colLLabel, y + 2, 0xFFFF55);
                if (def.getHoverText() != null && !def.getHoverText().isEmpty()) {
                    sectionLabel.setHoverText(def.getHoverText());
                }
                sw.addLabel(sectionLabel);
                y += 15;
                continue;
            }

            // ROW: explicit two-column pair
            if (def.getType() == FieldType.ROW) {
                FieldDef left = def.getLeftChild();
                FieldDef right = def.getRightChild();
                boolean leftVis = left != null && left.isVisible();
                boolean rightVis = right != null && right.isVisible();

                if (leftVis && rightVis) {
                    renderFieldAt(left, colLLabel, colLField, colLWidth, y);
                    renderFieldAt(right, colRLabel, colRField, colRWidth, y);
                } else if (leftVis) {
                    renderFullWidth(left, y);
                } else if (rightVis) {
                    renderFullWidth(right, y);
                }
                if (leftVis || rightVis) y += rowHeight;
                continue;
            }

            // Full-width field
            renderFullWidth(def, y);
            y += rowHeight;
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

    protected void setLastHandledIndex(int index) {
        this.lastHandledIndex = index;
    }

    protected int getLastHandledIndex() {
        return lastHandledIndex;
    }

    // ═══════════════════════════════════════════════════════════════════
    // FIELD RENDERING
    // ═══════════════════════════════════════════════════════════════════

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void renderFieldAt(FieldDef def, int labelX, int fieldX, int fieldW, int y) {
        GuiNpcLabel fieldLabel = new GuiNpcLabel(labelId++, def.getLabel(), labelX, y + 5, 0xFFFFFF);
        String hover = def.getHoverText();
        if (hover != null && !hover.isEmpty()) {
            fieldLabel.setHoverText(hover);
        }
        sw.addLabel(fieldLabel);

        switch (def.getType()) {
            case FLOAT: {
                float fVal = def.getValue() instanceof Number ? ((Number) def.getValue()).floatValue() : 0f;
                GuiNpcTextField tf = new GuiNpcTextField(widgetId, parent, fontRenderer, fieldX, y, fieldW, 20, String.valueOf(fVal));
                tf.setFloatsOnly();
                tf.setMinMaxDefaultFloat(def.getMin(), def.getMax(), fVal);
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
                tf.setMinMaxDefault((int) def.getMin(), (int) def.getMax(), iVal);
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
                if ("gui.name".equals(def.getLabel())) {
                    tf.setFileNameSafe();
                }
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
                        if (values[i].equals(curVal)) {
                            selected = i;
                            break;
                        }
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
                    btnW = fieldW - 22;
                    GuiNpcButton clearBtn = new GuiNpcButton(clearId, fieldX + fieldW - 20, y, 20, 20, "X");
                    sw.addButton(clearBtn);
                    clearFieldMap.put(clearId, def);
                } else {
                    btnW = fieldW;
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
            case STRING_BROWSE: {
                // Text field for manual input (URLs, paths) + browse button
                int btnW = 20;
                int tfW = fieldW - btnW - 2;
                String sVal = def.getValue() != null ? def.getValue().toString() : "";
                GuiNpcTextField tf = new GuiNpcTextField(widgetId, parent, fontRenderer, fieldX, y, tfW, 20, sVal);
                if (!def.isEnabled()) tf.setEnabled(false);
                if (hover != null) tf.setHoverText(hover);
                sw.addTextField(tf);
                textFieldMap.put(widgetId, def);
                widgetId++;
                // Browse button
                GuiNpcButton browseBtn = new GuiNpcButton(clearId, fieldX + tfW + 2, y, btnW, 20, "...");
                if (!def.isEnabled()) browseBtn.setEnabled(false);
                sw.addButton(browseBtn);
                buttonFieldMap.put(clearId, def);
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

    private void renderFullWidth(FieldDef def, int y) {
        int fieldW = getFullFieldWidth(def);
        String translated = StatCollector.translateToLocal(def.getLabel());
        int labelW = fontRenderer.getStringWidth(translated);
        int fieldX = colLLabel + labelW + labelPadding;
        // Available width before the scrollbar
        int maxW = contentRight - fieldX - 15;
        if ((def.getType() == FieldType.SUB_GUI || def.getType() == FieldType.STRING_BROWSE) && maxW > 0) {
            // SUB_GUI fields fill all available width up to the scrollbar
            fieldW = maxW;
        } else if (maxW > 0 && fieldW > maxW) {
            fieldW = maxW;
        }
        renderFieldAt(def, colLLabel, fieldX, fieldW, y);
    }

    protected int getFullFieldWidth(FieldDef def) {
        switch (def.getType()) {
            case FLOAT:
            case INT:
                return 60;
            case STRING:
                return 200;
            case BOOLEAN:
                return 50;
            case LABEL:
                return 60;
            case ENUM:
            case STRING_ENUM:
                return 120;
            case SUB_GUI:
                return def.hasClearAction() ? 175 : 200;
            case STRING_BROWSE:
                return 200;
            default:
                return 80;
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // EVENT HANDLING
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Handles button events for declarative fields.
     * Call from parent GUI's buttonEvent(). Returns true if the event was handled.
     * <p>
     * For SUB_GUI fields, the sub-gui is opened directly via {@code parent.setSubGuiWithResult()},
     * so the result handler survives initGui() rebuilds. After this returns true, check
     * {@code parent.hasSubGui()} to determine if initGui() should be called.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean handleButtonEvent(int buttonId, GuiButton button) {
        // Check clear buttons first
        if (clearFieldMap.containsKey(buttonId)) {
            FieldDef def = clearFieldMap.get(buttonId);
            if (def != null && def.hasClearAction()) {
                def.getClearAction().run();
                return true;
            }
        }

        // Check field buttons
        FieldDef def = buttonFieldMap.get(buttonId);
        if (def == null) return false;

        switch (def.getType()) {
            case BOOLEAN:
                def.setValue(((GuiNpcButton) button).getValue() == 1);
                return true;
            case ENUM: {
                int idx = ((GuiNpcButton) button).getValue();
                Class<? extends Enum<?>> ec = def.getEnumClass();
                if (ec != null) {
                    Enum<?>[] constants = ec.getEnumConstants();
                    if (idx >= 0 && idx < constants.length) def.setValue(constants[idx]);
                }
                return true;
            }
            case STRING_ENUM: {
                int idx = ((GuiNpcButton) button).getValue();
                String[] values = def.getStringEnumValues();
                if (values != null && idx >= 0 && idx < values.length) def.setStringEnumValue(values[idx]);
                return true;
            }
            case SUB_GUI:
                if (def.getSubGuiFactory() != null) {
                    parent.setSubGuiWithResult(def.getSubGuiFactory().get(), sub -> {
                        if (def.getSubGuiResultHandler() != null) {
                            def.getSubGuiResultHandler().accept(sub);
                        }
                    });
                }
                return true;
            case STRING_BROWSE:
                if (def.getSubGuiFactory() != null) {
                    parent.setSubGuiWithResult(def.getSubGuiFactory().get(), sub -> {
                        if (def.getSubGuiResultHandler() != null) {
                            def.getSubGuiResultHandler().accept(sub);
                        }
                    });
                }
                return true;
            default:
                return false;
        }
    }

    /**
     * Handles text field unfocus events for declarative fields.
     * Call from parent GUI's unFocused(). Returns true if the value actually changed.
     */
    public boolean handleTextFieldEvent(int textFieldId, GuiNpcTextField field) {
        FieldDef def = textFieldMap.get(textFieldId);
        if (def == null) return false;

        switch (def.getType()) {
            case FLOAT:
                try {
                    float newVal = Float.parseFloat(field.getText());
                    Object old = def.getValue();
                    float oldVal = old instanceof Number ? ((Number) old).floatValue() : 0f;
                    if (newVal != oldVal) {
                        def.setValue(newVal);
                        return true;
                    }
                } catch (NumberFormatException ignored) {
                }
                return false;
            case INT:
                try {
                    int newVal = Integer.parseInt(field.getText());
                    Object old = def.getValue();
                    int oldVal = old instanceof Number ? ((Number) old).intValue() : 0;
                    if (newVal != oldVal) {
                        def.setValue(newVal);
                        return true;
                    }
                } catch (NumberFormatException ignored) {
                }
                return false;
            case STRING:
            case STRING_BROWSE:
                String newVal = field.getText();
                Object old = def.getValue();
                String oldVal = old != null ? old.toString() : "";
                if (!newVal.equals(oldVal)) {
                    def.setValue(newVal);
                    return true;
                }
                return false;
            default:
                return false;
        }
    }

    /**
     * Handles sub-gui close events. Call from parent GUI's subGuiClosed().
     * <p>
     * Note: SUB_GUI fields opened via handleButtonEvent use
     * {@code parent.setSubGuiWithResult()}, so their results are handled
     * automatically by {@code GuiNPCInterface.closeSubGui()}. This method
     * only handles sub-guis opened through other means.
     */
    public boolean handleSubGuiClosed(SubGuiInterface subgui) {
        return false;
    }

    // ═══════════════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════════════

    public Map<Integer, FieldDef> getButtonFieldMap() {
        return buttonFieldMap;
    }

    public Map<Integer, FieldDef> getTextFieldMap() {
        return textFieldMap;
    }

    public Map<Integer, FieldDef> getClearFieldMap() {
        return clearFieldMap;
    }

    public GuiScrollWindow getScrollWindow() {
        return sw;
    }

    public int getLastBuildY() {
        return lastBuildY;
    }

    public int getNextLabelId() {
        return labelId;
    }
}
