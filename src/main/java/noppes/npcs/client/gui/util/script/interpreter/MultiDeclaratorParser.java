package noppes.npcs.client.gui.util.script.interpreter;

// ==================== MULTI-DECLARATOR HELPERS ====================
//
// JavaScript and Java both allow declaring multiple variables in a single
// statement, separated by commas:
//
//   JS:   var a = 1, b = "x", c = player;
//   Java: int x = 1, y, z = 3;
//
// The existing regex patterns only match the FIRST declarator because:
//   - The JS pattern `(var|let|const)\s+(\w+)` requires a keyword prefix.
//   - The Java FIELD_DECL_PATTERN requires a type name prefix.
//
// Continuation declarators after the first comma have no keyword/type prefix,
// so they must be scanned manually. The two helpers below handle this by
// scanning forward from the end of the first declarator for commas at
// nesting depth 0, then extracting the `name [= initializer]` pattern
// for each continuation declarator.

/**
 * Static helpers for parsing comma-separated multi-variable declarations.
 *
 * <p>Extracted from {@link ScriptDocument} to keep the main document class
 * focused on orchestration while this class owns the scanning algorithms.</p>
 */
public final class MultiDeclaratorParser {

    private MultiDeclaratorParser() {}

    // ======================== CALLBACKS ========================

    /** Callback for {@link #scanJSContinuationDeclarators}. */
    @FunctionalInterface
    public interface JSContinuationCallback {
        void accept(String varName, int absNamePos, String initializer, int absInitStart, int absInitEnd);
    }

    /** Callback for {@link #scanJavaContinuationDeclarators}. */
    @FunctionalInterface
    public interface JavaContinuationCallback {
        void accept(String varName, int absNamePos, int absInitStart, int absInitEnd);
    }

    // ======================== JS SCANNING ========================

    /**
     * Scan forward from {@code scanStart} in {@code source} for comma-separated
     * continuation declarators in a JS {@code var/let/const} statement.
     *
     * <p>Each continuation declarator follows the pattern: {@code name [= initializer]}.
     * The initializer (if present) is terminated using comma-aware scanning via
     * {@link ScriptDocument#findJsInitializerEnd(String, int, boolean)}, which stops at
     * top-level commas so that each declarator's RHS is correctly bounded.</p>
     *
     * <p>The callback receives (varName, absNamePos, initializer, absInitStart, absInitEnd)
     * for each continuation declarator found.</p>
     *
     * @param doc       the owning ScriptDocument (provides {@code findJsInitializerEnd}
     *                  and {@code skipSegmentWhitespace})
     * @param source    the source text to scan within (e.g. method body or global text)
     * @param scanStart the position in {@code source} to begin scanning (typically
     *                  right after the first declarator's initializer end or name end)
     * @param absBase   the absolute offset of {@code source[0]} in the full document text
     *                  (used to compute absolute positions for the callback)
     * @param callback  receives each continuation declarator
     */
    public static void scanJSContinuationDeclarators(
            ScriptDocument doc, String source, int scanStart, int absBase,
            JSContinuationCallback callback) {

        int pos = scanStart;
        while (pos < source.length()) {
            // Skip whitespace
            while (pos < source.length() && Character.isWhitespace(source.charAt(pos))) pos++;
            if (pos >= source.length()) break;

            // Expect a comma separating the next declarator
            if (source.charAt(pos) != ',') break;
            pos++; // skip the comma

            // Skip whitespace after comma
            while (pos < source.length() && Character.isWhitespace(source.charAt(pos))) pos++;
            if (pos >= source.length()) break;

            // Read the variable name (must be an identifier)
            int nameStart = pos;
            while (pos < source.length() && Character.isJavaIdentifierPart(source.charAt(pos))) pos++;
            if (pos == nameStart) break; // no identifier found; malformed
            String varName = source.substring(nameStart, pos);

            // Skip whitespace after name
            while (pos < source.length() && Character.isWhitespace(source.charAt(pos))) pos++;

            String initializer = null;
            int initStart = -1;
            int initEnd = -1;

            // Check for optional '=' initializer
            if (pos < source.length() && source.charAt(pos) == '=') {
                // Make sure it's not == 
                if (pos + 1 < source.length() && source.charAt(pos + 1) == '=') break;
                pos++; // skip '='
                int rhsStart = doc.skipSegmentWhitespace(source, pos);
                // Use comma-aware initializer termination so that the RHS stops at
                // the next top-level comma (which starts the next declarator).
                int rhsEnd = doc.findJsInitializerEnd(source, rhsStart, true);
                if (rhsEnd > rhsStart) {
                    initializer = source.substring(rhsStart, rhsEnd).trim();
                    if (initializer.isEmpty()) initializer = null;
                }
                initStart = rhsStart;
                initEnd = rhsEnd;
                pos = rhsEnd;
            }

            int absNamePos = absBase + nameStart;
            int absInitStart = initStart >= 0 ? absBase + initStart : -1;
            int absInitEnd = initEnd >= 0 ? absBase + initEnd : -1;

            callback.accept(varName, absNamePos, initializer, absInitStart, absInitEnd);
        }
    }

