package noppes.npcs.client.gui.builder;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.controllers.data.Animation;
import noppes.npcs.client.gui.SubGuiColorSelector;
import noppes.npcs.client.gui.select.GuiAnimationSelection;
import noppes.npcs.client.gui.select.GuiSoundSelection;
import noppes.npcs.client.gui.util.SubGuiInterface;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Declarative field definition for GUI rendering.
 * Built via static factories and chaining methods.
 * Client-side only — never instantiate or reference on the server.
 */
@SideOnly(Side.CLIENT)
public class FieldDef {

    private final String label;
    private final FieldType type;
    private String tab = null;
    private Supplier<Object> getter;
    private Consumer<Object> setter;
    private BooleanSupplier visibleWhen = () -> true;
    private BooleanSupplier enabledWhen = () -> true;
    private String hoverText = null;

    // Numeric range
    private float min = Float.MIN_VALUE;
    private float max = Float.MAX_VALUE;

    // Enum support
    private Class<? extends Enum<?>> enumClass;
    private String[] stringEnumValues;
    private Supplier<String> stringEnumGetter;
    private Consumer<String> stringEnumSetter;

    // Sub-gui support
    private Supplier<SubGuiInterface> subGuiFactory;
    private Consumer<SubGuiInterface> subGuiResultHandler;

    // Button display
    private Supplier<String> buttonLabelSupplier;
    private Supplier<Integer> buttonTextColorSupplier;
    private Runnable clearAction;

    // Row pairing (ROW type only)
    private FieldDef leftChild;
    private FieldDef rightChild;

    // Modern field extensions
    private int maxLength = 0;
    private int textAreaHeight = 80;
    private String placeholder = "";
    private int callbackSlot = 0;
    private String callbackAction = "";
    private Runnable onActionCallback;
    private boolean showCharCount = false;

    // Collapsible section support
    private boolean collapsible = false;
    private boolean defaultExpanded = true;

    // Action button support
    private Runnable actionRunnable;

    // Section remove + dynamic title
    private Runnable removeAction;
    private Supplier<String> titleSupplier;

    // Dynamic height for TEXT_AREA
    private boolean fillRemainingHeight = false;

    // Availability/Faction row support
    private Supplier<Integer> conditionGetter;
    private Consumer<Integer> conditionSetter;
    private Supplier<Integer> stanceGetter;
    private Consumer<Integer> stanceSetter;
    private Supplier<Integer> idGetter;
    private Consumer<Integer> idSetter;
    private Supplier<String> displayNameGetter;
    private String[] conditionValues;

    private FieldDef(String label, FieldType type) {
        this.label = label;
        this.type = type;
    }

    // ═══════════════════════════════════════════════════════════════════
    // STATIC FACTORIES
    // ═══════════════════════════════════════════════════════════════════

    public static FieldDef section(String label) {
        return new FieldDef(label, FieldType.SECTION_HEADER);
    }

    public static FieldDef floatField(String label, Supplier<Float> getter, Consumer<Float> setter) {
        FieldDef def = new FieldDef(label, FieldType.FLOAT);
        def.getter = () -> getter.get();
        def.setter = v -> setter.accept(((Number) v).floatValue());
        return def;
    }

    public static FieldDef intField(String label, Supplier<Integer> getter, Consumer<Integer> setter) {
        FieldDef def = new FieldDef(label, FieldType.INT);
        def.getter = () -> getter.get();
        def.setter = v -> setter.accept(((Number) v).intValue());
        return def;
    }

    public static FieldDef boolField(String label, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        FieldDef def = new FieldDef(label, FieldType.BOOLEAN);
        def.getter = () -> getter.get();
        def.setter = v -> setter.accept((Boolean) v);
        return def;
    }

    @SuppressWarnings("unchecked")
    public static <E extends Enum<E>> FieldDef enumField(String label, Class<E> enumClass, Supplier<E> getter, Consumer<E> setter) {
        FieldDef def = new FieldDef(label, FieldType.ENUM);
        def.enumClass = enumClass;
        def.getter = () -> getter.get();
        def.setter = v -> setter.accept((E) v);
        return def;
    }

