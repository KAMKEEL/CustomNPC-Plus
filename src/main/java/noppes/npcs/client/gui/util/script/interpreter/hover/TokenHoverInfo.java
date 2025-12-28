package noppes.npcs.client.gui.util.script.interpreter.hover;

import noppes.npcs.client.gui.util.script.interpreter.*;
import noppes.npcs.client.gui.util.script.interpreter.field.AssignmentInfo;
import noppes.npcs.client.gui.util.script.interpreter.field.FieldAccessInfo;
import noppes.npcs.client.gui.util.script.interpreter.field.FieldInfo;
import noppes.npcs.client.gui.util.script.interpreter.method.MethodCallInfo;
import noppes.npcs.client.gui.util.script.interpreter.method.MethodInfo;
import noppes.npcs.client.gui.util.script.interpreter.token.Token;
import noppes.npcs.client.gui.util.script.interpreter.token.TokenType;
import noppes.npcs.client.gui.util.script.interpreter.type.ScriptTypeInfo;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeResolver;

import java.lang.reflect.Field;
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
                
            case LITERAL:
            case KEYWORD:
            case MODIFIER:
            case STRING:
            case COMMENT:
                if (info.hasErrors())
                    return info;
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
        // Check if this token is part of a method call argument (positional lookup)
        MethodCallInfo containingCall = findMethodCallContainingPosition(token);
        if (containingCall != null) {
            MethodCallInfo.Argument containingArg = findArgumentContainingPosition(containingCall, token.getGlobalStart());
            if (containingArg != null) {
                // Show only this argument's specific error
                MethodCallInfo.ArgumentTypeError argError = findArgumentError(containingCall, containingArg);
                if (argError != null) {
                    errors.add(argError.getMessage());
                    return; // Only show argument error, not method-level errors
                }
            }
        }
        
        // Show method-level errors if this is the method name itself
        MethodCallInfo callInfo = token.getMethodCallInfo();
        if (callInfo != null) {
            if (callInfo.hasArgCountError()) {
                errors.add(callInfo.getErrorMessage());
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
        
        // Show field access errors
        FieldAccessInfo fieldAccessInfo = token.getFieldAccessInfo();
        if (fieldAccessInfo != null && fieldAccessInfo.hasError()) {
            errors.add(fieldAccessInfo.getErrorMessage());
        }
        
        // Show assignment errors (type mismatch, final reassignment, etc.)
        // Search through FieldInfo's assignments by position
        AssignmentInfo assignmentInfo = findAssignmentContainingPosition(token);
        if (assignmentInfo != null && assignmentInfo.hasError()) {
            errors.add(assignmentInfo.getErrorMessage());
        }
        
        // Show unresolved field errors
        FieldInfo fieldInfo = token.getFieldInfo();
        if (fieldInfo != null && !fieldInfo.isResolved()) {
            errors.add("Cannot resolve symbol '" + token.getText() + "'");
        }

        // Show method declaration errors (missing return, parameter errors, return type errors)
        MethodInfo methodDecl = findMethodDeclarationContainingPosition(token);
        if (methodDecl != null && methodDecl.hasError()) {
            int tokenStart = token.getGlobalStart();
            int tokenEnd = token.getGlobalEnd();
            
            // If hovering over the method name, show missing return error
            if (methodDecl.hasMissingReturnError()) {
                int methodNameStart = methodDecl.getNameOffset();
                int methodNameEnd = methodNameStart + methodDecl.getName().length();
                
                if (tokenStart >= methodNameStart && tokenEnd <= methodNameEnd) {
                    errors.add(methodDecl.getErrorMessage());
                }
            }
            
            // If hovering over duplicate method declaration, show the error
            if (methodDecl.getErrorType() == MethodInfo.ErrorType.DUPLICATE_METHOD) {
                int declStart = methodDecl.getFullDeclarationOffset();
                int declEnd = methodDecl.getDeclarationEnd();
                
                if (tokenStart >= declStart && tokenEnd <= declEnd) {
                    errors.add(methodDecl.getErrorMessage());
                }
            }
            
            // If hovering over a parameter with an error, show that error
            if (methodDecl.hasParameterErrors()) {
                for (MethodInfo.ParameterError paramError : methodDecl.getParameterErrors()) {
                    FieldInfo param = paramError.getParameter();
                    if (param != null && param.getDeclarationOffset() >= 0) {
                        int paramStart = param.getDeclarationOffset();
                        int paramEnd = paramStart + param.getName().length();
                        
                        if (tokenStart >= paramStart && tokenEnd <= paramEnd) {
                            errors.add(paramError.getMessage());
                        }
                    }
                }
            }
            
            // If hovering over a return statement with a type error, show that error
            if (methodDecl.hasReturnStatementErrors()) {
                for (MethodInfo.ReturnStatementError returnError : methodDecl.getReturnStatementErrors()) {
                    int returnStart = returnError.getStartOffset();
                    int returnEnd = returnError.getEndOffset();
                    
                    if (tokenStart >= returnStart && tokenEnd <= returnEnd) {
                        errors.add(returnError.getMessage());
                    }
                }
            }
        }
    }
    
    /**
     * Find an assignment that contains this token's position.
     * Searches through all assignments (script fields and external fields).
     */
    private AssignmentInfo findAssignmentContainingPosition(Token token) {
        ScriptLine line = token.getParentLine();
        if (line == null || line.getParent() == null) {
            return null;
        }
        
        ScriptDocument doc = line.getParent();
        int tokenStart = token.getGlobalStart();
        
        // Use ScriptDocument's method which handles all prioritization
        return doc.findAssignmentAtPosition(tokenStart);
    }
    
    /**
     * Find the method call that contains this token's position within its argument list.
     */
    private MethodCallInfo findMethodCallContainingPosition(Token token) {
        ScriptLine line = token.getParentLine();
        if (line == null || line.getParent() == null) {
            return null;
        }
        
        ScriptDocument doc = line.getParent();
        int tokenStart = token.getGlobalStart();
        
        for (MethodCallInfo call : doc.getMethodCalls()) {
            // Check if token is within the argument list
            if (tokenStart >= call.getOpenParenOffset() && tokenStart <= call.getCloseParenOffset()) {
                // Make sure it's not the method name itself
                if (tokenStart >= call.getMethodNameStart() && tokenStart <= call.getMethodNameEnd()) {
                    continue;
                }
                return call;
            }
        }
        return null;
    }

    /**
     * Find the method declaration that contains this token's position.
     * Returns null if the token is not within a method declaration (header or body).
     */
    private MethodInfo findMethodDeclarationContainingPosition(Token token) {
        ScriptLine line = token.getParentLine();
        if (line == null || line.getParent() == null) {
            return null;
        }

        ScriptDocument doc = line.getParent();
        int tokenStart = token.getGlobalStart();

        for (MethodInfo method : doc.getMethods()) {
            if (!method.isDeclaration())
                continue;

            // Check if token is within the method declaration header OR body
            int methodStart = method.getFullDeclarationOffset();
            if (methodStart < 0) methodStart = method.getTypeOffset();
            int bodyEnd = method.getBodyEnd();

            // Token is within the method (header + body)
            if (tokenStart >= methodStart && tokenStart <= bodyEnd) {
                return method;
            }
        }
        return null;
    }
    
    /**
     * Find the argument that contains the given position.
     */
    private MethodCallInfo.Argument findArgumentContainingPosition(MethodCallInfo callInfo, int position) {
        for (MethodCallInfo.Argument arg : callInfo.getArguments()) {
            if (position >= arg.getStartOffset() && position <= arg.getEndOffset()) {
                return arg;
            }
        }
        return null;
    }
    
    /**
     * Find the type error for a specific argument.
     */
    private MethodCallInfo.ArgumentTypeError findArgumentError(MethodCallInfo callInfo, MethodCallInfo.Argument argument) {
        if (!callInfo.hasArgTypeError()) {
            return null;
        }
        
        int argIndex = callInfo.getArguments().indexOf(argument);
        if (argIndex < 0) {
            return null;
        }
        
        for (MethodCallInfo.ArgumentTypeError error : callInfo.getArgumentTypeErrors()) {
            if (error.getArgIndex() == argIndex) {
                return error;
            }
        }
        return null;
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
            if (Modifier.isPublic(mods)) addSegment("public ", TokenType.MODIFIER.getHexColor());
            if (Modifier.isAbstract(mods) && !clazz.isInterface()) addSegment("abstract ", TokenType.MODIFIER.getHexColor());
            if (Modifier.isFinal(mods)) addSegment("final ", TokenType.MODIFIER.getHexColor());
            
            // Class type keyword
            if (clazz.isInterface()) {
                addSegment("interface ", TokenType.MODIFIER.getHexColor());
            } else if (clazz.isEnum()) {
                addSegment("enum ", TokenType.MODIFIER.getHexColor());
            } else {
                addSegment("class ", TokenType.MODIFIER.getHexColor());
            }
            
            // Class name - use proper color based on type
            int classColor = clazz.isInterface() ? TokenType.INTERFACE_DECL.getHexColor() 
                : clazz.isEnum() ? TokenType.ENUM_DECL.getHexColor() 
                : TokenType.IMPORTED_CLASS.getHexColor();
            addSegment(typeInfo.getSimpleName(), classColor);
            
            // Extends
            Class<?> superclass = clazz.getSuperclass();
            if (superclass != null && superclass != Object.class) {
                addSegment(" extends ", TokenType.MODIFIER.getHexColor());
                addSegment(superclass.getSimpleName(), getColorForClass(superclass));
            }

            List<TokenHoverInfo.TextSegment> declaration = null;

            // Implements
            Class<?>[] interfaces = clazz.getInterfaces();
            if (interfaces.length > 0) {
                addSegment(clazz.isInterface() ? " extends " : " implements ", TokenType.MODIFIER.getHexColor());
                for (int i = 0; i < Math.min(interfaces.length, 3); i++) {
                    if (i > 0) addSegment(", ", TokenType.DEFAULT.getHexColor());
                    addSegment(interfaces[i].getSimpleName(), TokenType.INTERFACE_DECL.getHexColor());
                }
                if (interfaces.length > 3) {
                    addSegment(", ...", TokenType.DEFAULT.getHexColor());
                }
            }
        } else if (typeInfo instanceof ScriptTypeInfo) {
            // Script-defined type
            ScriptTypeInfo scriptType = (ScriptTypeInfo) typeInfo;
            
            // Icon
            if (scriptType.getKind() == TypeInfo.Kind.INTERFACE) {
                iconIndicator = "I";
            } else if (scriptType.getKind() == TypeInfo.Kind.ENUM) {
                iconIndicator = "E";
            } else {
                iconIndicator = "C";
            }
            
            // Build declaration with modifiers
            int mods = scriptType.getModifiers();
            
            // Modifiers
            if (Modifier.isPublic(mods)) addSegment("public ", TokenType.MODIFIER.getHexColor());
            if (Modifier.isAbstract(mods) && scriptType.getKind() != TypeInfo.Kind.INTERFACE) 
                addSegment("abstract ", TokenType.MODIFIER.getHexColor());
            if (Modifier.isFinal(mods)) addSegment("final ", TokenType.MODIFIER.getHexColor());
            if (Modifier.isStatic(mods)) addSegment("static ", TokenType.MODIFIER.getHexColor());
            
            // Class type keyword
            if (scriptType.getKind() == TypeInfo.Kind.INTERFACE) {
                addSegment("interface ", TokenType.MODIFIER.getHexColor());
            } else if (scriptType.getKind() == TypeInfo.Kind.ENUM) {
                addSegment("enum ", TokenType.MODIFIER.getHexColor());
            } else {
                addSegment("class ", TokenType.MODIFIER.getHexColor());
            }
            
            // Class name
            int classColor = scriptType.getKind() == TypeInfo.Kind.INTERFACE ? TokenType.INTERFACE_DECL.getHexColor() 
                : scriptType.getKind() == TypeInfo.Kind.ENUM ? TokenType.ENUM_DECL.getHexColor() 
                : TokenType.IMPORTED_CLASS.getHexColor();
            addSegment(typeInfo.getSimpleName(), classColor);
            
        } else {
            // Unresolved type
            iconIndicator = "?";
            addSegment(typeInfo.getSimpleName(), TokenType.IMPORTED_CLASS.getHexColor());
            if (!typeInfo.isResolved()) {
                errors.add("Cannot resolve class '" + typeInfo.getSimpleName() + "'");
            }
        }
        
        // If this is a NEW_TYPE token with constructor info, show the constructor signature
        if (token.getMethodInfo() != null) {
            MethodInfo constructor = token.getMethodInfo();
            declaration.add(new TextSegment("\n", TokenType.DEFAULT.getHexColor()));
            additionalInfo.add("Constructor");
            buildConstructorDeclaration(constructor, typeInfo);
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
            // Show full qualified class name (like IntelliJ)
            String fullName = containingType.getFullName();
            if (fullName != null && !fullName.isEmpty()) {
                packageName = fullName;
            } else {
                String pkg = containingType.getPackageName();
                String className = containingType.getSimpleName();
                if (pkg != null && !pkg.isEmpty()) {
                    packageName = pkg + "." + className;
                } else {
                    packageName = className;
                }
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
        FieldAccessInfo chainedField = token.getFieldAccessInfo();
        if (fieldInfo == null && chainedField != null)
            fieldInfo = chainedField.getResolvedField();

        if (fieldInfo == null)
            return;

        iconIndicator = "f";

        // Add documentation if available
        if (fieldInfo.getDocumentation() != null && !fieldInfo.getDocumentation().isEmpty()) {
            String[] docLines = fieldInfo.getDocumentation().split("\n");
            for (String line : docLines) {
                documentation.add(line);
            }
        }

        TypeInfo declaredType = fieldInfo.getDeclaredType();

        // Try to get modifiers from Java reflection if this is a chained field
        boolean foundModifiers = false;
        if (chainedField != null && chainedField.getReceiverType() != null) {
            TypeInfo receiverType = chainedField.getReceiverType();
            if (receiverType.getJavaClass() != null) {
                try {
                    Field javaField = receiverType.getJavaClass().getField(fieldInfo.getName());
                    if (javaField != null)
                        addFieldModifiers(javaField.getModifiers());
                    foundModifiers = true;
                } catch (Exception e) {
                }
            }
        }
        
        if (!foundModifiers && fieldInfo.getDeclarationOffset() >= 0) {
            // Show modifiers from source if we have a declaration position
            String modifiers = extractModifiersAtPosition(fieldInfo.getDeclarationOffset());
            if (modifiers != null && !modifiers.isEmpty()) {
                addSegment(modifiers + " ", TokenType.MODIFIER.getHexColor());
            }
        }
        
        if (declaredType != null) {
            // Show field's type package.ClassName for context
            String pkg = declaredType.getPackageName();

            // For Minecraft.getMinecraft.thePlayer
            // Return net.minecraft.client.Minecraft
            if (chainedField != null)
                pkg = chainedField.getReceiverType().getFullName(); 
            
            if (pkg != null && !pkg.isEmpty()) 
                packageName = pkg;
            
            // Type - check for actual type color
            int typeColor = getColorForTypeInfo(declaredType);
            addSegment(declaredType.getSimpleName(), typeColor);
            addSegment(" ", TokenType.DEFAULT.getHexColor());
        }
        
        // Field name
        addSegment(fieldInfo.getName(), TokenType.GLOBAL_FIELD.getHexColor());
        
        // Add initialization value if available
        addInitializationTokens(token, fieldInfo);
    }

    private void extractLocalFieldInfo(Token token) {
        FieldInfo fieldInfo = token.getFieldInfo();
        if (fieldInfo == null) return;
        
        iconIndicator = "v";
        
        // Add documentation if available (though local vars rarely have docs)
        if (fieldInfo.getDocumentation() != null && !fieldInfo.getDocumentation().isEmpty()) {
            String[] docLines = fieldInfo.getDocumentation().split("\n");
            for (String line : docLines) {
                documentation.add(line);
            }
        }
        
        TypeInfo declaredType = fieldInfo.getDeclaredType();
        if (declaredType != null) {
            int typeColor = getColorForTypeInfo(declaredType);
            addSegment(declaredType.getSimpleName(), typeColor);
            addSegment(" ", TokenType.DEFAULT.getHexColor());
        }
        
        addSegment(fieldInfo.getName(), TokenType.LOCAL_FIELD.getHexColor());
        
        // Add initialization value if available
        addInitializationTokens(token, fieldInfo);
        
        // Show it's a local variable
        additionalInfo.add("Local variable");
    }

    private void extractParameterInfo(Token token) {
        FieldInfo fieldInfo = token.getFieldInfo();
        if (fieldInfo == null) return;
        
        iconIndicator = "p";
        
        // Add documentation if available (args/parameters might have docs from method javadoc)
        if (fieldInfo.getDocumentation() != null && !fieldInfo.getDocumentation().isEmpty()) {
            String[] docLines = fieldInfo.getDocumentation().split("\n");
            for (String line : docLines) {
                documentation.add(line);
            }
        }
        
        TypeInfo declaredType = fieldInfo.getDeclaredType();
        if (declaredType != null) {
            int typeColor = getColorForTypeInfo(declaredType);
            addSegment(declaredType.getSimpleName(), typeColor);
            addSegment(" ", TokenType.DEFAULT.getHexColor());
        }
        
        addSegment(fieldInfo.getName(), TokenType.PARAMETER.getHexColor());
        
        additionalInfo.add("Parameter");
    }

    private void extractUndefinedInfo(Token token) {
        iconIndicator = "?";
        addSegment(token.getText(), TokenType.UNDEFINED_VAR.getHexColor());
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

    private void buildConstructorDeclaration(MethodInfo constructor, TypeInfo containingType) {
        // Modifiers (public, private, etc.)
        declaration.clear();
        int mods = constructor.getModifiers();
        if (Modifier.isPublic(mods)) addSegment("public ", TokenType.MODIFIER.getHexColor());
        else if (Modifier.isProtected(mods)) addSegment("protected ", TokenType.MODIFIER.getHexColor());
        else if (Modifier.isPrivate(mods)) addSegment("private ", TokenType.MODIFIER.getHexColor());
        
        // Constructor name (same as class name)
        addSegment(constructor.getName(), containingType.getTokenType().getHexColor());
        
        // Parameters
        addSegment("(", TokenType.DEFAULT.getHexColor());
        List<FieldInfo> params = constructor.getParameters();
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) addSegment(", ", TokenType.DEFAULT.getHexColor());
            FieldInfo param = params.get(i);
            TypeInfo paramType = param.getDeclaredType();
            if (paramType != null) {
                int paramTypeColor = getColorForTypeInfo(paramType);
                addSegment(paramType.getSimpleName(), paramTypeColor);
                addSegment(" ", TokenType.DEFAULT.getHexColor());
            }
            addSegment(param.getName(), TokenType.PARAMETER.getHexColor());
        }
        addSegment(")", TokenType.DEFAULT.getHexColor());
        
        // Add documentation if available
        if (constructor.getDocumentation() != null && !constructor.getDocumentation().isEmpty()) {
            String[] docLines = constructor.getDocumentation().split("\n");
            for (String line : docLines) {
                documentation.add(line);
            }
        }
    }

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
        if (Modifier.isPublic(mods)) addSegment("public ", TokenType.MODIFIER.getHexColor());
        else if (Modifier.isProtected(mods)) addSegment("protected ", TokenType.MODIFIER.getHexColor());
        else if (Modifier.isPrivate(mods)) addSegment("private ", TokenType.MODIFIER.getHexColor());
        
        if (Modifier.isStatic(mods)) addSegment("static ", TokenType.MODIFIER.getHexColor());
        if (Modifier.isFinal(mods)) addSegment("final ", TokenType.MODIFIER.getHexColor());
        if (Modifier.isAbstract(mods)) addSegment("abstract ", TokenType.MODIFIER.getHexColor());
        if (Modifier.isSynchronized(mods)) addSegment("synchronized ", TokenType.MODIFIER.getHexColor());
        
        // Return type - check for actual type color
        Class<?> returnType = method.getReturnType();
        int returnTypeColor = getColorForClass(returnType);
        addSegment(returnType.getSimpleName(), returnTypeColor);
        addSegment(" ", TokenType.DEFAULT.getHexColor());
        
        // Method name
        addSegment(method.getName(), TokenType.METHOD_DECL.getHexColor());
        
        // Parameters
        addSegment("(", TokenType.DEFAULT.getHexColor());
        Class<?>[] paramTypes = method.getParameterTypes();
        java.lang.reflect.Parameter[] params = method.getParameters();
        for (int i = 0; i < paramTypes.length; i++) {
            if (i > 0) addSegment(", ", TokenType.DEFAULT.getHexColor());
            int paramTypeColor = getColorForClass(paramTypes[i]);
            addSegment(paramTypes[i].getSimpleName(), paramTypeColor);
            addSegment(" ", TokenType.DEFAULT.getHexColor());
            // Try to get parameter name if available
            String paramName = params.length > i ? params[i].getName() : "arg" + i;
            addSegment(paramName, TokenType.PARAMETER.getHexColor());
        }
        addSegment(")", TokenType.DEFAULT.getHexColor());
    }

    private void buildBasicMethodDeclaration(MethodInfo methodInfo, TypeInfo containingType) {
        // Try to extract modifiers from source if this is a declaration
        if (methodInfo.isDeclaration() && methodInfo.getDeclarationOffset() >= 0) {
            String modifiers = extractModifiersAtPosition(methodInfo.getDeclarationOffset());
            if (modifiers != null && !modifiers.isEmpty()) {
                addSegment(modifiers + " ", TokenType.MODIFIER.getHexColor());
            }
        } else {
            // Fallback: show static if we know it
            if (methodInfo.isStatic()) {
                addSegment("static ", TokenType.MODIFIER.getHexColor());
            }
        }
        
        // Return type
        TypeInfo returnType = methodInfo.getReturnType();
        if (returnType != null) {
            int returnTypeColor = getColorForTypeInfo(returnType);
            addSegment(returnType.getSimpleName(), returnTypeColor);
            addSegment(" ", TokenType.DEFAULT.getHexColor());
        } else {
            addSegment("void ", TokenType.KEYWORD.getHexColor());
        }
        
        // Method name
        addSegment(methodInfo.getName(), TokenType.METHOD_DECL.getHexColor());
        
        // Parameters
        addSegment("(", TokenType.DEFAULT.getHexColor());
        List<FieldInfo> params = methodInfo.getParameters();
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) addSegment(", ", TokenType.DEFAULT.getHexColor());
            FieldInfo param = params.get(i);
            TypeInfo paramType = param.getDeclaredType();
            if (paramType != null) {
                int paramTypeColor = getColorForTypeInfo(paramType);
                addSegment(paramType.getSimpleName(), paramTypeColor);
                addSegment(" ", TokenType.DEFAULT.getHexColor());
            }
            addSegment(param.getName(), TokenType.PARAMETER.getHexColor());
        }
        addSegment(")", TokenType.DEFAULT.getHexColor());

        // Add documentation if available
        if (methodInfo.getDocumentation() != null && !methodInfo.getDocumentation().isEmpty()) {
            String[] docLines = methodInfo.getDocumentation().split("\n");
            for (String line : docLines) {
                documentation.add(line);
            }
        }

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

    /**
     * Extract modifiers (public, private, static, final, etc.) before a declaration position.
     * Looks backward from the position to find modifiers.
     */
    private String extractModifiersAtPosition(int position) {
        if (token.getParentLine().getParent() == null)
            return null;

        String text = token.getParentLine().getParent().getText();
        if (position < 0 || position >= text.length())
            return null;

        // Look backward from position to find start of line or previous semicolon/brace
        int searchStart = position - 1;
        while (searchStart >= 0) {
            char c = text.charAt(searchStart);
            if (c == ';' || c == '{' || c == '}' || c == '\n') {
                searchStart++;
                break;
            }
            searchStart--;
        }
        if (searchStart < 0)
            searchStart = 0;

        // Extract text before the declaration
        String beforeDecl = text.substring(searchStart, position).trim();

        // Match modifier keywords
        StringBuilder modifiers = new StringBuilder();
        String[] words = beforeDecl.split("\\s+");
        for (String word : words) {
            if (TypeResolver.isModifier(word)) {
                if (modifiers.length() > 0)
                    modifiers.append(" ");
                modifiers.append(word);
            }
        }

        return modifiers.toString();
    }

    /**
     * Add field modifiers from Java reflection modifiers.
     */
    private void addFieldModifiers(int mods) {
        if (Modifier.isPublic(mods))
            addSegment("public ", TokenType.MODIFIER.getHexColor());
        else if (Modifier.isProtected(mods))
            addSegment("protected ", TokenType.MODIFIER.getHexColor());
        else if (Modifier.isPrivate(mods))
            addSegment("private ", TokenType.MODIFIER.getHexColor());

        if (Modifier.isStatic(mods))
            addSegment("static ", TokenType.MODIFIER.getHexColor());
        if (Modifier.isFinal(mods))
            addSegment("final ", TokenType.MODIFIER.getHexColor());
        if (Modifier.isVolatile(mods))
            addSegment("volatile ", TokenType.MODIFIER.getHexColor());
        if (Modifier.isTransient(mods))
            addSegment("transient ", TokenType.MODIFIER.getHexColor());
    }

    /**
     * Get the appropriate color for a Class<?> based on its type.
     */
    private int getColorForClass(Class<?> clazz) {
        if(clazz.isPrimitive()) return TokenType.KEYWORD.getHexColor();
        if (clazz.isInterface()) return TokenType.INTERFACE_DECL.getHexColor();
        if (clazz.isEnum()) return TokenType.ENUM_DECL.getHexColor();
        return TokenType.IMPORTED_CLASS.getHexColor();
    }
    
    /**
     * Get the appropriate color for a TypeInfo based on its Java class.
     */
    private int getColorForTypeInfo(TypeInfo typeInfo) {
        if (typeInfo != null) {
            Class<?> clazz = typeInfo.getJavaClass();
            if (clazz != null) 
                return getColorForClass(clazz);
        }
        return TokenType.IMPORTED_CLASS.getHexColor();
    }
    
    /**
     * Add initialization tokens from the field's initializer to the declaration.
     * Fetches the tokens in the initialization range and adds them with their proper coloring.
     */
    private void addInitializationTokens(Token token, FieldInfo fieldInfo) {
        if (!fieldInfo.hasInitializer()) return;
        
        ScriptLine line = token.getParentLine();
        if (line == null || line.getParent() == null) return;
        
        ScriptDocument doc = line.getParent();
        // Include the semicolon by extending range by 1
        List<Token> initTokens = doc.getTokensInRange(fieldInfo.getInitStart(), fieldInfo.getInitEnd() + 1);
        
        if (initTokens.isEmpty()) return;
        
        // Add space before '=' for readability
        addSegment(" ", TokenType.DEFAULT.getHexColor());

        // Add each token with its proper color, ensuring normalized spacing between tokens
        String lastText = null;
        for (Token initToken : initTokens) {
            String text = initToken.getText();

            // Normalize whitespace - replace all newlines and multiple spaces with single space
            text = text.replaceAll("\\s+", " ").trim();

            // Skip if token became empty after whitespace removal
            if (text.isEmpty()) {
                continue;
            }

            // Determine if we need a space between last token and current token
            if (lastText != null && shouldAddSpace(lastText, text)) {
                addSegment(" ", TokenType.DEFAULT.getHexColor());
            }

            addSegment(text, initToken.getType().getHexColor());
            lastText = text;
        }
    }

    /**
     * Determine if a space should be added between two tokens.
     */
    private boolean shouldAddSpace(String lastToken, String currentToken) {
        if (lastToken.isEmpty() || currentToken.isEmpty())
            return false;

        char lastChar = lastToken.charAt(lastToken.length() - 1);
        char firstChar = currentToken.charAt(0);

        // Never add space before these closing/trailing characters
        if (firstChar == '(' || firstChar == '[' || firstChar == '{' || 
            firstChar == '.' || firstChar == ',' || firstChar == ';' ||
            (firstChar == ':' && lastToken.equals("?"))) {
            return false;
        }

        // Never add space after these opening/leading characters
        if (lastChar == '(' || lastChar == '[' || lastChar == '{' || lastChar == '.') {
            return false;
        }

        // Add space around operators (check last/first characters)
        if (isOperatorChar(lastChar) || isOperatorChar(firstChar)) {
            return true;
        }

        // Add space after closing brackets/parens before other tokens
        if (lastChar == ')' || lastChar == ']' || lastChar == '}') {
            return true;
        }
        
        // Add space after colons and commas (except after ?: ternary)
        if ((lastChar == ':' && !lastToken.equals("?:")) || lastChar == ',') {
            return true;
        }

        // Add space between identifiers/keywords and numbers
        boolean lastIsIdentifier = Character.isLetterOrDigit(lastChar) || lastChar == '_';
        boolean firstIsIdentifier = Character.isLetterOrDigit(firstChar) || firstChar == '_';
        if (lastIsIdentifier && firstIsIdentifier) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if a character is an operator that needs spacing.
     */
    private boolean isOperatorChar(char c) {
        return "+-*/%<>=!&|^?:".indexOf(c) >= 0;
    }
    // ==================== GETTERS ====================

    public String getPackageName() { return packageName; }
    public String getIconIndicator() { return iconIndicator; }

    public List<TextSegment> getDeclaration() {
        if (getErrors().stream().anyMatch(err -> err.contains("Cannot resolve symbol")))
            return new ArrayList<>();
        return declaration;
    }
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
