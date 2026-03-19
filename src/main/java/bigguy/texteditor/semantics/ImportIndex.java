package bigguy.texteditor.semantics;

import bigguy.texteditor.treesitter.java.TextSpan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Unified import resolution index for a single script document.
 *
 * <p>Replaces the four separate import-tracking fields that previously lived in
 * {@code ScriptDocument} ({@code imports}, {@code wildcardPackages},
 * {@code importsBySimpleName}, {@code implicitImports}) with a single source of
 * truth backed by a list of {@link ImportEntry} objects.</p>
 *
 * <h3>Lazy derived indexes</h3>
 * <p>Two derived lookup maps ({@code bySimpleName} and {@code byFullPath}) are
 * computed lazily on first access after any mutation, and cached until the next
 * mutation invalidates them. This gives O(1) lookups without paying rebuild
 * cost on every add.</p>
 *
 * <h3>Version tracking</h3>
 * <p>A monotonically increasing version counter is bumped on every mutation.
 * External code can call {@link #version()} to detect whether the index has
 * changed since its last snapshot.</p>
 *
 * <h3>Thread safety</h3>
 * <p>All mutation methods are {@code synchronized}. All query methods return
 * defensive copies or unmodifiable snapshots, so callers never observe
 * partially-mutated state.</p>
 *
 * @since Phase 2 — CST editor architecture
 */
public final class ImportIndex {

    // ── Primary store ──────────────────────────────────────────────────

    /** The authoritative list of all import entries (explicit + implicit). */
    private final List<ImportEntry> entries;

    // ── Derived indexes (lazily rebuilt) ────────────────────────────────

    /** Quick lookup by unqualified simple name (e.g. "List" → entry). */
    private Map<String, ImportEntry> bySimpleName;

    /** Quick lookup by fully qualified path (e.g. "java.util.List" → entry). */
    private Map<String, ImportEntry> byFullPath;

    /** Whether the derived indexes need rebuilding. */
    private boolean dirty;

    /** Monotonically increasing version counter, bumped on every mutation. */
    private int version;

    // ── Construction ───────────────────────────────────────────────────

    /**
     * Creates a new, empty {@code ImportIndex}.
     */
    public ImportIndex() {
        this.entries = new ArrayList<ImportEntry>();
        this.bySimpleName = Collections.emptyMap();
        this.byFullPath = Collections.emptyMap();
        this.dirty = false;
        this.version = 0;
    }

    // ── Inner class ────────────────────────────────────────────────────

    /**
     * An individual import declaration (explicit or implicit).
     *
     * <p>Instances are immutable. Two entries are equal if and only if they
     * share the same {@code fullPath} and {@code isImplicit} flag — the
     * source range is deliberately excluded from equality so that
     * re-parsing the same import at a different offset does not produce
     * a spurious "changed" signal.</p>
     */
    public static final class ImportEntry {

        private final String fullPath;
        private final String simpleName;
        private final boolean isWildcard;
        private final boolean isStatic;
        private final boolean isImplicit;
        private final TextSpan range;
        private final String packagePath;

        /**
         * Creates a new {@code ImportEntry}.
         *
         * @param fullPath    the fully qualified import path, e.g. {@code "java.util.List"}
         *                    or {@code "java.util.*"}
         * @param simpleName  the unqualified name, e.g. {@code "List"} or {@code "*"}
         * @param isWildcard  {@code true} for wildcard imports ({@code import pkg.*})
         * @param isStatic    {@code true} for static imports ({@code import static ...})
         * @param isImplicit  {@code true} for auto-injected imports (e.g. {@code java.lang.*})
         * @param range       source location of the import statement; may be {@code null}
         *                    for implicit imports
         * @param packagePath the package portion, e.g. {@code "java.util"}
         */
        public ImportEntry(String fullPath, String simpleName, boolean isWildcard,
                           boolean isStatic, boolean isImplicit, TextSpan range,
                           String packagePath) {
            if (fullPath == null) {
                throw new IllegalArgumentException("fullPath must not be null");
            }
            if (simpleName == null) {
                throw new IllegalArgumentException("simpleName must not be null");
            }
            if (packagePath == null) {
                throw new IllegalArgumentException("packagePath must not be null");
            }
            this.fullPath = fullPath;
            this.simpleName = simpleName;
            this.isWildcard = isWildcard;
            this.isStatic = isStatic;
            this.isImplicit = isImplicit;
            this.range = range;
            this.packagePath = packagePath;
        }

        /**
         * Factory for creating an {@code ImportEntry} from a fully qualified path string.
         *
         * <p>Automatically derives {@code simpleName}, {@code isWildcard}, and
         * {@code packagePath} from the path.</p>
         *
         * @param fullPath   the fully qualified import path
         * @param isStatic   whether this is a static import
         * @param isImplicit whether this is an implicit (auto-injected) import
         * @param range      source location, or {@code null} for implicit imports
         * @return a new {@code ImportEntry}
         */
        public static ImportEntry fromFullPath(String fullPath, boolean isStatic,
                                               boolean isImplicit, TextSpan range) {
            boolean isWildcard = fullPath.endsWith(".*");
            String simpleName;
            String packagePath;
            int lastDot = fullPath.lastIndexOf('.');
            if (lastDot >= 0) {
                simpleName = fullPath.substring(lastDot + 1);
                packagePath = fullPath.substring(0, lastDot);
            } else {
                simpleName = fullPath;
                packagePath = "";
            }
            return new ImportEntry(fullPath, simpleName, isWildcard, isStatic,
                    isImplicit, range, packagePath);
        }

        /** @return the fully qualified import path (e.g. {@code "java.util.List"}) */
        public String getFullPath() { return fullPath; }

        /** @return the unqualified name (e.g. {@code "List"} or {@code "*"}) */
        public String getSimpleName() { return simpleName; }

        /** @return {@code true} for wildcard imports ({@code import pkg.*}) */
        public boolean isWildcard() { return isWildcard; }

        /** @return {@code true} for static imports */
        public boolean isStatic() { return isStatic; }

        /** @return {@code true} for auto-injected imports */
        public boolean isImplicit() { return isImplicit; }

        /**
         * @return the source location of the import statement, or {@code null}
         *         for implicit imports
         */
        public TextSpan getRange() { return range; }

        /** @return the package portion (e.g. {@code "java.util"}) */
        public String getPackagePath() { return packagePath; }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof ImportEntry)) return false;
            ImportEntry that = (ImportEntry) obj;
            return this.isImplicit == that.isImplicit
                    && this.fullPath.equals(that.fullPath);
        }

        @Override
        public int hashCode() {
            int result = fullPath.hashCode();
            result = 31 * result + (isImplicit ? 1 : 0);
            return result;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (isImplicit) sb.append("[implicit] ");
            if (isStatic) sb.append("static ");
            sb.append("import ").append(fullPath);
            if (range != null) {
                sb.append(" at ").append(range);
            }
            return sb.toString();
        }
    }

    // ── Building (mutators) ────────────────────────────────────────────

    /**
     * Removes all import entries and resets the version counter.
     */
    public synchronized void clear() {
        entries.clear();
        markDirty();
    }

    /**
     * Adds a single import entry.
     *
     * @param entry the entry to add; must not be {@code null}
     * @throws IllegalArgumentException if {@code entry} is {@code null}
     */
    public synchronized void addImport(ImportEntry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("entry must not be null");
        }
        entries.add(entry);
        markDirty();
    }

    /**
     * Adds an implicit (auto-injected) import from a fully qualified path.
     *
     * <p>The entry is created with {@code isImplicit=true}, {@code isStatic=false},
     * and {@code range=null}.</p>
     *
     * @param fullPath the fully qualified path (e.g. {@code "java.lang.*"})
     */
    public synchronized void addImplicitImport(String fullPath) {
        ImportEntry entry = ImportEntry.fromFullPath(fullPath, false, true, null);
        entries.add(entry);
        markDirty();
    }

    /**
     * Atomically replaces all entries with the given list.
     *
     * <p>The previous contents are discarded. This is the preferred method
     * when rebuilding imports from a fresh parse — it avoids intermediate
     * states visible to concurrent readers.</p>
     *
     * @param newEntries the replacement entries; the list is defensively copied
     */
    public synchronized void replaceAll(List<ImportEntry> newEntries) {
        entries.clear();
        if (newEntries != null) {
            entries.addAll(newEntries);
        }
        markDirty();
    }

    // ── Resolution (queries) ───────────────────────────────────────────

    /**
     * Resolves an import by its unqualified simple name.
     *
     * <p>Resolution order:</p>
     * <ol>
     *   <li>Exact match in explicit (non-wildcard) imports by simple name.</li>
     *   <li>If no exact match, returns the first wildcard import entry whose
     *       package could contain the type. (Full resolution would require a
     *       {@code TypeResolver} callback — for now the wildcard entry itself
     *       is returned so callers can perform the lookup.)</li>
     * </ol>
     *
     * @param simpleName the unqualified name to resolve (e.g. {@code "List"})
     * @return the matching entry, or {@code null} if not found
     */
    public synchronized ImportEntry resolveBySimpleName(String simpleName) {
        if (simpleName == null || simpleName.isEmpty()) {
            return null;
        }
        ensureIndexes();
        ImportEntry exact = bySimpleName.get(simpleName);
        if (exact != null) {
            return exact;
        }
        // Fall back to wildcard entries — return the first wildcard that could
        // potentially provide the type (caller must verify via TypeResolver).
        for (int i = 0, size = entries.size(); i < size; i++) {
            ImportEntry entry = entries.get(i);
            if (entry.isWildcard) {
                return entry;
            }
        }
        return null;
    }

    /**
     * Resolves an import by its fully qualified path.
     *
     * @param fullPath the fully qualified path (e.g. {@code "java.util.List"})
     * @return the matching entry, or {@code null} if not found
     */
    public synchronized ImportEntry resolveByFullPath(String fullPath) {
        if (fullPath == null || fullPath.isEmpty()) {
            return null;
        }
        ensureIndexes();
        return byFullPath.get(fullPath);
    }

    /**
     * Quick check whether a simple name is covered by any import
     * (explicit, wildcard, or implicit).
     *
     * @param simpleName the unqualified name to check
     * @return {@code true} if any import covers this name
     */
    public synchronized boolean isImported(String simpleName) {
        return resolveBySimpleName(simpleName) != null;
    }

    /**
     * Checks whether a fully qualified name is covered by an implicit import.
     *
     * <p>This checks both exact matches (e.g. an implicit import of
     * {@code "java.lang.String"}) and wildcard matches (e.g. an implicit
     * import of {@code "java.lang.*"} covers {@code "java.lang.String"}).</p>
     *
     * @param fqn the fully qualified name to check
     * @return {@code true} if an implicit import covers this FQN
     */
    public synchronized boolean isImplicitlyImported(String fqn) {
        if (fqn == null || fqn.isEmpty()) {
            return false;
        }
        for (int i = 0, size = entries.size(); i < size; i++) {
            ImportEntry entry = entries.get(i);
            if (!entry.isImplicit) {
                continue;
            }
            if (entry.fullPath.equals(fqn)) {
                return true;
            }
            // Check wildcard coverage: "java.lang.*" covers "java.lang.String"
            if (entry.isWildcard) {
                String pkg = entry.packagePath;
                if (fqn.startsWith(pkg + ".") && fqn.indexOf('.', pkg.length() + 1) < 0) {
                    return true;
                }
            }
        }
        return false;
    }

    // ── Wildcard package queries ───────────────────────────────────────

    /**
     * Returns all wildcard package paths (e.g. {@code "java.util"} from
     * {@code import java.util.*}).
     *
     * @return an unmodifiable set of package paths; never {@code null}
     */
    public synchronized Set<String> getWildcardPackages() {
        Set<String> result = new HashSet<String>();
        for (int i = 0, size = entries.size(); i < size; i++) {
            ImportEntry entry = entries.get(i);
            if (entry.isWildcard) {
                result.add(entry.packagePath);
            }
        }
        return Collections.unmodifiableSet(result);
    }

    // ── Iteration ──────────────────────────────────────────────────────

    /**
     * Returns a snapshot of all import entries (both explicit and implicit).
     *
     * @return an unmodifiable list of all entries; never {@code null}
     */
    public synchronized List<ImportEntry> getAllImports() {
        return Collections.unmodifiableList(new ArrayList<ImportEntry>(entries));
    }

    /**
     * Returns a snapshot of only the implicit (auto-injected) imports.
     *
     * @return an unmodifiable list; never {@code null}
     */
    public synchronized List<ImportEntry> getImplicitImports() {
        List<ImportEntry> result = new ArrayList<ImportEntry>();
        for (int i = 0, size = entries.size(); i < size; i++) {
            ImportEntry entry = entries.get(i);
            if (entry.isImplicit) {
                result.add(entry);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns a snapshot of only the explicit (non-implicit) imports.
     *
     * @return an unmodifiable list; never {@code null}
     */
    public synchronized List<ImportEntry> getExplicitImports() {
        List<ImportEntry> result = new ArrayList<ImportEntry>();
        for (int i = 0, size = entries.size(); i < size; i++) {
            ImportEntry entry = entries.get(i);
            if (!entry.isImplicit) {
                result.add(entry);
            }
        }
        return Collections.unmodifiableList(result);
    }

    // ── Pruning ────────────────────────────────────────────────────────

    /**
     * Removes implicit imports whose simple name is not in the given set
     * of used names.
     *
     * <p>Wildcard implicit imports are never removed by this method, since
     * they cannot be matched by simple name alone.</p>
     *
     * <p>This is typically called after the token-build pass to discard
     * implicit imports that turned out to be unused.</p>
     *
     * @param usedSimpleNames the set of simple names actually referenced
     *                        in the source; must not be {@code null}
     */
    public synchronized void removeUnusedImplicitImports(Set<String> usedSimpleNames) {
        if (usedSimpleNames == null) {
            throw new IllegalArgumentException("usedSimpleNames must not be null");
        }
        boolean changed = false;
        Iterator<ImportEntry> it = entries.iterator();
        while (it.hasNext()) {
            ImportEntry entry = it.next();
            if (entry.isImplicit && !entry.isWildcard
                    && !usedSimpleNames.contains(entry.simpleName)) {
                it.remove();
                changed = true;
            }
        }
        if (changed) {
            markDirty();
        }
    }

    // ── Utilities ──────────────────────────────────────────────────────

    /**
     * Returns the total number of import entries (explicit + implicit).
     *
     * @return the entry count
     */
    public synchronized int size() {
        return entries.size();
    }

    /**
     * Returns {@code true} if there are no import entries.
     *
     * @return {@code true} when empty
     */
    public synchronized boolean isEmpty() {
        return entries.isEmpty();
    }

    /**
     * Returns the current version counter.
     *
     * <p>The version is bumped on every mutation. External code can snapshot
     * this value and later compare to detect whether the index has changed.</p>
     *
     * @return the current version (starts at 0, increments monotonically)
     */
    public synchronized int version() {
        return version;
    }

    // ── Internals ──────────────────────────────────────────────────────

    /**
     * Marks the derived indexes as stale and increments the version counter.
     * Must be called under the instance lock.
     */
    private void markDirty() {
        dirty = true;
        version++;
    }

    /**
     * Rebuilds the derived indexes if they are stale. Must be called under
     * the instance lock.
     */
    private void ensureIndexes() {
        if (!dirty) {
            return;
        }
        Map<String, ImportEntry> newBySimpleName = new HashMap<String, ImportEntry>();
        Map<String, ImportEntry> newByFullPath = new HashMap<String, ImportEntry>();
        for (int i = 0, size = entries.size(); i < size; i++) {
            ImportEntry entry = entries.get(i);
            newByFullPath.put(entry.fullPath, entry);
            // Only index non-wildcard entries by simple name; wildcard entries
            // are handled by the fallback scan in resolveBySimpleName().
            if (!entry.isWildcard) {
                // Later entries with the same simple name win (last-import-wins
                // semantics, matching javac behavior for duplicate imports).
                newBySimpleName.put(entry.simpleName, entry);
            }
        }
        this.bySimpleName = newBySimpleName;
        this.byFullPath = newByFullPath;
        this.dirty = false;
    }

    @Override
    public String toString() {
        return "ImportIndex{size=" + entries.size() + ", version=" + version + "}";
    }
}
