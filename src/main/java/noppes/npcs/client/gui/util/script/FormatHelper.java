package noppes.npcs.client.gui.util.script;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Configurable code formatter with IntelliJ-like settings.
 * <p>
 * Features:
 * - Operator spacing (a+b → a + b)
 * - Whitespace normalization (a   b → a b)
 * - Long line wrapping
 * - Bracket spacing configuration
 * - Preserves string content and comments
 */
public class FormatHelper {

    // ==================== SETTINGS ====================

    /**
     * Formatting configuration - similar to IntelliJ Code Style settings
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
     * Format the entire text with all configured options
     */
    public String format(String text) {
        if (text == null || text.isEmpty()) return text;

        // Process line by line to preserve structure
        String[] lines = text.split("\n", -1);
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            String line = formatLine(lines[i], text, getLineStartOffset(lines, i));

            if (settings.trimTrailingWhitespace) {
                line = trimTrailing(line);
            }

            result.append(line);
            if (i < lines.length - 1) {
                result.append("\n");
            }
        }

        return result.toString();
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
     * Split content by semicolons, preserving those in strings and for-loops
     */
    private List<String> splitBySemicolons(String content) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int parenDepth = 0;
        boolean inString = false;
        boolean inChar = false;
        boolean escaped = false;
        int forLoopDepth = 0; // Track for(...) depth to preserve semicolons

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            char prev = i > 0 ? content.charAt(i - 1) : ' ';

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

    private int getLineStartOffset(String[] lines, int lineIndex) {
        int offset = 0;
        for (int i = 0; i < lineIndex; i++) {
            offset += lines[i].length() + 1; // +1 for \n
        }
        return offset;
    }

    /**
     * Format a single line of code
     */
    private String formatLine(String line, String fullText, int lineOffset) {
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
        String formatted = formatContent(content, fullText, lineOffset + indent);

        return indentation + formatted;
    }

    /**
     * Check if a line appears to be a continuation of a previous line
     * (i.e., starts with an operator, dot, or other continuation character)
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
     * Format line content, preserving strings and comments
     */
    private String formatContent(String content, String fullText, int offset) {
        // Find string/comment regions to preserve
        List<int[]> preserveRegions = findPreserveRegions(content);

        if (preserveRegions.isEmpty()) {
            // No strings/comments, format everything
            return formatCodeSegment(content);
        }

        // Build result by formatting non-preserved regions
        StringBuilder result = new StringBuilder();
        int lastEnd = 0;

        for (int[] region : preserveRegions) {
            // Format code before this preserved region
            if (region[0] > lastEnd) {
                String codeSegment = content.substring(lastEnd, region[0]);
                result.append(formatCodeSegment(codeSegment));
            }

            // Append preserved region unchanged
            result.append(content.substring(region[0], region[1]));
            lastEnd = region[1];
        }

        // Format remaining code after last preserved region
        if (lastEnd < content.length()) {
            result.append(formatCodeSegment(content.substring(lastEnd)));
        }

        return result.toString();
    }

    /**
     * Find string literals and comments that should not be formatted
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
     * Find end of string/char literal
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

    // ==================== CODE SEGMENT FORMATTING ====================

    /**
     * Format a code segment (no strings/comments)
     */
    private String formatCodeSegment(String code) {
        if (code.isEmpty()) return code;

        String result = code;

        // Normalize whitespace first
        if (settings.normalizeWhitespace) {
            result = normalizeWhitespace(result);
        }

        // Apply operator spacing
        result = formatOperators(result);

        // Apply bracket spacing
        result = formatBrackets(result);

        // Apply keyword spacing
        result = formatKeywords(result);

        // Apply punctuation spacing
        result = formatPunctuation(result);

        return result;
    }

    /**
     * Normalize whitespace (multiple spaces → single space)
     */
    private String normalizeWhitespace(String code) {
        // Replace multiple spaces with single space, but preserve indentation
        return code.replaceAll("  +", " ");
    }

