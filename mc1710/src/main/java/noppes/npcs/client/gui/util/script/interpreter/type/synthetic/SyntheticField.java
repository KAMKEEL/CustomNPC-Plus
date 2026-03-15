package noppes.npcs.client.gui.util.script.interpreter.type.synthetic;

import noppes.npcs.client.gui.util.script.interpreter.field.FieldInfo;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeResolver;

import java.lang.reflect.Modifier;

public class SyntheticField {
    public final String name;
    public final String typeName;
    public final String documentation;
    public final boolean isStatic;

    SyntheticField(String name, String typeName, String documentation, boolean isStatic) {
        this.name = name;
        this.typeName = typeName;
        this.documentation = documentation;
        this.isStatic = isStatic;
    }

    /**
     * Get the field type as TypeInfo.
     * @return The resolved TypeInfo for the field type, or unresolved if not found
     */
    public TypeInfo getTypeInfo() {
        TypeInfo type = TypeResolver.getInstance().resolve(typeName);
        if (type == null) {
            type = TypeInfo.unresolved(typeName, typeName);
        }
        return type;
    }

    public FieldInfo toFieldInfo() {
        TypeInfo type = TypeResolver.getInstance().resolve(typeName);
        if (type == null) {
            type = TypeInfo.unresolved(typeName, typeName);
        }
        int modifiers = Modifier.PUBLIC;
        if (isStatic) {
            modifiers |= Modifier.STATIC;
        }
        return FieldInfo.external(name, type, documentation, modifiers);
    }
}
