package noppes.npcs.client.gui.util.script.interpreter.js_parser;

import noppes.npcs.client.gui.util.script.interpreter.jsdoc.JSDocInfo;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeResolver;
/**
 * Represents a field/property in a TypeScript interface.
 */
public class JSFieldInfo {
    
    private final String name;
    private final String type;          // Raw type string for display
    private TypeInfo typeInfo;          // Resolved TypeInfo 
    private final boolean readonly;
    private JSDocInfo jsDocInfo;
    private JSTypeInfo containingType;  // The type that contains this field
    
    public JSFieldInfo(String name, String type, boolean readonly) {
        this.name = name;
        this.type = type;
        this.readonly = readonly;
    }
    
    public JSFieldInfo setJsDocInfo(JSDocInfo jsDocInfo) {
        this.jsDocInfo = jsDocInfo;
        return this;
    }
    
    
    /**
     * Set the containing type (the JSTypeInfo that owns this field).
     */
    public void setContainingType(JSTypeInfo containingType) {
        this.containingType = containingType;
    }

    public void setTypeInfo(TypeInfo typeInfo) {
        this.typeInfo = typeInfo;
    }
    
    /**
     * Get the resolved type, with fallback resolution using type parameters.
     * @param contextType The TypeInfo context for resolving type parameters
     * @return The resolved TypeInfo for the field type
     */
    public TypeInfo getResolvedType(TypeInfo contextType) {
        // Use pre-resolved if available
        if (typeInfo != null && typeInfo.isResolved()) 
            return typeInfo;
        
        // Fall back to resolving from string
        TypeResolver resolver = TypeResolver.getInstance();
        TypeInfo resolved = typeInfo = resolver.resolveJSType(type);
        
        // If not resolved and contextType has type parameters, try to resolve as type parameter
        if (contextType != null && !resolved.isResolved()) {
            TypeInfo paramResolution = contextType.resolveTypeParamToTypeInfo(type);
            if (paramResolution != null) 
                resolved = typeInfo = paramResolution; // cache it to typeInfo
        }
        
        return resolved;
    }
    
    // Getters
    public String getName() { return name; }
    public String getType() { return type; }
    public TypeInfo getTypeInfo() { return typeInfo; }
    public boolean isReadonly() { return readonly; }
    public JSDocInfo getJsDocInfo() { return jsDocInfo; }
    public JSTypeInfo getContainingType() { return containingType; }
    
    /**
     * Get documentation string (backward compatibility).
     * Extracts description from JSDocInfo if available.
     */
    public String getDocumentation() {
        return jsDocInfo != null ? jsDocInfo.getDescription() : null;
    }
    
    /**
     * Get display name - uses resolved TypeInfo display name if available,
     * including generic type arguments like List<String>.
     */
    public String getDisplayType() {
        if (typeInfo != null && typeInfo.isResolved()) {
            return typeInfo.getDisplayName();
        }
        return type;
    }
    
    /**
     * Build hover info HTML for this field.
     */
    public String buildHoverInfo() {
        StringBuilder sb = new StringBuilder();
        if (readonly) {
            sb.append("<i>readonly</i> ");
        }
        sb.append("<b>").append(name).append("</b>: <i>").append(type).append("</i>");
        
        if (jsDocInfo != null && jsDocInfo.getDescription() != null && !jsDocInfo.getDescription().isEmpty()) {
            sb.append("<br><br>").append(jsDocInfo.getDescription());
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return (readonly ? "readonly " : "") + name + ": " + type;
    }
}
