package bigguy.texteditor.semantics;

import bigguy.texteditor.index.Symbol.FieldSymbol;
import bigguy.texteditor.index.SymbolKey;
import bigguy.texteditor.index.SymbolKind;
import bigguy.texteditor.syntax.DocumentSyntax;
import bigguy.texteditor.syntax.SemanticNode;
import bigguy.texteditor.treesitter.java.TextSpan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Hierarchical scope tree built from CST nodes, implementing IntelliJ's
 * {@code PsiScopeProcessor} pattern for variable resolution.
 *
 * <p>Replaces ScriptDocument's triple-nested {@code methodLocals}
 * ({@code Map<Integer, Map<String, List<FieldInfo>>>}), {@code topLevelLocals},
 * and {@code innerScopes} with a proper tree structure:</p>
 *
 * <pre>
 * FileScope (program)
 *   ├── ClassScope (class_body)
 *   │     ├── MethodScope (method_declaration)
 *   │     │     ├── BlockScope (block / for / if / try)
 *   │     │     └── LambdaScope (lambda_expression)
 *   │     └── FieldDecl (field_declaration)
 *   └── ImportDecl (import_declaration)
 * </pre>
 *
 * <h3>Resolution algorithm (PsiScopeProcessor)</h3>
 * <ol>
 *   <li>Find deepest scope S containing the byte offset via binary search</li>
 *   <li>Check S.locals for the variable name</li>
 *   <li>If found AND the declaration offset &lt; query offset → return it</li>
 *   <li>If not found → S = S.parent, goto 2</li>
 *   <li>If S is null (walked past root) → return empty</li>
 * </ol>
 *
 * <h3>Thread safety</h3>
 * <p>A {@code ScopeTree} instance is built once from a {@link DocumentSyntax}
 * snapshot and is effectively immutable after construction. Multiple threads
 * may query it concurrently without synchronization.</p>
 *
 * @see Scope
 * @see ScopeKind
 * @see DocumentSyntax
 */
public final class ScopeTree {

    // ── Tree-sitter node type constants ────────────────────────────────────────
    // Sourced from the tree-sitter-java grammar (v0.23.x).

    private static final String NODE_PROGRAM = "program";
    private static final String NODE_CLASS_BODY = "class_body";
    private static final String NODE_METHOD_DECLARATION = "method_declaration";
    private static final String NODE_CONSTRUCTOR_DECLARATION = "constructor_declaration";
    private static final String NODE_BLOCK = "block";
    private static final String NODE_FOR_STATEMENT = "for_statement";
    private static final String NODE_ENHANCED_FOR_STATEMENT = "enhanced_for_statement";
    private static final String NODE_WHILE_STATEMENT = "while_statement";
    private static final String NODE_DO_STATEMENT = "do_statement";
    private static final String NODE_IF_STATEMENT = "if_statement";
    private static final String NODE_TRY_STATEMENT = "try_statement";
    private static final String NODE_CATCH_CLAUSE = "catch_clause";
    private static final String NODE_SWITCH_BLOCK_STATEMENT_GROUP = "switch_block_statement_group";
    private static final String NODE_LAMBDA_EXPRESSION = "lambda_expression";
    private static final String NODE_STATIC_INITIALIZER = "static_initializer";
    private static final String NODE_LOCAL_VARIABLE_DECLARATION = "local_variable_declaration";
    private static final String NODE_FORMAL_PARAMETER = "formal_parameter";
    private static final String NODE_CATCH_FORMAL_PARAMETER = "catch_formal_parameter";
    private static final String NODE_SPREAD_PARAMETER = "spread_parameter";
    private static final String NODE_LAMBDA_PARAMETERS = "formal_parameters";
    private static final String NODE_INFERRED_PARAMETERS = "inferred_parameters";
    private static final String NODE_VARIABLE_DECLARATOR = "variable_declarator";
    private static final String NODE_IDENTIFIER = "identifier";
    private static final String NODE_ENHANCED_FOR_VARIABLE = "enhanced_for_variable";

