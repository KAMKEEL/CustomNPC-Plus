package noppes.npcs.client.gui.util.script.autocomplete;

import noppes.npcs.client.gui.util.script.interpreter.ScriptDocument;
import noppes.npcs.client.gui.util.script.interpreter.js_parser.*;

import java.util.*;

/**
 * Autocomplete provider for JavaScript/ECMAScript scripts.
 * Uses JSTypeRegistry for type resolution.
 */
public class JSAutocompleteProvider implements AutocompleteProvider {
    
    private ScriptDocument document;
    private final JSTypeRegistry registry;
    
    // Variable types inferred from the document
    private Map<String, String> variableTypes = new HashMap<>();
    
    public JSAutocompleteProvider() {
        this.registry = JSTypeRegistry.getInstance();
        if (!registry.isInitialized()) {
            registry.initializeFromResources();
        }
    }
    
    public void setDocument(ScriptDocument document) {
        this.document = document;
    }
    
    /**
     * Update the variable type map from document analysis.
     */
    public void updateVariableTypes(Map<String, String> types) {
        this.variableTypes = types != null ? new HashMap<>(types) : new HashMap<>();
    }
    
    @Override
    public boolean canProvide(Context context) {
        return document != null && document.isJavaScript();
    }
    
    @Override
    public List<AutocompleteItem> getSuggestions(Context context) {
        List<AutocompleteItem> items = new ArrayList<>();
        
        // Resolve owner type for usage tracking
        String ownerFullName = null;
        if (context.isMemberAccess && context.receiverExpression != null) {
            String receiverType = resolveReceiverType(context.receiverExpression);
            if (receiverType != null) {
                ownerFullName = receiverType;
            }
        }
        
        if (context.isMemberAccess) {
            // Member access: resolve type of receiver and get its members
            addMemberSuggestions(context, items);
        } else {
            // Identifier context: show variables, functions, types in scope
            addScopeSuggestions(context, items);
        }
        
        // Filter and score by prefix, then apply usage boosts
        filterAndScore(items, context.prefix, context.isMemberAccess, ownerFullName);
        
        // Sort by score
        Collections.sort(items);
        
        // Limit results
        if (items.size() > 50) {
            items = items.subList(0, 50);
        }
        
        return items;
    }
    
    /**
     * Add suggestions for member access (after dot).
     */
    private void addMemberSuggestions(Context context, List<AutocompleteItem> items) {
        String receiverExpr = context.receiverExpression;
        if (receiverExpr == null || receiverExpr.isEmpty()) {
            return;
        }
        
        // Infer type of receiver
        String receiverType = inferExpressionType(receiverExpr);
        if (receiverType == null) {
            return;
        }
        
        JSTypeInfo typeInfo = registry.getType(receiverType);
        if (typeInfo == null) {
            return;
        }
        
        // Add methods
        addMethodsFromType(typeInfo, items, new HashSet<>());
        
        // Add fields
        addFieldsFromType(typeInfo, items, new HashSet<>());
    }
    
    /**
     * Recursively add methods from a type and its parents.
     */
    private void addMethodsFromType(JSTypeInfo type, List<AutocompleteItem> items, Set<String> added) {
        for (JSMethodInfo method : type.getMethods().values()) {
            String name = method.getName();
            // Skip overload markers (name$1, name$2, etc.)
            if (name.contains("$")) {
                name = name.substring(0, name.indexOf('$'));
            }
            if (!added.contains(name)) {
                added.add(name);
                items.add(AutocompleteItem.fromJSMethod(method));
            }
        }
        
        // Add from parent type
        if (type.getResolvedParent() != null) {
            addMethodsFromType(type.getResolvedParent(), items, added);
        }
    }
    
    /**
     * Recursively add fields from a type and its parents.
     */
    private void addFieldsFromType(JSTypeInfo type, List<AutocompleteItem> items, Set<String> added) {
        for (JSFieldInfo field : type.getFields().values()) {
            if (!added.contains(field.getName())) {
                added.add(field.getName());
                items.add(AutocompleteItem.fromJSField(field));
            }
        }
        
        // Add from parent type
        if (type.getResolvedParent() != null) {
            addFieldsFromType(type.getResolvedParent(), items, added);
        }
    }
    
