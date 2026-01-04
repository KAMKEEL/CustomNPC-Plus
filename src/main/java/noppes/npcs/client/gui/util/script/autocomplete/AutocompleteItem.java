package noppes.npcs.client.gui.util.script.autocomplete;

import noppes.npcs.client.gui.util.script.interpreter.field.FieldInfo;
import noppes.npcs.client.gui.util.script.interpreter.js_parser.JSFieldInfo;
import noppes.npcs.client.gui.util.script.interpreter.js_parser.JSMethodInfo;
import noppes.npcs.client.gui.util.script.interpreter.method.MethodInfo;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;

/**
 * Represents a single autocomplete suggestion.
 * Unified representation for both Java and JavaScript completions.
 */
public class AutocompleteItem implements Comparable<AutocompleteItem> {
    
    /**
     * The kind of completion item.
     */
    public enum Kind {
        ENUM_CONSTANT(-1), // Enum constant value (highest priority for enums)
        METHOD(0),      // Method or function
        FIELD(1),       // Field or property
        VARIABLE(2),    // Local variable or parameter
        CLASS(3),       // Class or interface type
        ENUM(4),        // Enum type
        KEYWORD(6),     // Language keyword
        SNIPPET(7);     // Code snippet
        
        private final int priority;
        
        Kind(int priority) {
            this.priority = priority;
        }
        
        public int getPriority() {
            return priority;
        }
    }
    
    private final String name;              // Display name (e.g., "getPlayer" or "getPlayer(UUID id)" for methods)
    private final String searchName;        // Name to search against (just the identifier, e.g., "getPlayer")
    private final String insertText;        // Text to insert (e.g., "getPlayer()")
    private final Kind kind;                // Type of completion
    private final String typeLabel;         // Type label (e.g., "IPlayer", "void")
    private final String signature;         // Full signature for methods
    private final String documentation;     // Documentation/description
    private final Object sourceData;        // Original source (MethodInfo, FieldInfo, etc.)
    private final boolean deprecated;       // Whether this item is deprecated
    
    // Import tracking
    private final boolean requiresImport;   // Whether selecting this item requires adding an import
    private final String importPath;        // Full path for import (e.g., "net.minecraft.client.Minecraft")
    
    // Match scoring
    private int matchScore = 0;             // How well this matches the query
    private int[] matchIndices;             // Indices of matched characters for highlighting
    
    private AutocompleteItem(String name, String searchName, String insertText, Kind kind, String typeLabel,
                             String signature, String documentation, Object sourceData, boolean deprecated,
                             boolean requiresImport, String importPath) {
        this.name = name;
        this.searchName = searchName != null ? searchName : name;  // Default to name if not provided
        this.insertText = insertText;
        this.kind = kind;
        this.typeLabel = typeLabel;
        this.signature = signature;
        this.documentation = documentation;
        this.sourceData = sourceData;
        this.deprecated = deprecated;
        this.requiresImport = requiresImport;
        this.importPath = importPath;
    }
    
    // ==================== FACTORY METHODS ====================
    
    /**
     * Create from a Java MethodInfo.
     */
    public static AutocompleteItem fromMethod(MethodInfo method) {
        String name = method.getName();
        StringBuilder insertText = new StringBuilder(name);
        insertText.append("(");
        
        // Add placeholders for parameters if any
        if (method.getParameterCount() > 0) {
            // Just add () - user will fill in params
        }
        insertText.append(")");
        
        String returnType = method.getReturnType() != null ? 
            method.getReturnType().getSimpleName() : "void";
        
        String signature = buildMethodSignature(method);
        
        // Build display name with parameters like "read(byte[] b)"
        StringBuilder displayName = new StringBuilder(name);
        displayName.append("(");
        for (int i = 0; i < method.getParameterCount(); i++) {
            if (i > 0) displayName.append(", ");
            FieldInfo param = method.getParameters().get(i);
            String paramType = param.getTypeInfo() != null ? 
                param.getTypeInfo().getSimpleName() : "?";
            displayName.append(paramType);
            if (param.getName() != null && !param.getName().isEmpty()) {
                displayName.append(" ").append(param.getName());
            }
        }
        displayName.append(")");
        
        return new AutocompleteItem(
            displayName.toString(),
            name,  // Search against just the method name, not the params
            insertText.toString(),
            Kind.METHOD,
            returnType,
            signature,
            method.getDocumentation(),
            method,
            false, // TODO: Check for @Deprecated annotation
            false, // Methods don't require imports
            null
        );
    }
    
