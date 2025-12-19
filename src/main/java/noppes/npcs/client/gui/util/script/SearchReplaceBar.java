package noppes.npcs.client.gui.util.script;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.gui.util.GuiUtil;
import noppes.npcs.client.gui.util.key.OverlayKeyPresetViewer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Search and Replace bar component for the script editor.
 * Provides IntelliJ-like search functionality with:
 * - Search text field with rounded edges
 * - Match case (Cc) toggle
 * - Whole word (W) toggle
 * - Previous/Next match navigation
 * - Replace functionality with Replace/Replace All/Exclude buttons
 * - Auto-scroll to matches
 * - All matches highlighting
 */
public class SearchReplaceBar {
    
    // ==================== TEXTURE ====================
    public static final ResourceLocation TEXTURE = OverlayKeyPresetViewer.TEXTURE;
    
    // ==================== DIMENSIONS ====================
    private int x, y, width;
    private int barHeight = 24;
    private int replaceBarHeight = 22;
    private int textFieldWidth = 150;
    private int textFieldHeight = 16;
    private int buttonSize = 16;
    private int buttonSpacing = 2;
    private int padding = 4;
    
    // ==================== STATE ====================
    private boolean visible = false;
    private boolean showReplace = false;
    private boolean searchFieldFocused = true;
    private boolean replaceFieldFocused = false;
    
    // ==================== SEARCH OPTIONS ====================
    private boolean matchCase = false;
    private boolean wholeWord = false;
    
    // ==================== TEXT FIELDS ====================
    private String searchText = "";
    private String replaceText = "";
    private int searchCursor = 0;
    private int replaceCursor = 0;
    private int searchSelectionStart = 0;
    private int searchSelectionEnd = 0;
    private int replaceSelectionStart = 0;
    private int replaceSelectionEnd = 0;
    // Text scroll offsets for when text exceeds field width
    private int searchScrollOffset = 0;
    private int replaceScrollOffset = 0;
    
    // ==================== MATCHES ====================
    private List<int[]> matches = new ArrayList<>();
    private int currentMatchIndex = -1;
    private List<Integer> excludedMatches = new ArrayList<>();
    
    // ==================== HOVER STATE ====================
    private boolean hoverClose = false;
    private boolean hoverSearchUnwind = false;
    private boolean hoverMatchCase = false;
    private boolean hoverWholeWord = false;
    private boolean hoverPrev = false;
    private boolean hoverNext = false;
    private boolean hoverReplace = false;
    private boolean hoverReplaceAll = false;
    private boolean hoverExclude = false;
    
    // ==================== CURSOR BLINK ====================
    private int cursorCounter = 0;
    private long lastInputTime = 0; // For cursor blink pause on activity
    
    // ==================== DOUBLE/TRIPLE CLICK ====================
    private long lastClickTime = 0;
    private int clickCount = 0;
    private boolean lastClickWasSearch = true;
    
    // ==================== CALLBACK ====================
    private SearchCallback callback;
    
    private final FontRenderer font = Minecraft.getMinecraft().fontRenderer;
    
    /**
     * Callback interface for search/replace operations
     */
    public interface SearchCallback {
        String getText();
        void setText(String text);
        void scrollToPosition(int position);
        void setSelection(int start, int end);
        int getGutterWidth();
        void unfocusMainEditor();
        void focusMainEditor();
        void onMatchesUpdated(); // Called when matches change (for undo/redo sync)
    }
    
    public SearchReplaceBar() {
    }
    
    public void setCallback(SearchCallback callback) {
        this.callback = callback;
    }
    
    /**
     * Initialize bar position based on text area bounds.
     * Preserves existing state (search text, matches, etc.) across initGui calls.
     */
    public void initGui(int textAreaX, int textAreaY, int textAreaWidth) {
        this.x = textAreaX;
        this.y = textAreaY;
        this.width = textAreaWidth;
        // State is preserved - don't reset searchText, matches, visible, etc.
    }
    
    /**
     * Open search bar (Ctrl+R)
     */
    public void openSearch() {
        visible = true;
        showReplace = false;
        searchFieldFocused = true;
        replaceFieldFocused = false;
        searchSelectionStart = 0;
        searchSelectionEnd = searchText.length();
        searchCursor = searchText.length();
        markActivity();
        if (callback != null) callback.unfocusMainEditor();
        updateMatches();
    }
    
    /**
     * Open search+replace bar (Ctrl+Shift+R)
     */
    public void openSearchReplace() {
        visible = true;
        showReplace = true;
        searchFieldFocused = true;
        replaceFieldFocused = false;
        searchSelectionStart = 0;
        searchSelectionEnd = searchText.length();
        searchCursor = searchText.length();
        markActivity();
        if (callback != null) callback.unfocusMainEditor();
        updateMatches();
    }
    
