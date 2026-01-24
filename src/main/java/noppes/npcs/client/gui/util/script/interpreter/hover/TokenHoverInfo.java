package noppes.npcs.client.gui.util.script.interpreter.hover;

import noppes.npcs.client.gui.util.script.interpreter.*;
import noppes.npcs.client.gui.util.script.interpreter.field.AssignmentInfo;
import noppes.npcs.client.gui.util.script.interpreter.field.EnumConstantInfo;
import noppes.npcs.client.gui.util.script.interpreter.field.FieldAccessInfo;
import noppes.npcs.client.gui.util.script.interpreter.field.FieldInfo;
import noppes.npcs.client.gui.util.script.interpreter.jsdoc.JSDocDeprecatedTag;
import noppes.npcs.client.gui.util.script.interpreter.jsdoc.JSDocInfo;
import noppes.npcs.client.gui.util.script.interpreter.jsdoc.JSDocParamTag;
import noppes.npcs.client.gui.util.script.interpreter.jsdoc.JSDocReturnTag;
import noppes.npcs.client.gui.util.script.interpreter.jsdoc.JSDocSeeTag;
import noppes.npcs.client.gui.util.script.interpreter.jsdoc.JSDocSinceTag;
import noppes.npcs.client.gui.util.script.interpreter.jsdoc.JSDocTypeTag;
import noppes.npcs.client.gui.util.script.interpreter.js_parser.JSTypeInfo;
import noppes.npcs.client.gui.util.script.interpreter.method.MethodCallInfo;
import noppes.npcs.client.gui.util.script.interpreter.method.MethodInfo;
import noppes.npcs.client.gui.util.script.interpreter.token.Token;
import noppes.npcs.client.gui.util.script.interpreter.token.TokenErrorMessage;
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

    /** JSDoc-formatted documentation with colored segments (sections like Params, Returns) */
    private List<DocumentationLine> jsDocLines = new ArrayList<>();
    
    /** Error messages (shown in red) */
    private List<String> errors = new ArrayList<>();
    
    /** Additional info lines (e.g., "Variable 'x' is never used") */
    private List<String> additionalInfo = new ArrayList<>();
    
    /** The token this info was built from */
    private final Token token;

    // ==================== DOCUMENTATION LINE ====================

    /**
     * A line of documentation that may contain colored segments.
     * Used for JSDoc-style rendering with "Params:", parameter names, etc.
     */
    public static class DocumentationLine {
        public final List<TextSegment> segments;

        public DocumentationLine() {
            this.segments = new ArrayList<>();
        }

        public void addSegment(String text, int color) {
            segments.add(new TextSegment(text, color));
        }

        public void addText(String text) {
            segments.add(new TextSegment(text, TextSegment.COLOR_DEFAULT));
        }

        public boolean isEmpty() {
            return segments.isEmpty() || segments.stream().allMatch(s -> s.text == null || s.text.isEmpty());
        }
    }

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

            case ENUM_CONSTANT:
                info.extractEnumConstantInfo(token);
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
                } else if (info.hasErrors())
                    return info;
                else {
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
        MethodCallInfo callInfo = token.isEnumConstant()? containingCall : token.getMethodCallInfo();
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
            
            // If hovering over a parameter with an error, show that error
            else if (methodDecl.hasParameterErrors()) {
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
            else if (methodDecl.hasReturnStatementErrors()) {
                for (MethodInfo.ReturnStatementError returnError : methodDecl.getReturnStatementErrors()) {
                    int returnStart = returnError.getStartOffset();
                    int returnEnd = returnError.getEndOffset();
                    
                    if (tokenStart >= returnStart && tokenEnd <= returnEnd) {
                        errors.add(returnError.getMessage());
                    }
                }
            }


            // All other errors
            else if (methodDecl.hasError()) {
                int declStart = methodDecl.getFullDeclarationOffset();
                int declEnd = methodDecl.getDeclarationEnd();

                if (tokenStart >= declStart && tokenEnd <= declEnd) {
                    errors.add(methodDecl.getErrorMessage());
                }
            }
        }

        ScriptTypeInfo scriptType = findScriptTypeContainingPosition(token);
        if (scriptType != null && scriptType.hasError()) {
            // Missing interface method errors
            for (ScriptTypeInfo.MissingMethodError err : scriptType.getMissingMethodErrors()) {
                errors.add(err.getMessage());
            }
            // Constructor mismatch errors
            for (ScriptTypeInfo.ConstructorMismatchError err : scriptType.getConstructorMismatchErrors()) {
                errors.add(err.getMessage());
            }
            // General error message
            if (scriptType.getErrorMessage() != null) {
                errors.add(scriptType.getErrorMessage());
            }
        }
        
        EnumConstantInfo enumConst = findEnumConstantContainingPosition(token);
        if (enumConst != null && enumConst.hasError()) {
            errors.add(enumConst.getErrorMessage());
        }
        
        if(token.getType() == TokenType.UNDEFINED_VAR)
            errors.add("Cannot resolve symbol '" + token.getText() + "'");

        TokenErrorMessage msg = token.getErrorMessage();
        if (msg != null && !msg.getMessage().isEmpty()) {
            if(msg.clearOtherErrors)
                errors.clear();
            
            errors.add(msg.getMessage());
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
            boolean isWithinName = tokenStart >= call.getMethodNameStart() && tokenStart <= call.getMethodNameEnd();
            
            // If this is an enum constant, return methodCall on name itself
            if (token.isEnumConstant() && isWithinName)
                return call;

            // Check if token is within the argument list
            if (tokenStart >= call.getOpenParenOffset() && tokenStart <= call.getCloseParenOffset()) {
                // Make sure it's not the method name itself
                if (isWithinName) {
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

        for (MethodInfo method : doc.getAllMethods()) {
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

    private ScriptTypeInfo findScriptTypeContainingPosition(Token token) {
        ScriptLine line = token.getParentLine();
        if (line == null || line.getParent() == null) {
            return null;
        }

        ScriptDocument doc = line.getParent();
        int tokenStart = token.getGlobalStart();

        for (ScriptTypeInfo scriptType : doc.getScriptTypes()) {
            int typeStart = scriptType.getDeclarationOffset();
            int typeEnd = scriptType.getBodyStart();

            // Token is within the type declaration
            if (tokenStart >= typeStart && tokenStart <= typeEnd) {
                return scriptType;
            }
        }
        return null;
    }
    
    private EnumConstantInfo findEnumConstantContainingPosition(Token token) {
        ScriptLine line = token.getParentLine();
        if (line == null || line.getParent() == null) {
            return null;
        }

        ScriptDocument doc = line.getParent();
        int tokenStart = token.getGlobalStart();

        for (EnumConstantInfo enumConst : doc.getAllEnumConstants()) {
            int constStart = enumConst.getDeclarationOffset();
            int constEnd = constStart + enumConst.getName().length();

            // Token is within the enum constant declaration
            if (tokenStart >= constStart && tokenStart <= constEnd) {
                return enumConst;
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
            boolean isEnum = clazz.isEnum();
            // Icon
            if (clazz.isInterface()) {
                iconIndicator = "I";
            } else if (isEnum) {
                iconIndicator = "E";
            } else {
                iconIndicator = "C";
            }
            
            // Build declaration
            int mods = clazz.getModifiers();
            
            // Modifiers
            if (Modifier.isPublic(mods)) addSegment("public ", TokenType.MODIFIER.getHexColor());
            if (Modifier.isAbstract(mods) && !clazz.isInterface()) addSegment("abstract ", TokenType.MODIFIER.getHexColor());
            if (Modifier.isFinal(mods) && !isEnum) addSegment("final ", TokenType.MODIFIER.getHexColor());
            
            // Class type keyword
            if (clazz.isInterface()) {
                addSegment("interface ", TokenType.MODIFIER.getHexColor());
            } else if (isEnum) {
                addSegment("enum ", TokenType.MODIFIER.getHexColor());
            } else {
                addSegment("class ", TokenType.MODIFIER.getHexColor());
            }
            
            // Class name - use proper color based on type
            int classColor = clazz.isInterface() ? TokenType.INTERFACE_DECL.getHexColor() 
                : clazz.isEnum() ? TokenType.ENUM_DECL.getHexColor() 
                : TokenType.IMPORTED_CLASS.getHexColor();
            addSegment(getName(typeInfo), classColor);
            
            // Extends
            Class<?> superclass = clazz.getSuperclass();
            if (superclass != null && superclass != Object.class && !isEnum) {
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
            addSegment(getName(typeInfo), classColor);
            
            // Extends clause for ScriptTypeInfo
            if (scriptType.hasSuperClass()) {
                addSegment(" extends ", TokenType.MODIFIER.getHexColor());
                TypeInfo superClass = scriptType.getSuperClass();
                if (superClass != null && superClass.isResolved()) {
                    // Color based on resolved type
                    addSegment(getName(superClass), getColorForTypeInfo(superClass));
                } else {
                    // Unresolved - show the raw name in undefined color
                    String superName = scriptType.getSuperClassName();
                    if (superName != null) {
                        addSegment(superName, TokenType.UNDEFINED_VAR.getHexColor());
                    }
                }
            }
            
            // Implements clause for ScriptTypeInfo
            List<TypeInfo> implementedInterfaces = scriptType.getImplementedInterfaces();
            if (!implementedInterfaces.isEmpty()) {
                // For interfaces, they "extend" other interfaces; for classes, they "implement"
                String keyword = scriptType.getKind() == TypeInfo.Kind.INTERFACE ? " extends " : " implements ";
                if (scriptType.hasSuperClass() && scriptType.getKind() == TypeInfo.Kind.INTERFACE) {
                    // Already showed extends, so use comma
                    addSegment(", ", TokenType.DEFAULT.getHexColor());
                } else {
                    addSegment(keyword, TokenType.MODIFIER.getHexColor());
                }
                
                List<String> interfaceNames = scriptType.getImplementedInterfaceNames();
                for (int i = 0; i < implementedInterfaces.size(); i++) {
                    if (i > 0) addSegment(", ", TokenType.DEFAULT.getHexColor());
                    
                    TypeInfo ifaceType = implementedInterfaces.get(i);
                    String ifaceName = (i < interfaceNames.size()) ? interfaceNames.get(i) : getName(ifaceType);
                    
                    if (ifaceType != null && ifaceType.isResolved()) {
                        addSegment(ifaceName, getColorForTypeInfo(ifaceType));
                    } else {
                        addSegment(ifaceName, TokenType.UNDEFINED_VAR.getHexColor());
                    }
                }
            }
            
            // Add ScriptTypeInfo errors
            if (false) { //|| scriptType.hasError()
                // Missing interface method errors
                for (ScriptTypeInfo.MissingMethodError err : scriptType.getMissingMethodErrors()) {
                    errors.add(err.getMessage());
                }
                // Constructor mismatch errors
                for (ScriptTypeInfo.ConstructorMismatchError err : scriptType.getConstructorMismatchErrors()) {
                    errors.add(err.getMessage());
                }
                // General error message
                if (scriptType.getErrorMessage() != null) {
                    errors.add(scriptType.getErrorMessage());
                }
            }

            JSDocInfo jsDocInfo = scriptType.getJSDocInfo();
            if (jsDocInfo != null) {
                formatJSDocumentation(jsDocInfo, null);
            }
            
        } else if (typeInfo.isJSType()) {
            JSTypeInfo jsType = typeInfo.getJSTypeInfo();
            
            iconIndicator = "I";
            addSegment("interface ", TokenType.MODIFIER.getHexColor());
            addSegment(getName(typeInfo), TokenType.INTERFACE_DECL.getHexColor());
            
            if (jsType.getExtendsType() != null) {
                addSegment(" extends ", TokenType.MODIFIER.getHexColor());
                addSegment(jsType.getExtendsType(), TokenType.INTERFACE_DECL.getHexColor());
            }
            
            JSDocInfo jsDocInfo = typeInfo.getJSDocInfo();
            if (jsDocInfo != null) {
                formatJSDocumentation(jsDocInfo, null);
            }
            
        } else {
            // Unresolved type
            iconIndicator = "?";
            addSegment(getName(typeInfo), token.getType().getHexColor());
            if (!typeInfo.isResolved()) {
                errors.add("Cannot resolve class '" + getName(typeInfo) + "'");
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
            
            //Checks true containing type from resolved method (handles inheritance cases)
            if (callInfo.getResolvedMethod() != null) {
                TypeInfo trueContainingType = callInfo.getResolvedMethod().getContainingType();
                if (trueContainingType != null)
                    containingType = trueContainingType;
            }
        }
        if (containingType == null && methodInfo != null) {
            containingType = methodInfo.getContainingType();
        }
        
        if (containingType != null) {
            // Show full qualified class name (like IntelliJ)
            String pkg = getPackageName(containingType);
            if (pkg != null && !pkg.isEmpty())
                packageName = pkg;
        }
        
        // Try to get actual Java method for more details
        if (methodInfo != null && methodInfo.getJavaMethod() != null) {
            buildMethodDeclaration(methodInfo.getJavaMethod(), containingType);
            extractJavadoc(methodInfo.getJavaMethod());
            return;
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
        FieldAccessInfo accessInfo = token.getFieldAccessInfo();
        if (fieldInfo == null && accessInfo != null)
            fieldInfo = accessInfo.getResolvedField();

        if (fieldInfo == null)
            return;

        iconIndicator = "f";

        // Add documentation if available
        JSDocInfo jsDoc = fieldInfo.getJSDocInfo();
        if (jsDoc != null) {
            formatJSDocumentation(jsDoc, null);
        } else if (fieldInfo.getDocumentation() != null && !fieldInfo.getDocumentation().isEmpty()) {
            String[] docLines = fieldInfo.getDocumentation().split("\n");
            for (String line : docLines) {
                documentation.add(line);
            }
        }

        TypeInfo declaredType = fieldInfo.getTypeInfo();

        // Try to get modifiers from Java reflection if this is a chained field
        boolean foundModifiers = false;
        if (accessInfo != null && accessInfo.getReceiverType() != null) {
            TypeInfo receiverType = accessInfo.getReceiverType();
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
        
        int modifiers = fieldInfo.getModifiers();
        if(modifiers !=0 && !foundModifiers) {
            addFieldModifiers(modifiers);
        }
        
//        if (!foundModifiers && fieldInfo.getDeclarationOffset() >= 0) {
//            // Show modifiers from source if we have a declaration position
//            String modifiers = extractModifiersAtPosition(fieldInfo.getDeclarationOffset());
//            if (modifiers != null && !modifiers.isEmpty()) {
//                addSegment(modifiers + " ", TokenType.MODIFIER.getHexColor());
//            }
//        }
        
        if (declaredType != null) {
            // accessInfo For Minecraft.getMinecraft.thePlayer
            // Return net.minecraft.client.Minecraft, and not net.minecraft.entity.EntityPlayer
            String pkg = getPackageName(accessInfo != null ? accessInfo.getReceiverType() : declaredType);
            
            if (pkg != null && !pkg.isEmpty()) 
                packageName = pkg;
            
            // Type - check for actual type color
            int typeColor = getColorForTypeInfo(declaredType);
            addSegment(getName(declaredType), typeColor);
            addSegment(" ", TokenType.DEFAULT.getHexColor());
        }
        
        // Field name
        addSegment(fieldInfo.getName(), TokenType.GLOBAL_FIELD.getHexColor());
        
        // Add initialization value if available
        addInitializationTokens(token, fieldInfo);
    }

    private void extractEnumConstantInfo(Token token) {
        FieldInfo fieldInfo = token.getFieldInfo();
        if (fieldInfo == null)
            return;
        
        EnumConstantInfo enumInfo = fieldInfo.getEnumInfo();
        if (enumInfo == null)
            return;

        iconIndicator = "e";

        // Add documentation if available
        if (fieldInfo.getDocumentation() != null && !fieldInfo.getDocumentation().isEmpty()) {
            String[] docLines = fieldInfo.getDocumentation().split("\n");
            for (String line : docLines) {
                documentation.add(line);
            }
        }

        TypeInfo enumType = enumInfo.getEnumType();

        if (enumType != null) {
            // Show enum's package
            String pkg = getPackageName(enumType);
            if (pkg != null && !pkg.isEmpty())
                packageName = pkg;


            // Type (enum type name)
            int typeColor = getColorForTypeInfo(enumType);
            addSegment(getName(enumType), typeColor);
            addSegment(" ", TokenType.DEFAULT.getHexColor());
        }

        // Enum constant name
        addSegment(token.getStylePrefix() + fieldInfo.getName(), TokenType.ENUM_CONSTANT.getHexColor());

        // Add constructor arguments if available
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
        
        TypeInfo declaredType = fieldInfo.getTypeInfo();
        if (declaredType != null) {
            int typeColor = getColorForTypeInfo(declaredType);
            addSegment(getName(declaredType), typeColor);
            addSegment(" ", TokenType.DEFAULT.getHexColor());
        }
        
        addSegment(fieldInfo.getName(), TokenType.LOCAL_FIELD.getHexColor());
        
        // Add initialization value if available
        addInitializationTokens(token, fieldInfo);
        
        // Show it's a local variable
        additionalInfo.add("Local variable");
    }

    public String getPackageName(TypeInfo type) {
        if (type == null)
            return null;

        String fullName = type.getFullName();
        if (fullName != null && !fullName.isEmpty()) {
            return fullName;
        } else {
            String pkg = type.getPackageName();
            String className = getName(type);
            if (pkg != null && !pkg.isEmpty()) {
                return pkg + "." + className;
            } else {
                return className;
            }
        }
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
        
        TypeInfo declaredType = fieldInfo.getTypeInfo();
        if (declaredType != null) {
            int typeColor = getColorForTypeInfo(declaredType);
            addSegment(getName(declaredType), typeColor);
            addSegment(" ", TokenType.DEFAULT.getHexColor());
        }
        
        addSegment(fieldInfo.getName(), TokenType.PARAMETER.getHexColor());
        
        additionalInfo.add("Parameter");
    }

    private void extractUndefinedInfo(Token token) {
        iconIndicator = "?";
        //addSegment(token.getText(), TokenType.UNDEFINED_VAR.getHexColor());
    }
    
    public String getName(TypeInfo type){
        return type.isJSType()? type.getFullName() : type.getSimpleName();
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
            TypeInfo paramType = param.getTypeInfo();
            if (paramType != null) {
                int paramTypeColor = getColorForTypeInfo(paramType);
                addSegment(getName(paramType), paramTypeColor);
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
            addSegment(getName(returnType), returnTypeColor);
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
            TypeInfo paramType = param.getTypeInfo();
            if (paramType != null) {
                int paramTypeColor = getColorForTypeInfo(paramType);
                addSegment(getName(paramType), paramTypeColor);
                addSegment(" ", TokenType.DEFAULT.getHexColor());
            }
            addSegment(param.getName(), TokenType.PARAMETER.getHexColor());
        }
        addSegment(")", TokenType.DEFAULT.getHexColor());

        // Add documentation if available - use JSDoc formatting if we have JSDocInfo
        JSDocInfo jsDoc = methodInfo.getJSDocInfo();
        if (jsDoc != null) {
            formatJSDocumentation(jsDoc, methodInfo.getParameters());
        } else if (methodInfo.getDocumentation() != null && !methodInfo.getDocumentation().isEmpty()) {
            // Fallback to raw documentation
            String[] docLines = methodInfo.getDocumentation().split("\n");
            for (String line : docLines) {
                documentation.add(line);
            }
        }

    }

    /**
     * Format JSDoc information in IntelliJ-style with "Params:" and "Returns:" sections.
     * Creates colored documentation lines with parameter names highlighted.
     */
    private void formatJSDocumentation(JSDocInfo jsDoc, List<FieldInfo> methodParams) {
        // Add description if available (without @tags)
        String description = jsDoc.getDescription();
        if (description != null && !description.isEmpty()) {
            // Clean up the description - remove leading/trailing whitespace and asterisks
            String[] descLines = description.split("\n");
            for (String line : descLines) {
                line = line.trim();
                if (line.startsWith("*")) {
                    line = line.substring(1).trim();
                }
                if (!line.isEmpty() && !line.startsWith("@")) {
                    documentation.add(line);
                }
            }
        }

        // Add Returns section if there's a @return tag
        JSDocTypeTag typeTag = jsDoc.getTypeTag();
        if (typeTag != null) {
            // "Type:" header
            DocumentationLine typeLine = new DocumentationLine();
            typeLine.addSegment("Type:", TokenType.JSDOC_TAG.getHexColor());

            // Type if available
            if (typeTag.hasType()) {
                typeLine.addText(" ");
                typeLine.addSegment("{", TokenType.JSDOC_TYPE.getHexColor());
                typeLine.addSegment(typeTag.getTypeName(), TokenType.getColor(typeTag.getTypeInfo()));
                typeLine.addSegment("}", TokenType.JSDOC_TYPE.getHexColor());
            }

            // Description if available
            String typeDesc = typeTag.getDescription();
            if (typeDesc != null && !typeDesc.isEmpty()) {
                typeLine.addText(" - ");
                typeLine.addText(typeDesc.trim());
            }

            jsDocLines.add(typeLine);
        }

        // Add Params section if there are @param tags
        List<JSDocParamTag> paramTags = jsDoc.getParamTags();
        if (paramTags != null && !paramTags.isEmpty()) {
            //  "Params:" header
            DocumentationLine paramsHeader = new DocumentationLine();
            paramsHeader.addSegment("Params:", TokenType.JSDOC_TAG.getHexColor());
            jsDocLines.add(paramsHeader);

            // Add each parameter
            for (JSDocParamTag paramTag : paramTags) {
                DocumentationLine paramLine = new DocumentationLine();

                // Indent and parameter name
                String paramName = paramTag.getParamName();
                if (paramName == null || paramName.isEmpty()) {
                    paramLine.addSegment("param", TokenType.JSDOC_TAG.getHexColor());
                } else {
                    boolean paramExists = methodParams != null && methodParams.stream()
                                                                          .anyMatch(p -> p.getName().equals(paramName));
                    paramLine.addSegment(paramName,
                            paramExists ? TokenType.PARAMETER.getHexColor() : TokenType.UNDEFINED_VAR.getHexColor());
                }

                // Type if available
                if (paramTag.hasType()) {
                    paramLine.addSegment(" {", TokenType.JSDOC_TYPE.getHexColor());
                    paramLine.addSegment(paramTag.getTypeName(), TokenType.getColor(paramTag.getTypeInfo()));
                    paramLine.addSegment("}", TokenType.JSDOC_TYPE.getHexColor());
                }

                // Description if available
                String paramDesc = paramTag.getDescription();
                if (paramDesc != null && !paramDesc.isEmpty()) {
                    paramLine.addText(" - ");
                    paramLine.addText(paramDesc.trim());
                }

                jsDocLines.add(paramLine);
            }
        }

        // Add Returns section if there's a @return tag
        JSDocReturnTag returnTag = jsDoc.getReturnTag();
        if (returnTag != null) {
            DocumentationLine returnLine = new DocumentationLine();
            
            //"Returns:" header
            returnLine.addSegment("Returns:", TokenType.JSDOC_TAG.getHexColor());

            // Type if available
            if (returnTag.hasType()) {
                returnLine.addText(" ");
                returnLine.addSegment("{", TokenType.JSDOC_TYPE.getHexColor());
                returnLine.addSegment(returnTag.getTypeName(), TokenType.getColor(returnTag.getTypeInfo()));
                returnLine.addSegment("}", TokenType.JSDOC_TYPE.getHexColor());
            }

            // Description if available
            String returnDesc = returnTag.getDescription();
            if (returnDesc != null && !returnDesc.isEmpty()) {
                returnLine.addText(" - ");
                returnLine.addText(returnDesc.trim());
            }

            jsDocLines.add(returnLine);
        }
        
        JSDocDeprecatedTag deprecatedTag = jsDoc.getDeprecatedTag();
        if (deprecatedTag != null) {
            DocumentationLine deprecatedLine = new DocumentationLine();
            deprecatedLine.addSegment("@deprecated", TokenType.JSDOC_TAG.getHexColor());
            
            if (deprecatedTag.hasReason()) {
                deprecatedLine.addText(" - ");
                deprecatedLine.addText(deprecatedTag.getReason());
            }
            
            jsDocLines.add(deprecatedLine);
        }
        
        JSDocSinceTag sinceTag = jsDoc.getSinceTag();
        if (sinceTag != null) {
            DocumentationLine sinceLine = new DocumentationLine();
            sinceLine.addSegment("Since:", TokenType.JSDOC_TAG.getHexColor());
            sinceLine.addText(" " + sinceTag.getVersion());
            
            jsDocLines.add(sinceLine);
        }
        
        List<JSDocSeeTag> seeTags = jsDoc.getSeeTags();
        if (seeTags != null && !seeTags.isEmpty()) {
            for (JSDocSeeTag seeTag : seeTags) {
                DocumentationLine seeLine = new DocumentationLine();
                seeLine.addSegment("See:", TokenType.JSDOC_TAG.getHexColor());
                
                String reference = seeTag.getReference();
                if (seeTag.hasLinkText()) {
                    seeLine.addText(" ");
                    seeLine.addSegment(seeTag.getLinkText(), TokenType.INTERFACE_DECL.getHexColor());
                } else if (reference != null) {
                    seeLine.addText(" " + reference);
                }
                
                jsDocLines.add(seeLine);
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
     * Get the appropriate color for a TypeInfo based on its type kind.
     * Works for both Java-backed TypeInfo and ScriptTypeInfo.
     */
    private int getColorForTypeInfo(TypeInfo typeInfo) {
        // Use the TypeInfo's own token type, which handles ScriptTypeInfo correctly
        return TokenType.getColor(typeInfo);
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
        return declaration;
    }
    public List<String> getDocumentation() { return documentation; }

    public List<DocumentationLine> getJSDocLines() {
        return jsDocLines;
    }
    public List<String> getErrors() { return errors; }
    public List<String> getAdditionalInfo() { return additionalInfo; }
    public Token getToken() { return token; }
    
    public boolean hasContent() {
        return !declaration.isEmpty() || !errors.isEmpty() || !documentation.isEmpty() || !jsDocLines.isEmpty();
    }

    public boolean hasJSDocContent() {
        return !jsDocLines.isEmpty();
    }
    
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
