package bigguy.texteditor.analysis;

import bigguy.texteditor.syntax.DocumentSyntax;
import bigguy.texteditor.syntax.SemanticNode;
import bigguy.texteditor.treesitter.java.TextSpan;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Classifies text edits into invalidation scopes, determining how much of
 * the semantic model needs rebuilding after an incremental reparse.
 *
 * <p>This is the intelligence behind incremental analysis — the core of the
 * "every keystroke must NOT cause a full rebuild" requirement. By inspecting
 * which CST nodes changed, the strategy assigns a minimal
 * {@link InvalidationScope} that tells {@code SemanticModel} exactly which
 * sub-components need updating.</p>
 *
 * <h3>Design</h3>
 * <p>The algorithm walks upward from each changed range's deepest node to
 * classify the edit into one of five categories:</p>
 * <ol>
 *   <li>{@link InvalidationScope#NONE NONE} — edit inside string/comment</li>
 *   <li>{@link InvalidationScope#LOCAL_SCOPE LOCAL_SCOPE} — edit inside method body</li>
 *   <li>{@link InvalidationScope#DECLARATION DECLARATION} — signature changed</li>
 *   <li>{@link InvalidationScope#IMPORTS IMPORTS} — import changed</li>
 *   <li>{@link InvalidationScope#STRUCTURAL STRUCTURAL} — class/method boundary changed</li>
 * </ol>
 *
 * <h3>Performance impact</h3>
 * <pre>
 * | InvalidationScope | SymbolTable | ScopeTree       | ImportIndex | Cost    |
 * |-------------------|-------------|-----------------|-------------|---------|
 * | NONE              | Keep        | Keep            | Keep        | ~0.01ms |
 * | LOCAL_SCOPE       | Keep        | Rebuild 1 scope | Keep        | ~2ms    |
 * | DECLARATION       | Rebuild     | Rebuild         | Keep        | ~10ms   |
 * | IMPORTS           | Keep        | Keep            | Rebuild     | ~10ms   |
 * | STRUCTURAL        | Rebuild all | Rebuild all     | Rebuild all | ~50ms   |
 * </pre>
 *
 * <h3>Thread safety</h3>
 * <p>This class is stateless and therefore inherently thread-safe. All methods
 * are pure functions of their arguments.</p>
 *
 * @see InvalidationScope
 */
public final class IncrementalStrategy {

    // ── CST node type constants ────────────────────────────────────────────────
    // Centralised here to avoid scattered string literals throughout the class.

    private static final String NODE_IMPORT_DECLARATION = "import_declaration";
    private static final String NODE_CLASS_DECLARATION = "class_declaration";
    private static final String NODE_INTERFACE_DECLARATION = "interface_declaration";
    private static final String NODE_ENUM_DECLARATION = "enum_declaration";
    private static final String NODE_ANNOTATION_TYPE_DECLARATION = "annotation_type_declaration";
    private static final String NODE_METHOD_DECLARATION = "method_declaration";
    private static final String NODE_CONSTRUCTOR_DECLARATION = "constructor_declaration";
    private static final String NODE_FIELD_DECLARATION = "field_declaration";
    private static final String NODE_BLOCK = "block";
    private static final String NODE_PROGRAM = "program";

    /**
     * Node types that represent type/member declarations whose modification
     * requires at least a {@link InvalidationScope#DECLARATION DECLARATION}
     * level rebuild.
     */
    private static final Set<String> DECLARATION_NODE_TYPES = Collections.unmodifiableSet(
            new HashSet<String>(Arrays.asList(
                    NODE_CLASS_DECLARATION,
                    NODE_INTERFACE_DECLARATION,
                    NODE_ENUM_DECLARATION,
                    NODE_ANNOTATION_TYPE_DECLARATION,
                    NODE_METHOD_DECLARATION,
                    NODE_CONSTRUCTOR_DECLARATION,
                    NODE_FIELD_DECLARATION
            ))
    );

    /**
     * Node types that represent method-like constructs (nodes whose "body"
     * child introduces a local scope).
     */
    private static final Set<String> METHOD_LIKE_NODE_TYPES = Collections.unmodifiableSet(
            new HashSet<String>(Arrays.asList(
                    NODE_METHOD_DECLARATION,
                    NODE_CONSTRUCTOR_DECLARATION
            ))
    );

    // ── Invalidation scope enum ────────────────────────────────────────────────

    /**
     * Describes the extent of semantic invalidation required after an edit.
     *
     * <p>Scopes are ordered from cheapest ({@link #NONE}) to most expensive
     * ({@link #STRUCTURAL}). When multiple changed ranges yield different
     * scopes, the highest (most expensive) scope wins.</p>
     */
    public enum InvalidationScope {

        /**
         * No semantic change — the edit was entirely inside a string literal or
         * comment. Only byte-offset adjustment is needed; all semantic data
         * structures remain valid.
         */
        NONE(0),

        /**
         * Edit inside a method or constructor body — only the affected scope
         * needs to be rebuilt in the {@code ScopeTree}. The {@code SymbolTable}
         * and {@code ImportIndex} are unaffected because no declarations or
         * imports changed.
         */
        LOCAL_SCOPE(1),

        /**
         * A type, method, or field declaration changed — the {@code SymbolTable}
         * and {@code ScopeTree} need rebuilding, but the {@code ImportIndex}
         * remains valid.
         */
        DECLARATION(2),

        /**
         * An import declaration changed — the {@code ImportIndex} needs
         * rebuilding. Downstream type resolution may also need re-running.
         */
        IMPORTS(3),

        /**
         * A class or method boundary changed (structural edit) — all semantic
         * data structures must be fully rebuilt. This is the worst case and
         * corresponds to edits that alter the fundamental structure of the file
         * (e.g., adding/removing a class, changing method signatures across
         * multiple declarations).
         */
        STRUCTURAL(4);

        private final int severity;

        InvalidationScope(int severity) {
            this.severity = severity;
        }

        /**
         * Returns the numeric severity level for comparison. Higher values
         * indicate more expensive rebuilds.
         *
         * @return the severity ordinal (0 = cheapest, 4 = most expensive)
         */
        public int severity() {
            return severity;
        }

        /**
         * Returns the more severe of two scopes.
         *
         * @param a the first scope
         * @param b the second scope
         * @return the scope with the higher severity
         */
        public static InvalidationScope max(InvalidationScope a, InvalidationScope b) {
            if (a == null) {
                return b;
            }
            if (b == null) {
                return a;
            }
            return a.severity >= b.severity ? a : b;
        }
    }

    // ── Constructor ────────────────────────────────────────────────────────────

    /**
     * Creates a new incremental strategy instance.
     *
     * <p>This class is stateless; a single instance can be reused across
     * all analysis cycles.</p>
     */
    public IncrementalStrategy() {
        // Stateless — no initialisation needed.
    }

    // ── Public API ─────────────────────────────────────────────────────────────

    /**
     * Classifies the combined impact of all CST changes to determine how much
     * of the semantic model needs re-analysis.
     *
     * <p>Each changed range is classified independently via
     * {@link #classifySingleChange(TextSpan, DocumentSyntax)}, and the
     * worst-case (highest severity) scope across all ranges is returned.</p>
     *
     * <p>If no ranges changed, returns {@link InvalidationScope#NONE}.</p>
     *
     * @param changedRanges the byte ranges that changed (from tree-sitter's
     *                      {@code getChangedRanges()}); may be {@code null} or empty
     * @param syntax        the <em>new</em> document syntax after reparse
     * @return the invalidation scope indicating how much needs rebuilding
     * @throws IllegalArgumentException if {@code syntax} is {@code null}
     */
    public InvalidationScope classify(TextSpan[] changedRanges, DocumentSyntax syntax) {
        if (syntax == null) {
            throw new IllegalArgumentException("syntax must not be null");
        }
        if (changedRanges == null || changedRanges.length == 0) {
            return InvalidationScope.NONE;
        }

        InvalidationScope worst = InvalidationScope.NONE;

        for (TextSpan range : changedRanges) {
            if (range == null) {
                continue;
            }
            InvalidationScope rangeScope = classifySingleChange(range, syntax);
            worst = InvalidationScope.max(worst, rangeScope);

            // Short-circuit: STRUCTURAL is the maximum possible severity.
            if (worst == InvalidationScope.STRUCTURAL) {
                return worst;
            }
        }

        return worst;
    }

    /**
     * Classifies a single changed byte range to determine its invalidation scope.
     *
     * <p>The algorithm proceeds in three steps:</p>
     * <ol>
     *   <li><strong>Exclusion check:</strong> If the edit start falls inside a
     *       string literal or comment, the edit is semantically invisible →
     *       {@link InvalidationScope#NONE}.</li>
     *   <li><strong>Node lookup:</strong> Find the deepest named CST node at the
     *       edit's start byte offset.</li>
     *   <li><strong>Ancestor walk:</strong> Walk upward from the deepest node,
     *       checking whether the change falls inside a method body (local scope),
     *       an import declaration, or a type/member declaration.</li>
     * </ol>
     *
     * @param changedRange the byte range that changed
     * @param syntax       the <em>new</em> document syntax after reparse
     * @return the invalidation scope for this single change
     * @throws IllegalArgumentException if either argument is {@code null}
     */
    public InvalidationScope classifySingleChange(TextSpan changedRange, DocumentSyntax syntax) {
        if (changedRange == null) {
            throw new IllegalArgumentException("changedRange must not be null");
        }
        if (syntax == null) {
            throw new IllegalArgumentException("syntax must not be null");
        }

        // Step 1: Check if the edit is inside an excluded region (string/comment).
        // If so, no semantic data structures are affected.
        if (syntax.isExcluded(changedRange.getStartByte())) {
            return InvalidationScope.NONE;
        }

        // Step 2: Find the deepest named node at the start of the change.
        SemanticNode node = syntax.nodeAtOffset(changedRange.getStartByte());
        if (node == null) {
            // Cannot determine context — assume the worst case.
            return InvalidationScope.STRUCTURAL;
        }

        // Step 3: Walk up the ancestry chain to classify the change.
        return classifyByAncestry(node, changedRange);
    }

    // ── Private classification logic ───────────────────────────────────────────

    /**
     * Walks upward from the given node to its ancestors, classifying the change
     * based on which syntactic construct encloses it.
     *
     * <p>The walk checks in order:</p>
     * <ol>
     *   <li>Is the node inside a method/constructor body? → {@code LOCAL_SCOPE}</li>
     *   <li>Is the node inside an import declaration? → {@code IMPORTS}</li>
     *   <li>Is the node inside a declaration node? → {@code DECLARATION}</li>
     *   <li>Otherwise → {@code STRUCTURAL}</li>
     * </ol>
     *
     * @param startNode    the deepest node at the change offset
     * @param changedRange the byte range that changed
     * @return the classified invalidation scope
     */
    private InvalidationScope classifyByAncestry(SemanticNode startNode, TextSpan changedRange) {
        SemanticNode current = startNode;

        // Track whether we've seen a block node that could be a method body.
        // This prevents misclassifying standalone blocks (like static initialisers)
        // as method bodies.
        boolean insideMethodBody = false;

        while (current != null) {
            String nodeType = current.nodeType();
            if (nodeType == null) {
                current = current.parent();
                continue;
            }

            // Check if we're inside a method/constructor body.
            // A "block" node that is the "body" field of a method/constructor
            // means the edit is contained within a local scope.
            if (NODE_BLOCK.equals(nodeType)) {
                SemanticNode parent = current.parent();
                if (parent != null && isMethodLikeNode(parent.nodeType())) {
                    // Verify the change is entirely within this body block.
                    TextSpan bodySpan = current.span();
                    if (bodySpan != null && bodySpan.contains(changedRange)) {
                        insideMethodBody = true;
                    }
                }
            }

            // If we've reached a method-like node and the edit was inside its body,
            // we can classify as LOCAL_SCOPE.
            if (isMethodLikeNode(nodeType) && insideMethodBody) {
                return InvalidationScope.LOCAL_SCOPE;
            }

            // Check for import declarations.
            if (NODE_IMPORT_DECLARATION.equals(nodeType)) {
                return InvalidationScope.IMPORTS;
            }

            // Check for declaration nodes (but not if we already know we're in a method body).
            // For method/constructor declarations, we reach here only if the edit was in
            // the signature (not the body), which is a DECLARATION-level change.
            if (isDeclarationNode(nodeType) && !insideMethodBody) {
                return InvalidationScope.DECLARATION;
            }

            // If we've reached the program root without finding a specific construct,
            // the edit is at the top level — structural change.
            if (NODE_PROGRAM.equals(nodeType)) {
                // If the edit is directly inside the program node (e.g., adding a new
                // class or import), and we haven't classified it yet, it's structural.
                break;
            }

            current = current.parent();
        }

        // Default: structural change (couldn't classify more specifically).
        return InvalidationScope.STRUCTURAL;
    }

    // ── Static helper methods ──────────────────────────────────────────────────

    /**
     * Tests whether the given node type represents a declaration construct
     * (class, interface, enum, annotation, method, constructor, or field).
     *
     * @param nodeType the tree-sitter node type string
     * @return {@code true} if the node type is a declaration
     */
    private static boolean isDeclarationNode(String nodeType) {
        return nodeType != null && DECLARATION_NODE_TYPES.contains(nodeType);
    }

    /**
     * Tests whether the given node type represents a method-like construct
     * (method or constructor declaration) that has a body introducing a
     * local scope.
     *
     * @param nodeType the tree-sitter node type string
     * @return {@code true} if the node type is method-like
     */
    private static boolean isMethodLikeNode(String nodeType) {
        return nodeType != null && METHOD_LIKE_NODE_TYPES.contains(nodeType);
    }

    @Override
    public String toString() {
        return "IncrementalStrategy{}";
    }
}