    /**
     * Close the search bar and restore focus to main editor
     */
    public void close() {
        visible = false;
        searchFieldFocused = false;
        replaceFieldFocused = false;
        if (callback != null) callback.focusMainEditor();
    }
    
    /**
     * Unfocus the search bar (when clicking main editor)
     */
    public void unfocus() {
        searchFieldFocused = false;
        replaceFieldFocused = false;
    }
    
    /**
     * Toggle visibility
     */
    public void toggle() {
        if (visible && !showReplace) {
            close();
        } else {
            openSearch();
        }
    }
    
    /**
     * Toggle replace mode
     */
    public void toggleReplace() {
        if (visible && showReplace) {
            close();
        } else {
            openSearchReplace();
        }
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    public boolean isSearchFocused() {
        return visible && searchFieldFocused;
    }
    
    public boolean isReplaceFocused() {
        return visible && replaceFieldFocused;
    }
    
    public boolean hasFocus() {
        return visible && (searchFieldFocused || replaceFieldFocused);
    }
    
    /**
     * Get total height of the bar (for viewport offset)
     */
    public int getTotalHeight() {
        if (!visible) return 0;
        return showReplace ? barHeight + replaceBarHeight : barHeight;
    }
    
    /**
     * Get all current matches for highlighting
     */
    public List<int[]> getMatches() {
        return matches;
    }
    
    /**
     * Get current match index
     */
    public int getCurrentMatchIndex() {
        return currentMatchIndex;
    }
    
    /**
     * Check if a match is excluded
     */
    public boolean isMatchExcluded(int index) {
        return excludedMatches.contains(index);
    }
    
    /**
     * Update cursor blink counter
     */
    public void updateCursor() {
        cursorCounter++;
    }
    
    /**
     * Mark activity to pause cursor blinking
     */
    private void markActivity() {
        lastInputTime = System.currentTimeMillis();
    }
    
    /**
     * Check if there was recent input (cursor stays visible during typing)
     */
    private boolean hadRecentInput() {
        return System.currentTimeMillis() - lastInputTime < 500;
    }
    
    /**
     * Check if cursor should be visible (solid during typing, blinking otherwise)
     */
    private boolean shouldShowCursor() {
        return hadRecentInput() || (cursorCounter / 10) % 2 == 0;
    }
    
    // ==================== DRAWING ====================
    
    /**
     * Draw the search/replace bar
     */
    public void draw(int mouseX, int mouseY) {
        if (!visible) return;
        
        updateHoverStates(mouseX, mouseY);
        
        int totalHeight = getTotalHeight();
        Gui.drawRect(x, y, x + width, y + totalHeight, 0xFF3c3f41);
        Gui.drawRect(x, y + totalHeight - 1, x + width, y + totalHeight, 0xFF515151);
        
        drawSearchRow(mouseX, mouseY);
        
        if (showReplace) {
            drawReplaceRow(mouseX, mouseY);
        }
    }
    
    private void drawSearchRow(int mouseX, int mouseY) {
        int rowY = y + padding;
        int currentX = x + padding;
        textFieldWidth=100;
        drawTextField(currentX, rowY, textFieldWidth, textFieldHeight, 
                     searchText, searchCursor, searchSelectionStart, searchSelectionEnd,
                     searchFieldFocused, "Search...", searchScrollOffset, true);
        currentX += textFieldWidth + buttonSpacing + 4;
        
        // "0 results" in red, otherwise "X/Y" in normal color
        String matchInfo = matches.isEmpty() ? "0 results" : 
                          (currentMatchIndex + 1) + "/" + matches.size();
        int matchInfoWidth = font.getStringWidth(matchInfo);
        font.drawString(matchInfo, currentX, rowY + 4, matches.isEmpty() ? 0xFFff6666 : 0xFFcccccc);
        currentX += matchInfoWidth + 8;
        
        drawToggleButton(currentX, rowY, "Cc", matchCase, hoverMatchCase, "Match Case");
        currentX += buttonSize + buttonSpacing;
        
        drawToggleButton(currentX, rowY, "W", wholeWord, hoverWholeWord, "Whole Word");
        currentX += buttonSize + buttonSpacing + 4;
        
        boolean prevEnabled = !matches.isEmpty();
        drawNavButton(currentX, rowY, "▲", prevEnabled, hoverPrev);
        currentX += buttonSize + buttonSpacing;
        
        boolean nextEnabled = !matches.isEmpty();
        drawNavButton(currentX, rowY, "▼", nextEnabled, hoverNext);
        currentX += buttonSize + buttonSpacing;
        
        int closeX = x + width - buttonSize - padding;
        drawCloseButton(closeX, rowY, hoverClose);

        int replaceUnwindButton = closeX - buttonSize - buttonSpacing;
        drawActionButton(replaceUnwindButton, rowY, showReplace ? "▼" : "◀", hoverSearchUnwind, true);
    }
    
    private void drawReplaceRow(int mouseX, int mouseY) {
        int rowY = y + barHeight + 2;
        int currentX = x + padding;
        
        drawTextField(currentX, rowY, textFieldWidth, textFieldHeight,
                     replaceText, replaceCursor, replaceSelectionStart, replaceSelectionEnd,
                     replaceFieldFocused, "Replace...", replaceScrollOffset, false);
        currentX += textFieldWidth + buttonSpacing + 4;
        
        drawActionButton(currentX, rowY, "Replace", hoverReplace, !matches.isEmpty());
        currentX += font.getStringWidth("Replace") + 12 + buttonSpacing;
        
        drawActionButton(currentX, rowY, "Replace All", hoverReplaceAll, !matches.isEmpty());
        currentX += font.getStringWidth("Replace All") + 12 + buttonSpacing;
        
        drawActionButton(currentX, rowY, "Exclude", hoverExclude, currentMatchIndex >= 0);
    }
    
    private void drawTextField(int fieldX, int fieldY, int fieldWidth, int fieldHeight,
                               String text, int cursor, int selStart, int selEnd,
                               boolean focused, String placeholder, int scrollOffset, boolean isSearchField) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
        GL11.glPushMatrix();
        GL11.glColor4f(1, 1, 1, 1);

        float boxScaleX = fieldWidth / 32f, boxScaleY = 1.5f;
        GL11.glScalef(boxScaleX, boxScaleY, 1);
        
        if (focused) {
            GL11.glColor4f(0.28f, 0.45f, 0.7f, 1f);
        } else {
            GL11.glColor4f(0.33f, 0.33f, 0.33f, 1f);
        }
        GuiUtil.drawTexturedModalRect(fieldX / boxScaleX, (fieldY - 16) / boxScaleY, 32, 20, 0, 492);
        GL11.glPopMatrix();
        
        int textX = fieldX + 4;
        int textY = fieldY + (fieldHeight - font.FONT_HEIGHT) / 2;
        int visibleWidth = fieldWidth - 8;
        
        // Calculate scroll offset to keep cursor visible
        int cursorPosX = font.getStringWidth(text.substring(0, Math.min(cursor, text.length())));
        if (cursorPosX - scrollOffset > visibleWidth - 2) {
            scrollOffset = cursorPosX - visibleWidth + 2;
        } else if (cursorPosX - scrollOffset < 0) {
            scrollOffset = Math.max(0, cursorPosX);
        }
        // Update stored scroll offset
        if (isSearchField) {
            searchScrollOffset = scrollOffset;
        } else {
            replaceScrollOffset = scrollOffset;
        }
        
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GuiUtil.setScissorClip(fieldX + 2, fieldY, fieldWidth - 4, fieldHeight);
        
        if (text.isEmpty() && !focused) {
            font.drawString(placeholder, textX, textY, 0xFF808080);
        } else {
            // Draw selection highlight
            if (selStart != selEnd && focused) {
                int startX = textX + font.getStringWidth(text.substring(0, Math.min(selStart, text.length()))) - scrollOffset;
                int endX = textX + font.getStringWidth(text.substring(0, Math.min(selEnd, text.length()))) - scrollOffset;
                Gui.drawRect(startX, textY - 1, endX, textY + font.FONT_HEIGHT, 0xFF2d5ca6);
            }
            
            // Draw text with scroll offset
            font.drawString(text, textX - scrollOffset, textY, 0xFFe0e0e0);
            
            // Draw cursor (stays visible during typing)
            if (focused && shouldShowCursor()) {
                int cursorDrawX = textX + cursorPosX - scrollOffset;
                Gui.drawRect(cursorDrawX, textY - 1, cursorDrawX + 1, textY + font.FONT_HEIGHT, 0xFFffffff);
            }
        }
        
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }
    
