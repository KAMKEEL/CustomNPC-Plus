package noppes.npcs.client.gui.util.script.interpreter.js_parser;

import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeResolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a generic type parameter like "T extends Entity" or "T extends Number & Comparable<T>".
 * Stores the parameter name and both the string representation (for deferred resolution)
 * and the resolved TypeInfo for the bound.
 * 
 * Multiple bounds (e.g., {@code <T extends Number & Comparable<T>>}) are supported:
 * the primary bound is the first type after "extends", and additional bounds are
 * interface types separated by "&".
 */
public class TypeParamInfo {
    private final String name;           // e.g., "T"
    private final String boundType;      // Simple name like "EntityPlayerMP" (may be null)
    private final String fullBoundType;  // Full name like "net.minecraft.entity.player.EntityPlayerMP" (may be null)
    private TypeInfo boundTypeInfo;      // Resolved TypeInfo for the primary bound (resolved in Phase 2)
    
    // Additional bounds from & syntax (e.g., Comparable<T> in "T extends Number & Comparable<T>")
    private final List<String> additionalBoundNames;
    private final List<TypeInfo> additionalBoundTypes;

    /**
     * Constructor for parse-time (Phase 1) - stores string names only.
     */
    public TypeParamInfo(String name, String boundType, String fullBoundType) {
        this.name = name;
        this.boundType = boundType;
        this.fullBoundType = fullBoundType;
        this.boundTypeInfo = null;  // Will be resolved later
        this.additionalBoundNames = new ArrayList<>();
        this.additionalBoundTypes = new ArrayList<>();
    }
    
    public String getName() { return name; }

    public TypeInfo getBoundTypeInfo() {
        return boundTypeInfo;
    }

    public void setBoundTypeInfo(TypeInfo boundTypeInfo) {
        this.boundTypeInfo = boundTypeInfo;
    }

    /**
     * Add an additional bound (from & syntax).
     * @param boundName The simple name of the additional bound type
     */
    public void addAdditionalBound(String boundName) {
        additionalBoundNames.add(boundName);
    }

    /**
     * Add a resolved additional bound TypeInfo.
     * @param typeInfo The resolved TypeInfo for an additional bound
     */
    public void addAdditionalBoundType(TypeInfo typeInfo) {
        additionalBoundTypes.add(typeInfo);
    }

    /**
     * Get the list of additional bound names (unresolved).
     */
    public List<String> getAdditionalBoundNames() {
        return Collections.unmodifiableList(additionalBoundNames);
    }

    /**
     * Get the list of resolved additional bound TypeInfos.
     */
    public List<TypeInfo> getAdditionalBoundTypes() {
        return Collections.unmodifiableList(additionalBoundTypes);
    }

    /**
     * Check if this type parameter has additional bounds (& syntax).
     */
    public boolean hasAdditionalBounds() {
        return !additionalBoundNames.isEmpty();
    }

    /**
     * Resolve the bound type using TypeResolver.
     * Called during Phase 2 after all types are loaded.
     */
    public void resolveBoundType() {
        if (boundTypeInfo != null)
            return;  // Already resolved

        if (fullBoundType != null && !fullBoundType.isEmpty()) {
            // Try to load the Java class using the full name
            boundTypeInfo = TypeResolver.getInstance().resolveFullName(fullBoundType);
        } else if (boundType != null && !boundType.isEmpty()) {
            // Try to resolve using the simple name
            boundTypeInfo = TypeResolver.getInstance().resolveJSType(boundType);
        }
    }

    /**
     * Get display name for the bound type, including generic arguments.
     */
    public String getBoundTypeName() {
        if (boundTypeInfo == null)
            return null;
        return boundTypeInfo.getDisplayName();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name);
        if (boundTypeInfo != null) {
            sb.append(" extends ").append(boundTypeInfo.getDisplayName());
        } else if (boundType != null || fullBoundType != null) {
            sb.append(" extends ").append(boundType != null ? boundType : fullBoundType);
        }
        for (int i = 0; i < additionalBoundTypes.size(); i++) {
            sb.append(" & ").append(additionalBoundTypes.get(i).getDisplayName());
        }
        // Show unresolved additional bounds that don't have a resolved type yet
        for (int i = additionalBoundTypes.size(); i < additionalBoundNames.size(); i++) {
            sb.append(" & ").append(additionalBoundNames.get(i));
        }
        return sb.toString();
    }
}
