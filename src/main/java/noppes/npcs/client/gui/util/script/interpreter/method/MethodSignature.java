package noppes.npcs.client.gui.util.script.interpreter.method;

import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a method signature for robust comparison.
 * Compares method name and parameter types as structured data, not as strings.
 */
public final class MethodSignature {
    private final String methodName;
    private final List<TypeInfo> parameterTypes;

    public MethodSignature(String methodName, List<TypeInfo> parameterTypes) {
        this.methodName = methodName;
        this.parameterTypes = new ArrayList<>(parameterTypes);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof MethodSignature))
            return false;

        MethodSignature other = (MethodSignature) obj;

        // Compare method names
        if (!this.methodName.equals(other.methodName))
            return false;

        // Compare parameter count
        if (this.parameterTypes.size() != other.parameterTypes.size())
            return false;

        // Compare each parameter type
        for (int i = 0; i < this.parameterTypes.size(); i++) {
            TypeInfo thisType = this.parameterTypes.get(i);
            TypeInfo otherType = other.parameterTypes.get(i);

            // Both null = equal
            if (thisType == null && otherType == null)
                continue;

            // One null, one not = not equal
            if (thisType == null || otherType == null)
                return false;

            // Compare full type names
            if (!thisType.getFullName().equals(otherType.getFullName()))
                return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = methodName.hashCode();
        for (TypeInfo paramType : parameterTypes) {
            result = 31 * result + (paramType != null ? paramType.getFullName().hashCode() : 0);
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(methodName);
        sb.append("(");
        for (int i = 0; i < parameterTypes.size(); i++) {
            if (i > 0)
                sb.append(", ");
            TypeInfo type = parameterTypes.get(i);
            sb.append(type != null ? type.getFullName() : "?");
        }
        sb.append(")");
        return sb.toString();
    }

    public static String asString(Method javaMethod) {
        StringBuilder sig = new StringBuilder(javaMethod.getName());
        sig.append("(");
        Class<?>[] paramTypes = javaMethod.getParameterTypes();
        for (int i = 0; i < paramTypes.length; i++) {
            if (i > 0)
                sig.append(", ");
            sig.append(paramTypes[i].getSimpleName());
        }
        sig.append(")");
        return sig.toString();
    }
}
