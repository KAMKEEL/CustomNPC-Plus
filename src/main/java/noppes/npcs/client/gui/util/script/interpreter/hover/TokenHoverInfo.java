package noppes.npcs.client.gui.util.script.interpreter.hover;

import noppes.npcs.client.gui.util.script.interpreter.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Data class containing all displayable information for a token hover tooltip.
 * Extracts and formats information from Token metadata for rendering.
 * 
 * Supports display for:
 * - Classes/Interfaces/Enums: Package, declaration with modifiers/extends/implements
 * - Methods: Return type, signature, parameters, Javadoc
 * - Fields: Type, containing class, declaration
 * - Variables: Type, scope (local/parameter/global)
 * - Errors: Validation errors, type mismatches
 */
public class TokenHoverInfo {

    // ==================== DISPLAY SECTIONS ====================
    
    /** Package name (e.g., "net.minecraft.client") - shown in gray */
    private String packageName;
    
    /** Icon indicator (e.g., "C" for class, "I" for interface, "m" for method) */
    private String iconIndicator;
    
    /** Primary declaration line with syntax coloring */
    private List<TextSegment> declaration = new ArrayList<>();
    
    /** Javadoc/documentation comment lines */
    private List<String> documentation = new ArrayList<>();
    
    /** Error messages (shown in red) */
    private List<String> errors = new ArrayList<>();
    
    /** Additional info lines (e.g., "Variable 'x' is never used") */
    private List<String> additionalInfo = new ArrayList<>();
    
    /** The token this info was built from */
    private final Token token;

    // ==================== TEXT SEGMENT ====================
    
    /**
     * A colored segment of text within the declaration line.
     */
    public static class TextSegment {
        public final String text;
        public final int color;
        
        public TextSegment(String text, int color) {
            this.text = text;
            this.color = color;
        }
        
        // Predefined colors matching IntelliJ dark theme
        public static final int COLOR_KEYWORD = 0xCC7832;     // Orange for keywords/modifiers
        public static final int COLOR_TYPE = 0x6897BB;        // Blue for types
        public static final int COLOR_CLASS = 0xA9B7C6;       // Light gray for class names
        public static final int COLOR_METHOD = 0xFFC66D;      // Yellow for method names
        public static final int COLOR_FIELD = 0x9876AA;       // Purple for fields
        public static final int COLOR_PARAM = 0xA9B7C6;       // Light gray for parameters
        public static final int COLOR_PACKAGE = 0x808080;     // Gray for package
        public static final int COLOR_DEFAULT = 0xA9B7C6;     // Default text
        public static final int COLOR_ERROR = 0xFF6B68;       // Red for errors
        public static final int COLOR_STRING = 0x6A8759;      // Green for strings
        public static final int COLOR_ANNOTATION = 0xBBB529;  // Yellow-green for annotations
    }

    // ==================== CONSTRUCTOR ====================

    private TokenHoverInfo(Token token) {
        this.token = token;
    }

    // ==================== FACTORY METHOD ====================

    /**
     * Build hover info from a token based on its type and metadata.
     */
    public static TokenHoverInfo fromToken(Token token) {
        if (token == null) return null;
        
        TokenHoverInfo info = new TokenHoverInfo(token);
        
        // First, check for errors
        info.extractErrors(token);
        
        // Then, extract type-specific information
        switch (token.getType()) {
            case IMPORTED_CLASS:
            case CLASS_DECL:
            case INTERFACE_DECL:
            case ENUM_DECL:
            case TYPE_DECL:
            case NEW_TYPE:
                info.extractClassInfo(token);
                break;
                
            case METHOD_CALL:
                info.extractMethodCallInfo(token);
                break;
                
            case METHOD_DECL:
                info.extractMethodDeclInfo(token);
                break;
                
            case GLOBAL_FIELD:
                info.extractGlobalFieldInfo(token);
                break;
                
            case LOCAL_FIELD:
                info.extractLocalFieldInfo(token);
                break;
                
            case PARAMETER:
                info.extractParameterInfo(token);
                break;
                
            case UNDEFINED_VAR:
                info.extractUndefinedInfo(token);
                break;
                
            case KEYWORD:
            case MODIFIER:
            case STRING:
            case NUMBER:
            case COMMENT:
                // These don't need hover info typically
                return null;
                
            default:
                // For other types, try to extract any available metadata
                if (token.getTypeInfo() != null) {
                    info.extractClassInfo(token);
                } else if (token.getMethodInfo() != null) {
                    info.extractMethodDeclInfo(token);
                } else if (token.getFieldInfo() != null) {
                    info.extractFieldInfoGeneric(token);
                } else {
                    return null; // Nothing to show
                }
                break;
        }
        
        return info;
    }

