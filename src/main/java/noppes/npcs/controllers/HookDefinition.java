package noppes.npcs.controllers;

import cpw.mods.fml.common.eventhandler.Cancelable;
import noppes.npcs.api.handler.IHookDefinition;
import noppes.npcs.janino.annotations.ParamName;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Implementation of {@link IHookDefinition} with a fluent builder API.
 *
 * <h3>Usage Examples</h3>
 * <pre>{@code
 * // Full definition with class reference
 * HookDefinition.builder("onDBCTransform")
 *     .eventClass(IDBCEvent.TransformEvent.class)
 *     .paramNames("event")
 *     .requiredImports("com.dbc.api.event.IDBCEvent")
 *     .cancelable(true)
 *     .build();
 *
 * // String-based (avoids classloading issues)
 * HookDefinition.builder("onDBCPowerUp")
 *     .eventClass("com.dbc.api.event.IDBCEvent$PowerUpEvent")
 *     .requiredImports("com.dbc.api.event.IDBCEvent")
 *     .build();
 *
 * // Simple hook (backward compat, no metadata)
 * HookDefinition.simple("legacyHook");
 * }</pre>
 */
public class HookDefinition implements IHookDefinition {

    private final String hookName;
    private final String eventClassName;
    private final String[] paramNames;
    private final String[] requiredImports;
    private final boolean cancelable;

    // Cached resolved class (transient - not serialized)
    private transient Class<?> cachedEventClass;
    private transient boolean classResolutionAttempted;

    private HookDefinition(Builder builder) {
        this.hookName = builder.hookName;
        this.eventClassName = builder.eventClassName;
        this.paramNames = builder.paramNames != null ? builder.paramNames : new String[]{"event"};
        this.requiredImports = builder.requiredImports != null ? builder.requiredImports : new String[0];
        this.cancelable = builder.cancelable;
    }

    // ==================== IHookDefinition Implementation ====================

    @Override
    public String hookName() {
        return hookName;
    }

    @Override
    public String eventClassName() {
        return eventClassName;
    }

    @Override
    public String[] paramNames() {
        return paramNames;
    }

    @Override
    public String[] requiredImports() {
        return requiredImports;
    }

    @Override
    public boolean isCancelable() {
        return cancelable;
    }

    @Override
    public Class<?> getEventClass() {
        if (classResolutionAttempted) {
            return cachedEventClass;
        }

        classResolutionAttempted = true;
        if (eventClassName == null || eventClassName.isEmpty()) {
            return null;
        }

        try {
            cachedEventClass = Class.forName(eventClassName);
        } catch (ClassNotFoundException e) {
            cachedEventClass = null;
        }

        return cachedEventClass;
    }

    // ==================== Factory Methods ====================

    /**
     * Create a new builder for a hook definition.
     *
     * @param hookName The hook function name
     * @return A new builder instance
     */
    public static Builder builder(String hookName) {
        return new Builder(hookName);
    }

    /**
     * Create a simple hook definition with just a name (no metadata).
     * For backward compatibility with legacy hook registration.
     *
     * @param hookName The hook function name
     * @return A minimal hook definition
     */
    public static HookDefinition simple(String hookName) {
        return builder(hookName).build();
    }

    /**
     * Create a hook definition from an interface method.
     * Automatically extracts metadata from the method signature and annotations.
     *
     * @param hookName The hook name (may differ from method name)
     * @param method The method to extract metadata from
     * @return A hook definition with extracted metadata
     */
    public static HookDefinition fromMethod(String hookName, Method method) {
        Builder builder = builder(hookName);

        // Extract event type from first parameter
        if (method.getParameterCount() > 0) {
            Class<?> eventType = method.getParameterTypes()[0];
            builder.eventClass(eventType);

            // Extract @Cancelable annotation
            if (eventType.isAnnotationPresent(Cancelable.class)) {
                builder.cancelable(true);
            }

            // Build required imports from event type
            String importName = getImportForClass(eventType);
            if (importName != null) {
                builder.requiredImports(importName);
            }
        }

        // Extract parameter names from @ParamName annotations
        Parameter[] params = method.getParameters();
        if (params.length > 0) {
            String[] names = new String[params.length];
            for (int i = 0; i < params.length; i++) {
                ParamName annotation = params[i].getAnnotation(ParamName.class);
                if (annotation != null) {
                    names[i] = annotation.value();
                } else {
                    names[i] = params[i].getType().getSimpleName().toLowerCase();
                }
            }
            builder.paramNames(names);
        }

        return builder.build();
    }

    /**
     * Get the import statement needed for a class.
     * For nested classes, returns the enclosing class.
     */
    private static String getImportForClass(Class<?> clazz) {
        if (clazz == null || clazz.isPrimitive()) {
            return null;
        }

        // For nested classes, get the top-level enclosing class
        Class<?> enclosing = clazz;
        while (enclosing.getEnclosingClass() != null) {
            enclosing = enclosing.getEnclosingClass();
        }

        String name = enclosing.getName();
        // Skip java.lang classes
        if (name.startsWith("java.lang.")) {
            return null;
        }

        return name;
    }

    // ==================== Builder ====================

    /**
     * Builder for creating HookDefinition instances with a fluent API.
     */
    public static class Builder {
        private final String hookName;
        private String eventClassName;
        private String[] paramNames;
        private String[] requiredImports;
        private boolean cancelable;

        private Builder(String hookName) {
            if (hookName == null || hookName.isEmpty()) {
                throw new IllegalArgumentException("hookName cannot be null or empty");
            }
            this.hookName = hookName;
        }

        /**
         * Set the event class by name (avoids classloading issues).
         *
         * @param className Full qualified class name (use $ for nested classes)
         * @return this builder
         */
        public Builder eventClass(String className) {
            this.eventClassName = className;
            return this;
        }

        /**
         * Set the event class directly (convenience method).
         * The class name will be extracted automatically.
         *
         * @param clazz The event class
         * @return this builder
         */
        public Builder eventClass(Class<?> clazz) {
            if (clazz != null) {
                this.eventClassName = clazz.getName();
                // Auto-detect cancelable
                if (clazz.isAnnotationPresent(Cancelable.class)) {
                    this.cancelable = true;
                }
            }
            return this;
        }

        /**
         * Set parameter names for stub generation.
         *
         * @param names Parameter names
         * @return this builder
         */
        public Builder paramNames(String... names) {
            this.paramNames = names;
            return this;
        }

        /**
         * Set required imports for this hook's event type.
         *
         * @param imports Fully qualified class names to import
         * @return this builder
         */
        public Builder requiredImports(String... imports) {
            this.requiredImports = imports;
            return this;
        }

        /**
         * Set whether the event is cancelable.
         *
         * @param cancelable true if the event can be canceled
         * @return this builder
         */
        public Builder cancelable(boolean cancelable) {
            this.cancelable = cancelable;
            return this;
        }

        /**
         * Build the hook definition.
         *
         * @return A new HookDefinition instance
         */
        public HookDefinition build() {
            return new HookDefinition(this);
        }
    }

    // ==================== Object Methods ====================

    @Override
    public String toString() {
        return "HookDefinition{" +
            "hookName='" + hookName + '\'' +
            ", eventClassName='" + eventClassName + '\'' +
            ", cancelable=" + cancelable +
            '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof HookDefinition)) return false;
        HookDefinition other = (HookDefinition) obj;
        return hookName.equals(other.hookName);
    }

    @Override
    public int hashCode() {
        return hookName.hashCode();
    }
}
