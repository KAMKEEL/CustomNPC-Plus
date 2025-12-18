package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ChatAllowedCharacters;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.gui.util.script.*;
import noppes.npcs.client.gui.util.script.JavaTextContainer.LineData;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import static net.minecraft.client.gui.GuiScreen.isCtrlKeyDown;

/**
 * Script text editor component with syntax highlighting, bracket matching,
 * smooth scrolling, and IDE-like features.
 * 
 * Helper classes used:
 * - ScrollState: smooth scroll animation and state management
 * - SelectionState: cursor position and text selection management
 * - BracketMatcher: bracket matching and brace span computation
 * - IndentHelper: indentation utilities and text formatting
 * - CommentHandler: line comment toggling
 * - CursorNavigation: cursor movement logic
 */
public class GuiScriptTextArea extends GuiNpcTextField {
    
    // ==================== DIMENSIONS & POSITION ====================
    public int id;
    public int x;
    public int y;
    public int width;
    public int height;
    
    // ==================== STATE FLAGS ====================
    public boolean active = false;
    public boolean enabled = true;
    public boolean visible = true;
    public boolean clicked = false;
    public boolean doubleClicked = false;
    public boolean tripleClicked = false;
    private int clickCount = 0;
    private long lastClicked = 0L;
    
    // ==================== TEXT & CONTAINER ====================
    public String text = null;
    private JavaTextContainer container = null;
    private boolean enableCodeHighlighting = false;

    // ==================== HELPER CLASS INSTANCES ====================
    private final ScrollState scroll = new ScrollState();
    private final SelectionState selection = new SelectionState();
    
    // ==================== UI COMPONENTS ====================
    private int cursorCounter;
    private ITextChangeListener listener;
    private static int LINE_NUMBER_GUTTER_WIDTH = 25;
    
    // ==================== UNDO/REDO ====================
    public List<UndoData> undoList = new ArrayList<>();
    public List<UndoData> redoList = new ArrayList<>();
    public boolean undoing = false;
    
    // ==================== CONSTRUCTOR ====================

    public GuiScriptTextArea(GuiScreen guiScreen, int id, int x, int y, int width, int height, String text) {
        super(id, guiScreen, x, y, width, height, null);
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.undoing = true;
        this.setText(text);
        this.undoing = false;
    }
    