    private void drawToggleButton(int btnX, int btnY, String label, boolean active, boolean hovered, String tooltip) {
        int bgColor = active ? 0xFF4a6da7 : (hovered ? 0xFF505050 : 0xFF3c3c3c);
        Gui.drawRect(btnX, btnY, btnX + buttonSize, btnY + buttonSize, bgColor);
        Gui.drawRect(btnX, btnY, btnX + buttonSize, btnY + 1, 0xFF606060);
        Gui.drawRect(btnX, btnY + buttonSize - 1, btnX + buttonSize, btnY + buttonSize, 0xFF2a2a2a);
        
        int textColor = active ? 0xFFffffff : (hovered ? 0xFFcccccc : 0xFF999999);
        int labelWidth = font.getStringWidth(label);
        font.drawString(label, btnX + (buttonSize - labelWidth) / 2, btnY + (buttonSize - font.FONT_HEIGHT) / 2 + 1, textColor);
    }
    
    private void drawNavButton(int btnX, int btnY, String arrow, boolean enabled, boolean hovered) {
        int bgColor = !enabled ? 0xFF2a2a2a : (hovered ? 0xFF505050 : 0xFF3c3c3c);
        Gui.drawRect(btnX, btnY, btnX + buttonSize, btnY + buttonSize, bgColor);
        
        int textColor = !enabled ? 0xFF555555 : (hovered ? 0xFFffffff : 0xFFaaaaaa);
        int arrowWidth = font.getStringWidth(arrow);
        font.drawString(arrow, btnX + (buttonSize - arrowWidth) / 2, btnY + (buttonSize - font.FONT_HEIGHT) / 2, textColor);
    }
    
