package noppes.npcs.client.gui.util.script.interpreter.js_parser;

import noppes.npcs.client.gui.util.script.interpreter.ScriptDocument;
import noppes.npcs.client.gui.util.script.interpreter.ScriptLine;
import noppes.npcs.client.gui.util.script.interpreter.field.FieldInfo;
import noppes.npcs.client.gui.util.script.interpreter.method.MethodInfo;
import noppes.npcs.client.gui.util.script.interpreter.token.TokenType;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;
import scala.annotation.meta.field;

import java.util.*;
import java.util.regex.*;

/**
 * Analyzes JavaScript scripts to produce syntax highlighting marks and type information.
 * Uses the JSTypeRegistry to resolve types and validate member access.
 * 
 * @deprecated This class is deprecated. All JavaScript analysis is now handled by the
 * unified pipeline in {@link ScriptDocument}. The functionality has been merged into:
 * <ul>
 *   <li>{@link ScriptDocument#parseJSFunctions()} - for function parsing</li>
 *   <li>{@link ScriptDocument#parseJSVariables()} - for variable parsing</li>
 *   <li>{@link ScriptDocument#buildJSMarks(List)} - for mark building</li>
 * </ul>
 * Use {@link ScriptDocument#getJSVariableTypes()} and {@link ScriptDocument#getJSFunctionParams()}
 * to access inferred type information.
 */