    // ======================== JAVA SCANNING ========================

    /**
     * Scan forward from {@code scanStart} in {@code source} for comma-separated
     * continuation declarators in a Java multi-variable declaration.
     *
     * <p>Java allows {@code int x = 1, y, z = 3;} where all declarators share
     * the same declared type. The {@code FIELD_DECL_PATTERN} regex only matches
     * the first declarator ({@code int x =}); subsequent names like {@code y}
     * and {@code z} lack a type prefix. This method manually scans the remainder
     * of the statement for those continuation names.</p>
     *
     * <p>Each continuation declarator follows the pattern: {@code name [= initializer]}.
     * The initializer is terminated at {@code ;} or {@code ,} at depth 0.</p>
     *
     * @param source    the source text to scan within
     * @param scanStart the position in {@code source} to begin scanning (typically
     *                  right after the semicolon/delimiter of the first declarator
     *                  match, or right after the first declarator's initializer)
     * @param absBase   the absolute offset of {@code source[0]} in the full document text
     * @param callback  receives each continuation declarator
     */
    public static void scanJavaContinuationDeclarators(
            String source, int scanStart, int absBase,
            JavaContinuationCallback callback) {

        int pos = scanStart;
        while (pos < source.length()) {
            // Skip whitespace
            while (pos < source.length() && Character.isWhitespace(source.charAt(pos))) pos++;
            if (pos >= source.length()) break;

            // Expect a comma separating the next declarator
            if (source.charAt(pos) != ',') break;
            pos++; // skip the comma

            // Skip whitespace after comma
            while (pos < source.length() && Character.isWhitespace(source.charAt(pos))) pos++;
            if (pos >= source.length()) break;

            // Read the variable name (must be an identifier)
            int nameStart = pos;
            while (pos < source.length() && Character.isJavaIdentifierPart(source.charAt(pos))) pos++;
            if (pos == nameStart) break; // no identifier found; malformed
            String varName = source.substring(nameStart, pos);

            // Skip whitespace after name
            while (pos < source.length() && Character.isWhitespace(source.charAt(pos))) pos++;

            int initStart = -1;
            int initEnd = -1;

            // Check for optional '=' initializer
            if (pos < source.length() && source.charAt(pos) == '=') {
                // Make sure it's not ==
                if (pos + 1 < source.length() && source.charAt(pos + 1) == '=') break;

                initStart = pos; // position of '='
                pos++; // skip '='
                int rhsStart = pos;
                // Skip whitespace after '='
                while (rhsStart < source.length() && Character.isWhitespace(source.charAt(rhsStart))) rhsStart++;

                // Scan forward for ';' or ',' at depth 0 to terminate this declarator's initializer
                int depth = 0;
                int angleDepth = 0;
                int scanPos = rhsStart;
                while (scanPos < source.length()) {
                    char c = source.charAt(scanPos);
                    if (c == '(' || c == '[' || c == '{') depth++;
                    else if (c == ')' || c == ']' || c == '}') depth--;
                    else if (c == '<') angleDepth++;
                    else if (c == '>') angleDepth--;
                    else if ((c == ';' || c == ',') && depth == 0 && angleDepth == 0) {
                        initEnd = scanPos;
                        break;
                    }
                    scanPos++;
                }
                pos = (initEnd >= 0) ? initEnd : scanPos;
            } else if (pos < source.length() && (source.charAt(pos) == ';' || source.charAt(pos) == ',')) {
                // No initializer — name only (e.g., the `y` in `int x, y, z;`)
                // Don't advance past ';' here; let the outer loop handle it.
                // If it's a comma, we'll pick it up on the next iteration.
            } else {
                // Unexpected character; stop scanning
                break;
            }

            int absNamePos = absBase + nameStart;
            int absInitStartAbs = initStart >= 0 ? absBase + initStart : -1;
            int absInitEndAbs = initEnd >= 0 ? absBase + initEnd : -1;

            callback.accept(varName, absNamePos, absInitStartAbs, absInitEndAbs);
        }
    }

