package noppes.npcs.janino;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Resolves script hook methods for compiled script classes.
 * Optimized for high-frequency hooks like TICK with fast O(1) lookups.
 */
public final class JaninoHookResolver {

    /**
     * Primary cache: hookName -> CachedHandle
     * Fast O(1) lookup without string building for non-overloaded hooks.
     */
    private final Map<String, CachedHandle> handleCache = new HashMap<>();

    /**
     * Track which compiled class we've cached methods for.
     * If the class changes (recompilation), we clear the cache.
     */
    private Class<?> lastCompiledClass;

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    /**
     * Cached handle with expected parameter count for quick validation.
     */
    private static final class CachedHandle {
        final MethodHandle handle;
        final int paramCount;

        CachedHandle(MethodHandle handle, int paramCount) {
            this.handle = handle;
            this.paramCount = paramCount;
        }
    }

    public JaninoHookResolver() {
    }

    /**
     * Clears internal resolution caches.
     */
    public void clearResolutionCaches() {
        handleCache.clear();
        lastCompiledClass = null;
    }

    /**
     * Resolve a MethodHandle for the given hook.
     * Optimized for repeated calls with same hook name - O(1) after first resolution.
     */
    public MethodHandle resolveHookHandle(String hookName, Object[] args, Object compiledInstance) {
        if (hookName == null || hookName.isEmpty() || compiledInstance == null)
            return null;

        Class<?> compiledClass = compiledInstance.getClass();

        // Clear cache if compiled class changed (recompilation)
        if (lastCompiledClass != compiledClass) {
            handleCache.clear();
            lastCompiledClass = compiledClass;
        }

        int argCount = args == null ? 0 : args.length;

        // Fast path: check primary cache by hookName only
        CachedHandle cached = handleCache.get(hookName);
        if (cached != null) {
            // Verify arg count matches (handles most cases without full signature check)
            if (cached.paramCount == argCount) {
                return cached.handle;
            }
            // Arg count mismatch - need full resolution for potential overload
        }

        // Slow path: resolve method and cache
        Method method = resolveMethod(compiledClass, hookName, args);
        if (method == null)
            return null;

        try {
            MethodHandle handle = LOOKUP.unreflect(method);
            handleCache.put(hookName, new CachedHandle(handle, method.getParameterCount()));
            return handle;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    /**
     * Find the best matching method on the compiled class.
     */
    private Method resolveMethod(Class<?> targetClass, String hookName, Object[] args) {
        Object[] safeArgs = args == null ? new Object[0] : args;

        Method best = null;
        int bestScore = Integer.MAX_VALUE;

        for (Method method : targetClass.getMethods()) {
            if (!method.getName().equals(hookName))
                continue;

            // Skip final methods and Object methods
            if (Modifier.isFinal(method.getModifiers()))
                continue;
            if (method.getDeclaringClass() == Object.class)
                continue;

            Class<?>[] paramTypes = method.getParameterTypes();

            // Try exact arg count match first
            if (paramTypes.length == safeArgs.length) {
                int score = scoreMethodMatch(paramTypes, safeArgs);
                if (score != Integer.MAX_VALUE && score < bestScore) {
                    bestScore = score;
                    best = method;
                    if (score == 0)
                        return best; // Perfect match
                }
            }
        }

        // If no match with args, try no-arg version
        if (best == null && safeArgs.length > 0) {
            for (Method method : targetClass.getMethods()) {
                if (!method.getName().equals(hookName))
                    continue;
                if (Modifier.isFinal(method.getModifiers()))
                    continue;
                if (method.getDeclaringClass() == Object.class)
                    continue;
                if (method.getParameterCount() == 0) {
                    return method;
                }
            }
        }

        return best;
    }

    /**
     * Score how well parameter types match runtime arguments.
     * Lower is better; MAX_VALUE means incompatible.
     */
    private static int scoreMethodMatch(Class<?>[] paramTypes, Object[] args) {
        int score = 0;
        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> paramType = paramTypes[i];
            Object arg = args[i];

            if (arg == null) {
                if (paramType.isPrimitive())
                    return Integer.MAX_VALUE;
                score += 1;
                continue;
            }

            Class<?> argType = arg.getClass();
            Class<?> boxedParam = boxType(paramType);

            if (boxedParam.equals(argType))
                continue;

            if (boxedParam.isAssignableFrom(argType)) {
                score += inheritanceDistance(argType, boxedParam);
                continue;
            }

            return Integer.MAX_VALUE;
        }
        return score;
    }

    private static Class<?> boxType(Class<?> type) {
        if (!type.isPrimitive())
            return type;
        if (type == boolean.class) return Boolean.class;
        if (type == byte.class) return Byte.class;
        if (type == short.class) return Short.class;
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == float.class) return Float.class;
        if (type == double.class) return Double.class;
        if (type == char.class) return Character.class;
        return type;
    }

    private static int inheritanceDistance(Class<?> child, Class<?> parent) {
        if (child.equals(parent))
            return 0;
        if (!parent.isAssignableFrom(child))
            return 1000;

        int distance = 0;
        Class<?> current = child;
        while (current != null && !current.equals(parent)) {
            current = current.getSuperclass();
            distance++;
        }
        return distance;
    }
}
