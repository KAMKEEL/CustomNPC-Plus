package noppes.npcs.client.gui.util.script.interpreter.js_parser;

import noppes.npcs.client.gui.util.script.interpreter.ScriptDocument;
import noppes.npcs.client.gui.util.script.interpreter.ScriptLine;
import noppes.npcs.client.gui.util.script.interpreter.token.TokenType;
import scala.annotation.meta.field;

import java.util.*;
import java.util.regex.*;

/**
 * Analyzes JavaScript scripts to produce syntax highlighting marks and type information.
 * Uses the JSTypeRegistry to resolve types and validate member access.
 */
public class JSScriptAnalyzer {
    
    private final ScriptDocument document;
    private final String text;
    private final JSTypeRegistry registry;
    
    // Variable tracking: varName -> inferred type name
    private final Map<String, String> variableTypes = new HashMap<>();
    
    // Function parameter types: funcName -> paramName -> typeName
    private final Map<String, Map<String, String>> functionParams = new HashMap<>();
    
    // Excluded ranges (strings, comments)
    private final List<int[]> excludedRanges = new ArrayList<>();
    
    // Patterns
    private static final Pattern STRING_PATTERN = Pattern.compile("\"(?:[^\"\\\\]|\\\\.)*\"|'(?:[^'\\\\]|\\\\.)*'");
    private static final Pattern COMMENT_PATTERN = Pattern.compile("//.*?$|/\\*.*?\\*/", Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern FUNCTION_PATTERN = Pattern.compile("function\\s+(\\w+)\\s*\\(([^)]*)\\)");
    private static final Pattern VAR_DECL_PATTERN = Pattern.compile("(?:var|let|const)\\s+(\\w+)(?:\\s*=\\s*([^;]+))?");
    private static final Pattern ASSIGNMENT_PATTERN = Pattern.compile("(\\w+)\\s*=\\s*([^;]+)");
    private static final Pattern METHOD_CALL_PATTERN = Pattern.compile("(\\w+(?:\\.\\w+)*)\\s*\\(");
    private static final Pattern MEMBER_ACCESS_PATTERN = Pattern.compile("(\\w+)(?:\\.(\\w+))+");
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\b");
    
    private static final Pattern JS_KEYWORD_PATTERN = Pattern.compile(
        "\\b(function|var|let|const|if|else|for|while|do|switch|case|break|continue|return|" +
        "try|catch|finally|throw|new|typeof|instanceof|in|of|this|null|undefined|true|false|" +
        "class|extends|import|export|default|async|await|yield)\\b");
    
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\b\\d+\\.?\\d*\\b");
    
    public JSScriptAnalyzer(ScriptDocument document) {
        this.document = document;
        this.text = document.getText();
        this.registry = JSTypeRegistry.getInstance();
        // Ensure registry is initialized
        if (!registry.isInitialized()) {
            registry.initializeFromResources();
        }
    }
    
    /**
     * Analyze the script and produce marks.
     */
    public List<ScriptLine.Mark> analyze() {
        List<ScriptLine.Mark> marks = new ArrayList<>();
        
        // First pass: find excluded regions
        findExcludedRanges();
        
        // Mark comments and strings
        addPatternMarks(marks, COMMENT_PATTERN, TokenType.COMMENT);
        addPatternMarks(marks, STRING_PATTERN, TokenType.STRING);
        
        // Mark keywords
        addPatternMarks(marks, JS_KEYWORD_PATTERN, TokenType.KEYWORD);
        
        // Mark numbers
        addPatternMarks(marks, NUMBER_PATTERN, TokenType.LITERAL);
        
        // Parse functions and infer parameter types from hooks
        parseFunctions(marks);
        
        // Parse variable declarations
        parseVariables(marks);
        
        // Mark member accesses with type validation
        markMemberAccesses(marks);
        
        // Mark method calls
        markMethodCalls(marks);
        
        return marks;
    }
    
    /**
     * Find strings and comments to exclude from analysis.
     */
    private void findExcludedRanges() {
        Matcher m = STRING_PATTERN.matcher(text);
        while (m.find()) {
            excludedRanges.add(new int[]{m.start(), m.end()});
        }
        
        m = COMMENT_PATTERN.matcher(text);
        while (m.find()) {
            excludedRanges.add(new int[]{m.start(), m.end()});
        }
    }
    
    private boolean isExcluded(int pos) {
        for (int[] range : excludedRanges) {
            if (pos >= range[0] && pos < range[1]) return true;
        }
        return false;
    }
    
    /**
     * Parse function declarations and infer parameter types from hook signatures.
     */
    private void parseFunctions(List<ScriptLine.Mark> marks) {
        Matcher m = FUNCTION_PATTERN.matcher(text);
        while (m.find()) {
            if (isExcluded(m.start())) continue;
            
            String funcName = m.group(1);
            String params = m.group(2);
            
            // Mark function name
            int nameStart = m.start(1);
            int nameEnd = m.end(1);
            
            // Check if this is a known hook
            if (registry.isHook(funcName)) {
                marks.add(new ScriptLine.Mark(nameStart, nameEnd, TokenType.METHOD_DECL, 
                    new JSHoverInfo.FunctionInfo(funcName, registry.getHookSignatures(funcName))));
                
                // Infer parameter types from hook
                List<JSTypeRegistry.HookSignature> sigs = registry.getHookSignatures(funcName);
                if (!sigs.isEmpty() && !params.isEmpty()) {
                    // Parse parameter names from function
                    String[] paramNames = params.split(",");
                    if (paramNames.length > 0) {
                        String paramName = paramNames[0].trim();
                        // Use first hook signature's type
                        String paramType = sigs.get(0).paramType;
                        
                        // Store in function params and variable types
                        Map<String, String> funcParamMap = new HashMap<>();
                        funcParamMap.put(paramName, paramType);
                        functionParams.put(funcName, funcParamMap);
                        variableTypes.put(paramName, paramType);
                        
                        // Mark the parameter
                        int paramStart = m.start(2) + params.indexOf(paramName);
                        int paramEnd = paramStart + paramName.length();
                        JSTypeInfo typeInfo = registry.getType(paramType);
                        marks.add(new ScriptLine.Mark(paramStart, paramEnd, TokenType.PARAMETER,
                            new JSHoverInfo.VariableInfo(paramName, paramType, typeInfo)));
                    }
                }
            } else {
                marks.add(new ScriptLine.Mark(nameStart, nameEnd, TokenType.METHOD_DECL, funcName));
            }
        }
    }
    
    /**
     * Parse variable declarations and infer types.
     */
    private void parseVariables(List<ScriptLine.Mark> marks) {
        Matcher m = VAR_DECL_PATTERN.matcher(text);
        while (m.find()) {
            if (isExcluded(m.start())) continue;
            
            String varName = m.group(1);
            String initializer = m.group(2);
            
            // Infer type from initializer
            String inferredType = null;
            if (initializer != null) {
                inferredType = inferTypeFromExpression(initializer.trim());
            }
            
            if (inferredType != null) {
                variableTypes.put(varName, inferredType);
            }
            
            // Mark variable declaration
            int varStart = m.start(1);
            int varEnd = m.end(1);
            JSTypeInfo typeInfo = inferredType != null ? registry.getType(inferredType) : null;
            marks.add(new ScriptLine.Mark(varStart, varEnd, TokenType.LOCAL_FIELD,
                new JSHoverInfo.VariableInfo(varName, inferredType != null ? inferredType : "any", typeInfo)));
        }
    }
    
    /**
     * Infer type from an expression.
     */
    private String inferTypeFromExpression(String expr) {
        if (expr == null || expr.isEmpty()) return null;
        
        // String literal
        if (expr.startsWith("\"") || expr.startsWith("'")) {
            return "string";
        }
        
        // Number literal
        if (expr.matches("\\d+\\.?\\d*")) {
            return "number";
        }
        
        // Boolean literal
        if (expr.equals("true") || expr.equals("false")) {
            return "boolean";
        }
        
        // null/undefined
        if (expr.equals("null")) return "null";
        if (expr.equals("undefined")) return "undefined";
        
        // Array literal
        if (expr.startsWith("[")) {
            return "any[]";
        }
        
        // Object literal
        if (expr.startsWith("{")) {
            return "object";
        }
        
        // Method call: something.method() - infer from method return type
        if (expr.contains(".") && expr.contains("(")) {
            return inferTypeFromMethodCall(expr);
        }
        
        // Variable reference
        if (variableTypes.containsKey(expr)) {
            return variableTypes.get(expr);
        }
        
        return null;
    }
    
    /**
     * Infer type from a method call chain.
     */
    private String inferTypeFromMethodCall(String expr) {
        // Remove trailing parentheses and args for analysis
        int parenIndex = expr.indexOf('(');
        if (parenIndex > 0) {
            expr = expr.substring(0, parenIndex);
        }
        
        String[] parts = expr.split("\\.");
        if (parts.length < 2) return null;
        
        // Start with the receiver type
        String currentType = variableTypes.get(parts[0]);
        if (currentType == null) return null;
        
        // Walk the chain
        for (int i = 1; i < parts.length; i++) {
            JSTypeInfo typeInfo = registry.getType(currentType);
            if (typeInfo == null) return null;
            
            String member = parts[i];
            
            // Check if it's a method
            JSMethodInfo method = typeInfo.getMethod(member);
            if (method != null) {
                currentType = method.getReturnType();
                continue;
            }
            
            // Check if it's a field
            JSFieldInfo field = typeInfo.getField(member);
            if (field != null) {
                currentType = field.getType();
                continue;
            }
            
            return null; // Unknown member
        }
        
        return currentType;
    }
    
    /**
     * Mark member accesses (x.y.z) with type validation.
     */
    private void markMemberAccesses(List<ScriptLine.Mark> marks) {
        Matcher m = MEMBER_ACCESS_PATTERN.matcher(text);
        while (m.find()) {
            if (isExcluded(m.start())) continue;
            
            String fullAccess = m.group(0);
            String[] parts = fullAccess.split("\\.");
            
            if (parts.length < 2) continue;
            
            // Get receiver type
            String receiverName = parts[0];
            String currentType = variableTypes.get(receiverName);
            
            int pos = m.start();
            
            // Mark receiver
            if (currentType != null) {
                JSTypeInfo typeInfo = registry.getType(currentType);
                marks.add(new ScriptLine.Mark(pos, pos + receiverName.length(), TokenType.LOCAL_FIELD,
                    new JSHoverInfo.VariableInfo(receiverName, currentType, typeInfo)));
            }
            
            pos += receiverName.length() + 1; // +1 for the dot
            
            // Walk the chain and mark each member
            for (int i = 1; i < parts.length; i++) {
                String member = parts[i];
                int memberStart = pos;
                int memberEnd = pos + member.length();
                
                if (currentType != null) {
                    JSTypeInfo typeInfo = registry.getType(currentType);
                    if (typeInfo != null) {
                        // Check method first
                        JSMethodInfo method = typeInfo.getMethod(member);
                        if (method != null) {
                            marks.add(new ScriptLine.Mark(memberStart, memberEnd, TokenType.METHOD_CALL,
                                new JSHoverInfo.MethodInfo(method, typeInfo)));
                            currentType = method.getReturnType();
                        } else {
                            // Check field
                            JSFieldInfo field = typeInfo.getField(member);
                            if (field != null) {
                                marks.add(new ScriptLine.Mark(memberStart, memberEnd, TokenType.GLOBAL_FIELD,
                                    new JSHoverInfo.FieldInfo(field, typeInfo)));
                                currentType = field.getType();
                            } else {
                                // Unknown member - mark as undefined
                                marks.add(new ScriptLine.Mark(memberStart, memberEnd, TokenType.UNDEFINED_VAR,
                                    "Unknown member '" + member + "' on type " + typeInfo.getFullName()));
                                currentType = null;
                            }
                        }
                    } else {
                        currentType = null;
                    }
                }
                
                pos = memberEnd + 1; // +1 for the next dot
            }
        }
    }
    
    /**
     * Mark method calls.
     */
    private void markMethodCalls(List<ScriptLine.Mark> marks) {
        Matcher m = METHOD_CALL_PATTERN.matcher(text);
        while (m.find()) {
            if (isExcluded(m.start())) continue;
            
            String callExpr = m.group(1);
            if (!callExpr.contains(".")) {
                // Simple function call
                int nameStart = m.start(1);
                int nameEnd = m.end(1);
                
                if (registry.isHook(callExpr)) {
                    // It's a known hook being called
                    marks.add(new ScriptLine.Mark(nameStart, nameEnd, TokenType.METHOD_CALL,
                        new JSHoverInfo.FunctionInfo(callExpr, registry.getHookSignatures(callExpr))));
                }
            }
            // Chained calls are handled in markMemberAccesses
        }
    }
    
    /**
     * Add pattern-based marks.
     */
    private void addPatternMarks(List<ScriptLine.Mark> marks, Pattern pattern, TokenType type) {
        Matcher m = pattern.matcher(text);
        while (m.find()) {
            marks.add(new ScriptLine.Mark(m.start(), m.end(), type));
        }
    }
    
    /**
     * Get the inferred type of a variable at a position.
     */
    public String getVariableType(String varName) {
        return variableTypes.get(varName);
    }
    
    /**
     * Get all inferred variable types.
     */
    public Map<String, String> getVariableTypes() {
        return new HashMap<>(variableTypes);
    }
}
