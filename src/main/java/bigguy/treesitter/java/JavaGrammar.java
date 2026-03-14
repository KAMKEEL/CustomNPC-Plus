package bigguy.treesitter.java;

import org.treesitter.TSLanguage;
import org.treesitter.TSQuery;
import org.treesitter.TreeSitterJava;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the tree-sitter Java grammar lifecycle and compiled query cache.
 *
 * <p>This class implements Zed's lazy-initialization pattern: the native
 * grammar is loaded once on first access, and compiled queries are cached
 * by their source string to avoid redundant compilation. All operations
 * are thread-safe.</p>
 *
 * <p>Typical usage:</p>
 * <pre>{@code
 * JavaGrammar grammar = JavaGrammar.getInstance();
 * TSQuery query = grammar.compileQuery(QueryPatterns.HIGHLIGHTS);
 * }</pre>
 *
 * <p>Implements {@link AutoCloseable}; closing releases the native grammar
 * and all cached queries. After close, the singleton can be re-acquired
 * via {@link #getInstance()}, which will create a fresh instance.</p>
 */
public final class JavaGrammar implements AutoCloseable {

    private static volatile JavaGrammar instance;
    private static final Object LOCK = new Object();

    private final TSLanguage language;
    private final ConcurrentHashMap<String, TSQuery> queryCache = new ConcurrentHashMap<>();
    private volatile boolean closed = false;

    private JavaGrammar() {
        this.language = new TreeSitterJava();
    }

    /**
     * Returns the shared singleton instance, creating it if necessary.
     *
     * <p>Thread-safe via double-checked locking. If a previous instance was
     * closed, a new one is created.</p>
     *
     * @return the shared grammar instance
     */
    public static JavaGrammar getInstance() {
        JavaGrammar local = instance;
        if (local == null || local.closed) {
            synchronized (LOCK) {
                local = instance;
                if (local == null || local.closed) {
                    local = new JavaGrammar();
                    instance = local;
                }
            }
        }
        return local;
    }

    /**
     * Creates a non-singleton instance for isolated use (e.g. testing).
     *
     * @return a fresh grammar instance independent of the singleton
     */
    public static JavaGrammar createIsolated() {
        return new JavaGrammar();
    }

    TSLanguage getLanguage() {
        ensureOpen();
        return language;
    }

    /**
     * Compiles a tree-sitter query from an S-expression source string.
     *
     * <p>Results are cached: the same source string will return the same
     * compiled query on subsequent calls. The cache is thread-safe.</p>
     *
     * @param querySource the S-expression query (e.g. from {@link QueryPatterns})
     * @return the compiled query
     * @throws org.treesitter.TSQueryException if the query has a syntax error
     */
    public TSQuery compileQuery(String querySource) {
        ensureOpen();
        return queryCache.computeIfAbsent(querySource,
                src -> new TSQuery(language, src));
    }

    /**
     * Removes all cached compiled queries, freeing their native memory.
     */
    public void clearQueryCache() {
        for (TSQuery q : queryCache.values()) {
            q.close();
        }
        queryCache.clear();
    }

    private void ensureOpen() {
        if (closed) throw new IllegalStateException("JavaGrammar has been closed");
    }

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            clearQueryCache();
        }
    }
}
