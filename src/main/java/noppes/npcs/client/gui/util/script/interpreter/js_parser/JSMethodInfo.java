package noppes.npcs.client.gui.util.script.interpreter.js_parser;

import noppes.npcs.client.gui.util.script.interpreter.jsdoc.JSDocInfo;
import noppes.npcs.client.gui.util.script.interpreter.type.GenericContext;
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
     * Get the resolved return type, with type parameter substitution based on receiver context.
     * 
     * For parameterized receivers like List<String>, this will substitute type variables:
     * - If return type is "T" and receiver is List<String>, returns TypeInfo(String)
     * - Handles nested generics like List<Map<K,V>> with proper substitution
     * 
     * @param contextType The TypeInfo context for resolving type parameters (e.g., List<String> to resolve E → String)
     * @return The resolved TypeInfo for the return type, with type variables substituted
     */
    public TypeInfo getResolvedReturnType(TypeInfo contextType) {
        TypeResolver resolver = TypeResolver.getInstance();
        TypeInfo resolved = resolver.resolveJSType(returnType);
        
        if (contextType != null) {
            GenericContext ctx = GenericContext.forReceiver(contextType);
            resolved = ctx.substituteType(resolved, returnType, resolver);
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
         * Get the resolved type, with type parameter substitution based on receiver context.
         * 
         * For parameterized receivers like Consumer<IAction>, this will substitute type variables:
         * - If param type is "T" and receiver is Consumer<IAction>, returns TypeInfo(IAction)
         * 
         * @param contextType The TypeInfo context for resolving type parameters
         * @return The resolved TypeInfo for the parameter type
         */
        public TypeInfo getResolvedType(TypeInfo contextType) {
            TypeResolver resolver = TypeResolver.getInstance();
            TypeInfo resolved = resolver.resolveJSType(type);
            
            if (contextType != null) {
                GenericContext ctx = GenericContext.forReceiver(contextType);
                resolved = ctx.substituteType(resolved, type, resolver);
            }

            return resolved;
        }
        
        public String getName() { return name; }
        public String getType() { return type; }
        public TypeInfo getTypeInfo() {return typeInfo;}
        public JSMethodInfo getContainingMethod() {return containingMethod;}

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
        
        @Override
        public String toString() {
            return name + ": " + type;
        }
    }
}
