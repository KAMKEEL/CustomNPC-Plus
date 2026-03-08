package noppes.npcs.client.gui.util.script.interpreter;

import noppes.npcs.client.gui.util.script.interpreter.field.FieldInfo;
import noppes.npcs.client.gui.util.script.interpreter.js_parser.JSTypeInfo;
import noppes.npcs.client.gui.util.script.interpreter.method.MethodInfo;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for JavaScript object literal (AKA dicts) analysis, parsing, type-shape inference,
 * and dynamic property access handling.
 *
 * <h3>Purpose</h3>
 * Encapsulates the logic for:
 * <ul>
 *   <li>Parsing object literal syntax ({@code { key: value, ... }}) into structured property lists.</li>
 *   <li>Inferring synthetic {@link TypeInfo} shapes from object literal contents for hover/autocomplete.</li>
 *   <li>Parsing dynamic property accesses ({@code obj.prop}, {@code obj["prop"]}) on the assignment LHS.</li>
 *   <li>Extending synthetic object shapes when new properties are assigned after initial declaration.</li>
 *   <li>Detecting synthetic object literal display names for UI rendering.</li>
 * </ul>
 *
 * <h3>V1 Scope</h3>
 * <ul>
 *   <li>Only simple identifier keys and quoted-string keys are supported. Computed keys ({@code [expr]: val})
 *       and spread ({@code ...obj}) are recognized but gracefully skipped.</li>
 *   <li>Nested object literals inside values degrade to {@link TypeInfo#ANY} rather than recursive inference.</li>
 *   <li>Value type inference delegates to the caller-supplied {@link ExpressionTypeResolverFn}; this class
 *       does not resolve types itself.</li>
 * </ul>
 *
 * <h3>Degradation Behavior</h3>
 * When the parser encounters unsupported syntax (computed keys, spread, deeply nested literals),
 * it sets {@link ObjectLiteralAnalysis#supportsInference} to {@code false} and returns partial
 * property data. Callers (e.g. mark building) can still use the parsed properties for key highlighting
 * even when full type inference is unavailable.
 *
 * <h3>Architecture Note</h3>
 * Methods that need access to ScriptDocument's type resolution ({@code resolveExpressionType},
 * {@code resolveVariable}) accept functional interfaces ({@link ExpressionTypeResolverFn},
 * {@link VariableResolverFn}) so that this class remains decoupled from ScriptDocument's internals.
 * ScriptDocument retains ownership of {@code markObjectLiteralKeys} and
 * {@code findMatchingBraceEndInDocument} which need direct access to document state (excluded ranges, text).
 */
public final class ObjectLiteralParser {

    private ObjectLiteralParser() {
        // Static utility class — not instantiable
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Functional interfaces for ScriptDocument callbacks
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Callback to resolve an expression's type given the expression text and its absolute position
     * within the document. Delegates to {@code ScriptDocument.resolveExpressionType(expr, position)}.
     */
    @FunctionalInterface
    public interface ExpressionTypeResolverFn {
        TypeInfo resolve(String expr, int position);
    }

    /** Resolves a script-declared method by name. Used to copy parameter/return info into callable properties. */
    @FunctionalInterface
    public interface MethodReferenceResolverFn {
        MethodInfo resolve(String methodName, TypeInfo[] argTypes);
    }

    /**
     * Callback to resolve a variable by name at a given document position.
     * Delegates to {@code ScriptDocument.resolveVariable(name, position)}.
     */
    @FunctionalInterface
    public interface VariableResolverFn {
        FieldInfo resolve(String name, int position);
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Data classes
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Result of analyzing an object literal expression.
     *
     * <p>Contains the parsed properties, whether full type inference is feasible,
     * and (when inference succeeded) the synthetic {@link TypeInfo} representing the object's shape.</p>
     */
    public static final class ObjectLiteralAnalysis {
        /** Parsed properties (key name, offsets, optional inferred value type). */
        public final List<ObjectLiteralProperty> properties;
        /**
         * {@code true} when every property was fully parseable and the closing brace was found,
         * meaning a synthetic type shape can be constructed. {@code false} when unsupported syntax
         * was encountered or the literal is incomplete.
         */
        public final boolean supportsInference;
        /**
         * The synthetic {@link TypeInfo} representing this object literal's shape, or {@code null}
         * when inference was not requested or not supported.
         */
        public final TypeInfo inferredType;

        ObjectLiteralAnalysis(List<ObjectLiteralProperty> properties, boolean supportsInference, TypeInfo inferredType) {
            this.properties = properties;
            this.supportsInference = supportsInference;
            this.inferredType = inferredType;
        }
    }

    /**
     * A single property inside an object literal.
     *
     * <p>Tracks the key name, whether the key was written as a bare identifier or a quoted string,
     * the absolute document offsets for both key and value spans, the inferred value type
     * (when type inference was requested), and optional callable metadata when the value is a
     * function expression, arrow function, or shorthand method.</p>
     */
    public static final class ObjectLiteralProperty {
        /** The property key name (without quotes if originally quoted). */
        public final String keyName;
        /** {@code true} if the key was a bare identifier; {@code false} if it was a quoted string. */
        public final boolean isIdentifierKey;
        /** Absolute start offset of the key in the document. */
        public final int keyStartAbs;
        /** Absolute end offset of the key in the document. */
        public final int keyEndAbs;
        /** Absolute start offset of the value expression in the document. */
        public final int valueStartAbs;
        /** Absolute end offset of the value expression in the document. */
        public final int valueEndAbs;
        /** The inferred type of the value expression, or {@code null} if inference was not requested. */
        public final TypeInfo valueType;
        /** Callable metadata if this property's value is a function expression, arrow, or shorthand method. */
        public final CallableInfo callableInfo;

        ObjectLiteralProperty(String keyName, boolean isIdentifierKey, int keyStartAbs, int keyEndAbs,
                              int valueStartAbs, int valueEndAbs, TypeInfo valueType, CallableInfo callableInfo) {
            this.keyName = keyName;
            this.isIdentifierKey = isIdentifierKey;
            this.keyStartAbs = keyStartAbs;
            this.keyEndAbs = keyEndAbs;
            this.valueStartAbs = valueStartAbs;
            this.valueEndAbs = valueEndAbs;
            this.valueType = valueType;
            this.callableInfo = callableInfo;
        }

        public boolean isCallable() {
            return callableInfo != null;
        }
    }

    /**
     * Represents a dynamic property access on the LHS of an assignment.
     *
     * <p>Two forms are supported:</p>
     * <ul>
     *   <li>Dot access: {@code obj.prop} → {@code receiverExpr="obj", propertyName="prop", bracketAccess=false}</li>
     *   <li>Bracket access: {@code obj["prop"]} → {@code receiverExpr="obj", propertyName="prop", bracketAccess=true}</li>
     * </ul>
     */
    public static final class DynamicPropertyAccess {
        /** The expression for the receiver object (everything before the dot or bracket). */
        public final String receiverExpr;
        /** The property name being accessed. */
        public final String propertyName;
        /** Absolute document offset where the receiver expression starts. */
        public final int receiverStart;
        /** {@code true} if this is bracket notation ({@code obj["prop"]}); {@code false} for dot notation. */
        public final boolean bracketAccess;

        DynamicPropertyAccess(String receiverExpr, String propertyName, int receiverStart, boolean bracketAccess) {
            this.receiverExpr = receiverExpr;
            this.propertyName = propertyName;
            this.receiverStart = receiverStart;
            this.bracketAccess = bracketAccess;
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Object literal analysis
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Analyze an object literal string and extract its properties.
     *
     * <p>The input is expected to start with {@code '{'} (after optional leading whitespace) and
     * may or may not be a complete literal. Properties are parsed into {@link ObjectLiteralProperty}
     * records. When {@code inferTypes} is {@code true} and inference is feasible, a synthetic
     * {@link TypeInfo} shape is constructed with fields matching the parsed properties.</p>
     *
     * @param objectLiteral   the source text of the object literal (starting at or near '{')
     * @param baseOffset      the absolute document offset corresponding to the start of {@code objectLiteral}
     * @param inferTypes      if {@code true}, attempt to resolve value types and build a synthetic shape
     * @param assumeComplete  if {@code true}, treat the entire string as a complete literal (no brace matching needed)
     * @param typeResolver    callback for resolving value expression types (may be {@code null} if {@code inferTypes} is false)
     * @param methodChecker   callback for checking if an identifier is a known script method (for callable detection)
     * @return the analysis result, or {@code null} if the input is not a valid object literal start
     */
    public static ObjectLiteralAnalysis parse(String objectLiteral, int baseOffset,
                                              boolean inferTypes, boolean assumeComplete,
                                              ExpressionTypeResolverFn typeResolver,
                                              MethodReferenceResolverFn methodResolver) {
        if (objectLiteral == null) {
            return null;
        }

        // Trim leading and trailing whitespace.
        int leading = 0;
        while (leading < objectLiteral.length() && Character.isWhitespace(objectLiteral.charAt(leading))) {
            leading++;
        }
        int trailing = objectLiteral.length();
        while (trailing > leading && Character.isWhitespace(objectLiteral.charAt(trailing - 1))) {
            trailing--;
        }

        String src = objectLiteral.substring(leading, trailing);
        int absBase = baseOffset + leading;

        // Verify this starts with '{'.
        if (src.isEmpty() || src.charAt(0) != '{') {
            return null;
        }

        // Find the matching '}' or use the entire string if assumeComplete is true.
        int endExclusive;
        if (assumeComplete) {
            endExclusive = src.length();
        } else {
            endExclusive = findMatchingObjectLiteralEnd(src);
            if (endExclusive <= 0) {
                return null;
            }
        }

        // Only attempt type inference if the literal is complete (closing brace found).
        boolean supportsInference = (endExclusive == src.length());
        int bodyStart = 1;
        int bodyEnd = endExclusive - 1;

        List<ObjectLiteralProperty> props = new ArrayList<>();
        boolean sawUnsupported = false;

        // Parse each property in the object literal body.
        int i = bodyStart;
        while (i < bodyEnd) {
            i = skipSeparatorsAndComments(src, i, bodyEnd);
            if (i >= bodyEnd) {
                break;
            }

            int keyStart = i;
            String keyName = null;
            boolean isIdentifierKey = false;

            char kc = src.charAt(i);
            // Parse key: quoted string or bare identifier or async method shorthand
            if (kc == '"' || kc == '\'') {
                // Quoted key — extract and validate
                int keyEnd = scanStringLiteralEnd(src, i);
                if (keyEnd < 0 || keyEnd > bodyEnd) {
                    sawUnsupported = true;
                    break;
                }
                keyName = src.substring(i + 1, keyEnd - 1);
                isIdentifierKey = false;
                if (!isSimpleIdentifier(keyName)) {
                    sawUnsupported = true;
                    i = scanToNextTopLevelComma(src, keyEnd, bodyEnd);
                    continue;
                }
                i = keyEnd;
            } else if (Character.isJavaIdentifierStart(kc)) {
                // Bare identifier key
                i++;
                while (i < bodyEnd && Character.isJavaIdentifierPart(src.charAt(i))) {
                    i++;
                }
                keyName = src.substring(keyStart, i);
                isIdentifierKey = true;
                
                // Handle 'async methodName()' shorthand syntax
                if ("async".equals(keyName)) {
                    int afterAsync = i;
                    while (afterAsync < bodyEnd && Character.isWhitespace(src.charAt(afterAsync))) {
                        afterAsync++;
                    }
                    if (afterAsync < bodyEnd && Character.isJavaIdentifierStart(src.charAt(afterAsync))) {
                        int realKeyStart = afterAsync;
                        afterAsync++;
                        while (afterAsync < bodyEnd && Character.isJavaIdentifierPart(src.charAt(afterAsync))) {
                            afterAsync++;
                        }
                        keyStart = realKeyStart;
                        keyName = src.substring(realKeyStart, afterAsync);
                        i = afterAsync;
                    }
                }
            } else if (kc == '[') {
                // Computed key — unsupported for inference, skip to next property
                sawUnsupported = true;
                i = scanToNextTopLevelComma(src, i, bodyEnd);
                continue;
            } else if (kc == '.' && i + 2 < bodyEnd && src.charAt(i + 1) == '.' && src.charAt(i + 2) == '.') {
                // Spread syntax — unsupported, skip
                sawUnsupported = true;
                i = scanToNextTopLevelComma(src, i, bodyEnd);
                continue;
            } else {
                sawUnsupported = true;
                i = scanToNextTopLevelComma(src, i, bodyEnd);
                continue;
            }

            int keyEnd = i;
            while (i < bodyEnd && Character.isWhitespace(src.charAt(i))) {
                i++;
            }

            // Check for shorthand method syntax: key() { ... }
            if (i < bodyEnd && src.charAt(i) == '(' && isIdentifierKey) {
                int parenOpen = i;
                int parenClose = findMatchingParen(src, parenOpen);
                if (parenClose < 0 || parenClose >= bodyEnd) {
                    sawUnsupported = true;
                    i = scanToNextTopLevelComma(src, i, bodyEnd);
                    continue;
                }

                // Extract parameter names and infer return type from the method body.
                List<String> params = extractParameterNames(src.substring(parenOpen + 1, parenClose));
                int afterParen = parenClose + 1;
                while (afterParen < bodyEnd && Character.isWhitespace(src.charAt(afterParen))) {
                    afterParen++;
                }
                // Expect a '{' after the parameter list
                if (afterParen >= bodyEnd || src.charAt(afterParen) != '{') {
                    sawUnsupported = true;
                    i = scanToNextTopLevelComma(src, afterParen, bodyEnd);
                    continue;
                }

                int braceEnd = findMatchingObjectLiteralEnd(src.substring(afterParen));
                if (braceEnd <= 0) {
                    sawUnsupported = true;
                    break;
                }
                int methodBodyEnd = afterParen + braceEnd;
                String bodyText = src.substring(afterParen, methodBodyEnd);
                TypeInfo returnType = inferReturnTypeFromBlockBody(bodyText, absBase + afterParen, typeResolver);

                CallableInfo callableInfo = new CallableInfo(params, returnType,
                        absBase + afterParen, absBase + methodBodyEnd, true);
                int keyStartAbs = absBase + keyStart;
                int keyEndAbs = absBase + keyEnd;
                int valueStartAbs = absBase + parenOpen;
                int valueEndAbs = absBase + methodBodyEnd;

                props.add(new ObjectLiteralProperty(keyName, true, keyStartAbs, keyEndAbs,
                        valueStartAbs, valueEndAbs, returnType, callableInfo));
                i = methodBodyEnd;
                while (i < bodyEnd && Character.isWhitespace(src.charAt(i))) {
                    i++;
                }
                if (i < bodyEnd && src.charAt(i) == ',') {
                    i++;
                }
                continue;
            }

            // Regular property: key: value
            if (i >= bodyEnd || src.charAt(i) != ':') {
                sawUnsupported = true;
                i = scanToNextTopLevelComma(src, i, bodyEnd);
                continue;
            }
            i++;

            while (i < bodyEnd && Character.isWhitespace(src.charAt(i))) {
                i++;
            }
            int valueStart = i;
            int valueEnd = scanTopLevelValueEnd(src, valueStart, bodyEnd);
            if (valueEnd < valueStart) {
                sawUnsupported = true;
                break;
            }

            // Try to detect callable or infer value type
            TypeInfo valueType = null;
            CallableInfo callableInfo = null;
            if (inferTypes && typeResolver != null) {
                String valueExpr = src.substring(valueStart, valueEnd).trim();
                callableInfo = detectCallableExpression(valueExpr, absBase + valueStart, methodResolver, typeResolver);
                if (callableInfo != null) {
                    valueType = callableInfo.returnType;
                } else {
                    valueType = inferValueType(valueExpr, absBase + valueStart, typeResolver);
                }
            }

            int keyStartAbs = absBase + keyStart;
            int keyEndAbs = absBase + keyEnd;
            int valueStartAbs = absBase + valueStart;
            int valueEndAbs = absBase + valueEnd;

            if (keyName != null) {
                props.add(new ObjectLiteralProperty(keyName, isIdentifierKey, keyStartAbs, keyEndAbs,
                        valueStartAbs, valueEndAbs, valueType, callableInfo));
            }

            i = valueEnd;
            i = skipSeparatorsAndComments(src, i, bodyEnd);
        }

        if (sawUnsupported) {
            supportsInference = false;
        }

        TypeInfo inferredType = null;
        if (inferTypes && supportsInference) {
            inferredType = TypeInfo.objectLiteral(props);
        }

        return new ObjectLiteralAnalysis(props, supportsInference, inferredType);
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Synthetic object literal display name detection
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Determine whether a {@link JSTypeInfo} represents a synthetic object literal type,
     * and if so return its display name ({@code "Object"}).
     *
     * <p>Synthetic object literal types are created by {@link #parse} with
     * names of the form {@code __ObjectLiteral$<offset>} under the {@code __synthetic__} namespace.
     * This method recognizes that naming convention and maps it to a human-readable display name.</p>
     *
     * @param jsType the JS type info to inspect (may be {@code null})
     * @return {@code "Object"} if this is a synthetic object literal type, {@code null} otherwise
     */
    public static String getSyntheticObjectLiteralDisplayName(JSTypeInfo jsType) {
        if (jsType == null) {
            return null;
        }
        String namespace = jsType.getNamespace();
        String simple = jsType.getSimpleName();
        if ("__synthetic__".equals(namespace) && simple != null && simple.startsWith("__ObjectLiteral$")) {
            return "Object";
        }
        return null;
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Dynamic property access parsing
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Parse a left-hand-side expression to detect dynamic property access patterns.
     *
     * <p>Recognizes two forms:</p>
     * <ul>
     *   <li>Dot access: {@code obj.prop} — the last dot-separated segment is treated as the property name.</li>
     *   <li>Bracket access: {@code obj["prop"]} or {@code obj['prop']} — the quoted string inside brackets
     *       is treated as the property name.</li>
     * </ul>
     *
     * <p>Only simple identifier property names are accepted. Complex expressions (e.g., {@code obj[expr]}
     * where {@code expr} is not a string literal) return {@code null}.</p>
     *
     * @param lhs      the left-hand-side expression text
     * @param lhsStart the absolute document offset of the LHS start
     * @return a {@link DynamicPropertyAccess} if a valid pattern was detected, {@code null} otherwise
     */
    public static DynamicPropertyAccess parseDynamicPropertyAccess(String lhs, int lhsStart) {
        if (lhs == null || lhs.isEmpty()) {
            return null;
        }

        int dotIndex = lhs.lastIndexOf('.');
        int bracketIndex = lhs.lastIndexOf('[');

        if (dotIndex >= 0 && dotIndex > bracketIndex) {
            String receiverExpr = lhs.substring(0, dotIndex).trim();
            String propertyName = lhs.substring(dotIndex + 1).trim();
            if (!receiverExpr.isEmpty() && isSimpleIdentifier(propertyName)) {
                return new DynamicPropertyAccess(receiverExpr, propertyName, lhsStart, false);
            }
            return null;
        }

        if (bracketIndex >= 0 && lhs.endsWith("]")) {
            String receiverExpr = lhs.substring(0, bracketIndex).trim();
            String keyExpr = lhs.substring(bracketIndex + 1, lhs.length() - 1).trim();
            String propertyName = extractQuotedPropertyName(keyExpr);
            if (!receiverExpr.isEmpty() && propertyName != null && isSimpleIdentifier(propertyName)) {
                return new DynamicPropertyAccess(receiverExpr, propertyName, lhsStart, true);
            }
        }

        return null;
    }

    /**
     * Resolve the receiver type for a dynamic property access by first trying expression type resolution
     * and then falling back to variable resolution.
     *
     * @param access           the dynamic property access to resolve
     * @param typeResolver     callback for expression type resolution
     * @param variableResolver callback for variable resolution
     * @return the resolved receiver {@link TypeInfo}, or {@code null} if resolution failed
     */
    public static TypeInfo resolveDynamicPropertyReceiverType(DynamicPropertyAccess access,
                                                              ExpressionTypeResolverFn typeResolver,
                                                              VariableResolverFn variableResolver) {
        if (access == null) {
            return null;
        }

        TypeInfo receiverType = typeResolver.resolve(access.receiverExpr, access.receiverStart);
        if (receiverType != null && receiverType.isResolved()) {
            return receiverType;
        }

        FieldInfo receiverField = variableResolver.resolve(access.receiverExpr, access.receiverStart);
        if (receiverField != null) {
            return receiverField.getTypeInfo();
        }

        return null;
    }

    /**
     * Extract a property name from a quoted string expression.
     *
     * <p>Handles both single and double quotes, plus basic backslash escapes.
     * Returns {@code null} if the expression is not a properly quoted string.</p>
     *
     * @param expr the expression to extract from (e.g., {@code "prop"} or {@code 'prop'})
     * @return the unquoted, unescaped property name, or {@code null} if extraction failed
     */
    public static String extractQuotedPropertyName(String expr) {
        if (expr == null || expr.length() < 2) {
            return null;
        }
        char quote = expr.charAt(0);
        if ((quote != '"' && quote != '\'') || expr.charAt(expr.length() - 1) != quote) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < expr.length() - 1; i++) {
            char c = expr.charAt(i);
            if (c == '\\') {
                if (i + 1 >= expr.length() - 1) {
                    return null;
                }
                char escaped = expr.charAt(++i);
                sb.append(escaped);
                continue;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * Extend a synthetic object literal shape with a new property (or update an existing one).
     *
     * <p>This is called when an assignment like {@code obj.newProp = value} is encountered and
     * {@code obj} was previously inferred as a synthetic object literal type. The new property
     * is added to the underlying {@link JSTypeInfo} so subsequent hover/autocomplete sees it.</p>
     *
     * <p>Only operates on types that are recognized as synthetic object literals
     * (see {@link #getSyntheticObjectLiteralDisplayName}). Returns {@code false} and does nothing
     * if the receiver type is not a synthetic object literal.</p>
     *
     * @param receiverType  the type of the receiver object
     * @param propertyName  the name of the property to add/update
     * @param propertyType  the inferred type of the property value (defaults to {@link TypeInfo#ANY} if null/unresolved)
     * @return {@code true} if the shape was extended/updated, {@code false} if the receiver is not a synthetic object literal
     */
    public static boolean extendSyntheticObjectShape(TypeInfo receiverType, String propertyName, TypeInfo propertyType) {
        if (receiverType == null || propertyName == null || propertyName.isEmpty()) {
            return false;
        }
        if (!receiverType.isSyntheticObjectLiteralType()) {
            return false;
        }
        TypeInfo safeType = (propertyType != null && propertyType.isResolved()) ? propertyType : TypeInfo.ANY;
        receiverType.removeSyntheticField(propertyName);
        receiverType.addSyntheticField(propertyName, safeType);
        return true;
    }

    /** Holds the resolved receiver type, property name, and field for a dynamic property access result. */
    public static final class DynamicFieldResult {
        public final TypeInfo receiverType;
        public final String propertyName;
        public final FieldInfo field;

        DynamicFieldResult(TypeInfo receiverType, String propertyName, FieldInfo field) {
            this.receiverType = receiverType;
            this.propertyName = propertyName;
            this.field = field;
        }
    }

    /** Resolve an existing field on a dynamic property access receiver without modifying the type shape. */
    public static DynamicFieldResult resolveExistingField(DynamicPropertyAccess access,
            ExpressionTypeResolverFn typeResolver, VariableResolverFn varResolver) {
        TypeInfo receiver = resolveDynamicPropertyReceiverType(access, typeResolver, varResolver);
        FieldInfo field = (receiver != null && receiver.isResolved()) ? receiver.getFieldInfo(access.propertyName) : null;
        return new DynamicFieldResult(receiver, access.propertyName, field);
    }

    /** Extend a synthetic object's shape with a new property, then return the resolved field. Overload without RHS text. */
    public static DynamicFieldResult extendAndGetField(DynamicPropertyAccess access,
            TypeInfo existingReceiver, TypeInfo sourceType,
            ExpressionTypeResolverFn typeResolver, VariableResolverFn varResolver) {
        return extendAndGetField(access, existingReceiver, sourceType, null, typeResolver, varResolver);
    }

    /**
     * Extend a synthetic object's shape with a new property, then return the resolved field.
     * When {@code rhsText} is a function expression, a synthetic method is registered instead of a plain field.
     */
    public static DynamicFieldResult extendAndGetField(DynamicPropertyAccess access,
            TypeInfo existingReceiver, TypeInfo sourceType, String rhsText,
            ExpressionTypeResolverFn typeResolver, VariableResolverFn varResolver) {
        // Prefer the already-resolved receiver to avoid a redundant re-resolution.
        TypeInfo receiver = (existingReceiver != null && existingReceiver.isResolved())
                ? existingReceiver
                : resolveDynamicPropertyReceiverType(access, typeResolver, varResolver);
        if (receiver == null || !receiver.isSyntheticObjectLiteralType()) return null;

        // If the RHS looks like a callable (function/arrow/ref), register it as a method; otherwise as a field.
        CallableInfo callable = (rhsText != null) ? detectCallableExpression(rhsText.trim(), -1, null, typeResolver) : null;
        if (callable != null) {
            extendSyntheticObjectWithMethod(receiver, access.propertyName, callable);
        } else {
            if (!extendSyntheticObjectShape(receiver, access.propertyName, sourceType)) return null;
        }
        return new DynamicFieldResult(receiver, access.propertyName, receiver.getFieldInfo(access.propertyName));
    }

    /**
     * Metadata for a callable property value (function expression, arrow function, or shorthand method).
     *
     * <p>Captures the parameter names and the inferred return type so that the property can be
     * registered as a synthetic method on the object literal's type.</p>
     */
    public static final class CallableInfo {
        private static final TypeInfo THIS_RETURN_SENTINEL = TypeInfo.unresolved("<object_literal_this>", "<object_literal_this>");
        public final List<String> parameterNames;
        public final TypeInfo returnType;
        public final int bodyStartAbs;
        public final int bodyEndAbs;
        public final boolean blockBody;

        CallableInfo(List<String> parameterNames, TypeInfo returnType, int bodyStartAbs, int bodyEndAbs, boolean blockBody) {
            this.parameterNames = parameterNames != null ? parameterNames : new ArrayList<>();
            this.returnType = returnType;
            this.bodyStartAbs = bodyStartAbs;
            this.bodyEndAbs = bodyEndAbs;
            this.blockBody = blockBody;
        }

        public boolean returnsThis() {
            return returnType == THIS_RETURN_SENTINEL;
        }

        static TypeInfo thisReturnSentinel() {
            return THIS_RETURN_SENTINEL;
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Callable expression detection
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Detect whether a value expression is callable (function expression, arrow function, or bare script method reference).
     * Returns a {@link CallableInfo} with parameter names and inferred return type, or {@code null} if not callable.
     */
    static CallableInfo detectCallableExpression(String expr, int exprStartAbs,
                                                 MethodReferenceResolverFn methodResolver,
                                                 ExpressionTypeResolverFn typeResolver) {
        if (expr == null || expr.isEmpty()) return null;

        // Inline function expression: function(...) { ... }
        if (expr.startsWith("function")) {
            return parseFunctionExpressionCallable(expr, exprStartAbs, typeResolver);
        }

        // Arrow function: (...) => ... or x => ...
        if (expr.contains("=>")) {
            return parseArrowFunctionCallable(expr, exprStartAbs, typeResolver);
        }

        // Bare identifier that resolves to a known script method (e.g. onTick: handleTick)
        if (isSimpleIdentifier(expr.trim()) && methodResolver != null) {
            MethodInfo method = methodResolver.resolve(expr.trim(), new TypeInfo[0]);
            if (method != null) {
                TypeInfo returnType = method.getReturnType() != null ? method.getReturnType() : TypeInfo.ANY;
                List<String> paramNames = new ArrayList<>();
                for (FieldInfo param : method.getParameters()) paramNames.add(param.getName());
                return new CallableInfo(paramNames, returnType, exprStartAbs, exprStartAbs + expr.trim().length(), false);
            }
        }

        return null;
    }

    /** Parse a {@code function(...) { ... }} expression into a CallableInfo, extracting params and inferring return type. */
    private static CallableInfo parseFunctionExpressionCallable(String expr, int exprStartAbs,
                                                                 ExpressionTypeResolverFn typeResolver) {
        int parenOpen = expr.indexOf('(');
        if (parenOpen < 0) return null;
        int parenClose = findMatchingParen(expr, parenOpen);
        if (parenClose < 0) return null;

        List<String> params = extractParameterNames(expr.substring(parenOpen + 1, parenClose));
        TypeInfo returnType = inferReturnTypeFromBody(expr, parenClose + 1, exprStartAbs, typeResolver);
        String rest = expr.substring(parenClose + 1).trim();
        int bodyOffset = expr.indexOf(rest, parenClose + 1);
        return new CallableInfo(params, returnType,
                exprStartAbs >= 0 ? exprStartAbs + Math.max(bodyOffset, 0) : -1,
                exprStartAbs >= 0 ? exprStartAbs + expr.length() : -1,
                rest.startsWith("{"));
    }

    /**
     * Parse an arrow function — handles both parenthesized params {@code (a, b) => ...}
     * and single bare param {@code x => ...}.
     */
    private static CallableInfo parseArrowFunctionCallable(String expr, int exprStartAbs,
                                                           ExpressionTypeResolverFn typeResolver) {
        int arrowIdx = findArrowOperator(expr);
        if (arrowIdx < 0) return null;

        String paramPart = expr.substring(0, arrowIdx).trim();
        List<String> params;
        if (paramPart.startsWith("(") && paramPart.endsWith(")")) {
            // Parenthesized parameter list
            params = extractParameterNames(paramPart.substring(1, paramPart.length() - 1));
        } else {
            // Single bare parameter (no parens)
            params = new ArrayList<>();
            if (isSimpleIdentifier(paramPart)) {
                params.add(paramPart);
            }
        }

        String bodyPart = expr.substring(arrowIdx + 2).trim();
        int bodyOffset = expr.indexOf(bodyPart, arrowIdx + 2);
        int bodyStartAbs = exprStartAbs >= 0 ? exprStartAbs + Math.max(bodyOffset, 0) : -1;
        // Route both block and expression bodies through the shared inference path
        TypeInfo returnType = inferReturnTypeFromBody(bodyPart, 0, bodyStartAbs, typeResolver);
        return new CallableInfo(params, returnType,
                bodyStartAbs,
                bodyStartAbs >= 0 ? bodyStartAbs + bodyPart.length() : -1,
                bodyPart.startsWith("{"));
    }

    /**
     * Find the {@code =>} operator in an arrow function expression, skipping strings and nested parentheses
     * to avoid false positives from comparison operators inside nested expressions.
     */
    private static int findArrowOperator(String expr) {
        boolean inString = false;
        char stringChar = 0; 
        int parenDepth = 0;

        for (int i = 0; i < expr.length() - 1; i++) {
            char c = expr.charAt(i);
            if (inString) {
                if (c == '\\') { i++; continue; }
                if (c == stringChar) inString = false;
                continue;
            }
            if (c == '"' || c == '\'') { inString = true; stringChar = c; continue; }
            if (c == '(') parenDepth++;
            else if (c == ')') parenDepth = Math.max(0, parenDepth - 1);

            if (c == '=' && expr.charAt(i + 1) == '>' && parenDepth == 0) {
                return i;
            }
        }
        return -1;
    }

    /** Split a comma-separated parameter list into individual names, stripping any leading type annotation. */
    private static List<String> extractParameterNames(String paramList) {
        List<String> params = new ArrayList<>();
        if (paramList == null || paramList.trim().isEmpty()) return params;

        for (String part : paramList.split(",")) {
            String name = part.trim();
            int spaceIdx = name.indexOf(' ');
            if (spaceIdx > 0) name = name.substring(0, spaceIdx);
            if (isSimpleIdentifier(name)) {
                params.add(name);
            }
        }
        return params;
    }

    /** Find the closing {@code )} matching the opening {@code (} at {@code openPos}, respecting strings and nesting. */
    private static int findMatchingParen(String src, int openPos) {
        int depth = 0;
        boolean inString = false;
        char stringChar = 0;
        for (int i = openPos; i < src.length(); i++) {
            char c = src.charAt(i);
            if (inString) {
                if (c == '\\') { i++; continue; }
                if (c == stringChar) inString = false;
                continue;
            }
            if (c == '"' || c == '\'') { inString = true; stringChar = c; continue; }
            if (c == '(') depth++;
            else if (c == ')') { depth--; if (depth == 0) return i; }
        }
        return -1;
    }

    /** Infer the return type from the portion of a function expression after the parameter list. */
    private static TypeInfo inferReturnTypeFromBody(String expr, int afterParams,
                                                    int exprStartAbs, ExpressionTypeResolverFn typeResolver) {
        String rest = expr.substring(afterParams).trim();
        int restOffset = expr.indexOf(rest, afterParams);
        int restStartAbs = exprStartAbs >= 0 ? exprStartAbs + restOffset : -1;
        if (rest.startsWith("{")) {
            return inferReturnTypeFromBlockBody(rest, restStartAbs, typeResolver);
        }
        return inferReturnTypeFromExpressionBody(rest, restStartAbs, typeResolver);
    }

    /**
     * Infer a return type from a block body {@code { ... }} by scanning {@code return} statements
     * and resolving each expression via the type resolver. Falls back to {@code ANY} if nothing resolves.
     */
    private static TypeInfo inferReturnTypeFromBlockBody(String body, int bodyStartAbs,
                                                         ExpressionTypeResolverFn typeResolver) {
        if (body == null || !body.startsWith("{")) return TypeInfo.ANY;

        int endBrace = findMatchingObjectLiteralEnd(body);
        if (endBrace <= 0) return TypeInfo.ANY;

        String inner = body.substring(1, endBrace - 1);
        int innerStartAbs = bodyStartAbs >= 0 ? bodyStartAbs + 1 : -1;

        // Scan for 'return <expr>' statements and resolve each one.
        int i = 0;
        while (i < inner.length()) {
            int returnIdx = inner.indexOf("return", i);
            if (returnIdx < 0) break;

            // Ensure 'return' is a keyword boundary, not part of a longer identifier.
            if (returnIdx > 0 && Character.isJavaIdentifierPart(inner.charAt(returnIdx - 1))) {
                i = returnIdx + 6;
                continue;
            }
            int afterReturn = returnIdx + 6;
            if (afterReturn >= inner.length()) break;
            char afterChar = inner.charAt(afterReturn);
            if (!Character.isWhitespace(afterChar) && afterChar != ';') {
                i = afterReturn;
                continue;
            }

            while (afterReturn < inner.length() && Character.isWhitespace(inner.charAt(afterReturn))) {
                afterReturn++;
            }

            // Scan to end of the return expression (stop at ';' or newline at depth 0).
            int exprEnd = afterReturn;
            int depth = 0;
            while (exprEnd < inner.length()) {
                char c = inner.charAt(exprEnd);
                if (c == '{' || c == '(' || c == '[') depth++;
                else if (c == '}' || c == ')' || c == ']') { if (depth == 0) break; depth--; }
                else if ((c == ';' || c == '\n' || c == '\r') && depth == 0) break;
                exprEnd++;
            }

            String returnExpr = inner.substring(afterReturn, exprEnd).trim();
            if (returnExpr.isEmpty()) { i = exprEnd + 1; continue; }

            if ("this".equals(returnExpr)) return CallableInfo.thisReturnSentinel();

            // Try full type resolution, then fall back to literal matching.
            if (typeResolver != null && innerStartAbs >= 0) {
                TypeInfo resolved = typeResolver.resolve(returnExpr, innerStartAbs + afterReturn);
                if (resolved != null && resolved.isResolved()) return resolved;
            }
            TypeInfo literal = resolveLiteralType(returnExpr);
            if (literal != TypeInfo.ANY) return literal;

            i = exprEnd + 1;
        }

        return TypeInfo.ANY;
    }

    /**
     * Infer a return type from a concise arrow-function body (expression, not block).
     * Delegates to the type resolver first, then falls back to literal pattern matching.
     */
    private static TypeInfo inferReturnTypeFromExpressionBody(String bodyExpr, int bodyStartAbs,
                                                               ExpressionTypeResolverFn typeResolver) {
         if (bodyExpr == null || bodyExpr.isEmpty()) return TypeInfo.ANY;
        bodyExpr = bodyExpr.trim();

        if ("this".equals(bodyExpr)) return CallableInfo.thisReturnSentinel();

        // Full resolution via ScriptDocument — covers variables, method calls, chains, etc.
        if (typeResolver != null && bodyStartAbs >= 0) {
            TypeInfo resolved = typeResolver.resolve(bodyExpr, bodyStartAbs);
            if (resolved != null && resolved.isResolved()) return resolved;
        }

        return resolveLiteralType(bodyExpr);
    }

    /** Match simple literal expressions to their TypeInfo without needing a resolver. */
    private static TypeInfo resolveLiteralType(String expr) {
        if (expr.startsWith("{") && expr.endsWith("}")) return TypeInfo.ANY;
        if (expr.startsWith("\"") && expr.endsWith("\"")) return TypeInfo.string();
        if (expr.startsWith("'") && expr.endsWith("'")) return TypeInfo.fromPrimitive("char");
        if (expr.equals("true") || expr.equals("false")) return TypeInfo.fromPrimitive("boolean");
        if (expr.matches("[-+]?\\d*\\.?\\d+[fFdD]?")) return TypeInfo.NUMBER;
        if (expr.contains("+") || expr.contains("-") || expr.contains("*") || expr.contains("/") || expr.contains("%")) return TypeInfo.NUMBER;
        return TypeInfo.ANY;
    }

    /**
     * Register a callable property as both a synthetic field (for field-access resolution)
     * and a synthetic method (for call-site resolution) on the receiver's type shape.
     */
    private static void extendSyntheticObjectWithMethod(TypeInfo receiver, String name, CallableInfo callable) {
        // Build typed parameter list — all params default to ANY since we only have names, not types.
        List<FieldInfo> params = new ArrayList<>();
        for (String paramName : callable.parameterNames) {
            params.add(FieldInfo.external(paramName, TypeInfo.ANY, null, java.lang.reflect.Modifier.PUBLIC));
        }
        // Use 'receiver' as the return type sentinel when the method returns 'this'.
        TypeInfo returnType = callable.returnsThis() ? receiver : (callable.returnType != null ? callable.returnType : TypeInfo.ANY);
        receiver.removeSyntheticField(name);
        receiver.addSyntheticField(name, TypeInfo.ANY);
        receiver.removeSyntheticMethod(name);
        receiver.addSyntheticMethod(name, returnType, params);
    }

    /**
     * Walk backward from the start of an inner callable scope to find the enclosing object literal and
     * attach its inferred {@link TypeInfo} as the scope's containing object type (enabling {@code this.} resolution).
     * Handles three cases: {@code key: function(...)} (colon), {@code obj = { ... }} (equals), and shorthand methods.
     */
    static void attachObjectLiteralContext(InnerCallableScope scope, ScriptDocument doc) {
        // Only applicable for JS; skip if already has a containing object type.
        if (!doc.isJavaScript() || scope == null || scope.getContainingObjectType() != null) {
            return;
        }
        String text = doc.getText();
        int headerStart = scope.getHeaderStart();
        if (headerStart < 0 || headerStart >= text.length()) {
            return;
        }

        // Walk backward from the scope header to find a ':' or '=' that indicates object literal context.
        int searchPos = headerStart - 1;
        while (searchPos >= 0 && Character.isWhitespace(text.charAt(searchPos))) {
            searchPos--;
        }
        if (searchPos < 0) {
            return;
        }

        // Find the opening '{' of the enclosing object literal by walking backward, respecting nesting and strings.
        int braceStart = -1;
        if (text.charAt(searchPos) == ':') {
            // Case 1: key: function(...) — find the containing {...}
            searchPos--;
            int depth = 0;
            boolean inString = false;
            char stringChar = 0;
            for (int i = searchPos; i >= 0; i--) {
                char c = text.charAt(i);
                if (doc.isExcluded(i)) {
                    continue;
                }
                if (inString) {
                    if (c == stringChar && (i == 0 || text.charAt(i - 1) != '\\')) {
                        inString = false;
                    }
                    continue;
                }
                if (c == '"' || c == '\'') {
                    inString = true;
                    stringChar = c;
                    continue;
                }
                if (c == '}') {
                    depth++;
                } else if (c == '{') {
                    if (depth == 0) {
                        braceStart = i;
                        break;
                    }
                    depth--;
                }
            }
        } else if (text.charAt(searchPos) == '=') {
            // Case 2: variable = {...} — find the containing {...}
            searchPos--;
            while (searchPos >= 0 && Character.isWhitespace(text.charAt(searchPos))) {
                searchPos--;
            }
            int depth = 0;
            boolean inString = false;
            char stringChar = 0;
            for (int i = searchPos; i >= 0; i--) {
                char c = text.charAt(i);
                if (doc.isExcluded(i)) {
                    continue;
                }
                if (inString) {
                    if (c == stringChar && (i == 0 || text.charAt(i - 1) != '\\')) {
                        inString = false;
                    }
                    continue;
                }
                if (c == '"' || c == '\'') {
                    inString = true;
                    stringChar = c;
                    continue;
                }
                if (c == '}') {
                    depth++;
                } else if (c == '{') {
                    if (depth == 0) {
                        braceStart = i;
                        break;
                    }
                    depth--;
                }
            }
        } else if (scope.getKind() == InnerCallableScope.Kind.JS_SHORTHAND_METHOD) {
            // Case 3: Shorthand method inside {...} — find the containing {...}
            int depth = 0;
            boolean inString = false;
            char stringChar = 0;
            for (int i = searchPos; i >= 0; i--) {
                char c = text.charAt(i);
                if (doc.isExcluded(i)) {
                    continue;
                }
                if (inString) {
                    if (c == stringChar && (i == 0 || text.charAt(i - 1) != '\\')) {
                        inString = false;
                    }
                    continue;
                }
                if (c == '"' || c == '\'') {
                    inString = true;
                    stringChar = c;
                    continue;
                }
                if (c == '}') {
                    depth++;
                } else if (c == '{') {
                    if (depth == 0) {
                        braceStart = i;
                        break;
                    }
                    depth--;
                }
            }
        }

        if (braceStart < 0) {
            return;
        }

        ObjectLiteralAnalysis analysis = doc.getObjectLiteral(braceStart);
        if (analysis != null && analysis.inferredType != null) {
            scope.setContainingObjectType(analysis.inferredType);
        }
    }

    
    /**
     * Infer the TypeInfo for a single value expression inside an object literal.
     * Nested object literals are recursively parsed; arrays and other complex expressions fall back to {@code ANY}.
     */
    private static TypeInfo inferValueType(String valueExpr, int valueStartAbs, ExpressionTypeResolverFn typeResolver) {
        if (valueExpr == null || valueExpr.isEmpty()) {
            return TypeInfo.ANY;
        }

        char first = valueExpr.charAt(0);
        if (first == '{') {
            ObjectLiteralAnalysis nested = parse(valueExpr, valueStartAbs, true, false, typeResolver,null);
            if (nested != null && nested.supportsInference && nested.inferredType != null) {
                return nested.inferredType;
            }
            return TypeInfo.ANY;
        }

        TypeInfo inferred = typeResolver.resolve(valueExpr, valueStartAbs);
        return (inferred != null) ? inferred : TypeInfo.ANY;
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Scanning / parsing utilities (package-private static, used by analyzer)
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Scan to the end of a string literal starting at {@code start}.
     *
     * <p>Handles backslash escapes. The returned index points one past the closing quote character.</p>
     *
     * @param src   the source text
     * @param start index of the opening quote character
     * @return index one past the closing quote, or {@code -1} if the string is unterminated
     */
    static int scanStringLiteralEnd(String src, int start) {
        if (start < 0 || start >= src.length()) {
            return -1;
        }
        char quote = src.charAt(start);
        int i = start + 1;
        while (i < src.length()) {
            char c = src.charAt(i);
            if (c == '\\') {
                i += (i + 1 < src.length()) ? 2 : 1;
                continue;
            }
            if (c == quote) {
                return i + 1;
            }
            i++;
        }
        return -1;
    }

    /**
     * Check whether a string is a valid simple identifier (Java identifier rules).
     *
     * @param s the string to check
     * @return {@code true} if {@code s} is non-empty and consists of valid Java identifier characters
     */
    static boolean isSimpleIdentifier(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        if (!Character.isJavaIdentifierStart(s.charAt(0))) {
            return false;
        }
        for (int i = 1; i < s.length(); i++) {
            if (!Character.isJavaIdentifierPart(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Scan forward to find the end of a top-level value expression within an object literal.
     *
     * <p>Respects nested braces, brackets, parentheses, string literals, and comments.
     * Stops at a top-level comma or the {@code endLimit}.</p>
     *
     * @param src      the source text
     * @param start    where to start scanning
     * @param endLimit the exclusive boundary (typically the position of the closing brace)
     * @return the index where the value expression ends (at a comma or {@code endLimit})
     */
    static int scanTopLevelValueEnd(String src, int start, int endLimit) {
        int parenDepth = 0;
        int bracketDepth = 0;
        int braceDepth = 0;

        boolean inString = false;
        char stringChar = 0;
        boolean inLineComment = false;
        boolean inBlockComment = false;

        int i = start;
        while (i < endLimit) {
            char c = src.charAt(i);
            char next = (i + 1 < endLimit) ? src.charAt(i + 1) : 0;

            if (inLineComment) {
                if (c == '\n' || c == '\r') {
                    inLineComment = false;
                }
                i++;
                continue;
            }

            if (inBlockComment) {
                if (c == '*' && next == '/') {
                    inBlockComment = false;
                    i += 2;
                    continue;
                }
                i++;
                continue;
            }

            if (inString) {
                if (c == '\\') {
                    i += (i + 1 < endLimit) ? 2 : 1;
                    continue;
                }
                if (c == stringChar) {
                    inString = false;
                }
                i++;
                continue;
            }

            if (c == '/' && next == '/') {
                inLineComment = true;
                i += 2;
                continue;
            }
            if (c == '/' && next == '*') {
                inBlockComment = true;
                i += 2;
                continue;
            }

            if (c == '"' || c == '\'') {
                inString = true;
                stringChar = c;
                i++;
                continue;
            }

            if (c == '(') parenDepth++;
            else if (c == ')') parenDepth = Math.max(0, parenDepth - 1);
            else if (c == '[') bracketDepth++;
            else if (c == ']') bracketDepth = Math.max(0, bracketDepth - 1);
            else if (c == '{') braceDepth++;
            else if (c == '}') braceDepth = Math.max(0, braceDepth - 1);

            if (c == ',' && parenDepth == 0 && bracketDepth == 0 && braceDepth == 0) {
                return i;
            }

            i++;
        }

        return endLimit;
    }

    /** Advance past whitespace, commas, and line/block comments between object literal properties. */
    private static int skipSeparatorsAndComments(String src, int start, int endLimit) {
        int i = start;
        while (i < endLimit) {
            char c = src.charAt(i);
            char next = (i + 1 < endLimit) ? src.charAt(i + 1) : 0;
            if (Character.isWhitespace(c) || c == ',') {
                i++;
                continue;
            }
            if (c == '/' && next == '/') {
                i += 2;
                while (i < endLimit) {
                    char line = src.charAt(i);
                    if (line == '\n' || line == '\r') {
                        break;
                    }
                    i++;
                }
                continue;
            }
            if (c == '/' && next == '*') {
                i += 2;
                while (i + 1 < endLimit) {
                    if (src.charAt(i) == '*' && src.charAt(i + 1) == '/') {
                        i += 2;
                        break;
                    }
                    i++;
                }
                continue;
            }
            break;
        }
        return i;
    }

    /**
     * Determine whether the document position is inside an object literal by counting unmatched braces
     * and verifying that the nearest enclosing {@code {} is preceded by {@code =}, {@code :}, {@code (}, etc.
     */
    static  boolean isInsideObjectLiteral(int position, ScriptDocument doc) {
        // First pass: count unmatched braces forward to position to check if we're in a brace context.
        int braceDepth = 0;
        boolean inString = false;
        char stringChar = 0;
        String text = doc.getText();
        for (int i = 0; i < position && i < text.length(); i++) {
            if (doc.isExcluded(i)) {
                continue;
            }
            char c = text.charAt(i);
            if (inString) {
                if (c == '\\') { i++; continue; }
                if (c == stringChar) inString = false;
                continue;
            }
            if (c == '"' || c == '\'') { inString = true; stringChar = c; continue; }
            if (c == '{') braceDepth++;
            else if (c == '}') braceDepth = Math.max(0, braceDepth - 1);
        }
        // Not inside any braces at all.
        if (braceDepth <= 0) return false;

        // Second pass: walk backward to find the matching '{' for our position.
        int braceStart = -1;
        int depth = 0;
        inString = false;
        stringChar = 0;
        for (int i = position - 1; i >= 0; i--) {
            char c = text.charAt(i);
            if (doc.isExcluded(i)) {
                continue;
            }
            if (inString) {
                if (c == stringChar && (i == 0 || text.charAt(i - 1) != '\\')) {
                    inString = false;
                }
                continue;
            }
            if (c == '"' || c == '\'') {
                inString = true;
                stringChar = c;
                continue;
            }
            if (c == '}') {
                depth++;
            } else if (c == '{') {
                if (depth == 0) {
                    braceStart = i;
                    break;
                }
                depth--;
            }
        }
        if (braceStart < 0) {
            return false;
        }

        // Third pass: check if the '{' is preceded by a literal indicator (=, :, (, [, comma, or nested {).
        for (int i = braceStart - 1; i >= 0; i--) {
            char c = text.charAt(i);
            if (doc.isExcluded(i) || Character.isWhitespace(c)) {
                continue;
            }
            return c == '=' || c == ':' || c == '(' || c == '[' || c == ',' || c == '{';
        }
        return true;
    }

    /**
     * Scan forward to find the next top-level comma (skipping nested structures and strings).
     *
     * <p>Used to skip over unsupported property entries (computed keys, spread, etc.)
     * and advance to the next property in the object literal.</p>
     *
     * @param src      the source text
     * @param start    where to start scanning
     * @param endLimit the exclusive boundary
     * @return the index just past the comma, or {@code endLimit} if no comma is found
     */
    static int scanToNextTopLevelComma(String src, int start, int endLimit) {
        int i = start;
        int depth = 0;
        boolean inString = false;
        char stringChar = 0;
        while (i < endLimit) {
            char c = src.charAt(i);
            if (inString) {
                if (c == '\\') {
                    i += (i + 1 < endLimit) ? 2 : 1;
                    continue;
                }
                if (c == stringChar) {
                    inString = false;
                }
                i++;
                continue;
            }
            if (c == '"' || c == '\'') {
                inString = true;
                stringChar = c;
                i++;
                continue;
            }
            if (c == '{' || c == '[' || c == '(') depth++;
            else if (c == '}' || c == ']' || c == ')') depth = Math.max(0, depth - 1);
            if (c == ',' && depth == 0) {
                return i + 1;
            }
            i++;
        }
        return endLimit;
    }

    /**
     * Find the matching closing brace for an object literal within a standalone string.
     *
     * <p>Unlike {@code ScriptDocument.findMatchingBraceEndInDocument} which operates on the full
     * document text with excluded-range awareness, this method works on an isolated string snippet
     * and handles comments/strings inline.</p>
     *
     * @param src the source text starting with '{' (or containing it)
     * @return the index one past the matching '}', or {@code -1} if not found
     */
    static int findMatchingObjectLiteralEnd(String src) {
        int depth = 0;
        boolean inString = false;
        char stringChar = 0;
        boolean inLineComment = false;
        boolean inBlockComment = false;

        for (int i = 0; i < src.length(); i++) {
            char c = src.charAt(i);
            char next = (i + 1 < src.length()) ? src.charAt(i + 1) : 0;

            if (inLineComment) {
                if (c == '\n' || c == '\r') {
                    inLineComment = false;
                }
                continue;
            }
            if (inBlockComment) {
                if (c == '*' && next == '/') {
                    inBlockComment = false;
                    i++;
                }
                continue;
            }
            if (inString) {
                if (c == '\\') {
                    i += (i + 1 < src.length()) ? 1 : 0;
                    continue;
                }
                if (c == stringChar) {
                    inString = false;
                }
                continue;
            }

            if (c == '/' && next == '/') {
                inLineComment = true;
                i++;
                continue;
            }
            if (c == '/' && next == '*') {
                inBlockComment = true;
                i++;
                continue;
            }
            if (c == '"' || c == '\'') {
                inString = true;
                stringChar = c;
                continue;
            }

            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return i + 1;
                }
            }
        }

        return -1;
    }
}
