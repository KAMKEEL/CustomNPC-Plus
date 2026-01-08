package noppes.npcs.client.gui.util.script.autocomplete;

import noppes.npcs.client.gui.util.script.interpreter.js_parser.*;
import noppes.npcs.client.gui.util.script.interpreter.type.synthetic.*;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeChecker;
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

        // Check for synthetic types first (e.g., Nashorn Java object, custom types)
        SyntheticType syntheticType = 
            document.getTypeResolver().getSyntheticType(receiverType.getSimpleName());
        if (syntheticType != null) {
            addSyntheticTypeSuggestions(syntheticType, items);
            return;
        }

        // If this is a Java type (has a Java class), delegate to parent's Java handling
        // This preserves static/instance context, modifiers, and all Java-specific behavior
        Class<?> javaClass = receiverType.getJavaClass();
        if (javaClass != null) {
            super.addMemberSuggestions(context, items);
            return;
        }

        // For pure JS types, check JSTypeRegistry
        JSTypeInfo jsTypeInfo = receiverType.getJSTypeInfo();
        if (jsTypeInfo != null) {
            // Pass both: jsTypeInfo (current type in hierarchy) and receiverType (context for type params)
            addMethodsFromType(jsTypeInfo, receiverType, items, new HashSet<>());
            addFieldsFromType(jsTypeInfo, receiverType, items, new HashSet<>());
        }
    }
    
    /**
     * Add autocomplete suggestions for a synthetic type's methods and fields.
     */
    private void addSyntheticTypeSuggestions(SyntheticType syntheticType, List<AutocompleteItem> items) {
        // Add methods
        for (SyntheticMethod method : syntheticType.getMethods()) {
            AutocompleteItem item = AutocompleteItem.fromSyntheticMethod(method, syntheticType.getTypeInfo());
            items.add(item);
        }
        
        // Add fields
        for (SyntheticField field : syntheticType.getFields()) {
            AutocompleteItem item = AutocompleteItem.fromSyntheticField(field);
            items.add(item);
        }
    }
    
    /**
     * Recursively add methods from a type and its parents.
     * @param type The current JS type to get methods from (changes as we walk up inheritance)
     * @param contextType The original TypeInfo context for resolving type parameters (stays constant)
     */
    protected void addMethodsFromType(JSTypeInfo type, TypeInfo contextType, List<AutocompleteItem> items, Set<String> added) {
        addMethodsFromType(type, contextType, items, added, 0);
    }
    
    /**
     * Recursively add methods from a type and its parents with inheritance depth tracking.
     * @param type The current JS type in inheritance chain
     * @param contextType The original TypeInfo context for resolving type parameters (e.g., IPlayer with T → EntityPlayerMP)
     */
    private void addMethodsFromType(JSTypeInfo type, TypeInfo contextType, List<AutocompleteItem> items, Set<String> added, int depth) {
        for (JSMethodInfo method : type.getMethods().values()) {
            String name = method.getName();
            // Skip overload markers (name$1, name$2, etc.)
            if (name.contains("$")) {
                name = name.substring(0, name.indexOf('$'));
            }
            if (!added.contains(name)) {
                added.add(name);
                // Pass contextType (original receiver) for type parameter resolution, not current type
                items.add(AutocompleteItem.fromJSMethod(method, contextType, depth));
            }
        }
        
        // Add from parent type with incremented depth - keep same contextType
        if (type.getResolvedParent() != null) {
            addMethodsFromType(type.getResolvedParent(), contextType, items, added, depth + 1);
        }
    }
    
    /**
     * Recursively add fields from a type and its parents.
     * @param type The current JS type to get fields from (changes as we walk up inheritance)
     * @param contextType The original TypeInfo context for resolving type parameters (stays constant)
     */
    protected void addFieldsFromType(JSTypeInfo type, TypeInfo contextType, List<AutocompleteItem> items, Set<String> added) {
        addFieldsFromType(type, contextType, items, added, 0);
    }
    
    /**
     * Recursively add fields from a type and its parents with inheritance depth tracking.
     * @param type The current JS type in inheritance chain
     * @param contextType The original TypeInfo context for resolving type parameters
     */
    private void addFieldsFromType(JSTypeInfo type, TypeInfo contextType, List<AutocompleteItem> items, Set<String> added, int depth) {
        for (JSFieldInfo field : type.getFields().values()) {
            if (!added.contains(field.getName())) {
                added.add(field.getName());
                // Pass contextType (original receiver) for type parameter resolution, not current type
                items.add(AutocompleteItem.fromJSField(field, contextType, depth));
            }
        }
        
        // Add from parent type with incremented depth - keep same contextType
        if (type.getResolvedParent() != null) {
            addFieldsFromType(type.getResolvedParent(), contextType, items, added, depth + 1);
        }
    }

    protected void addUnimportedClassSuggestions(String prefix, List<AutocompleteItem> items) {
        // For JavaScript, we typically don't suggest unimported classes
        // as imports are handled differently. This can be customized
        // if needed to suggest global JS types.
    }

    protected UsageTracker getUsageTracker() {
        return UsageTracker.getJSInstance();
    }

    public String[] getKeywords() {
        return TypeChecker.getJaveScriptKeywords();
    }
}