    /**
     * Create from a Java FieldInfo.
     */
    public static AutocompleteItem fromField(FieldInfo field) {
        String typeLabel = field.getTypeInfo() != null ? 
            field.getTypeInfo().getSimpleName() : "?";
        
        Kind kind;
        switch (field.getScope()) {
            case PARAMETER:
                kind = Kind.VARIABLE;
                break;
            case LOCAL:
                kind = Kind.VARIABLE;
                break;
            case ENUM_CONSTANT:
                kind = Kind.ENUM_CONSTANT;
                break;
            default:
                kind = Kind.FIELD;
        }
        
        return new AutocompleteItem(
            field.getName(),
            field.getName(),  // searchName same as display name for fields
            field.getName(),
            kind,
            typeLabel,
            typeLabel + " " + field.getName(),
            null,
            field,
            false,
            false, // Fields don't require imports
            null
        );
    }
    
    /**
     * Create from a Java TypeInfo.
     */
    public static AutocompleteItem fromType(TypeInfo type) {
        Kind kind;
        switch (type.getKind()) {
            case INTERFACE:
                kind = Kind.CLASS;
                break;
            case ENUM:
                kind = Kind.ENUM;
                break;
            default:
                kind = Kind.CLASS;
        }
        
        return new AutocompleteItem(
            type.getSimpleName(),
            type.getSimpleName(),  // searchName same as display name for types
            type.getSimpleName(),
            kind,
            type.getPackageName(),
            type.getFullName(),
            null,
            type,
            false,
            false, // Will be overridden for unimported types
            null
        );
    }
    
    /**
     * Create from a JavaScript JSMethodInfo.
     */
    public static AutocompleteItem fromJSMethod(JSMethodInfo method) {
        String name = method.getName();
        StringBuilder insertText = new StringBuilder(name);
        insertText.append("(");
        insertText.append(")");
        
        // Use signature for display name if available, otherwise just name
        String displayName = method.getSignature() != null && method.getSignature().contains("(") ?
            method.getSignature().substring(method.getSignature().indexOf(name)) : name + "()";
        
        return new AutocompleteItem(
            displayName,
            name,  // Search against just the method name
            insertText.toString(),
            Kind.METHOD,
            method.getReturnType(),
            method.getSignature(),
            method.getDocumentation(),
            method,
            false,
            false,
            null
        );
    }
    
    /**
     * Create from a JavaScript JSFieldInfo.
     */
    public static AutocompleteItem fromJSField(JSFieldInfo field) {
        return new AutocompleteItem(
            field.getName(),
            field.getName(),  // searchName same as display name
            field.getName(),
            Kind.FIELD,
            field.getType(),
            field.toString(),
            field.getDocumentation(),
            field,
            false,
            false,
            null
        );
    }
    
    /**
     * Create a keyword item.
     */
    public static AutocompleteItem keyword(String keyword) {
        return new AutocompleteItem(
            keyword,
            keyword,  // searchName same as display name
            keyword,
            Kind.KEYWORD,
            "keyword",
            null,
            null,
            null,
            false,
            false,
            null
        );
    }
    
    // ==================== HELPER METHODS ====================
    
