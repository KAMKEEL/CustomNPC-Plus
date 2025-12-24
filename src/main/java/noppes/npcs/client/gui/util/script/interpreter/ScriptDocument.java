package noppes.npcs.client.gui.util.script.interpreter;

import net.minecraft.client.Minecraft;
import noppes.npcs.client.ClientProxy;

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
            "\\b([A-Za-z_][a-zA-Z0-9_<>,\\s\\[\\]]*)\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*(=|;)");
    private static final Pattern NEW_TYPE_PATTERN = Pattern.compile("\\bnew\\s+([A-Za-z_][a-zA-Z0-9_]*)");

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
    }

    /**
     * Parse class, interface, and enum declarations defined in the script.
     * Creates ScriptTypeInfo instances and stores them for later resolution.
     */
    private void parseScriptTypes() {
        // Pattern: (class|interface|enum) ClassName { ... }
        Pattern typeDecl = Pattern.compile(
                "\\b(class|interface|enum)\\s+([A-Za-z_][a-zA-Z0-9_]*)\\s*(?:extends\\s+[A-Za-z_][a-zA-Z0-9_.]*)?\\s*(?:implements\\s+[A-Za-z_][a-zA-Z0-9_.,\\s]*)?\\s*\\{");
        
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
            
            ScriptTypeInfo scriptType = ScriptTypeInfo.create(
                    typeName, kind, m.start(), bodyStart, bodyEnd);
            
            // Parse fields and methods inside this type
            parseScriptTypeMembers(scriptType);
            
            scriptTypes.put(typeName, scriptType);
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
            
            String typeName = fm.group(1).trim();
            String fieldName = fm.group(2);
            
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
            
            TypeInfo fieldType = resolveType(typeName);
            FieldInfo fieldInfo = FieldInfo.globalField(fieldName, fieldType, absPos);
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
            
            TypeInfo returnType = resolveType(returnTypeName);
            List<FieldInfo> params = parseParametersWithPositions(paramList, 
                    bodyStart + 1 + mm.start(3));
            
            MethodInfo methodInfo = MethodInfo.declaration(
                    methodName, returnType, params, absPos, methodBodyStart, methodBodyEnd);
            scriptType.addMethod(methodInfo);
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

            // Parse parameters with their actual positions
            List<FieldInfo> params = parseParametersWithPositions(paramList, m.start(3));

            MethodInfo methodInfo = MethodInfo.declaration(
                    methodName,
                    resolveType(returnType),
                    params,
                    m.start(),
                    bodyStart,
                    bodyEnd
            );
            methods.add(methodInfo);
        }

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
            Pattern localDecl = Pattern.compile(
                    "\\b([A-Za-z_][a-zA-Z0-9_<>\\[\\]]*)\\s+([A-Za-z_][a-zA-Z0-9_]*)\\s*(=|;|,)");
            Matcher m = localDecl.matcher(bodyText);
            while (m.find()) {
                int absPos = bodyStart + m.start();
                if (isExcluded(absPos)) continue;

                String typeName = m.group(1);
                String varName = m.group(2);
                String delimiter = m.group(3);
                
                // Skip if it looks like a method call or control flow
                if (typeName.equals("return") || typeName.equals("if") || typeName.equals("while") ||
                    typeName.equals("for") || typeName.equals("switch") || typeName.equals("catch") ||
                    typeName.equals("new") || typeName.equals("throw")) {
                    continue;
                }

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
                
                int declPos = bodyStart + m.start(2);
                FieldInfo fieldInfo = FieldInfo.localField(varName, typeInfo, declPos, method);
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
        if (Character.isUpperCase(firstIdent.charAt(0))) {
            // Static access like Event.player
            currentType = resolveType(firstIdent);
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
                    MethodInfo methodInfo = currentType.getMethodInfo(segment);
                    currentType = (methodInfo != null) ? methodInfo.getReturnType() : null;
                } else {
                    return null;
                }
                
                // Skip to after the closing paren
                int closeParen = findMatchingParen(pos);
                if (closeParen < 0) return null;
                pos = closeParen + 1;
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
            if (isExcluded(m.start()))
                continue;

            String typeName = m.group(1);
            String fieldName = m.group(2);
            int position = m.start(2);

            // Check if inside a method - if so, it's a local, not global
            boolean insideMethod = false;
            for (MethodInfo method : methods) {
                if (method.containsPosition(position)) {
                    insideMethod = true;
                    break;
                }
            }

            if (!insideMethod) {
                TypeInfo typeInfo = resolveType(typeName);
                FieldInfo fieldInfo = FieldInfo.globalField(fieldName, typeInfo, position);
                globalFields.put(fieldName, fieldInfo);
            }
        }
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
        addPatternMarks(marks, NEW_TYPE_PATTERN, TokenType.NEW_TYPE, 1);

        // Methods
        markMethodDeclarations(marks);

        // Numbers
        addPatternMarks(marks, NUMBER_PATTERN, TokenType.NUMBER);

        // Variables and fields
        markVariables(marks);

        // Chained field accesses (e.g., mc.player.world, this.field)
        markChainedFieldAccesses(marks);

        markMethodCalls(marks);
        
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
                        "([A-Z][a-zA-Z0-9_]*)\\s*");

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

            // Only process if it looks like a type name (starts with uppercase)
            if (Character.isUpperCase(typeName.charAt(0))) {
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
            if (returnType.equals("class") || returnType.equals("interface") || returnType.equals("enum")) {
                continue;
            }

            // Return type
            marks.add(new ScriptLine.Mark(m.start(1), m.end(1), TokenType.TYPE_DECL));
            // Method name
            marks.add(new ScriptLine.Mark(m.start(2), m.end(2), TokenType.METHOD_DECL));
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
            
            // Resolve the receiver chain and get the final type
            TypeInfo receiverType = resolveReceiverChain(nameStart);
            MethodInfo resolvedMethod = null;

            if (receiverType != null) {
                // We have a resolved receiver type - check if method exists
                if (receiverType.hasMethod(methodName)) {
                    resolvedMethod = receiverType.getMethodInfo(methodName);
                    
                    // Create MethodCallInfo for validation (with static access flag)
                    MethodCallInfo callInfo = new MethodCallInfo(
                        methodName, nameStart, nameEnd, openParen, closeParen,
                        arguments, receiverType, resolvedMethod, isStaticAccess
                    );
                    callInfo.validate();
                    
                    // Always mark method call as green (METHOD_CALL), with error info attached for underline
                    // The MethodCallInfo is attached so rendering can draw curly underline if there's an error
                    marks.add(new ScriptLine.Mark(nameStart, nameEnd, TokenType.METHOD_CALL, callInfo));
                } else {
                    // Method doesn't exist on this type
                    marks.add(new ScriptLine.Mark(nameStart, nameEnd, TokenType.UNDEFINED_VAR));
                }
            } else {
                // No receiver OR receiver couldn't be resolved
                // Check if there's a dot before this (meaning there WAS a receiver, we just couldn't resolve it)
                boolean hasDot = isPrecededByDot(nameStart);

                if (hasDot) {
                    // There was a receiver but we couldn't resolve it - mark as undefined
                    marks.add(new ScriptLine.Mark(nameStart, nameEnd, TokenType.UNDEFINED_VAR));
                } else {
                    // No receiver - standalone method call
                    // Check if this method is defined in the script itself
                    if (isScriptMethod(methodName)) {
                        resolvedMethod = getScriptMethodInfo(methodName);
                        
                        // Create MethodCallInfo for validation
                        MethodCallInfo callInfo = new MethodCallInfo(
                            methodName, nameStart, nameEnd, openParen, closeParen,
                            arguments, null, resolvedMethod
                        );
                        callInfo.validate();
                        
                        // Always mark method call as green (METHOD_CALL), with error info attached for underline
                        marks.add(new ScriptLine.Mark(nameStart, nameEnd, TokenType.METHOD_CALL, callInfo));
                    } else {
                        // Unknown standalone method - mark as undefined
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
        
        // It's static access if the identifier starts with uppercase
        return !ident.isEmpty() && Character.isUpperCase(ident.charAt(0));
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
                    TypeInfo argType = resolveExpressionType(argText, actualStart);
                    
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
     * Try to resolve the type of an expression (for argument type checking).
     * This is a simplified version that handles common cases.
     */
    private TypeInfo resolveExpressionType(String expr, int position) {
        expr = expr.trim();
        
        if (expr.isEmpty()) {
            return null;
        }
        
        // String literals
        if (expr.startsWith("\"") && expr.endsWith("\"")) {
            return resolveType("String");
        }
        
        // Character literals
        if (expr.startsWith("'") && expr.endsWith("'")) {
            return TypeInfo.forPrimitive("char");
        }
        
        // Boolean literals
        if (expr.equals("true") || expr.equals("false")) {
            return TypeInfo.forPrimitive("boolean");
        }
        
        // Null literal
        if (expr.equals("null")) {
            return null; // null is compatible with any reference type
        }
        
        // Numeric literals
        if (expr.matches("-?\\d+[lL]?")) {
            if (expr.toLowerCase().endsWith("l")) {
                return TypeInfo.forPrimitive("long");
            }
            return TypeInfo.forPrimitive("int");
        }
        if (expr.matches("-?\\d+\\.\\d*[fF]?")) {
            if (expr.toLowerCase().endsWith("f")) {
                return TypeInfo.forPrimitive("float");
            }
            return TypeInfo.forPrimitive("double");
        }
        if (expr.matches("-?\\d+\\.\\d*[dD]")) {
            return TypeInfo.forPrimitive("double");
        }
        
        // Simple variable reference
        if (expr.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            FieldInfo varInfo = resolveVariable(expr, position);
            if (varInfo != null) {
                return varInfo.getTypeInfo();
            }
        }
        
        // "this" keyword
        if (expr.equals("this")) {
            // Could return the enclosing class type if we had that info
            return null;
        }
        
        // "new Type()" expressions
        if (expr.startsWith("new ")) {
            Matcher newMatcher = NEW_TYPE_PATTERN.matcher(expr);
            if (newMatcher.find()) {
                return resolveType(newMatcher.group(1));
            }
        }
        
        // Chain expressions like "obj.field" or "obj.method()"
        // This is complex - for now, return null to indicate unknown
        // The type compatibility check will be lenient with null types
        
        return null;
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

        // Walk backward to collect all segments in the chain
        // Segments can be identifiers OR method calls (identifier followed by parentheses)
        List<ChainSegment> chainSegments = new ArrayList<>();

        int pos = scanPos; // Currently at the dot

        while (pos >= 0) {
            // Skip the dot
            pos--;

            // Skip whitespace
            while (pos >= 0 && Character.isWhitespace(text.charAt(pos)))
                pos--;

            if (pos < 0)
                break;

            char c = text.charAt(pos);
            
            // Check if this is the end of a method call (closing paren)
            if (c == ')') {
                // Find the matching opening paren
                int closeParen = pos;
                int openParen = findMatchingParenBackward(pos);
                if (openParen < 0) {
                    return null; // Malformed
                }
                
                // Now find the method name before the open paren
                int beforeParen = openParen - 1;
                while (beforeParen >= 0 && Character.isWhitespace(text.charAt(beforeParen)))
                    beforeParen--;
                
                if (beforeParen < 0 || !Character.isJavaIdentifierPart(text.charAt(beforeParen))) {
                    return null; // No method name
                }
                
                // Read the method name backward
                int methodNameEnd = beforeParen + 1;
                while (beforeParen >= 0 && Character.isJavaIdentifierPart(text.charAt(beforeParen)))
                    beforeParen--;
                int methodNameBegin = beforeParen + 1;
                
                String methodName = text.substring(methodNameBegin, methodNameEnd);
                chainSegments.add(0, new ChainSegment(methodName, methodNameBegin, methodNameEnd, true));
                
                pos = beforeParen;
            } else if (Character.isJavaIdentifierPart(c)) {
                // Regular identifier (field or variable)
                int identEnd = pos + 1;
                while (pos >= 0 && Character.isJavaIdentifierPart(text.charAt(pos)))
                    pos--;
                int identStart = pos + 1;

                String ident = text.substring(identStart, identEnd);
                chainSegments.add(0, new ChainSegment(ident, identStart, identEnd, false));
            } else {
                // Unexpected character - end of chain
                break;
            }

            // Skip whitespace
            while (pos >= 0 && Character.isWhitespace(text.charAt(pos)))
                pos--;

            // Check if there's another dot (continuing the chain)
            if (pos >= 0 && text.charAt(pos) == '.') {
                // Continue the loop to get the next segment
                continue;
            } else {
                // No more dots - end of chain
                break;
            }
        }

        if (chainSegments.isEmpty()) {
            return null;
        }

        // Now resolve the chain from left to right
        ChainSegment firstSeg = chainSegments.get(0);
        String firstSegment = firstSeg.name;
        TypeInfo currentType = null;

        if (firstSegment.equals("this")) {
            // For 'this', check if we're inside a script-defined class
            currentType = findEnclosingScriptType(methodNameStart);
            
            if (currentType == null && chainSegments.size() > 1) {
                // Fallback: this.something - check globalFields
                ChainSegment nextSeg = chainSegments.get(1);
                if (!nextSeg.isMethodCall && globalFields.containsKey(nextSeg.name)) {
                    currentType = globalFields.get(nextSeg.name).getTypeInfo();
                    // Continue resolving from index 2
                    for (int i = 2; i < chainSegments.size(); i++) {
                        currentType = resolveChainSegment(currentType, chainSegments.get(i));
                        if (currentType == null) return null;
                    }
                    return currentType;
                }
            }
            // If we have a script type, continue from index 1
            if (currentType != null) {
                for (int i = 1; i < chainSegments.size(); i++) {
                    currentType = resolveChainSegment(currentType, chainSegments.get(i));
                    if (currentType == null) return null;
                }
                return currentType;
            }
            return null;
        } else if (Character.isUpperCase(firstSegment.charAt(0))) {
            // Static access like Minecraft.getMinecraft() - the type is the class itself
            currentType = resolveType(firstSegment);
        } else if (!firstSeg.isMethodCall) {
            // Variable like mc.thePlayer.worldObj
            FieldInfo varInfo = resolveVariable(firstSegment, firstSeg.start);
            if (varInfo != null) {
                currentType = varInfo.getTypeInfo();
            }
        } else {
            // First segment is a method call without a receiver - check script methods
            if (isScriptMethod(firstSegment)) {
                MethodInfo scriptMethod = getScriptMethodInfo(firstSegment);
                if (scriptMethod != null) {
                    currentType = scriptMethod.getReturnType();
                }
            }
        }

        // Resolve remaining segments
        for (int i = 1; i < chainSegments.size(); i++) {
            currentType = resolveChainSegment(currentType, chainSegments.get(i));
            if (currentType == null) return null;
        }

        return currentType;
    }

    /**
     * Helper class for chain segments (can be field access or method call).
     */
    private static class ChainSegment {
        final String name;
        final int start;
        final int end;
        final boolean isMethodCall;
        
        ChainSegment(String name, int start, int end, boolean isMethodCall) {
            this.name = name;
            this.start = start;
            this.end = end;
            this.isMethodCall = isMethodCall;
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
            // Method call - get return type
            if (currentType.hasMethod(segment.name)) {
                MethodInfo methodInfo = currentType.getMethodInfo(segment.name);
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
                        marks.add(new ScriptLine.Mark(m.start(1), m.end(1), TokenType.LOCAL_FIELD, localInfo));
                        continue;
                    }
                }

                // Check global fields
                if (globalFields.containsKey(name)) {
                    FieldInfo fieldInfo = globalFields.get(name);
                    marks.add(new ScriptLine.Mark(m.start(1), m.end(1), TokenType.GLOBAL_FIELD, fieldInfo));
                    continue;
                }

                // Skip uppercase if not a known field - type handling will deal with it
                if (isUppercase)
                    continue;

                // Unknown variable inside method - mark as undefined
                marks.add(new ScriptLine.Mark(m.start(1), m.end(1), TokenType.UNDEFINED_VAR));
            } else {
                // Outside any method
                // Check global fields
                if (globalFields.containsKey(name)) {
                    FieldInfo fieldInfo = globalFields.get(name);
                    marks.add(new ScriptLine.Mark(m.start(1), m.end(1), TokenType.GLOBAL_FIELD, fieldInfo));
                    continue;
                }

                // Skip uppercase if not a known field - type handling will deal with it
                if (isUppercase)
                    continue;

                // Unknown variable outside method - mark as undefined
                marks.add(new ScriptLine.Mark(m.start(1), m.end(1), TokenType.UNDEFINED_VAR));
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

            if (firstIsThis) {
                // For 'this', we don't have a class context to resolve, but we can mark subsequent fields
                // as global fields if they exist in globalFields
                currentType = null; // We'll use globalFields for the next segment
            } else if (Character.isUpperCase(firstSegment.charAt(0))) {
                // Static field access like SomeClass.field
                currentType = resolveType(firstSegment);
            } else {
                // Variable field access
                FieldInfo varInfo = resolveVariable(firstSegment, chainStart);
                if (varInfo != null) {
                    currentType = varInfo.getTypeInfo();
                }
            }

            // Mark the segments - skip first segment (already marked by markVariables or markImportedClassUsages)
            for (int i = 1; i < chainSegments.size(); i++) {
                String segment = chainSegments.get(i);
                int[] segPos = segmentPositions.get(i);

                if (isExcluded(segPos[0]))
                    continue;

                if (i == 1 && firstIsThis) {
                    // For "this.fiel/d", check if field exists in globalFields
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
                    //TODO: THIS MAY NEED TO HANDLE STATIC VS INSTANCE FIELDS WITH DIFFERENT RENDERING
                    if (currentType.hasField(segment)) {
                        FieldInfo fieldInfo = currentType.getFieldInfo(segment);
                        marks.add(new ScriptLine.Mark(segPos[0], segPos[1], TokenType.GLOBAL_FIELD, fieldInfo));
                        // Update currentType for next segment
                        currentType = (fieldInfo != null) ? fieldInfo.getTypeInfo() : null;

                        /*TODO THIS ELSE OVERRIDES FOR CHAINED METHOD CALLS LIKE "Minecraft.getMinecraft()"
                        Do we resolve this by increasing METHOD_CALL PRIORITY or how exactly?
                        Think of the best fitting solution that doesnt regress other parts of the whole framework.   
                        */
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
    }

    private void markImportedClassUsages(List<ScriptLine.Mark> marks) {
        // Find uppercase identifiers followed by dot (static method calls, field access)
        Pattern classUsage = Pattern.compile("\\b([A-Z][a-zA-Z0-9_]*)\\s*\\.");
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
        Pattern typeUsage = Pattern.compile("\\b(new\\s+)?([A-Z][a-zA-Z0-9_]*)(?:\\s*<[^>]*>)?\\s*(?:\\(|\\[|\\b[a-z])");
        Matcher tm = typeUsage.matcher(text);

        while (tm.find()) {
            String className = tm.group(2);
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

    public Map<String, FieldInfo> getGlobalFields() {
        return Collections.unmodifiableMap(globalFields);
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
}