    // ==================== EXTRACTION METHODS ====================

    private void extractErrors(Token token) {
        MethodCallInfo callInfo = token.getMethodCallInfo();
        if (callInfo != null) {
            if (callInfo.hasArgCountError()) {
                errors.add("Expected " + getExpectedArgCount(callInfo) + " argument(s) but found " + callInfo.getArguments().size());
            }
            if (callInfo.hasArgTypeError()) {
                for (MethodCallInfo.ArgumentTypeError error : callInfo.getArgumentTypeErrors()) {
                    errors.add(error.getMessage());
                }
            }
            if (callInfo.hasReturnTypeMismatch()) {
                errors.add(callInfo.getErrorMessage());
            }
            if (callInfo.hasStaticAccessError()) {
                errors.add(callInfo.getErrorMessage());
            }
        }
        
        FieldInfo fieldInfo = token.getFieldInfo();
        if (fieldInfo != null && !fieldInfo.isResolved()) {
            errors.add("Cannot resolve symbol '" + token.getText() + "'");
        }
    }
    
    private int getExpectedArgCount(MethodCallInfo callInfo) {
        MethodInfo method = callInfo.getResolvedMethod();
        if (method != null) {
            return method.getParameterCount();
        }
        return 0;
    }

    private void extractClassInfo(Token token) {
        TypeInfo typeInfo = token.getTypeInfo();
        if (typeInfo == null) return;
        
        packageName = typeInfo.getPackageName();
        
        Class<?> clazz = typeInfo.getJavaClass();
        if (clazz != null) {
            // Icon
            if (clazz.isInterface()) {
                iconIndicator = "I";
            } else if (clazz.isEnum()) {
                iconIndicator = "E";
            } else {
                iconIndicator = "C";
            }
            
            // Build declaration
            int mods = clazz.getModifiers();
            
            // Modifiers
            if (Modifier.isPublic(mods)) addSegment("public ", TextSegment.COLOR_KEYWORD);
            if (Modifier.isAbstract(mods) && !clazz.isInterface()) addSegment("abstract ", TextSegment.COLOR_KEYWORD);
            if (Modifier.isFinal(mods)) addSegment("final ", TextSegment.COLOR_KEYWORD);
            
            // Class type keyword
            if (clazz.isInterface()) {
                addSegment("interface ", TextSegment.COLOR_KEYWORD);
            } else if (clazz.isEnum()) {
                addSegment("enum ", TextSegment.COLOR_KEYWORD);
            } else {
                addSegment("class ", TextSegment.COLOR_KEYWORD);
            }
            
            // Class name
            addSegment(typeInfo.getSimpleName(), TextSegment.COLOR_CLASS);
            
            // Extends
            Class<?> superclass = clazz.getSuperclass();
            if (superclass != null && superclass != Object.class) {
                addSegment(" extends ", TextSegment.COLOR_KEYWORD);
                int superClassCol = TextSegment.COLOR_TYPE;
                if (superclass.isInterface()) {
                    superClassCol = TokenType.INTERFACE_DECL.getHexColor();
                } else if (superclass.isEnum()) {
                    superClassCol = TokenType.ENUM_DECL.getHexColor();
                } else {
                    superClassCol = TokenType.IMPORTED_CLASS.getHexColor();

                }
                addSegment(superclass.getSimpleName(), superClassCol);
            }

            List<TokenHoverInfo.TextSegment> declaration = null;

            // Implements
            Class<?>[] interfaces = clazz.getInterfaces();
            if (interfaces.length > 0) {
                addSegment(clazz.isInterface() ? " extends " : " implements ", TextSegment.COLOR_KEYWORD);
                for (int i = 0; i < Math.min(interfaces.length, 3); i++) {
                    if (i > 0) addSegment(", ", TextSegment.COLOR_DEFAULT);
                    addSegment(interfaces[i].getSimpleName(), TokenType.INTERFACE_DECL.getHexColor());
                }
                if (interfaces.length > 3) {
                    addSegment(", ...", TextSegment.COLOR_DEFAULT);
                }
            }
        } else {
            // Unresolved type
            iconIndicator = "?";
            addSegment(typeInfo.getSimpleName(), TextSegment.COLOR_CLASS);
            if (!typeInfo.isResolved()) {
                errors.add("Cannot resolve class '" + typeInfo.getSimpleName() + "'");
            }
        }
    }

