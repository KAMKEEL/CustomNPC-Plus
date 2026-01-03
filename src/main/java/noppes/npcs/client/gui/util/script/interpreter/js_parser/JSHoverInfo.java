package noppes.npcs.client.gui.util.script.interpreter.js_parser;

import java.util.List;

/**
 * Container classes for hover information in JS scripts.
 */
public class JSHoverInfo {
    
    /**
     * Hover info for a variable.
     */
    public static class VariableInfo {
        public final String name;
        public final String typeName;
        public final JSTypeInfo resolvedType;
        
        public VariableInfo(String name, String typeName, JSTypeInfo resolvedType) {
            this.name = name;
            this.typeName = typeName;
            this.resolvedType = resolvedType;
        }
        
        public String buildHoverHtml() {
            StringBuilder sb = new StringBuilder();
            sb.append("<b>").append(name).append("</b>: <i>").append(typeName).append("</i>");
            
            if (resolvedType != null && resolvedType.getDocumentation() != null) {
                sb.append("<br><br>").append(resolvedType.getDocumentation());
            }
            
            return sb.toString();
        }
    }
    
    /**
     * Hover info for a method.
     */
    public static class MethodInfo {
        public final JSMethodInfo method;
        public final JSTypeInfo containingType;
        
        public MethodInfo(JSMethodInfo method, JSTypeInfo containingType) {
            this.method = method;
            this.containingType = containingType;
        }
        
        public String buildHoverHtml() {
            StringBuilder sb = new StringBuilder();
            
            // Show containing type
            if (containingType != null) {
                sb.append("<i>").append(containingType.getFullName()).append("</i><br>");
            }
            
            // Show method signature
            sb.append(method.buildHoverInfo());
            
            return sb.toString();
        }
    }
    
    /**
     * Hover info for a field.
     */
    public static class FieldInfo {
        public final JSFieldInfo field;
        public final JSTypeInfo containingType;
        
        public FieldInfo(JSFieldInfo field, JSTypeInfo containingType) {
            this.field = field;
            this.containingType = containingType;
        }
        
        public String buildHoverHtml() {
            StringBuilder sb = new StringBuilder();
            
            // Show containing type
            if (containingType != null) {
                sb.append("<i>").append(containingType.getFullName()).append("</i><br>");
            }
            
            // Show field info
            sb.append(field.buildHoverInfo());
            
            return sb.toString();
        }
    }
    
    /**
     * Hover info for a hook function.
     */
    public static class FunctionInfo {
        public final String name;
        public final List<JSTypeRegistry.HookSignature> signatures;
        
        public FunctionInfo(String name, List<JSTypeRegistry.HookSignature> signatures) {
            this.name = name;
            this.signatures = signatures;
        }
        
        public String buildHoverHtml() {
            StringBuilder sb = new StringBuilder();
            sb.append("<b>Hook: ").append(name).append("</b>");
            
            if (!signatures.isEmpty()) {
                sb.append("<br><br>Overloads:");
                for (JSTypeRegistry.HookSignature sig : signatures) {
                    sb.append("<br>• function ").append(name).append("(")
                      .append(sig.paramName).append(": <i>").append(sig.paramType).append("</i>)");
                }
            }
            
            return sb.toString();
        }
    }
}
