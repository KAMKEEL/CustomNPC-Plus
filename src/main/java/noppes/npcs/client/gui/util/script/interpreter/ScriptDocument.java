package noppes.npcs.client.gui.util.script.interpreter;

import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.gui.util.script.interpreter.expression.CastExpressionResolver;
import noppes.npcs.client.gui.util.script.interpreter.expression.ExpressionNode;
import noppes.npcs.client.gui.util.script.interpreter.expression.ExpressionTypeResolver;
import noppes.npcs.client.gui.util.script.interpreter.field.AssignmentInfo;
import noppes.npcs.client.gui.util.script.interpreter.field.FieldAccessInfo;
import noppes.npcs.client.gui.util.script.interpreter.field.FieldInfo;
import noppes.npcs.client.gui.util.script.interpreter.method.MethodCallInfo;
import noppes.npcs.client.gui.util.script.interpreter.method.MethodInfo;
import noppes.npcs.client.gui.util.script.interpreter.method.MethodSignature;
import noppes.npcs.client.gui.util.script.interpreter.token.Token;
import noppes.npcs.client.gui.util.script.interpreter.token.TokenErrorMessage;
import noppes.npcs.client.gui.util.script.interpreter.token.TokenType;
import noppes.npcs.client.gui.util.script.interpreter.type.ImportData;
import noppes.npcs.client.gui.util.script.interpreter.type.ScriptTypeInfo;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeChecker;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeResolver;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The main document container that manages script text, lines, and tokens.
 * This is the clean reimplementation of JavaTextContainer.
 *
 * Architecture:
 * - ScriptDocument holds the complete source text and global state
 * - ScriptLine holds individual lines with their tokens
 * - Token holds individual syntax elements with type-specific metadata
 * - TypeResolver handles all class/type resolution
 *
 * Single-pass tokenization:
 * 1. Parse excluded regions (comments, strings)
 * 2. Parse imports and resolve types
 * 3. Parse structure (methods, classes, fields)
 * 4. Tokenize with all context available
 */
public class ScriptDocument {

    // ==================== PATTERNS ====================

    private static final Pattern WORD_PATTERN = Pattern.compile("[\\p{L}\\p{N}_-]+|\\n|$");

    // Literals
    private static final Pattern STRING_PATTERN = Pattern.compile("([\"'])(?:(?=(\\\\?))\\2.)*?\\1");
    private static final Pattern COMMENT_PATTERN = Pattern.compile("/\\*[\\s\\S]*?(?:\\*/|$)|//.*|#.*");
    private static final Pattern NUMBER_PATTERN = Pattern.compile(
            "\\b-?(?:0[xX][\\dA-Fa-f]+|0[bB][01]+|0[oO][0-7]+|\\d*\\.?\\d+(?:[Ee][+-]?\\d+)?(?:[fFbBdDlLsS])?|NaN|null|Infinity|true|false)\\b");

    // Keywords
    private static final Pattern MODIFIER_PATTERN = Pattern.compile(
            "\\b(public|protected|private|static|final|abstract|synchronized|native|default)\\b");
    private static final Pattern KEYWORD_PATTERN = Pattern.compile(
            "\\b(null|boolean|int|float|double|long|char|byte|short|void|if|else|switch|case|for|while|do|try|catch|finally|return|throw|var|let|const|function|continue|break|this|new|typeof|instanceof|import)\\b");

    // Declarations - Updated to capture method parameters
    private static final Pattern IMPORT_PATTERN = Pattern.compile(
            "(?m)\\bimport\\s+(?:static\\s+)?([A-Za-z_][A-Za-z0-9_]*(?:\\s*\\.\\s*[A-Za-z_][A-Za-z0-9_]*)*)(?:\\s*\\.\\s*\\*?)?\\s*(?:;|$)");
    private static final Pattern CLASS_DECL_PATTERN = Pattern.compile(
            "\\b(class|interface|enum)\\s+([A-Za-z_][a-zA-Z0-9_]*)");
    private static final Pattern METHOD_DECL_PATTERN = Pattern.compile(
            "\\b([A-Za-z_][a-zA-Z0-9_<>\\[\\]]*)\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(");
    private static final Pattern METHOD_CALL_PATTERN = Pattern.compile(
            "([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(");
    private static final Pattern FIELD_DECL_PATTERN = Pattern.compile(
            "\\b([A-Za-z_][a-zA-Z0-9_<>,[ \\t]\\[\\]]*)[ \\t]+([a-zA-Z_][a-zA-Z0-9_]*)[ \\t]*(=|;)");
    private static final Pattern NEW_TYPE_PATTERN = Pattern.compile("\\bnew\\s+([A-Za-z_][a-zA-Z0-9_]*)");
    
    // Cast expression pattern: captures type name inside cast parentheses
    // Matches: (Type), (Type[]), (pkg.Type), (Type<Generic>)
    private static final Pattern CAST_TYPE_PATTERN = Pattern.compile(
            "\\(\\s*([A-Za-z_][a-zA-Z0-9_]*(?:\\s*\\.\\s*[A-Za-z_][a-zA-Z0-9_]*)*)\\s*(?:<[^>]*>)?\\s*(\\[\\s*\\])*\\s*\\)\\s*(?=[a-zA-Z_\"'(\\d!~+-])");

    // Function parameters (for JS-style scripts)
    private static final Pattern FUNC_PARAMS_PATTERN = Pattern.compile(
            "\\bfunction\\s+[a-zA-Z_][a-zA-Z0-9_]*\\s*\\(([^)]*)\\)");

    // ==================== STATE ====================

    private String text = "";
    private final List<ScriptLine> lines = new ArrayList<>();
    private final List<ImportData> imports = new ArrayList<>();
    private final List<MethodInfo> methods = new ArrayList<>();
    private final Map<String, FieldInfo> globalFields = new HashMap<>();
    private final Set<String> wildcardPackages = new HashSet<>();
    private Map<String, ImportData> importsBySimpleName = new HashMap<>();
    
    // Script-defined types (classes, interfaces, enums defined in the script)
    private final Map<String, ScriptTypeInfo> scriptTypes = new HashMap<>();
    
    // Local variables per method (methodStartOffset -> {varName -> FieldInfo})
    private final Map<Integer, Map<String, FieldInfo>> methodLocals = new HashMap<>();

    // Method calls - stores all parsed method call information
    private final List<MethodCallInfo> methodCalls = new ArrayList<>();

    // Field accesses - stores all parsed field access information
    private final List<FieldAccessInfo> fieldAccesses = new ArrayList<>();
    
    // Assignments to external fields (fields from reflection, not script-defined)
    private final List<AssignmentInfo> externalFieldAssignments = new ArrayList<>();

    // Declaration errors (duplicate declarations, etc.)
    private final List<AssignmentInfo> declarationErrors = new ArrayList<>();

    // Excluded regions (strings/comments) - positions where other patterns shouldn't match
    private final List<int[]> excludedRanges = new ArrayList<>();

    // Type resolver
    private final TypeResolver typeResolver;

    // Layout properties
    public int lineHeight = 13;
    public int totalHeight;
    public int visibleLines = 1;
    public int linesCount;

    // ==================== CONSTRUCTOR ====================

    public ScriptDocument(String text) {
        this.typeResolver = TypeResolver.getInstance();
        setText(text);
    }

    public ScriptDocument(String text, TypeResolver resolver) {
        this.typeResolver = resolver != null ? resolver : TypeResolver.getInstance();
        setText(text);
    }

    // ==================== TEXT MANAGEMENT ====================

    public void setText(String text) {
        this.text = text != null ? text.replaceAll("\\r?\\n|\\r", "\n") : "";
    }

    public String getText() {
        return text;
    }

    // ==================== INITIALIZATION ====================

    /**
     * Initialize the document with layout constraints.
     * Builds lines based on width wrapping.
     */
    public void init(int width, int height) {
        lines.clear();
        lineHeight = ClientProxy.Font.height();
        if (lineHeight == 0)
            lineHeight = 12;

        String[] sourceLines = text.split("\n", -1);
        int totalChars = 0;
        int lineIndex = 0;

        for (String sourceLine : sourceLines) {
            StringBuilder currentLine = new StringBuilder();
            Matcher m = WORD_PATTERN.matcher(sourceLine);
            int i = 0;

            while (m.find()) {
                String word = sourceLine.substring(i, m.start());
                if (ClientProxy.Font.width(currentLine + word) > width - 10) {
                    // Wrap line
                    int lineStart = totalChars;
                    int lineEnd = totalChars + currentLine.length();
                    lines.add(new ScriptLine(currentLine.toString(), lineStart, lineEnd, lineIndex++));
                    totalChars = lineEnd;
                    currentLine = new StringBuilder();
                }
                currentLine.append(word);
                i = m.start();
            }

            // Add final line segment (including newline character in range)
            int lineStart = totalChars;
            int lineEnd = totalChars + currentLine.length() + 1;
            lines.add(new ScriptLine(currentLine.toString(), lineStart, lineEnd, lineIndex++));
            totalChars = lineEnd;
        }

        // Set up line navigation
        for (int li = 0; li < lines.size(); li++) {
            ScriptLine line = lines.get(li);
            line.setParent(this);
            if (li > 0) {
                line.setPrev(lines.get(li - 1));
                lines.get(li - 1).setNext(line);
            }
        }

        linesCount = lines.size();
        totalHeight = linesCount * lineHeight;
        visibleLines = Math.max(height / lineHeight - 1, 1);
    }

    // ==================== TOKENIZATION ====================

    /**
     * Main tokenization entry point.
     * Performs complete analysis and builds tokens for all lines.
     */
    public void formatCodeText() {
        // Clear previous state
        imports.clear();
        methods.clear();
        globalFields.clear();
        wildcardPackages.clear();
        excludedRanges.clear();
        methodLocals.clear();
        scriptTypes.clear();
        methodCalls.clear();
        externalFieldAssignments.clear();
        declarationErrors.clear();

        // Phase 1: Find excluded regions (strings/comments)
        findExcludedRanges();

        // Phase 2: Parse imports
        parseImports();

        // Phase 3: Parse structure (script types, methods, fields, locals)
        parseStructure();

        // Phase 4: Build marks and assign to lines
        List<ScriptLine.Mark> marks = buildMarks();

        // Phase 5: Resolve conflicts and sort
        marks = resolveConflicts(marks);

        // Phase 6: Build tokens for each line
        for (ScriptLine line : lines) {
            line.buildTokensFromMarks(marks, text, this);
        }

        // Phase 7: Compute indent guides
        computeIndentGuides(marks);
    }

    // ==================== PHASE 1: EXCLUDED RANGES ====================

    private void findExcludedRanges() {
        // Find strings
        Matcher m = STRING_PATTERN.matcher(text);
        while (m.find()) {
            excludedRanges.add(new int[]{m.start(), m.end()});
        }

        // Find comments
        m = COMMENT_PATTERN.matcher(text);
        while (m.find()) {
            excludedRanges.add(new int[]{m.start(), m.end()});
        }

        // Sort and merge overlapping ranges
        excludedRanges.sort(Comparator.comparingInt(a -> a[0]));
        mergeOverlappingRanges();
    }

    private void mergeOverlappingRanges() {
        if (excludedRanges.size() < 2)
            return;

        List<int[]> merged = new ArrayList<>();
        int[] current = excludedRanges.get(0);

        for (int i = 1; i < excludedRanges.size(); i++) {
            int[] next = excludedRanges.get(i);
            if (next[0] <= current[1]) {
                current[1] = Math.max(current[1], next[1]);
            } else {
                merged.add(current);
                current = next;
            }
        }
        merged.add(current);

        excludedRanges.clear();
        excludedRanges.addAll(merged);
    }

    private boolean isExcluded(int position) {
        for (int[] range : excludedRanges) {
            if (position >= range[0] && position < range[1]) {
                return true;
            }
        }
        return false;
    }

    // ==================== PHASE 2: IMPORTS ====================

    private void parseImports() {
        Matcher m = IMPORT_PATTERN.matcher(text);
        while (m.find()) {
            if (isExcluded(m.start()))
                continue;

            String fullPath = m.group(1).replaceAll("\\s+", "").trim();
            String matchText = m.group(0);
            boolean isWildcard = matchText.contains("*");
            boolean isStatic = matchText.contains("static");
            
            // Skip if path ends with dot (incomplete import)
            if (fullPath.endsWith(".")) {
             //   continue;
            }

            int lastDot = fullPath.lastIndexOf('.');
            String simpleName = isWildcard ? null : (lastDot >= 0 ? fullPath.substring(lastDot + 1) : fullPath);

            ImportData importData = new ImportData(
                    fullPath, simpleName, isWildcard, isStatic,
                    m.start(), m.end(), m.start(1), m.end(1)
            );
            imports.add(importData);

            if (isWildcard) {
                wildcardPackages.add(fullPath);
            }
        }

        // Resolve all imports
        importsBySimpleName = typeResolver.resolveImports(imports);
    }

    // ==================== PHASE 3: STRUCTURE ====================

    private void parseStructure() {
        // Clear import references before re-parsing
        for (ImportData imp : imports) {
            imp.clearReferences();
        }
        
        // Parse script-defined types (classes, interfaces, enums)
        parseScriptTypes();
        
        // Parse methods
        parseMethodDeclarations();

        // Parse local variables inside methods
        parseLocalVariables();

        // Parse global fields (outside methods)
        parseGlobalFields();
        
        // Parse and validate assignments (reassignments) - stores in FieldInfo
        parseAssignments();
    }

    /**
     * Parse class, interface, and enum declarations defined in the script.
     * Creates ScriptTypeInfo instances and stores them for later resolution.
     */
    private void parseScriptTypes() {
        // Pattern: [modifiers] (class|interface|enum) ClassName [optional ()] { ... }
        // Matches optional modifiers (public, private, static, final, abstract) before class/interface/enum
        // Also allows optional () after the class name (common mistake)
        Pattern typeDecl = Pattern.compile(
                "(?:(?:public|private|protected|static|final|abstract)\\s+)*(class|interface|enum)\\s+([A-Za-z_][a-zA-Z0-9_]*)\\s*(?:\\(\\))?\\s*(?:extends\\s+[A-Za-z_][a-zA-Z0-9_.]*)?\\s*(?:implements\\s+[A-Za-z_][a-zA-Z0-9_.,\\s]*)?\\s*\\{");
        
        Matcher m = typeDecl.matcher(text);
        while (m.find()) {
            if (isExcluded(m.start()))
                continue;
            
            String kindStr = m.group(1);
            String typeName = m.group(2);
            int bodyStart = text.indexOf('{', m.start());
            int bodyEnd = findMatchingBrace(bodyStart);
            
            if (bodyEnd < 0) {
                bodyEnd = text.length();
            }
            
            TypeInfo.Kind kind;
            switch (kindStr) {
                case "interface": kind = TypeInfo.Kind.INTERFACE; break;
                case "enum": kind = TypeInfo.Kind.ENUM; break;
                default: kind = TypeInfo.Kind.CLASS; break;
            }
            
            // Extract modifiers from the matched text
            String fullMatch = text.substring(m.start(), m.end());
            int modifiers = parseModifiers(fullMatch);
            
            ScriptTypeInfo scriptType = ScriptTypeInfo.create(
                    typeName, kind, m.start(), bodyStart, bodyEnd, modifiers);

            scriptTypes.put(typeName, scriptType);

            // Parse fields and methods inside this type AFTER adding the scriptType globally, so its members can reference it
            parseScriptTypeMembers(scriptType);
            
        }
    }

    /**
     * Parse fields and methods inside a script-defined type.
     */
    private void parseScriptTypeMembers(ScriptTypeInfo scriptType) {
        int bodyStart = scriptType.getBodyStart();
        int bodyEnd = scriptType.getBodyEnd();
        
        if (bodyStart < 0 || bodyEnd <= bodyStart) return;
        
        String bodyText = text.substring(bodyStart + 1, Math.min(bodyEnd, text.length()));
        
        // Parse field declarations
        Pattern fieldPattern = Pattern.compile(
                "\\b([A-Za-z_][a-zA-Z0-9_<>,\\s\\[\\]]*)\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*(=|;)");
        Matcher fm = fieldPattern.matcher(bodyText);
        while (fm.find()) {
            int absPos = bodyStart + 1 + fm.start();
            if (isExcluded(absPos)) continue;
            
            String typeNameRaw = fm.group(1).trim();
            String fieldName = fm.group(2);
            
            // Parse modifiers and strip them
            int modifiers = parseModifiers(typeNameRaw);
            String typeName = stripModifiers(typeNameRaw);
            
            // Skip if this looks like a method declaration
            if (typeName.equals("return") || typeName.equals("if") || typeName.equals("while") ||
                typeName.equals("for") || typeName.equals("switch") || typeName.equals("catch") ||
                typeName.equals("new") || typeName.equals("throw")) {
                continue;
            }
            
            // Skip if inside a nested method body
            if (isInsideNestedMethod(absPos, bodyStart, bodyEnd)) {
                continue;
            }
            
            // Extract documentation before this field
            String documentation = extractDocumentationBefore(absPos);
            
            TypeInfo fieldType = resolveType(typeName);
            FieldInfo fieldInfo = FieldInfo.globalField(fieldName, fieldType, absPos, documentation, -1, -1, modifiers);
            scriptType.addField(fieldInfo);
        }
        
        // Parse method declarations
        Pattern methodPattern = Pattern.compile(
                "\\b([A-Za-z_][a-zA-Z0-9_<>\\[\\]]*)\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(([^)]*)\\)\\s*\\{");
        Matcher mm = methodPattern.matcher(bodyText);
        while (mm.find()) {
            int absPos = bodyStart + 1 + mm.start();
            if (isExcluded(absPos)) continue;
            
            String returnTypeName = mm.group(1);
            String methodName = mm.group(2);
            String paramList = mm.group(3);
            
            // Skip class/interface/enum keywords
            if (returnTypeName.equals("class") || returnTypeName.equals("interface") || 
                returnTypeName.equals("enum")) {
                continue;
            }
            
            int methodBodyStart = bodyStart + 1 + mm.end() - 1;
            int methodBodyEnd = findMatchingBrace(methodBodyStart);
            if (methodBodyEnd < 0) methodBodyEnd = bodyEnd;
            
            // Extract documentation before this method
            String documentation = extractDocumentationBefore(absPos);
            
            // Extract modifiers by scanning backwards in bodyText from match start
            int modifiers = extractModifiersBackwards(mm.start() - 1, bodyText);
            
            TypeInfo returnType = resolveType(returnTypeName);
            List<FieldInfo> params = parseParametersWithPositions(paramList, 
                    bodyStart + 1 + mm.start(3));
            
            // Calculate the three offsets
            int typeOffset = bodyStart + 1 + mm.start(1);  // Start of return type
            int nameOffset = bodyStart + 1 + mm.start(2);  // Start of method name
            int fullDeclOffset = findFullDeclarationStart(mm.start(1), bodyText);
            if (fullDeclOffset >= 0) {
                fullDeclOffset += bodyStart + 1;
            } else {
                fullDeclOffset = typeOffset;
            }
            
            MethodInfo methodInfo = MethodInfo.declaration(
                    methodName, returnType, params, fullDeclOffset, typeOffset, nameOffset, methodBodyStart, methodBodyEnd, modifiers, documentation);
            
            // Validate the method (including return type checking)
            String methodBodyText = text.substring(methodBodyStart + 1, methodBodyEnd);
            methodInfo.validate(methodBodyText, (expr, pos) -> resolveExpressionType(expr, pos));
            
            scriptType.addMethod(methodInfo);
        }
        
        // Parse constructors (ClassName(params) { ... })
        // Constructor pattern: ClassName matches the script type name, no return type
        String typeName = scriptType.getSimpleName();
        Pattern constructorPattern = Pattern.compile(
                "\\b" + Pattern.quote(typeName) + "\\s*\\(([^)]*)\\)\\s*\\{");
        Matcher cm = constructorPattern.matcher(bodyText);
        while (cm.find()) {
            int absPos = bodyStart + 1 + cm.start();
            if (isExcluded(absPos)) continue;
            
            String paramList = cm.group(1);
            
            int constructorBodyStart = bodyStart + 1 + cm.end() - 1;
            int constructorBodyEnd = findMatchingBrace(constructorBodyStart);
            if (constructorBodyEnd < 0) constructorBodyEnd = bodyEnd;
            
            // Extract documentation before this constructor
            String documentation = extractDocumentationBefore(absPos);
            
            // Extract modifiers by scanning backwards
            int modifiers = extractModifiersBackwards(cm.start() - 1, bodyText);
            
            // Parse parameters with their actual positions
            List<FieldInfo> params = parseParametersWithPositions(paramList, bodyStart + 1 + cm.start(1));
            
            // Calculate the three offsets for constructor
            // For constructors, type offset and name offset are the same (start of constructor name)
            int nameOffset = bodyStart + 1 + cm.start();
            int typeOffset = nameOffset;  // No separate type for constructors
            int fullDeclOffset = findFullDeclarationStart(cm.start(), bodyText);
            if (fullDeclOffset >= 0) {
                fullDeclOffset += bodyStart + 1;
            } else {
                fullDeclOffset = nameOffset;
            }
            
            // Constructors don't have a return type, but we'll use the containing type as a marker
            MethodInfo constructorInfo = MethodInfo.declaration(
                    typeName,
                    scriptType,  // Return type is the type itself
                    params,
                    fullDeclOffset,
                    typeOffset,
                    nameOffset,
                    constructorBodyStart,
                    constructorBodyEnd,
                    modifiers,
                    documentation
            );
            scriptType.addConstructor(constructorInfo);
        }
    }

