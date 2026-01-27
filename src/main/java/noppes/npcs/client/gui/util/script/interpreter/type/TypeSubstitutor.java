package noppes.npcs.client.gui.util.script.interpreter.type;

import noppes.npcs.client.gui.util.script.interpreter.js_parser.TypeParamInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility for substituting type variables (like T, E, K, V) with concrete types.
 * 
 * Used when resolving members on parameterized types:
 * - List<String>.get(int) returns String (not T/Object)
 * - Map<String, Integer>.get(Object) returns Integer (not V)
 * 
 * Handles nested generics:
 * - List<Map<String, Integer>>.get(0) returns Map<String, Integer>
 */
public class TypeSubstitutor {
    
    /**
     * Create a binding map from declared type parameters to applied type arguments.
     * 
     * Example: For List<String> where List is declared as interface List<E>:
     * - declaredParams = [TypeParamInfo("E", ...)]
     * - appliedArgs = [TypeInfo(String)]
     * - Result: {"E" -> TypeInfo(String)}
     * 
     * @param declaredParams The declared type parameters (from the generic type definition)
     * @param appliedArgs The applied type arguments (from the parameterized usage)
     * @return A map from type parameter names to their concrete TypeInfo values
     */
    public static Map<String, TypeInfo> createBindings(List<TypeParamInfo> declaredParams, List<TypeInfo> appliedArgs) {
        Map<String, TypeInfo> bindings = new HashMap<>();
        
        if (declaredParams == null || appliedArgs == null) {
            return bindings;
        }
        
        int count = Math.min(declaredParams.size(), appliedArgs.size());
        for (int i = 0; i < count; i++) {
            String paramName = declaredParams.get(i).getName();
            TypeInfo argType = appliedArgs.get(i);
            if (paramName != null && argType != null) {
                bindings.put(paramName, argType);
            }
        }
        
        return bindings;
    }
    
    /**
     * Create bindings from a parameterized receiver type.
     * 
     * @param receiverType The parameterized type (e.g., List<String>)
     * @return Bindings from the type's declared params to its applied args
     */
    public static Map<String, TypeInfo> createBindingsFromReceiver(TypeInfo receiverType) {
        if (receiverType == null) {
            return new HashMap<>();
        }
        
        // Get the raw type to access declared type parameters
        TypeInfo rawType = receiverType.getRawType();
        List<TypeParamInfo> declaredParams = null;
        
        // Get declared params from JSTypeInfo or reflection
        if (rawType.isJSType() && rawType.getJSTypeInfo() != null) {
            declaredParams = rawType.getJSTypeInfo().getTypeParams();
        } else {
            declaredParams = rawType.getTypeParams();
        }
        
        // Get applied args from the parameterized type
        List<TypeInfo> appliedArgs = receiverType.getAppliedTypeArgs();
        
        return createBindings(declaredParams, appliedArgs);
    }
    
    /**
     * Substitute type variables in a type using the given bindings.
     * 
     * @param type The type to substitute (may contain type variables like T, E)
     * @param bindings The map from type variable names to concrete types
     * @return The substituted type
     */
    public static TypeInfo substitute(TypeInfo type, Map<String, TypeInfo> bindings) {
        if (type == null || bindings == null || bindings.isEmpty()) {
            return type;
        }
        
        String typeName = type.getSimpleName();
        
        // Check if this type is itself a type variable
        if (!type.isResolved() || isTypeVariable(typeName)) {
            TypeInfo substitution = bindings.get(typeName);
            if (substitution != null) {
                return substitution;
            }
        }
        
        // If the type has applied type arguments, substitute within them recursively
        if (type.isParameterized()) {
            List<TypeInfo> originalArgs = type.getAppliedTypeArgs();
            List<TypeInfo> substitutedArgs = new ArrayList<>();
            boolean changed = false;
            
            for (TypeInfo arg : originalArgs) {
                TypeInfo substitutedArg = substitute(arg, bindings);
                substitutedArgs.add(substitutedArg);
                if (substitutedArg != arg) {
                    changed = true;
                }
            }
            
            if (changed) {
                // Create a new parameterized type with substituted args
                return type.getRawType().parameterize(substitutedArgs);
            }
        }
        
        // Handle array types - substitute the element type
        if (type.getSimpleName().endsWith("[]")) {
            // Extract element type and substitute
            String elementTypeName = typeName.substring(0, typeName.length() - 2);
            TypeInfo elementSubstitution = bindings.get(elementTypeName);
            if (elementSubstitution != null) {
                return TypeInfo.arrayOf(elementSubstitution);
            }
        }
        
        return type;
    }
    