    private void extractMethodCallInfo(Token token) {
        MethodCallInfo callInfo = token.getMethodCallInfo();
        MethodInfo methodInfo = callInfo != null ? callInfo.getResolvedMethod() : token.getMethodInfo();
        
        if (methodInfo == null && callInfo == null) return;
        
        iconIndicator = "m";
        
        TypeInfo containingType = null;
        if (callInfo != null) {
            containingType = callInfo.getReceiverType();
        }
        if (containingType == null && methodInfo != null) {
            containingType = methodInfo.getContainingType();
        }
        
        if (containingType != null) {
            // Show full package.ClassName for context (like IntelliJ)
            String pkg = containingType.getPackageName();
            String className = containingType.getSimpleName();
            if (pkg != null && !pkg.isEmpty()) {
                packageName = pkg + "." + className;
            } else {
                packageName = className;
            }
        }
        
        // Try to get actual Java method for more details
        if (containingType != null && containingType.getJavaClass() != null && methodInfo != null) {
            Class<?> clazz = containingType.getJavaClass();
            Method javaMethod = findJavaMethod(clazz, methodInfo.getName(), methodInfo.getParameterCount());
            
            if (javaMethod != null) {
                buildMethodDeclaration(javaMethod, containingType);
                extractJavadoc(javaMethod);
                return;
            }
        }
        
        // Fallback to basic method info
        if (methodInfo != null) {
            buildBasicMethodDeclaration(methodInfo, containingType);
        }
    }

    private void extractMethodDeclInfo(Token token) {
        MethodInfo methodInfo = token.getMethodInfo();
        if (methodInfo == null) return;
        
        iconIndicator = "m";
        
        // For script-defined methods, show basic declaration
        buildBasicMethodDeclaration(methodInfo, null);
    }

    private void extractGlobalFieldInfo(Token token) {
        FieldInfo fieldInfo = token.getFieldInfo();
        if (fieldInfo == null) return;
        
        iconIndicator = "f";
        
        TypeInfo declaredType = fieldInfo.getDeclaredType();
        if (declaredType != null) {
            // Show field's type package.ClassName for context
            String pkg = declaredType.getPackageName();
            String className = declaredType.getSimpleName();
            if (pkg != null && !pkg.isEmpty()) {
                packageName = pkg;
            }
            
            // Type
            addSegment(declaredType.getSimpleName(), TextSegment.COLOR_TYPE);
            addSegment(" ", TextSegment.COLOR_DEFAULT);
        }
        
        // Field name
        addSegment(fieldInfo.getName(), TextSegment.COLOR_FIELD);
    }

    private void extractLocalFieldInfo(Token token) {
        FieldInfo fieldInfo = token.getFieldInfo();
        if (fieldInfo == null) return;
        
        iconIndicator = "v";
        
        TypeInfo declaredType = fieldInfo.getDeclaredType();
        if (declaredType != null) {
            addSegment(declaredType.getSimpleName(), TextSegment.COLOR_TYPE);
            addSegment(" ", TextSegment.COLOR_DEFAULT);
        }
        
        addSegment(fieldInfo.getName(), TextSegment.COLOR_FIELD);
        
        // Show it's a local variable
        additionalInfo.add("Local variable");
    }

    private void extractParameterInfo(Token token) {
        FieldInfo fieldInfo = token.getFieldInfo();
        if (fieldInfo == null) return;
        
        iconIndicator = "p";
        
        TypeInfo declaredType = fieldInfo.getDeclaredType();
        if (declaredType != null) {
            addSegment(declaredType.getSimpleName(), TextSegment.COLOR_TYPE);
            addSegment(" ", TextSegment.COLOR_DEFAULT);
        }
        
        addSegment(fieldInfo.getName(), TextSegment.COLOR_PARAM);
        
        additionalInfo.add("Parameter");
    }

    private void extractUndefinedInfo(Token token) {
        iconIndicator = "?";
        addSegment(token.getText(), TextSegment.COLOR_ERROR);
        errors.add("Cannot resolve symbol '" + token.getText() + "'");
    }

    private void extractFieldInfoGeneric(Token token) {
        FieldInfo fieldInfo = token.getFieldInfo();
        if (fieldInfo == null) return;
        
        switch (fieldInfo.getScope()) {
            case GLOBAL:
                extractGlobalFieldInfo(token);
                break;
            case LOCAL:
                extractLocalFieldInfo(token);
                break;
            case PARAMETER:
                extractParameterInfo(token);
                break;
        }
    }

    // ==================== HELPER METHODS ====================

    private void addSegment(String text, int color) {
        declaration.add(new TextSegment(text, color));
    }

