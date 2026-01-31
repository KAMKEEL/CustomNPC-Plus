package somehussar.gui.field;

import somehussar.gui.FieldConstraints;
import somehussar.gui.GuiEditable;

import java.lang.invoke.MethodHandle;

public final class EditableField<T> {

    private final String name;
    private final Class<T> type;
    private final MethodHandle getter;
    private final MethodHandle setter;
    private final FieldConstraints constraints;
    private final int order;
    private final String group;
    private final GuiEditable.Size size;

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }

    @SuppressWarnings({"unchecked"})
    public T read(T instance) {
        try {
            return (T) getter.invoke(instance);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to read field " + name, t);
        }
    }

    public void write(T instance, T value) {
        constraints.validate(value);
        try {
            setter.invoke(instance, value);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to write field " + name, t);
        }
    }

    public FieldConstraints getConstraints() {
        return this.constraints;
    }

    public int getOrder() { return this.order; }

    EditableField(Builder<T> b) {
        this.name = b.name;
        this.type = b.type;
        this.getter = b.getter;
        this.setter = b.setter;
        this.constraints = b.constraints;
        this.order = b.order;
        this.group = b.group;
        this.size = b.size;
    }

    public static class Builder<T> {
        private String name;
        private Class<T> type;
        private MethodHandle getter;
        private MethodHandle setter;
        private FieldConstraints constraints;
        private int order;
        private String group = null;
        private GuiEditable.Size size;

        public Builder<T> name(String v) { this.name = v; return this; }
        public Builder<T> type(Class<T> v) { this.type = v; return this; }
        public Builder<T> getter(MethodHandle v) { this.getter = v; return this; }
        public Builder<T> setter(MethodHandle v) { this.setter = v; return this; }
        public Builder<T> constraints(FieldConstraints v) { this.constraints = v; return this; }
        public Builder<T> order(int order) { this.order = order; return this; }
        public Builder<T> group(String value) { this.group = value; return this; }
        public Builder<T> size(GuiEditable.Size size) { this.size = size; return this; }

        public EditableField<T> build() {
            return new EditableField<>(this);
        }
    }
}