    private void drawCloseButton(int btnX, int btnY, boolean hovered) {
        int bgColor = hovered ? 0xFFc75050 : 0xFF3c3c3c;
        Gui.drawRect(btnX, btnY, btnX + buttonSize, btnY + buttonSize, bgColor);
        
        int textColor = hovered ? 0xFFffffff : 0xFFaaaaaa;
        String xChar = "X";
        int xWidth = font.getStringWidth(xChar);
        font.drawString(xChar, btnX + (buttonSize - xWidth) / 2, btnY + (buttonSize - font.FONT_HEIGHT) / 2 + 1, textColor);
    }
    
    private void drawActionButton(int btnX, int btnY, String label, boolean hovered, boolean enabled) {
        int btnWidth = font.getStringWidth(label) + 10;
        int btnHeight = 14;
        
        int bgColor = !enabled ? 0xFF2a2a2a : (hovered ? 0xFF505050 : 0xFF3c3c3c);
        Gui.drawRect(btnX, btnY + 1, btnX + btnWidth, btnY + btnHeight + 1, bgColor);
        Gui.drawRect(btnX, btnY + 1, btnX + btnWidth, btnY + 2, 0xFF555555);
        Gui.drawRect(btnX, btnY + btnHeight, btnX + btnWidth, btnY + btnHeight + 1, 0xFF2a2a2a);
        
        int textColor = !enabled ? 0xFF555555 : (hovered ? 0xFFffffff : 0xFFaaaaaa);
        font.drawString(label, btnX + 5, btnY + (btnHeight - font.FONT_HEIGHT) / 2 + 2, textColor);
    }
    
    // ==================== HOVER DETECTION ====================
    
    private void updateHoverStates(int mouseX, int mouseY) {
        int rowY = y + padding;
        int currentX = x + padding;
        
        int searchFieldEndX = currentX + textFieldWidth;
        currentX = searchFieldEndX + buttonSpacing + 4;
        
        String matchInfo = matches.isEmpty() ? "0 results" : (currentMatchIndex + 1) + "/" + matches.size();
        currentX += font.getStringWidth(matchInfo) + 8;
        
        int matchCaseX = currentX;
        hoverMatchCase = isMouseOver(mouseX, mouseY, matchCaseX, rowY, buttonSize, buttonSize);
        currentX += buttonSize + buttonSpacing;
        
        int wholeWordX = currentX;
        hoverWholeWord = isMouseOver(mouseX, mouseY, wholeWordX, rowY, buttonSize, buttonSize);
        currentX += buttonSize + buttonSpacing + 4;
        
        int prevX = currentX;
        hoverPrev = isMouseOver(mouseX, mouseY, prevX, rowY, buttonSize, buttonSize);
        currentX += buttonSize + buttonSpacing;
        
        int nextX = currentX;
        hoverNext = isMouseOver(mouseX, mouseY, nextX, rowY, buttonSize, buttonSize);
        
        int closeX = x + width - buttonSize - padding;
        hoverClose = isMouseOver(mouseX, mouseY, closeX, rowY, buttonSize, buttonSize);

        int replaceUnwindButton = closeX - buttonSize - buttonSpacing;
        hoverSearchUnwind = isMouseOver(mouseX, mouseY, replaceUnwindButton, rowY, buttonSize, buttonSize);

        if (showReplace) {
            int replaceRowY = y + barHeight + 2;
            currentX = x + padding + textFieldWidth + buttonSpacing + 4;
            
            int replaceWidth = font.getStringWidth("Replace") + 12;
            hoverReplace = isMouseOver(mouseX, mouseY, currentX, replaceRowY + 1, replaceWidth, 14);
            currentX += replaceWidth + buttonSpacing;
            
            int replaceAllWidth = font.getStringWidth("Replace All") + 12;
            hoverReplaceAll = isMouseOver(mouseX, mouseY, currentX, replaceRowY + 1, replaceAllWidth, 14);
            currentX += replaceAllWidth + buttonSpacing;
            
            int excludeWidth = font.getStringWidth("Exclude") + 12;
            hoverExclude = isMouseOver(mouseX, mouseY, currentX, replaceRowY + 1, excludeWidth, 14);
        } else {
            hoverReplace = hoverReplaceAll = hoverExclude = false;
        }
    }
    