    /**
     * Check if a position is inside a nested method body within a class.
     * This prevents field declarations inside methods from being treated as class fields.
     */
    private boolean isInsideNestedMethod(int position, int classBodyStart, int classBodyEnd) {
        String bodyText = text.substring(classBodyStart + 1, Math.min(classBodyEnd, text.length()));
        int relativePos = position - classBodyStart - 1;
        
        Pattern methodPattern = Pattern.compile(
                "\\b[A-Za-z_][a-zA-Z0-9_<>\\[\\]]*\\s+[a-zA-Z_][a-zA-Z0-9_]*\\s*\\([^)]*\\)\\s*\\{");
        Matcher m = methodPattern.matcher(bodyText);
        
        while (m.find()) {
            int methodBodyStart = m.end() - 1;
            int absMethodBodyStart = classBodyStart + 1 + methodBodyStart;
            int absMethodBodyEnd = findMatchingBrace(absMethodBodyStart);
            
            if (absMethodBodyEnd > 0 && position > absMethodBodyStart && position < absMethodBodyEnd) {
                return true;
            }
        }
        return false;
    }

    private void parseMethodDeclarations() {
        Pattern methodWithBody = Pattern.compile(
                "\\b([a-zA-Z_][a-zA-Z0-9_<>\\[\\]]*)\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(([^)]*)\\)\\s*\\{");

        Matcher m = methodWithBody.matcher(text);
        while (m.find()) {
            if (isExcluded(m.start()))
                continue;

            String returnType = m.group(1);
            String methodName = m.group(2);
            String paramList = m.group(3);

            int bodyStart = text.indexOf('{', m.end() - 1);
            int bodyEnd = findMatchingBrace(bodyStart);
            if (bodyEnd < 0)
                bodyEnd = text.length();

            // Extract documentation before this method
            String documentation = extractDocumentationBefore(m.start());

            // Extract modifiers by scanning backwards from match start
            int modifiers = extractModifiersBackwards(m.start() - 1, text);
            
            // Calculate offset positions
            int typeOffset = m.start(1);        // Start of return type
            int nameOffset = m.start(2);        // Start of method name
            int fullDeclOffset = findFullDeclarationStart(m.start(1), text); // Start including modifiers

            // Parse parameters with their actual positions
            List<FieldInfo> params = parseParametersWithPositions(paramList, m.start(3));

            MethodInfo methodInfo = MethodInfo.declaration(
                    methodName,
                    resolveType(returnType),
                    params,
                    fullDeclOffset,
                    typeOffset,
                    nameOffset,
                    bodyStart,
                    bodyEnd,
                    modifiers,
                    documentation
            );
            
            // Validate the method (return statements, parameters) with type resolution
            String methodBodyText = bodyEnd > bodyStart + 1 ? text.substring(bodyStart + 1, bodyEnd) : "";
            methodInfo.validate(methodBodyText, (expr, pos) -> resolveExpressionType(expr, pos));
            
            methods.add(methodInfo);
        }

        // Check for duplicate method declarations
        checkDuplicateMethods();

        // Also parse JS-style functions
        Matcher funcM = FUNC_PARAMS_PATTERN.matcher(text);
        while (funcM.find()) {
            if (isExcluded(funcM.start()))
                continue;
            // JS functions don't have explicit return types
            String paramList = funcM.group(1);
            List<FieldInfo> params = new ArrayList<>();
            if (paramList != null && !paramList.trim().isEmpty()) {
                for (String p : paramList.split(",")) {
                    String pn = p.trim();
                    if (!pn.isEmpty()) {
                        params.add(FieldInfo.parameter(pn, null, funcM.start(), null));
                    }
                }
            }
            // We don't need to store JS functions in methods list - just extract params
        }
    }

