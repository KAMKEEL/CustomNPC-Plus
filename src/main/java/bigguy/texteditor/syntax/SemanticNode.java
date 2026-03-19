package bigguy.texteditor.syntax;

import bigguy.texteditor.treesitter.java.SyntaxNode;
import bigguy.texteditor.treesitter.java.TextSpan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * PSI-style wrapper around a tree-sitter CST node that provides semantic
 * enrichment without modifying the underlying concrete syntax tree.
 *
 * <p>The {@link SyntaxNode} is the <em>structure</em> (syntax) — it knows about
 * node types, positions, text, and parent/child relationships. This class is
 * the <em>meaning</em> (semantics layered on top) — it carries lazily-resolved
 * type information, symbol bindings, and scope references.</p>
 *
 * <h3>Design (IntelliJ IDEA analogy)</h3>
 * <table>
 *   <tr><th>IntelliJ</th><th>This project</th></tr>
 *   <tr><td>{@code ASTNode}</td><td>{@link SyntaxNode} (raw CST)</td></tr>
 *   <tr><td>{@code PsiElement}</td><td>{@code SemanticNode} (semantic view)</td></tr>
 *   <tr><td>{@code PsiFile}</td><td>{@link DocumentSyntax} (root container)</td></tr>
 * </table>
 *
 * <h3>Semantic slots</h3>
 * <p>The three mutable slots ({@code resolvedType}, {@code symbol}, {@code scope})
 * are typed as {@link Object} intentionally. The concrete types (TypeInfo, Symbol,
 * Scope) live in downstream packages that do not yet exist. Using {@code Object}
 * avoids circular package dependencies. Callers access them through generic
 * getters that perform an unchecked cast — the analysis pipeline guarantees the
 * stored object matches the expected type at each call site.</p>
 *
 * <p>Semantic data is populated lazily during analysis and may be {@code null}
 * when in "dumb mode" (i.e., only the CST has been built, no analysis pass
 * has run yet).</p>
 *
 * <h3>Thread safety</h3>
 * <p>Not thread-safe. The owning {@link DocumentSyntax} must coordinate access.</p>
 */
public final class SemanticNode {

