package noppes.npcs.client.gui.util.script.interpreter.type;

import noppes.npcs.client.gui.util.script.interpreter.js_parser.TypeParamInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A context for resolving and substituting declared type parameters (T, E, K, V...)
 * based on a receiver type.
 *
 * Policy:
 * - Prefer applied type arguments when present (e.g. List<String> binds E -> String).
 * - Fall back to declared bounds when no applied argument exists (e.g. T extends Entity -> Entity).
 */
public final class GenericContext {

    /** Singleton context for types with no generic parameters. Used to avoid object allocation. */
    private static final GenericContext EMPTY = new GenericContext(new HashMap<>(), new HashMap<>());

    /** 
     * Map of type variable names to their applied type arguments.
     * For example, in List&lt;String&gt;, this contains E -&gt; TypeInfo.STRING.
     * Applied bindings take precedence over bound fallbacks.
     */
    private final Map<String, TypeInfo> appliedBindings;
    
    /** 
     * Map of type variable names to their declared upper bounds.
     * For example, in &lt;T extends Entity&gt;, this contains T -&gt; TypeInfo(Entity).
     * Used as fallback when no applied argument exists.
     */
    private final Map<String, TypeInfo> boundFallbacks;

    private GenericContext(Map<String, TypeInfo> appliedBindings, Map<String, TypeInfo> boundFallbacks) {
        this.appliedBindings = appliedBindings != null ? appliedBindings : new HashMap<>();
        this.boundFallbacks = boundFallbacks != null ? boundFallbacks : new HashMap<>();
    }

    /**
     * Create a GenericContext for a receiver type.
     * 
     * Fast path: returns singleton EMPTY for non-generic types (no allocation overhead).
     * Slow path: builds maps from type parameters and applied arguments for generic types.
     * 
     * @param receiverType the type to extract generic bindings from (e.g., List&lt;String&gt;, DAO&lt;T extends Entity&gt;)
     * @return a context with applied bindings and bound fallbacks, or EMPTY singleton if receiverType is not generic
     */
    public static GenericContext forReceiver(TypeInfo receiverType) {
        // Fast path: return singleton for non-generic types
        if (receiverType == null || !hasGenerics(receiverType)) {
            return EMPTY;
        }

        TypeInfo rawType = receiverType.getRawType();
        List<TypeParamInfo> declaredParams = rawType != null ? rawType.getTypeParams() : null;
        List<TypeInfo> appliedArgs = receiverType.getAppliedTypeArgs();

        Map<String, TypeInfo> applied = new HashMap<>();
        if (declaredParams != null && appliedArgs != null) {
            int count = Math.min(declaredParams.size(), appliedArgs.size());
            for (int i = 0; i < count; i++) {
                TypeParamInfo declared = declaredParams.get(i);
                TypeInfo arg = appliedArgs.get(i);
                if (declared == null || declared.getName() == null || declared.getName().isEmpty() || arg == null) {
                    continue;
                }
                applied.put(declared.getName(), arg);
            }
        }

        Map<String, TypeInfo> bounds = new HashMap<>();
        if (declaredParams != null) {
            for (TypeParamInfo declared : declaredParams) {
                if (declared == null) continue;
                String name = declared.getName();
                if (name == null || name.isEmpty()) continue;
                TypeInfo bound = declared.getBoundTypeInfo();
                if (bound != null && bound.isResolved()) {
                    bounds.put(name, bound);
                }
            }
        }

        return new GenericContext(applied, bounds);
    }

    /**
     * Check if a type has generics that need substitution.
     * 
     * Returns true if:
     * - Type is parameterized (e.g., List&lt;String&gt;)
     * - Type's raw form differs from the type itself
     * - Type has declared type parameters
     * 
     * @param type the type to check
     * @return true if this type contains generic information that may need substitution
     */
    public static boolean hasGenerics(TypeInfo type) {
        return type.isParameterized() || 
               type.getRawType() != type || 
               (type.getTypeParams() != null && !type.getTypeParams().isEmpty());
    }

    /**
     * Resolve a type variable to its bound or applied type.
     * 
     * Prefers applied bindings (e.g., E -&gt; String in List&lt;String&gt;) over declared bounds.
     * Falls back to bounds (e.g., T -&gt; Entity in &lt;T extends Entity&gt;) if no applied argument exists.
     * Returns null if the variable is not found in either map.
     * 
     * @param name the type variable name (e.g., "T", "E", "K")
     * @return the resolved TypeInfo, or null if variable not found
     */
    public TypeInfo resolveTypeVariable(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }

        TypeInfo applied = appliedBindings.get(name);
        if (applied != null) {
            return applied;
        }

