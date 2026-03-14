package bigguy.texteditor.treesitter.java;

import org.treesitter.TSInputEdit;
import org.treesitter.TSPoint;
import org.treesitter.TSRange;
import org.treesitter.TSTree;

/**
 * Immutable wrapper around a tree-sitter {@link TSTree} representing
 * the parsed syntax structure of a Java source file.
 *
 * <p>Holds both the native tree and the source text it was parsed from.
 * Provides Java-friendly traversal via {@link SyntaxNode} and supports
 * incremental editing through {@link #edit(EditOperation)}.</p>
 *
 * <p>Implements {@link AutoCloseable} to ensure native tree memory is
 * released. Must not be used after being closed.</p>
 */
public final class SyntaxTree implements AutoCloseable {

    private final TSTree tree;
    private final String sourceText;
    private boolean closed = false;

    SyntaxTree(TSTree tree, String sourceText) {
        if (tree == null) throw new IllegalArgumentException("tree must not be null");
        this.tree = tree;
        this.sourceText = sourceText;
    }

    private void ensureOpen() {
        if (closed) throw new IllegalStateException("SyntaxTree has been closed");
    }

    public SyntaxNode getRootNode() {
        ensureOpen();
        return new SyntaxNode(tree.getRootNode(), sourceText);
    }

    public String getSourceText() {
        return sourceText;
    }

    public boolean hasErrors() {
        ensureOpen();
        return tree.getRootNode().hasError();
    }

    /**
     * Creates a shallow copy of the underlying tree for use on another thread.
     *
     * <p>Tree-sitter trees are not thread-safe; use this to get a thread-local
     * copy. The copy shares structure with the original, so it is very cheap.</p>
     *
     * @return a new {@code SyntaxTree} backed by a copy of the native tree
     */
    public SyntaxTree copy() {
        ensureOpen();
        return new SyntaxTree(tree.copy(), sourceText);
    }

    /**
     * Applies an edit to the underlying tree, preparing it for incremental re-parse.
     *
     * <p>After calling this method, pass the returned tree (via its internal handle)
     * as the {@code oldTree} parameter to the next parse. Tree-sitter will reuse
     * unchanged subtrees.</p>
     *
     * @param op the edit operation describing what changed
     */
    public void edit(EditOperation op) {
        ensureOpen();
        TSInputEdit tsEdit = new TSInputEdit(
                op.startByte, op.oldEndByte, op.newEndByte,
                new TSPoint(op.startRow, op.startColumn),
                new TSPoint(op.oldEndRow, op.oldEndColumn),
                new TSPoint(op.newEndRow, op.newEndColumn)
        );
        tree.edit(tsEdit);
    }

    /**
     * Computes the ranges that changed between this tree and a newer version.
     *
     * @param newTree the tree produced after re-parsing with this tree as oldTree
     * @return array of changed ranges
     */
    public TextSpan[] getChangedRanges(SyntaxTree newTree) {
        ensureOpen();
        newTree.ensureOpen();
        TSRange[] ranges = TSTree.getChangedRanges(this.tree, newTree.tree);
        TextSpan[] result = new TextSpan[ranges.length];
        for (int i = 0; i < ranges.length; i++) {
            TSRange r = ranges[i];
            result[i] = new TextSpan(
                    r.getStartPoint().getRow(), r.getStartPoint().getColumn(),
                    r.getEndPoint().getRow(), r.getEndPoint().getColumn(),
                    r.getStartByte(), r.getEndByte()
            );
        }
        return result;
    }

    TSTree unwrap() {
        ensureOpen();
        return tree;
    }

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            tree.close();
        }
    }

    /**
     * Describes a text edit operation for incremental parsing.
     */
    public static final class EditOperation {
        public final int startByte;
        public final int oldEndByte;
        public final int newEndByte;
        public final int startRow;
        public final int startColumn;
        public final int oldEndRow;
        public final int oldEndColumn;
        public final int newEndRow;
        public final int newEndColumn;

        public EditOperation(int startByte, int oldEndByte, int newEndByte,
                             int startRow, int startColumn,
                             int oldEndRow, int oldEndColumn,
                             int newEndRow, int newEndColumn) {
            this.startByte = startByte;
            this.oldEndByte = oldEndByte;
            this.newEndByte = newEndByte;
            this.startRow = startRow;
            this.startColumn = startColumn;
            this.oldEndRow = oldEndRow;
            this.oldEndColumn = oldEndColumn;
            this.newEndRow = newEndRow;
            this.newEndColumn = newEndColumn;
        }
    }
}
