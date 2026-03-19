package bigguy.texteditor.analysis;

import bigguy.texteditor.index.Symbol;
import bigguy.texteditor.index.Symbol.FieldSymbol;
import bigguy.texteditor.index.Symbol.ImportSymbol;
import bigguy.texteditor.index.Symbol.MethodSymbol;
import bigguy.texteditor.index.Symbol.TypeSymbol;
import bigguy.texteditor.index.SymbolKind;
import bigguy.texteditor.render.Mark;
import bigguy.texteditor.semantics.SemanticModel;
import bigguy.texteditor.semantics.SymbolTable;
import bigguy.texteditor.syntax.DocumentSyntax;
import bigguy.texteditor.syntax.SemanticNode;
import bigguy.texteditor.treesitter.java.TextSpan;
import noppes.npcs.client.gui.util.script.interpreter.token.TokenType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Translates the semantic model (symbols extracted from the CST) into {@link Mark}
 * objects that the render pipeline can merge with syntax marks.
 *
 * <p>This builder bridges semantic analysis and visual rendering. It walks the CST
 * via {@link DocumentSyntax} to locate the <em>name</em> portion of each declaration,
 * then creates a {@link Mark#semantic(int, int, TokenType, Object) semantic mark}
 * with the correct {@link TokenType} and the originating {@link Symbol} as metadata.
 * Attaching the symbol enables downstream features like hover tooltips and
 * go-to-definition.</p>
 *
 * <h3>Why walk the CST instead of using Symbol ranges?</h3>
 * <p>{@link Symbol#range()} covers the <em>entire</em> declaration (e.g., the full
 * {@code public class Foo { ... }} block for a {@link TypeSymbol}). Highlighting
 * the full range would incorrectly color keywords, punctuation, and body content.
 * By walking the CST and extracting each declaration's "name" child node, we
 * highlight only the identifier — matching what users expect from a modern IDE.</p>
 *
 * <h3>Symbol → TokenType mapping</h3>
 * <table>
 *   <tr><th>Symbol type</th><th>Condition</th><th>TokenType</th></tr>
 *   <tr><td>{@link TypeSymbol}</td><td>{@link SymbolKind#CLASS}</td>
 *       <td>{@link TokenType#CLASS_DECL}</td></tr>
 *   <tr><td>{@link TypeSymbol}</td><td>{@link SymbolKind#INTERFACE}</td>
 *       <td>{@link TokenType#INTERFACE_DECL}</td></tr>
 *   <tr><td>{@link TypeSymbol}</td><td>{@link SymbolKind#ENUM}</td>
 *       <td>{@link TokenType#ENUM_DECL}</td></tr>
 *   <tr><td>{@link TypeSymbol}</td><td>{@link SymbolKind#ANNOTATION}</td>
 *       <td>{@link TokenType#JSDOC_TAG}</td></tr>
 *   <tr><td>{@link MethodSymbol}</td><td>any</td>
 *       <td>{@link TokenType#METHOD_DECL}</td></tr>
 *   <tr><td>{@link FieldSymbol}</td><td>static &amp; final</td>
 *       <td>{@link TokenType#STATIC_FINAL_FIELD}</td></tr>
 *   <tr><td>{@link FieldSymbol}</td><td>otherwise</td>
 *       <td>{@link TokenType#GLOBAL_FIELD}</td></tr>
 *   <tr><td>{@link ImportSymbol}</td><td>always</td>
 *       <td>{@link TokenType#IMPORTED_CLASS}</td></tr>
 * </table>
 *
 * <h3>Thread safety</h3>
 * <p>This class is stateless — all state lives in the method parameters.
 * It is safe to call {@link #build(SemanticModel, DocumentSyntax)} from any
 * thread, provided the model and syntax are not being concurrently mutated.</p>
 *
 * @see Mark#semantic(int, int, TokenType, Object)
 * @see SemanticModel
 * @see SymbolTable
 */
public final class SemanticMarkBuilder {

    // ── CST node type constants (mirror SemanticModel's constants) ──────────

    private static final String NODE_IMPORT_DECLARATION = "import_declaration";
    private static final String NODE_CLASS_DECLARATION = "class_declaration";
    private static final String NODE_INTERFACE_DECLARATION = "interface_declaration";
    private static final String NODE_ENUM_DECLARATION = "enum_declaration";
    private static final String NODE_ANNOTATION_TYPE_DECLARATION = "annotation_type_declaration";
    private static final String NODE_METHOD_DECLARATION = "method_declaration";
    private static final String NODE_CONSTRUCTOR_DECLARATION = "constructor_declaration";
    private static final String NODE_FIELD_DECLARATION = "field_declaration";
    private static final String NODE_VARIABLE_DECLARATOR = "variable_declarator";
    private static final String NODE_SCOPED_IDENTIFIER = "scoped_identifier";
    private static final String NODE_IDENTIFIER = "identifier";

    private static final String FIELD_NAME = "name";

    /**
     * All CST node types that represent type declarations.
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

    private SemanticMarkBuilder() {
        // Utility class — no instances
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Public API
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Generates semantic marks from the given model and syntax tree.
     *
     * <p>Walks all declaration nodes in the CST, looks up the corresponding
     * {@link Symbol} in the {@link SymbolTable}, and creates a
     * {@link Mark#semantic(int, int, TokenType, Object)} for each declaration's
     * <em>name</em> node. Only the name identifier is highlighted — keywords,
     * modifiers, and body content are left to tree-sitter's syntax marks.</p>
     *
     * <p>The returned list is unsorted. Callers (typically {@code MarkLayer})
     * should sort the marks before merging them with syntax marks.</p>
     *
     * @param model  the populated semantic model (may be {@code null})
     * @param syntax the document syntax produced by the parser (may be {@code null})
     * @return a mutable list of semantic marks, never {@code null}; empty if the
     *         model is not built or either argument is {@code null}
     */
    public static List<Mark> build(SemanticModel model, DocumentSyntax syntax) {
        if (model == null || syntax == null || !model.isBuilt()) {
            return Collections.emptyList();
        }

        SymbolTable table = model.getSymbolTable();
        if (table.isEmpty()) {
            return Collections.emptyList();
        }

        SemanticNode root = syntax.root();
        if (root == null) {
            return Collections.emptyList();
        }

        List<Mark> marks = new ArrayList<Mark>();
        walkDeclarations(root, table, marks);
        return marks;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CST walking
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Recursively walks the CST, visiting declaration nodes and creating marks
     * for each declaration's name identifier.
     *
     * @param node  the current CST node
     * @param table the symbol table to look up symbols
     * @param marks the output list to append marks to
     */
    private static void walkDeclarations(SemanticNode node, SymbolTable table, List<Mark> marks) {
        List<SemanticNode> children = node.namedChildren();
        for (SemanticNode child : children) {
            String nodeType = child.nodeType();
            if (nodeType == null) {
                continue;
            }

            if (TYPE_DECLARATION_TYPES.contains(nodeType)) {
                processTypeDeclaration(child, nodeType, table, marks);
            } else if (NODE_METHOD_DECLARATION.equals(nodeType)
                    || NODE_CONSTRUCTOR_DECLARATION.equals(nodeType)) {
                processMethodDeclaration(child, table, marks);
            } else if (NODE_FIELD_DECLARATION.equals(nodeType)) {
                processFieldDeclaration(child, table, marks);
            } else if (NODE_IMPORT_DECLARATION.equals(nodeType)) {
                processImportDeclaration(child, table, marks);
            } else {
                // Recurse into non-declaration nodes (e.g., class_body, block)
                // to find nested declarations.
                walkDeclarations(child, table, marks);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Declaration processors
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Processes a type declaration (class, interface, enum, annotation).
     * Creates a mark for the type's name identifier and recurses into the body
     * for nested declarations.
     *
     * @param typeNode the CST node for the type declaration
     * @param nodeType the node type string (e.g., "class_declaration")
     * @param table    the symbol table
     * @param marks    the output list
     */
    private static void processTypeDeclaration(SemanticNode typeNode, String nodeType,
                                               SymbolTable table, List<Mark> marks) {
        SemanticNode nameNode = typeNode.childByFieldName(FIELD_NAME);
        if (nameNode == null) {
            return;
        }

        String simpleName = nameNode.text();
        if (simpleName == null || simpleName.isEmpty()) {
            return;
        }

        TextSpan nameSpan = nameNode.span();
        if (nameSpan == null || nameSpan.getByteLength() <= 0) {
            return;
        }

        // Look up the symbol by simple name to get the exact symbol instance.
        TypeSymbol symbol = findTypeSymbol(table, simpleName, typeNode.span());
        TokenType tokenType = mapTypeNodeToTokenType(nodeType);

        marks.add(Mark.semantic(
                nameSpan.getStartByte(),
                nameSpan.getEndByte(),
                tokenType,
                symbol
        ));

        // Recurse into the body for nested type/method/field declarations.
        SemanticNode body = typeNode.childByFieldName("body");
        if (body != null) {
            walkDeclarations(body, table, marks);
        }
    }

    /**
     * Processes a method or constructor declaration. Creates a mark for the
     * method's name identifier.
     *
     * @param methodNode the CST node for the method/constructor declaration
     * @param table      the symbol table
     * @param marks      the output list
     */
    private static void processMethodDeclaration(SemanticNode methodNode,
                                                 SymbolTable table, List<Mark> marks) {
        SemanticNode nameNode = methodNode.childByFieldName(FIELD_NAME);
        if (nameNode == null) {
            return;
        }

        String methodName = nameNode.text();
        if (methodName == null || methodName.isEmpty()) {
            return;
        }

        TextSpan nameSpan = nameNode.span();
        if (nameSpan == null || nameSpan.getByteLength() <= 0) {
            return;
        }

        MethodSymbol symbol = findMethodSymbol(table, methodName, methodNode.span());

        marks.add(Mark.semantic(
                nameSpan.getStartByte(),
                nameSpan.getEndByte(),
                TokenType.METHOD_DECL,
                symbol
        ));

        // Recurse into the method body for local declarations (e.g., anonymous classes).
        SemanticNode body = methodNode.childByFieldName("body");
        if (body != null) {
            walkDeclarations(body, table, marks);
        }
    }

    /**
     * Processes a field declaration. Handles multi-declarator fields
     * (e.g., {@code int x, y, z;}) by extracting each variable declarator's
     * name and creating a separate mark for each.
     *
     * @param fieldNode the CST node for the field declaration
     * @param table     the symbol table
     * @param marks     the output list
     */
    private static void processFieldDeclaration(SemanticNode fieldNode,
                                                SymbolTable table, List<Mark> marks) {
        // Multi-declarator fields: "int x, y, z;"
        List<SemanticNode> declarators = fieldNode.findAll(NODE_VARIABLE_DECLARATOR);

        if (!declarators.isEmpty()) {
            for (SemanticNode declarator : declarators) {
                SemanticNode nameNode = declarator.childByFieldName(FIELD_NAME);
                if (nameNode == null) {
                    continue;
                }

                String fieldName = nameNode.text();
                if (fieldName == null || fieldName.isEmpty()) {
                    continue;
                }

                TextSpan nameSpan = nameNode.span();
                if (nameSpan == null || nameSpan.getByteLength() <= 0) {
                    continue;
                }

                FieldSymbol symbol = findFieldSymbol(table, fieldName, fieldNode.span());
                TokenType tokenType = mapFieldSymbol(symbol);

                marks.add(Mark.semantic(
                        nameSpan.getStartByte(),
                        nameSpan.getEndByte(),
                        tokenType,
                        symbol
                ));
            }
        } else {
            // Fallback: single declarator without variable_declarator wrapper.
            SemanticNode nameNode = fieldNode.childByFieldName(FIELD_NAME);
            if (nameNode == null) {
                return;
            }

            String fieldName = nameNode.text();
            if (fieldName == null || fieldName.isEmpty()) {
                return;
            }

            TextSpan nameSpan = nameNode.span();
            if (nameSpan == null || nameSpan.getByteLength() <= 0) {
                return;
            }

            FieldSymbol symbol = findFieldSymbol(table, fieldName, fieldNode.span());
            TokenType tokenType = mapFieldSymbol(symbol);

            marks.add(Mark.semantic(
                    nameSpan.getStartByte(),
                    nameSpan.getEndByte(),
                    tokenType,
                    symbol
            ));
        }
    }

    /**
     * Processes an import declaration. Extracts the last identifier segment
     * (the simple class name) from the import path and creates a mark for it.
     *
     * <p>For a scoped identifier like {@code java.util.List}, the mark covers
     * the entire scoped identifier node, matching how tree-sitter structures
     * the import path. For wildcard imports ({@code java.util.*}), the entire
     * scoped path is marked.</p>
     *
     * @param importNode the CST node for the import declaration
     * @param table      the symbol table
     * @param marks      the output list
     */
    private static void processImportDeclaration(SemanticNode importNode,
                                                 SymbolTable table, List<Mark> marks) {
        // Find the scoped_identifier or identifier child — this is the import path.
        SemanticNode pathNode = findImportPathNode(importNode);
        if (pathNode == null) {
            return;
        }

        TextSpan pathSpan = pathNode.span();
        if (pathSpan == null || pathSpan.getByteLength() <= 0) {
            return;
        }

        // Look up the import symbol by matching the full import range.
        ImportSymbol symbol = findImportSymbol(table, importNode.span());

        marks.add(Mark.semantic(
                pathSpan.getStartByte(),
                pathSpan.getEndByte(),
                TokenType.IMPORTED_CLASS,
                symbol
        ));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Symbol lookup helpers
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Finds the {@link TypeSymbol} in the table matching the given name and
     * whose declaration range overlaps the given CST node span.
     *
     * <p>If multiple types share the same simple name (e.g., nested classes),
     * the one whose range contains the declaration node's range wins.</p>
     *
     * @param table      the symbol table
     * @param simpleName the type's simple name
     * @param declSpan   the full declaration span from the CST node
     * @return the matching symbol, or {@code null} if not found
     */
    private static TypeSymbol findTypeSymbol(SymbolTable table, String simpleName,
                                             TextSpan declSpan) {
        List<TypeSymbol> candidates = table.getTypesBySimpleName(simpleName);
        if (candidates.isEmpty()) {
            return null;
        }
        if (candidates.size() == 1) {
            return candidates.get(0);
        }
        // Disambiguate by declaration range overlap.
        if (declSpan != null) {
            for (TypeSymbol ts : candidates) {
                if (spansOverlap(ts.range(), declSpan)) {
                    return ts;
                }
            }
        }
        return candidates.get(0);
    }

    /**
     * Finds the {@link MethodSymbol} in the table matching the given name and
     * whose declaration range overlaps the given CST node span.
     *
     * @param table      the symbol table
     * @param methodName the method name
     * @param declSpan   the full declaration span from the CST node
     * @return the matching symbol, or {@code null} if not found
     */
    private static MethodSymbol findMethodSymbol(SymbolTable table, String methodName,
                                                 TextSpan declSpan) {
        List<MethodSymbol> candidates = table.getMethodsByName(methodName);
        if (candidates.isEmpty()) {
            return null;
        }
        if (candidates.size() == 1) {
            return candidates.get(0);
        }
        if (declSpan != null) {
            for (MethodSymbol ms : candidates) {
                if (spansOverlap(ms.range(), declSpan)) {
                    return ms;
                }
            }
        }
        return candidates.get(0);
    }

    /**
     * Finds the {@link FieldSymbol} in the table matching the given name and
     * whose declaration range overlaps the given CST node span.
     *
     * @param table     the symbol table
     * @param fieldName the field name
     * @param declSpan  the field declaration span from the CST node
     * @return the matching symbol, or {@code null} if not found
     */
    private static FieldSymbol findFieldSymbol(SymbolTable table, String fieldName,
                                               TextSpan declSpan) {
        List<FieldSymbol> candidates = table.getFieldsByName(fieldName);
        if (candidates.isEmpty()) {
            return null;
        }
        if (candidates.size() == 1) {
            return candidates.get(0);
        }
        if (declSpan != null) {
            for (FieldSymbol fs : candidates) {
                if (spansOverlap(fs.range(), declSpan)) {
                    return fs;
                }
            }
        }
        return candidates.get(0);
    }

    /**
     * Finds the {@link ImportSymbol} in the table whose declaration range
     * overlaps the given import node span.
     *
     * @param table    the symbol table
     * @param declSpan the import declaration span from the CST node
     * @return the matching symbol, or {@code null} if not found
     */
    private static ImportSymbol findImportSymbol(SymbolTable table, TextSpan declSpan) {
        List<ImportSymbol> allImports = table.getAllImports();
        if (allImports.isEmpty()) {
            return null;
        }
        if (declSpan == null) {
            return allImports.get(0);
        }
        for (ImportSymbol is : allImports) {
            if (spansOverlap(is.range(), declSpan)) {
                return is;
            }
        }
        return null;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TokenType mapping
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Maps a tree-sitter type declaration node type to the corresponding
     * {@link TokenType} for rendering.
     *
     * @param nodeType the CST node type (e.g., "class_declaration")
     * @return the token type for coloring
     */
    private static TokenType mapTypeNodeToTokenType(String nodeType) {
        if (NODE_INTERFACE_DECLARATION.equals(nodeType)) {
            return TokenType.INTERFACE_DECL;
        }
        if (NODE_ENUM_DECLARATION.equals(nodeType)) {
            return TokenType.ENUM_DECL;
        }
        if (NODE_ANNOTATION_TYPE_DECLARATION.equals(nodeType)) {
            return TokenType.JSDOC_TAG;
        }
        // NODE_CLASS_DECLARATION and fallback
        return TokenType.CLASS_DECL;
    }

    /**
     * Maps a {@link FieldSymbol}'s modifiers to the appropriate {@link TokenType}.
     *
     * <p>Static final fields get {@link TokenType#STATIC_FINAL_FIELD} (magenta,
     * bold+italic), matching IntelliJ's convention for constants. All other
     * fields get {@link TokenType#GLOBAL_FIELD} (aqua).</p>
     *
     * @param symbol the field symbol (may be {@code null} if the symbol table
     *               lookup failed)
     * @return the token type for coloring
     */
    private static TokenType mapFieldSymbol(FieldSymbol symbol) {
        if (symbol != null && symbol.isStatic() && symbol.isFinal()) {
            return TokenType.STATIC_FINAL_FIELD;
        }
        return TokenType.GLOBAL_FIELD;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Utility
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Finds the import path node (scoped_identifier or identifier) within
     * an import_declaration CST node.
     *
     * @param importNode the import declaration node
     * @return the path node, or {@code null} if not found
     */
    private static SemanticNode findImportPathNode(SemanticNode importNode) {
        List<SemanticNode> children = importNode.namedChildren();
        for (SemanticNode child : children) {
            String type = child.nodeType();
            if (NODE_SCOPED_IDENTIFIER.equals(type) || NODE_IDENTIFIER.equals(type)) {
                return child;
            }
        }
        return null;
    }

    /**
     * Tests whether two {@link TextSpan}s overlap.
     *
     * @param a the first span (may be {@code null})
     * @param b the second span (may be {@code null})
     * @return {@code true} if both spans are non-null and overlap
     */
    private static boolean spansOverlap(TextSpan a, TextSpan b) {
        if (a == null || b == null) {
            return false;
        }
        return a.getStartByte() < b.getEndByte() && b.getStartByte() < a.getEndByte();
    }
}
