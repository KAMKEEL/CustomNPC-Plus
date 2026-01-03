package noppes.npcs.client.gui.util.script.interpreter.js_parser;

import java.util.*;

/**
 * Represents a TypeScript interface/type parsed from .d.ts files.
 * This is the JS equivalent of TypeInfo for Java types.
 */
public class JSTypeInfo {
    
    private final String simpleName;      // e.g., "InteractEvent"
    private final String fullName;        // e.g., "IPlayerEvent.InteractEvent"
    private final String namespace;       // e.g., "IPlayerEvent" (parent namespace, null for top-level)
    
    // Members
    private final Map<String, JSMethodInfo> methods = new LinkedHashMap<>();
    private final Map<String, JSFieldInfo> fields = new LinkedHashMap<>();
    
    // Inheritance
    private String extendsType;           // The type this interface extends
    private JSTypeInfo resolvedParent;    // Resolved parent type (after registry is built)
    
    // Inner types (for namespaces)
    private final Map<String, JSTypeInfo> innerTypes = new LinkedHashMap<>();
    private JSTypeInfo parentType;        // The containing type (for inner types)
    
    // Documentation
    private String documentation;
    
    public JSTypeInfo(String simpleName, String namespace) {
        this.simpleName = simpleName;
        this.namespace = namespace;
        this.fullName = namespace != null ? namespace + "." + simpleName : simpleName;
    }
    
    // Builder methods
    public JSTypeInfo setExtends(String extendsType) {
        this.extendsType = extendsType;
        return this;
    }
    
    public JSTypeInfo setDocumentation(String documentation) {
        this.documentation = documentation;
        return this;
    }
    
    public void addMethod(JSMethodInfo method) {
        // Handle overloads - store with index if name already exists
        String key = method.getName();
        if (methods.containsKey(key)) {
            // Find next available key for overload
            int index = 1;
            while (methods.containsKey(key + "$" + index)) {
                index++;
            }
            methods.put(key + "$" + index, method);
        } else {
            methods.put(key, method);
        }
    }
    
    public void addField(JSFieldInfo field) {
        fields.put(field.getName(), field);
    }
    
    public void addInnerType(JSTypeInfo inner) {
        inner.parentType = this;
        innerTypes.put(inner.getSimpleName(), inner);
    }
    
    public void setResolvedParent(JSTypeInfo parent) {
        this.resolvedParent = parent;
    }
    
    // Getters
    public String getSimpleName() { return simpleName; }
    public String getFullName() { return fullName; }
    public String getNamespace() { return namespace; }
    public String getExtendsType() { return extendsType; }
    public JSTypeInfo getResolvedParent() { return resolvedParent; }
    public String getDocumentation() { return documentation; }
    public JSTypeInfo getParentType() { return parentType; }
    
    public Map<String, JSMethodInfo> getMethods() { return methods; }
    public Map<String, JSFieldInfo> getFields() { return fields; }
    public Map<String, JSTypeInfo> getInnerTypes() { return innerTypes; }
    
    /**
     * Get a method by name, including inherited methods.
     */
    public JSMethodInfo getMethod(String name) {
        JSMethodInfo method = methods.get(name);
        if (method != null) return method;
        
        // Check parent
        if (resolvedParent != null) {
            return resolvedParent.getMethod(name);
        }
        return null;
    }
    
    /**
     * Get all methods with a given name (for overloads).
     */
    public List<JSMethodInfo> getMethodOverloads(String name) {
        List<JSMethodInfo> overloads = new ArrayList<>();
        
        // Get from this type
        if (methods.containsKey(name)) {
            overloads.add(methods.get(name));
        }
        // Get numbered overloads
        int index = 1;
        while (methods.containsKey(name + "$" + index)) {
            overloads.add(methods.get(name + "$" + index));
            index++;
        }
        
        // Get from parent
        if (resolvedParent != null) {
            overloads.addAll(resolvedParent.getMethodOverloads(name));
        }
        
        return overloads;
    }
    
    /**
     * Check if this type has a method (including inherited).
     */
    public boolean hasMethod(String name) {
        return getMethod(name) != null;
    }
    
    /**
     * Get a field by name, including inherited fields.
     */
    public JSFieldInfo getField(String name) {
        JSFieldInfo field = fields.get(name);
        if (field != null) return field;
        
        // Check parent
        if (resolvedParent != null) {
            return resolvedParent.getField(name);
        }
        return null;
    }
    
    /**
     * Check if this type has a field (including inherited).
     */
    public boolean hasField(String name) {
        return getField(name) != null;
    }
    
    /**
     * Get an inner type by name.
     */
    public JSTypeInfo getInnerType(String name) {
        return innerTypes.get(name);
    }
    
    @Override
    public String toString() {
        return "JSTypeInfo{" + fullName + "}";
    }
}
