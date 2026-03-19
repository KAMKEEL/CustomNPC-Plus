package bigguy.texteditor.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Layout manager that owns the flat list of {@link RenderLine}s and provides
 * viewport calculations for scrolled rendering.
 *
 * <p>Replaces the layout fields previously scattered across {@code ScriptDocument}:
 * {@code lineHeight}, {@code totalHeight}, {@code visibleLines}, {@code linesCount}.
 *
 * <p>Thread safety: the {@code lines} reference is volatile so that a background
 * rebuild can publish a new list that the render thread picks up on the next frame.
 * Individual reads through the accessors are safe without external synchronization.
 */
public class RenderModel {

    private volatile List<RenderLine> lines = Collections.emptyList();
    private volatile int lineHeight = 10;

    // ==================== LINE ACCESS ====================

    /**
     * @return total number of render lines
     */
    public int getLineCount() {
        return lines.size();
    }

    /**
     * @return total pixel height of all lines ({@code lineCount * lineHeight})
     */
    public int getTotalHeight() {
        return lines.size() * lineHeight;
    }

    /**
     * Compute how many lines fit in the given viewport height.
     *
     * @param viewportHeight pixel height of the visible area
     * @return number of fully visible lines
     */
    public int getVisibleLineCount(int viewportHeight) {
        return lineHeight > 0 ? viewportHeight / lineHeight : 0;
    }

    /**
     * Bounds-checked access to a single render line.
     *
     * @param index 0-based line index
     * @return the RenderLine at the given index
     * @throws IndexOutOfBoundsException if index is out of range
     */
    public RenderLine getLine(int index) {
        return lines.get(index);
    }

    /**
     * Get the sub-list of lines visible for the current scroll position.
     *
     * @param scrollOffset pixel offset from the top of the document
     * @param viewportHeight pixel height of the visible area
     * @return unmodifiable sublist of visible lines (may be empty)
     */
    public List<RenderLine> getVisibleLines(int scrollOffset, int viewportHeight) {
        List<RenderLine> snapshot = lines;
        if (snapshot.isEmpty() || lineHeight <= 0) {
            return Collections.emptyList();
        }

        int firstLine = Math.max(0, scrollOffset / lineHeight);
        int visibleCount = (viewportHeight / lineHeight) + 2;
        int lastLine = Math.min(snapshot.size(), firstLine + visibleCount);

        if (firstLine >= snapshot.size()) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(snapshot.subList(firstLine, lastLine));
    }

    /**
     * Find the line that contains the given global byte offset using binary search.
     *
     * @param globalOffset byte offset in the full document
     * @return the RenderLine containing that offset, or null if not found
     */
    public RenderLine getLineAtOffset(int globalOffset) {
        List<RenderLine> snapshot = lines;
        int idx = getLineIndexForOffset(globalOffset);
        if (idx >= 0 && idx < snapshot.size()) {
            return snapshot.get(idx);
        }
        return null;
    }

    /**
     * Binary search for the line index containing the given global byte offset.
     *
     * @param offset byte offset in the full document
     * @return 0-based line index, or -1 if no line contains the offset
     */
    public int getLineIndexForOffset(int offset) {
        List<RenderLine> snapshot = lines;
        int lo = 0;
        int hi = snapshot.size() - 1;

        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            RenderLine line = snapshot.get(mid);

            if (offset < line.getGlobalStart()) {
                hi = mid - 1;
            } else if (offset >= line.getGlobalEnd()) {
                lo = mid + 1;
            } else {
                return mid;
            }
        }

        return -1;
    }

    /**
     * @return unmodifiable view of all render lines
     */
    public List<RenderLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    /**
     * @return current pixel height per line
     */
    public int getLineHeight() {
        return lineHeight;
    }

    /**
     * @param lineHeight new pixel height per line
     */
    public void setLineHeight(int lineHeight) {
        this.lineHeight = lineHeight;
    }

    // ==================== REBUILD ====================

    /**
     * Rebuild the entire line model from source text and merged marks.
     *
     * <ol>
     *   <li>Splits {@code sourceText} by newlines into logical lines</li>
     *   <li>Creates a {@link RenderLine} for each with correct
     *       globalStart/globalEnd/lineIndex</li>
     *   <li>Calls {@link RenderLine#buildTokensFromMarks(List, String)}
     *       with the marks that overlap each line</li>
     *   <li>Computes indent guides per line</li>
     * </ol>
     *
     * @param sourceText  the complete document text
     * @param mergedMarks merged marks from {@link MarkLayer#merge()}, sorted by start
     * @param lineHeight  pixel height per line
     */
    public void rebuild(String sourceText, List<Mark> mergedMarks, int lineHeight) {
        this.lineHeight = lineHeight;

        List<RenderLine> newLines = new ArrayList<>();

        if (sourceText.isEmpty()) {
            RenderLine empty = new RenderLine("", 0, 0, 0);
            empty.buildTokensFromMarks(Collections.<Mark>emptyList(), sourceText);
            newLines.add(empty);
            this.lines = newLines;
            return;
        }

        int lineIdx = 0;
        int pos = 0;
        int textLen = sourceText.length();

        while (pos <= textLen) {
            int nlPos = sourceText.indexOf('\n', pos);
            int lineEnd;
            int globalEnd;

            if (nlPos == -1) {
                lineEnd = textLen;
                globalEnd = textLen;
            } else {
                lineEnd = nlPos;
                globalEnd = nlPos + 1;
            }

            String lineText = sourceText.substring(pos, lineEnd);
            RenderLine renderLine = new RenderLine(lineText, pos, globalEnd, lineIdx);

            List<Mark> lineMarks = collectMarksForRange(mergedMarks, pos, globalEnd);
            renderLine.buildTokensFromMarks(lineMarks, sourceText);

            computeIndentGuides(renderLine, lineText);

            newLines.add(renderLine);
            lineIdx++;

            if (nlPos == -1) {
                break;
            }
            pos = nlPos + 1;

            if (pos == textLen) {
                RenderLine trailing = new RenderLine("", pos, pos, lineIdx);
                trailing.buildTokensFromMarks(Collections.<Mark>emptyList(), sourceText);
                newLines.add(trailing);
                break;
            }
        }

        this.lines = newLines;
    }

    /**
     * Collect marks that overlap the range {@code [rangeStart, rangeEnd)}.
     * Assumes {@code allMarks} is sorted by start position and uses a linear scan
     * with early exit.
     */
    private static List<Mark> collectMarksForRange(List<Mark> allMarks, int rangeStart, int rangeEnd) {
        List<Mark> result = new ArrayList<>();
        for (Mark m : allMarks) {
            if (m.start >= rangeEnd) {
                break;
            }
            if (m.end > rangeStart) {
                result.add(m);
            }
        }
        return result;
    }

    /**
     * Compute indent guides for a line based on its leading whitespace.
     * Adds a guide at each tab-stop column (every 4 characters) within the indentation.
     */
    private static void computeIndentGuides(RenderLine line, String text) {
        int indent = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ' ') {
                indent++;
            } else if (c == '\t') {
                indent = ((indent / 4) + 1) * 4;
            } else {
                break;
            }
        }

        int tabSize = 4;
        for (int col = tabSize; col <= indent; col += tabSize) {
            line.addIndentGuide(col);
        }
    }

    @Override
    public String toString() {
        return "RenderModel{lines=" + lines.size()
                + ", lineHeight=" + lineHeight
                + ", totalHeight=" + getTotalHeight() + "}";
    }
}
