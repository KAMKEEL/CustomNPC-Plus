package noppes.npcs.client.gui.util.script.interpreter.bridge;

import noppes.npcs.client.gui.util.script.interpreter.js_parser.JSFieldInfo;
import noppes.npcs.client.gui.util.script.interpreter.js_parser.JSMethodInfo;
import noppes.npcs.client.gui.util.script.interpreter.js_parser.JSTypeInfo;
import noppes.npcs.client.gui.util.script.interpreter.js_parser.JSTypeRegistry;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeResolver;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class DtsJavaBridge {

    /** Cache of reflection Method -> matching JS method info (for docs/param names). */
    private static final Map<Method, JSMethodInfo> METHOD_CACHE = new ConcurrentHashMap<>();
    /** Cache of reflection Field -> matching JS field info (for docs/typing). */
    private static final Map<Field, JSFieldInfo> FIELD_CACHE = new ConcurrentHashMap<>();
    /** Cache of reflection Method -> resolved Java TypeInfo override from .d.ts. */
    private static final Map<Method, TypeInfo> RETURN_OVERRIDE_CACHE = new ConcurrentHashMap<>();
    /** Negative cache for methods with no usable override. */
    private static final Set<Method> RETURN_OVERRIDE_MISSES = ConcurrentHashMap.newKeySet();

    private DtsJavaBridge() {}

    public static void clearCache() {
        METHOD_CACHE.clear();
        FIELD_CACHE.clear();
        RETURN_OVERRIDE_CACHE.clear();
        RETURN_OVERRIDE_MISSES.clear();
    }

    /**
     * Resolve a Java return type override for a reflected method using its .d.ts twin.
     *
     * The override is used only for editor typing (e.g., Janino hover/autocomplete) and
     * never changes runtime reflection. This returns a Java-resolved TypeInfo when the
     * .d.ts return type can be mapped to a concrete Java class.
     */
    public static TypeInfo resolveReturnTypeOverride(Method method, TypeInfo containingType, JSMethodInfo jsMethod) {
        if (method == null || jsMethod == null) return null;

        TypeInfo cached = RETURN_OVERRIDE_CACHE.get(method);
        if (cached != null) return cached;
        if (RETURN_OVERRIDE_MISSES.contains(method)) return null;

        String jsReturnType = jsMethod.getReturnType();
        if (jsReturnType == null || jsReturnType.isEmpty()) {
            RETURN_OVERRIDE_MISSES.add(method);
            return null;
        }

        String normalizedJsReturn = jsReturnType;
        if (normalizedJsReturn.contains("|")) {
            normalizedJsReturn = normalizedJsReturn.split("\\|")[0].trim();
        }
        normalizedJsReturn = normalizedJsReturn.replace("?", "").trim();

        // Fast-path: skip resolver if the .d.ts return matches reflection (common case).
        Class<?> reflectedReturn = method.getReturnType();
        if (reflectedReturn != null) {
            if (normalizedJsReturn.equals(reflectedReturn.getName())
                    || normalizedJsReturn.equals(reflectedReturn.getSimpleName())
                    || normalizedJsReturn.equals("Java." + reflectedReturn.getName())) {
                RETURN_OVERRIDE_MISSES.add(method);
                return null;
            }

            String lower = normalizedJsReturn.toLowerCase();
            if (("void".equals(lower) && (reflectedReturn == void.class || reflectedReturn == Void.class))
                    || ("boolean".equals(lower) && (reflectedReturn == boolean.class || reflectedReturn == Boolean.class))
                    || ("string".equals(lower) && reflectedReturn == String.class)
                    || ("number".equals(lower) && (reflectedReturn == double.class || reflectedReturn == Double.class))) {
                RETURN_OVERRIDE_MISSES.add(method);
                return null;
            }
        }

        TypeResolver resolver = TypeResolver.getInstance();
        TypeInfo resolved = resolver.resolveJSType(normalizedJsReturn);
        TypeInfo javaResolved = null;

        if (resolved != null) {
            if (resolved.getJavaClass() != null) {
                javaResolved = resolved;
            } else if (resolved.isJSType() && resolved.getJSTypeInfo() != null) {
                String javaFqn = resolved.getJSTypeInfo().getJavaFqn();
                if (javaFqn != null && !javaFqn.isEmpty()) {
                    javaResolved = resolver.resolveFullName(javaFqn);
                }
            }
        }

        if (javaResolved == null || javaResolved.getJavaClass() == null) {
            RETURN_OVERRIDE_MISSES.add(method);
            return null;
        }

        RETURN_OVERRIDE_CACHE.put(method, javaResolved);
        return javaResolved;
    }

    public static JSMethodInfo findMatchingMethod(Method method, TypeInfo containingType) {
        JSMethodInfo cached = METHOD_CACHE.get(method);
        if (cached != null) return cached;

        JSTypeInfo jsType = findJSTypeInfo(method, containingType);
        if (jsType == null) return null;

        List<JSMethodInfo> overloads = jsType.getMethodOverloads(method.getName());
        if (overloads.isEmpty()) return null;

        Class<?>[] paramTypes = method.getParameterTypes();
        JSMethodInfo best = null;
        int bestScore = -1;

        for (JSMethodInfo candidate : overloads) {
            if (candidate.getParameterCount() != paramTypes.length) continue;
            int score = scoreOverload(candidate, paramTypes, containingType, method.isVarArgs());
            if (score > bestScore) {
                bestScore = score;
                best = candidate;
            }
        }

        if (bestScore >= 0 && best != null) {
            METHOD_CACHE.put(method, best);
            return best;
        }
        return null;
    }

    public static JSFieldInfo findMatchingField(Field field, TypeInfo containingType) {
        JSFieldInfo cached = FIELD_CACHE.get(field);
        if (cached != null) return cached;

        JSTypeInfo jsType = findJSTypeInfo(field, containingType);
        if (jsType == null) return null;
        JSFieldInfo match = jsType.getField(field.getName());
        if (match != null) {
            FIELD_CACHE.put(field, match);
        }
        return match;
    }

    private static int scoreOverload(JSMethodInfo method, Class<?>[] paramTypes, TypeInfo containingType, boolean isVarArgs) {
        int score = 0;
        List<JSMethodInfo.JSParameterInfo> jsParams = method.getParameters();
        for (int i = 0; i < paramTypes.length; i++) {
            int paramScore = scoreParam(paramTypes[i], jsParams.get(i), containingType, isVarArgs, i == paramTypes.length - 1);
            if (paramScore < 0) return -1;
            score += paramScore;
        }
        return score;
    }

    private static int scoreParam(Class<?> javaParam, JSMethodInfo.JSParameterInfo jsParam, TypeInfo containingType, boolean isVarArgs, boolean isLastParam) {
        if (javaParam == null || jsParam == null) return -1;

        TypeInfo resolved = jsParam.getResolvedType(containingType);
        if (resolved != null && resolved.getJavaClass() != null) {
            Class<?> jsClass = resolved.getJavaClass();
            if (javaParam.equals(jsClass)) return 4;
            if (jsClass.isAssignableFrom(javaParam)) return 3;
            if (javaParam.isAssignableFrom(jsClass)) return 2;
        }

        String jsTypeName = jsParam.getType();
        if (jsTypeName == null || jsTypeName.isEmpty()) return -1;

        if (isVarArgs && isLastParam && javaParam.isArray()) {
            if (matchesArrayElement(javaParam.getComponentType(), jsTypeName, containingType)) {
                return 2;
            }
        }

        if (matchesPrimitive(jsTypeName, javaParam)) {
            return 3;
        }

        if (javaParam.isArray() && jsTypeName.endsWith("[]")) {
            String elementType = jsTypeName.substring(0, jsTypeName.length() - 2);
            return matchesArrayElement(javaParam.getComponentType(), elementType, containingType) ? 2 : -1;
        }

        JSTypeInfo jsType = resolveJSTypeInfo(jsTypeName);
        if (jsType != null && jsType.getJavaFqn() != null) {
            String javaFqn = normalizeJavaFqn(jsType.getJavaFqn());
            String paramFqn = normalizeJavaFqn(javaParam.getName());
            if (javaFqn.equals(paramFqn)) return 3;
        }

        if (jsTypeName.equals(javaParam.getSimpleName())) return 1;
        if (jsTypeName.equals(javaParam.getName())) return 1;
        if ("any".equals(jsTypeName)) return 1;
        return -1;
    }

    private static boolean matchesArrayElement(Class<?> elementClass, String jsElementType, TypeInfo containingType) {
        if (elementClass == null) return false;
        if (matchesPrimitive(jsElementType, elementClass)) return true;

        JSTypeInfo jsType = resolveJSTypeInfo(jsElementType);
        if (jsType != null && jsType.getJavaFqn() != null) {
            String javaFqn = normalizeJavaFqn(jsType.getJavaFqn());
            String elementFqn = normalizeJavaFqn(elementClass.getName());
            return javaFqn.equals(elementFqn);
        }

        return jsElementType.equals(elementClass.getSimpleName()) || jsElementType.equals(elementClass.getName());
    }

    private static boolean matchesPrimitive(String jsTypeName, Class<?> javaParam) {
        switch (jsTypeName) {
            case "string":
                return javaParam == String.class || javaParam == char.class || javaParam == Character.class;
            case "boolean":
                return javaParam == boolean.class || javaParam == Boolean.class;
            case "number":
                return isNumberType(javaParam);
            case "void":
                return javaParam == void.class || javaParam == Void.class;
            default:
                return false;
        }
    }

    private static boolean isNumberType(Class<?> type) {
        if (type == null) return false;
        return type == byte.class || type == short.class || type == int.class || type == long.class
            || type == float.class || type == double.class
            || Number.class.isAssignableFrom(type);
    }

    private static JSTypeInfo findJSTypeInfo(Method method, TypeInfo containingType) {
        if (containingType != null && containingType.isJSType()) {
            return containingType.getJSTypeInfo();
        }

        Class<?> javaClass = containingType != null ? containingType.getJavaClass() : method.getDeclaringClass();
        return resolveJSTypeInfo(javaClass);
    }

    private static JSTypeInfo findJSTypeInfo(Field field, TypeInfo containingType) {
        if (containingType != null && containingType.isJSType()) {
            return containingType.getJSTypeInfo();
        }

        Class<?> javaClass = containingType != null ? containingType.getJavaClass() : field.getDeclaringClass();
        return resolveJSTypeInfo(javaClass);
    }

    private static JSTypeInfo resolveJSTypeInfo(Class<?> javaClass) {
        if (javaClass == null) return null;

        JSTypeInfo direct = resolveJSTypeInfo(javaClass.getName());
        if (direct != null) return direct;

        for (Class<?> iface : javaClass.getInterfaces()) {
            JSTypeInfo ifaceType = resolveJSTypeInfo(iface.getName());
            if (ifaceType != null) return ifaceType;
        }

        Class<?> superClass = javaClass.getSuperclass();
        if (superClass != null && superClass != Object.class) {
            return resolveJSTypeInfo(superClass);
        }

        return null;
    }

    private static JSTypeInfo resolveJSTypeInfo(String javaFqnOrType) {
        if (javaFqnOrType == null || javaFqnOrType.isEmpty()) return null;
        if (javaFqnOrType.startsWith("Java.")) {
            javaFqnOrType = javaFqnOrType.substring(5);
        }
        JSTypeRegistry registry = TypeResolver.getInstance().getJSTypeRegistry();
        JSTypeInfo direct = registry.getTypeByJavaFqn(javaFqnOrType);
        if (direct != null) return direct;

        String normalized = normalizeJavaFqn(javaFqnOrType);
        if (!normalized.equals(javaFqnOrType)) {
            JSTypeInfo normalizedType = registry.getTypeByJavaFqn(normalized);
            if (normalizedType != null) return normalizedType;
        }

        return registry.getType(javaFqnOrType);
    }

    private static String normalizeJavaFqn(String javaFqn) {
        if (javaFqn == null) return null;
        return javaFqn.replace('$', '.');
    }
}
