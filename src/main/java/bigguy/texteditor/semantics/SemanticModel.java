package bigguy.texteditor.semantics;

import bigguy.texteditor.analysis.IncrementalStrategy;
import bigguy.texteditor.analysis.IncrementalStrategy.InvalidationScope;
import bigguy.texteditor.index.Symbol;
import bigguy.texteditor.index.Symbol.FieldSymbol;
import bigguy.texteditor.index.Symbol.ImportSymbol;
import bigguy.texteditor.index.Symbol.MethodSymbol;
import bigguy.texteditor.index.Symbol.TypeSymbol;
import bigguy.texteditor.index.SymbolKey;
import bigguy.texteditor.index.SymbolKind;
import bigguy.texteditor.syntax.DocumentSyntax;
import bigguy.texteditor.syntax.SemanticNode;
import bigguy.texteditor.treesitter.java.TextSpan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Composite coordinator that owns and orchestrates all semantic data structures.
 *
 * <p>This is the single entry point for semantic queries, replacing the scattered
 * field access on the legacy {@code ScriptDocument}. It owns four sub-components
 * and provides convenience query methods that delegate to them:</p>
 *
 * <pre>
 * SemanticModel
 *   ├── SymbolTable     — all declared symbols + indexed views
 *   ├── ScopeTree       — scope hierarchy + variable resolution
 *   ├── ImportIndex      — import resolution
 *   └── InferenceCache   — lambda/object literal caches
 * </pre>
 *
 * <p>The model follows IntelliJ IDEA's {@code FileViewProvider} pattern: a single
 * coordinator that lazily builds and caches multiple semantic views of the same
 * source file. Callers access sub-components directly for specialised queries,
 * or use the convenience methods on this class for common lookups.</p>
 *
 * <h3>Lifecycle</h3>
 * <ol>
 *   <li>Construct an empty model via {@link #SemanticModel()}.</li>
 *   <li>After parsing, call {@link #rebuild(DocumentSyntax)} for a full build.</li>
 *   <li>On subsequent edits, call
 *       {@link #incrementalUpdate(DocumentSyntax, InvalidationScope)} with the
 *       scope computed by {@link IncrementalStrategy}.</li>
 * </ol>
 *
 * <h3>Thread safety</h3>
 * <p>Not thread-safe. The orchestrator (background analysis thread) must
 * coordinate access. Sub-components have their own synchronization for
 * query-vs-mutation safety, but the model itself does not.</p>
 *
 * @see SymbolTable
 * @see ScopeTree
 * @see ImportIndex
 * @see InferenceCache
 */
public final class SemanticModel {

    // ── CST node type constants ────────────────────────────────────────────────

    private static final String NODE_IMPORT_DECLARATION = "import_declaration";
    private static final String NODE_CLASS_DECLARATION = "class_declaration";
    private static final String NODE_INTERFACE_DECLARATION = "interface_declaration";
    private static final String NODE_ENUM_DECLARATION = "enum_declaration";
    private static final String NODE_ANNOTATION_TYPE_DECLARATION = "annotation_type_declaration";
    private static final String NODE_METHOD_DECLARATION = "method_declaration";
    private static final String NODE_CONSTRUCTOR_DECLARATION = "constructor_declaration";
    private static final String NODE_FIELD_DECLARATION = "field_declaration";

    private static final String FIELD_NAME = "name";
    private static final String FIELD_BODY = "body";
    private static final String FIELD_TYPE = "type";
    private static final String FIELD_PARAMETERS = "parameters";

    private static final String NODE_FORMAL_PARAMETER = "formal_parameter";
    private static final String NODE_SPREAD_PARAMETER = "spread_parameter";
    private static final String NODE_VARIABLE_DECLARATOR = "variable_declarator";
    private static final String NODE_IDENTIFIER = "identifier";
    private static final String NODE_SCOPED_IDENTIFIER = "scoped_identifier";
    private static final String NODE_MODIFIERS = "modifiers";

    private static final String MODIFIER_STATIC = "static";
    private static final String MODIFIER_FINAL = "final";
    private static final String INIT_NAME = "<init>";

    /**
     * Node types that represent type declarations.
     */
    private static final Set<String> TYPE_DECLARATION_TYPES;

    static {
        Set<String> types = new HashSet<String>();
        types.add(NODE_CLASS_DECLARATION);
        types.add(NODE_INTERFACE_DECLARATION);
        types.add(NODE_ENUM_DECLARATION);
        types.add(NODE_ANNOTATION_TYPE_DECLARATION);
        TYPE_DECLARATION_TYPES = Collections.unmodifiableSet(types);
    }

    // ── Sub-components ─────────────────────────────────────────────────────────

    private final SymbolTable symbolTable;
    private ScopeTree scopeTree;
    private final ImportIndex importIndex;
    private final InferenceCache inferenceCache;

    // ── State ──────────────────────────────────────────────────────────────────

    private boolean built;
    private int compositeVersion;

    // ── Constructor ────────────────────────────────────────────────────────────

    /**
     * Creates an empty semantic model with fresh sub-components.
     *
     * <p>The model starts in an unbuilt state ({@link #isBuilt()} returns
     * {@code false}). Call {@link #rebuild(DocumentSyntax)} after the first
     * parse to populate the semantic data structures.</p>
     */
    public SemanticModel() {
        this.symbolTable = new SymbolTable();
        this.scopeTree = null;
        this.importIndex = new ImportIndex();
        this.inferenceCache = new InferenceCache();
        this.built = false;
        this.compositeVersion = 0;
    }

    // ── Component access ───────────────────────────────────────────────────────

    /**
     * Returns the symbol table containing all declared symbols and indexed views.
     *
     * @return the symbol table (never {@code null})
     */
    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    /**
     * Returns the scope tree for variable resolution and scope walking.
     *
     * @return the scope tree, or {@code null} if the model has not been built yet
     */
    public ScopeTree getScopeTree() {
        return scopeTree;
    }

    /**
     * Returns the import index for import resolution.
     *
     * @return the import index (never {@code null})
     */
    public ImportIndex getImportIndex() {
        return importIndex;
    }

    /**
     * Returns the inference cache for lambda and object literal type caches.
     *
     * @return the inference cache (never {@code null})
     */
    public InferenceCache getInferenceCache() {
        return inferenceCache;
    }

    // ── Lifecycle ──────────────────────────────────────────────────────────────

    /**
     * Rebuilds the entire semantic model from a parsed CST.
     *
     * <p>Called from the background analysis thread after syntax parsing.
     * This method performs a complete rebuild of all sub-components:</p>
     * <ol>
     *   <li>Clear all sub-components</li>
     *   <li>Walk CST to extract imports → populate ImportIndex</li>
     *   <li>Walk CST to extract declarations → populate SymbolTable</li>
     *   <li>Build ScopeTree from CST</li>
     *   <li>Set source version on InferenceCache</li>
     * </ol>
     *
     * @param syntax the document syntax produced by the parser
     * @throws IllegalArgumentException if {@code syntax} is {@code null}
     */
    public void rebuild(DocumentSyntax syntax) {
        if (syntax == null) {
            throw new IllegalArgumentException("syntax must not be null");
        }

        clear();

        inferenceCache.setSourceVersion(syntax.getSourceVersion());

        SemanticNode root = syntax.root();
        if (root == null) {
            built = true;
            compositeVersion++;
            return;
        }

        extractImports(root);
        extractDeclarations(root, "", "");

        scopeTree = ScopeTree.buildFromCST(syntax);

        built = true;
        compositeVersion++;
    }

    /**
     * Performs an incremental update based on the invalidation scope.
     *
     * <p>Only rebuilds the sub-components that are affected by the change,
     * as determined by the {@link InvalidationScope}:</p>
     * <ul>
     *   <li>{@link InvalidationScope#NONE NONE} — no-op (only offset adjustment needed)</li>
     *   <li>{@link InvalidationScope#LOCAL_SCOPE LOCAL_SCOPE} — rebuild scope tree only</li>
     *   <li>{@link InvalidationScope#DECLARATION DECLARATION} — full rebuild</li>
     *   <li>{@link InvalidationScope#IMPORTS IMPORTS} — rebuild imports only</li>
     *   <li>{@link InvalidationScope#STRUCTURAL STRUCTURAL} — full rebuild</li>
     * </ul>
     *
     * @param syntax the updated document syntax after reparse
     * @param scope  the invalidation scope from {@link IncrementalStrategy}
     * @throws IllegalArgumentException if either argument is {@code null}
     */
    public void incrementalUpdate(DocumentSyntax syntax, InvalidationScope scope) {
        if (syntax == null) {
            throw new IllegalArgumentException("syntax must not be null");
        }
        if (scope == null) {
            throw new IllegalArgumentException("scope must not be null");
        }

        inferenceCache.setSourceVersion(syntax.getSourceVersion());

        switch (scope) {
            case NONE:
                break;

            case LOCAL_SCOPE:
                scopeTree = ScopeTree.buildFromCST(syntax);
                compositeVersion++;
                break;

            case DECLARATION:
                rebuild(syntax);
                break;

            case IMPORTS:
                rebuildImports(syntax);
                compositeVersion++;
                break;

            case STRUCTURAL:
                rebuild(syntax);
                break;

            default:
                rebuild(syntax);
                break;
        }
    }

    // ── Convenience queries ────────────────────────────────────────────────────

    /**
     * Resolves a variable name at the given byte offset via scope walking.
     *
     * <p>Delegates to {@link ScopeTree#resolveVariable(String, int)} to
     * walk the scope chain from the innermost scope containing the offset
     * outward until a matching variable declaration is found.</p>
     *
     * @param name       the variable name to resolve
     * @param byteOffset the byte offset in the source where the reference occurs
     * @return an {@link Optional} containing the resolved field symbol,
     *         or empty if no variable with that name is visible at the offset
     */
    public Optional<FieldSymbol> resolveVariable(String name, int byteOffset) {
        if (name == null || scopeTree == null) {
            return Optional.empty();
        }
        FieldSymbol result = scopeTree.resolveVariable(name, byteOffset);
        return result != null ? Optional.of(result) : Optional.<FieldSymbol>empty();
    }

    /**
     * Resolves a type by simple name.
     *
     * <p>First checks the {@link SymbolTable} for locally declared types,
     * then falls back to the {@link ImportIndex} to check if the type was
     * imported. If found via import, creates a synthetic {@link TypeSymbol}
     * from the import information.</p>
     *
     * @param simpleName the unqualified type name to resolve
     * @return an {@link Optional} containing the resolved type symbol,
     *         or empty if the type is not declared or imported
     */
    public Optional<TypeSymbol> resolveType(String simpleName) {
        if (simpleName == null) {
            return Optional.empty();
        }

        List<TypeSymbol> localTypes = symbolTable.getTypesBySimpleName(simpleName);
        if (!localTypes.isEmpty()) {
            return Optional.of(localTypes.get(0));
        }

        ImportIndex.ImportEntry importEntry = importIndex.resolveBySimpleName(simpleName);
        if (importEntry != null) {
            TextSpan range = importEntry.getRange();
            if (range == null) {
                // Implicit imports have no source location — use a zero-length synthetic span.
                range = new TextSpan(0, 0, 0, 0, 0, 0);
            }
            SymbolKey key = new SymbolKey(simpleName, SymbolKind.CLASS, range);
            TypeSymbol synthetic = new TypeSymbol(
                    key, range, simpleName,
                    importEntry.getFullPath(),
                    simpleName
            );
            return Optional.of(synthetic);
        }

        return Optional.empty();
    }

    /**
     * Returns all method symbols with the given name.
     *
     * <p>Delegates to {@link SymbolTable#getMethodsByName(String)}.</p>
     *
     * @param name the method name to look up
     * @return an unmodifiable list of matching method symbols (never {@code null})
     */
    public List<MethodSymbol> resolveMethods(String name) {
        if (name == null) {
            return Collections.emptyList();
        }
        return symbolTable.getMethodsByName(name);
    }

    /**
     * Checks whether a byte offset falls inside an excluded region
     * (string literal or comment).
     *
     * <p>This is a convenience method. Since exclusion data lives on the
     * {@link DocumentSyntax} (via its {@code ExcludedRangeIndex}), callers
     * should use {@link DocumentSyntax#isExcluded(int)} directly when they
     * have a syntax reference. This method exists for cases where the
     * last-known syntax snapshot is not readily available.</p>
     *
     * @param byteOffset the byte offset to test (0-based)
     * @param syntax     the document syntax to query for exclusion data
     * @return {@code true} if the offset is inside a string or comment
     */
    public boolean isExcluded(int byteOffset, DocumentSyntax syntax) {
        if (syntax == null) {
            return false;
        }
        return syntax.isExcluded(byteOffset);
    }

    // ── Version tracking ───────────────────────────────────────────────────────

    /**
     * Returns the composite version number.
     *
     * <p>This version is incremented on every rebuild or incremental update.
     * Consumers can compare versions to detect staleness without inspecting
     * individual sub-component versions.</p>
     *
     * @return the composite version number
     */
    public int version() {
        return compositeVersion;
    }

    /**
     * Returns whether the model has been built at least once.
     *
     * <p>Before the first {@link #rebuild(DocumentSyntax)} call, queries
     * will return empty results. Callers should check this flag before
     * relying on semantic data.</p>
     *
     * @return {@code true} after at least one successful rebuild
     */
    public boolean isBuilt() {
        return built;
    }

    // ── Reset ──────────────────────────────────────────────────────────────────

    /**
     * Clears all sub-components and resets the model to an unbuilt state.
     *
     * <p>After calling this method, {@link #isBuilt()} returns {@code false}
     * and all queries return empty results until {@link #rebuild(DocumentSyntax)}
     * is called again.</p>
     */
    public void clear() {
        symbolTable.clear();
        scopeTree = null;
        importIndex.clear();
        inferenceCache.invalidateAll();
        built = false;
    }

    // ── Private — import extraction ────────────────────────────────────────────

    /**
     * Walks the root node's direct children to find and extract all import
     * declarations, populating the ImportIndex.
     */
    private void extractImports(SemanticNode root) {
        List<SemanticNode> children = root.namedChildren();
        for (SemanticNode child : children) {
            String nodeType = child.nodeType();
            if (NODE_IMPORT_DECLARATION.equals(nodeType)) {
                processImportDeclaration(child);
            }
        }
    }

    /**
     * Processes a single import_declaration CST node and adds the result
     * to both the ImportIndex and SymbolTable.
     */
    private void processImportDeclaration(SemanticNode importNode) {
        TextSpan range = importNode.span();
        if (range == null) {
            return;
        }

        boolean isStatic = hasStaticModifier(importNode);
        String rawPath = extractImportPath(importNode);
        if (rawPath == null || rawPath.isEmpty()) {
            return;
        }

        ImportIndex.ImportEntry entry = ImportIndex.ImportEntry.fromFullPath(
                rawPath, isStatic, false, range
        );
        importIndex.addImport(entry);

        String simpleName = entry.getSimpleName();
        boolean isWildcard = entry.isWildcard();
        String fullPath = entry.getFullPath();

        SymbolKey key = new SymbolKey(simpleName, SymbolKind.IMPORT, range);
        ImportSymbol symbol = new ImportSymbol(
                key, range, simpleName, fullPath, isWildcard, isStatic
        );
        symbolTable.addSymbol(symbol);
    }

    /**
     * Extracts the full import path from an import_declaration node.
     *
     * <p>Handles both regular imports ({@code import java.util.List;}) and
     * wildcard imports ({@code import java.util.*;}) by walking the
     * scoped_identifier chain.</p>
     */
    private String extractImportPath(SemanticNode importNode) {
        List<SemanticNode> children = importNode.namedChildren();
        for (SemanticNode child : children) {
            String type = child.nodeType();
            if (NODE_SCOPED_IDENTIFIER.equals(type) || NODE_IDENTIFIER.equals(type)) {
                String text = child.text();
                if (text != null) {
                    return text.trim();
                }
            }
        }

        String fullText = importNode.text();
        if (fullText != null) {
            fullText = fullText.trim();
            if (fullText.startsWith("import ")) {
                fullText = fullText.substring("import ".length()).trim();
            }
            if (fullText.startsWith("static ")) {
                fullText = fullText.substring("static ".length()).trim();
            }
            if (fullText.endsWith(";")) {
                fullText = fullText.substring(0, fullText.length() - 1).trim();
            }
            return fullText;
        }

        return null;
    }

    /**
     * Checks whether an import_declaration node has a "static" keyword.
     */
    private boolean hasStaticModifier(SemanticNode importNode) {
        String text = importNode.text();
        if (text == null) {
            return false;
        }
        return text.trim().startsWith("import static ");
    }

    // ── Private — declaration extraction ───────────────────────────────────────

    /**
     * Recursively walks the CST starting from the given node, extracting
     * type, method, and field declarations and adding them to the SymbolTable.
     *
     * @param node       the current CST node to inspect
     * @param parentFqn  the fully qualified name of the enclosing type (empty string at top level)
     * @param parentDot  the dot-path from enclosing scope (empty string at top level)
     */
    private void extractDeclarations(SemanticNode node, String parentFqn, String parentDot) {
        List<SemanticNode> children = node.namedChildren();
        for (SemanticNode child : children) {
            String nodeType = child.nodeType();
            if (nodeType == null) {
                continue;
            }

            if (TYPE_DECLARATION_TYPES.contains(nodeType)) {
                processTypeDeclaration(child, nodeType, parentFqn, parentDot);
            } else if (NODE_METHOD_DECLARATION.equals(nodeType)) {
                processMethodDeclaration(child, false);
            } else if (NODE_CONSTRUCTOR_DECLARATION.equals(nodeType)) {
                processMethodDeclaration(child, true);
            } else if (NODE_FIELD_DECLARATION.equals(nodeType)) {
                processFieldDeclaration(child);
            }
        }
    }

    /**
     * Processes a type declaration (class, interface, enum, annotation) and
     * recurses into its body for member declarations.
     */
    private void processTypeDeclaration(SemanticNode typeNode, String nodeType,
                                        String parentFqn, String parentDot) {
        SemanticNode nameNode = typeNode.childByFieldName(FIELD_NAME);
        if (nameNode == null) {
            return;
        }

        String simpleName = nameNode.text();
        if (simpleName == null || simpleName.isEmpty()) {
            return;
        }

        String fullName = parentFqn.isEmpty() ? simpleName : parentFqn + "." + simpleName;
        String dotName = parentDot.isEmpty() ? simpleName : parentDot + "." + simpleName;

        TextSpan range = typeNode.span();
        if (range == null) {
            return;
        }

        SymbolKind kind = symbolKindForTypeNode(nodeType);
        SymbolKey key = new SymbolKey(simpleName, kind, range);
        TypeSymbol symbol = new TypeSymbol(key, range, simpleName, fullName, dotName);
        symbolTable.addSymbol(symbol);

        SemanticNode body = typeNode.childByFieldName(FIELD_BODY);
        if (body != null) {
            extractDeclarations(body, fullName, dotName);
        }
    }

    /**
     * Processes a method or constructor declaration node.
     */
    private void processMethodDeclaration(SemanticNode methodNode, boolean isConstructor) {
        String methodName;
        if (isConstructor) {
            SemanticNode nameNode = methodNode.childByFieldName(FIELD_NAME);
            methodName = (nameNode != null && nameNode.text() != null)
                    ? nameNode.text() : INIT_NAME;
        } else {
            SemanticNode nameNode = methodNode.childByFieldName(FIELD_NAME);
            if (nameNode == null) {
                return;
            }
            methodName = nameNode.text();
            if (methodName == null || methodName.isEmpty()) {
                return;
            }
        }

        TextSpan range = methodNode.span();
        if (range == null) {
            return;
        }

        TextSpan bodyRange = null;
        SemanticNode bodyNode = methodNode.childByFieldName(FIELD_BODY);
        if (bodyNode != null) {
            bodyRange = bodyNode.span();
        }

        List<String> parameterNames = extractParameterNames(methodNode);

        SymbolKind kind = isConstructor ? SymbolKind.CONSTRUCTOR : SymbolKind.METHOD;
        SymbolKey key = new SymbolKey(methodName, kind, range);
        MethodSymbol symbol = new MethodSymbol(key, range, methodName, bodyRange, parameterNames);
        symbolTable.addSymbol(symbol);
    }

    /**
     * Extracts parameter names from a method or constructor declaration.
     */
    private List<String> extractParameterNames(SemanticNode methodNode) {
        SemanticNode params = methodNode.childByFieldName(FIELD_PARAMETERS);
        if (params == null) {
            return Collections.emptyList();
        }

        List<SemanticNode> paramChildren = params.namedChildren();
        if (paramChildren.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> names = new ArrayList<String>(paramChildren.size());
        for (SemanticNode param : paramChildren) {
            String paramType = param.nodeType();
            if (NODE_FORMAL_PARAMETER.equals(paramType) || NODE_SPREAD_PARAMETER.equals(paramType)) {
                SemanticNode nameNode = param.childByFieldName(FIELD_NAME);
                if (nameNode != null && nameNode.text() != null) {
                    names.add(nameNode.text());
                }
            }
        }
        return names;
    }

    /**
     * Processes a field declaration node. Handles multi-declarator fields
     * (e.g., {@code int x, y, z;}) by extracting each declarator's name.
     */
    private void processFieldDeclaration(SemanticNode fieldNode) {
        TextSpan fieldRange = fieldNode.span();
        if (fieldRange == null) {
            return;
        }

        String typeName = extractFieldTypeName(fieldNode);
        boolean isStatic = hasModifier(fieldNode, MODIFIER_STATIC);
        boolean isFinal = hasModifier(fieldNode, MODIFIER_FINAL);

        List<SemanticNode> declarators = fieldNode.findAll(NODE_VARIABLE_DECLARATOR);
        if (declarators.isEmpty()) {
            SemanticNode nameNode = fieldNode.childByFieldName(FIELD_NAME);
            if (nameNode != null && nameNode.text() != null) {
                String name = nameNode.text();
                SymbolKey key = new SymbolKey(name, SymbolKind.FIELD, fieldRange);
                FieldSymbol symbol = new FieldSymbol(key, fieldRange, name, typeName, isStatic, isFinal);
                symbolTable.addSymbol(symbol);
            }
            return;
        }

        for (SemanticNode declarator : declarators) {
            SemanticNode nameNode = declarator.childByFieldName(FIELD_NAME);
            if (nameNode == null) {
                continue;
            }
            String name = nameNode.text();
            if (name == null || name.isEmpty()) {
                continue;
            }

            TextSpan declRange = declarator.span();
            if (declRange == null) {
                declRange = fieldRange;
            }

            SymbolKey key = new SymbolKey(name, SymbolKind.FIELD, declRange);
            FieldSymbol symbol = new FieldSymbol(key, declRange, name, typeName, isStatic, isFinal);
            symbolTable.addSymbol(symbol);
        }
    }

    /**
     * Extracts the type name from a field declaration node.
     */
    private String extractFieldTypeName(SemanticNode fieldNode) {
        SemanticNode typeNode = fieldNode.childByFieldName(FIELD_TYPE);
        if (typeNode != null) {
            String text = typeNode.text();
            if (text != null) {
                return text.trim();
            }
        }
        return null;
    }

    /**
     * Checks whether a declaration node has a specific modifier (e.g., "static", "final").
     */
    private boolean hasModifier(SemanticNode declarationNode, String modifier) {
        SemanticNode modifiers = declarationNode.childByFieldName(NODE_MODIFIERS);
        if (modifiers == null) {
            List<SemanticNode> children = declarationNode.namedChildren();
            for (SemanticNode child : children) {
                if (NODE_MODIFIERS.equals(child.nodeType())) {
                    modifiers = child;
                    break;
                }
            }
        }

        if (modifiers == null) {
            return false;
        }

        String modText = modifiers.text();
        if (modText != null) {
            return modText.contains(modifier);
        }

        return false;
    }

    // ── Private — import-only rebuild ──────────────────────────────────────────

    /**
     * Rebuilds only the ImportIndex from the current syntax, leaving the
     * SymbolTable and ScopeTree intact. Used for
     * {@link InvalidationScope#IMPORTS} incremental updates.
     */
    private void rebuildImports(DocumentSyntax syntax) {
        importIndex.clear();

        removeImportSymbolsFromTable();

        SemanticNode root = syntax.root();
        if (root != null) {
            extractImports(root);
        }
    }

    /**
     * Removes all import symbols from the SymbolTable.
     * Called before re-extracting imports so the table doesn't accumulate stale entries.
     */
    private void removeImportSymbolsFromTable() {
        List<Symbol> allSymbols = symbolTable.getAllSymbols();
        List<Symbol> nonImports = new ArrayList<Symbol>(allSymbols.size());
        for (Symbol sym : allSymbols) {
            if (sym.kind() != SymbolKind.IMPORT) {
                nonImports.add(sym);
            }
        }
        symbolTable.replaceAll(nonImports);
    }

    // ── Private — utility ──────────────────────────────────────────────────────

    /**
     * Maps a tree-sitter type declaration node type to the corresponding SymbolKind.
     */
    private static SymbolKind symbolKindForTypeNode(String nodeType) {
        if (NODE_CLASS_DECLARATION.equals(nodeType)) {
            return SymbolKind.CLASS;
        }
        if (NODE_INTERFACE_DECLARATION.equals(nodeType)) {
            return SymbolKind.INTERFACE;
        }
        if (NODE_ENUM_DECLARATION.equals(nodeType)) {
            return SymbolKind.ENUM;
        }
        if (NODE_ANNOTATION_TYPE_DECLARATION.equals(nodeType)) {
            return SymbolKind.ANNOTATION;
        }
        return SymbolKind.CLASS;
    }

    @Override
    public String toString() {
        return "SemanticModel{"
                + "built=" + built
                + ", version=" + compositeVersion
                + ", symbols=" + symbolTable.size()
                + ", imports=" + importIndex.size()
                + ", scopeTree=" + (scopeTree != null ? "present" : "absent")
                + '}';
    }
}