    private boolean isMouseOver(int mouseX, int mouseY, int bx, int by, int bw, int bh) {
        return mouseX >= bx && mouseX < bx + bw && mouseY >= by && mouseY < by + bh;
    }
    
    // ==================== MOUSE INPUT ====================
    
    /**
     * Handle mouse click - supports double-click for word selection, triple-click for all
     * @return true if click was consumed
     */
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (!visible) return false;
        if (button != 0) return false; // Only handle left clicks
        
        int totalHeight = getTotalHeight();
        // If click is outside the bar, don't consume it (let main editor handle)
        if (mouseX < x || mouseX > x + width || mouseY < y || mouseY > y + totalHeight) {
            return false;
        }
        
        int rowY = y + padding;
        int currentX = x + padding;
        
        // Handle click timing for double/triple click
        long now = System.currentTimeMillis();
        boolean isSearchFieldClick = isMouseOver(mouseX, mouseY, currentX, rowY - 2, textFieldWidth, textFieldHeight + 4);
        boolean isReplaceFieldClick = showReplace && isMouseOver(mouseX, mouseY, x + padding, y + barHeight, textFieldWidth, textFieldHeight + 4);

        if ((isSearchFieldClick || isReplaceFieldClick) && callback != null) callback.unfocusMainEditor();

        if (isSearchFieldClick) {
            boolean sameField = lastClickWasSearch;
            if (sameField && now - lastClickTime < 300) {
                clickCount++;
            } else {
                clickCount = 1;
            }
            lastClickTime = now;
            lastClickWasSearch = true;
            
            searchFieldFocused = true;
            replaceFieldFocused = false;
            markActivity();
            
            if (clickCount == 1) {
                // Single click - position cursor
                int clickX = mouseX - currentX - 4 + searchScrollOffset;
                searchCursor = getTextCursorPosition(searchText, clickX);
                searchSelectionStart = searchSelectionEnd = searchCursor;
            } else if (clickCount == 2) {
                // Double click - select word
                selectWordAt(searchText, getTextCursorPosition(searchText, mouseX - currentX - 4 + searchScrollOffset), true);
            } else if (clickCount >= 3) {
                // Triple click - select all
                searchSelectionStart = 0;
                searchSelectionEnd = searchText.length();
                searchCursor = searchText.length();
                clickCount = 0;
            }
            return true;
        }
        currentX += textFieldWidth + buttonSpacing + 4;
        
        String matchInfo = matches.isEmpty() ? "0 results" : (currentMatchIndex + 1) + "/" + matches.size();
        currentX += font.getStringWidth(matchInfo) + 8;
        
        if (hoverMatchCase) {
            matchCase = !matchCase;
            updateMatches();
            return true;
        }
        currentX += buttonSize + buttonSpacing;
        
        if (hoverWholeWord) {
            wholeWord = !wholeWord;
            updateMatches();
            return true;
        }
        currentX += buttonSize + buttonSpacing + 4;
        
        if (hoverPrev && !matches.isEmpty()) {
            goToPreviousMatch();
            return true;
        }
        currentX += buttonSize + buttonSpacing;
        
        if (hoverNext && !matches.isEmpty()) {
            goToNextMatch();
            return true;
        }
        
        if (hoverClose) {
            close();
            return true;
        }

        if (hoverSearchUnwind) {
            showReplace = !showReplace;
            return true;
        }

        if (showReplace) {
            int replaceRowY = y + barHeight + 2;
            
            if (isReplaceFieldClick || isMouseOver(mouseX, mouseY, x + padding, replaceRowY - 2, textFieldWidth, textFieldHeight + 4)) {
                // Handle click timing for replace field
                boolean sameField = !lastClickWasSearch;
                if (sameField && now - lastClickTime < 300) {
                    clickCount++;
                } else {
                    clickCount = 1;
                }
                lastClickTime = now;
                lastClickWasSearch = false;
                
                searchFieldFocused = false;
                replaceFieldFocused = true;
                markActivity();
                
                if (clickCount == 1) {
                    int clickX = mouseX - x - padding - 4 + replaceScrollOffset;
                    replaceCursor = getTextCursorPosition(replaceText, clickX);
                    replaceSelectionStart = replaceSelectionEnd = replaceCursor;
                } else if (clickCount == 2) {
                    selectWordAt(replaceText, getTextCursorPosition(replaceText, mouseX - x - padding - 4 + replaceScrollOffset), false);
                } else if (clickCount >= 3) {
                    replaceSelectionStart = 0;
                    replaceSelectionEnd = replaceText.length();
                    replaceCursor = replaceText.length();
                    clickCount = 0;
                }
                return true;
            }
            
            if (hoverReplace && !matches.isEmpty()) {
                replaceCurrent();
                return true;
            }
            
            if (hoverReplaceAll && !matches.isEmpty()) {
                replaceAll();
                return true;
            }
            
            if (hoverExclude && currentMatchIndex >= 0) {
                excludeCurrentMatch();
                return true;
            }
        }
        
