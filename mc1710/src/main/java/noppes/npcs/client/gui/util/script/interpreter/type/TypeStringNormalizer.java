package noppes.npcs.client.gui.util.script.interpreter.type;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared utilities for cleaning and normalizing type expressions.
 *
 * This is intentionally a small, dependency-light helper that both the runtime resolver
 * and the .d.ts parser can use.
 */
public final class TypeStringNormalizer {

    private TypeStringNormalizer() {}

    public static final class ArraySplit {
        public final String base;
        public final int dimensions;

        public ArraySplit(String base, int dimensions) {
            this.base = base;
            this.dimensions = dimensions;
        }
    }

    /**
     * Strip TypeScript import() type references anywhere in the string.
     *
     * Examples:
     * - import('./data/IAction').IAction                 -> IAction
     * - Java.java.util.function.Consumer<import('./x').T> -> Java.java.util.function.Consumer<T>
     */
    public static String stripImportTypeSyntax(String type) {
        if (type == null) {
            return null;
        }

        String out = type.trim();
        int importIdx;
        while ((importIdx = out.indexOf("import(")) >= 0) {
            int i = importIdx + "import(".length();
            int depth = 1;
            boolean inString = false;
            char stringChar = 0;

            while (i < out.length() && depth > 0) {
                char c = out.charAt(i);
                if (inString) {
                    if (c == stringChar && (i == 0 || out.charAt(i - 1) != '\\')) {
                        inString = false;
                    }
                } else {
                    if (c == '\'' || c == '"') {
                        inString = true;
                        stringChar = c;
                    } else if (c == '(') {
                        depth++;
                    } else if (c == ')') {
                        depth--;
                    }
                }
                i++;
            }

            // i is positioned just after the matching ')', if found.
            if (depth != 0) {
                break; // Unbalanced import( ... ) - stop trying to clean.
            }

            // If immediately followed by ".", remove the "import(...)." prefix.
            if (i < out.length() && out.charAt(i) == '.') {
                int removeEnd = i + 1;
                out = out.substring(0, importIdx) + out.substring(removeEnd);
            } else {
                // Remove just the import(...) portion.
                out = out.substring(0, importIdx) + out.substring(i);
            }
        }

        return out;
    }

    /**
     * Split a trailing TypeScript nullable suffix: Foo? -> Foo.
     */
    public static String stripNullableSuffix(String type) {
        if (type == null) {
            return null;
        }
        String out = type.trim();
        if (out.endsWith("?")) {
            return out.substring(0, out.length() - 1).trim();
        }
        return out;
    }

    /**
     * Split trailing array suffixes: Foo[][] -> base=Foo, dimensions=2.
     */
    public static ArraySplit splitArraySuffixes(String type) {
        if (type == null) {
            return new ArraySplit(null, 0);
        }
        String out = type.trim();
        int dims = 0;
        while (out.endsWith("[]")) {
            dims++;
            out = out.substring(0, out.length() - 2).trim();
        }
        return new ArraySplit(out, dims);
    }

    /**
     * Split a top-level union (depth-aware) into branches.
     */
    public static List<String> splitTopLevelUnion(String type) {
        List<String> parts = new ArrayList<>();
        if (type == null) {
            return parts;
        }

        String expr = type.trim();
        if (expr.isEmpty()) {
            parts.add("");
            return parts;
        }

        int depth = 0;
        boolean inString = false;
        char stringChar = 0;

        int start = 0;
        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);

            if (inString) {
                if (c == stringChar && (i == 0 || expr.charAt(i - 1) != '\\')) {
                    inString = false;
                }
                continue;
            }

            if (c == '\'' || c == '"' || c == '`') {
                inString = true;
                stringChar = c;
                continue;
            }

            if (c == '<' || c == '(' || c == '[' || c == '{') {
                depth++;
                continue;
            }
            if (c == '>' || c == ')' || c == ']' || c == '}') {
                if (depth > 0) {
                    depth--;
                }
                continue;
            }

            if (c == '|' && depth == 0) {
                String part = expr.substring(start, i).trim();
                if (!part.isEmpty()) {
                    parts.add(part);
                }
                start = i + 1;
            }
        }

        String tail = expr.substring(start).trim();
        if (!tail.isEmpty()) {
            parts.add(tail);
        }

        if (parts.isEmpty()) {
            parts.add(expr);
        }
        return parts;
    }

    /**
     * Choose a single branch to represent a union type for resolution.
     *
     * Policy:
     * - Prefer the first non-nullish branch (not null/undefined/void).
     * - Otherwise, fall back to the first branch.
     */
    public static String pickPreferredUnionBranch(String type) {
        if (type == null) {
            return null;
        }

        List<String> parts = splitTopLevelUnion(type);
        if (parts.size() <= 1) {
            return type.trim();
        }

        for (String part : parts) {
            if (!isNullishBranch(part)) {
                return part.trim();
            }
        }

        return parts.get(0).trim();
    }

    private static boolean isNullishBranch(String part) {
        if (part == null) {
            return false;
        }

        String s = part.trim();
        if (s.isEmpty()) {
            return false;
        }

        // Strip generics so "null<...>" doesn't behave weirdly.
        s = GenericTypeParser.stripGenerics(s);
        s = s.trim().toLowerCase();
        return "null".equals(s) || "undefined".equals(s) || "void".equals(s);
    }
}