    // ======================== SCAN-START POSITION HELPERS ========================

    /**
     * Determine the scan position for continuation declarators after a JS
     * declarator match. If the first declarator has an initializer, we scan
     * from the initializer end; otherwise we scan from after the variable name.
     *
     * @param bodyText  the body text being parsed
     * @param matchEnd  the end position of the regex match (end of var name or '=')
     * @param hasEquals whether the first declarator has '='
     * @param initEnd   the initializer end position (in bodyText coords), or -1
     * @return the position in bodyText to start scanning for continuation commas
     */
    public static int jsDeclaratorScanStart(String bodyText, int matchEnd, boolean hasEquals, int initEnd) {
        if (hasEquals && initEnd >= 0) {
            return initEnd;
        }
        // No initializer: scan from after the variable name
        return matchEnd;
    }

    /**
     * Determine the scan position for continuation declarators after a Java
     * FIELD_DECL_PATTERN match. The pattern's group(3) matches either '=', ';',
     * or ','. If it matched '=', the scan starts after the initializer expression;
     * if ';', there are no continuations (semicolon ends the statement).
     *
     * <p><b>Note:</b> when the regex greedily consumed commas into the type name
     * (e.g. {@code "int x, y,"} as the type group), the caller must detect this
     * via {@link #findFirstDepthZeroComma(String)} on the matched type string
     * and use the returned comma position directly as the scan start, bypassing
     * this method. See the call sites in {@link ScriptDocument} for details.</p>
     *
     * @param source    the source text
     * @param delimiter the delimiter matched by group(3): "=", ";", or ","
     * @param delimEnd  the end position of the delimiter in source coords
     * @param initEnd   the initializer end position, or -1 if no initializer
     * @return the position in source to start scanning for continuation commas,
     *         or -1 if the statement ended at a semicolon
     */
    public static int javaDeclaratorScanStart(String source, String delimiter, int delimEnd, int initEnd) {
        if (";".equals(delimiter)) {
            return -1; // semicolon terminates the statement; no continuations possible
        }
        // delimiter is '=' or ',' — scan from after the initializer (which stops at ',' or ';')
        if (initEnd >= 0) {
            return initEnd;
        }
        // When delimiter is ',' with no initializer (e.g. the 'x' in "int x,y,z;"),
        // m.end(3) lands AFTER the comma. The scanner expects to START at the comma
        // so it can consume it as the continuation separator — so back up by one.
        if (",".equals(delimiter)) {
            return delimEnd - 1;
        }
        return delimEnd;
    }

    // ======================== GREEDY-REGEX COMMA HELPERS ========================

