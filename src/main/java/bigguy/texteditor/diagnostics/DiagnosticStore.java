package bigguy.texteditor.diagnostics;

import bigguy.texteditor.diagnostics.Diagnostic.Severity;
import bigguy.texteditor.diagnostics.Diagnostic.Source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe, centralized storage for diagnostics with query capabilities.
 *
 * <p>Diagnostics are added from background analysis threads and read from the
 * UI thread. All mutation operations increment the internal {@link #version()},
 * allowing consumers to detect changes cheaply via version comparison.</p>
 *
 * <p>All query methods return unmodifiable snapshots — callers never see
 * the internal mutable list.</p>
 */
public final class DiagnosticStore {

    private final CopyOnWriteArrayList<Diagnostic> diagnostics = new CopyOnWriteArrayList<>();
    private final AtomicInteger version = new AtomicInteger(0);

    /**
     * Adds a diagnostic to the store.
     *
     * @param diagnostic the diagnostic to add
     * @throws NullPointerException if {@code diagnostic} is null
     */
    public void addDiagnostic(Diagnostic diagnostic) {
        if (diagnostic == null) {
            throw new NullPointerException("diagnostic must not be null");
        }
        diagnostics.add(diagnostic);
        version.incrementAndGet();
    }

    /**
     * Returns all diagnostics whose range overlaps the given byte interval.
     * Useful for rendering underlines on visible lines.
     *
     * @param startByte inclusive start of the query interval
     * @param endByte   exclusive end of the query interval
     * @return an unmodifiable list of matching diagnostics
     */
    public List<Diagnostic> getDiagnosticsInRange(int startByte, int endByte) {
        List<Diagnostic> result = new ArrayList<>();
        for (Diagnostic d : diagnostics) {
            if (d.range().getStartByte() < endByte && startByte < d.range().getEndByte()) {
                result.add(d);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns all diagnostics produced by the given analysis phase.
     *
     * @param source the originating analysis phase
     * @return an unmodifiable list of matching diagnostics
     */
    public List<Diagnostic> getDiagnosticsBySource(Source source) {
        List<Diagnostic> result = new ArrayList<>();
        for (Diagnostic d : diagnostics) {
            if (d.source() == source) {
                result.add(d);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns all diagnostics with the given severity level.
     *
     * @param severity the severity to filter by
     * @return an unmodifiable list of matching diagnostics
     */
    public List<Diagnostic> getDiagnosticsBySeverity(Severity severity) {
        List<Diagnostic> result = new ArrayList<>();
        for (Diagnostic d : diagnostics) {
            if (d.severity() == severity) {
                result.add(d);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns an unmodifiable snapshot of all diagnostics.
     *
     * @return all stored diagnostics
     */
    public List<Diagnostic> getAll() {
        return Collections.unmodifiableList(new ArrayList<>(diagnostics));
    }

    /** Removes all diagnostics from the store. */
    public void clear() {
        if (!diagnostics.isEmpty()) {
            diagnostics.clear();
            version.incrementAndGet();
        }
    }

    /**
     * Removes all diagnostics produced by the given analysis phase.
     * Useful for incremental invalidation when only one phase reruns.
     *
     * @param source the analysis phase whose diagnostics should be removed
     */
    public void clearBySource(Source source) {
        boolean removed = diagnostics.removeIf(d -> d.source() == source);
        if (removed) {
            version.incrementAndGet();
        }
    }

    /** @return the number of diagnostics with {@link Severity#ERROR} */
    public int errorCount() {
        int count = 0;
        for (Diagnostic d : diagnostics) {
            if (d.isError()) {
                count++;
            }
        }
        return count;
    }

    /** @return the number of diagnostics with {@link Severity#WARNING} */
    public int warningCount() {
        int count = 0;
        for (Diagnostic d : diagnostics) {
            if (d.isWarning()) {
                count++;
            }
        }
        return count;
    }

    /** @return the total number of stored diagnostics */
    public int size() {
        return diagnostics.size();
    }

    /** @return {@code true} if no diagnostics are stored */
    public boolean isEmpty() {
        return diagnostics.isEmpty();
    }

    /**
     * Returns a monotonically increasing version number that increments
     * on every mutation. Consumers can compare versions to detect changes
     * without re-scanning the full list.
     *
     * @return the current version
     */
    public int version() {
        return version.get();
    }

    @Override
    public String toString() {
        return "DiagnosticStore{size=" + diagnostics.size()
                + ", errors=" + errorCount()
                + ", warnings=" + warningCount()
                + ", version=" + version.get() + "}";
    }
}