    /**
     * Format operator spacing
     */
    private String formatOperators(String code) {
        String result = code;

        // Assignment operators
        if (settings.spaceAroundAssignment) {
            // Compound assignments first (longer patterns first)
            result = formatBinaryOp(result, "+=", " += ");
            result = formatBinaryOp(result, "-=", " -= ");
            result = formatBinaryOp(result, "*=", " *= ");
            result = formatBinaryOp(result, "/=", " /= ");
            result = formatBinaryOp(result, "%=", " %= ");
            result = formatBinaryOp(result, "&=", " &= ");
            result = formatBinaryOp(result, "|=", " |= ");
            result = formatBinaryOp(result, "^=", " ^= ");
            result = formatBinaryOp(result, "<<=", " <<= ");
            result = formatBinaryOp(result, ">>=", " >>= ");
            result = formatBinaryOp(result, ">>>=", " >>>= ");

            // Simple assignment (but not ==, !=, <=, >=)
            result = formatSimpleAssignment(result);
        }

        // Comparison operators
        if (settings.spaceAroundComparison) {
            result = formatBinaryOp(result, "==", " == ");
            result = formatBinaryOp(result, "!=", " != ");
            result = formatBinaryOp(result, "<=", " <= ");
            result = formatBinaryOp(result, ">=", " >= ");
            // Simple < > (but not generics)
            result = formatComparisonOperators(result);
        }

        // Logical operators
        if (settings.spaceAroundLogical) {
            result = formatBinaryOp(result, "&&", " && ");
            result = formatBinaryOp(result, "||", " || ");
        }

        // Arithmetic operators
        if (settings.spaceAroundArithmetic) {
            // Handle ++ and -- first to avoid conflicts
            result = formatArithmeticOperators(result);
        }

        // Bitwise operators (but not in generics)
        if (settings.spaceAroundBitwise) {
            result = formatBitwiseOperators(result);
        }

        return result;
    }

    /**
     * Format a binary operator with proper spacing
     */
    private String formatBinaryOp(String code, String op, String replacement) {
        // First normalize: remove extra spaces around the operator
        String pattern = "\\s*" + Pattern.quote(op) + "\\s*";
        return code.replaceAll(pattern, replacement);
    }