@Deprecated
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
        
        // Mark standalone identifiers (parameters and variables in function bodies)
        markIdentifiers(marks);
        
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
                // Create a unified MethodInfo for the hook
                List<JSTypeRegistry.HookSignature> sigs = registry.getHookSignatures(funcName);
                if (!sigs.isEmpty()) {
                    JSTypeRegistry.HookSignature sig = sigs.get(0);
                    
                    // Create unified MethodInfo for hover info
                    TypeInfo paramTypeInfo = resolveJSType(sig.paramType);
                    List<FieldInfo> methodParams = new ArrayList<>();
                    methodParams.add(FieldInfo.reflectionParam(sig.paramName, paramTypeInfo));
                    
                    MethodInfo hookMethod = MethodInfo.declaration(
                        funcName, null, TypeInfo.fromPrimitive("void"), methodParams,
                        nameStart, nameStart, nameStart, -1, -1, 0, sig.doc
                    );
                    
                    marks.add(new ScriptLine.Mark(nameStart, nameEnd, TokenType.METHOD_DECL, hookMethod));
                    
                    // Infer parameter types from hook
                    if (!params.isEmpty()) {
                        // Parse parameter names from function
                        String[] paramNames = params.split(",");
                        if (paramNames.length > 0) {
                            String paramName = paramNames[0].trim();
                            String paramType = sig.paramType;
                            
                            // Store in function params and variable types
                            Map<String, String> funcParamMap = new HashMap<>();
                            funcParamMap.put(paramName, paramType);
                            functionParams.put(funcName, funcParamMap);
                            variableTypes.put(paramName, paramType);
                            
                            // Mark the parameter with unified FieldInfo
                            int paramStart = m.start(2) + params.indexOf(paramName);
                            int paramEnd = paramStart + paramName.length();
                            FieldInfo paramFieldInfo = FieldInfo.parameter(
                                paramName, paramTypeInfo, paramStart, hookMethod
                            );
                            marks.add(new ScriptLine.Mark(paramStart, paramEnd, TokenType.PARAMETER, paramFieldInfo));
                        }
                    }
                } else {
                    marks.add(new ScriptLine.Mark(nameStart, nameEnd, TokenType.METHOD_DECL, funcName));
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
            
            // Mark variable declaration with unified FieldInfo
            int varStart = m.start(1);
            int varEnd = m.end(1);
            TypeInfo typeInfo = resolveJSType(inferredType != null ? inferredType : "any");
            FieldInfo varFieldInfo = FieldInfo.localField(varName, typeInfo, varStart, null);
            marks.add(new ScriptLine.Mark(varStart, varEnd, TokenType.LOCAL_FIELD, varFieldInfo));
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
            JSFieldInfo f = typeInfo.getField(member);
            if (f != null) {
                currentType = f.getType();
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
            
            // Mark receiver with unified FieldInfo
            if (currentType != null) {
                TypeInfo unifiedType = resolveJSType(currentType);
                FieldInfo receiverField = FieldInfo.localField(receiverName, unifiedType, pos, null);
                marks.add(new ScriptLine.Mark(pos, pos + receiverName.length(), TokenType.LOCAL_FIELD, receiverField));
            }
            
            pos += receiverName.length() + 1; // +1 for the dot
            
            // Walk the chain and mark each member
            for (int i = 1; i < parts.length; i++) {
                String member = parts[i];
                int memberStart = pos;
                int memberEnd = pos + member.length();
                
                if (currentType != null) {
                    JSTypeInfo jsTypeInfo = registry.getType(currentType);
                    if (jsTypeInfo != null) {
                        TypeInfo unifiedContainingType = TypeInfo.fromJSTypeInfo(jsTypeInfo);
                        
                        // Check method first
                        JSMethodInfo jsMethod = jsTypeInfo.getMethod(member);
                        if (jsMethod != null) {
                            // Convert to unified MethodInfo
                            MethodInfo unifiedMethod = MethodInfo.fromJSMethod(jsMethod, unifiedContainingType);
                            marks.add(new ScriptLine.Mark(memberStart, memberEnd, TokenType.METHOD_CALL, unifiedMethod));
                            currentType = jsMethod.getReturnType();
                        } else {
                            // Check field
                            JSFieldInfo jsField = jsTypeInfo.getField(member);
                            if (jsField != null) {
                                // Convert to unified FieldInfo
                                FieldInfo unifiedField = FieldInfo.fromJSField(jsField, unifiedContainingType);
                                marks.add(new ScriptLine.Mark(memberStart, memberEnd, TokenType.GLOBAL_FIELD, unifiedField));
                                currentType = jsField.getType();
                            } else {
                                // Unknown member - mark as undefined
                                marks.add(new ScriptLine.Mark(memberStart, memberEnd, TokenType.UNDEFINED_VAR,
                                    "Unknown member '" + member + "' on type " + jsTypeInfo.getFullName()));
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
                    // It's a known hook being called - create unified MethodInfo
                    List<JSTypeRegistry.HookSignature> sigs = registry.getHookSignatures(callExpr);
                    if (!sigs.isEmpty()) {
                        JSTypeRegistry.HookSignature sig = sigs.get(0);
                        TypeInfo paramTypeInfo = resolveJSType(sig.paramType);
                        List<FieldInfo> methodParams = new ArrayList<>();
                        methodParams.add(FieldInfo.reflectionParam(sig.paramName, paramTypeInfo));
                        
                        MethodInfo hookMethod = MethodInfo.declaration(
                            callExpr, null, TypeInfo.fromPrimitive("void"), methodParams,
                            nameStart, nameStart, nameStart, -1, -1, 0, sig.doc
                        );
                        marks.add(new ScriptLine.Mark(nameStart, nameEnd, TokenType.METHOD_CALL, hookMethod));
                    }
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
    
    /**
     * Resolves a JavaScript type name to a unified TypeInfo.
     * Handles primitives, mapped types, and custom types from the registry.
     */
    private TypeInfo resolveJSType(String jsTypeName) {
        if (jsTypeName == null || jsTypeName.isEmpty() || "void".equals(jsTypeName)) {
            return TypeInfo.fromPrimitive("void");
        }
        
        // Handle JS primitives
        switch (jsTypeName) {
            case "string":
                return TypeInfo.fromClass(String.class);
            case "number":
                return TypeInfo.fromClass(double.class);
            case "boolean":
                return TypeInfo.fromClass(boolean.class);
            case "any":
                return TypeInfo.fromClass(Object.class);
            case "void":
                return TypeInfo.fromPrimitive("void");
        }
        
        // Handle array types
        if (jsTypeName.endsWith("[]")) {
            String elementType = jsTypeName.substring(0, jsTypeName.length() - 2);
            TypeInfo elementTypeInfo = resolveJSType(elementType);
            return TypeInfo.arrayOf(elementTypeInfo);
        }
        
        // Try to resolve from the JS type registry
        if (registry != null) {
            JSTypeInfo jsTypeInfo = registry.getType(jsTypeName);
            if (jsTypeInfo != null) {
                return TypeInfo.fromJSTypeInfo(jsTypeInfo);
            }
        }
        
        // Fallback: unresolved type
        return TypeInfo.unresolved(jsTypeName, jsTypeName);
    }
    
    /**
     * Mark all identifiers in the script - variables and parameters.
     * This ensures consistent coloring for parameters throughout function bodies.
     */
    private void markIdentifiers(List<ScriptLine.Mark> marks) {
        // Build set of positions already marked
        Set<Integer> markedPositions = new HashSet<>();
        for (ScriptLine.Mark mark : marks) {
            for (int i = mark.start; i < mark.end; i++) {
                markedPositions.add(i);
            }
        }
        
        // Find all identifiers
        Matcher matcher = IDENTIFIER_PATTERN.matcher(text);
        while (matcher.find()) {
            if (isExcluded(matcher.start())) {
                continue;
            }
            
            int start = matcher.start();
            int end = matcher.end();
            
            // Skip if already marked
            boolean alreadyMarked = false;
            for (int i = start; i < end; i++) {
                if (markedPositions.contains(i)) {
                    alreadyMarked = true;
                    break;
                }
            }
            if (alreadyMarked) {
                continue;
            }
            
            String identifier = matcher.group();
            
            // Check if it's a parameter
            boolean isParameter = false;
            for (Map<String, String> params : functionParams.values()) {
                if (params.containsKey(identifier)) {
                    isParameter = true;
                    break;
                }
            }
            
            if (isParameter) {
                marks.add(new ScriptLine.Mark(start, end, TokenType.PARAMETER));
                // Mark this position as used
                for (int i = start; i < end; i++) {
                    markedPositions.add(i);
                }
            } else if (variableTypes.containsKey(identifier)) {
                // It's a local variable
                marks.add(new ScriptLine.Mark(start, end, TokenType.LOCAL_FIELD));
                // Mark this position as used
                for (int i = start; i < end; i++) {
                    markedPositions.add(i);
                }
            }
        }
    }
}
