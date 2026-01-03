package noppes.npcs.client.gui.util.script.interpreter.js_parser;

import java.util.*;

/**
 * Represents a method signature parsed from .d.ts files.
 */
public class JSMethodInfo {
    
    private final String name;
    private final String returnType;      // Raw type string, e.g., "number", "IEntity", "void"
    private final List<JSParameterInfo> parameters;
    private String documentation;
    
    public JSMethodInfo(String name, String returnType, List<JSParameterInfo> parameters) {
        this.name = name;
        this.returnType = returnType;
        this.parameters = parameters != null ? new ArrayList<>(parameters) : new ArrayList<>();
    }
    
    public JSMethodInfo setDocumentation(String documentation) {
        this.documentation = documentation;
        return this;
    }
    
    // Getters
    public String getName() { return name; }
    public String getReturnType() { return returnType; }
    public List<JSParameterInfo> getParameters() { return parameters; }
    public String getDocumentation() { return documentation; }
    public int getParameterCount() { return parameters.size(); }
    
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
        
        if (documentation != null && !documentation.isEmpty()) {
            sb.append("<br><br>").append(documentation);
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
        
        public JSParameterInfo(String name, String type) {
            this.name = name;
            this.type = type;
        }
        
        public String getName() { return name; }
        public String getType() { return type; }
        
        @Override
        public String toString() {
            return name + ": " + type;
        }
    }
}
