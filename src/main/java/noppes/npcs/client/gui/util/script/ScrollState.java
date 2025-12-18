package noppes.npcs.client.gui.util.script;

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
}