    private static String buildMethodSignature(MethodInfo method) {
        StringBuilder sb = new StringBuilder();
        String returnType = method.getReturnType() != null ? 
            method.getReturnType().getSimpleName() : "void";
        sb.append(returnType).append(" ").append(method.getName()).append("(");
        
        for (int i = 0; i < method.getParameterCount(); i++) {
            if (i > 0) sb.append(", ");
            FieldInfo param = method.getParameters().get(i);
            String paramType = param.getTypeInfo() != null ? 
                param.getTypeInfo().getSimpleName() : "?";
            sb.append(paramType).append(" ").append(param.getName());
        }
        
        sb.append(")");
        return sb.toString();
    }
    
    // ==================== MATCHING ====================
    
    /**
     * Calculate fuzzy match score against a query string.
     * Returns -1 if no match, or a positive score (higher = better match).
     * 
     * @param query The search query
     * @param requirePrefix If true, only match items that start with the query (no fuzzy/contains matching)
     */
    public int calculateMatchScore(String query, boolean requirePrefix) {
        if (query == null || query.isEmpty()) {
            matchScore = 100; // Everything matches empty query
            matchIndices = new int[0];
            return matchScore;
        }
        
        String lowerName = searchName.toLowerCase();
        String lowerQuery = query.toLowerCase();
        
        // Exact prefix match is best
        if (lowerName.startsWith(lowerQuery)) {
            matchScore = 1000 - query.length(); // Shorter prefixes score higher
            matchIndices = new int[query.length()];
            for (int i = 0; i < query.length(); i++) {
                matchIndices[i] = i;
            }
            return matchScore;
        }
        
        // If requirePrefix is true, stop here - no fuzzy/substring matching
        if (requirePrefix) {
            matchScore = -1;
            matchIndices = new int[0];
            return matchScore;
        }
        
        // Exact substring match
        int subIndex = lowerName.indexOf(lowerQuery);
        if (subIndex >= 0) {
            matchScore = 500 - subIndex; // Earlier substrings score higher
            matchIndices = new int[query.length()];
            for (int i = 0; i < query.length(); i++) {
                matchIndices[i] = subIndex + i;
            }
            return matchScore;
        }
        
        // Fuzzy match - characters must appear in order
        int[] indices = new int[query.length()];
        int nameIdx = 0;
        int queryIdx = 0;
        int gaps = 0;
        int consecutiveBonus = 0;
        int lastMatchIdx = -2;
        
        while (queryIdx < query.length() && nameIdx < searchName.length()) {
            if (Character.toLowerCase(searchName.charAt(nameIdx)) == lowerQuery.charAt(queryIdx)) {
                indices[queryIdx] = nameIdx;
                
                // Bonus for consecutive matches
                if (nameIdx == lastMatchIdx + 1) {
                    consecutiveBonus += 10;
                }
                
                // Bonus for matching at word boundaries (camelCase)
                if (nameIdx == 0 || !Character.isLetterOrDigit(searchName.charAt(nameIdx - 1)) ||
                    (Character.isUpperCase(searchName.charAt(nameIdx)) && nameIdx > 0 && 
                     Character.isLowerCase(searchName.charAt(nameIdx - 1)))) {
                    consecutiveBonus += 20;
                }
                
                lastMatchIdx = nameIdx;
                queryIdx++;
            } else {
                gaps++;
            }
            nameIdx++;
        }
        
        if (queryIdx < query.length()) {
            // Didn't match all characters
            matchScore = -1;
            matchIndices = null;
            return -1;
        }
        
        matchScore = 100 + consecutiveBonus - gaps;
        matchIndices = indices;
        return matchScore;
    }
    
    /**
     * Backward compatibility overload - defaults to fuzzy matching (no prefix requirement).
     */
    public int calculateMatchScore(String query) {
        return calculateMatchScore(query, false);
    }
    
    // ==================== GETTERS ====================
    
