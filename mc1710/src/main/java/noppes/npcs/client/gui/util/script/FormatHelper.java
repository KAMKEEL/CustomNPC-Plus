package noppes.npcs.client.gui.util.script;

import java.util.ArrayList;
import java.util.List;

/**
 * Configurable code formatter with IntelliJ-like settings.
 * <p>
 * Architecture: Token-based pipeline (tokenize → format → reconstruct).
 * <p>
 * The formatter processes code in three phases:
 * <ol>
 *   <li><b>Tokenize</b> — Splits code segments into typed tokens
 *       (operators, keywords, identifiers, literals, punctuation, whitespace).
 *       Multi-character operators are matched longest-first to avoid sub-token clobbering.</li>
 *   <li><b>Format</b> — Applies spacing rules to each token pair based on their types
 *       and the current {@link FormatSettings}. Context-aware: distinguishes unary from
 *       binary operators, generic brackets from comparison operators, etc.</li>
 *   <li><b>Reconstruct</b> — Joins formatted tokens back into a code string.</li>
 * </ol>
 * <p>
 * String literals and comments are identified before tokenization and passed through
 * unchanged (the "preserve regions" approach). Line-by-line processing preserves
 * original structure and indentation.
 * <p>
 * Features:
 * <ul>
 *   <li>Operator spacing (a+b → a + b)</li>
 *   <li>Whitespace normalization (a   b → a b)</li>
 *   <li>Long line wrapping</li>
 *   <li>Bracket spacing configuration</li>
 *   <li>Preserves string content and comments</li>
 * </ul>
 */
public class FormatHelper {

    // ==================== TOKEN TYPES ====================

    /**
     * Token type classification for the formatter tokenizer.
     */
    enum TokenType {
        /** String literal: "..." or '...' (with escape handling) */
        STRING_LITERAL,
        /** Block comment: /&#42; ... &#42;/ */
        BLOCK_COMMENT,
        /** Line comment: // ... */
        LINE_COMMENT,
        /** Multi-char or single-char operator: ==, !=, >=, +, -, etc. */
        OPERATOR,
        /** Control-flow keyword: if, while, for, switch, catch, else, return, etc. */
        KEYWORD,
        /** ( */
        PAREN_OPEN,
        /** ) */
        PAREN_CLOSE,
        /** [ */
        BRACKET_OPEN,
        /** ] */
        BRACKET_CLOSE,
        /** { */
        BRACE_OPEN,
        /** } */
        BRACE_CLOSE,
        /** , */
        COMMA,
        /** ; */
        SEMICOLON,
        /** : */
        COLON,
        /** . */
        DOT,
        /** Variable name, method name, type name, etc. */
        IDENTIFIER,
        /** Numeric literal */
        NUMBER,
        /** Spaces, tabs (not newlines — those are line delimiters) */
        WHITESPACE
    }

    // ==================== TOKEN CLASS ====================

    /**
     * A single token produced by the tokenizer.
     */
    static class Token {
        final TokenType type;
        final String value;
        final int position; // position in original code segment

        Token(TokenType type, String value, int position) {
            this.type = type;
            this.value = value;
            this.position = position;
        }

        @Override
        public String toString() {
            return type + "(" + value + ")@" + position;
        }
    }

    // ==================== OPERATOR CLASSIFICATION ====================

    /**
     * Multi-character operators ordered longest-first for greedy matching.
     */
    private static final String[] MULTI_CHAR_OPERATORS = {
        ">>>=",      // unsigned right shift assign
        "===", "!==", // JS strict equality
        "<<=", ">>=", // shift assign
        ">>>",        // unsigned right shift
        "+=", "-=", "*=", "/=", "%=", // compound assign
        "&=", "|=", "^=",             // bitwise compound assign
        "&&", "||",   // logical
        "==", "!=",   // equality
        "<=", ">=",   // comparison
        "<<", ">>",   // shift
        "++", "--",   // increment/decrement
        "->", "=>",   // arrow operators (Java lambda, JS arrow)
    };

    /**
     * Single-character operators. Note: < and > are handled specially
     * (could be generics).
     */
    private static final String SINGLE_CHAR_OPERATORS = "=+-*/%&|^!~<>?";

    /**
     * Keywords that get special spacing before parentheses.
     */
    private static final String[] CONTROL_KEYWORDS = {
        "if", "while", "for", "switch", "catch",
        "else", "return", "new", "throw", "typeof", "instanceof",
        "in", "of", "var", "let", "const", "function", "class",
        "try", "finally", "do", "case", "default", "break", "continue",
        "void", "delete", "yield", "await", "async", "extends", "implements",
        "import", "export", "package", "interface", "enum", "abstract",
        "static", "final", "synchronized", "volatile", "transient", "native",
        "this", "super", "null", "true", "false", "undefined",
        "public", "private", "protected"
    };

    /**
     * Keywords that require a space before '(' when setting is enabled.
     */
    private static final String[] PAREN_KEYWORDS = {
        "if", "while", "for", "switch", "catch"
    };

    // ==================== SETTINGS ====================

    /**
     * Formatting configuration — similar to IntelliJ Code Style settings.
     */
    public static class FormatSettings {
        // Spaces around operators
        public boolean spaceAroundAssignment = true;          // a = b
        public boolean spaceAroundArithmetic = true;          // a + b - c * d / e
        public boolean spaceAroundComparison = true;          // a == b, a != b, a <= b
        public boolean spaceAroundLogical = true;             // a && b || c
        public boolean spaceAroundBitwise = true;             // a & b | c ^ d

        // Spaces within brackets
        public boolean spaceWithinParens = false;             // (a) vs ( a )
        public boolean spaceWithinBrackets = false;           // [a] vs [ a ]
        public boolean spaceWithinBraces = true;              // {a} vs { a } (for initializers)

        // Spaces before/after
        public boolean spaceAfterComma = true;                // a, b vs a,b
        public boolean spaceBeforeComma = false;              // a, b vs a , b
        public boolean spaceAfterSemicolon = true;            // for (a; b; c) vs for (a;b;c)
        public boolean spaceBeforeSemicolon = false;
        public boolean spaceAfterColon = true;                // case a: b vs case a:b
        public boolean spaceBeforeColon = false;

        // Method calls
        public boolean spaceBeforeMethodParens = false;       // foo() vs foo ()
        public boolean spaceAfterTypeCast = true;             // (int) x vs (int)x

        // Control statements
        public boolean spaceBeforeIfParens = true;            // if (...) vs if(...)
        public boolean spaceBeforeWhileParens = true;
        public boolean spaceBeforeForParens = true;
        public boolean spaceBeforeSwitchParens = true;
        public boolean spaceBeforeCatchParens = true;

        // Braces
        public boolean spaceBeforeOpenBrace = true;           // if () { vs if (){

        // Line length
        public int maxLineLength = 120;
        public boolean wrapLongLines = false;                  // Auto-wrap lines longer than maxLineLength
        public int wrapIndentSpaces = 4;                       // Extra indent for wrapped lines
        public boolean wrapComments = true;                    // Also wrap long comments

        // Wrap break preferences (where to break long lines)
        public boolean wrapAfterComma = true;                  // Prefer breaking after commas
        public boolean wrapAfterOperator = true;               // Prefer breaking after operators
        public boolean wrapBeforeDot = true;                   // Break before . for chained calls
        public boolean wrapMethodArguments = true;             // Each arg on new line when wrapping method calls

        // Whitespace normalization
        public boolean normalizeWhitespace = true;            // Multiple spaces → single space
        public boolean trimTrailingWhitespace = true;

        // Unary operators
        public boolean spaceAroundUnaryNot = false;           // !a vs ! a
        public boolean spaceAroundUnaryIncDec = false;        // ++i vs ++ i

        /**
         * Create default settings (similar to IntelliJ defaults)
         */
        public static FormatSettings defaults() {
            return new FormatSettings();
        }

        /**
         * Create compact settings (minimal spacing)
         */
        public static FormatSettings compact() {
            FormatSettings s = new FormatSettings();
            s.spaceWithinParens = false;
            s.spaceWithinBrackets = false;
            s.spaceWithinBraces = false;
            s.wrapLongLines = false;
            return s;
        }
    }

    private FormatSettings settings;

    public FormatHelper() {
        this.settings = FormatSettings.defaults();
    }

    public FormatHelper(FormatSettings settings) {
        this.settings = settings != null ? settings : FormatSettings.defaults();
    }

    public void setSettings(FormatSettings settings) {
        this.settings = settings;
    }

    public FormatSettings getSettings() {
        return settings;
    }

    // ==================== MAIN FORMAT METHOD ====================

    /**
     * Format the entire text with all configured options.
     */
    public String format(String text) {
        if (text == null || text.isEmpty()) return text;

        String[] lines = text.split("\n", -1);
        StringBuilder result = new StringBuilder();

        List<String> lambdaIndentStack = new ArrayList<>();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            if (!lambdaIndentStack.isEmpty()) {
                line = fixLambdaBlockIndent(line, lambdaIndentStack);
            }

            line = formatLine(line);

            if (settings.trimTrailingWhitespace) {
                line = trimTrailing(line);
            }

            updateLambdaBlockStack(line, lambdaIndentStack);

            result.append(line);
            if (i < lines.length - 1) {
                result.append("\n");
            }
        }

