package noppes.npcs.client.gui.util.script.autocomplete;

import noppes.npcs.client.gui.util.script.interpreter.ScriptDocument;
import noppes.npcs.client.gui.util.script.interpreter.field.FieldInfo;
import noppes.npcs.client.gui.util.script.interpreter.method.MethodInfo;
import noppes.npcs.client.gui.util.script.interpreter.type.ScriptTypeInfo;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeResolver;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Autocomplete provider for Java/Groovy scripts.
 * Uses ScriptDocument for type resolution and scope analysis.
 */
public class JavaAutocompleteProvider implements AutocompleteProvider {
    
    private ScriptDocument document;
    
    public void setDocument(ScriptDocument document) {
        this.document = document;
    }
    
    @Override
    public boolean canProvide(Context context) {
        return document != null && !document.isJavaScript();
    }
    
    @Override
    public List<AutocompleteItem> getSuggestions(Context context) {
        List<AutocompleteItem> items = new ArrayList<>();
        
        if (document == null) {
            return items;
        }
        
        if (context.isMemberAccess) {
            // Member access: resolve type of receiver and get its members
            addMemberSuggestions(context, items);
        } else {
            // Identifier context: show variables, methods, types in scope
            addScopeSuggestions(context, items);
        }
        
        // Filter and score by prefix
        filterAndScore(items, context.prefix);
        
        // Sort by score
        Collections.sort(items);
        
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
        
        // Resolve the type of the receiver expression
        TypeInfo receiverType = document.resolveExpressionType(receiverExpr, context.prefixStart);
        
        if (receiverType == null || !receiverType.isResolved()) {
            return;
        }
        
        // Determine if this is a static context (accessing a class type)
        boolean isStaticContext = isStaticAccess(receiverExpr, context.prefixStart);
        
        Class<?> clazz = receiverType.getJavaClass();
        if (clazz == null) {
            // Try ScriptTypeInfo
            if (receiverType instanceof ScriptTypeInfo) {
                addScriptTypeMembers((ScriptTypeInfo) receiverType, items, isStaticContext);
            }
            return;
        }
        
        // Add methods
        Set<String> addedMethods = new HashSet<>();
        for (Method method : clazz.getMethods()) {
            if (Modifier.isPublic(method.getModifiers())) {
                // Filter by static context
                if (isStaticContext && !Modifier.isStatic(method.getModifiers())) {
                    continue;
                }
                
                String sig = method.getName() + "(" + method.getParameterCount() + ")";
                if (!addedMethods.contains(sig)) {
                    addedMethods.add(sig);
                    MethodInfo methodInfo = MethodInfo.fromReflection(method, receiverType);
                    items.add(AutocompleteItem.fromMethod(methodInfo));
                }
            }
        }
        
        // Add fields
        for (Field field : clazz.getFields()) {
            if (Modifier.isPublic(field.getModifiers())) {
                // Filter by static context
                if (isStaticContext && !Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                
                FieldInfo fieldInfo = FieldInfo.fromReflection(field, receiverType);
                items.add(AutocompleteItem.fromField(fieldInfo));
            }
        }
        
    }
    
    /**
     * Add members from a script-defined type.
     */
    private void addScriptTypeMembers(ScriptTypeInfo scriptType, List<AutocompleteItem> items, boolean isStaticContext) {
        // Add methods (getMethods returns Map<String, List<MethodInfo>>)
        for (List<MethodInfo> overloads : scriptType.getMethods().values()) {
            for (MethodInfo method : overloads) {
                // Filter by static context
                if (isStaticContext && !method.isStatic()) {
                    continue;
                }
                items.add(AutocompleteItem.fromMethod(method));
            }
        }
        
        // Add fields (getFields returns Map<String, FieldInfo>)
        for (FieldInfo field : scriptType.getFields().values()) {
            // Filter by static context
            if (isStaticContext && !field.isStatic()) {
                continue;
            }
            items.add(AutocompleteItem.fromField(field));
        }
        
        // Add parent class members
        if (scriptType.hasSuperClass()) {
            TypeInfo superType = scriptType.getSuperClass();
            if (superType != null && superType.getJavaClass() != null) {
                Class<?> superClazz = superType.getJavaClass();
                Set<String> addedMethods = new HashSet<>();
                for (Method method : superClazz.getMethods()) {
                    if (Modifier.isPublic(method.getModifiers())) {
                        String sig = method.getName() + "(" + method.getParameterCount() + ")";
                        if (!addedMethods.contains(sig)) {
                            addedMethods.add(sig);
                            MethodInfo methodInfo = MethodInfo.fromReflection(method, superType);
                            items.add(AutocompleteItem.fromMethod(methodInfo));
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Add suggestions based on current scope (not after a dot).
     */
    private void addScopeSuggestions(Context context, List<AutocompleteItem> items) {
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
        
        // Add enclosing type fields (find via script types map)
        ScriptTypeInfo enclosingType = findEnclosingType(pos);
        if (enclosingType != null) {
            for (FieldInfo field : enclosingType.getFields().values()) {
                if (field.isVisibleAt(pos)) {
                    items.add(AutocompleteItem.fromField(field));
                }
            }
            
            // Add methods from enclosing type
            for (List<MethodInfo> overloads : enclosingType.getMethods().values()) {
                for (MethodInfo method : overloads) {
                    items.add(AutocompleteItem.fromMethod(method));
                }
            }
        }
        
        // Add script-defined methods
        for (MethodInfo method : document.getAllMethods()) {
            items.add(AutocompleteItem.fromMethod(method));
        }
        
        // Add imported types
        for (TypeInfo type : document.getImportedTypes()) {
            items.add(AutocompleteItem.fromType(type));
        }
        
        // Add script-defined types
        for (ScriptTypeInfo scriptType : document.getScriptTypesMap().values()) {
            items.add(AutocompleteItem.fromType(scriptType));
        }
        
        // Add unimported classes that match the prefix (for auto-import)
        if (context.prefix != null && context.prefix.length() >= 2 && Character.isUpperCase(context.prefix.charAt(0))) {
            addUnimportedClassSuggestions(context.prefix, items);
        }
        
        // Add keywords
        addKeywords(items);
    }
    
    /**
     * Add suggestions for unimported classes that match the prefix.
     * These will trigger auto-import when selected.
     */
    private void addUnimportedClassSuggestions(String prefix, List<AutocompleteItem> items) {
        // Get the type resolver
        TypeResolver resolver = TypeResolver.getInstance();
        
        // Find classes matching this prefix (not just exact matches)
        List<String> matchingClasses = resolver.findClassesByPrefix(prefix, 15);
        
        // Track what's already imported to avoid duplicates
        Set<String> importedFullNames = new HashSet<>();
        for (TypeInfo imported : document.getImportedTypes()) {
            importedFullNames.add(imported.getFullName());
        }
        
        // Also track simple names already in the list to avoid showing both
        // imported and unimported versions of the same class
        Set<String> existingSimpleNames = new HashSet<>();
        for (AutocompleteItem item : items) {
            if (item.getKind() == AutocompleteItem.Kind.CLASS || 
                item.getKind() == AutocompleteItem.Kind.ENUM) {
                existingSimpleNames.add(item.getName());
            }
        }
        
        for (String fullName : matchingClasses) {
            // Skip if already imported
            if (importedFullNames.contains(fullName)) {
                continue;
            }
            
            TypeInfo type = resolver.resolveFullName(fullName);
            if (type != null && type.isResolved()) {
                // Skip if a class with this simple name is already in the list
                if (existingSimpleNames.contains(type.getSimpleName())) {
                    continue;
                }
                
                // Create an item that requires import
                AutocompleteItem item = new AutocompleteItem.Builder()
                    .name(type.getSimpleName())
                    .insertText(type.getSimpleName())
                    .kind(type.getKind() == TypeInfo.Kind.ENUM ? 
                          AutocompleteItem.Kind.ENUM : AutocompleteItem.Kind.CLASS)
                    .typeLabel(type.getPackageName())
                    .signature(type.getFullName())
                    .sourceData(type)
                    .requiresImport(true)
                    .importPath(fullName)
                    .build();
                items.add(item);
                existingSimpleNames.add(type.getSimpleName());
            }
        }
    }
    
    /**
     * Add Java keywords.
     */
    private void addKeywords(List<AutocompleteItem> items) {
        String[] keywords = {
            "if", "else", "for", "while", "do", "switch", "case", "break", "continue",
            "return", "try", "catch", "finally", "throw", "throws", "new", "this", "super",
            "true", "false", "null", "instanceof", "import", "class", "interface", "enum",
            "extends", "implements", "public", "private", "protected", "static", "final",
            "abstract", "synchronized", "volatile", "transient", "native", "void",
            "boolean", "byte", "short", "int", "long", "float", "double", "char"
        };
        
        for (String keyword : keywords) {
            items.add(AutocompleteItem.keyword(keyword));
        }
    }
    
    /**
     * Find the enclosing script type at a position.
     * This is a workaround since findEnclosingScriptType is package-private.
     */
    private ScriptTypeInfo findEnclosingType(int position) {
        for (ScriptTypeInfo type : document.getScriptTypesMap().values()) {
            if (type.containsPosition(position)) {
                return type;
            }
        }
        return null;
    }
    
    /**
     * Check if the receiver expression represents static access (class type).
     * Similar logic to FieldChainMarker.isStaticContext().
     */
    private boolean isStaticAccess(String receiverExpr, int position) {
        // Try to resolve the receiver expression as a type
        TypeInfo typeCheck = document.resolveType(receiverExpr);
        return typeCheck != null && typeCheck.isResolved();
    }
    
    /**
     * Filter items by prefix and calculate match scores.
     */
    private void filterAndScore(List<AutocompleteItem> items, String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            // No filtering needed, all items get a base score
            for (AutocompleteItem item : items) {
                item.calculateMatchScore("");
            }
            return;
        }
        
        // Filter and score
        Iterator<AutocompleteItem> iter = items.iterator();
        while (iter.hasNext()) {
            AutocompleteItem item = iter.next();
            int score = item.calculateMatchScore(prefix);
            if (score < 0) {
                iter.remove();
            }
        }
    }
    
    /**
     * Builder class for AutocompleteItem to handle cases without source data.
     */
    private static class AutocompleteItemBuilder {
        // This would be needed if AutocompleteItem had a builder pattern
    }
}
