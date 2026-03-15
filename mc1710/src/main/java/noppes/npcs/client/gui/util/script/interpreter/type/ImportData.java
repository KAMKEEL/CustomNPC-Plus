package noppes.npcs.client.gui.util.script.interpreter.type;

import noppes.npcs.client.gui.util.script.interpreter.token.Token;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a single import statement with all its metadata.
 * Tracks the full import path, resolution status, source positions, and references.
 */
public final class ImportData {

    private final String fullPath;          // e.g., "java.util.List" or "kamkeel.npcdbc.api.*"
    private final String simpleName;        // e.g., "List" or null for wildcard
    private final boolean isWildcard;       // true if ends with .*
    private final boolean isStatic;         // true if 'import static'
    private final int startOffset;          // start of 'import' keyword in source
    private final int endOffset;            // end of import statement (before or at semicolon)
    private final int pathStartOffset;      // start of the package.Class path
    private final int pathEndOffset;        // end of the package.Class path (before .*)
    
    private TypeInfo resolvedType;          // null if wildcard or unresolved
    private boolean resolved;               // whether resolution was attempted and succeeded
    
    // Track usage count (simpler than tracking individual tokens)
    private int usageCount = 0;
    
    // Track all tokens that reference this import (optional, for detailed analysis)
    private final List<Token> referencingTokens = new ArrayList<>();

    public ImportData(String fullPath, String simpleName, boolean isWildcard, boolean isStatic,
                      int startOffset, int endOffset, int pathStartOffset, int pathEndOffset) {
        this.fullPath = fullPath;
        this.simpleName = simpleName;
        this.isWildcard = isWildcard;
        this.isStatic = isStatic;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.pathStartOffset = pathStartOffset;
        this.pathEndOffset = pathEndOffset;
        this.resolved = false;
    }

    // Getters
    public String getFullPath() { return fullPath; }
    public String getSimpleName() { return simpleName; }
    public boolean isWildcard() { return isWildcard; }
    public boolean isStatic() { return isStatic; }
    public int getStartOffset() { return startOffset; }
    public int getEndOffset() { return endOffset; }
    public int getPathStartOffset() { return pathStartOffset; }
    public int getPathEndOffset() { return pathEndOffset; }
    public TypeInfo getResolvedType() { return resolvedType; }
    public boolean isResolved() { return resolved; }

    /**
     * Set the resolved type information.
     */
    public void setResolvedType(TypeInfo typeInfo) {
        this.resolvedType = typeInfo;
        this.resolved = typeInfo != null && typeInfo.isResolved();
    }

    /**
     * Mark this import as resolved (for wildcard imports where we confirm the package exists).
     */
    public void markResolved(boolean resolved) {
        this.resolved = resolved;
    }
    
    // ==================== REFERENCE TRACKING ====================
    
    /**
     * Increment the usage count for this import.
     * Called during type resolution when this import is used.
     */
    public void incrementUsage() {
        usageCount++;
    }
    
    /**
     * Add a token that references this import.
     * Called when a token is created that uses this import's type.
     */
    public void addReference(Token token) {
        if (token != null && !referencingTokens.contains(token)) {
            referencingTokens.add(token);
        }
    }
    
    /**
     * Get all tokens that reference this import.
     */
    public List<Token> getReferencingTokens() {
        return Collections.unmodifiableList(referencingTokens);
    }
    
    /**
     * Check if this import is used (has any references or usages).
     */
    public boolean isUsed() {
        return usageCount > 0 || !referencingTokens.isEmpty();
    }
    
    /**
     * Get the usage count for this import.
     */
    public int getUsageCount() {
        return usageCount;
    }
    
    /**
     * Get the number of references to this import.
     */
    public int getReferenceCount() {
        return referencingTokens.size();
    }
    
    /**
     * Clear all references and reset usage count (used when re-tokenizing).
     */
    public void clearReferences() {
        referencingTokens.clear();
        usageCount = 0;
    }

    /**
     * Get the package portion of the import path.
     * For "java.util.List" returns "java.util"
     * For wildcards like "java.util.*" returns "java.util"
     */
    public String getPackagePortion() {
        if (isWildcard) {
            return fullPath;
        }
        int lastDot = fullPath.lastIndexOf('.');
        return lastDot > 0 ? fullPath.substring(0, lastDot) : "";
    }

    /**
     * Get all segments of the import path.
     */
    public String[] getPathSegments() {
        return fullPath.split("\\.");
    }

    /**
     * Check if this import could provide the given simple class name.
     * For wildcard imports, always returns true (might provide any class).
     * For specific imports, checks if the simple name matches.
     */
    public boolean couldProvide(String className) {
        if (isWildcard) {
            return true;
        }
        return className.equals(simpleName);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("import ");
        if (isStatic) sb.append("static ");
        sb.append(fullPath);
        if (isWildcard) sb.append(".*");
        sb.append(" [").append(resolved ? "resolved" : "unresolved").append("]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImportData that = (ImportData) o;
        return fullPath.equals(that.fullPath) && isWildcard == that.isWildcard && isStatic == that.isStatic;
    }

    @Override
    public int hashCode() {
        return fullPath.hashCode() * 31 + (isWildcard ? 1 : 0) + (isStatic ? 2 : 0);
    }
}
