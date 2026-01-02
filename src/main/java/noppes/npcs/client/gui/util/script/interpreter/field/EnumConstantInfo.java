package noppes.npcs.client.gui.util.script.interpreter.field;

import noppes.npcs.client.gui.util.script.interpreter.ScriptDocument;
import noppes.npcs.client.gui.util.script.interpreter.method.MethodCallInfo;
import noppes.npcs.client.gui.util.script.interpreter.method.MethodInfo;
import noppes.npcs.client.gui.util.script.interpreter.type.ScriptTypeInfo;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents an enum constant declaration with its constructor call.
 * Combines a FieldInfo (the constant itself) with a MethodCallInfo (the constructor call validation).
 * This allows us to reuse existing method call validation logic for enum constructor matching.
 */
public class EnumConstantInfo {

    private final FieldInfo fieldInfo;
    private final MethodCallInfo constructorCall;  // Can be null if no args provided
    private final ScriptTypeInfo enumType;

    private EnumConstantInfo(FieldInfo fieldInfo, MethodCallInfo constructorCall, ScriptTypeInfo enumType) {
        this.fieldInfo = fieldInfo;
        this.constructorCall = constructorCall;
        this.enumType = enumType;
    }

    /**
     * Parse enum constants from an enum body.
     * Returns a list of EnumConstantInfo objects, each representing one constant.
     *
     * @param enumType The enum type being parsed
     * @param bodyText The text content of the enum body
     * @param bodyOffset The absolute offset where the enum body starts
     * @param keywordPattern Pattern to detect Java keywords (to skip them)
     * @return List of parsed enum constants
     */
    public static List<EnumConstantInfo> parseEnumConstants(
            ScriptTypeInfo enumType,
            String bodyText,
            int bodyOffset,
            Pattern keywordPattern) {

        List<EnumConstantInfo> constants = new ArrayList<>();

        // Find where enum constants end (first semicolon not in parens, or first method/field declaration)
        int constantsEnd = findEnumConstantsEnd(bodyText);
        if (constantsEnd <= 0) {
            // No semicolon found - entire body might be constants
            constantsEnd = bodyText.length();
        }

        // Pattern to match enum constants: NAME or NAME() or NAME(args)
        Pattern constantPattern = Pattern.compile(
                "([A-Za-z_][a-zA-Z0-9_]*)\\s*(\\(([^)]*)\\))?");

        Matcher m = constantPattern.matcher(bodyText);
        int lastEnd = 0;

        while (m.find()) {
            // Skip if we're past the constants section
            if (m.start() >= constantsEnd) {
                break;
            }
            
            int absPos = bodyOffset + m.start();
            if (ScriptDocument.INSTANCE.isExcluded(absPos)) {
                lastEnd = m.end();
                continue;
            }

            // Check if this is at a valid position (after comma or at start)
            String beforeMatch = bodyText.substring(lastEnd, m.start()).trim();
            if (!beforeMatch.isEmpty() && !beforeMatch.equals(",")) {
                // Not a valid enum constant position - might be inside parens
                continue;
            }

            String constantName = m.group(1);
            String argsClause = m.group(2);  // includes parens if present
            String args = m.group(3);        // just the args without parens

            // Skip keywords
            if (keywordPattern.matcher(constantName).matches()) {
                lastEnd = m.end();
                continue;
            }

            // Determine init range (the constructor arguments)
            int initStart = -1;
            int initEnd = -1;
            
            // Create MethodCallInfo for constructor validation if args present
            MethodCallInfo constructorCall = null;
            if (argsClause != null && !argsClause.isEmpty()) {
                initStart = bodyOffset + m.start(2);  // Position of '('
                initEnd = bodyOffset + m.end(2) - 1;     // Position after ')'
                
                constructorCall = createConstructorCall(
                        enumType,
                        constantName,
                        absPos,
                        initStart,
                        initEnd
                );
            } else if (enumType.hasConstructors()) {
                // No args provided, but enum has constructors - validate against no-arg constructor
                initStart = absPos + constantName.length();
                initEnd = initStart;

                constructorCall = createConstructorCall(
                        enumType,
                        constantName,
                        absPos,
                        initStart,
                        initEnd
                );
            }


            // Create FieldInfo for the enum constant
            FieldInfo fieldInfo = FieldInfo.enumConstant(
                    constantName,
                    enumType,
                    absPos,
                    args,
                    enumType,
                    initStart,
                    initEnd
            );
            
            EnumConstantInfo constantInfo = new EnumConstantInfo(fieldInfo, constructorCall, enumType);
            fieldInfo.setEnumConstantInfo(constantInfo);
            constants.add(constantInfo);

            lastEnd = m.end();
        }

        return constants;
    }