    private Method findJavaMethod(Class<?> clazz, String name, int paramCount) {
        try {
            for (Method m : clazz.getMethods()) {
                if (m.getName().equals(name) && m.getParameterCount() == paramCount) {
                    return m;
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }

    private void buildMethodDeclaration(Method method, TypeInfo containingType) {
        int mods = method.getModifiers();
        
        // Annotations (show @Contract if present, etc.)
        // Skip for now - could add later
        
        // Modifiers
        if (Modifier.isPublic(mods)) addSegment("public ", TextSegment.COLOR_KEYWORD);
        else if (Modifier.isProtected(mods)) addSegment("protected ", TextSegment.COLOR_KEYWORD);
        else if (Modifier.isPrivate(mods)) addSegment("private ", TextSegment.COLOR_KEYWORD);
        
        if (Modifier.isStatic(mods)) addSegment("static ", TextSegment.COLOR_KEYWORD);
        if (Modifier.isFinal(mods)) addSegment("final ", TextSegment.COLOR_KEYWORD);
        if (Modifier.isAbstract(mods)) addSegment("abstract ", TextSegment.COLOR_KEYWORD);
        if (Modifier.isSynchronized(mods)) addSegment("synchronized ", TextSegment.COLOR_KEYWORD);
        
        // Return type
        Class<?> returnType = method.getReturnType();
        addSegment(returnType.getSimpleName(), TextSegment.COLOR_TYPE);
        addSegment(" ", TextSegment.COLOR_DEFAULT);
        
        // Method name
        addSegment(method.getName(), TextSegment.COLOR_METHOD);
        
        // Parameters
        addSegment("(", TextSegment.COLOR_DEFAULT);
        Class<?>[] paramTypes = method.getParameterTypes();
        java.lang.reflect.Parameter[] params = method.getParameters();
        for (int i = 0; i < paramTypes.length; i++) {
            if (i > 0) addSegment(", ", TextSegment.COLOR_DEFAULT);
            addSegment(paramTypes[i].getSimpleName(), TextSegment.COLOR_TYPE);
            addSegment(" ", TextSegment.COLOR_DEFAULT);
            // Try to get parameter name if available
            String paramName = params.length > i ? params[i].getName() : "arg" + i;
            addSegment(paramName, TextSegment.COLOR_PARAM);
        }
        addSegment(")", TextSegment.COLOR_DEFAULT);
    }

    private void buildBasicMethodDeclaration(MethodInfo methodInfo, TypeInfo containingType) {
        // Modifiers
        if (methodInfo.isStatic()) {
            addSegment("static ", TextSegment.COLOR_KEYWORD);
        }
        
        // Return type
        TypeInfo returnType = methodInfo.getReturnType();
        if (returnType != null) {
            addSegment(returnType.getSimpleName(), TextSegment.COLOR_TYPE);
            addSegment(" ", TextSegment.COLOR_DEFAULT);
        } else {
            addSegment("void ", TextSegment.COLOR_KEYWORD);
        }
        
        // Method name
        addSegment(methodInfo.getName(), TextSegment.COLOR_METHOD);
        
        // Parameters
        addSegment("(", TextSegment.COLOR_DEFAULT);
        List<FieldInfo> params = methodInfo.getParameters();
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) addSegment(", ", TextSegment.COLOR_DEFAULT);
            FieldInfo param = params.get(i);
            TypeInfo paramType = param.getDeclaredType();
            if (paramType != null) {
                addSegment(paramType.getSimpleName(), TextSegment.COLOR_TYPE);
                addSegment(" ", TextSegment.COLOR_DEFAULT);
            }
            addSegment(param.getName(), TextSegment.COLOR_PARAM);
        }
        addSegment(")", TextSegment.COLOR_DEFAULT);
    }

    private void extractJavadoc(Method method) {
        // Java reflection doesn't provide Javadoc at runtime
        // We could potentially load it from source files or external documentation
        // For now, we'll leave this as a placeholder for future enhancement
        
        // Check for @Deprecated annotation
        if (method.isAnnotationPresent(Deprecated.class)) {
            additionalInfo.add("@Deprecated");
        }
    }

    // ==================== GETTERS ====================

    public String getPackageName() { return packageName; }
    public String getIconIndicator() { return iconIndicator; }
    public List<TextSegment> getDeclaration() { return declaration; }
    public List<String> getDocumentation() { return documentation; }
    public List<String> getErrors() { return errors; }
    public List<String> getAdditionalInfo() { return additionalInfo; }
    public Token getToken() { return token; }
    
    public boolean hasContent() {
        return !declaration.isEmpty() || !errors.isEmpty() || !documentation.isEmpty();
    }
    
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