    /**
     * Add suggestions based on current scope (not after a dot).
     */
    private void addScopeSuggestions(Context context, List<AutocompleteItem> items) {
        // Add local variables
        for (Map.Entry<String, String> entry : variableTypes.entrySet()) {
            String varName = entry.getKey();
            String varType = entry.getValue();
            
            items.add(new AutocompleteItem.Builder()
                .name(varName)
                .insertText(varName)
                .kind(AutocompleteItem.Kind.VARIABLE)
                .typeLabel(varType != null ? varType : "any")
                .build());
        }
        
        // Add hook functions
        for (String hookName : registry.getHookNames()) {
            List<JSTypeRegistry.HookSignature> sigs = registry.getHookSignatures(hookName);
            if (!sigs.isEmpty()) {
                JSTypeRegistry.HookSignature sig = sigs.get(0);
                items.add(new AutocompleteItem.Builder()
                    .name(hookName)
                    .insertText(hookName)
                    .kind(AutocompleteItem.Kind.METHOD)
                    .typeLabel("hook")
                    .signature("function " + hookName + "(e: " + sig.paramType + ")")
                    .documentation(sig.doc)
                    .build());
            }
        }
        
        // Add global types
        for (String typeName : registry.getTypeNames()) {
            JSTypeInfo type = registry.getType(typeName);
            if (type != null) {
                items.add(new AutocompleteItem.Builder()
                    .name(typeName)
                    .insertText(typeName)
                    .kind(AutocompleteItem.Kind.CLASS)
                    .typeLabel("interface")
                    .build());
            }
        }
        
        // Add JavaScript keywords
        addJSKeywords(items);
    }
    
    /**
     * Infer the type of an expression.
     */
    private String inferExpressionType(String expr) {
        return resolveReceiverType(expr);
    }
    
    /**
     * Resolve the type of a receiver expression for member access.
     */
    private String resolveReceiverType(String expr) {
        if (expr == null || expr.isEmpty()) {
            return null;
        }
        
        expr = expr.trim();
        
        // Check if it's a known variable
        if (variableTypes.containsKey(expr)) {
            return variableTypes.get(expr);
        }
        
        // Handle method chain: x.y.z() -> resolve step by step
        if (expr.contains(".")) {
            String[] parts = expr.split("\\.");
            String currentType = null;
            
            // Start with the first part
            if (variableTypes.containsKey(parts[0])) {
                currentType = variableTypes.get(parts[0]);
            }
            
            if (currentType == null) {
                return null;
            }
            
            // Walk through the chain
            for (int i = 1; i < parts.length; i++) {
                String member = parts[i];
                // Remove () for method calls
                if (member.endsWith("()")) {
                    member = member.substring(0, member.length() - 2);
                }
                
                JSTypeInfo typeInfo = registry.getType(currentType);
                if (typeInfo == null) {
                    return null;
                }
                
                // Check method
                JSMethodInfo method = typeInfo.getMethod(member);
                if (method != null) {
                    currentType = method.getReturnType();
                    continue;
                }
                
                // Check field
                JSFieldInfo field = typeInfo.getField(member);
                if (field != null) {
                    currentType = field.getType();
                    continue;
                }
                
                // Unknown member
                return null;
            }
            
            return currentType;
        }
        
        return null;
    }
    
    /**
     * Add JavaScript keywords.
     */
    private void addJSKeywords(List<AutocompleteItem> items) {
        String[] keywords = {
            "function", "var", "let", "const", "if", "else", "for", "while", "do",
            "switch", "case", "break", "continue", "return", "try", "catch", "finally",
            "throw", "new", "typeof", "instanceof", "in", "of", "this", "null",
            "undefined", "true", "false", "async", "await", "yield", "class", "extends",
            "import", "export", "default"
        };
        
        for (String keyword : keywords) {
            items.add(AutocompleteItem.keyword(keyword));
        }
    }
    
    /**
     * Filter items by prefix, calculate match scores, and apply usage boosts.
     */
    private void filterAndScore(List<AutocompleteItem> items, String prefix,
                                 boolean isMemberAccess, String ownerFullName) {
        UsageTracker tracker = UsageTracker.getJSInstance();
        
        if (prefix == null || prefix.isEmpty()) {
            for (AutocompleteItem item : items) {
                item.calculateMatchScore("");
                applyUsageBoost(item, tracker, ownerFullName);
            }
            return;
        }
        
        // For non-member access (first word), require strict prefix matching
        // For member access (after dot), allow fuzzy/contains matching
        boolean requirePrefix = !isMemberAccess;
        
        Iterator<AutocompleteItem> iter = items.iterator();
        while (iter.hasNext()) {
            AutocompleteItem item = iter.next();
            int score = item.calculateMatchScore(prefix, requirePrefix);
            if (score < 0) {
                iter.remove();
            } else {
                applyUsageBoost(item, tracker, ownerFullName);
            }
        }
    }
    
    /**
     * Apply usage-based score boost to an item.
     */
    private void applyUsageBoost(AutocompleteItem item, UsageTracker tracker, String ownerFullName) {
        int usageCount = tracker.getUsageCount(item, ownerFullName);
        int boost = UsageTracker.calculateUsageBoost(usageCount);
        item.addScoreBoost(boost);
    }
}
