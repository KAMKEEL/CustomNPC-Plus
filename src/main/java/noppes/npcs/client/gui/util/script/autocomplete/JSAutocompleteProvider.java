package noppes.npcs.client.gui.util.script.autocomplete;

import noppes.npcs.client.gui.util.script.interpreter.ScriptDocument;
import noppes.npcs.client.gui.util.script.interpreter.field.FieldInfo;
import noppes.npcs.client.gui.util.script.interpreter.js_parser.*;
import noppes.npcs.client.gui.util.script.interpreter.method.MethodInfo;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;

import java.util.*;

/**
 * Autocomplete provider for JavaScript/ECMAScript scripts.
 * Uses ScriptDocument's unified TypeInfo/TypeResolver system for type resolution.
 */
public class JSAutocompleteProvider extends JavaAutocompleteProvider {
    
    private final JSTypeRegistry registry;
    
    public JSAutocompleteProvider() {
        this.registry = JSTypeRegistry.getInstance();
        if (!registry.isInitialized()) {
            registry.initializeFromResources();
        }
    }
    
    @Override
    public boolean canProvide(Context context) {
        return document != null && document.isJavaScript();
    }
    
    @Override
    public List<AutocompleteItem> getSuggestions(Context context) {
        List<AutocompleteItem> items = new ArrayList<>();

        // Resolve owner type for usage tracking using ScriptDocument's unified type resolution
        String ownerFullName = null;
        if (context.isMemberAccess && context.receiverExpression != null) {
            TypeInfo receiverType = document.resolveExpressionType(context.receiverExpression, context.prefixStart);
            if (receiverType != null && receiverType.isResolved()) {
                ownerFullName = receiverType.getFullName();
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
        filterAndScore(items, context.prefix, context.isMemberAccess, false, ownerFullName);
        
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
     * Uses ScriptDocument's unified type resolution system.
     */
    protected void addMemberSuggestions(Context context, List<AutocompleteItem> items) {
        String receiverExpr = context.receiverExpression;
        if (receiverExpr == null || receiverExpr.isEmpty()) {
            return;
        }

        // Use ScriptDocument's resolveExpressionType - handles both Java and JS
        TypeInfo receiverType = document.resolveExpressionType(receiverExpr, context.prefixStart);
        if (receiverType == null || !receiverType.isResolved()) {
            return;
        }

        // For JS types, check JSTypeRegistry
        JSTypeInfo jsTypeInfo = registry.getType(receiverType.getSimpleName());
        if (jsTypeInfo != null) {
            // Add methods
            addMethodsFromType(jsTypeInfo, items, new HashSet<>());
            // Add fields
            addFieldsFromType(jsTypeInfo, items, new HashSet<>());
            return;
        }

        // For Java types with Java class, use reflection to get members
        Class<?> javaClass = receiverType.getJavaClass();
        if (javaClass != null) {
            try {
                // Add methods from Java reflection
                for (java.lang.reflect.Method method : javaClass.getMethods()) {
                    String methodName = method.getName();
                    StringBuilder signature = new StringBuilder();
                    signature.append(methodName).append("(");
                    Class<?>[] params = method.getParameterTypes();
                    for (int i = 0; i < params.length; i++) {
                        if (i > 0)
                            signature.append(", ");
                        signature.append(params[i].getSimpleName());
                    }
                    signature.append(")");

                    items.add(new AutocompleteItem.Builder()
                            .name(methodName)
                            .insertText(methodName)
                            .kind(AutocompleteItem.Kind.METHOD)
                            .typeLabel(method.getReturnType().getSimpleName())
                            .signature(signature.toString())
                            .build());
                }

                // Add fields from Java reflection
                for (java.lang.reflect.Field field : javaClass.getFields()) {
                    items.add(new AutocompleteItem.Builder()
                            .name(field.getName())
                            .insertText(field.getName())
                            .kind(AutocompleteItem.Kind.FIELD)
                            .typeLabel(field.getType().getSimpleName())
                            .build());
                }
            } catch (SecurityException e) {
                // Can't access members, skip
            }
        }
    }
    
    /**
     * Recursively add methods from a type and its parents.
     */
    protected void addMethodsFromType(JSTypeInfo type, List<AutocompleteItem> items, Set<String> added) {
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
    protected void addFieldsFromType(JSTypeInfo type, List<AutocompleteItem> items, Set<String> added) {
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
     * Uses ScriptDocument's unified data structures for variables and functions.
     */
    protected void addScopeSuggestions(Context context, List<AutocompleteItem> items) {

        int pos = context.cursorPosition;

        // Find containing method
        MethodInfo containingMethod = document.findContainingMethod(pos);

        // Add local variables
        if (containingMethod != null) {
            Map<String, FieldInfo> locals = document.getLocalsForMethod(containingMethod);
            if (locals != null) {
                for (FieldInfo local : locals.values()) {
                    if (local.isVisibleAt(pos)) {
                        items.add(AutocompleteItem.fromField(local));
                    }
                }
            }

            // Add method parameters
            for (FieldInfo param : containingMethod.getParameters()) {
                items.add(AutocompleteItem.fromField(param));
            }
        }

        // Add global fields
        for (FieldInfo globalField : document.getGlobalFields().values()) {
            if (globalField.isVisibleAt(pos)) {
                items.add(AutocompleteItem.fromField(globalField));
            }
        }

        // Add script-defined methods
        for (MethodInfo method : document.getAllMethods()) {
            items.add(AutocompleteItem.fromMethod(method));
            
        }
        
        // Add JavaScript keywords
        addKeywords(items);
    }
    

    
    /**
     * Add JavaScript keywords.
     */
    protected void addKeywords(List<AutocompleteItem> items) {
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

    protected UsageTracker getUsageTracker() {
        return UsageTracker.getJSInstance();
    }
}
