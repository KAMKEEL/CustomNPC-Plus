package noppes.npcs.client.gui.util.script.interpreter.js_parser;

import noppes.npcs.client.gui.util.script.interpreter.jsdoc.JSDocInfo;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeResolver;

import java.util.*;

/**
 * Represents a method signature parsed from .d.ts files.
 */
public class JSMethodInfo {
    
    private final String name;
    private final String returnType;      // Raw type string for display, e.g., "number", "IEntity", "void"
    private TypeInfo returnTypeInfo;      // Resolved TypeInfo (set during Phase 2)
    private final List<JSParameterInfo> parameters;
    private JSDocInfo jsDocInfo;
    private JSTypeInfo containingType;    // The type that contains this method
    
    public JSMethodInfo(String name, String returnType, List<JSParameterInfo> parameters) {
        this.name = name;
        this.returnType = returnType;
        this.parameters = parameters != null ? new ArrayList<>(parameters) : new ArrayList<>();
    }
    
    public JSMethodInfo setJsDocInfo(JSDocInfo jsDocInfo) {
        this.jsDocInfo = jsDocInfo;
        return this;
    }

    /**
     * Set the resolved return type info (called during Phase 2 type resolution).
     */
    public void setReturnTypeInfo(TypeInfo typeInfo) {
        this.returnTypeInfo = typeInfo;
    }

    /**
     * Set the containing type (the JSTypeInfo that owns this method).
     */
    public void setContainingType(JSTypeInfo containingType) {
        this.containingType = containingType;
        // Also set for all parameters
        for (JSParameterInfo param : parameters) 
            param.setContainingMethod(this);
    }

    /**
     * Get the resolved return type, with fallback resolution using type parameters.
     * @param contextType The TypeInfo context for resolving type parameters (e.g., IPlayer to resolve T → EntityPlayerMP)
     * @return The resolved TypeInfo for the return type
     */
    public TypeInfo getResolvedReturnType(TypeInfo contextType) {
        // Use pre-resolved if available
        if (returnTypeInfo != null && returnTypeInfo.isResolved()) 
            return returnTypeInfo;

        // Fall back to resolving from string
        TypeResolver resolver = TypeResolver.getInstance();
        TypeInfo resolved = returnTypeInfo = resolver.resolveJSType(returnType);

        // If not resolved and contextType has type parameters, try to resolve as type parameter
        if (contextType != null && !resolved.isResolved()) {
            TypeInfo paramResolution = contextType.resolveTypeParamToTypeInfo(returnType);
            if (paramResolution != null) 
                resolved = returnTypeInfo = paramResolution; //cache it to returnTypeInfo
        }

        return resolved;
    }
    
    // Getters
    public String getName() { return name; }
    public String getReturnType() { return returnType; }
    public TypeInfo getReturnTypeInfo() { return returnTypeInfo; }
    public List<JSParameterInfo> getParameters() { return parameters; }
    public JSDocInfo getJsDocInfo() { return jsDocInfo; }
    public int getParameterCount() { return parameters.size(); }
    public JSTypeInfo getContainingType() { return containingType; }
    
    /**
     * Get documentation string (backward compatibility).
     * Extracts description from JSDocInfo if available.
     */
    public String getDocumentation() {
        return jsDocInfo != null ? jsDocInfo.getDescription() : null;
    }
    
    /**
     * Get a formatted signature string for display.
     */
    public String getSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("(");
        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0) sb.append(", ");
            JSParameterInfo param = parameters.get(i);
            sb.append(param.getName()).append(": ").append(param.getType());
        }
        sb.append("): ").append(returnType);
        return sb.toString();
    }
    
    /**
     * Build hover info HTML for this method.
     */
    public String buildHoverInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("<b>").append(name).append("</b>(");
        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0) sb.append(", ");
            JSParameterInfo param = parameters.get(i);
            sb.append(param.getName()).append(": <i>").append(param.getType()).append("</i>");
        }
        sb.append("): <i>").append(returnType).append("</i>");
        
        if (jsDocInfo != null && jsDocInfo.getDescription() != null && !jsDocInfo.getDescription().isEmpty()) {
            sb.append("<br><br>").append(jsDocInfo.getDescription());
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return getSignature();
    }
    
    /**
     * Represents a parameter in a method signature.
     */
    public static class JSParameterInfo {
        private final String name;
        private final String type;
        private TypeInfo typeInfo;  // Resolved TypeInfo (set during Phase 2)
        private JSMethodInfo containingMethod;  // The type that contains the method with this parameter
        
        public JSParameterInfo(String name, String type) {
            this.name = name;
            this.type = type;
        }

        public void setContainingMethod(JSMethodInfo containingMethod) {
            this.containingMethod = containingMethod;
        }

        public void setTypeInfo(TypeInfo typeInfo) {
            this.typeInfo = typeInfo;
        }
        
        /**
         * Get the resolved type, with fallback resolution using type parameters.
         * @param contextType The TypeInfo context for resolving type parameters
         * @return The resolved TypeInfo for the parameter type
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
                    resolved = typeInfo = paramResolution; //cache it to typeInfo
            }

            return resolved;
        }
        
        public String getName() { return name; }
        public String getType() { return type; }
        public TypeInfo getTypeInfo() {return typeInfo;}
        public JSMethodInfo getContainingMethod() {return containingMethod;}

        /**
         * Get display name - uses resolved TypeInfo simple name if available.
         */
        public String getDisplayType() {
            if (typeInfo != null && typeInfo.isResolved()) {
                return typeInfo.getSimpleName();
            }
            return type;
        }
        
        @Override
        public String toString() {
            return name + ": " + type;
        }
    }
}
