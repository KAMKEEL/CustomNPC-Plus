package bigguy.texteditor.treesitter.java;

import org.treesitter.TSParser;
import org.treesitter.TSTree;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe Java source code parser backed by tree-sitter.
 *
 * <p>Maps to Zed's parsing pipeline: buffer.rs triggers parsing, syntax_map.rs
 * manages the tree lifecycle with incremental updates. This class unifies both
 * roles into a single, lock-guarded parser that supports:</p>
 * <ul>
 *   <li>Full parsing of Java source strings</li>
 *   <li>Incremental re-parsing after edits (via {@link SyntaxTree#edit(SyntaxTree.EditOperation)})</li>
 *   <li>Thread-safe access to the underlying native parser</li>
 * </ul>
 *
 * <p>Typical usage:</p>
 * <pre>{@code
 * try (JavaParser parser = new JavaParser()) {
 *     SyntaxTree tree = parser.parse("public class Foo {}");
 *     SyntaxNode root = tree.getRootNode();
 *     // ...
 * }
 * }</pre>
 */
public final class JavaParser implements AutoCloseable {

    private final JavaGrammar grammar;
    private final JavaParserConfig config;
    private final TSParser nativeParser;
    private final ReentrantLock parseLock = new ReentrantLock();
    private SyntaxTree lastTree;
    private volatile boolean closed = false;

    public JavaParser() {
        this(JavaGrammar.getInstance(), JavaParserConfig.defaults());
    }

    public JavaParser(JavaParserConfig config) {
        this(JavaGrammar.getInstance(), config);
    }

    public JavaParser(JavaGrammar grammar, JavaParserConfig config) {
        this.grammar = grammar;
        this.config = config;
        this.nativeParser = new TSParser();
        if (!this.nativeParser.setLanguage(grammar.getLanguage())) {
            this.nativeParser.close();
            throw new IllegalStateException(
                    "Failed to set Java language — ABI version mismatch between " +
                    "tree-sitter library and tree-sitter-java grammar");
        }
    }

    /**
     * Parses a complete Java source string from scratch.
     *
     * @param sourceText the full Java source code
     * @return the parsed syntax tree
     * @throws IllegalStateException if the parser is closed or parsing fails
     */
    public SyntaxTree parse(String sourceText) {
        ensureOpen();
        parseLock.lock();
        try {
            TSTree nativeTree = nativeParser.parseString(null, sourceText);
            if (nativeTree == null) {
                throw new IllegalStateException("Parsing failed — no language set or parser cancelled");
            }
            SyntaxTree tree = new SyntaxTree(nativeTree, sourceText);
            if (config.isIncrementalParsingEnabled()) {
                lastTree = tree;
            }
            return tree;
        } finally {
            parseLock.unlock();
        }
    }

    /**
     * Incrementally re-parses source after an edit has been applied.
     *
     * <p>The {@code oldTree} must have had {@link SyntaxTree#edit(SyntaxTree.EditOperation)}
     * called on it before this method is invoked. Tree-sitter will reuse
     * unchanged subtrees from the old tree, making this significantly faster
     * than a full parse.</p>
     *
     * @param oldTree    the previously parsed tree, post-edit
     * @param newSource  the complete new source text
     * @return the updated syntax tree
     */
    public SyntaxTree reparse(SyntaxTree oldTree, String newSource) {
        ensureOpen();
        parseLock.lock();
        try {
            TSTree nativeTree = nativeParser.parseString(oldTree.unwrap(), newSource);
            if (nativeTree == null) {
                throw new IllegalStateException("Incremental parsing failed");
            }
            SyntaxTree tree = new SyntaxTree(nativeTree, newSource);
            if (config.isIncrementalParsingEnabled()) {
                lastTree = tree;
            }
            return tree;
        } finally {
            parseLock.unlock();
        }
    }

    /**
     * Convenience: re-parses using the last parsed tree as the old tree.
     *
     * <p>Only works when incremental parsing is enabled and at least one
     * prior parse has occurred. Falls back to full parse otherwise.</p>
     *
     * @param editOp    the edit that was applied to the source
     * @param newSource the complete new source text
     * @return the updated syntax tree
     */
    public SyntaxTree reparseAfterEdit(SyntaxTree.EditOperation editOp, String newSource) {
        ensureOpen();
        parseLock.lock();
        try {
            if (lastTree != null && config.isIncrementalParsingEnabled()) {
                lastTree.edit(editOp);
                return reparse(lastTree, newSource);
            }
            return parse(newSource);
        } finally {
            parseLock.unlock();
        }
    }

    public JavaGrammar getGrammar() {
        return grammar;
    }

    public JavaParserConfig getConfig() {
        return config;
    }

    private void ensureOpen() {
        if (closed) throw new IllegalStateException("JavaParser has been closed");
    }

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            nativeParser.close();
        }
    }
}
