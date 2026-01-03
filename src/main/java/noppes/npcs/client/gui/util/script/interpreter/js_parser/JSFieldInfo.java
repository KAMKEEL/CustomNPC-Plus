package noppes.npcs.client.gui.util.script.interpreter.js_parser;

/**
 * Represents a field/property in a TypeScript interface.
 */
public class JSFieldInfo {
    
    private final String name;
    private final String type;          // Raw type string
    private final boolean readonly;
    private String documentation;
    
    public JSFieldInfo(String name, String type, boolean readonly) {
        this.name = name;
        this.type = type;
        this.readonly = readonly;
    }
    
    public JSFieldInfo setDocumentation(String documentation) {
        this.documentation = documentation;
        return this;
    }
    
    // Getters
    public String getName() { return name; }
    public String getType() { return type; }
    public boolean isReadonly() { return readonly; }
    public String getDocumentation() { return documentation; }
    
    /**
     * Build hover info HTML for this field.
     */
    public String buildHoverInfo() {
        StringBuilder sb = new StringBuilder();
        if (readonly) {
            sb.append("<i>readonly</i> ");
        }
        sb.append("<b>").append(name).append("</b>: <i>").append(type).append("</i>");
        
        if (documentation != null && !documentation.isEmpty()) {
            sb.append("<br><br>").append(documentation);
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return (readonly ? "readonly " : "") + name + ": " + type;
    }
}
