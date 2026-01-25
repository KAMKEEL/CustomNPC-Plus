package noppes.npcs.client.gui.util.script.interpreter.type.synthetic;

import noppes.npcs.client.gui.util.script.interpreter.method.MethodInfo;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;

import java.util.*;

/**
 * Represents a fully-built synthetic type.
 */
public class SyntheticType {
    private final String name;
    private final String documentation;
    private final Map<String, SyntheticMethod> methods;
    private final Map<String, SyntheticField> fields;
    private TypeInfo typeInfo;

    SyntheticType(String name, String documentation, List<SyntheticMethod> methods,
                  List<SyntheticField> fields) {
        this.name = name;
        this.documentation = documentation;
        this.methods = new LinkedHashMap<>();
        for (SyntheticMethod m : methods) {
            this.methods.put(m.name, m);
        }
        this.fields = new LinkedHashMap<>();
        for (SyntheticField f : fields) {
            this.fields.put(f.name, f);
        }
    }

    public String getName() {
        return name;
    }

    public String getDocumentation() {
        return documentation;
    }

    public SyntheticMethod getMethod(String methodName) {
        return methods.get(methodName);
    }

    public Collection<SyntheticMethod> getMethods() {
        return methods.values();
    }

    public SyntheticField getField(String fieldName) {
        return fields.get(fieldName);
    }

    public Collection<SyntheticField> getFields() {
        return fields.values();
    }

    public boolean hasMethod(String methodName) {
        return methods.containsKey(methodName);
    }

    public boolean hasField(String fieldName) {
        return fields.containsKey(fieldName);
    }

    /**
     * Get or create the TypeInfo for this synthetic type.
     */
    public TypeInfo getTypeInfo() {
        if (typeInfo == null) {
            typeInfo = TypeInfo.resolved(name, name, "", TypeInfo.Kind.CLASS, null);
        }
        return typeInfo;
    }

    /**
     * Create a MethodInfo for a method in this type.
     */
    public MethodInfo getMethodInfo(String methodName) {
        SyntheticMethod method = methods.get(methodName);
        if (method == null)
            return null;
        return method.toMethodInfo(getTypeInfo());
    }

    /**
     * Resolve the return type of a method given arguments.
     * Used for special methods like Java.type().
     */
    public TypeInfo resolveMethodReturnType(String methodName, String[] arguments) {
        SyntheticMethod method = methods.get(methodName);
        if (method == null)
            return null;

        return method.resolveReturnType(arguments);
    }
}