    // ==================== RENDERING ====================
    public void drawTextBox(int xMouse, int yMouse) {
        if (!visible)
            return;
        clampSelectionBounds();
        
        // Dynamically calculate gutter width based on line count digits
        if (container != null && container.linesCount > 0) {
            int maxLineNum = container.linesCount;
            String maxLineStr = String.valueOf(maxLineNum);
            int digitWidth = ClientProxy.Font.width(maxLineStr);
            LINE_NUMBER_GUTTER_WIDTH = digitWidth + 10; // 10px total padding (5px left + 5px right)
        }
        // Draw outer border around entire area
        drawRect(x - 1, y - 1, x + width + 1, y + height + 1, 0xffa0a0a0);
        // Draw line number gutter background
        drawRect(x, y, x + LINE_NUMBER_GUTTER_WIDTH, y + height, 0xff000000);
        // Draw text viewport background (starts after gutter)
        drawRect(x + LINE_NUMBER_GUTTER_WIDTH, y, x + width, y + height, 0xff000000);
        // Draw separator line between gutter and text area
        drawRect(x + LINE_NUMBER_GUTTER_WIDTH-1, y, x + LINE_NUMBER_GUTTER_WIDTH, y + height, 0xff3c3f41);

        // Enable scissor test to clip drawing to the TEXT viewport rectangle (excludes gutter)
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        scissorViewport();
        
        container.visibleLines = height / container.lineHeight-1;

        int maxScroll = Math.max(0, this.container.linesCount - container.visibleLines);

        // Handle mouse wheel scroll
        if (listener instanceof GuiNPCInterface) {
            int wheelDelta = ((GuiNPCInterface) listener).mouseScroll = Mouse.getDWheel();
            if (wheelDelta != 0) 
                scroll.applyWheelScroll(wheelDelta, maxScroll);
        }

        // Handle scrollbar dragging (delegated to ScrollState)
        if (scroll.isClickScrolling())
            scroll.handleClickScrolling(yMouse, x, y, height, container.visibleLines, container.linesCount, maxScroll);
        
        // Update scroll animation
        scroll.initializeIfNeeded(scroll.getScrolledLine());
        scroll.update(maxScroll);

        // Handle click-dragging for selection
        if (clicked) {
            clicked = Mouse.isButtonDown(0);
            int i = getSelectionPos(xMouse, yMouse);
            if (i != selection.getCursorPosition()) {
                if (doubleClicked || tripleClicked) {
                    selection.reset(selection.getCursorPosition());
                    doubleClicked = false;
                    tripleClicked = false;
                }
                setCursor(i, true);
            }
        } else if (doubleClicked || tripleClicked) {
            doubleClicked = false;
            tripleClicked = false;
        }

        // Calculate braces next to cursor to highlight
        int startBracket = 0, endBracket = 0;
        if (selection.getStartSelection() >= 0 && text != null && text.length() > 0 &&
                (selection.getEndSelection() - selection.getStartSelection() == 1 || !selection.hasSelection())) {
            int[] span = BracketMatcher.findBracketSpanAt(text,selection.getStartSelection());
            if (span != null) {
                startBracket = span[0];
                endBracket = span[1];
            }
        }

        List<JavaTextContainer.LineData> list = new ArrayList<>(container.lines);

        // Build brace spans: {origDepth, open line, close line, adjustedDepth}
        List<int[]> braceSpans = BracketMatcher.computeBraceSpans(text, list);

        // Determine which exact brace span (openLine/closeLine) should be highlighted based on bracket under caret
        int highlightedOpenLine = -1;
        int highlightedCloseLine = -1;
        if (startBracket != endBracket && startBracket >= 0) {
            int bracketLineIdx = -1;
            for (int li = 0; li < list.size(); li++) {
                LineData ld = list.get(li);
                if (startBracket >= ld.start && startBracket < ld.end) {
                    bracketLineIdx = li;
                    break;
                }
            }
            if (bracketLineIdx >= 0) {
                for (int[] span : braceSpans) {
                    int openLine = span[1];
                    int closeLine = span[2];
                    if (bracketLineIdx >= openLine && bracketLineIdx <= closeLine) {
                        highlightedOpenLine = openLine;
                        highlightedCloseLine = closeLine;
                        break;
                    }
                }
            }
        }

        String wordHightLight = null;
        if (selection.hasSelection()) {
            Matcher m = container.regexWord.matcher(text);
            while (m.find()) {
                if (m.start() == selection.getStartSelection() && m.end() == selection.getEndSelection()) {
                    wordHightLight = text.substring(selection.getStartSelection(), selection.getEndSelection());
                }
            }
        }
        // Expand render range by one line above/below so partially-visible lines are drawn
        int renderStart = Math.max(0, scroll.getScrolledLine() - 1);
        int renderEnd = Math.min(list.size() - 1, scroll.getScrolledLine() + container.visibleLines + 1);

        // Apply fractional GL translate for sub-pixel smooth scrolling
        int stringYOffset = 2;
        double fracOffset = scroll.getFractionalOffset();
        float fracPixels = (float) (fracOffset * container.lineHeight);
        GL11.glPushMatrix();
        GL11.glTranslatef(0.0f, -fracPixels, 0.0f);
        
        // Render LINE GUTTER numbers
        for (int i = renderStart; i <= renderEnd; i++) {
            int posY = y + (i - scroll.getScrolledLine()) * container.lineHeight + stringYOffset;
            String lineNum = String.valueOf(i + 1);
            int lineNumWidth = ClientProxy.Font.width(lineNum);
            int lineNumX = x + LINE_NUMBER_GUTTER_WIDTH - lineNumWidth - 5; // right-align with 5px padding
            int lineNumY = posY + 1;
            // Highlight current line number
            int lineNumColor = 0xFF606366;
            if (active && isEnabled()) {
                for (int li = 0; li < list.size(); li++) {
                    LineData ld = list.get(li);
                    if (selection.getCursorPosition() >= ld.start && selection.getCursorPosition() < ld.end || (li == list.size() - 1 && selection.getCursorPosition() == text.length())) {
                        if (li == i) {
                            lineNumColor = 0xFFb9c7d6;
                            break;
                        }
                    }
                }
            }
            ClientProxy.Font.drawString(lineNum, lineNumX, lineNumY, lineNumColor);
        }

        // Render Viewport
        for (int i = renderStart; i <= renderEnd; i++) {
            LineData data = list.get(i);
            String line = data.text;
            int w = line.length();
            // Use integer Y relative to scrolledLine; fractional offset applied via GL translate
            int posY = y + (i - scroll.getScrolledLine()) * container.lineHeight;
            if (i >= renderStart && i <= renderEnd) {
                //Highlight braces the cursor position is on
                if (startBracket != endBracket) {
                    if (startBracket >= data.start && startBracket < data.end) {
                        int s = ClientProxy.Font.width(line.substring(0, startBracket - data.start));
                        int e = ClientProxy.Font.width(line.substring(0, startBracket - data.start + 1)) + 1;
                        drawRect(x + LINE_NUMBER_GUTTER_WIDTH + 1 + s, posY, x + LINE_NUMBER_GUTTER_WIDTH + 1 + e, posY + container.lineHeight + 0, 0x9900cc00);
                    }
                    if (endBracket >= data.start && endBracket < data.end) {
                        int s = ClientProxy.Font.width(line.substring(0, endBracket - data.start));
                        int e = ClientProxy.Font.width(line.substring(0, endBracket - data.start + 1)) + 1;
                        drawRect(x + LINE_NUMBER_GUTTER_WIDTH + 1 + s, posY, x + LINE_NUMBER_GUTTER_WIDTH + 1 + e, posY + container.lineHeight + 0, 0x9900cc00);
                    }
                }
                //Highlight words
                if (wordHightLight != null) {
                    Matcher m = container.regexWord.matcher(line);
                    while (m.find()) {
                        if (line.substring(m.start(), m.end()).equals(wordHightLight)) {
                            int s = ClientProxy.Font.width(line.substring(0, m.start()));
                            int e = ClientProxy.Font.width(line.substring(0, m.end())) + 1;
                            drawRect(x + LINE_NUMBER_GUTTER_WIDTH + 1 + s, posY, x + LINE_NUMBER_GUTTER_WIDTH + 1 + e, posY + container.lineHeight, 0x99004c00);
                        }
                    }
                }
                // Highlight the current line (light gray) under any selection
                if (active && isEnabled() && (selection.getCursorPosition() >= data.start && selection.getCursorPosition() < data.end || (i == list.size() - 1 && selection.getCursorPosition() == text.length()))) {
                    drawRect(x , posY, x + width - 1, posY + container.lineHeight, 0x22e0e0e0);
                }
                // Highlight selection
                if (selection.hasSelection() && selection.getEndSelection() > data.start && selection.getStartSelection() <= data.end) {
                    if (selection.getStartSelection() < data.end) {
                        int s = ClientProxy.Font.width(
                                line.substring(0, Math.max(selection.getStartSelection() - data.start, 0)));
                        int e = ClientProxy.Font.width(
                                line.substring(0, Math.min(selection.getEndSelection() - data.start, w))) + 1;
                        drawRect(x + LINE_NUMBER_GUTTER_WIDTH + 1 + s, posY, x + LINE_NUMBER_GUTTER_WIDTH + 1 + e, posY + container.lineHeight, 0x992172ff);
                    }
                }

                // Draw indent guides once per visible block based on brace spans
                if (i == Math.max(0, scroll.getScrolledLine()) && !braceSpans.isEmpty()) {
                    int visStart = Math.max(0, scroll.getScrolledLine());
                    int visEnd = Math.min(list.size() - 1, visStart + container.visibleLines - 0);
                    for (int[] span : braceSpans) {
                        int originalDepth = span[0];
                        int openLine = span[1];
                        int closeLine = span[2];
                        int depth = span.length > 3 ? span[3] : originalDepth;
                        // Skip top-level (depth 1) using the original depth to avoid hiding nested guides when adjusted
                        if (originalDepth <= 1)
                            continue;
                        int startLine = openLine + 1; // start under the opening brace
                        int endLine = closeLine - 1;  // stop before the closing brace
                        if (startLine > endLine)
                            continue;
                        if (endLine < visStart || startLine > visEnd)
                            continue;

                        int drawStart = Math.max(startLine, visStart);
                        int drawEnd = Math.min(endLine, visEnd);
                        // Compute horizontal position: 4 spaces per indent level, minus a tiny left offset
                        int safeDepth = Math.max(1, depth);
                        int spaces = (safeDepth - 1) * 4;
                        StringBuilder sb = new StringBuilder();
                        for (int k = 0; k < spaces; k++)
                            sb.append(' ');
                        int px = ClientProxy.Font.width(sb.toString());
                        int gx = x + LINE_NUMBER_GUTTER_WIDTH + 4 + px - 2; // shift left ~2px for the IntelliJ feel

                        int topY = y + (drawStart - scroll.getScrolledLine()) * container.lineHeight;
                        int bottomY = y + (endLine - scroll.getScrolledLine() + 1) * container.lineHeight;
                            int guideColor = (openLine == highlightedOpenLine && closeLine == highlightedCloseLine) ? 0x9933cc00 : 0x33FFFFFF;
                            drawRect(gx, topY, gx + 1, bottomY, guideColor);
                    }
                }
                int yPos = posY + stringYOffset;
                data.drawString(x + LINE_NUMBER_GUTTER_WIDTH + 1, yPos, 0xFFe0e0e0);

                // Draw cursor: pause blinking while user is active recently
                boolean recentInput = selection.hadRecentInput();
                if (active && isEnabled() && (recentInput || (cursorCounter / 10) % 2 == 0) && (selection.getCursorPosition() >= data.start && selection.getCursorPosition() < data.end || (i == list.size() - 1 && selection.getCursorPosition() == text.length()))) {
                    int posX = x + LINE_NUMBER_GUTTER_WIDTH + ClientProxy.Font.width(
                            line.substring(0, Math.min(selection.getCursorPosition() - data.start, line.length())));
                    drawRect(posX + 1, yPos -1, posX + 2, yPos - 1 + container.lineHeight, 0xffffffff);
                }
            }
        }
        GL11.glPopMatrix();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        
        if (hasVerticalScrollbar()) {
            Minecraft.getMinecraft().renderEngine.bindTexture(GuiCustomScroll.resource);
            int sbSize = Math.max((int) (1f * (container.visibleLines) / container.linesCount * height), 2);

            int posX = x + width - 6;
            double linesCount = Math.max(1, (double) container.linesCount);
            int posY = (int) (y + 1f * scroll.getScrollPos() / linesCount * (height - 4)) + 1;

            drawRect(posX, posY, posX + 5, posY + sbSize + 2, 0xFFe0e0e0);
        }
    }

