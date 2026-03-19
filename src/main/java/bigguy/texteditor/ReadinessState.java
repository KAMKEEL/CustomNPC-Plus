package bigguy.texteditor;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Models the readiness lifecycle of the script editor's semantic pipeline.
 *
 * <p>Inspired by IntelliJ IDEA's {@code DumbService} dumb/smart mode pattern,
 * this enum tracks how much of the analysis pipeline has completed and which
 * editor features are currently safe to invoke.</p>
 *
 * <h3>State machine</h3>
 * <pre>{@code
 *   UNINITIALIZED ──→ SYNTAX_READY   (after initial CST parse)
 *   SYNTAX_READY  ──→ ANALYZING      (background semantic analysis started)
 *   ANALYZING     ──→ SMART          (analysis complete — all features available)
 *   ANALYZING     ──→ DEGRADED       (analysis failed — fall back to syntax-only)
 *   Any state     ──→ SYNTAX_READY   (text changed — semantics invalidated)
 * }</pre>
 *
 * <h3>Feature gating</h3>
 * <p>Each state advertises which editor capabilities are available via
 * {@link #isSyntaxAvailable()} and {@link #isSemanticsAvailable()}.
 * UI code should query these predicates rather than comparing states
 * directly, so that new states can be added without breaking callers.</p>
 *
 * <h3>Thread safety</h3>
 * <p>Enum constants are inherently immutable. The state <em>variable</em> holding
 * the current readiness should be protected by whatever concurrency mechanism
 * the orchestrator uses (e.g. {@code AtomicReference<ReadinessState>}).</p>
 */
public enum ReadinessState {

    /**
     * No parse tree exists yet. The editor is essentially a plain text area.
     * No syntax highlighting, no autocomplete, no diagnostics.
     */
    UNINITIALIZED("Uninitialized — no parse tree", false, false, false),

    /**
     * A concrete syntax tree is available. Tree-sitter highlighting, bracket
     * matching, and keyword-only autocomplete are functional. Semantic features
     * (type resolution, full autocomplete, error underlines) are not yet ready.
     */
    SYNTAX_READY("Syntax ready — CST available, semantics pending", true, false, false),

    /**
     * Background semantic analysis is in progress. The same features as
     * {@link #SYNTAX_READY} are available; callers should show a progress
     * indicator but must not block on the result.
     */
    ANALYZING("Analyzing — semantic pass in progress", true, false, true),

    /**
     * Full analysis is complete. Every editor feature is available: error
     * underlines, scope-aware autocomplete, hover tooltips, go-to-definition,
     * and scope coloring.
     */
    SMART("Smart — full analysis complete", true, true, false),

    /**
     * Semantic analysis failed (e.g. the type registry could not load, or an
     * internal error occurred). The editor falls back to syntax-only mode
     * and surface-level autocomplete. This state is recoverable: the next
     * text change will transition back to {@link #SYNTAX_READY}.
     */
    DEGRADED("Degraded — analysis failed, syntax-only fallback", true, false, false);

    private static final Set<ReadinessState> FROM_UNINITIALIZED =
            Collections.unmodifiableSet(EnumSet.of(SYNTAX_READY));

    private static final Set<ReadinessState> FROM_SYNTAX_READY =
            Collections.unmodifiableSet(EnumSet.of(ANALYZING, SYNTAX_READY));

    private static final Set<ReadinessState> FROM_ANALYZING =
            Collections.unmodifiableSet(EnumSet.of(SMART, DEGRADED, SYNTAX_READY));

    private static final Set<ReadinessState> FROM_SMART =
            Collections.unmodifiableSet(EnumSet.of(SYNTAX_READY));

    private static final Set<ReadinessState> FROM_DEGRADED =
            Collections.unmodifiableSet(EnumSet.of(SYNTAX_READY));

    private final String description;
    private final boolean syntaxAvailable;
    private final boolean semanticsAvailable;
    private final boolean analyzing;

    ReadinessState(String description, boolean syntaxAvailable,
                   boolean semanticsAvailable, boolean analyzing) {
        this.description = description;
        this.syntaxAvailable = syntaxAvailable;
        this.semanticsAvailable = semanticsAvailable;
        this.analyzing = analyzing;
    }

    /**
     * Returns {@code true} when a concrete syntax tree is available.
     *
     * <p>When syntax is available, the following features are safe to use:</p>
     * <ul>
     *   <li>Tree-sitter syntax highlighting</li>
     *   <li>Bracket matching (CST-based)</li>
     *   <li>Keyword-only autocomplete</li>
     * </ul>
     *
     * @return whether CST-based features are available
     */
    public boolean isSyntaxAvailable() {
        return syntaxAvailable;
    }

    /**
     * Returns {@code true} when full semantic analysis has completed successfully.
     *
     * <p>When semantics are available, the following features are safe to use:</p>
     * <ul>
     *   <li>Error underlines and diagnostic rendering</li>
     *   <li>Full (scope-aware, type-aware) autocomplete</li>
     *   <li>Hover tooltips with type information</li>
     *   <li>Go-to-definition navigation</li>
     *   <li>Scope coloring</li>
     * </ul>
     *
     * @return whether semantic features are available
     */
    public boolean isSemanticsAvailable() {
        return semanticsAvailable;
    }

    /**
     * Returns {@code true} when background analysis is currently in progress.
     *
     * <p>UI code can use this to display a progress indicator without blocking.</p>
     *
     * @return whether analysis is running
     */
    public boolean isAnalyzing() {
        return analyzing;
    }

    /**
     * Checks whether a transition from this state to {@code next} is valid
     * according to the state machine's transition rules.
     *
     * @param next the proposed target state
     * @return {@code true} if the transition is allowed
     * @throws NullPointerException if {@code next} is {@code null}
     */
    public boolean canTransitionTo(ReadinessState next) {
        if (next == null) {
            throw new NullPointerException("Target state must not be null");
        }
        return validTargets().contains(next);
    }

    /**
     * Returns the set of states that this state may transition to.
     *
     * @return an unmodifiable set of valid target states
     */
    public Set<ReadinessState> validTargets() {
        switch (this) {
            case UNINITIALIZED: return FROM_UNINITIALIZED;
            case SYNTAX_READY:  return FROM_SYNTAX_READY;
            case ANALYZING:     return FROM_ANALYZING;
            case SMART:         return FROM_SMART;
            case DEGRADED:      return FROM_DEGRADED;
            default:            return Collections.emptySet();
        }
    }

    /**
     * Returns a human-readable description of this state, suitable for
     * status bar display or debug logging.
     *
     * @return a descriptive string (never {@code null})
     */
    @Override
    public String toString() {
        return description;
    }
}
