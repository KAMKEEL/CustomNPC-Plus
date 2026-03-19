package bigguy.texteditor.syntax;

import bigguy.texteditor.treesitter.java.SyntaxNode;
import bigguy.texteditor.treesitter.java.SyntaxTree;
import bigguy.texteditor.treesitter.java.TextSpan;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Document-level syntax representation combining CST structure with
 * semantic enrichment capabilities.
 *
 * <p>Wraps the low-level {@link SyntaxTree} (tree-sitter) and provides:</p>
 * <ol>
 *   <li>{@link SemanticNode} factory with identity caching (avoids re-wrapping)</li>
 *   <li>{@link ExcludedRangeIndex} for string/comment skipping</li>
 *   <li>Source version tracking for invalidation</li>
 *   <li>Node lookup by byte offset</li>
 * </ol>
 *
 * <p>Analogous to IntelliJ's {@code PsiFile} (root PSI element for a file)
 * and Zed's {@code SyntaxSnapshot} (immutable syntax view).</p>
 *
 * <h3>Thread safety</h3>
 * <p>Not thread-safe. The orchestrator must coordinate access
 * (e.g., via copy-on-parse or synchronization).</p>
 */
public final class DocumentSyntax {

    private static final String[] EXCLUDED_NODE_TYPES = {
            "string_literal",
            "string_fragment",
            "character_literal",
            "line_comment",
            "block_comment",
            "template_literal"
    };

    private final SyntaxTree syntaxTree;
    private final int sourceVersion;
    private final ExcludedRangeIndex excluded;
    private final Map<SyntaxNode, SemanticNode> nodeCache;

    private DocumentSyntax(SyntaxTree syntaxTree, int sourceVersion, ExcludedRangeIndex excluded) {
        this.syntaxTree = syntaxTree;
        this.sourceVersion = sourceVersion;
        this.excluded = excluded;
        this.nodeCache = new IdentityHashMap<>();
    }

    /**
     * Creates a new {@code DocumentSyntax} from a parsed tree-sitter tree.
     *
     * <p>Walks the CST to build the {@link ExcludedRangeIndex} for string
     * and comment regions, and initialises an empty node cache.</p>
     *
     * @param tree    the parsed tree-sitter syntax tree
     * @param version the source version this tree was parsed from
     * @return a new document syntax instance
     * @throws IllegalArgumentException if {@code tree} is {@code null}
     */
    public static DocumentSyntax create(SyntaxTree tree, int version) {
        if (tree == null) {
            throw new IllegalArgumentException("tree must not be null");
        }
        ExcludedRangeIndex excludedIndex = buildExcludedRanges(tree.getRootNode());
        return new DocumentSyntax(tree, version, excludedIndex);
    }

    /**
     * Wraps a raw {@link SyntaxNode} into a {@link SemanticNode}, returning a
     * cached instance if one already exists for this node identity.
     *
     * @param node the CST node to wrap (may be {@code null})
     * @return the cached or newly created {@code SemanticNode}, or {@code null}
     *         if {@code node} is {@code null} or a null-node
     */
    public SemanticNode wrap(SyntaxNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        SemanticNode cached = nodeCache.get(node);
        if (cached != null) {
            return cached;
        }
        SemanticNode created = new SemanticNode(node, this);
        nodeCache.put(node, created);
        return created;
    }

    /**
     * @return the root node of the syntax tree, wrapped as a {@link SemanticNode}
     */
    public SemanticNode root() {
        return wrap(syntaxTree.getRootNode());
    }

    /**
     * @return the full source text that was parsed to produce this tree
     */
    public String getSourceText() {
        return syntaxTree.getSourceText();
    }

    /**
     * @return the source version number this tree was parsed from
     */
    public int getSourceVersion() {
        return sourceVersion;
    }

    /**
     * Tests whether the given byte offset falls inside a string literal or comment.
     *
     * @param offset the byte offset to test (0-based)
     * @return {@code true} if the offset is inside an excluded region
     */
    public boolean isExcluded(int offset) {
        return excluded.contains(offset);
    }

    /**
     * Tests whether any part of the given span overlaps with a string literal or comment.
     *
     * @param span the text span to test
     * @return {@code true} if the span overlaps any excluded region
     */
    public boolean isExcluded(TextSpan span) {
        if (span == null) {
            return false;
        }
        return excluded.overlaps(span.getStartByte(), span.getEndByte());
    }

    /**
     * @return the excluded range index for direct querying
     */
    public ExcludedRangeIndex getExcludedRanges() {
        return excluded;
    }