    private void scissorViewport() {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int scaleFactor = sr.getScaleFactor();
        int scissorX = (this.x) * scaleFactor;
        int scissorY = (sr.getScaledHeight() - (this.y + this.height)) * scaleFactor;
        int scissorW = (this.width) * scaleFactor;
        int scissorH = this.height * scaleFactor;
        GL11.glScissor(scissorX, scissorY, scissorW, scissorH);
    }
    // ==================== SELECTION & CURSOR POSITION ====================

    // Get cursor position from mouse coordinates
    private int getSelectionPos(int xMouse, int yMouse) {
        xMouse -= (this.x + LINE_NUMBER_GUTTER_WIDTH + 1);
        yMouse -= this.y + 1;
        // Adjust yMouse to account for fractional GL translation (negative offset applied in rendering)
        double fracPixels = scroll.getFractionalOffset() * container.lineHeight;
        yMouse = (int) Math.round(yMouse + fracPixels);
        
        ArrayList list = new ArrayList(this.container.lines);

        for (int i = 0; i < list.size(); ++i) {
            LineData data = (LineData) list.get(i);
            if (i >= scroll.getScrolledLine() && i <= scroll.getScrolledLine() + this.container.visibleLines) {
                int yPos = (i - scroll.getScrolledLine()) * this.container.lineHeight;
                if (yMouse >= yPos && yMouse < yPos + this.container.lineHeight) {
                    int lineWidth = 0;
                    char[] chars = data.text.toCharArray();

                    for (int j = 1; j <= chars.length; ++j) {
                        int w = ClientProxy.Font.width(data.text.substring(0, j));
                        if (xMouse < lineWidth + (w - lineWidth) / 2) {
                            return data.start + j - 1;
                        }

                        lineWidth = w;
                    }

                    // Place cursor after the last visible character of the line.
                    // `data.end - 1` previously pointed at the newline for non-last lines
                    // which made clicks land on the newline rather than after the text.
                    // Use data.start + chars.length to return the position directly
                    // after the line's characters, clamped to the total text length.
                    int posAfterChars = data.start + chars.length;
                    return Math.min(posAfterChars, text.length());
                }
            }
        }

        return this.container.text.length();
    }

    // Find which line the cursor is on (0-indexed)
    private int getCursorLineIndex() {
        return selection.getCursorLineIndex(container.lines, text != null ? text.length() : 0);
    }
    
    
    // Scroll viewport to keep cursor visible (minimal adjustment, like IntelliJ)
    // Only scrolls if cursor is outside the visible area
    private void scrollToCursor() {
        if (container == null || container.lines == null || container.lines.isEmpty()) return;
        
        int lineIdx = getCursorLineIndex();
        int visible = Math.max(1, container.visibleLines);
        int maxScroll = Math.max(0, container.linesCount - visible);

        scroll.scrollToLine(lineIdx, visible, maxScroll);
    }
    
    // ==================== KEYBOARD INPUT HANDLING ====================

