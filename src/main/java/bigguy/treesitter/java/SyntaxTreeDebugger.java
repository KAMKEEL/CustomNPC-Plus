package bigguy.treesitter.java;

/**
 * A production-ready utility for printing the complete Concrete Syntax Tree (CST)
 * produced by tree-sitter, with comprehensive metadata for debugging and visualization.
 *
 * <p>Traverses every node in the tree (both named and anonymous) and renders a
 * human-readable, indented representation that includes:</p>
 * <ul>
 *   <li>Node type with {@code [TEST]} marker if the type contains "test"</li>
 *   <li>Span in the format {@code [row:col..row:col](bytes start..end)}</li>
 *   <li>Named/anonymous indicator</li>
 *   <li>Error and missing flags</li>
 *   <li>Source text for leaf nodes (truncated to 50 characters)</li>
 *   <li>Child count for interior nodes</li>
 * </ul>
 *
 * <p>Output uses ANSI color codes for terminal readability:</p>
 * <ul>
 *   <li><b>Cyan</b> — named node types</li>
 *   <li><b>Dark gray</b> — anonymous node types (punctuation, keywords)</li>
 *   <li><b>Green</b> — leaf node source text</li>
 *   <li><b>Red</b> — error and missing markers</li>
 *   <li><b>Yellow</b> — test markers</li>
 *   <li><b>Magenta</b> — span information</li>
 * </ul>
 *
 * <h3>Usage Examples</h3>
 * <pre>{@code
 * // From a SyntaxTree
 * try (JavaParser parser = new JavaParser()) {
 *     SyntaxTree tree = parser.parse("public class Hello { int x = 42; }");
 *
 *     // Print to console directly
 *     SyntaxTreeDebugger.printTreeToConsole(tree);
 *
 *     // Or capture as a String for logging
 *     String dump = SyntaxTreeDebugger.printTree(tree);
 *     logger.debug("CST:\n{}", dump);
 * }
 *
 * // From a specific subtree
 * SyntaxNode methodNode = root.findFirst("method_declaration");
 * if (methodNode != null) {
 *     SyntaxTreeDebugger.printTreeToConsole(methodNode);
 * }
 * }</pre>
 *
 * <h3>Sample Output</h3>
 * <pre>
 * ── CST Dump (32 nodes) ──────────────────────────────
 * program [0:0..0:34](bytes 0..34) (named) [4 children]
 *   class_declaration [0:0..0:34](bytes 0..34) (named) [4 children]
 *     modifiers [0:0..0:6](bytes 0..6) (named) [1 children]
 *       "public" [0:0..0:6](bytes 0..6) (anonymous) "public"
 *     "class" [0:7..0:12](bytes 7..12) (anonymous) "class"
 *     name: identifier [0:13..0:18](bytes 13..18) (named) "Hello"
 *     body: class_body [0:19..0:34](bytes 19..34) (named) [3 children]
 *       "{" [0:19..0:20](bytes 19..20) (anonymous) "{"
 *       field_declaration [0:21..0:32](bytes 21..32) (named) ...
 *       "}" [0:33..0:34](bytes 33..34) (anonymous) "}"
 * ── End CST Dump ─────────────────────────────────────
 * </pre>
 *
 * <p>This class is stateless and all methods are static. It has no dependencies
 * beyond the {@code bigguy.treesitter.java} package itself.</p>
 *
 * @see SyntaxTree
 * @see SyntaxNode
 * @see TextSpan
 */
public final class SyntaxTreeDebugger {