    public static FieldDef stringEnumField(String label, String[] values, Supplier<String> getter, Consumer<String> setter) {
        FieldDef def = new FieldDef(label, FieldType.STRING_ENUM);
        def.stringEnumValues = values;
        def.stringEnumGetter = getter;
        def.stringEnumSetter = setter;
        return def;
    }

    public static FieldDef stringField(String label, Supplier<String> getter, Consumer<String> setter) {
        FieldDef def = new FieldDef(label, FieldType.STRING);
        def.getter = () -> getter.get();
        def.setter = v -> setter.accept((String) v);
        return def;
    }

    public static FieldDef labelField(String label, Supplier<String> textSupplier) {
        FieldDef def = new FieldDef(label, FieldType.LABEL);
        def.getter = () -> textSupplier.get();
        return def;
    }

    public static FieldDef subGuiField(String label, Supplier<SubGuiInterface> factory, Consumer<SubGuiInterface> resultHandler) {
        FieldDef def = new FieldDef(label, FieldType.SUB_GUI);
        def.subGuiFactory = factory;
        def.subGuiResultHandler = resultHandler;
        return def;
    }

    /**
     * Creates a FieldDef with a custom FieldType. Used by extension code
     * (e.g. AbilityFieldDefs) to create fields with types like EFFECTS_LIST
     * that the base GuiFieldBuilder does not handle.
     */
    public static FieldDef custom(String label, FieldType type, Supplier<Object> getter, Consumer<Object> setter) {
        FieldDef def = new FieldDef(label, type);
        def.getter = getter;
        def.setter = setter;
        return def;
    }

    /**
     * Creates a two-column row pairing two fields side by side.
     * If one child is hidden (via visibleWhen), the other renders full-width.
     */
    public static FieldDef row(FieldDef left, FieldDef right) {
        FieldDef def = new FieldDef("", FieldType.ROW);
        def.leftChild = left;
        def.rightChild = right;
        return def;
    }

    // ═══════════════════════════════════════════════════════════════════
    // MODERN FIELD FACTORIES
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Multi-line text area field rendered as ModernTextArea by ModernFieldPanel.
     */
    public static FieldDef textAreaField(String label, Supplier<String> getter, Consumer<String> setter) {
        FieldDef def = new FieldDef(label, FieldType.TEXT_AREA);
        def.getter = () -> getter.get();
        def.setter = v -> setter.accept((String) v);
        return def;
    }

    /**
     * Color field rendered as ModernColorButton by ModernFieldPanel.
     * Triggers onColorSelect callback via listener.
     */
    public static FieldDef colorField(String label, Supplier<Integer> getter, Consumer<Integer> setter) {
        FieldDef def = new FieldDef(label, FieldType.COLOR);
        def.getter = () -> getter.get();
        def.setter = v -> setter.accept((Integer) v);
        return def;
    }

    /**
     * External selector field rendered as ModernSelectButton by ModernFieldPanel.
     * Use .action() and .slot() to identify the callback, or .onAction() for direct callback.
     */
    public static FieldDef selectField(String label, Supplier<String> displayGetter) {
        FieldDef def = new FieldDef(label, FieldType.SELECT);
        def.getter = () -> displayGetter.get();
        return def;
    }

    /**
     * Availability requirement row: condition dropdown + select button + clear button.
     * Used for quest/dialog requirements with condition-based enabling.
     */
    public static FieldDef availabilityRow(String label, String[] conditions,
            Supplier<Integer> condGetter, Consumer<Integer> condSetter,
            Supplier<Integer> idGetter, Consumer<Integer> idSetter,
            Supplier<String> displayGetter) {
        FieldDef def = new FieldDef(label, FieldType.AVAILABILITY_ROW);
        def.conditionValues = conditions;
        def.conditionGetter = condGetter;
        def.conditionSetter = condSetter;
        def.idGetter = idGetter;
        def.idSetter = idSetter;
        def.displayNameGetter = displayGetter;
        return def;
    }

