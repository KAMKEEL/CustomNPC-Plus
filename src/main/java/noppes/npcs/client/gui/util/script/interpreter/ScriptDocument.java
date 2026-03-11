package noppes.npcs.client.gui.util.script.interpreter;

import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.gui.util.script.interpreter.expression.CastExpressionResolver;
import noppes.npcs.client.gui.util.script.interpreter.expression.ExpressionNode;
import noppes.npcs.client.gui.util.script.interpreter.expression.ExpressionTypeResolver;
import noppes.npcs.client.gui.util.script.interpreter.field.AssignmentInfo;
import noppes.npcs.client.gui.util.script.interpreter.field.EnumConstantInfo;
import noppes.npcs.client.gui.util.script.interpreter.field.FieldAccessInfo;
import noppes.npcs.client.gui.util.script.interpreter.field.FieldInfo;
import noppes.npcs.client.gui.util.script.interpreter.js_parser.JSScriptAnalyzer;
import noppes.npcs.client.gui.util.script.interpreter.js_parser.JSMethodInfo;
import noppes.npcs.client.gui.util.script.interpreter.js_parser.JSTypeRegistry;
import noppes.npcs.client.gui.util.script.interpreter.js_parser.TypeParamInfo;
import noppes.npcs.client.gui.util.script.interpreter.jsdoc.JSDocInfo;
import noppes.npcs.client.gui.util.script.interpreter.jsdoc.JSDocParamTag;
import noppes.npcs.client.gui.util.script.interpreter.jsdoc.JSDocParser;
import noppes.npcs.client.gui.util.script.interpreter.method.MethodCallInfo;
import noppes.npcs.client.gui.util.script.interpreter.method.MethodInfo;
import noppes.npcs.client.gui.util.script.interpreter.method.MethodSignature;
import noppes.npcs.client.gui.util.script.interpreter.token.Token;
import noppes.npcs.client.gui.util.script.interpreter.token.TokenErrorMessage;
import noppes.npcs.client.gui.util.script.interpreter.token.TokenType;
import noppes.npcs.client.gui.util.script.interpreter.type.*;
import noppes.npcs.client.gui.util.script.interpreter.type.synthetic.SyntheticField;
import noppes.npcs.client.gui.util.script.interpreter.type.synthetic.SyntheticMethod;
import noppes.npcs.client.gui.util.script.interpreter.type.synthetic.SyntheticType;
import noppes.npcs.client.gui.util.script.ScopeInfo;
import noppes.npcs.constants.ScriptContext;
import noppes.npcs.controllers.data.DataScript;
import noppes.npcs.controllers.ScriptHookController;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    public static ScriptDocument INSTANCE = null; // For easy access in expressions
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
            "\\b(null|boolean|int|float|double|long|char|byte|short|void|if|else|switch|case|for|while|do|try|catch|finally|return|throw|var|let|const|function|continue|break|this|super|new|typeof|instanceof|import)\\b");

    private static final Pattern KEYWORD_JS_PATTERN = Pattern.compile(
            "\\b(function|var|let|const|if|else|for|while|do|switch|case|break|continue|return|" +
                    "try|catch|finally|throw|delete|new|typeof|instanceof|in|of|this|null|undefined|true|false|" +
                    "class|extends|import|export|default|async|await|yield)\\b");
    
    // Declarations - Updated to capture method parameters
    private static final Pattern IMPORT_PATTERN = Pattern.compile(
            "(?m)\\bimport\\s+(?:static\\s+)?([A-Za-z_][A-Za-z0-9_]*(?:\\s*\\.\\s*[A-Za-z_][A-Za-z0-9_]*)*)(?:\\s*\\.\\s*\\*?)?\\s*(?:;|$)");
    private static final Pattern CLASS_DECL_PATTERN = Pattern.compile(
            "\\b(class|interface|enum)\\s+([A-Za-z_][a-zA-Z0-9_]*)");
    private static final Pattern METHOD_DECL_PATTERN = Pattern.compile(
            "\\b([A-Za-z_][a-zA-Z0-9_<>\\[\\]]*)\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(");
    private static final Pattern METHOD_CALL_PATTERN = Pattern.compile(
            "([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(");
    // Also matches ',' as a delimiter so that "int x, y, z;" is recognized
    // (the first declarator "int x," is captured; continuations are scanned manually).
    private static final Pattern FIELD_DECL_PATTERN = Pattern.compile(
            "\\b([A-Za-z_][a-zA-Z0-9_.<>,[ \\t\\n\\r]\\[\\]]*)[ \\t]+([a-zA-Z_][a-zA-Z0-9_]*)[ \\t]*(=|;|,)");
    private static final Pattern NEW_TYPE_PATTERN = Pattern.compile("\\bnew\\s+([A-Za-z_][a-zA-Z0-9_]*)\\s*(?:<([^>]*)>)?");
    
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
    private final Set<String> wildcardPackages = new HashSet<>();
    private Map<String, ImportData> importsBySimpleName = new HashMap<>();

    // Script-defined types (classes, interfaces, enums defined in the script)
    // PRIMARY: simple name → ScriptTypeInfo (backward-compatible, top-level only)
    private final Map<String, ScriptTypeInfo> scriptTypes = new HashMap<>();
    // SECONDARY: dollar-separated full name → ScriptTypeInfo (all types including nested)
    // Enables O(1) lookup by qualified name for any nesting depth (e.g., "Outer$Middle$Inner")
    private final Map<String, ScriptTypeInfo> scriptTypesByFullName = new HashMap<>();
    // TERTIARY: dot-separated display name → ScriptTypeInfo (for resolveType("Outer.Inner"))
    // Populated alongside scriptTypesByFullName during parsing; matches user-written syntax
    private final Map<String, ScriptTypeInfo> scriptTypesByDotName = new HashMap<>();
    
    private final List<MethodInfo> methods = new ArrayList<>();
    private final Map<String, FieldInfo> globalFields = new HashMap<>();
    // Local variables per method (methodStartOffset -> {varName -> FieldInfo})
    private final Map<Integer, Map<String, List<FieldInfo>>> methodLocals = new HashMap<>();
    // Top-level variables (outside methods) - for JavaScript, these can be treated as globals, but we keep them separate for clarity
    // Mainly top level loops like for (var x : collection) { } where x is a local variable but not inside a method
    private final Map<String, List<FieldInfo>> topLevelLocals = new HashMap<>();

    // Inner callable scopes (lambdas, JS function expressions) - NOT in methods list
    private final List<InnerCallableScope> innerScopes = new ArrayList<>();

    // Method calls - stores all parsed method call information
    private final List<MethodCallInfo> methodCalls = new ArrayList<>();
    // Field accesses - stores all parsed field access information
    private final List<FieldAccessInfo> fieldAccesses = new ArrayList<>();
    // Assignments to external fields (fields from reflection, not script-defined)
    private final List<AssignmentInfo> externalFieldAssignments = new ArrayList<>();

    // Declaration errors (duplicate declarations, etc.)
    private final List<AssignmentInfo> declarationErrors = new ArrayList<>();

    // Centralized error list - populated by ErrorUnderlineRenderer during rendering
    private final List<DocumentError> errors = new ArrayList<>();

    // Excluded regions (strings/comments) - positions where other patterns shouldn't match
    private final List<int[]> excludedRanges = new ArrayList<>();
    
    // SAM context tracking for named function references
    // Maps method name -> first SAM signature applied (for conflict detection)
    private final Map<String, MethodInfo> scriptMethodSamContexts = new HashMap<>();

    // Cached object literal analyses, keyed by brace-start offset in text.
    // Populated once in parseObjectLiterals() and consumed by markObjectLiteralKeys,
    // resolveExpressionType, and attachObjectLiteralContext.
    private final Map<Integer, ObjectLiteralParser.ObjectLiteralAnalysis> objectLiterals = new HashMap<>();
    
    // Thread-local to communicate SAM conflict errors from resolveIdentifier back to parseMethodArguments
    // Set when injectSamParameterTypes detects a conflict, cleared after argument is created
    private static final ThreadLocal<String> CURRENT_SAM_CONFLICT_ERROR = new ThreadLocal<>();

    // Type resolver
    private final TypeResolver typeResolver;
    
    // JSDoc parser for extracting type information from JSDoc comments
    private final JSDocParser jsDocParser = new JSDocParser(this);
    
    // Script language: "ECMAScript", "Groovy", etc.
    private String language = "ECMAScript";

    // Script context: NPC, PLAYER, BLOCK, ITEM, etc.
    // Determines which hooks and event types are available
    private ScriptContext scriptContext = ScriptContext.GLOBAL;

    // Editor-injected globals (name -> type name) provided by the handler
    private Map<String, String> editorGlobals = Collections.emptyMap();

    // Implicit imports from JaninoScript default imports and hook parameter types
    // These are types that should be resolved even without explicit import statements
    private final Set<String> implicitImports = new HashSet<>();

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
    
    public ScriptDocument(String text, String language) {
        this.typeResolver = TypeResolver.getInstance();
        this.language = language != null ? language : "ECMAScript";
        setText(text);
    }
    
    /**
     * Set the scripting language.
     */
    public void setLanguage(String language) {
        this.language = language != null ? language : "ECMAScript";
    }
    
    /**
     * Get the scripting language.
     */
    public String getLanguage() {
        return language;
    }
    
    /**
     * Check if this is a JavaScript/ECMAScript document.
     */
    public boolean isJavaScript() {
        return "ECMAScript".equalsIgnoreCase(language);
    }

    /**
     * Set the script context (NPC, PLAYER, BLOCK, ITEM, etc.).
     * This determines which hooks and event types are available for autocomplete.
     *
     * @param context The script context
     */
    public void setScriptContext(ScriptContext context) {
        this.scriptContext = context != null ? context : ScriptContext.GLOBAL;
    }

    /**
     * Get the current script context.
     *
     * @return The script context (NPC, PLAYER, BLOCK, ITEM, etc.)
     */
    public ScriptContext getScriptContext() {
        return scriptContext;
    }

    /**
     * Set editor-injected globals (name -> type name).
     * i.e. DataScript global definitions {@link DataScript#getEditorGlobals(String)}
     * This is provided by the script handler at editor init time.
     */
    public void setEditorGlobals(Map<String, String> globals) {
        if (globals == null || globals.isEmpty()) {
            this.editorGlobals = Collections.emptyMap();
            return;
        }
        this.editorGlobals = new LinkedHashMap<>(globals);
    }

    /**
     * Get editor-injected globals (name -> type name).
     */
    public Map<String, String> getEditorGlobals() {
        return Collections.unmodifiableMap(editorGlobals);
    }

    /**
     * Add implicit imports that should be resolved without explicit import statements.
     * Used for JaninoScript default imports and hook parameter types.
     * 
     * Patterns can be:
     * - Wildcard: "noppes.npcs.api.*" (imports all classes from package)
     * - Specific class: "noppes.npcs.api.event.INpcEvent$InitEvent" (nested class)
     * - Regular class: "noppes.npcs.api.entity.IEntity"
     *
     * @param patterns Array of import patterns to add (packages with .* or fully qualified class names)
     */
    public void addImplicitImports(String... patterns) {
        if (patterns != null) {
            for (String pattern : patterns) {
                if (pattern != null && !pattern.isEmpty()) {
                    this.implicitImports.add(pattern);
                }
            }
        }
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
        INSTANCE = this;
    }

    // ==================== TOKENIZATION ====================

    /**
     * Main tokenization entry point.
     * Performs complete analysis and builds tokens for all lines.
     * Uses the SAME unified pipeline for both Java and JavaScript.
     * 
     * All data structures (methods, globalFields, methodLocals, methodCalls, 
     * fieldAccesses, etc.) are shared between languages.
     */
    public void formatCodeText() {
        // Clear previous state (same for both languages)
        errors.clear();
        imports.clear();
        methods.clear();
        globalFields.clear();
        wildcardPackages.clear();
        excludedRanges.clear();
        methodLocals.clear();
        topLevelLocals.clear();
        scriptTypes.clear();
        scriptTypesByFullName.clear();
        scriptTypesByDotName.clear();
        innerScopes.clear();
        methodCalls.clear();
        externalFieldAssignments.clear();
        declarationErrors.clear();
        scriptMethodSamContexts.clear();
        objectLiterals.clear();
        
        // Unified pipeline for both languages
        List<ScriptLine.Mark> marks = formatUnified();

        // Phase 5: Resolve conflicts and sort
        marks = resolveConflicts(marks);

        // Phase 6: Build tokens for each line
        for (ScriptLine line : lines) {
            line.buildTokensFromMarks(marks, text, this);
        }

        // Phase 6.5: Now that tokens are built, remove implicit imports that aren't actually used
        removeUnusedImplicitImports();

        // Phase 7: Compute indent guides
        computeIndentGuides(marks);

        // Phase 8: Populate centralized error list from all error sources
        populateErrors();
    }

    // Store the last JS analyzer for autocomplete (deprecated - use methods/globalFields/methodLocals instead)
    @Deprecated
    private JSScriptAnalyzer currentJSAnalyzer;
    
    /**
     * @deprecated Use the unified pipeline. Access methods/globalFields/methodLocals directly.
     */
    @Deprecated
    public JSScriptAnalyzer getJSAnalyzer() {
        return currentJSAnalyzer;
    }
    
    /**
     * Unified format method - single pipeline for both Java and JavaScript.
     * ALL methods called here handle BOTH languages using the SAME data structures.
     */
    private List<ScriptLine.Mark> formatUnified() {
        TypeChecker.enterTypeCheckingContext(isJavaScript());
        
        // Phase 1: Find excluded regions (strings/comments) - same for both
        findExcludedRanges();

        // Phase 2: Parse imports (Java only, JS skips this)
        parseImports();

        // Phase 3: Parse structure (methods, fields, locals) - language aware
        parseStructure();

        // Phase 4: Build marks - language aware
        return buildMarks();
    }
    
    /**
     * @deprecated Use formatUnified() instead. Kept for compatibility.
     */
    @Deprecated
    private List<ScriptLine.Mark> formatJavaScript() {
        // Delegate to unified pipeline
        currentJSAnalyzer = new JSScriptAnalyzer(this);
        return currentJSAnalyzer.analyze();
    }
    
    /**
     * @deprecated Use formatUnified() instead. Kept for compatibility.
     */
    @Deprecated  
    private List<ScriptLine.Mark> formatJava() {
        return formatUnified();
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

    
    public boolean isExcluded(int position) {
        for (int[] range : excludedRanges) {
            if (position >= range[0] && position < range[1]) {
                return true;
            }
        }
        return false;
    }

    /**
     * Like isExcluded but uses inclusive end bound (position <= range end).
     * Used by auto-pair logic where the cursor sits right at a string boundary.
     */
    public boolean isExcludedInclusive(int position) {
        for (int[] range : excludedRanges) {
            if (position >= range[0] && position <= range[1]) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if a position falls inside one of the type's inner class bodies.
     * Used to prevent field/constructor declarations inside inner classes
     * from being misattributed to the outer class during member parsing.
     */
    private boolean isInsideNestedType(int position, ScriptTypeInfo type) {
        for (ScriptTypeInfo inner : type.getInnerClasses()) {
            if (inner.containsPosition(position)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the list of excluded ranges (comments, strings, etc.).
     * Used by AutocompleteManager to skip over excluded regions.
     * @return List of [start, end) ranges to exclude
     */
    public List<int[]> getExcludedRanges() {
        return excludedRanges;
    }

    /**
     * Check if a position is within a comment range (not string).
     * Used to skip comment text when scanning for identifiers.
     */
    private boolean isInCommentRange(int position) {
        // Check if position is in a comment by checking against COMMENT_PATTERN
        Matcher m = COMMENT_PATTERN.matcher(text);
        while (m.find()) {
            if (position >= m.start() && position < m.end()) {
                return true;
            }
        }
        return false;
    }

    // ==================== PHASE 2: IMPORTS ====================

    private void parseImports() {
        // JavaScript doesn't use Java-style imports
        if (isJavaScript()) {
            return;
        }
        
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

        // Add ALL implicit imports initially so tokens can be built with proper type resolution
        addAllImplicitImportsToList();
        
        // Resolve all imports (including all implicit ones)
        importsBySimpleName = typeResolver.resolveImports(imports);
    }

    /**
     * Add ALL implicit imports to the imports list initially.
     * This allows tokens to be built with proper type resolution.
     * After tokenization, we'll remove the unused ones.
     */
    private void addAllImplicitImportsToList() {
        for (String pattern : implicitImports) {
            if (pattern == null || pattern.isEmpty()) continue;
            
            boolean isWildcard = pattern.endsWith(".*");
            String fullPath;
            String simpleName;
            
            if (isWildcard) {
                fullPath = pattern.substring(0, pattern.length() - 2); // Remove ".*"
                simpleName = null;
                wildcardPackages.add(fullPath);
            } else {
                fullPath = pattern.replace('$', '.');
                int lastDot = fullPath.lastIndexOf('.');
                simpleName = lastDot >= 0 ? fullPath.substring(lastDot + 1) : fullPath;
            }
            
            ImportData importData = new ImportData(
                    fullPath, simpleName, isWildcard, false,
                    -1, -1, -1, -1
            );
            
            if (!imports.contains(importData)) {
                imports.add(importData);
            }
        }
    }

    /**
     * Remove implicit imports that aren't actually used in the text.
     * This keeps autocomplete clean by not showing types from hooks that aren't present.
     * Called after tokenization is complete.
     */
    private void removeUnusedImplicitImports() {
        // Collect imports to remove (can't remove during iteration)
        List<ImportData> toRemove = new ArrayList<>();
        
        for (ImportData imp : imports) {
            // Only check implicit imports (those with offset -1)
            if (imp.getStartOffset() != -1) continue;
            
            // Skip wildcard imports - keep them all
            if (imp.isWildcard()) continue;
            
            // Check if this specific class import is actually used
            String simpleName = imp.getSimpleName();
            String fullPath = imp.getFullPath();
            
            if (!isTypeReferenced(simpleName, fullPath)) {
                toRemove.add(imp);
            }
        }
        
        // Remove unused implicit imports
        imports.removeAll(toRemove);
        
        // Re-resolve imports after removing unused ones
        importsBySimpleName = typeResolver.resolveImports(imports);
    }

    /**
     * Check if a type is referenced anywhere in the script tokens.
     * Looks for the simple name or any part of the fully qualified name.
     */
    private boolean isTypeReferenced(String simpleName, String fullPath) {
        if (simpleName == null || simpleName.isEmpty()) return false;
        
        // Check all lines and their tokens
        for (ScriptLine line : lines) {
            for (Token token : line.getTokens()) {
                String tokenText = token.getText();
                
                // Check if token matches the simple name
                if (tokenText.equals(simpleName)) {
                    return true;
                }
                
                // Check if token contains nested class reference (e.g., "InitEvent" in "INpcEvent.InitEvent")
                if (fullPath.contains("$") && tokenText.equals(simpleName)) {
                    return true;
                }
                
                // Check for partial matches in qualified references
                if (fullPath.contains(".") && tokenText.contains(".")) {
                    // e.g., "INpcEvent.InitEvent" matches "noppes.npcs.api.event.INpcEvent$InitEvent"
                    String[] pathParts = fullPath.replace('$', '.').split("\\.");
                    String[] tokenParts = tokenText.split("\\.");
                    
                    // Check if the last parts match (e.g., "InitEvent")
                    if (pathParts.length > 0 && tokenParts.length > 0) {
                        String lastPathPart = pathParts[pathParts.length - 1];
                        String lastTokenPart = tokenParts[tokenParts.length - 1];
                        if (lastTokenPart.equals(lastPathPart)) {
                            return true;
                        }
                    }
                }
            }
        }
        
        return false;
    }


    // ==================== PHASE 3: STRUCTURE ====================

    /**
     * Parse the script structure - methods, fields, variables.
     * Uses the SAME logic and data structures for both Java and JavaScript.
     */
    private void parseStructure() {
        // Clear import references before re-parsing
        for (ImportData imp : imports) {
            imp.clearReferences();
        }
        
        // Parse script-defined types (classes, interfaces, enums) - Java only
        if (!isJavaScript()) {
            parseScriptTypes();
        }
        
        // Parse methods/functions - UNIFIED for both languages
        parseMethodDeclarations();

        // Parse inner callable scopes (lambdas, JS function expressions)
        parseInnerCallableScopes();

        // Parse local variables inside methods/functions - UNIFIED for both languages
        parseLocalVariables();

        // Parse global fields (outside methods) - UNIFIED for both languages
        parseGlobalFields();
        
        // Parse and validate assignments (reassignments) - UNIFIED for both languages
        parseAssignments();

        // Cache object literals after globals/locals are available for accurate value-type inference
        parseObjectLiterals();

        // Detect method overrides and interface implementations for script types - Java only
        if (!isJavaScript()) {
            detectMethodInheritance();
        }
    }

    /**
     * Intermediate record for type declarations discovered in Pass 1 of parseScriptTypes().
     * Holds raw data before hierarchy relationships are established in Pass 2.
     */
    private static class RawTypeDeclaration {
        final String name;
        final TypeInfo.Kind kind;
        final int declOffset;
        final int bodyStart;
        final int bodyEnd;
        final int modifiers;
        final String typeParamsClause;
        final int typeParamsClauseOffset; // Global offset of first char inside <...>
        final String extendsClause;
        final String implementsClause;
        final JSDocInfo jsDoc;

        RawTypeDeclaration(String name, TypeInfo.Kind kind, int declOffset, int bodyStart, int bodyEnd,
                           int modifiers, String typeParamsClause, int typeParamsClauseOffset,
                           String extendsClause, String implementsClause, JSDocInfo jsDoc) {
            this.name = name;
            this.kind = kind;
            this.declOffset = declOffset;
            this.bodyStart = bodyStart;
            this.bodyEnd = bodyEnd;
            this.modifiers = modifiers;
            this.typeParamsClause = typeParamsClause;
            this.typeParamsClauseOffset = typeParamsClauseOffset;
            this.extendsClause = extendsClause;
            this.implementsClause = implementsClause;
            this.jsDoc = jsDoc;
        }
    }

    /**
     * Parse class, interface, and enum declarations using a two-pass strategy.
     *
     * Pass 1: Regex scan discovers all type declarations with their body spans.
     * Pass 2: Sort by bodyStart, then use a stack to determine parent-child nesting.
     *         The stack invariant is: top = innermost enclosing type for the current position.
     *         This correctly handles arbitrary nesting depth in O(n) time.
     */
    private void parseScriptTypes() {
        // ========== PASS 1: Discover all type declarations (flat list) ==========
        // Regex only anchors on keyword + name — generic clauses are extracted manually
        // below to correctly handle nested angle brackets like <T extends Comparable<T>>.
        Pattern typeDecl = Pattern.compile(
                "(?:(?:public|private|protected|static|final|abstract)\\s+)*(class|interface|enum)\\s+([A-Za-z_][a-zA-Z0-9_]*)");

        List<RawTypeDeclaration> rawDeclarations = new ArrayList<>();
        Matcher m = typeDecl.matcher(text);
        while (m.find()) {
            if (isExcluded(m.start()))
                continue;

            String kindStr = m.group(1);
            String typeName = m.group(2);

            // Manually extract type params, extends, implements using depth-aware scanning
            int scanPos = m.end();
            while (scanPos < text.length() && Character.isWhitespace(text.charAt(scanPos)))
                scanPos++;

            // Extract type params clause <...> if present (depth-aware)
            String typeParamsClause = null;
            int typeParamsClauseOffset = -1;
            if (scanPos < text.length() && text.charAt(scanPos) == '<') {
                int depth = 1, i = scanPos + 1;
                while (i < text.length() && depth > 0) {
                    char c = text.charAt(i++);
                    if (c == '<') depth++;
                    else if (c == '>') depth--;
                }
                if (depth == 0) {
                    typeParamsClauseOffset = scanPos + 1;
                    typeParamsClause = text.substring(scanPos + 1, i - 1);
                    scanPos = i;
                    while (scanPos < text.length() && Character.isWhitespace(text.charAt(scanPos)))
                        scanPos++;
                }
            }

            // Find the opening brace, scanning depth-aware for angle brackets
            int bracePos = scanPos;
            int angleDepth = 0;
            while (bracePos < text.length()) {
                char c = text.charAt(bracePos);
                if (c == '<') angleDepth++;
                else if (c == '>') angleDepth--;
                else if (c == '{' && angleDepth == 0) break;
                bracePos++;
            }
            if (bracePos >= text.length()) continue;

            String betweenParamsAndBrace = text.substring(scanPos, bracePos).trim();

            // Parse extends/implements from the text between type params and opening brace
            String extendsClause = null;
            String implementsClause = null;

            int extIdx = indexOfAtDepthZero(betweenParamsAndBrace, "extends");
            int implIdx = indexOfAtDepthZero(betweenParamsAndBrace, "implements");

            if (extIdx >= 0) {
                int extEnd = implIdx >= 0 ? implIdx : betweenParamsAndBrace.length();
                extendsClause = betweenParamsAndBrace.substring(extIdx + 7, extEnd).trim();
            }
            if (implIdx >= 0) {
                implementsClause = betweenParamsAndBrace.substring(implIdx + 10).trim();
            }

            int bodyStart = bracePos;
            int bodyEnd = findMatchingBrace(bodyStart);
            if (bodyEnd < 0) bodyEnd = text.length();

            TypeInfo.Kind kind;
            switch (kindStr) {
                case "interface": kind = TypeInfo.Kind.INTERFACE; break;
                case "enum": kind = TypeInfo.Kind.ENUM; break;
                default: kind = TypeInfo.Kind.CLASS; break;
            }

            String fullMatch = text.substring(m.start(), bodyStart + 1);
            int modifiers = parseModifiers(fullMatch);
            JSDocInfo jsDoc = jsDocParser.extractJSDocBefore(text, m.start());

            rawDeclarations.add(new RawTypeDeclaration(
                    typeName, kind, m.start(), bodyStart, bodyEnd,
                    modifiers, typeParamsClause, typeParamsClauseOffset, extendsClause, implementsClause, jsDoc));
        }

        // ========== PASS 2: Build hierarchy using stack-based containment ==========
        // Sort by bodyStart ascending so outer types are processed before inner types
        rawDeclarations.sort(Comparator.comparingInt(r -> r.bodyStart));

        // Stack tracks the nesting chain: top = current innermost enclosing type
        Deque<ScriptTypeInfo> parentStack = new ArrayDeque<>();

        for (RawTypeDeclaration raw : rawDeclarations) {
            // Pop any parent whose body has ended before this declaration starts.
            // This handles sibling types at the same level: when we reach InnerB,
            // InnerA (whose bodyEnd <= InnerB.bodyStart) gets popped, leaving Outer on top.
            while (!parentStack.isEmpty() && parentStack.peek().getBodyEnd() <= raw.bodyStart) {
                parentStack.pop();
            }

            ScriptTypeInfo scriptType;
            if (parentStack.isEmpty()) {
                // Top-level type: no enclosing parent
                scriptType = ScriptTypeInfo.create(
                        raw.name, raw.kind, raw.declOffset, raw.bodyStart, raw.bodyEnd, raw.modifiers);
                scriptTypes.put(raw.name, scriptType);
            } else {
                // Inner type: parent is top of stack. createInner sets outerClass link
                // and adds this type to the parent's innerClasses list.
                ScriptTypeInfo parent = parentStack.peek();
                scriptType = ScriptTypeInfo.createInner(
                        raw.name, raw.kind, parent, raw.declOffset, raw.bodyStart, raw.bodyEnd, raw.modifiers);
            }

            if (raw.jsDoc != null) {
                scriptType.setJSDocInfo(raw.jsDoc);
            }

            // Parse generic type parameters (e.g., <E>, <K, V>, <T extends Entity>)
            if (raw.typeParamsClause != null && !raw.typeParamsClause.trim().isEmpty()) {
                parseTypeParamsClause(raw.typeParamsClause, scriptType, raw.bodyStart + 1, raw.typeParamsClauseOffset);
            }

            // Register in all lookup maps for O(1) access by any naming convention
            scriptTypesByFullName.put(scriptType.getFullName(), scriptType);
            scriptTypesByDotName.put(scriptType.getDotSeparatedName(), scriptType);

            // Push onto stack — this type can contain further nested types
            parentStack.push(scriptType);

            // Resolve extends clause (parent class inheritance)
            // Use bodyStart+1 so findEnclosingScriptType returns this type,
            // making its declared type params (T, E, etc.) resolvable.
            if (raw.extendsClause != null && !raw.extendsClause.trim().isEmpty()) {
                String parentName = raw.extendsClause.trim();
                // Strip generic args (e.g., "NumberBox<T>" -> "NumberBox") for type resolution
                int genIdx = parentName.indexOf('<');
                String parentGenericArgs = null;
                if (genIdx >= 0) {
                    parentGenericArgs = parentName.substring(genIdx);
                    parentName = parentName.substring(0, genIdx);
                }
                TypeInfo parentType = resolveType(parentName, raw.bodyStart + 1);
                if (parentType == null) {
                    parentType = TypeInfo.unresolved(parentName, parentName);
                }
                // Parameterize the parent type with generic args (e.g., NumberBox<T> -> NumberBox parameterized with T)
                if (parentGenericArgs != null && parentType.isResolved()) {
                    // Strip surrounding < > from parentGenericArgs
                    String argsContent = parentGenericArgs.substring(1, parentGenericArgs.length() - 1).trim();
                    if (!argsContent.isEmpty()) {
                        parentType = parameterizeWithClause(parentType, argsContent, raw.bodyStart + 1);
                    }
                }
                scriptType.setSuperClass(parentType, parentName);
            }

            // Resolve implements clause (interfaces)
            if (raw.implementsClause != null && !raw.implementsClause.trim().isEmpty()) {
                List<String> interfaceNames = splitAtDepthZero(raw.implementsClause, ',');
                for (String ifaceName : interfaceNames) {
                    String trimmedName = ifaceName.trim();
                    if (trimmedName.isEmpty())
                        continue;
                    // Strip generic args (e.g., "Comparable<T>" -> "Comparable")
                    int genIdx = trimmedName.indexOf('<');
                    String ifaceGenericArgs = null;
                    if (genIdx >= 0) {
                        ifaceGenericArgs = trimmedName.substring(genIdx);
                        trimmedName = trimmedName.substring(0, genIdx);
                    }
                    TypeInfo ifaceType = resolveType(trimmedName, raw.bodyStart + 1);
                    if (ifaceType == null) {
                        ifaceType = TypeInfo.unresolved(trimmedName, trimmedName);
                    }
                    if (ifaceGenericArgs != null && ifaceType.isResolved()) {
                        String argsContent = ifaceGenericArgs.substring(1, ifaceGenericArgs.length() - 1).trim();
                        if (!argsContent.isEmpty()) {
                            ifaceType = parameterizeWithClause(ifaceType, argsContent, raw.bodyStart + 1);
                        }
                    }
                    scriptType.addImplementedInterface(ifaceType, trimmedName);
                }
            }

            // Parse fields and methods inside this type
            parseScriptTypeMembers(scriptType);
        }
    }

    /**
     * Parse a generic type parameters clause like "E", "K, V", or "T extends Entity"
     * into TypeParamInfo objects and add them to the script type.
     * Validates that in multi-bound declarations (e.g., {@code T extends Number & Comparable<T>}),
     * all bounds after the first must be interfaces (Java 8+ rule).
     */
    private void parseTypeParamsClause(String clause, ScriptTypeInfo scriptType, int declOffset, int clauseStartOffset) {
        List<String> params = splitAtDepthZero(clause, ',');
        int paramTextPos = 0;
        for (String param : params) {
            String trimmed = param.trim();
            int leadingSpaces = param.indexOf(trimmed.isEmpty() ? param : trimmed);
            if (trimmed.isEmpty()) {
                paramTextPos += param.length() + 1;
                continue;
            }

            int extendsIdx = indexOfAtDepthZero(trimmed, "extends");
            String paramName = extendsIdx >= 0 ? trimmed.substring(0, extendsIdx).trim() : trimmed;
            
            int endIdx = 0;
            while (endIdx < paramName.length() && 
                   (Character.isJavaIdentifierPart(paramName.charAt(endIdx)) || paramName.charAt(endIdx) == '.')) {
                endIdx++;
            }
            paramName = paramName.substring(0, endIdx);
            
            String boundName = null;
            List<String> additionalBoundNames = new ArrayList<>();
            if (extendsIdx >= 0) {
                String afterExtends = trimmed.substring(extendsIdx + 7).trim();
                List<String> bounds = splitAtDepthZero(afterExtends, '&');
                
                // Track position within trimmed to find each bound's absolute location
                int searchFrom = extendsIdx + 7;
                
                for (int bi = 0; bi < bounds.size(); bi++) {
                    String bound = bounds.get(bi).trim();
                    if (bound.isEmpty()) continue;
                    int boundEndIdx = 0;
                    while (boundEndIdx < bound.length() && 
                           (Character.isJavaIdentifierPart(bound.charAt(boundEndIdx)) || bound.charAt(boundEndIdx) == '.')) {
                        boundEndIdx++;
                    }
                    if (boundEndIdx > 0) {
                        String name = bound.substring(0, boundEndIdx);
                        
                        // Find the position of this bound name within trimmed
                        int nameIdxInTrimmed = trimmed.indexOf(name, searchFrom);
                        searchFrom = nameIdxInTrimmed + name.length();
                        
                        if (bi == 0) {
                            boundName = name;
                        } else {
                            additionalBoundNames.add(name);
                            
                            // Validate: secondary bounds must be interfaces
                            if (clauseStartOffset >= 0 && nameIdxInTrimmed >= 0) {
                                TypeInfo addBoundCheck = resolveType(name, declOffset);
                                if (addBoundCheck != null && addBoundCheck.isResolved() && !isInterfaceType(addBoundCheck)) {
                                    int errorStart = clauseStartOffset + paramTextPos + leadingSpaces + nameIdxInTrimmed;
                                    int errorEnd = errorStart + name.length();
                                    addError(null, errorStart, errorEnd, "Interface expected here");
                                }
                            }
                        }
                    }
                }
            }
            
            TypeParamInfo typeParam = new TypeParamInfo(paramName, boundName, null);
            if (boundName != null) {
                TypeInfo boundTypeInfo = resolveType(boundName, declOffset);
                if (boundTypeInfo != null && boundTypeInfo.isResolved()) {
                    typeParam.setBoundTypeInfo(boundTypeInfo);
                }
            }
            for (String addBound : additionalBoundNames) {
                typeParam.addAdditionalBound(addBound);
                TypeInfo addBoundType = resolveType(addBound, declOffset);
                if (addBoundType != null && addBoundType.isResolved()) {
                    typeParam.addAdditionalBoundType(addBoundType);
                }
            }
            scriptType.addDeclaredTypeParam(typeParam);
            paramTextPos += param.length() + 1;
        }
        
        registerTypeParamInterfaceBounds(scriptType);
    }
    
    private boolean isInterfaceType(TypeInfo typeInfo) {
        if (typeInfo.isInterface()) return true;
        Class<?> javaClass = typeInfo.getJavaClass();
        if (javaClass != null) return javaClass.isInterface();
        return false;
    }
    
    private void registerTypeParamInterfaceBounds(ScriptTypeInfo scriptType) {
        for (TypeParamInfo param : scriptType.getDeclaredTypeParams()) {
            TypeInfo primaryBound = param.getBoundTypeInfo();
            if (primaryBound != null && isInterfaceType(primaryBound)) {
                scriptType.addImplementedInterface(primaryBound, primaryBound.getSimpleName());
            }
            for (TypeInfo addBound : param.getAdditionalBoundTypes()) {
                if (isInterfaceType(addBound)) {
                    scriptType.addImplementedInterface(addBound, addBound.getSimpleName());
                }
            }
        }
    }

    /**
     * Split a string by a delimiter character, but only at angle-bracket depth 0.
     * This ensures commas inside nested generics like {@code Map<String, Integer>} are not treated as separators.
     */
    private static List<String> splitAtDepthZero(String s, char delimiter) {
        List<String> result = new ArrayList<>();
        int depth = 0;
        int start = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '<') depth++;
            else if (c == '>') depth--;
            else if (c == delimiter && depth == 0) {
                result.add(s.substring(start, i));
                start = i + 1;
            }
        }
        result.add(s.substring(start));
        return result;
    }

    /**
     * Find the index of a keyword in a string, but only at angle-bracket depth 0.
     * The keyword must be preceded and followed by non-identifier characters (word boundary).
     * Returns -1 if not found at depth 0.
     */
    private static int indexOfAtDepthZero(String s, String keyword) {
        int depth = 0;
        int keyLen = keyword.length();
        for (int i = 0; i <= s.length() - keyLen; i++) {
            char c = s.charAt(i);
            if (c == '<') { depth++; continue; }
            if (c == '>') { depth--; continue; }
            if (depth == 0 && s.regionMatches(i, keyword, 0, keyLen)) {
                // Check word boundaries
                boolean startOk = (i == 0 || !Character.isJavaIdentifierPart(s.charAt(i - 1)));
                boolean endOk = (i + keyLen >= s.length() || !Character.isJavaIdentifierPart(s.charAt(i + keyLen)));
                if (startOk && endOk) {
                    return i;
                }
            }
        }
        return -1;
    }

    private TypeInfo parameterizeWithClause(TypeInfo baseType, String typeArgsClause, int position) {
        return parameterizeWithClause(baseType, typeArgsClause, position, -1);
    }
    
    private TypeInfo parameterizeWithClause(TypeInfo baseType, String typeArgsClause, int position, int clauseTextOffset) {
        List<String> argSegments = splitAtDepthZero(typeArgsClause, ',');
        List<TypeInfo> typeArgs = new ArrayList<>();
        for (String argName : argSegments) {
            String trimmed = argName.trim();
            if (trimmed.isEmpty()) continue;
            // Strip nested generic args for resolution (e.g., "Map<String, Integer>" -> "Map")
            int ltIdx = trimmed.indexOf('<');
            String baseName = ltIdx >= 0 ? trimmed.substring(0, ltIdx).trim() : trimmed;
            TypeInfo argType = resolveType(baseName, position);
            if (argType == null) {
                argType = TypeInfo.unresolved(baseName, baseName);
            }
            if (ltIdx >= 0 && argType.isResolved() && trimmed.endsWith(">")) {
                String nestedArgs = trimmed.substring(ltIdx + 1, trimmed.length() - 1).trim();
                if (!nestedArgs.isEmpty()) {
                    argType = parameterizeWithClause(argType, nestedArgs, position);
                }
            }
            typeArgs.add(argType);
        }
        if (!typeArgs.isEmpty()) {
            if (clauseTextOffset >= 0) {
                validateTypeArgBounds(baseType, typeArgs, argSegments, clauseTextOffset);
            }
            return baseType.parameterize(typeArgs);
        }
        return baseType;
    }
    
    private void validateTypeArgBounds(TypeInfo baseType, List<TypeInfo> typeArgs, 
                                       List<String> argSegments, int clauseTextOffset) {
        List<TypeParamInfo> typeParams = baseType.getTypeParams();
        if (typeParams == null || typeParams.isEmpty()) return;
        
        int count = Math.min(typeArgs.size(), typeParams.size());
        int charPos = 0;
        int segIdx = 0;
        for (int i = 0; i < count; i++) {
            TypeInfo argType = typeArgs.get(i);
            TypeParamInfo param = typeParams.get(i);
            
            String segment = segIdx < argSegments.size() ? argSegments.get(segIdx) : "";
            String trimmedSeg = segment.trim();
            int leading = 0;
            while (leading < segment.length() && Character.isWhitespace(segment.charAt(leading))) leading++;
            
            if (argType != null && argType.isResolved() && !trimmedSeg.isEmpty()) {
                TypeInfo primaryBound = param.getBoundTypeInfo();
                if (primaryBound != null && primaryBound.isResolved()) {
                    if (!TypeChecker.isTypeCompatible(primaryBound, argType)) {
                        int errorStart = clauseTextOffset + charPos + leading;
                        int errorEnd = errorStart + trimmedSeg.length();
                        addError(null, errorStart, errorEnd,
                                "Type argument " + argType.getSimpleName() + " is not within bounds of type parameter " + 
                                param.getName() + " (expected extends " + primaryBound.getSimpleName() + ")");
                    }
                }
                
                for (TypeInfo addBound : param.getAdditionalBoundTypes()) {
                    if (addBound == null || !addBound.isResolved()) continue;
                    if (!TypeChecker.isTypeCompatible(addBound, argType)) {
                        int errorStart = clauseTextOffset + charPos + leading;
                        int errorEnd = errorStart + trimmedSeg.length();
                        addError(null, errorStart, errorEnd,
                                "Type argument " + argType.getSimpleName() + " does not implement required interface " + 
                                addBound.getSimpleName());
                        break;
                    }
                }
            }
            
            charPos += segment.length() + 1;
            segIdx++;
        }
    }
    
    private void markTypeParamDeclarations(List<ScriptLine.Mark> marks, int afterNameEnd, ScriptTypeInfo typeInfo) {
        int ltPos = text.indexOf('<', afterNameEnd);
        if (ltPos < 0 || ltPos > afterNameEnd + 5) return;

        // Depth-aware matching to find the closing '>' (handles <T extends Comparable<T>>)
        int depth = 1, i = ltPos + 1;
        while (i < text.length() && depth > 0) {
            char c = text.charAt(i++);
            if (c == '<') depth++;
            else if (c == '>') depth--;
        }
        if (depth != 0) return;
        int gtPos = i - 1;

        String clause = text.substring(ltPos + 1, gtPos);

        // Split type params by commas at depth 0 to handle bounds with nested generics
        List<String> paramSegments = splitAtDepthZero(clause, ',');
        int segmentOffset = ltPos + 1;
        
        int paramIndex = 0;
        List<TypeParamInfo> declaredParams = typeInfo.getDeclaredTypeParams();
        
        for (String segment : paramSegments) {
            if (paramIndex >= declaredParams.size()) break;
            TypeParamInfo param = declaredParams.get(paramIndex++);

            int paramIdx = segment.indexOf(param.getName());
            if (paramIdx < 0) continue;

            int paramStart = segmentOffset + paramIdx;
            int paramEnd = paramStart + param.getName().length();
            marks.add(new ScriptLine.Mark(paramStart, paramEnd, TokenType.GENERIC_TYPE_PARAM, param));

            int extendsIdx = indexOfAtDepthZero(segment, "extends");
            if (extendsIdx >= 0) {
                int extendsStart = segmentOffset + extendsIdx;
                marks.add(new ScriptLine.Mark(extendsStart, extendsStart + 7, TokenType.KEYWORD));

                TypeInfo boundType = param.getBoundTypeInfo();
                if (boundType != null) {
                    String afterExtends = segment.substring(extendsIdx + 7).trim();
                    if (!afterExtends.isEmpty()) {
                        // Split on & at depth 0 to handle multiple bounds
                        List<String> boundSegments = splitAtDepthZero(afterExtends, '&');
                        int boundScanPos = extendsStart + 7;
                        while (boundScanPos < gtPos && Character.isWhitespace(text.charAt(boundScanPos))) boundScanPos++;
                        
                        for (int bi = 0; bi < boundSegments.size(); bi++) {
                            String boundSeg = boundSegments.get(bi).trim();
                            if (boundSeg.isEmpty()) continue;
                            
                            // Mark the '&' separator (for bounds after the first)
                            if (bi > 0) {
                                int ampSearch = boundScanPos - 1;
                                while (ampSearch >= 0 && text.charAt(ampSearch) != '&') ampSearch--;
                                if (ampSearch >= 0) {
                                    marks.add(new ScriptLine.Mark(ampSearch, ampSearch + 1, TokenType.DEFAULT));
                                }
                            }
                            
                            // Extract base type name (without generic args) for marking
                            int nameEnd = 0;
                            while (nameEnd < boundSeg.length() &&
                                   (Character.isJavaIdentifierPart(boundSeg.charAt(nameEnd)) || boundSeg.charAt(nameEnd) == '.')) {
                                nameEnd++;
                            }
                            if (nameEnd > 0) {
                                String bName = boundSeg.substring(0, nameEnd);
                                TypeInfo bType;
                                if (bi == 0) {
                                    bType = boundType;
                                } else {
                                    List<TypeInfo> addBounds = param.getAdditionalBoundTypes();
                                    bType = (bi - 1 < addBounds.size()) ? addBounds.get(bi - 1) : null;
                                }
                                if (bType != null) {
                                    marks.add(new ScriptLine.Mark(boundScanPos, boundScanPos + bName.length(), TokenType.getByType(bType), bType));
                                }
                                
                                if (nameEnd < boundSeg.length() && boundSeg.charAt(nameEnd) == '<') {
                                    int nestedStart = boundScanPos + nameEnd;
                                    markNestedTypeParamUsages(marks, nestedStart, gtPos, declaredParams);
                                }
                            }
                            
                            // Advance past this bound segment + the '&' separator
                            boundScanPos += boundSeg.length();
                            // Skip whitespace and '&' to reach the next bound
                            while (boundScanPos < gtPos && (Character.isWhitespace(text.charAt(boundScanPos)) || text.charAt(boundScanPos) == '&')) {
                                boundScanPos++;
                            }
                        }
                    }
                }
            }

            segmentOffset += segment.length() + 1; // +1 for the comma
        }
    }

    private void markNestedTypeParamUsages(List<ScriptLine.Mark> marks, int start, int limit, List<TypeParamInfo> typeParams) {
        Map<String, TypeParamInfo> paramMap = new HashMap<>();
        for (TypeParamInfo tp : typeParams) paramMap.put(tp.getName(), tp);

        Pattern ident = Pattern.compile("\\b([A-Za-z_][a-zA-Z0-9_]*)\\b");
        String region = text.substring(start, Math.min(limit, text.length()));
        Matcher m = ident.matcher(region);
        while (m.find()) {
            TypeParamInfo tp = paramMap.get(m.group(1));
            if (tp != null) {
                int absStart = start + m.start(1);
                marks.add(new ScriptLine.Mark(absStart, absStart + m.group(1).length(), TokenType.GENERIC_TYPE_PARAM, tp));
            }
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
        
        // Parse constructors (ClassName(params) { ... })
        // Constructor pattern: ClassName matches the script type name, no return type
        String typeName = scriptType.getSimpleName();
        Pattern constructorPattern = Pattern.compile(
                "\\b" + Pattern.quote(typeName) + "\\s*\\(([^)]*)\\)\\s*\\{");
        Matcher cm = constructorPattern.matcher(bodyText);
        while (cm.find()) {
            int absPos = bodyStart + 1 + cm.start();
            if (isExcluded(absPos)) continue;
            // Skip constructors found inside a nested type's body — they belong to the inner class
            if (isInsideNestedType(absPos, scriptType)) continue;
            
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
                    scriptType,
                    // Return type is the type itself
                    scriptType,
                    params,
                    fullDeclOffset,
                    typeOffset,
                    nameOffset,
                    constructorBodyStart,
                    constructorBodyEnd,
                    modifiers,
                    documentation);
            scriptType.addConstructor(constructorInfo);
        }

        // Parse enum constants (for enum types only)
        if (scriptType.getKind() == TypeInfo.Kind.ENUM) {
            List<EnumConstantInfo> constants = EnumConstantInfo.parseEnumConstants(
                    scriptType,
                    bodyText,
                    bodyStart + 1,
                    KEYWORD_PATTERN
            );

            for (EnumConstantInfo constant : constants) {
                scriptType.addEnumConstant(constant);
                if (constant.getConstructorCall() != null) {

                }
                //  methodCalls.add(constant.getConstructorCall());

            }
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

    /**
     * Parse method/function declarations - UNIFIED for both Java and JavaScript.
     * Stores results in the shared 'methods' list.
     * 
     * For Java: Parses "ReturnType methodName(params) { ... }"
     * For JavaScript: Parses "function funcName(params) { ... }" with hook type inference
     */
    private void parseMethodDeclarations() {
        if (isJavaScript()) {
            // JavaScript: function funcName(params) { ... }
            Pattern funcPattern = Pattern.compile("function\\s+(\\w+)\\s*\\(([^)]*)\\)\\s*\\{");
            Matcher m = funcPattern.matcher(text);
            
            while (m.find()) {
                if (isExcluded(m.start())) continue;
                
                String funcName = m.group(1);
                String paramList = m.group(2);
                
                int nameStart = m.start(1);
                int bodyStart = text.indexOf('{', m.end() - 1);
                int bodyEnd = findMatchingBrace(bodyStart);
                if (bodyEnd < 0) bodyEnd = text.length();
                
                // Extract documentation before this method
                String documentation = extractDocumentationBefore(m.start());
                
                // Check for JSDoc before the function declaration
                JSDocInfo jsDoc = jsDocParser.extractJSDocBefore(text, m.start());
                
                // For JS hooks, infer parameter types from registry
                // Use the script context's namespaces (e.g., ["IPlayerEvent", "IAnimationEvent"]) for lookup
                List<FieldInfo> params = new ArrayList<>();
                TypeInfo returnType = TypeInfo.fromPrimitive("void");
                List<String> namespaces = scriptContext != null ? scriptContext.getNamespaces() : Collections.singletonList("Global");

                boolean runtimeHook = false;
                if (ScriptHookController.Instance != null && scriptContext != null) {
                    String hookContext = scriptContext.hookContext;
                    if (hookContext != null && !hookContext.isEmpty()) {
                        runtimeHook = ScriptHookController.Instance.hasHook(hookContext, funcName);
                    }
                }

                if (runtimeHook) {
                    if (typeResolver.isJSHook(namespaces, funcName)) {
                        List<JSTypeRegistry.HookSignature> sigs = typeResolver.getJSHookSignatures(namespaces, funcName);
                        if (!sigs.isEmpty()) {
                            JSTypeRegistry.HookSignature sig = sigs.get(0);
                            documentation = sig.doc;

                            if (paramList != null && !paramList.trim().isEmpty()) {
                                String[] paramNames = paramList.split(",");
                                if (paramNames.length > 0) {
                                    String paramName = paramNames[0].trim();
                                    TypeInfo paramType = typeResolver.resolveJSType(sig.paramType);
                                    int paramStart = m.start(2) + paramList.indexOf(paramName);
                                    params.add(FieldInfo.parameter(paramName, paramType, paramStart, null));
                                }
                            }
                        }
                    } else {
                        if (paramList != null && !paramList.trim().isEmpty()) {
                            String[] paramNames = paramList.split(",");
                            if (paramNames.length > 0) {
                                String paramName = paramNames[0].trim();
                                int paramStart = m.start(2) + paramList.indexOf(paramName);
                                params.add(FieldInfo.parameter(paramName, TypeInfo.ANY, paramStart, null));
                            }
                        }
                    }
                } else {
                    // Non-hook function - use JSDoc param types if available, else 'any' type
                    if (paramList != null && !paramList.trim().isEmpty()) {
                        int paramOffset = m.start(2);
                        for (String p : paramList.split(",")) {
                            String pn = p.trim();
                            if (!pn.isEmpty()) {
                                int paramStart = paramOffset + paramList.indexOf(pn);
                                
                                // Check if JSDoc has a @param tag for this parameter
                                TypeInfo paramType = TypeInfo.ANY;
                                if (jsDoc != null) { 
                                    JSDocParamTag paramTag = jsDoc.getParamTag(pn);
                                    if (paramTag != null && paramTag.getTypeInfo() != null) {
                                        paramType = paramTag.getTypeInfo();
                                    }
                                }
                                
                                params.add(FieldInfo.parameter(pn, paramType, paramStart, null));
                            }
                        }
                    }
                    
                    // Use JSDoc @return type if available
                    if (jsDoc != null && jsDoc.hasReturnTag() && jsDoc.getReturnType() != null) {
                        returnType = jsDoc.getReturnType();
                    }
                }
                
                // Create MethodInfo and add to shared methods list
                MethodInfo methodInfo = MethodInfo.declaration(
                    funcName, null, returnType, params,
                    m.start(), nameStart, nameStart,
                    bodyStart, bodyEnd, 0, documentation
                );
                
                if(jsDoc!=null)
                    methodInfo.setJSDocInfo(jsDoc);
                
                methods.add(methodInfo);
            }
        } else {
            // Java: ReturnType methodName(params) { ... } or { ; }
            Pattern methodWithBody = Pattern.compile(
                    "\\b([a-zA-Z_][a-zA-Z0-9_<>\\[\\]]*)\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(([^)]*)\\)\\s*(\\{|;)");

            Matcher m = methodWithBody.matcher(text);
            while (m.find()) {
                String methodName = m.group(2);

                if (isExcluded(m.start()) || isKeyword(methodName))
                    continue;

                String returnType = m.group(1);

                if (returnType.equals("class") || returnType.equals("interface") || returnType.equals("enum") || returnType.equals("new")
                        || TypeResolver.isModifier(returnType)) {
                    continue;
                }
                
                String paramList = m.group(3);
                String delimiter = m.group(4);
                boolean hasBody = delimiter.equals("{");

                int bodyStart = !hasBody ? m.end() : text.indexOf('{', m.end() - 1);
                int bodyEnd = !hasBody ? m.end() : findMatchingBrace(bodyStart);
                if (bodyEnd < 0)
                    bodyEnd = text.length();

                // Extract documentation before this method
                String documentation = extractDocumentationBefore(m.start());

                // Extract modifiers by scanning backwards from match start
                int modifiers = extractModifiersBackwards(m.start() - 1, text);
                
                // Calculate offset positions
                int typeOffset = m.start(1);
                int nameOffset = m.start(2);
                int fullDeclOffset = findFullDeclarationStart(m.start(1), text);

                // Use findEnclosingScriptType (recursive descent) so methods declared
                // inside inner classes are attributed to the innermost containing type.
                ScriptTypeInfo scriptType = findEnclosingScriptType(bodyStart);
                
                // Parse parameters with their actual positions
                List<FieldInfo> params = parseParametersWithPositions(paramList, m.start(3));

                MethodInfo methodInfo = MethodInfo.declaration(
                        methodName,
                        scriptType,
                        resolveType(returnType, m.start(1)),
                        params,
                        fullDeclOffset,
                        typeOffset,
                        nameOffset,
                        bodyStart,
                        bodyEnd,
                        modifiers,
                        documentation
                );

                if (scriptType != null) {
                    scriptType.addMethod(methodInfo);
                } else
                    methods.add(methodInfo);

                // Validate the method (return statements, parameters) with type resolution
                String methodBodyText = bodyEnd > bodyStart + 1 ? text.substring(bodyStart + 1, bodyEnd) : "";
                methodInfo.validate(methodBodyText, hasBody, (expr, pos) -> resolveExpressionType(expr, pos));
            }

           
        }
        // Check for duplicate method declarations
        checkDuplicateMethods();
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
        // Group methods by their containing type (script class/interface or document root)
        // Methods are only duplicates if they're in the SAME containing type
        Map<TypeInfo, List<MethodInfo>> methodsByType = new HashMap<>();
        
        for (MethodInfo method : getAllMethods()) {
            TypeInfo containingType = method.getContainingType();
            methodsByType.computeIfAbsent(containingType, k -> new ArrayList<>()).add(method);
        }

        // Check each type for duplicate method signatures
        for (List<MethodInfo> typeMethods : methodsByType.values()) {
            Map<MethodSignature, List<MethodInfo>> signatureMap = new HashMap<>();

            for (MethodInfo method : typeMethods) {
                MethodSignature signature = method.getSignature();
                signatureMap.computeIfAbsent(signature, k -> new ArrayList<>()).add(method);
            }

            // Mark all methods with duplicate signatures within the same type
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

        // Collapse all whitespace runs to single spaces, tracking original positions via posMap.
        // posMap[normalizedIndex] = originalIndex, enabling correct absolute offsets after matching.
        StringBuilder normalized = new StringBuilder(paramList.length());
        int[] posMap = new int[paramList.length() + 1];
        boolean lastWasSpace = false;
        for (int i = 0; i < paramList.length(); i++) {
            char c = paramList.charAt(i);
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                if (!lastWasSpace) {
                    posMap[normalized.length()] = i;
                    normalized.append(' ');
                    lastWasSpace = true;
                }
            } else {
                posMap[normalized.length()] = i;
                normalized.append(c);
                lastWasSpace = false;
            }
        }
        posMap[normalized.length()] = paramList.length();
        String normalizedStr = normalized.toString();

        // Split by top-level commas (not inside < > brackets), then parse each parameter individually
        List<int[]> paramRanges = new ArrayList<>();
        int depth = 0;
        int start = 0;
        for (int i = 0; i < normalizedStr.length(); i++) {
            char c = normalizedStr.charAt(i);
            if (c == '<') depth++;
            else if (c == '>') depth--;
            else if (c == ',' && depth == 0) {
                paramRanges.add(new int[]{start, i});
                start = i + 1;
            }
        }
        paramRanges.add(new int[]{start, normalizedStr.length()});

        // Matches: type (with optional generics, arrays, dots) + optional varargs + whitespace + paramName
        Pattern paramPattern = Pattern.compile(
                "\\s*([a-zA-Z_][a-zA-Z0-9_.<>,? \\[\\]]*)(?:\\.{3})?\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*");

        for (int[] range : paramRanges) {
            String segment = normalizedStr.substring(range[0], range[1]);
            if (segment.trim().isEmpty()) continue;

            Matcher m = paramPattern.matcher(segment);
            if (!m.matches()) continue;

            String rawType = m.group(1).replaceAll("\\s+", "");
            String paramName = m.group(2);
            String between = segment.substring(m.end(1), m.start(2));
            boolean isVarArg = between.contains("...");

            TypeInfo typeInfo = resolveType(rawType, paramListStart);

            // m.start(2) is relative to segment; add range[0] to get position in normalizedStr, then posMap to original
            int normalizedNamePos = range[0] + m.start(2);
            int paramNameStart = paramListStart + posMap[normalizedNamePos];
            FieldInfo fieldInfo = FieldInfo.parameter(paramName, typeInfo, paramNameStart, null);
            fieldInfo.setVarArg(isVarArg);
            params.add(fieldInfo);
        }
        return params;
    }

    private ScopeInfo computeBlockScope(int bodyStart, int bodyEnd, int declarationOffset) {
        int end = Math.min(bodyEnd, text.length());
        int pos = Math.min(Math.max(declarationOffset, bodyStart), end);

        Deque<Integer> stack = new ArrayDeque<>();
        for (int i = bodyStart; i < pos && i < text.length(); i++) {
            if (isExcluded(i)) {
                continue;
            }
            char c = text.charAt(i);
            if (c == '{') {
                stack.push(i);
            } else if (c == '}') {
                if (!stack.isEmpty()) {
                    stack.pop();
                }
            }
        }

        if (stack.isEmpty()) {
            ScopeInfo parenScope = computeParenStatementScope(bodyStart, bodyEnd, pos);
            if (parenScope != null) {
                return parenScope;
            }
            return new ScopeInfo(bodyStart, bodyEnd, false, "method");
        }

        int openBrace = stack.peek();
        int closeBrace = findMatchingBrace(openBrace);
        if (closeBrace < 0 || closeBrace > bodyEnd) {
            return new ScopeInfo(bodyStart, bodyEnd, false, "method");
        }
        return new ScopeInfo(openBrace + 1, closeBrace, false, "block");
    }

    private ScopeInfo computeParenStatementScope(int bodyStart, int bodyEnd, int position) {
        int openParen = findEnclosingParenStart(bodyStart, position);
        if (openParen < 0) {
            return null;
        }

        int closeParen = findMatchingParen(openParen, bodyEnd);
        if (closeParen < 0 || position > closeParen) {
            return null;
        }

        String keyword = readKeywordBefore(openParen);
        if (!"for".equals(keyword) && !"catch".equals(keyword)) {
            return null;
        }

        int after = skipWhitespaceAndExcluded(closeParen + 1, bodyEnd);
        if (after < 0 || after >= bodyEnd) {
            return null;
        }

        if (text.charAt(after) == '{') {
            int closeBrace = findMatchingBrace(after);
            if (closeBrace > 0) {
                return new ScopeInfo(openParen, closeBrace, false, "block");
            }
            return null;
        }

        int stmtEnd = findStatementEnd(after, bodyEnd);
        if (stmtEnd > after) {
            return new ScopeInfo(openParen, stmtEnd, false, "block");
        }
        return null;
    }

    int skipWhitespaceAndExcluded(int pos, int limit) {
        int i = Math.max(pos, 0);
        int max = Math.min(limit, text.length());
        while (i < max) {
            if (isExcluded(i)) {
                i++;
                continue;
            }
            if (!Character.isWhitespace(text.charAt(i))) {
                return i;
            }
            i++;
        }
        return -1;
    }

    int findStatementEnd(int start, int limit) {
        int max = Math.min(limit, text.length());
        int parenDepth = 0;
        int bracketDepth = 0;
        for (int i = start; i < max; i++) {
            if (isExcluded(i)) {
                continue;
            }
            char c = text.charAt(i);
            if (c == '(') parenDepth++;
            else if (c == ')') parenDepth = Math.max(0, parenDepth - 1);
            else if (c == '[') bracketDepth++;
            else if (c == ']') bracketDepth = Math.max(0, bracketDepth - 1);
            else if (c == '{') {
                int closeBrace = findMatchingBrace(i);
                return closeBrace > 0 ? closeBrace : -1;
            } else if (c == ';' && parenDepth == 0 && bracketDepth == 0) {
                return i + 1;
            }
        }
        return -1;
    }

    int findEnclosingParenStart(int min, int position) {
        int depth = 0;
        for (int i = Math.min(position - 1, text.length() - 1); i >= min; i--) {
            if (isExcluded(i)) {
                continue;
            }
            char c = text.charAt(i);
            if (c == ')') {
                depth++;
            } else if (c == '(') {
                if (depth == 0) {
                    return i;
                }
                depth--;
            }
        }
        return -1;
    }

    int findMatchingParen(int openParenIndex, int limit) {
        if (openParenIndex < 0 || openParenIndex >= text.length()) {
            return -1;
        }
        int max = Math.min(limit, text.length());
        int depth = 0;
        for (int i = openParenIndex; i < max; i++) {
            if (isExcluded(i)) {
                continue;
            }
            char c = text.charAt(i);
            if (c == '(') depth++;
            else if (c == ')') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    String readKeywordBefore(int position) {
        int i = position - 1;
        while (i >= 0) {
            if (isExcluded(i)) {
                i--;
                continue;
            }
            if (!Character.isWhitespace(text.charAt(i))) {
                break;
            }
            i--;
        }
        if (i < 0) {
            return "";
        }
        int end = i + 1;
        while (i >= 0 && Character.isJavaIdentifierPart(text.charAt(i))) {
            i--;
        }
        int start = i + 1;
        if (start >= end) {
            return "";
        }
        return text.substring(start, end);
    }

    private FieldInfo pickVisibleLocal(List<FieldInfo> candidates, int position) {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        FieldInfo best = null;
        for (FieldInfo f : candidates) {
            if (f == null) {
                continue;
            }
            if (!f.isVisibleAt(position)) {
                continue;
            }
            if (best == null || f.getDeclarationOffset() > best.getDeclarationOffset()) {
                best = f;
            }
        }
        return best;
    }

    private FieldInfo pickVisibleTopLevelLocal(String name, int position) {
        return pickVisibleLocal(topLevelLocals.get(name), position);
    }

    void addTopLevelLocal(FieldInfo fieldInfo) {
        if (fieldInfo == null) {
            return;
        }
        topLevelLocals.computeIfAbsent(fieldInfo.getName(), k -> new ArrayList<>()).add(fieldInfo);
    }

    private Collection<FieldInfo> getVisibleTopLevelLocals(int position) {
        List<FieldInfo> visible = new ArrayList<>();
        for (List<FieldInfo> candidates : topLevelLocals.values()) {
            FieldInfo best = pickVisibleLocal(candidates, position);
            if (best != null) {
                visible.add(best);
            }
        }
        return visible;
    }

    Collection<FieldInfo> getTopLevelLocalCandidates() {
        List<FieldInfo> all = new ArrayList<>();
        for (List<FieldInfo> fields : topLevelLocals.values()) {
            all.addAll(fields);
        }
        return all;
    }

    /**
     * Parse local variables inside methods/functions - UNIFIED for both Java and JavaScript.
     * Stores results in the shared 'methodLocals' map (methodOffset -> varName -> FieldInfo).
     * 
     * For Java: Parses "Type varName = expr;" or "Type varName;"
     * For JavaScript: Parses "var/let/const varName = expr;" with type inference
     */
    private void parseLocalVariables() {
        List<MethodInfo> allMethodsAndConstructors = getAllMethods();
        allMethodsAndConstructors.addAll(getAllConstructors());
        for (MethodInfo method : allMethodsAndConstructors) {
            Map<String, List<FieldInfo>> locals = new HashMap<>();
            methodLocals.put(method.getDeclarationOffset(), locals);

            int bodyStart = method.getBodyStart();
            int bodyEnd = method.getBodyEnd();
            if (bodyStart < 0 || bodyEnd <= bodyStart) continue;

            String bodyText = text.substring(bodyStart, Math.min(bodyEnd, text.length()));
            
            if (isJavaScript()) {
                // JavaScript: var/let/const varName = expr;
                Pattern varPattern = Pattern.compile("(var|let|const)\\s+(\\w+)(?:\\s*(=))?");
                Matcher m = varPattern.matcher(bodyText);
                
                while (m.find()) {
                    int absPos = bodyStart + m.start(2);
                    if (isExcluded(absPos)) continue;

                    int afterVar = m.end(2);
                    if (afterVar < bodyText.length()) {
                        String rest = bodyText.substring(afterVar);
                        if (rest.length() > 0 && java.util.regex.Pattern.compile("^\\s+(?:in|of)\\s").matcher(rest).find()) continue;
                    }

                    boolean insideInner = false;
                    for (InnerCallableScope scope : innerScopes) {
                        if (scope.containsPosition(absPos)) {
                            insideInner = true;
                            break;
                        }
                    }
                    if (insideInner) continue;
                    
                    String kind = m.group(1);
                    String varName = m.group(2);
                    String initializer = null;
                    int initializerStart = -1;
                    int initializerEnd = -1;
                    if (m.group(3) != null) {
                        initializerStart = skipSegmentWhitespace(bodyText, m.end(3));
                        initializerEnd = findJsInitializerEnd(bodyText, initializerStart, true);
                        if (initializerEnd > initializerStart) {
                            initializer = bodyText.substring(initializerStart, initializerEnd).trim();
                            if (initializer.isEmpty()) {
                                initializer = null;
                            }
                        }
                    }
                    
                    // Check for JSDoc type annotation before the declaration
                    int absStart = bodyStart + m.start();
                    JSDocInfo jsDoc = jsDocParser.extractJSDocBefore(text, absStart);
                    
                    TypeInfo typeInfo = null;
                    
                    // Priority 1: JSDoc @type takes precedence
                    if (jsDoc != null && jsDoc.hasTypeTag()) {
                        typeInfo = jsDoc.getDeclaredType();
                    }
                    
                    // Priority 2: Infer type from initializer if no JSDoc type
                    if (typeInfo == null && initializer != null && !initializer.isEmpty()) {
                        TypeInfo inferred = resolveExpressionType(initializer, bodyStart + initializerStart);
                        if (!TypeInfo.NULL.equals(inferred)) {
                            typeInfo = inferred;
                        }
                    }
                    
                    // Priority 3: Use "any" type for uninitialized variables
                    if (typeInfo == null) {
                        typeInfo = TypeInfo.ANY;
                    }
                    
                    int initStart = -1, initEnd = -1;
                    if (m.group(3) != null) {
                        initStart = bodyStart + m.start(3);
                        initEnd = (initializerEnd >= 0) ? bodyStart + initializerEnd : bodyStart + m.end(3);
                    }
                    
                    FieldInfo fieldInfo = FieldInfo.localField(varName, typeInfo, absPos, method, initStart, initEnd, 0);
                    ScopeInfo scopeInfo;
                    if ("var".equals(kind)) {
                        scopeInfo = new ScopeInfo(bodyStart, bodyEnd, false, "method");
                    } else {
                        scopeInfo = computeBlockScope(bodyStart, bodyEnd, absPos);
                    }
                    fieldInfo.setScopeInfo(scopeInfo);
                    
                    if(jsDoc != null)
                        fieldInfo.setJSDocInfo(jsDoc);
                    // Check for duplicate
                    List<FieldInfo> existing = locals.get(varName);
                    if (existing != null && !existing.isEmpty()) {
                        boolean dup = false;
                        if ("let".equals(kind) || "const".equals(kind)) {
                            for (FieldInfo prev : existing) {
                                ScopeInfo prevScope = prev.getScopeInfo();
                                if (prevScope != null && prevScope.startOffset == scopeInfo.startOffset && prevScope.endOffset == scopeInfo.endOffset) {
                                    dup = true;
                                    break;
                                }
                            }
                        }
                        if (dup || globalFields.containsKey(varName)) {
                            AssignmentInfo dupError = AssignmentInfo.duplicateDeclaration(
                                varName, absPos, absPos + varName.length(),
                                "Variable '" + varName + "' is already defined in the scope");
                            dupError.setScopeInfo(scopeInfo);
                            declarationErrors.add(dupError);
                            continue;
                        }
                        if ("var".equals(kind)) {
                            continue;
                        }
                    } else if (globalFields.containsKey(varName)) {
                        AssignmentInfo dupError = AssignmentInfo.duplicateDeclaration(
                            varName, absPos, absPos + varName.length(),
                            "Variable '" + varName + "' is already defined in the scope");
                        dupError.setScopeInfo(scopeInfo);
                        declarationErrors.add(dupError);
                        continue;
                    }

                    locals.computeIfAbsent(varName, k -> new ArrayList<>()).add(fieldInfo);

                    // --- Multi-declarator continuation for JS ---
                    // For "var a=1, b='x', c=player;" the regex only matched 'a'.
                    // Scan forward from the end of this declarator for comma-separated
                    // continuation names (b, c, ...) that share the same var/let/const keyword.
                    final String fKind = kind;
                    final MethodInfo fMethod = method;
                    final int fBodyStart = bodyStart;
                    final int fBodyEnd = bodyEnd;
                    final Map<String, List<FieldInfo>> fLocals = locals;
                    int contScanStart = MultiDeclaratorParser.jsDeclaratorScanStart(bodyText, m.end(2), m.group(3) != null, initializerEnd);
                    MultiDeclaratorParser.scanJSContinuationDeclarators(this, bodyText, contScanStart, bodyStart,
                        (cVarName, cAbsNamePos, cInit, cAbsInitStart, cAbsInitEnd) -> {
                            if (isExcluded(cAbsNamePos)) return;
                            boolean cInsideInner = false;
                            for (InnerCallableScope sc : innerScopes) {
                                if (sc.containsPosition(cAbsNamePos)) { cInsideInner = true; break; }
                            }
                            if (cInsideInner) return;

                            TypeInfo cType = null;
                            if (cInit != null && !cInit.isEmpty()) {
                                TypeInfo inferred = resolveExpressionType(cInit, cAbsInitStart);
                                if (!TypeInfo.NULL.equals(inferred)) cType = inferred;
                            }
                            if (cType == null) cType = TypeInfo.ANY;

                            FieldInfo cField = FieldInfo.localField(cVarName, cType, cAbsNamePos, fMethod,
                                    cAbsInitStart, cAbsInitEnd, 0);
                            ScopeInfo cScope;
                            if ("var".equals(fKind)) {
                                cScope = new ScopeInfo(fBodyStart, fBodyEnd, false, "method");
                            } else {
                                cScope = computeBlockScope(fBodyStart, fBodyEnd, cAbsNamePos);
                            }
                            cField.setScopeInfo(cScope);

                            if (fLocals.containsKey(cVarName) || globalFields.containsKey(cVarName)) {
                                AssignmentInfo dupErr = AssignmentInfo.duplicateDeclaration(
                                    cVarName, cAbsNamePos, cAbsNamePos + cVarName.length(),
                                    "Variable '" + cVarName + "' is already defined in the scope");
                                dupErr.setScopeInfo(cScope);
                                declarationErrors.add(dupErr);
                                return;
                            }
                            fLocals.computeIfAbsent(cVarName, k -> new ArrayList<>()).add(cField);
                        });
                }
            } else {
                // Java: Type varName = expr; or Type varName;
                Matcher m = FIELD_DECL_PATTERN.matcher(bodyText);
                while (m.find()) {
                    String typeName = m.group(1);
                    String varName = m.group(2);
                    String delimiter = m.group(3);
                    
                    int absPos = bodyStart + m.start(2);
                    if (isExcluded(absPos)) continue;
                    
                    // FIELD_DECL_PATTERN's type group allows newlines, so a match can
                    // start inside a line comment (e.g., "// Integer\nString str =")
                    // where group(2) lands on the next line outside the comment.
                    int absTypeStart = bodyStart + m.start(1);
                    if (isExcluded(absTypeStart)) continue;

                    // --- FIX: Detect greedy regex consuming commas into the type name ---
                    // FIELD_DECL_PATTERN allows commas in its type character class (for generics).
                    // This causes "int x, y, z = 10;" to match as type="int x, y," name="z" delim="=".
                    // When a depth-0 comma exists in typeName, the regex swallowed extra declarators.
                    // We extract the real type and first variable, then let the continuation scanner
                    // handle ALL variables (including the regex-matched one).
                    int commaInType = MultiDeclaratorParser.findFirstDepthZeroComma(typeName);
                    if (commaInType >= 0) {
                        String realTypeName = MultiDeclaratorParser.extractRealTypeFromGreedyMatch(typeName, commaInType);
                        String firstVar = MultiDeclaratorParser.extractFirstVarFromGreedyMatch(typeName, commaInType);
                        int firstVarAbsPos = MultiDeclaratorParser.findFirstVarPosition(
                                bodyText, m.start(1), typeName, commaInType) + bodyStart;

                        // Skip control flow keywords on the real type
                        if (realTypeName.equals("return") || realTypeName.equals("if") || realTypeName.equals("while") ||
                            realTypeName.equals("for") || realTypeName.equals("switch") || realTypeName.equals("catch") ||
                            realTypeName.equals("new") || realTypeName.equals("throw")) {
                            continue;
                        }

                        int enclosingParen1 = findEnclosingParenStart(bodyStart, firstVarAbsPos);
                        if (enclosingParen1 >= 0) continue;

                        int greedyModifiers = parseModifiers(realTypeName);
                        TypeInfo greedyTypeInfo = resolveType(realTypeName, firstVarAbsPos);

                        // Register the first variable (extracted from the greedy type group)
                        FieldInfo firstField = FieldInfo.localField(firstVar, greedyTypeInfo, firstVarAbsPos,
                                method, -1, -1, greedyModifiers);
                        ScopeInfo firstScope = computeBlockScope(bodyStart, bodyEnd, firstVarAbsPos);
                        firstField.setScopeInfo(firstScope);

                        List<FieldInfo> existingFirst = locals.get(firstVar);
                        if (existingFirst != null) {
                            boolean dup = false;
                            for (FieldInfo prev : existingFirst) {
                                ScopeInfo prevScope = prev.getScopeInfo();
                                if (prevScope != null && prevScope.containsPosition(firstVarAbsPos)) {
                                    dup = true;
                                    break;
                                }
                            }
                            if (dup || globalFields.containsKey(firstVar)) {
                                AssignmentInfo dupErr = AssignmentInfo.duplicateDeclaration(
                                    firstVar, firstVarAbsPos, firstVarAbsPos + firstVar.length(),
                                    "Variable '" + firstVar + "' is already defined in the scope");
                                dupErr.setScopeInfo(firstScope);
                                declarationErrors.add(dupErr);
                            } else {
                                locals.computeIfAbsent(firstVar, k -> new ArrayList<>()).add(firstField);
                            }
                        } else if (globalFields.containsKey(firstVar)) {
                            AssignmentInfo dupErr = AssignmentInfo.duplicateDeclaration(
                                firstVar, firstVarAbsPos, firstVarAbsPos + firstVar.length(),
                                "Variable '" + firstVar + "' is already defined in the scope");
                            dupErr.setScopeInfo(firstScope);
                            declarationErrors.add(dupErr);
                        } else {
                            locals.computeIfAbsent(firstVar, k -> new ArrayList<>()).add(firstField);
                        }

                        // Scan continuations from the first depth-0 comma in the type group.
                        // This will pick up all remaining variables (y, z, etc.) including the
                        // regex-matched varName which was the last one consumed by the greedy match.
                        final TypeInfo greedySharedType = greedyTypeInfo;
                        final int greedySharedMod = greedyModifiers;
                        final MethodInfo fMethod3 = method;
                        final Map<String, List<FieldInfo>> fLocals3 = locals;
                        int greedyScanStart = m.start(1) + commaInType;
                        MultiDeclaratorParser.scanJavaContinuationDeclarators(bodyText, greedyScanStart, bodyStart,
                            (cVarName, cAbsNamePos, cAbsInitStart, cAbsInitEnd) -> {
                                if (isExcluded(cAbsNamePos)) return;
                                if (findEnclosingParenStart(bodyStart, cAbsNamePos) >= 0) return;
                                FieldInfo cField = FieldInfo.localField(cVarName, greedySharedType, cAbsNamePos,
                                        fMethod3, cAbsInitStart, cAbsInitEnd, greedySharedMod);
                                ScopeInfo cScope = computeBlockScope(bodyStart, bodyEnd, cAbsNamePos);
                                cField.setScopeInfo(cScope);

                                if (fLocals3.containsKey(cVarName) || globalFields.containsKey(cVarName)) {
                                    AssignmentInfo dupErr = AssignmentInfo.duplicateDeclaration(
                                        cVarName, cAbsNamePos, cAbsNamePos + cVarName.length(),
                                        "Variable '" + cVarName + "' is already defined in the scope");
                                    dupErr.setScopeInfo(cScope);
                                    declarationErrors.add(dupErr);
                                    return;
                                }
                                fLocals3.computeIfAbsent(cVarName, k -> new ArrayList<>()).add(cField);
                            });
                        continue;
                    }
                    
                    // Skip control flow keywords
                    if (typeName.equals("return") || typeName.equals("if") || typeName.equals("while") ||
                        typeName.equals("for") || typeName.equals("switch") || typeName.equals("catch") ||
                        typeName.equals("new") || typeName.equals("throw")) {
                        continue;
                    }

                    int enclosingParen = findEnclosingParenStart(bodyStart, absPos);
                    if (enclosingParen >= 0 && "for".equals(readKeywordBefore(enclosingParen))) continue;

                    int modifiers = parseModifiers(typeName);

                    TypeInfo typeInfo;
                    if ((typeName.equals("var") || typeName.equals("let") || typeName.equals("const")) 
                            && delimiter.equals("=")) {
                        int rhsStart = bodyStart + m.end();
                        typeInfo = inferTypeFromExpression(rhsStart);
                    } else {
                        // Use position-aware resolution so inner class type names resolve in scope
                        typeInfo = resolveType(typeName, bodyStart + m.start(2));
                    }
                    
                    int initStart = -1, initEnd = -1;
                    if ("=".equals(delimiter)) {
                        initStart = bodyStart + m.start(3);
                        int searchPos = bodyStart + m.end(3);
                        int depth = 0;
                        int angleDepth = 0;
                        while (searchPos < text.length()) {
                            char c = text.charAt(searchPos);
                            if (c == '(' || c == '[' || c == '{') depth++;
                            else if (c == ')' || c == ']' || c == '}') depth--;
                            else if (c == '<') angleDepth++;
                            else if (c == '>') angleDepth--;
                            else if ((c == ';' || c == ',') && depth == 0 && angleDepth == 0) {
                                initEnd = searchPos;
                                break;
                            }
                            searchPos++;
                        }
                    }
                    
                    int declPos = bodyStart + m.start(2);
                    FieldInfo fieldInfo = FieldInfo.localField(varName, typeInfo, declPos, method, initStart, initEnd, modifiers);
                    ScopeInfo scopeInfo = computeBlockScope(bodyStart, bodyEnd, declPos);
                    fieldInfo.setScopeInfo(scopeInfo);
                    
                    List<FieldInfo> existing = locals.get(varName);
                    if (existing != null) {
                        boolean dup = false;
                        for (FieldInfo prev : existing) {
                            ScopeInfo prevScope = prev.getScopeInfo();
                            if (prevScope != null && prevScope.containsPosition(declPos)) {
                                dup = true;
                                break;
                            }
                        }
                        if (dup || globalFields.containsKey(varName)) {
                            AssignmentInfo dupError = AssignmentInfo.duplicateDeclaration(
                                varName, declPos, declPos + varName.length(),
                                "Variable '" + varName + "' is already defined in the scope");
                            dupError.setScopeInfo(scopeInfo);
                            declarationErrors.add(dupError);
                            continue;
                        }
                    } else if (globalFields.containsKey(varName)) {
                        AssignmentInfo dupError = AssignmentInfo.duplicateDeclaration(
                            varName, declPos, declPos + varName.length(),
                            "Variable '" + varName + "' is already defined in the scope");
                        dupError.setScopeInfo(scopeInfo);
                        declarationErrors.add(dupError);
                        continue;
                    }

                    locals.computeIfAbsent(varName, k -> new ArrayList<>()).add(fieldInfo);

                    // --- Multi-declarator continuation for Java ---
                    // For "int x = 1, y, z = 3;" FIELD_DECL_PATTERN matched "int x =" (or "int x,").
                    // Scan forward for comma-separated continuation declarators (y, z)
                    // that share the same declared type from the first match.
                    final TypeInfo sharedType = typeInfo;
                    final int sharedModifiers = modifiers;
                    final MethodInfo fMethod2 = method;
                    final Map<String, List<FieldInfo>> fLocals2 = locals;
                    int javaScanStart = MultiDeclaratorParser.javaDeclaratorScanStart(bodyText, delimiter, m.end(3), 
                            initEnd >= 0 ? initEnd - bodyStart : -1);
                    if (javaScanStart >= 0) {
                        MultiDeclaratorParser.scanJavaContinuationDeclarators(bodyText, javaScanStart, bodyStart,
                            (cVarName, cAbsNamePos, cAbsInitStart, cAbsInitEnd) -> {
                                if (isExcluded(cAbsNamePos)) return;
                                FieldInfo cField = FieldInfo.localField(cVarName, sharedType, cAbsNamePos,
                                        fMethod2, cAbsInitStart, cAbsInitEnd, sharedModifiers);
                                ScopeInfo cScope = computeBlockScope(bodyStart, bodyEnd, cAbsNamePos);
                                cField.setScopeInfo(cScope);

                                if (fLocals2.containsKey(cVarName) || globalFields.containsKey(cVarName)) {
                                    AssignmentInfo dupErr = AssignmentInfo.duplicateDeclaration(
                                        cVarName, cAbsNamePos, cAbsNamePos + cVarName.length(),
                                        "Variable '" + cVarName + "' is already defined in the scope");
                                    dupErr.setScopeInfo(cScope);
                                    declarationErrors.add(dupErr);
                                    return;
                                }
                                fLocals2.computeIfAbsent(cVarName, k -> new ArrayList<>()).add(cField);
                            });
                    }
                }
            }
        }
        
        // Also parse locals inside inner callable scopes
        for (InnerCallableScope scope : innerScopes) {
            parseLocalVariablesInScope(scope);
        }

        new LoopVariableParser(this, text).parse(methodLocals);
    }

    private void parseLocalVariablesInScope(InnerCallableScope scope) {
        int start = scope.getBodyStart();
        int end = scope.getBodyEnd();
        
        if (start < 0 || end <= start) return;
        
        if (isJavaScript()) {
            parseJSLocalsInRange(start, end, scope);
        } else {
            parseJavaLocalsInRange(start, end, scope);
        }
    }
    
    /**
     * Parse Java local variable declarations in the range [start, end).
     * Pattern: Type varName = ... or var varName = ...
     */
    private void parseJavaLocalsInRange(int start, int end, InnerCallableScope scope) {
        String rangeText = text.substring(start, Math.min(end, text.length()));
        
        Matcher m = FIELD_DECL_PATTERN.matcher(rangeText);
        while (m.find()) {
            int declPos = start + m.start();
            if (declPos < start || declPos >= end) continue;
            if (isExcluded(declPos)) continue;
            
            String typeNameRaw = m.group(1);
            String varName = m.group(2);
            String delimiter = m.group(3);

            // --- FIX: Detect greedy regex consuming commas into the type name ---
            // Same issue as the other call sites: FIELD_DECL_PATTERN greedily consumes
            // multi-declarator commas into group(1). Handle by extracting the real type
            // and first variable, then scanning continuations from the first comma.
            int commaInType = MultiDeclaratorParser.findFirstDepthZeroComma(typeNameRaw);
            if (commaInType >= 0) {
                String realTypeNameRaw = MultiDeclaratorParser.extractRealTypeFromGreedyMatch(typeNameRaw, commaInType);
                String firstVar = MultiDeclaratorParser.extractFirstVarFromGreedyMatch(typeNameRaw, commaInType);
                int firstVarAbsPos = MultiDeclaratorParser.findFirstVarPosition(
                        rangeText, m.start(1), typeNameRaw, commaInType) + start;

                // Skip control flow keywords on the real type
                if (realTypeNameRaw.equals("return") || realTypeNameRaw.equals("if") || realTypeNameRaw.equals("while") ||
                    realTypeNameRaw.equals("for") || realTypeNameRaw.equals("switch") || realTypeNameRaw.equals("catch") ||
                    realTypeNameRaw.equals("new") || realTypeNameRaw.equals("throw")) {
                    continue;
                }

                String greedyTypeName = stripModifiers(realTypeNameRaw);
                int greedyModifiers = parseModifiers(realTypeNameRaw);
                TypeInfo greedyVarType = resolveType(greedyTypeName, firstVarAbsPos);

                // Register the first variable
                FieldInfo firstField = FieldInfo.localField(firstVar, greedyVarType, firstVarAbsPos,
                        null, -1, -1, greedyModifiers);
                scope.addLocal(firstVar, firstField);

                // Scan continuations from the first depth-0 comma
                final TypeInfo greedySharedType = greedyVarType;
                final int greedySharedMod = greedyModifiers;
                final InnerCallableScope fScope3 = scope;
                int greedyScanStart = m.start(1) + commaInType;
                MultiDeclaratorParser.scanJavaContinuationDeclarators(rangeText, greedyScanStart, start,
                    (cVarName, cAbsNamePos, cAbsInitStart, cAbsInitEnd) -> {
                        if (isExcluded(cAbsNamePos)) return;
                        FieldInfo cField = FieldInfo.localField(cVarName, greedySharedType, cAbsNamePos,
                                null, cAbsInitStart, cAbsInitEnd, greedySharedMod);
                        fScope3.addLocal(cVarName, cField);
                    });
                continue;
            }
            
            // Skip control flow keywords
            if (typeNameRaw.equals("return") || typeNameRaw.equals("if") || typeNameRaw.equals("while") ||
                typeNameRaw.equals("for") || typeNameRaw.equals("switch") || typeNameRaw.equals("catch") ||
                typeNameRaw.equals("new") || typeNameRaw.equals("throw")) {
                continue;
            }
            
            String typeName = stripModifiers(typeNameRaw);
            int modifiers = parseModifiers(typeNameRaw);
            
            TypeInfo varType = null;
            if (!typeName.equals("var") && !typeName.equals("let") && !typeName.equals("const")) {
                varType = resolveType(typeName);
            } else if (delimiter.equals("=")) {
                // Infer type from initializer
                int rhsStart = start + m.end();
                varType = inferTypeFromExpression(rhsStart);
            }
            
            int initStart = -1, initEnd = -1;
            if ("=".equals(delimiter)) {
                initStart = start + m.start(3);
                int searchPos = start + m.end(3);
                int depth = 0;
                int angleDepth = 0;
                while (searchPos < end) {
                    char c = text.charAt(searchPos);
                    if (c == '(' || c == '[' || c == '{') depth++;
                    else if (c == ')' || c == ']' || c == '}') depth--;
                    else if (c == '<') angleDepth++;
                    else if (c == '>') angleDepth--;
                    else if ((c == ';' || c == ',') && depth == 0 && angleDepth == 0) {
                        initEnd = searchPos;
                        break;
                    }
                    searchPos++;
                }
            }
            
            int absPos = start + m.start(2);
            FieldInfo localVar = FieldInfo.localField(varName, varType, absPos, null, initStart, initEnd, modifiers);
            scope.addLocal(varName, localVar);

            // Multi-declarator continuation for Java: "int x, y = 2, z;"
            final TypeInfo sharedType = varType;
            final int sharedModifiers = modifiers;
            final InnerCallableScope fScope2 = scope;
            int javaScanStart = MultiDeclaratorParser.javaDeclaratorScanStart(rangeText, delimiter, m.end(3),
                    initEnd >= 0 ? initEnd - start : -1);
            if (javaScanStart >= 0) {
                MultiDeclaratorParser.scanJavaContinuationDeclarators(rangeText, javaScanStart, start,
                    (cVarName, cAbsNamePos, cAbsInitStart, cAbsInitEnd) -> {
                        if (isExcluded(cAbsNamePos)) return;
                        FieldInfo cField = FieldInfo.localField(cVarName, sharedType, cAbsNamePos,
                                null, cAbsInitStart, cAbsInitEnd, sharedModifiers);
                        fScope2.addLocal(cVarName, cField);
                    });
            }
        }
    }
    
    /**
     * Parse JS local variable declarations in the range [start, end).
     * Pattern: var/let/const varName = ...
     */
    private void parseJSLocalsInRange(int start, int end, InnerCallableScope scope) {
        String rangeText = text.substring(start, Math.min(end, text.length()));
        
        Pattern varPattern = Pattern.compile("(?:var|let|const)\\s+(\\w+)(?:\\s*(=))?");
        Matcher m = varPattern.matcher(rangeText);
        
        while (m.find()) {
            int declPos = start + m.start();
            if (declPos < start || declPos >= end) continue;
            if (isExcluded(declPos)) continue;

            int afterVar = m.end(1);
            if (afterVar < rangeText.length()) {
                String rest = rangeText.substring(afterVar);
                if (rest.length() > 0 && java.util.regex.Pattern.compile("^\\s+(?:in|of)\\s").matcher(rest).find()) continue;
            }
            
            String varName = m.group(1);
            String initializer = null;
            int initializerStart = -1;
            int initializerEnd = -1;
            if (m.group(2) != null) {
                initializerStart = skipSegmentWhitespace(rangeText, m.end(2));
                // Use comma-aware termination so multi-declarator RHS is bounded correctly
                initializerEnd = findJsInitializerEnd(rangeText, initializerStart, true);
                if (initializerEnd > initializerStart) {
                    initializer = rangeText.substring(initializerStart, initializerEnd).trim();
                    if (initializer.isEmpty()) {
                        initializer = null;
                    }
                }
            }
            
            // Check for JSDoc type annotation
            JSDocInfo jsDoc = jsDocParser.extractJSDocBefore(text, declPos);
            
            TypeInfo varType = null;
            
            // Priority 1: JSDoc @type
            if (jsDoc != null && jsDoc.hasTypeTag()) {
                varType = jsDoc.getDeclaredType();
            }
            
            // Priority 2: Infer from initializer
            if (varType == null && initializer != null && !initializer.isEmpty()) {
                TypeInfo inferred = resolveExpressionType(initializer, start + initializerStart);
                if (!TypeInfo.NULL.equals(inferred)) {
                    varType = inferred;
                }
            }
            
            // Priority 3: Use "any" type
            if (varType == null) {
                varType = TypeInfo.ANY;
            }
            
            int initStart = -1, initEnd = -1;
            if (m.group(2) != null) {
                initStart = start + m.start(2);
                initEnd = (initializerEnd >= 0) ? start + initializerEnd : start + m.end(2);
            }
            
            int absPos = start + m.start(1);
            FieldInfo localVar = FieldInfo.localField(varName, varType, absPos, null, initStart, initEnd, 0);
            
            if (jsDoc != null) {
                localVar.setJSDocInfo(jsDoc);
            }
            
            scope.addLocal(varName, localVar);

            // Multi-declarator continuation: scan for "var a=1, b, c=3"
            final InnerCallableScope fScope = scope;
            int contScanStart = MultiDeclaratorParser.jsDeclaratorScanStart(rangeText, m.end(1), m.group(2) != null, initializerEnd);
            MultiDeclaratorParser.scanJSContinuationDeclarators(this, rangeText, contScanStart, start,
                (cVarName, cAbsNamePos, cInit, cAbsInitStart, cAbsInitEnd) -> {
                    if (isExcluded(cAbsNamePos)) return;
                    TypeInfo cType = null;
                    if (cInit != null && !cInit.isEmpty()) {
                        TypeInfo inferred = resolveExpressionType(cInit, cAbsInitStart);
                        if (!TypeInfo.NULL.equals(inferred)) cType = inferred;
                    }
                    if (cType == null) cType = TypeInfo.ANY;
                    FieldInfo cField = FieldInfo.localField(cVarName, cType, cAbsNamePos, null,
                            cAbsInitStart, cAbsInitEnd, 0);
                    fScope.addLocal(cVarName, cField);
                });
        }
    }

    int skipSegmentWhitespace(String source, int pos) {
        while (pos < source.length() && Character.isWhitespace(source.charAt(pos))) {
            pos++;
        }
        return pos;
    }

    /**
     * Scans forward from {@code rhsStart} to find where a JavaScript initializer expression ends.
     *
     * Unlike Java, JS has no mandatory semicolons, so this method implements a conservative
     * ASI (Automatic Semicolon Insertion) heuristic:
     * <ul>
     *   <li>A {@code ;} at depth 0 always terminates the expression.</li>
     *   <li>A newline while inside open parens, brackets, or braces does NOT terminate —
     *       the expression continues on the next line (e.g. multi-line object literals,
     *       chained calls).</li>
     *   <li>A newline at depth 0 terminates only if {@link #shouldContinueJsInitializer}
     *       says the line break is NOT a continuation. Continuation is inferred from
     *       the current line ending with an operator/open-bracket, or the next line
     *       starting with {@code .}, {@code ?.}, or {@code [}.</li>
     * </ul>
     *
     * String literals (single/double-quoted) and both comment forms ({@code //} and
     * {@code /* ... *\/}) are skipped so their contents never affect depth tracking.
     *
     * @param source   the full source text to scan
     * @param rhsStart the index to begin scanning (typically the character after {@code =})
     * @return the index of the terminating character (a {@code ;} or newline), or
     *         {@code source.length()} if the source ends before a terminator is found.
     *         Returns {@code rhsStart} unchanged if {@code rhsStart} is out of bounds.
     */
    int findJsInitializerEnd(String source, int rhsStart) {
        return findJsInitializerEnd(source, rhsStart, false);
    }

    int findJsInitializerEnd(String source, int rhsStart, boolean stopAtTopLevelComma) {
        if (rhsStart < 0 || rhsStart >= source.length()) {
            return rhsStart;
        }

        int pos = rhsStart;
        int lineExprStart = rhsStart;

        int parenDepth = 0;
        int bracketDepth = 0;
        int braceDepth = 0;

        boolean inString = false;
        char stringChar = 0;
        boolean inLineComment = false;
        boolean inBlockComment = false;

        while (pos < source.length()) {
            char c = source.charAt(pos);
            char next = (pos + 1 < source.length()) ? source.charAt(pos + 1) : 0;

            if (inLineComment) {
                if (c == '\n' || c == '\r') {
                    inLineComment = false;
                } else {
                    pos++;
                    continue;
                }
            }

            if (inBlockComment) {
                if (c == '*' && next == '/') {
                    inBlockComment = false;
                    pos += 2;
                    continue;
                }
                pos++;
                continue;
            }

            if (inString) {
                if (c == '\\') {
                    pos += (pos + 1 < source.length()) ? 2 : 1;
                    continue;
                }
                if (c == stringChar) {
                    inString = false;
                }
                pos++;
                continue;
            }

            if (c == '/' && next == '/') {
                inLineComment = true;
                pos += 2;
                continue;
            }
            if (c == '/' && next == '*') {
                inBlockComment = true;
                pos += 2;
                continue;
            }

            if (c == '"' || c == '\'') {
                inString = true;
                stringChar = c;
                pos++;
                continue;
            }

            if (c == '(') {
                parenDepth++;
            } else if (c == ')') {
                parenDepth = Math.max(0, parenDepth - 1);
            } else if (c == '[') {
                bracketDepth++;
            } else if (c == ']') {
                bracketDepth = Math.max(0, bracketDepth - 1);
            } else if (c == '{') {
                braceDepth++;
            } else if (c == '}') {
                braceDepth = Math.max(0, braceDepth - 1);
            }

            if (c == ';' && parenDepth == 0 && bracketDepth == 0 && braceDepth == 0) {
                return pos;
            }

            if (stopAtTopLevelComma && c == ',' && parenDepth == 0 && bracketDepth == 0 && braceDepth == 0) {
                return pos;
            }

            if (c == '\n' || c == '\r') {
                int lineBreakPos = pos;
                int nextPos = pos + 1;
                if (c == '\r' && nextPos < source.length() && source.charAt(nextPos) == '\n') {
                    nextPos++;
                }

                if (parenDepth != 0 || bracketDepth != 0 || braceDepth != 0) {
                    pos = nextPos;
                    lineExprStart = nextPos;
                    continue;
                }

                int currentLineEnd = lineBreakPos;
                while (currentLineEnd > lineExprStart && Character.isWhitespace(source.charAt(currentLineEnd - 1))) {
                    currentLineEnd--;
                }
                String currentLineExpr = source.substring(lineExprStart, currentLineEnd);

                int nextExprStart = nextPos;
                while (nextExprStart < source.length()) {
                    char n = source.charAt(nextExprStart);
                    if (n == ' ' || n == '\t') {
                        nextExprStart++;
                        continue;
                    }
                    if (n == ';') {
                        return nextExprStart;
                    }
                    if (n == '\n' || n == '\r') {
                        nextExprStart++;
                        if (n == '\r' && nextExprStart < source.length() && source.charAt(nextExprStart) == '\n') {
                            nextExprStart++;
                        }
                        continue;
                    }
                    break;
                }

                if (nextExprStart >= source.length()) {
                    return source.length();
                }

                int nextLineEnd = nextExprStart;
                while (nextLineEnd < source.length()) {
                    char n = source.charAt(nextLineEnd);
                    if (n == '\n' || n == '\r' || n == ';') {
                        break;
                    }
                    nextLineEnd++;
                }
                String nextLineExpr = source.substring(nextExprStart, nextLineEnd).trim();

                if (!shouldContinueJsInitializer(currentLineExpr, nextLineExpr)) {
                    return lineBreakPos;
                }

                pos = nextExprStart;
                lineExprStart = nextExprStart;
                continue;
            }
            pos++;
        }
        return source.length();
    }

    private boolean shouldContinueJsInitializer(String currentLineExpr, String nextLineExpr) {
        if (nextLineExpr.isEmpty()) {
            return false;
        }

        if (nextLineExpr.startsWith(".") || nextLineExpr.startsWith("?.") || nextLineExpr.startsWith("[")) {
            return true;
        }

        String current = currentLineExpr.trim();
        if (current.isEmpty()) {
            return false;
        }

        char tail = current.charAt(current.length() - 1);
        return tail == '+' || tail == '-' || tail == '*' || tail == '/' || tail == '%' ||
                tail == '&' || tail == '|' || tail == '^' || tail == '!' || tail == '=' ||
                tail == '<' || tail == '>' || tail == '?' || tail == ':' || tail == ',' ||
                tail == '.' || tail == '(' || tail == '[' || tail == '{';
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
                return resolveType(typeName, typeStart);
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
        if ("this".equals(firstIdent)) {
            currentType = resolveThisType(identStart);
        }

        TypeInfo typeCheck = resolveType(firstIdent, identStart);
        if (currentType == null && typeCheck != null && typeCheck.isResolved()) {
            // Static access like Event.player or scriptType.field
            currentType = typeCheck;
        } else if (currentType == null) {
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
                    
                    // Check if this is a synthetic type with dynamic return type resolver (like Java.type())
                    SyntheticType syntheticType = null;
                    if (isJavaScript() && typeResolver.isSyntheticType(firstIdent)) {
                        syntheticType = typeResolver.getSyntheticType(firstIdent);
                    }
                    
                    if (syntheticType != null && syntheticType.hasMethod(segment)) {
                        SyntheticMethod synMethod = syntheticType.getMethod(segment);
                        if (synMethod != null) {
                            // Try dynamic resolution first
                            String[] strArgs = TypeResolver.parseStringArguments(argsText);
                            TypeInfo dynamicType = synMethod.resolveReturnType(strArgs);
                            if (dynamicType != null) {
                                currentType = dynamicType;
                            } else {
                                // Fall back to static return type
                                currentType = synMethod.getReturnTypeInfo();
                            }
                        }
                    } else {
                        MethodInfo methodInfo = currentType.getBestMethodOverload(segment, argTypes);
                        currentType = (methodInfo != null) ? methodInfo.getReturnType() : null;
                    }
                    
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

    /**
     * Parse inner callable scopes (lambdas and JS function expressions).
     * These are NOT added to the methods list - they're resolution-only scopes.
     */
    private void parseInnerCallableScopes() {
        // For Java: detect (params) -> expr and (params) -> { ... } and param -> expr
        // For JS: detect function(params) { ... } that are expressions (not declarations)
        
        if (isJavaScript()) {
            parseJSFunctionExpressions();
            parseJSArrowFunctions();
            parseJSShorthandMethods();
        } else {
            parseJavaLambdas();
        }
        
        // Sort by header start and set up parent relationships
        innerScopes.sort((a, b) -> Integer.compare(a.getHeaderStart(), b.getHeaderStart()));
        setupScopeParents();
    }

    private void parseJavaLambdas() {
        // Pattern: (params) -> or identifier ->
        // Look for -> that's not inside strings/comments
        String arrowPattern = "->";
        int pos = 0;
        while ((pos = text.indexOf(arrowPattern, pos)) >= 0) {
            if (isExcluded(pos)) {
                pos += 2;
                continue;
            }
            
            // Found a potential lambda arrow
            int arrowPos = pos;
            
            // Look backwards to find the parameter list
            int headerStart = findLambdaHeaderStart(arrowPos);
            if (headerStart < 0) {
                pos += 2;
                continue;
            }
            
            // Look forward to find the body end
            int bodyStart = arrowPos + 2;
            // Skip whitespace after ->
            while (bodyStart < text.length() && Character.isWhitespace(text.charAt(bodyStart))) {
                bodyStart++;
            }
            
            if (bodyStart >= text.length()) {
                pos += 2;
                continue;
            }
            
            int bodyEnd;
            if (text.charAt(bodyStart) == '{') {
                // Block lambda
                bodyEnd = findMatchingBrace(bodyStart);
                if (bodyEnd < 0) bodyEnd = text.length();
                else bodyEnd++; // Include the closing brace
            } else {
                // Expression lambda - find end of expression
                bodyEnd = findLambdaExpressionEnd(bodyStart);
            }
            
            InnerCallableScope scope = new InnerCallableScope(
                InnerCallableScope.Kind.JAVA_LAMBDA,
                headerStart,
                arrowPos + 2,
                bodyStart,
                bodyEnd
            );
            
            // Parse parameters (will be typed later during resolution)
            parseLambdaParameters(scope, headerStart, arrowPos);
            
            innerScopes.add(scope);
            pos = bodyEnd;
        }
    }

    private int findLambdaHeaderStart(int arrowPos) {
        // Work backwards from arrow to find start of parameter list
        int pos = arrowPos - 1;
        
        // Skip whitespace
        while (pos >= 0 && Character.isWhitespace(text.charAt(pos))) {
            pos--;
        }
        
        if (pos < 0) return -1;
        
        if (text.charAt(pos) == ')') {
            // Parenthesized params: find matching (
            int depth = 1;
            pos--;
            while (pos >= 0 && depth > 0) {
                if (isExcluded(pos)) {
                    pos--;
                    continue;
                }
                char c = text.charAt(pos);
                if (c == ')') depth++;
                else if (c == '(') depth--;
                pos--;
            }
            return pos + 1; // Position of '('
        } else if (Character.isJavaIdentifierPart(text.charAt(pos))) {
            // Single identifier param (no parens): identifier ->
            while (pos >= 0 && Character.isJavaIdentifierPart(text.charAt(pos))) {
                pos--;
            }
            return pos + 1;
        }
        
        return -1;
    }

    private int findLambdaExpressionEnd(int start) {
        // Find end of expression lambda body
        // Ends at: ; , ) ] } (at depth 0) or end of text
        int depth = 0;
        int angleDepth = 0;
        int pos = start;
        boolean inString = false;
        char stringChar = 0;
        
        while (pos < text.length()) {
            char c = text.charAt(pos);
            
            // Handle strings
            if (!inString && (c == '"' || c == '\'')) {
                inString = true;
                stringChar = c;
                pos++;
                continue;
            }
            if (inString) {
                if (c == stringChar && (pos == 0 || text.charAt(pos - 1) != '\\')) {
                    inString = false;
                }
                pos++;
                continue;
            }
            
            // Track nesting
            if (c == '(' || c == '[' || c == '{') depth++;
            else if (c == ')' || c == ']' || c == '}') {
                if (depth == 0) return pos;
                depth--;
            }
            else if (c == '<') angleDepth++;
            else if (c == '>') angleDepth = Math.max(0, angleDepth - 1);
            else if ((c == ';' || c == ',') && depth == 0 && angleDepth == 0) {
                return pos;
            }
            
            pos++;
        }
        
        return pos;
    }

    private void parseLambdaParameters(InnerCallableScope scope, int headerStart, int arrowPos) {
        String headerText = text.substring(headerStart, arrowPos).trim();
        
        if (headerText.startsWith("(") && headerText.endsWith(")")) {
            // Parenthesized: (a, b) or (Type a, Type b)
            String paramsText = headerText.substring(1, headerText.length() - 1).trim();
            if (!paramsText.isEmpty()) {
                String[] params = paramsText.split(",");
                int offset = headerStart + 1; // After '('
                for (String param : params) {
                    param = param.trim();
                    if (param.isEmpty()) continue;
                    
                    // Find position in original text
                    int paramOffset = text.indexOf(param, offset);
                    if (paramOffset < 0) paramOffset = offset;
                    
                    String[] parts = param.split("\\s+");
                    String paramName;
                    TypeInfo paramType = null;
                    
                    if (parts.length >= 2) {
                        // Typed: Type name
                        paramName = parts[parts.length - 1];
                        String typeName = parts[parts.length - 2];
                        boolean isVarArg = typeName.endsWith("...");
                        if (isVarArg) typeName = typeName.substring(0, typeName.length() - 3);
                        paramType = resolveType(typeName);
                        int nameOffset = text.indexOf(paramName, paramOffset);
                        if (nameOffset < 0) nameOffset = paramOffset;
                        FieldInfo paramInfo = FieldInfo.parameter(paramName, paramType, nameOffset, null);
                        paramInfo.setVarArg(isVarArg);
                        scope.addParameter(paramInfo);
                    } else {
                        // Untyped: just name (type will be inferred from expected FI)
                        paramName = parts[0];
                        int nameOffset = text.indexOf(paramName, paramOffset);
                        if (nameOffset < 0) nameOffset = paramOffset;
                        FieldInfo paramInfo = FieldInfo.parameter(paramName, paramType, nameOffset, null);
                        scope.addParameter(paramInfo);
                    }
                    
                    offset = paramOffset + param.length();
                }
            }
        } else {
            // Single identifier: x ->
            String paramName = headerText;
            int nameOffset = text.indexOf(paramName, headerStart);
            if (nameOffset < 0) nameOffset = headerStart;
            
            FieldInfo paramInfo = FieldInfo.parameter(paramName, null, nameOffset, null);
            scope.addParameter(paramInfo);
        }
    }

    private void parseJSFunctionExpressions() {
        // Pattern: function(params) { ... } or function name(params) { ... }
        // But NOT function declarations at top level (those are already in methods)
        Pattern funcExprPattern = Pattern.compile("function\\s*(?:\\w+)?\\s*\\(([^)]*)\\)\\s*\\{");
        Matcher m = funcExprPattern.matcher(text);
        
        while (m.find()) {
            int start = m.start();
            if (isExcluded(start)) continue;
            
            // Check if this is a function expression (not a declaration)
            // A function declaration is at statement level; expression is inside parens, after =, etc.
            if (isFunctionDeclaration(start)) continue;
            
            int headerStart = start;
            int headerEnd = m.end() - 1; // Position of '{'
            int bodyStart = m.end() - 1;
            int bodyEnd = findMatchingBrace(bodyStart);
            if (bodyEnd < 0) bodyEnd = text.length();
            else bodyEnd++; // Include closing brace
            
            InnerCallableScope scope = new InnerCallableScope(
                InnerCallableScope.Kind.JS_FUNCTION_EXPR,
                headerStart,
                headerEnd,
                bodyStart,
                bodyEnd
            );
            
            // Parse parameters
            String paramsText = m.group(1).trim();
            if (!paramsText.isEmpty()) {
                String[] params = paramsText.split(",");
                int offset = m.start(1);
                for (String param : params) {
                    param = param.trim();
                    if (param.isEmpty()) continue;
                    
                    int nameOffset = text.indexOf(param, offset);
                    if (nameOffset < 0) nameOffset = offset;
                    
                    // JS params don't have types by default; will infer from expected FI
                    FieldInfo paramInfo = FieldInfo.parameter(param, TypeInfo.ANY, nameOffset, null);
                    scope.addParameter(paramInfo);
                    
                    offset = nameOffset + param.length();
                }
            }
            
            innerScopes.add(scope);
        }
    }

    private void parseJSArrowFunctions() {
        String arrowToken = "=>";
        int pos = 0;
        while ((pos = text.indexOf(arrowToken, pos)) >= 0) {
            if (isExcluded(pos)) {
                pos += 2;
                continue;
            }

            int arrowPos = pos;
            int headerStart = findArrowFunctionHeaderStart(arrowPos);
            if (headerStart < 0) {
                pos += 2;
                continue;
            }

            int bodyStart = arrowPos + 2;
            while (bodyStart < text.length() && Character.isWhitespace(text.charAt(bodyStart))) {
                bodyStart++;
            }

            if (bodyStart >= text.length()) {
                pos += 2;
                continue;
            }

            int bodyEnd;
            if (text.charAt(bodyStart) == '{') {
                bodyEnd = findMatchingBrace(bodyStart);
                if (bodyEnd < 0) bodyEnd = text.length();
                else bodyEnd++;
            } else {
                bodyEnd = findLambdaExpressionEnd(bodyStart);
            }

            InnerCallableScope scope = new InnerCallableScope(
                InnerCallableScope.Kind.JS_ARROW_FUNC,
                headerStart,
                arrowPos + 2,
                bodyStart,
                bodyEnd
            );

            parseArrowFunctionParameters(scope, headerStart, arrowPos);
            innerScopes.add(scope);
            pos = bodyEnd;
        }
    }

    private int findArrowFunctionHeaderStart(int arrowPos) {
        int pos = arrowPos - 1;
        while (pos >= 0 && Character.isWhitespace(text.charAt(pos))) {
            pos--;
        }
        if (pos < 0) return -1;

        if (text.charAt(pos) == ')') {
            int depth = 1;
            pos--;
            while (pos >= 0 && depth > 0) {
                if (isExcluded(pos)) { pos--; continue; }
                char c = text.charAt(pos);
                if (c == ')') depth++;
                else if (c == '(') depth--;
                pos--;
            }
            return pos + 1;
        } else if (Character.isJavaIdentifierPart(text.charAt(pos))) {
            while (pos >= 0 && Character.isJavaIdentifierPart(text.charAt(pos))) {
                pos--;
            }
            return pos + 1;
        }
        return -1;
    }

    private void parseArrowFunctionParameters(InnerCallableScope scope, int headerStart, int arrowPos) {
        String headerText = text.substring(headerStart, arrowPos).trim();

        if (headerText.startsWith("(") && headerText.endsWith(")")) {
            String paramsText = headerText.substring(1, headerText.length() - 1).trim();
            if (!paramsText.isEmpty()) {
                String[] params = paramsText.split(",");
                int offset = headerStart + 1;
                for (String param : params) {
                    param = param.trim();
                    if (param.isEmpty()) continue;

                    int nameOffset = text.indexOf(param, offset);
                    if (nameOffset < 0) nameOffset = offset;

                    FieldInfo paramInfo = FieldInfo.parameter(param, TypeInfo.ANY, nameOffset, null);
                    scope.addParameter(paramInfo);
                    offset = nameOffset + param.length();
                }
            }
        } else {
            if (ObjectLiteralParser.isSimpleIdentifier(headerText)) {
                int nameOffset = text.indexOf(headerText, headerStart);
                if (nameOffset < 0) nameOffset = headerStart;
                FieldInfo paramInfo = FieldInfo.parameter(headerText, TypeInfo.ANY, nameOffset, null);
                scope.addParameter(paramInfo);
            }
        }
    }

    private void parseJSShorthandMethods() {
        Pattern shorthandPattern = Pattern.compile("(\\w+)\\s*\\(([^)]*)\\)\\s*\\{");
        Matcher m = shorthandPattern.matcher(text);

        while (m.find()) {
            int start = m.start();
            if (isExcluded(start)) continue;

            if (!ObjectLiteralParser.isInsideObjectLiteral(start,this)) continue;

            String methodName = m.group(1);
            if (TypeChecker.isJavaScriptKeyword(methodName)) continue;

            int headerStart = m.start(1);
            int bodyStart = m.end() - 1;
            int bodyEnd = findMatchingBrace(bodyStart);
            if (bodyEnd < 0) bodyEnd = text.length();
            else bodyEnd++;

            InnerCallableScope scope = new InnerCallableScope(
                InnerCallableScope.Kind.JS_SHORTHAND_METHOD,
                headerStart,
                bodyStart,
                bodyStart,
                bodyEnd
            );

            String paramsText = m.group(2).trim();
            if (!paramsText.isEmpty()) {
                String[] params = paramsText.split(",");
                int offset = m.start(2);
                for (String param : params) {
                    param = param.trim();
                    if (param.isEmpty()) continue;

                    int nameOffset = text.indexOf(param, offset);
                    if (nameOffset < 0) nameOffset = offset;

                    FieldInfo paramInfo = FieldInfo.parameter(param, TypeInfo.ANY, nameOffset, null);
                    scope.addParameter(paramInfo);
                    offset = nameOffset + param.length();
                }
            }

            innerScopes.add(scope);
        }
    }
    
    private boolean isFunctionDeclaration(int funcStart) {
        // A function declaration is a statement, check context:
        // - If preceded by = or ( or , or : or [ it's an expression
        // - If at line start (possibly with whitespace) or after { or ; it's a declaration
        
        int pos = funcStart - 1;
        while (pos >= 0 && Character.isWhitespace(text.charAt(pos)) && text.charAt(pos) != '\n') {
            pos--;
        }
        
        if (pos < 0) return true; // Start of file = declaration
        
        char prev = text.charAt(pos);
        // These characters indicate it's an expression, not a declaration
        if (prev == '=' || prev == '(' || prev == ',' || prev == ':' || prev == '[' || prev == '?') {
            return false;
        }
        // After newline + whitespace could be declaration
        if (prev == '\n' || prev == '{' || prev == ';' || prev == '}') {
            return true;
        }
        
        return true; // Default to declaration to avoid false positives in inner scopes
    }

    private void setupScopeParents() {
        // For each scope, find its immediate parent (smallest enclosing scope)
        for (int i = 0; i < innerScopes.size(); i++) {
            InnerCallableScope scope = innerScopes.get(i);
            InnerCallableScope parent = null;
            
            for (int j = 0; j < innerScopes.size(); j++) {
                if (i == j) continue;
                InnerCallableScope candidate = innerScopes.get(j);
                
                // Check if candidate contains scope
                if (candidate.getBodyStart() < scope.getHeaderStart() && 
                    candidate.getBodyEnd() > scope.getFullEnd()) {
                    // candidate contains scope
                    if (parent == null || 
                        (candidate.getBodyStart() > parent.getBodyStart())) {
                        // candidate is smaller (more immediate) than current parent
                        parent = candidate;
                    }
                }
            }
            
            scope.setParentScope(parent);
        }
    }

    /**
     * Parse global fields/variables (outside methods) - UNIFIED for both Java and JavaScript.
     * Stores results in the shared 'globalFields' map.
     * 
     * For Java: Parses "[modifiers] Type fieldName [= expr];"
     * For JavaScript: Parses "var/let/const varName [= expr];" outside of functions
     */
    private void parseGlobalFields() {
        if (isJavaScript()) {
            // JavaScript: var/let/const varName = expr; (outside functions)
            Pattern varPattern = Pattern.compile("(?:var|let|const)\\s+(\\w+)(?:\\s*(=))?");
            Matcher m = varPattern.matcher(text);
            
            while (m.find()) {
                int position = m.start(1);
                if (isExcluded(position)) continue;
                
                // Check if inside a function - if so, skip (handled by parseLocalVariables)
                boolean insideMethod = false;
                for (MethodInfo method : getAllMethods()) {
                    if (method.containsPosition(position)) {
                        insideMethod = true;
                        break;
                    }
                }
                if (!insideMethod) {
                    for (MethodInfo constructor : getAllConstructors()) {
                        if (constructor.containsPosition(position)) {
                            insideMethod = true;
                            break;
                        }
                    }
                }
                if (insideMethod) continue;

                boolean insideInner = false;
                for (InnerCallableScope scope : innerScopes) {
                    if (scope.containsPosition(position)) {
                        insideInner = true;
                        break;
                    }
                }
                if (insideInner) continue;

                int afterVar = m.end(1);
                if (afterVar < text.length() && LoopVariableParser.FOR_IN_OF_LOOKAHEAD.matcher(text.substring(afterVar)).find()) continue;

                String varName = m.group(1);
                String initializer = null;
                int initializerStart = -1;
                int initializerEnd = -1;
                if (m.group(2) != null) {
                    initializerStart = skipSegmentWhitespace(text, m.end(2));
                    // Use comma-aware termination for multi-declarator support
                    initializerEnd = findJsInitializerEnd(text, initializerStart, true);
                    if (initializerEnd > initializerStart) {
                        initializer = text.substring(initializerStart, initializerEnd).trim();
                        if (initializer.isEmpty()) {
                            initializer = null;
                        }
                    }
                }
                
                // First, check for JSDoc type annotation
                String documentation = extractDocumentationBefore(m.start());
                JSDocInfo jsDoc = jsDocParser.extractJSDocBefore(text, m.start());
                
                TypeInfo typeInfo = null;
                
                // Priority 1: JSDoc @type takes precedence
                if (jsDoc != null && jsDoc.hasTypeTag()) {
                    typeInfo = jsDoc.getDeclaredType();
                }
                
                // Priority 2: Infer from initializer if no JSDoc type
                if (typeInfo == null && initializer != null && !initializer.isEmpty()) {
                    TypeInfo inferred = resolveExpressionType(initializer, initializerStart);
                    if (!TypeInfo.NULL.equals(inferred)) {
                        typeInfo = inferred;
                    }
                }
                
                // Priority 3: Use "any" type for uninitialized variables
                if (typeInfo == null) {
                    typeInfo = TypeInfo.ANY;
                }
                
                int initStart = -1, initEnd = -1;
                if (m.group(2) != null) {
                    initStart = m.start(2);
                    initEnd = (initializerEnd >= 0) ? initializerEnd : m.end(2);
                }

                FieldInfo fieldInfo = FieldInfo.globalField(varName, typeInfo, position, documentation, initStart, initEnd, 0);
                
                if(jsDoc != null)
                    fieldInfo.setJSDocInfo(jsDoc);
                
                if (globalFields.containsKey(varName)) {
                    AssignmentInfo dupError = AssignmentInfo.duplicateDeclaration(
                        varName, position, position + varName.length(),
                        "Variable '" + varName + "' is already defined in the scope");
                    declarationErrors.add(dupError);
                } else {
                    globalFields.put(varName, fieldInfo);
                }

                // Multi-declarator continuation for global JS: "var a=1, b='x', c;"
                int contScanStart = MultiDeclaratorParser.jsDeclaratorScanStart(text, m.end(1), m.group(2) != null, initializerEnd);
                MultiDeclaratorParser.scanJSContinuationDeclarators(this, text, contScanStart, 0,
                    (cVarName, cAbsNamePos, cInit, cAbsInitStart, cAbsInitEnd) -> {
                        if (isExcluded(cAbsNamePos)) return;
                        boolean cInsideMethod = false;
                        for (MethodInfo method : getAllMethods()) {
                            if (method.containsPosition(cAbsNamePos)) { cInsideMethod = true; break; }
                        }
                        if (!cInsideMethod) {
                            for (MethodInfo constructor : getAllConstructors()) {
                                if (constructor.containsPosition(cAbsNamePos)) { cInsideMethod = true; break; }
                            }
                        }
                        if (cInsideMethod) return;
                        boolean cInsideInner = false;
                        for (InnerCallableScope sc : innerScopes) {
                            if (sc.containsPosition(cAbsNamePos)) { cInsideInner = true; break; }
                        }
                        if (cInsideInner) return;

                        TypeInfo cType = null;
                        if (cInit != null && !cInit.isEmpty()) {
                            TypeInfo inferred = resolveExpressionType(cInit, cAbsInitStart);
                            if (!TypeInfo.NULL.equals(inferred)) cType = inferred;
                        }
                        if (cType == null) cType = TypeInfo.ANY;

                        FieldInfo cField = FieldInfo.globalField(cVarName, cType, cAbsNamePos, null,
                                cAbsInitStart, cAbsInitEnd, 0);
                        if (globalFields.containsKey(cVarName)) {
                            AssignmentInfo dupErr = AssignmentInfo.duplicateDeclaration(
                                cVarName, cAbsNamePos, cAbsNamePos + cVarName.length(),
                                "Variable '" + cVarName + "' is already defined in the scope");
                            declarationErrors.add(dupErr);
                        } else {
                            globalFields.put(cVarName, cField);
                        }
                    });
            }
        } else {
            // Java: [modifiers] Type fieldName [= expr];
            Matcher m = FIELD_DECL_PATTERN.matcher(text);
            while (m.find()) {
                String typeNameRaw = m.group(1);
                String fieldName = m.group(2);
                String delimiter = m.group(3);
                int position = m.start(2);
                
                if (isExcluded(position))
                    continue;
                
                // Guard against cross-line matches that start inside a comment
                if (isExcluded(m.start(1)))
                    continue;

                // --- FIX: Detect greedy regex consuming commas into the type name ---
                // Same issue as parseLocalVariables: FIELD_DECL_PATTERN greedily consumes
                // multi-declarator commas into group(1). When a depth-0 comma exists in
                // typeNameRaw, extract the real type and first variable, then let the
                // continuation scanner handle ALL variables.
                int commaInType = MultiDeclaratorParser.findFirstDepthZeroComma(typeNameRaw);
                if (commaInType >= 0) {
                    String realTypeNameRaw = MultiDeclaratorParser.extractRealTypeFromGreedyMatch(typeNameRaw, commaInType);
                    String firstVar = MultiDeclaratorParser.extractFirstVarFromGreedyMatch(typeNameRaw, commaInType);
                    int firstVarAbsPos = MultiDeclaratorParser.findFirstVarPosition(
                            text, m.start(1), typeNameRaw, commaInType);

                    if (isExcluded(firstVarAbsPos)) continue;

                    int enclosingParen1 = findEnclosingParenStart(0, firstVarAbsPos);
                    if (enclosingParen1 >= 0 && "for".equals(readKeywordBefore(enclosingParen1))) continue;

                    int greedyModifiers = parseModifiers(realTypeNameRaw);
                    String greedyTypeName = stripModifiers(realTypeNameRaw);

                    // Use findEnclosingScriptType (recursive descent) so inner class fields
                    // are attributed to the innermost containing type, not just the first
                    // HashMap match (which could be the outer class).
                    ScriptTypeInfo greedyContainingType = findEnclosingScriptType(firstVarAbsPos);

                    boolean greedyInsideMethod = false;
                    if (greedyContainingType != null && isInsideNestedMethod(firstVarAbsPos,
                            greedyContainingType.getBodyStart(), greedyContainingType.getBodyEnd()))
                        greedyInsideMethod = true;
                    else {
                        for (MethodInfo method : getAllMethods()) {
                            if (method.containsPosition(firstVarAbsPos)) { greedyInsideMethod = true; break; }
                        }
                        if (!greedyInsideMethod) {
                            for (MethodInfo constructor : getAllConstructors()) {
                                if (constructor.containsPosition(firstVarAbsPos)) { greedyInsideMethod = true; break; }
                            }
                        }
                    }
                    if (greedyInsideMethod) continue;

                    String greedyDocumentation = extractDocumentationBefore(m.start());
                    TypeInfo greedyTypeInfo = resolveType(greedyTypeName, firstVarAbsPos);

                    // Register the first variable (no initializer since the regex consumed it into the type group)
                    FieldInfo firstField = FieldInfo.globalField(firstVar, greedyTypeInfo, firstVarAbsPos,
                            greedyDocumentation, -1, -1, greedyModifiers);
                    if (greedyContainingType != null) {
                        greedyContainingType.addField(firstField);
                    } else if (globalFields.containsKey(firstVar)) {
                        AssignmentInfo dupErr = AssignmentInfo.duplicateDeclaration(
                            firstVar, firstVarAbsPos, firstVarAbsPos + firstVar.length(),
                            "Variable '" + firstVar + "' is already defined in the scope");
                        declarationErrors.add(dupErr);
                    } else {
                        globalFields.put(firstVar, firstField);
                    }

                    // Scan continuations from the first depth-0 comma in the type group
                    final TypeInfo greedySharedType = greedyTypeInfo;
                    final int greedySharedMod = greedyModifiers;
                    final ScriptTypeInfo fGreedyContType = greedyContainingType;
                    int greedyScanStart = m.start(1) + commaInType;
                    MultiDeclaratorParser.scanJavaContinuationDeclarators(text, greedyScanStart, 0,
                        (cVarName, cAbsNamePos, cAbsInitStart, cAbsInitEnd) -> {
                            if (isExcluded(cAbsNamePos)) return;
                            boolean cInsideMethod = false;
                            if (fGreedyContType != null && isInsideNestedMethod(cAbsNamePos,
                                    fGreedyContType.getBodyStart(), fGreedyContType.getBodyEnd()))
                                cInsideMethod = true;
                            else {
                                for (MethodInfo method : getAllMethods()) {
                                    if (method.containsPosition(cAbsNamePos)) { cInsideMethod = true; break; }
                                }
                                if (!cInsideMethod) {
                                    for (MethodInfo constructor : getAllConstructors()) {
                                        if (constructor.containsPosition(cAbsNamePos)) { cInsideMethod = true; break; }
                                    }
                                }
                            }
                            if (cInsideMethod) return;

                            FieldInfo cField = FieldInfo.globalField(cVarName, greedySharedType, cAbsNamePos,
                                    null, cAbsInitStart, cAbsInitEnd, greedySharedMod);
                            if (fGreedyContType != null) {
                                fGreedyContType.addField(cField);
                            } else if (globalFields.containsKey(cVarName)) {
                                AssignmentInfo dupErr = AssignmentInfo.duplicateDeclaration(
                                    cVarName, cAbsNamePos, cAbsNamePos + cVarName.length(),
                                    "Variable '" + cVarName + "' is already defined in the scope");
                                declarationErrors.add(dupErr);
                            } else {
                                globalFields.put(cVarName, cField);
                            }
                        });
                    continue;
                }

                int enclosingParen = findEnclosingParenStart(0, position);
                if (enclosingParen >= 0 && "for".equals(readKeywordBefore(enclosingParen)))
                    continue;

                int modifiers = parseModifiers(typeNameRaw);
                String typeName = stripModifiers(typeNameRaw);

                // Use findEnclosingScriptType (recursive descent) so inner class fields
                // are attributed to the innermost containing type, not just the first
                // HashMap match (which could be the outer class).
                ScriptTypeInfo containingScriptType = findEnclosingScriptType(position);

                // Check if inside a method - if so, it's a local, not global
                boolean insideMethod = false;
                if (containingScriptType != null && isInsideNestedMethod(position, containingScriptType.getBodyStart(),
                        containingScriptType.getBodyEnd()))
                    insideMethod = true;
                else {
                    for (MethodInfo method : getAllMethods()) {
                        if (method.containsPosition(position)) {
                            insideMethod = true;
                            break;
                        }
                    }
                    if (!insideMethod) {
                        for (MethodInfo constructor : getAllConstructors()) {
                            if (constructor.containsPosition(position)) {
                                insideMethod = true;
                                break;
                            }
                        }
                    }
                }

                if (insideMethod)
                    continue;
                
                String documentation = extractDocumentationBefore(m.start());
                
                int initStart = -1;
                int initEnd = -1;
                if ("=".equals(delimiter)) {
                    initStart = m.start(3);
                    int searchPos = m.end(3);
                    int depth = 0;
                    // Stop at ',' or ';' at depth 0 so that multi-declarator
                    // statements like "int x = 1, y, z = 3;" correctly bound
                    // the initializer of x to just "1" (not "1, y, z = 3").
                    while (searchPos < text.length()) {
                        char c = text.charAt(searchPos);
                        if (c == '(' || c == '[' || c == '{') depth++;
                        else if (c == ')' || c == ']' || c == '}') depth--;
                        else if ((c == ';' || c == ',') && depth == 0) {
                            initEnd = searchPos;
                            break;
                        }
                        searchPos++;
                    }
                }
                
                // Use position-aware resolution so inner class type names resolve correctly
                TypeInfo typeInfo = resolveType(typeName, position);
                FieldInfo fieldInfo = FieldInfo.globalField(fieldName, typeInfo, position, documentation, initStart, initEnd, modifiers);

                if (containingScriptType != null) {
                    containingScriptType.addField(fieldInfo);
                } else if (globalFields.containsKey(fieldName)) {
                    AssignmentInfo dupError = AssignmentInfo.duplicateDeclaration(
                        fieldName, position, position + fieldName.length(),
                        "Variable '" + fieldName + "' is already defined in the scope");
                    declarationErrors.add(dupError);
                } else {
                    globalFields.put(fieldName, fieldInfo);
                }

                // --- Multi-declarator continuation for global Java fields ---
                // For "int x = 1, y, z = 3;" FIELD_DECL_PATTERN matched "int x ="
                // (or "int x,"). Scan forward for comma-separated continuation
                // declarators (y, z) that share the same declared type.
                final TypeInfo sharedType = typeInfo;
                final int sharedModifiers = modifiers;
                final ScriptTypeInfo fContainingType = containingScriptType;
                int javaScanStart = MultiDeclaratorParser.javaDeclaratorScanStart(text, delimiter, m.end(3),
                        initEnd >= 0 ? initEnd : -1);
                if (javaScanStart >= 0) {
                    MultiDeclaratorParser.scanJavaContinuationDeclarators(text, javaScanStart, 0,
                        (cVarName, cAbsNamePos, cAbsInitStart, cAbsInitEnd) -> {
                            if (isExcluded(cAbsNamePos)) return;
                            // Check if inside a method — if so, skip (it's a local, not global)
                            boolean cInsideMethod = false;
                            if (fContainingType != null && isInsideNestedMethod(cAbsNamePos,
                                    fContainingType.getBodyStart(), fContainingType.getBodyEnd()))
                                cInsideMethod = true;
                            else {
                                for (MethodInfo method : getAllMethods()) {
                                    if (method.containsPosition(cAbsNamePos)) { cInsideMethod = true; break; }
                                }
                                if (!cInsideMethod) {
                                    for (MethodInfo constructor : getAllConstructors()) {
                                        if (constructor.containsPosition(cAbsNamePos)) { cInsideMethod = true; break; }
                                    }
                                }
                            }
                            if (cInsideMethod) return;

                            FieldInfo cField = FieldInfo.globalField(cVarName, sharedType, cAbsNamePos,
                                    null, cAbsInitStart, cAbsInitEnd, sharedModifiers);
                            if (fContainingType != null) {
                                fContainingType.addField(cField);
                            } else if (globalFields.containsKey(cVarName)) {
                                AssignmentInfo dupErr = AssignmentInfo.duplicateDeclaration(
                                    cVarName, cAbsNamePos, cAbsNamePos + cVarName.length(),
                                    "Variable '" + cVarName + "' is already defined in the scope");
                                declarationErrors.add(dupErr);
                            } else {
                                globalFields.put(cVarName, cField);
                            }
                        });
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

    public TypeInfo resolveType(String typeName) {
        /**
         * Resolve a JS type name to unified TypeInfo.
         * Handles JS primitives, .d.ts defined types, and falls back to Java types.
         */
        if (isJavaScript()) 
            return typeResolver.resolveJSType(typeName);
        
        return resolveTypeAndTrackUsage(typeName);
    }

    /**
     * Position-aware type resolution overload.
     * Resolves a type name with positional context so that an unqualified inner class name
     * (e.g., just "Inner") resolves correctly when the cursor/position is inside the body
     * of an enclosing type that declares that inner class.
     *
     * Resolution order:
     * 1. Standard resolveType(typeName) — handles simple names, dot-names, imports, primitives
     * 2. If unresolved and the name is a simple identifier (no dots), check the enclosing
     *    type at 'position' for an inner class with that name.
     *    This walks the nesting hierarchy upward so "Inner" resolves even from deeply nested code.
     *
     * @param typeName The type name to resolve (simple or dot-separated)
     * @param position The absolute offset in the script text for scope context
     * @return The resolved TypeInfo, or an unresolved TypeInfo if not found
     */
    public TypeInfo resolveType(String typeName, int position) {
        // First: delegate to the standard (positionless) resolver
        TypeInfo resolved = resolveType(typeName);

        // If already resolved, no need for positional fallback
        if (resolved != null && resolved.isResolved()) {
            return resolved;
        }

        if (typeName != null && !typeName.contains(".")) {
            TypeStringNormalizer.ArraySplit arraySplit = TypeStringNormalizer.splitArraySuffixes(typeName);
            String baseTypeName = arraySplit.base;
            int arrayDims = arraySplit.dimensions;

            ScriptTypeInfo enclosing = findEnclosingScriptType(position);
            while (enclosing != null) {
                // Check if the name is a declared type parameter on this class (e.g., E in class Box<E>)
                TypeParamInfo typeParam = enclosing.getDeclaredTypeParam(baseTypeName);
                if (typeParam != null) {
                    TypeInfo boundType = typeParam.getBoundTypeInfo();
                    if (boundType == null) {
                        typeParam.resolveBoundType();
                        boundType = typeParam.getBoundTypeInfo();
                    }
                    TypeInfo result = (boundType != null && boundType.isResolved())
                            ? TypeInfo.typeParameter(baseTypeName, boundType)
                            : TypeInfo.typeParameter(baseTypeName);
                    for (int i = 0; i < arrayDims; i++) result = TypeInfo.arrayOf(result);
                    return result;
                }
                
                // Check inner classes
                ScriptTypeInfo inner = enclosing.getInnerClass(typeName);
                if (inner != null) {
                    return inner;
                }
                // Move up to the parent type — inner classes of outer types are also in scope
                enclosing = enclosing.getOuterClass();
            }
        }

        // Return whatever resolveType() returned (possibly unresolved)
        return resolved;
    }
    
    /**
     * Resolve a type and track the import usage for unused import detection.
     * Checks script-defined types first, then falls back to imported types.
     * Used for Java/Groovy scripts.
     */
    private TypeInfo resolveTypeAndTrackUsage(String typeName) {
        if (typeName == null || typeName.isEmpty())
            return TypeInfo.unresolved(typeName, typeName);

        final String normalized = typeName.trim();
        final String normalizedFinal = stripLeadingModifiers(normalized);

        // Split array suffixes first
        TypeStringNormalizer.ArraySplit arraySplit = TypeStringNormalizer.splitArraySuffixes(normalizedFinal);
        String baseExpr = arraySplit.base;
        int arrayDims = arraySplit.dimensions;

        // Base type resolver with import tracking
        Function<String, TypeInfo> resolveBase = baseName -> {
            TypeInfo resolved;

            // Primitives
            if (TypeResolver.isPrimitiveType(baseName)) {
                resolved = TypeInfo.fromPrimitive(baseName);
            }
            // Common java.lang.String
            else if ("String".equals(baseName)) {
                resolved = typeResolver.resolveFullName("java.lang.String");
            }
            // Script-defined types (simple names only)
            else if (!baseName.contains(".") && scriptTypes.containsKey(baseName)) {
                resolved = scriptTypes.get(baseName);
            }
            // Script-defined inner types (dot-separated, e.g., "Outer.Inner")
            // Checked before Java full-name resolution so script types shadow Java types
            else if (baseName.contains(".") && scriptTypesByDotName.containsKey(baseName)) {
                resolved = scriptTypesByDotName.get(baseName);
            }
            // Fully-qualified Java types
            else if (baseName.contains(".")) {
                resolved = typeResolver.resolveFullName(baseName);
                // Fallback: "SimpleName.InnerClass" where SimpleName is a known import
                // e.g., "Map.Entry" when java.util.Map is imported → java.util.Map$Entry
                if (resolved == null || !resolved.isResolved()) {
                    int firstDot = baseName.indexOf('.');
                    String outerSimple = baseName.substring(0, firstDot);
                    String innerPath = baseName.substring(firstDot + 1);
                    TypeInfo outerType = typeResolver.resolveSimpleName(outerSimple, importsBySimpleName, wildcardPackages);
                    if (outerType != null && outerType.isResolved() && outerType.getJavaClass() != null) {
                        Class<?> currentClass = outerType.getJavaClass();
                        for (String part : innerPath.split("\\.")) {
                            Class<?> found = null;
                            for (Class<?> inner : currentClass.getClasses()) {
                                if (inner.getSimpleName().equals(part)) {
                                    found = inner;
                                    break;
                                }
                            }
                            currentClass = found;
                            if (currentClass == null) break;
                        }
                        if (currentClass != null) {
                            resolved = TypeInfo.fromClass(currentClass);
                            // Track import usage for the outer class
                            ImportData usedImport = importsBySimpleName.get(outerSimple);
                            if (usedImport != null) {
                                usedImport.incrementUsage();
                            } else if (wildcardPackages != null) {
                                String resultPkg = outerType.getPackageName();
                                for (ImportData imp : imports) {
                                    if (imp.isWildcard() && resultPkg != null && resultPkg.equals(imp.getFullPath())) {
                                        imp.incrementUsage();
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // Imported/simple
            else {
                resolved = typeResolver.resolveSimpleName(baseName, importsBySimpleName, wildcardPackages);

                // Track import usage
                if (resolved != null && resolved.isResolved()) {
                    ImportData usedImport = importsBySimpleName.get(baseName);
                    if (usedImport != null) {
                        usedImport.incrementUsage();
                    } else if (wildcardPackages != null) {
                        String resultPkg = resolved.getPackageName();
                        for (ImportData imp : imports) {
                            if (imp.isWildcard() && resultPkg != null && resultPkg.equals(imp.getFullPath())) {
                                imp.incrementUsage();
                                break;
                            }
                        }
                    }
                }
            }

            return resolved != null ? resolved : TypeInfo.unresolved(baseName, normalizedFinal);
        };

        TypeInfo resolved;

        // Fast path: no generics - skip expensive parsing
        if (!baseExpr.contains("<")) {
            resolved = resolveBase.apply(baseExpr);
        } else {
            // Slow path: parse and resolve generics
            GenericTypeParser.ParsedType parsed = GenericTypeParser.parse(baseExpr);
            if (parsed != null) {
                // Normalize whitespace around dots in base name
                String baseName = parsed.baseName.replaceAll("\\s*\\.\\s*", ".").trim();
                resolved = resolveBase.apply(baseName);

                // Apply generic arguments
                if (parsed.hasTypeArgs() && resolved != null && resolved.isResolved()) {
                   List<TypeInfo> resolvedArgs = new ArrayList<>();
                    for (GenericTypeParser.ParsedType argParsed : parsed.typeArgs) {
                        if (argParsed == null) {
                            resolvedArgs.add(TypeInfo.fromClass(Object.class));
                        } else {
                            TypeInfo argType = resolveTypeAndTrackUsage(argParsed.rawString);
                            resolvedArgs.add(argType != null ? argType : TypeInfo.unresolved(argParsed.baseName,
                                    argParsed.baseName));
                        }
                    }
                    if (!resolvedArgs.isEmpty()) {
                        resolved = resolved.parameterize(resolvedArgs);
                    }
                }
            } else
                // Fallback: treat as simple type
                resolved = resolveBase.apply(baseExpr);
        }

        // Apply array dimensions
        for (int i = 0; i < arrayDims; i++) {
            resolved = TypeInfo.arrayOf(resolved);
        }

        return resolved;
    }

    /**
     * Strip only LEADING Java modifiers from a type expression.
     * This avoids breaking generic argument lists which may contain whitespace.
     */
    private String stripLeadingModifiers(String typeExpr) {
        if (typeExpr == null) {
            return null;
        }
        int i = 0;
        int len = typeExpr.length();
        while (i < len) {
            while (i < len && Character.isWhitespace(typeExpr.charAt(i))) i++;
            int start = i;
            while (i < len && Character.isJavaIdentifierPart(typeExpr.charAt(i))) i++;
            if (start == i) {
                break;
            }
            String word = typeExpr.substring(start, i);
            if (TypeResolver.isModifier(word)) {
                // Continue stripping modifiers
                continue;
            }
            // Not a modifier; rewind to the start of this token
            return typeExpr.substring(start).trim();
        }
        return typeExpr.trim();
    }

    // ==================== PHASE 4: BUILD MARKS ====================

    /**
     * Build syntax highlighting marks - UNIFIED for both Java and JavaScript.
     * Uses the SAME mark methods for both languages since they share data structures.
     */
    private List<ScriptLine.Mark> buildMarks() {
        List<ScriptLine.Mark> marks = new ArrayList<>();

        // Strings first to protect their content
        addPatternMarks(marks, STRING_PATTERN, TokenType.STRING);
        
        // JSDoc comments with fragmented marking (avoids conflicts with @tags and {Type})
        markJSDocElements(marks);
        
        // Regular comments (non-JSDoc)
        markNonJSDocComments(marks);
        
        // Keywords - same for both languages (KEYWORD_PATTERN includes JS keywords like function, var, let, const)
        addPatternMarks(marks, KEYWORD_PATTERN, TokenType.KEYWORD);
        if(isJavaScript())
            addPatternMarks(marks, KEYWORD_JS_PATTERN, TokenType.KEYWORD);
            
        // Numbers - same for both languages
        addPatternMarks(marks, NUMBER_PATTERN, TokenType.LITERAL);

        if (isJavaScript()) {
            markObjectLiteralKeys(marks);
        }
         
        // Import statements - Java only
        if (!isJavaScript()) {
            markImports(marks);
        }

        // Class/interface/enum declarations - Java only
        if (!isJavaScript()) {
            markClassDeclarations(marks);
            markEnumConstants(marks);
        }

        // Modifiers - Java only
        if (!isJavaScript()) {
            addPatternMarks(marks, MODIFIER_PATTERN, TokenType.MODIFIER);
        }

        // Type declarations and usages - Java only (JS doesn't have explicit types)
        if (!isJavaScript()) {
            markTypeDeclarations(marks);
        }

        // Methods/functions - UNIFIED (uses shared 'methods' list)
        markMethodDeclarations(marks);

        // Pre-infer constructor lambda parameter types so body resolution works
        // Must run before markMethodCalls so lambda body chains/calls see typed parameters
        inferConstructorLambdaTypes();

        // Method calls - UNIFIED (stores in shared 'methodCalls' list)
        markMethodCalls(marks);

        // Variables and fields - UNIFIED (uses shared globalFields, methodLocals)
        markVariables(marks);

        // Chained field accesses - UNIFIED (uses resolveVariable which handles both)
        markChainedFieldAccesses(marks);
        markImportedClassUsages(marks);

        // Java-specific final passes
        if (!isJavaScript()) {
            markCastTypes(marks);
            markUnusedImports(marks);
            // Mark method reference expressions (target::methodName)
            markMethodReferences(marks);
        }
        
        // Mark lambda and method reference operators (-> and ::)
        markLambdaOperators(marks);
        
        // Mark lambda/function parameters with type info
        markInnerScopeParameters(marks);
        
        // Validate lambda return types
        validateLambdaReturnTypes(marks);
        
        // Final pass: Mark any remaining unmarked identifiers as undefined
        markUndefinedIdentifiers(marks);

        return marks;
    }

    private void markObjectLiteralKeys(List<ScriptLine.Mark> marks) {
        for (ObjectLiteralParser.ObjectLiteralAnalysis analysis : objectLiterals.values()) {
            for (ObjectLiteralParser.ObjectLiteralProperty p : analysis.properties) {
                if (p.isIdentifierKey) {
                    marks.add(new ScriptLine.Mark(p.keyStartAbs, p.keyEndAbs, TokenType.LOCAL_FIELD));
                }
            }
        }
    }

    /**
     * Two-phase object literal parsing.
     *
     * <p><b>Phase 1 (structure-only)</b>: parse every object literal with a no-op type
     * resolver.  This produces {@link ObjectLiteralParser.ObjectLiteralAnalysis} entries
     * whose property <em>names</em> are correct but whose value types are all {@code ANY}.
     * The synthetic {@link TypeInfo} shapes built from those names are then attached to
     * the corresponding {@link InnerCallableScope}s via
     * {@link ObjectLiteralParser#attachObjectLiteralContext}, so that
     * {@code resolveThisType()} can return the containing object literal type for
     * {@code this.property} chains.</p>
     *
     * <p><b>Phase 2 (full resolution)</b>: re-parse with the real
     * {@code this::resolveExpressionType} callback.  Because scopes now have their
     * {@code containingObjectType} wired, expressions like {@code return this.faah.length}
     * inside method bodies resolve correctly instead of falling back to {@code ANY}.</p>
     */
    private void parseObjectLiterals() {
        if (!isJavaScript()) return;

        List<int[]> braceRanges = findObjectLiteralBraces();
        if (braceRanges.isEmpty())
            return;

        // Phase 1: structure-only — property names extracted, value types = ANY
        ObjectLiteralParser.ExpressionTypeResolverFn nullResolver = (expr, pos) -> null;
        for (int[] range : braceRanges) {
            String objectLiteral = text.substring(range[0], range[1]);
            ObjectLiteralParser.ObjectLiteralAnalysis analysis =
                    ObjectLiteralParser.parse(objectLiteral, range[0], true, true, nullResolver,
                            this::getScriptMethodInfo);
            if (analysis != null)
                objectLiterals.put(range[0], analysis);
        }

        for (InnerCallableScope scope : innerScopes)
            ObjectLiteralParser.attachObjectLiteralContext(scope, this);

        // Phase 2: full resolution — accurate value / return types via resolveExpressionType
        for (int[] range : braceRanges) {
            String objectLiteral = text.substring(range[0], range[1]);
            ObjectLiteralParser.ObjectLiteralAnalysis analysis =
                    ObjectLiteralParser.parse(objectLiteral, range[0], true, true, this::resolveExpressionType,
                            this::getScriptMethodInfo);
            if (analysis != null)
                objectLiterals.put(range[0], analysis);
        }

        // Re-attach with fully-resolved types (reset so the guard allows re-attachment)
        for (InnerCallableScope scope : innerScopes) {
            scope.setContainingObjectType(null);
            ObjectLiteralParser.attachObjectLiteralContext(scope, this);
        }
    }

    /**
     * Scan the document for opening braces that are likely object-literal starts and
     * return their {@code [start, end)} ranges.  Shared by both parsing phases to avoid
     * duplicate scanning.
     */
    private List<int[]> findObjectLiteralBraces() {
        List<int[]> ranges = new ArrayList<>();
        int i = 0;
        while (i < text.length()) {
            int brace = text.indexOf('{', i);
            if (brace < 0) break;
            if (isExcluded(brace) || !isLikelyObjectLiteralStart(brace)) {
                i = brace + 1;
                continue;
            }
            int end = findMatchingBraceEndInDocument(brace);
            if (end <= brace) {
                i = brace + 1;
                continue;
            }
            ranges.add(new int[]{brace, end});
            i = brace + 1;
        }
        return ranges;
    }

    private boolean isLikelyObjectLiteralStart(int bracePos) {
        int prev = bracePos - 1;
        while (prev >= 0 && Character.isWhitespace(text.charAt(prev))) {
            prev--;
        }
        if (prev < 0) {
            return false;
        }

        char pc = text.charAt(prev);
        if (pc == '=' || pc == '(' || pc == '[' || pc == ',' || pc == ':' || pc == '?' || pc == '!' ||
                pc == '+' || pc == '-' || pc == '*' || pc == '/' || pc == '%' || pc == '&' || pc == '|' ||
                pc == '^') {
            return true;
        }

        if (Character.isJavaIdentifierPart(pc)) {
            int end = prev + 1;
            int start = prev;
            while (start >= 0 && Character.isJavaIdentifierPart(text.charAt(start))) {
                start--;
            }
            start++;
            String word = text.substring(start, end);
            return "return".equals(word);
        }

        return false;
    }

    private int findMatchingBraceEndInDocument(int braceStart) {
        int depth = 0;
        for (int pos = braceStart; pos < text.length(); pos++) {
            if (isExcluded(pos)) {
                continue;
            }
            char c = text.charAt(pos);
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return pos + 1;
                }
            }
        }
        return -1;
    }






    /**
     * Mark lambda arrow (->) and method reference (::) operators.
     */
    private void markLambdaOperators(List<ScriptLine.Mark> marks) {
        // Mark -> operators (lambda arrows)
        for (int i = 0; i < text.length() - 1; i++) {
            if (text.charAt(i) == '-' && text.charAt(i + 1) == '>') {
                if (!isExcluded(i)) {
                    marks.add(new ScriptLine.Mark(i, i + 2, TokenType.KEYWORD));
                }
            }
        }
        
        // Mark :: operators (method references)
        for (int i = 0; i < text.length() - 1; i++) {
            if (text.charAt(i) == ':' && text.charAt(i + 1) == ':') {
                if (!isExcluded(i)) {
                    marks.add(new ScriptLine.Mark(i, i + 2, TokenType.DEFAULT));
                }
            }
        }
    }
    
    /**
     * Pattern for method reference: target::methodName
     * target can be: identifier, qualified name (a.b.c), or 'this'/'super'
     */
    private static final java.util.regex.Pattern METHOD_REF_PATTERN = 
        java.util.regex.Pattern.compile("([a-zA-Z_$][a-zA-Z0-9_$]*(?:\\.[a-zA-Z_$][a-zA-Z0-9_$]*)*)\\s*::\\s*([a-zA-Z_$][a-zA-Z0-9_$]*|new)");
    
    /**
     * Mark method reference expressions with appropriate token types.
     * Target gets its resolved type's token, method name gets METHOD_CALL if valid.
     * Validates that the method exists on the target type.
     */
    private void markMethodReferences(List<ScriptLine.Mark> marks) {
        java.util.regex.Matcher m = METHOD_REF_PATTERN.matcher(text);
        while (m.find()) {
            int targetStart = m.start(1);
            int targetEnd = m.end(1);
            int methodStart = m.start(2);
            int methodEnd = m.end(2);
            
            // Skip if in excluded region (string/comment)
            if (isExcluded(targetStart) || isExcluded(methodStart)) {
                continue;
            }
            
            String target = m.group(1);
            String methodName = m.group(2);
            
            // Check if there are parentheses after the method name (invalid for method references)
            if (methodEnd < text.length() && text.charAt(methodEnd) == '(') {
                // Mark the :: as error
                int doubleColonPos = targetEnd;
                while (doubleColonPos < methodStart && text.charAt(doubleColonPos) != ':') {
                    doubleColonPos++;
                }
                if (doubleColonPos < methodStart) {
                    marks.add(new ScriptLine.Mark(doubleColonPos, doubleColonPos + 2, TokenType.UNDEFINED_VAR,
                        TokenErrorMessage.from("Method references cannot have parentheses. Use '" + target + "::" + methodName + "' instead of '" + target + "::" + methodName + "()'")));
                }
                // Also mark the method name as error
                marks.add(new ScriptLine.Mark(methodStart, methodEnd, TokenType.UNDEFINED_VAR,
                    TokenErrorMessage.from("Method references cannot have parentheses")));
                // Mark opening paren as error too
                marks.add(new ScriptLine.Mark(methodEnd, methodEnd + 1, TokenType.UNDEFINED_VAR,
                    TokenErrorMessage.from("Remove parentheses from method reference")));
                continue; // Skip normal processing for this invalid reference
            }
            
            // Mark the target based on what it resolves to
            markMethodRefTarget(marks, target, targetStart, targetEnd);
            
            // Resolve the target type to validate method existence
            TypeInfo targetType = resolveMethodRefTargetType(target, targetStart);
            
            if (targetType != null && targetType.isResolved()) {
                // Handle constructor references (::new)
                if ("new".equals(methodName)) {
                    if (targetType.hasConstructors()) {
                        MethodInfo ctorInfo = targetType.findConstructor(0);
                        if (ctorInfo == null) {
                            List<MethodInfo> ctors = targetType.getConstructors();
                            if (!ctors.isEmpty()) {
                                ctorInfo = ctors.get(0);
                            }
                        }
                        marks.add(new ScriptLine.Mark(methodStart, methodEnd, TokenType.METHOD_CALL, ctorInfo));
                    } else {
                        // No constructors found
                        marks.add(new ScriptLine.Mark(methodStart, methodEnd, TokenType.UNDEFINED_VAR,
                            TokenErrorMessage.from("No constructor found for '" + targetType.getSimpleName() + "'")));
                    }
                } else if (targetType.hasMethod(methodName)) {
                    // Method exists - get the MethodInfo for metadata
                    MethodInfo methodInfo = targetType.getMethodInfo(methodName);
                    if (methodInfo == null) {
                        // Try getting from overloads if single getMethodInfo fails
                        List<MethodInfo> overloads = targetType.getAllMethodOverloads(methodName);
                        if (!overloads.isEmpty()) {
                            methodInfo = overloads.get(0);
                        }
                    }
                    marks.add(new ScriptLine.Mark(methodStart, methodEnd, TokenType.METHOD_CALL, methodInfo));
                } else {
                    // Method does not exist on target type
                    marks.add(new ScriptLine.Mark(methodStart, methodEnd, TokenType.UNDEFINED_VAR,
                        TokenErrorMessage.from("Method '" + methodName + "' not found in '" + targetType.getSimpleName() + "'")));
                }
            } else {
                // Could not resolve target type - mark method as potentially valid (no error)
                // This allows for cases where the target type cannot be resolved but might be valid at runtime
                marks.add(new ScriptLine.Mark(methodStart, methodEnd, TokenType.UNDEFINED_VAR));
            }
        }
    }
    
    /**
     * Resolve the target type for a method reference expression.
     * @param target The target expression (e.g., "this", "String", "java.util.Arrays")
     * @param position The position in the text for scope resolution
     * @return The resolved TypeInfo, or null if it cannot be resolved
     */
    private TypeInfo resolveMethodRefTargetType(String target, int position) {
        // Handle keywords
        if ("this".equals(target)) {
            // Resolve 'this' to the enclosing type
            ScriptTypeInfo enclosingType = findEnclosingScriptType(position);
            if (enclosingType != null) {
                return enclosingType;
            }
            // For hook methods, resolve to the implied 'this' type
            MethodInfo containingMethod = findContainingMethod(position);
            if (containingMethod != null && containingMethod.getContainingType() != null) {
                return containingMethod.getContainingType();
            }
            return null;
        }
        
        if ("super".equals(target)) {
            // Resolve 'super' to the parent type of the enclosing class
            ScriptTypeInfo enclosingType = findEnclosingScriptType(position);
            if (enclosingType != null && enclosingType.hasSuperClass()) {
                return enclosingType.getSuperClass();
            }
            return null;
        }
        
        // Handle qualified names (a.b.ClassName)
        if (target.contains(".")) {
            TypeInfo typeInfo = resolveType(target, position);
            if (typeInfo != null && typeInfo.isResolved()) {
                return typeInfo;
            }
            // Could not resolve as type - leave unresolved
            return null;
        }
        
        // Simple identifier - try as variable first (instance reference like myList::add)
        FieldInfo varInfo = resolveVariable(target, position);
        if (varInfo != null && varInfo.getTypeInfo() != null) {
            return varInfo.getTypeInfo();
        }
        
        // Try as type name (class reference like String::valueOf)
        TypeInfo typeInfo = resolveType(target, position);
        if (typeInfo != null && typeInfo.isResolved()) {
            return typeInfo;
        }
        
        return null;
    }
    
    /**
     * Mark the target of a method reference with the appropriate token type.
     */
    private void markMethodRefTarget(List<ScriptLine.Mark> marks, String target, int start, int end) {
        // Handle keywords
        if ("this".equals(target)) {
            marks.add(new ScriptLine.Mark(start, end, TokenType.KEYWORD));
            return;
        }
        if ("super".equals(target)) {
            marks.add(new ScriptLine.Mark(start, end, TokenType.KEYWORD));
            return;
        }
        
        // Handle qualified names (a.b.ClassName) - mark the whole thing
        if (target.contains(".")) {
            // Try to resolve as a type
            TypeInfo typeInfo = resolveType(target);
            if (typeInfo != null && typeInfo.isResolved()) {
                addTypeMark(marks, start, end, TokenType.IMPORTED_CLASS, typeInfo);
                return;
            }
            // Otherwise mark the parts separately
            markQualifiedTargetParts(marks, target, start);
            return;
        }
        
        // Simple identifier - determine its token type
        // Check for local variable
        FieldInfo varInfo = resolveVariable(target, start);
        if (varInfo != null) {
            TokenType tokenType = varInfo.isParameter() ? TokenType.PARAMETER 
                                : varInfo.isGlobal() ? TokenType.GLOBAL_FIELD 
                                : TokenType.LOCAL_FIELD;
            marks.add(new ScriptLine.Mark(start, end, tokenType, varInfo));
            return;
        }
        
        // Check for type name (imported class)
        TypeInfo typeInfo = resolveType(target);
        if (typeInfo != null && typeInfo.isResolved()) {
            addTypeMark(marks, start, end, TokenType.IMPORTED_CLASS, typeInfo);
            return;
        }
        
        // Unknown - leave as default
    }
    
    /**
     * Mark parts of a qualified name like java.util.Arrays::asList
     */
    private void markQualifiedTargetParts(List<ScriptLine.Mark> marks, String qualifiedName, int baseStart) {
        String[] parts = qualifiedName.split("\\.");
        int offset = baseStart;
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            int partEnd = offset + part.length();
            
            if (i == parts.length - 1) {
                // Last part is typically the class name
                marks.add(new ScriptLine.Mark(offset, partEnd, TokenType.IMPORTED_CLASS));
            } else {
                // Package parts
                marks.add(new ScriptLine.Mark(offset, partEnd, TokenType.TYPE_DECL));
            }
            
            offset = partEnd + 1; // +1 for the dot
        }
    }
    
    /**
     * Mark parameters in all inner callable scopes (lambdas and JS function expressions).
     */
    private void markInnerScopeParameters(List<ScriptLine.Mark> marks) {
        for (InnerCallableScope scope : innerScopes) {
            for (FieldInfo param : scope.getParameters()) {
                int start = param.getDeclarationOffset();
                int end = start + param.getName().length();
                
                // Use FieldInfo as metadata - hover system will extract tooltip
                marks.add(new ScriptLine.Mark(start, end, TokenType.PARAMETER, param));
            }
        }
    }
    
    /**
     * Validate lambda return types against expected SAM return type.
     */
    private void validateLambdaReturnTypes(List<ScriptLine.Mark> marks) {
        for (InnerCallableScope scope : innerScopes) {
            if (scope.getKind() == InnerCallableScope.Kind.JAVA_LAMBDA || 
                scope.getKind() == InnerCallableScope.Kind.JS_FUNCTION_EXPR ||
                scope.getKind() == InnerCallableScope.Kind.JS_ARROW_FUNC ||
                scope.getKind() == InnerCallableScope.Kind.JS_SHORTHAND_METHOD) {
                validateLambdaReturnType(scope, marks);
            }
        }
    }
    
    /**
     * Validate that a lambda's body return type matches the expected SAM return type.
     */
    private void validateLambdaReturnType(InnerCallableScope lambda, List<ScriptLine.Mark> marks) {
        TypeInfo expectedType = lambda.getExpectedType();
        if (expectedType == null || !expectedType.isFunctionalInterface()) {
            return; // No expected type to validate against
        }
        
        MethodInfo sam = expectedType.getSingleAbstractMethod();
        if (sam == null) {
            return;
        }
        
        TypeInfo expectedReturnType = sam.getReturnType();
        
        int bodyStart = lambda.getBodyStart();
        int bodyEnd = lambda.getBodyEnd();
        if (bodyStart < 0 || bodyEnd <= bodyStart || bodyEnd > text.length()) {
            return;
        }
        
        String bodyText = text.substring(bodyStart, bodyEnd).trim();
        
        if (bodyText.startsWith("{")) {
            // Block lambda - validate return statements
            validateBlockLambdaReturns(lambda, bodyStart, bodyEnd, expectedReturnType, marks);
        } else {
            // Expression lambda - validate expression type
            try {
                TypeInfo bodyType = resolveExpressionType(bodyText, bodyStart);
                
                if (bodyType != null && expectedReturnType != null) {
                    // Check compatibility
                    if (!isCompatibleType(bodyType, expectedReturnType)) {
                        // Mark error at body start
                        String error = "Incompatible return type: expected " + 
                                      expectedReturnType.getSimpleName() + 
                                      " but was " + bodyType.getSimpleName();
                        marks.add(new ScriptLine.Mark(bodyStart, bodyEnd, TokenType.UNDEFINED_VAR, TokenErrorMessage.from(error)));
                    }
                }
            } catch (Exception e) {
                // Fail soft - don't crash on malformed expressions
            }
        }
    }
    
    /**
     * Validate return statements in a block lambda (one with { }).
     * Checks that all return statements return compatible types with the expected SAM return type.
     */
    private void validateBlockLambdaReturns(InnerCallableScope lambda, int blockStart, int blockEnd, 
                                            TypeInfo expectedReturnType, List<ScriptLine.Mark> marks) {
        // Find all return statements in the block
        String blockText = text.substring(blockStart, blockEnd);
        Pattern returnPattern = Pattern.compile("\\breturn\\b(\\s*;|\\s+[^;]+;)");
        Matcher m = returnPattern.matcher(blockText);
        
        while (m.find()) {
            int returnStart = blockStart + m.start();
            int returnEnd = blockStart + m.end();
            
            if (isExcluded(returnStart)) {
                continue;
            }
            
            // Get the return expression (if any)
            String returnStmt = m.group(1).trim();
            
            if (returnStmt.equals(";")) {
                // return; - void return
                if (expectedReturnType != null && !expectedReturnType.getSimpleName().equals("void")) {
                    String error = "Cannot return void from lambda expecting " + expectedReturnType.getSimpleName();
                    marks.add(new ScriptLine.Mark(returnStart, returnEnd, TokenType.UNDEFINED_VAR, TokenErrorMessage.from(error)));
                }
            } else {
                // return <expr>; - typed return
                String expr = returnStmt.substring(0, returnStmt.length() - 1).trim();
                
                if (expectedReturnType != null && expectedReturnType.getSimpleName().equals("void")) {
                    String error = "Cannot return a value from void lambda";
                    marks.add(new ScriptLine.Mark(returnStart, returnEnd, TokenType.UNDEFINED_VAR, TokenErrorMessage.from(error)));
                } else {
                    try {
                        // Validate return expression type
                        int exprStart = returnStart + m.group(0).indexOf(expr);
                        TypeInfo returnType = resolveExpressionType(expr, exprStart);
                        
                        if (returnType != null && expectedReturnType != null) {
                            if (!isCompatibleType(returnType, expectedReturnType)) {
                                String error = "Incompatible return type: expected " + 
                                              expectedReturnType.getSimpleName() + 
                                              " but returned " + returnType.getSimpleName();
                                int exprEnd = exprStart + expr.length();
                                marks.add(new ScriptLine.Mark(exprStart, exprEnd, TokenType.UNDEFINED_VAR, TokenErrorMessage.from(error)));
                            }
                        }
                    } catch (Exception e) {
                        // Fail soft on malformed expressions
                    }
                }
            }
        }
        
        // Check for missing returns in non-void lambdas
        // This is a warning rather than an error - Java allows it if the lambda throws or has infinite loop
        // We skip this validation to avoid false positives
    }
    
    /**
     * Check if an actual type is compatible with an expected type.
     * Includes boxing, subtype checking, and special cases like void.
     */
    private boolean isCompatibleType(TypeInfo actual, TypeInfo expected) {
        if (actual == null || expected == null) return false;
        if (actual.equals(expected)) return true;
        if (actual.getSimpleName().equals(expected.getSimpleName())) return true;
        
        // void is compatible with anything (statement lambda)
        if (expected.getSimpleName().equals("void")) return true;
        
        // Object is compatible with anything
        if (expected.getSimpleName().equals("Object") || expected.getFullName().equals("java.lang.Object")) return true;
        
        // Check primitive boxing
        if (isBoxingCompatible(actual.getSimpleName(), expected.getSimpleName())) return true;
        
        // Use TypeChecker for more complex compatibility
        return TypeChecker.isTypeCompatible(expected, actual);
    }
    
    /**
     * Check if types are compatible via boxing/unboxing.
     */
    private boolean isBoxingCompatible(String actualName, String expectedName) {
        if (actualName.equals("int") && expectedName.equals("Integer")) return true;
        if (actualName.equals("Integer") && expectedName.equals("int")) return true;
        if (actualName.equals("boolean") && expectedName.equals("Boolean")) return true;
        if (actualName.equals("Boolean") && expectedName.equals("boolean")) return true;
        if (actualName.equals("long") && expectedName.equals("Long")) return true;
        if (actualName.equals("Long") && expectedName.equals("long")) return true;
        if (actualName.equals("double") && expectedName.equals("Double")) return true;
        if (actualName.equals("Double") && expectedName.equals("double")) return true;
        if (actualName.equals("float") && expectedName.equals("Float")) return true;
        if (actualName.equals("Float") && expectedName.equals("float")) return true;
        if (actualName.equals("byte") && expectedName.equals("Byte")) return true;
        if (actualName.equals("Byte") && expectedName.equals("byte")) return true;
        if (actualName.equals("short") && expectedName.equals("Short")) return true;
        if (actualName.equals("Short") && expectedName.equals("short")) return true;
        if (actualName.equals("char") && expectedName.equals("Character")) return true;
        if (actualName.equals("Character") && expectedName.equals("char")) return true;
        return false;
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
    
    /**
     * Final pass: Mark any remaining unmarked identifiers as UNDEFINED_VAR.
     * This should be called last, after all other marking passes are complete.
     * Only marks identifiers that haven't been marked by any other pass.
     */
    private void markUndefinedIdentifiers(List<ScriptLine.Mark> marks) {
        // Build a set of all marked positions for fast lookup
        // Use a boolean array for O(1) lookup
        boolean[] markedPositions = new boolean[text.length()];
        for (ScriptLine.Mark mark : marks) {
            for (int i = mark.start; i < mark.end && i < markedPositions.length; i++) {
                markedPositions[i] = true;
            }
        }
        
        // Keywords that should not be marked as undefined
        Set<String> knownKeywords = new HashSet<>(Arrays.asList(
                "boolean", "int", "float", "double", "long", "char", "byte", "short", "void",
                "null", "true", "false", "if", "else", "switch", "case", "for", "while", "do",
                "try", "catch", "finally", "return", "throw", "var", "let", "const", "function",
                "continue", "break", "this", "new", "typeof", "instanceof", "class", "interface",
                "extends", "implements", "import", "package", "public", "private", "protected",
                "static", "final", "abstract", "synchronized", "native", "default", "enum",
                "throws", "super", "assert", "volatile", "transient"
        ));

        if (isJavaScript()) {
            knownKeywords.add("delete");
            knownKeywords.add("undefined");
        }
        
        // Find all identifiers
        Pattern identifier = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\b");
        Matcher m = identifier.matcher(text);
        
        while (m.find()) {
            int start = m.start(1);
            int end = m.end(1);
            String name = m.group(1);
            
            // Skip if already marked
            if (start < markedPositions.length && markedPositions[start]) {
                continue;
            }
            
            // Skip if in excluded region (string/comment)
            if (isExcluded(start)) {
                continue;
            }
            
            // Skip keywords
            if (knownKeywords.contains(name)) {
                continue;
            }
            
            // Mark as undefined
            marks.add(new ScriptLine.Mark(start, end, TokenType.UNDEFINED_VAR, null));
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
            // Skip implicit imports (from JaninoScript defaults, not in source text)
            // These have offset -1 and don't need to be visually marked
            if (imp.getStartOffset() < 0) 
                continue;
            
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
        Pattern classKeyword = Pattern.compile(
                "\\b(class|interface|enum)\\s+([A-Za-z_][a-zA-Z0-9_]*)");

        Matcher m = classKeyword.matcher(text);
        while (m.find()) {
            if (isExcluded(m.start()))
                continue;

            marks.add(new ScriptLine.Mark(m.start(1), m.end(1), TokenType.CLASS_KEYWORD));

            String kind = m.group(1);
            TokenType nameType;
            if ("interface".equals(kind)) {
                nameType = TokenType.INTERFACE_DECL;
            } else if ("enum".equals(kind)) {
                nameType = TokenType.ENUM_DECL;
            } else {
                nameType = TokenType.CLASS_DECL;
            }
            String typeName = m.group(2);
            ScriptTypeInfo typeInfo = scriptTypes.get(typeName);
            if (typeInfo == null) {
                for (ScriptTypeInfo candidate : scriptTypesByFullName.values()) {
                    if (candidate.getSimpleName().equals(typeName)
                            && m.start(2) >= candidate.getDeclarationOffset()
                            && m.start(2) < candidate.getBodyEnd()) {
                        typeInfo = candidate;
                        break;
                    }
                }
            }
            marks.add(new ScriptLine.Mark(m.start(2), m.end(2), nameType, typeInfo));

            // Skip past type params clause <...> using depth-aware scan
            int scanPos = m.end(2);
            while (scanPos < text.length() && Character.isWhitespace(text.charAt(scanPos)))
                scanPos++;

            if (typeInfo != null && typeInfo.hasDeclaredTypeParams()) {
                markTypeParamDeclarations(marks, m.end(2), typeInfo);
            }

            if (scanPos < text.length() && text.charAt(scanPos) == '<') {
                int depth = 1, i = scanPos + 1;
                while (i < text.length() && depth > 0) {
                    char c = text.charAt(i++);
                    if (c == '<') depth++;
                    else if (c == '>') depth--;
                }
                if (depth == 0) scanPos = i;
            }

            // Skip optional () (for enum constructors etc.)
            while (scanPos < text.length() && Character.isWhitespace(text.charAt(scanPos)))
                scanPos++;
            if (scanPos + 1 < text.length() && text.charAt(scanPos) == '(' && text.charAt(scanPos + 1) == ')') {
                scanPos += 2;
            }

            // Find opening brace, scanning for extends/implements keywords
            int bracePos = scanPos;
            while (bracePos < text.length() && text.charAt(bracePos) != '{') {
                bracePos++;
            }
            if (bracePos >= text.length()) continue;

            String betweenParamsAndBrace = text.substring(scanPos, bracePos);

            // Find extends keyword at depth 0
            int extIdx = indexOfAtDepthZero(betweenParamsAndBrace, "extends");
            if (extIdx >= 0) {
                int extendsAbsStart = scanPos + extIdx;
                marks.add(new ScriptLine.Mark(extendsAbsStart, extendsAbsStart + 7, TokenType.IMPORT_KEYWORD));

                // Extract parent name after 'extends '
                String afterExtends = betweenParamsAndBrace.substring(extIdx + 7).trim();
                // Stop at 'implements' keyword or end
                int implIdx = indexOfAtDepthZero(afterExtends, "implements");
                String parentSection = implIdx >= 0 ? afterExtends.substring(0, implIdx).trim() : afterExtends.trim();

                // Extract just the base name (without generics) for markExtendsClause
                int nameEnd = 0;
                while (nameEnd < parentSection.length() &&
                       (Character.isJavaIdentifierPart(parentSection.charAt(nameEnd)) || parentSection.charAt(nameEnd) == '.')) {
                    nameEnd++;
                }
                if (nameEnd > 0) {
                    String parentName = parentSection.substring(0, nameEnd);
                    int parentAbsStart = extendsAbsStart + 7;
                    while (parentAbsStart < bracePos && Character.isWhitespace(text.charAt(parentAbsStart)))
                        parentAbsStart++;
                    markExtendsClause(marks, parentAbsStart, parentName);

                    // Mark generic args on the parent (e.g., <T> in extends Parent<T>)
                    if (nameEnd < parentSection.length() && parentSection.charAt(nameEnd) == '<') {
                        int genStart = parentAbsStart + nameEnd;
                        markGenericArgsInExtendsClause(marks, genStart, typeInfo);
                    }
                }
            }

            // Find implements keyword at depth 0
            int implIdx = indexOfAtDepthZero(betweenParamsAndBrace, "implements");
            if (implIdx >= 0) {
                int implAbsStart = scanPos + implIdx;
                marks.add(new ScriptLine.Mark(implAbsStart, implAbsStart + 10, TokenType.IMPORT_KEYWORD));

                String afterImpl = betweenParamsAndBrace.substring(implIdx + 10).trim();
                int implListStart = implAbsStart + 10;
                while (implListStart < bracePos && Character.isWhitespace(text.charAt(implListStart)))
                    implListStart++;
                markImplementsClause(marks, implListStart, afterImpl);
            }
        }
    }

    private void markGenericArgsInExtendsClause(List<ScriptLine.Mark> marks, int ltPos, ScriptTypeInfo childType) {
        if (ltPos >= text.length() || text.charAt(ltPos) != '<') return;
        int depth = 1, i = ltPos + 1;
        while (i < text.length() && depth > 0) {
            char c = text.charAt(i++);
            if (c == '<') depth++;
            else if (c == '>') depth--;
        }
        if (depth != 0) return;
        int gtPos = i - 1;

        if (childType != null && childType.hasDeclaredTypeParams()) {
            List<TypeParamInfo> typeParams = childType.getDeclaredTypeParams();
            markNestedTypeParamUsages(marks, ltPos + 1, gtPos, typeParams);
        }
    }

    /**
     * Mark enum constants with ENUM_CONSTANT token type.
     * Adds marks for all enum constants in script-defined enums.
     */
    private void markEnumConstants(List<ScriptLine.Mark> marks) {
        // Iterate all types (including inner) so enum constants in nested enums are marked
        for (ScriptTypeInfo scriptType : scriptTypesByFullName.values()) {
            if (!scriptType.isEnum()) 
                continue;

            // Mark each enum constant
            for (EnumConstantInfo constant : scriptType.getEnumConstants().values()) {
                FieldInfo fieldInfo = constant.getFieldInfo();
                int start = fieldInfo.getDeclarationOffset();
                int end = start + fieldInfo.getName().length();

                // Always use ENUM_CONSTANT token type (blue + bold + italic)
                // Errors are shown via underline, not by changing the token type
                marks.add(new ScriptLine.Mark(start, end, TokenType.ENUM_CONSTANT, fieldInfo));
            }
        }
    }

    /**
     * Mark the extends clause with proper coloring.
     * The parent class is colored based on its resolved type.
     */
    private void markExtendsClause(List<ScriptLine.Mark> marks, int clauseStart, String parentName) {
        String trimmedName = parentName.trim();
        if (trimmedName.isEmpty())
            return;


        // Resolve the parent type with position context for inner class names
        TypeInfo parentType = resolveType(trimmedName, clauseStart);
        TokenType tokenType;

        if (parentType != null && parentType.isResolved()) {
            // Use the type's specific token type (CLASS, INTERFACE, ENUM)
            tokenType = parentType.getTokenType();
        } else {
            // Unresolved - mark as undefined
            tokenType = TokenType.UNDEFINED_VAR;
            if (parentType == null) {
                parentType = TypeInfo.unresolved(trimmedName, trimmedName);
            }
        }

        // Find actual position in text (might have leading whitespace in the group)
        int actualStart = clauseStart;
        String fullClause = parentName;
        while (actualStart < clauseStart + fullClause.length() &&
                Character.isWhitespace(text.charAt(actualStart))) {
            actualStart++;
        }

        marks.add(new ScriptLine.Mark(actualStart, actualStart + trimmedName.length(), tokenType, parentType));
    }

    /**
     * Mark the implements clause with proper coloring.
     * Each interface is colored based on its resolved type.
     */
    private void markImplementsClause(List<ScriptLine.Mark> marks, int clauseStart, String implementsList) {
        String[] interfaces = implementsList.split(",");
        int currentPos = clauseStart;

        for (String ifaceName : interfaces) {
            String trimmedName = ifaceName.trim();
            if (trimmedName.isEmpty()) {
                currentPos += ifaceName.length() + 1; // +1 for comma
                continue;
            }

            // Find the actual start position of this interface name in the text
            int leadingSpaces = 0;
            while (leadingSpaces < ifaceName.length() && Character.isWhitespace(ifaceName.charAt(leadingSpaces))) {
                leadingSpaces++;
            }
            int actualStart = currentPos + leadingSpaces;

            // Resolve the interface type with position context for inner class names
            TypeInfo ifaceType = resolveType(trimmedName, actualStart);
            TokenType tokenType;

            if (ifaceType != null && ifaceType.isResolved()) {
                tokenType = ifaceType.getTokenType();
            } else {
                tokenType = TokenType.UNDEFINED_VAR;
                if (ifaceType == null) {
                    ifaceType = TypeInfo.unresolved(trimmedName, trimmedName);
                }
            }

            marks.add(new ScriptLine.Mark(actualStart, actualStart + trimmedName.length(), tokenType, ifaceType));

            // Move past this interface name and the comma
            currentPos += ifaceName.length() + 1;
        }
    }

    private void markTypeDeclarations(List<ScriptLine.Mark> marks) {
        // First, mark variables that hold Java.type() results
        if (isJavaScript()) {
            markJavaTypeVariables(marks);
        }
        
        // Pattern for type optionally followed by generics - we'll manually parse generics
        Pattern typeStart = Pattern.compile(
                "(?:(?:public|private|protected|static|final|transient|volatile)\\s+)*" +
                        "([a-zA-Z][a-zA-Z0-9_]*)\\s*");

        Matcher m = typeStart.matcher(text);
        int searchFrom = 0;

        while (m.find(searchFrom)) {
            int typeNameStart = m.start(1);
            int typeNameEnd = m.end(1);

            if (isExcluded(typeNameStart) || isInImportOrPackage(typeNameStart)) {
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

            int varScanPos = posAfterType;
            while (text.startsWith("[]", varScanPos)) {
                marks.add(new ScriptLine.Mark(varScanPos, varScanPos + 2, TokenType.DEFAULT, null));
                varScanPos += 2;
                while (varScanPos < text.length() && Character.isWhitespace(text.charAt(varScanPos))) varScanPos++;
            }

            // Check if this looks like a type declaration:
            boolean hasGeneric = genericContent != null && !genericContent.isEmpty();
            boolean followedByVarName = false;
            boolean atEndOfLine = varScanPos >= text.length() || text.charAt(varScanPos) == '\n';

            if (!atEndOfLine && varScanPos < text.length()) {
                char nextChar = text.charAt(varScanPos);
                followedByVarName = Character.isLetter(nextChar) || nextChar == '_';
            }

            // Accept as type if: has generics OR followed by variable name
            if (hasGeneric || followedByVarName) {
                // Resolve with position context so type params (T, E, etc.) resolve via enclosing class
                TypeInfo info = resolveType(typeName, typeNameStart);
                TokenType tokenType = (info != null && info.isResolved()) ? info.getTokenType() : TokenType.UNDEFINED_VAR;
                marks.add(new ScriptLine.Mark(typeNameStart, typeNameEnd, tokenType, info));

                // Handle generic content recursively
                if (hasGeneric && genericStart >= 0) {
                    int contentStart = genericStart + 1;
                    markGenericTypesRecursive(genericContent, contentStart, marks, typeNameStart);
                }
            }

            searchFrom = m.end();
        }
    }



    /**
     * Mark variables that hold Java.type() class references.
     * Example: var File = Java.type("java.io.File");
     */
    private void markJavaTypeVariables(List<ScriptLine.Mark> marks) {
        // Pattern: var/let/const varName = Java.type("className")
        Pattern javaTypePattern = Pattern.compile(
            "\\b(var|let|const)\\s+(\\w+)\\s*=\\s*Java\\.type\\s*\\(\\s*[\"']([^\"']+)[\"']\\s*\\)");
        
        Matcher m = javaTypePattern.matcher(text);
        
        while (m.find()) {
            if (isExcluded(m.start())) continue;
            
            String varName = m.group(2);
            String className = m.group(3);
            int varStart = m.start(2);
            int varEnd = m.end(2);
            
            // Resolve the class name
            TypeInfo classType = typeResolver.resolveFullName(className);
            if (classType != null && classType.isResolved()) {
                // Create ClassTypeInfo to represent that this variable holds a Class reference
                ClassTypeInfo classRef = new ClassTypeInfo(classType);
                
                // Mark the variable with the ClassTypeInfo for hover info
                marks.add(new ScriptLine.Mark(varStart, varEnd, TokenType.LOCAL_FIELD, classRef));
            }
        }
    }
    
    /**
     * Recursively parse and mark generic type parameters.
     * Handles arbitrarily nested generics like Map<String, List<Map<String, String>>>.
     */
    private void markGenericTypesRecursive(String content, int baseOffset, List<ScriptLine.Mark> marks) {
        markGenericTypesRecursive(content, baseOffset, marks, -1);
    }

    private static final Set<String> GENERIC_KEYWORDS = new HashSet<>(Arrays.asList("extends", "super"));

    private void markGenericTypesRecursive(String content, int baseOffset, List<ScriptLine.Mark> marks, int positionContext) {
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

            // Skip keywords that appear in generic bounds (extends, super)
            if (GENERIC_KEYWORDS.contains(typeName)) {
                continue;
            }

            int absStart = baseOffset + start;
            int absEnd = baseOffset + i;

            TypeInfo typeCheck = positionContext >= 0
                    ? resolveType(typeName, positionContext)
                    : resolveType(typeName);
            if (typeCheck != null && typeCheck.isResolved()) {
                if (!isExcluded(absStart)) {
                    TokenType tokenType = typeCheck.getTokenType();
                    marks.add(new ScriptLine.Mark(absStart, absEnd, tokenType, typeCheck));
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
                    markGenericTypesRecursive(nestedContent, baseOffset + nestedStart, marks, positionContext);
                }
            }
        }
    }

    /**
     * Mark JSDoc elements within comments for syntax highlighting.
     * Adds marks for @tags (like @param, @return, @type) and {Type} references.
     */
    private void markJSDocElements(List<ScriptLine.Mark> marks) {
        // Find all JSDoc comments (/** ... */)
        Pattern jsDocPattern = Pattern.compile("/\\*\\*([\\s\\S]*?)\\*/");
        Matcher jsDocMatcher = jsDocPattern.matcher(text);
        
        while (jsDocMatcher.find()) {
            int commentStart = jsDocMatcher.start();
            int commentEnd = jsDocMatcher.end();
            
            // Skip if this JSDoc is inside a string
            boolean insideString = false;
            for (ScriptLine.Mark mark : marks) {
                if (mark.type == TokenType.STRING && 
                    commentStart >= mark.start && commentEnd <= mark.end) {
                    insideString = true;
                    break;
                }
            }
            if (insideString) {
                continue;
            }
            
            String commentContent = jsDocMatcher.group(0);

            // Find the method that this JSDoc belongs to, for parameter validation
            MethodInfo associatedMethod = findMethodAfterPosition(commentEnd);
            Set<String> methodParamNames = new HashSet<>();
            if (associatedMethod != null) {
                for (FieldInfo param : associatedMethod.getParameters()) {
                    methodParamNames.add(param.getName());
                }
            }
            
            // Collect all special element positions (@tags and {Type}s) that should NOT be gray
            java.util.List<int[]> specialRanges = new java.util.ArrayList<>();

            // Find @tags and process @param and @type specially
            Pattern tagPattern = Pattern.compile("@(\\w+)");
            Matcher tagMatcher = tagPattern.matcher(commentContent);
            while (tagMatcher.find()) {
                int tagStart = commentStart + tagMatcher.start();
                int tagEnd = commentStart + tagMatcher.end();
                specialRanges.add(new int[]{tagStart, tagEnd});
                
                String tagName = tagMatcher.group(1);
                
                // For @type tag, resolve the type and attach it for hover
                TypeInfo tagTypeInfo = null;
                if ("type".equals(tagName)) {
                    // Look for {Type} after @type
                    int afterTag = tagMatcher.end();
                    String afterTagText = commentContent.substring(afterTag);
                    Pattern typeRefPattern = Pattern.compile("^\\s*\\{([^}]+)\\}");
                    Matcher typeRefMatcher = typeRefPattern.matcher(afterTagText);
                    if (typeRefMatcher.find()) {
                        String typeName = typeRefMatcher.group(1).trim();
                        tagTypeInfo = resolveType(typeName);
                    }
                }
                
                // Mark the @tag itself (with TypeInfo for @type)
                marks.add(new ScriptLine.Mark(tagStart, tagEnd, TokenType.JSDOC_TAG, tagTypeInfo));

                // If this is @param, look for parameter name after the type
                if ("param".equals(tagName)) {
                    // Look for: @param {Type} paramName or @param paramName
                    int afterTag = tagMatcher.end();
                    String afterTagText = commentContent.substring(afterTag);

                    // Pattern to match optional {Type} followed by parameter name
                    Pattern paramNamePattern = Pattern.compile("^\\s*(?:\\{[^}]*\\}\\s*)?([a-zA-Z_][a-zA-Z0-9_]*)");
                    Matcher paramNameMatcher = paramNamePattern.matcher(afterTagText);
                    if (paramNameMatcher.find()) {
                        String paramName = paramNameMatcher.group(1);
                        int paramNameStart = commentStart + afterTag + paramNameMatcher.start(1);
                        int paramNameEnd = commentStart + afterTag + paramNameMatcher.end(1);

                        // Check if this parameter exists in the method
                        boolean paramExists = methodParamNames.contains(paramName);
                        TokenType paramTokenType = paramExists ? TokenType.PARAMETER : TokenType.UNDEFINED_VAR;

                        // Add to special ranges to exclude from comment marking
                        specialRanges.add(new int[]{paramNameStart, paramNameEnd});
                        // Mark the parameter name
                        marks.add(new ScriptLine.Mark(paramNameStart, paramNameEnd, paramTokenType, null));
                    }
                }
            }
            
            // Find {Type} references
            Pattern typePattern = Pattern.compile("\\{([^}]+)\\}");
            Matcher typeMatcher = typePattern.matcher(commentContent);
            while (typeMatcher.find()) {
                int braceStart = commentStart + typeMatcher.start();
                int braceEnd = commentStart + typeMatcher.end();
                specialRanges.add(new int[]{braceStart, braceEnd});
                
                // Mark braces
                marks.add(new ScriptLine.Mark(braceStart, braceStart + 1, TokenType.JSDOC_TYPE, null)); // {
                marks.add(new ScriptLine.Mark(braceEnd - 1, braceEnd, TokenType.JSDOC_TYPE, null)); // }
                
                // Mark the type name inside braces
                String typeName = typeMatcher.group(1).trim();
                int typeStart = commentStart + typeMatcher.start(1);
                int typeEnd = commentStart + typeMatcher.end(1);
                
                // Try to resolve the type for hover info
                TypeInfo resolvedType = resolveType(typeName);
                addTypeMark(marks, typeStart, typeEnd,
                        resolvedType != null ? resolvedType.getTokenType() : TokenType.UNDEFINED_VAR,
                        resolvedType);
            }
            
            // Sort special ranges by start position
            specialRanges.sort((a, b) -> Integer.compare(a[0], b[0]));
            
            // Mark the comment in FRAGMENTS between special elements
            int lastPos = commentStart;
            for (int[] range : specialRanges) {
                // Mark from lastPos to start of special element
                if (lastPos < range[0]) {
                    marks.add(new ScriptLine.Mark(lastPos, range[0], TokenType.COMMENT, null));
                }
                lastPos = range[1]; // Move past the special element
            }
            // Mark from last special element to end of comment
            if (lastPos < commentEnd) {
                marks.add(new ScriptLine.Mark(lastPos, commentEnd, TokenType.COMMENT, null));
            }
        }
    }

    /**
     * Find the method declaration that immediately follows the given position.
     * Used to associate JSDoc comments with their methods for parameter validation.
     */
    private MethodInfo findMethodAfterPosition(int position) {
        // Skip whitespace after position
        int searchStart = position;
        while (searchStart < text.length() && Character.isWhitespace(text.charAt(searchStart))) {
            searchStart++;
        }

        // Find the method with the smallest offset that is >= searchStart
        MethodInfo closestMethod = null;
        int closestDistance = Integer.MAX_VALUE;

        for (MethodInfo method : methods) {
            if (!method.isDeclaration())
                continue;
            int methodStart = method.getFullDeclarationOffset();
            if (methodStart < 0)
                methodStart = method.getNameOffset();
            if (methodStart < 0)
                continue;

            // Check if this method starts at or after our search position
            // but within a reasonable distance (e.g., 200 chars to account for modifiers)
            if (methodStart >= position && methodStart < position + 200) {
                int distance = methodStart - position;
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestMethod = method;
                }
            }
        }

        // Also check methods inside script types (including inner classes)
        for (ScriptTypeInfo scriptType : scriptTypesByFullName.values()) {
            for (MethodInfo method : scriptType.getAllMethodsFlat()) {
                if (!method.isDeclaration())
                    continue;
                int methodStart = method.getFullDeclarationOffset();
                if (methodStart < 0)
                    methodStart = method.getNameOffset();
                if (methodStart < 0)
                    continue;

                if (methodStart >= position && methodStart < position + 200) {
                    int distance = methodStart - position;
                    if (distance < closestDistance) {
                        closestDistance = distance;
                        closestMethod = method;
                    }
                }
            }
        }

        return closestMethod;
    }
    
    /**
     * Mark all comments EXCEPT JSDoc comments (slash-star-star style).
     * JSDoc comments are handled separately in markJSDocElements.
     */
    private void markNonJSDocComments(List<ScriptLine.Mark> marks) {
        // Match: /* ... */ (but not /**), // ..., # ...
        Matcher m = COMMENT_PATTERN.matcher(text);
        while (m.find()) {
            int start = m.start();
            int end = m.end();
            String comment = m.group();
            
            // Skip JSDoc comments (/** style) - they're handled separately
            if (comment.startsWith("/**")) {
                continue;
            }
            
            marks.add(new ScriptLine.Mark(start, end, TokenType.COMMENT, null));
        }
    }

    private void addTypeMark(List<ScriptLine.Mark> marks, int start, int end, TokenType type, Object metadata) {
        int bracketStart = text.indexOf('[', start);
        int coreEnd = (bracketStart >= start && bracketStart < end) ? bracketStart : end;
        if (coreEnd < end) marks.add(new ScriptLine.Mark(coreEnd, end, TokenType.DEFAULT, null));
        marks.add(new ScriptLine.Mark(start, coreEnd, type, metadata));
    }

    private void markMethodDeclarations(List<ScriptLine.Mark> marks) {
        Matcher m = METHOD_DECL_PATTERN.matcher(text);
        while (m.find()) {
            if (isExcluded(m.start()))
                continue;

            // Skip class/interface/enum declarations - these look like method declarations
            // but "class Foo(" is not a method, it's a class declaration3
            String returnType = m.group(1);
            if (returnType.equals("class") || returnType.equals("interface") || returnType.equals("enum") ||returnType.equals("new")) {
                continue;
            }

            // Find the corresponding MethodInfo created in parseMethodDeclarations
            int methodDeclStart = m.start();
            int methodNameStart = m.start(2);
            MethodInfo methodInfo = null;
            for (MethodInfo method : getAllMethods()) {
                boolean matchesDeclStart = method.getTypeOffset() == methodDeclStart
                        || method.getFullDeclarationOffset() == methodDeclStart;
                boolean matchesNameStart = method.getNameOffset() == methodNameStart;

                if (matchesDeclStart || matchesNameStart) {
                    methodInfo = method;
                    break;
                }
            }

            // Return type
            TokenType returnToken = TokenType.UNDEFINED_VAR;
            if (methodInfo != null)
                returnToken = methodInfo.getReturnType().getTokenType();

            int returnStart = m.start(1);
            int returnEnd = m.end(1);
            addTypeMark(marks, returnStart, returnEnd,
                    returnToken, methodInfo != null ? methodInfo.getReturnType() : null);
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

            if (isExcluded(nameStart) || isKeyword(methodName))
                continue;

            // Skip if in import/package statement
            if (isInImportOrPackage(nameStart))
                continue;

            boolean skip = false;
            for (MethodInfo decl : methods)
                if (decl.getNameOffset() == nameStart)
                    // This is a method declaration, not a call
                    skip = true;
            
            if (skip)
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

            // Handle super() constructor calls
            if (methodName.equals("super")) {
                handleSuperConstructorCall(marks, nameStart, nameEnd, openParen, closeParen);
                continue;
            }

            // Parse the arguments (first pass - without expected types for overload resolution)
            List<MethodCallInfo.Argument> arguments = parseMethodArguments(openParen + 1, closeParen, null, null);

            // Check if this is a static access (Class.method() style)
            boolean isStaticAccess = isStaticAccessCall(nameStart);
            
            // Resolve receiver using existing chain-based resolver and detect static access
            TypeInfo receiverType = resolveReceiverChain(nameStart);
            MethodInfo resolvedMethod = null;

            if (receiverType != null) {
                // Check for synthetic types first (JavaScript only)
                SyntheticType syntheticType = null;
                if (isJavaScript()) {
                    syntheticType = typeResolver.getSyntheticType(receiverType.getSimpleName());
                }

                if (syntheticType != null && syntheticType.hasMethod(methodName)) {
                    // Synthetic type method call (e.g., Java.type())
                    resolvedMethod = syntheticType.getMethodInfo(methodName);

                    // Check for dynamic return type resolution (like Java.type("className"))
                    SyntheticMethod synMethod = syntheticType.getMethod(methodName);
                    TypeInfo dynamicReturnType = null;
                    if (synMethod != null) {
                        String argsText = text.substring(openParen + 1, closeParen);
                        String[] strArgs = TypeResolver.parseStringArguments(argsText);
                        dynamicReturnType = synMethod.resolveReturnType(strArgs);
                    }

                    MethodCallInfo callInfo = new MethodCallInfo(methodName, nameStart, nameEnd, openParen,
                            closeParen, arguments, receiverType, resolvedMethod, false);

                    // Set the actual resolved return type for downstream type resolution
                    if (dynamicReturnType != null) {
                        callInfo.setResolvedReturnType(dynamicReturnType);
                    }

                    callInfo.validate();
                    methodCalls.add(callInfo);
                    marks.add(new ScriptLine.Mark(nameStart, nameEnd, TokenType.METHOD_CALL, callInfo));
                    continue;
                }
                // Regular type - check for method existence using hierarchy search if it's a ScriptTypeInfo
                boolean hasMethod = false;
                TypeInfo rawReceiver = receiverType.getRawType();
                if (rawReceiver instanceof ScriptTypeInfo) {
                    hasMethod = ((ScriptTypeInfo) rawReceiver).hasMethodInHierarchy(methodName);
                } else {
                    hasMethod = receiverType.hasMethod(methodName);
                }

                if (hasMethod) {
           
                    TypeInfo[] argTypes = arguments.stream().map(MethodCallInfo.Argument::getResolvedType).toArray(TypeInfo[]::new);

                    // Get best method overload based on argument types
                    resolvedMethod = receiverType.getBestMethodOverload(methodName, argTypes);

                    if (isStaticAccess && resolvedMethod != null && !resolvedMethod.isStatic()) {
                        TokenErrorMessage errorMsg = TokenErrorMessage
                                .from("Cannot call non-static method '" + methodName + "' from static context '" + receiverType.getSimpleName() + "'")
                                .clearOtherErrors();
                        marks.add(new ScriptLine.Mark(nameStart, nameEnd, TokenType.UNDEFINED_VAR, errorMsg));
                    } else {
                        // Second pass: Re-resolve arguments with expected parameter types if method was found
                        if (resolvedMethod != null && resolvedMethod.getParameters().size() == arguments.size())
                            arguments = parseMethodArguments(openParen + 1, closeParen, resolvedMethod, receiverType);
                        
                        
                        MethodCallInfo callInfo = new MethodCallInfo(methodName, nameStart, nameEnd, openParen,
                                closeParen, arguments, receiverType, resolvedMethod, isStaticAccess);
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
                    }
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

                        // Second pass: Re-resolve arguments with expected parameter types if method was found
                        if (resolvedMethod != null && resolvedMethod.getParameters().size() == arguments.size()) {
                            arguments = parseMethodArguments(openParen + 1, closeParen, resolvedMethod, null);
                        }
                        
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
                    } else if (isGlobalEngineFunction(methodName)) {
                        JSMethodInfo jsGlobalMethod = JSTypeRegistry.getInstance().getGlobalEngineFunction(methodName);
                        MethodInfo resolvedGlobal = MethodInfo.fromJSMethod(jsGlobalMethod, null);

                        List<MethodCallInfo.Argument> globalArguments = arguments;
                        if (resolvedGlobal.getParameters().size() == arguments.size()) {
                            globalArguments = parseMethodArguments(openParen + 1, closeParen, resolvedGlobal, null);
                        }

                        MethodCallInfo callInfo = new MethodCallInfo(
                            methodName, nameStart, nameEnd, openParen, closeParen,
                            globalArguments, null, resolvedGlobal
                        );
                        if (!isFollowedByDot(closeParen)) {
                            TypeInfo expectedType = findExpectedTypeAtPosition(nameStart);
                            if (expectedType != null) callInfo.setExpectedType(expectedType);
                        }
                        callInfo.validate();
                        methodCalls.add(callInfo);
                        marks.add(new ScriptLine.Mark(nameStart, nameEnd, TokenType.METHOD_CALL, callInfo));
                    } else {
                        // Not a top-level script method or engine function — check if the call site is inside
                        // a ScriptTypeInfo whose method table contains this name (implicit 'this' call).
                        // e.g. getYaw() called without 'this.' or 'abilityLine.' inside AbilityLine resolves via AbilityLine's method hierarchy.
                        ScriptTypeInfo callerType = findEnclosingScriptType(nameStart);
                  
                        if (callerType != null && callerType.hasMethodInHierarchy(methodName)) {
                            TypeInfo[] argTypes = arguments.stream().map(MethodCallInfo.Argument::getResolvedType).toArray(TypeInfo[]::new);
                            MethodInfo implicitMethod = callerType.getBestMethodOverload(methodName, argTypes);
                            if (implicitMethod != null && implicitMethod.getParameters().size() == arguments.size()) {
                                arguments = parseMethodArguments(openParen + 1, closeParen, implicitMethod, callerType);
                            }
                            MethodCallInfo callInfo = new MethodCallInfo(
                                methodName, nameStart, nameEnd, openParen, closeParen,
                                arguments, callerType, implicitMethod
                            );
                            if (!isFollowedByDot(closeParen)) {
                                TypeInfo expectedType = findExpectedTypeAtPosition(nameStart);
                                if (expectedType != null) callInfo.setExpectedType(expectedType);
                            }
                            callInfo.validate();
                            methodCalls.add(callInfo);
                            marks.add(new ScriptLine.Mark(nameStart, nameEnd, TokenType.METHOD_CALL, callInfo));
                        } else {
                            marks.add(new ScriptLine.Mark(nameStart, nameEnd, TokenType.UNDEFINED_VAR));
                        }
                    }
                }
            }
        }
    }
    
    public boolean isKeyword(String word){
        Set<String> keywords = new HashSet<>(Arrays.asList(TypeChecker.getJavaKeywords()));
        if (isJavaScript())
            keywords.addAll(Arrays.asList(TypeChecker.getJavaScriptKeywords()));
        return keywords.contains(word);
    }
    /**
     * Check if a method call is a static access (Class.method() style).
     * Returns true if the immediate receiver before the dot is a class name (uppercase).
     */
    /**
     * Check if a method call at the given position is static access.
     * Walks backward from the method name to analyze the receiver.
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
            // Method call or array - would need complex expression resolution
            // For now, conservatively treat as instance access
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
        
        // Skip whitespace before the identifier
        while (pos >= 0 && Character.isWhitespace(text.charAt(pos)))
            pos--;

        // If preceded by a dot, this is part of a chain - treat as instance for now
        if (pos >= 0 && text.charAt(pos) == '.') {
            return false;
        }

        // Direct identifier - check if it resolves to a type (static) or variable (instance)
        if (ident.isEmpty()) return false;

        // Use unified static access checker
        return TypeResolver.isStaticAccessExpression(ident, identStart, this);
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
    /**
     * Parse method call arguments.
     *
     * @param start Start position of arguments (after opening paren)
     * @param end End position of arguments (at closing paren)
     * @param methodInfo Optional MethodInfo to provide expected parameter types for validation
     * @param receiverType Optional receiver/containing type for substituting generic type parameters in argument types
     * @return List of parsed arguments with resolved types
     */
    public List<MethodCallInfo.Argument> parseMethodArguments(int start, int end, MethodInfo methodInfo, TypeInfo receiverType) {
        List<MethodCallInfo.Argument> args = new ArrayList<>();
        
        if (start >= end) 
            return args; // No arguments
        
        end = Math.min(end, text.length());
        
        
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
                    // Get expected parameter type if available
                    TypeInfo expectedParamType = null;
                    if (methodInfo != null && args.size() < methodInfo.getParameters().size()) {
                        FieldInfo parameter = methodInfo.getParameters().get(args.size());
                        expectedParamType = parameter.getTypeInfo();
                    }

                    TypeInfo argType = resolveArgumentType(argText, actualStart, expectedParamType);
                    
                    // Substitute type parameters using receiver's generic context
                    if (receiverType != null && argType != null && GenericContext.hasGenerics(receiverType)) {
                        GenericContext ctx = GenericContext.forReceiver(receiverType);
                        TypeInfo substituted = ctx.substitute(argType);
                        if (substituted != null && substituted.isResolved()) {
                            argType = substituted;
                        }
                    }
                    
                    String samConflictError = CURRENT_SAM_CONFLICT_ERROR.get();
                    if (samConflictError != null) {
                        CURRENT_SAM_CONFLICT_ERROR.remove();
                        args.add(new MethodCallInfo.Argument( 
                            argText, actualStart, actualEnd, argType, false, samConflictError
                        ));
                    } else {
                        args.add(new MethodCallInfo.Argument( 
                            argText, actualStart, actualEnd, argType, true, null
                        ));
                    }
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
     *
     * @param argText The argument text
     * @param position The position in the document
     * @param expectedType Optional expected parameter type for validation
     * @return The resolved type of the argument
     */
    private TypeInfo resolveArgumentType(String argText, int position, TypeInfo expectedType) {
        argText = argText.trim();
        
        // Check if this looks like a parameter declaration: "Type varName"
        // Pattern: identifier followed by whitespace and another identifier
        if (argText.matches("^[A-Za-z_][a-zA-Z0-9_<>\\[\\],\\s\\n\\r]*\\s+[a-zA-Z_][a-zA-Z0-9_]*$")) {
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

        // Otherwise, treat it as an expression with expected type context
        if (expectedType != null) {
            ExpressionTypeResolver.CURRENT_EXPECTED_TYPE = expectedType;
            try {
                return resolveExpressionType(argText, position);
            } finally {
                ExpressionTypeResolver.CURRENT_EXPECTED_TYPE = null;
            }
        }

        // No expected type - resolve normally
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

    private TypeInfo narrowIntLiteral(String literalText) {
        if (isJavaScript()) 
            return TypeInfo.NUMBER;
        
        TypeInfo expectedType = ExpressionTypeResolver.CURRENT_EXPECTED_TYPE;
        if (expectedType != null) {
            TypeInfo narrowed = TypeChecker.narrowLiteralToExpectedType(literalText, expectedType);
            if (narrowed != null) {
                return narrowed;
            }
        }
        return TypeInfo.fromPrimitive("int");
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
    public TypeInfo resolveExpressionType(String expr, int position) {
        expr = expr.trim();
        expr = stripLineComments(expr);

        if (expr.endsWith(".")) 
            expr = expr.substring(0, expr.length() - 1).trim();
        
        if (expr.isEmpty()) 
            return null;

        if (isJavaScript() && expr.charAt(0) == '{') {
            ObjectLiteralParser.ObjectLiteralAnalysis analysis = objectLiterals.get(position);
            if (analysis == null) 
                analysis = ObjectLiteralParser.parse(expr, position, true, false, this::resolveExpressionType, this::getScriptMethodInfo);
            
            if (analysis == null) {
                return TypeInfo.ANY;
            }
            if (!analysis.supportsInference || analysis.inferredType == null) {
                return TypeInfo.ANY;
            }
            return analysis.inferredType;
        }
        
         // Check if expression contains operators - if so, use the full expression resolver
         // Handle cast expressions: (Type)expr, ((Type)expr).method(), etc
         // Also route JS function expressions and arrow lambdas through the parser
         if (containsOperators(expr) || expr.contains("::") || expr.contains("->") || expr.contains("=>") || expr.startsWith("(") || looksLikeFunctionOrLambda(expr)) {
              return resolveExpressionWithParserAPI(expr, position);
          }
        
        // JS array literals: [], [a, b], ["hi", "bye"]
        if (isJavaScript() && expr.startsWith("[") && expr.endsWith("]")) {
            String inner = expr.substring(1, expr.length() - 1).trim();
            return TypeInfo.arrayOf(unifyJsArrayElementType(inner, position + 1));
        }

        // Invalid expressions starting with brackets
        if (expr.startsWith("[") || expr.startsWith("]")) {
            return null; // Invalid syntax
        }
        
        // String literals
        if (expr.startsWith("\"") && expr.endsWith("\"")) {
            return TypeInfo.string();
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
            return TypeInfo.NULL; // null is compatible with any reference type
        }
        
        // Numeric literals with precision checking
        // Float: can be 10f, 10.5f, 10.f, .5f
        if (expr.matches("-?\\d*\\.?\\d+[fF]")) {
            if (isJavaScript()) 
                return TypeInfo.NUMBER;
            
            // Check if it has too many decimal places for float (>7 significant digits)
            // If so, treat it as double (causing type mismatch)
            if (hasExcessivePrecision(expr, 7)) {
                return TypeInfo.fromPrimitive("double");
            }
            return TypeInfo.fromPrimitive("float");
        }
        // Double: can be 10d, 10.5d, 10.5, .5, 10., but NOT plain integers
        if (expr.matches("-?\\d*\\.\\d+[dD]?") || expr.matches("-?\\d+\\.[dD]?") || expr.matches("-?\\d+[dD]")) {
            if (isJavaScript()) 
                return TypeInfo.NUMBER;
            
            // Check if it has too many decimal places for double (>15 significant digits)
            // Return null to indicate the literal is invalid/unrepresentable
            if (hasExcessivePrecision(expr, 15)) {
                return null; // Exceeds double precision
            }
            return TypeInfo.fromPrimitive("double");
        }
        // Long: 10L or 10l
        if (expr.matches("-?\\d+[lL]")) {
            if (isJavaScript()) 
                return TypeInfo.NUMBER;
            
            return TypeInfo.fromPrimitive("long");
        }
        // Hex integer literals: 0x7F, -0x80, 0X1A
        if (expr.matches("[-+]?0[xX][0-9a-fA-F][0-9a-fA-F_]*")) {
            return narrowIntLiteral(expr);
        }
        // Binary integer literals: 0b1010, -0b1100, 0B1111
        if (expr.matches("[-+]?0[bB][01][01_]*")) {
            return narrowIntLiteral(expr);
        }
        // Int: plain integers without suffix
        if (expr.matches("[-+]?\\d[\\d_]*")) {
            return narrowIntLiteral(expr);
        }
        
        // "this" keyword
        if (expr.equals("this")) {
            return resolveThisType(position);
        }
        
        if (expr.startsWith("new ")) {
            Matcher newMatcher = NEW_TYPE_PATTERN.matcher(expr);
            if (newMatcher.find()) {
                String typeName = newMatcher.group(1);
                FieldInfo varInfo = resolveVariable(typeName, position);
                if (varInfo != null && varInfo.getTypeInfo() instanceof ClassTypeInfo) {
                    return ((ClassTypeInfo) varInfo.getTypeInfo()).getInstanceType();
                }
                if (isJavaScript() && typeName.equals("Array")) {
                    String rest = expr.substring(newMatcher.end()).trim();
                    String argsText = (rest.startsWith("(") && rest.endsWith(")"))
                            ? rest.substring(1, rest.length() - 1).trim()
                            : "";
                    return TypeInfo.arrayOf(unifyJsArrayElementType(argsText, position));
                }
                TypeInfo baseType = resolveType(typeName, position);
                if (baseType == null) return null;
                
                // Parameterize with generic type arguments (e.g., new Box<String>(...))
                String typeArgsClause = newMatcher.group(2);
                if (typeArgsClause != null && !typeArgsClause.trim().isEmpty()) {
                    int typeArgsTextOffset = position + newMatcher.start(2);
                    baseType = parameterizeWithClause(baseType, typeArgsClause, position, typeArgsTextOffset);
                }
                
                String rest = expr.substring(newMatcher.end());
                int dims = 0;
                for (int i = 0; i < rest.length() && rest.charAt(i) != '('; i++) {
                    if (rest.charAt(i) == '[') { dims++; i = rest.indexOf(']', i); }
                }
                for (int i = 0; i < dims; i++) baseType = TypeInfo.arrayOf(baseType);
                return baseType;
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
            currentType = resolveThisType(position);
            // Handle this.field where we don't have a script type
            if (currentType == null && segments.size() > 1 && !segments.get(1).isMethodCall) {
                String fieldName = segments.get(1).name;
                if (globalFields.containsKey(fieldName)) {
                    currentType = globalFields.get(fieldName).getTypeInfo();
                    currentType = applyBracketAccess(currentType, segments.get(1));
                    // Continue from segment 2
                    for (int i = 2; i < segments.size(); i++) {
                        currentType = resolveChainSegment(currentType, segments.get(i));
                        currentType = applyBracketAccess(currentType, segments.get(i));
                        if (currentType == null) return null;
                    }
                    return currentType;
                }
            }
        } else if (first.name.equals("super")) {
            // Resolve super to parent class
            ScriptTypeInfo enclosingType = findEnclosingScriptType(position);
            if (enclosingType != null && enclosingType.hasSuperClass()) {
                currentType = enclosingType.getSuperClass();
            } else {
                return null; // No parent class
            }
        } else {
            // Check if first segment is a type name for static access
            TypeInfo typeCheck = resolveType(first.name, position);
            if (typeCheck != null && typeCheck.isResolved()) {
                currentType = typeCheck;
            }
        }
        
        if (currentType == null && !first.isMethodCall) {
            // Regular variable
            FieldInfo varInfo = resolveVariable(first.name, position);
            if (varInfo != null) {
                currentType = varInfo.getTypeInfo();
            } else if (isScriptMethod(first.name)) {
                // Script method reference used as an expression.
                // In JS, this is commonly used as a SAM callback (e.g., schedule("id", actionFunction)).
                TypeInfo expectedType = ExpressionTypeResolver.CURRENT_EXPECTED_TYPE;
                TypeInfo samType = resolveScriptMethodAsSam(first.name, expectedType);
                if (samType != null) {
                    currentType = samType;
                } else if (isJavaScript()) {
                    // Provide a non-null placeholder type so overload selection can prefer functional-interface params.
                    currentType = TypeInfo.unresolved("<script_method_ref>", "__script_method_ref__");
                }
            }
        } else {
            // First segment is a method call - check script methods
            if (isScriptMethod(first.name)) {
                MethodInfo scriptMethod = getScriptMethodInfo(first.name);
                if (scriptMethod != null) {
                    currentType = scriptMethod.getReturnType();
                }
            }
            if (currentType == null && isGlobalEngineFunction(first.name)) {
                currentType = getGlobalEngineFunctionReturnType(first.name);
            }
            if (currentType == null) {
                // Implicit 'this' call: bare method name inside a ScriptTypeInfo body (e.g. getYaw() inside AbilityLine.foo())
                ScriptTypeInfo enclosingType = findEnclosingScriptType(position);
                if (enclosingType != null && enclosingType.hasMethodInHierarchy(first.name)) {
                    MethodInfo implicitMethod = enclosingType.getMethodInfoInHierarchy(first.name);
                    if (implicitMethod != null) {
                        currentType = implicitMethod.getReturnType();
                    }
                }
            }
        }

        currentType = applyBracketAccess(currentType, first);

        // Resolve the rest of the chain
        for (int i = 1; i < segments.size(); i++) {
            currentType = resolveChainSegment(currentType, segments.get(i));
            currentType = applyBracketAccess(currentType, segments.get(i));
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
            
            boolean hasArrayAccess = false;
            String bracketKey = null;
            while (i < expr.length() && Character.isWhitespace(expr.charAt(i))) {
                i++;
            }
            if (i < expr.length() && expr.charAt(i) == '[') {
                hasArrayAccess = true;
                int bracketStart = i + 1;
                int depth = 1;
                i++;
                while (i < expr.length() && depth > 0) {
                    char c = expr.charAt(i);
                    if (c == '[') depth++;
                    else if (c == ']') depth--;
                    i++;
                }
                String bracketContent = expr.substring(bracketStart, i - 1).trim();
                if (bracketContent.length() >= 2) {
                    char q = bracketContent.charAt(0);
                    if ((q == '"' || q == '\'') && bracketContent.charAt(bracketContent.length() - 1) == q) {
                        bracketKey = bracketContent.substring(1, bracketContent.length() - 1);
                    }
                }
            }

            segments.add(new ChainSegment(name, start, i, isMethodCall, arguments, hasArrayAccess, bracketKey));
            
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
    TypeInfo resolveReceiverChain(int methodNameStart) {
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
     * For JavaScript array literals, unify the element types to determine the array's element type.
     */
    private TypeInfo unifyJsArrayElementType(String argsText, int position) {
        if (argsText.isEmpty()) return TypeInfo.ANY;
        TypeInfo[] elementTypes = parseArgumentTypes(argsText, position);
        if (elementTypes.length == 1 && TypeInfo.NUMBER.equals(elementTypes[0])) return TypeInfo.ANY;
        TypeInfo common = null;
        boolean allSame = true;
        for (TypeInfo t : elementTypes) {
            boolean isUnresolvable = t == null || TypeInfo.NULL.equals(t);
            if (isUnresolvable) continue;
            if (common == null) {
                common = t;
            } else if (!common.equals(t)) {
                allSame = false;
                break;
            }
        }
        return allSame && common != null ? common : TypeInfo.ANY;
    }

    /**
     * Helper class for chain segments (can be field access or method call).
     */
    private static class ChainSegment {
        final String name;
        final int start;
        final int end;
        final boolean isMethodCall;
        final String arguments;
        final boolean hasArrayAccess;
        final String bracketKey;

        ChainSegment(String name, int start, int end, boolean isMethodCall, String arguments, boolean hasArrayAccess, String bracketKey) {
            this.name = name;
            this.start = start;
            this.end = end;
            this.isMethodCall = isMethodCall;
            this.arguments = arguments;
            this.hasArrayAccess = hasArrayAccess;
            this.bracketKey = bracketKey;
        }
    }

    /**
     *
     */
    private String stripLineComments(String expr) {
        if (!expr.contains("//"))
            return expr;

        String[] lines = expr.split("\n", -1);
        StringBuilder result = new StringBuilder();
        for (String line : lines) {
            int ci = findLineCommentStart(line);
            String part = (ci >= 0 ? line.substring(0, ci) : line).trim();
            if (!part.isEmpty()) {
                if (result.length() > 0) result.append(' ');
                result.append(part);
            }
        }
        return result.toString().trim();
    }

    private static int findLineCommentStart(String line) {
        boolean inString = false;
        char stringChar = 0;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inString) {
                if (c == '\\' && i + 1 < line.length()) {
                    i++;
                    continue;
                }
                if (c == stringChar) {
                    inString = false;
                }
                continue;
            }
            if (c == '"' || c == '\'') {
                inString = true;
                stringChar = c;
                continue;
            }
            if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '/') {
                return i;
            }
        }
        return -1;
    }

    /**
     * Handle ObjectLiteral bracket access (e.g., obj["field"] or obj[fieldName]) and array access (e.g., items[0]).
     * @param type
     * @param seg
     * @return
     */
    private TypeInfo applyBracketAccess(TypeInfo type, ChainSegment seg) {
        if (type == null || !seg.hasArrayAccess) return type;
        if (seg.bracketKey != null) {
            FieldInfo f = type.getFieldInfo(seg.bracketKey);
            return f != null ? f.getTypeInfo() : TypeInfo.ANY;
        }
        if (type.isSyntheticObjectLiteralType()) return TypeInfo.ANY;
        return unwrapArrayElement(type);
    }
    /**
     * Extract the element type from an array type, using multiple fallback strategies.
     * 
     * Used when a chain segment has array subscript notation (e.g., items[1]).
     * Given an array type like IItemStack[], returns IItemStack so that subsequent
     * chain resolution (e.g., .getName()) operates on the element type, not the array type.
     * 
     * Strategy (in priority order):
     * 1. If TypeInfo stores elementType (preferred, from arrayOf factory), use it directly
     * 2. If fullName has [] suffix (legacy or JS types), strip it and re-resolve
     * 3. If underlying javaClass is an array, use Class.getComponentType()
     * 4. Fallback to ANY type to prevent null propagation
     * 
     * @param arrayType The array type to unwrap
     * @return The element type, or TypeInfo.ANY if unwrapping fails
     */
    private TypeInfo unwrapArrayElement(TypeInfo arrayType) {
        if (arrayType == null) return null;
        TypeInfo el = arrayType.getElementType();
        if (el != null) return el;
        String name = arrayType.getFullName();
        if (name != null && name.endsWith("[]")) {
            return resolveType(name.substring(0, name.length() - 2));
        }
        if (arrayType.getJavaClass() != null && arrayType.getJavaClass().isArray()) {
            return TypeInfo.fromClass(arrayType.getJavaClass().getComponentType());
        }
        return TypeInfo.ANY;
    }

    /**
     * Resolve a single segment of a chain given the current type context.
     */
    private TypeInfo resolveChainSegment(TypeInfo currentType, ChainSegment segment) {
        if (currentType == null || !currentType.isResolved()) {
            return null;
        }

        // Check if current type is a synthetic type
                if (isJavaScript()) {
                    SyntheticType syntheticType = typeResolver.getSyntheticType(currentType.getSimpleName());
                    if (syntheticType != null) {
                        return resolveSyntheticChainSegment(syntheticType, segment);
                    }
                }
        
        if (segment.isMethodCall) {
            // Method call - get return type with argument-based overload resolution
            // Check for method existence using hierarchy search if it's a ScriptTypeInfo
            boolean hasMethod = false;
            TypeInfo rawType = currentType.getRawType();
            if (rawType instanceof ScriptTypeInfo) {
                hasMethod = ((ScriptTypeInfo) rawType).hasMethodInHierarchy(segment.name);
            } else {
                hasMethod = currentType.hasMethod(segment.name);
            }

            if (hasMethod) {
                // Parse argument types from the method call
                TypeInfo[] argTypes = parseArgumentTypes(segment.arguments, segment.start);
                
                // Get the best matching overload based on argument types
                // getBestMethodOverload is now overridden in ScriptTypeInfo to search hierarchy
                MethodInfo methodInfo = currentType.getBestMethodOverload(segment.name, argTypes);
                return (methodInfo != null) ? methodInfo.getReturnType() : null;
            }
            return null;
        } else {
            // Field access - use hierarchy search for ScriptTypeInfo
            boolean hasField = false;
            FieldInfo fieldInfo = null;

            TypeInfo rawType = currentType.getRawType();
            if (rawType instanceof ScriptTypeInfo) {
                hasField = ((ScriptTypeInfo) rawType).hasFieldInHierarchy(segment.name);
                if (hasField) {
                    fieldInfo = currentType.getFieldInfo(segment.name);
                }
            } else {
                hasField = currentType.hasField(segment.name);
                if (hasField) {
                    fieldInfo = currentType.getFieldInfo(segment.name);
                }
            }

            if (hasField) {
                return (fieldInfo != null) ? fieldInfo.getTypeInfo() : null;
            }

            // Field not found — check if the segment names an inner class.
            // This handles chains like Outer.Inner where Inner is a nested type, not a field.
            if (rawType instanceof ScriptTypeInfo) {
                ScriptTypeInfo innerClass = ((ScriptTypeInfo) rawType).getInnerClass(segment.name);
                if (innerClass != null) {
                    return innerClass;
                }
            }
            // Also check Java inner classes via reflection (e.g., Map.Entry)
            if (currentType.getJavaClass() != null) {
                try {
                    for (Class<?> nested : currentType.getJavaClass().getDeclaredClasses()) {
                        if (java.lang.reflect.Modifier.isPublic(nested.getModifiers())
                                && nested.getSimpleName().equals(segment.name)) {
                            return TypeInfo.fromClass(nested);
                        }
                    }
                } catch (SecurityException ignored) { }
            }

            return null;
        }
    }

    /**
     * Resolve a chain segment on a synthetic type (like Nashorn's Java object).
     */
    /**
     * Resolve a chain segment for a synthetic type (method call or field access).
     * Handles dynamic return type resolution for methods like Java.type().
     */
    private TypeInfo resolveSyntheticChainSegment(SyntheticType syntheticType, ChainSegment segment) {
        if (segment.isMethodCall) {
            SyntheticMethod method = syntheticType.getMethod(segment.name);
            if (method != null) {
                // For methods with dynamic return type resolvers (like Java.type),
                // extract string arguments and resolve
                if (segment.arguments != null) {
                    String[] args = TypeResolver.parseStringArguments(segment.arguments);
                    TypeInfo resolved = method.resolveReturnType(args);
                    if (resolved != null) {
                        return resolved;
                    }
                }
                // Fall back to static return type
                TypeInfo returnType = typeResolver.resolve(method.returnType);
                return returnType != null ? returnType : TypeInfo.unresolved(method.returnType, method.returnType);
            }
        } else {
            SyntheticField field = syntheticType.getField(segment.name);
            if (field != null) {
                TypeInfo fieldType = typeResolver.resolve(field.typeName);
                return fieldType != null ? fieldType : TypeInfo.unresolved(field.typeName, field.typeName);
            }
        }
        return null;
    }

    /**
     * Resolve the type of "this" at a given position by finding the innermost scope and checking for containing object types.
     * For both Java and JavaScript, this will correctly resolve to the appropriate type based on the current context (e.g., enclosing class, script type, or synthetic type).
     * @param position
     * @return
     */
    TypeInfo resolveThisType(int position) {
        Object innermostScope = findInnermostScopeAt(position);
        if (innermostScope instanceof InnerCallableScope) {
            TypeInfo objectType = ((InnerCallableScope) innermostScope).getContainingObjectType();
            if (objectType != null && objectType.isResolved()) {
                return objectType;
            }
        }
        return findEnclosingScriptType(position);
    }

    // ==================== OPERATOR EXPRESSION RESOLUTION ====================
    
    /**
     * Check if an expression contains operators that need advanced resolution.
     * This is a quick heuristic check - it may have false positives for operators
     * inside strings, but those are handled by the full parser.
     */
    public boolean containsOperators(String expr) {
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
                        if (Character.isJavaIdentifierPart(prev) || prev == ')' || prev == ']' || prev == '"' || prev == '\'' || Character.isDigit(prev)) {
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
                    // <<, <= are always operators
                    if (next == '<' || next == '=') return true;
                    // If preceded by digit/)/] it's definitely a relational operator (not a generic)
                    prevIdx = i - 1;
                    while (prevIdx >= 0 && Character.isWhitespace(expr.charAt(prevIdx))) prevIdx--;
                    if (prevIdx >= 0) {
                        char prevC = expr.charAt(prevIdx);
                        if (Character.isDigit(prevC) || prevC == ')' || prevC == ']') return true;
                    }
                    // Generics follow an uppercase type name; anything else is relational
                    int nextNonSpace = i + 1;
                    while (nextNonSpace < expr.length() && Character.isWhitespace(expr.charAt(nextNonSpace))) nextNonSpace++;
                    if (nextNonSpace < expr.length() && !Character.isUpperCase(expr.charAt(nextNonSpace))) {
                        return true;
                    }
                    break;
                    
                case '>':
                    // >>, >= are always operators
                    if (next == '>' || next == '=') return true;
                    // If preceded by digit/)/] it's definitely a relational operator (not closing generic)
                    prevIdx = i - 1;
                    while (prevIdx >= 0 && Character.isWhitespace(expr.charAt(prevIdx))) prevIdx--;
                    if (prevIdx >= 0) {
                        char prevC = expr.charAt(prevIdx);
                        if (Character.isDigit(prevC) || prevC == ')' || prevC == ']') return true;
                    }
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
     * Check if an expression looks like a JS function or arrow lambda.
     * This is a fast heuristic to detect:
     * - JS function expressions: function(...) {}
     * - Arrow lambdas: (...) => ...
     * These need to be routed through the parser for proper SAM typing.
     */
    private boolean looksLikeFunctionOrLambda(String expr) {
        if (expr == null || expr.isEmpty()) return false;
        String trimmed = expr.trim();
        if (trimmed.startsWith("function")) return true;
        if (trimmed.contains("=>")) return true;
        if (trimmed.contains("->")) return true;
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
        // Pass the position as basePosition so lambda position calculations work correctly
        ExpressionTypeResolver resolver = new ExpressionTypeResolver(context, this, position);
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
                if ("this".equals(name)) {
                    return resolveThisType(position);
                }
                if ("super".equals(name)) {
                    // Resolve super to parent class type
                    ScriptTypeInfo enclosingType = findEnclosingScriptType(position);
                    if (enclosingType != null && enclosingType.hasSuperClass()) {
                        return enclosingType.getSuperClass();
                    }
                    return null;
                }
                if ("true".equals(name) || "false".equals(name)) {
                    return TypeInfo.fromPrimitive("boolean");
                }
                if ("null".equals(name)) {
                    return TypeInfo.unresolved("null", "<null>");
                }
                
                // Variable shadowing: variables take precedence over script methods
                FieldInfo varInfo = resolveVariable(name, position);
                if (varInfo != null) {
                    return varInfo.getTypeInfo();
                }
                
                if (name.length() > 0) {
                    TypeInfo typeCheck = resolveType(name, position);
                    if (typeCheck != null && typeCheck.isResolved()) {
                        return typeCheck;
                    }
                }
                
                // Named script function as SAM callback: schedule("id", actionFunction)
                TypeInfo expectedType = ExpressionTypeResolver.CURRENT_EXPECTED_TYPE;
                TypeInfo samType = resolveScriptMethodAsSam(name, expectedType);
                if (samType != null) {
                    return samType;
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
                    if (isGlobalEngineFunction(methodName)) {
                        return getGlobalEngineFunctionReturnType(methodName);
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
            return TypeInfo.string();
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
        // Hex integer literals: 0x7F, -0x80, 0X1A
        if (expr.matches("[-+]?0[xX][0-9a-fA-F][0-9a-fA-F_]*")) {
            return narrowIntLiteral(expr);
        }
        // Binary integer literals: 0b1010, -0b1100, 0B1111
        if (expr.matches("[-+]?0[bB][01][01_]*")) {
            return narrowIntLiteral(expr);
        }
        if (expr.matches("[-+]?\\d[\\d_]*")) {
            return narrowIntLiteral(expr);
        }
        
        // "this" keyword
        if (expr.equals("this")) {
            return resolveThisType(position);
        }
        
        // "new Type()" expressions
        if (expr.startsWith("new ")) {
            Matcher newMatcher = NEW_TYPE_PATTERN.matcher(expr);
            if (newMatcher.find()) {
                String typeName = newMatcher.group(1);
                
                // First check if it's a variable holding a ClassTypeInfo (like var File = Java.type("java.io.File"))
                FieldInfo varInfo = resolveVariable(typeName, position);
                if (varInfo != null && varInfo.getTypeInfo() instanceof ClassTypeInfo) {
                    // It's a variable holding a class reference, return the wrapped class
                    ClassTypeInfo classRef = (ClassTypeInfo) varInfo.getTypeInfo();
                    return classRef.getInstanceType();
                }
                
                // Otherwise treat it as a type name
                TypeInfo baseType = resolveType(typeName);
                if (baseType != null) {
                    String typeArgsClause = newMatcher.group(2);
                    if (typeArgsClause != null && !typeArgsClause.trim().isEmpty()) {
                        int typeArgsTextOffset = position + newMatcher.start(2);
                        baseType = parameterizeWithClause(baseType, typeArgsClause, position, typeArgsTextOffset);
                    }
                }
                return baseType;
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
            currentType = resolveThisType(position);
        } else {
            // Check if first segment is a type name
            TypeInfo typeCheck = resolveType(first.name, position);
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
            if (currentType == null && isGlobalEngineFunction(first.name)) {
                currentType = getGlobalEngineFunctionReturnType(first.name);
            }
        }

        currentType = applyBracketAccess(currentType, first);

        for (int i = 1; i < segments.size(); i++) {
            currentType = resolveChainSegment(currentType, segments.get(i));
            currentType = applyBracketAccess(currentType, segments.get(i));
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
    int[] findReceiverBoundsBefore(int dotIndex) {
        if (dotIndex < 0 || dotIndex >= text.length() || text.charAt(dotIndex) != '.') {
            return null;
        }

        int pos = dotIndex - 1;
        // Skip whitespace
        while (pos >= 0 && Character.isWhitespace(text.charAt(pos))) pos--;
        if (pos < 0) return null;

        while (pos >= 0) {
            // Check if we're in an excluded range (comment, string, etc.)
            // Skip excluded regions to avoid picking up types from comment text
            if (isExcluded(pos)) {
                // Find the excluded range and skip to before it
                for (int[] range : excludedRanges) {
                    if (pos >= range[0] && pos < range[1]) {
                        pos = range[0] - 1; // Jump to before the excluded range
                        break;
                    }
                }
                continue; // Continue scanning from before the excluded range
            }
            
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
                // Skip whitespace to check for chained identifier (preceded by a dot)
                int checkPos = pos;
                while (checkPos >= 0 && Character.isWhitespace(text.charAt(checkPos))) checkPos--;
                // If it's part of a chained identifier (preceded by a dot), continue
                if (checkPos >= 0 && text.charAt(checkPos) == '.') { pos = checkPos - 1; continue; }
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
     * Find the innermost script-defined type that contains the given position.
     * Uses recursive descent: starts from top-level types in scriptTypes, then drills
     * into inner classes. This is O(depth) rather than O(total_types).
     */
    public ScriptTypeInfo findEnclosingScriptType(int position) {
for (ScriptTypeInfo type:scriptTypes.values()) {
        if (type.containsPosition(position)) {
        return findInnermostType(type,position);
    }
                                  }
                 return null;
        }

    /**
     * Recursively descend into inner classes to find the innermost type containing the position.
     * At each level, if an inner class contains the position, recurse into it.
     * If no inner class matches, the current parent is the innermost enclosing type.
     */
    private ScriptTypeInfo findInnermostType(ScriptTypeInfo parent, int position) {
        for (ScriptTypeInfo inner : parent.getInnerClasses()) {
            if (inner.containsPosition(position)) {
                return findInnermostType(inner, position);
            }
        }
        return parent;
    }

    /**
     * Find the constructor that contains the given position.
     * Used for validating super() calls.
     */
    private MethodInfo findEnclosingConstructor(int position) {
        ScriptTypeInfo enclosingType = findEnclosingScriptType(position);
        if (enclosingType == null)
            return null;

        for (MethodInfo constructor : enclosingType.getConstructors()) {
            int bodyStart = constructor.getBodyStart();
            int bodyEnd = constructor.getBodyEnd();
            if (bodyStart >= 0 && bodyEnd > bodyStart && position >= bodyStart && position < bodyEnd) {
                return constructor;
            }
        }
        return null;
    }

    /**
     * Handle super() constructor calls with validation and argument matching.
     */
    private void handleSuperConstructorCall(List<ScriptLine.Mark> marks, int nameStart, int nameEnd, int openParen,
                                            int closeParen) {
        // Validate that we're in a constructor
        TokenErrorMessage errorMsg = null;

        //Parent class
        ScriptTypeInfo enclosingType = findEnclosingScriptType(nameStart);
        MethodInfo enclosingConstructor = findEnclosingConstructor(nameStart);
        TypeInfo superClass = enclosingType != null ? enclosingType.getSuperClass() : null;

        // Parse arguments and find matching parent constructor
        List<MethodCallInfo.Argument> arguments = parseMethodArguments(openParen + 1, closeParen, null, superClass);
        TypeInfo[] argTypes = arguments.stream().map(MethodCallInfo.Argument::getResolvedType).toArray(TypeInfo[]::new);

        // Find matching constructor in parent class
        MethodInfo parentConstructor = superClass != null ? superClass.findConstructor(argTypes) : null;

        if (enclosingType == null || !enclosingType.hasSuperClass()) {
            // No parent class - error
            errorMsg = TokenErrorMessage
                    .from(enclosingType == null ? "'super()' can only be used within a class" : "Class '" + enclosingType.getSimpleName() + "' does not have a parent class")
                    .clearOtherErrors();
        } else if (enclosingConstructor == null) {
            // Not in a constructor - error
            errorMsg = TokenErrorMessage.from("Call to 'super()' only allowed in constructor body").clearOtherErrors();
        } else if (superClass == null || !superClass.isResolved()) {
            // Parent class not resolved - error
            errorMsg = TokenErrorMessage.from(
                    "Cannot resolve parent class '" + enclosingType.getSuperClassName() + "'");
        } else if (parentConstructor == null) {
            // No matching constructor found
            String argTypeStr = java.util.Arrays.stream(argTypes)
                                                .map(t -> t != null ? t.getSimpleName() : "unknown")
                                                .collect(java.util.stream.Collectors.joining(", "));
            errorMsg = TokenErrorMessage
                    .from("No constructor found in '" + superClass.getSimpleName() + "' matching super(" + argTypeStr + ")")
                    .clearOtherErrors();
        }
        
        // Successfully resolved - mark as a valid method call (constructor call)
        MethodCallInfo callInfo = new MethodCallInfo("super", nameStart, nameEnd, openParen, closeParen, arguments,
                superClass, parentConstructor, false).setConstructor(true);
        callInfo.validate();
        methodCalls.add(callInfo);
        marks.add(new ScriptLine.Mark(nameStart, nameEnd, TokenType.IMPORT_KEYWORD,
                errorMsg != null ? errorMsg : callInfo));
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

    private TypeInfo resolveScriptMethodAsSam(String methodName, TypeInfo expectedType) {
        if (methodName == null || expectedType == null || !expectedType.isFunctionalInterface() || !isScriptMethod(methodName)) {
            return null;
        }

        // Java mode: bare method names are invalid as SAM callbacks
        if (!isJavaScript()) {
            CURRENT_SAM_CONFLICT_ERROR.set(
                "Bare method name '" + methodName + "' cannot be used as callback in Java. Use this::" + methodName
            );
            return expectedType;
        }

        MethodInfo sam = expectedType.getSingleAbstractMethod();
        if (sam == null) {
            return null;
        }

        int samArity = sam.getParameters().size();
        MethodInfo bestMatch = getScriptMethodBySamArity(methodName, samArity);
        if (bestMatch == null) {
            return null;
        }

        injectSamParameterTypes(bestMatch, sam);
        return expectedType;
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
     MethodInfo getScriptMethodInfo(String methodName, TypeInfo[] argTypes) {
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
     * Get the best matching script method for use as a SAM callback by arity.
     * Returns null if no match found, or if multiple overloads match by arity (ambiguous).
     * 
     * @param methodName Name of the script method
     * @param samArity Number of parameters in the SAM interface method
     * @return The matching MethodInfo, or null if no unambiguous match
     */
    private MethodInfo getScriptMethodBySamArity(String methodName, int samArity) {
        List<MethodInfo> matchingByArity = new ArrayList<>();
        
        for (MethodInfo method : methods) {
            if (method.getName().equals(methodName)) {
                if (method.getParameters().size() == samArity) {
                    matchingByArity.add(method);
                }
            }
        }
        
        if (matchingByArity.size() == 1) {
            return matchingByArity.get(0);
        }
        
        if (matchingByArity.isEmpty()) {
            return getScriptMethodInfo(methodName);
        }
        
        CURRENT_SAM_CONFLICT_ERROR.set(
            "Ambiguous overload for '" + methodName + "' with " + samArity + " parameter(s): " +
            matchingByArity.size() + " overloads match"
        );
        return null;
    }

    /**
     * True if in JS mode and name is a registered global engine function (parseInt, parseFloat, etc.).
     */
    private boolean isGlobalEngineFunction(String name) {
        return isJavaScript() && JSTypeRegistry.getInstance().isGlobalEngineFunction(name);
    }

    /**
     * Return the resolved return type of a global engine function call, or null if unknown.
     */
    private TypeInfo getGlobalEngineFunctionReturnType(String name) {
        JSMethodInfo m = JSTypeRegistry.getInstance().getGlobalEngineFunction(name);
        return m != null ? m.getResolvedReturnType(null) : null;
    }
    
    private void injectSamParameterTypes(MethodInfo scriptMethod, MethodInfo sam) {
        String methodName = scriptMethod.getName();
        
        MethodInfo previousSam = scriptMethodSamContexts.get(methodName);
        if (previousSam != null) {
            if (!areSamSignaturesCompatible(previousSam, sam)) {
                String existingSig = formatSamSignature(previousSam);
                String newSig = formatSamSignature(sam);
                CURRENT_SAM_CONFLICT_ERROR.set(
                    "Function '" + methodName + "' used in incompatible SAM contexts: " +
                    existingSig + " vs " + newSig
                );
                return;
            }
        } else {
            scriptMethodSamContexts.put(methodName, sam);
        }
        
        List<FieldInfo> scriptParams = scriptMethod.getParameters();
        List<FieldInfo> samParams = sam.getParameters();
        
        if (scriptParams.size() != samParams.size()) {
            return;
        }
        
        for (int i = 0; i < scriptParams.size(); i++) {
            FieldInfo scriptParam = scriptParams.get(i);
            TypeInfo samParamType = samParams.get(i).getTypeInfo();
            
            if (samParamType == null) {
                continue;
            }
            
            TypeInfo declaredType = scriptParam.getDeclaredType();
            
            boolean hasExplicitType = declaredType != null 
                    && declaredType.isResolved() 
                    && !"any".equals(declaredType.getFullName());
            
            if (hasExplicitType) {
                if (!TypeChecker.isTypeCompatible(declaredType, samParamType)) {
                    scriptMethod.addSamTypeError(i, samParamType, declaredType);
                }
            } else {
                scriptParam.setInferredType(samParamType);
            }
        }
    }
    
    private boolean areSamSignaturesCompatible(MethodInfo sam1, MethodInfo sam2) {
        List<FieldInfo> params1 = sam1.getParameters();
        List<FieldInfo> params2 = sam2.getParameters();
        
        if (params1.size() != params2.size()) {
            return false;
        }
        
        for (int i = 0; i < params1.size(); i++) {
            TypeInfo type1 = params1.get(i).getTypeInfo();
            TypeInfo type2 = params2.get(i).getTypeInfo();
            
            if (type1 == null || type2 == null) {
                continue;
            }
            
            if (!TypeChecker.isTypeCompatible(type1, type2) && !TypeChecker.isTypeCompatible(type2, type1)) {
                return false;
            }
        }
        
        TypeInfo return1 = sam1.getReturnType();
        TypeInfo return2 = sam2.getReturnType();
        
        if (return1 != null && return2 != null) {
            if (!TypeChecker.isTypeCompatible(return1, return2) && !TypeChecker.isTypeCompatible(return2, return1)) {
                return false;
            }
        }
        
        return true;
    }
    
    private String formatSamSignature(MethodInfo sam) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        List<FieldInfo> params = sam.getParameters();
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) sb.append(", ");
            TypeInfo type = params.get(i).getTypeInfo();
            sb.append(type != null ? type.getSimpleName() : "?");
        }
        sb.append(") -> ");
        TypeInfo returnType = sam.getReturnType();
        sb.append(returnType != null ? returnType.getSimpleName() : "void");
        return sb.toString();
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
        for (ScriptTypeInfo scriptType : scriptTypesByFullName.values()) {
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

        if (isJavaScript()) {
            knownKeywords.add("delete");
            knownKeywords.add("undefined");
        }

        // First pass: mark method parameters in their declaration positions
        List<MethodInfo> allMethods = getAllMethods();
        allMethods.addAll(getAllConstructors());

        for (MethodInfo method : allMethods) {
            for (FieldInfo param : method.getParameters()) {
                int pos = param.getDeclarationOffset();
                String name = param.getName();
                marks.add(new ScriptLine.Mark(pos, pos + name.length(), TokenType.PARAMETER, param));
            }
        }

        Pattern identifier = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\b");
        Matcher m = identifier.matcher(text);

        identifierLoop:
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

            // Scope resolution order:
            // 1. Inner callable scope parameters (lambda/function expressions)
            // 2. Inner callable scope locals
            // 3. Method parameters (if inside method)
            // 4. Method local variables (if inside method)
            // 5. Enclosing type fields (if inside method)
            // 6. Global fields
            // 7. Script type fields

            boolean breakOuterLoop = false;
            // Mark the parameter name at its declaration site in the parameter list.
            // Example: marks the first 'item' in func = (item) => item + 1
            // Needed because the header region is outside the scope body, so findInnermostScopeAt won't find it.
            for (InnerCallableScope scope : innerScopes) {
                for (FieldInfo param : scope.getParameters()) {
                    int declStart = param.getDeclarationOffset();
                    if (declStart == m.start(1) && name.equals(param.getName())) {
                        marks.add(new ScriptLine.Mark(m.start(1), m.end(1), TokenType.PARAMETER, param));
                        breakOuterLoop = true;
                        break;
                    }
                }
                if (breakOuterLoop) 
                    break;
                
            }
            if (breakOuterLoop) 
                continue;
            
              
            // Resolve parameter/local usage sites inside the scope body via the innermost enclosing scope.
            // Example: marks the second 'item' in func = (item) => item + 1
            Object innermostScope = findInnermostScopeAt(m.start(1));
            if (innermostScope instanceof InnerCallableScope) {
                InnerCallableScope innerScope = (InnerCallableScope) innermostScope;
                FieldInfo innerParam = innerScope.getParameter(name);
                if (innerParam != null) {
                    marks.add(new ScriptLine.Mark(m.start(1), m.end(1), TokenType.PARAMETER, innerParam));
                    continue;
                }
                // Also check locals
                FieldInfo innerLocal = innerScope.getLocals().get(name);
                if (innerLocal != null) {
                    marks.add(new ScriptLine.Mark(m.start(1), m.end(1), TokenType.LOCAL_FIELD, innerLocal));
                    continue;
                }
            }
            
            // Check parameters (method scope) - fallback if not in inner scope
            if (containingMethod != null && containingMethod.hasParameter(name)) {
                FieldInfo paramInfo = containingMethod.getParameter(name);
                marks.add(new ScriptLine.Mark(m.start(1), m.end(1), TokenType.PARAMETER, paramInfo));
                continue;
            }

            FieldInfo localInfo = null;
            if (containingMethod != null) {
                Map<String, List<FieldInfo>> locals = methodLocals.get(containingMethod.getDeclarationOffset());
                if (locals != null) {
                    localInfo = pickVisibleLocal(locals.get(name), position);
                }
            } else {
                localInfo = pickVisibleTopLevelLocal(name, position);
            }
            if (localInfo != null) {
                Object metadata = callInfo != null ? new FieldInfo.ArgInfo(localInfo, callInfo) : localInfo;
                marks.add(new ScriptLine.Mark(m.start(1), m.end(1), TokenType.LOCAL_FIELD, metadata));
                continue;
            }

            // Check enclosing type fields (when inside method)
            if (containingMethod != null) {
                ScriptTypeInfo enclosingType = findEnclosingScriptType(position);
                if (enclosingType != null && enclosingType.hasField(name)) {
                    FieldInfo fieldInfo = enclosingType.getFieldInfo(name);
                    if (fieldInfo.isVisibleAt(position)) {
                        Object metadata = callInfo != null ? new FieldInfo.ArgInfo(fieldInfo, callInfo) : fieldInfo;
                        marks.add(new ScriptLine.Mark(m.start(1), m.end(1), TokenType.GLOBAL_FIELD, metadata));
                        continue;
                    }
                }
            }

            // Check other script type fields (only if position is within that type's boundaries).
            // Use continue identifierLoop to exit the outer while when a match is found —
            // a plain 'continue' would only skip to the next scriptType, causing fall-through
            // to the UNDEFINED_VAR mark even after a GLOBAL_FIELD mark was already added.
            for (ScriptTypeInfo scriptType : scriptTypesByFullName.values()) {
                if (scriptType.hasField(name)) {
                    // Only check if position is within this script type's class body
                    if (position >= scriptType.getBodyStart() && position <= scriptType.getBodyEnd()) {
                        FieldInfo fieldInfo = scriptType.getFieldInfo(name);
                        if(fieldInfo.isEnumConstant())
                            continue;
                        
                        if (fieldInfo.isVisibleAt(position)) {
                            Object metadata = callInfo != null ? new FieldInfo.ArgInfo(fieldInfo, callInfo) : fieldInfo;
                            marks.add(new ScriptLine.Mark(m.start(1), m.end(1), TokenType.GLOBAL_FIELD, metadata));
                            continue identifierLoop;
                        }
                    }
                }
            }
            
            // Check global fields
            if (globalFields.containsKey(name)) {
                FieldInfo fieldInfo = globalFields.get(name);
                if (fieldInfo.isVisibleAt(position)) {
                    Object metadata = callInfo != null ? new FieldInfo.ArgInfo(fieldInfo, callInfo) : fieldInfo;
                    marks.add(new ScriptLine.Mark(m.start(1), m.end(1), TokenType.GLOBAL_FIELD, metadata));
                    continue;
                }
            }

            if (isJavaScript()) {
                // check if a variable in global objects 
                JSTypeRegistry registry = JSTypeRegistry.getInstance();
                String globalObjectType = registry.getGlobalObjectType(name);
                if (globalObjectType != null) {
                    // Resolve the type and create a FieldInfo for it
                    FieldInfo fieldInfo = resolveVariable(name, position);
                    if (fieldInfo != null && fieldInfo.isResolved()) {
                        Object metadata = callInfo != null ? new FieldInfo.ArgInfo(fieldInfo, callInfo) : fieldInfo;
                        marks.add(new ScriptLine.Mark(m.start(1), m.end(1), TokenType.GLOBAL_FIELD, metadata));
                        continue;
                    }
                }

                // Mark DataScript global variable definitions
                if (!editorGlobals.isEmpty() && editorGlobals.containsKey(name)) {
                    FieldInfo fieldInfo = resolveVariable(name, position);
                    if (fieldInfo != null) {
                        Object metadata = callInfo != null ? new FieldInfo.ArgInfo(fieldInfo, callInfo) : fieldInfo;
                        marks.add(new ScriptLine.Mark(m.start(1), m.end(1), TokenType.GLOBAL_FIELD, metadata));
                        continue;
                    }
                }
            }

            // Skip uppercase if not a known field - type handling will deal with it
            if (isUppercase)
                continue;

            // Check if it's a script method used as a value (e.g., in schedule("action", methodName))
            if (isScriptMethod(name)) {
                MethodInfo scriptMethod = getScriptMethodInfo(name);
                if (scriptMethod != null) {
                    marks.add(new ScriptLine.Mark(m.start(1), m.end(1), TokenType.METHOD_CALL, scriptMethod));
                    continue;
                }
            }

            // Unknown variable - mark as undefined
            if (containingMethod != null) {
                marks.add(new ScriptLine.Mark(m.start(1), m.end(1), TokenType.UNDEFINED_VAR, callInfo));
            }
        }
    }

    /**
     * Mark chained field accesses like: mc.player.world, array.length, this.field, etc.
     * This handles dot-separated access chains and colors each segment appropriately.
     * Does NOT mark method calls (identifiers followed by parentheses) - those are handled by {@link #markMethodCalls}.
     */
    private void markChainedFieldAccesses(List<ScriptLine.Mark> marks) {
        FieldChainMarker marker = new FieldChainMarker(this, text);
        marker.markChainedFieldAccesses(marks);
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
        for (Map<String, List<FieldInfo>> locals : methodLocals.values()) {
            for (List<FieldInfo> fields : locals.values()) {
                for (FieldInfo field : fields) {
                    field.clearAssignments();
                }
            }
        }
        for (List<FieldInfo> fields : topLevelLocals.values()) {
            for (FieldInfo field : fields) {
                field.clearAssignments();
            }
        }
        // Also clear assignments in script type fields (including inner classes)
        for (ScriptTypeInfo scriptType : scriptTypesByFullName.values()) {
            for (FieldInfo field : scriptType.getFields().values()) {
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
            
            // Find the start of this statement: walk backward to ';', '{', '}', or a
            // depth-0 ',' (multi-declarator separator like "int x=1, y=2").
            // Track '()'/`[]` depth so commas inside argument lists are not treated
            // as declarator separators.
            int stmtStart = equalsPos - 1;
            {
                int backDepth = 0;
                while (stmtStart >= 0) {
                    char c = text.charAt(stmtStart);
                    if (c == ')' || c == ']') {
                        backDepth++;
                    } else if (c == '(' || c == '[') {
                        if (backDepth == 0) { stmtStart++; break; }
                        backDepth--;
                    } else if (c == ';' || c == '{' || c == '}') {
                        stmtStart++;
                        break;
                    } else if (c == ',' && backDepth == 0) {
                        stmtStart++;
                        break;
                    }
                    stmtStart--;
                }
            }
            if (stmtStart < 0) stmtStart = 0;
            
            // Skip leading whitespace to get to the actual first character of the statement
            while (stmtStart < equalsPos && Character.isWhitespace(text.charAt(stmtStart))) {
                stmtStart++;
            }
            
            int stmtEnd;
            if (isJavaScript()) {
                int rhsBoundaryStart = equalsPos + 1;
                while (rhsBoundaryStart < text.length() && Character.isWhitespace(text.charAt(rhsBoundaryStart))) {
                    rhsBoundaryStart++;
                }
                // For multi-declarator JS statements (var a=1, b=2), the RHS of
                // each declarator must stop at the top-level comma. We detect this
                // by checking if the statement begins with var/let/const.
                boolean isMultiDecl = MultiDeclaratorParser.startsWithJSKeyword(text, stmtStart, equalsPos);
                stmtEnd = findJsInitializerEnd(text, rhsBoundaryStart, isMultiDecl);
                if (stmtEnd < equalsPos + 1) {
                    stmtEnd = equalsPos + 1;
                }
            } else {
                // For Java, stop at depth-0 ',' or ';' so that each declarator in
                // "int x='str', y=20, z='str'" is bounded independently.
                // findJsInitializerEnd handles string literals and bracket depth correctly.
                stmtEnd = findJsInitializerEnd(text, equalsPos + 1, true);
                if (stmtEnd < equalsPos + 1) stmtEnd = equalsPos + 1;
            }
            
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

        ObjectLiteralParser.DynamicPropertyAccess dynamicPropertyAccess = isJavaScript() ? ObjectLiteralParser.parseDynamicPropertyAccess(lhs, stmtStart) : null;
        
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
            
            // Check if the last segment has array subscript (e.g., obj.items[0])
            boolean lastSegmentHasArrayAccess = targetName.contains("[");
            if (lastSegmentHasArrayAccess) {
                targetName = targetName.substring(0, targetName.indexOf('[')).trim();
            }
            
            // Resolve the chain to get the target type
            String receiverExpr = lhs.substring(0, lhs.lastIndexOf('.')).trim();
            receiverType = resolveExpressionType(receiverExpr, lhsStart);
            
            if (receiverType != null && receiverType.isResolved()) {
                // Get the field from the receiver type
                if (receiverType.hasField(targetName)) {
                    targetField = receiverType.getFieldInfo(targetName);
                    targetType = targetField.getTypeInfo();
                    if (lastSegmentHasArrayAccess) {
                        targetType = unwrapArrayElement(targetType);
                    }
                    reflectionField = targetField.getReflectionField();
                }
            }
          //  Minecraft.getMinecraft().thePlayer.PERSISTED_NBT_TAG = null;

        } else if (lhs.contains("[")) {
            // Array subscript access (e.g., items[0])
            // Extract the variable name before the bracket
            String varName = lhs.substring(0, lhs.indexOf('[')).trim();
            targetName = varName;
            if (dynamicPropertyAccess != null) {
                ObjectLiteralParser.DynamicFieldResult res = ObjectLiteralParser.resolveExistingField(
                        dynamicPropertyAccess, this::resolveExpressionType, this::resolveVariable);
                receiverType = res.receiverType;
                targetName = res.propertyName;
                targetField = res.field;
                if (targetField != null) {
                    targetType = targetField.getTypeInfo();
                    reflectionField = targetField.getReflectionField();
                }
            }

            if (targetField == null) {
                targetField = resolveVariable(varName, lhsStart);
            }

            if (targetField != null && targetType == null) {
                // The variable type is the array type (e.g., ItemStack[])
                // Unwrap to get the element type (e.g., ItemStack)
                targetType = unwrapArrayElement(targetField.getTypeInfo());
                reflectionField = targetField.getReflectionField();
            }
        } else {
            // Simple variable
            targetField = resolveVariable(targetName, lhsStart);
            if (targetField != null) {
                targetType = targetField.getTypeInfo();
                reflectionField = targetField.getReflectionField();
            }
        }

        // Resolve the source type (RHS) with expected type context
        TypeInfo sourceType;
        ExpressionTypeResolver.CURRENT_EXPECTED_TYPE = targetType;
        try {
            sourceType = resolveExpressionType(rhs, equalsPos + 1);
        } finally {
            ExpressionTypeResolver.CURRENT_EXPECTED_TYPE = null;
        }

        if (dynamicPropertyAccess != null) {
            ObjectLiteralParser.DynamicFieldResult res = ObjectLiteralParser.extendAndGetField(
                    dynamicPropertyAccess, receiverType, sourceType, rhs,
                    this::resolveExpressionType, this::resolveVariable);
            if (res != null) {
                receiverType = res.receiverType;
                targetName = res.propertyName;
                targetField = res.field;
                if (targetField != null) {
                    targetType = targetField.getTypeInfo();
                    reflectionField = targetField.getReflectionField();
                }
            }
        }
        
        // Type inference: If the target field has "any" type and no inferred type yet,
        // set the inferred type from the resolved source type (first assignment wins)
        if (targetField != null && targetField.canInferType() && targetField.getInferredType() == null 
                && sourceType != null && sourceType.isResolved()) {
            // Don't infer "any" type - that doesn't refine anything
            if (!"any".equals(sourceType.getFullName())) {
                targetField.setInferredType(sourceType);
            }
        }
        
        // Determine if this is a script-defined field or external field
        FieldInfo finalTargetField = targetField;
        boolean isScriptField = targetField != null && 
            (globalFields.containsValue(targetField) || 
             methodLocals.values().stream().anyMatch(m -> m.values().stream().anyMatch(list -> list.contains(finalTargetField))) ||
             topLevelLocals.values().stream().anyMatch(list -> list.contains(finalTargetField)));
        
        // Determine if the target field is final
        // For script fields, use the modifiers; for external fields, use reflection
        // Array element assignment (items[0] = ...) is always allowed even if the array itself is final
        boolean isFinal = false;
        if (targetField != null && !lhs.contains("[")) {
            isFinal = targetField.isFinal();
        }
        
        // Allow final field initialization inside a constructor of the declaring type.
        // In Java, final fields can be assigned exactly once in the constructor.
        if (isFinal && targetField != null) {
            MethodInfo enclosingCtor = findEnclosingConstructor(stmtStart);
            if (enclosingCtor != null) {
                ScriptTypeInfo enclosingType = findEnclosingScriptType(stmtStart);
                if (enclosingType != null && enclosingType.getFields().containsValue(targetField)) {
                    isFinal = false;
                }
            }
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

        if (targetField != null) {
            info.setScopeInfo(targetField.getScopeInfo());
        }
        
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
        
        TypeInfo targetType = targetField.getTypeInfo();

        // Resolve the source type with expected type context
        TypeInfo sourceType;
        ExpressionTypeResolver.CURRENT_EXPECTED_TYPE = targetType;
        try {
            sourceType = resolveExpressionType(rhs, rhsStart);
        } finally {
            ExpressionTypeResolver.CURRENT_EXPECTED_TYPE = null;
        }
        
        // Type inference: If the target field has "any" type and no inferred type yet,
        // set the inferred type from the resolved source type
        if (targetField.canInferType() && targetField.getInferredType() == null && sourceType != null && sourceType.isResolved()) {
            // Don't infer "any" type - that doesn't refine anything
            if (!"any".equals(sourceType.getFullName())) {
                targetField.setInferredType(sourceType);
            }
        }
        
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

        info.setScopeInfo(targetField.getScopeInfo());
        
        // Validate the assignment
        info.validate();
        
        // Attach as the declaration assignment
        targetField.setDeclarationAssignment(info);
    }

    // ==================== METHOD INHERITANCE DETECTION ====================

    /**
     * Detect method overrides and interface implementations for all script-defined types.
     * This analyzes each method in each ScriptTypeInfo to determine if it:
     * - Overrides a method from a parent class (extends)
     * - Implements a method from an interface (implements)
     *
     * The detection uses signature matching (method name + parameter types).
     * Also validates that all interface methods are implemented and constructors match.
     */
    private void detectMethodInheritance() {
        for (ScriptTypeInfo scriptType : scriptTypesByFullName.values()) {
            scriptType.clearErrors();  // Clear previous errors
            detectMethodInheritanceForType(scriptType);
            scriptType.validate();  // Validate after detecting inheritance
        }
    }

    /**
     * Detect method inheritance for a single script type.
     */
    private void detectMethodInheritanceForType(ScriptTypeInfo scriptType) {
        // Check each method in this type
        for (List<MethodInfo> overloads : scriptType.getMethods().values()) {
            for (MethodInfo method : overloads) {
                // Check if method overrides a parent class method
                if (scriptType.hasSuperClass()) {
                    TypeInfo superClass = scriptType.getSuperClass();
                    if (superClass != null && superClass.isResolved()) {
                        TypeInfo overrideSource = findMethodInHierarchy(superClass, method, false);
                        if (overrideSource != null) {
                            method.setOverridesFrom(overrideSource);
                        }
                    }
                }

                // Check if method implements an interface method (only if not already marked as override)
                if (!method.isOverride() && scriptType.hasImplementedInterfaces()) {
                    for (TypeInfo iface : scriptType.getImplementedInterfaces()) {
                        if (iface != null && iface.isResolved()) {
                            TypeInfo implementsSource = findMethodInInterface(iface, method);
                            if (implementsSource != null) {
                                method.setImplementsFrom(implementsSource);
                                break; // Only mark first matching interface
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Search for a matching method in a type hierarchy (class inheritance).
     * @param type The type to search in (and its superclasses)
     * @param method The method to find a match for
     * @param includeInterfaces Whether to also search interfaces
     * @return The TypeInfo containing the matching method, or null if not found
     */
    private TypeInfo findMethodInHierarchy(TypeInfo type, MethodInfo method, boolean includeInterfaces) {
        if (type == null || !type.isResolved())
            return null;

        // Check if this type has the method
        if (hasMatchingMethod(type, method)) {
            return type;
        }

        // If it's a ScriptTypeInfo, check its super class
        if (type instanceof ScriptTypeInfo) {
            ScriptTypeInfo scriptType = (ScriptTypeInfo) type;
            if (scriptType.hasSuperClass()) {
                TypeInfo result = findMethodInHierarchy(scriptType.getSuperClass(), method, includeInterfaces);
                if (result != null)
                    return result;
            }
        }

        // If it's a Java class, check its superclass
        Class<?> javaClass = type.getJavaClass();
        if (javaClass != null) {
            Class<?> superClass = javaClass.getSuperclass();
            if (superClass != null && superClass != Object.class) {
                TypeInfo superType = TypeInfo.fromClass(superClass);
                TypeInfo result = findMethodInHierarchy(superType, method, includeInterfaces);
                if (result != null)
                    return result;
            }
        }

        return null;
    }

    /**
     * Search for a matching method in an interface and its super-interfaces.
     * @param iface The interface to search in
     * @param method The method to find a match for
     * @return The TypeInfo containing the matching method, or null if not found
     */
    private TypeInfo findMethodInInterface(TypeInfo iface, MethodInfo method) {
        if (iface == null || !iface.isResolved())
            return null;

        // Check if this interface has the method
        if (hasMatchingMethod(iface, method)) {
            return iface;
        }

        // Check super-interfaces
        Class<?> javaClass = iface.getJavaClass();
        if (javaClass != null && javaClass.isInterface()) {
            for (Class<?> superIface : javaClass.getInterfaces()) {
                TypeInfo superType = TypeInfo.fromClass(superIface);
                TypeInfo result = findMethodInInterface(superType, method);
                if (result != null)
                    return result;
            }
        }

        return null;
    }

    /**
     * Check if a type has a method matching the given signature.
     * Uses method name and parameter types for matching.
     */
    private boolean hasMatchingMethod(TypeInfo type, MethodInfo method) {
        String methodName = method.getName();
        int paramCount = method.getParameterCount();

        // For ScriptTypeInfo, check its methods map
        if (type instanceof ScriptTypeInfo) {
            ScriptTypeInfo scriptType = (ScriptTypeInfo) type;
            List<MethodInfo> overloads = scriptType.getAllMethodOverloads(methodName);
            for (MethodInfo candidate : overloads) {
                if (signaturesMatch(method, candidate)) {
                    return true;
                }
            }
            return false;
        }

        // For Java classes, use reflection
        Class<?> javaClass = type.getJavaClass();
        if (javaClass == null)
            return false;

        try {
            for (java.lang.reflect.Method javaMethod : javaClass.getMethods()) {
                if (javaMethod.getName().equals(methodName) &&
                        javaMethod.getParameterCount() == paramCount) {
                    // Check parameter types match
                    if (parameterTypesMatch(method, javaMethod)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            // Security or linkage error
        }

        return false;
    }

    /**
     * Check if two MethodInfo signatures match (same name and parameter types).
     */
    private boolean signaturesMatch(MethodInfo m1, MethodInfo m2) {
        if (!m1.getName().equals(m2.getName()))
            return false;
        if (m1.getParameterCount() != m2.getParameterCount())
            return false;

        List<FieldInfo> params1 = m1.getParameters();
        List<FieldInfo> params2 = m2.getParameters();

        for (int i = 0; i < params1.size(); i++) {
            TypeInfo type1 = params1.get(i).getDeclaredType();
            TypeInfo type2 = params2.get(i).getDeclaredType();

            // Both null = match
            if (type1 == null && type2 == null)
                continue;

            // One null, one not = no match
            if (type1 == null || type2 == null)
                return false;

            // Compare full names
            if (!type1.getFullName().equals(type2.getFullName())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check if a MethodInfo's parameter types match a Java reflection Method's parameter types.
     */
    private boolean parameterTypesMatch(MethodInfo methodInfo, java.lang.reflect.Method javaMethod) {
        List<FieldInfo> params = methodInfo.getParameters();
        Class<?>[] javaParams = javaMethod.getParameterTypes();

        if (params.size() != javaParams.length)
            return false;

        for (int i = 0; i < params.size(); i++) {
            TypeInfo paramType = params.get(i).getDeclaredType();
            if (paramType == null)
                continue; // Unresolved param, skip check

            Class<?> javaParamClass = javaParams[i];
            String javaParamName = javaParamClass.getName();

            // Compare type names
            if (!paramType.getFullName().equals(javaParamName) &&
                    !paramType.getSimpleName().equals(javaParamClass.getSimpleName())) {
                return false;
            }
        }

        return true;
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
            
            // Resolve the type with position context for inner class names
            TypeInfo info = resolveType(typeName, typeStart);
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

    /**
     * Pre-infer lambda parameter types for constructor arguments (new Foo(... lambda ...)).
     * Constructor SAM inference normally happens in markImportedClassUsages, which runs AFTER
     * markMethodCalls/markVariables/markChainedFieldAccesses. That ordering means lambda body
     * resolution (e.g., event.getHookName()) fails because parameter types aren't set yet.
     * This early pass sets inferredType on lambda parameters so body resolution works correctly.
     */
    private void inferConstructorLambdaTypes() {
        Pattern newExpr = Pattern.compile("\\bnew\\s+([A-Za-z][a-zA-Z0-9_.]*?)\\s*(?:<[^>]*>)?\\s*\\(");
        Matcher m = newExpr.matcher(text);

        while (m.find()) {
            if (isExcluded(m.start()))
                continue;
            if (isInImportOrPackage(m.start()))
                continue;

            String className = m.group(1);
            int openParen = m.end() - 1;
            int closeParen = findMatchingParen(openParen);
            if (closeParen < 0)
                continue;

            String argsText = text.substring(openParen + 1, closeParen);
            boolean hasLambda = isJavaScript()
                    ? (argsText.contains("function") || argsText.contains("=>"))
                    : argsText.contains("->");
            if (!hasLambda)
                continue;

            TypeInfo info = resolveType(className, m.start(1));
            if (info == null || !info.isResolved())
                continue;
            if (!info.hasConstructors())
                continue;

            List<MethodCallInfo.Argument> arguments = parseMethodArguments(openParen + 1, closeParen, null, info);
            TypeInfo[] argTypes = arguments.stream().map(MethodCallInfo.Argument::getResolvedType)
                                           .toArray(TypeInfo[]::new);

            MethodInfo constructor = info.findConstructor(argTypes);
            if (constructor == null) {
                constructor = info.findConstructor(arguments.size());
            }

            if (constructor != null && constructor.getParameters().size() == arguments.size()) {
                parseMethodArguments(openParen + 1, closeParen, constructor, info);
            }
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

            // For JavaScript, first check synthetic types (Nashorn built-ins like Java, print, etc.)
            if (isJavaScript()) {
                if (typeResolver.isSyntheticType(className)) {
                    SyntheticType syntheticType = typeResolver.getSyntheticType(className);
                    // Mark as IMPORTED_CLASS since it's a type reference
                    // Pass the TypeInfo (which the hover system can display)
                    marks.add(new ScriptLine.Mark(m.start(1), m.end(1), TokenType.IMPORTED_CLASS,
                            syntheticType.getTypeInfo()));
                    continue;
                }
                JSTypeRegistry jsRegistry = JSTypeRegistry.getInstance();
                if (jsRegistry.isGlobalImport(className)) {
                    // Mark as IMPORTED_CLASS since it's a type reference
                    TypeInfo globalType = TypeInfo.fromJSTypeInfo(jsRegistry.getGlobalImportType(className));
                    marks.add(new ScriptLine.Mark(m.start(1), m.end(1), TokenType.INTERFACE_DECL, globalType));
                    continue;
                }
            }
            
            // Use position-aware resolution so inner class names (e.g., OrbBuilder inside Outer)
            // resolve via the positional fallback that walks enclosing.getInnerClass(name).
            TypeInfo info = resolveType(className, start);
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

            // When the class name is preceded by a dot (e.g., OrbBuilder in AbilityLine.OrbBuilder),
            // walk backwards to reconstruct the full qualified name and resolve via scriptTypesByDotName.
            // resolveType(simpleNameOnly) fails outside the enclosing class body; the qualified name always works.
            // We also track outerQualStart so we can detect 'new' before the full qualified name.
            String resolveKey = className;
            int outerQualStart = start;
            if (isPrecededByDot(start)) {
                int pos = start - 1;
                while (pos >= 0 && Character.isWhitespace(text.charAt(pos))) pos--;
                while (pos >= 0 && text.charAt(pos) == '.') {
                    pos--;
                    while (pos >= 0 && Character.isWhitespace(text.charAt(pos))) pos--;
                    int identEnd = pos + 1;
                    while (pos > 0 && (Character.isLetterOrDigit(text.charAt(pos - 1)) || text.charAt(pos - 1) == '_')) pos--;
                    resolveKey = text.substring(pos, identEnd) + "." + resolveKey;
                    outerQualStart = pos;
                    pos--;
                    while (pos >= 0 && Character.isWhitespace(text.charAt(pos))) pos--;
                }
            }
            TypeInfo info = resolveType(resolveKey, start);
            if (info == null || !info.isResolved()) {
                info = resolveType(className, start);
            }
            boolean isClassTypeInfo = false;
            ClassTypeInfo classRef = null;
            FieldInfo varInfo = null;
            // If not a class name, check if it's a variable holding a ClassTypeInfo
            if ((info == null || !info.isResolved()) && isJavaScript()) {
                varInfo = resolveVariable(className, start);
                if (varInfo != null && varInfo.getTypeInfo() instanceof ClassTypeInfo) {
                    // Variable holds a class reference (like var File = Java.type("java.io.File"))
                    classRef = (ClassTypeInfo) varInfo.getTypeInfo();
                    info = classRef.getInstanceType();
                    isClassTypeInfo = true;
                }
            }
            
            if (info != null && info.isResolved()) {
                boolean isNewCreation = newKeyword != null && newKeyword.trim().equals("new");
                // For dot-qualified types like 'new AbilityLine.OrbBuilder(...)', the regex never
                // captures 'new' in group 1 because 'new' precedes 'AbilityLine', not 'OrbBuilder'.
                // Detect it by checking whether outerQualStart is preceded by the 'new' keyword.
                if (!isNewCreation && outerQualStart < start) {
                    int checkPos = outerQualStart - 1;
                    while (checkPos >= 0 && Character.isWhitespace(text.charAt(checkPos))) checkPos--;
                    if (checkPos >= 2 && "new".equals(text.substring(checkPos - 2, checkPos + 1))) {
                        char beforeNew = checkPos >= 3 ? text.charAt(checkPos - 3) : ' ';
                        if (!Character.isLetterOrDigit(beforeNew) && beforeNew != '_') {
                            isNewCreation = true;
                        }
                    }
                }
                boolean isConstructorDecl = info instanceof ScriptTypeInfo && className.equals(info.getSimpleName());
                
                // Check if this is a "new" expression or constructor declaration
                if (isNewCreation || isConstructorDecl) {
                    // Find opening paren after the class name
                    int searchPos = end;
                    while (searchPos < text.length() && Character.isWhitespace(text.charAt(searchPos)))
                        searchPos++;
                    
                    // Skip generic args or diamond operator (e.g., <Integer> or <>)
                    if (searchPos < text.length() && text.charAt(searchPos) == '<') {
                        int closeAngle = text.indexOf('>', searchPos);
                        if (closeAngle >= 0) {
                            searchPos = closeAngle + 1;
                            while (searchPos < text.length() && Character.isWhitespace(text.charAt(searchPos)))
                                searchPos++;
                        }
                    }
                    
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
                            List<MethodCallInfo.Argument> arguments = parseMethodArguments(openParen + 1, closeParen,
                                    null, info);
                            int argCount = arguments.size();
                            TypeInfo[] argTypes = arguments.stream().map(MethodCallInfo.Argument::getResolvedType)
                                                           .toArray(TypeInfo[]::new);


                            // Try to find matching constructor (may be null if not found).
                            // First try exact type-match; if that fails but constructors exist, fall back to
                            // arg-count match so validate() can fire a WRONG_ARG_TYPE error instead of
                            // incorrectly treating the call as "no constructor matches N arguments".
                            MethodInfo constructor = null;
                            if (info.hasConstructors()) {
                                constructor = info.findConstructor(argTypes);
                                if (constructor == null) {
                                    constructor = info.findConstructor(argCount);
                                }
                            }

                            // Second pass: re-parse arguments with expected parameter types from resolved constructor
                            // This enables SAM type inference for lambda arguments (e.g., Consumer<T> parameters)
                            if (constructor != null && constructor.getParameters().size() == arguments.size()) {
                                arguments = parseMethodArguments(openParen + 1, closeParen, constructor, info);
                            }
                            
                            // Create MethodCallInfo for constructor
                            // Use the actual variable name (className) for variables, not the class's simple name
                            MethodCallInfo ctorCall;
                            if (isClassTypeInfo) {
                                // Use constructor directly with variable name
                                ctorCall = new MethodCallInfo(
                                    className,           // Use variable name, not class name
                                    start, end,
                                    openParen, closeParen,
                                    arguments,
                                        classRef,                // The actual class type
                                    constructor,
                                    false
                                ).setConstructor(true);
                                ctorCall.isClassTypeAccess = true;
                            } else {
                                ctorCall = MethodCallInfo.constructor(
                                    info, constructor, start, end, openParen, closeParen, arguments
                                );
                            }
                            
                            ctorCall.validate();
                            methodCalls.add(ctorCall);  // Add to methodCalls list for error tracking


                            TokenType type = isClassTypeInfo ? varInfo.isGlobal() ? TokenType.GLOBAL_FIELD
                                    : TokenType.LOCAL_FIELD : info.getTokenType();
                            marks.add(new ScriptLine.Mark(start, end, type, isClassTypeInfo ? varInfo : ctorCall));
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
    FieldAccessInfo createFieldAccessInfo(String name, int start, int end,
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

    /**
     * Resolve a variable by name at a given position.
     * Works for BOTH Java and JavaScript using unified data structures.
     * Checks innermost scope first (lambda or method), then walks up parent chain.
     */
    public FieldInfo resolveVariable(String name, int position) {
        // 1. Check innermost scope (lambda or method)
        Object innermostScope = findInnermostScopeAt(position);
        
        if (innermostScope instanceof InnerCallableScope) {
            InnerCallableScope scope = (InnerCallableScope) innermostScope;
            
            // Check lambda parameters and locals, walking up parent chain
            InnerCallableScope currentScope = scope;
            while (currentScope != null) {
                // Check parameters
                FieldInfo param = currentScope.getParameter(name);
                if (param != null) {
                    return param;
                }
                
                // Check locals
                FieldInfo local = currentScope.getLocals().get(name);
                if (local != null) {
                    return local;
                }
                
                currentScope = currentScope.getParentScope();
            }
            
            // Not found in lambda scopes - fall back to enclosing method
            // Find which method this lambda is inside
            innermostScope = findMethodAtPosition(position);
        }
        
        // 2. Check method scope (params + locals)
        if (innermostScope instanceof MethodInfo) {
            MethodInfo method = (MethodInfo) innermostScope;
            
            // Check method parameters
            if (method.hasParameter(name)) {
                return method.getParameter(name);
            }
            
            // Check method locals
            Map<String, List<FieldInfo>> locals = methodLocals.get(method.getDeclarationOffset());
            if (locals != null) {
                FieldInfo localInfo = pickVisibleLocal(locals.get(name), position);
                if (localInfo != null) {
                    return localInfo;
                }
            }
        }
        
        FieldInfo topLocal = pickVisibleTopLevelLocal(name, position);
        if (topLocal != null) return topLocal;

        // 4. For Java only: Check if we're inside a script type and look for fields there
        if (!isJavaScript()) {
            ScriptTypeInfo enclosingType = findEnclosingScriptType(position);
            if (enclosingType != null && enclosingType.hasField(name)) {
                return enclosingType.getFieldInfo(name);
            }
        }
        
        // 4. Check global fields (stores both Java global fields and JS global var/let/const)
        if (globalFields.containsKey(name)) {
            return globalFields.get(name);
        }

        // 5. Check JS global objects from JSTypeRegistry (like API, DBCAPI)
        if (isJavaScript()) {
            JSTypeRegistry registry = JSTypeRegistry.getInstance();
            String globalObjectType = registry.getGlobalObjectType(name);
            if (globalObjectType != null) {
                // Resolve the type and create a FieldInfo for it
                TypeInfo typeInfo = resolveType(globalObjectType);
                if (typeInfo != null && typeInfo.isResolved()) {
                    // Create a global field for this object with GLOBAL scope
                    return FieldInfo.globalField(name, typeInfo, -1);
                }
            }

            // Check if is DataScript global variable definition
            String editorGlobalType = editorGlobals.get(name);
            if (editorGlobalType != null) {
                TypeInfo typeInfo = resolveType(editorGlobalType);
                if (typeInfo == null || !typeInfo.isResolved()) {
                    typeInfo = TypeInfo.ANY;
                }
                return FieldInfo.globalField(name, typeInfo, -1);
            }
        }
        
        return null;
    }

    boolean isPrecededByDot(int position) {
        if (position <= 0)
            return false;
        int i = position - 1;
        while (i >= 0 && Character.isWhitespace(text.charAt(i)))
            i--;
        return i >= 0 && text.charAt(i) == '.';
    }

    boolean isInImportOrPackage(int position) {
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

    /** Check if position is followed by '(' (method call) */
    boolean isFollowedByParen(int pos) {
        int check = skipWhitespace(pos);
        return check < text.length() && text.charAt(check) == '(';
    }

    /** Check if position is followed by '.' */
    boolean isFollowedByDot(int pos) {
        int check = skipWhitespace(pos);
        return check < text.length() && text.charAt(check) == '.';
    }

    /** Skip whitespace and return new position */
    int skipWhitespace(int pos) {
        while (pos < text.length() && Character.isWhitespace(text.charAt(pos)))
            pos++;
        return pos;
    }

    MethodInfo findMethodAtPosition(int position) {
        for (MethodInfo method : getAllMethods()) {
            if (method.containsPosition(position)) {
                return method;
            }
        }
        for (MethodInfo constructor : getAllConstructors()) {
            if (constructor.containsPosition(position)) {
                return constructor;
            }
        }
        return null;
    }

    /**
     * Find the innermost scope (method or inner callable) at a position.
     * Returns null if not inside any scope.
     */
    public Object findInnermostScopeAt(int position) {
        // First check inner callable scopes (most specific)
        InnerCallableScope innermost = null;
        for (InnerCallableScope scope : innerScopes) {
            if (scope.containsPosition(position) || scope.containsHeaderPosition(position)) {
                if (innermost == null || scope.getBodyStart() > innermost.getBodyStart()) {
                    innermost = scope;
                }
            }
        }
        
        if (innermost != null) {
            return innermost;
        }
        
        // Fall back to method scope
        return findMethodAtPosition(position);
    }

    int findMatchingBrace(int openBraceIndex) {
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

    public List<InnerCallableScope> getInnerScopes() {
        return Collections.unmodifiableList(innerScopes);
    }

    public ObjectLiteralParser.ObjectLiteralAnalysis getObjectLiteral(int braceOffset) {
        return objectLiterals.get(braceOffset);
    }

    public List<ScriptTypeInfo> getScriptTypes() {
        // Return all types including inner classes — needed by error underline rendering,
        // validation loops, and mark building so inner classes get full treatment
        return new ArrayList<>(scriptTypesByFullName.values());
    }

    public List<MethodInfo> getAllMethods() {
        List<MethodInfo> allMethods = new ArrayList<>(methods);
        // Iterate all types (including inner) so nested class methods are included
        for (ScriptTypeInfo scriptType : scriptTypesByFullName.values()) {
            allMethods.addAll(scriptType.getAllMethodsFlat());
        }
        
        return allMethods;
    }

    public List<MethodInfo> getAllConstructors() {
        List<MethodInfo> allConstructors = new ArrayList<>();
        // Iterate all types (including inner) so nested class constructors are included
        for (ScriptTypeInfo scriptType : scriptTypesByFullName.values()) {
            allConstructors.addAll(scriptType.getConstructors());
        }
        return allConstructors;
    }
    
    public List<MethodCallInfo> getMethodCalls() {
        List<MethodCallInfo> allCalls = new ArrayList<>(methodCalls);

        for (EnumConstantInfo constant : getAllEnumConstants()) {
            if (constant.getConstructorCall() != null)
                allCalls.add(constant.getConstructorCall());
        }
        
        
        return allCalls;
    }

    public List<FieldAccessInfo> getFieldAccesses() {
        return Collections.unmodifiableList(fieldAccesses);
    }

    public List<EnumConstantInfo> getAllEnumConstants() {
        List<EnumConstantInfo> enums = new ArrayList<>();
        for (ScriptTypeInfo scriptType : scriptTypesByFullName.values()) {
            if (!scriptType.hasEnumConstants())
                continue;

            enums.addAll(scriptType.getEnumConstants().values());
        }
        return enums;
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

        for (Map<String, List<FieldInfo>> locals : methodLocals.values()) {
            for (List<FieldInfo> fields : locals.values()) {
                for (FieldInfo field : fields) {
                    AssignmentInfo assign = field.findAssignmentAtPosition(position);
                    if (assign != null) {
                        return assign;
                    }
                }
            }
        }
        for (List<FieldInfo> fields : topLevelLocals.values()) {
            for (FieldInfo field : fields) {
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

        for (ScriptTypeInfo scriptType : scriptTypesByFullName.values()) {
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
     * Get all variables available at a specific position.
     * Used by autocomplete to show scope-aware suggestions.
     * Returns variables from innermost scope first (parameters, then locals),
     * walking up through parent scopes, then method scope, then globals.
     * 
     * @param position The cursor position
     * @return List of FieldInfo for all available variables, ordered by priority
     */
    public List<FieldInfo> getAvailableVariablesAt(int position) {
        List<FieldInfo> variables = new ArrayList<>();
        
        // Check innermost scope (lambda or method)
        Object innermostScope = findInnermostScopeAt(position);
        
        if (innermostScope instanceof InnerCallableScope) {
            InnerCallableScope scope = (InnerCallableScope) innermostScope;
            
            // Add lambda parameters and locals, walking up parent chain
            InnerCallableScope currentScope = scope;
            while (currentScope != null) {
                // Add parameters from this scope
                variables.addAll(currentScope.getParameters());
                
                // Add locals from this scope (only those visible at position)
                for (FieldInfo local : currentScope.getLocals().values()) {
                    if (local.isVisibleAt(position)) {
                        variables.add(local);
                    }
                }
                
                currentScope = currentScope.getParentScope();
            }
            
            // Fall back to enclosing method
            innermostScope = findMethodAtPosition(position);
        }
        
        // Add method scope variables
        if (innermostScope instanceof MethodInfo) {
            MethodInfo method = (MethodInfo) innermostScope;
            
            // Add method parameters
            variables.addAll(method.getParameters());
            
            // Add method locals (only those visible at position)
            Map<String, List<FieldInfo>> locals = methodLocals.get(method.getDeclarationOffset());
            if (locals != null) {
                for (List<FieldInfo> candidates : locals.values()) {
                    FieldInfo best = pickVisibleLocal(candidates, position);
                    if (best != null) {
                        variables.add(best);
                    }
                }
            }
        }
        
        variables.addAll(getVisibleTopLevelLocals(position));

        // Add global fields (only those visible at position)
        for (FieldInfo globalField : globalFields.values()) {
            if (globalField.isVisibleAt(position)) {
                variables.add(globalField);
            }
        }
        
        // For Java: add fields from enclosing type
        if (!isJavaScript()) {
            ScriptTypeInfo enclosingType = findEnclosingScriptType(position);
            if (enclosingType != null) {
                for (FieldInfo field : enclosingType.getFields().values()) {
                    if (field.isVisibleAt(position)) {
                        variables.add(field);
                    }
                }
            }
        }
        
        // For JS: add global engine objects and editor globals
        if (isJavaScript()) {
            JSTypeRegistry registry = JSTypeRegistry.getInstance();
            
            // Add global engine objects (like API, DBCAPI)
            for (String globalName : registry.getGlobalEngineObjects().keySet()) {
                FieldInfo field = resolveVariable(globalName, position);
                if (field != null && field.isResolved()) {
                    variables.add(field);
                }
            }
            
            // Add editor/DataScript global variables
            for (String globalName : editorGlobals.keySet()) {
                FieldInfo field = resolveVariable(globalName, position);
                if (field != null && field.isResolved()) {
                    variables.add(field);
                }
            }
        }
        
        return variables;
    }
    
    /**
     * Get all errored assignments across all fields (global, local, and external).
     * Used by ScriptLine to draw error underlines.
     *
     * @deprecated Use {@link #getErrors()} instead. Errors are now collected in {@link #populateErrors()}.
     */
    @Deprecated
    public List<AssignmentInfo> getAllErroredAssignments() {
        List<AssignmentInfo> errored = new ArrayList<>();
        
        // Check global fields
        for (FieldInfo field : globalFields.values()) {
            errored.addAll(field.getErroredAssignments());
        }
        
        // Check method locals
        for (Map<String, List<FieldInfo>> locals : methodLocals.values()) {
            for (List<FieldInfo> fields : locals.values()) {
                for (FieldInfo field : fields) {
                    errored.addAll(field.getErroredAssignments());
                }
            }
        }
        for (List<FieldInfo> fields : topLevelLocals.values()) {
            for (FieldInfo field : fields) {
                errored.addAll(field.getErroredAssignments());
            }
        }
        
        // Check external field assignments
        for (AssignmentInfo assign : externalFieldAssignments) {
            if (assign.hasError()) {
                errored.add(assign);
            }
        }


        for (ScriptTypeInfo scriptType : scriptTypesByFullName.values()) {
            for (FieldInfo field : scriptType.getFields().values()) {
                errored.addAll(field.getErroredAssignments());
            }
        }
        

        // Include declaration errors (duplicates, etc.)
        errored.addAll(declarationErrors);
        
        return errored;
    }

    public void addError(Token token, int startPos, int endPos, String message) {
        errors.add(new DocumentError(token, startPos, endPos, message));
    }

    public List<DocumentError> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    /**
     * Collect all validation errors from MethodCallInfo, AssignmentInfo, MethodInfo, and ScriptTypeInfo
     * into the centralized error list. Called once during analysis (formatCodeText), not during rendering.
     */
    private void populateErrors() {
        // Method call errors
        List<MethodInfo> allMethods = getAllMethods();
        for (MethodCallInfo call : getMethodCalls()) {
            boolean isDeclaration = false;
            int methodStart = call.getMethodNameStart();
            for (MethodInfo mi : allMethods) {
                if (!call.isConstructor() && mi.getDeclarationOffset() <= methodStart && mi.getBodyStart() >= methodStart) {
                    isDeclaration = true;
                    break;
                }
            }
            if (isDeclaration)
                continue;

            if (call.hasArgCountError()) {
                int methodEnd = methodStart + call.getMethodName().length();
                addError(null, methodStart, methodEnd, call.getErrorMessage());
            } else if (call.hasArgTypeError()) {
                for (MethodCallInfo.ArgumentTypeError error : call.getArgumentTypeErrors()) {
                    MethodCallInfo.Argument arg = error.getArg();
                    addError(null, arg.getStartOffset(), arg.getEndOffset(), error.getMessage());
                }
            } else if (call.hasError()) {
                addError(null, call.getMethodNameStart(), call.getCloseParenOffset() + 1, call.getErrorMessage());
            }
        }

        // Assignment errors
        for (AssignmentInfo assign : getAllErroredAssignments()) {
            int underlineStart, underlineEnd;

            if (assign.isLhsError()) {
                underlineStart = assign.getLhsStart();
                underlineEnd = assign.getLhsEnd();
            } else if (assign.isRhsError()) {
                underlineStart = assign.getRhsStart();
                underlineEnd = assign.getRhsEnd();
            } else if (assign.isFullLineError()) {
                underlineStart = assign.getStatementStart();
                underlineEnd = assign.getRhsEnd();
            } else {
                continue;
            }

            addError(null, underlineStart, underlineEnd, assign.getErrorMessage());
        }

        // Method declaration errors
        for (MethodInfo method : allMethods) {
            if (!method.isDeclaration() || !method.hasError())
                continue;

            if (method.hasReturnStatementErrors()) {
                for (MethodInfo.ReturnStatementError returnError : method.getReturnStatementErrors()) {
                    addError(null, returnError.getStartOffset(), returnError.getEndOffset(), returnError.getMessage());
                }
            } else if (method.hasMissingReturnError()) {
                int methodNameStart = method.getNameOffset();
                int methodNameEnd = methodNameStart + method.getName().length();
                addError(null, methodNameStart, methodNameEnd, method.getErrorMessage());
            } else if (method.hasParameterErrors()) {
                for (MethodInfo.ParameterError paramError : method.getParameterErrors()) {
                    FieldInfo param = paramError.getParameter();
                    if (param == null || param.getDeclarationOffset() < 0)
                        continue;

                    int paramStart = param.getDeclarationOffset();
                    int paramEnd = paramStart + param.getName().length();
                    addError(null, paramStart, paramEnd, paramError.getMessage());
                }
            } else if (method.hasError()) {
                addError(null, method.getFullDeclarationOffset(), method.getDeclarationEnd(), method.getErrorMessage());
            }
        }

        for (EnumConstantInfo enumConst : getAllEnumConstants()) {
            if (enumConst != null && enumConst.hasError()) {
                addError(null, 0, 0, enumConst.getErrorMessage());
            }
        }

        // Script type declaration errors
        for (ScriptTypeInfo type : getScriptTypes()) {
            if (!type.hasError())
                continue;

            int typeStart = type.getDeclarationOffset();
            int typeEnd = type.getBodyStart();

            for (ScriptTypeInfo.MissingMethodError err : type.getMissingMethodErrors())
                addError(null, typeStart, typeEnd, err.getMessage());

            // Constructor mismatch errors
            for (ScriptTypeInfo.ConstructorMismatchError err : type.getConstructorMismatchErrors())
                addError(null, typeStart, typeEnd, err.getMessage());

            // General error message
            if (type.getErrorMessage() != null)
                addError(null, typeStart, typeEnd, type.getErrorMessage());
            
            String msg = type.getErrorMessage();
            if(msg != null && !msg.isEmpty())
                addError(null, typeStart, typeEnd, type.getErrorMessage());
        }
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
            Map<String, List<FieldInfo>> locals = methodLocals.get(containingMethod.getDeclarationOffset());
            if (locals != null) {
                FieldInfo field = pickVisibleLocal(locals.get(varName), position);
                if (field != null) {
                    return field.getTypeInfo();
                }
            }
        }

        FieldInfo topLevelLocal = pickVisibleTopLevelLocal(varName, position);
        if (topLevelLocal != null) {
            return topLevelLocal.getTypeInfo();
        }

        // Check global fields
        if (globalFields.containsKey(varName)) {
            return globalFields.get(varName).getTypeInfo();
        }

        return null;
    }
    
    // ==================== AUTOCOMPLETE SUPPORT ====================
    
    /**
     * Find the method that contains the given position.
     * Public accessor for autocomplete.
     */
    public MethodInfo findContainingMethod(int position) {
        return findMethodAtPosition(position);
    }
    
    /**
     * Get local variables for a specific method.
     * Used by autocomplete to show variables in scope.
     */
    public Map<String, FieldInfo> getLocalsForMethod(MethodInfo method) {
        if (method == null) return null;
        Map<String, List<FieldInfo>> locals = methodLocals.get(method.getDeclarationOffset());
        if (locals == null) {
            return null;
        }
        Map<String, FieldInfo> flattened = new HashMap<>();
        for (Map.Entry<String, List<FieldInfo>> e : locals.entrySet()) {
            List<FieldInfo> fields = e.getValue();
            if (fields != null && !fields.isEmpty()) {
                flattened.put(e.getKey(), fields.get(0));
            }
        }
        return flattened;
    }
    
    /**
     * Get all imported types that have been resolved.
     * Used by autocomplete to show available types.
     */
    public Set<TypeInfo> getImportedTypes() {
        Set<TypeInfo> types = new HashSet<>();
        for (ImportData imp : imports) {
            if (!imp.isWildcard() && imp.isResolved()) {
                TypeInfo type = typeResolver.resolveSimpleName(imp.getSimpleName(), importsBySimpleName, wildcardPackages);
                if (type != null && type.isResolved()) {
                    types.add(type);
                }
            }
        }
        // For class-level wildcard imports (e.g. INpcEvent.*), add the outer class itself.
        // Package-level wildcards (e.g. noppes.npcs.api.event.*) are skipped — those are
        // already covered by addUnimportedClassSuggestions via ClassIndex.
        for (String pkg : wildcardPackages) {
            TypeInfo outerType = typeResolver.resolveFullName(pkg);
            if (outerType != null && outerType.isResolved()) {
                // resolveFullName succeeded → pkg is a class, not a package
                types.add(outerType);
            }
        }
        return types;
    }
    
    /**
     * Get script types as a Map (needed by autocomplete).
     * Returns only top-level types keyed by simple name for backward compatibility.
     */
    public Map<String, ScriptTypeInfo> getScriptTypesMap() {
        return Collections.unmodifiableMap(scriptTypes);
    }

    /**
     * Get all script types including inner classes, keyed by full ($-separated) name.
     * Use this when you need to iterate or look up inner classes.
     */
    public Map<String, ScriptTypeInfo> getAllScriptTypes() {
        return Collections.unmodifiableMap(scriptTypesByFullName);
    }
}
