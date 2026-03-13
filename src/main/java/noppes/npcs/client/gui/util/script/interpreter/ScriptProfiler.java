package noppes.npcs.client.gui.util.script.interpreter;

import java.util.*;

/**
 * Profiler for the ScriptDocument parsing/marking pipeline.
 * <p>
 * <b>Usage:</b>
 * <pre>
 *     ScriptProfiler.setEnabled(true);
 *     ScriptProfiler.setReportingThresholdMs(5.0);
 *     // Results auto-print to System.out when root section ends.
 *     // Or: ScriptProfiler.getLastReport();
 * </pre>
 * <p>
 * When disabled, begin/end are single boolean checks with zero overhead.
 * Single-threaded only (Minecraft client thread).
 */
public final class ScriptProfiler {

    private static boolean enabled = false;
    private static double reportingThresholdMs = 0.0;
    private static boolean autoPrint = true;
    private static int maxHistory = 50;

    private static final Deque<ActiveSection> sectionStack = new ArrayDeque<>();
    private static final List<SectionResult> currentResults = new ArrayList<>();
    private static int currentDepth = 0;
    private static int beginSequence = 0;
    private static final Deque<ProfileSnapshot> history = new ArrayDeque<>();
    private static String lastReport = null;
    private static ProfileSnapshot lastSnapshot = null;
    private static final Map<String, AccumulatedStats> accumulatedStats = new LinkedHashMap<>();
    private static int invocationCount = 0;