    /**
     * Faction requirement row: condition dropdown + stance dropdown + select button + clear button.
     */
    public static FieldDef factionRow(String label,
            Supplier<Integer> condGetter, Consumer<Integer> condSetter,
            Supplier<Integer> stanceGetter, Consumer<Integer> stanceSetter,
            Supplier<Integer> idGetter, Consumer<Integer> idSetter,
            Supplier<String> displayGetter) {
        FieldDef def = new FieldDef(label, FieldType.FACTION_ROW);
        def.conditionGetter = condGetter;
        def.conditionSetter = condSetter;
        def.stanceGetter = stanceGetter;
        def.stanceSetter = stanceSetter;
        def.idGetter = idGetter;
        def.idSetter = idSetter;
        def.displayNameGetter = displayGetter;
        return def;
    }

    /**
     * Action button field — renders as a clickable button that executes a Runnable.
     * Used for "Add", "Delete", or other action triggers within a field list.
     */
    public static FieldDef actionButton(String label, Runnable action) {
        FieldDef def = new FieldDef(label, FieldType.ACTION_BUTTON);
        def.actionRunnable = action;
        return def;
    }

    // ═══════════════════════════════════════════════════════════════════
    // CONVENIENCE FACTORIES (SUB_GUI wrappers)
    // ═══════════════════════════════════════════════════════════════════

    public static FieldDef colorSubGui(String label, Supplier<Integer> getter, Consumer<Integer> setter) {
        return subGuiField(label,
            () -> new SubGuiColorSelector(getter.get()),
            gui -> setter.accept(((SubGuiColorSelector) gui).color & 0x00FFFFFF))
            .buttonLabel(() -> String.format("%06X", getter.get() & 0xFFFFFF))
            .buttonTextColor(() -> getter.get() & 0xFFFFFF);
    }

    public static FieldDef soundSubGui(String label, Supplier<String> getter, Consumer<String> setter) {
        return subGuiField(label,
            () -> new GuiSoundSelection(getter.get()),
            gui -> {
                GuiSoundSelection s = (GuiSoundSelection) gui;
                if (s.selectedResource != null) setter.accept(s.selectedResource.toString());
            })
            .buttonLabel(() -> {
                String s = getter.get();
                return s == null || s.isEmpty() ? "gui.none" : s;
            })
            .clearable(() -> setter.accept(""));
    }

    public static FieldDef animSubGui(String label,
            Supplier<Integer> idGetter, Consumer<Integer> idSetter,
            Supplier<String> nameGetter, Consumer<String> nameSetter) {
        return subGuiField(label,
            () -> new GuiAnimationSelection(idGetter.get(), nameGetter.get()),
            gui -> {
                GuiAnimationSelection sel = (GuiAnimationSelection) gui;
                if (sel.isBuiltInSelected()) {
                    nameSetter.accept(sel.selectedBuiltInName);
                    idSetter.accept(-1);
                } else {
                    idSetter.accept(sel.selectedAnimationId);
                    nameSetter.accept("");
                }
            })
            .buttonLabel(() -> {
                String name = nameGetter.get();
                if (name != null && !name.isEmpty()) return name;
                int id = idGetter.get();
                if (id >= 0) {
                    Animation anim = AnimationController.Instance != null
                        ? (Animation) AnimationController.Instance.get(id) : null;
                    String animName = anim != null ? anim.getName() : "";
                    return animName != null && !animName.isEmpty()
                        ? "(ID: " + id + ") " + animName : "ID: " + id;
                }
                return "gui.none";
            })
            .clearable(() -> { idSetter.accept(-1); nameSetter.accept(""); });
    }

    // ═══════════════════════════════════════════════════════════════════
    // CHAINING METHODS
    // ═══════════════════════════════════════════════════════════════════

    public FieldDef tab(String tabName) {
        this.tab = tabName;
        return this;
    }

    public FieldDef range(float min, float max) {
        this.min = min;
        this.max = max;
        return this;
    }

    public FieldDef min(float min) {
        this.min = min;
        return this;
    }

    public FieldDef max(float max) {
        this.max = max;
        return this;
    }

