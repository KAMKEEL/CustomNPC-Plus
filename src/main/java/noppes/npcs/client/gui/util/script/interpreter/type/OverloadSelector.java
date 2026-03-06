package noppes.npcs.client.gui.util.script.interpreter.type;

import noppes.npcs.client.gui.util.script.interpreter.field.FieldInfo;
import noppes.npcs.client.gui.util.script.interpreter.method.MethodInfo;

import java.util.List;

/**
 * Helper class for selecting the best method overload from a list of candidates.
 * 
 * This encapsulates the scoring-based overload selection logic that considers:
 * - Exact type matches
 * - Numeric promotion compatibility
 * - Arity (parameter count) matching
 * - Varargs handling
 * - Unknown (null) argument types
 * 
 * Lower scores indicate better matches.
 */
public class OverloadSelector {

    // Scoring constants
    private static final int ARITY_MISMATCH_BASE = 10000;
    private static final int VARARGS_PENALTY = 50;
    private static final int UNKNOWN_TYPE_PENALTY = 10;
    private static final int COMPATIBLE_TYPE_PENALTY = 5;
    private static final int INCOMPATIBLE_TYPE_PENALTY = 100;
    
    /**
     * Select the best overload from a list of candidates based on argument types.
     * 
     * Selection phases:
     * 1. Exact match (all types match exactly)
     * 2. Numeric promotion match (all args can promote to common param type)
     * 3. Scoring-based selection (handles unknown types, varargs, etc.)
     * 
     * @param overloads List of candidate method overloads
     * @param argTypes Array of argument types (may contain nulls for unknown types)
     * @return The best matching overload, or the first overload if no good match found
     */
    public static MethodInfo selectBestOverload(List<MethodInfo> overloads, TypeInfo[] argTypes) {
        if (overloads == null || overloads.isEmpty()) return null;
        
        int argCount = (argTypes == null) ? 0 : argTypes.length;
        
        // Fast path: If no arguments provided, try to find zero-arg method
        if (argCount == 0) {
            for (MethodInfo method : overloads) {
                if (method.getParameterCount() == 0) {
                    return method;
                }
            }
            // Fall back to closest arity
            return selectClosestArity(overloads, 0);
        }
        
        // Check if all arg types are known (non-null)
        boolean allArgsKnown = true;
        for (TypeInfo argType : argTypes) {
            if (argType == null) {
                allArgsKnown = false;
                break;
            }
        }
        
        // Phase 1: Try exact match (only when all args known)
        if (allArgsKnown) {
            MethodInfo exactMatch = findExactMatch(overloads, argTypes, argCount);
            if (exactMatch != null) return exactMatch;
        }
        
        // Phase 2: Try numeric promotion match (only when all args known)
        if (allArgsKnown) {
            MethodInfo numericMatch = findNumericPromotionMatch(overloads, argTypes, argCount);
            if (numericMatch != null) return numericMatch;
        }
        
        // Phase 3: Scoring-based selection with arity-first preference
        // This handles unknown (null) arg types gracefully and prefers fixed-arity over varargs
        return scoringBasedSelection(overloads, argTypes, argCount);
    }
    
    /**
     * Find an overload with exact type match for all arguments.
     */
    private static MethodInfo findExactMatch(List<MethodInfo> overloads, TypeInfo[] argTypes, int argCount) {
        for (MethodInfo method : overloads) {
            if (method.getParameterCount() == argCount) {
                boolean exactMatch = true;
                List<FieldInfo> params = method.getParameters();
                for (int i = 0; i < argCount; i++) {
                    TypeInfo paramType = params.get(i).getTypeInfo();
                    TypeInfo argType = argTypes[i];
                    if (paramType == null || !paramType.equals(argType)) {
                        exactMatch = false;
                        break;
                    }
                }
                if (exactMatch) return method;
            }
        }
        return null;
    }
    
