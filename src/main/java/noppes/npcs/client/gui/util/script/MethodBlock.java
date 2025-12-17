package noppes.npcs.client.gui.util.script;

import java.util.ArrayList;
import java.util.List;
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

    public int startOffset;
    public int endOffset;
    public String text;
    public List<String> localVariables = new ArrayList<>();

    public MethodBlock(int start, int end, String text) {
        this.startOffset = start;
        this.endOffset = end;
        this.text = text;
        extractLocalVariables();
    }

    // Extract local variables declared within this method
    private void extractLocalVariables() {
        localVariables.clear();
        Matcher m = LOCAL_VAR_DECL.matcher(text);
        while (m.find()) {
            String varName = m.group(2);
            if (!localVariables.contains(varName)) {
                localVariables.add(varName);
            }
        }
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

    // Find ranges of text to skip (strings and comments)
    private static List<int[]> findExcludedRanges(String text) {
        List<int[]> ranges = new ArrayList<>();

        // Find all strings using the pattern from JavaTextContainer
        Pattern stringPattern = Pattern.compile("([\"'])(?:(?=(\\\\?))\\2.)*?\\1");
        Matcher stringMatcher = stringPattern.matcher(text);
        while (stringMatcher.find()) {
            ranges.add(new int[] { stringMatcher.start(), stringMatcher.end() });
        }

        // Find all comments
        Pattern commentPattern = Pattern.compile("/\\*[\\s\\S]*?(?:\\*/|$)|//.*|#.*");
        Matcher commentMatcher = commentPattern.matcher(text);
        while (commentMatcher.find()) {
            ranges.add(new int[] { commentMatcher.start(), commentMatcher.end() });
        }

        // Sort ranges by start position
        ranges.sort((a, b) -> Integer.compare(a[0], b[0]));
        return ranges;
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
        List<int[]> excludedRanges = findExcludedRanges(text);

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
