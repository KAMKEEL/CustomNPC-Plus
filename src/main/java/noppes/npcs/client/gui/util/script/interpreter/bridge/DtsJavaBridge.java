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

public final class DtsJavaBridge {

    private DtsJavaBridge() {}

    public static JSMethodInfo findMatchingMethod(Method method, TypeInfo containingType) {
        JSTypeInfo jsType = findJSTypeInfo(method, containingType);
        if (jsType == null) return null;

        List<JSMethodInfo> overloads = jsType.getMethodOverloads(method.getName());
        if (overloads.isEmpty()) return null;

        Class<?>[] paramTypes = method.getParameterTypes();
        JSMethodInfo best = null;
        int bestScore = -1;

        for (JSMethodInfo candidate : overloads) {
            if (candidate.getParameterCount() != paramTypes.length) continue;
            int score = scoreOverload(candidate, paramTypes, containingType);
            if (score > bestScore) {
                bestScore = score;
                best = candidate;
            }
        }

        return bestScore >= 0 ? best : null;
    }

    public static JSFieldInfo findMatchingField(Field field, TypeInfo containingType) {
        JSTypeInfo jsType = findJSTypeInfo(field, containingType);
        if (jsType == null) return null;
        return jsType.getField(field.getName());
    }

    private static int scoreOverload(JSMethodInfo method, Class<?>[] paramTypes, TypeInfo containingType) {
        int score = 0;
        List<JSMethodInfo.JSParameterInfo> jsParams = method.getParameters();
        for (int i = 0; i < paramTypes.length; i++) {
            int paramScore = scoreParam(paramTypes[i], jsParams.get(i), containingType);
            if (paramScore < 0) return -1;
            score += paramScore;
        }
        return score;
    }

    private static int scoreParam(Class<?> javaParam, JSMethodInfo.JSParameterInfo jsParam, TypeInfo containingType) {
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
        return resolveJSTypeInfo(javaClass != null ? javaClass.getName() : null);
    }

    private static JSTypeInfo findJSTypeInfo(Field field, TypeInfo containingType) {
        if (containingType != null && containingType.isJSType()) {
            return containingType.getJSTypeInfo();
        }

        Class<?> javaClass = containingType != null ? containingType.getJavaClass() : field.getDeclaringClass();
        return resolveJSTypeInfo(javaClass != null ? javaClass.getName() : null);
    }

    private static JSTypeInfo resolveJSTypeInfo(String javaFqnOrType) {
        if (javaFqnOrType == null || javaFqnOrType.isEmpty()) return null;
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