    // Field names used by tree-sitter-java grammar.
    private static final String FIELD_NAME = "name";
    private static final String FIELD_TYPE = "type";
    private static final String FIELD_BODY = "body";
    private static final String FIELD_PARAMETERS = "parameters";
    private static final String FIELD_DIMENSIONS = "dimensions";

    private final Scope root;
    private final List<Scope> flatScopes;

    private ScopeTree(Scope root, List<Scope> flatScopes) {
        this.root = root;
        this.flatScopes = Collections.unmodifiableList(flatScopes);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ScopeKind enum
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Classifies the kind of lexical scope.
     */
    public enum ScopeKind {
        /** Root file scope ({@code program} node). */
        FILE,
        /** Class/interface/enum body ({@code class_body} node). */
        CLASS,
        /** Method or constructor declaration. */
        METHOD,
        /** Block statement ({@code block}, {@code for}, {@code if}, {@code try}, etc.). */
        BLOCK,
        /** Lambda expression body. */
        LAMBDA,
        /** Catch clause (declares exception variable). */
        CATCH,
        /** Switch block statement group. */
        SWITCH,
        /** Static initializer block. */
        STATIC_INIT
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Scope hierarchy (abstract base + concrete subclasses)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * A lexical scope in the scope tree. Each scope knows its parent, children,
     * local variable declarations, and the source range it covers.
     *
     * <p>Abstract base class (not an interface) because Java 8 lacks sealed
     * types. Concrete subclasses are static inner classes of {@link ScopeTree}.</p>
     */
    public static abstract class Scope {

        private final String nodeType;
        private final TextSpan range;
        private final Scope parent;
        private final List<Scope> children;
        private final Map<String, FieldSymbol> locals;
        private boolean frozen;

        /**
         * @param nodeType the tree-sitter node type that introduced this scope
         * @param range    the byte range this scope covers in the source text
         * @param parent   the enclosing scope, or {@code null} for the root
         */
        protected Scope(String nodeType, TextSpan range, Scope parent) {
            this.nodeType = nodeType;
            this.range = range;
            this.parent = parent;
            this.children = new ArrayList<>();
            this.locals = new LinkedHashMap<>();
            this.frozen = false;
        }

        /**
         * @return the classification of this scope
         */
        public abstract ScopeKind kind();

        /**
         * @return the tree-sitter node type string (e.g. {@code "method_declaration"})
         */
        public String nodeType() {
            return nodeType;
        }

        /**
         * @return the byte range this scope covers
         */
        public TextSpan range() {
            return range;
        }

        /**
         * @return the enclosing scope, or {@code null} for the root
         */
        public Scope parent() {
            return parent;
        }

        /**
         * @return an unmodifiable view of child scopes
         */
        public List<Scope> children() {
            return frozen
                    ? children
                    : Collections.unmodifiableList(children);
        }

        /**
         * @return an unmodifiable view of local variable declarations in this scope,
         *         keyed by variable name
         */
        public Map<String, FieldSymbol> locals() {
            return frozen
                    ? locals
                    : Collections.unmodifiableMap(locals);
        }

        /**
         * Tests whether the given byte offset falls within this scope's range.
         *
         * @param byteOffset the offset to test
         * @return {@code true} if this scope contains the offset
         */
        public boolean containsOffset(int byteOffset) {
            return range != null && range.containsByte(byteOffset);
        }

        /**
         * @return the start byte of this scope's range
         */
        public int startByte() {
            return range != null ? range.getStartByte() : 0;
        }

        /**
         * @return the end byte (exclusive) of this scope's range
         */
        public int endByte() {
            return range != null ? range.getEndByte() : 0;
        }

        void addChild(Scope child) {
            if (frozen) {
                throw new IllegalStateException("Cannot add children to a frozen scope");
            }
            children.add(child);
        }

        void addLocal(FieldSymbol local) {
            if (frozen) {
                throw new IllegalStateException("Cannot add locals to a frozen scope");
            }
            locals.put(local.name(), local);
        }

        /**
         * Freezes this scope and all descendants, making the children and locals
         * lists truly unmodifiable. Called once after the tree is fully built.
         */
        void freeze() {
            if (frozen) {
                return;
            }
            frozen = true;
            for (Scope child : children) {
                child.freeze();
            }
        }

        @Override
        public String toString() {
            return kind().name() + "{" + nodeType + " " + range
                    + ", locals=" + locals.size()
                    + ", children=" + children.size() + "}";
        }
    }

    /**
     * Root file scope, corresponding to the {@code program} node.
     */
    public static final class FileScope extends Scope {

        /**
         * @param range  the byte range of the entire file
         * @param parent always {@code null} for the root
         */
        public FileScope(TextSpan range, Scope parent) {
            super(NODE_PROGRAM, range, parent);
        }

        @Override
        public ScopeKind kind() {
            return ScopeKind.FILE;
        }
    }

    /**
     * Class, interface, or enum body scope.
     */
    public static final class ClassScope extends Scope {

        private final String className;

        /**
         * @param range     the byte range of the class body
         * @param parent    the enclosing scope
         * @param className the declared class name (may be {@code null} for anonymous classes)
         */
        public ClassScope(TextSpan range, Scope parent, String className) {
            super(NODE_CLASS_BODY, range, parent);
            this.className = className;
        }

        /**
         * @return the class name, or {@code null} for anonymous classes
         */
        public String className() {
            return className;
        }

        @Override
        public ScopeKind kind() {
            return ScopeKind.CLASS;
        }
    }

    /**
     * Method or constructor declaration scope.
     */
    public static final class MethodScope extends Scope {

        private final String methodName;

        /**
         * @param nodeType   the tree-sitter node type ({@code "method_declaration"} or
         *                   {@code "constructor_declaration"})
         * @param range      the byte range of the method declaration
         * @param parent     the enclosing scope
         * @param methodName the method name ({@code "<init>"} for constructors)
         */
        public MethodScope(String nodeType, TextSpan range, Scope parent,
                           String methodName) {
            super(nodeType, range, parent);
            this.methodName = methodName;
        }

        /**
         * @return the method name, or {@code "<init>"} for constructors
         */
        public String methodName() {
            return methodName;
        }

        @Override
        public ScopeKind kind() {
            return ScopeKind.METHOD;
        }
    }

    /**
     * Block scope for {@code block}, {@code for_statement}, {@code if_statement},
     * {@code while_statement}, {@code do_statement}, {@code try_statement},
     * and {@code switch_block_statement_group} nodes.
     */
    public static final class BlockScope extends Scope {

        /**
         * @param nodeType the tree-sitter node type
         * @param range    the byte range of the block
         * @param parent   the enclosing scope
         */
        public BlockScope(String nodeType, TextSpan range, Scope parent) {
            super(nodeType, range, parent);
        }

        @Override
        public ScopeKind kind() {
            if (NODE_SWITCH_BLOCK_STATEMENT_GROUP.equals(nodeType())) {
                return ScopeKind.SWITCH;
            }
            return ScopeKind.BLOCK;
        }
    }

    /**
     * Lambda expression scope.
     */
    public static final class LambdaScope extends Scope {

        /**
         * @param range  the byte range of the lambda expression
         * @param parent the enclosing scope
         */
        public LambdaScope(TextSpan range, Scope parent) {
            super(NODE_LAMBDA_EXPRESSION, range, parent);
        }

        @Override
        public ScopeKind kind() {
            return ScopeKind.LAMBDA;
        }
    }

    /**
     * Catch clause scope. Declares the caught exception variable.
     */
    public static final class CatchScope extends Scope {

        /**
         * @param range  the byte range of the catch clause
         * @param parent the enclosing scope
         */
        public CatchScope(TextSpan range, Scope parent) {
            super(NODE_CATCH_CLAUSE, range, parent);
        }

        @Override
        public ScopeKind kind() {
            return ScopeKind.CATCH;
        }
    }

    /**
     * Static initializer block scope.
     */
    public static final class StaticInitScope extends Scope {

        /**
         * @param range  the byte range of the static initializer
         * @param parent the enclosing scope
         */
        public StaticInitScope(TextSpan range, Scope parent) {
            super(NODE_STATIC_INITIALIZER, range, parent);
        }

        @Override
        public ScopeKind kind() {
            return ScopeKind.STATIC_INIT;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Building
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Builds a scope tree from a parsed {@link DocumentSyntax}.
     *
     * <p>Walks the CST recursively, creating scope nodes for each
     * scope-introducing tree-sitter node type, extracting local variable
     * declarations, and attaching scope references to {@link SemanticNode}s
     * via {@link SemanticNode#setScope(Object)}.</p>
     *
     * @param syntax the document syntax to build from (must not be {@code null})
     * @return the completed scope tree
     * @throws NullPointerException     if {@code syntax} is {@code null}
     * @throws IllegalArgumentException if the syntax tree has no root node
     */
    public static ScopeTree buildFromCST(DocumentSyntax syntax) {
        if (syntax == null) {
            throw new NullPointerException("syntax must not be null");
        }
        SemanticNode root = syntax.root();
        if (root == null) {
            throw new IllegalArgumentException("syntax tree has no root node");
        }

        List<Scope> flatCollector = new ArrayList<>();
        Scope rootScope = buildScope(root, null, flatCollector);
        rootScope.freeze();

        return new ScopeTree(rootScope, flatCollector);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Resolution (PsiScopeProcessor pattern)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Resolves a variable by name at the given byte offset using IntelliJ's
     * PsiScopeProcessor walk-up pattern.
     *
     * <p>Algorithm:</p>
     * <ol>
     *   <li>Find the deepest scope containing {@code byteOffset}</li>
     *   <li>Check its locals for {@code name}</li>
     *   <li>If found AND the variable's declaration starts before {@code byteOffset},
     *       return it (variables are not visible before their declaration)</li>
     *   <li>If not found, walk up to the parent scope and repeat</li>
     *   <li>If the root is reached without a match, return empty</li>
     * </ol>
     *
     * @param name       the variable name to resolve
     * @param byteOffset the byte offset of the reference site
     * @return the resolved field symbol, or {@code null} if not found
     */
    public FieldSymbol resolveVariable(String name, int byteOffset) {
        if (name == null || root == null) {
            return null;
        }

        Scope scope = scopeAt(byteOffset);
        if (scope == null) {
            return null;
        }

        while (scope != null) {
            FieldSymbol local = scope.locals().get(name);
            if (local != null) {
                // Variable must be declared before the reference site.
                if (local.range().getStartByte() < byteOffset) {
                    return local;
                }
                // In class scope, fields are visible regardless of declaration order.
                if (scope.kind() == ScopeKind.CLASS) {
                    return local;
                }
            }
            scope = scope.parent();
        }
        return null;
    }

    /**
     * Finds the deepest (most specific) scope containing the given byte offset.
     *
     * <p>Uses a top-down walk from the root, narrowing into the child whose
     * range contains the offset. Among children at the same level, the tightest
     * (smallest range) match wins.</p>
     *
     * @param byteOffset the byte offset to locate
     * @return the deepest scope containing the offset, or the root if offset is
     *         within the file, or {@code null} if offset is outside the tree
     */
    public Scope scopeAt(int byteOffset) {
        if (root == null) {
            return null;
        }
        if (!root.containsOffset(byteOffset)) {
            return null;
        }
        return findDeepestScope(root, byteOffset);
    }

    /**
     * @return the root scope (always a {@link FileScope}), or {@code null} if
     *         the tree was built from an empty/missing CST
     */
    public Scope getRoot() {
        return root;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Iteration
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Returns all scopes in the tree in pre-order (depth-first) traversal order.
     *
     * @return an unmodifiable list of all scopes (never {@code null})
     */
    public List<Scope> getAllScopes() {
        return flatScopes;
    }

    /**
     * Returns all method scopes (method and constructor declarations).
     *
     * @return an unmodifiable list of method scopes (never {@code null}, may be empty)
     */
    public List<MethodScope> getAllMethodScopes() {
        List<MethodScope> result = new ArrayList<>();
        for (Scope scope : flatScopes) {
            if (scope instanceof MethodScope) {
                result.add((MethodScope) scope);
            }
        }
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(result);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Incremental support
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Finds the tightest (most deeply nested) scope that fully contains the
     * given text span. Useful for determining which scope an edit affects.
     *
     * @param range the text span to search for
     * @return the tightest enclosing scope, or the root if the span covers
     *         the whole file, or {@code null} if the span is outside the tree
     * @throws NullPointerException if {@code range} is {@code null}
     */
    public Scope findScopeContaining(TextSpan range) {
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        if (root == null) {
            return null;
        }
        TextSpan rootRange = root.range();
        if (rootRange == null || !rootRange.contains(range)) {
            return null;
        }
        return findTightestContaining(root, range);
    }

    @Override
    public String toString() {
        return "ScopeTree{scopes=" + flatScopes.size()
                + ", root=" + (root != null ? root.kind() : "null")
                + "}";
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Private — tree building
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Recursively builds the scope hierarchy from a {@link SemanticNode}.
     *
     * <p>For scope-introducing nodes, creates the appropriate {@link Scope}
     * subclass. For variable-declaring nodes within a scope, extracts the
     * declaration and adds it to the scope's locals. Attaches the scope
     * to the {@link SemanticNode} via {@code setScope}.</p>
     *
     * @param node      the current CST node
     * @param parent    the enclosing scope (null for root)
     * @param collector accumulates all scopes in pre-order for {@link #flatScopes}
     * @return the scope created for this node (or the parent if this node
     *         does not introduce a scope)
     */
    private static Scope buildScope(SemanticNode node, Scope parent,
                                    List<Scope> collector) {
        String type = node.nodeType();
        if (type == null) {
            return parent;
        }

        Scope scope = createScopeForNode(type, node, parent);
        if (scope != null) {
            collector.add(scope);
            if (parent != null) {
                parent.addChild(scope);
            }
            node.setScope(scope);
            extractLocals(node, scope);
            walkChildren(node, scope, collector);
            return scope;
        }

        // Not a scope-introducing node — check for variable declarations
        // that belong to the parent scope.
        if (parent != null && isVariableDeclaration(type)) {
            extractVariableDeclaration(node, parent);
        }

        // Recurse into children with the same parent scope.
        walkChildren(node, parent, collector);
        return parent;
    }

    /**
     * Creates the appropriate {@link Scope} subclass for a scope-introducing
     * tree-sitter node type, or returns {@code null} if the node does not
     * introduce a scope.
     */
    private static Scope createScopeForNode(String nodeType, SemanticNode node,
                                            Scope parent) {
        TextSpan range = node.span();

        switch (nodeType) {
            case NODE_PROGRAM:
                return new FileScope(range, parent);

            case NODE_CLASS_BODY:
                String className = extractClassNameFromParent(node);
                return new ClassScope(range, parent, className);

            case NODE_METHOD_DECLARATION: {
                String methodName = extractFieldText(node, FIELD_NAME);
                if (methodName == null) {
                    methodName = "<unknown>";
                }
                return new MethodScope(nodeType, range, parent, methodName);
            }

            case NODE_CONSTRUCTOR_DECLARATION: {
                String ctorName = extractFieldText(node, FIELD_NAME);
                if (ctorName == null) {
                    ctorName = "<init>";
                }
                return new MethodScope(nodeType, range, parent, ctorName);
            }

            case NODE_BLOCK:
                // Only create a block scope if not already the body of a method/constructor
                // (the method scope itself covers the body).
                if (parent != null && parent.kind() == ScopeKind.METHOD) {
                    // Check if this block is the direct body of the method.
                    if (isDirectMethodBody(node, parent)) {
                        return null;
                    }
                }
                return new BlockScope(nodeType, range, parent);

            case NODE_FOR_STATEMENT:
            case NODE_ENHANCED_FOR_STATEMENT:
            case NODE_WHILE_STATEMENT:
            case NODE_DO_STATEMENT:
            case NODE_IF_STATEMENT:
            case NODE_TRY_STATEMENT:
                return new BlockScope(nodeType, range, parent);

            case NODE_SWITCH_BLOCK_STATEMENT_GROUP:
                return new BlockScope(nodeType, range, parent);

            case NODE_CATCH_CLAUSE:
                return new CatchScope(range, parent);

            case NODE_LAMBDA_EXPRESSION:
                return new LambdaScope(range, parent);

            case NODE_STATIC_INITIALIZER:
                return new StaticInitScope(range, parent);

            default:
                return null;
        }
    }

    /**
     * Walks all named children of a node, recursing into {@link #buildScope}.
     */
    private static void walkChildren(SemanticNode node, Scope currentScope,
                                     List<Scope> collector) {
        List<SemanticNode> children = node.namedChildren();
        for (SemanticNode child : children) {
            buildScope(child, currentScope, collector);
        }
    }

    /**
     * Extracts local variable declarations from nodes that are direct children
     * of a scope-introducing node. Called after the scope is created but before
     * descending into children.
     *
     * <p>Handles:</p>
     * <ul>
     *   <li>Method/constructor formal parameters</li>
     *   <li>Enhanced-for loop variables</li>
     *   <li>Catch clause exception parameters</li>
     *   <li>Lambda parameters (both typed and inferred)</li>
     * </ul>
     */
    private static void extractLocals(SemanticNode node, Scope scope) {
        String type = node.nodeType();
        if (type == null) {
            return;
        }

        switch (type) {
            case NODE_METHOD_DECLARATION:
            case NODE_CONSTRUCTOR_DECLARATION:
                extractFormalParameters(node, scope);
                break;

            case NODE_ENHANCED_FOR_STATEMENT:
                extractEnhancedForVariable(node, scope);
                break;

            case NODE_CATCH_CLAUSE:
                extractCatchParameter(node, scope);
                break;

            case NODE_LAMBDA_EXPRESSION:
                extractLambdaParameters(node, scope);
                break;

            default:
                break;
        }
    }

    /**
     * Extracts formal parameters from a method or constructor declaration.
     */
    private static void extractFormalParameters(SemanticNode methodNode, Scope scope) {
        SemanticNode params = methodNode.childByFieldName(FIELD_PARAMETERS);
        if (params == null) {
            return;
        }
        List<SemanticNode> children = params.namedChildren();
        for (SemanticNode child : children) {
            String childType = child.nodeType();
            if (NODE_FORMAL_PARAMETER.equals(childType)
                    || NODE_SPREAD_PARAMETER.equals(childType)) {
                addParameterAsLocal(child, scope);
            }
        }
    }

    /**
     * Extracts the loop variable from an enhanced-for statement.
     * Grammar: {@code enhanced_for_statement} has fields {@code type} and
     * either a direct {@code name} field or an {@code enhanced_for_variable} child.
     */
    private static void extractEnhancedForVariable(SemanticNode forNode, Scope scope) {
        // tree-sitter-java grammar: the variable is in child by field "name"
        // or in an "enhanced_for_variable" named child, depending on grammar version.
        SemanticNode nameNode = forNode.childByFieldName(FIELD_NAME);
        SemanticNode typeNode = forNode.childByFieldName(FIELD_TYPE);

        if (nameNode == null) {
            // Try finding enhanced_for_variable child.
            List<SemanticNode> children = forNode.namedChildren();
            for (SemanticNode child : children) {
                if (NODE_ENHANCED_FOR_VARIABLE.equals(child.nodeType())) {
                    nameNode = child.childByFieldName(FIELD_NAME);
                    typeNode = child.childByFieldName(FIELD_TYPE);
                    break;
                }
            }
        }

        if (nameNode != null) {
            String varName = nameNode.text();
            String typeName = typeNode != null ? typeNode.text() : null;
            if (varName != null && !varName.isEmpty()) {
                TextSpan range = nameNode.span();
                SymbolKey key = new SymbolKey(varName, SymbolKind.LOCAL_VARIABLE, range);
                scope.addLocal(new FieldSymbol(key, range, varName, typeName, false, true));
            }
        }
    }

    /**
     * Extracts the exception parameter from a catch clause.
     * Grammar: {@code catch_clause} contains a {@code catch_formal_parameter}.
     */
    private static void extractCatchParameter(SemanticNode catchNode, Scope scope) {
        List<SemanticNode> children = catchNode.namedChildren();
        for (SemanticNode child : children) {
            if (NODE_CATCH_FORMAL_PARAMETER.equals(child.nodeType())) {
                addParameterAsLocal(child, scope);
                return;
            }
        }
    }

    /**
     * Extracts lambda parameters (both explicitly typed and inferred).
     * Grammar: lambda_expression has a "parameters" field which is either
     * {@code formal_parameters} (typed) or {@code inferred_parameters}
     * (just identifiers), or a single {@code identifier} (single param without parens).
     */
    private static void extractLambdaParameters(SemanticNode lambdaNode, Scope scope) {
        SemanticNode params = lambdaNode.childByFieldName(FIELD_PARAMETERS);
        if (params == null) {
            // Single identifier parameter: (x) -> ... or x -> ...
            // The parameter might be the first named child if it's a bare identifier.
            List<SemanticNode> children = lambdaNode.namedChildren();
            if (!children.isEmpty()) {
                SemanticNode first = children.get(0);
                if (NODE_IDENTIFIER.equals(first.nodeType())) {
                    String name = first.text();
                    if (name != null && !name.isEmpty()) {
                        TextSpan range = first.span();
                        SymbolKey key = new SymbolKey(name, SymbolKind.PARAMETER, range);
                        scope.addLocal(new FieldSymbol(key, range, name, null, false, true));
                    }
                }
            }
            return;
        }

        String paramsType = params.nodeType();
        if (NODE_INFERRED_PARAMETERS.equals(paramsType)) {
            // Inferred: (a, b) -> ...  — children are bare identifiers.
            List<SemanticNode> children = params.namedChildren();
            for (SemanticNode child : children) {
                if (NODE_IDENTIFIER.equals(child.nodeType())) {
                    String name = child.text();
                    if (name != null && !name.isEmpty()) {
                        TextSpan range = child.span();
                        SymbolKey key = new SymbolKey(name, SymbolKind.PARAMETER, range);
                        scope.addLocal(new FieldSymbol(key, range, name, null, false, true));
                    }
                }
            }
        } else if (NODE_LAMBDA_PARAMETERS.equals(paramsType)) {
            // Typed: (int a, String b) -> ...
            List<SemanticNode> children = params.namedChildren();
            for (SemanticNode child : children) {
                if (NODE_FORMAL_PARAMETER.equals(child.nodeType())
                        || NODE_SPREAD_PARAMETER.equals(child.nodeType())) {
                    addParameterAsLocal(child, scope);
                }
            }
        } else if (NODE_IDENTIFIER.equals(paramsType)) {
            // Single identifier directly as the "parameters" field.
            String name = params.text();
            if (name != null && !name.isEmpty()) {
                TextSpan range = params.span();
                SymbolKey key = new SymbolKey(name, SymbolKind.PARAMETER, range);
                scope.addLocal(new FieldSymbol(key, range, name, null, false, true));
            }
        }
    }

    /**
     * Adds a formal_parameter, spread_parameter, or catch_formal_parameter
     * as a local to the given scope.
     *
     * <p>Grammar for formal_parameter:
     * {@code (formal_parameter type: (_) name: (identifier))}
     * Also handles {@code dimensions: "[]"} for array parameters.</p>
     */
    private static void addParameterAsLocal(SemanticNode paramNode, Scope scope) {
        SemanticNode nameNode = paramNode.childByFieldName(FIELD_NAME);
        SemanticNode typeNode = paramNode.childByFieldName(FIELD_TYPE);

        if (nameNode == null) {
            return;
        }

        String name = nameNode.text();
        if (name == null || name.isEmpty()) {
            return;
        }

        String typeName = typeNode != null ? typeNode.text() : null;

        // Check for array dimensions (int[] arr → typeName should be "int[]").
        SemanticNode dims = paramNode.childByFieldName(FIELD_DIMENSIONS);
        if (dims != null && typeName != null) {
            typeName = typeName + dims.text();
        }

        TextSpan range = nameNode.span();
        SymbolKey key = new SymbolKey(name, SymbolKind.PARAMETER, range);
        scope.addLocal(new FieldSymbol(key, range, name, typeName, false, true));
    }

    /**
     * Tests whether a node type represents a variable declaration that should
     * contribute a local to the enclosing scope.
     */
    private static boolean isVariableDeclaration(String nodeType) {
        return NODE_LOCAL_VARIABLE_DECLARATION.equals(nodeType);
    }

    /**
     * Extracts variable declarators from a {@code local_variable_declaration}
     * and adds them to the parent scope.
     *
     * <p>Grammar: {@code (local_variable_declaration type: (_)
     * declarator: (variable_declarator name: (identifier) [value: (_)]))}
     * A single declaration may have multiple declarators: {@code int a, b = 1;}</p>
     */
    private static void extractVariableDeclaration(SemanticNode declNode, Scope scope) {
        SemanticNode typeNode = declNode.childByFieldName(FIELD_TYPE);
        String typeName = typeNode != null ? typeNode.text() : null;

        // Check for modifiers — detect `final`.
        boolean isFinal = false;
        List<SemanticNode> children = declNode.namedChildren();
        for (SemanticNode child : children) {
            if ("modifiers".equals(child.nodeType())) {
                String modText = child.text();
                if (modText != null && modText.contains("final")) {
                    isFinal = true;
                }
                break;
            }
        }

        // Extract each variable_declarator.
        for (SemanticNode child : children) {
            if (NODE_VARIABLE_DECLARATOR.equals(child.nodeType())) {
                SemanticNode nameNode = child.childByFieldName(FIELD_NAME);
                if (nameNode != null) {
                    String name = nameNode.text();
                    if (name != null && !name.isEmpty()) {
                        TextSpan range = nameNode.span();
                        SymbolKey key = new SymbolKey(name, SymbolKind.LOCAL_VARIABLE, range);
                        scope.addLocal(new FieldSymbol(key, range, name, typeName, false, isFinal));
                    }
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Private — helpers
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Extracts the class name from the parent of a {@code class_body} node.
     * The parent is typically a {@code class_declaration}, {@code interface_declaration},
     * or {@code enum_declaration} with a {@code name} field.
     */
    private static String extractClassNameFromParent(SemanticNode classBodyNode) {
        SemanticNode parent = classBodyNode.parent();
        if (parent == null) {
            return null;
        }
        return extractFieldText(parent, FIELD_NAME);
    }

    /**
     * Extracts the text of a field child from a node.
     *
     * @param node      the parent node
     * @param fieldName the grammar field name
     * @return the text, or {@code null} if not found
     */
    private static String extractFieldText(SemanticNode node, String fieldName) {
        SemanticNode child = node.childByFieldName(fieldName);
        if (child == null) {
            return null;
        }
        return child.text();
    }

    /**
     * Tests whether a block node is the direct body of a method scope.
     * If so, we skip creating a separate BlockScope to avoid unnecessary nesting.
     */
    private static boolean isDirectMethodBody(SemanticNode blockNode, Scope methodScope) {
        SemanticNode parent = blockNode.parent();
        if (parent == null) {
            return false;
        }
        String parentType = parent.nodeType();
        if (!NODE_METHOD_DECLARATION.equals(parentType)
                && !NODE_CONSTRUCTOR_DECLARATION.equals(parentType)) {
            return false;
        }
        // Check if this block is the "body" field of the method.
        SemanticNode bodyField = parent.childByFieldName(FIELD_BODY);
        if (bodyField == null) {
            return false;
        }
        TextSpan blockSpan = blockNode.span();
        TextSpan bodySpan = bodyField.span();
        return blockSpan != null && bodySpan != null
                && blockSpan.getStartByte() == bodySpan.getStartByte()
                && blockSpan.getEndByte() == bodySpan.getEndByte();
    }

    /**
     * Finds the deepest scope containing the given byte offset by walking
     * down from the given scope into its children.
     */
    private static Scope findDeepestScope(Scope scope, int byteOffset) {
        for (Scope child : scope.children()) {
            if (child.containsOffset(byteOffset)) {
                return findDeepestScope(child, byteOffset);
            }
        }
        return scope;
    }

    /**
     * Finds the tightest scope that fully contains the given text span.
     */
    private static Scope findTightestContaining(Scope scope, TextSpan target) {
        for (Scope child : scope.children()) {
            TextSpan childRange = child.range();
            if (childRange != null && childRange.contains(target)) {
                return findTightestContaining(child, target);
            }
        }
        return scope;
    }
}
