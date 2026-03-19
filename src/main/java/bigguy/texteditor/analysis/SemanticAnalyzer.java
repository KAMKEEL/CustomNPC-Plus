package bigguy.texteditor.analysis;

import bigguy.texteditor.analysis.IncrementalStrategy.InvalidationScope;
import bigguy.texteditor.semantics.SemanticModel;
import bigguy.texteditor.syntax.DocumentSyntax;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Owns the background semantic analysis pipeline and schedules semantic
 * analysis after syntax parsing completes.
 *
 * <p>This class is the bridge between the UI-thread syntax pipeline and the
 * background semantic pipeline. It is called by the orchestrator after each
 * text change and follows a strict revision-based protocol to prevent stale
 * semantic data from contaminating fresh syntax state.</p>
 *
 * <h3>Lifecycle</h3>
 * <ol>
 *   <li>The orchestrator calls {@link #scheduleAnalysis} or
 *       {@link #scheduleIncrementalAnalysis} after each text change.</li>
 *   <li>Any in-flight analysis is cancelled immediately.</li>
 *   <li>An immutable snapshot of the current state is captured and submitted
 *       to a single-thread {@link ExecutorService}.</li>
 *   <li>On completion, results are published back to the caller via an
 *       {@link AnalysisCallback}.</li>
 *   <li>Stale results (where the revision has advanced since submission)
 *       are silently discarded.</li>
 * </ol>
 *
 * <h3>Stale-result protocol</h3>
 * <p>The key invariant is: <strong>never blend stale semantic data with fresh
 * syntax</strong>. When a revision changes:</p>
 * <ol>
 *   <li>Drop ALL semantic decorations immediately.</li>
 *   <li>Show syntax-only highlighting.</li>
 *   <li>When semantic analysis completes for the <em>current</em> revision,
 *       apply atomically.</li>
 *   <li>When semantic analysis completes for an <em>old</em> revision,
 *       discard silently.</li>
 * </ol>
 *
 * <h3>Thread safety</h3>
 * <p>All public methods are safe to call from any thread. The executor is a
 * single-thread pool, so analysis tasks are serialised. The {@code disposed}
 * and {@code inFlightAnalysis} fields use {@code volatile} for visibility
 * across threads, and the {@code currentRevision} uses {@link AtomicLong}
 * for atomic updates.</p>
 *
 * <h3>Callback threading</h3>
 * <p>The {@link AnalysisCallback} is invoked on the <em>background</em>
 * analysis thread, not the UI thread. Callers (typically the orchestrator)
 * must handle the thread boundary themselves &mdash; for example, via
 * Minecraft's scheduled-task system &mdash; before applying results to
 * UI-visible state.</p>
 *
 * @see SemanticModel
 * @see IncrementalStrategy
 * @see DocumentSyntax
 */
public final class SemanticAnalyzer {

    private static final Logger LOGGER = Logger.getLogger(SemanticAnalyzer.class.getName());

    /**
     * Name assigned to the daemon analysis thread. Appears in thread dumps
     * and debugger views for easy identification.
     */
    private static final String THREAD_NAME = "ScriptEditor-SemanticAnalysis";

    /**
     * Maximum time (in milliseconds) to wait for the executor to terminate
     * during {@link #dispose()}.
     */
    private static final long SHUTDOWN_TIMEOUT_MS = 2000L;

    // ── Callback interface ─────────────────────────────────────────────────────

    /**
     * Callback interface for receiving analysis results.
     *
     * <p>The orchestrator implements this to apply semantic marks and
     * transition state. <strong>Important:</strong> callbacks are invoked on
     * the background analysis thread, not the UI thread. Implementors must
     * marshal results to the appropriate thread before modifying UI state.</p>
     *
     * <p>The caller <strong>must</strong> check that the revision still matches
     * the current document revision before applying results. The analyzer
     * performs its own revision check before invoking the callback, but there
     * is an inherent race window between the check and the callback body
     * executing, so a second check in the callback is recommended.</p>
     */
    public interface AnalysisCallback {

        /**
         * Called when analysis completes successfully for the given revision.
         *
         * <p>The caller <strong>must</strong> verify that the revision still
         * matches the current document revision before applying results.
         * If the revision has advanced, these results are stale and must
         * be discarded.</p>
         *
         * @param revision      the revision number this analysis was for
         * @param semanticModel the populated semantic model
         * @param syntax        the {@link DocumentSyntax} that was analyzed
         */
        void onAnalysisComplete(long revision, SemanticModel semanticModel, DocumentSyntax syntax);

        /**
         * Called when analysis fails with an exception.
         *
         * <p>The revision is provided so the caller can determine whether
         * the failure is still relevant to the current document state.</p>
         *
         * @param revision the revision number this analysis was for
         * @param error    the exception that occurred during analysis
         */
        void onAnalysisFailed(long revision, Exception error);
    }

    // ── Fields ─────────────────────────────────────────────────────────────────

    /**
     * Single-thread executor for background analysis. Uses a daemon thread
     * so it does not prevent JVM (Minecraft) shutdown.
     */
    private final ExecutorService executor;

    /**
     * The semantic model being built and updated. Owned exclusively by this
     * analyzer; only accessed from the analysis thread (except for
     * {@link #getSemanticModel()} which returns a snapshot reference).
     */
    private final SemanticModel semanticModel;

    /**
     * Stateless strategy for classifying edits into invalidation scopes.
     * Used by the incremental analysis path to determine which parts of
     * the semantic model need rebuilding.
     */
    private final IncrementalStrategy strategy;

    /**
     * The currently in-flight analysis {@link Future}, or {@code null} if
     * no analysis is running. Volatile for cross-thread visibility.
     */
    private volatile Future<?> inFlightAnalysis;

    /**
     * The revision number that was most recently submitted for analysis.
     * Used for stale-result detection: if the revision has advanced past
     * this value by the time analysis completes, the results are discarded.
     */
    private final AtomicLong currentRevision;

    /**
     * Set to {@code true} when {@link #dispose()} is called. Prevents new
     * analysis submissions and causes in-flight analysis to bail out early.
     */
    private volatile boolean disposed;

    // ── Constructor ────────────────────────────────────────────────────────────

    /**
     * Creates a new {@code SemanticAnalyzer} with a single-thread daemon
     * executor, a fresh {@link SemanticModel}, and a stateless
     * {@link IncrementalStrategy}.
     *
     * <p>The executor uses a daemon thread with slightly lower than normal
     * priority ({@link Thread#NORM_PRIORITY} - 1) so that analysis does not
     * compete with UI rendering for CPU time.</p>
     */
    public SemanticAnalyzer() {
        this.executor = Executors.newSingleThreadExecutor(createDaemonThreadFactory());
        this.semanticModel = new SemanticModel();
        this.strategy = new IncrementalStrategy();
        this.inFlightAnalysis = null;
        this.currentRevision = new AtomicLong(0L);
        this.disposed = false;
    }

    // ── Public API ─────────────────────────────────────────────────────────────

    /**
     * Schedules a full semantic analysis for the given syntax snapshot.
     *
     * <p>Any currently in-flight analysis is cancelled before scheduling the
     * new one. The revision number is recorded so that stale results can be
     * detected and discarded when the analysis completes.</p>
     *
     * <p>The analysis performs a complete rebuild of the {@link SemanticModel}:
     * all sub-components (SymbolTable, ScopeTree, ImportIndex, InferenceCache)
     * are cleared and rebuilt from scratch by walking the CST.</p>
     *
     * <p>If the analyzer has been {@linkplain #dispose() disposed}, this
     * method is a no-op.</p>
     *
     * @param syntax   the document syntax to analyze (immutable snapshot from
     *                 the parser); must not be {@code null}
     * @param revision the revision number for stale-detection; callers should
     *                 use a monotonically increasing counter
     * @param callback the callback to invoke with results; must not be
     *                 {@code null}
     * @throws IllegalArgumentException if {@code syntax} or {@code callback}
     *                                  is {@code null}
     */
    public void scheduleAnalysis(DocumentSyntax syntax, long revision, AnalysisCallback callback) {
        validateScheduleArguments(syntax, revision, callback);
        if (disposed) {
            return;
        }

        currentRevision.set(revision);
        cancelInFlight();

        inFlightAnalysis = executor.submit(new FullAnalysisTask(syntax, revision, callback));
    }

    /**
     * Schedules an incremental analysis using the provided invalidation scope.
     *
     * <p>Only rebuilds the affected parts of the semantic model, as determined
     * by the {@link InvalidationScope}:</p>
     * <ul>
     *   <li>{@link InvalidationScope#NONE NONE} &mdash; no-op; the callback
     *       is still invoked with the existing model so the orchestrator can
     *       apply offset-adjusted decorations.</li>
     *   <li>{@link InvalidationScope#LOCAL_SCOPE LOCAL_SCOPE} &mdash; only
     *       the scope tree is rebuilt.</li>
     *   <li>{@link InvalidationScope#DECLARATION DECLARATION} &mdash; full
     *       rebuild (symbol table + scope tree).</li>
     *   <li>{@link InvalidationScope#IMPORTS IMPORTS} &mdash; only the import
     *       index is rebuilt.</li>
     *   <li>{@link InvalidationScope#STRUCTURAL STRUCTURAL} &mdash; full
     *       rebuild of all sub-components.</li>
     * </ul>
     *
     * <p>Any currently in-flight analysis is cancelled before scheduling.
     * If the analyzer has been {@linkplain #dispose() disposed}, this method
     * is a no-op.</p>
     *
     * @param syntax   the document syntax after reparse; must not be
     *                 {@code null}
     * @param revision the revision number for stale-detection
     * @param scope    the invalidation scope from {@link IncrementalStrategy};
     *                 must not be {@code null}
     * @param callback the callback to invoke with results; must not be
     *                 {@code null}
     * @throws IllegalArgumentException if any argument is {@code null}
     */
    public void scheduleIncrementalAnalysis(DocumentSyntax syntax, long revision,
                                            InvalidationScope scope,
                                            AnalysisCallback callback) {
        validateScheduleArguments(syntax, revision, callback);
        if (scope == null) {
            throw new IllegalArgumentException("scope must not be null");
        }
        if (disposed) {
            return;
        }

        currentRevision.set(revision);
        cancelInFlight();

        inFlightAnalysis = executor.submit(
                new IncrementalAnalysisTask(syntax, revision, scope, callback)
        );
    }

    /**
     * Cancels any in-flight analysis. Safe to call from any thread.
     *
     * <p>If an analysis is currently running, it is interrupted via
     * {@link Future#cancel(boolean) cancel(true)}. The background task checks
     * for interruption at key points and will bail out gracefully.</p>
     *
     * <p>After cancellation, {@link #isAnalyzing()} will return {@code false}
     * once the background thread has actually stopped (which may not be
     * immediate if the task is in the middle of a non-interruptible
     * operation).</p>
     */
    public void cancelInFlight() {
        Future<?> current = inFlightAnalysis;
        if (current != null && !current.isDone()) {
            current.cancel(true);
        }
        inFlightAnalysis = null;
    }

    /**
     * Returns whether an analysis is currently running or queued.
     *
     * <p>A return value of {@code true} means there is a {@link Future} that
     * has not yet completed. Note that due to the inherent race between
     * checking and acting, this method is advisory only &mdash; the analysis
     * may complete (or be cancelled) between the check and the caller's next
     * action.</p>
     *
     * @return {@code true} if an analysis task is in flight
     */
    public boolean isAnalyzing() {
        Future<?> current = inFlightAnalysis;
        return current != null && !current.isDone();
    }

    /**
     * Returns the current semantic model.
     *
     * <p>The returned reference is the <em>live</em> model that the background
     * thread mutates during analysis. Callers should be aware that the model's
     * contents may change between calls if an analysis is in flight. For
     * consistent reads, callers should either:</p>
     * <ul>
     *   <li>Read only when {@link #isAnalyzing()} is {@code false}, or</li>
     *   <li>Use the model snapshot provided via {@link AnalysisCallback}.</li>
     * </ul>
     *
     * <p>This method is primarily intended for incremental analysis paths that
     * need access to the previous model state before scheduling an update.</p>
     *
     * @return the semantic model (never {@code null})
     */
    public SemanticModel getSemanticModel() {
        return semanticModel;
    }

    /**
     * Returns the incremental strategy used by this analyzer.
     *
     * <p>The strategy is stateless and can be used by external callers
     * (e.g., the orchestrator) to classify edits before deciding whether
     * to call {@link #scheduleAnalysis} or
     * {@link #scheduleIncrementalAnalysis}.</p>
     *
     * @return the incremental strategy (never {@code null})
     */
    public IncrementalStrategy getStrategy() {
        return strategy;
    }

    /**
     * Returns the revision number that was most recently submitted for
     * analysis.
     *
     * <p>This can be compared against the document's current revision to
     * determine whether the latest analysis results are still relevant.</p>
     *
     * @return the most recently submitted revision number
     */
    public long getCurrentRevision() {
        return currentRevision.get();
    }

    /**
     * Shuts down the background executor and cancels any in-flight analysis.
     *
     * <p><strong>Must</strong> be called when the editor closes to release
     * the background thread. After disposal:</p>
     * <ul>
     *   <li>All {@code schedule*} methods become no-ops.</li>
     *   <li>{@link #isAnalyzing()} returns {@code false}.</li>
     *   <li>Any in-flight analysis bails out without invoking the callback.</li>
     * </ul>
     *
     * <p>This method blocks for up to {@value #SHUTDOWN_TIMEOUT_MS}ms waiting
     * for the executor to terminate. If the executor does not terminate in
     * time, it is forcefully shut down via {@link ExecutorService#shutdownNow()}.
     * </p>
     *
     * <p>Safe to call multiple times; subsequent calls are no-ops.</p>
     */
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;

        cancelInFlight();
        semanticModel.clear();

        executor.shutdown();
        try {
            if (!executor.awaitTermination(SHUTDOWN_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                LOGGER.log(Level.WARNING,
                        "Semantic analysis executor did not terminate within {0}ms; forcing shutdown",
                        SHUTDOWN_TIMEOUT_MS);
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Returns whether this analyzer has been disposed.
     *
     * @return {@code true} if {@link #dispose()} has been called
     */
    public boolean isDisposed() {
        return disposed;
    }

    // ── Analysis tasks ─────────────────────────────────────────────────────────

    /**
     * Runnable that performs a full semantic model rebuild on the background
     * thread. Checks for disposal and revision staleness before and after
     * the rebuild.
     */
    private final class FullAnalysisTask implements Runnable {

        private final DocumentSyntax syntax;
        private final long revision;
        private final AnalysisCallback callback;

        FullAnalysisTask(DocumentSyntax syntax, long revision, AnalysisCallback callback) {
            this.syntax = syntax;
            this.revision = revision;
            this.callback = callback;
        }

        @Override
        public void run() {
            if (shouldAbort()) {
                return;
            }

            try {
                semanticModel.rebuild(syntax);

                if (shouldAbort()) {
                    return;
                }

                deliverSuccess();
            } catch (Exception e) {
                deliverFailure(e);
            }
        }

        /**
         * Checks whether this task should abort execution.
         *
         * <p>Abort conditions:</p>
         * <ul>
         *   <li>The analyzer has been disposed.</li>
         *   <li>The thread has been interrupted (task was cancelled).</li>
         *   <li>The revision has been superseded by a newer submission.</li>
         * </ul>
         *
         * @return {@code true} if the task should stop immediately
         */
        private boolean shouldAbort() {
            return disposed
                    || Thread.currentThread().isInterrupted()
                    || currentRevision.get() != revision;
        }

        /**
         * Delivers the successful analysis result via the callback, after a
         * final staleness check. If the revision has been superseded between
         * the analysis completing and this delivery attempt, the result is
         * silently discarded.
         */
        private void deliverSuccess() {
            if (disposed || currentRevision.get() != revision) {
                return;
            }
            try {
                callback.onAnalysisComplete(revision, semanticModel, syntax);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING,
                        "Exception in AnalysisCallback.onAnalysisComplete for revision " + revision, e);
            }
        }

        /**
         * Delivers a failure notification via the callback, unless the
         * analyzer is disposed or the thread was interrupted (in which case
         * the failure is expected and not reported to the caller).
         */
        private void deliverFailure(Exception error) {
            if (disposed || Thread.currentThread().isInterrupted()) {
                return;
            }
            try {
                callback.onAnalysisFailed(revision, error);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING,
                        "Exception in AnalysisCallback.onAnalysisFailed for revision " + revision, e);
            }
        }
    }

    /**
     * Runnable that performs an incremental semantic model update on the
     * background thread. Uses the {@link InvalidationScope} to determine
     * which sub-components of the model need rebuilding.
     *
     * <p>For {@link InvalidationScope#NONE}, the model is left untouched but
     * the callback is still invoked so the orchestrator can apply
     * offset-adjusted decorations using the existing model state.</p>
     */
    private final class IncrementalAnalysisTask implements Runnable {

        private final DocumentSyntax syntax;
        private final long revision;
        private final InvalidationScope scope;
        private final AnalysisCallback callback;

        IncrementalAnalysisTask(DocumentSyntax syntax, long revision,
                                InvalidationScope scope, AnalysisCallback callback) {
            this.syntax = syntax;
            this.revision = revision;
            this.scope = scope;
            this.callback = callback;
        }

        @Override
        public void run() {
            if (shouldAbort()) {
                return;
            }

            try {
                // For NONE scope, the model is already up-to-date; we still
                // invoke the callback so the orchestrator can re-apply
                // decorations with corrected offsets.
                if (scope != InvalidationScope.NONE) {
                    semanticModel.incrementalUpdate(syntax, scope);
                }

                if (shouldAbort()) {
                    return;
                }

                deliverSuccess();
            } catch (Exception e) {
                deliverFailure(e);
            }
        }

        /**
         * Checks whether this task should abort execution.
         *
         * @return {@code true} if the task should stop immediately
         * @see FullAnalysisTask#shouldAbort()
         */
        private boolean shouldAbort() {
            return disposed
                    || Thread.currentThread().isInterrupted()
                    || currentRevision.get() != revision;
        }

        /**
         * Delivers the successful analysis result via the callback, after a
         * final staleness check.
         */
        private void deliverSuccess() {
            if (disposed || currentRevision.get() != revision) {
                return;
            }
            try {
                callback.onAnalysisComplete(revision, semanticModel, syntax);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING,
                        "Exception in AnalysisCallback.onAnalysisComplete for revision " + revision, e);
            }
        }

        /**
         * Delivers a failure notification via the callback.
         */
        private void deliverFailure(Exception error) {
            if (disposed || Thread.currentThread().isInterrupted()) {
                return;
            }
            try {
                callback.onAnalysisFailed(revision, error);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING,
                        "Exception in AnalysisCallback.onAnalysisFailed for revision " + revision, e);
            }
        }
    }

    // ── Argument validation ────────────────────────────────────────────────────

    /**
     * Validates common arguments for the {@code schedule*} methods.
     *
     * @param syntax   the document syntax (must not be {@code null})
     * @param revision the revision number (informational; no constraint)
     * @param callback the analysis callback (must not be {@code null})
     * @throws IllegalArgumentException if {@code syntax} or {@code callback}
     *                                  is {@code null}
     */
    private static void validateScheduleArguments(DocumentSyntax syntax, long revision,
                                                   AnalysisCallback callback) {
        if (syntax == null) {
            throw new IllegalArgumentException("syntax must not be null");
        }
        if (callback == null) {
            throw new IllegalArgumentException("callback must not be null");
        }
    }

    // ── Thread factory ─────────────────────────────────────────────────────────

    /**
     * Creates a {@link ThreadFactory} that produces a single daemon thread
     * with a descriptive name and slightly reduced priority.
     *
     * <p>The daemon flag ensures the thread does not prevent JVM shutdown
     * (critical in Minecraft's modded environment where the game client may
     * close while analysis is in flight). The reduced priority
     * ({@link Thread#NORM_PRIORITY} - 1) ensures analysis does not starve
     * the render thread.</p>
     *
     * @return a thread factory for the analysis executor
     */
    private static ThreadFactory createDaemonThreadFactory() {
        return new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, THREAD_NAME);
                t.setDaemon(true);
                t.setPriority(Thread.NORM_PRIORITY - 1);
                return t;
            }
        };
    }

    @Override
    public String toString() {
        return "SemanticAnalyzer{"
                + "analyzing=" + isAnalyzing()
                + ", revision=" + currentRevision.get()
                + ", disposed=" + disposed
                + ", model=" + semanticModel
                + '}';
    }
}