    /**
     * Finds the deepest named node containing the given byte offset.
     *
     * <p>Walks from the root downward, at each level picking the child whose
     * span contains the offset, until no further narrowing is possible.</p>
     *
     * @param byteOffset the byte offset to search for (0-based)
     * @return the deepest named {@link SemanticNode} containing the offset,
     *         or {@code null} if the offset is outside the tree
     */
    public SemanticNode nodeAtOffset(int byteOffset) {
        SyntaxNode root = syntaxTree.getRootNode();
        if (root == null || root.isNull()) {
            return null;
        }
        TextSpan rootSpan = root.getSpan();
        if (rootSpan == null || !rootSpan.containsByte(byteOffset)) {
            return null;
        }
        SyntaxNode deepest = findDeepestNamedNode(root, byteOffset);
        return wrap(deepest);
    }

    /**
     * @return the underlying {@link SyntaxTree} for direct tree-sitter operations
     *         (queries, changed-range computation, etc.)
     */
    public SyntaxTree unwrapTree() {
        return syntaxTree;
    }

    /**
     * @return {@code true} if the parse tree contains any syntax errors
     */
    public boolean hasErrors() {
        return syntaxTree.hasErrors();
    }

    /**
     * Clears the entire node cache, discarding all {@link SemanticNode} wrappers
     * and their attached semantic data. Used after a full reparse.
     */
    public void invalidateCache() {
        nodeCache.clear();
    }

    /**
     * Evicts cached nodes whose spans overlap the given byte range.
     * Used after an incremental reparse to discard stale semantic data
     * in the edited region while preserving unaffected nodes.
     *
     * @param startByte inclusive start of the invalidated range
     * @param endByte   exclusive end of the invalidated range
     */
    public void invalidateRange(int startByte, int endByte) {
        Iterator<Map.Entry<SyntaxNode, SemanticNode>> iterator = nodeCache.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<SyntaxNode, SemanticNode> entry = iterator.next();
            SyntaxNode node = entry.getKey();
            TextSpan span = node.getSpan();
            if (span != null && span.getStartByte() < endByte && startByte < span.getEndByte()) {
                iterator.remove();
            }
        }
    }

    private static SyntaxNode findDeepestNamedNode(SyntaxNode node, int byteOffset) {
        SyntaxNode best = node;
        int childCount = node.getChildCount();
        for (int i = 0; i < childCount; i++) {
            SyntaxNode child = node.getChild(i);
            if (child == null) {
                continue;
            }
            TextSpan childSpan = child.getSpan();
            if (childSpan == null || !childSpan.containsByte(byteOffset)) {
                continue;
            }
            SyntaxNode deeper = findDeepestNamedNode(child, byteOffset);
            if (deeper.isNamed()) {
                best = deeper;
            }
        }
        return best;
    }

    private static ExcludedRangeIndex buildExcludedRanges(SyntaxNode root) {
        if (root == null || root.isNull()) {
            return ExcludedRangeIndex.empty();
        }
        List<ExcludedRangeIndex.Range> ranges = new ArrayList<>();
        collectExcludedRanges(root, ranges);
        if (ranges.isEmpty()) {
            return ExcludedRangeIndex.empty();
        }
        return ExcludedRangeIndex.build(ranges);
    }

    private static void collectExcludedRanges(SyntaxNode node, List<ExcludedRangeIndex.Range> out) {
        String type = node.getType();
        if (type != null && isExcludedNodeType(type)) {
            TextSpan span = node.getSpan();
            if (span != null && span.getByteLength() > 0) {
                out.add(new ExcludedRangeIndex.Range(span.getStartByte(), span.getEndByte()));
            }
            return;
        }
        int childCount = node.getChildCount();
        for (int i = 0; i < childCount; i++) {
            SyntaxNode child = node.getChild(i);
            if (child != null) {
                collectExcludedRanges(child, out);
            }
        }
    }

    private static boolean isExcludedNodeType(String type) {
        for (String excluded : EXCLUDED_NODE_TYPES) {
            if (excluded.equals(type)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        SyntaxNode root = syntaxTree.getRootNode();
        int nodeCount = nodeCache.size();
        return "DocumentSyntax{version=" + sourceVersion
                + ", errors=" + syntaxTree.hasErrors()
                + ", cached=" + nodeCount
                + ", excluded=" + excluded.size()
                + ", root=" + (root != null ? root.getType() : "null")
                + '}';
    }
}
