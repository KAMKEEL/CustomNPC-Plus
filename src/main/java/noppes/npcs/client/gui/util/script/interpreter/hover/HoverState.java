package noppes.npcs.client.gui.util.script.interpreter.hover;

import noppes.npcs.client.gui.util.script.interpreter.ScriptDocument;
import noppes.npcs.client.gui.util.script.interpreter.ScriptLine;
import noppes.npcs.client.gui.util.script.interpreter.Token;

/**
 * Manages the hover state for token tooltips in the script editor.
 * 
 * Tracks:
 * - Current mouse position
 * - Hovered token (if any)
 * - Hover duration for delayed tooltip display
 * - Whether tooltip should be visible
 * 
 * Implements a 500ms delay before showing tooltips, resetting when
 * the mouse moves to a different token.
 */
public class HoverState {

    /** Minimum hover time (ms) before showing tooltip */
    private static final long HOVER_DELAY_MS = 500;
    
    // ==================== STATE ====================
    
    /** Currently hovered token (null if none) */
    private Token hoveredToken;
    
    /** Time when hover on current token started */
    private long hoverStartTime;
    
    /** Whether the tooltip should currently be displayed */
    private boolean tooltipVisible;
    
    /** Cached hover info for the current token */
    private TokenHoverInfo hoverInfo;
    
    /** Last known mouse position */
    private int lastMouseX;
    private int lastMouseY;
    
    /** Position of the hovered token (for tooltip positioning) */
    private int tokenScreenX;
    private int tokenScreenY;
    private int tokenWidth;

    // ==================== UPDATE ====================

    /**
     * Update the hover state based on current mouse position.
     * Should be called every frame from drawTextBox.
     * 
     * @param mouseX Current mouse X position
     * @param mouseY Current mouse Y position
     * @param document The script document
     * @param viewportX X position of the text viewport (after gutter)
     * @param viewportY Y position of the text viewport
     * @param viewportWidth Width of the text viewport
     * @param viewportHeight Height of the text viewport
     * @param scrollOffset Current vertical scroll offset (in lines)
     * @param lineHeight Height of each line in pixels
     * @param gutterWidth Width of the line number gutter
     */
    public void update(int mouseX, int mouseY, ScriptDocument document,
                       int viewportX, int viewportY, int viewportWidth, int viewportHeight,
                       float scrollOffset, int lineHeight, int gutterWidth) {
        
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        
        // Check if mouse is within the text viewport
        if (mouseX < viewportX || mouseX > viewportX + viewportWidth ||
            mouseY < viewportY || mouseY > viewportY + viewportHeight) {
            clearHover();
            return;
        }
        
        if (document == null) {
            clearHover();
            return;
        }
        
        // Calculate which line the mouse is over
        int relativeY = mouseY - viewportY;
        int lineIndex = (int) (scrollOffset + (relativeY / (float) lineHeight));
        
        // Get the line
        ScriptLine line = null;
        for (ScriptLine l : document.getLines()) {
            if (l.getLineIndex() == lineIndex) {
                line = l;
                break;
            }
        }
        
        if (line == null) {
            clearHover();
            return;
        }
        
        // Calculate character position within the line
        int relativeX = mouseX - viewportX;
        int globalPos = line.getGlobalStart() + getCharacterIndexAtX(line, relativeX);
        
        // Find the token at this position
        Token token = line.getTokenAt(globalPos);
        
        if (token == null) {
            clearHover();
            return;
        }
        
        // Check if this is a new token
        if (token != hoveredToken) {
            // New token - reset timer
            hoveredToken = token;
            hoverStartTime = System.currentTimeMillis();
            tooltipVisible = false;
            hoverInfo = null;
            
            // Calculate token screen position for tooltip
            calculateTokenPosition(line, token, viewportX, viewportY, scrollOffset, lineHeight);
        } else {
            // Same token - check if delay has elapsed
            long elapsed = System.currentTimeMillis() - hoverStartTime;
            if (elapsed >= HOVER_DELAY_MS && !tooltipVisible) {
                tooltipVisible = true;
                hoverInfo = TokenHoverInfo.fromToken(token);
            }
        }
    }