    public String getName() { return name; }
    public String getInsertText() { return insertText; }
    public Kind getKind() { return kind; }
    public String getTypeLabel() { return typeLabel; }
    public String getSignature() { return signature; }
    public String getDocumentation() { return documentation; }
    public Object getSourceData() { return sourceData; }
    public boolean isDeprecated() { return deprecated; }
    public boolean requiresImport() { return requiresImport; }
    public String getImportPath() { return importPath; }
    public int getMatchScore() { return matchScore; }
    public int[] getMatchIndices() { return matchIndices; }
    
    /**
     * Get icon identifier based on kind.
     */
    public String getIconId() {
        switch (kind) {
            case METHOD: return "m";
            case FIELD: return "f";
            case VARIABLE: return "v";
            case CLASS: return "C";
            case ENUM: return "E";
            case ENUM_CONSTANT: return "e";
            case KEYWORD: return "k";
            case SNIPPET: return "s";
            default: return "?";
        }
    }
    
    /**
     * Get color for the kind icon.
     */
    public int getIconColor() {
        switch (kind) {
            case METHOD: return 0xFFB877DB;     // Purple for methods
            case FIELD: return 0xFF79C0FF;      // Blue for fields
            case VARIABLE: return 0xFF7EE787;   // Green for variables
            case CLASS: return 0xFFFFA657;      // Orange for classes
            case ENUM: return 0xFFFFD866;       // Yellow for enums
            case ENUM_CONSTANT: return 0xFFFFD866;
            case KEYWORD: return 0xFFFF7B72;    // Red for keywords
            case SNIPPET: return 0xFFCCCCCC;    // Gray for snippets
            default: return 0xFFCCCCCC;
        }
    }
    
    @Override
    public int compareTo(AutocompleteItem other) {
        // First by match score (descending)
        if (this.matchScore != other.matchScore) {
            return other.matchScore - this.matchScore;
        }
        // Then by kind priority
        if (this.kind.getPriority() != other.kind.getPriority()) {
            return this.kind.getPriority() - other.kind.getPriority();
        }
        // Finally alphabetically
        return this.name.compareToIgnoreCase(other.name);
    }
    
    @Override
    public String toString() {
        return name + " (" + kind + ")";
    }
    
    // ==================== BUILDER ====================
    
    /**
     * Builder for creating AutocompleteItem instances without source data.
     */
    public static class Builder {
        private String name;
        private String searchName;
        private String insertText;
        private Kind kind = Kind.FIELD;
        private String typeLabel = "";
        private String signature;
        private String documentation;
        private Object sourceData;
        private boolean deprecated = false;
        private boolean requiresImport = false;
        private String importPath = null;
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder searchName(String searchName) {
            this.searchName = searchName;
            return this;
        }
        
        public Builder insertText(String insertText) {
            this.insertText = insertText;
            return this;
        }
        
        public Builder kind(Kind kind) {
            this.kind = kind;
            return this;
        }
        
        public Builder typeLabel(String typeLabel) {
            this.typeLabel = typeLabel;
            return this;
        }
        
        public Builder signature(String signature) {
            this.signature = signature;
            return this;
        }
        
        public Builder documentation(String documentation) {
            this.documentation = documentation;
            return this;
        }
        
        public Builder sourceData(Object sourceData) {
            this.sourceData = sourceData;
            return this;
        }
        
        public Builder deprecated(boolean deprecated) {
            this.deprecated = deprecated;
            return this;
        }
        
        public Builder requiresImport(boolean requiresImport) {
            this.requiresImport = requiresImport;
            return this;
        }
        
        public Builder importPath(String importPath) {
            this.importPath = importPath;
            return this;
        }
        
        public AutocompleteItem build() {
            if (insertText == null) {
                insertText = name;
            }
            return new AutocompleteItem(name, searchName, insertText, kind, typeLabel, 
                signature, documentation, sourceData, deprecated, requiresImport, importPath);
        }
    }
}