        return boundFallbacks.get(name);
    }

    /**
     * Substitute type variables in a TypeInfo recursively.
     * 
     * Handles:
     * - Direct type variables (unresolved types representing T, E, etc.)
     * - Parameterized types (recursively substitutes inside type arguments)
     * - Array types (substitutes the element type, preserves array dimensions)
     * 
     * @param type the type to substitute (may contain type variables)
     * @return the substituted type, or the original type if no substitutions apply
     */
    public TypeInfo substitute(TypeInfo type) {
        if (type == null) {
            return null;
        }

        // Direct type variable substitution (unresolved types represent type variables like T).
        if (!type.isResolved()) {
            TypeInfo substitution = resolveTypeVariable(type.getSimpleName());
            if (substitution != null) {
                return substitution;
            }
        }

        // Parameterized types: substitute inside args.
        if (type.isParameterized()) {
            List<TypeInfo> originalArgs = type.getAppliedTypeArgs();
            List<TypeInfo> substitutedArgs = new ArrayList<>(originalArgs.size());
            boolean changed = false;

            for (TypeInfo arg : originalArgs) {
                TypeInfo substitutedArg = substitute(arg);
                substitutedArgs.add(substitutedArg);
                if (substitutedArg != arg) {
                    changed = true;
                }
            }

            if (changed) {
                return type.getRawType().parameterize(substitutedArgs);
            }
        }

        // Array wrapper types are represented by a display-name suffix; we can still substitute
        // a plain "T[]" by looking up "T".
        String simple = type.getSimpleName();
        if (simple != null && simple.endsWith("[]")) {
            TypeStringNormalizer.ArraySplit split = TypeStringNormalizer.splitArraySuffixes(simple);
            String elementName = split.base;
            int dims = split.dimensions;
            TypeInfo elementSub = resolveTypeVariable(elementName);
            if (elementSub != null) {
                TypeInfo result = elementSub;
                for (int i = 0; i < dims; i++) {
                    result = TypeInfo.arrayOf(result);
                }
                return result;
            }
        }

        return type;
    }

    /**
     * Substitute type variables in a type string.
     * 
     * Attempts substitution in this order:
     * 1. Normalize the string (strip imports, pick union branch, remove nullable suffix, split arrays)
     * 2. Try direct type variable lookup (e.g., "T" -&gt; resolved Entity)
     * 3. Fall back to full type resolution with substitution applied
     * 
     * @param typeString the type expression as a string (e.g., "T", "T[]", "List&lt;T&gt;")
     * @param resolver optional TypeResolver for fallback resolution; if null, returns null on failure
     * @return the substituted type, or null if resolution fails
     */
    public TypeInfo substituteString(String typeString, TypeResolver resolver) {
        if (typeString == null || typeString.trim().isEmpty()) {
            return null;
        }

        String normalized = TypeStringNormalizer.stripImportTypeSyntax(typeString);
        normalized = TypeStringNormalizer.pickPreferredUnionBranch(normalized);
        normalized = TypeStringNormalizer.stripNullableSuffix(normalized);

        TypeStringNormalizer.ArraySplit arraySplit = TypeStringNormalizer.splitArraySuffixes(normalized);
        String baseExpr = arraySplit.base;
        int dims = arraySplit.dimensions;

        String bareBase = GenericTypeParser.stripGenerics(baseExpr);
        TypeInfo direct = resolveTypeVariable(bareBase);
        if (direct != null) {
            TypeInfo result = direct;
            for (int i = 0; i < dims; i++) {
                result = TypeInfo.arrayOf(result);
            }
            return result;
        }

        if (resolver == null) {
            return null;
        }
        TypeInfo resolved = resolver.resolveJSType(typeString);
        return substitute(resolved);
    }

    /**
     * Substitute type variables in a resolved type, with string fallback.
     * 
     * Two-phase approach:
     * 1. Try substituting the resolved type directly
     * 2. If that fails, attempt substitution via the raw string as fallback
     * 
     * This handles edge cases where the TypeInfo doesn't capture enough information
     * to do proper substitution (e.g., union types, complex generics).
     * 
     * @param resolvedType a TypeInfo that has already been resolved (may still contain type variables)
     * @param rawTypeString the original type string before resolution (for fallback)
     * @param resolver optional TypeResolver for string-based fallback resolution
     * @return the substituted type, or the best attempt if full substitution fails
     */
    public TypeInfo substituteType(TypeInfo resolvedType, String rawTypeString, TypeResolver resolver) {
        if (resolvedType == null) {
            return null;
        }

        TypeInfo substituted = substitute(resolvedType);
        if (substituted != null && substituted.isResolved()) {
            return substituted;
        }

        if (rawTypeString != null && !rawTypeString.trim().isEmpty()) {
            TypeInfo fromString = substituteString(rawTypeString, resolver);
            if (fromString != null && fromString.isResolved()) {
                return fromString;
            }
        }

        return substituted;
    }
}