    /**
     * Handles keyboard input for the text area, delegating to specialized handlers
     * for different types of input: navigation, deletion, shortcuts, and character input.
     */
    @Override
    public boolean textboxKeyTyped(char c, int i) {
        if (!active)
            return false;

        if (this.isKeyComboCtrlA(i)) {
            selection.selectAll(text.length());
            return true;
        }

        if (!isEnabled())
            return false;

        if (handleNavigationKeys(i)) return true;
        if (handleDeletionKeys(i)) return true;
        if (handleShortcutKeys(i)) return true;
        if (handleCharacterInput(c)) return true;

        return true;
    }

    /**
     * Handles cursor navigation keys (arrows) with support for word-jumping (Ctrl)
     * and selection (Shift). Updates cursor position and scrolls viewport if needed.
     */
    private boolean handleNavigationKeys(int i) {
        // LEFT ARROW: move cursor left; with Ctrl -> jump by word
        if (i == Keyboard.KEY_LEFT) {
            int j = 1; // default: move one character
            if (isCtrlKeyDown()) {
                // When Ctrl is down, compute distance to previous word boundary.
                // We match words in the text slice before the cursor and pick
                // the last match start as the new boundary.
                Matcher m = container.regexWord.matcher(text.substring(0, selection.getCursorPosition()));
                while (m.find()) {
                    if (m.start() == m.end())
                        continue; // skip empty matches
                    // j becomes the number of chars to move left to reach word start
                    j = selection.getCursorPosition() - m.start();
                }
            }
            int newPos = Math.max(selection.getCursorPosition() - j, 0);
            // If Shift is held, extend selection; otherwise place caret.
            setCursor(newPos, GuiScreen.isShiftKeyDown());
            return true;
        }

        // RIGHT ARROW: move cursor right; with Ctrl -> jump to next word start
        if (i == Keyboard.KEY_RIGHT) {
            int j = 1; // default: move one character
            if (isCtrlKeyDown()) {
                // With Ctrl, search for the next word boundary starting at the cursor.
                // We look at the substring after the cursor and use the matcher to find
                // the first match; its start offset indicates how many chars to skip.
                Matcher m = container.regexWord.matcher(text.substring(selection.getCursorPosition()));
                // m.find() called once is sufficient; the previous code attempted
                // a redundant second find in some cases. Keep first positive find.
                if (m.find()) {
                    j = m.start();
                }
            }
            int newPos = Math.min(selection.getCursorPosition() + j, text.length());
            setCursor(newPos, GuiScreen.isShiftKeyDown());
            return true;
        }

        // UP/DOWN: logical cursor movement across lines while preserving
        // column where possible. After moving, ensure the caret remains visible
        // by adjusting the scroll if necessary.
        if (i == Keyboard.KEY_UP) {
            setCursor(cursorUp(), GuiScreen.isShiftKeyDown());
            scrollToCursor();
            return true;
        }
        if (i == Keyboard.KEY_DOWN) {
            setCursor(cursorDown(), GuiScreen.isShiftKeyDown());
            scrollToCursor();
            return true;
        }

        return false; // not a navigation key
    }