    /**
     * Find an overload where all numeric arguments can promote to a common parameter type.
     * Prefers narrower numeric types (e.g., int over long over double).
     */
    private static MethodInfo findNumericPromotionMatch(List<MethodInfo> overloads, TypeInfo[] argTypes, int argCount) {
        MethodInfo bestNumericMatch = null;
        int bestNumericRank = Integer.MAX_VALUE;
        
        for (MethodInfo method : overloads) {
            if (method.getParameterCount() == argCount) {
                List<FieldInfo> params = method.getParameters();
                
                // Check if all parameters and arguments are numeric primitives
                boolean allNumeric = true;
                for (int i = 0; i < argCount; i++) {
                    TypeInfo paramType = params.get(i).getTypeInfo();
                    TypeInfo argType = argTypes[i];
                    if (paramType == null || 
                        !TypeChecker.isNumericPrimitive(paramType) || !TypeChecker.isNumericPrimitive(argType)) {
                        allNumeric = false;
                        break;
                    }
                }
                
                if (allNumeric) { 
                    // Check if all parameters are the same numeric type
                    TypeInfo commonParamType = params.get(0).getTypeInfo();
                    boolean allParamsSame = true;
                    for (int i = 1; i < params.size(); i++) {
                        TypeInfo paramType = params.get(i).getTypeInfo();
                        if (!paramType.equals(commonParamType)) {
                            allParamsSame = false;
                            break;
                        }
                    }
                    
                    // If all parameters are the same numeric type, check if args can promote to it
                    if (allParamsSame) {
                        boolean canPromote = true;
                        for (int i = 0; i < argCount; i++) {
                            if (!TypeChecker.canPromoteNumeric(argTypes[i], commonParamType)) {
                                canPromote = false;
                                break;
                            }
                        }
                        
                        // If all args can promote, check if this is the narrowest match so far
                        if (canPromote) {
                            int paramRank = TypeChecker.getNumericRank(commonParamType.getJavaClass());
                            if (paramRank < bestNumericRank) {
                                bestNumericRank = paramRank;
                                bestNumericMatch = method;
                            }
                        }
                    }
                }
            }
        }
        
        return bestNumericMatch;
    }
    
    /**
     * Use scoring to select the best overload when exact/numeric matches don't apply.
     */
    private static MethodInfo scoringBasedSelection(List<MethodInfo> overloads, TypeInfo[] argTypes, int argCount) {
        MethodInfo bestCandidate = null;
        int bestScore = Integer.MAX_VALUE;
        
        for (MethodInfo method : overloads) {
            int score = scoreOverload(method, argTypes, argCount);
            if (score < bestScore) {
                bestScore = score;
                bestCandidate = method;
            }
        }
        
        return (bestCandidate != null) ? bestCandidate : overloads.get(0);
    }
    
