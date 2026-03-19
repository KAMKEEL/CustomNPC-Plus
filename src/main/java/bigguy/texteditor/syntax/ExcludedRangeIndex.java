package bigguy.texteditor.syntax;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * An immutable, sorted index of non-overlapping byte ranges that should be
 * excluded from semantic analysis (string literals, comments, block comments).
 *
 * <p>Built once from a tree-sitter CST walk, this index answers containment
 * and overlap queries in O(log n) via binary search over a flat sorted array.
 * After an edit, call {@link #shiftRanges(int, int)} to produce a new index
 * with adjusted offsets rather than rebuilding from scratch.</p>
 */
public final class ExcludedRangeIndex {

    /**
     * A single contiguous byte range within the source text.
     * Immutable value object with natural ordering by start offset.
     */
    public static final class Range implements Comparable<Range> {

        private final int start;
        private final int end;

        /**
         * @param start inclusive start byte offset (0-based)
         * @param end   exclusive end byte offset (0-based)
         * @throws IllegalArgumentException if start exceeds end
         */
        public Range(int start, int end) {
            if (start > end) {
                throw new IllegalArgumentException(
                        "start (" + start + ") must not exceed end (" + end + ")");
            }
            this.start = start;
            this.end = end;
        }

        /** @return inclusive start byte offset */
        public int start() { return start; }

        /** @return exclusive end byte offset */
        public int end() { return end; }

        /**
         * @param offset the byte offset to test
         * @return {@code true} if {@code start <= offset < end}
         */
        public boolean contains(int offset) {
            return offset >= start && offset < end;
        }

        /**
         * @param otherStart inclusive start of the query interval
         * @param otherEnd   exclusive end of the query interval
         * @return {@code true} if the intervals share at least one byte
         */
        public boolean overlaps(int otherStart, int otherEnd) {
            return this.start < otherEnd && otherStart < this.end;
        }

        @Override
        public int compareTo(Range other) {
            int cmp = Integer.compare(this.start, other.start);
            return cmp != 0 ? cmp : Integer.compare(this.end, other.end);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Range)) return false;
            Range that = (Range) obj;
            return start == that.start && end == that.end;
        }

        @Override
        public int hashCode() {
            return 31 * start + end;
        }

        @Override
        public String toString() {
            return "[" + start + ".." + end + ")";
        }
    }

    private static final ExcludedRangeIndex EMPTY = new ExcludedRangeIndex(new Range[0]);

    private final Range[] ranges;

    private ExcludedRangeIndex(Range[] ranges) {
        this.ranges = ranges;
    }

    /**
     * Returns an empty index that excludes nothing.
     *
     * @return a shared empty instance
     */
    public static ExcludedRangeIndex empty() {
        return EMPTY;
    }

    /**
     * Builds an index from an unsorted, potentially overlapping collection of ranges.
     * The builder sorts the input, merges overlapping/adjacent ranges, and produces
     * a minimal, non-overlapping representation.
     *
     * @param rawRanges the ranges to index (may be unsorted and overlapping)
     * @return an immutable index over the merged ranges
     */
    public static ExcludedRangeIndex build(List<Range> rawRanges) {
        if (rawRanges.isEmpty()) {
            return EMPTY;
        }

        Range[] sorted = rawRanges.toArray(new Range[0]);
        Arrays.sort(sorted);

        Range[] merged = new Range[sorted.length];
        int count = 0;
        Range current = sorted[0];

        for (int i = 1; i < sorted.length; i++) {
            Range next = sorted[i];
            if (next.start <= current.end) {
                current = new Range(current.start, Math.max(current.end, next.end));
            } else {
                merged[count++] = current;
                current = next;
            }
        }
        merged[count++] = current;

        return new ExcludedRangeIndex(Arrays.copyOf(merged, count));
    }

    /**
     * Tests whether the given byte offset falls inside any excluded range.
     * Runs in O(log n) via binary search.
     *
     * @param offset the byte offset to test (0-based)
     * @return {@code true} if the offset is inside a string literal, comment, etc.
     */
    public boolean contains(int offset) {
        int lo = 0;
        int hi = ranges.length - 1;

        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            Range r = ranges[mid];

            if (offset < r.start) {
                hi = mid - 1;
            } else if (offset >= r.end) {
                lo = mid + 1;
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * Tests whether any excluded range overlaps with the half-open interval
     * {@code [start, end)}. Runs in O(log n).
     *
     * @param start inclusive start byte offset
     * @param end   exclusive end byte offset
     * @return {@code true} if any excluded range intersects the query interval
     */
    public boolean overlaps(int start, int end) {
        if (start >= end || ranges.length == 0) {
            return false;
        }

        int lo = 0;
        int hi = ranges.length - 1;

        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            Range r = ranges[mid];

            if (r.end <= start) {
                lo = mid + 1;
            } else if (r.start >= end) {
                hi = mid - 1;
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * Produces a new index with all ranges shifted to account for an edit operation.
     * Ranges entirely before {@code editStart} are unchanged. Ranges overlapping
     * the edit point are expanded/contracted. Ranges after are shifted by {@code delta}.
     *
     * @param editStart the byte offset where the edit occurred
     * @param delta     positive for insertion, negative for deletion
     * @return a new index with adjusted ranges
     */
    public ExcludedRangeIndex shiftRanges(int editStart, int delta) {
        if (ranges.length == 0 || delta == 0) {
            return this;
        }

        Range[] shifted = new Range[ranges.length];
        int count = 0;

        for (Range r : ranges) {
            if (r.end <= editStart) {
                shifted[count++] = r;
            } else if (r.start >= editStart) {
                int newStart = r.start + delta;
                int newEnd = r.end + delta;
                if (newEnd > 0 && newStart < newEnd) {
                    shifted[count++] = new Range(Math.max(0, newStart), newEnd);
                }
            } else {
                int newEnd = r.end + delta;
                if (newEnd > r.start) {
                    shifted[count++] = new Range(r.start, newEnd);
                }
            }
        }

        if (count == 0) {
            return EMPTY;
        }
        return new ExcludedRangeIndex(Arrays.copyOf(shifted, count));
    }

    /** @return the number of excluded ranges in this index */
    public int size() {
        return ranges.length;
    }

    /** @return {@code true} if no ranges are indexed */
    public boolean isEmpty() {
        return ranges.length == 0;
    }

    /**
     * Returns an unmodifiable view of all ranges, sorted by start offset.
     *
     * @return the excluded ranges
     */
    public List<Range> getRanges() {
        return Collections.unmodifiableList(Arrays.asList(ranges));
    }

    @Override
    public String toString() {
        if (ranges.length == 0) {
            return "ExcludedRangeIndex{empty}";
        }
        StringBuilder sb = new StringBuilder("ExcludedRangeIndex{");
        for (int i = 0; i < ranges.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(ranges[i]);
        }
        sb.append('}');
        return sb.toString();
    }
}
