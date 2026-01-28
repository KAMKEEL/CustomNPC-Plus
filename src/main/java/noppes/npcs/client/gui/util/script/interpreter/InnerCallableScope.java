package noppes.npcs.client.gui.util.script.interpreter;

import noppes.npcs.client.gui.util.script.interpreter.field.FieldInfo;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Represents an inner callable scope (lambda or JS function expression).
 * These are NOT added to the methods list - they're resolution-only scopes.
 */
public class InnerCallableScope {
    
    public enum Kind {
        JAVA_LAMBDA,
        JS_FUNCTION_EXPR
    }
    
    private final Kind kind;
    private final int headerStart;  // Start of "(params) ->" or "function(params)"
    private final int headerEnd;    // End of header (position of "->" for lambda, ")" for JS func)
    private final int bodyStart;    // Start of body (expression start or '{')
    private final int bodyEnd;      // End of body (expression end or '}')
    private final List<FieldInfo> parameters = new ArrayList<>();
    private final Map<String, FieldInfo> locals = new HashMap<>();
    private InnerCallableScope parentScope;  // Enclosing inner scope (for nested lambdas)
    private TypeInfo expectedType;  // The expected functional interface type (set during resolution)
    
    public InnerCallableScope(Kind kind, int headerStart, int headerEnd, int bodyStart, int bodyEnd) {
        this.kind = kind;
        this.headerStart = headerStart;
        this.headerEnd = headerEnd;
        this.bodyStart = bodyStart;
        this.bodyEnd = bodyEnd;
    }
    
    // Getters
    public Kind getKind() { return kind; }
    public int getHeaderStart() { return headerStart; }
    public int getHeaderEnd() { return headerEnd; }
    public int getBodyStart() { return bodyStart; }
    public int getBodyEnd() { return bodyEnd; }
    public List<FieldInfo> getParameters() { return parameters; }
    public Map<String, FieldInfo> getLocals() { return locals; }
    public InnerCallableScope getParentScope() { return parentScope; }
    public TypeInfo getExpectedType() { return expectedType; }
    
    // Setters
    public void setParentScope(InnerCallableScope parent) { this.parentScope = parent; }
    public void setExpectedType(TypeInfo type) { this.expectedType = type; }
    
    public void addParameter(FieldInfo param) {
        parameters.add(param);
    }
    
    public void addLocal(String name, FieldInfo local) {
        locals.put(name, local);
    }
    
    public FieldInfo getParameter(String name) {
        for (FieldInfo param : parameters) {
            if (param.getName().equals(name)) {
                return param;
            }
        }
        return null;
    }
    
    public boolean hasParameter(String name) {
        return getParameter(name) != null;
    }
    
    /**
     * Check if a position is inside this scope's body.
     */
    public boolean containsPosition(int position) {
        return position >= bodyStart && position < bodyEnd;
    }
    
    /**
     * Check if a position is inside this scope's header (parameter list).
     */
    public boolean containsHeaderPosition(int position) {
        return position >= headerStart && position < headerEnd;
    }
    
    /**
     * Get the full range (header + body).
     */
    public int getFullStart() {
        return headerStart;
    }
    
    public int getFullEnd() {
        return bodyEnd;
    }
}