    /**
     * Handles deletion keys: Delete, Backspace, and Ctrl+Backspace.
     * Includes smart backspace behavior for indentation-aware line merging,
     * auto-pair deletion for brackets/quotes, and word-level deletion.
     */
    private boolean handleDeletionKeys(int i) {
        // DELETE key: remove the character under the cursor if no selection,
        // otherwise remove the selected region's tail.
        if (i == Keyboard.KEY_DELETE) {
            String s = getSelectionAfterText();
            if (!s.isEmpty() && !selection.hasSelection())
                // remove single character after caret when nothing is selected
                s = s.substring(1);
            setText(getSelectionBeforeText() + s);
            // Keep caret at same start selection
            selection.reset(selection.getStartSelection());
            return true;
        }

        // CTRL+BACKSPACE: delete to previous word or whitespace boundary.
        if (isKeyComboCtrlBackspace(i)) {
            String s = getSelectionBeforeText();
            if (selection.getStartSelection() > 0 && !selection.hasSelection()) {
                int nearestCondition = selection.getCursorPosition();
                int g;
                // If the char left of caret is whitespace, find the first non-space to the left;
                // otherwise find first whitespace/newline to the left (word boundary).
                boolean cursorInWhitespace = Character.isWhitespace(s.charAt(selection.getCursorPosition() - 1));
                if (cursorInWhitespace) {
                    // Scan left until non-whitespace (start of previous word)
                    for (g = selection.getCursorPosition() - 1; g >= 0; g--) {
                        char currentChar = s.charAt(g);
                        if (!Character.isWhitespace(currentChar)) {
                            nearestCondition = g;
                            break;
                        }
                        if (g == 0) {
                            nearestCondition = 0;
                        }
                    }
                } else {
                    // Scan left until whitespace/newline is found (word boundary)
                    for (g = selection.getCursorPosition() - 1; g >= 0; g--) {
                        char currentChar = s.charAt(g);
                        if (Character.isWhitespace(currentChar) || currentChar == '\n') {
                            nearestCondition = g;
                            break;
                        }
                        if (g == 0) {
                            nearestCondition = 0;
                        }
                    }
                }

                // Trim the prefix up to the discovered boundary
                s = s.substring(0, nearestCondition);
                // Adjust selection start to match removed characters
                selection.setStartSelection(
                        selection.getStartSelection() - (selection.getCursorPosition() - nearestCondition));
            }
            setText(s + getSelectionAfterText());
            selection.reset(selection.getStartSelection());
            return true;
        }

        // BACKSPACE: complex handling with a few cases:
        // 1) If a selection exists, delete it
        // 2) If at start, nothing to do
        // 3) Smart indent-aware merge with previous line when caret is at/near expected indent
        // 4) Auto-pair deletion (remove both opening and closing chars when deleting an opener)
        // 5) Fallback: delete a single char to the left
        if (i == Keyboard.KEY_BACK) {
            // 1) selection deletion
            if (selection.hasSelection()) {
                String s = getSelectionBeforeText();
                setText(s + getSelectionAfterText());
                selection.reset(selection.getStartSelection());
                scrollToCursor();
                return true;
            }

            // 2) nothing to delete
            if (selection.getStartSelection() <= 0) {
                return true;
            }

            // 3) indent-aware merge: find current line and compute expected indent
            LineData curr = selection.findCurrentLine(container.lines);
            if (curr != null && curr.start > 0) {
                int col = selection.getCursorPosition() - curr.start;
                int actualIndent = IndentHelper.getLineIndent(curr.text);
                int expectedIndent = IndentHelper.getExpectedIndent(curr, container.lines);

                // Trigger smart merge only when caret is at or before the expected indent.
                if (col <= expectedIndent) {
                    boolean lineHasContent = curr.text.trim().length() > 0;
                    int newlinePos = curr.start - 1;  // index of newline before this line

                    if (!lineHasContent) {
                        // Empty or whitespace-only line: remove it including its trailing newline
                        int removeEnd = text.indexOf('\n', curr.start);
                        if (removeEnd == -1) {
                            removeEnd = text.length();
                        } else {
                            removeEnd = removeEnd + 1;  // include the newline in removal
                        }
                        String before = text.substring(0, curr.start);
                        String after = removeEnd <= text.length() ? text.substring(removeEnd) : "";
                        setText(before + after);
                        // Place caret at end of previous line
                        int newCursor = Math.max(0, curr.start - 1);
                        selection.reset(newCursor);
                        scrollToCursor();
                        return true;
                    } else {
                        // Merge current line content with the previous line preserving spacing
                        int contentStart = curr.start + actualIndent;
                        String before = text.substring(0, newlinePos);
                        String content = contentStart <= text.length() ? text.substring(contentStart) : "";

                        // Decide whether a space is needed between concatenated fragments.
                        String spacer = "";
                        if (before.length() > 0 && content.length() > 0) {
                            char lastChar = before.charAt(before.length() - 1);
                            char firstChar = content.charAt(0);
                            // Avoid adding space when punctuation/brackets are adjacent
                            if (!Character.isWhitespace(lastChar) && 
                                lastChar != '{' && lastChar != '(' && lastChar != '[' &&
                                firstChar != '}' && firstChar != ')' && firstChar != ']' && 
                                firstChar != ';' && firstChar != ',' && firstChar != '.' &&
                                firstChar != '\n') {
                                spacer = " ";
                            }
                        }

                        setText(before + spacer + content);
                        int newCursor = before.length() + spacer.length();
                        selection.reset(newCursor);
                        scrollToCursor();
                        return true;
                    }
                }
            }

            // 4) Auto-pair deletion: when deleting an opener and a matching closer follows,
            // remove both so the pair is cleaned up in one backspace.
            if (selection.getStartSelection() > 0 && selection.getStartSelection() < text.length()) {
                char prev = text.charAt(selection.getStartSelection() - 1);
                char nextc = text.charAt(selection.getStartSelection());
                if ((prev == '(' && nextc == ')') || 
                    (prev == '[' && nextc == ']') || 
                    (prev == '{' && nextc == '}') || 
                    (prev == '\'' && nextc == '\'') || 
                    (prev == '"' && nextc == '"')) {
                    String before = text.substring(0, selection.getStartSelection() - 1);
                    String after = selection.getStartSelection() + 1 < text.length() ? text.substring(
                            selection.getStartSelection() + 1) : "";
                    setText(before + after);
                    selection.setStartSelection(selection.getStartSelection() - 1);
                    selection.reset(selection.getStartSelection());
                    scrollToCursor();
                    return true;
                }
            }

            // 5) Normal single-character backspace
            String s = getSelectionBeforeText();
            s = s.substring(0, s.length() - 1);
            selection.setStartSelection(selection.getStartSelection() - 1);
            setText(s + getSelectionAfterText());
            selection.reset(selection.getStartSelection());
            scrollToCursor();
            return true;
        }

        return false;
    }

