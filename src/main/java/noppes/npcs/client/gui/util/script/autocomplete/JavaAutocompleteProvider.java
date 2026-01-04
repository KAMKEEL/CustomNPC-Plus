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
        
        // Resolve owner type for usage tracking and static context detection
        String ownerFullName = null;
        boolean isStaticContext = false;
        if (context.isMemberAccess && context.receiverExpression != null) {
            TypeInfo receiverType = document.resolveExpressionType(
                context.receiverExpression, context.prefixStart);
            if (receiverType != null && receiverType.isResolved()) {
                ownerFullName = receiverType.getFullName();
            }
            // Check if accessing through a class (static context) vs instance
            isStaticContext = isStaticAccess(context.receiverExpression, context.prefixStart);
        }
        
        if (context.isMemberAccess) {
            // Member access: resolve type of receiver and get its members
            addMemberSuggestions(context, items);
        } else {
            // Identifier context: show variables, methods, types in scope
            addScopeSuggestions(context, items);
        }
        
        // Filter and score by prefix, then apply usage boosts and static penalties
        filterAndScore(items, context.prefix, context.isMemberAccess, isStaticContext, ownerFullName);
        
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
        List<String> matchingClasses = resolver.findClassesByPrefix(prefix, -1);
        
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
     * Filter items by prefix, calculate match scores, apply usage boosts, and penalize static members in instance contexts.
     */
    private void filterAndScore(List<AutocompleteItem> items, String prefix, 
                                 boolean isMemberAccess, boolean isStaticContext, String ownerFullName) {
        UsageTracker tracker = UsageTracker.getJavaInstance();
        
        if (prefix == null || prefix.isEmpty()) {
            // No filtering needed, all items get a base score + usage boost
            for (AutocompleteItem item : items) {
                item.calculateMatchScore("", false);
                applyUsageBoost(item, tracker, ownerFullName);
                applyStaticPenalty(item, isMemberAccess, isStaticContext);
                applyObjectMethodPenalty(item);
            }
            return;
        }
        
        // For non-member access (first word), require strict prefix matching
        // For member access (after dot), allow fuzzy/contains matching
        boolean requirePrefix = !isMemberAccess;
        
        // Filter, score, apply usage boosts, and apply penalties
        Iterator<AutocompleteItem> iter = items.iterator();
        while (iter.hasNext()) {
            AutocompleteItem item = iter.next();
            int score = item.calculateMatchScore(prefix, requirePrefix);
            if (score < 0) {
                iter.remove();
            } else {
                applyUsageBoost(item, tracker, ownerFullName);
                applyStaticPenalty(item, isMemberAccess, isStaticContext);
                applyObjectMethodPenalty(item);
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
    
    /**
     * Apply penalty to static members when accessed in a non-static (instance) context.
     * This matches IntelliJ's behavior where static members are deprioritized when
     * accessing through an instance (e.g., Minecraft.getMinecraft().getMinecraft()).
     * However, if the item is a very strong match (exact prefix), don't penalize as much.
     */
    private void applyStaticPenalty(AutocompleteItem item, boolean isMemberAccess, boolean isStaticContext) {
        // Only apply penalty in member access contexts (after dot)
        if (!isMemberAccess) {
            return;
        }
        
        // Only penalize when we're in an instance context (not static)
        if (isStaticContext) {
            return;
        }
        
        boolean isStatic = item.isStatic();
   
        // Apply penalty to static members in instance context
        if (isStatic) {
            int matchScore = item.getMatchScore();
            // Strong prefix matches (score >= 800) get a lighter penalty
            // Weaker matches get pushed down more aggressively
            if (matchScore >= 800) {
                // Light penalty for exact prefix matches - just deprioritize slightly
                item.addScoreBoost(-200);
            } else {
                // Heavy penalty for fuzzy/substring matches - push to bottom
                item.addScoreBoost(-matchScore);
            }
        }
    }
    
    /**
     * Apply penalty to inherited Object methods to push them to bottom.
     * Strong matches get lighter penalty, weak matches get pushed all the way down.
     */
    private void applyObjectMethodPenalty(AutocompleteItem item) {
        if (!item.isInheritedObjectMethod()) {
            return;
        }
        
        int matchScore = item.getMatchScore();
        // Strong prefix matches (score >= 900) get a moderate penalty
        // Everything else gets pushed to the very bottom
        if (matchScore >= 900) {
            // Strong match - moderate penalty to keep it visible but below normal methods
            item.addScoreBoost(-500);
        } else {
            // Weak match - heavy penalty to push to bottom
            item.addScoreBoost(-10000);
        }
    }
    
    /**
     * Builder class for AutocompleteItem to handle cases without source data.
     */
    private static class AutocompleteItemBuilder {
        // This would be needed if AutocompleteItem had a builder pattern
    }
}
