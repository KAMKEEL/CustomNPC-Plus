package noppes.npcs.client.gui.util.script.autocomplete;

import noppes.npcs.client.gui.util.script.interpreter.js_parser.*;
import noppes.npcs.client.gui.util.script.interpreter.field.FieldInfo;
import noppes.npcs.client.gui.util.script.interpreter.method.MethodInfo;
import noppes.npcs.client.gui.util.script.interpreter.type.synthetic.*;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeChecker;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;
import noppes.npcs.config.ConfigScript;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
        TypeInfo receiverType = document.resolveExpressionType(receiverExpr, getMemberAccessResolvePosition(context));
        if (receiverType == null || !receiverType.isResolved()) {
            return;
        }

        // Check for synthetic types first (e.g., Nashorn Java object, custom types)
        SyntheticType syntheticType = 
            document.getTypeResolver().getSyntheticType(receiverType.getSimpleName());
        if (syntheticType != null) {
            addSyntheticTypeSuggestions(syntheticType, items, context.methodsOnly);
            return;
        }

        boolean isStaticContext = isStaticAccess(receiverExpr, getMemberAccessResolvePosition(context));

        Class<?> javaClass = receiverType.getJavaClass();
        if (javaClass != null) {
            Set<String> addedMethods = new HashSet<>();
            for (Method method : javaClass.getMethods()) {
                if (!Modifier.isPublic(method.getModifiers())) continue;
                if (isStaticContext && !Modifier.isStatic(method.getModifiers())) continue;
                String sig = method.getName() + "(" + method.getParameterCount() + ")";
                if (addedMethods.add(sig)) {
                    items.add(AutocompleteItem.fromMethod(MethodInfo.fromReflection(method, receiverType), context.methodsOnly));
                }
            }
            List<MethodInfo> effectiveSyntheticMethods = receiverType.getSyntheticMethods();
            if (!effectiveSyntheticMethods.isEmpty()) {
                for (MethodInfo method : effectiveSyntheticMethods) {
                    if (isStaticContext && !method.isStatic()) continue;
                    items.add(AutocompleteItem.fromMethod(method, context.methodsOnly));
                }
            } else {
                JSTypeInfo jsType = getMergeRegistryType(javaClass);
                if (jsType != null) {
                    addMethodsFromType(jsType, receiverType, items, new HashSet<>(), context.methodsOnly, isStaticContext);
                }
            }
            if (!context.methodsOnly) {
                for (Field field : javaClass.getFields()) {
                    if (!Modifier.isPublic(field.getModifiers())) continue;
                    if (isStaticContext && !Modifier.isStatic(field.getModifiers())) continue;
                    items.add(AutocompleteItem.fromField(FieldInfo.fromReflection(field, receiverType)));
                }
                if (!receiverType.getSyntheticFields().isEmpty()) {
                    for (FieldInfo field : receiverType.getSyntheticFields()) {
                        if (isStaticContext && !field.isStatic()) continue;
                        items.add(AutocompleteItem.fromField(field));
                    }
                } else {
                    JSTypeInfo jsType = getMergeRegistryType(javaClass);
                    if (jsType != null) {
                        addFieldsFromType(jsType, receiverType, items, new HashSet<>(), isStaticContext);
                    }
                }
            }
            if (!isStaticContext && javaClass != Object.class) {
                JSTypeInfo objJSType = registry.getType("Object");
                if (objJSType != null) {
                    for (JSMethodInfo m : objJSType.getMethods().values()) {
                        if (!m.isStatic()) {
                            items.add(AutocompleteItem.fromJSMethod(m, receiverType, 1, context.methodsOnly));
                        }
                    }
                }
            }
            return;
        }

        Set<String> syntheticMethodNames = new HashSet<>();
        for (MethodInfo method : receiverType.getSyntheticMethods()) {
            syntheticMethodNames.add(method.getName());
            if (isStaticContext && !method.isStatic()) continue;
            items.add(AutocompleteItem.fromMethod(method, context.methodsOnly));
        }
        for (FieldInfo field : receiverType.getSyntheticFields()) {
            if (!context.methodsOnly) {
                if (isStaticContext && !field.isStatic()) continue;
                if (syntheticMethodNames.contains(field.getName())) continue;
                items.add(AutocompleteItem.fromField(field));
            }
        }

        JSTypeInfo jsTypeInfo = receiverType.getJSTypeInfo();
        if (jsTypeInfo != null) {
            addMethodsFromType(jsTypeInfo, receiverType, items, new HashSet<>(), context.methodsOnly, isStaticContext);
            if (!context.methodsOnly && ConfigScript.ShowImplementationFieldsInAutocomplete) {
                addFieldsFromType(jsTypeInfo, receiverType, items, new HashSet<>(), isStaticContext);
            }
        }

        if (!isStaticContext) {
            Set<String> addedObjMethods = new HashSet<>();
            for (Method m : Object.class.getMethods()) {
                if (!Modifier.isPublic(m.getModifiers()) || Modifier.isStatic(m.getModifiers())) continue;
                String sig = m.getName() + "(" + m.getParameterCount() + ")";
                if (addedObjMethods.add(sig)) {
                    items.add(AutocompleteItem.fromMethod(MethodInfo.fromReflection(m, receiverType), context.methodsOnly));
                }
            }
            JSTypeInfo objJSType = registry.getType("Object");
            if (objJSType != null) {
                for (JSMethodInfo m : objJSType.getMethods().values()) {
                    if (!m.isStatic()) {
                        items.add(AutocompleteItem.fromJSMethod(m, receiverType, 1, context.methodsOnly));
                    }
                }
            }
        }
    }

    private JSTypeInfo getMergeRegistryType(Class<?> javaClass) {
        return document.getTypeResolver().findJSTypeForJavaClass(javaClass);
    }
    
    /**
     * Add autocomplete suggestions for a synthetic type's methods and fields.
     * @param syntheticType The synthetic type
     * @param items The list to add items to
     * @param forMethodReference If true, only add methods with no parentheses in insert text
     */
    private void addSyntheticTypeSuggestions(SyntheticType syntheticType, List<AutocompleteItem> items, boolean forMethodReference) {
        // Add methods
        for (SyntheticMethod method : syntheticType.getMethods()) {
            AutocompleteItem item = AutocompleteItem.fromSyntheticMethod(method, syntheticType.getTypeInfo(), forMethodReference);
            items.add(item);
        }
        
        // Add fields (skip if forMethodReference is true)
        if (!forMethodReference) {
            for (SyntheticField field : syntheticType.getFields()) {
                AutocompleteItem item = AutocompleteItem.fromSyntheticField(field);
                items.add(item);
            }
        }
    }
    
    /**
     * Recursively add methods from a type and its parents.
     * @param type The current JS type to get methods from (changes as we walk up inheritance)
     * @param contextType The original TypeInfo context for resolving type parameters (stays constant)
     */
    protected void addMethodsFromType(JSTypeInfo type, TypeInfo contextType, List<AutocompleteItem> items, Set<String> added) {
        addMethodsFromType(type, contextType, items, added, 0, false, false);
    }
    
    /**
     * Recursively add methods from a type and its parents.
     * @param type The current JS type to get methods from (changes as we walk up inheritance)
     * @param contextType The original TypeInfo context for resolving type parameters (stays constant)
     * @param forMethodReference If true, insert text will NOT include parentheses
     */
     protected void addMethodsFromType(JSTypeInfo type, TypeInfo contextType, List<AutocompleteItem> items, Set<String> added, boolean forMethodReference) {
         addMethodsFromType(type, contextType, items, added, 0, forMethodReference, false);
     }

     /**
      * Recursively add methods from a type and its parents.
      * @param type The current JS type to get methods from (changes as we walk up inheritance)
      * @param contextType The original TypeInfo context for resolving type parameters (stays constant)
      * @param forMethodReference If true, insert text will NOT include parentheses
      * @param isStaticContext If true, only add static methods
      */
     protected void addMethodsFromType(JSTypeInfo type, TypeInfo contextType, List<AutocompleteItem> items, Set<String> added, boolean forMethodReference, boolean isStaticContext) {
         addMethodsFromType(type, contextType, items, added, 0, forMethodReference, isStaticContext);
     }
    
    /**
     * Recursively add methods from a type and its parents with inheritance depth tracking.
     * Shows all overloads - one autocomplete item per overload.
     * @param type The current JS type in inheritance chain
     * @param contextType The original TypeInfo context for resolving type parameters (e.g., IPlayer with T → EntityPlayerMP)
     */
    private void addMethodsFromType(JSTypeInfo type, TypeInfo contextType, List<AutocompleteItem> items, Set<String> added, int depth) {
        addMethodsFromType(type, contextType, items, added, depth, false, false);
    }
    
    /**
     * Recursively add methods from a type and its parents with inheritance depth tracking.
     * Shows all overloads - one autocomplete item per overload.
     * @param type The current JS type in inheritance chain
     * @param contextType The original TypeInfo context for resolving type parameters (e.g., IPlayer with T → EntityPlayerMP)
     * @param forMethodReference If true, insert text will NOT include parentheses
     */
    private void addMethodsFromType(JSTypeInfo type, TypeInfo contextType, List<AutocompleteItem> items, Set<String> added, int depth, boolean forMethodReference) {
        addMethodsFromType(type, contextType, items, added, depth, forMethodReference, false);
    }

    private void addMethodsFromType(JSTypeInfo type, TypeInfo contextType, List<AutocompleteItem> items, Set<String> added, int depth, boolean forMethodReference, boolean isStaticContext) {
        // Collect base method names (without $N suffix) that we haven't processed yet
        Set<String> baseNames = new HashSet<>();
        for (String key : type.getMethods().keySet()) {
            String baseName = key.contains("$") ? key.substring(0, key.indexOf('$')) : key;
            if (!added.contains(baseName)) {
                baseNames.add(baseName);
            }
        }
        
        // For each base method name, add all overloads
        for (String baseName : baseNames) {
            added.add(baseName);
            // Use getMethodOverloads to get all overloads for this method (excluding inherited)
            List<JSMethodInfo> overloads = new ArrayList<>();
            if (type.getMethods().containsKey(baseName)) {
                overloads.add(type.getMethods().get(baseName));
            }
            int index = 1;
            while (type.getMethods().containsKey(baseName + "$" + index)) {
                overloads.add(type.getMethods().get(baseName + "$" + index));
                index++;
            }
            // Add one autocomplete item per overload
            for (JSMethodInfo method : overloads) {
                if (isStaticContext && !method.isStatic()) continue;
                items.add(AutocompleteItem.fromJSMethod(method, contextType, depth, forMethodReference));
            }
        }
        
        // Add from parent type with incremented depth - keep same contextType
        if (type.getResolvedParent() != null) {
            addMethodsFromType(type.getResolvedParent(), contextType, items, added, depth + 1, forMethodReference, isStaticContext);
        }
    }
    
    /**
     * Recursively add fields from a type and its parents.
     * @param type The current JS type to get fields from (changes as we walk up inheritance)
     * @param contextType The original TypeInfo context for resolving type parameters (stays constant)
     */
    protected void addFieldsFromType(JSTypeInfo type, TypeInfo contextType, List<AutocompleteItem> items, Set<String> added) {
        addFieldsFromType(type, contextType, items, added, 0, false);
    }

    protected void addFieldsFromType(JSTypeInfo type, TypeInfo contextType, List<AutocompleteItem> items, Set<String> added, boolean isStaticContext) {
        addFieldsFromType(type, contextType, items, added, 0, isStaticContext);
    }
    
    /**
     * Recursively add fields from a type and its parents with inheritance depth tracking.
     * @param type The current JS type in inheritance chain
     * @param contextType The original TypeInfo context for resolving type parameters
     */
    private void addFieldsFromType(JSTypeInfo type, TypeInfo contextType, List<AutocompleteItem> items, Set<String> added, int depth) {
        addFieldsFromType(type, contextType, items, added, depth, false);
    }

    private void addFieldsFromType(JSTypeInfo type, TypeInfo contextType, List<AutocompleteItem> items, Set<String> added, int depth, boolean isStaticContext) {
        for (JSFieldInfo field : type.getFields().values()) {
            if (!added.contains(field.getName())) {
                if (isStaticContext && !field.isStatic()) continue;
                added.add(field.getName());
                // Pass contextType (original receiver) for type parameter resolution, not current type
                items.add(AutocompleteItem.fromJSField(field, contextType, depth));
            }
        }
        
        // Add from parent type with incremented depth - keep same contextType
        if (type.getResolvedParent() != null) {
            addFieldsFromType(type.getResolvedParent(), contextType, items, added, depth + 1, isStaticContext);
        }
    }

    protected void addUnimportedClassSuggestions(String prefix, List<AutocompleteItem> items) {
        // For JavaScript, we typically don't suggest unimported classes
        // as imports are handled differently. This can be customized
        // if needed to suggest global JS types.
    }

    @Override
    protected void addLanguageUniqueSuggestions(Context context, List<AutocompleteItem> items) {
        super.addLanguageUniqueSuggestions(context, items);

        // Add global variables from both global engine objects and document editor/DataScript globals
        List<String> globalNames = new ArrayList<>(); 
        globalNames.addAll(registry.getGlobalEngineObjects().keySet());
        globalNames.addAll(document.getEditorGlobals().keySet());

        for (String name : globalNames) {
            FieldInfo fieldInfo = document.resolveVariable(name, context.cursorPosition);
            if (fieldInfo == null || !fieldInfo.isResolved())
                continue;

            items.add(AutocompleteItem.fromField(fieldInfo));
        }

        // Add global imports in JSTypeRegistry.globalEngineImports
        for (String importName : registry.getGlobalEngineImports().keySet()) {
            JSTypeInfo jsTypeInfo = registry.getGlobalImportType(importName);
            if (jsTypeInfo != null) {
                items.add(AutocompleteItem.fromType(TypeInfo.fromJSTypeInfo(jsTypeInfo)));
            }
        }

        // Add global engine functions (parseInt, parseFloat, isNaN, isFinite, etc.)
        for (JSMethodInfo fn : registry.getGlobalEngineFunctions().values()) {
            items.add(AutocompleteItem.fromJSMethod(fn, 0));
        }
    }

    protected UsageTracker getUsageTracker() {
        return UsageTracker.getJSInstance();
    }

    public String[] getKeywords() {
        return TypeChecker.getJavaScriptKeywords();
    }
}