    private static final Set<String> SCOPE_INTRODUCING_TYPES = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(
                    "program",
                    "class_body",
                    "method_declaration",
                    "constructor_declaration",
                    "block",
                    "for_statement",
                    "enhanced_for_statement",
                    "while_statement",
                    "do_statement",
                    "if_statement",
                    "try_statement",
                    "catch_clause",
                    "switch_block_statement_group",
                    "lambda_expression",
                    "static_initializer"
            ))
    );

    private static final Set<String> TYPE_NODE_TYPES = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(
                    "type_identifier",
                    "generic_type",
                    "array_type",
                    "scoped_type_identifier",
                    "wildcard",
                    "void_type",
                    "integral_type",
                    "floating_point_type",
                    "boolean_type"
            ))
    );

    private static final Set<String> LITERAL_KEYWORDS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("true", "false", "null"))
    );

    private final SyntaxNode syntaxNode;
    private final DocumentSyntax document;

    private Object resolvedType;
    private Object symbol;
    private Object scope;

    /**
     * Creates a semantic wrapper around the given CST node.
     *
     * <p>Package-private: only {@link DocumentSyntax#wrap(SyntaxNode)} should
     * create instances, ensuring identity caching is respected.</p>
     *
     * @param syntaxNode the underlying CST node (must not be null or null-node)
     * @param document   the owning document (must not be null)
     */
    SemanticNode(SyntaxNode syntaxNode, DocumentSyntax document) {
        if (syntaxNode == null || syntaxNode.isNull()) {
            throw new IllegalArgumentException("syntaxNode must not be null or a null-node");
        }
        if (document == null) {
            throw new IllegalArgumentException("document must not be null");
        }
        this.syntaxNode = syntaxNode;
        this.document = document;
    }

    /**
     * Returns the tree-sitter node type string (e.g., {@code "method_declaration"},
     * {@code "identifier"}).
     *
     * @return the node type, or {@code null} if the underlying node is null
     */
    public String nodeType() {
        return syntaxNode.getType();
    }

    /**
     * Returns the source text span (byte offsets and row/column positions)
     * covered by this node.
     *
     * @return the span, or {@code null} if the underlying node is null
     */
    public TextSpan span() {
        return syntaxNode.getSpan();
    }

    /**
     * @return the source text covered by this node, or {@code null} if unavailable
     */
    public String text() {
        return syntaxNode.getText();
    }

    /**
     * @return {@code true} if this is a named node in the grammar (not anonymous punctuation)
     */
    public boolean isNamed() {
        return syntaxNode.isNamed();
    }

    /**
     * @return {@code true} if this node or any descendant contains a parse error
     */
    public boolean hasError() {
        return syntaxNode.hasError();
    }

    /**
     * @return {@code true} if this node itself is an ERROR node
     */
    public boolean isError() {
        return syntaxNode.isError();
    }

    /**
     * @return {@code true} if this node is a MISSING node (expected but absent in source)
     */
    public boolean isMissing() {
        return syntaxNode.isMissing();
    }

    /**
     * @return total number of children (named and anonymous)
     */
    public int childCount() {
        return syntaxNode.getChildCount();
    }

    /**
     * Returns the child at the given index, wrapped as a {@code SemanticNode}.
     *
     * @param index zero-based child index
     * @return the wrapped child, or {@code null} if the index is out of range
     */
    public SemanticNode child(int index) {
        return document.wrap(syntaxNode.getChild(index));
    }

    /**
     * Returns the named child at the given index, wrapped as a {@code SemanticNode}.
     *
     * @param index zero-based named-child index
     * @return the wrapped named child, or {@code null} if the index is out of range
     */
    public SemanticNode namedChild(int index) {
        return document.wrap(syntaxNode.getNamedChild(index));
    }

    /**
     * Returns the child with the given field name (as defined by tree-sitter grammar),
     * wrapped as a {@code SemanticNode}.
     *
     * @param name the field name (e.g., {@code "name"}, {@code "body"}, {@code "type"})
     * @return the wrapped child, or {@code null} if no child has that field name
     */
    public SemanticNode childByFieldName(String name) {
        return document.wrap(syntaxNode.getChildByFieldName(name));
    }

    /**
     * @return the wrapped parent, or {@code null} if this is the root
     */
    public SemanticNode parent() {
        return document.wrap(syntaxNode.getParent());
    }

    /**
     * @return the wrapped next sibling (named or anonymous), or {@code null}
     */
    public SemanticNode nextSibling() {
        return document.wrap(syntaxNode.getNextSibling());
    }

    /**
     * @return the wrapped next named sibling, or {@code null}
     */
    public SemanticNode nextNamedSibling() {
        return document.wrap(syntaxNode.getNextNamedSibling());
    }

    /**
     * @return the wrapped previous sibling (named or anonymous), or {@code null}
     */
    public SemanticNode prevSibling() {
        return document.wrap(syntaxNode.getPrevSibling());
    }

    /**
     * @return an unmodifiable list of all named children, each wrapped as {@code SemanticNode}
     */
    public List<SemanticNode> namedChildren() {
        List<SyntaxNode> rawChildren = syntaxNode.getNamedChildren();
        if (rawChildren.isEmpty()) {
            return Collections.emptyList();
        }
        List<SemanticNode> result = new ArrayList<>(rawChildren.size());
        for (SyntaxNode raw : rawChildren) {
            SemanticNode wrapped = document.wrap(raw);
            if (wrapped != null) {
                result.add(wrapped);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Finds the first descendant matching the given tree-sitter node type.
     *
     * @param nodeType the node type to search for (e.g., {@code "identifier"})
     * @return the first matching descendant wrapped as {@code SemanticNode}, or {@code null}
     */
    public SemanticNode findFirst(String nodeType) {
        return document.wrap(syntaxNode.findFirst(nodeType));
    }

    /**
     * Finds all descendants matching the given tree-sitter node type.
     *
     * @param nodeType the node type to search for
     * @return an unmodifiable list of matching descendants, never {@code null}
     */
    public List<SemanticNode> findAll(String nodeType) {
        List<SyntaxNode> rawMatches = syntaxNode.findAll(nodeType);
        if (rawMatches.isEmpty()) {
            return Collections.emptyList();
        }
        List<SemanticNode> result = new ArrayList<>(rawMatches.size());
        for (SyntaxNode raw : rawMatches) {
            SemanticNode wrapped = document.wrap(raw);
            if (wrapped != null) {
                result.add(wrapped);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns the resolved type information attached to this node.
     * The unchecked cast is safe — the analysis pipeline guarantees type consistency.
     *
     * @param <T> the expected type (typically TypeInfo)
     * @return the resolved type, or {@code null} if not yet resolved
     */
    @SuppressWarnings("unchecked")
    public <T> T getResolvedType() {
        return (T) resolvedType;
    }

    /**
     * @param <T>  the type of the value (typically TypeInfo)
     * @param type the resolved type information to attach
     */
    public <T> void setResolvedType(T type) {
        this.resolvedType = type;
    }

    /**
     * Returns the symbol binding attached to this node (a named declaration
     * that this node declares or references).
     *
     * @param <T> the expected type (typically Symbol)
     * @return the symbol, or {@code null} if not yet resolved
     */
    @SuppressWarnings("unchecked")
    public <T> T getSymbol() {
        return (T) symbol;
    }

    /**
     * @param <T>    the type of the value (typically Symbol)
     * @param symbol the symbol binding to attach
     */
    public <T> void setSymbol(T symbol) {
        this.symbol = symbol;
    }

    /**
     * Returns the scope attached to this node (lexical scope that this node
     * introduces or belongs to).
     *
     * @param <T> the expected type (typically Scope)
     * @return the scope, or {@code null} if not yet resolved
     */
    @SuppressWarnings("unchecked")
    public <T> T getScope() {
        return (T) scope;
    }

    /**
     * @param <T>   the type of the value (typically Scope)
     * @param scope the scope to attach
     */
    public <T> void setScope(T scope) {
        this.scope = scope;
    }

    /** @return {@code true} if a resolved type has been attached */
    public boolean hasResolvedType() {
        return resolvedType != null;
    }

    /** @return {@code true} if a symbol binding has been attached */
    public boolean hasSymbol() {
        return symbol != null;
    }

    /** @return {@code true} if a scope has been attached */
    public boolean hasScope() {
        return scope != null;
    }

    /**
     * @return {@code true} if the node type ends with {@code "_declaration"}
     */
    public boolean isDeclaration() {
        String type = syntaxNode.getType();
        return type != null && type.endsWith("_declaration");
    }

    /**
     * @return {@code true} if this node represents an expression (types ending
     *         with {@code "_expression"}, or {@code "identifier"}, {@code "method_invocation"}, etc.)
     */
    public boolean isExpression() {
        String type = syntaxNode.getType();
        if (type == null) {
            return false;
        }
        return type.endsWith("_expression")
                || "identifier".equals(type)
                || "method_invocation".equals(type)
                || "field_access".equals(type)
                || "array_access".equals(type)
                || "object_creation_expression".equals(type)
                || "cast_expression".equals(type);
    }

    /**
     * @return {@code true} if the node type ends with {@code "_statement"}
     */
    public boolean isStatement() {
        String type = syntaxNode.getType();
        return type != null && type.endsWith("_statement");
    }

    /**
     * @return {@code true} if the node type contains {@code "literal"} or is
     *         {@code "true"}, {@code "false"}, or {@code "null"}
     */
    public boolean isLiteral() {
        String type = syntaxNode.getType();
        if (type == null) {
            return false;
        }
        return type.contains("literal") || LITERAL_KEYWORDS.contains(type);
    }

    /**
     * @return {@code true} if this node is a type reference ({@code "type_identifier"},
     *         {@code "generic_type"}, {@code "array_type"}, etc.)
     */
    public boolean isTypeNode() {
        String type = syntaxNode.getType();
        return type != null && TYPE_NODE_TYPES.contains(type);
    }

    /**
     * @return {@code true} if the node type is exactly {@code "identifier"}
     */
    public boolean isIdentifier() {
        return "identifier".equals(syntaxNode.getType());
    }

    /**
     * @return {@code true} if this node introduces a new lexical scope
     *         (program, class_body, method_declaration, block, lambda_expression, etc.)
     */
    public boolean isScopeIntroducing() {
        String type = syntaxNode.getType();
        return type != null && SCOPE_INTRODUCING_TYPES.contains(type);
    }

    /**
     * Returns the underlying raw CST node. Prefer the delegating methods on this class;
     * use this escape hatch only when the low-level tree-sitter API is needed directly.
     *
     * @return the underlying {@link SyntaxNode}, never {@code null}
     */
    public SyntaxNode unwrap() {
        return syntaxNode;
    }

    /**
     * @return the {@link DocumentSyntax} that owns this node
     */
    public DocumentSyntax getDocument() {
        return document;
    }

    @Override
    public String toString() {
        TextSpan s = syntaxNode.getSpan();
        String type = syntaxNode.getType();
        StringBuilder sb = new StringBuilder("SemanticNode{");
        sb.append(type);
        if (s != null) {
            sb.append(' ').append(s);
        }
        if (resolvedType != null) {
            sb.append(" type=").append(resolvedType);
        }
        if (symbol != null) {
            sb.append(" sym=").append(symbol);
        }
        sb.append('}');
        return sb.toString();
    }
}
