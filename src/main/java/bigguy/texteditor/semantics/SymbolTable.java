package bigguy.texteditor.semantics;

import bigguy.texteditor.index.Symbol;
import bigguy.texteditor.index.Symbol.FieldSymbol;
import bigguy.texteditor.index.Symbol.ImportSymbol;
import bigguy.texteditor.index.Symbol.MethodSymbol;
import bigguy.texteditor.index.Symbol.TypeSymbol;
import bigguy.texteditor.index.SymbolKey;
import bigguy.texteditor.index.SymbolKind;
import bigguy.texteditor.treesitter.java.TextSpan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Central registry of all symbols declared in a document.
 *
 * <p>Replaces ScriptDocument's triple-redundant type maps ({@code scriptTypes},
 * {@code scriptTypesByFullName}, {@code scriptTypesByDotName}) and scattered
 * method/field collections with a single source of truth.</p>
 *
 * <h3>Architecture</h3>
 * <p>One primary {@link LinkedHashMap} stores all symbols keyed by {@link SymbolKey}.
 * Derived index views (types by simple name, types by full name, methods by name, etc.)
 * are rebuilt lazily on first access after any mutation. This follows IntelliJ's
 * {@code StubIndex} pattern: one canonical store with derived, cached projections.</p>
 *
 * <h3>Thread safety</h3>
 * <p>Mutation methods ({@link #addSymbol}, {@link #removeSymbol}, {@link #replaceAll},
 * {@link #clear}, {@link #adjustOffsets}) are {@code synchronized} and intended to be
 * called from a background analysis thread. Query methods return unmodifiable snapshot
 * copies and are safe to call from the UI thread without external synchronization.</p>
 *
 * <h3>Version tracking</h3>
 * <p>An {@link AtomicInteger} version counter increments on every mutation. Consumers
 * can poll {@link #version()} to detect staleness without deep-comparing contents.</p>
 *
 * @see Symbol
 * @see SymbolKey
 * @see SymbolKind
 */
public final class SymbolTable {

    // ── Primary store ──────────────────────────────────────────────────────────
    // Insertion-ordered so getAllSymbols() returns symbols in declaration order.

    private final LinkedHashMap<SymbolKey, Symbol> symbols;

    // ── Version counter ────────────────────────────────────────────────────────

    private final AtomicInteger version;

    // ── Derived indexes (lazily rebuilt) ────────────────────────────────────────
    // All index fields are guarded by `this`. They are set to null on mutation
    // and rebuilt on first query access after mutation.

    /** Types grouped by simple (unqualified) name. {@code null} means dirty. */
    private Map<String, List<TypeSymbol>> typesBySimpleName;

    /** Types keyed by fully qualified name. {@code null} means dirty. */
    private Map<String, TypeSymbol> typesByFullName;

    /** Methods grouped by method name. {@code null} means dirty. */
    private Map<String, List<MethodSymbol>> methodsByName;

    /** Fields/parameters/locals grouped by name. {@code null} means dirty. */
    private Map<String, List<FieldSymbol>> fieldsByName;

    /** Import symbols keyed by their full import path. {@code null} means dirty. */
    private Map<String, ImportSymbol> importsByPath;

    // ── Constructors ───────────────────────────────────────────────────────────

    /**
     * Creates an empty symbol table.
     */
    public SymbolTable() {
        this.symbols = new LinkedHashMap<>();
        this.version = new AtomicInteger(0);
    }

    /**
     * Creates a symbol table pre-populated with the given symbols.
     *
     * @param initial the symbols to add (must not be {@code null})
     * @throws NullPointerException if {@code initial} is {@code null}
     */
    public SymbolTable(List<Symbol> initial) {
        this();
        if (initial == null) {
            throw new NullPointerException("initial must not be null");
        }
        for (Symbol sym : initial) {
            if (sym != null) {
                symbols.put(sym.key(), sym);
            }
        }
        // Initial population counts as version 1.
        if (!symbols.isEmpty()) {
            version.set(1);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Mutation (background analysis thread)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Removes all symbols from the table and invalidates all derived indexes.
     */
    public synchronized void clear() {
        if (symbols.isEmpty()) {
            return;
        }
        symbols.clear();
        invalidateIndexes();
        version.incrementAndGet();
    }

    /**
     * Adds a symbol to the table. If a symbol with the same key already exists,
     * it is replaced.
     *
     * @param symbol the symbol to add (must not be {@code null})
     * @throws NullPointerException if {@code symbol} is {@code null}
     */
    public synchronized void addSymbol(Symbol symbol) {
        if (symbol == null) {
            throw new NullPointerException("symbol must not be null");
        }
        symbols.put(symbol.key(), symbol);
        invalidateIndexes();
        version.incrementAndGet();
    }

    /**
     * Removes the symbol identified by {@code key}. No-op if the key is not present.
     *
     * @param key the key of the symbol to remove (must not be {@code null})
     * @throws NullPointerException if {@code key} is {@code null}
     */
    public synchronized void removeSymbol(SymbolKey key) {
        if (key == null) {
            throw new NullPointerException("key must not be null");
        }
        Symbol removed = symbols.remove(key);
        if (removed != null) {
            invalidateIndexes();
            version.incrementAndGet();
        }
    }

    /**
     * Atomically replaces all symbols in the table with the given list.
     *
     * <p>This is the primary bulk-update method used after a full analysis pass.
     * It clears the table, inserts all new symbols, and bumps the version
     * counter exactly once.</p>
     *
     * @param newSymbols the complete set of symbols to store (must not be {@code null};
     *                   {@code null} elements within the list are silently skipped)
     * @throws NullPointerException if {@code newSymbols} is {@code null}
     */
    public synchronized void replaceAll(List<Symbol> newSymbols) {
        if (newSymbols == null) {
            throw new NullPointerException("newSymbols must not be null");
        }
        symbols.clear();
        for (Symbol sym : newSymbols) {
            if (sym != null) {
                symbols.put(sym.key(), sym);
            }
        }
        invalidateIndexes();
        version.incrementAndGet();
    }

    /**
     * Shifts byte offsets of all symbols whose declaration starts at or after
     * {@code editStart} by {@code delta} bytes. Used for incremental offset
     * adjustment after an edit, so that symbol ranges stay in sync with the
     * modified source text without a full re-analysis.
     *
     * <p>Symbols whose range starts before {@code editStart} are left unchanged.
     * Symbols overlapping the edit point (start &lt; editStart &lt; end) are
     * removed, since their ranges are no longer valid.</p>
     *
     * <p><strong>Note:</strong> This mutates the table in-place by rebuilding
     * affected entries with adjusted {@link TextSpan}s and new {@link SymbolKey}s.
     * The version counter is incremented exactly once.</p>
     *
     * @param editStart the byte offset at which the edit occurred (0-based)
     * @param delta     the number of bytes inserted (positive) or removed (negative)
     */
    public synchronized void adjustOffsets(int editStart, int delta) {
        if (delta == 0) {
            return;
        }

        List<Symbol> adjusted = new ArrayList<>(symbols.size());
        for (Symbol sym : symbols.values()) {
            TextSpan range = sym.range();
            int start = range.getStartByte();
            int end = range.getEndByte();

            if (end <= editStart) {
                // Entirely before the edit — keep as-is.
                adjusted.add(sym);
            } else if (start >= editStart) {
                // Entirely at or after the edit — shift both endpoints.
                Symbol shifted = shiftSymbol(sym, delta);
                if (shifted != null) {
                    adjusted.add(shifted);
                }
            }
            // else: overlaps the edit point — discard (invalid range).
        }

        symbols.clear();
        for (Symbol sym : adjusted) {
            symbols.put(sym.key(), sym);
        }
        invalidateIndexes();
        version.incrementAndGet();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Queries (UI thread — return unmodifiable snapshots)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Returns the symbol identified by {@code key}, or {@code null} if not found.
     *
     * @param key the key to look up (must not be {@code null})
     * @return the symbol, or {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     */
    public synchronized Symbol getSymbol(SymbolKey key) {
        if (key == null) {
            throw new NullPointerException("key must not be null");
        }
        return symbols.get(key);
    }

    /**
     * Returns all type symbols that share the given simple (unqualified) name.
     *
     * @param simpleName the simple type name to look up
     * @return an unmodifiable list of matching types (never {@code null}, may be empty)
     */
    public synchronized List<TypeSymbol> getTypesBySimpleName(String simpleName) {
        if (simpleName == null) {
            return Collections.emptyList();
        }
        ensureTypesBySimpleName();
        List<TypeSymbol> result = typesBySimpleName.get(simpleName);
        return result != null ? result : Collections.<TypeSymbol>emptyList();
    }

    /**
     * Returns the type symbol with the given fully qualified name, or {@code null}.
     *
     * @param fullName the fully qualified type name (e.g. {@code "java.util.List"})
     * @return the matching type symbol, or {@code null}
     */
    public synchronized TypeSymbol getTypeByFullName(String fullName) {
        if (fullName == null) {
            return null;
        }
        ensureTypesByFullName();
        return typesByFullName.get(fullName);
    }

    /**
     * Returns all method symbols with the given name.
     *
     * @param name the method name to look up
     * @return an unmodifiable list of matching methods (never {@code null}, may be empty)
     */
    public synchronized List<MethodSymbol> getMethodsByName(String name) {
        if (name == null) {
            return Collections.emptyList();
        }
        ensureMethodsByName();
        List<MethodSymbol> result = methodsByName.get(name);
        return result != null ? result : Collections.<MethodSymbol>emptyList();
    }

    /**
     * Returns all field symbols (fields, parameters, locals) with the given name.
     *
     * @param name the field name to look up
     * @return an unmodifiable list of matching field symbols (never {@code null}, may be empty)
     */
    public synchronized List<FieldSymbol> getFieldsByName(String name) {
        if (name == null) {
            return Collections.emptyList();
        }
        ensureFieldsByName();
        List<FieldSymbol> result = fieldsByName.get(name);
        return result != null ? result : Collections.<FieldSymbol>emptyList();
    }

    /**
     * Returns the import symbol for the given full import path, or {@code null}.
     *
     * @param fullPath the full import path (e.g. {@code "java.util.List"})
     * @return the matching import symbol, or {@code null}
     */
    public synchronized ImportSymbol getImportByPath(String fullPath) {
        if (fullPath == null) {
            return null;
        }
        ensureImportsByPath();
        return importsByPath.get(fullPath);
    }

    /**
     * Returns an unmodifiable snapshot of all symbols in declaration order.
     *
     * @return all symbols (never {@code null}, may be empty)
     */
    public synchronized List<Symbol> getAllSymbols() {
        if (symbols.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<Symbol>(symbols.values()));
    }

    /**
     * Returns an unmodifiable snapshot of all type symbols.
     *
     * @return all types (never {@code null}, may be empty)
     */
    public synchronized List<TypeSymbol> getAllTypes() {
        List<TypeSymbol> result = new ArrayList<>();
        for (Symbol sym : symbols.values()) {
            if (sym instanceof TypeSymbol) {
                result.add((TypeSymbol) sym);
            }
        }
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns an unmodifiable snapshot of all method symbols.
     *
     * @return all methods (never {@code null}, may be empty)
     */
    public synchronized List<MethodSymbol> getAllMethods() {
        List<MethodSymbol> result = new ArrayList<>();
        for (Symbol sym : symbols.values()) {
            if (sym instanceof MethodSymbol) {
                result.add((MethodSymbol) sym);
            }
        }
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns an unmodifiable snapshot of all field symbols
     * (fields, parameters, local variables, enum constants).
     *
     * @return all fields (never {@code null}, may be empty)
     */
    public synchronized List<FieldSymbol> getAllFields() {
        List<FieldSymbol> result = new ArrayList<>();
        for (Symbol sym : symbols.values()) {
            if (sym instanceof FieldSymbol) {
                result.add((FieldSymbol) sym);
            }
        }
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns an unmodifiable snapshot of all import symbols.
     *
     * @return all imports (never {@code null}, may be empty)
     */
    public synchronized List<ImportSymbol> getAllImports() {
        List<ImportSymbol> result = new ArrayList<>();
        for (Symbol sym : symbols.values()) {
            if (sym instanceof ImportSymbol) {
                result.add((ImportSymbol) sym);
            }
        }
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(result);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Utilities
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * @return the number of symbols in the table
     */
    public synchronized int size() {
        return symbols.size();
    }

    /**
     * @return {@code true} if the table contains no symbols
     */
    public synchronized boolean isEmpty() {
        return symbols.isEmpty();
    }

    /**
     * Returns the current version number. The version is incremented on every
     * mutation. Consumers can cache this value and compare to detect staleness.
     *
     * @return the version counter (starts at 0 for an empty table)
     */
    public int version() {
        return version.get();
    }

    /**
     * Tests whether a symbol with the given key exists in the table.
     *
     * @param key the key to test (must not be {@code null})
     * @return {@code true} if present
     * @throws NullPointerException if {@code key} is {@code null}
     */
    public synchronized boolean containsKey(SymbolKey key) {
        if (key == null) {
            throw new NullPointerException("key must not be null");
        }
        return symbols.containsKey(key);
    }

    @Override
    public String toString() {
        return "SymbolTable{size=" + symbols.size()
                + ", version=" + version.get()
                + ", types=" + countByKindCategory(true, false, false, false)
                + ", methods=" + countByKindCategory(false, true, false, false)
                + ", fields=" + countByKindCategory(false, false, true, false)
                + ", imports=" + countByKindCategory(false, false, false, true)
                + '}';
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Private — index rebuilding
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Marks all derived indexes as dirty. Must be called under {@code synchronized}.
     */
    private void invalidateIndexes() {
        typesBySimpleName = null;
        typesByFullName = null;
        methodsByName = null;
        fieldsByName = null;
        importsByPath = null;
    }

    /**
     * Rebuilds {@link #typesBySimpleName} if dirty.
     * Must be called under {@code synchronized}.
     */
    private void ensureTypesBySimpleName() {
        if (typesBySimpleName != null) {
            return;
        }
        Map<String, List<TypeSymbol>> index = new LinkedHashMap<>();
        for (Symbol sym : symbols.values()) {
            if (sym instanceof TypeSymbol) {
                TypeSymbol ts = (TypeSymbol) sym;
                String name = ts.name();
                List<TypeSymbol> bucket = index.get(name);
                if (bucket == null) {
                    bucket = new ArrayList<>(2);
                    index.put(name, bucket);
                }
                bucket.add(ts);
            }
        }
        // Freeze each bucket as an unmodifiable list.
        for (Map.Entry<String, List<TypeSymbol>> entry : index.entrySet()) {
            entry.setValue(Collections.unmodifiableList(entry.getValue()));
        }
        typesBySimpleName = Collections.unmodifiableMap(index);
    }

    /**
     * Rebuilds {@link #typesByFullName} if dirty.
     * Must be called under {@code synchronized}.
     */
    private void ensureTypesByFullName() {
        if (typesByFullName != null) {
            return;
        }
        Map<String, TypeSymbol> index = new LinkedHashMap<>();
        for (Symbol sym : symbols.values()) {
            if (sym instanceof TypeSymbol) {
                TypeSymbol ts = (TypeSymbol) sym;
                // fullName is the canonical key; last-write-wins on collision
                // (same behaviour as the legacy ScriptDocument maps).
                index.put(ts.fullName(), ts);
            }
        }
        typesByFullName = Collections.unmodifiableMap(index);
    }

    /**
     * Rebuilds {@link #methodsByName} if dirty.
     * Must be called under {@code synchronized}.
     */
    private void ensureMethodsByName() {
        if (methodsByName != null) {
            return;
        }
        Map<String, List<MethodSymbol>> index = new LinkedHashMap<>();
        for (Symbol sym : symbols.values()) {
            if (sym instanceof MethodSymbol) {
                MethodSymbol ms = (MethodSymbol) sym;
                String name = ms.name();
                List<MethodSymbol> bucket = index.get(name);
                if (bucket == null) {
                    bucket = new ArrayList<>(2);
                    index.put(name, bucket);
                }
                bucket.add(ms);
            }
        }
        for (Map.Entry<String, List<MethodSymbol>> entry : index.entrySet()) {
            entry.setValue(Collections.unmodifiableList(entry.getValue()));
        }
        methodsByName = Collections.unmodifiableMap(index);
    }

    /**
     * Rebuilds {@link #fieldsByName} if dirty.
     * Must be called under {@code synchronized}.
     */
    private void ensureFieldsByName() {
        if (fieldsByName != null) {
            return;
        }
        Map<String, List<FieldSymbol>> index = new LinkedHashMap<>();
        for (Symbol sym : symbols.values()) {
            if (sym instanceof FieldSymbol) {
                FieldSymbol fs = (FieldSymbol) sym;
                String name = fs.name();
                List<FieldSymbol> bucket = index.get(name);
                if (bucket == null) {
                    bucket = new ArrayList<>(2);
                    index.put(name, bucket);
                }
                bucket.add(fs);
            }
        }
        for (Map.Entry<String, List<FieldSymbol>> entry : index.entrySet()) {
            entry.setValue(Collections.unmodifiableList(entry.getValue()));
        }
        fieldsByName = Collections.unmodifiableMap(index);
    }

    /**
     * Rebuilds {@link #importsByPath} if dirty.
     * Must be called under {@code synchronized}.
     */
    private void ensureImportsByPath() {
        if (importsByPath != null) {
            return;
        }
        Map<String, ImportSymbol> index = new LinkedHashMap<>();
        for (Symbol sym : symbols.values()) {
            if (sym instanceof ImportSymbol) {
                ImportSymbol is = (ImportSymbol) sym;
                index.put(is.fullPath(), is);
            }
        }
        importsByPath = Collections.unmodifiableMap(index);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Private — offset shifting helpers
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Creates a new {@link TextSpan} shifted by {@code delta} bytes.
     * Row/column information is cleared (set to 0) because accurate
     * line mapping requires the full source text, which we don't have here.
     * The next full analysis pass will provide correct row/column data.
     *
     * @param original the original span
     * @param delta    bytes to add to both start and end
     * @return the shifted span, or {@code null} if the shift would produce
     *         a negative start byte
     */
    private static TextSpan shiftSpan(TextSpan original, int delta) {
        int newStart = original.getStartByte() + delta;
        int newEnd = original.getEndByte() + delta;
        if (newStart < 0 || newEnd < newStart) {
            return null;
        }
        // Row/column zeroed — will be corrected by the next full analysis pass.
        return new TextSpan(0, 0, 0, 0, newStart, newEnd);
    }

    /**
     * Creates a shifted copy of the given symbol. Returns {@code null} if the
     * shift would produce an invalid range.
     *
     * @param sym   the symbol to shift
     * @param delta bytes to add
     * @return the shifted symbol, or {@code null}
     */
    private static Symbol shiftSymbol(Symbol sym, int delta) {
        TextSpan shifted = shiftSpan(sym.range(), delta);
        if (shifted == null) {
            return null;
        }
        SymbolKey newKey = new SymbolKey(sym.key().name(), sym.key().kind(), shifted);

        if (sym instanceof TypeSymbol) {
            TypeSymbol ts = (TypeSymbol) sym;
            return new TypeSymbol(newKey, shifted, ts.name(), ts.fullName(), ts.dotName());
        }
        if (sym instanceof MethodSymbol) {
            MethodSymbol ms = (MethodSymbol) sym;
            TextSpan shiftedBody = null;
            if (ms.bodyRange() != null) {
                shiftedBody = shiftSpan(ms.bodyRange(), delta);
            }
            return new MethodSymbol(newKey, shifted, ms.name(), shiftedBody, ms.parameterNames());
        }
        if (sym instanceof FieldSymbol) {
            FieldSymbol fs = (FieldSymbol) sym;
            return new FieldSymbol(newKey, shifted, fs.name(), fs.typeName(), fs.isStatic(), fs.isFinal());
        }
        if (sym instanceof ImportSymbol) {
            ImportSymbol is = (ImportSymbol) sym;
            return new ImportSymbol(newKey, shifted, is.name(), is.fullPath(), is.isWildcard(), is.isStatic());
        }
        // Unknown symbol subtype — should not happen with the closed hierarchy.
        return null;
    }

    /**
     * Counts symbols whose {@link SymbolKind} matches the requested category flags.
     * Used only for {@link #toString()} diagnostics.
     */
    private int countByKindCategory(boolean types, boolean callables,
                                    boolean variables, boolean imports) {
        int count = 0;
        for (Symbol sym : symbols.values()) {
            SymbolKind k = sym.kind();
            if (types && k.isType()) {
                count++;
            } else if (callables && k.isCallable()) {
                count++;
            } else if (variables && k.isVariable()) {
                count++;
            } else if (imports && k == SymbolKind.IMPORT) {
                count++;
            }
        }
        return count;
    }
}