        // Click was within bar bounds but not on any control - consume it to prevent MTA from getting focus
        return true;
    }
    
    private int getTextCursorPosition(String text, int clickX) {
        if (text.isEmpty() || clickX <= 0) return 0;
        
        for (int i = 1; i <= text.length(); i++) {
            int w = font.getStringWidth(text.substring(0, i));
            if (clickX < w) {
                int prevW = font.getStringWidth(text.substring(0, i - 1));
                return clickX < (prevW + w) / 2 ? i - 1 : i;
            }
        }
        return text.length();
    }
    
    /**
     * Select word at cursor position (for double-click)
     */
    private void selectWordAt(String text, int pos, boolean isSearchField) {
        if (text.isEmpty()) return;
        
        // Find word boundaries
        int start = pos;
        int end = pos;
        
        // Expand left
        while (start > 0 && isWordChar(text.charAt(start - 1))) {
            start--;
        }
        // Expand right
        while (end < text.length() && isWordChar(text.charAt(end))) {
            end++;
        }
        
        if (isSearchField) {
            searchSelectionStart = start;
            searchSelectionEnd = end;
            searchCursor = end;
        } else {
            replaceSelectionStart = start;
            replaceSelectionEnd = end;
            replaceCursor = end;
        }
    }
    
    private boolean isWordChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }
    
    // ==================== KEYBOARD INPUT ====================
    
    /**
     * Handle keyboard input
     * @return true if input was consumed
     */
    public boolean keyTyped(char c, int keyCode) {
        if (!visible || (!searchFieldFocused && !replaceFieldFocused)) return false;
        
        // Check modifier keys - don't let them trigger first-match issue
        boolean ctrl = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
        boolean shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
        boolean alt = Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU);
        
        // Ignore lone modifier key presses
        if (keyCode == Keyboard.KEY_LCONTROL || keyCode == Keyboard.KEY_RCONTROL ||
            keyCode == Keyboard.KEY_LSHIFT || keyCode == Keyboard.KEY_RSHIFT ||
            keyCode == Keyboard.KEY_LMENU || keyCode == Keyboard.KEY_RMENU) {
            return true; // Consume but don't process
        }
        
        if (keyCode == Keyboard.KEY_ESCAPE) {
            close();
            return true;
        }
        
        if (keyCode == Keyboard.KEY_TAB && showReplace) {
            searchFieldFocused = !searchFieldFocused;
            replaceFieldFocused = !replaceFieldFocused;
            markActivity();
            return true;
        }
        
        // Enter/Shift+Enter for next/previous match
        if (keyCode == Keyboard.KEY_RETURN) {
            if (shift) {
                // Shift+Enter = previous match
                if (!matches.isEmpty()) {
                    goToPreviousMatch();
                }
            } else if (replaceFieldFocused && !matches.isEmpty()) {
                replaceCurrent();
            } else if (!matches.isEmpty()) {
                goToNextMatch();
            }
            return true;
        }
        
        // Up/Down arrows cycle through matches when SRB is focused
        if (keyCode == Keyboard.KEY_UP && !matches.isEmpty()) {
            goToPreviousMatch();
            return true;
        }
        if (keyCode == Keyboard.KEY_DOWN && !matches.isEmpty()) {
            goToNextMatch();
            return true;
        }
        
        if (searchFieldFocused) {
            handleTextFieldInput(c, keyCode, true);
        } else if (replaceFieldFocused) {
            handleTextFieldInput(c, keyCode, false);
        }
        
        return true;
    }
    
    private void handleTextFieldInput(char c, int keyCode, boolean isSearchField) {
        String text = isSearchField ? searchText : replaceText;
        int cursor = isSearchField ? searchCursor : replaceCursor;
        int selStart = isSearchField ? searchSelectionStart : replaceSelectionStart;
        int selEnd = isSearchField ? searchSelectionEnd : replaceSelectionEnd;
        
        boolean hasSelection = selStart != selEnd;
        boolean ctrl = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
        boolean shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
        
        if (ctrl && keyCode == Keyboard.KEY_A) {
            selStart = 0;
            selEnd = text.length();
            cursor = text.length();
        }
        else if (ctrl && keyCode == Keyboard.KEY_C) {
            if (hasSelection) {
                String selected = text.substring(Math.min(selStart, selEnd), Math.max(selStart, selEnd));
                setClipboard(selected);
            }
        }
        else if (ctrl && keyCode == Keyboard.KEY_X) {
            if (hasSelection) {
                String selected = text.substring(Math.min(selStart, selEnd), Math.max(selStart, selEnd));
                setClipboard(selected);
                text = text.substring(0, Math.min(selStart, selEnd)) + text.substring(Math.max(selStart, selEnd));
                cursor = Math.min(selStart, selEnd);
                selStart = selEnd = cursor;
            }
        }
        else if (ctrl && keyCode == Keyboard.KEY_V) {
            String clipboard = getClipboard();
            if (clipboard != null) {
                clipboard = clipboard.replace("\n", "").replace("\r", "");
                if (hasSelection) {
                    text = text.substring(0, Math.min(selStart, selEnd)) + clipboard + text.substring(Math.max(selStart, selEnd));
                    cursor = Math.min(selStart, selEnd) + clipboard.length();
                } else {
                    text = text.substring(0, cursor) + clipboard + text.substring(cursor);
                    cursor += clipboard.length();
                }
                selStart = selEnd = cursor;
            }
        }
        else if (keyCode == Keyboard.KEY_BACK) {
            if (hasSelection) {
                text = text.substring(0, Math.min(selStart, selEnd)) + text.substring(Math.max(selStart, selEnd));
                cursor = Math.min(selStart, selEnd);
            } else if (cursor > 0) {
                text = text.substring(0, cursor - 1) + text.substring(cursor);
                cursor--;
            }
            selStart = selEnd = cursor;
        }
        else if (keyCode == Keyboard.KEY_DELETE) {
            if (hasSelection) {
                text = text.substring(0, Math.min(selStart, selEnd)) + text.substring(Math.max(selStart, selEnd));
                cursor = Math.min(selStart, selEnd);
            } else if (cursor < text.length()) {
                text = text.substring(0, cursor) + text.substring(cursor + 1);
            }
            selStart = selEnd = cursor;
        }
        else if (keyCode == Keyboard.KEY_LEFT) {
            if (shift) {
                if (cursor > 0) {
                    cursor--;
                    if (selStart == selEnd) {
                        selEnd = cursor + 1;
                        selStart = cursor;
                    } else if (cursor < selStart) {
                        selStart = cursor;
                    } else {
                        selEnd = cursor;
                    }
                }
            } else {
                if (hasSelection) {
                    cursor = Math.min(selStart, selEnd);
                } else if (cursor > 0) {
                    cursor--;
                }
                selStart = selEnd = cursor;
            }
        }
        else if (keyCode == Keyboard.KEY_RIGHT) {
            if (shift) {
                if (cursor < text.length()) {
                    cursor++;
                    if (selStart == selEnd) {
                        selStart = cursor - 1;
                        selEnd = cursor;
                    } else if (cursor > selEnd) {
                        selEnd = cursor;
                    } else {
                        selStart = cursor;
                    }
                }
            } else {
                if (hasSelection) {
                    cursor = Math.max(selStart, selEnd);
                } else if (cursor < text.length()) {
                    cursor++;
                }
                selStart = selEnd = cursor;
            }
        }
        else if (keyCode == Keyboard.KEY_HOME) {
            if (shift) {
                selEnd = selStart == selEnd ? cursor : selEnd;
                selStart = 0;
            } else {
                selStart = selEnd = 0;
            }
            cursor = 0;
        }
        else if (keyCode == Keyboard.KEY_END) {
            if (shift) {
                selStart = selStart == selEnd ? cursor : selStart;
                selEnd = text.length();
            } else {
                selStart = selEnd = text.length();
            }
            cursor = text.length();
        }
        else if (ChatAllowedCharacters.isAllowedCharacter(c)) {
            if (hasSelection) {
                text = text.substring(0, Math.min(selStart, selEnd)) + c + text.substring(Math.max(selStart, selEnd));
                cursor = Math.min(selStart, selEnd) + 1;
            } else {
                text = text.substring(0, cursor) + c + text.substring(cursor);
                cursor++;
            }
            selStart = selEnd = cursor;
        }
        
        if (selStart > selEnd) {
            int temp = selStart;
            selStart = selEnd;
            selEnd = temp;
        }
        
        // Mark activity on any input to keep cursor visible
        markActivity();
        
        if (isSearchField) {
            searchText = text;
            searchCursor = cursor;
            searchSelectionStart = selStart;
            searchSelectionEnd = selEnd;
            updateMatches();
        } else {
            replaceText = text;
            replaceCursor = cursor;
            replaceSelectionStart = selStart;
            replaceSelectionEnd = selEnd;
        }
    }
    
    private void setClipboard(String text) {
        try {
            java.awt.datatransfer.StringSelection selection = new java.awt.datatransfer.StringSelection(text);
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
        } catch (Exception ignored) {}
    }
    
    private String getClipboard() {
        try {
            return (String) java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().getData(java.awt.datatransfer.DataFlavor.stringFlavor);
        } catch (Exception e) {
            return null;
        }
    }
    
    // ==================== SEARCH LOGIC ====================
    
    /**
     * Update matches based on current search text and options
     */
    public void updateMatches() {
        matches.clear();
        excludedMatches.clear();
        currentMatchIndex = -1;
        
        if (callback == null || searchText.isEmpty()) {
            if (callback != null) callback.onMatchesUpdated();
            return;
        }
        
        String sourceText = callback.getText();
        if (sourceText == null || sourceText.isEmpty()) {
            callback.onMatchesUpdated();
            return;
        }
        
        String searchPattern = searchText;
        
        if (wholeWord) {
            searchPattern = "\\b" + Pattern.quote(searchPattern) + "\\b";
        } else {
            searchPattern = Pattern.quote(searchPattern);
        }
        
        int flags = matchCase ? 0 : Pattern.CASE_INSENSITIVE;
        
        try {
            Pattern pattern = Pattern.compile(searchPattern, flags);
            Matcher matcher = pattern.matcher(sourceText);
            
            while (matcher.find()) {
                matches.add(new int[]{matcher.start(), matcher.end()});
            }
        } catch (Exception ignored) {
        }
        
        if (!matches.isEmpty()) {
            currentMatchIndex = 0;
            navigateToCurrentMatch();
        }
        
        callback.onMatchesUpdated();
    }
    
    /**
     * Navigate to next match
     */
    public void goToNextMatch() {
        if (matches.isEmpty()) return;
        
        int startIndex = currentMatchIndex;
        do {
            currentMatchIndex = (currentMatchIndex + 1) % matches.size();
        } while (excludedMatches.contains(currentMatchIndex) && currentMatchIndex != startIndex);
        
        navigateToCurrentMatch();
    }
    
    /**
     * Navigate to previous match
     */
    public void goToPreviousMatch() {
        if (matches.isEmpty()) return;
        
        int startIndex = currentMatchIndex;
        do {
            currentMatchIndex = (currentMatchIndex - 1 + matches.size()) % matches.size();
        } while (excludedMatches.contains(currentMatchIndex) && currentMatchIndex != startIndex);
        
        navigateToCurrentMatch();
    }
    
    /**
     * Navigate viewport to current match and select it
     */
    private void navigateToCurrentMatch() {
        if (callback == null || currentMatchIndex < 0 || currentMatchIndex >= matches.size()) return;
        
        int[] match = matches.get(currentMatchIndex);
        callback.scrollToPosition(match[0]);
        callback.setSelection(match[0], match[1]);
    }
    
    // ==================== REPLACE LOGIC ====================
    
    /**
     * Replace current match
     */
    public void replaceCurrent() {
        if (callback == null || currentMatchIndex < 0 || matches.isEmpty()) return;
        if (excludedMatches.contains(currentMatchIndex)) {
            goToNextMatch();
            return;
        }
        
        String sourceText = callback.getText();
        int[] match = matches.get(currentMatchIndex);
        
        String newText = sourceText.substring(0, match[0]) + replaceText + sourceText.substring(match[1]);
        callback.setText(newText);
        
        updateMatches();
        
        if (!matches.isEmpty()) {
            int replacementEnd = match[0] + replaceText.length();
            for (int i = 0; i < matches.size(); i++) {
                if (matches.get(i)[0] >= replacementEnd) {
                    currentMatchIndex = i;
                    break;
                }
            }
            navigateToCurrentMatch();
        }
    }
    
    /**
     * Replace all matches
     */
    public void replaceAll() {
        if (callback == null || matches.isEmpty()) return;
        
        String sourceText = callback.getText();
        StringBuilder result = new StringBuilder();
        int lastEnd = 0;
        
        for (int i = 0; i < matches.size(); i++) {
            if (excludedMatches.contains(i)) {
                continue;
            }
            int[] match = matches.get(i);
            result.append(sourceText, lastEnd, match[0]);
            result.append(replaceText);
            lastEnd = match[1];
        }
        result.append(sourceText.substring(lastEnd));
        
        callback.setText(result.toString());
        updateMatches();
    }
    
    /**
     * Exclude current match from replacement
     */
    public void excludeCurrentMatch() {
        if (currentMatchIndex >= 0 && !excludedMatches.contains(currentMatchIndex)) {
            excludedMatches.add(currentMatchIndex);
            goToNextMatch();
        }
    }
    
    // ==================== UTILITY ====================
    
    /**
     * Get search text for external highlighting
     */
    public String getSearchText() {
        return searchText;
    }
    
    /**
     * Check if bar consumes click at position
     */
    public boolean isMouseOverBar(int mouseX, int mouseY) {
        if (!visible) return false;
        int totalHeight = getTotalHeight();
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + totalHeight;
    }
}