    /**
     * Update the hover state with a specific token.
     * Simplified version for when the token has already been found.
     * 
     * @param mouseX Current mouse X position
     * @param mouseY Current mouse Y position
     * @param token The token at the mouse position (or null if none)
     * @param tokenX Screen X position of the token
     * @param tokenY Screen Y position of the token
     * @param tokenW Width of the token in pixels
     */
    public void update(int mouseX, int mouseY, Token token, int tokenX, int tokenY, int tokenW) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        
        if (token == null) {
           clearHover();
            return;
        }
        
        // Check if this is a new token
        if (token != hoveredToken) {
            // New token - reset timer
            hoveredToken = token;
            hoverStartTime = System.currentTimeMillis();
            tooltipVisible = false;
            hoverInfo = null;
            
            // Store token position
            tokenScreenX = tokenX;
            tokenScreenY = tokenY;
            tokenWidth = tokenW;
        } else {
            // Same token - check if delay has elapsed
            long elapsed = System.currentTimeMillis() - hoverStartTime;
            if (elapsed >= HOVER_DELAY_MS && !tooltipVisible) {
                tooltipVisible = true;
                hoverInfo = TokenHoverInfo.fromToken(token);
            }
        }
    }

    /**
     * Clear the current hover state.
     */
    public void clearHover() {
        if (hoveredToken != null) {
            hoveredToken = null;
            hoverStartTime = 0;
            tooltipVisible = false;
            hoverInfo = null;
        }
    }

    /**
     * Force the tooltip to hide (e.g., when clicking).
     */
    public void hideTooltip() {
        tooltipVisible = false;
    }

    // ==================== POSITION CALCULATION ====================

    /**
     * Get the character index within a line at the given X pixel position.
     */
    private int getCharacterIndexAtX(ScriptLine line, int x) {
        String text = line.getText();
        if (text == null || text.isEmpty()) return 0;
        
        int accumWidth = 0;
        for (int i = 0; i < text.length(); i++) {
            int charWidth = noppes.npcs.client.ClientProxy.Font.width(String.valueOf(text.charAt(i)));
            if (accumWidth + charWidth / 2 > x) {
                return i;
            }
            accumWidth += charWidth;
        }
        return text.length();
    }

    /**
     * Calculate the screen position of a token for tooltip positioning.
     */
    private void calculateTokenPosition(ScriptLine line, Token token, 
                                        int viewportX, int viewportY,
                                        float scrollOffset, int lineHeight) {
        // X position: calculate width of text before the token
        String lineText = line.getText();
        int tokenLocalStart = token.getGlobalStart() - line.getGlobalStart();
        tokenLocalStart = Math.max(0, Math.min(tokenLocalStart, lineText.length()));
        
        String textBefore = lineText.substring(0, tokenLocalStart);
        tokenScreenX = viewportX + noppes.npcs.client.ClientProxy.Font.width(textBefore);
        
        // Y position: line position minus scroll
        int lineY = line.getLineIndex();
        tokenScreenY = viewportY + (int) ((lineY - scrollOffset) * lineHeight);
        
        // Token width
        tokenWidth = noppes.npcs.client.ClientProxy.Font.width(token.getText());
    }

    // ==================== GETTERS ====================

    public boolean isTooltipVisible() {
        return tooltipVisible && hoverInfo != null && hoverInfo.hasContent();
    }

    public TokenHoverInfo getHoverInfo() {
        return hoverInfo;
    }

    public Token getHoveredToken() {
        return hoveredToken;
    }

    public int getTokenScreenX() {
        return tokenScreenX;
    }

    public int getTokenScreenY() {
        return tokenScreenY;
    }

    public int getTokenWidth() {
        return tokenWidth;
    }

    public int getLastMouseX() {
        return lastMouseX;
    }

    public int getLastMouseY() {
        return lastMouseY;
    }

    /**
     * Get the progress (0.0 to 1.0) of the hover delay.
     * Can be used for fade-in animation.
     */
    public float getHoverProgress() {
        if (hoveredToken == null) return 0f;
        long elapsed = System.currentTimeMillis() - hoverStartTime;
        return Math.min(1.0f, elapsed / (float) HOVER_DELAY_MS);
    }
}