    public FieldDef visibleWhen(BooleanSupplier condition) {
        this.visibleWhen = condition;
        return this;
    }

    public FieldDef enabledWhen(BooleanSupplier condition) {
        this.enabledWhen = condition;
        return this;
    }

    public FieldDef hover(String hoverText) {
        this.hoverText = hoverText;
        return this;
    }

    public FieldDef buttonLabel(Supplier<String> supplier) {
        this.buttonLabelSupplier = supplier;
        return this;
    }

    public FieldDef buttonTextColor(Supplier<Integer> supplier) {
        this.buttonTextColorSupplier = supplier;
        return this;
    }

    public FieldDef clearable(Runnable action) {
        this.clearAction = action;
        return this;
    }

    public FieldDef maxLength(int max) {
        this.maxLength = max;
        return this;
    }

    public FieldDef placeholder(String text) {
        this.placeholder = text;
        return this;
    }

    public FieldDef height(int h) {
        this.textAreaHeight = h;
        return this;
    }

    /**
     * Makes a SECTION_HEADER render as a CollapsibleSection (expanded by default).
     */
    public FieldDef collapsed() {
        this.collapsible = true;
        this.defaultExpanded = false;
        return this;
    }

    /**
     * Makes a SECTION_HEADER render as a CollapsibleSection with the given expand state.
     */
    public FieldDef collapsed(boolean startExpanded) {
        this.collapsible = true;
        this.defaultExpanded = startExpanded;
        return this;
    }

    /**
     * Callback action type for SELECT/COLOR fields (e.g. "quest", "sound", "dialog").
     */
    public FieldDef action(String action) {
        this.callbackAction = action;
        return this;
    }

    /**
     * Callback slot identifier for SELECT/COLOR fields.
     */
    public FieldDef slot(int slot) {
        this.callbackSlot = slot;
        return this;
    }

    /**
     * Direct action callback for SELECT fields (alternative to action/slot).
     */
    public FieldDef onAction(Runnable callback) {
        this.onActionCallback = callback;
        return this;
    }

    /**
     * Show character count below a TEXT_AREA field.
     */
    public FieldDef charCount() {
        this.showCharCount = true;
        return this;
    }

    /**
     * Set dynamic title supplier for SECTION_HEADER (updated each draw).
     */
    public FieldDef titleSupplier(Supplier<String> supplier) {
        this.titleSupplier = supplier;
        return this;
    }

    /**
     * Set remove action for SECTION_HEADER (adds "X" button to header).
     */
    public FieldDef removeAction(Runnable action) {
        this.removeAction = action;
        return this;
    }

    /**
     * Make TEXT_AREA fill remaining available height.
     * Requires ModernFieldPanel.setAvailableHeight() to be called.
     */
    public FieldDef fillHeight() {
        this.fillRemainingHeight = true;
        return this;
    }

    // ═══════════════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════════════

    public String getLabel() { return label; }
    public FieldType getType() { return type; }
    public String getTab() { return tab; }
    public boolean isVisible() { return visibleWhen.getAsBoolean(); }
    public boolean isEnabled() { return enabledWhen.getAsBoolean(); }
    public String getHoverText() { return hoverText; }
    public float getMin() { return min; }
    public float getMax() { return max; }
    public boolean hasRange() { return min != Float.MIN_VALUE || max != Float.MAX_VALUE; }
    public Class<? extends Enum<?>> getEnumClass() { return enumClass; }
    public String[] getStringEnumValues() { return stringEnumValues; }
    public Supplier<SubGuiInterface> getSubGuiFactory() { return subGuiFactory; }
    public Consumer<SubGuiInterface> getSubGuiResultHandler() { return subGuiResultHandler; }
    public boolean hasClearAction() { return clearAction != null; }
    public Runnable getClearAction() { return clearAction; }

    public Object getValue() {
        return getter != null ? getter.get() : null;
    }

    public void setValue(Object value) {
        if (setter != null) setter.accept(value);
    }

    public String getStringEnumValue() {
        return stringEnumGetter != null ? stringEnumGetter.get() : null;
    }

