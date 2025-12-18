package noppes.npcs.client.gui.util.script;

import org.lwjgl.input.Mouse;

/**
 * Manages smooth scrolling state and animation for the script text area.
 * Uses exponential smoothing for fluid scroll animations.
 */
public class ScrollState {
    // Current fractional scroll position (in lines)
    private double scrollPos = -1.0;
    // Target scroll position to animate towards
    private double targetScroll = 0.0;
    // Current scroll velocity for momentum calculations
    private double scrollVelocity = 0.0;
    // Last timestamp for delta time calculation
    private long lastScrollTime = 0L;
    // Integer line index for compatibility with rendering
    private int scrolledLine = 0;
    // Scrollbar drag offset when dragging thumb
    private int scrollbarDragOffset = 0;
    // Whether currently dragging the scrollbar
    private boolean clickScrolling = false;
    
    // Animation parameters
    private static final double TAU = 0.1; // Time constant (~55ms feels snappy)
    private static final double SNAP_THRESHOLD = 0.01; // Snap to target when this close
    private static final double MAX_DT = 0.05; // Max delta time for stability
    
    /**
     * Reset scroll state to initial values
     */
    public void reset() {
        scrollPos = 0.0;
        targetScroll = 0.0;
        scrollVelocity = 0.0;
        scrolledLine = 0;
        lastScrollTime = System.currentTimeMillis();
    }
    
    /**
     * Initialize scroll position if not already initialized
     */
    public void initializeIfNeeded(int currentLine) {
        if (scrollPos < 0) {
            scrollPos = currentLine;
            lastScrollTime = System.currentTimeMillis();
        }
    }
    
    /**
     * Update scroll animation. Call once per frame.
     * @param maxScroll Maximum valid scroll position
     */
    public void update(int maxScroll) {
        long nowMs = System.currentTimeMillis();
        double dt = Math.min(MAX_DT, (nowMs - lastScrollTime) / 1000.0);
        lastScrollTime = nowMs;
        
        double dist = targetScroll - scrollPos;
        
        // Snap to target if close enough
        if (Math.abs(dist) < SNAP_THRESHOLD) {
            scrollPos = targetScroll;
            scrollVelocity = 0.0;
        } else {
            // Exponential smoothing
            double alpha = 1.0 - Math.exp(-dt / Math.max(1e-6, TAU));
            double prev = scrollPos;
            scrollPos += dist * alpha;
            scrollVelocity = (scrollPos - prev) / (dt > 0 ? dt : 1e-6);
            
            // Clamp overshoot
            if ((dist > 0 && scrollPos > targetScroll) || (dist < 0 && scrollPos < targetScroll)) {
                scrollPos = targetScroll;
                scrollVelocity = 0.0;
            }
        }
        
        // Keep in bounds
        clampToBounds(maxScroll);
        
        // Update integer line for rendering
        scrolledLine = Math.max(0, Math.min((int) Math.floor(scrollPos), maxScroll));
    }
    
    /**
     * Clamp scroll position to valid bounds
     */
    public void clampToBounds(int maxScroll) {
        if (scrollPos < 0) scrollPos = 0;
        if (scrollPos > maxScroll) scrollPos = maxScroll;
        if (targetScroll < 0) targetScroll = 0;
        if (targetScroll > maxScroll) targetScroll = maxScroll;
        scrolledLine = Math.max(0, Math.min(scrolledLine, maxScroll));
    }
    
    /**
     * Apply mouse wheel scroll
     * @param wheelDelta Positive = scroll up, negative = scroll down
     * @param maxScroll Maximum valid scroll position
     */
    public void applyWheelScroll(int wheelDelta, int maxScroll) {
        double sign = Math.copySign(1, wheelDelta);
        targetScroll -= sign * 2.0; // 2 lines per wheel tick
        clampToBounds(maxScroll);
    }
    
    /**
     * Set target scroll directly (for scrollbar dragging)
     */
    public void setTargetScroll(double target, int maxScroll) {
        targetScroll = Math.max(0, Math.min(target, maxScroll));
    }
    
