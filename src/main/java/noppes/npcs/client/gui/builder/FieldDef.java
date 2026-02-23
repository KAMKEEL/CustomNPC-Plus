package noppes.npcs.client.gui.builder;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import noppes.npcs.client.gui.SubGuiColorSelector;
import noppes.npcs.client.gui.select.GuiAnimationSelection;
import noppes.npcs.client.gui.select.GuiSoundSelection;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.controllers.data.Animation;

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
    private float min = 0;
    private float max = Float.POSITIVE_INFINITY;

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

    @SuppressWarnings("unchecked")
    public static <T> FieldDef subGuiField(String label, Supplier<T> factory, Consumer<T> resultHandler) {
        FieldDef def = new FieldDef(label, FieldType.SUB_GUI);
        def.subGuiFactory = (Supplier<SubGuiInterface>) (Supplier<?>) factory;
        def.subGuiResultHandler = (Consumer<SubGuiInterface>) (Consumer<?>) resultHandler;
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
    // CONVENIENCE FACTORIES (SUB_GUI wrappers)
    // ═══════════════════════════════════════════════════════════════════

    public static FieldDef colorSubGui(String label, Supplier<Integer> getter, Consumer<Integer> setter) {
        return subGuiField(label,
            () -> new SubGuiColorSelector(getter.get() & 0xFFFFFF),
            gui -> setter.accept((((SubGuiColorSelector) gui).color & 0x00FFFFFF) | (getter.get() & 0xFF000000)))
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
            .clearable(() -> {
                idSetter.accept(-1);
                nameSetter.accept("");
            });
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

    // ═══════════════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════════════

    public String getLabel() {
        return label;
    }

    public FieldType getType() {
        return type;
    }

    public String getTab() {
        return tab;
    }

    public boolean isVisible() {
        return visibleWhen.getAsBoolean();
    }

    public boolean isEnabled() {
        return enabledWhen.getAsBoolean();
    }

    public String getHoverText() {
        return hoverText;
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

    public boolean hasRange() {
        return min != Float.NEGATIVE_INFINITY || max != Float.POSITIVE_INFINITY;
    }

    public Class<? extends Enum<?>> getEnumClass() {
        return enumClass;
    }

    public String[] getStringEnumValues() {
        return stringEnumValues;
    }

    public Supplier<SubGuiInterface> getSubGuiFactory() {
        return subGuiFactory;
    }

    public Consumer<SubGuiInterface> getSubGuiResultHandler() {
        return subGuiResultHandler;
    }

    public boolean hasClearAction() {
        return clearAction != null;
    }

    public Runnable getClearAction() {
        return clearAction;
    }

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

    public FieldDef getLeftChild() {
        return leftChild;
    }

    public FieldDef getRightChild() {
        return rightChild;
    }

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
     * @param condition   The condition to replace
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
