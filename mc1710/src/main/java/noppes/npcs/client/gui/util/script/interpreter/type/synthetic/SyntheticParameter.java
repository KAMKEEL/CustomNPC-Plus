package noppes.npcs.client.gui.util.script.interpreter.type.synthetic;

import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeResolver;

public class SyntheticParameter {
    public final String name;
    public final String typeName;
    public final String documentation;

    SyntheticParameter(String name, String typeName, String documentation) {
        this.name = name;
        this.typeName = typeName;
        this.documentation = documentation;
    }

    /**
     * Get the parameter type as TypeInfo.
     * @return The resolved TypeInfo for the parameter type, or unresolved if not found
     */
    public TypeInfo getTypeInfo() {
        TypeInfo type = TypeResolver.getInstance().resolve(typeName);
        if (type == null) {
            type = TypeInfo.unresolved(typeName, typeName);
        }
        return type;
    }
}
