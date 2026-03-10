package noppes.npcs.client.gui.util.script.interpreter;

/**
 * Utility class for parsing and analyzing code text.
 * Provides methods for finding matching braces/parentheses, removing comments/strings,
 * and locating keywords in code.
 */
public final class CodeParser {

    private CodeParser() {} // Utility class

    // ==================== Matching Delimiters ====================

    /**
     * Find the closing brace that matches the opening brace at the given position.
     * @param text The text to search
     * @param openBrace Position of the opening '{'
     * @return Position of matching '}' or -1 if not found
     */
    public static int findMatchingBrace(String text, int openBrace) {
        if (openBrace < 0 || openBrace >= text.length() || text.charAt(openBrace) != '{') {
            return -1;
        }
        int depth = 1;
        for (int i = openBrace + 1; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    /**
     * Find the closing parenthesis that matches the opening parenthesis at the given position.
     * @param text The text to search
     * @param openParen Position of the opening '('
     * @return Position of matching ')' or -1 if not found
     */
    public static int findMatchingParen(String text, int openParen) {
        if (openParen < 0 || openParen >= text.length() || text.charAt(openParen) != '(') {
            return -1;
        }
        int depth = 1;
        for (int i = openParen + 1; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '(') depth++;
            else if (c == ')') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    // ==================== Comment/String Removal ====================

    /**
     * Remove string literals and comments from code for structural analysis.
     * This is used when analyzing control flow to avoid false positives from
     * keywords inside strings or comments.
     * @param code The source code
     * @return Code with strings and comments removed
     */
    public static String removeStringsAndComments(String code) {
        StringBuilder result = new StringBuilder();
        boolean inString = false;
        boolean inChar = false;
        boolean inLineComment = false;
        boolean inBlockComment = false;
        
        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            char next = (i + 1 < code.length()) ? code.charAt(i + 1) : 0;
            
            if (inLineComment) {
                if (c == '\n') {
                    inLineComment = false;
                    result.append(c);
                }
                continue;
            }
            
            if (inBlockComment) {
                if (c == '*' && next == '/') {
                    inBlockComment = false;
                    i++; // skip '/'
                }
                continue;
            }
            
            if (inString || inChar) {
                if (c == '\\' && i + 1 < code.length()) {
                    i++; // skip escaped character
                    continue;
                }
                if ((inString && c == '"') || (inChar && c == '\'')) {
                    inString = false;
                    inChar = false;
                }
                continue;
            }
            
            // Check for start of comments or strings
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
            if (c == '"') {
                inString = true;
                continue;
            }
            if (c == '\'') {
                inChar = true;
                continue;
            }
            
            result.append(c);
        }
        
        return result.toString();
    }

    /**
     * Remove only comments from code, keeping strings intact and preserving positions.
     * Comments are replaced with spaces to maintain character positions.
     * @param code The source code
     * @return Code with comments replaced by spaces
     */
    public static String removeComments(String code) {
        StringBuilder result = new StringBuilder();
        boolean inString = false;
        boolean inChar = false;
        boolean inLineComment = false;
        boolean inBlockComment = false;
        
        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            char next = (i + 1 < code.length()) ? code.charAt(i + 1) : 0;
            
            if (inLineComment) {
                if (c == '\n') {
                    inLineComment = false;
                    result.append(c);
                } else {
                    result.append(' '); // Preserve position with spaces
                }
                continue;
            }
            
            if (inBlockComment) {
                if (c == '*' && next == '/') {
                    inBlockComment = false;
                    result.append("  "); // Preserve position
                    i++;
                } else {
                    result.append(c == '\n' ? '\n' : ' '); // Preserve newlines
                }
                continue;
            }
            
            if (inString) {
                result.append(c);
                if (c == '\\' && i + 1 < code.length()) {
                    result.append(code.charAt(++i));
                    continue;
                }
                if (c == '"') inString = false;
                continue;
            }
            
            if (inChar) {
                result.append(c);
                if (c == '\\' && i + 1 < code.length()) {
                    result.append(code.charAt(++i));
                    continue;
                }
                if (c == '\'') inChar = false;
                continue;
            }
            
            // Check for start of comments
            if (c == '/' && next == '/') {
                inLineComment = true;
                result.append("  "); // Preserve position
                i++;
                continue;
            }
            if (c == '/' && next == '*') {
                inBlockComment = true;
                result.append("  "); // Preserve position
                i++;
                continue;
            }
            if (c == '"') { inString = true; }
            if (c == '\'') { inChar = true; }
            
            result.append(c);
        }
        
        return result.toString();
    }

    // ==================== Keyword Detection ====================

    /**
     * Check if a keyword exists at the given position in text.
     * Verifies the keyword is not part of a larger identifier.
     * @param text The text to check
     * @param pos Position to check
     * @param keyword The keyword to look for
     * @return true if the keyword is at this position as a standalone word
     */
    public static boolean isKeywordAt(String text, int pos, String keyword) {
        if (pos + keyword.length() > text.length()) return false;
        if (!text.substring(pos).startsWith(keyword)) return false;
        
        boolean validBefore = pos == 0 || !Character.isLetterOrDigit(text.charAt(pos - 1));
        boolean validAfter = pos + keyword.length() >= text.length() 
            || !Character.isLetterOrDigit(text.charAt(pos + keyword.length()));
        
        return validBefore && validAfter;
    }

    /**
     * Find the next "return" keyword that is not inside a string literal.
     * @param text The text to search
     * @param start Position to start searching from
     * @return Position of "return" or -1 if not found
     */
    public static int findReturnKeyword(String text, int start) {
        boolean inString = false;
        boolean inChar = false;
        
        for (int i = start; i < text.length() - 5; i++) {
            char c = text.charAt(i);
            
            if (inString) {
                if (c == '\\' && i + 1 < text.length()) { i++; continue; }
                if (c == '"') inString = false;
                continue;
            }
            if (inChar) {
                if (c == '\\' && i + 1 < text.length()) { i++; continue; }
                if (c == '\'') inChar = false;
                continue;
            }
            
            if (c == '"') { inString = true; continue; }
            if (c == '\'') { inChar = true; continue; }
            
            // Check for "return" keyword
            if (isKeywordAt(text, i, "return")) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Find the semicolon that ends a return statement, handling nested structures.
     * @param text The text to search
     * @param start Position to start searching (after "return" keyword)
     * @return Position of ';' or -1 if not found
     */
    public static int findReturnSemicolon(String text, int start) {
        int parenDepth = 0;
        int braceDepth = 0;
        boolean inString = false;
        boolean inChar = false;
        
        for (int i = start; i < text.length(); i++) {
            char c = text.charAt(i);
            
            if (inString) {
                if (c == '\\' && i + 1 < text.length()) { i++; continue; }
                if (c == '"') inString = false;
                continue;
            }
            if (inChar) {
                if (c == '\\' && i + 1 < text.length()) { i++; continue; }
                if (c == '\'') inChar = false;
                continue;
            }
            
            if (c == '"') { inString = true; continue; }
            if (c == '\'') { inChar = true; continue; }
            if (c == '(') { parenDepth++; continue; }
            if (c == ')') { parenDepth--; continue; }
            if (c == '{') { braceDepth++; continue; }
            if (c == '}') { braceDepth--; continue; }
            
            if (c == ';' && parenDepth == 0 && braceDepth == 0) {
                return i;
            }
        }
        return -1;
    }

    // ==================== Whitespace Utilities ====================

    /**
     * Skip whitespace characters starting from the given position.
     * @param text The text
     * @param pos Starting position
     * @return Position of first non-whitespace character
     */
    public static int skipWhitespace(String text, int pos) {
        while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) {
            pos++;
        }
        return pos;
    }
}