    /**
     * Substitute type variables in a type string using the given bindings.
     * Used when we have a raw type string (like "T" or "List<T>") that needs substitution.
     * 
     * @param typeString The raw type string
     * @param bindings The map from type variable names to concrete types
     * @param resolver The type resolver to use for parsing
     * @return The substituted TypeInfo
     */
    public static TypeInfo substituteString(String typeString, Map<String, TypeInfo> bindings, TypeResolver resolver) {
        if (typeString == null || typeString.isEmpty()) {
            return null;
        }
        
        // First check if the whole string is a type variable
        String trimmed = typeString.trim();
        
        // Handle array suffix
        boolean isArray = trimmed.endsWith("[]");
        String baseName = isArray ? trimmed.substring(0, trimmed.length() - 2).trim() : trimmed;
        
        // Strip generic args to check the base name
        String bareBaseName = GenericTypeParser.stripGenerics(baseName);
        
        // If bare base name is a type variable, substitute
        TypeInfo directSubstitution = bindings.get(bareBaseName);
        if (directSubstitution != null) {
            // It's a type variable, return the substitution
            TypeInfo result = directSubstitution;
            if (isArray) {
                result = TypeInfo.arrayOf(result);
            }
            return result;
        }
        
        // Parse and resolve the type, then substitute within it
        TypeInfo resolved = resolver.resolveJSType(typeString);
        return substitute(resolved, bindings);
    }
    
    /**
     * Check if a type name looks like a type variable (single uppercase letter or short uppercase name).
     */
    private static boolean isTypeVariable(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        // Common type variable patterns: T, E, K, V, R, U, etc.
        // Also handles longer ones like KEY, VALUE but those are rare
        return name.length() <= 3 && 
               Character.isUpperCase(name.charAt(0)) &&
               name.chars().allMatch(c -> Character.isUpperCase(c) || Character.isDigit(c));
    }
    
    /**
     * Get the return type for a method on a parameterized receiver, with type variable substitution.
     * 
     * @param rawReturnType The method's raw return type (may contain type variables)
     * @param rawReturnTypeString The method's raw return type as a string (for unresolved types)
     * @param receiverType The parameterized receiver type
     * @param resolver The type resolver
     * @return The substituted return type
     */
    public static TypeInfo getSubstitutedReturnType(TypeInfo rawReturnType, String rawReturnTypeString, 
                                                     TypeInfo receiverType, TypeResolver resolver) {
        Map<String, TypeInfo> bindings = createBindingsFromReceiver(receiverType);
        if (bindings.isEmpty()) {
            return rawReturnType;
        }
        
        // Try substituting the resolved type first
        TypeInfo substituted = substitute(rawReturnType, bindings);
        if (substituted != rawReturnType && substituted.isResolved()) {
            return substituted;
        }
        
        // If still not resolved, try substituting from the string
        if (rawReturnTypeString != null && !rawReturnTypeString.isEmpty()) {
            TypeInfo fromString = substituteString(rawReturnTypeString, bindings, resolver);
            if (fromString != null && fromString.isResolved()) {
                return fromString;
            }
        }
        
        return substituted;
    }
}