    /**
     * Handles keyboard shortcuts: clipboard operations (cut/copy/paste),
     * undo/redo, tab indentation, code formatting, enter with brace handling,
     * comment toggling, and line duplication.
     */
    private boolean handleShortcutKeys(int i) {
        // CTRL+X: Cut
        if (this.isKeyComboCtrlX(i)) {
            if (selection.hasSelection()) {
                // Copy selected text into clipboard, then remove the selection
                NoppesStringUtils.setClipboardContents(selection.getSelectedText(text));
                String s = getSelectionBeforeText();
                setText(s + getSelectionAfterText());
                selection.reset(s.length());
                scrollToCursor();
            }
            return true;
        }

        // CTRL+C: Copy
        if (this.isKeyComboCtrlC(i)) {
            if (selection.hasSelection()) {
                NoppesStringUtils.setClipboardContents(selection.getSelectedText(text));
            }
            return true;
        }

        // CTRL+V: Paste (insert clipboard contents at caret)
        if (this.isKeyComboCtrlV(i)) {
            addText(NoppesStringUtils.getClipboardContents());
            scrollToCursor();
            return true;
        }

        // UNDO (Ctrl+Z): restore last entry from undoList and push current state to redoList
        if (i == Keyboard.KEY_Z && isCtrlKeyDown()) {
            if (undoList.isEmpty())
                return false; // nothing to undo
            undoing = true;
            redoList.add(new UndoData(this.text, selection.getCursorPosition()));
            UndoData data = undoList.remove(undoList.size() - 1);
            setText(data.text);
            selection.reset(data.cursorPosition);
            undoing = false;
            scrollToCursor();
            return true;
        }

        // REDO (Ctrl+Y): opposite of undo
        if (i == Keyboard.KEY_Y && isCtrlKeyDown()) {
            if (redoList.isEmpty())
                return false;
            undoing = true;
            undoList.add(new UndoData(this.text, selection.getCursorPosition()));
            UndoData data = redoList.remove(redoList.size() - 1);
            setText(data.text);
            selection.reset(data.cursorPosition);
            undoing = false;
            scrollToCursor();
            return true;
        }

        // TAB: indent or unindent depending on Shift
        if (i == Keyboard.KEY_TAB) {
            boolean shift = isShiftKeyDown();
            if (shift) {
                handleShiftTab();
            } else {
                handleTab();
            }
            scrollToCursor();
            return true;
        }

        // CTRL+F: format the text according to IndentHelper rules
        if (i == Keyboard.KEY_F && isCtrlKeyDown()) {
            formatText();
            return true;
        }

        // RETURN/ENTER: special handling when preceding char is an opening brace '{'
        if (i == Keyboard.KEY_RETURN) {
            if (selection.getCursorPosition() > 0 && selection.getCursorPosition() <= text.length() && text.charAt(
                    selection.getCursorPosition() - 1) == '{') {
                // Compute current and child indent strings
                String indent = getIndentCurrentLine();
                String childIndent = indent + "    ";
                String before = getSelectionBeforeText();
                String after = getSelectionAfterText();

                // If there's code after the brace on the same line, assume it should be
                // moved inside the new inner line: insert child indent only.
                int firstNewline = after.indexOf('\n');
                String leadingSegment = firstNewline == -1 ? after : after.substring(0, firstNewline);
                if (leadingSegment.trim().length() > 0) {
                    addText("\n" + childIndent);
                    scrollToCursor();
                    return true;
                }

                // Otherwise, determine whether a matching closing brace already exists at same indent.
                boolean hasMatchingCloseSameIndent = false;
                try {
                    int openLineIdx = -1;
                    int bracePos = selection.getCursorPosition() - 1;
                    for (int li = 0; li < this.container.lines.size(); li++) {
                        LineData ld = this.container.lines.get(li);
                        if (bracePos >= ld.start && bracePos < ld.end) {
                            openLineIdx = li;
                            break;
                        }
                    }

                    if (openLineIdx >= 0) {
                        List<int[]> spans = BracketMatcher.computeBraceSpans(text, this.container.lines);
                        for (int[] span : spans) {
                            int spanOpen = span[1];
                            int spanClose = span[2];
                            if (spanOpen == openLineIdx) {
                                // If the matching close has the same indent as current line,
                                // we only insert the child indent (do not auto-insert a closing brace).
                                int closeIndent = IndentHelper.getLineIndent(this.container.lines.get(spanClose).text);
                                if (closeIndent == indent.length()) {
                                    hasMatchingCloseSameIndent = true;
                                }
                                break;
                            }
                        }
                    }
                } catch (Exception ex) {
                    // On any error, default to conservative behavior (insert closing brace)
                    hasMatchingCloseSameIndent = false;
                }

                if (hasMatchingCloseSameIndent) {
                    addText("\n" + childIndent);
                    scrollToCursor();
                } else {
                    // Insert child line and a closing brace aligned with current indent
                    String insert = "\n" + childIndent + "\n" + indent + "}";
                    setText(before + insert + after);
                    int newCursor = before.length() + 1 + childIndent.length();
                    selection.reset(newCursor);
                    scrollToCursor();
                }
            } else {
                // Normal enter: insert newline + auto indent for caret position
                addText(Character.toString('\n') + getAutoIndentForEnter());
                scrollToCursor();
            }
            return true;
        }

        // CTRL+/ : toggle comment for selection or current line
        if (i == Keyboard.KEY_SLASH && isCtrlKeyDown()) {
            if (selection.hasSelection()) {
                toggleCommentSelection();
            } else {
                toggleCommentLineAtCursor();
            }
            return true;
        }

        // CTRL+D : duplicate selection or current line
        if (i == Keyboard.KEY_D && isCtrlKeyDown()) {
            if (selection.hasSelection()) {
                // Multi-line selection duplication: find first and last covered lines,
                // then insert the whole block after the last line without adding extra newline.
                LineData firstLine = null, lastLine = null;
                for (LineData line : container.lines) {
                    if (line.end > selection.getStartSelection() && line.start < selection.getEndSelection()) {
                        if (firstLine == null) firstLine = line;
                        lastLine = line;
                    }
                }
                if (firstLine != null && lastLine != null) {
                    String selectedText = text.substring(firstLine.start, lastLine.end);
                    String insertText = selectedText;
                    int savedStart = selection.getStartSelection();
                    int savedEnd = selection.getEndSelection();
                    int insertAt = lastLine.end;
                    setText(text.substring(0, insertAt) + insertText + text.substring(insertAt));
                    // Restore prior selection / cursor positions
                    selection.setStartSelection(savedStart);
                    selection.setEndSelection(savedEnd);
                    selection.setCursorPositionDirect(savedEnd);
                    return true;
                }
            } else {
                // Duplicate current line when nothing is selected
                for (LineData line : container.lines) {
                    if (selection.getCursorPosition() >= line.start && selection.getCursorPosition() <= line.end) {
                        int lineStart = line.start, lineEnd = line.end;
                        String lineText = text.substring(lineStart, lineEnd);
                        // If the line already ends with a newline, reuse it; otherwise
                        // prefix a newline so duplicate appears after current line.
                        String insertText;
                        if (lineText.endsWith("\n")) {
                            insertText = lineText;
                        } else {
                            insertText = "\n" + lineText;
                        }
                        int insertionPoint = lineEnd;
                        setText(text.substring(0, insertionPoint) + insertText + text.substring(insertionPoint));
                        int newCursor = insertionPoint + insertText.length() - (insertText.endsWith("\n") ? 1 : 0);
                        selection.reset(Math.max(0, Math.min(newCursor, this.text.length())));
                        return true;
                    }
                }
            }
            return true;
        }

        return false;
    }

    /**
     * Handles printable character input with auto-pairing for quotes and brackets,
     * and smart skipping over existing closing characters.
     */
    private boolean handleCharacterInput(char c) {
        if (ChatAllowedCharacters.isAllowedCharacter(c)) {
            String before = getSelectionBeforeText();
            String after = getSelectionAfterText();

            // If the user types a closing character and that same closer is
            // already immediately after the caret, move caret past it instead
            // of inserting another closer. This prevents duplicate closers
            // when the editor auto-inserts pairs.
            if ((c == ')' || c == ']' || c == '"' || c == '\'' ) && after.length() > 0 && after.charAt(0) == c) {
                // Move caret forward by one (skip over existing closer)
                selection.reset(before.length() + 1);
                scrollToCursor();
                return true;
            }

            // Auto-pair insertion: when opening a quote/brace/bracket is typed,
            // insert a matching closer and place caret between the pair.
            if (c == '"') {
                setText(before + "\"\"" + after);
                selection.reset(before.length() + 1);
                scrollToCursor();
                return true;
            }
            if (c == '\'') {
                setText(before + "''" + after);
                selection.reset(before.length() + 1);
                scrollToCursor();
                return true;
            }
            if (c == '[') {
                setText(before + "[]" + after);
                selection.reset(before.length() + 1);
                scrollToCursor();
                return true;
            }
            if (c == '(') {
                setText(before + "()" + after);
                selection.reset(before.length() + 1);
                scrollToCursor();
                return true;
            }

            // Default insertion for printable characters: insert at caret (replacing selection)
            addText(Character.toString(c));
            scrollToCursor();
            return true;
        }
        return false;
    }

