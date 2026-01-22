package noppes.npcs.janino;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Resolves script hook methods for compiled script classes using runtime
 * argument values. Resolution favors exact matches and then the "closest"
 * assignable parameter types (measured by inheritance distance).
 *
 * Methods are resolved dynamically from the compiled class - no interface
 * definitions required.
 */
public final class JaninoHookResolver {
    private final Class<?> interfaceType;

    /**
     * Cache for methods found on compiled classes (keyed by signature).
     */
    private final Map<String, Method> compiledClassCache = new HashMap<>();

    /**
     * Track which compiled class we've cached methods for.
     * If the class changes (recompilation), we clear the cache.
     */
    private Class<?> lastCompiledClass;

    // MethodHandle caches
    private final Map<String, MethodHandle> compiledHandleCache = new HashMap<>();

    // Method -> MethodHandle for identity-based deduplication
    private final Map<Method, MethodHandle> methodToHandle = new IdentityHashMap<>();

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    /**
     * Create a resolver for the provided script interface type.
     * @param interfaceType non-null interface class that scripts implement
     */
    public JaninoHookResolver(Class<?> interfaceType) {
        this.interfaceType = Objects.requireNonNull(interfaceType, "interfaceType");
    }

    /**
     * Clears internal resolution caches so future calls perform fresh
     * method resolution. Call when scripts or available methods change.
     */
    public void clearResolutionCaches() {
        compiledClassCache.clear();
        compiledHandleCache.clear();
        methodToHandle.clear();
        lastCompiledClass = null;
    }

    /**
     * Resolve a hook method by name, searching the compiled class.
     *
     * <p>Resolution order:</p>
     * <ol>
     *   <li>Compiled class methods (user-defined)</li>
     *   <li>Fallback to no-arg versions</li>
     * </ol>
     *
     * @param hookName hook method name
     * @param args runtime arguments to use for overload resolution
     * @param compiledInstance the compiled script instance
     * @return the resolved {@link Method} or {@code null} if none matched
     */
    public Method resolveHookMethod(String hookName, Object[] args, Object compiledInstance) {
        if (hookName == null || hookName.isEmpty() || compiledInstance == null)
            return null;

        Object[] safeArgs = args == null ? new Object[0] : args;
        String signatureKey = buildSignatureKey(hookName, safeArgs);

        Class<?> compiledClass = compiledInstance.getClass();

        // Clear cache if compiled class changed (recompilation)
        if (lastCompiledClass != compiledClass) {
            compiledClassCache.clear();
            compiledHandleCache.clear();
            methodToHandle.clear();
            lastCompiledClass = compiledClass;
        }

        // Check compiled class cache
        Method method = compiledClassCache.get(signatureKey);
        if (method != null)
            return method;

        // Search compiled class for matching method
        method = resolveHookMethodInternal(compiledClass, hookName, safeArgs);
        if (method != null) {
            compiledClassCache.put(signatureKey, method);
            return method;
        }

        // Try no-arg version on compiled class
        String noArgKey = buildSignatureKey(hookName, new Object[0]);
        method = compiledClassCache.get(noArgKey);
        if (method != null)
            return method;

        method = resolveHookMethodInternal(compiledClass, hookName, new Object[0]);
        if (method != null) {
            compiledClassCache.put(noArgKey, method);
            return method;
        }

        return null;
    }

    /**
     * Resolve a MethodHandle for the given hook. Faster than Method.invoke().
     * Uses existing Method resolution, then converts to cached MethodHandle.
     */
    public MethodHandle resolveHookHandle(String hookName, Object[] args, Object compiledInstance) {
        if (hookName == null || hookName.isEmpty() || compiledInstance == null)
            return null;

        Object[] safeArgs = args == null ? new Object[0] : args;
        String signatureKey = buildSignatureKey(hookName, safeArgs);

        Class<?> compiledClass = compiledInstance.getClass();

        // Clear cache if compiled class changed (recompilation)
        if (lastCompiledClass != compiledClass) {
            compiledClassCache.clear();
            compiledHandleCache.clear();
            methodToHandle.clear();
            lastCompiledClass = compiledClass;
        }

        // Check handle cache
        MethodHandle handle = compiledHandleCache.get(signatureKey);
        if (handle != null)
            return handle;

        // Resolve Method first
        Method method = resolveHookMethod(hookName, args, compiledInstance);
        if (method == null)
            return null;

        // Convert Method to MethodHandle (deduplicated by Method identity)
        handle = methodToHandle.get(method);
        if (handle == null) {
            try {
                handle = LOOKUP.unreflect(method);
                methodToHandle.put(method, handle);
            } catch (IllegalAccessException e) {
                return null;
            }
        }

        compiledHandleCache.put(signatureKey, handle);
        return handle;
    }

    /**
     * Build a cache key for a hook name and runtime argument list.
     */
    private static String buildSignatureKey(String hookName, Object[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append(hookName).append('#');

        if (args == null || args.length == 0)
            return sb.append('0').toString();

        sb.append(args.length).append(':');
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (i > 0)
                sb.append(',');

            sb.append(arg == null ? "null" : arg.getClass().getName());
        }
        return sb.toString();
    }

    /**
     * Internal resolution routine. Attempts to find the best matching method
     * on the provided class by comparing parameter counts and a simple
     * match scoring function.
     */
    private static Method resolveHookMethodInternal(Class<?> targetClass, String hookName, Object[] args) {
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
            if (paramTypes.length != safeArgs.length)
                continue;

            int score = scoreMethodMatch(paramTypes, safeArgs);
            if (score == Integer.MAX_VALUE)
                continue;

            if (score < bestScore) {
                bestScore = score;
                best = method;
                if (score == 0)
                    return best;
            }
        }

        return best;
    }

    /**
     * Score how well a method's parameter types match the provided runtime arguments.
     * Lower scores are better; Integer.MAX_VALUE indicates an incompatible parameter.
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
        if (type == boolean.class)
            return Boolean.class;
        if (type == byte.class)
            return Byte.class;
        if (type == short.class)
            return Short.class;
        if (type == int.class)
            return Integer.class;
        if (type == long.class)
            return Long.class;
        if (type == float.class)
            return Float.class;
        if (type == double.class)
            return Double.class;
        if (type == char.class)
            return Character.class;
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