    /**
     * Score an overload for matching against provided arguments.
     * Lower score = better match.
     * 
     * Scoring priorities:
     * - Arity mismatch: +10000 + |paramCount - argCount|
     * - Varargs penalty: +1000
     * - Unknown arg type (null): +10 per arg
     * - Compatible but not exact: +5 per arg
     * - Incompatible type: +100 per arg
     * - Exact type match: +0 per arg
     * 
     * @param method The method overload to score
     * @param argTypes Array of argument types (may contain nulls)
     * @param argCount Number of arguments
     * @return Score (lower is better)
     */
    public static int scoreOverload(MethodInfo method, TypeInfo[] argTypes, int argCount) {
        int score = 0;
        int paramCount = method.getParameterCount();
        
        // Check if method is varargs
        boolean isVarArgs = false;
        java.lang.reflect.Method javaMethod = method.getJavaMethod();
        if (javaMethod != null) {
            isVarArgs = javaMethod.isVarArgs();
        } else {
            // JS methods from .d.ts have no backing Java method; vararg info lives on the last FieldInfo
            List<FieldInfo> checkParams = method.getParameters();
            if (!checkParams.isEmpty()) {
                isVarArgs = checkParams.get(checkParams.size() - 1).isVarArg();
            }
        }
        
        // Determine arity applicability
        boolean arityApplicable;
        if (isVarArgs) {
            // Varargs methods accept argCount >= paramCount - 1 
            // (the varargs array can be empty or have multiple elements)
            arityApplicable = argCount >= paramCount - 1;
        } else {
            arityApplicable = paramCount == argCount;
        }
        
        if (!arityApplicable) {
            // Large penalty for arity mismatch, plus distance to encourage closest match
            score += ARITY_MISMATCH_BASE + Math.abs(paramCount - argCount);
        }
        
        // Varargs penalty (fixed-arity preferred over varargs when both apply)
        if (isVarArgs) {
            score += VARARGS_PENALTY;
        }
        
        // Score each argument's type compatibility
        List<FieldInfo> params = method.getParameters();
        int paramsToCheck = Math.min(argCount, paramCount);
        
        for (int i = 0; i < paramsToCheck; i++) {
            TypeInfo paramType = params.get(i).getTypeInfo();
            TypeInfo argType = (argTypes != null && i < argTypes.length) ? argTypes[i] : null;
            
            if (argType == null) {
                // Unknown arg type - don't disqualify, but add penalty
                score += UNKNOWN_TYPE_PENALTY;
            } else if (paramType == null) {
                // Unknown param type - add penalty
                score += UNKNOWN_TYPE_PENALTY;
            } else if (paramType.equals(argType)) {
                // Exact match - no penalty
                score += 0;
            } else if (TypeChecker.isTypeCompatible(paramType, argType)) {
                // Compatible but not exact
                score += COMPATIBLE_TYPE_PENALTY;
            } else {
                // Incompatible type
                score += INCOMPATIBLE_TYPE_PENALTY;
            }
        }
        
        // For vararg methods, also score extra args (index >= paramCount) against the element type
        if (isVarArgs && argCount > paramCount && !params.isEmpty()) {
            TypeInfo varargElemType = params.get(paramCount - 1).getTypeInfo();
            for (int i = paramCount; i < argCount; i++) {
                TypeInfo argType = (argTypes != null && i < argTypes.length) ? argTypes[i] : null;
                if (argType == null || varargElemType == null) {
                    score += UNKNOWN_TYPE_PENALTY;
                } else if (varargElemType.equals(argType)) {
                    // exact match
                } else if (TypeChecker.isTypeCompatible(varargElemType, argType)) {
                    score += COMPATIBLE_TYPE_PENALTY;
                } else {
                    score += INCOMPATIBLE_TYPE_PENALTY;
                }
            }
        }
        
        // Penalize extra args that don't match params (for non-varargs)
        if (!isVarArgs && argCount > paramCount) {
            score += (argCount - paramCount) * INCOMPATIBLE_TYPE_PENALTY;
        }
        
        return score;
    }
    
    /**
     * Select the overload with closest arity to the target.
     * Tie-breaker: lower parameter count wins.
     * 
     * @param overloads List of candidate overloads
     * @param targetArity The desired number of parameters
     * @return The overload with closest arity, or first overload if list is empty
     */
    public static MethodInfo selectClosestArity(List<MethodInfo> overloads, int targetArity) {
        if (overloads == null || overloads.isEmpty()) return null;
        
        MethodInfo best = null;
        int bestDistance = Integer.MAX_VALUE;
        int bestParamCount = Integer.MAX_VALUE;
        
        for (MethodInfo method : overloads) {
            int paramCount = method.getParameterCount();
            int distance = Math.abs(paramCount - targetArity);
            
            // Prefer smaller distance; tie-break by lower paramCount
            if (distance < bestDistance || (distance == bestDistance && paramCount < bestParamCount)) {
                best = method;
                bestDistance = distance;
                bestParamCount = paramCount;
            }
        }
        
        return (best != null) ? best : overloads.get(0);
    }
}
