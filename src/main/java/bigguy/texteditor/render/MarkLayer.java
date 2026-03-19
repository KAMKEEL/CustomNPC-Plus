package bigguy.texteditor.render;

import noppes.npcs.client.gui.util.script.interpreter.token.TokenType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * Merges three independent mark sources — syntax, semantic, and error — into
 * a single non-overlapping list resolved by {@link TokenType#getPriority()}.
 *
 * <p>Thread safety: syntax marks are set from the UI thread while semantic marks
 * arrive from a background analysis thread. All mutators are synchronized;
 * {@link #merge()} produces a snapshot safe for the caller to iterate without
 * holding the lock.
 *
 * <p>A monotonically increasing {@link #getRevision() revision} counter lets
 * consumers detect stale merge results cheaply.
 */
public class MarkLayer {

    private List<Mark> syntaxMarks = Collections.emptyList();
    private List<Mark> semanticMarks = Collections.emptyList();
    private List<Mark> errorMarks = Collections.emptyList();
    private volatile int revision;

    /**
     * Replace the syntax mark layer (produced by tree-sitter highlight queries).
     *
     * @param marks new syntax marks; must not be null
     */
    public synchronized void setSyntaxMarks(List<Mark> marks) {
        this.syntaxMarks = new ArrayList<>(marks);
        revision++;
    }

    /**
     * Replace the semantic mark layer (produced by type resolution / scope analysis).
     * Typically called from a background thread.
     *
     * @param marks new semantic marks; must not be null
     */
    public synchronized void setSemanticMarks(List<Mark> marks) {
        this.semanticMarks = new ArrayList<>(marks);
        revision++;
    }

    /**
     * Replace the error mark layer (diagnostic underlines).
     * Error marks do not participate in color conflict resolution —
     * they are kept in a separate channel and drawn as underlines.
     *
     * @param marks new error marks; must not be null
     */
    public synchronized void setErrorMarks(List<Mark> marks) {
        this.errorMarks = new ArrayList<>(marks);
        revision++;
    }

    /** @return the current syntax marks (defensive copy) */
    public synchronized List<Mark> getSyntaxMarks() {
        return new ArrayList<>(syntaxMarks);
    }

    /** @return the current semantic marks (defensive copy) */
    public synchronized List<Mark> getSemanticMarks() {
        return new ArrayList<>(semanticMarks);
    }

    /**
     * Error marks are kept in their own channel and drawn as underlines
     * rather than merged into the color layer.
     *
     * @return the current error marks (defensive copy)
     */
    public synchronized List<Mark> getErrorMarks() {
        return new ArrayList<>(errorMarks);
    }

    /**
     * Monotonically increasing counter that increments on every mutation.
     * Compare against a cached value to detect stale merge results.
     *
     * @return current revision
     */
    public int getRevision() {
        return revision;
    }

    /**
     * Merge syntax and semantic marks into a single non-overlapping list.
     * Error marks are NOT included — they live in a separate rendering channel.
     *
     * <p>Algorithm:
     * <ol>
     *   <li>Combine syntax + semantic marks and sort by start position</li>
     *   <li>Walk left-to-right through a "sweep line" over every boundary point</li>
     *   <li>At each boundary, the active mark with the highest
     *       {@link TokenType#getPriority()} wins</li>
     *   <li>Semantic marks override syntax marks for the same range
     *       (semantic priority is typically equal or higher)</li>
     *   <li>Output non-overlapping marks sorted by start position</li>
     * </ol>
     *
     * @return merged, non-overlapping mark list (safe to iterate without synchronization)
     */
    public List<Mark> merge() {
        List<Mark> syntax;
        List<Mark> semantic;
        synchronized (this) {
            syntax = new ArrayList<>(syntaxMarks);
            semantic = new ArrayList<>(semanticMarks);
        }

        if (syntax.isEmpty() && semantic.isEmpty()) {
            return Collections.emptyList();
        }

        // Build identity set for O(1) semantic membership checks
        Set<Mark> semanticSet = Collections.newSetFromMap(new IdentityHashMap<>());
        semanticSet.addAll(semantic);

        // Collect all boundary points
        List<Integer> boundaries = new ArrayList<>();
        for (Mark m : syntax) {
            boundaries.add(m.start);
            boundaries.add(m.end);
        }
        for (Mark m : semantic) {
            boundaries.add(m.start);
            boundaries.add(m.end);
        }
        Collections.sort(boundaries);
        // Deduplicate
        List<Integer> uniqueBoundaries = new ArrayList<>();
        for (int i = 0; i < boundaries.size(); i++) {
            if (i == 0 || !boundaries.get(i).equals(boundaries.get(i - 1))) {
                uniqueBoundaries.add(boundaries.get(i));
            }
        }

        List<Mark> combined = new ArrayList<>(syntax.size() + semantic.size());
        combined.addAll(syntax);
        combined.addAll(semantic);

        List<Mark> result = new ArrayList<>();

        // For each interval between consecutive boundaries, find the winning mark
        for (int i = 0; i < uniqueBoundaries.size() - 1; i++) {
            int intervalStart = uniqueBoundaries.get(i);
            int intervalEnd = uniqueBoundaries.get(i + 1);
            if (intervalStart >= intervalEnd) {
                continue;
            }

            Mark winner = null;
            boolean winnerIsSemantic = false;

            for (Mark m : combined) {
                // Mark must fully cover this interval to be a candidate
                if (m.start > intervalStart || m.end < intervalEnd) {
                    continue;
                }

                if (m.start <= intervalStart && m.end >= intervalEnd) {
                    boolean isSemantic = semanticSet.contains(m);

                    if (winner == null) {
                        winner = m;
                        winnerIsSemantic = isSemantic;
                    } else if (isSemantic && !winnerIsSemantic) {
                        // Semantic always overrides syntax for same range
                        winner = m;
                        winnerIsSemantic = true;
                    } else if (isSemantic == winnerIsSemantic) {
                        // Same source layer: higher priority wins
                        if (m.type.getPriority() > winner.type.getPriority()) {
                            winner = m;
                        }
                    }
                }
            }

            if (winner != null) {
                // Try to merge with previous result if same type and metadata and adjacent
                if (!result.isEmpty()) {
                    Mark prev = result.get(result.size() - 1);
                    if (prev.end == intervalStart
                            && prev.type == winner.type
                            && prev.metadata == winner.metadata) {
                        result.set(result.size() - 1,
                                new Mark(prev.start, intervalEnd, prev.type, prev.metadata));
                        continue;
                    }
                }
                result.add(new Mark(intervalStart, intervalEnd, winner.type, winner.metadata));
            }
        }

        return result;
    }

    /** Remove all marks from every layer and increment the revision. */
    public synchronized void clear() {
        syntaxMarks = Collections.emptyList();
        semanticMarks = Collections.emptyList();
        errorMarks = Collections.emptyList();
        revision++;
    }

    @Override
    public String toString() {
        return "MarkLayer{syntax=" + syntaxMarks.size()
                + ", semantic=" + semanticMarks.size()
                + ", error=" + errorMarks.size()
                + ", rev=" + revision + "}";
    }
}
