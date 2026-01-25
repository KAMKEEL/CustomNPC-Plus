package noppes.npcs.client.gui.util.script.interpreter.js_parser;

import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeResolver;

/**
 * Represents a generic type parameter like "T extends Entity".
 * Stores the parameter name and both the string representation (for deferred resolution)
 * and the resolved TypeInfo for the bound.
 */
public class TypeParamInfo {
    private final String name;           // e.g., "T"
    private final String boundType;      // Simple name like "EntityPlayerMP" (may be null)
    private final String fullBoundType;  // Full name like "net.minecraft.entity.player.EntityPlayerMP" (may be null)
    private TypeInfo boundTypeInfo;      // Resolved TypeInfo for the bound (resolved in Phase 2)

    /**
     * Constructor for parse-time (Phase 1) - stores string names only.
     */
    public TypeParamInfo(String name, String boundType, String fullBoundType) {
        this.name = name;
        this.boundType = boundType;
        this.fullBoundType = fullBoundType;
        this.boundTypeInfo = null;  // Will be resolved later
    }
    
    public String getName() { return name; }

    public TypeInfo getBoundTypeInfo() {
        return boundTypeInfo;
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
     * Get simple display name for the bound type.
     */
    public String getBoundTypeName() {
        if (boundTypeInfo == null)
            return null;
        return boundTypeInfo.getSimpleName();
    }
    
    @Override
    public String toString() {
        if (boundTypeInfo != null) {
            return name + " extends " + boundTypeInfo.getSimpleName();
        } else if (boundType != null || fullBoundType != null) {
            return name + " extends " + (boundType != null ? boundType : fullBoundType);
        }
        return name;
    }
}