    /**
     * Scroll to make a specific line visible
     * @param lineIdx Line index to make visible
     * @param visibleLines Number of visible lines in viewport
     * @param maxScroll Maximum scroll position
     */
    public void scrollToLine(int lineIdx, int visibleLines, int maxScroll) {
        int firstVisible = scrolledLine;
        int lastFullyVisible = scrolledLine + visibleLines;
        
        if (lineIdx < firstVisible) {
            // Line is above viewport - scroll up
            targetScroll = lineIdx;
        } else if (lineIdx > lastFullyVisible) {
            // Line is below viewport - scroll down
            targetScroll = Math.min(lineIdx - visibleLines, maxScroll);
        }
        // If line is visible, don't change scroll
    }
    
    // Getters
    public double getScrollPos() { return scrollPos; }
    public double getTargetScroll() { return targetScroll; }
    public int getScrolledLine() { return scrolledLine; }
    public double getScrollVelocity() { return scrollVelocity; }
    public boolean isClickScrolling() { return clickScrolling; }
    public int getScrollbarDragOffset() { return scrollbarDragOffset; }
    
    /**
     * Get fractional offset for sub-pixel rendering
     */
    public double getFractionalOffset() {
        return scrollPos - scrolledLine;
    }
    
    // Setters for scrollbar interaction
    public void setClickScrolling(boolean scrolling) { this.clickScrolling = scrolling; }
    public void setScrollbarDragOffset(int offset) { this.scrollbarDragOffset = offset; }
    public void setScrolledLine(int line) { this.scrolledLine = line; }

    /**
     * Handle scrollbar dragging interaction. This encapsulates the logic that was
     * previously in the GUI class for handling thumb clicks/drags.
     * @param yMouse current mouse Y coordinate
     * @param areaX GUI area X (unused but kept for parity)
     * @param areaY GUI area Y (top of text area)
     * @param areaHeight height of the scroll track area
     * @param visibleLines number of visible lines in viewport
     * @param linesCount total number of lines
     * @param maxScroll maximum allowed scroll value
     */
    public void handleClickScrolling(int yMouse, int areaX, int areaY, int areaHeight, int visibleLines, int linesCount, int maxScroll) {
        // Keep dragging while mouse button is down
        setClickScrolling(Mouse.isButtonDown(0));
        int diff = Math.max(0, linesCount - visibleLines);
        if (diff > 0) {
            int sbSize = Math.max((int) (1f * visibleLines / Math.max(1, linesCount) * areaHeight), 2);
            int trackTop = areaY + 1;
            int trackHeight = Math.max(1, areaHeight - 4);
            int thumbRange = Math.max(1, trackHeight - sbSize);
            double linesCountD = Math.max(1, (double) linesCount);
            int thumbTop = (int) (areaY + 1f * getScrollPos() / linesCountD * (areaHeight - 4)) + 1;

            if (yMouse < thumbTop || yMouse > thumbTop + sbSize) {
                double centerRatio = (double) (yMouse - trackTop) / (double) trackHeight;
                centerRatio = Math.max(0.0, Math.min(1.0, centerRatio));
                setTargetScroll(centerRatio * diff, maxScroll);
                setScrollbarDragOffset(sbSize / 2);
            } else {
                int desiredTop = yMouse - getScrollbarDragOffset();
                desiredTop = Math.max(trackTop, Math.min(trackTop + thumbRange, desiredTop));
                double ratio = (double) (desiredTop - trackTop) / (double) thumbRange;
                ratio = Math.max(0.0, Math.min(1.0, ratio));
                setTargetScroll(ratio * diff, maxScroll);
            }
        }
        if (!isClickScrolling()) {
            setScrollbarDragOffset(0);
        }
    }

    /**
     * Attempt to start a scrollbar drag if the mouse down occurred on the scrollbar thumb area.
     * Returns true if drag was initiated (caller should cancel normal text-click drag behavior).
     */
    public void startScrollbarDrag(int yMouse, int areaY, int areaHeight, int linesCount) {
        // Thumb width in GuiScriptTextArea is 6px (posX..posX+5), so check clicks near the right edge
        // Start click-scrolling mode and set initial drag offset relative to thumb top.
        setClickScrolling(true);
        double linesCountD = Math.max(1, (double) linesCount);
        int thumbTop = (int) (areaY + 1f * getScrollPos() / linesCountD * (areaHeight - 4)) + 1;
        setScrollbarDragOffset(yMouse - thumbTop);
    }
}