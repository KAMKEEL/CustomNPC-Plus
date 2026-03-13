package noppes.npcs.client.gui.util.script.autocomplete;

import noppes.npcs.client.gui.util.script.autocomplete.weighter.ScoringContext;
import noppes.npcs.client.gui.util.script.autocomplete.weighter.WeigherChain;
import noppes.npcs.client.gui.util.script.interpreter.ScriptDocument;
import noppes.npcs.client.gui.util.script.interpreter.field.EnumConstantInfo;
import noppes.npcs.client.gui.util.script.interpreter.field.FieldInfo;
import noppes.npcs.client.gui.util.script.interpreter.method.MethodInfo;
import noppes.npcs.client.gui.util.script.interpreter.token.TokenType;
import noppes.npcs.client.gui.util.script.interpreter.type.ScriptTypeInfo;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeChecker;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeResolver;

import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Autocomplete provider for Java/Groovy scripts.
 * Uses ScriptDocument for type resolution and scope analysis.
 */
public class JavaAutocompleteProvider implements AutocompleteProvider {
    
    protected ScriptDocument document;
    
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
            int resolvePos = getMemberAccessResolvePosition(context);
            TypeInfo receiverType = document.resolveExpressionType(
                context.receiverExpression, resolvePos);
            if (receiverType != null && receiverType.isResolved()) {
                ownerFullName = receiverType.getFullName();
            }
            // Check if accessing through a class (static context) vs instance
            isStaticContext = isStaticAccess(context.receiverExpression, resolvePos);
        }
        
        if (context.isMemberAccess) {
            // Member access: resolve type of receiver and get its members
            addMemberSuggestions(context, items);
        } else {
            // Identifier context: show variables, methods, types in scope
            addScopeSuggestions(context, items);
        }
        
        // For method reference context, filter to show only methods (and constructor "new")
        if (context.methodsOnly) {
            items.removeIf(item -> item.getKind() != AutocompleteItem.Kind.METHOD);
            
            // Add "new" constructor reference option if the receiver is a class
            if (context.receiverExpression != null) {
                int resolvePos = getMemberAccessResolvePosition(context);
                TypeInfo receiverType = document.resolveExpressionType(
                    context.receiverExpression, resolvePos);
                if (receiverType != null && receiverType.isResolved() && isStaticContext) {
                    items.add(new AutocompleteItem.Builder()
                        .name("new")
                        .insertText("new")
                        .kind(AutocompleteItem.Kind.KEYWORD)
                        .typeLabel("constructor")
                        .signature(receiverType.getSimpleName() + "::new")
                        .build());
                }
            }
        }
        
        // Filter by prefix, then sort using weigher chain
        filterAndScore(items, context.prefix, context.isMemberAccess, isStaticContext, ownerFullName);
        
        return items;
    }
    
    /**
     * Add suggestions for member access (after dot).
     */
    protected void addMemberSuggestions(Context context, List<AutocompleteItem> items) {
        String receiverExpr = context.receiverExpression;
        if (receiverExpr == null || receiverExpr.isEmpty()) {
            return;
        }

        // Resolve position once — used for both type resolution and static context detection
        int resolvePos = getMemberAccessResolvePosition(context);
        
        TypeInfo receiverType = document.resolveExpressionType(receiverExpr, resolvePos);
        
        if (receiverType == null || !receiverType.isResolved()) {
            return;
        }
        
        boolean isStaticContext = isStaticAccess(receiverExpr, resolvePos);
        
        // Shared deduplication sets across all member sources
        Set<String> addedMethods = new HashSet<>();
        Set<String> addedFields = new HashSet<>();

        // Unified method enumeration — works for Java types, ScriptTypeInfo, and intersection bounds
        for (MethodInfo method : receiverType.getAllMethods()) {
            if (isStaticContext && !Modifier.isStatic(method.getModifiers())) continue;
            String sig = method.getName() + "(" + method.getParameterCount() + ")";
            if (addedMethods.add(sig))
                items.add(AutocompleteItem.fromMethod(method, context.methodsOnly));
        }
        
        if (!context.methodsOnly) {
            // Unified field enumeration with deduplication
            for (FieldInfo field : receiverType.getAllFields()) {
                if (isStaticContext && !Modifier.isStatic(field.getModifiers())) continue;
                if (addedFields.add(field.getName()))
                    items.add(AutocompleteItem.fromField(field));
            }
        }
        
        // Nested types (inner classes) in static context via getAllNestedTypes()
        if (isStaticContext && !context.methodsOnly) {
            for (TypeInfo nestedType : receiverType.getAllNestedTypes()) {
                items.add(AutocompleteItem.fromType(nestedType));
            }
        }

        // Enum constants — polymorphic via TypeInfo.getEnumConstants()
        if (!context.methodsOnly) {
            for (EnumConstantInfo enumConstant : receiverType.getEnumConstants().values()) {
                items.add(AutocompleteItem.fromField(enumConstant.getFieldInfo()));
            }
        }
    }

    /**
     * Pick a stable position for resolving member-access receiver types.
     *
     * Using `prefixStart` can land on end-exclusive scope boundaries (position == bodyEnd)
     * depending on caller cursor semantics and surrounding syntax, causing scope lookups
     * to fail. For member access we prefer the dot position (or nearest non-whitespace)
     * immediately before prefixStart.
     */
    protected int getMemberAccessResolvePosition(Context context) {
        if (context == null || context.text == null || context.text.isEmpty()) {
            return 0;
        }

        int pos = Math.max(0, Math.min(context.prefixStart, context.text.length()));
        int i = Math.min(pos - 1, context.text.length() - 1);

        // Skip whitespace backwards
        while (i >= 0 && Character.isWhitespace(context.text.charAt(i))) {
            i--;
        }

        // Prefer the dot index if present
        if (i >= 0 && context.text.charAt(i) == '.') {
            return i;
        }

        // Fallback: use the provided prefixStart (caret context)
        return Math.max(0, Math.min(context.prefixStart, context.text.length()));
    }
    
    /**
     * Add suggestions based on current scope (not after a dot).
     */
    protected void addScopeSuggestions(Context context, List<AutocompleteItem> items) {
        int pos = context.cursorPosition;

        // Find containing method
        MethodInfo containingMethod = document.findContainingMethod(pos);

        // Add local variables
        if (containingMethod != null) {
            List<FieldInfo> available = document.getAvailableVariablesAt(pos);
            Set<String> seenNames = new HashSet<>();
            for (FieldInfo field : available) {
                if (!(field.isLocal() || field.isParameter())) {
                    continue;
                }
                if (seenNames.add(field.getName())) {
                    items.add(AutocompleteItem.fromField(field));
                }
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
                    items.add(AutocompleteItem.fromMethod(method, context.methodsOnly));
                }
            }

            // Add inner classes of the enclosing type as type suggestions
            for (ScriptTypeInfo inner : enclosingType.getInnerClasses()) {
                items.add(AutocompleteItem.fromType(inner));
            }

            // Walk up the outerClass chain to include outer class members and sibling types.
            // Inside a nested class, users can access outer class fields/methods/sibling types.
            ScriptTypeInfo outer = enclosingType.getOuterClass();
            while (outer != null) {
                for (ScriptTypeInfo sibling : outer.getInnerClasses()) {
                    if (sibling != enclosingType) {
                        items.add(AutocompleteItem.fromType(sibling));
                    }
                }
                for (FieldInfo field : outer.getFields().values()) {
                    items.add(AutocompleteItem.fromField(field));
                }
                for (List<MethodInfo> overloads : outer.getMethods().values()) {
                    for (MethodInfo method : overloads) {
                        items.add(AutocompleteItem.fromMethod(method, context.methodsOnly));
                    }
                }
                outer = outer.getOuterClass();
            }
        }
        
        // Add script-defined methods
        for (MethodInfo method : document.getAllMethods()) {
            items.add(AutocompleteItem.fromMethod(method, context.methodsOnly));
        }

        addLanguageUniqueSuggestions(context, items);
        
        // Add keywords
        addKeywords(items);
    }

    protected void addLanguageUniqueSuggestions(Context context, List<AutocompleteItem> items) {
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
    }
    
    
    /**
     * Add suggestions for unimported classes that match the prefix.
     * These will trigger auto-import when selected.
     */
    protected void addUnimportedClassSuggestions(String prefix, List<AutocompleteItem> items) {
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
                AutocompleteItem item = new AutocompleteItem.Builder()
                    .name(type.getSimpleName())
                    .insertText(type.getSimpleName())
                    .kind(type.getKind() == TypeInfo.Kind.ENUM ? 
                          AutocompleteItem.Kind.ENUM : AutocompleteItem.Kind.CLASS)
                    .typeLabel(type.getPackageName())
                    .typeInfo(type)
                    .signature(type.getFullName())
                    .sourceData(type)
                    .requiresImport(true)
                    .importPath(fullName)
                    .color(TokenType.UNUSED_IMPORT.getHexColor())
                    .build();
                items.add(item);
                existingSimpleNames.add(type.getSimpleName());
            }
        }
    }
    
    /**
     * Add Java keywords.
     */
    protected void addKeywords(List<AutocompleteItem> items) {
        for (String keyword : getKeywords()) {
            items.add(AutocompleteItem.keyword(keyword));
        }
    }

    public String[] getKeywords() {
        return TypeChecker.getJavaKeywords();
    }
    
    /**
     * Find the enclosing script type at a position.
     * Delegates to ScriptDocument's recursive descent implementation
     * which correctly returns the innermost enclosing type for nested classes.
     */
    protected ScriptTypeInfo findEnclosingType(int position) {
        return document.findEnclosingScriptType(position);
    }
    
    /**
     * Check if the receiver expression represents static access (class type).
     * Returns true if the expression is a TYPE NAME (not a variable).
     */
    protected boolean isStaticAccess(String receiverExpr, int position) {
        // Use unified static access checker
        return TypeResolver.isStaticAccessExpression(receiverExpr, position, document);
    }
    
    protected  UsageTracker getUsageTracker() {
        return UsageTracker.getJavaInstance();
    }
    
    protected WeigherChain getWeigherChain() {
        return CompletionWeigherChains.javaChain();
    }

    protected void filterAndScore(List<AutocompleteItem> items, String prefix,
                                 boolean isMemberAccess, boolean isStaticContext, String ownerFullName) {
        boolean requirePrefix = !isMemberAccess;

        Iterator<AutocompleteItem> iter = items.iterator();
        while (iter.hasNext()) {
            AutocompleteItem item = iter.next();
            int score = item.calculateMatchScore(
                    prefix != null ? prefix : "", requirePrefix);
            if (score < 0) {
                iter.remove();
            }
        }

        ScoringContext context = new ScoringContext(
            prefix, isMemberAccess, isStaticContext,
            ownerFullName, getUsageTracker(), requirePrefix
        );

        WeigherChain weigherChain = getWeigherChain();
        Comparator<AutocompleteItem> comparator = weigherChain.buildComparator(context);
        items.sort(comparator);
    }
}