        return result.toString();
    }

    /**
     * Scan a formatted line for arrow-block patterns ({@code -> &#123;} or {@code => &#123;})
     * and track open/close braces to maintain a stack of lambda block indent levels.
     * <p>
     * When an arrow-block opens, the line's leading indent is pushed onto the stack.
     * When a closing brace {@code &#125;} is found and the stack is non-empty, the top
     * entry is popped (the brace closes the innermost lambda block).
     */
    private void updateLambdaBlockStack(String line, List<String> stack) {
        String trimmed = line.trim();
        if (trimmed.isEmpty()) return;

        String indent = extractIndent(line);
        String contentNoStrings = stripStringLiterals(trimmed);

        int opens = 0;
        int closes = 0;
        for (int i = 0; i < contentNoStrings.length(); i++) {
            char c = contentNoStrings.charAt(i);
            if (c == '{') opens++;
            else if (c == '}') closes++;
        }

        boolean hasArrowBlock = endsWithArrowBlock(contentNoStrings);

        // Process closes first (a line like "});" closes the current block)
        int netCloses = closes - opens;
        if (netCloses > 0) {
            for (int j = 0; j < netCloses && !stack.isEmpty(); j++) {
                stack.remove(stack.size() - 1);
            }
        }

        if (hasArrowBlock) {
            stack.add(indent);
        } else if (opens > closes) {
            // Non-arrow block opened — only push if inside a lambda context already
            // to track nested depth correctly
            int netOpens = opens - closes;
            if (!stack.isEmpty()) {
                for (int j = 0; j < netOpens; j++) {
                    stack.add(indent);
                }
            }
        }
    }

    /**
     * Fix indentation for a line that's inside a lambda block body.
     * Body lines get parent indent + 4 spaces; closing braces get parent indent.
     */
    private String fixLambdaBlockIndent(String line, List<String> stack) {
        if (stack.isEmpty()) return line;

        String trimmed = line.trim();
        if (trimmed.isEmpty()) return line;

        String currentIndent = extractIndent(line);
        String parentIndent = stack.get(stack.size() - 1);
        String bodyIndent = parentIndent + "    ";

        boolean isClosingBrace = trimmed.startsWith("}");

        String requiredIndent = isClosingBrace ? parentIndent : bodyIndent;

        if (getVisualLength(currentIndent) < getVisualLength(requiredIndent)) {
            return requiredIndent + trimmed;
        }

        return line;
    }

    private boolean endsWithArrowBlock(String contentNoStrings) {
        int lastOpenBrace = contentNoStrings.lastIndexOf('{');
        if (lastOpenBrace < 0) return false;

        int matchingClose = contentNoStrings.indexOf('}', lastOpenBrace);
        if (matchingClose >= 0) return false;

        int searchEnd = lastOpenBrace;
        while (searchEnd > 0 && contentNoStrings.charAt(searchEnd - 1) == ' ') searchEnd--;

        if (searchEnd >= 2) {
            String beforeBrace = contentNoStrings.substring(searchEnd - 2, searchEnd);
            return beforeBrace.equals("->") || beforeBrace.equals("=>");
        }

        return false;
    }

    private String stripStringLiterals(String content) {
        StringBuilder sb = new StringBuilder(content.length());
        boolean inString = false;
        char stringChar = 0;

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (!inString) {
                if (c == '"' || c == '\'') {
                    inString = true;
                    stringChar = c;
                    sb.append(' ');
                } else {
                    sb.append(c);
                }
            } else {
                sb.append(' ');
                if (c == '\\' && i + 1 < content.length()) {
                    i++;
                    sb.append(' ');
                } else if (c == stringChar) {
                    inString = false;
                }
            }
        }
        return sb.toString();
    }

    /**
     * Split multiple statements on one line into separate lines with proper indentation.
     * E.g., "    this.a = 1; this.b = 2;" becomes two lines
     * Also handles scope: "if(x){ int y = 20; }" becomes proper multi-line structure
     */
    private String splitStatementsOntoSeparateLines(String text) {
        String[] lines = text.split("\n", -1);
        StringBuilder result = new StringBuilder();

        for (int lineIdx = 0; lineIdx < lines.length; lineIdx++) {
            String line = lines[lineIdx];

            // Skip empty lines or comment-only lines
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("//") || trimmed.startsWith("/*") || trimmed.startsWith("*")) {
                result.append(line);
                if (lineIdx < lines.length - 1) result.append("\n");
                continue;
            }

            // Extract leading indentation
            int indent = 0;
            while (indent < line.length() && (line.charAt(indent) == ' ' || line.charAt(indent) == '\t')) {
                indent++;
            }
            String indentation = line.substring(0, indent);
            String content = line.substring(indent);

            // Split by semicolons (but preserve those in strings, for loops, etc.)
            List<String> statements = splitBySemicolons(content);

            for (int si = 0; si < statements.size(); si++) {
                String stmt = statements.get(si).trim();
                if (stmt.isEmpty()) continue;

                // Check if statement opens a scope { and has code after it
                int braceIdx = stmt.indexOf('{');
                if (braceIdx >= 0 && braceIdx < stmt.length() - 1) {
                    String afterBrace = stmt.substring(braceIdx + 1).trim();
                    if (!afterBrace.isEmpty() && !afterBrace.equals("}")) {
                        // Split: keep up to and including {, put rest on new line with extra indent
                        result.append(indentation).append(stmt, 0, braceIdx + 1).append("\n");
                        // Calculate new indent (add 4 spaces)
                        String newIndent = indentation + "    ";
                        result.append(newIndent).append(afterBrace);
                        if (si < statements.size() - 1) result.append("\n");
                        continue;
                    }
                }

                result.append(indentation).append(stmt);
                // Add semicolon back if it was removed
                if (si < statements.size() - 1) {
                    result.append("\n");
                }
            }

            if (lineIdx < lines.length - 1) result.append("\n");
        }

        return result.toString();
    }

    /**
     * Split content by semicolons, preserving those in strings and for-loops.
     */
    private List<String> splitBySemicolons(String content) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int parenDepth = 0;
        boolean inString = false;
        boolean inChar = false;
        boolean escaped = false;
        int forLoopDepth = 0;

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);

            // Track strings
            if (c == '\"' && !escaped && !inChar) {
                inString = !inString;
            } else if (c == '\'' && !escaped && !inString) {
                inChar = !inChar;
            }

            escaped = (c == '\\' && !escaped);

            if (inString || inChar) {
                current.append(c);
                continue;
            }

            // Track for loops
            if (c == '(' && i >= 3 && content.substring(Math.max(0, i - 3), i + 1).matches(".*\\bfor\\s*\\(")) {
                forLoopDepth++;
                parenDepth++;
            } else if (c == '(') {
                parenDepth++;
            } else if (c == ')') {
                parenDepth--;
                if (forLoopDepth > 0 && parenDepth < forLoopDepth) {
                    forLoopDepth = 0;
                }
            }

            // Semicolon: split if not in for-loop header
            if (c == ';' && forLoopDepth == 0) {
                current.append(c);
                statements.add(current.toString());
                current = new StringBuilder();
                continue;
            }

            current.append(c);
        }

        // Add remaining content
        if (current.length() > 0) {
            statements.add(current.toString());
        }

        return statements;
    }

    /**
     * Format a single line of code.
     */
    private String formatLine(String line) {
        if (line.trim().isEmpty()) return line;

        // Preserve leading indentation
        int indent = 0;
        while (indent < line.length() && (line.charAt(indent) == ' ' || line.charAt(indent) == '\t')) {
            indent++;
        }
        String indentation = line.substring(0, indent);
        String content = line.substring(indent);

        if (content.isEmpty()) return line;

        // Skip formatting for lines that are entirely comments
        String trimmed = content.trim();
        if (trimmed.startsWith("//") || trimmed.startsWith("*") || trimmed.startsWith("/*")) {
            return line;
        }

        // Skip formatting for continuation lines (lines that start with operators, dots, etc.)
        // These are wrapped lines from a previous format and should be preserved as-is
        if (isContinuationLine(trimmed)) {
            return line;
        }

        // Format the content
        String formatted = formatContent(content);

        return indentation + formatted;
    }

    /**
     * Check if a line appears to be a continuation of a previous line
     * (i.e., starts with an operator, dot, or other continuation character).
     */
    private boolean isContinuationLine(String trimmedContent) {
        if (trimmedContent.isEmpty()) return false;
        char firstChar = trimmedContent.charAt(0);
        // Lines starting with these are likely continuations
        if (firstChar == '.' || firstChar == '+' || firstChar == '-' ||
            firstChar == '*' || firstChar == '/' || firstChar == '%' ||
            firstChar == '&' || firstChar == '|' || firstChar == '^' ||
            firstChar == '?' || firstChar == ':' || firstChar == ',') {
            return true;
        }
        // Also check for && and ||
        if (trimmedContent.startsWith("&&") || trimmedContent.startsWith("||")) {
            return true;
        }
        return false;
    }

    /**
     * Format line content, preserving strings and comments.
     * Splits content into code segments and preserved regions (strings/comments),
     * then tokenizes and formats each code segment.
     */
    private String formatContent(String content) {
        List<int[]> preserveRegions = findPreserveRegions(content);

        if (preserveRegions.isEmpty()) {
            return formatCodeSegment(content);
        }

        StringBuilder result = new StringBuilder();
        int lastEnd = 0;

        for (int[] region : preserveRegions) {
            if (region[0] > lastEnd) {
                String codeSegment = content.substring(lastEnd, region[0]);
                String formatted = formatCodeSegment(codeSegment);
                result.append(formatted);

                // Ensure space between code ending with operator and following preserved region
                if (!formatted.isEmpty() && !formatted.endsWith(" ")) {
                    char lastChar = formatted.charAt(formatted.length() - 1);
                    boolean operatorEnd = SINGLE_CHAR_OPERATORS.indexOf(lastChar) >= 0;
                    boolean identifierEnd = Character.isJavaIdentifierPart(lastChar);
                    if (operatorEnd || identifierEnd) {
                        result.append(' ');
                    }
                }
            }

            // Ensure space before line comments (// ...)
            String preserved = content.substring(region[0], region[1]);
            if (preserved.startsWith("//") && result.length() > 0 && result.charAt(result.length() - 1) != ' ') {
                result.append(' ');
            }

            // Normalize line comment: ensure space after // prefix
            if (preserved.startsWith("//") && preserved.length() > 2 && preserved.charAt(2) != ' ') {
                preserved = "// " + preserved.substring(2);
            }

            result.append(preserved);
            lastEnd = region[1];
        }

        if (lastEnd < content.length()) {
            String codeSegment = content.substring(lastEnd);
            String formatted = formatCodeSegment(codeSegment);
            // Ensure space after block comment end (*/) before code tokens
            if (result.length() > 1 && !formatted.isEmpty()) {
                String tail = result.substring(result.length() - 2);
                char nextChar = formatted.charAt(0);
                if (tail.equals("*/") && nextChar != ';' && nextChar != ',' && nextChar != ')') {
                    result.append(' ');
                }
            }
            result.append(formatted);
        }

        return result.toString();
    }

    /**
     * Find string literals and comments that should not be formatted.
     */
    private List<int[]> findPreserveRegions(String content) {
        List<int[]> regions = new ArrayList<>();

        int i = 0;
        while (i < content.length()) {
            char c = content.charAt(i);

            // String literal
            if (c == '"') {
                int end = findStringEnd(content, i, '"');
                regions.add(new int[]{i, end});
                i = end;
                continue;
            }

            // Char literal
            if (c == '\'') {
                int end = findStringEnd(content, i, '\'');
                regions.add(new int[]{i, end});
                i = end;
                continue;
            }

            // Line comment
            if (c == '/' && i + 1 < content.length() && content.charAt(i + 1) == '/') {
                regions.add(new int[]{i, content.length()});
                break;
            }

            // Block comment (could span multiple lines but we process line-by-line)
            if (c == '/' && i + 1 < content.length() && content.charAt(i + 1) == '*') {
                int end = content.indexOf("*/", i + 2);
                if (end >= 0) {
                    regions.add(new int[]{i, end + 2});
                    i = end + 2;
                } else {
                    regions.add(new int[]{i, content.length()});
                    break;
                }
                continue;
            }

            i++;
        }

        return regions;
    }

    /**
     * Find end of string/char literal.
     */
    private int findStringEnd(String content, int start, char quote) {
        int i = start + 1;
        while (i < content.length()) {
            char c = content.charAt(i);
            if (c == '\\' && i + 1 < content.length()) {
                i += 2; // Skip escape sequence
                continue;
            }
            if (c == quote) {
                return i + 1;
            }
            i++;
        }
        return content.length();
    }

    // ==================== TOKEN-BASED CODE SEGMENT FORMATTING ====================

    /**
     * Format a code segment (no strings/comments) using the token pipeline:
     * tokenize → format → reconstruct.
     */
    private String formatCodeSegment(String code) {
        if (code.isEmpty()) return code;

        // Phase 1: Tokenize
        List<Token> tokens = tokenize(code);

        // Phase 1.5: Merge split operators (e.g. = = → ==, > = → >=)
        if (settings.normalizeWhitespace) {
            tokens = mergeSplitOperators(tokens);
        }

        // Phase 1.75: Split >> and >>> into individual > tokens when in generic context.
        // Must run AFTER merge so that merged >> tokens get split back when generic.
        tokens = splitGenericShiftOperators(tokens);

        // Phase 2: Format (apply spacing rules)
        List<Token> formatted = formatTokens(tokens);

        // Phase 3: Reconstruct
        return reconstruct(formatted);
    }

    // ==================== PHASE 1: TOKENIZER ====================

    /**
     * Tokenize a code segment into a list of typed tokens.
     * Multi-character operators are matched longest-first.
     * Whitespace is collapsed into single-space tokens when normalizeWhitespace is enabled.
     */
    private List<Token> tokenize(String code) {
        List<Token> tokens = new ArrayList<>();
        int i = 0;
        int len = code.length();

        while (i < len) {
            char c = code.charAt(i);

            // Whitespace
            if (c == ' ' || c == '\t') {
                int start = i;
                while (i < len && (code.charAt(i) == ' ' || code.charAt(i) == '\t')) {
                    i++;
                }
                String ws = settings.normalizeWhitespace ? " " : code.substring(start, i);
                tokens.add(new Token(TokenType.WHITESPACE, ws, start));
                continue;
            }

            // Try multi-char operators (longest first)
            String multiOp = matchMultiCharOperator(code, i);
            if (multiOp != null) {
                tokens.add(new Token(TokenType.OPERATOR, multiOp, i));
                i += multiOp.length();
                continue;
            }

            // Punctuation (single-char tokens with specific types)
            if (c == '(') { tokens.add(new Token(TokenType.PAREN_OPEN, "(", i)); i++; continue; }
            if (c == ')') { tokens.add(new Token(TokenType.PAREN_CLOSE, ")", i)); i++; continue; }
            if (c == '[') { tokens.add(new Token(TokenType.BRACKET_OPEN, "[", i)); i++; continue; }
            if (c == ']') { tokens.add(new Token(TokenType.BRACKET_CLOSE, "]", i)); i++; continue; }
            if (c == '{') { tokens.add(new Token(TokenType.BRACE_OPEN, "{", i)); i++; continue; }
            if (c == '}') { tokens.add(new Token(TokenType.BRACE_CLOSE, "}", i)); i++; continue; }
            if (c == ',') { tokens.add(new Token(TokenType.COMMA, ",", i)); i++; continue; }
            if (c == ';') { tokens.add(new Token(TokenType.SEMICOLON, ";", i)); i++; continue; }
            if (c == ':' && i + 1 < len && code.charAt(i + 1) == ':') {
                tokens.add(new Token(TokenType.OPERATOR, "::", i));
                i += 2;
                continue;
            }
            if (c == ':') { tokens.add(new Token(TokenType.COLON, ":", i)); i++; continue; }
            if (c == '.') { tokens.add(new Token(TokenType.DOT, ".", i)); i++; continue; }

            // Single-char operators (=, +, -, *, /, %, &, |, ^, !, ~, <, >, ?)
            if (SINGLE_CHAR_OPERATORS.indexOf(c) >= 0) {
                tokens.add(new Token(TokenType.OPERATOR, String.valueOf(c), i));
                i++;
                continue;
            }

            // Number
            if (Character.isDigit(c) || (c == '.' && i + 1 < len && Character.isDigit(code.charAt(i + 1)))) {
                int start = i;
                // Handle hex (0x...), binary (0b...), octal (0o...)
                if (c == '0' && i + 1 < len) {
                    char next = code.charAt(i + 1);
                    if (next == 'x' || next == 'X' || next == 'b' || next == 'B' || next == 'o' || next == 'O') {
                        i += 2;
                        while (i < len && isHexDigit(code.charAt(i))) i++;
                        tokens.add(new Token(TokenType.NUMBER, code.substring(start, i), start));
                        continue;
                    }
                }
                while (i < len && (Character.isDigit(code.charAt(i)) || code.charAt(i) == '.')) i++;
                // Handle suffixes like L, f, d
                if (i < len && "LlFfDd".indexOf(code.charAt(i)) >= 0) i++;
                tokens.add(new Token(TokenType.NUMBER, code.substring(start, i), start));
                continue;
            }

            // Annotation: @ followed by identifier → single token (@Override, @Deprecated)
            if (c == '@') {
                int start = i;
                i++;
                // Skip whitespace between @ and annotation name
                while (i < len && (code.charAt(i) == ' ' || code.charAt(i) == '\t')) i++;
                if (i < len && Character.isJavaIdentifierStart(code.charAt(i))) {
                    while (i < len && Character.isJavaIdentifierPart(code.charAt(i))) i++;
                    String annotation = "@" + code.substring(start + 1, i).trim();
                    tokens.add(new Token(TokenType.IDENTIFIER, annotation, start));
                } else {
                    tokens.add(new Token(TokenType.IDENTIFIER, "@", start));
                }
                continue;
            }

            // Identifier or keyword
            if (Character.isJavaIdentifierStart(c)) {
                int start = i;
                while (i < len && Character.isJavaIdentifierPart(code.charAt(i))) i++;
                String word = code.substring(start, i);
                if (isKeyword(word)) {
                    tokens.add(new Token(TokenType.KEYWORD, word, start));
                } else {
                    tokens.add(new Token(TokenType.IDENTIFIER, word, start));
                }
                continue;
            }

            // Anything else — emit as identifier (safety fallback)
            tokens.add(new Token(TokenType.IDENTIFIER, String.valueOf(c), i));
            i++;
        }

        return tokens;
    }

    /**
     * Try to match a multi-character operator at the given position.
     * Checks longest operators first for correct greedy matching.
     */
    private String matchMultiCharOperator(String code, int pos) {
        for (String op : MULTI_CHAR_OPERATORS) {
            if (pos + op.length() <= code.length() &&
                code.substring(pos, pos + op.length()).equals(op)) {
                return op;
            }
        }
        return null;
    }

    private boolean isHexDigit(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F') || c == '_';
    }

    private boolean isKeyword(String word) {
        for (String kw : CONTROL_KEYWORDS) {
            if (kw.equals(word)) return true;
        }
        return false;
    }

    // ==================== PHASE 1.25: SPLIT GENERIC SHIFT OPERATORS ====================

    /**
     * Split {@code >>} and {@code >>>} operator tokens into individual {@code >}
     * tokens when they appear inside a generic type context (e.g., {@code List<Map<String, Integer>>}).
     * <p>
     * Without this, the greedy tokenizer matches {@code >>} as the right-shift operator,
     * causing the formatter to add spaces around it as an arithmetic/bitwise operator.
     */
    private List<Token> splitGenericShiftOperators(List<Token> tokens) {
        int genericDepth = 0;
        List<Token> result = new ArrayList<>();

        for (int i = 0; i < tokens.size(); i++) {
            Token t = tokens.get(i);

            if (t.type == TokenType.OPERATOR && t.value.equals("<")) {
                Token before = findPrevNonWhitespaceInList(result);
                if (before != null && before.type == TokenType.IDENTIFIER && !before.value.isEmpty()
                    && Character.isUpperCase(before.value.charAt(0))) {
                    genericDepth++;
                }
            }

            if (genericDepth > 0 && t.type == TokenType.OPERATOR
                && (t.value.equals(">>") || t.value.equals(">>>"))) {
                for (int c = 0; c < t.value.length(); c++) {
                    result.add(new Token(TokenType.OPERATOR, ">", t.position + c));
                    genericDepth--;
                    if (genericDepth < 0) genericDepth = 0;
                }
                continue;
            }

            if (t.type == TokenType.OPERATOR && t.value.equals(">")) {
                if (genericDepth > 0) {
                    genericDepth--;
                }
            }

            result.add(t);
        }

        return result;
    }

    private Token findPrevNonWhitespaceInList(List<Token> tokens) {
        for (int i = tokens.size() - 1; i >= 0; i--) {
            if (tokens.get(i).type != TokenType.WHITESPACE) {
                return tokens.get(i);
            }
        }
        return null;
    }

    // ==================== PHASE 1.5: MERGE SPLIT OPERATORS ====================

    /**
     * Merge operator tokens that were split by whitespace in the original code.
     * Example: tokens [=] [ws] [=] → single token [==].
     * This handles the case where users type "x = = y" and the formatter
     * should recognize it as "x == y" (the == operator was split).
     *
     * Merge candidates (longest first):
     *   > > > =  →  >>>=       = = =  →  ===       ! = =  →  !==
     *   > > =    →  >>=        < < =  →  <<=       > > >  →  >>>
     *   > >      →  >>         < <    →  <<        = =    →  ==
     *   ! =      →  !=         > =    →  >=        < =    →  <=
     *   + =      →  +=         - =    →  -=        * =    →  *=
     *   / =      →  /=         % =    →  %=        & =    →  &=
     *   | =      →  |=         ^ =    →  ^=        & &    →  &&
     *   | |      →  ||
     */
    private List<Token> mergeSplitOperators(List<Token> tokens) {
        // Extract only operator values (ignoring whitespace between them)
        // and try to merge consecutive operators into known multi-char operators
        List<Token> result = new ArrayList<>();
        int i = 0;

        while (i < tokens.size()) {
            Token t = tokens.get(i);

            if (t.type == TokenType.OPERATOR) {
                // Try to merge with following operator tokens (skipping whitespace)
                String merged = tryMergeOperators(tokens, i);
                if (merged != null && merged.length() > t.value.length()) {
                    result.add(new Token(TokenType.OPERATOR, merged, t.position));
                    // Skip past all the tokens we consumed
                    i = skipMergedTokens(tokens, i, merged);
                    continue;
                }
            }

            // Merge split :: (method reference) — two COLON tokens with optional whitespace
            if (t.type == TokenType.COLON) {
                int next = i + 1;
                while (next < tokens.size() && tokens.get(next).type == TokenType.WHITESPACE) next++;
                if (next < tokens.size() && tokens.get(next).type == TokenType.COLON) {
                    result.add(new Token(TokenType.OPERATOR, "::", t.position));
                    i = next + 1;
                    continue;
                }
            }

            result.add(t);
            i++;
        }

        return result;
    }

    /**
     * Try to merge operator tokens starting at index, skipping whitespace between them.
     * Returns the merged operator string if a known multi-char operator is formed,
     * or null if no merge is possible.
     */
    private String tryMergeOperators(List<Token> tokens, int startIdx) {
        StringBuilder ops = new StringBuilder();
        List<Integer> opIndices = new ArrayList<>();
        boolean hasWhitespaceBetween = false;

        for (int i = startIdx; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            if (t.type == TokenType.OPERATOR) {
                ops.append(t.value);
                opIndices.add(i);
            } else if (t.type == TokenType.WHITESPACE) {
                if (!opIndices.isEmpty()) {
                    hasWhitespaceBetween = true;
                }
                continue;
            } else {
                break;
            }
        }

        String allOps = ops.toString();

        for (String multiOp : MULTI_CHAR_OPERATORS) {
            if (allOps.startsWith(multiOp) && multiOp.length() > 1) {
                // Don't merge ++ or -- when whitespace separated the operators.
                // Whitespace-separated +/+ or -/- means binary op + unary op (e.g. a - -b),
                // not increment/decrement. Real ++ and -- are tokenized as single tokens.
                if (hasWhitespaceBetween && (multiOp.equals("++") || multiOp.equals("--"))) {
                    continue;
                }
                return multiOp;
            }
        }

        return null;
    }

    /**
     * Skip past the tokens that were consumed by the merge.
     * Returns the index of the first token AFTER the merged sequence.
     */
    private int skipMergedTokens(List<Token> tokens, int startIdx, String merged) {
        int charsConsumed = 0;
        int i = startIdx;

        while (i < tokens.size() && charsConsumed < merged.length()) {
            Token t = tokens.get(i);
            if (t.type == TokenType.OPERATOR) {
                charsConsumed += t.value.length();
            }
            // Skip whitespace tokens between operators
            i++;
        }

        return i;
    }

    // ==================== PHASE 2: TOKEN FORMATTER ====================

    /**
     * Apply formatting rules to a token list, producing a new list with
     * whitespace tokens inserted/removed/adjusted according to settings.
     * <p>
     * The formatter works by iterating through tokens and deciding what
     * whitespace should appear between each pair of non-whitespace tokens.
     */
    private List<Token> formatTokens(List<Token> tokens) {
        // First, strip existing whitespace tokens to normalize
        List<Token> stripped = new ArrayList<>();
        for (Token t : tokens) {
            if (t.type != TokenType.WHITESPACE) {
                stripped.add(t);
            }
        }

        if (stripped.isEmpty()) return tokens;

        // Now rebuild with correct spacing
        List<Token> result = new ArrayList<>();
        result.add(stripped.get(0));

        for (int i = 1; i < stripped.size(); i++) {
            Token prev = stripped.get(i - 1);
            Token curr = stripped.get(i);

            // Determine the previous non-whitespace before prev (for context)
            Token prevPrev = i >= 2 ? stripped.get(i - 2) : null;

            String spacing = determineSpacing(prevPrev, prev, curr, stripped, i);

            if (!spacing.isEmpty()) {
                result.add(new Token(TokenType.WHITESPACE, spacing, -1));
            }
            result.add(curr);
        }

        return result;
    }

    /**
     * Determine the spacing string that should appear between prev and curr tokens.
     * Returns "" for no space, " " for single space.
     *
     * @param prevPrev token before prev (may be null)
     * @param prev     the token before the gap
     * @param curr     the token after the gap
     * @param allTokens all stripped tokens
     * @param currIdx  index of curr in allTokens
     */
    private String determineSpacing(Token prevPrev, Token prev, Token curr,
                                     List<Token> allTokens, int currIdx) {

        // ---- DOT: never space around dots (method chains, property access) ----
        if (prev.type == TokenType.DOT || curr.type == TokenType.DOT) {
            return "";
        }

        // ---- OPERATOR SPACING ----
        if (prev.type == TokenType.OPERATOR || curr.type == TokenType.OPERATOR) {
            return determineOperatorSpacing(prevPrev, prev, curr, allTokens, currIdx);
        }

        // ---- KEYWORD before PAREN_OPEN: if (...), while (...), etc. ----
        if (prev.type == TokenType.KEYWORD && curr.type == TokenType.PAREN_OPEN) {
            return determineKeywordParenSpacing(prev);
        }

        // ---- IDENTIFIER before PAREN_OPEN: method call foo(...) ----
        if (prev.type == TokenType.IDENTIFIER && curr.type == TokenType.PAREN_OPEN) {
            return settings.spaceBeforeMethodParens ? " " : "";
        }

        // ---- PAREN_CLOSE before BRACE_OPEN: ) { ----
        if (prev.type == TokenType.PAREN_CLOSE && curr.type == TokenType.BRACE_OPEN) {
            return settings.spaceBeforeOpenBrace ? " " : "";
        }

        // ---- KEYWORD before BRACE_OPEN: else {, try {, finally { ----
        if (prev.type == TokenType.KEYWORD && curr.type == TokenType.BRACE_OPEN) {
            return settings.spaceBeforeOpenBrace ? " " : "";
        }

        // ---- IDENTIFIER/NUMBER/OPERATOR(>) before BRACE_OPEN: class Foo {, enum Bar { ----
        if ((prev.type == TokenType.IDENTIFIER || prev.type == TokenType.NUMBER ||
             (prev.type == TokenType.OPERATOR && prev.value.equals(">")))
            && curr.type == TokenType.BRACE_OPEN) {
            return settings.spaceBeforeOpenBrace ? " " : "";
        }

        // ---- PAREN_OPEN / PAREN_CLOSE: inner spacing ----
        if (prev.type == TokenType.PAREN_OPEN) {
            if (curr.type == TokenType.PAREN_CLOSE) return ""; // empty ()
            return settings.spaceWithinParens ? " " : "";
        }
        if (curr.type == TokenType.PAREN_CLOSE) {
            return settings.spaceWithinParens ? " " : "";
        }

        // ---- BRACKET_OPEN / BRACKET_CLOSE: inner spacing ----
        if (prev.type == TokenType.BRACKET_OPEN) {
            if (curr.type == TokenType.BRACKET_CLOSE) return ""; // empty []
            return settings.spaceWithinBrackets ? " " : "";
        }
        if (curr.type == TokenType.BRACKET_CLOSE) {
            return settings.spaceWithinBrackets ? " " : "";
        }

        // ---- COMMA spacing ----
        if (prev.type == TokenType.COMMA) {
            return settings.spaceAfterComma ? " " : "";
        }
        if (curr.type == TokenType.COMMA) {
            return settings.spaceBeforeComma ? " " : "";
        }

        // ---- SEMICOLON spacing ----
        if (prev.type == TokenType.SEMICOLON) {
            return settings.spaceAfterSemicolon ? " " : "";
        }
        if (curr.type == TokenType.SEMICOLON) {
            return settings.spaceBeforeSemicolon ? " " : "";
        }

        // ---- COLON spacing (ternary : always gets space, enhanced-for : always gets space, label : uses settings) ----
        if (prev.type == TokenType.COLON) {
            if (isTernaryColon(allTokens, currIdx - 1)) return " ";
            if (isEnhancedForLoopColon(allTokens, currIdx - 1)) return " ";
            return settings.spaceAfterColon ? " " : "";
        }
        if (curr.type == TokenType.COLON) {
            if (isTernaryColon(allTokens, currIdx)) return " ";
            if (isEnhancedForLoopColon(allTokens, currIdx)) return " ";
            return settings.spaceBeforeColon ? " " : "";
        }

        // ---- IDENTIFIER/NUMBER next to BRACKET_OPEN: arr[0] no space ----
        if ((prev.type == TokenType.IDENTIFIER || prev.type == TokenType.NUMBER ||
             prev.type == TokenType.PAREN_CLOSE) && curr.type == TokenType.BRACKET_OPEN) {
            return "";
        }

        // ---- BRACKET_CLOSE before IDENTIFIER/KEYWORD: int[] name, String[] args ----
        if (prev.type == TokenType.BRACKET_CLOSE &&
            (curr.type == TokenType.IDENTIFIER || curr.type == TokenType.KEYWORD ||
             curr.type == TokenType.NUMBER)) {
            return " ";
        }

        // ---- KEYWORD next to KEYWORD, IDENTIFIER, etc: must have space ----
        if (prev.type == TokenType.KEYWORD &&
            (curr.type == TokenType.KEYWORD || curr.type == TokenType.IDENTIFIER ||
             curr.type == TokenType.NUMBER)) {
            return " ";
        }
        if (curr.type == TokenType.KEYWORD &&
            (prev.type == TokenType.KEYWORD || prev.type == TokenType.IDENTIFIER ||
             prev.type == TokenType.NUMBER || prev.type == TokenType.PAREN_CLOSE ||
             prev.type == TokenType.BRACE_CLOSE)) {
            return " ";
        }

        // ---- IDENTIFIER/NUMBER adjacency: need space ----
        if ((prev.type == TokenType.IDENTIFIER || prev.type == TokenType.NUMBER) &&
            (curr.type == TokenType.IDENTIFIER || curr.type == TokenType.NUMBER)) {
            return " ";
        }

        // ---- PAREN_CLOSE before IDENTIFIER/KEYWORD: ) instanceof, (int) x ----
        if (prev.type == TokenType.PAREN_CLOSE &&
            (curr.type == TokenType.IDENTIFIER || curr.type == TokenType.NUMBER)) {
            return " ";
        }

        // ---- BRACE_OPEN: space after { when followed by code ----
        if (prev.type == TokenType.BRACE_OPEN) {
            if (curr.type == TokenType.BRACE_CLOSE) return ""; // empty {}
            return settings.spaceWithinBraces ? " " : "";
        }
        // ---- BRACE_CLOSE: space before } when preceded by code ----
        if (curr.type == TokenType.BRACE_CLOSE) {
            return settings.spaceWithinBraces ? " " : "";
        }

        // Default: no space
        return "";
    }

    /**
     * Determine spacing around an operator token, considering whether it's
     * unary, binary, assignment, comparison, logical, arithmetic, or bitwise.
     */
    private String determineOperatorSpacing(Token prevPrev, Token prev, Token curr,
                                             List<Token> allTokens, int currIdx) {
        // When prev is the operator
        if (prev.type == TokenType.OPERATOR) {
            String op = prev.value;

            // ++ and -- : never add spaces around them (they bind tightly)
            if (op.equals("++") || op.equals("--")) {
                return settings.spaceAroundUnaryIncDec ? " " : "";
            }

            // :: method reference: no spaces
            if (op.equals("::")) {
                return "";
            }

            // Arrow operators -> and => always get spaces
            if (op.equals("->") || op.equals("=>")) {
                return " ";
            }

            // Determine operator category and apply setting
            if (isAssignmentOperator(op)) {
                return settings.spaceAroundAssignment ? " " : "";
            }
            if (isComparisonOperator(op)) {
                if ((op.equals("<") || op.equals(">")) && isGenericContext(allTokens, currIdx - 1)) {
                    if (op.equals(">") && (curr.type == TokenType.IDENTIFIER || curr.type == TokenType.KEYWORD)) {
                        return " ";
                    }
                    return "";
                }
                return settings.spaceAroundComparison ? " " : "";
            }
            if (isLogicalOperator(op)) {
                return settings.spaceAroundLogical ? " " : "";
            }
            // Unary ! and ~ — check before bitwise since ~ is in both categories
            if (op.equals("!") || op.equals("~")) {
                return settings.spaceAroundUnaryNot ? " " : "";
            }
            if (isBitwiseOperator(op)) {
                return settings.spaceAroundBitwise ? " " : "";
            }
            if (isArithmeticOperator(op)) {
                // Check unary context: was this operator unary?
                // prevPrev is the token before this operator
                if (isUnaryOperatorToken(op, null, prevPrev)) {
                    return "";  // No space after unary operator
                }
                return settings.spaceAroundArithmetic ? " " : "";
            }
            // Ternary ?
            if (op.equals("?")) {
                return " ";
            }

            // Default for unknown operators
            return " ";
        }

        // When curr is the operator
        if (curr.type == TokenType.OPERATOR) {
            String op = curr.value;

            // ++ and -- : never add spaces around them
            if (op.equals("++") || op.equals("--")) {
                return settings.spaceAroundUnaryIncDec ? " " : "";
            }

            // :: method reference: no spaces
            if (op.equals("::")) {
                return "";
            }

            if (op.equals("->") || op.equals("=>")) {
                return " ";
            }

            if (isAssignmentOperator(op)) {
                return settings.spaceAroundAssignment ? " " : "";
            }
            if (isComparisonOperator(op)) {
                if ((op.equals("<") || op.equals(">")) && isGenericContext(allTokens, currIdx)) {
                    // Space before < when preceded by a keyword (e.g. public <T>)
                    if (op.equals("<") && prev.type == TokenType.KEYWORD) {
                        return " ";
                    }
                    return "";
                }
                return settings.spaceAroundComparison ? " " : "";
            }
            if (isLogicalOperator(op)) {
                return settings.spaceAroundLogical ? " " : "";
            }
            // Unary ! and ~ — check before bitwise since ~ is in both categories
            if (op.equals("!") || op.equals("~")) {
                return settings.spaceAroundUnaryNot ? " " : "";
            }
            if (isBitwiseOperator(op)) {
                return settings.spaceAroundBitwise ? " " : "";
            }
            if (isArithmeticOperator(op)) {
                // Check if unary
                if (isUnaryOperatorToken(op, prevPrev, prev)) {
                    return "";  // No space before unary operator
                }
                return settings.spaceAroundArithmetic ? " " : "";
            }
            // Ternary ?
            if (op.equals("?")) {
                return " ";
            }

            return " ";
        }

        return " ";
    }

    /**
     * Determine spacing between a keyword and a following open-paren.
     * Applies settings for if/while/for/switch/catch keywords.
     */
    private String determineKeywordParenSpacing(Token keyword) {
        String kw = keyword.value;
        if (kw.equals("if")) return settings.spaceBeforeIfParens ? " " : "";
        if (kw.equals("while")) return settings.spaceBeforeWhileParens ? " " : "";
        if (kw.equals("for")) return settings.spaceBeforeForParens ? " " : "";
        if (kw.equals("switch")) return settings.spaceBeforeSwitchParens ? " " : "";
        if (kw.equals("catch")) return settings.spaceBeforeCatchParens ? " " : "";
        // For other keywords followed by paren (e.g. return(...) — unusual but allowed)
        return " ";
    }

    // ---- Operator classification helpers ----

    /**
     * Check if a colon at the given index is part of a ternary expression (? :)
     * by scanning backwards for a matching ? operator.
     */
    private boolean isTernaryColon(List<Token> tokens, int colonIdx) {
        for (int i = colonIdx - 1; i >= 0; i--) {
            Token t = tokens.get(i);
            if (t.type == TokenType.OPERATOR && t.value.equals("?")) return true;
            if (t.type == TokenType.SEMICOLON || t.type == TokenType.BRACE_OPEN ||
                t.type == TokenType.BRACE_CLOSE) return false;
        }
        return false;
    }

    /**
     * Check if a colon at the given index is the separator in an enhanced for-loop
     * ({@code for (Type var : collection)}) by scanning backwards from the colon
     * for an opening paren preceded by the {@code for} keyword.
     */
    private boolean isEnhancedForLoopColon(List<Token> tokens, int colonIdx) {
        int depth = 0;
        for (int i = colonIdx - 1; i >= 0; i--) {
            Token t = tokens.get(i);
            if (t.type == TokenType.PAREN_CLOSE) depth++;
            else if (t.type == TokenType.PAREN_OPEN) {
                if (depth > 0) {
                    depth--;
                } else {
                    // Found the opening paren at our depth — check if preceded by 'for'
                    for (int j = i - 1; j >= 0; j--) {
                        if (tokens.get(j).type != TokenType.WHITESPACE) {
                            return tokens.get(j).type == TokenType.KEYWORD
                                && tokens.get(j).value.equals("for");
                        }
                    }
                    return false;
                }
            }
            // Stop at statement boundaries
            if (t.type == TokenType.SEMICOLON || t.type == TokenType.BRACE_OPEN ||
                t.type == TokenType.BRACE_CLOSE) {
                return false;
            }
        }
        return false;
    }

    private boolean isAssignmentOperator(String op) {
        switch (op) {
            case "=": case "+=": case "-=": case "*=": case "/=": case "%=":
            case "&=": case "|=": case "^=": case "<<=": case ">>=": case ">>>=":
                return true;
            default:
                return false;
        }
    }

    private boolean isComparisonOperator(String op) {
        switch (op) {
            case "==": case "!=": case "===": case "!==":
            case "<": case ">": case "<=": case ">=":
                return true;
            default:
                return false;
        }
    }

    private boolean isLogicalOperator(String op) {
        return op.equals("&&") || op.equals("||");
    }

    private boolean isBitwiseOperator(String op) {
        switch (op) {
            case "&": case "|": case "^": case "~":
            case "<<": case ">>": case ">>>":
                return true;
            default:
                return false;
        }
    }

    private boolean isArithmeticOperator(String op) {
        return op.equals("+") || op.equals("-") || op.equals("*") ||
               op.equals("/") || op.equals("%");
    }

    /**
     * Determine if an operator is in unary context based on the previous token.
     * Unary context: after operator, open paren/bracket/brace, comma, semicolon,
     * colon, keyword, or at start of expression.
     */
    private boolean isUnaryOperatorToken(String op, Token prevPrev, Token prevToken) {
        // Only +, - can be unary in arithmetic context
        if (!op.equals("+") && !op.equals("-")) return false;

        // If prevToken is null (start of expression) → unary
        if (prevToken == null) return true;

        // After these token types → unary
        switch (prevToken.type) {
            case OPERATOR:
            case PAREN_OPEN:
            case BRACKET_OPEN:
            case BRACE_OPEN:
            case COMMA:
            case SEMICOLON:
            case COLON:
            case KEYWORD:
                return true;
            default:
                return false;
        }
    }

    /**
     * Check if a < or > operator at the given token index is likely part of a
     * generic type declaration (e.g., List<String>).
     * Uses surrounding token context rather than regex.
     */
    private boolean isGenericContext(List<Token> tokens, int opIdx) {
        if (opIdx < 0 || opIdx >= tokens.size()) return false;

        Token opToken = tokens.get(opIdx);
        String op = opToken.value;

        if (op.equals("<")) {
            // Check if preceded by an identifier that looks like a type (starts with uppercase)
            // or by known generic-compatible keywords (var, let, const)
            Token before = findPrevNonWhitespace(tokens, opIdx);
            if (before != null) {
                if (before.type == TokenType.IDENTIFIER && !before.value.isEmpty() &&
                    Character.isUpperCase(before.value.charAt(0))) {
                    return true;
                }
                if (before.type == TokenType.KEYWORD &&
                    (before.value.equals("var") || before.value.equals("let") ||
                     before.value.equals("const"))) {
                    return true;
                }
            }
            // Check if there's a matching > ahead
            int depth = 1;
            for (int i = opIdx + 1; i < tokens.size() && depth > 0; i++) {
                Token t = tokens.get(i);
                if (t.type == TokenType.WHITESPACE) continue;
                if (t.type == TokenType.OPERATOR && t.value.equals("<")) depth++;
                else if (t.type == TokenType.OPERATOR && t.value.equals(">")) depth--;
                else if (t.type == TokenType.SEMICOLON || t.type == TokenType.BRACE_OPEN ||
                         t.type == TokenType.BRACE_CLOSE) break; // Not a generic
            }
            return depth == 0;
        } else if (op.equals(">")) {
            // Check if there's a matching < before
            int depth = 1;
            for (int i = opIdx - 1; i >= 0 && depth > 0; i--) {
                Token t = tokens.get(i);
                if (t.type == TokenType.WHITESPACE) continue;
                if (t.type == TokenType.OPERATOR && t.value.equals(">")) depth++;
                else if (t.type == TokenType.OPERATOR && t.value.equals("<")) depth--;
                else if (t.type == TokenType.SEMICOLON || t.type == TokenType.BRACE_OPEN ||
                         t.type == TokenType.BRACE_CLOSE) break;
            }
            return depth == 0;
        }

        return false;
    }

    /**
     * Find the previous non-whitespace token before the given index.
     */
    private Token findPrevNonWhitespace(List<Token> tokens, int idx) {
        for (int i = idx - 1; i >= 0; i--) {
            if (tokens.get(i).type != TokenType.WHITESPACE) {
                return tokens.get(i);
            }
        }
        return null;
    }

    // ==================== PHASE 3: RECONSTRUCTOR ====================

    /**
     * Reconstruct formatted code from a list of tokens.
     */
    private String reconstruct(List<Token> tokens) {
        StringBuilder sb = new StringBuilder();
        for (Token t : tokens) {
            sb.append(t.value);
        }
        return sb.toString();
    }

    /**
     * Trim trailing whitespace from a line.
     */
    private String trimTrailing(String line) {
        int end = line.length();
        while (end > 0 && (line.charAt(end - 1) == ' ' || line.charAt(end - 1) == '\t')) {
            end--;
        }
        return line.substring(0, end);
    }

    // ==================== STATIC UTILITIES ====================

    /**
     * Format code with default settings.
     */
    public static String formatCode(String code) {
        return new FormatHelper().format(code);
    }

    /**
     * Format code with custom settings.
     */
    public static String formatCode(String code, FormatSettings settings) {
        return new FormatHelper(settings).format(code);
    }

    /**
     * Sanitize clipboard text for paste into the script editor.
     * <p>
     * This is a <b>paste-only</b> cleanup intended to prevent large trailing
     * whitespace runs from causing misleading soft-wrap splits in the editor.
     * <p>
     * Processing:
     * <ul>
     *   <li>Normalizes line separators ({@code \r\n} and bare {@code \r}) to {@code \n}.</li>
     *   <li>For each line, strips trailing spaces/tabs <b>only</b> when the trailing
     *       whitespace run length is &ge; {@code trimThreshold}.</li>
     *   <li>Preserves all leading whitespace, interior spacing, and newline structure
     *       (trailing empty lines are kept).</li>
     * </ul>
     * The threshold avoids clobbering intentional minor trailing spaces while still
     * removing the large padding blocks that confuse soft-wrap layout.
     *
     * @param clipboard     the raw clipboard text (may be {@code null} or empty)
     * @param trimThreshold minimum trailing whitespace run length to trigger trimming;
     *                      runs shorter than this are left intact
     * @return sanitized text, or the original reference if {@code null}/empty
     */
    public static String sanitizeClipboard(String clipboard, int trimThreshold) {
        if (clipboard == null || clipboard.isEmpty()) {
            return clipboard;
        }

        // Normalize line separators: \r\n -> \n, then bare \r -> \n
        String normalized = clipboard.replace("\r\n", "\n").replace("\r", "\n");

        String[] lines = normalized.split("\n", -1);
        StringBuilder result = new StringBuilder(normalized.length());

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            // Measure trailing whitespace run length
            int end = line.length();
            while (end > 0 && (line.charAt(end - 1) == ' ' || line.charAt(end - 1) == '\t')) {
                end--;
            }
            int trailingLen = line.length() - end;

            if (trailingLen >= trimThreshold) {
                result.append(line, 0, end);
            } else {
                result.append(line);
            }

            if (i < lines.length - 1) {
                result.append('\n');
            }
        }

        return result.toString();
    }

    // ==================== LINE WRAPPING ====================

    /**
     * Wrap long lines according to settings.
     * Call this after format() if wrapLongLines is enabled.
     *
     * @param text     The text to wrap
     * @param maxWidth Maximum line width (use settings.maxLineLength if 0)
     * @return Text with wrapped lines
     */
    public String wrapLines(String text, int maxWidth) {
        if (text == null || text.isEmpty()) return text;

        int width = maxWidth > 0 ? maxWidth : settings.maxLineLength;

        String joined = format(joinContinuationLines(text));

        String[] lines = joined.split("\n", -1);
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            if (getVisualLength(line) <= width) {
                result.append(line);
            } else {
                // Determine if this is a comment line
                String trimmed = line.trim();
                if (trimmed.startsWith("//")) {
                    result.append(wrapCommentLine(line, width));
                } else if (trimmed.startsWith("/*") || trimmed.startsWith("*")) {
                    result.append(wrapBlockCommentLine(line, width));
                } else {
                    result.append(wrapCodeLine(line, width));
                }
            }

            if (i < lines.length - 1) {
                result.append("\n");
            }
        }

        return result.toString();
    }

    /**
     * Join continuation lines back onto their parent line so they can be
     * re-evaluated and re-wrapped at the current target width.
     * <p>
     * A "continuation line" is one whose trimmed content starts with an operator,
     * dot, comma, {@code ?}, or {@code :} — i.e. the wrapping markers used by
     * {@link #wrapCodeContent}. Only code continuation lines are joined; comment
     * lines ({@code //}, {@code *}) and lines inside lambda/block bodies (deeper
     * indent than expected) are left alone.
     */
    private String joinContinuationLines(String text) {
        String[] lines = text.split("\n", -1);
        List<String> out = new ArrayList<>();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String trimmed = line.trim();

            if (trimmed.isEmpty()
                    || trimmed.startsWith("//")
                    || trimmed.startsWith("/*")
                    || trimmed.startsWith("*")) {
                out.add(line);
                continue;
            }

            boolean shouldJoin = false;
            int parentIdx = -1;

            if ((isContinuationLine(trimmed) || startsWithDeclarationKeyword(trimmed))
                    && !out.isEmpty()) {
                int idx = out.size() - 1;
                while (idx >= 0 && out.get(idx).trim().isEmpty()) idx--;
                if (idx >= 0) {
                    String parentTrimmed = out.get(idx).trim();
                    char last = parentTrimmed.isEmpty() ? 0 : parentTrimmed.charAt(parentTrimmed.length() - 1);
                    if (last != '{' && last != '}' && last != ';') {
                        shouldJoin = true;
                        parentIdx = idx;
                    }
                }
            }

            if (!shouldJoin && !out.isEmpty()) {
                int idx = out.size() - 1;
                while (idx >= 0 && out.get(idx).trim().isEmpty()) idx--;
                if (idx >= 0) {
                    String parentTrimmed = out.get(idx).trim();
                    if (!parentTrimmed.isEmpty()) {
                        char last = parentTrimmed.charAt(parentTrimmed.length() - 1);
                        if (last == '&' || last == '|' || last == '<' || last == '+'
                                || last == '-' || last == '?' || last == ',' || last == '>') {
                            shouldJoin = true;
                            parentIdx = idx;
                        }
                    }
                }
            }

            if (shouldJoin) {
                out.set(parentIdx, trimTrailing(out.get(parentIdx)) + " " + trimmed);
                continue;
            }

            out.add(line);
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < out.size(); i++) {
            sb.append(out.get(i));
            if (i < out.size() - 1) sb.append("\n");
        }
        return sb.toString();
    }

    private boolean startsWithDeclarationKeyword(String trimmed) {
        for (String kw : DECLARATION_BREAK_KEYWORDS) {
            if (trimmed.startsWith(kw)
                && (trimmed.length() == kw.length()
                    || !Character.isJavaIdentifierPart(trimmed.charAt(kw.length())))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get visual length of a line (tabs count as 4 spaces).
     */
    private int getVisualLength(String line) {
        int len = 0;
        for (char c : line.toCharArray()) {
            if (c == '\t') {
                len += 4;
            } else {
                len++;
            }
        }
        return len;
    }

    /**
     * Extract leading indentation from a line.
     */
    private String extractIndent(String line) {
        int i = 0;
        while (i < line.length() && (line.charAt(i) == ' ' || line.charAt(i) == '\t')) {
            i++;
        }
        return line.substring(0, i);
    }

    /**
     * Wrap a single-line comment (// style).
     */
    private String wrapCommentLine(String line, int maxWidth) {
        if (!settings.wrapComments) return line;

        String indent = extractIndent(line);
        String content = line.substring(indent.length());

        // Must start with //
        if (!content.startsWith("//")) return line;

        String commentContent = content.substring(2).trim();
        String prefix = indent + "// ";
        int prefixLen = getVisualLength(prefix);
        int contentWidth = maxWidth - prefixLen;

        if (contentWidth <= 10) return line; // Too narrow to wrap

        StringBuilder result = new StringBuilder();
        result.append(indent).append("// ");

        String[] words = commentContent.split("\\s+");
        int lineLen = prefixLen;

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            int wordLen = word.length();

            if (lineLen + wordLen > maxWidth && lineLen > prefixLen) {
                // Start new line
                result.append("\n").append(prefix);
                lineLen = prefixLen;
            }

            if (lineLen > prefixLen) {
                result.append(" ");
                lineLen++;
            }

            result.append(word);
            lineLen += wordLen;
        }

        return result.toString();
    }

    /**
     * Wrap a block comment line (* style).
     */
    private String wrapBlockCommentLine(String line, int maxWidth) {
        if (!settings.wrapComments) return line;

        String indent = extractIndent(line);
        String content = line.substring(indent.length()).trim();

        // Determine prefix for wrapped lines
        String prefix;
        String textContent;

        if (content.startsWith("/**")) {
            prefix = indent + " * ";
            textContent = content.substring(3).trim();
        } else if (content.startsWith("/*")) {
            prefix = indent + " * ";
            textContent = content.substring(2).trim();
        } else if (content.startsWith("*")) {
            prefix = indent + " * ";
            textContent = content.substring(1).trim();
        } else {
            return line;
        }

        // Handle closing */
        boolean hasClose = textContent.endsWith("*/");
        if (hasClose) {
            textContent = textContent.substring(0, textContent.length() - 2).trim();
        }

        int prefixLen = getVisualLength(prefix);
        int contentWidth = maxWidth - prefixLen;

        if (contentWidth <= 10) return line;

        StringBuilder result = new StringBuilder();

        // First line keeps original prefix
        if (content.startsWith("/**")) {
            result.append(indent).append("/** ");
        } else if (content.startsWith("/*")) {
            result.append(indent).append("/* ");
        } else {
            result.append(indent).append(" * ");
        }

        String[] words = textContent.split("\\s+");
        int lineLen = prefixLen;

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            int wordLen = word.length();

            if (lineLen + wordLen > maxWidth && lineLen > prefixLen) {
                result.append("\n").append(prefix);
                lineLen = prefixLen;
            }

            if (lineLen > prefixLen) {
                result.append(" ");
                lineLen++;
            }

            result.append(word);
            lineLen += wordLen;
        }

        if (hasClose) {
            result.append(" */");
        }

        return result.toString();
    }

    /**
     * Find the start position of an inline line comment ({@code //}) in a code line,
     * correctly skipping {@code //} inside string literals and block comments.
     *
     * @return index of the {@code //} in content, or -1 if no inline comment exists
     */
    private int findInlineCommentStart(String content) {
        boolean inString = false;
        char stringChar = 0;

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            char prev = i > 0 ? content.charAt(i - 1) : 0;

            if ((c == '"' || c == '\'') && prev != '\\') {
                if (!inString) {
                    inString = true;
                    stringChar = c;
                } else if (c == stringChar) {
                    inString = false;
                }
            }

            if (inString) continue;

            // Skip block comments
            if (c == '/' && i + 1 < content.length() && content.charAt(i + 1) == '*') {
                int endBlock = content.indexOf("*/", i + 2);
                if (endBlock >= 0) {
                    i = endBlock + 1;
                    continue;
                }
            }

            if (c == '/' && i + 1 < content.length() && content.charAt(i + 1) == '/') {
                return i;
            }
        }

        return -1;
    }

    /**
     * Word-wrap a comment string across multiple {@code //} continuation lines.
     * Each continuation line uses the given prefix (indent + "// ").
     *
     * @param commentText the text after {@code //} (already trimmed)
     * @param prefix      indent + "// " for each continuation line
     * @param maxWidth    maximum line width
     * @param firstLineCapacity chars available on the first output line (-1 to start fresh)
     * @return the wrapped comment lines joined with newlines
     */
    private String wordWrapComment(String commentText, String prefix, int maxWidth,
                                    int firstLineCapacity) {
        if (commentText.isEmpty()) return "//";

        String[] words = commentText.split("\\s+");
        int prefixLen = getVisualLength(prefix);
        int contentWidth = maxWidth - prefixLen;
        if (contentWidth <= 10) {
            return "// " + commentText;
        }

        StringBuilder result = new StringBuilder();
        boolean onFirstLine = (firstLineCapacity > 0);
        int capacity = onFirstLine ? firstLineCapacity : contentWidth;
        int lineLen = 0;

        if (!onFirstLine) {
            result.append(prefix);
        } else {
            result.append("// ");
            lineLen = 3;
        }

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            int wordLen = word.length();

            if (lineLen + (lineLen > (onFirstLine ? 3 : 0) ? 1 : 0) + wordLen > capacity && lineLen > 0) {
                result.append("\n").append(prefix);
                lineLen = 0;
                capacity = contentWidth;
                onFirstLine = false;
            }

            if (lineLen > 0) {
                result.append(" ");
                lineLen++;
            }

            result.append(word);
            lineLen += wordLen;
        }

        return result.toString();
    }

    /**
     * Wrap a code line at appropriate break points, with intelligent inline comment handling.
     * <p>
     * If the line has a trailing {@code // comment}, the comment is extracted first.
     * The code portion is wrapped normally, then the comment is reattached:
     * <ul>
     *   <li>If code + comment fit within maxWidth → keep inline</li>
     *   <li>If only the code fits → comment moves to its own indented line</li>
     *   <li>If the comment is very long → it gets word-wrapped across multiple lines</li>
     * </ul>
     */
    private String wrapCodeLine(String line, int maxWidth) {
        String indent = extractIndent(line);
        String content = line.substring(indent.length());

        // --- Phase 1: Detect and extract inline comment ---
        int commentStart = findInlineCommentStart(content);
        String codePart = content;
        String commentPart = null;

        if (commentStart >= 0) {
            codePart = content.substring(0, commentStart);
            commentPart = content.substring(commentStart);
            // Trim trailing space from code part
            while (!codePart.isEmpty() && codePart.charAt(codePart.length() - 1) == ' ') {
                codePart = codePart.substring(0, codePart.length() - 1);
            }
        }

        // --- Phase 2: Wrap the code part (without the comment) ---
        String wrappedCode = wrapCodeContent(codePart, indent, maxWidth);

        // --- Phase 3: Reattach the comment intelligently ---
        if (commentPart == null) {
            return wrappedCode;
        }

        // Find the last line of the wrapped code to measure available space
        int lastNewline = wrappedCode.lastIndexOf('\n');
        String lastLine = (lastNewline >= 0) ? wrappedCode.substring(lastNewline + 1) : wrappedCode;
        int lastLineLen = getVisualLength(lastLine);

        // Measure the comment: " // comment text"
        int commentLen = 1 + getVisualLength(commentPart); // +1 for the space separator

        // Strategy A: Comment fits inline on the last line
        if (lastLineLen + commentLen <= maxWidth) {
            return wrappedCode + " " + commentPart;
        }

        // Strategy B: Comment on its own line(s), indented to match original code level
        String commentIndent = indent;
        String commentText = commentPart.startsWith("//") ?
            commentPart.substring(2).trim() : commentPart;
        String commentPrefix = commentIndent + "// ";
        int availableWidth = maxWidth - getVisualLength(commentPrefix);

        if (availableWidth > 10 && getVisualLength(commentText) > availableWidth) {
            // Word-wrap the long comment
            String wrappedComment = wordWrapComment(commentText, commentPrefix, maxWidth, -1);
            return wrappedCode + "\n" + wrappedComment;
        } else {
            return wrappedCode + "\n" + commentPrefix + commentText;
        }
    }

    /**
     * Core code wrapping logic (extracted from old wrapCodeLine, operates on code without comments).
     */
    private String wrapCodeContent(String content, String indent, int maxWidth) {
        if (settings.wrapMethodArguments) {
            String argWrapped = tryArgumentAwareWrap(content, indent, maxWidth);
            if (argWrapped != null) {
                return argWrapped;
            }
        }

        String wrapIndent = indent + spaces(settings.wrapIndentSpaces);
        String fullLine = indent + content;

        if (getVisualLength(fullLine) <= maxWidth) {
            return fullLine;
        }

        List<BreakPoint> breakPoints = findBreakPoints(content);

        if (breakPoints.isEmpty()) {
            return fullLine;
        }

        StringBuilder result = new StringBuilder();
        result.append(indent);

        int currentLen = getVisualLength(indent);
        int lastBreak = 0;

        for (BreakPoint bp : breakPoints) {
            int segmentEnd = bp.position;
            String segment = content.substring(lastBreak, segmentEnd);
            int segmentLen = getVisualLength(segment);

            if (currentLen + segmentLen > maxWidth && lastBreak > 0) {
                result.append("\n").append(wrapIndent);
                currentLen = getVisualLength(wrapIndent);
                segment = trimLeading(segment);
            }

            result.append(segment);
            currentLen += getVisualLength(segment);
            lastBreak = segmentEnd;
        }

        if (lastBreak < content.length()) {
            String remaining = content.substring(lastBreak);
            int remLen = getVisualLength(remaining);

            if (currentLen + remLen > maxWidth && lastBreak > 0) {
                result.append("\n").append(wrapIndent);
                remaining = trimLeading(remaining);
            }

            result.append(remaining);
        }

        return result.toString();
    }

    /**
     * Try argument-aware wrapping for method/constructor calls.
     * When a call like {@code new Foo(arg1, arg2, arg3)} exceeds maxWidth,
     * break after the opening paren, put each argument on its own line,
     * and close paren on its own line:
     * <pre>
     * new Foo(
     *     arg1,
     *     arg2,
     *     arg3
     * )
     * </pre>
     *
     * @return the wrapped string, or {@code null} if this content doesn't
     *         qualify for argument-aware wrapping
     */
    private String tryArgumentAwareWrap(String content, String indent, int maxWidth) {
        String fullLine = indent + content;
        if (getVisualLength(fullLine) <= maxWidth) {
            return null;
        }

        int callParenIdx = findOutermostCallParen(content);
        if (callParenIdx < 0) {
            return null;
        }

        int matchingClose = findMatchingClose(content, callParenIdx);
        if (matchingClose < 0 || matchingClose >= content.length()) {
            return null;
        }

        String prefix = content.substring(0, callParenIdx + 1);
        String argsBody = content.substring(callParenIdx + 1, matchingClose);
        String suffix = content.substring(matchingClose);

        List<String> args = splitArguments(argsBody);
        if (args.size() < 2) {
            return null;
        }

        String argIndent = indent + spaces(settings.wrapIndentSpaces);
        String closeIndent = indent;

        StringBuilder result = new StringBuilder();
        result.append(indent).append(prefix).append("\n");

        for (int i = 0; i < args.size(); i++) {
            String arg = args.get(i).trim();
            if (arg.isEmpty()) continue;

            result.append(argIndent).append(arg);
            if (i < args.size() - 1) {
                result.append(",");
            }
            result.append("\n");
        }

        result.append(closeIndent).append(suffix);

        String wrapped = result.toString();
        if (getVisualLength(indent + prefix) <= maxWidth) {
            return wrapped;
        }

        return wrapped;
    }

    /**
     * Find the opening paren of the outermost method/constructor call
     * that should trigger argument-aware wrapping.
     * Looks for patterns: {@code identifier(}, {@code new Type(}, {@code .method(}
     *
     * @return index of the '(' or -1 if no suitable call found
     */
    private int findOutermostCallParen(String content) {
        int bestIdx = -1;
        boolean inString = false;
        char stringChar = 0;

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            char prev = i > 0 ? content.charAt(i - 1) : ' ';

            if ((c == '"' || c == '\'') && prev != '\\') {
                if (!inString) {
                    inString = true;
                    stringChar = c;
                } else if (c == stringChar) {
                    inString = false;
                }
            }
            if (inString) continue;

            if (c == '(') {
                if (i > 0 && (Character.isJavaIdentifierPart(content.charAt(i - 1)) ||
                              content.charAt(i - 1) == '>')) {
                    bestIdx = i;
                    break;
                }
            }
        }

        return bestIdx;
    }

    private int findMatchingClose(String content, int openIdx) {
        int depth = 1;
        boolean inString = false;
        char stringChar = 0;

        for (int i = openIdx + 1; i < content.length(); i++) {
            char c = content.charAt(i);
            char prev = i > 0 ? content.charAt(i - 1) : ' ';

            if ((c == '"' || c == '\'') && prev != '\\') {
                if (!inString) {
                    inString = true;
                    stringChar = c;
                } else if (c == stringChar) {
                    inString = false;
                }
            }
            if (inString) continue;

            if (c == '(') depth++;
            else if (c == ')') {
                depth--;
                if (depth == 0) return i;
            }
        }

        return -1;
    }

    /**
     * Split argument body by top-level commas, preserving nested parens,
     * brackets, braces, strings, and lambda bodies.
     */
    private List<String> splitArguments(String argsBody) {
        List<String> args = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int depth = 0;
        boolean inString = false;
        char stringChar = 0;

        for (int i = 0; i < argsBody.length(); i++) {
            char c = argsBody.charAt(i);
            char prev = i > 0 ? argsBody.charAt(i - 1) : ' ';

            if ((c == '"' || c == '\'') && prev != '\\') {
                if (!inString) {
                    inString = true;
                    stringChar = c;
                } else if (c == stringChar) {
                    inString = false;
                }
            }

            if (inString) {
                current.append(c);
                continue;
            }

            if (c == '(' || c == '[' || c == '{') depth++;
            else if (c == ')' || c == ']' || c == '}') depth--;

            if (c == ',' && depth == 0) {
                args.add(current.toString());
                current = new StringBuilder();
                continue;
            }

            current.append(c);
        }

        if (current.length() > 0) {
            args.add(current.toString());
        }

        return args;
    }

    private String trimLeading(String s) {
        int start = 0;
        while (start < s.length() && Character.isWhitespace(s.charAt(start))) {
            start++;
        }
        return s.substring(start);
    }

    /**
     * A potential line break point.
     */
    private static class BreakPoint {
        int position;      // Position in string (after this char)
        @SuppressWarnings("unused")
        int priority;      // Lower = better place to break (for future sorting)

        BreakPoint(int pos, int prio) {
            this.position = pos;
            this.priority = prio;
        }
    }

    private static final String[] DECLARATION_BREAK_KEYWORDS = {
        "extends", "implements", "throws", "permits"
    };

    /**
     * Find good positions to break a line.
     */
    private List<BreakPoint> findBreakPoints(String content) {
        List<BreakPoint> points = new ArrayList<>();

        int parenDepth = 0;
        int bracketDepth = 0;
        int braceDepth = 0;
        boolean inString = false;
        char stringChar = 0;

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            char prev = i > 0 ? content.charAt(i - 1) : ' ';

            // Track string state
            if ((c == '"' || c == '\'') && prev != '\\') {
                if (!inString) {
                    inString = true;
                    stringChar = c;
                } else if (c == stringChar) {
                    inString = false;
                }
            }

            if (inString) continue;

            // Track bracket depth
            if (c == '(') parenDepth++;
            else if (c == ')') parenDepth--;
            else if (c == '[') bracketDepth++;
            else if (c == ']') bracketDepth--;
            else if (c == '{') braceDepth++;
            else if (c == '}') braceDepth--;

            // Calculate total nesting depth (prefer breaking at lower depth)
            int depth = parenDepth + bracketDepth + braceDepth;

            // Find break points
            if (settings.wrapAfterComma && c == ',') {
                // After comma is a good break point (better at lower depth)
                points.add(new BreakPoint(i + 1, 1 + depth));
            }

            // Arrow operators (-> and =>) are atomic — never break inside them
            if ((c == '-' || c == '=') && i + 1 < content.length() && content.charAt(i + 1) == '>') {
                i++;
                continue;
            }

            if (settings.wrapAfterOperator) {
                if ((c == '+' || c == '-') && i > 0 && i < content.length() - 1) {
                    char next = content.charAt(i + 1);
                    if (prev != '(' && prev != '[' && prev != ',' && prev != '=' &&
                        next != '+' && next != '-' && next != '=' && next != '>') {
                        points.add(new BreakPoint(i + 1, 3 + depth));
                    }
                }
                if (c == '&' && i + 1 < content.length() && content.charAt(i + 1) == '&') {
                    points.add(new BreakPoint(i + 2, 2 + depth));
                    i++; // skip second &
                    continue;
                }
                if (c == '|' && i + 1 < content.length() && content.charAt(i + 1) == '|') {
                    points.add(new BreakPoint(i + 2, 2 + depth));
                    i++; // skip second |
                    continue;
                }
                // Single & as type intersection operator (e.g. T extends A & B)
                if (c == '&' && i > 0 && i < content.length() - 1
                    && content.charAt(i + 1) != '&' && content.charAt(i + 1) != '=') {
                    points.add(new BreakPoint(i + 1, 3 + depth));
                }
                if (c == '?' && depth == 0 && i > 0 && i < content.length() - 1) {
                    points.add(new BreakPoint(i, 2));
                }
                if (c == ':' && depth == 0 && i > 0 && i < content.length() - 1) {
                    if (prev != ':' && (i + 1 >= content.length() || content.charAt(i + 1) != ':')) {
                        points.add(new BreakPoint(i, 2));
                    }
                }
                if (c == '=' && depth == 0 && i > 0 && i < content.length() - 1) {
                    char next = content.charAt(i + 1);
                    if (prev != '=' && prev != '!' && prev != '<' && prev != '>' &&
                        prev != '+' && prev != '-' && prev != '*' && prev != '/' &&
                        prev != '%' && prev != '&' && prev != '|' && prev != '^' &&
                        next != '=' && next != '>') {
                        points.add(new BreakPoint(i + 1, 4 + depth));
                    }
                }
            }

            if (settings.wrapBeforeDot && c == '.') {
                // Before dot (for method chaining)
                points.add(new BreakPoint(i, 2 + depth));
            }

            // After opening brace (even when last char — allows break before {)
            if (c == '{') {
                if (i + 1 < content.length()) {
                    points.add(new BreakPoint(i + 1, 1));
                }
                // Break before { so the brace can move to its own line
                if (i > 0) {
                    points.add(new BreakPoint(i, 2));
                }
            }

            // Break before declaration keywords (extends, implements, throws, permits)
            if (c == ' ' && i + 1 < content.length() && Character.isLetter(content.charAt(i + 1))) {
                for (String kw : DECLARATION_BREAK_KEYWORDS) {
                    if (i + 1 + kw.length() <= content.length()
                        && content.substring(i + 1, i + 1 + kw.length()).equals(kw)
                        && (i + 1 + kw.length() >= content.length()
                            || !Character.isJavaIdentifierPart(content.charAt(i + 1 + kw.length())))) {
                        points.add(new BreakPoint(i + 1, 2));
                        break;
                    }
                }
            }
        }

        // Sort by position
        points.sort((a, b) -> Integer.compare(a.position, b.position));

        return points;
    }

    /**
     * Create a string of n spaces.
     */
    private String spaces(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }

    /**
     * Format and wrap code in one step.
     */
    public String formatAndWrap(String text, int maxWidth) {
        String formatted = format(text);
        if (settings.wrapLongLines) {
            return wrapLines(formatted, maxWidth);
        }
        return formatted;
    }
}
