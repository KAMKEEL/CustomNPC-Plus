package noppes.npcs.client.gui.util.script.interpreter.type;

import noppes.npcs.client.gui.util.script.interpreter.field.FieldInfo;
import noppes.npcs.client.gui.util.script.interpreter.method.MethodInfo;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents an intersection type from multiple bounds on a type parameter.
 * For example, {@code <T extends Number & Comparable<T>>} creates an intersection
 * of Number and Comparable.
 *
 * <p>The primary bound determines the display name and main type identity.
 * Member lookup (hasMethod, getMethodInfo, hasField, getFieldInfo, getAllMethodOverloads)
 * delegates to ALL constituent types, so methods from any bound are accessible.</p>
 *
 * <p>This is used by {@link GenericContext} when building bound fallbacks for type
 * parameters with additional interface bounds.</p>
 */
public class IntersectionTypeInfo extends TypeInfo {

    /** The primary (first) bound type — used for display and as the "main" type. */
    private final TypeInfo primaryBound;

    /** Additional interface bounds (everything after {@code &}). */
    private final List<TypeInfo> additionalBounds;

    private IntersectionTypeInfo(TypeInfo primaryBound, List<TypeInfo> additionalBounds) {
        super(primaryBound.getSimpleName(), primaryBound.getFullName(), "",
                primaryBound.getKind(), primaryBound.getJavaClass(), true, null, true);
        this.primaryBound = primaryBound;
        this.additionalBounds = additionalBounds;
    }

    /**
     * Create an intersection type from a primary bound and additional interface bounds.
     * If there are no additional bounds, returns the primary bound directly (no wrapper).
     *
     * @param primary    the primary (first) bound type
     * @param additional the additional interface bounds (may be empty)
     * @return an IntersectionTypeInfo if there are additional bounds, otherwise the primary itself
     */
    public static TypeInfo of(TypeInfo primary, List<TypeInfo> additional) {
        if (additional == null || additional.isEmpty()) {
            return primary;
        }
        return new IntersectionTypeInfo(primary, new ArrayList<>(additional));
    }

    @Override
    public boolean hasMethod(String methodName) {
        if (primaryBound.hasMethod(methodName)) return true;
        for (TypeInfo bound : additionalBounds) {
            if (bound.hasMethod(methodName)) return true;
        }
        return false;
    }

    @Override
    public boolean hasMethod(String methodName, int paramCount) {
        if (primaryBound.hasMethod(methodName, paramCount)) return true;
        for (TypeInfo bound : additionalBounds) {
            if (bound.hasMethod(methodName, paramCount)) return true;
        }
        return false;
    }

    @Override
    public MethodInfo getMethodInfo(String methodName) {
        MethodInfo m = primaryBound.getMethodInfo(methodName);
        if (m != null) return m;
        for (TypeInfo bound : additionalBounds) {
            m = bound.getMethodInfo(methodName);
            if (m != null) return m;
        }
        return null;
    }

    @Override
    public List<MethodInfo> getAllMethodOverloads(String methodName) {
        Set<MethodInfo> seen = new LinkedHashSet<>();
        seen.addAll(primaryBound.getAllMethodOverloads(methodName));
        for (TypeInfo bound : additionalBounds) {
            seen.addAll(bound.getAllMethodOverloads(methodName));
        }
        return new ArrayList<>(seen);
    }

    @Override
    public MethodInfo getBestMethodOverload(String methodName, TypeInfo[] argTypes) {
        MethodInfo m = primaryBound.getBestMethodOverload(methodName, argTypes);
        if (m != null) return m;
        for (TypeInfo bound : additionalBounds) {
            m = bound.getBestMethodOverload(methodName, argTypes);
            if (m != null) return m;
        }
        return null;
    }

    @Override
    public MethodInfo getBestMethodOverload(String methodName, TypeInfo expectedReturnType) {
        MethodInfo m = primaryBound.getBestMethodOverload(methodName, expectedReturnType);
        if (m != null) return m;
        for (TypeInfo bound : additionalBounds) {
            m = bound.getBestMethodOverload(methodName, expectedReturnType);
            if (m != null) return m;
        }
        return null;
    }

    @Override
    public boolean hasField(String fieldName) {
        if (primaryBound.hasField(fieldName)) return true;
        for (TypeInfo bound : additionalBounds) {
            if (bound.hasField(fieldName)) return true;
        }
        return false;
    }

    @Override
    public FieldInfo getFieldInfo(String fieldName) {
        FieldInfo f = primaryBound.getFieldInfo(fieldName);
        if (f != null) return f;
        for (TypeInfo bound : additionalBounds) {
            f = bound.getFieldInfo(fieldName);
            if (f != null) return f;
        }
        return null;
    }

    public TypeInfo getPrimaryBound() {
        return primaryBound;
    }

    public List<TypeInfo> getAdditionalBounds() {
        return new ArrayList<>(additionalBounds);
    }

    @Override
    public String getDisplayName() {
        StringBuilder sb = new StringBuilder(primaryBound.getDisplayName());
        for (TypeInfo bound : additionalBounds) {
            sb.append(" & ").append(bound.getDisplayName());
        }
        return sb.toString();
    }

    @Override
    public boolean isResolved() {
        return true;
    }

    @Override
    public String toString() {
        return "IntersectionTypeInfo{" + getDisplayName() + "}";
    }
}