    /**
     * Format simple assignment = (not ==, !=, <=, >=, etc.)
     */
    private String formatSimpleAssignment(String code) {
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < code.length()) {
            char c = code.charAt(i);
            if (c == '=') {
                // Check it's not part of ==, !=, <=, >=, +=, etc.
                char prev = i > 0 ? code.charAt(i - 1) : ' ';
                char next = i + 1 < code.length() ? code.charAt(i + 1) : ' ';

                boolean isCompound = prev == '!' || prev == '<' || prev == '>' ||
                    prev == '+' || prev == '-' || prev == '*' ||
                    prev == '/' || prev == '%' || prev == '&' ||
                    prev == '|' || prev == '^';
                boolean isEquality = next == '=';

                if (!isCompound && !isEquality) {
                    // Ensure space before =
                    if (result.length() > 0 && result.charAt(result.length() - 1) != ' ') {
                        result.append(' ');
                    }
                    result.append('=');
                    // Ensure space after =
                    if (next != ' ') {
                        result.append(' ');
                    }
                } else {
                    result.append(c);
                }
            } else {
                result.append(c);
            }
            i++;
        }
        return result.toString();
    }

    /**
     * Format < and > comparison operators (avoiding generics)
     */
    private String formatComparisonOperators(String code) {
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < code.length()) {
            char c = code.charAt(i);

            if ((c == '<' || c == '>') && !isLikelyGeneric(code, i, c)) {
                char prev = i > 0 ? code.charAt(i - 1) : ' ';
                char next = i + 1 < code.length() ? code.charAt(i + 1) : ' ';

                // Skip if part of <<, >>, <=, >=
                if ((c == '<' && next == '<') || (c == '>' && next == '>') ||
                    next == '=' || prev == '<' || prev == '>') {
                    result.append(c);
                } else {
                    // Add spacing
                    if (result.length() > 0 && result.charAt(result.length() - 1) != ' ') {
                        result.append(' ');
                    }
                    result.append(c);
                    if (next != ' ' && next != '=') {
                        result.append(' ');
                    }
                }
            } else {
                result.append(c);
            }
            i++;
        }
        return result.toString();
    }

    /**
     * Check if < or > is likely part of a generic type declaration
     */
    private boolean isLikelyGeneric(String code, int pos, char bracket) {
        if (bracket == '<') {
            // Check for pattern like List<, Map<, etc.
            if (pos > 0) {
                int start = pos - 1;
                while (start > 0 && Character.isLetterOrDigit(code.charAt(start - 1))) {
                    start--;
                }
                String before = code.substring(start, pos);
                // Common generic types or capital letter start (type name)
                if (!before.isEmpty() && Character.isUpperCase(before.charAt(0))) {
                    return true;
                }
            }
            // Check if there's a matching >
            int depth = 1;
            for (int i = pos + 1; i < code.length() && depth > 0; i++) {
                char c = code.charAt(i);
                if (c == '<') depth++;
                else if (c == '>') depth--;
                else if (c == ';' || c == '{' || c == '}') break; // Not a generic
            }
            return depth == 0;
        } else { // '>'
            // Check if there's a matching < before
            int depth = 1;
            for (int i = pos - 1; i >= 0 && depth > 0; i--) {
                char c = code.charAt(i);
                if (c == '>') depth++;
                else if (c == '<') depth--;
                else if (c == ';' || c == '{' || c == '}') break;
            }
            return depth == 0;
        }
    }

    /**
     * Format arithmetic operators (+, -, *, /, %)
     */
    private String formatArithmeticOperators(String code) {
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < code.length()) {
            char c = code.charAt(i);
            char next = i + 1 < code.length() ? code.charAt(i + 1) : ' ';
            char prev = i > 0 ? code.charAt(i - 1) : ' ';

            boolean isArithOp = c == '+' || c == '-' || c == '*' || c == '/' || c == '%';

            if (isArithOp) {
                // Skip ++, --, +=, -=, *=, /=, %=, and unary +/-
                if ((c == '+' && next == '+') || (c == '-' && next == '-') ||
                    (c == '+' && prev == '+') || (c == '-' && prev == '-') ||
                    next == '=') {
                    result.append(c);
                } else if (isUnaryContext(code, i)) {
                    // Unary + or - (after operator, open paren, comma, etc.)
                    result.append(c);
                } else {
                    // Binary operator - add spacing
                    if (result.length() > 0 && result.charAt(result.length() - 1) != ' ') {
                        result.append(' ');
                    }
                    result.append(c);
                    if (next != ' ' && next != '=' && !((c == '+' && next == '+') || (c == '-' && next == '-'))) {
                        result.append(' ');
                    }
                }
            } else {
                result.append(c);
            }
            i++;
        }
        return result.toString();
    }

    /**
     * Check if position is in a unary context (operator is unary, not binary)
     */
    private boolean isUnaryContext(String code, int pos) {
        if (pos == 0) return true;

        // Look back for context
        int i = pos - 1;
        while (i >= 0 && code.charAt(i) == ' ') {
            i--;
        }

        if (i < 0) return true;

        char prev = code.charAt(i);
        // Unary context after: ( [ { , ; = + - * / % < > & | ^ ? :
        return prev == '(' || prev == '[' || prev == '{' || prev == ',' ||
            prev == ';' || prev == '=' || prev == '+' || prev == '-' ||
            prev == '*' || prev == '/' || prev == '%' || prev == '<' ||
            prev == '>' || prev == '&' || prev == '|' || prev == '^' ||
            prev == '?' || prev == ':' || prev == '!';
    }

    /**
     * Format bitwise operators
     */
    private String formatBitwiseOperators(String code) {
        String result = code;

        // Shift operators first
        result = formatBinaryOp(result, ">>>", " >>> ");
        result = formatBinaryOp(result, "<<", " << ");
        result = formatBinaryOp(result, ">>", " >> ");

        // Single & | ^ (not && ||)
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < result.length()) {
            char c = result.charAt(i);
            char next = i + 1 < result.length() ? result.charAt(i + 1) : ' ';
            char prev = i > 0 ? result.charAt(i - 1) : ' ';

            if ((c == '&' && next != '&' && prev != '&' && next != '=') ||
                (c == '|' && next != '|' && prev != '|' && next != '=') ||
                (c == '^' && next != '=')) {
                if (sb.length() > 0 && sb.charAt(sb.length() - 1) != ' ') {
                    sb.append(' ');
                }
                sb.append(c);
                if (next != ' ') {
                    sb.append(' ');
                }
            } else {
                sb.append(c);
            }
            i++;
        }

        return sb.toString();
    }

    // ==================== BRACKET FORMATTING ====================

    /**
     * Format bracket spacing
     */
    private String formatBrackets(String code) {
        String result = code;

        // Parentheses
        if (settings.spaceWithinParens) {
            result = result.replaceAll("\\(\\s*", "( ");
            result = result.replaceAll("\\s*\\)", " )");
        } else {
            result = result.replaceAll("\\(\\s+", "(");
            result = result.replaceAll("\\s+\\)", ")");
        }

        // Square brackets
        if (settings.spaceWithinBrackets) {
            result = result.replaceAll("\\[\\s*", "[ ");
            result = result.replaceAll("\\s*\\]", " ]");
        } else {
            result = result.replaceAll("\\[\\s+", "[");
            result = result.replaceAll("\\s+\\]", "]");
        }

        // Curly braces (for array initializers inline)
        // Note: standalone braces for blocks are handled by indentation logic

        return result;
    }

    // ==================== KEYWORD FORMATTING ====================

    /**
     * Format keyword spacing (if, while, for, etc.)
     */
    private String formatKeywords(String code) {
        String result = code;

        if (settings.spaceBeforeIfParens) {
            result = result.replaceAll("\\bif\\s*\\(", "if (");
        } else {
            result = result.replaceAll("\\bif\\s+\\(", "if(");
        }

        if (settings.spaceBeforeWhileParens) {
            result = result.replaceAll("\\bwhile\\s*\\(", "while (");
        } else {
            result = result.replaceAll("\\bwhile\\s+\\(", "while(");
        }

        if (settings.spaceBeforeForParens) {
            result = result.replaceAll("\\bfor\\s*\\(", "for (");
        } else {
            result = result.replaceAll("\\bfor\\s+\\(", "for(");
        }

        if (settings.spaceBeforeSwitchParens) {
            result = result.replaceAll("\\bswitch\\s*\\(", "switch (");
        } else {
            result = result.replaceAll("\\bswitch\\s+\\(", "switch(");
        }

        if (settings.spaceBeforeCatchParens) {
            result = result.replaceAll("\\bcatch\\s*\\(", "catch (");
        } else {
            result = result.replaceAll("\\bcatch\\s+\\(", "catch(");
        }

        // Space before opening brace
        if (settings.spaceBeforeOpenBrace) {
            result = result.replaceAll("\\)\\s*\\{", ") {");
        } else {
            result = result.replaceAll("\\)\\s+\\{", "){");
        }

        return result;
    }

    // ==================== PUNCTUATION FORMATTING ====================

    /**
     * Format punctuation spacing (commas, semicolons, colons)
     */
    private String formatPunctuation(String code) {
        String result = code;

        // Commas
        if (settings.spaceAfterComma && !settings.spaceBeforeComma) {
            result = result.replaceAll("\\s*,\\s*", ", ");
        } else if (settings.spaceAfterComma && settings.spaceBeforeComma) {
            result = result.replaceAll("\\s*,\\s*", " , ");
        } else if (!settings.spaceAfterComma && settings.spaceBeforeComma) {
            result = result.replaceAll("\\s*,\\s*", " ,");
        } else {
            result = result.replaceAll("\\s*,\\s*", ",");
        }

        // Semicolons (but not in for loops - handle more carefully)
        // This is tricky because for(;;) should be handled differently
        // For now, just handle trailing semicolons
        if (settings.trimTrailingWhitespace) {
            result = result.replaceAll("\\s+;", ";");
        }

        return result;
    }

    /**
     * Trim trailing whitespace from a line
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
     * Format code with default settings
     */
    public static String formatCode(String code) {
        return new FormatHelper().format(code);
    }

    /**
     * Format code with custom settings
     */
    public static String formatCode(String code, FormatSettings settings) {
        return new FormatHelper(settings).format(code);
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
        String[] lines = text.split("\n", -1);
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
     * Get visual length of a line (tabs count as 4 spaces)
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
     * Extract leading indentation from a line
     */
    private String extractIndent(String line) {
        int i = 0;
        while (i < line.length() && (line.charAt(i) == ' ' || line.charAt(i) == '\t')) {
            i++;
        }
        return line.substring(0, i);
    }

    /**
     * Wrap a single-line comment (// style)
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
     * Wrap a block comment line (* style)
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
     * Wrap a code line at appropriate break points
     */
    private String wrapCodeLine(String line, int maxWidth) {
        String indent = extractIndent(line);
        String content = line.substring(indent.length());

        // Additional indent for wrapped lines
        String wrapIndent = indent + spaces(settings.wrapIndentSpaces);

        // Find break points - positions where we can break the line
        List<BreakPoint> breakPoints = findBreakPoints(content);

        if (breakPoints.isEmpty()) {
            // No good break points, just return as-is
            return line;
        }

        StringBuilder result = new StringBuilder();
        result.append(indent);

        int currentLen = getVisualLength(indent);
        int lastBreak = 0;
        boolean isFirstLine = true;

        for (BreakPoint bp : breakPoints) {
            int segmentEnd = bp.position;
            String segment = content.substring(lastBreak, segmentEnd);
            int segmentLen = getVisualLength(segment);

            // Check if adding this segment would exceed max width
            if (currentLen + segmentLen > maxWidth && !isFirstLine) {
                // Break here
                result.append("\n").append(wrapIndent);
                currentLen = getVisualLength(wrapIndent);
                isFirstLine = false;

                // Trim leading space from continuation if any
                segment = trimLeading(segment);
            } else if (currentLen + segmentLen > maxWidth && isFirstLine && lastBreak > 0) {
                // Even first segment is too long after indent, break before it
                result.append("\n").append(wrapIndent);
                currentLen = getVisualLength(wrapIndent);
                isFirstLine = false;
            }

            result.append(segment);
            currentLen += getVisualLength(segment);
            lastBreak = segmentEnd;
            isFirstLine = false;
        }

        // Append remaining content
        if (lastBreak < content.length()) {
            String remaining = content.substring(lastBreak);
            int remLen = getVisualLength(remaining);

            if (currentLen + remLen > maxWidth && !isFirstLine) {
                result.append("\n").append(wrapIndent);
                remaining = trimLeading(remaining);
            }

            result.append(remaining);
        }

        return result.toString();
    }

    /**
     * Trim leading whitespace (Java 8 compatible)
     */
    private String trimLeading(String s) {
        int start = 0;
        while (start < s.length() && Character.isWhitespace(s.charAt(start))) {
            start++;
        }
        return s.substring(start);
    }

    /**
     * A potential line break point
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

    /**
     * Find good positions to break a line
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

            if (settings.wrapAfterOperator) {
                // After binary operators
                if ((c == '+' || c == '-') && i > 0 && i < content.length() - 1) {
                    char next = content.charAt(i + 1);
                    if (prev != '(' && prev != '[' && prev != ',' && prev != '=' &&
                        next != '+' && next != '-' && next != '=') {
                        points.add(new BreakPoint(i + 1, 3 + depth));
                    }
                }
                if (c == '&' && i + 1 < content.length() && content.charAt(i + 1) == '&') {
                    points.add(new BreakPoint(i + 2, 2 + depth));
                }
                if (c == '|' && i + 1 < content.length() && content.charAt(i + 1) == '|') {
                    points.add(new BreakPoint(i + 2, 2 + depth));
                }
            }

            if (settings.wrapBeforeDot && c == '.') {
                // Before dot (for method chaining)
                points.add(new BreakPoint(i, 2 + depth));
            }

            // After opening brace
            if (c == '{' && i + 1 < content.length()) {
                points.add(new BreakPoint(i + 1, 1));
            }
        }

        // Sort by position
        points.sort((a, b) -> Integer.compare(a.position, b.position));

        return points;
    }

    /**
     * Create a string of n spaces
     */
    private String spaces(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }

    /**
     * Format and wrap code in one step
     */
    public String formatAndWrap(String text, int maxWidth) {
        String formatted = format(text);
        if (settings.wrapLongLines) {
            return wrapLines(formatted, maxWidth);
        }
        return formatted;
    }
}