    public void setStringEnumValue(String value) {
        if (stringEnumSetter != null) stringEnumSetter.accept(value);
    }

    public String getButtonLabel() {
        if (buttonLabelSupplier != null) return buttonLabelSupplier.get();
        return label;
    }

    public Integer getButtonTextColor() {
        return buttonTextColorSupplier != null ? buttonTextColorSupplier.get() : null;
    }

    public FieldDef getLeftChild() { return leftChild; }
    public FieldDef getRightChild() { return rightChild; }

    // Modern field getters
    public int getMaxLength() { return maxLength; }
    public int getTextAreaHeight() { return textAreaHeight; }
    public String getPlaceholder() { return placeholder; }
    public int getCallbackSlot() { return callbackSlot; }
    public String getCallbackAction() { return callbackAction; }
    public Runnable getOnActionCallback() { return onActionCallback; }
    public boolean showsCharCount() { return showCharCount; }
    public boolean isCollapsible() { return collapsible; }
    public boolean isDefaultExpanded() { return defaultExpanded; }

    // Availability/Faction row getters
    public Supplier<Integer> getConditionGetter() { return conditionGetter; }
    public Consumer<Integer> getConditionSetter() { return conditionSetter; }
    public Supplier<Integer> getStanceGetter() { return stanceGetter; }
    public Consumer<Integer> getStanceSetter() { return stanceSetter; }
    public Supplier<Integer> getIdGetter() { return idGetter; }
    public Consumer<Integer> getIdSetter() { return idSetter; }
    public Supplier<String> getDisplayNameGetter() { return displayNameGetter; }
    public String[] getConditionValues() { return conditionValues; }

    // Action button / section extensions
    public Runnable getActionRunnable() { return actionRunnable; }
    public Runnable getRemoveAction() { return removeAction; }
    public Supplier<String> getTitleSupplier() { return titleSupplier; }
    public boolean shouldFillHeight() { return fillRemainingHeight; }

    // ═══════════════════════════════════════════════════════════════════
    // LIST MANIPULATION (for mod injection)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Insert a field before the first field matching the target label.
     * Searches top-level labels and ROW children.
     *
     * @param fields      The field list to modify
     * @param targetLabel The label of the field to insert before
     * @param newField    The field to insert
     * @return true if the target was found and the field was inserted
     */
    public static boolean insertBefore(java.util.List<FieldDef> fields, String targetLabel, FieldDef newField) {
        for (int i = 0; i < fields.size(); i++) {
            if (matchesLabel(fields.get(i), targetLabel)) {
                fields.add(i, newField);
                return true;
            }
        }
        return false;
    }

    /**
     * Insert a field after the first field matching the target label.
     * Searches top-level labels and ROW children.
     *
     * @param fields      The field list to modify
     * @param targetLabel The label of the field to insert after
     * @param newField    The field to insert
     * @return true if the target was found and the field was inserted
     */
    public static boolean insertAfter(List<FieldDef> fields, String targetLabel, FieldDef newField) {
        for (int i = 0; i < fields.size(); i++) {
            if (matchesLabel(fields.get(i), targetLabel)) {
                fields.add(i + 1, newField);
                return true;
            }
        }
        return false;
    }

    /**
     * Modify the visibility condition of a field.
     * Searches top-level labels and ROW children.
     *
     * @param fields      The field list to modify
     * @param targetLabel The label of the field to modify
     * @param condition    The condition to replace
     * @return true if the target was found and the condition was modified
     */
    public static boolean modifyVisibility(List<FieldDef> fields, String targetLabel, BooleanSupplier condition) {
        for (FieldDef field : fields) {
            if (matchesLabel(field, targetLabel)) {
                field.visibleWhen(condition);
                return true;
            }
        }
        return false;
    }

    private static boolean matchesLabel(FieldDef def, String targetLabel) {
        if (targetLabel.equals(def.label)) return true;
        if (def.type == FieldType.ROW) {
            if (def.leftChild != null && targetLabel.equals(def.leftChild.label)) return true;
            if (def.rightChild != null && targetLabel.equals(def.rightChild.label)) return true;
        }
        return false;
    }

}
