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
        if (!type.isResolved()) {
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
            TypeStringNormalizer.ArraySplit split = TypeStringNormalizer.splitArraySuffixes(typeName);
            String elementTypeName = split.base;
            int dims = split.dimensions;
            TypeInfo elementSubstitution = elementTypeName != null ? bindings.get(elementTypeName) : null;
            if (elementSubstitution != null) {
                TypeInfo result = elementSubstitution;
                for (int i = 0; i < dims; i++) {
                    result = TypeInfo.arrayOf(result);
                }
                return result;
            }
        }
        
        return type;
    }
    
}