    private static final int MAX_TEXT_LENGTH = 50;
    private static final String INDENT_UNIT = "  ";

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_GRAY = "\u001B[90m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_MAGENTA = "\u001B[35m";
    private static final String ANSI_BOLD = "\u001B[1m";
    private static final String ANSI_DIM = "\u001B[2m";

    private SyntaxTreeDebugger() {
        throw new AssertionError("Utility class — do not instantiate");
    }

    /**
     * Formats the entire CST of a {@link SyntaxTree} as a human-readable string.
     *
     * <p>Includes ANSI color codes for terminal display. The output begins and
     * ends with a decorative header/footer showing the total node count.</p>
     *
     * @param tree the syntax tree to format; must not be {@code null}
     * @return the formatted CST string
     * @throws IllegalArgumentException if {@code tree} is {@code null}
     */
    public static String printTree(SyntaxTree tree) {
        if (tree == null) {
            throw new IllegalArgumentException("tree must not be null");
        }
        return printTree(tree.getRootNode());
    }

    /**
     * Formats the subtree rooted at {@code root} as a human-readable string.
     *
     * <p>Includes ANSI color codes for terminal display. The output begins and
     * ends with a decorative header/footer showing the total node count.</p>
     *
     * @param root the root node of the subtree to format; must not be {@code null}
     * @return the formatted CST string
     * @throws IllegalArgumentException if {@code root} is {@code null} or represents a null node
     */
    public static String printTree(SyntaxNode root) {
        if (root == null || root.isNull()) {
            throw new IllegalArgumentException("root node must not be null");
        }
        int totalNodes = countNodes(root);
        StringBuilder sb = new StringBuilder(totalNodes * 80);

        sb.append(ANSI_DIM)
          .append("── CST Dump (")
          .append(totalNodes)
          .append(" nodes) ")
          .append(repeatChar('─', Math.max(1, 50 - String.valueOf(totalNodes).length())))
          .append(ANSI_RESET)
          .append('\n');

        appendNode(sb, root, 0);

        sb.append(ANSI_DIM)
          .append("── End CST Dump ")
          .append(repeatChar('─', 49))
          .append(ANSI_RESET)
          .append('\n');

        return sb.toString();
    }

    /**
     * Prints the entire CST of a {@link SyntaxTree} directly to {@link System#out}.
     *
     * @param tree the syntax tree to print; must not be {@code null}
     * @throws IllegalArgumentException if {@code tree} is {@code null}
     */
    public static void printTreeToConsole(SyntaxTree tree) {
        System.out.print(printTree(tree));
    }

    /**
     * Prints the subtree rooted at {@code root} directly to {@link System#out}.
     *
     * @param root the root node of the subtree to print; must not be {@code null}
     * @throws IllegalArgumentException if {@code root} is {@code null} or represents a null node
     */
    public static void printTreeToConsole(SyntaxNode root) {
        System.out.print(printTree(root));
    }

    private static void appendNode(StringBuilder sb, SyntaxNode node, int depth) {
        if (node == null || node.isNull()) {
            return;
        }

        appendIndent(sb, depth);
        appendNodeLine(sb, node);
        sb.append('\n');

        int childCount = node.getChildCount();
        for (int i = 0; i < childCount; i++) {
            SyntaxNode child = node.getChild(i);
            appendNode(sb, child, depth + 1);
        }
    }

    private static void appendNodeLine(StringBuilder sb, SyntaxNode node) {
        String type = node.getType();
        boolean isNamed = node.isNamed();
        boolean isLeaf = node.getChildCount() == 0;

        if (isNamed) {
            sb.append(ANSI_CYAN).append(ANSI_BOLD).append(type).append(ANSI_RESET);
        } else {
            sb.append(ANSI_GRAY).append('"').append(type).append('"').append(ANSI_RESET);
        }

        if (type != null && type.toLowerCase().contains("test")) {
            sb.append(' ').append(ANSI_YELLOW).append("[TEST]").append(ANSI_RESET);
        }

        TextSpan span = node.getSpan();
        if (span != null) {
            sb.append(' ')
              .append(ANSI_MAGENTA)
              .append(span)
              .append(ANSI_RESET);
        }

        sb.append(' ')
          .append(ANSI_DIM)
          .append('(')
          .append(isNamed ? "named" : "anonymous")
          .append(')')
          .append(ANSI_RESET);

        if (node.hasError()) {
            sb.append(' ').append(ANSI_RED).append(ANSI_BOLD).append("[ERROR]").append(ANSI_RESET);
        }
        if (node.isMissing()) {
            sb.append(' ').append(ANSI_RED).append(ANSI_BOLD).append("[MISSING]").append(ANSI_RESET);
        }

        if (isLeaf) {
            String text = node.getText();
            if (text != null) {
                String displayText = truncateText(text);
                sb.append(' ')
                  .append(ANSI_GREEN)
                  .append('"')
                  .append(escapeForDisplay(displayText))
                  .append('"')
                  .append(ANSI_RESET);
            }
        } else {
            int childCount = node.getChildCount();
            sb.append(' ')
              .append(ANSI_DIM)
              .append('[')
              .append(childCount)
              .append(childCount == 1 ? " child" : " children")
              .append(']')
              .append(ANSI_RESET);
        }
    }

    private static void appendIndent(StringBuilder sb, int depth) {
        for (int i = 0; i < depth; i++) {
            sb.append(INDENT_UNIT);
        }
    }

    private static String truncateText(String text) {
        if (text.length() <= MAX_TEXT_LENGTH) {
            return text;
        }
        return text.substring(0, MAX_TEXT_LENGTH) + "…";
    }

    private static String escapeForDisplay(String text) {
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                default:
                    if (Character.isISOControl(c)) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    private static int countNodes(SyntaxNode node) {
        if (node == null || node.isNull()) {
            return 0;
        }
        int count = 1;
        int childCount = node.getChildCount();
        for (int i = 0; i < childCount; i++) {
            count += countNodes(node.getChild(i));
        }
        return count;
    }

    private static String repeatChar(char c, int count) {
        char[] chars = new char[count];
        java.util.Arrays.fill(chars, c);
        return new String(chars);
    }
}
