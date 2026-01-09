package noppes.npcs.client.gui.util.script.interpreter.type;

import noppes.npcs.client.gui.util.script.interpreter.field.FieldInfo;
import noppes.npcs.client.gui.util.script.interpreter.jsdoc.JSDocInfo;
import noppes.npcs.client.gui.util.script.interpreter.jsdoc.JSDocParamTag;
import noppes.npcs.client.gui.util.script.interpreter.jsdoc.JSDocReturnTag;
import noppes.npcs.client.gui.util.script.interpreter.method.MethodInfo;

import java.lang.reflect.Modifier;
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
    
    public static class SyntheticMethod {
        public final String name;
        public final String returnType;
        public final List<SyntheticParameter> parameters;
        public final String documentation;
        public final boolean isStatic;
        public final ReturnTypeResolver returnTypeResolver;
        
        SyntheticMethod(String name, String returnType, List<SyntheticParameter> parameters, 
                       String documentation, boolean isStatic, ReturnTypeResolver returnTypeResolver) {
            this.name = name;
            this.returnType = returnType;
            this.parameters = Collections.unmodifiableList(new ArrayList<>(parameters));
            this.documentation = documentation;
            this.isStatic = isStatic;
            this.returnTypeResolver = returnTypeResolver;
        }
        
        /**
         * Create a MethodInfo from this synthetic method.
         */
        public MethodInfo toMethodInfo(TypeInfo containingType) {
            List<FieldInfo> paramInfos = new ArrayList<>();
            for (SyntheticParameter param : parameters) {
                TypeInfo paramType = TypeResolver.getInstance().resolve(param.typeName);
                if (paramType == null) {
                    paramType = TypeInfo.unresolved(param.typeName, param.typeName);
                }
                paramInfos.add(FieldInfo.parameter(param.name, paramType, -1, null));
            }
            
            TypeInfo returnTypeInfo = TypeResolver.getInstance().resolve(returnType);
            if (returnTypeInfo == null) {
                returnTypeInfo = TypeInfo.unresolved(returnType, returnType);
            }
            
            int modifiers = Modifier.PUBLIC;
            if (isStatic) {
                modifiers |= Modifier.STATIC;
            }
            
            MethodInfo methodInfo = MethodInfo.external(name, returnTypeInfo, containingType, paramInfos, modifiers, null);
            
            // Create JSDocInfo from documentation
            if (documentation != null && !documentation.isEmpty()) {
                JSDocInfo jsDocInfo = createJSDocInfo(returnTypeInfo);
                methodInfo.setJSDocInfo(jsDocInfo);
            }
            
            return methodInfo;
        }
        
        /**
         * Create JSDocInfo from the documentation string.
         */
        private JSDocInfo createJSDocInfo(TypeInfo returnTypeInfo) {
            // Parse the documentation to extract description and separate sections
            String[] lines = documentation.split("\n");  // Split on actual newlines, not \\n
            StringBuilder descBuilder = new StringBuilder();
            
            // Extract description (everything before @param or @returns)
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("@param") || line.startsWith("@returns") || line.startsWith("@return")) {
                    break;
                }
                if (!line.isEmpty()) {
                    if (descBuilder.length() > 0) descBuilder.append("\n");  // Use actual newline
                    descBuilder.append(line);
                }
            }
            
            JSDocInfo jsDocInfo = new JSDocInfo(documentation, -1, -1);
            jsDocInfo.setDescription(descBuilder.toString());
            
            // Add @param tags for each parameter
            for (int i = 0; i < parameters.size(); i++) {
                SyntheticParameter param = parameters.get(i);
                TypeInfo paramType = TypeResolver.getInstance().resolve(param.typeName);
                if (paramType == null) {
                    paramType = TypeInfo.unresolved(param.typeName, param.typeName);
                }
                
                JSDocParamTag paramTag = JSDocParamTag.create(
                    -1, -1, -1,  // offsets
                    param.typeName, paramType, -1, -1,  // type info
                    param.name, -1, -1,  // param name
                    param.documentation  // description
                );
                jsDocInfo.addParamTag(paramTag);
            }
            
            // Add @returns tag - use simple name for display but full TypeInfo for resolution
            String displayTypeName = returnTypeInfo != null && returnTypeInfo.isResolved() 
                ? returnTypeInfo.getSimpleName() 
                : returnType;
            
            JSDocReturnTag returnTag = JSDocReturnTag.create(
                "returns", -1, -1, -1,  // offsets
                displayTypeName, returnTypeInfo, -1, -1,  // type name for display, TypeInfo for resolution
                null  // description extracted from main documentation
            );
            jsDocInfo.setReturnTag(returnTag);
            
            return jsDocInfo;
        }
        
        /**
         * Get signature string for display.
         */
        public String getSignature() {
            StringBuilder sb = new StringBuilder();
            sb.append(name).append("(");
            for (int i = 0; i < parameters.size(); i++) {
                if (i > 0) sb.append(", ");
                SyntheticParameter p = parameters.get(i);
                sb.append(p.name).append(": ").append(p.typeName);
            }
            sb.append("): ").append(returnType);
            return sb.toString();
        }
    }
    
    public static class SyntheticParameter {
        public final String name;
        public final String typeName;
        public final String documentation;
        
        SyntheticParameter(String name, String typeName, String documentation) {
            this.name = name;
            this.typeName = typeName;
            this.documentation = documentation;
        }
    }
    
    public static class SyntheticField {
        public final String name;
        public final String typeName;
        public final String documentation;
        public final boolean isStatic;
        
        SyntheticField(String name, String typeName, String documentation, boolean isStatic) {
            this.name = name;
            this.typeName = typeName;
            this.documentation = documentation;
            this.isStatic = isStatic;
        }
        
        public FieldInfo toFieldInfo() {
            TypeInfo type = TypeResolver.getInstance().resolve(typeName);
            if (type == null) {
                type = TypeInfo.unresolved(typeName, typeName);
            }
            int modifiers = Modifier.PUBLIC;
            if (isStatic) {
                modifiers |= Modifier.STATIC;
            }
            return FieldInfo.external(name, type, documentation, modifiers);
        }
    }
    
    // ==================== SyntheticType (the result) ====================
    
    /**
     * Represents a fully-built synthetic type.
     */
    public static class SyntheticType {
        private final String name;
        private final String documentation;
        private final Map<String, SyntheticMethod> methods;
        private final Map<String, SyntheticField> fields;
        private TypeInfo typeInfo;
        
        SyntheticType(String name, String documentation, List<SyntheticMethod> methods, List<SyntheticField> fields) {
            this.name = name;
            this.documentation = documentation;
            this.methods = new LinkedHashMap<>();
            for (SyntheticMethod m : methods) {
                this.methods.put(m.name, m);
            }
            this.fields = new LinkedHashMap<>();
            for (SyntheticField f : fields) {
                this.fields.put(f.name, f);
            }
        }
        
        public String getName() { return name; }
        public String getDocumentation() { return documentation; }
        
        public SyntheticMethod getMethod(String methodName) {
            return methods.get(methodName);
        }
        
        public Collection<SyntheticMethod> getMethods() {
            return methods.values();
        }
        
        public SyntheticField getField(String fieldName) {
            return fields.get(fieldName);
        }
        
        public Collection<SyntheticField> getFields() {
            return fields.values();
        }
        
        public boolean hasMethod(String methodName) {
            return methods.containsKey(methodName);
        }
        
        public boolean hasField(String fieldName) {
            return fields.containsKey(fieldName);
        }
        
        /**
         * Get or create the TypeInfo for this synthetic type.
         */
        public TypeInfo getTypeInfo() {
            if (typeInfo == null) {
                typeInfo = TypeInfo.resolved(name, name, "", TypeInfo.Kind.CLASS, null);
            }
            return typeInfo;
        }
        
        /**
         * Create a MethodInfo for a method in this type.
         */
        public MethodInfo getMethodInfo(String methodName) {
            SyntheticMethod method = methods.get(methodName);
            if (method == null) return null;
            return method.toMethodInfo(getTypeInfo());
        }
        
        /**
         * Resolve the return type of a method given arguments.
         * Used for special methods like Java.type().
         */
        public TypeInfo resolveMethodReturnType(String methodName, String[] arguments) {
            SyntheticMethod method = methods.get(methodName);
            if (method == null) return null;
            
            if (method.returnTypeResolver != null) {
                return method.returnTypeResolver.resolve(arguments);
            }
            
            // Fall back to static return type
            return TypeResolver.getInstance().resolve(method.returnType);
        }
    }
}
