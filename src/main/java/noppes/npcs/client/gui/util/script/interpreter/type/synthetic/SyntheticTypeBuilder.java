package noppes.npcs.client.gui.util.script.interpreter.type.synthetic;

import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;

import java.util.*;

/**
 * Builder for creating synthetic (non-reflection-based) types.
 * Used for built-in Nashorn objects like 'Java' that have no corresponding Java class.
 * 
 * <p>Usage:</p>
 * <pre>
 * SyntheticType javaType = new SyntheticTypeBuilder("Java")
 *     .addMethod("type")
 *         .parameter("className", "string")
 *         .returns("Class")
 *         .documentation("Loads a Java class by fully-qualified name.")
 *         .done()
 *     .build();
 * </pre>
 */
public class SyntheticTypeBuilder {
    
    private final String name;
    private String documentation;
    private final List<SyntheticMethod> methods = new ArrayList<>();
    private final List<SyntheticField> fields = new ArrayList<>();
    
    public SyntheticTypeBuilder(String name) {
        this.name = name;
    }
    
    public SyntheticTypeBuilder documentation(String doc) {
        this.documentation = doc;
        return this;
    }
    
    /**
     * Start building a method for this type.
     */
    public MethodBuilder addMethod(String methodName) {
        return new MethodBuilder(this, methodName);
    }
    
    /**
     * Add a field to this type.
     */
    public SyntheticTypeBuilder addField(String fieldName, String typeName, String doc) {
        fields.add(new SyntheticField(fieldName, typeName, doc, false));
        return this;
    }
    
    /**
     * Add a static field to this type.
     */
    public SyntheticTypeBuilder addStaticField(String fieldName, String typeName, String doc) {
        fields.add(new SyntheticField(fieldName, typeName, doc, true));
        return this;
    }
    
    void addBuiltMethod(SyntheticMethod method) {
        methods.add(method);
    }
    
    /**
     * Build the synthetic type.
     */
    public SyntheticType build() {
        return new SyntheticType(name, documentation, methods, fields);
    }
    
    // ==================== Inner classes ====================
    
    /**
     * Builder for methods within a synthetic type.
     */
    public static class MethodBuilder {
        private final SyntheticTypeBuilder parent;
        private final String name;
        private final List<SyntheticParameter> parameters = new ArrayList<>();
        private String returnType = "void";
        private String documentation;
        private boolean isStatic = false;
        private ReturnTypeResolver returnTypeResolver;
        
        MethodBuilder(SyntheticTypeBuilder parent, String name) {
            this.parent = parent;
            this.name = name;
        }
        
        /**
         * Add a parameter to this method.
         */
        public MethodBuilder parameter(String paramName, String typeName) {
            parameters.add(new SyntheticParameter(paramName, typeName, null));
            return this;
        }
        
        /**
         * Add a parameter with documentation.
         */
        public MethodBuilder parameter(String paramName, String typeName, String doc) {
            parameters.add(new SyntheticParameter(paramName, typeName, doc));
            return this;
        }
        
        /**
         * Set the return type.
         */
        public MethodBuilder returns(String typeName) {
            this.returnType = typeName;
            return this;
        }
        
        /**
         * Set a dynamic return type resolver.
         * Used for methods like Java.type() where return type depends on arguments.
         */
        public MethodBuilder returnsResolved(ReturnTypeResolver resolver) {
            this.returnTypeResolver = resolver;
            return this;
        }
        
        /**
         * Set method documentation.
         */
        public MethodBuilder documentation(String doc) {
            this.documentation = doc;
            return this;
        }
        
        /**
         * Mark this method as static.
         */
        public MethodBuilder asStatic() {
            this.isStatic = true;
            return this;
        }
        
        /**
         * Finish building this method and return to the type builder.
         */
        public SyntheticTypeBuilder done() {
            parent.addBuiltMethod(new SyntheticMethod(name, returnType, parameters, documentation, isStatic, returnTypeResolver));
            return parent;
        }
    }
    
    /**
     * Functional interface for resolving return types dynamically.
     */
    @FunctionalInterface
    public interface ReturnTypeResolver {
        /**
         * Resolve the return type given the arguments passed to the method.
         * @param arguments The string arguments (for methods like Java.type("className"))
         * @return The resolved TypeInfo, or null if cannot be resolved
         */
        TypeInfo resolve(String[] arguments);
    }
    
    // ==================== Data classes ====================

    // ==================== SyntheticType (the result) ====================
}