    /**
     * Create a MethodCallInfo for an enum constant's constructor call.
     * Validates the arguments against available constructors.
     */
    private static MethodCallInfo createConstructorCall(
            ScriptTypeInfo enumType,
            String constantName,
            int constantStart,
            int openParenPos,
            int closeParenPos) {

        List<MethodInfo> constructors = enumType.getConstructors();

        // Parse arguments - pass absolute positions (bodyOffset accounts for the offset into the document)
        List<MethodCallInfo.Argument> arguments = ScriptDocument.INSTANCE.parseMethodArguments(
                openParenPos + 1,
                closeParenPos,
                null
        );

        // Find matching constructor
        MethodInfo matchedConstructor = null;
        List<MethodInfo> candidates = new ArrayList<>();

        for (MethodInfo constructor : constructors) {
            candidates.add(constructor);
            if (constructor.getParameterCount() == arguments.size()) {
                matchedConstructor = constructor;
                break;
            }
        }

        // Create MethodCallInfo
        MethodCallInfo callInfo = new MethodCallInfo(
                constantName,  // Use constant name as "method" name
                constantStart,
                openParenPos,
                openParenPos,
                closeParenPos, 
                arguments,
                enumType,              // Receiver is the enum type
                matchedConstructor
        );


        callInfo.setConstructor(true);
        callInfo.validate();
        return callInfo;
    }
    

    /**
     * Find where enum constants section ends.
     * Returns the position of the first semicolon that's not inside parentheses,
     * or the position of the first method/field declaration.
     */
    private static int findEnumConstantsEnd(String bodyText) {
        int parenDepth = 0;
        boolean foundConstant = false;

        for (int i = 0; i < bodyText.length(); i++) {
            char c = bodyText.charAt(i);

            if (c == '(') {
                parenDepth++;
            } else if (c == ')') {
                parenDepth--;
            } else if (c == ';' && parenDepth == 0) {
                return i;
            } else if (c == '{' && parenDepth == 0) {
                // Start of method body - constants end before this
                return findStatementStart(bodyText, i);
            }

            // Track if we've found at least one identifier (potential constant)
            if (Character.isJavaIdentifierStart(c)) {
                foundConstant = true;
            }
        }

        // If we found constants but no semicolon, return the full length
        return foundConstant ? bodyText.length() : -1;
    }

    /**
     * Find the start of a statement by going backwards from a position.
     */
    private static int findStatementStart(String text, int fromPos) {
        int pos = fromPos - 1;
        int parenDepth = 0;

        while (pos >= 0) {
            char c = text.charAt(pos);
            if (c == ')')
                parenDepth++;
            else if (c == '(')
                parenDepth--;
            else if ((c == ',' || c == ';' || c == '{' || c == '}') && parenDepth == 0) {
                return pos + 1;
            }
            pos--;
        }
        return 0;
    }

    // ==================== GETTERS ====================

    public FieldInfo getFieldInfo() {
        return fieldInfo;
    }

    public MethodCallInfo getConstructorCall() {
        return constructorCall;
    }

    public ScriptTypeInfo getEnumType() {
        return enumType;
    }

    public String getName() {
        return fieldInfo.getName();
    }

    public int getDeclarationOffset() {
        return fieldInfo.getDeclarationOffset();
    }

    /**
     * Check if this enum constant has any errors (constructor mismatch, etc).
     */
    public boolean hasError() {
        return (constructorCall != null && constructorCall.hasError());
    }

    /**
     * Get the error message if any.
     */
    public String getErrorMessage() {
        return null;
    }
}
