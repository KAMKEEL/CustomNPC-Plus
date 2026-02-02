package kamkeel.npcs.controllers.data.ability.gui;

import kamkeel.npcs.controllers.data.ability.AbilityEffect;
import noppes.npcs.client.gui.SubGuiColorSelector;
import noppes.npcs.client.gui.select.GuiAnimationSelection;
import noppes.npcs.client.gui.select.GuiSoundSelection;
import noppes.npcs.client.gui.util.SubGuiInterface;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Declarative field definition for ability GUI rendering.
 * Built via static factories and chaining methods.
 */
public class FieldDef {

    private final String label;
    private final FieldType type;
    private TabTarget tab = TabTarget.TYPE;
    private String customTabName = null;
    private ColumnHint column = ColumnHint.FULL;
    private Supplier<Object> getter;
    private Consumer<Object> setter;
    private BooleanSupplier visibleWhen = () -> true;
    private BooleanSupplier enabledWhen = () -> true;
    private String tooltip = null;
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
    public static FieldDef effectsListField(String label, Supplier<List<AbilityEffect>> getter, Consumer<List<AbilityEffect>> setter) {
        FieldDef def = new FieldDef(label, FieldType.EFFECTS_LIST);
        def.getter = () -> getter.get();
        def.setter = v -> setter.accept((List<AbilityEffect>) v);
        return def;
    }

    public static FieldDef subGuiField(String label, Supplier<SubGuiInterface> factory, Consumer<SubGuiInterface> resultHandler) {
        FieldDef def = new FieldDef(label, FieldType.SUB_GUI);
        def.subGuiFactory = factory;
        def.subGuiResultHandler = resultHandler;
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
                return id >= 0 ? "ID: " + id : "gui.none";
            })
            .clearable(() -> { idSetter.accept(-1); nameSetter.accept(""); });
    }

    // ═══════════════════════════════════════════════════════════════════
    // CHAINING METHODS
    // ═══════════════════════════════════════════════════════════════════

    public FieldDef tab(TabTarget tab) {
        this.tab = tab;
        return this;
    }

    public FieldDef customTab(String tabName) {
        this.tab = TabTarget.CUSTOM;
        this.customTabName = tabName;
        return this;
    }

    public FieldDef range(float min, float max) {
        this.min = min;
        this.max = max;
        return this;
    }

    public FieldDef column(ColumnHint column) {
        this.column = column;
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

    public FieldDef tooltip(String tooltip) {
        this.tooltip = tooltip;
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

    public String getLabel() { return label; }
    public FieldType getType() { return type; }
    public TabTarget getTab() { return tab; }
    public String getCustomTabName() { return customTabName; }
    public ColumnHint getColumn() { return column; }
    public boolean isVisible() { return visibleWhen.getAsBoolean(); }
    public boolean isEnabled() { return enabledWhen.getAsBoolean(); }
    public String getTooltip() { return tooltip; }
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

}
