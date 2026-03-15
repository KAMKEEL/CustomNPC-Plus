package noppes.npcs.client.gui.util.script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MethodBlock {

    // pattern matches returnType + methodName + parentheses
    public static final Pattern METHOD_PATTERN = Pattern.compile(
        "\\b([a-zA-Z_][a-zA-Z0-9_<>\\[\\]]*)\\s+" + // return type
            "([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(" // method name
    );

    // Pattern for variable declarations (same as in JavaTextContainer)
    public static final Pattern LOCAL_VAR_DECL = Pattern.compile(
        "\\b([A-Z][a-zA-Z0-9_<>\\[\\]]*|[a-z][a-zA-Z0-9_]*)\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*(=|;)");

    // Pattern for method parameters: Type name (with optional ... for varargs)
    public static final Pattern PARAMETER_PATTERN = Pattern.compile(
        "([a-zA-Z_][a-zA-Z0-9_<>\\[\\]]*(?:\\.{3})?)\\s+([a-zA-Z_][a-zA-Z0-9_]*)");

    public int startOffset;
    public int endOffset;
    public String text;
    public List<String> localVariables = new ArrayList<>();
    public List<String> parameters = new ArrayList<>();  // Method parameters
    public Map<String, Integer> localVarPositions = new HashMap<>();  // Track declaration positions

    public MethodBlock(int start, int end, String text) {
        this.startOffset = start;
        this.endOffset = end;
        this.text = text;
        extractParameters();
        extractLocalVariables();
    }

    // Extract method parameters from the method signature
    private void extractParameters() {
        parameters.clear();

        // Find the parameter list between ( and )
        int parenStart = text.indexOf('(');
        int parenEnd = text.indexOf(')');

        if (parenStart < 0 || parenEnd <= parenStart) return;

        String paramList = text.substring(parenStart + 1, parenEnd);

        // Skip if empty
        if (paramList.trim().isEmpty()) return;

        // Split by comma (but be careful of generic types like List<A, B>)
        Matcher m = PARAMETER_PATTERN.matcher(paramList);
        while (m.find()) {
            String paramName = m.group(2);
            if (!parameters.contains(paramName)) {
                parameters.add(paramName);
            }
        }
    }

    // Extract local variables declared within this method
    private void extractLocalVariables() {
        localVariables.clear();
        localVarPositions.clear();

        // Get excluded ranges (strings and comments) for this method's text
        List<int[]> excludedRanges = getExcludedRanges(text);

        Matcher m = LOCAL_VAR_DECL.matcher(text);
        while (m.find()) {
            String varName = m.group(2);
            int declPosition = m.start();

            // Skip if inside a string or comment
            if (isInExcludedRange(declPosition, excludedRanges)) {
                continue;
            }

            // Check if this is a "this.field" assignment - if so, skip it
            // Look backwards from the declaration position to see if preceded by "this."
            boolean isThisFieldAssignment = false;
            if (declPosition >= 5) {
                int checkStart = Math.max(0, declPosition - 10);
                String before = text.substring(checkStart, declPosition);
                if (before.matches(".*\\bthis\\s*\\.\\s*$")) {
                    isThisFieldAssignment = true;
                }
            }

            if (!isThisFieldAssignment && !localVariables.contains(varName)) {
                localVariables.add(varName);
                // Store the absolute position in the full text (startOffset + relative position)
                localVarPositions.put(varName, startOffset + declPosition);
            }
        }
    }

    // Check if a reference position comes before the local variable declaration
    // This handles cases like: global.field on line 7, then var global = "x" on line 9
    // At line 7, the local 'global' doesn't exist yet, so it should refer to the global 'global'
    public boolean isLocalDeclaredAtPosition(String varName, int absolutePosition) {
        if (!localVariables.contains(varName)) {
            return false;
        }
        Integer declPos = localVarPositions.get(varName);
        if (declPos == null) {
            return false;
        }
        // The local variable is only "in scope" at or after its declaration position
        return absolutePosition >= declPos;
    }

    // Check if a position (in the full text) falls within this method
    public boolean containsPosition(int position) {
        return position >= startOffset && position < endOffset;
    }

    public String toString() {
        return text;
    }

    public static List<MethodBlock> collectMethodBlocks(String text) {
        List<MethodBlock> methods = new ArrayList<>();

        // Matches: returnType methodName(...) { (non-greedy capture until first { )
        Pattern methodPattern = Pattern.compile(
            "\\b([a-zA-Z_][a-zA-Z0-9_<>\\[\\]]*)\\s+" + // return type
                "([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\([^)]*\\)\\s*" + // method name + parameters
                "\\{", // opening brace
            Pattern.MULTILINE);

        Matcher m = methodPattern.matcher(text);
        while (m.find()) {
            int start = m.start(); // start of method declaration
            int braceStart = text.indexOf("{", m.end() - 1); // first { after declaration
            if (braceStart == -1)
                continue;

            int braceEnd = findMatchingBrace(text, braceStart);
            if (braceEnd == -1)
                continue;

            String methodText = text.substring(start, braceEnd + 1);
            methods.add(new MethodBlock(start, braceEnd + 1, methodText));
        }

        return methods;
    }

    // Find ranges of text to skip (strings and comments). Returns merged, sorted ranges.
    public static List<int[]> getExcludedRanges(String text) {
        List<int[]> ranges = new ArrayList<>();

        // Find all strings and char literals (simple heuristic)
        Pattern stringPattern = Pattern.compile("([\"'])(?:(?=(\\\\?))\\2.)*?\\1");
        Matcher stringMatcher = stringPattern.matcher(text);
        while (stringMatcher.find()) {
            ranges.add(new int[]{stringMatcher.start(), stringMatcher.end()});
        }

        // Find all comments (block and line comments)
        Pattern commentPattern = Pattern.compile("/\\*[\\s\\S]*?(?:\\*/|$)|//.*");
        Matcher commentMatcher = commentPattern.matcher(text);
        while (commentMatcher.find()) {
            ranges.add(new int[]{commentMatcher.start(), commentMatcher.end()});
        }

        if (ranges.isEmpty())
            return ranges;

        // Sort ranges by start position
        ranges.sort((a, b) -> Integer.compare(a[0], b[0]));

        // Merge overlapping/adjacent ranges for faster scanning
        List<int[]> merged = new ArrayList<>();
        int[] current = ranges.get(0);
        for (int i = 1; i < ranges.size(); i++) {
            int[] next = ranges.get(i);
            if (next[0] <= current[1]) {
                // overlap or adjacent
                current[1] = Math.max(current[1], next[1]);
            } else {
                merged.add(current);
                current = next;
            }
        }
        merged.add(current);
        return merged;
    }

    // Check if a position is within an excluded range
    private static boolean isInExcludedRange(int pos, List<int[]> ranges) {
        for (int[] range : ranges) {
            if (pos >= range[0] && pos < range[1]) {
                return true;
            }
        }
        return false;
    }

    // Utility to find the matching closing brace, ignoring braces in strings and
    // comments
    public static int findMatchingBrace(String text, int openBraceIndex) {
        List<int[]> excludedRanges = getExcludedRanges(text);

        int depth = 0;
        for (int i = openBraceIndex; i < text.length(); i++) {
            // Skip characters inside strings or comments
            if (isInExcludedRange(i, excludedRanges)) {
                continue;
            }

            char c = text.charAt(i);
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1; // no match
    }
}