    /**
     * Find the start of a full method declaration, including any modifiers before the type.
     */
    private int findFullDeclarationStart(int typeStart, String sourceText) {
        int pos = typeStart - 1;
        
        // Skip whitespace before type
        while (pos >= 0 && Character.isWhitespace(sourceText.charAt(pos))) {
            pos--;
        }
        
        // Check for modifiers
        int earliestPos = typeStart;
        while (pos >= 0) {
            // Skip whitespace
            while (pos >= 0 && Character.isWhitespace(sourceText.charAt(pos))) {
                pos--;
            }
            if (pos < 0) break;
            
            // Read word backwards
            int wordEnd = pos + 1;
            while (pos >= 0 && Character.isJavaIdentifierPart(sourceText.charAt(pos))) {
                pos--;
            }
            int wordStart = pos + 1;
            
            if (wordStart < wordEnd) {
                String word = sourceText.substring(wordStart, wordEnd);
                if (TypeResolver.isModifier(word)) {
                    earliestPos = wordStart;
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        
        return earliestPos;
    }

    /**
     * Check for duplicate method declarations with same signature in same scope.
     */
    private void checkDuplicateMethods() {
        // Group methods by scope depth
        Map<Integer, List<MethodInfo>> methodsByScope = new HashMap<>();
        
        for (MethodInfo method : methods) {
            int scopeDepth = calculateScopeDepth(method.getFullDeclarationOffset());
            methodsByScope.computeIfAbsent(scopeDepth, k -> new ArrayList<>()).add(method);
        }
        
        // Check each scope for duplicates
        for (List<MethodInfo> scopeMethods : methodsByScope.values()) {
            Map<MethodSignature, List<MethodInfo>> signatureMap = new HashMap<>();
            
            for (MethodInfo method : scopeMethods) {
                MethodSignature signature = method.getSignature();
                signatureMap.computeIfAbsent(signature, k -> new ArrayList<>()).add(method);
            }
            
            // Mark all methods with duplicate signatures
            for (Map.Entry<MethodSignature, List<MethodInfo>> entry : signatureMap.entrySet()) {
                List<MethodInfo> duplicates = entry.getValue();
                if (duplicates.size() > 1) {
                    for (MethodInfo duplicate : duplicates) {
                        duplicate.setError(
                            MethodInfo.ErrorType.DUPLICATE_METHOD,
                            duplicate.getSignature() + " is already defined in this scope"
                        );
                    }
                }
            }
        }
    }

    /**
     * Calculate the scope depth of a position (0 = top level, 1 = in class, etc.).
     */
    private int calculateScopeDepth(int position) {
        int depth = 0;
        int braceDepth = 0;
        boolean inString = false;
        boolean inComment = false;
        boolean inLineComment = false;
        
        for (int i = 0; i < position && i < text.length(); i++) {
            char c = text.charAt(i);
            char next = (i + 1 < text.length()) ? text.charAt(i + 1) : 0;
            
            // Handle strings
            if (c == '"' && !inComment && !inLineComment) {
                inString = !inString;
                continue;
            }
            if (inString) continue;
            
            // Handle comments
            if (!inComment && !inLineComment && c == '/' && next == '/') {
                inLineComment = true;
                i++;
                continue;
            }
            if (!inComment && !inLineComment && c == '/' && next == '*') {
                inComment = true;
                i++;
                continue;
            }
            if (inComment && c == '*' && next == '/') {
                inComment = false;
                i++;
                continue;
            }
            if (inLineComment && c == '\n') {
                inLineComment = false;
                continue;
            }
            if (inComment || inLineComment) continue;
            
            // Track braces
            if (c == '{') {
                braceDepth++;
                depth = Math.max(depth, braceDepth);
            } else if (c == '}') {
                braceDepth--;
            }
        }
        
        return depth;
    }

    private List<FieldInfo> parseParametersWithPositions(String paramList, int paramListStart) {
        List<FieldInfo> params = new ArrayList<>();
        if (paramList == null || paramList.trim().isEmpty()) {
            return params;
        }

        // Pattern: Type varName (with optional spaces)
        Pattern paramPattern = Pattern.compile(
                "([a-zA-Z_][a-zA-Z0-9_<>\\[\\]]*(?:\\.{3})?)\\s+([a-zA-Z_][a-zA-Z0-9_]*)");
        Matcher m = paramPattern.matcher(paramList);
        while (m.find()) {
            String typeName = m.group(1);
            String paramName = m.group(2);
            TypeInfo typeInfo = resolveType(typeName);
            // Store the absolute position of the parameter name
            int paramNameStart = paramListStart + m.start(2);
            params.add(FieldInfo.parameter(paramName, typeInfo, paramNameStart, null));
        }
        return params;
    }

    private void parseLocalVariables() {
        for (MethodInfo method : methods) {
            Map<String, FieldInfo> locals = new HashMap<>();
            methodLocals.put(method.getDeclarationOffset(), locals);

            int bodyStart = method.getBodyStart();
            int bodyEnd = method.getBodyEnd();
            if (bodyStart < 0 || bodyEnd <= bodyStart) continue;

            String bodyText = text.substring(bodyStart, Math.min(bodyEnd, text.length()));
            
            // Pattern for local variable declarations: Type varName = or Type varName;
            // Allows capital var names like "Minecraft Capital = new Minecraft();"
            // Use [ \t] instead of \s to prevent matching across newlines
            Pattern localDecl = Pattern.compile(
                    "\\b([A-Za-z_][a-zA-Z0-9_<>\\[\\]]*)[ \\t]+([A-Za-z_][a-zA-Z0-9_]*)[ \\t]*(=|;|,)");
            Matcher m = localDecl.matcher(bodyText);
            while (m.find()) {
                String typeName = m.group(1);
                String varName = m.group(2);
                String delimiter = m.group(3);
                
                // Skip if the variable name itself is excluded
                int absPos = bodyStart + m.start(2);
                if (isExcluded(absPos)) continue;
                
                // Skip if it looks like a method call or control flow
                if (typeName.equals("return") || typeName.equals("if") || typeName.equals("while") ||
                    typeName.equals("for") || typeName.equals("switch") || typeName.equals("catch") ||
                    typeName.equals("new") || typeName.equals("throw")) {
                    continue;
                }

                // Parse modifiers from the raw type declaration
                int modifiers = parseModifiers(typeName);

                TypeInfo typeInfo;
                
                // For var/let/const, infer type from the right-hand side expression
                if ((typeName.equals("var") || typeName.equals("let") || typeName.equals("const")) 
                        && delimiter.equals("=")) {
                    // Find the right-hand side expression
                    int rhsStart = bodyStart + m.end();
                    typeInfo = inferTypeFromExpression(rhsStart);
                } else {
                    typeInfo = resolveType(typeName);
                }
                
                // Extract initialization range if there's an '=' delimiter
                int initStart = -1;
                int initEnd = -1;
                if ("=".equals(delimiter)) {
                    initStart = bodyStart + m.start(3); // Absolute position of '='
                    // Find the semicolon or comma that ends this declaration
                    int searchPos = bodyStart + m.end(3);
                    int depth = 0; // Track nested parens/brackets/braces
                    while (searchPos < text.length()) {
                        char c = text.charAt(searchPos);
                        if (c == '(' || c == '[' || c == '{') depth++;
                        else if (c == ')' || c == ']' || c == '}') depth--;
                        else if ((c == ';' || c == ',') && depth == 0) {
                            initEnd = searchPos; // Position of ';' or ',' (exclusive)
                            break;
                        }
                        searchPos++;
                    }
                }
                
                int declPos = bodyStart + m.start(2);
                FieldInfo fieldInfo = FieldInfo.localField(varName, typeInfo, declPos, method, initStart, initEnd, modifiers);

                // Check for duplicate declaration (in locals or globals)
                if (locals.containsKey(varName) || globalFields.containsKey(varName)) {
                    // Create a declaration error for this duplicate
                    AssignmentInfo dupError = AssignmentInfo.duplicateDeclaration(
                            varName, declPos, declPos + varName.length(),
                            "Variable '" + varName + "' is already defined in the scope");
                    declarationErrors.add(dupError);
                } else if (!locals.containsKey(varName))
                    locals.put(varName, fieldInfo);
            }
        }
    }
    
    /**
     * Infer the type of an expression starting at the given position.
     * Handles patterns like:
     *   - "new TypeName()" - returns TypeName
     *   - "receiver.fieldOrMethod" - resolves chain and returns result type
     *   - "variable" - looks up variable type
     *   - Literals like numbers, strings, booleans
     */
    private TypeInfo inferTypeFromExpression(int position) {
        // Skip whitespace
        while (position < text.length() && Character.isWhitespace(text.charAt(position)))
            position++;
        
        if (position >= text.length())
            return null;
        
        // Check for 'new' keyword
        if (text.startsWith("new ", position)) {
            position += 4;
            while (position < text.length() && Character.isWhitespace(text.charAt(position)))
                position++;
            
            // Read the type name
            int typeStart = position;
            while (position < text.length() && Character.isJavaIdentifierPart(text.charAt(position)))
                position++;
            
            if (position > typeStart) {
                String typeName = text.substring(typeStart, position);
                return resolveType(typeName);
            }
            return null;
        }
        
        // Check for string literal
        if (position < text.length() && (text.charAt(position) == '"' || text.charAt(position) == '\'')) {
            return resolveType("String");
        }
        
        // Check for numeric literal
        if (position < text.length() && Character.isDigit(text.charAt(position))) {
            // Check for float/double (has decimal point or f/d suffix)
            int numEnd = position;
            boolean hasDecimal = false;
            while (numEnd < text.length() && (Character.isDigit(text.charAt(numEnd)) || 
                   text.charAt(numEnd) == '.' || text.charAt(numEnd) == 'f' || 
                   text.charAt(numEnd) == 'd' || text.charAt(numEnd) == 'F' || 
                   text.charAt(numEnd) == 'D' || text.charAt(numEnd) == 'L' ||
                   text.charAt(numEnd) == 'l')) {
                if (text.charAt(numEnd) == '.') hasDecimal = true;
                numEnd++;
            }
            String num = text.substring(position, numEnd).toLowerCase();
            if (num.endsWith("f")) return resolveType("float");
            if (num.endsWith("d") || hasDecimal) return resolveType("double");
            if (num.endsWith("l")) return resolveType("long");
            return resolveType("int");
        }
        
        // Check for boolean literals
        if (text.startsWith("true", position) || text.startsWith("false", position)) {
            return resolveType("boolean");
        }
        
        // Check for null
        if (text.startsWith("null", position)) {
            return null; // Can't infer type from null
        }
        
        // Must be an identifier or chain - read the first identifier
        if (Character.isJavaIdentifierStart(text.charAt(position))) {
            int identStart = position;
            while (position < text.length() && Character.isJavaIdentifierPart(text.charAt(position)))
                position++;
            String ident = text.substring(identStart, position);
            
            // Skip whitespace
            while (position < text.length() && Character.isWhitespace(text.charAt(position)))
                position++;
            
            // Check if this is the start of a chain (followed by .)
            if (position < text.length() && text.charAt(position) == '.') {
                // This is a chain like "event.player" or "Minecraft.getMinecraft()"
                // We need to resolve the entire chain to get the final type
                return inferChainType(ident, identStart, position);
            }
            
            // Check if this is a method call (followed by ())
            if (position < text.length() && text.charAt(position) == '(') {
                // Method call - check if it's a script method
                if (isScriptMethod(ident)) {
                    MethodInfo methodInfo = getScriptMethodInfo(ident);
                    return (methodInfo != null) ? methodInfo.getReturnType() : null;
                }
                return null;
            }
            
            // Just a variable - resolve its type
            FieldInfo varInfo = resolveVariable(ident, identStart);
            return (varInfo != null) ? varInfo.getTypeInfo() : null;
        }
        
        return null;
    }
    
    /**
     * Infer the type from a chain expression starting with the given identifier.
     */
    private TypeInfo inferChainType(String firstIdent, int identStart, int dotPosition) {
        TypeInfo currentType = null;
        
        // Resolve the first segment
        TypeInfo typeCheck = resolveType(firstIdent);
        if (typeCheck != null && typeCheck.isResolved()) {
            // Static access like Event.player or scriptType.field
            currentType = typeCheck;
        } else {
            // Variable access
            FieldInfo varInfo = resolveVariable(firstIdent, identStart);
            currentType = (varInfo != null) ? varInfo.getTypeInfo() : null;
        }
        
        if (currentType == null || !currentType.isResolved())
            return null;
        
        // Now resolve the rest of the chain
        int pos = dotPosition;
        while (pos < text.length() && text.charAt(pos) == '.') {
            pos++; // Skip the dot
            
            // Skip whitespace
            while (pos < text.length() && Character.isWhitespace(text.charAt(pos)))
                pos++;
            
            if (pos >= text.length() || !Character.isJavaIdentifierStart(text.charAt(pos)))
                break;
            
            // Read the next identifier
            int segStart = pos;
            while (pos < text.length() && Character.isJavaIdentifierPart(text.charAt(pos)))
                pos++;
            String segment = text.substring(segStart, pos);
            
            // Skip whitespace
            while (pos < text.length() && Character.isWhitespace(text.charAt(pos)))
                pos++;
            
            // Check if this is a method call
            if (pos < text.length() && text.charAt(pos) == '(') {
                // Method call
                if (currentType.hasMethod(segment)) {
                    // Parse argument types
                    int closeParen = findMatchingParen(pos);
                    if (closeParen < 0) return null;
                    String argsText = text.substring(pos + 1, closeParen).trim();
                    TypeInfo[] argTypes = parseArgumentTypes(argsText, pos + 1);
                    
                    MethodInfo methodInfo = currentType.getBestMethodOverload(segment, argTypes);
                    currentType = (methodInfo != null) ? methodInfo.getReturnType() : null;
                    
                    // Skip to after the closing paren
                    pos = closeParen + 1;
                } else {
                    return null;
                }
            } else {
                // Field access
                if (currentType.hasField(segment)) {
                    FieldInfo fieldInfo = currentType.getFieldInfo(segment);
                    currentType = (fieldInfo != null) ? fieldInfo.getTypeInfo() : null;
                } else {
                    return null;
                }
            }
            
            if (currentType == null || !currentType.isResolved())
                return null;
            
            // Skip whitespace for next iteration
            while (pos < text.length() && Character.isWhitespace(text.charAt(pos)))
                pos++;
        }
        
        return currentType;
    }

    private void parseGlobalFields() {
        Matcher m = FIELD_DECL_PATTERN.matcher(text);
        while (m.find()) {
            String typeNameRaw = m.group(1);
            String fieldName = m.group(2);
            String delimiter = m.group(3);
            int position = m.start(2);
            
            // Skip if the field name itself is excluded
            if (isExcluded(position))
                continue;

            // Parse modifiers from the raw type declaration
            int modifiers = parseModifiers(typeNameRaw);

            // Strip modifiers (public, private, protected, static, final, etc.) from type name
            String typeName = stripModifiers(typeNameRaw);

            // Check if inside a method - if so, it's a local, not global
            boolean insideMethod = false;
            for (MethodInfo method : methods) {
                if (method.containsPosition(position)) {
                    insideMethod = true;
                    break;
                }
            }

            if (!insideMethod) {
                // Extract documentation before this field
                String documentation = extractDocumentationBefore(m.start());
                
                // Extract initialization range if there's an '=' delimiter
                int initStart = -1;
                int initEnd = -1;
                if ("=".equals(delimiter)) {
                    initStart = m.start(3); // Position of '='
                    // Find the semicolon that ends this declaration
                    int searchPos = m.end(3);
                    int depth = 0; // Track nested parens/brackets/braces
                    while (searchPos < text.length()) {
                        char c = text.charAt(searchPos);
                        if (c == '(' || c == '[' || c == '{') depth++;
                        else if (c == ')' || c == ']' || c == '}') depth--;
                        else if (c == ';' && depth == 0) {
                            initEnd = searchPos; // Position of ';' (exclusive)
                            break;
                        }
                        searchPos++;
                    }
                }
                
                TypeInfo typeInfo = resolveType(typeName);
                FieldInfo fieldInfo = FieldInfo.globalField(fieldName, typeInfo, position, documentation, initStart, initEnd, modifiers);

                // Check for duplicate declaration
                if (globalFields.containsKey(fieldName)) {
                    // Create a declaration error for this duplicate
                    AssignmentInfo dupError = AssignmentInfo.duplicateDeclaration(
                            fieldName, position, position + fieldName.length(),
                            "Variable '" + fieldName + "' is already defined in the scope");
                    declarationErrors.add(dupError);
                } else {
                    globalFields.put(fieldName, fieldInfo);
                }
                
             
            }
        }
    }

    /**
     * Strip Java modifiers from a type name string.
     * e.g., "public static String" -> "String"
     */
    private String stripModifiers(String typeName) {
        if (typeName == null) return null;
        
        String[] parts = typeName.trim().split("\\s+");
        // Filter out known modifiers
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (!TypeResolver.isModifier(part)) {
                if (result.length() > 0) result.append(" ");
                result.append(part);
            }
        }
        return result.toString();
    }

    /**
     * Parse modifiers from a declaration string and return the corresponding Modifier flags.
     * e.g., "public static final" -> Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL
     */
    private int parseModifiers(String declaration) {
        if (declaration == null) return 0;
        
        int modifiers = 0;
        String[] parts = declaration.trim().split("\\s+");
        for (String part : parts) {
            if (part.equals("public")) modifiers |= java.lang.reflect.Modifier.PUBLIC;
            else if (part.equals("private")) modifiers |= java.lang.reflect.Modifier.PRIVATE;
            else if (part.equals("protected")) modifiers |= java.lang.reflect.Modifier.PROTECTED;
            else if (part.equals("static")) modifiers |= java.lang.reflect.Modifier.STATIC;
            else if (part.equals("final")) modifiers |= java.lang.reflect.Modifier.FINAL;
            else if (part.equals("abstract")) modifiers |= java.lang.reflect.Modifier.ABSTRACT;
            else if (part.equals("synchronized")) modifiers |= java.lang.reflect.Modifier.SYNCHRONIZED;
            else if (part.equals("volatile")) modifiers |= java.lang.reflect.Modifier.VOLATILE;
            else if (part.equals("transient")) modifiers |= java.lang.reflect.Modifier.TRANSIENT;
            else if (part.equals("native")) modifiers |= java.lang.reflect.Modifier.NATIVE;
            else if (part.equals("strictfp")) modifiers |= java.lang.reflect.Modifier.STRICT;
        }
        return modifiers;
    }

    /**
     * Scan backwards from scanStart position in the given text to extract modifier keywords.
     * Returns the parsed modifier flags.
     * 
     * @param scanStart The position to start scanning backwards from (exclusive)
     * @param sourceText The text to scan in
     * @return The combined modifier flags
     */
    private int extractModifiersBackwards(int scanStart, String sourceText) {
        // Skip whitespace
        while (scanStart >= 0 && Character.isWhitespace(sourceText.charAt(scanStart))) {
            scanStart--;
        }
        
        // Scan backwards collecting modifier words
        StringBuilder modifiersText = new StringBuilder();
        while (scanStart >= 0) {
            // Skip whitespace
            while (scanStart >= 0 && Character.isWhitespace(sourceText.charAt(scanStart))) {
                scanStart--;
            }
            if (scanStart < 0) break;
            
            // Read a word backwards
            int wordEnd = scanStart + 1;
            while (scanStart >= 0 && Character.isJavaIdentifierPart(sourceText.charAt(scanStart))) {
                scanStart--;
            }
            int wordStart = scanStart + 1;
            if (wordStart < wordEnd) {
                String word = sourceText.substring(wordStart, wordEnd);
                // Check if it's a modifier
                if (TypeResolver.isModifier(word)) {
                    if (modifiersText.length() > 0) {
                        modifiersText.insert(0, " ");
                    }
                    modifiersText.insert(0, word);
                } else {
                    // Hit a non-modifier word, stop scanning
                    break;
                }
            } else {
                break;
            }
        }
        
        // Parse the collected modifiers
        return parseModifiers(modifiersText.toString());
    }

    /**
     * Extract documentation comment immediately preceding a position.
     * Supports both single-line (//) and multi-line (/* *\/) comment styles.
     * Returns null if no documentation is found.
     * 
     * @param position The position of the declaration (e.g., start of method/field)
     * @return The documentation text, or null if none found
     */
    private String extractDocumentationBefore(int position) {
        if (position <= 0) return null;
        
        // First, skip backwards to get off the current declaration line
        // (declarations might have modifiers like 'public' before the matched position)
        int searchStart = position - 1;
        
        // Skip backwards to the previous newline
        while (searchStart >= 0 && text.charAt(searchStart) != '\n') {
            searchStart--;
        }
        
        if (searchStart < 0) return null;
        
        // Now we're at a newline before the declaration line
        // Skip backwards through any additional whitespace
        searchStart--;
        while (searchStart >= 0 && Character.isWhitespace(text.charAt(searchStart))) {
            searchStart--;
        }
        
        if (searchStart < 0) return null;
        
        // Now searchStart should be at the last non-whitespace character before the declaration line
        
        // Check if we're at the end of a comment
        // Case 1: Multi-line comment ending with */
        if (searchStart > 0 && text.charAt(searchStart) == '/' && text.charAt(searchStart - 1) == '*') {
            // Find the start of this comment
            int commentEnd = searchStart + 1;
            int commentStart = text.lastIndexOf("/*", searchStart - 1);
            if (commentStart >= 0) {
                String comment = text.substring(commentStart, commentEnd);
                return cleanDocumentation(comment);
            }
        }
        
        // Case 2: Single-line comments (may be multiple consecutive lines)
        StringBuilder doc = new StringBuilder();
        int currentPos = searchStart;
        
        // Walk backward through consecutive comment lines
        while (currentPos >= 0) {
            // Find start of current line
            int lineStart = currentPos;
            while (lineStart > 0 && text.charAt(lineStart - 1) != '\n') {
                lineStart--;
            }
            
            // Find end of current line
            int lineEnd = currentPos;
            while (lineEnd < text.length() && text.charAt(lineEnd) != '\n') {
                lineEnd++;
            }
            
            // Extract the line and trim
            String line = text.substring(lineStart, lineEnd).trim();
            
            // Check if this line is a comment
            if (line.startsWith("//") || line.startsWith("#")) {
                // Prepend to doc (we're going backwards)
                String commentText = line.substring(2).trim();
                if (doc.length() > 0) {
                    doc.insert(0, "\n");
                }
                doc.insert(0, commentText);
                
                // Move to previous line
                if (lineStart > 0) {
                    currentPos = lineStart - 1;
                    // Skip to previous line (skip the newline)
                    while (currentPos >= 0 && text.charAt(currentPos) == '\n') {
                        currentPos--;
                    }
                } else {
                    break;
                }
            } else {
                // Not a comment line, stop
                break;
            }
        }
        
        if (doc.length() > 0) {
            return doc.toString();
        }
        
        return null;
    }
    
    /**
     * Clean up documentation comment text by removing comment markers and extra formatting.
     */
    private String cleanDocumentation(String comment) {
        if (comment == null || comment.isEmpty()) return null;
        
        // Remove /* and */ markers
        comment = comment.replaceAll("^/\\*+\\s*", "").replaceAll("\\s*\\*+/$", "");
        
        // Split into lines and clean each one
        String[] lines = comment.split("\n");
        StringBuilder cleaned = new StringBuilder();
        
        for (String line : lines) {
            // Remove leading asterisks and whitespace
            line = line.trim().replaceAll("^\\*+\\s*", "");
            if (cleaned.length() > 0 && !line.isEmpty()) {
                cleaned.append("\n");
            }
            if (!line.isEmpty()) {
                cleaned.append(line);
            }
        }
        
        String result = cleaned.toString().trim();
        return result.isEmpty() ? null : result;
    }

    private TypeInfo resolveType(String typeName) {
        return resolveTypeAndTrackUsage(typeName);
    }
    
    /**
     * Resolve a type and track the import usage for unused import detection.
     * Checks script-defined types first, then falls back to imported types.
     */
    private TypeInfo resolveTypeAndTrackUsage(String typeName) {
        if (typeName == null || typeName.isEmpty())
            return null;

        // Strip generics for resolution
        int genericStart = typeName.indexOf('<');
        String baseName = genericStart > 0 ? typeName.substring(0, genericStart) : typeName;

        // Strip array brackets
        baseName = baseName.replace("[]", "").trim();

        // Check primitive types first
        if (TypeResolver.isPrimitiveType(baseName)) {
            return TypeInfo.fromPrimitive(baseName);
        }

        // Check for String (common case)
        if (baseName.equals("String")) {
            return typeResolver.resolveFullName("java.lang.String");
        }

        // Check script-defined types FIRST
        if (scriptTypes.containsKey(baseName)) {
            return scriptTypes.get(baseName);
        }

        // Check if there's an explicit import for this type
        ImportData usedImport = importsBySimpleName.get(baseName);
        
        // Try to resolve through imports/classpath
        TypeInfo result = typeResolver.resolveSimpleName(baseName, importsBySimpleName, wildcardPackages);
        
        // Track the import usage if we found one and the resolution succeeded
        if (result != null && usedImport != null) {
            usedImport.incrementUsage();
        }
        
        // Also check wildcard imports if the type was resolved through one
        if (result != null && usedImport == null && wildcardPackages != null) {
            String resultPkg = result.getPackageName();
            // Check if resolved through a wildcard
            for (ImportData imp : imports) {
                if (imp.isWildcard() && resultPkg != null && resultPkg.equals(imp.getFullPath())) {
                    imp.incrementUsage();
                    break;
                }
            }
        }
        
        return result;
    }

    // ==================== PHASE 4: BUILD MARKS ====================

    private List<ScriptLine.Mark> buildMarks() {
        List<ScriptLine.Mark> marks = new ArrayList<>();

        // Comments and strings first (highest priority)
        addPatternMarks(marks, COMMENT_PATTERN, TokenType.COMMENT);
        addPatternMarks(marks, STRING_PATTERN, TokenType.STRING);

        // Import statements
        markImports(marks);

        // Class/interface/enum declarations
        markClassDeclarations(marks);

        // Keywords and modifiers
        addPatternMarks(marks, KEYWORD_PATTERN, TokenType.KEYWORD);
        addPatternMarks(marks, MODIFIER_PATTERN, TokenType.MODIFIER);

        // Type declarations and usages
        markTypeDeclarations(marks);

        // Methods
        markMethodDeclarations(marks);

        // Numbers and othe
        addPatternMarks(marks, NUMBER_PATTERN, TokenType.LITERAL);

        // Method calls (parse before variables so we can attach context)
        markMethodCalls(marks);

        // Variables and fields
        markVariables(marks);

        // Chained field accesses (e.g., mc.player.world, this.field)
        markChainedFieldAccesses(marks);
        
        // Cast type expressions (e.g., (EntityPlayer) entity)
        markCastTypes(marks);
        
        // Imported class usages
        markImportedClassUsages(marks);
        
        // Mark unused imports (after all other marks are built)
        markUnusedImports(marks);

        return marks;
    }
    
    /**
     * Find and mark unused imports as UNUSED_IMPORT type.
     * This must be called after all other mark building is complete.
     */
    private void markUnusedImports(List<ScriptLine.Mark> marks) {
        for (ImportData imp : imports) {
            if (!imp.isUsed() && imp.isResolved() && !imp.isWildcard()) {
                // This import is not used - find and update its marks
                // Mark the entire import line as unused (gray color)
                marks.add(new ScriptLine.Mark(imp.getStartOffset(), imp.getEndOffset(), 
                          TokenType.UNUSED_IMPORT, imp));
            }
        }
    }

    private void addPatternMarks(List<ScriptLine.Mark> marks, Pattern pattern, TokenType type) {
        addPatternMarks(marks, pattern, type, 0);
    }

    private void addPatternMarks(List<ScriptLine.Mark> marks, Pattern pattern, TokenType type, int group) {
        Matcher m = pattern.matcher(text);
        while (m.find()) {
            marks.add(new ScriptLine.Mark(m.start(group), m.end(group), type));
        }
    }

    private void markImports(List<ScriptLine.Mark> marks) {
        for (ImportData imp : imports) {
            // Mark 'import' keyword
            marks.add(new ScriptLine.Mark(imp.getStartOffset(), imp.getStartOffset() + 6, TokenType.IMPORT_KEYWORD, imp));

            // Parse path tokens
            int pathStart = imp.getPathStartOffset();
            int pathEnd = imp.getPathEndOffset();
            String pathText = text.substring(pathStart, Math.min(pathEnd, text.length()));
            
            // Skip if path ends with dot (incomplete)
            if (pathText.trim().endsWith(".")) {
                continue;
            }
            
            // Tokenize the path
            List<String> tokens = new ArrayList<>();
            List<Integer> tokenStarts = new ArrayList<>();
            List<Integer> tokenEnds = new ArrayList<>();
            Pattern idPattern = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");
            Matcher idm = idPattern.matcher(pathText);
            while (idm.find()) {
                tokens.add(idm.group());
                tokenStarts.add(pathStart + idm.start());
                tokenEnds.add(pathStart + idm.end());
            }
            
            if (tokens.isEmpty()) continue;
            
            if (imp.isWildcard()) {
                // Wildcard import: mark all tokens as package (blue) if valid
                boolean pkgValid = typeResolver.isValidPackage(imp.getFullPath());
                
                // Also check if it might be a class wildcard (OuterClass.*)
                TypeInfo outerType = typeResolver.resolveFullName(imp.getFullPath());
                
                if (pkgValid || outerType != null) {
                    // Mark package portion in blue
                    int pkgTokenCount = outerType != null ? tokens.size() - 1 : tokens.size();
                    for (int i = 0; i < Math.min(pkgTokenCount, tokens.size()); i++) {
                        marks.add(new ScriptLine.Mark(tokenStarts.get(i), tokenEnds.get(i), TokenType.TYPE_DECL, imp));
                    }
                    // If it's a class wildcard, mark the class with its type
                    if (outerType != null && tokens.size() > 0) {
                        int lastIdx = tokens.size() - 1;
                        marks.add(new ScriptLine.Mark(tokenStarts.get(lastIdx), tokenEnds.get(lastIdx), 
                                outerType.getTokenType(), imp));
                    }
                } else {
                    // Unknown package/class - mark as undefined
                    marks.add(new ScriptLine.Mark(pathStart, pathEnd, TokenType.UNDEFINED_VAR, imp));
                }
            } else {
                // Non-wildcard import: package is blue, class name(s) get type colors
                TypeInfo resolvedType = imp.getResolvedType();
                
                if (resolvedType != null && resolvedType.isResolved()) {
                    // Count package segments
                    String fullPath = imp.getFullPath();
                    String resolvedName = resolvedType.getFullName();
                    String pkgName = resolvedType.getPackageName();
                    
                    int pkgSegments = pkgName != null && !pkgName.isEmpty() 
                            ? pkgName.split("\\.").length : 0;
                    
                    // Mark package tokens in blue
                    for (int i = 0; i < Math.min(pkgSegments, tokens.size()); i++) {
                        marks.add(new ScriptLine.Mark(tokenStarts.get(i), tokenEnds.get(i), TokenType.TYPE_DECL, imp));
                    }
                    
                    // Mark class tokens with appropriate type colors
                    // For inner classes, each segment might have a different type
                    for (int i = pkgSegments; i < tokens.size(); i++) {
                        String segmentName = tokens.get(i);
                        // Try to resolve this specific class/inner class
                        StringBuilder classPath = new StringBuilder();
                        if (pkgName != null && !pkgName.isEmpty()) {
                            classPath.append(pkgName).append(".");
                        }
                        for (int j = pkgSegments; j <= i; j++) {
                            if (j > pkgSegments) classPath.append("$");
                            classPath.append(tokens.get(j));
                        }
                        
                        TypeInfo segmentType = typeResolver.resolveFullName(classPath.toString());
                        TokenType tokenType = segmentType != null 
                                ? segmentType.getTokenType() 
                                : resolvedType.getTokenType();
                        
                        marks.add(new ScriptLine.Mark(tokenStarts.get(i), tokenEnds.get(i), tokenType, imp));
                    }
                } else {
                    // Try to figure out which parts are valid vs invalid
                    // First, try to find valid package segments
                    int lastValidPkg = -1;
                    StringBuilder pkgBuilder = new StringBuilder();
                    
                    for (int i = 0; i < tokens.size(); i++) {
                        if (i > 0) pkgBuilder.append(".");
                        pkgBuilder.append(tokens.get(i));
                        
                        // Check if this is a valid package
                        if (typeResolver.isValidPackage(pkgBuilder.toString())) {
                            lastValidPkg = i;
                        }
                    }

                    // Now try to resolve outer classes that might exist after the package
                    // For import like kamkeel.api.IOverlay.idk where IOverlay exists but idk doesn't
                    int lastValidClass = -1;
                    TypeInfo lastValidType = null;
                    StringBuilder classPath = new StringBuilder();
                    
                    if (lastValidPkg >= 0) {
                        classPath.append(pkgBuilder.substring(0,
                                pkgBuilder.toString().indexOf(tokens.get(lastValidPkg)) + tokens.get(lastValidPkg)
                                                                                                .length()));
                    }

                    for (int i = lastValidPkg + 1; i < tokens.size(); i++) {
                        if (classPath.length() > 0)
                            classPath.append(i == lastValidPkg + 1 ? "." : "$");
                        classPath.append(tokens.get(i));

                        TypeInfo segmentType = typeResolver.resolveFullName(classPath.toString());
                        if (segmentType != null && segmentType.isResolved()) {
                            lastValidClass = i;
                            lastValidType = segmentType;
                        } else {
                            break; // Stop at first unresolved segment
                        }
                    }

                    // Mark valid package portion in blue
                    for (int i = 0; i <= lastValidPkg; i++) {
                        marks.add(new ScriptLine.Mark(tokenStarts.get(i), tokenEnds.get(i), TokenType.TYPE_DECL, imp));
                    }

                    // Mark valid class segments with their type colors
                    StringBuilder resolvedPath = new StringBuilder();
                    if (lastValidPkg >= 0) {
                        for (int i = 0; i <= lastValidPkg; i++) {
                            if (i > 0)
                                resolvedPath.append(".");
                            resolvedPath.append(tokens.get(i));
                        }
                    }

                    for (int i = lastValidPkg + 1; i <= lastValidClass; i++) {
                        if (resolvedPath.length() > 0)
                            resolvedPath.append(i == lastValidPkg + 1 ? "." : "$");
                        resolvedPath.append(tokens.get(i));

                        TypeInfo segmentType = typeResolver.resolveFullName(resolvedPath.toString());
                        TokenType tokenType = (segmentType != null) ? segmentType.getTokenType() : TokenType.IMPORTED_CLASS;
                        marks.add(new ScriptLine.Mark(tokenStarts.get(i), tokenEnds.get(i), tokenType, imp));
                    }

                    // Mark remaining unresolved segments as undefined (red)
                    int firstUnresolved = Math.max(lastValidPkg + 1, lastValidClass + 1);
                    for (int i = firstUnresolved; i < tokens.size(); i++) {
                        marks.add(new ScriptLine.Mark(tokenStarts.get(i), tokenEnds.get(i), TokenType.UNDEFINED_VAR,
                                imp));
                    }

                    // If nothing was valid at all, mark everything as undefined
                    if (lastValidPkg < 0 && lastValidClass < 0) {
                        marks.add(new ScriptLine.Mark(pathStart, pathEnd, TokenType.UNDEFINED_VAR, imp));
                    }
                }
            }
        }
    }

    private void markClassDeclarations(List<ScriptLine.Mark> marks) {
        Matcher m = CLASS_DECL_PATTERN.matcher(text);
        while (m.find()) {
            if (isExcluded(m.start()))
                continue;

            // Mark the keyword (class/interface/enum)
            marks.add(new ScriptLine.Mark(m.start(1), m.end(1), TokenType.CLASS_KEYWORD));

            // Mark the class name
            String kind = m.group(1);
            TokenType nameType;
            if ("interface".equals(kind)) {
                nameType = TokenType.INTERFACE_DECL;
            } else if ("enum".equals(kind)) {
                nameType = TokenType.ENUM_DECL;
            } else {
                nameType = TokenType.CLASS_DECL;
            }
            marks.add(new ScriptLine.Mark(m.start(2), m.end(2), nameType));
        }
    }

    private void markTypeDeclarations(List<ScriptLine.Mark> marks) {
        // Pattern for type optionally followed by generics - we'll manually parse generics
        Pattern typeStart = Pattern.compile(
                "(?:(?:public|private|protected|static|final|transient|volatile)\\s+)*" +
                        "([a-zA-Z][a-zA-Z0-9_]*)\\s*");

        Matcher m = typeStart.matcher(text);
        int searchFrom = 0;

        while (m.find(searchFrom)) {
            int typeNameStart = m.start(1);
            int typeNameEnd = m.end(1);

            if (isExcluded(typeNameStart)) {
                searchFrom = m.end();
                continue;
            }

            String typeName = m.group(1);
            int posAfterType = m.end(1);

            // Skip whitespace after type name
            while (posAfterType < text.length() && Character.isWhitespace(text.charAt(posAfterType))) {
                posAfterType++;
            }

            // Check for generic parameters
            String genericContent = null;
            int genericStart = -1;
            int genericEnd = -1;

            if (posAfterType < text.length() && text.charAt(posAfterType) == '<') {
                genericStart = posAfterType;
                int depth = 1;
                int i = posAfterType + 1;
                while (i < text.length() && depth > 0) {
                    char c = text.charAt(i);
                    if (c == '<')
                        depth++;
                    else if (c == '>')
                        depth--;
                    i++;
                }
                if (depth == 0) {
                    genericEnd = i;
                    genericContent = text.substring(genericStart + 1, genericEnd - 1);
                    posAfterType = genericEnd;
                }
            }

            // Skip whitespace after generics
            while (posAfterType < text.length() && Character.isWhitespace(text.charAt(posAfterType))) {
                posAfterType++;
            }

            // Check if this looks like a type declaration:
            boolean hasGeneric = genericContent != null && !genericContent.isEmpty();
            boolean followedByVarName = false;
            boolean atEndOfLine = posAfterType >= text.length() || text.charAt(posAfterType) == '\n';

            if (!atEndOfLine && posAfterType < text.length()) {
                char nextChar = text.charAt(posAfterType);
                followedByVarName = Character.isLetter(nextChar) || nextChar == '_';
            }

            // Accept as type if: has generics OR followed by variable name
            if (hasGeneric || followedByVarName) {
                // Resolve the main type
                TypeInfo info = resolveType(typeName);
                TokenType tokenType = (info != null && info.isResolved()) ? info.getTokenType() : TokenType.UNDEFINED_VAR;
                marks.add(new ScriptLine.Mark(typeNameStart, typeNameEnd, tokenType, info));

                // Handle generic content recursively
                if (hasGeneric && genericStart >= 0) {
                    int contentStart = genericStart + 1;
                    markGenericTypesRecursive(genericContent, contentStart, marks);
                }
            }

            searchFrom = m.end();
        }
    }



    /**
     * Recursively parse and mark generic type parameters.
     * Handles arbitrarily nested generics like Map<String, List<Map<String, String>>>.
     */
    private void markGenericTypesRecursive(String content, int baseOffset, List<ScriptLine.Mark> marks) {
        if (content == null || content.isEmpty())
            return;

        int i = 0;
        while (i < content.length()) {
            char c = content.charAt(i);

            // Skip whitespace and punctuation
            if (!Character.isJavaIdentifierStart(c)) {
                i++;
                continue;
            }

            // Found start of identifier
            int start = i;
            while (i < content.length() && Character.isJavaIdentifierPart(content.charAt(i))) {
                i++;
            }
            String typeName = content.substring(start, i);

            // Only process if it's an actual type (check via resolveType)
            TypeInfo typeCheck = resolveType(typeName);
            if (typeCheck != null && typeCheck.isResolved()) {
                int absStart = baseOffset + start;
                int absEnd = baseOffset + i;

                if (!isExcluded(absStart)) {
                    TypeInfo info = resolveType(typeName);
                    TokenType tokenType = (info != null && info.isResolved()) ? info.getTokenType() : TokenType.UNDEFINED_VAR;
                    marks.add(new ScriptLine.Mark(absStart, absEnd, tokenType, info));
                }
            }

            // Skip whitespace
            while (i < content.length() && Character.isWhitespace(content.charAt(i))) {
                i++;
            }

            // Check for nested generic
            if (i < content.length() && content.charAt(i) == '<') {
                int nestedStart = i + 1;
                int depth = 1;
                i++;

                // Find matching >
                while (i < content.length() && depth > 0) {
                    if (content.charAt(i) == '<')
                        depth++;
                    else if (content.charAt(i) == '>')
                        depth--;
                    i++;
                }

                // Recursively parse nested content
                if (nestedStart < i - 1) {
                    String nestedContent = content.substring(nestedStart, i - 1);
                    markGenericTypesRecursive(nestedContent, baseOffset + nestedStart, marks);
                }
            }
        }
    }

    private void markMethodDeclarations(List<ScriptLine.Mark> marks) {
        Matcher m = METHOD_DECL_PATTERN.matcher(text);
        while (m.find()) {
            if (isExcluded(m.start()))
                continue;

            // Skip class/interface/enum declarations - these look like method declarations
            // but "class Foo(" is not a method, it's a class declaration
            String returnType = m.group(1);
            if (returnType.equals("class") || returnType.equals("interface") || returnType.equals("enum") ||returnType.equals("new")) {
                continue;
            }

            // Find the corresponding MethodInfo created in parseMethodDeclarations
            int methodDeclStart = m.start();
            MethodInfo methodInfo = null;
            for (MethodInfo method : methods) {
                if (method.getDeclarationOffset() == methodDeclStart) {
                    methodInfo = method;
                    break;
                }
            }

            // Return type
            marks.add(new ScriptLine.Mark(m.start(1), m.end(1), TokenType.TYPE_DECL));
            // Method name with MethodInfo metadata
            marks.add(new ScriptLine.Mark(m.start(2), m.end(2), TokenType.METHOD_DECL, methodInfo));
        }
    }

    private void markMethodCalls(List<ScriptLine.Mark> marks) {
        Matcher m = METHOD_CALL_PATTERN.matcher(text);
        while (m.find()) {
            int nameStart = m.start(1);
            int nameEnd = m.end(1);
            String methodName = m.group(1);

            if (isExcluded(nameStart))
                continue;

            // Skip if in import/package statement
            if (isInImportOrPackage(nameStart))
                continue;

            // Find the opening parenthesis by scanning forward from the method name end
            // The regex includes \( but we scan manually to be safe
            int openParen = nameEnd;
            while (openParen < text.length() && Character.isWhitespace(text.charAt(openParen))) {
                openParen++;
            }
            
            if (openParen >= text.length() || text.charAt(openParen) != '(') {
                // Not actually a method call
                continue;
            }
            
            // Find the matching closing parenthesis
            int closeParen = findMatchingParen(openParen);
            if (closeParen < 0) {
                // Malformed - no closing paren
                continue;
            }

            // Parse the arguments
            List<MethodCallInfo.Argument> arguments = parseMethodArguments(openParen + 1, closeParen);

            // Check if this is a static access (Class.method() style)
            boolean isStaticAccess = isStaticAccessCall(nameStart);
            
            // Resolve receiver using existing chain-based resolver and detect static access
            TypeInfo receiverType = resolveReceiverChain(nameStart);
            MethodInfo resolvedMethod = null;
            boolean computedStaticAccess = isStaticAccessCall(nameStart);

            if (receiverType != null) {
                if (receiverType.hasMethod(methodName)) {
          
                    TypeInfo[] argTypes = arguments.stream().map(MethodCallInfo.Argument::getResolvedType).toArray(TypeInfo[]::new);
                    // Get best method overload based on argument types
                    resolvedMethod = receiverType.getBestMethodOverload(methodName, argTypes);
                    
                    MethodCallInfo callInfo = new MethodCallInfo(
                        methodName, nameStart, nameEnd, openParen, closeParen,
                        arguments, receiverType, resolvedMethod, computedStaticAccess
                    );
                    
                    // Set expected type for validation if this is the final expression
                    if (!isFollowedByDot(closeParen)) {
                        TypeInfo expectedType = findExpectedTypeAtPosition(nameStart);
                        if (expectedType != null) {
                            callInfo.setExpectedType(expectedType);
                        }
                    }
                    
                    callInfo.validate();
                    methodCalls.add(callInfo);
                    marks.add(new ScriptLine.Mark(nameStart, nameEnd, TokenType.METHOD_CALL, callInfo));
                } else {
                    marks.add(new ScriptLine.Mark(nameStart, nameEnd, TokenType.UNDEFINED_VAR));
                }
            } else {
                boolean hasDot = isPrecededByDot(nameStart);
                if (hasDot) {
                    marks.add(new ScriptLine.Mark(nameStart, nameEnd, TokenType.UNDEFINED_VAR));
                } else {
                    if (isScriptMethod(methodName)) {
                        // Extract argument types from parsed arguments
                        
                        TypeInfo[] argTypes = arguments.stream().map(MethodCallInfo.Argument::getResolvedType).toArray(TypeInfo[]::new);
                        resolvedMethod = getScriptMethodInfo(methodName, argTypes);
                        
                        // Check if this is a method from a script type
                        // Instance methods from script types cannot be called without a receiver
                        if (resolvedMethod != null && isMethodFromScriptType(resolvedMethod) && !resolvedMethod.isStatic()) {
                            // Instance method called without receiver - mark as undefined
                            marks.add(new ScriptLine.Mark(nameStart, nameEnd, TokenType.UNDEFINED_VAR));
                        } else {
                            MethodCallInfo callInfo = new MethodCallInfo(
                                methodName, nameStart, nameEnd, openParen, closeParen,
                                arguments, null, resolvedMethod
                            );
                            
                            // Only set expected type if this is the final expression (not followed by .field or .method)
                            if (!isFollowedByDot(closeParen)) {
                                TypeInfo expectedType = findExpectedTypeAtPosition(nameStart);
                                if (expectedType != null) {
                                    callInfo.setExpectedType(expectedType);
                                }
                            }
                            
                            callInfo.validate();
                            methodCalls.add(callInfo);
                            marks.add(new ScriptLine.Mark(nameStart, nameEnd, TokenType.METHOD_CALL, callInfo));
                        }
                    } else {
                        marks.add(new ScriptLine.Mark(nameStart, nameEnd, TokenType.UNDEFINED_VAR));
                    }
                }
            }
        }
    }
    
    /**
     * Check if a method call is a static access (Class.method() style).
     * Returns true if the immediate receiver before the dot is a class name (uppercase).
     */
    private boolean isStaticAccessCall(int methodNameStart) {
        // Walk backward to find the dot
        int pos = methodNameStart - 1;
        while (pos >= 0 && Character.isWhitespace(text.charAt(pos)))
            pos--;
        
        if (pos < 0 || text.charAt(pos) != '.')
            return false;
        
        // Skip the dot and any whitespace
        pos--;
        while (pos >= 0 && Character.isWhitespace(text.charAt(pos)))
            pos--;
        
        if (pos < 0)
            return false;
        
        // Check what's before the dot - could be:
        // 1. An identifier ending (field or class name)
        // 2. A closing paren (method call result)
        // 3. A closing bracket (array access)
        
        char c = text.charAt(pos);
        if (c == ')' || c == ']') {
            // Method call or array - this is instance access
            return false;
        }
        
        if (!Character.isJavaIdentifierPart(c))
            return false;
        
        // Read the identifier backward
        int identEnd = pos + 1;
        while (pos >= 0 && Character.isJavaIdentifierPart(text.charAt(pos)))
            pos--;
        int identStart = pos + 1;
        
        String ident = text.substring(identStart, identEnd);
        
        // Check if this is a class name (starts with uppercase)
        // AND check if there's no dot before it (making it a direct class reference)
        // If there's a chain like "obj.SomeClass.method()", we need to check further
        
        // Skip whitespace before the identifier
        while (pos >= 0 && Character.isWhitespace(text.charAt(pos)))
            pos--;
        
        // If preceded by a dot, this might be part of a longer chain
        // In that case, it's not a direct static access
        if (pos >= 0 && text.charAt(pos) == '.') {
            return false;
        }
        
        // It's static access if the identifier resolves to a type
        if (ident.isEmpty()) return false;
        TypeInfo typeCheck = resolveType(ident);
        return typeCheck != null && typeCheck.isResolved();
    }

    /**
     * Find the MethodCallInfo that contains the given position as an argument.
     * Returns null if the position is not inside any method call's argument list.
     */
    private MethodCallInfo findMethodCallContainingPosition(int position) {
        for (MethodCallInfo call : methodCalls) {
            // Check if position is within the argument list (between open and close parens)
            if (position >= call.getOpenParenOffset() && position <= call.getCloseParenOffset()) {
                // Check if it's within any of the arguments
                for (MethodCallInfo.Argument arg : call.getArguments()) {
                    if (position >= arg.getStartOffset() && position <= arg.getEndOffset()) {
                        return call;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Find the matching closing parenthesis for an opening parenthesis.
     * Handles nested parentheses, strings, and comments.
     */
    private int findMatchingParen(int openPos) {
        if (openPos < 0 || openPos >= text.length() || text.charAt(openPos) != '(') {
            return -1;
        }
        
        int depth = 1;
        boolean inString = false;
        boolean inChar = false;
        char stringChar = 0;
        
        for (int i = openPos + 1; i < text.length(); i++) {
            char c = text.charAt(i);
            char prev = (i > 0) ? text.charAt(i - 1) : 0;
            
            // Handle string literals
            if (!inChar && (c == '"' || c == '\'') && prev != '\\') {
                if (!inString) {
                    inString = true;
                    stringChar = c;
                } else if (c == stringChar) {
                    inString = false;
                }
                continue;
            }
            
            if (inString) continue;
            
            // Skip excluded regions (comments)
            if (isExcluded(i)) continue;
            
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        
        return -1; // No matching paren found
    }

    /**
     * Parse method arguments from the text between opening and closing parentheses.
     * Handles nested expressions, strings, and complex argument types.
     */
    private List<MethodCallInfo.Argument> parseMethodArguments(int start, int end) {
        List<MethodCallInfo.Argument> args = new ArrayList<>();
        
        if (start >= end) {
            return args; // No arguments
        }
        
        int depth = 0;
        int argStart = start;
        boolean inString = false;
        char stringChar = 0;
        
        for (int i = start; i <= end; i++) {
            if (i == end || (depth == 0 && !inString && text.charAt(i) == ',')) {
                // End of an argument
                String argText = text.substring(argStart, i).trim();
                if (!argText.isEmpty()) {
                    // Find the actual start/end positions (excluding leading/trailing whitespace)
                    int actualStart = argStart;
                    while (actualStart < i && Character.isWhitespace(text.charAt(actualStart))) {
                        actualStart++;
                    }
                    int actualEnd = i;
                    while (actualEnd > actualStart && Character.isWhitespace(text.charAt(actualEnd - 1))) {
                        actualEnd--;
                    }
                    
                    // Try to resolve the argument type
                    // First check if this looks like a parameter declaration (Type varName)
                    TypeInfo argType = resolveArgumentType(argText, actualStart);
                    
                    args.add(new MethodCallInfo.Argument( 
                        argText, actualStart, actualEnd, argType, true, null
                    ));
                }
                argStart = i + 1;
                continue;
            }
            
            char c = text.charAt(i);
            char prev = (i > start) ? text.charAt(i - 1) : 0;
            
            // Handle string literals
            if (!inString && (c == '"' || c == '\'') && prev != '\\') {
                inString = true;
                stringChar = c;
            } else if (inString && c == stringChar && prev != '\\') {
                inString = false;
            }
            
            if (!inString) {
                if (c == '(' || c == '[' || c == '{' || c == '<') {
                    depth++;
                } else if (c == ')' || c == ']' || c == '}' || c == '>') {
                    depth--;
                }
            }
        }
        
        return args;
    }

    /**
     * Resolve the type of a method call argument.
     * Handles both parameter declarations (Type varName) and expressions (variable.field()).
     */
    private TypeInfo resolveArgumentType(String argText, int position) {
        argText = argText.trim();
        
        // Check if this looks like a parameter declaration: "Type varName"
        // Pattern: identifier followed by whitespace and another identifier
        if (argText.matches("^[A-Za-z_][a-zA-Z0-9_<>\\[\\],\\s]*\\s+[a-zA-Z_][a-zA-Z0-9_]*$")) {
            // Split into tokens
            String[] parts = argText.split("\\s+");
            if (parts.length >= 2) {
                // Last part is the variable name, everything before is the type
                // Join all but last as the type name
                StringBuilder typeBuilder = new StringBuilder();
                for (int i = 0; i < parts.length - 1; i++) {
                    if (i > 0) typeBuilder.append(" ");
                    typeBuilder.append(parts[i]);
                }
                String typeName = typeBuilder.toString();
                return resolveType(typeName);
            }
        }
        
        // Otherwise, treat it as an expression
        return resolveExpressionType(argText, position);
    }
    
    /**
     * Check if a numeric literal has excessive precision for its type.
     * Counts significant digits (excluding leading zeros, decimal point, and suffix).
     * 
     * @param numLiteral The numeric literal string (e.g., "1.23456789f", "0.00123456789")
     * @param maxDigits Maximum significant digits allowed (7 for float, 15 for double)
     * @return true if the literal has more significant digits than allowed
     */
    private boolean hasExcessivePrecision(String numLiteral, int maxDigits) {
        String cleaned = numLiteral.trim();
        
        // Remove leading sign
        if (cleaned.startsWith("-") || cleaned.startsWith("+")) {
            cleaned = cleaned.substring(1);
        }
        
        // Remove suffix (f, F, d, D, l, L)
        if (cleaned.endsWith("f") || cleaned.endsWith("F") || 
            cleaned.endsWith("d") || cleaned.endsWith("D") ||
            cleaned.endsWith("l") || cleaned.endsWith("L")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }
        
        // Remove decimal point for counting
        cleaned = cleaned.replace(".", "");
        
        // Remove leading zeros (they're not significant in the mantissa)
        while (cleaned.startsWith("0") && cleaned.length() > 1) {
            cleaned = cleaned.substring(1);
        }
        
        // If it's just "0", that's fine
        if (cleaned.equals("0") || cleaned.isEmpty()) {
            return false;
        }
        
        // Count significant digits
        int significantDigits = cleaned.length();
        
        return significantDigits > maxDigits;
    }

    /**
     * Comprehensive expression type resolver that handles:
     * - Literals (strings, numbers, booleans, null)
     * - Variables (local, parameter, global)
     * - Simple identifiers
     * - Chained field accesses (a.b.c)
     * - Chained method calls (a().b().c())
     * - Mixed chains (a.b().c.d())
     * - Static access (Class.field, Class.method())
     * - New expressions (new Type())
     * - All Java operators (arithmetic, logical, bitwise, etc.)
     * 
     * This is THE method that should be used for all type resolution needs.
     */
    private TypeInfo resolveExpressionType(String expr, int position) {
        expr = expr.trim();
        
        if (expr.isEmpty()) {
            return null;
        }
        
        // Check if expression contains operators - if so, use the full expression resolver
        // Handle cast expressions: (Type)expr, ((Type)expr).method(), etc.
        if (containsOperators(expr) || expr.startsWith("(")) {
            return resolveExpressionWithParserAPI(expr, position);
        }
        
        // Invalid expressions starting with brackets
        if (expr.startsWith("[") || expr.startsWith("]")) {
            return null; // Invalid syntax
        }
        
        // String literals
        if (expr.startsWith("\"") && expr.endsWith("\"")) {
            return resolveType("String");
        }
        
        // Character literals
        if (expr.startsWith("'") && expr.endsWith("'")) {
            return TypeInfo.fromPrimitive("char");
        }
        
        // Boolean literals
        if (expr.equals("true") || expr.equals("false")) {
            return TypeInfo.fromPrimitive("boolean");
        }
        
        // Null literal
        if (expr.equals("null")) {
            return TypeInfo.unresolved("null", "<null>"); // null is compatible with any reference type
        }
        
        // Numeric literals with precision checking
        // Float: can be 10f, 10.5f, 10.f, .5f
        if (expr.matches("-?\\d*\\.?\\d+[fF]")) {
            // Check if it has too many decimal places for float (>7 significant digits)
            // If so, treat it as double (causing type mismatch)
            if (hasExcessivePrecision(expr, 7)) {
                return TypeInfo.fromPrimitive("double");
            }
            return TypeInfo.fromPrimitive("float");
        }
        // Double: can be 10d, 10.5d, 10.5, .5, 10., but NOT plain integers
        if (expr.matches("-?\\d*\\.\\d+[dD]?") || expr.matches("-?\\d+\\.[dD]?") || expr.matches("-?\\d+[dD]")) {
            // Check if it has too many decimal places for double (>15 significant digits)
            // Return null to indicate the literal is invalid/unrepresentable
            if (hasExcessivePrecision(expr, 15)) {
                return null; // Exceeds double precision
            }
            return TypeInfo.fromPrimitive("double");
        }
        // Long: 10L or 10l
        if (expr.matches("-?\\d+[lL]")) {
            return TypeInfo.fromPrimitive("long");
        }
        // Int: plain integers without suffix
        if (expr.matches("-?\\d+")) {
            return TypeInfo.fromPrimitive("int");
        }
        
        // "this" keyword
        if (expr.equals("this")) {
            return findEnclosingScriptType(position);
        }
        
        // "new Type()" expressions
        if (expr.startsWith("new ")) {
            Matcher newMatcher = NEW_TYPE_PATTERN.matcher(expr);
            if (newMatcher.find()) {
                return resolveType(newMatcher.group(1));
            }
        }
        
        // Now handle the complex case: chains of fields and method calls
        // Parse the expression into segments
        List<ChainSegment> segments = parseExpressionChain(expr);
        
        if (segments.isEmpty()) {
            return null;
        }
        
        // Resolve the first segment
        ChainSegment first = segments.get(0);
        TypeInfo currentType = null;
        
        if (first.name.equals("this")) {
            currentType = findEnclosingScriptType(position);
            // Handle this.field where we don't have a script type
            if (currentType == null && segments.size() > 1 && !segments.get(1).isMethodCall) {
                String fieldName = segments.get(1).name;
                if (globalFields.containsKey(fieldName)) {
                    currentType = globalFields.get(fieldName).getTypeInfo();
                    // Continue from segment 2
                    for (int i = 2; i < segments.size(); i++) {
                        currentType = resolveChainSegment(currentType, segments.get(i));
                        if (currentType == null) return null;
                    }
                    return currentType;
                }
            }
        } else {
            // Check if first segment is a type name for static access
            TypeInfo typeCheck = resolveType(first.name);
            if (typeCheck != null && typeCheck.isResolved()) {
                currentType = typeCheck;
            }
        }
        
        if (currentType == null && !first.isMethodCall) {
            // Regular variable
            FieldInfo varInfo = resolveVariable(first.name, position);
            if (varInfo != null) {
                currentType = varInfo.getTypeInfo();
            }
        } else {
            // First segment is a method call - check script methods
            if (isScriptMethod(first.name)) {
                MethodInfo scriptMethod = getScriptMethodInfo(first.name);
                if (scriptMethod != null) {
                    currentType = scriptMethod.getReturnType();
                }
            }
        }
        
        // Resolve the rest of the chain
        for (int i = 1; i < segments.size(); i++) {
            currentType = resolveChainSegment(currentType, segments.get(i));
            if (currentType == null) {
                return null;
            }
        }
        
        return currentType;
    }

    /**
     * Resolve a cast or parenthesized expression.
     * Delegates to CastExpressionResolver helper class.
     * Handles:
     * - Simple casts: (Type) expr
     * - Nested casts: ((Type) expr)
     * - Cast chains: ((Type) expr).method()
     * - Parenthesized expressions: (expr)
     * 
     * @param expr The expression starting with '('
     * @param position The position in the source text
     * @return The resolved type
     */
    private TypeInfo resolveCastOrParenthesizedExpression(String expr, int position) {
        return CastExpressionResolver.resolveCastOrParenthesizedExpression(
            expr,
            position,
            this::resolveType,
            this::resolveExpressionType,
            this::parseExpressionChain,
            this::resolveChainSegment
        );
    }

    
    /**
     * Parse an expression string into chain segments.
     * Handles dots, method calls, and nested expressions.
     * Examples:
     * - "a.b.c" -> [a, b, c] (all fields)
     * - "a().b()" -> [a(), b()] (all methods)
     * - "a.b().c" -> [a, b(), c] (mixed)
     */
    private List<ChainSegment> parseExpressionChain(String expr) {
        List<ChainSegment> segments = new ArrayList<>();
        int i = 0;
        
        while (i < expr.length()) {
            // Skip whitespace
            while (i < expr.length() && Character.isWhitespace(expr.charAt(i))) {
                i++;
            }
            
            if (i >= expr.length()) break;
            
            // Read identifier
            int start = i;
            while (i < expr.length() && Character.isJavaIdentifierPart(expr.charAt(i))) {
                i++;
            }
            
            if (i == start) {
                // Not an identifier, skip this character
                i++;
                continue;
            }
            
            String name = expr.substring(start, i);
            
            // Skip whitespace
            while (i < expr.length() && Character.isWhitespace(expr.charAt(i))) {
                i++;
            }
            
            // Check if followed by parentheses (method call)
            boolean isMethodCall = false;
            String arguments = null;
            if (i < expr.length() && expr.charAt(i) == '(') {
                isMethodCall = true;
                int argsStart = i + 1;
                // Skip to the matching closing paren
                int depth = 1;
                i++;
                while (i < expr.length() && depth > 0) {
                    char c = expr.charAt(i);
                    if (c == '(') depth++;
                    else if (c == ')') depth--;
                    i++;
                }
                // Extract argument text (between parentheses)
                int argsEnd = i - 1;  // Position of closing paren
                if (argsEnd > argsStart) {
                    arguments = expr.substring(argsStart, argsEnd);
                } else {
                    arguments = "";  // Empty arguments
                }
            }
            
            // Check if followed by array brackets (array access)
            // Skip array accesses like [0] or [i] - treat them as part of the current segment
            while (i < expr.length() && Character.isWhitespace(expr.charAt(i))) {
                i++;
            }
            if (i < expr.length() && expr.charAt(i) == '[') {
                // Skip to the matching closing bracket
                int depth = 1;
                i++;
                while (i < expr.length() && depth > 0) {
                    char c = expr.charAt(i);
                    if (c == '[') depth++;
                    else if (c == ']') depth--;
                    i++;
                }
            }
            
            segments.add(new ChainSegment(name, start, i, isMethodCall, arguments));
            
            // Skip whitespace
            while (i < expr.length() && Character.isWhitespace(expr.charAt(i))) {
                i++;
            }
            
            // Check for dot (continuing chain)
            if (i < expr.length() && expr.charAt(i) == '.') {
                i++; // Skip the dot
            }
        }
        
        return segments;
    }

    /**
     * Resolve the full receiver chain before a method call position.
     * For example, for "mc.thePlayer.worldObj.weatherEffects.get()", 
     * this would resolve mc -> Minecraft -> thePlayer -> EntityPlayer -> worldObj -> World -> weatherEffects -> List
     * and return the final TypeInfo (List).
     * 
     * Also handles method calls in chains like "Minecraft.getMinecraft().thePlayer" by resolving
     * the return type of getMinecraft() and continuing resolution.
     *
     * @param methodNameStart The start position of the method name
     * @return The TypeInfo of the final receiver, or null if no receiver or couldn't resolve
     */
    private TypeInfo resolveReceiverChain(int methodNameStart) {
        // First check if preceded by a dot
        int scanPos = methodNameStart - 1;
        while (scanPos >= 0 && Character.isWhitespace(text.charAt(scanPos)))
            scanPos--;

        if (scanPos < 0 || text.charAt(scanPos) != '.') {
            return null; // No receiver
        }

        // Use the shared helper to locate the receiver expression before the dot,
        // then delegate resolution to the comprehensive resolver.
        int[] bounds = findReceiverBoundsBefore(scanPos);
        if (bounds == null) return null;
        int start = bounds[0];
        int end = bounds[1];
        String receiverExpr = text.substring(start, end).trim();
        if (receiverExpr.isEmpty()) return null;
        return resolveExpressionType(receiverExpr, start);
    }

    /**
     * Parse argument types from a method call's argument list.
     * @param argsText The text between parentheses (e.g., "20, 20" or "x, y.toString()")
     * @param position The position in the source text
     * @return Array of TypeInfo for each argument, or empty array if no arguments
     */
    private TypeInfo[] parseArgumentTypes(String argsText, int position) {
        if (argsText == null || argsText.trim().isEmpty()) {
            return new TypeInfo[0];
        }
        
        // Split arguments by comma, respecting nested parentheses and strings
        java.util.List<String> args = new java.util.ArrayList<>();
        int depth = 0;
        int start = 0;
        boolean inString = false;
        char stringChar = 0;
        
        for (int i = 0; i < argsText.length(); i++) {
            char c = argsText.charAt(i);
            
            // Handle strings
            if ((c == '"' || c == '\'') && (i == 0 || argsText.charAt(i-1) != '\\')) {
                if (!inString) {
                    inString = true;
                    stringChar = c;
                } else if (c == stringChar) {
                    inString = false;
                }
                continue;
            }
            
            if (inString) continue;
            
            // Track parentheses depth
            if (c == '(' || c == '[' || c == '{') {
                depth++;
            } else if (c == ')' || c == ']' || c == '}') {
                depth--;
            } else if (c == ',' && depth == 0) {
                // Found argument separator at top level
                args.add(argsText.substring(start, i).trim());
                start = i + 1;
            }
        }
        
        // Add the last argument
        if (start < argsText.length()) {
            args.add(argsText.substring(start).trim());
        }
        
        // Resolve each argument's type
        TypeInfo[] argTypes = new TypeInfo[args.size()];
        for (int i = 0; i < args.size(); i++) {
            argTypes[i] = resolveExpressionType(args.get(i), position);
        }
        
        return argTypes;
    }

    /**
     * Helper class for chain segments (can be field access or method call).
     */
    private static class ChainSegment {
        final String name;
        final int start;
        final int end;
        final boolean isMethodCall;
        final String arguments;  // The text between parentheses for method calls, or null for fields
        
        ChainSegment(String name, int start, int end, boolean isMethodCall, String arguments) {
            this.name = name;
            this.start = start;
            this.end = end;
            this.isMethodCall = isMethodCall;
            this.arguments = arguments;
        }
    }

    /**
     * Resolve a single segment of a chain given the current type context.
     */
    private TypeInfo resolveChainSegment(TypeInfo currentType, ChainSegment segment) {
        if (currentType == null || !currentType.isResolved()) {
            return null;
        }
        
        if (segment.isMethodCall) {
            // Method call - get return type with argument-based overload resolution
            if (currentType.hasMethod(segment.name)) {
                // Parse argument types from the method call
                TypeInfo[] argTypes = parseArgumentTypes(segment.arguments, segment.start);
                
                // Get the best matching overload based on argument types
                MethodInfo methodInfo = currentType.getBestMethodOverload(segment.name, argTypes);
                return (methodInfo != null) ? methodInfo.getReturnType() : null;
            }
            return null;
        } else {
            // Field access
            if (currentType.hasField(segment.name)) {
                FieldInfo fieldInfo = currentType.getFieldInfo(segment.name);
                return (fieldInfo != null) ? fieldInfo.getTypeInfo() : null;
            }
            return null;
        }
    }

    // ==================== OPERATOR EXPRESSION RESOLUTION ====================
    
    /**
     * Check if an expression contains operators that need advanced resolution.
     * This is a quick heuristic check - it may have false positives for operators
     * inside strings, but those are handled by the full parser.
     */
    private boolean containsOperators(String expr) {
        if (expr == null) return false;
        
        boolean inString = false;
        boolean inChar = false;
        
        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);
            char next = (i + 1 < expr.length()) ? expr.charAt(i + 1) : 0;
            
            // Track string literals
            if (c == '"' && !inChar) {
                if (!inString) {
                    inString = true;
                } else if (i > 0 && expr.charAt(i - 1) != '\\') {
                    inString = false;
                }
                continue;
            }
            
            // Track char literals  
            if (c == '\'' && !inString) {
                if (!inChar) {
                    inChar = true;
                } else if (i > 0 && expr.charAt(i - 1) != '\\') {
                    inChar = false;
                }
                continue;
            }
            
            // Skip content inside strings/chars
            if (inString || inChar) continue;
            
            // Check for operators (excluding . which is used for member access)
            switch (c) {
                case '+':
                    // + is arithmetic unless it's unary at start or after operator
                    if (next == '+') return true; // ++
                    if (next == '=') return true; // +=
                    // Check if this is binary + by looking at previous non-whitespace
                    int prevIdx = i - 1;
                    while (prevIdx >= 0 && Character.isWhitespace(expr.charAt(prevIdx))) prevIdx--;
                    if (prevIdx >= 0) {
                        char prev = expr.charAt(prevIdx);
                        // If previous char is identifier char or ), this is binary
                        if (Character.isJavaIdentifierPart(prev) || prev == ')' || prev == ']' || Character.isDigit(prev)) {
                            return true;
                        }
                    }
                    break;
                    
                case '-':
                    // - is arithmetic unless it's unary minus before a number
                    if (next == '-') return true; // --
                    if (next == '=') return true; // -=
                    // Check if this is binary - by looking at previous non-whitespace
                    prevIdx = i - 1;
                    while (prevIdx >= 0 && Character.isWhitespace(expr.charAt(prevIdx))) prevIdx--;
                    if (prevIdx >= 0) {
                        char prev = expr.charAt(prevIdx);
                        // If previous char is identifier char or ), this is binary
                        if (Character.isJavaIdentifierPart(prev) || prev == ')' || prev == ']' || Character.isDigit(prev)) {
                            return true;
                        }
                    }
                    break;
                    
                case '*': case '/': case '%':
                    return true;
                    
                case '&':
                    if (next == '&' || next == '=') return true;
                    // Single & is bitwise AND
                    return true;
                    
                case '|':
                    if (next == '|' || next == '=') return true;
                    // Single | is bitwise OR
                    return true;
                    
                case '^': case '~':
                    return true;
                    
                case '<':
                    // Could be < > <= >= << or generics
                    if (next == '<' || next == '=') return true;
                    // Check if this looks like relational (not generic type params)
                    // Generics typically follow a type name directly with no space
                    int nextNonSpace = i + 1;
                    while (nextNonSpace < expr.length() && Character.isWhitespace(expr.charAt(nextNonSpace))) nextNonSpace++;
                    if (nextNonSpace < expr.length() && !Character.isUpperCase(expr.charAt(nextNonSpace))) {
                        return true;
                    }
                    break;
                    
                case '>':
                    if (next == '>' || next == '=') return true;
                    // Similar check for generics
                    break;
                    
                case '!':
                    if (next == '=') return true; // !=
                    // Standalone ! is logical NOT
                    return true;
                    
                case '=':
                    if (next == '=') return true; // ==
                    // Single = is assignment
                    return true;
                    
                case '?':
                    // Ternary operator - but be careful of generics with ?
                    // If followed by :, it's definitely ternary
                    for (int j = i + 1; j < expr.length(); j++) {
                        if (expr.charAt(j) == ':') return true;
                    }
                    break;
            }
        }
        
        // Check for instanceof keyword
        if (expr.contains(" instanceof ")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Resolve an expression that contains operators or casts using the full expression parser.
     * This handles all Java operators with proper precedence and type promotion rules.
     */
    private TypeInfo resolveExpressionWithParserAPI(String expr, int position) {
        // Create a context that bridges to ScriptDocument's existing resolution methods
        ExpressionNode.TypeResolverContext context = createExpressionResolverContext(position);
        
        // Use the expression resolver to parse and resolve the type
        ExpressionTypeResolver resolver = new ExpressionTypeResolver(context);
        TypeInfo result = resolver.resolve(expr);
        
        // Special case: null literal type is "unresolved" but valid
        if (result != null && "<null>".equals(result.getFullName())) {
            return result;
        }
        
        // If the expression resolver couldn't resolve it, fall back to simple resolution
        if (result == null || !result.isResolved()) {
            // Try the simple path in case the operator detection was a false positive
            return resolveSimpleExpression(expr, position);
        }
        
        return result;
    }
    
    /**
     * Create a type resolver context that connects the expression resolver
     * to ScriptDocument's existing type resolution infrastructure.
     */
    private ExpressionNode.TypeResolverContext createExpressionResolverContext(int position) {
        return new ExpressionNode.TypeResolverContext() {
            @Override
            public TypeInfo resolveIdentifier(String name) {
                // Check for special keywords first
                if ("this".equals(name)) {
                    return findEnclosingScriptType(position);
                }
                if ("true".equals(name) || "false".equals(name)) {
                    return TypeInfo.fromPrimitive("boolean");
                }
                if ("null".equals(name)) {
                    return TypeInfo.unresolved("null", "<null>");
                }
                
                // Try to resolve as a variable
                FieldInfo varInfo = resolveVariable(name, position);
                if (varInfo != null) {
                    return varInfo.getTypeInfo();
                }
                
                // Try as a class name (for static access)
                if (name.length() > 0) {
                    TypeInfo typeCheck = resolveType(name);
                    if (typeCheck != null && typeCheck.isResolved()) {
                        return typeCheck;
                    }
                }
                
                return null;
            }
            
            @Override
            public TypeInfo resolveMemberAccess(TypeInfo targetType, String memberName) {
                if (targetType == null || !targetType.isResolved()) {
                    return null;
                }
                
                if (targetType.hasField(memberName)) {
                    FieldInfo fieldInfo = targetType.getFieldInfo(memberName);
                    return (fieldInfo != null) ? fieldInfo.getTypeInfo() : null;
                }
                
                return null;
            }
            
            @Override
            public TypeInfo resolveMethodCall(TypeInfo targetType, String methodName, TypeInfo[] argTypes) {
                if (targetType == null || !targetType.isResolved()) {
                    // Try as a script-defined method
                    if (isScriptMethod(methodName)) {
                        MethodInfo scriptMethod = getScriptMethodInfo(methodName);
                        if (scriptMethod != null) {
                            return scriptMethod.getReturnType();
                        }
                    }
                    return null;
                }
                
                if (targetType.hasMethod(methodName)) {
                    MethodInfo methodInfo = targetType.getBestMethodOverload(methodName, argTypes);
                    return (methodInfo != null) ? methodInfo.getReturnType() : null;
                }
                
                return null;
            }
            
            @Override
            public TypeInfo resolveArrayAccess(TypeInfo arrayType) {
                if (arrayType == null || !arrayType.isResolved()) {
                    return null;
                }
                
                String typeName = arrayType.getFullName();
                if (typeName.endsWith("[]")) {
                    String elementTypeName = typeName.substring(0, typeName.length() - 2);
                    // Try to resolve the element type properly
                    return resolveType(elementTypeName);
                }
                
                // For List<T> or similar, try to extract the element type
                // This is a simplified version - could be enhanced for full generic support
                return null;
            }
            
            @Override
            public TypeInfo resolveTypeName(String typeName) {
                return resolveType(typeName);
            }
        };
    }
    
    /**
     * Resolve a simple expression without operators.
     * This is the fallback path when operator detection was a false positive.
     */
    private TypeInfo resolveSimpleExpression(String expr, int position) {
        // String literals
        if (expr.startsWith("\"") && expr.endsWith("\"")) {
            return resolveType("String");
        }
        
        // Character literals
        if (expr.startsWith("'") && expr.endsWith("'")) {
            return TypeInfo.fromPrimitive("char");
        }
        
        // Boolean literals
        if (expr.equals("true") || expr.equals("false")) {
            return TypeInfo.fromPrimitive("boolean");
        }
        
        // Null literal
        if (expr.equals("null")) {
            return null;
        }
        
        // Numeric literals
        if (expr.matches("-?\\d*\\.?\\d+[fF]")) {
            if (hasExcessivePrecision(expr, 7)) {
                return TypeInfo.fromPrimitive("double");
            }
            return TypeInfo.fromPrimitive("float");
        }
        if (expr.matches("-?\\d*\\.\\d+[dD]?") || expr.matches("-?\\d+\\.[dD]?") || expr.matches("-?\\d+[dD]")) {
            if (hasExcessivePrecision(expr, 15)) {
                return null;
            }
            return TypeInfo.fromPrimitive("double");
        }
        if (expr.matches("-?\\d+[lL]")) {
            return TypeInfo.fromPrimitive("long");
        }
        if (expr.matches("-?\\d+")) {
            return TypeInfo.fromPrimitive("int");
        }
        
        // "this" keyword
        if (expr.equals("this")) {
            return findEnclosingScriptType(position);
        }
        
        // "new Type()" expressions
        if (expr.startsWith("new ")) {
            Matcher newMatcher = NEW_TYPE_PATTERN.matcher(expr);
            if (newMatcher.find()) {
                return resolveType(newMatcher.group(1));
            }
        }
        
        // Try as variable or field chain
        List<ChainSegment> segments = parseExpressionChain(expr);
        if (segments.isEmpty()) {
            return null;
        }
        
        ChainSegment first = segments.get(0);
        TypeInfo currentType = null;
        
        if (first.name.equals("this")) {
            currentType = findEnclosingScriptType(position);
        } else {
            // Check if first segment is a type name
            TypeInfo typeCheck = resolveType(first.name);
            if (typeCheck != null && typeCheck.isResolved()) {
                currentType = typeCheck;
            }
        }
        
        if (currentType == null && !first.isMethodCall) {
            FieldInfo varInfo = resolveVariable(first.name, position);
            if (varInfo != null) {
                currentType = varInfo.getTypeInfo();
            }
        } else {
            if (isScriptMethod(first.name)) {
                MethodInfo scriptMethod = getScriptMethodInfo(first.name);
                if (scriptMethod != null) {
                    currentType = scriptMethod.getReturnType();
                }
            }
        }
        
        for (int i = 1; i < segments.size(); i++) {
            currentType = resolveChainSegment(currentType, segments.get(i));
            if (currentType == null) {
                return null;
            }
        }
        
        return currentType;
    }

    /**
     * Find the matching opening parenthesis when given a closing paren position.
     */
    private int findMatchingParenBackward(int closeParenPos) {
        if (closeParenPos < 0 || closeParenPos >= text.length() || text.charAt(closeParenPos) != ')') {
            return -1;
        }
        
        int depth = 1;
        boolean inString = false;
        char stringChar = 0;
        
        for (int i = closeParenPos - 1; i >= 0; i--) {
            char c = text.charAt(i);
            char next = (i < text.length() - 1) ? text.charAt(i + 1) : 0;
            
            // Handle string literals (going backward, check for escapes)
            if (!inString && (c == '"' || c == '\'')) {
                // Check if this quote is escaped (look back for backslash)
                int backslashCount = 0;
                for (int j = i - 1; j >= 0 && text.charAt(j) == '\\'; j--) {
                    backslashCount++;
                }
                if (backslashCount % 2 == 0) {
                    inString = true;
                    stringChar = c;
                }
            } else if (inString && c == stringChar) {
                int backslashCount = 0;
                for (int j = i - 1; j >= 0 && text.charAt(j) == '\\'; j--) {
                    backslashCount++;
                }
                if (backslashCount % 2 == 0) {
                    inString = false;
                }
            }
            
            if (inString) continue;
            
            // Skip excluded regions (comments)
            if (isExcluded(i)) continue;
            
            if (c == ')') {
                depth++;
            } else if (c == '(') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        
        return -1; // No matching paren found
    }

    /**
     * Find the matching opening bracket of a given closing bracket position.
     * Supports any pair of open/close chars (e.g., '[' and ']').
     */
    private int findMatchingBracketBackward(int closePos, char openChar, char closeChar) {
        if (closePos < 0 || closePos >= text.length() || text.charAt(closePos) != closeChar) {
            return -1;
        }

        int depth = 1;
        boolean inString = false;
        char stringChar = 0;

        for (int i = closePos - 1; i >= 0; i--) {
            char c = text.charAt(i);

            // Handle string literals (backward)
            if (!inString && (c == '"' || c == '\'')) {
                int backslashCount = 0;
                for (int j = i - 1; j >= 0 && text.charAt(j) == '\\'; j--) backslashCount++;
                if (backslashCount % 2 == 0) {
                    inString = true;
                    stringChar = c;
                }
            } else if (inString && c == stringChar) {
                int backslashCount = 0;
                for (int j = i - 1; j >= 0 && text.charAt(j) == '\\'; j--) backslashCount++;
                if (backslashCount % 2 == 0) {
                    inString = false;
                }
            }

            if (inString) continue;

            if (isExcluded(i)) continue;

            if (c == closeChar) depth++;
            else if (c == openChar) {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    /**
     * Given the position of a dot ('.') in `text`, find the bounds [start, end)
     * of the receiver expression immediately to the left of the dot. The end
     * returned will be the dot index (exclusive). Returns null if not found or malformed.
     */
    private int[] findReceiverBoundsBefore(int dotIndex) {
        if (dotIndex < 0 || dotIndex >= text.length() || text.charAt(dotIndex) != '.') {
            return null;
        }

        int pos = dotIndex - 1;
        // Skip whitespace
        while (pos >= 0 && Character.isWhitespace(text.charAt(pos))) pos--;
        if (pos < 0) return null;

        while (pos >= 0) {
            char c = text.charAt(pos);
            if (Character.isWhitespace(c)) { pos--; continue; }

            if (c == ')') {
                int open = findMatchingParenBackward(pos);
                if (open < 0) return null;
                pos = open - 1;
                continue;
            }

            if (c == ']') {
                int open = findMatchingBracketBackward(pos, '[', ']');
                if (open < 0) return null;
                pos = open - 1;
                continue;
            }

            if (Character.isJavaIdentifierPart(c)) {
                while (pos >= 0 && Character.isJavaIdentifierPart(text.charAt(pos))) pos--;
                // If it's part of a chained identifier (preceded by a dot), continue
                if (pos >= 0 && text.charAt(pos) == '.') { pos--; continue; }
                break;
            }

            // Unexpected char - stop and treat as start after this
            break;
        }

        int start = pos + 1;
        int end = dotIndex; // exclusive
        if (start >= end) return null;
        return new int[]{start, end};
    }

    /**
     * Find the script-defined type that contains the given position.
     * Used for resolving 'this' references.
     */
    private ScriptTypeInfo findEnclosingScriptType(int position) {
        for (ScriptTypeInfo type : scriptTypes.values()) {
            if (type.containsPosition(position)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Check if a method name is defined in this script.
     */
    private boolean isScriptMethod(String methodName) {
        for (MethodInfo method : methods) {
            if (method.getName().equals(methodName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the MethodInfo for a script-defined method by name.
     */
    private MethodInfo getScriptMethodInfo(String methodName) {
        for (MethodInfo method : methods) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }
    
    /**
     * Get the best matching script method overload based on argument types.
     */
    private MethodInfo getScriptMethodInfo(String methodName, TypeInfo[] argTypes) {
        List<MethodInfo> candidates = new ArrayList<>();
        
        // Collect all methods with matching name
        for (MethodInfo method : methods) {
            if (method.getName().equals(methodName)) {
                candidates.add(method);
            }
        }
        
        if (candidates.isEmpty()) {
            return null;
        }
        
        if (candidates.size() == 1) {
            return candidates.get(0);
        }
        
        // Use same 3-phase matching as TypeInfo.getBestMethodOverload
        
        // Phase 1: Look for exact match
        for (MethodInfo method : candidates) {
            List<FieldInfo> params = method.getParameters();
            if (params.size() != argTypes.length) {
                continue;
            }
            
            boolean exactMatch = true;
            for (int i = 0; i < argTypes.length; i++) {
                TypeInfo paramType = params.get(i).getDeclaredType();
                TypeInfo argType = argTypes[i];
                
                if (argType == null || paramType == null || !argType.equals(paramType)) {
                    exactMatch = false;
                    break;
                }
            }
            
            if (exactMatch) {
                return method;
            }
        }
        
        // Phase 2: Look for compatible match (widening/autoboxing)
        for (MethodInfo method : candidates) {
            List<FieldInfo> params = method.getParameters();
            if (params.size() != argTypes.length) {
                continue;
            }
            
            boolean compatible = true;
            for (int i = 0; i < argTypes.length; i++) {
                TypeInfo paramType = params.get(i).getDeclaredType();
                TypeInfo argType = argTypes[i];
                
                if (argType != null && paramType != null && !TypeChecker.isTypeCompatible(paramType, argType)) {
                    compatible = false;
                    break;
                }
            }
            
            if (compatible) {
                return method;
            }
        }
        
        // Phase 3: Fallback to first overload
        return candidates.get(0);
    }
    
    /**
     * Check if a method belongs to a script-defined type (class/interface/enum).
     * Returns true if the method is defined inside a script type.
     */
    private boolean isMethodFromScriptType(MethodInfo method) {
        if (method == null || !method.isDeclaration()) {
            return false;
        }
        
        int declPos = method.getDeclarationOffset();
        if (declPos < 0) {
            return false;
        }
        
        // Check if the method's declaration position is inside any script type's body
        for (ScriptTypeInfo scriptType : scriptTypes.values()) {
            if (scriptType.containsPosition(declPos)) {
                return true;
            }
        }
        
        return false;
    }

    private void markVariables(List<ScriptLine.Mark> marks) {
        Set<String> knownKeywords = new HashSet<>(Arrays.asList(
                "boolean", "int", "float", "double", "long", "char", "byte", "short", "void",
                "null", "true", "false", "if", "else", "switch", "case", "for", "while", "do",
                "try", "catch", "finally", "return", "throw", "var", "let", "const", "function",
                "continue", "break", "this", "new", "typeof", "instanceof", "class", "interface",
                "extends", "implements", "import", "package", "public", "private", "protected",
                "static", "final", "abstract", "synchronized", "native", "default", "enum",
                "throws", "super", "assert", "volatile", "transient"
        ));

        // First pass: mark method parameters in their declaration positions
        for (MethodInfo method : methods) {
            for (FieldInfo param : method.getParameters()) {
                int pos = param.getDeclarationOffset();
                String name = param.getName();
                marks.add(new ScriptLine.Mark(pos, pos + name.length(), TokenType.PARAMETER, param));
            }
        }
        
        // Mark constructor parameters from script types
        for (ScriptTypeInfo scriptType : scriptTypes.values()) {
            for (MethodInfo constructor : scriptType.getConstructors()) {
                for (FieldInfo param : constructor.getParameters()) {
                    int pos = param.getDeclarationOffset();
                    if (pos >= 0) {
                        String name = param.getName();
                        marks.add(new ScriptLine.Mark(pos, pos + name.length(), TokenType.PARAMETER, param));
                    }
                }
            }
        }

        Pattern identifier = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\b");
        Matcher m = identifier.matcher(text);

        while (m.find()) {
            String name = m.group(1);
            int position = m.start(1);

            if (isExcluded(position))
                continue;
            if (knownKeywords.contains(name))
                continue;

            // Skip if preceded by dot (field access - handled separately)
            if (isPrecededByDot(position))
                continue;

            // Skip import/package statements
            if (isInImportOrPackage(position))
                continue;

            // Skip method calls (followed by paren)
            if (isFollowedByParen(m.end(1)))
                continue;

            // Find containing method
            MethodInfo containingMethod = findMethodAtPosition(position);
            // Check if this variable is an argument to a method call
            MethodCallInfo callInfo = findMethodCallContainingPosition(position);

            // For uppercase identifiers, only process if it's a known field
            // Otherwise, let type handling (markImportedClassUsages) handle it
            boolean isUppercase = Character.isUpperCase(name.charAt(0));

            if (containingMethod != null) {
                // Check parameters
                if (containingMethod.hasParameter(name)) {
                    FieldInfo paramInfo = containingMethod.getParameter(name);
                    marks.add(new ScriptLine.Mark(m.start(1), m.end(1), TokenType.PARAMETER, paramInfo));
                    continue;
                }

                // Check local variables
                Map<String, FieldInfo> locals = methodLocals.get(containingMethod.getDeclarationOffset());
                if (locals != null && locals.containsKey(name)) {
                    FieldInfo localInfo = locals.get(name);
                    // Only highlight if the position is after the declaration
                    if (localInfo.isVisibleAt(position)) {
                        Object metadata = callInfo != null ? new FieldInfo.ArgInfo(localInfo, callInfo) : localInfo;
                        marks.add(new ScriptLine.Mark(m.start(1), m.end(1), TokenType.LOCAL_FIELD, metadata));
                        continue;
                    }
                }
            } else {
                // Check global fields
                if (globalFields.containsKey(name)) {
                    FieldInfo fieldInfo = globalFields.get(name);
                    Object metadata = callInfo != null ? new FieldInfo.ArgInfo(fieldInfo, callInfo) : fieldInfo;
                    marks.add(new ScriptLine.Mark(m.start(1), m.end(1), TokenType.GLOBAL_FIELD, metadata));
                    continue;
                }

                // Skip uppercase if not a known field - type handling will deal with it
                if (isUppercase)
                    continue;

                // Unknown variable inside method - mark as undefined
                marks.add(new ScriptLine.Mark(m.start(1), m.end(1), TokenType.UNDEFINED_VAR, callInfo));
            }
        }
    }

    /**
     * Mark chained field accesses like: mc.player.world, array.length, this.field, etc.
     * This handles dot-separated access chains and colors each segment appropriately.
     * Does NOT mark method calls (identifiers followed by parentheses) - those are handled by markMethodCalls.
     */
    private void markChainedFieldAccesses(List<ScriptLine.Mark> marks) {
        // Pattern to find identifier chains: identifier.identifier, this.identifier, etc.
        // Start with an identifier or 'this' followed by at least one dot and another identifier
        Pattern chainPattern = Pattern.compile("\\b(this|[a-zA-Z_][a-zA-Z0-9_]*)\\s*\\.\\s*([a-zA-Z_][a-zA-Z0-9_]*)");
        Matcher m = chainPattern.matcher(text);
 
        while (m.find()) {
            int chainStart = m.start(1);

            if (isExcluded(chainStart))
                continue;

            // Skip import/package statements
            if (isInImportOrPackage(chainStart))
                continue;

            // Check if the SECOND segment is a method call (followed by parentheses)
            // If so, we skip this match entirely - markMethodCalls will handle it
            int afterSecond = m.end(2);
            int checkPos = afterSecond;
            while (checkPos < text.length() && Character.isWhitespace(text.charAt(checkPos)))
                checkPos++;
            if (checkPos < text.length() && text.charAt(checkPos) == '(') {
                // This is "something.methodCall()" - skip, handled by markMethodCalls
                continue;
            }

            // Build the full chain by continuing to match subsequent .identifier patterns
            List<String> chainSegments = new ArrayList<>();
            List<int[]> segmentPositions = new ArrayList<>(); // [start, end] for each segment

            String firstSegment = m.group(1);
            chainSegments.add(firstSegment);
            segmentPositions.add(new int[]{m.start(1), m.end(1)});

            String secondSegment = m.group(2);
            chainSegments.add(secondSegment);
            segmentPositions.add(new int[]{m.start(2), m.end(2)});

            // Continue reading more segments
            int pos = m.end(2);
            while (pos < text.length()) {
                // Skip whitespace
                while (pos < text.length() && Character.isWhitespace(text.charAt(pos)))
                    pos++;

                if (pos >= text.length() || text.charAt(pos) != '.')
                    break;
                pos++; // Skip the dot

                // Skip whitespace after dot
                while (pos < text.length() && Character.isWhitespace(text.charAt(pos)))
                    pos++;

                if (pos >= text.length() || !Character.isJavaIdentifierStart(text.charAt(pos)))
                    break;

                // Read next identifier
                int identStart = pos;
                while (pos < text.length() && Character.isJavaIdentifierPart(text.charAt(pos)))
                    pos++;
                int identEnd = pos;

                // Check if this is followed by parentheses (method call - stop)
                int checkPosInner = pos;
                while (checkPosInner < text.length() && Character.isWhitespace(text.charAt(checkPosInner)))
                    checkPosInner++;
                if (checkPosInner < text.length() && text.charAt(checkPosInner) == '(') {
                    break; // This is a method call, not a field
                }

                chainSegments.add(text.substring(identStart, identEnd));
                segmentPositions.add(new int[]{identStart, identEnd});
            }

            // Now resolve the chain type by type
            // Start with resolving the first segment
            TypeInfo currentType = null;
            boolean firstIsThis = firstSegment.equals("this");
            boolean firstIsPrecededByDot = isPrecededByDot(chainStart);
            
            // Determine the starting index for marking
            // If the first segment is preceded by a dot, it's a field access (not a variable), so we should mark it
            int startMarkingIndex = firstIsPrecededByDot ? 0 : 1;

            if (firstIsThis) {
                // For 'this', we don't have a class context to resolve, but we can mark subsequent fields
                // as global fields if they exist in globalFields
                currentType = null; // We'll use globalFields for the next segment
            } else {
                // Try to resolve as a type first (class/interface/enum name)
                TypeInfo typeCheck = resolveType(firstSegment);
                if (typeCheck != null && typeCheck.isResolved()) {
                    // Static field access like SomeClass.field or scriptType.field
                    currentType = typeCheck;
                } else if (firstIsPrecededByDot) {
                    // This is a field access on a previous result (e.g., getMinecraft().thePlayer)
                    // Resolve the receiver chain to get the type of what comes before the dot
                    TypeInfo receiverType = resolveReceiverChain(chainStart);
                    if (receiverType != null && receiverType.hasField(firstSegment)) {
                        FieldInfo varInfo = receiverType.getFieldInfo(firstSegment);
                        currentType = (varInfo != null) ? varInfo.getTypeInfo() : null;
                    } else {
                        currentType = null; // Can't resolve
                    }
                } else {
                    // This is a variable starting the chain
                    FieldInfo varInfo = resolveVariable(firstSegment, chainStart);
                    if (varInfo != null) {
                        currentType = varInfo.getTypeInfo();
                    } else {
                        currentType = null;
                    }
                }
            }

            // Mark the segments - start from index determined above
            for (int i = startMarkingIndex; i < chainSegments.size(); i++) {
                String segment = chainSegments.get(i);
                int[] segPos = segmentPositions.get(i);

                if (isExcluded(segPos[0]))
                    continue;

                boolean isLastSegment = (i == chainSegments.size() - 1);
                boolean isStaticAccess = false;
                if (i - 1 >= 0) {
                    String prev = chainSegments.get(i - 1);
                    // Check if previous segment is a class/type name (indicating static access)
                    TypeInfo prevType = resolveType(prev);
                    if (prevType != null && prevType.isResolved()) {
                        isStaticAccess = true;
                    }
                }
                
                // Handle the first segment if we're marking it (i.e., when preceded by dot)
                if (i == 0 && firstIsPrecededByDot) {
                    // This is a field access on a receiver (e.g., the "thePlayer" in "getMinecraft().thePlayer")
                    TypeInfo receiverType = resolveReceiverChain(chainStart);
                    if (receiverType != null && receiverType.hasField(segment)) {
                        FieldInfo fieldInfo = receiverType.getFieldInfo(segment);
                        FieldAccessInfo accessInfo = createFieldAccessInfo(segment, segPos[0], segPos[1], receiverType, fieldInfo, isLastSegment, isStaticAccess);

                        marks.add(new ScriptLine.Mark(segPos[0], segPos[1], TokenType.GLOBAL_FIELD, accessInfo));
                        currentType = (fieldInfo != null) ? fieldInfo.getTypeInfo() : null;
                    } else {
                        marks.add(new ScriptLine.Mark(segPos[0], segPos[1], TokenType.UNDEFINED_VAR));
                        currentType = null;
                    }
                } else if (i == 1 && firstIsThis) {
                    // For "this.field", check if field exists in globalFields
                    if (globalFields.containsKey(segment)) {
                        FieldInfo fieldInfo = globalFields.get(segment);
                        marks.add(new ScriptLine.Mark(segPos[0], segPos[1], TokenType.GLOBAL_FIELD, fieldInfo));
                        currentType = fieldInfo.getTypeInfo();
                    } else {
                        marks.add(new ScriptLine.Mark(segPos[0], segPos[1], TokenType.UNDEFINED_VAR));
                        currentType = null;
                    }
                } else if (currentType != null && currentType.isResolved()) {
                    // Check if this type has this field
                    if (currentType.hasField(segment)) {
                        FieldInfo fieldInfo = currentType.getFieldInfo(segment);

                        // If accessing from a class name (isStaticAccessSeg) and field is not static, that's an error
                        if (isStaticAccess && fieldInfo != null && !fieldInfo.isStatic()) {
                            // Mark as error - trying to access non-static field from static context
                            TokenErrorMessage errorMsg = TokenErrorMessage
                                    .from("Cannot access non-static field '" + segment + "' from static context '" + currentType.getSimpleName() + "'")
                                    .clearOtherErrors();
                            marks.add(new ScriptLine.Mark(segPos[0], segPos[1], TokenType.UNDEFINED_VAR, errorMsg));
                            currentType = null; // Can't continue resolving
                        } else {
                            // Valid access
                            FieldAccessInfo accessInfo = createFieldAccessInfo(segment, segPos[0], segPos[1],
                                    currentType, fieldInfo, isLastSegment, isStaticAccess);
                            marks.add(new ScriptLine.Mark(segPos[0], segPos[1], TokenType.GLOBAL_FIELD, accessInfo));

                            // Update currentType for next segment
                            currentType = (fieldInfo != null && fieldInfo.getTypeInfo() != null) ? fieldInfo.getTypeInfo() : null;
                        }
                    } else {
                        marks.add(new ScriptLine.Mark(segPos[0], segPos[1], TokenType.UNDEFINED_VAR));
                        currentType = null; // Can't continue resolving
                    }
                } else {
                    // Can't resolve - mark as undefined
                    marks.add(new ScriptLine.Mark(segPos[0], segPos[1], TokenType.UNDEFINED_VAR));
                    currentType = null;
                }
            }
        }

        // Second pass: handle cases where the receiver is a method call or array access
        // Example: getMinecraft().thePlayer -> initial chainPattern won't match because
        // the left side isn't a simple identifier. Find ".identifier" occurrences where
        // the char before the dot ends with ')' or ']' and resolve the receiver expression.
        Pattern dotIdent = Pattern.compile("\\.\\s*([a-zA-Z_][a-zA-Z0-9_]*)");
        Matcher md = dotIdent.matcher(text);
        while (md.find()) {
            int identStart = md.start(1);
            int identEnd = md.end(1);
            int dotPos = md.start();

            // Skip if excluded or in import/package
            if (isExcluded(identStart) || isInImportOrPackage(identStart))
                continue;

            // Find non-whitespace char before the dot
            int before = dotPos - 1;
            while (before >= 0 && Character.isWhitespace(text.charAt(before)))
                before--;
            if (before < 0)
                continue;

            char bc = text.charAt(before);
            // Only handle when left side ends with ')' or ']' (method call or array access)
            if (bc != ')' && bc != ']')
                continue;

            // Extract receiver bounds and resolve its type
            int[] bounds = findReceiverBoundsBefore(dotPos);
            if (bounds == null)
                continue;
            int recvStart = bounds[0];
            int recvEnd = bounds[1];

            if (isExcluded(recvStart) || isInImportOrPackage(recvStart))
                continue;

            String receiverExpr = text.substring(recvStart, recvEnd).trim();
            if (receiverExpr.isEmpty())
                continue;

            TypeInfo receiverType = resolveExpressionType(receiverExpr, recvStart);

            // Now mark the first field after the dot and continue chaining
            String firstField = md.group(1);
            FieldInfo fInfo = null;
            if (receiverType != null && receiverType.hasField(firstField)) {
                fInfo = receiverType.getFieldInfo(firstField);
                
                // Check if there are more segments after this one
                boolean hasMoreSegments = false;
                int checkPos = identEnd;
                while (checkPos < text.length() && Character.isWhitespace(text.charAt(checkPos)))
                    checkPos++;
                if (checkPos < text.length() && text.charAt(checkPos) == '.') {
                    hasMoreSegments = true;
                }
                
                boolean isStaticAccessFirst = false;
                if (receiverType != null) {
                    String recvName = receiverType.getSimpleName();
                    if (recvName != null && !recvName.isEmpty() && Character.isUpperCase(recvName.charAt(0)))
                        isStaticAccessFirst = true;
                }
                FieldAccessInfo accessInfoFirst = createFieldAccessInfo(firstField, identStart, identEnd, receiverType, fInfo, !hasMoreSegments, isStaticAccessFirst);
                marks.add(new ScriptLine.Mark(identStart, identEnd, TokenType.GLOBAL_FIELD, accessInfoFirst));
            } else {
                marks.add(new ScriptLine.Mark(identStart, identEnd, TokenType.UNDEFINED_VAR));
            }

            // Attempt to continue the chain after this identifier
            int pos = identEnd;
            TypeInfo currentType = (fInfo != null) ? fInfo.getTypeInfo() : null;
            while (pos < text.length()) {
                // Skip whitespace
                while (pos < text.length() && Character.isWhitespace(text.charAt(pos)))
                    pos++;
                if (pos >= text.length() || text.charAt(pos) != '.')
                    break;
                pos++; // skip dot
                // Skip whitespace
                while (pos < text.length() && Character.isWhitespace(text.charAt(pos)))
                    pos++;
                if (pos >= text.length() || !Character.isJavaIdentifierStart(text.charAt(pos)))
                    break;

                int nStart = pos;
                while (pos < text.length() && Character.isJavaIdentifierPart(text.charAt(pos)))
                    pos++;
                int nEnd = pos;
                String seg = text.substring(nStart, nEnd);

                // Check if next segment is a method call (followed by '(') -> stop
                int checkPosInner = nEnd;
                while (checkPosInner < text.length() && Character.isWhitespace(text.charAt(checkPosInner)))
                    checkPosInner++;
                if (checkPosInner < text.length() && text.charAt(checkPosInner) == '(')
                    break;

                if (isExcluded(nStart))
                    break;

                // Check if this is the last segment (no more dots after)
                boolean isLastSegment = true;
                int checkNext = nEnd;
                while (checkNext < text.length() && Character.isWhitespace(text.charAt(checkNext)))
                    checkNext++;
                if (checkNext < text.length() && text.charAt(checkNext) == '.') {
                    isLastSegment = false;
                }

                if (currentType != null && currentType.isResolved() && currentType.hasField(seg)) {
                    FieldInfo segInfo = currentType.getFieldInfo(seg);
                    
                    boolean isStaticAccessSeg2 = false;
                    if (currentType != null) {
                        String tn = currentType.getSimpleName();
                        if (tn != null && !tn.isEmpty() && Character.isUpperCase(tn.charAt(0)))
                            isStaticAccessSeg2 = true;
                    }
                    FieldAccessInfo accessInfoSeg = createFieldAccessInfo(seg, nStart, nEnd, currentType, segInfo, isLastSegment, isStaticAccessSeg2);
                    marks.add(new ScriptLine.Mark(nStart, nEnd, TokenType.GLOBAL_FIELD, accessInfoSeg));
                    
                    currentType = (segInfo != null) ? segInfo.getTypeInfo() : null;
                } else {
                    marks.add(new ScriptLine.Mark(nStart, nEnd, TokenType.UNDEFINED_VAR));
                    currentType = null;
                }
            }
        }

    }
    
    /**
     * Parse assignment statements (reassignments, not declarations) and validate type compatibility.
     * This detects patterns like: varName = expr; or obj.field = expr;
     * Assignments are stored in the corresponding FieldInfo, not as marks.
     */
    private void parseAssignments() {
        // First clear any existing assignments in all FieldInfo objects
        for (FieldInfo field : globalFields.values()) {
            field.clearAssignments();
        }
        for (Map<String, FieldInfo> locals : methodLocals.values()) {
            for (FieldInfo field : locals.values()) {
                field.clearAssignments();
            }
        }
        externalFieldAssignments.clear();
        
        // Pattern to find assignments: identifier = expression;
        // But NOT declarations (Type varName = expr) or compound assignments (+=, -=, etc.)
        // We need to find '=' that is:
        // 1. Not preceded by another operator (!, <, >, =)
        // 2. Not followed by '='
        // 3. The LHS must be a valid l-value (variable or field access)
        
        int pos = 0;
        while (pos < text.length()) {
            // Find the next '=' character
            int equalsPos = text.indexOf('=', pos);
            if (equalsPos < 0) break;
            
            // Skip if in excluded region (string/comment)
            if (isExcluded(equalsPos)) {
                pos = equalsPos + 1;
                continue;
            }
            
            // Check it's not part of ==, !=, <=, >=, +=, -=, *=, /=, %=, &=, |=, ^=
            if (equalsPos > 0 && "!<>=+-*/%&|^".indexOf(text.charAt(equalsPos - 1)) >= 0) {
                pos = equalsPos + 1;
                continue;
            }
            if (equalsPos < text.length() - 1 && text.charAt(equalsPos + 1) == '=') {
                pos = equalsPos + 2;
                continue;
            }
            
            // Find the start of this statement (previous ; or { or } or start of text)
            int stmtStart = equalsPos - 1;
            while (stmtStart >= 0) {
                char c = text.charAt(stmtStart);
                if (c == ';' || c == '{' || c == '}') {
                    stmtStart++;
                    break;
                }
                stmtStart--;
            }
            if (stmtStart < 0) stmtStart = 0;
            
            // Skip leading whitespace to get to the actual first character of the statement
            while (stmtStart < equalsPos && Character.isWhitespace(text.charAt(stmtStart))) {
                stmtStart++;
            }
            
            // Find the end of this statement (next ;)
            int stmtEnd = text.indexOf(';', equalsPos);
            if (stmtEnd < 0) stmtEnd = text.length();
            
            // Extract LHS (before =) and RHS (after =)
            String lhsRaw = text.substring(stmtStart, equalsPos).trim();
            String rhsRaw = text.substring(equalsPos + 1, stmtEnd).trim();
            
            // Skip empty assignments
            if (lhsRaw.isEmpty() || rhsRaw.isEmpty()) {
                pos = equalsPos + 1;
                continue;
            }
            
            // Check if this is a declaration (has type before variable name)
            // Declarations have: Type varName = expr
            // Reassignments have: varName = expr or obj.field = expr
            boolean isDeclaration = isVariableDeclaration(lhsRaw);
            
            if (isDeclaration) {
                // This is a declaration with initializer - validate the initial value
                createAndAttachDeclarationAssignment(lhsRaw, rhsRaw, stmtStart, equalsPos, stmtEnd);
            } else {
                // This is a reassignment - create AssignmentInfo and attach to FieldInfo
                createAndAttachAssignment(lhsRaw, rhsRaw, stmtStart, equalsPos, stmtEnd);
            }
            
            pos = stmtEnd + 1;
        }
    }
    
    /**
     * Check if the LHS represents a variable declaration (has a type before the variable name).
     */
    private boolean isVariableDeclaration(String lhs) {
        // Split by whitespace
        String[] parts = lhs.trim().split("\\s+");
        
        // Single word = reassignment (e.g., "x")
        if (parts.length == 1) {
            // Could also be a.b = expr (field access)
            return false;
        }
        
        // Check if it matches pattern: [modifiers] Type varName
        // Last part is the variable name, second-to-last should be a type
        String potentialType = parts[parts.length - 2];
        String potentialVar = parts[parts.length - 1];
        
        // Type patterns: primitives, or capitalized class names, or generic types
        if (TypeResolver.isPrimitiveType(potentialType) || 
            (Character.isUpperCase(potentialType.charAt(0)) && potentialType.matches("[A-Za-z_][A-Za-z0-9_<>\\[\\],\\s]*")) ||
            potentialType.equals("var") || potentialType.equals("let") || potentialType.equals("const")) {
            
            // Make sure the variable name is a valid identifier (not a field access chain)
            if (potentialVar.matches("[a-zA-Z_][a-zA-Z0-9_]*") && !potentialVar.contains(".")) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Create an AssignmentInfo for a reassignment statement and attach it to the appropriate FieldInfo.
     */
    private void createAndAttachAssignment(String lhs, String rhs, int stmtStart, int equalsPos, int stmtEnd) {
        lhs = lhs.trim();
        rhs = rhs.trim();
        
        // Parse the target (LHS)
        // Could be: varName or obj.field or this.field or array[index]
        String targetName = lhs;
        FieldInfo targetField = null;
        TypeInfo targetType = null;
        TypeInfo receiverType = null;
        java.lang.reflect.Field reflectionField = null;
        
        // Find the position where the LHS starts in the actual text (skip leading whitespace)
        int lhsStart = stmtStart;
        while (lhsStart < equalsPos && Character.isWhitespace(text.charAt(lhsStart))) {
            lhsStart++;
        }
        
        // Calculate the actual LHS end (before '=' minus trailing whitespace)
        int lhsEnd = equalsPos;
        while (lhsEnd > lhsStart && Character.isWhitespace(text.charAt(lhsEnd - 1))) {
            lhsEnd--;
        }
        
        // Calculate RHS start (first non-whitespace after '=')
        int rhsStart = equalsPos + 1;
        while (rhsStart < stmtEnd && Character.isWhitespace(text.charAt(rhsStart))) {
            rhsStart++;
        }
        
        // RHS end is at the semicolon position (inclusive for error underline)
        int rhsEnd = stmtEnd;
        
        // Handle chained field access (obj.field)
        if (lhs.contains(".")) {
            // Split into segments
            String[] segments = lhs.split("\\.");
            targetName = segments[segments.length - 1].trim();
            
            // Resolve the chain to get the target type
            String receiverExpr = lhs.substring(0, lhs.lastIndexOf('.')).trim();
            receiverType = resolveExpressionType(receiverExpr, lhsStart);
            
            if (receiverType != null && receiverType.isResolved()) {
                // Get the field from the receiver type
                if (receiverType.hasField(targetName)) {
                    targetField = receiverType.getFieldInfo(targetName);
                    targetType = targetField.getTypeInfo();
                    reflectionField = targetField.getReflectionField();
                }
            }
          //  Minecraft.getMinecraft().thePlayer.PERSISTED_NBT_TAG = null;

        } else {
            // Simple variable
            targetField = resolveVariable(targetName, lhsStart);
            if (targetField != null) {
                targetType = targetField.getDeclaredType();
                reflectionField = targetField.getReflectionField();
            }
        }

        // Resolve the source type (RHS)
        TypeInfo sourceType = resolveExpressionType(rhs, equalsPos + 1);
        
        // Determine if this is a script-defined field or external field
        FieldInfo finalTargetField = targetField;
        boolean isScriptField = targetField != null && 
            (globalFields.containsValue(targetField) || 
             methodLocals.values().stream().anyMatch(m -> m.containsValue(finalTargetField)));
        
        // Determine if the target field is final
        // For script fields, use the modifiers; for external fields, use reflection
        boolean isFinal = false;
        if (targetField != null) {
            isFinal = targetField.isFinal();
        }
        
        // Create the assignment info using the new constructor
        AssignmentInfo info = new AssignmentInfo(
            targetName,
            stmtStart,
            lhsStart,
            lhsEnd,
            targetType,
            rhsStart,
            rhsEnd,
            sourceType,
            rhs,
            receiverType,
            reflectionField,
            isFinal
        );
        
        // Validate the assignment
        info.validate();
        
        // Attach to the appropriate location
        if (isScriptField && targetField != null) {
            // Script-defined field - attach to FieldInfo
            targetField.addAssignment(info);
        } else {
            // External field or unresolved - store separately
            externalFieldAssignments.add(info);
        }
    }
    
    /**
     * Create an AssignmentInfo for a declaration with initializer and attach it to the appropriate FieldInfo.
     * Example: String str = 20; or final int x = "test";
     */
    private void createAndAttachDeclarationAssignment(String lhs, String rhs, int stmtStart, int equalsPos, int stmtEnd) {
        lhs = lhs.trim();
        rhs = rhs.trim();
        
        // Parse the declaration: [modifiers] Type varName
        String[] parts = lhs.split("\\s+");
        if (parts.length < 2) {
            return; // Invalid declaration format
        }
        
        // Last part is the variable name
        String varName = parts[parts.length - 1].trim();
        
        // Find the actual position of the variable name in the text
        int varNameStart = stmtStart;
        int searchStart = stmtStart;
        while (searchStart < equalsPos) {
            int found = text.indexOf(varName, searchStart);
            if (found >= 0 && found < equalsPos) {
                // Verify it's the actual variable name (not part of type name)
                // Check that it's either at start or preceded by whitespace
                if (found == stmtStart || Character.isWhitespace(text.charAt(found - 1))) {
                    // Check that it's followed by whitespace or '='
                    int afterVar = found + varName.length();
                    if (afterVar >= text.length() || Character.isWhitespace(text.charAt(afterVar)) || text.charAt(afterVar) == '=') {
                        varNameStart = found;
                        break;
                    }
                }
                searchStart = found + 1;
            } else {
                break;
            }
        }
        
        int varNameEnd = varNameStart + varName.length();
        
        // Calculate RHS positions
        int rhsStart = equalsPos + 1;
        while (rhsStart < stmtEnd && Character.isWhitespace(text.charAt(rhsStart))) {
            rhsStart++;
        }
        int rhsEnd = stmtEnd;
        
        // Resolve the target field (should already exist from parseGlobalFields or parseLocalVariables)
        FieldInfo targetField = resolveVariable(varName, varNameStart);
        if (targetField == null || targetField.getDeclarationAssignment() != null) {
            return; // Field doesn't exist, can't attach assignment
        }
        
        TypeInfo targetType = targetField.getDeclaredType();
        TypeInfo sourceType = resolveExpressionType(rhs, rhsStart);
        
        // Create the assignment info
        // Declaration assignments should NOT check final status - this is the one place where final fields can be assigned
        AssignmentInfo info = new AssignmentInfo(
            varName,
            stmtStart,
            varNameStart,
            varNameEnd,
            targetType,
            rhsStart,
            rhsEnd,
            sourceType,
            rhs,
            null, // No receiver for simple declarations
            targetField.getReflectionField(),
            false // Don't flag as final for declaration assignments - initial assignment is always allowed
        );
        
        // Validate the assignment
        info.validate();
        
        // Attach as the declaration assignment
        targetField.setDeclarationAssignment(info);
    }
    
    /**
     * Mark types in cast expressions with their appropriate type color.
     * Handles: (Type), (Type[]), (pkg.Type), etc.
     */
    private void markCastTypes(List<ScriptLine.Mark> marks) {
        Matcher m = CAST_TYPE_PATTERN.matcher(text);
        
        while (m.find()) {
            String typeName = m.group(1);
            int typeStart = m.start(1);
            int typeEnd = m.end(1);
            
            // Skip if inside string or comment
            if (isExcluded(typeStart)) continue;
            
            // Skip in import/package statements
            if (isInImportOrPackage(typeStart)) continue;
            
            // Resolve the type
            TypeInfo info = resolveType(typeName);
            if (info != null && info.isResolved()) {
                // Mark the type with its appropriate color
                marks.add(new ScriptLine.Mark(typeStart, typeEnd, info.getTokenType(), info));
            } else {
                // Unknown type - check if it's a primitive
                if (isPrimitiveType(typeName)) {
                    marks.add(new ScriptLine.Mark(typeStart, typeEnd, TokenType.KEYWORD));
                } else {
                    // Mark as undefined type
                    marks.add(new ScriptLine.Mark(typeStart, typeEnd, TokenType.UNDEFINED_VAR));
                }
            }
        }
    }
    
    /**
     * Check if a type name is a primitive type.
     */
    private boolean isPrimitiveType(String typeName) {
        switch (typeName) {
            case "byte": case "short": case "int": case "long":
            case "float": case "double": case "char": case "boolean":
            case "void":
                return true;
            default:
                return false;
        }
    }

    private void markImportedClassUsages(List<ScriptLine.Mark> marks) {
        // Find uppercase identifiers followed by dot (static method calls, field access)
        Pattern classUsage = Pattern.compile("\\b([A-Za-z][a-zA-Z0-9_]*)\\s*\\.");
        Matcher m = classUsage.matcher(text);

        while (m.find()) {
            String className = m.group(1);
            int start = m.start(1);
            int end = m.end(1);

            if (isExcluded(start))
                continue;

            // Skip in import/package statements
            if (isInImportOrPackage(start))
                continue;

            // Try to resolve the class
            TypeInfo info = resolveType(className);
            if (info != null && info.isResolved()) {
                marks.add(new ScriptLine.Mark(start, end, info.getTokenType(), info));
            } else {
                // Unknown type - mark as undefined
                marks.add(new ScriptLine.Mark(start, end, TokenType.UNDEFINED_VAR));
            }
        }

        // Also mark uppercase identifiers in type positions (new X(), X variable, etc.)
        Pattern typeUsage = Pattern.compile("\\b(new\\s+)?([A-Za-z][a-zA-Z0-9_]*)(?:\\s*<[^>]*>)?\\s*(?:\\(|\\[|\\b[a-z])");
        Matcher tm = typeUsage.matcher(text);

        while (tm.find()) {
            String className = tm.group(2);
            String newKeyword = tm.group(1);
            int start = tm.start(2);
            int end = tm.end(2);

            if (isExcluded(start))
                continue;

            // Skip in import/package statements
            if (isInImportOrPackage(start))
                continue;

            // Try to resolve the class
            TypeInfo info = resolveType(className);
            if (info != null && info.isResolved()) {
                boolean isNewCreation = newKeyword != null && newKeyword.trim().equals("new");
                boolean isConstructorDecl = info instanceof ScriptTypeInfo && className.equals(info.getSimpleName());
                
                // Check if this is a "new" expression or constructor declaration
                if (isNewCreation || isConstructorDecl) {
                    // Find opening paren after the class name
                    int searchPos = end;
                    while (searchPos < text.length() && Character.isWhitespace(text.charAt(searchPos)))
                        searchPos++;
                    
                    if (searchPos < text.length() && text.charAt(searchPos) == '(') {
                        int openParen = searchPos;
                        int closeParen = findMatchingParen(openParen);
                        
                        if (closeParen >= 0) {
                            // For constructor declarations, verify it's followed by opening brace
                            if (isConstructorDecl && !isNewCreation) {
                                int braceSearch = closeParen + 1;
                                while (braceSearch < text.length() && Character.isWhitespace(text.charAt(braceSearch)))
                                    braceSearch++;
                                
                                if (braceSearch >= text.length() || text.charAt(braceSearch) != '{') {
                                    // Not a constructor declaration, treat as normal type usage
                                    marks.add(new ScriptLine.Mark(start, end, info.getTokenType(), info));
                                    continue;
                                }
                            }
                            
                            // Parse arguments to find matching constructor
                            List<MethodCallInfo.Argument> arguments = parseMethodArguments(openParen + 1, closeParen);
                            int argCount = arguments.size();
                            
                            // Try to find matching constructor (may be null if not found)
                            MethodInfo constructor = info.hasConstructors() ? info.findConstructor(argCount) : null;
                            
                            // Create MethodCallInfo for constructor (even if null, so errors are tracked)
                            MethodCallInfo ctorCall = MethodCallInfo.constructor(
                                info, constructor, start, end, openParen, closeParen, arguments
                            );
                            ctorCall.validate();
                            methodCalls.add(ctorCall);  // Add to methodCalls list for error tracking
                            marks.add(new ScriptLine.Mark(start, end, info.getTokenType(), ctorCall));
                            continue;
                        }
                    }
                }
                
                marks.add(new ScriptLine.Mark(start, end, info.getTokenType(), info));
            } else {
                // Unknown type - mark as undefined
                marks.add(new ScriptLine.Mark(start, end, TokenType.UNDEFINED_VAR));
            }
        }
    }

    // ==================== PHASE 5: CONFLICT RESOLUTION ====================

    private List<ScriptLine.Mark> resolveConflicts(List<ScriptLine.Mark> marks) {
        // Sort by start position, then by descending priority
        marks.sort((a, b) -> {
            if (a.start != b.start)
                return Integer.compare(a.start, b.start);
            return Integer.compare(b.type.getPriority(), a.type.getPriority());
        });

        List<ScriptLine.Mark> result = new ArrayList<>();
        for (ScriptLine.Mark m : marks) {
            boolean skip = false;

            // Remove lower-priority overlapping marks
            for (int i = result.size() - 1; i >= 0; i--) {
                ScriptLine.Mark r = result.get(i);
                if (m.start < r.end && m.end > r.start) {
                    if (r.type.getPriority() < m.type.getPriority()) {
                        result.remove(i);
                    } else {
                        skip = true;
                        break;
                    }
                }
            }

            if (!skip) {
                result.add(m);
            }
        }

        result.sort(Comparator.comparingInt(m -> m.start));
        return result;
    }

    // ==================== PHASE 7: INDENT GUIDES ====================

    private void computeIndentGuides(List<ScriptLine.Mark> marks) {
        for (ScriptLine line : lines) {
            line.clearIndentGuides();
        }

        // Build ignored ranges from STRING and COMMENT marks
        Set<int[]> ignored = new HashSet<>();
        for (ScriptLine.Mark m : marks) {
            if (m.type == TokenType.STRING || m.type == TokenType.COMMENT) {
                ignored.add(new int[]{m.start, m.end});
            }
        }

        class OpenBrace {
            int lineIdx, col;

            OpenBrace(int l, int c) {
                lineIdx = l;
                col = c;
            }
        }
        Deque<OpenBrace> stack = new ArrayDeque<>();
        final int tabSize = 4;

        for (int li = 0; li < lines.size(); li++) {
            ScriptLine line = lines.get(li);
            String s = line.getText();

            for (int i = 0; i < s.length(); i++) {
                int absPos = line.getGlobalStart() + i;

                // Check if in ignored range
                boolean isIgnored = false;
                for (int[] range : ignored) {
                    if (absPos >= range[0] && absPos < range[1]) {
                        isIgnored = true;
                        break;
                    }
                }
                if (isIgnored)
                    continue;

                char c = s.charAt(i);
                if (c == '{') {
                    int leading = 0;
                    for (int k = 0; k < i; k++) {
                        char ch = s.charAt(k);
                        leading += (ch == '\t') ? tabSize : 1;
                    }
                    stack.push(new OpenBrace(li, leading));
                } else if (c == '}') {
                    if (!stack.isEmpty()) {
                        OpenBrace open = stack.pop();
                        if (open.lineIdx == li)
                            continue;

                        int from = Math.max(0, open.lineIdx + 1);
                        int to = Math.min(lines.size() - 1, li);
                        for (int l = from; l <= to; l++) {
                            lines.get(l).addIndentGuide(open.col);
                        }
                    }
                }
            }
        }
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Helper to create and validate a FieldAccessInfo. Does NOT add marks or register the info —
     * callers should add to `fieldAccesses` and `marks` as appropriate so marking logic remains in-place.
     */
    private FieldAccessInfo createFieldAccessInfo(String name, int start, int end,
                                                  TypeInfo receiverType, FieldInfo fieldInfo,
                                                  boolean isLastSegment, boolean isStaticAccess) {
        FieldAccessInfo accessInfo = new FieldAccessInfo(name, start, end, receiverType, fieldInfo, isStaticAccess);
        if (isLastSegment) {
            TypeInfo expectedType = findExpectedTypeAtPosition(start);
            if (expectedType != null) {
                accessInfo.setExpectedType(expectedType);
            }
        }
        accessInfo.validate();
        fieldAccesses.add(accessInfo);
        return accessInfo;
    }

    private FieldInfo resolveVariable(String name, int position) {
        // Find containing method
        MethodInfo containingMethod = findMethodAtPosition(position);
        
        if (containingMethod != null) {
            // Check parameters
            if (containingMethod.hasParameter(name)) {
                return containingMethod.getParameter(name);
            }
            
            // Check local variables
            Map<String, FieldInfo> locals = methodLocals.get(containingMethod.getDeclarationOffset());
            if (locals != null && locals.containsKey(name)) {
                FieldInfo localInfo = locals.get(name);
                if (localInfo.isVisibleAt(position)) {
                    return localInfo;
                }
            }
        }
        
        // Check global fields
        if (globalFields.containsKey(name)) {
            return globalFields.get(name);
        }
        
        return null;
    }

    private boolean isPrecededByDot(int position) {
        if (position <= 0)
            return false;
        int i = position - 1;
        while (i >= 0 && Character.isWhitespace(text.charAt(i)))
            i--;
        return i >= 0 && text.charAt(i) == '.';
    }

    private boolean isFollowedByParen(int position) {
        int i = position;
        while (i < text.length() && Character.isWhitespace(text.charAt(i)))
            i++;
        return i < text.length() && text.charAt(i) == '(';
    }

    private boolean isFollowedByDot(int position) {
        // Start checking after the given position (e.g. after a closing paren)
        int i = position + 1;
        while (i < text.length() && Character.isWhitespace(text.charAt(i)))
            i++;
        return i < text.length() && text.charAt(i) == '.';
    }

    private boolean isInImportOrPackage(int position) {
        if (position < 0 || position >= text.length())
            return false;

        int lineStart = text.lastIndexOf('\n', position);
        lineStart = lineStart < 0 ? 0 : lineStart + 1;

        int i = lineStart;
        while (i < text.length() && Character.isWhitespace(text.charAt(i)))
            i++;

        if (text.startsWith("import", i) || text.startsWith("package", i)) {
            return true;
        }
        return false;
    }

    private MethodInfo findMethodAtPosition(int position) {
        for (MethodInfo method : methods) {
            if (method.containsPosition(position)) {
                return method;
            }
        }
        return null;
    }

    private int findMatchingBrace(int openBraceIndex) {
        if (openBraceIndex < 0 || openBraceIndex >= text.length())
            return -1;

        int depth = 0;
        for (int i = openBraceIndex; i < text.length(); i++) {
            if (isExcluded(i))
                continue;

            char c = text.charAt(i);
            if (c == '{')
                depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0)
                    return i;
            }
        }
        return -1;
    }

    // ==================== ACCESSORS ====================

    public List<ScriptLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    public List<ImportData> getImports() {
        return Collections.unmodifiableList(imports);
    }

    public List<MethodInfo> getMethods() {
        return Collections.unmodifiableList(methods);
    }

    public List<MethodCallInfo> getMethodCalls() {
        return Collections.unmodifiableList(methodCalls);
    }

    public List<FieldAccessInfo> getFieldAccesses() {
        return Collections.unmodifiableList(fieldAccesses);
    }
    
    /**
     * Find an assignment at the given position, prioritizing LHS over RHS.
     * Searches across all script fields and external field assignments.
     * Used by TokenHoverInfo for finding assignment errors.
     */
    public AssignmentInfo findAssignmentAtPosition(int position) {
        // Check script fields (FieldInfo.findAssignmentAtPosition already handles LHS/RHS priority)
        for (FieldInfo field : globalFields.values()) {
            AssignmentInfo assign = field.findAssignmentAtPosition(position);
            if (assign != null) {
                return assign;
            }
        }

        for (Map<String, FieldInfo> locals : methodLocals.values()) {
            for (FieldInfo field : locals.values()) {
                AssignmentInfo assign = field.findAssignmentAtPosition(position);
                if (assign != null) {
                    return assign;
                }
            }
        }

        for (AssignmentInfo assign : declarationErrors) {
            if (assign.containsLhsPosition(position)) {
                return assign;
            }
        }
        
        // Check external field assignments with same LHS-first priority
        for (AssignmentInfo assign : externalFieldAssignments) {
            if (assign.containsLhsPosition(position)) {
                return assign;
            }
        }
        
        for (AssignmentInfo assign : externalFieldAssignments) {
            if (assign.containsRhsPosition(position)) {
                return assign;
            }
        }

        for (ScriptTypeInfo scriptType : scriptTypes.values()) {
            for (FieldInfo field : scriptType.getFields().values()) {
                AssignmentInfo assign = field.findAssignmentAtPosition(position);
                if (assign != null)
                    return assign;
            }
        }
        
        return null;
    }

    public Map<String, FieldInfo> getGlobalFields() {
        return Collections.unmodifiableMap(globalFields);
    }
    
    /**
     * Get all errored assignments across all fields (global, local, and external).
     * Used by ScriptLine to draw error underlines.
     */
    public List<AssignmentInfo> getAllErroredAssignments() {
        List<AssignmentInfo> errored = new ArrayList<>();
        
        // Check global fields
        for (FieldInfo field : globalFields.values()) {
            errored.addAll(field.getErroredAssignments());
        }
        
        // Check method locals
        for (Map<String, FieldInfo> locals : methodLocals.values()) {
            for (FieldInfo field : locals.values()) {
                errored.addAll(field.getErroredAssignments());
            }
        }
        
        // Check external field assignments
        for (AssignmentInfo assign : externalFieldAssignments) {
            if (assign.hasError()) {
                errored.add(assign);
            }
        }


        for (ScriptTypeInfo scriptType : scriptTypes.values()) {
            for (FieldInfo field : scriptType.getFields().values()) {
                errored.addAll(field.getErroredAssignments());
            }
        }
        

        // Include declaration errors (duplicates, etc.)
        errored.addAll(declarationErrors);
        
        return errored;
    }

    public TypeResolver getTypeResolver() {
        return typeResolver;
    }

    public ScriptLine getLine(int index) {
        for (ScriptLine line : lines) {
            if (line.getLineIndex() == index) {
                return line;
            }
        }
        return null;
    }

    public ScriptLine getLineAt(int globalPosition) {
        for (ScriptLine line : lines) {
            if (line.containsPosition(globalPosition)) {
                return line;
            }
        }
        return null;
    }

    public int getLineIndexAt(int globalPosition) {
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).containsPosition(globalPosition)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Get all tokens that fall within a given range [start, end).
     * Returns tokens in order. Tokens that span across the range boundaries
     * are included if any part of them is within the range.
     */
    public List<Token> getTokensInRange(int start, int end) {
        List<Token> result = new ArrayList<>();
        if (start < 0 || end <= start) return result;
        
        for (ScriptLine line : lines) {
            // Skip lines entirely before the range
            if (line.getGlobalEnd() <= start) continue;
            // Stop if line is entirely after the range
            if (line.getGlobalStart() >= end) break;
            
            for (Token token : line.getTokens()) {
                // Check if token overlaps with range
                if (token.getGlobalEnd() > start && token.getGlobalStart() < end) {
                    result.add(token);
                }
            }
        }
        return result;
    }

    /**
     * Find the expected type for an expression at the given position by looking for assignment context.
     * Returns the type of the variable being assigned to, or null if not in an assignment.
     * 
     * Examples:
     * - "Type varName = expr;" -> returns Type
     * - "varName = expr;" -> returns type of varName
     */
    public TypeInfo findExpectedTypeAtPosition(int position) {
        // Walk backward from position to find '='
        int pos = position - 1;
        while (pos >= 0 && Character.isWhitespace(text.charAt(pos))) {
            pos--;
        }

        // Look for '=' that precedes this position
        int equalsPos = -1;
        int depth = 0; // Track parentheses/brackets depth
        for (int i = pos; i >= 0; i--) {
            if (isExcluded(i)) continue;

            char c = text.charAt(i);
            if (c == ')' || c == ']') depth++;
            else if (c == '(' || c == '[') depth--;
            else if (c == '=' && depth == 0) {
                // Make sure it's not ==, !=, <=, >=
                if (i > 0 && "!<>=".indexOf(text.charAt(i - 1)) >= 0) continue;
                if (i < text.length() - 1 && text.charAt(i + 1) == '=') continue;
                equalsPos = i;
                break;
            }
            else if (c == ';' || c == '{' || c == '}') {
                // Reached statement boundary without finding assignment
                break;
            }
        }

        if (equalsPos < 0) {
            return null; // Not in an assignment
        }

        // Now parse the left-hand side of the assignment
        // Could be: "Type varName = expr" or "varName = expr"
        pos = equalsPos - 1;
        while (pos >= 0 && Character.isWhitespace(text.charAt(pos))) {
            pos--;
        }

        if (pos < 0) return null;

        // Find the variable name (work backward to find identifier)
        int varNameEnd = pos + 1;
        int varNameStart = pos;
        while (varNameStart >= 0 && (Character.isJavaIdentifierPart(text.charAt(varNameStart)))) {
            varNameStart--;
        }
        varNameStart++; // Move to first char of identifier

        if (varNameStart >= varNameEnd) return null;

        String varName = text.substring(varNameStart, varNameEnd);

        // Now check if there's a type declaration before the variable name
        pos = varNameStart - 1;
        while (pos >= 0 && Character.isWhitespace(text.charAt(pos))) {
            pos--;
        }

        if (pos < 0) {
            // Just "varName = expr" - look up varName in scope
            return lookupVariableType(varName, position);
        }

        // Check if this could be a type declaration (look for identifier before varName)
        int typeEnd = pos + 1;
        int typeStart = pos;
        while (typeStart >= 0 && (Character.isJavaIdentifierPart(text.charAt(typeStart)) || 
                                   text.charAt(typeStart) == '<' || text.charAt(typeStart) == '>' ||
                                   text.charAt(typeStart) == '[' || text.charAt(typeStart) == ']' ||
                                   text.charAt(typeStart) == ',')) {
            typeStart--;
        }
        typeStart++;

        if (typeStart >= typeEnd) {
            // Just "varName = expr" - look up varName
            return lookupVariableType(varName, position);
        }

        String typeStr = text.substring(typeStart, typeEnd).trim();
        
        // Check if this is a var/let/const keyword (type inference)
        if (typeStr.equals("var") || typeStr.equals("let") || typeStr.equals("const")) {
            // Can't determine expected type from var/let/const
            return null;
        }

        // Resolve the type
        return resolveType(typeStr);
    }

    /**
     * Look up the type of a variable by name at the given position.
     */
    private TypeInfo lookupVariableType(String varName, int position) {
        // Check method locals
        MethodInfo containingMethod = findMethodAtPosition(position);
        if (containingMethod != null) {
            Map<String, FieldInfo> locals = methodLocals.get(containingMethod.getDeclarationOffset());
            if (locals != null && locals.containsKey(varName)) {
                FieldInfo field = locals.get(varName);
                if (field.isVisibleAt(position)) {
                    return field.getDeclaredType();
                }
            }
        }

        // Check global fields
        if (globalFields.containsKey(varName)) {
            return globalFields.get(varName).getDeclaredType();
        }

        return null;
    }
}
