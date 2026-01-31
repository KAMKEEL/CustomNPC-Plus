package somehussar.gui;

import somehussar.gui.constraints.*;
import somehussar.gui.field.EditableField;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GuiFieldHandler {

    private static final Map<Class<?>, ClassMetadata> CACHE = new HashMap<>();
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    public static final Map<Class<? extends Annotation>, ConstraintFactory<?>> registry = new HashMap<>();

    static {
        registry.put(NumericConstraint.class, new NumericConstraintFactory());
    }


    private GuiFieldHandler() {}

    public static ClassMetadata getMetadata(Class<?> type) {
        return CACHE.computeIfAbsent(type, GuiFieldHandler::scanClass);
    }


    private static ClassMetadata scanClass(Class<?> type) {
        if (!type.isAnnotationPresent(GuiEditable.class)) {
            return null;
        }

        ClassMetadata parent = null;

        Class<?> superCls = type.getSuperclass();
        if (superCls != null && superCls != Object.class) {
            parent = getMetadata(superCls);
        }

        List<EditableField> list = new ArrayList<>();
        for (Field field : type.getDeclaredFields()) {
            EditableField editableField = createEditableField(field);

            if (editableField != null) {
                list.add(editableField);
            }
        }

        return new ClassMetadata(
            type,
            list.toArray(list.toArray(new EditableField[0])),
            parent
        );
    }

    @SuppressWarnings({"RawUseOfParameterized", "unchecked"})
    private static EditableField createEditableField(Field field) {
        try {
            GuiEditable.Field fieldConfig = field.getAnnotation(GuiEditable.Field.class);
            if (fieldConfig == null) return null;
            if (Modifier.isFinal(field.getModifiers())) return null;

            field.setAccessible(true);
            EditableField.Builder builder = new EditableField.Builder()
                .name(fieldConfig.value())
                .type(field.getType())
                .getter(LOOKUP.unreflectGetter(field))
                .setter(LOOKUP.unreflectSetter(field))
                .constraints(buildConstraints(field))
                .order(fieldConfig.order());

            GuiEditable.Group group = field.getAnnotation(GuiEditable.Group.class);
            if (group != null)
                builder.group(group.value());


            return builder.build();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(
                "Failed to bind field: " + field, e);
        }
    }

    @SuppressWarnings({"RawUseOfParameterized", "unchecked"})
    private static FieldConstraints buildConstraints(Field field) {
        List<FieldConstraint> list = new ArrayList<>();

        for (Annotation ann : field.getAnnotations()) {
            ConstraintFactory factory = registry.get(ann.annotationType());
            if (factory != null) {
                FieldConstraint c = factory.create(field, ann);
                list.add(c);
            }
        }

        if (list.isEmpty()) return FieldConstraints.empty();
        return new FieldConstraints(list.toArray(new FieldConstraint[0]));
    }


    @SuppressWarnings({"RawUseOfParameterized"})
    public static final class ClassMetadata {
        final Class<?> type;
        final EditableField[] fields;   // only declared fields
        final ClassMetadata parent;     // superclass metadata (nullable)

        ClassMetadata(Class<?> type, EditableField[] fields, ClassMetadata parent) {
            this.type = type;
            this.fields = fields;
            this.parent = parent;
        }

        public ClassMetadata getParent() {
            return parent;
        }

        public EditableField[] getDeclaredFields() {
            return fields;
        }

        public Class<?> getType() {
            return type;
        }
    }


}