    public static void setEnabled(boolean enable) {
        enabled = enable;
        if (!enable) {
            reset();
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setReportingThresholdMs(double thresholdMs) {
        reportingThresholdMs = thresholdMs;
    }

    public static void setAutoPrint(boolean auto) {
        autoPrint = auto;
    }

    /**
     * Begin timing a named section. Sections nest: begin("A") -> begin("B") -> end("B") -> end("A").
     */
    public static void begin(String name) {
        if (!enabled) return;
        sectionStack.push(new ActiveSection(name, System.nanoTime(), currentDepth, beginSequence++));
        currentDepth++;
    }

    /**
     * End timing a named section. Must match the most recent begin().
     */
    public static void end(String name) {
        if (!enabled) return;
        if (sectionStack.isEmpty()) {
            System.err.println("[ScriptProfiler] WARNING: end('" + name + "') called with no active section");
            return;
        }

        ActiveSection active = sectionStack.pop();
        currentDepth--;

        if (!active.name.equals(name)) {
            System.err.println("[ScriptProfiler] WARNING: end('" + name + "') doesn't match begin('" + active.name + "')");
        }

        long elapsed = System.nanoTime() - active.startNano;
        currentResults.add(new SectionResult(active.name, active.depth, elapsed, active.beginOrder));

        if (sectionStack.isEmpty()) {
            finalizeReport();
        }
    }

    /**
     * Convenience: time a Runnable.
     */
    public static void time(String name, Runnable work) {
        begin(name);
        try {
            work.run();
        } finally {
            end(name);
        }
    }

    public static String getLastReport() {
        return lastReport;
    }

    public static ProfileSnapshot getLastSnapshot() {
        return lastSnapshot;
    }

    public static List<ProfileSnapshot> getHistory() {
        return new ArrayList<>(history);
    }

    public static Map<String, AccumulatedStats> getAccumulatedStats() {
        return Collections.unmodifiableMap(accumulatedStats);
    }

    public static int getInvocationCount() {
        return invocationCount;
    }

    public static void reset() {
        sectionStack.clear();
        currentResults.clear();
        currentDepth = 0;
        beginSequence = 0;
        history.clear();
        lastReport = null;
        lastSnapshot = null;
        accumulatedStats.clear();
        invocationCount = 0;
    }

    public static void resetStats() {
        history.clear();
        accumulatedStats.clear();
        invocationCount = 0;
    }

    /**
     * Summary of accumulated stats across all invocations -
     * identifies consistent bottlenecks vs. one-off spikes.
     */
    public static String getAccumulatedReport() {
        if (accumulatedStats.isEmpty()) {
            return "[ScriptProfiler] No accumulated data.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\n============================================================\n");
        sb.append("     SCRIPT PROFILER - ACCUMULATED STATS\n");
        sb.append("  Invocations: ").append(invocationCount).append("\n");
        sb.append("============================================================\n");
        sb.append(String.format("  %-30s | %7s | %7s | %7s\n", "Section", "Avg(ms)", "Max(ms)", "Min(ms)"));
        sb.append("------------------------------------------------------------\n");

        for (Map.Entry<String, AccumulatedStats> entry : accumulatedStats.entrySet()) {
            AccumulatedStats stats = entry.getValue();
            String indent = getIndent(stats.depth);
            String label = indent + truncate(entry.getKey(), 30 - indent.length());
            sb.append(String.format("  %-30s | %7.2f | %7.2f | %7.2f\n",
                    label, stats.getAverageMs(), stats.getMaxMs(), stats.getMinMs()));
        }

        sb.append("============================================================\n");
        return sb.toString();
    }

    // ======================== Internals ========================

    private static void finalizeReport() {
        invocationCount++;

        List<SectionResult> results = new ArrayList<>(currentResults);
        Collections.sort(results, new Comparator<SectionResult>() {
            public int compare(SectionResult a, SectionResult b) {
                return Integer.compare(a.beginOrder, b.beginOrder);
            }
        });
        ProfileSnapshot snapshot = new ProfileSnapshot(results, System.currentTimeMillis());
        lastSnapshot = snapshot;

        history.addLast(snapshot);
        while (history.size() > maxHistory) {
            history.removeFirst();
        }

        for (SectionResult r : results) {
            AccumulatedStats stats = accumulatedStats.get(r.name);
            if (stats == null) {
                stats = new AccumulatedStats(r.depth);
                accumulatedStats.put(r.name, stats);
            }
            stats.record(r.elapsedNanos);
        }

        String report = buildReport(snapshot);
        lastReport = report;

        if (autoPrint) {
            double totalMs = snapshot.getTotalMs();
            if (totalMs >= reportingThresholdMs) {
                System.out.println(report);
            }
        }

        currentResults.clear();
        currentDepth = 0;
        beginSequence = 0;
    }

    private static String buildReport(ProfileSnapshot snapshot) {
        List<SectionResult> results = snapshot.results;
        if (results.isEmpty()) return "[ScriptProfiler] Empty report.";

        SectionResult root = null;
        for (SectionResult r : results) {
            if (r.depth == 0) {
                root = r;
                break;
            }
        }
        if (root == null) root = results.get(0);

        double totalMs = root.elapsedNanos / 1_000_000.0;

        StringBuilder sb = new StringBuilder();
        sb.append("\n================================================================\n");
        sb.append("     SCRIPT DOCUMENT PROFILER - Pipeline Breakdown\n");
        sb.append("  Total: ").append(String.format("%.2fms", totalMs));
        sb.append("  |  Lines: ").append(getLineCount());
        sb.append("  |  Chars: ").append(getCharCount());
        sb.append("\n================================================================\n");
        sb.append(String.format("  %-36s | %8s | %5s | %s\n", "Stage", "Time(ms)", "  %  ", "Bar"));
        sb.append("----------------------------------------------------------------\n");

        for (SectionResult r : results) {
            double ms = r.elapsedNanos / 1_000_000.0;
            double pct = (root.elapsedNanos > 0) ? (100.0 * r.elapsedNanos / root.elapsedNanos) : 0;
            String indent = getIndent(r.depth);
            String label = indent + truncate(r.name, 36 - indent.length());
            String bar = makeBar(pct, 8);

            String flag = "";
            if (r.depth > 0 && (pct > 20.0 || ms > 5.0)) {
                flag = " << SLOW";
            }

            sb.append(String.format("  %-36s | %8.2f | %4.1f%% | %-8s %s\n",
                    label, ms, pct, bar, flag));
        }

        long childSum = 0;
        for (SectionResult r : results) {
            if (r.depth == 1) {
                childSum += r.elapsedNanos;
            }
        }
        if (root.elapsedNanos > 0) {
            long unaccounted = root.elapsedNanos - childSum;
            if (unaccounted > 0) {
                double uMs = unaccounted / 1_000_000.0;
                double uPct = 100.0 * unaccounted / root.elapsedNanos;
                sb.append(String.format("  %-36s | %8.2f | %4.1f%% | %-8s\n",
                        "  (overhead/unlisted)", uMs, uPct, makeBar(uPct, 8)));
            }
        }

        sb.append("================================================================\n");
        sb.append(buildBottleneckSummary(results, root.elapsedNanos));

        return sb.toString();
    }

    private static String buildBottleneckSummary(List<SectionResult> results, long totalNanos) {
        if (results.size() <= 1) return "";

        List<SectionResult> sorted = new ArrayList<>();
        for (SectionResult r : results) {
            if (r.depth > 0) sorted.add(r);
        }
        Collections.sort(sorted, new Comparator<SectionResult>() {
            public int compare(SectionResult a, SectionResult b) {
                return Long.compare(b.elapsedNanos, a.elapsedNanos);
            }
        });

        StringBuilder sb = new StringBuilder();
        sb.append("  Top bottlenecks:\n");
        int count = Math.min(3, sorted.size());
        for (int i = 0; i < count; i++) {
            SectionResult r = sorted.get(i);
            double ms = r.elapsedNanos / 1_000_000.0;
            double pct = totalNanos > 0 ? (100.0 * r.elapsedNanos / totalNanos) : 0;
            sb.append(String.format("     %d. %-30s  %7.2fms (%4.1f%%)\n", i + 1, r.name, ms, pct));
        }
        sb.append("\n");
        return sb.toString();
    }

    private static String getIndent(int depth) {
        if (depth <= 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) sb.append("  ");
        return sb.toString();
    }

    private static String truncate(String s, int max) {
        if (max <= 0) return "";
        if (s.length() <= max) return s;
        return s.substring(0, max - 2) + "..";
    }

    private static String makeBar(double pct, int maxLen) {
        int filled = (int) Math.round(pct / 100.0 * maxLen);
        filled = Math.min(filled, maxLen);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < filled; i++) sb.append('#');
        for (int i = filled; i < maxLen; i++) sb.append('.');
        return sb.toString();
    }

    private static String pad(int n) {
        if (n <= 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(' ');
        return sb.toString();
    }

    private static int getLineCount() {
        ScriptDocument doc = ScriptDocument.INSTANCE;
        return doc != null ? doc.getLines().size() : 0;
    }

    private static int getCharCount() {
        ScriptDocument doc = ScriptDocument.INSTANCE;
        return doc != null ? doc.getText().length() : 0;
    }

    // ======================== Data Classes ========================

    private static class ActiveSection {
        final String name;
        final long startNano;
        final int depth;
        final int beginOrder;

        ActiveSection(String name, long startNano, int depth, int beginOrder) {
            this.name = name;
            this.startNano = startNano;
            this.depth = depth;
            this.beginOrder = beginOrder;
        }
    }

    public static class SectionResult {
        public final String name;
        public final int depth;
        public final long elapsedNanos;
        final int beginOrder;

        SectionResult(String name, int depth, long elapsedNanos, int beginOrder) {
            this.name = name;
            this.depth = depth;
            this.elapsedNanos = elapsedNanos;
            this.beginOrder = beginOrder;
        }

        public double getElapsedMs() {
            return elapsedNanos / 1_000_000.0;
        }
    }

    public static class ProfileSnapshot {
        public final List<SectionResult> results;
        public final long timestampMs;

        ProfileSnapshot(List<SectionResult> results, long timestampMs) {
            this.results = Collections.unmodifiableList(results);
            this.timestampMs = timestampMs;
        }

        public double getTotalMs() {
            if (results.isEmpty()) return 0;
            return results.get(0).getElapsedMs();
        }

        public double getSectionMs(String name) {
            for (SectionResult r : results) {
                if (r.name.equals(name)) return r.getElapsedMs();
            }
            return -1;
        }
    }

    public static class AccumulatedStats {
        public final int depth;
        private long totalNanos;
        private long maxNanos;
        private long minNanos;
        private int count;

        AccumulatedStats(int depth) {
            this.depth = depth;
            this.totalNanos = 0;
            this.maxNanos = Long.MIN_VALUE;
            this.minNanos = Long.MAX_VALUE;
            this.count = 0;
        }

        void record(long nanos) {
            totalNanos += nanos;
            if (nanos > maxNanos) maxNanos = nanos;
            if (nanos < minNanos) minNanos = nanos;
            count++;
        }

        public double getAverageMs() {
            return count > 0 ? (totalNanos / (double) count) / 1_000_000.0 : 0;
        }

        public double getMaxMs() {
            return maxNanos != Long.MIN_VALUE ? maxNanos / 1_000_000.0 : 0;
        }

        public double getMinMs() {
            return minNanos != Long.MAX_VALUE ? minNanos / 1_000_000.0 : 0;
        }

        public int getCount() {
            return count;
        }
    }

    private ScriptProfiler() {}
}