    private boolean isShiftKeyDown() {
        return Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54);
    }

    // ==================== COMMENT TOGGLING ====================
    // Uses CommentHandler helper for comment operations
    
    private void toggleCommentSelection() {
        CommentHandler.SelectionToggleResult result = CommentHandler.toggleCommentSelection(
                text, container.lines, selection.getStartSelection(), selection.getEndSelection());
        setText(result.newText);
        selection.setStartSelection(result.newStartSelection);
        selection.setEndSelection(result.newEndSelection);
        selection.setCursorPositionDirect(result.newEndSelection);
    }

    private void toggleCommentLineAtCursor() {
        CommentHandler.SingleLineToggleResult result = CommentHandler.toggleCommentAtCursor(
                text, container.lines, selection.getCursorPosition());
        setText(result.newText);
        setCursor(result.newCursorPosition, false);
    }
    
    // ==================== KEYBOARD MODIFIERS ====================

    private boolean isAltKeyDown() {
        return Keyboard.isKeyDown(56) || Keyboard.isKeyDown(184);
    }

    private boolean isKeyComboCtrlX(int keyID) {
        return keyID == 45 && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
    }

    private boolean isKeyComboCtrlBackspace(int keyID) {
        return keyID == Keyboard.KEY_BACK && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
    }

    private boolean isKeyComboCtrlV(int keyID) {
        return keyID == 47 && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
    }

    private boolean isKeyComboCtrlC(int keyID) {
        return keyID == 46 && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
    }

    private boolean isKeyComboCtrlA(int keyID) {
        return keyID == 30 && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
    }

    private String getIndentCurrentLine() {
        for (LineData data : this.container.lines) {
            if (selection.getCursorPosition() > data.start && selection.getCursorPosition() <= data.end) {
                int i;
                for (i = 0; i < data.text.length() && data.text.charAt(i) == ' '; ++i) {
                }
                return data.text.substring(0, i);
            }
        }
        return "";
    }

    private String getAutoIndentForEnter() {
        LineData currentLine = selection.findCurrentLine(container.lines);

        if (currentLine == null) {
            return "";
        }
        return IndentHelper.getAutoIndentForEnter(currentLine.text, selection.getCursorPosition() - currentLine.start);
    }
    // ==================== TEXT FORMATTING ====================
    
    private int getTabSize() {
        return IndentHelper.TAB_SIZE;
    }

    private String repeatSpace(int count) {
        return IndentHelper.spaces(count);
    }

    private void formatText() {
        IndentHelper.FormatResult result = IndentHelper.formatText(text, selection.getCursorPosition());
        setText(result.text);
        selection.reset(Math.max(0, Math.min(result.cursorPosition, this.text.length())));
    }
    
    // ==================== TAB HANDLING ====================

    private void handleTab() {
        LineData currentLine = selection.findCurrentLine(container.lines);
        if (currentLine == null) {
            addText("    ");
            return;
        }
        int tab = getTabSize();
        int indentLen = IndentHelper.getLineIndent(currentLine.text);
        int textStartPos = currentLine.start + indentLen;

        if (selection.getCursorPosition() <= textStartPos) {
            // Cursor before any text: if cursor is exactly at text start, move forward to next tab stop.
            // If cursor is inside leading whitespace (before text start), choose nearest tab stop (tie -> forward).
            int targetIndent;
            if (selection.getCursorPosition() == textStartPos) {
                targetIndent = ((indentLen / tab) + 1) * tab;
            } else {
                int remainder = indentLen % tab;
                if (remainder == 0) {
                    targetIndent = indentLen + tab; // already aligned -> next level
                } else {
                    int down = indentLen - remainder;
                    int up = indentLen + (tab - remainder);
                    int distDown = remainder;
                    int distUp = tab - remainder;
                    if (distUp < distDown) targetIndent = up;
                    else if (distUp > distDown) targetIndent = down;
                    else targetIndent = up; // tie -> forward
                }
            }
            if (targetIndent < 0) targetIndent = 0;
            String newIndent = repeatSpace(targetIndent);
            String rest = currentLine.text.substring(indentLen);
            String before = text.substring(0, currentLine.start);
            int contentEnd = Math.min(currentLine.start + currentLine.text.length(), text.length());
            int sepEnd = Math.min(currentLine.end, text.length());
            String sep = contentEnd < sepEnd ? text.substring(contentEnd, sepEnd) : "";
            String after = text.substring(sepEnd);
            setText(before + newIndent + rest + sep + after);
            int newCursor = currentLine.start + targetIndent;
            selection.reset(Math.min(newCursor, this.text.length()));
        } else {
            // Cursor is after start of text: insert spaces at cursor to move following text to next tab stop
            int column = selection.getCursorPosition() - currentLine.start;
            int targetColumn = ((column / tab) + 1) * tab;
            int toInsert = Math.max(0, targetColumn - column);
            if (toInsert > 0) {
                String spaces = repeatSpace(toInsert);
                addText(spaces);
            }
        }
    }

    private void handleShiftTab() {
        LineData currentLine = selection.findCurrentLine(container.lines);
        if (currentLine == null)
            return;
        int tab = getTabSize();
        int indentLen = IndentHelper.getLineIndent(currentLine.text);
        int textStartPos = currentLine.start + indentLen;

        if (selection.getCursorPosition() <= textStartPos) {
            // Cursor before any text: reduce leading indent to previous tab stop
            int targetIndent = Math.max(0, ((indentLen - 1) / tab) * tab);
            String newIndent = repeatSpace(targetIndent);
            String rest = currentLine.text.substring(indentLen);
            String before = text.substring(0, currentLine.start);
            int contentEnd = Math.min(currentLine.start + currentLine.text.length(), text.length());
            int sepEnd = Math.min(currentLine.end, text.length());
            String sep = contentEnd < sepEnd ? text.substring(contentEnd, sepEnd) : "";
            String after = text.substring(sepEnd);
            setText(before + newIndent + rest + sep + after);
            int newCursor = currentLine.start + targetIndent;
            selection.reset(Math.min(newCursor, this.text.length()));
        } else {
            // Cursor after start of text: remove up to previous tab stop worth of spaces immediately before cursor
            int column = selection.getCursorPosition() - currentLine.start;
            int mod = column % tab;
            int toRemove = mod == 0 ? tab : mod;
            int removed = 0;
            int pos = selection.getCursorPosition() - 1;
            while (pos >= currentLine.start && removed < toRemove && text.charAt(pos) == ' ') {
                pos--;
                removed++;
            }
            if (removed > 0) {
                int removeStart = pos + 1;
                String before = text.substring(0, removeStart);
                String after = text.substring(selection.getCursorPosition());
                setText(before + after);
                int newCursor = removeStart;
                selection.reset(Math.min(newCursor, this.text.length()));
            }
        }
    }
    
    // ==================== CURSOR MANAGEMENT ====================

    private void setCursor(int i, boolean select) {
        selection.setCursor(i, text != null ? text.length() : 0, select);
    }

    private void addText(String s) {
        int insertPos = selection.getStartSelection();
        this.setText(this.getSelectionBeforeText() + s + this.getSelectionAfterText());
        selection.afterTextInsert(insertPos + s.length());
    }

    private int cursorUp() {
        return CursorNavigation.cursorUp(selection.getCursorPosition(), container.lines, text);
    }

    private int cursorDown() {
        return CursorNavigation.cursorDown(selection.getCursorPosition(), container.lines, text);
    }

    public String getSelectionBeforeText() {
        return selection.getTextBefore(text);
    }

    public String getSelectionAfterText() {
        return selection.getTextAfter(text);
    }
    
    // ==================== MOUSE HANDLING ====================

    public void mouseClicked(int xMouse, int yMouse, int mouseButton) {
        // Determine whether click occurred inside the text area bounds
        this.active = xMouse >= this.x && xMouse < this.x + this.width && yMouse >= this.y && yMouse < this.y + this.height;
        if (this.active) {
            // Compute logical click position in text and reset selection/caret
            int clickPos = this.getSelectionPos(xMouse, yMouse);
            selection.reset(clickPos);
            selection.markActivity();

            // Prepare click state (left button starts most interactions)
            this.clicked = mouseButton == 0;
            this.doubleClicked = false;
            this.tripleClicked = false;
            long time = System.currentTimeMillis();

            // Prefer delegating scrollbar-start logic to ScrollState. If the click
            // is on the scrollbar area and the scrollbar can be dragged, we start
            // click-scrolling mode and cancel the normal text click-drag behavior.
            if (this.clicked && this.container.linesCount * this.container.lineHeight > this.height && xMouse > this.x + this.width - 8) {
                // We consumed the mouse-down as a scrollbar drag start
                this.clicked = false;
                scroll.startScrollbarDrag(yMouse,this.y,this.height,container.linesCount);
            } else {
                // Handle double/triple click selection counting
                if (time - this.lastClicked < 300L) {
                    this.clickCount++;
                } else {
                    this.clickCount = 1;
                }

                if (this.clickCount == 2) {
                    // Double-click: select the word under the caret using the container's word regex
                    this.doubleClicked = true;
                    selection.selectWordAtCursor(this.text, this.container.regexWord);
                } else if (this.clickCount >= 3) {
                    // Triple-click: select the entire logical line that contains the caret
                    this.tripleClicked = true;
                    selection.selectLineAtCursor(container.lines);
                    this.clickCount = 0;
                }
            }

            this.lastClicked = time;
            activeTextfield = this;
        }
    }

    public void updateCursorCounter() {
        ++this.cursorCounter;
    }
    
    // ==================== TEXT MANAGEMENT ====================

    public void setText(String text) {
        if (text == null) {
            return;
        }

        text = text.replace("\r", "");
        text = text.replace("\t", "    ");
        if (this.text == null || !this.text.equals(text)) {
            if (this.listener != null) {
                this.listener.textUpdate(text);
            }

            if (!this.undoing) {
                this.undoList.add(new GuiScriptTextArea.UndoData(this.text, selection.getCursorPosition()));
                this.redoList.clear();
            }

            this.text = text;
            //this.container = new TextContainer(text);
            this.container = new JavaTextContainer(text);
            this.container.init(this.width, this.height);
            if (this.enableCodeHighlighting) {
                this.container.formatCodeText();
            }

            // Ensure scroll state stays in bounds after text change
            int maxScroll = Math.max(0, this.container.linesCount - this.container.visibleLines);
            scroll.clampToBounds(maxScroll);

            selection.clamp(this.text.length());

            // Consider text changes user activity to pause caret blinking briefly
            selection.markActivity();

        }
    }

    public String getText() {
        return this.text;
    }

    public boolean isEnabled() {
        return this.enabled && this.visible;
    }

    public boolean hasVerticalScrollbar() {
        return this.container.visibleLines < this.container.linesCount;
    }

    public void enableCodeHighlighting() {
        this.enableCodeHighlighting = true;
        this.container.formatCodeText();
    }

    public void setListener(ITextChangeListener listener) {
        this.listener = listener;
    }

    private void clampSelectionBounds() {
        selection.clamp(text != null ? text.length() : 0);
    }
    
    // ==================== INNER CLASSES ====================

    class UndoData {
        public String text;
        public int cursorPosition;

        public UndoData(String text, int cursorPosition) {
            this.text = text;
            this.cursorPosition = cursorPosition;
        }
    }
}