    /**
     * Find the first comma at bracket-depth 0 in the given string.
     *
     * <p>The Java {@code FIELD_DECL_PATTERN} regex allows commas inside the
     * type-name character class (to support generics like {@code Map<K, V>}).
     * This causes the regex to greedily consume multi-declarator commas into
     * group(1). For example, {@code "int x, y, z;"} matches with
     * group(1)={@code "int x, y,"} and group(2)={@code "z"}.</p>
     *
     * <p>This helper scans for the first comma that is NOT inside angle brackets
     * ({@code <>}), parentheses, square brackets, or braces, i.e. at depth 0.
     * A depth-0 comma indicates a multi-declarator separator that was consumed
     * by the greedy regex.</p>
     *
     * @param s the string to search (typically the matched type group)
     * @return the index of the first depth-0 comma, or -1 if none found
     */
    public static int findFirstDepthZeroComma(String s) {
        int depth = 0;
        int angleDepth = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '(' || c == '[' || c == '{') depth++;
            else if (c == ')' || c == ']' || c == '}') depth--;
            else if (c == '<') angleDepth++;
            else if (c == '>') angleDepth--;
            else if (c == ',' && depth == 0 && angleDepth == 0) return i;
        }
        return -1;
    }

    /**
     * Extract the real type name from a regex group(1) that greedily consumed
     * extra variable names and commas.
     *
     * <p>When the regex matches {@code "int x, y, z;"} as group(1)={@code "int x, y,"},
     * the real type name is {@code "int"} — everything before the first identifier
     * that precedes the first depth-0 comma.</p>
     *
     * <p>For {@code "final int x, y,"}, returns {@code "final int"}.
     * For {@code "Map<String, Integer> x, y,"}, returns {@code "Map<String, Integer>"}.</p>
     *
     * @param typeGroup  the full group(1) string from the regex match
     * @param commaIndex the index of the first depth-0 comma in {@code typeGroup}
     *                   (as returned by {@link #findFirstDepthZeroComma(String)})
     * @return the real type name (may include modifiers like "final")
     */
    public static String extractRealTypeFromGreedyMatch(String typeGroup, int commaIndex) {
        // The part before the first depth-0 comma contains "Type firstVar"
        // (possibly with modifiers). We need to split off the last identifier
        // token before the comma, as that's the first variable name, not the type.
        String beforeComma = typeGroup.substring(0, commaIndex).trim();
        int lastSpace = beforeComma.lastIndexOf(' ');
        int lastTab = beforeComma.lastIndexOf('\t');
        int lastWS = Math.max(lastSpace, lastTab);
        if (lastWS < 0) {
            // No whitespace found — the "type" is actually just a variable name
            // This shouldn't happen for valid declarations but handle gracefully
            return beforeComma;
        }
        return beforeComma.substring(0, lastWS).trim();
    }

    /**
     * Extract the first variable name from a regex group(1) that greedily consumed
     * extra variable names and commas.
     *
     * <p>When the regex matches {@code "int x, y, z;"} as group(1)={@code "int x, y,"},
     * the first variable name is {@code "x"} — the last identifier token before
     * the first depth-0 comma.</p>
     *
     * @param typeGroup  the full group(1) string from the regex match
     * @param commaIndex the index of the first depth-0 comma in {@code typeGroup}
     *                   (as returned by {@link #findFirstDepthZeroComma(String)})
     * @return the first variable name
     */
    public static String extractFirstVarFromGreedyMatch(String typeGroup, int commaIndex) {
        String beforeComma = typeGroup.substring(0, commaIndex).trim();
        int lastSpace = beforeComma.lastIndexOf(' ');
        int lastTab = beforeComma.lastIndexOf('\t');
        int lastWS = Math.max(lastSpace, lastTab);
        if (lastWS < 0) {
            return beforeComma;
        }
        return beforeComma.substring(lastWS + 1).trim();
    }

    /**
     * Find the absolute position of the first variable name within a greedily-consumed
     * type group. Searches for the first variable name (the last token before the
     * first depth-0 comma) within the source text starting from the type group start.
     *
     * @param source         the full source text
     * @param typeGroupStart the absolute start of group(1) in the source
     * @param typeGroup      the group(1) string
     * @param commaIndex     the index of the first depth-0 comma in typeGroup
     * @return the absolute position of the first variable name in source
     */
    public static int findFirstVarPosition(String source, int typeGroupStart, String typeGroup, int commaIndex) {
        String firstVar = extractFirstVarFromGreedyMatch(typeGroup, commaIndex);
        // The first variable appears in typeGroup just before the comma.
        // Search backwards from commaIndex to find it.
        String beforeComma = typeGroup.substring(0, commaIndex).trim();
        int varStartInGroup = beforeComma.lastIndexOf(firstVar);
        return typeGroupStart + varStartInGroup;
    }

    // ======================== MISC HELPERS ========================

    /**
     * Check whether the statement starting at {@code stmtStart} begins with
     * a JS declaration keyword ({@code var}, {@code let}, or {@code const}).
     * Used by {@link ScriptDocument}'s {@code parseAssignments()} to decide
     * whether the RHS of an {@code =} should stop at a top-level comma
     * (multi-declarator context).
     */
    public static boolean startsWithJSKeyword(String source, int stmtStart, int equalsPos) {
        int i = stmtStart;
        while (i < equalsPos && Character.isWhitespace(source.charAt(i))) i++;
        String prefix = source.substring(i, Math.min(i + 6, equalsPos)).trim();
        return prefix.startsWith("var ") || prefix.startsWith("var\t")
            || prefix.startsWith("let ") || prefix.startsWith("let\t")
            || prefix.startsWith("const ") || prefix.startsWith("const\t");
    }
}
