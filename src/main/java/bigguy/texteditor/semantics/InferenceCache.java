package bigguy.texteditor.semantics;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Cache for expensive type inference results that can be reused across
 * analysis passes within a single source version.
 *
 * <p>Replaces three separate cache maps that previously lived in
 * {@code ScriptDocument}:</p>
 * <ul>
 *   <li>{@code objectLiterals} — cached object-literal parses keyed by
 *       brace-start byte offset</li>
 *   <li>{@code lambdaCache} — cached lambda inferences keyed by
 *       header-start byte offset</li>
 *   <li>{@code scriptMethodSamContexts} — SAM conflict tracking keyed
 *       by method name</li>
 * </ul>
 *
 * <h3>Type safety</h3>
 * <p>The old code used raw {@code int} keys with no type safety. This
 * class preserves the integer-keyed approach but provides strongly-typed
 * accessor families ({@code putObjectLiteral}/{@code getObjectLiteral},
 * {@code putLambda}/{@code getLambda}, {@code putSamContext}/{@code getSamContext})
 * so callers cannot accidentally cross-contaminate caches.</p>
 *
 * <p>Cache entries are stored as {@link Object} internally because the
 * concrete analysis types ({@code ObjectLiteralAnalysis},
 * {@code LambdaCacheEntry}) live in the legacy package and this class
 * must not depend on them. Generic getter methods perform unchecked casts
 * that are safe as long as callers maintain type consistency per cache
 * family.</p>
 *
 * <h3>Version-aware invalidation</h3>
 * <p>Each cache instance tracks the source version it was built against.
 * When the source changes, callers invoke {@link #invalidateIfStale(int)}
 * to clear all entries if the version has drifted.</p>
 *
 * <h3>Thread safety</h3>
 * <p>This class is <strong>not</strong> thread-safe. It is designed to be
 * accessed exclusively from the analysis thread. If cross-thread access
 * is needed in the future, external synchronization must be applied.</p>
 *
 * @since Phase 2 — CST editor architecture
 */
public final class InferenceCache {

    private final Map<Integer, Object> objectLiterals;
    private final Map<Integer, Object> lambdas;
    private final Map<String, Object> samContexts;
    private int sourceVersion;

    /**
     * Creates a new, empty {@code InferenceCache} at source version 0.
     */
    public InferenceCache() {
        this.objectLiterals = new HashMap<Integer, Object>();
        this.lambdas = new HashMap<Integer, Object>();
        this.samContexts = new HashMap<String, Object>();
        this.sourceVersion = 0;
    }

    // ── Source version tracking ────────────────────────────────────────

    /**
     * Sets the source version this cache is valid for.
     *
     * <p>This does <em>not</em> automatically invalidate existing entries.
     * Call {@link #invalidateIfStale(int)} for conditional invalidation,
     * or {@link #invalidateAll()} to force a full clear.</p>
     *
     * @param version the new source version
     */
    public void setSourceVersion(int version) {
        this.sourceVersion = version;
    }

    /**
     * Returns the source version this cache was last set to.
     *
     * @return the current source version
     */
    public int getSourceVersion() {
        return sourceVersion;
    }

    // ── Invalidation ───────────────────────────────────────────────────

    /**
     * Clears all three cache stores unconditionally.
     */
    public void invalidateAll() {
        objectLiterals.clear();
        lambdas.clear();
        samContexts.clear();
    }

    /**
     * Clears all caches if the current version does not match the given
     * version, then updates the stored version.
     *
     * <p>This is the standard entry point at the start of an analysis pass:
     * if the source has changed since the last pass, all cached results are
     * stale and must be discarded.</p>
     *
     * @param currentVersion the version to check against
     */
    public void invalidateIfStale(int currentVersion) {
        if (this.sourceVersion != currentVersion) {
            invalidateAll();
            this.sourceVersion = currentVersion;
        }
    }

    /**
     * Removes all offset-keyed cache entries whose key falls within the
     * half-open range {@code [startByte, endByte)}.
     *
     * <p>This supports fine-grained incremental invalidation: when a small
     * edit occurs, only entries overlapping the edited range need to be
     * discarded. The SAM context cache (string-keyed) is left untouched
     * since method names are not offset-based.</p>
     *
     * @param startByte inclusive start of the invalidation range
     * @param endByte   exclusive end of the invalidation range
     */
    public void invalidateRange(int startByte, int endByte) {
        removeKeysInRange(objectLiterals, startByte, endByte);
        removeKeysInRange(lambdas, startByte, endByte);
    }

    // ── Object literal cache ───────────────────────────────────────────

    /**
     * Stores an object-literal analysis result keyed by brace-start offset.
     *
     * @param braceStartOffset byte offset of the opening brace
     * @param analysis         the analysis result (typically an
     *                         {@code ObjectLiteralAnalysis} from the legacy package)
     */
    public void putObjectLiteral(int braceStartOffset, Object analysis) {
        objectLiterals.put(braceStartOffset, analysis);
    }

    /**
     * Retrieves a cached object-literal analysis by brace-start offset.
     *
     * @param braceStartOffset byte offset of the opening brace
     * @param <T>              the expected result type
     * @return the cached analysis, or {@code null} if not present
     */
    @SuppressWarnings("unchecked")
    public <T> T getObjectLiteral(int braceStartOffset) {
        return (T) objectLiterals.get(braceStartOffset);
    }

    /**
     * Checks whether an object-literal analysis is cached for the given offset.
     *
     * @param braceStartOffset byte offset of the opening brace
     * @return {@code true} if a cached entry exists
     */
    public boolean hasObjectLiteral(int braceStartOffset) {
        return objectLiterals.containsKey(braceStartOffset);
    }

    /**
     * Removes a single object-literal cache entry.
     *
     * @param braceStartOffset byte offset of the opening brace
     */
    public void removeObjectLiteral(int braceStartOffset) {
        objectLiterals.remove(braceStartOffset);
    }

    // ── Lambda cache ───────────────────────────────────────────────────

    /**
     * Stores a lambda inference result keyed by header-start offset.
     *
     * @param headerStartOffset byte offset of the lambda header start
     * @param cacheEntry        the inference result (typically a
     *                          {@code LambdaCacheEntry} from the legacy package)
     */
    public void putLambda(int headerStartOffset, Object cacheEntry) {
        lambdas.put(headerStartOffset, cacheEntry);
    }

    /**
     * Retrieves a cached lambda inference by header-start offset.
     *
     * @param headerStartOffset byte offset of the lambda header start
     * @param <T>               the expected result type
     * @return the cached entry, or {@code null} if not present
     */
    @SuppressWarnings("unchecked")
    public <T> T getLambda(int headerStartOffset) {
        return (T) lambdas.get(headerStartOffset);
    }

    /**
     * Checks whether a lambda inference is cached for the given offset.
     *
     * @param headerStartOffset byte offset of the lambda header start
     * @return {@code true} if a cached entry exists
     */
    public boolean hasLambda(int headerStartOffset) {
        return lambdas.containsKey(headerStartOffset);
    }

    /**
     * Removes a single lambda cache entry.
     *
     * @param headerStartOffset byte offset of the lambda header start
     */
    public void removeLambda(int headerStartOffset) {
        lambdas.remove(headerStartOffset);
    }

    // ── SAM context cache ──────────────────────────────────────────────

    /**
     * Stores a SAM context (method info) keyed by method name.
     *
     * @param methodName the method name serving as cache key
     * @param methodInfo the SAM context info (typically a {@code MethodInfo}
     *                   from the legacy package)
     */
    public void putSamContext(String methodName, Object methodInfo) {
        samContexts.put(methodName, methodInfo);
    }

    /**
     * Retrieves a cached SAM context by method name.
     *
     * @param methodName the method name
     * @param <T>        the expected result type
     * @return the cached entry, or {@code null} if not present
     */
    @SuppressWarnings("unchecked")
    public <T> T getSamContext(String methodName) {
        return (T) samContexts.get(methodName);
    }

    /**
     * Checks whether a SAM context is cached for the given method name.
     *
     * @param methodName the method name
     * @return {@code true} if a cached entry exists
     */
    public boolean hasSamContext(String methodName) {
        return samContexts.containsKey(methodName);
    }

    /**
     * Removes a single SAM context cache entry.
     *
     * @param methodName the method name
     */
    public void removeSamContext(String methodName) {
        samContexts.remove(methodName);
    }

    // ── Statistics ─────────────────────────────────────────────────────

    /**
     * @return the number of cached object-literal analyses
     */
    public int objectLiteralCount() {
        return objectLiterals.size();
    }

    /**
     * @return the number of cached lambda inferences
     */
    public int lambdaCount() {
        return lambdas.size();
    }

    /**
     * @return the number of cached SAM contexts
     */
    public int samContextCount() {
        return samContexts.size();
    }

    /**
     * @return the total number of cached entries across all three stores
     */
    public int totalSize() {
        return objectLiterals.size() + lambdas.size() + samContexts.size();
    }

    /**
     * @return {@code true} if all three stores are empty
     */
    public boolean isEmpty() {
        return objectLiterals.isEmpty() && lambdas.isEmpty() && samContexts.isEmpty();
    }

    // ── Offset adjustment ──────────────────────────────────────────────

    /**
     * Adjusts all offset-keyed cache entries to account for an edit.
     *
     * <p>Any entry whose byte-offset key is strictly greater than
     * {@code editStart} has its key shifted by {@code delta}. Entries at
     * or before {@code editStart} are left in place. The SAM context
     * cache (string-keyed) is not affected.</p>
     *
     * <p>This is used for incremental updates: after a small insertion or
     * deletion, existing cache entries that follow the edit point can be
     * reused if their keys are shifted to match the new byte layout.</p>
     *
     * @param editStart the byte offset where the edit occurred
     * @param delta     the number of bytes inserted (positive) or removed
     *                  (negative)
     */
    public void adjustOffsets(int editStart, int delta) {
        if (delta == 0) {
            return;
        }
        adjustOffsetsInMap(objectLiterals, editStart, delta);
        adjustOffsetsInMap(lambdas, editStart, delta);
    }

    // ── Internal helpers ───────────────────────────────────────────────

    /**
     * Removes entries from an integer-keyed map whose keys fall within
     * {@code [startByte, endByte)}.
     */
    private static void removeKeysInRange(Map<Integer, Object> map,
                                          int startByte, int endByte) {
        if (map.isEmpty()) {
            return;
        }
        Iterator<Integer> it = map.keySet().iterator();
        while (it.hasNext()) {
            int key = it.next();
            if (key >= startByte && key < endByte) {
                it.remove();
            }
        }
    }

    /**
     * Shifts keys in an integer-keyed map: any key strictly greater than
     * {@code editStart} is replaced by {@code key + delta}. The map is
     * rebuilt to avoid concurrent-modification issues with in-place shifts.
     */
    private static void adjustOffsetsInMap(Map<Integer, Object> map,
                                           int editStart, int delta) {
        if (map.isEmpty()) {
            return;
        }
        Map<Integer, Object> adjusted = new HashMap<Integer, Object>(map.size());
        for (Map.Entry<Integer, Object> entry : map.entrySet()) {
            int key = entry.getKey();
            if (key > editStart) {
                int newKey = key + delta;
                if (newKey >= 0) {
                    adjusted.put(newKey, entry.getValue());
                }
                // Entries shifted to negative offsets are silently discarded
                // (they fell into the deleted region).
            } else {
                adjusted.put(key, entry.getValue());
            }
        }
        map.clear();
        map.putAll(adjusted);
    }

    @Override
    public String toString() {
        return "InferenceCache{version=" + sourceVersion
                + ", objectLiterals=" + objectLiterals.size()
                + ", lambdas=" + lambdas.size()
                + ", samContexts=" + samContexts.size() + "}";
    }
}
