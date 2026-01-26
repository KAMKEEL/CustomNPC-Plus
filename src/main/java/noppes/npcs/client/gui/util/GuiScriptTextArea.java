package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ChatAllowedCharacters;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.gui.script.GuiScriptInterface;
import noppes.npcs.client.gui.util.key.OverlayKeyPresetViewer;
import noppes.npcs.client.gui.util.script.*;
import noppes.npcs.client.gui.util.script.JavaTextContainer.LineData;
// New interpreter system imports
import noppes.npcs.client.gui.util.script.autocomplete.AutocompleteMenu;
import noppes.npcs.client.gui.util.script.interpreter.ScriptLine;
import noppes.npcs.client.gui.util.script.interpreter.ScriptTextContainer;
import noppes.npcs.client.gui.util.script.interpreter.field.FieldInfo;
import noppes.npcs.client.gui.util.script.interpreter.field.FieldAccessInfo;
import noppes.npcs.client.gui.util.script.interpreter.method.MethodCallInfo;
import noppes.npcs.client.gui.util.script.interpreter.method.MethodInfo;
import noppes.npcs.client.gui.util.script.interpreter.token.Token;
import noppes.npcs.client.gui.util.script.interpreter.token.TokenType;
import noppes.npcs.client.gui.util.script.interpreter.hover.GutterIconRenderer;
import noppes.npcs.client.gui.util.script.interpreter.hover.HoverState;
import noppes.npcs.client.gui.util.script.interpreter.hover.TokenHoverRenderer;
import noppes.npcs.client.gui.util.script.autocomplete.AutocompleteManager;
import noppes.npcs.client.gui.util.script.interpreter.type.ScriptTypeInfo;
import noppes.npcs.client.key.impl.ScriptEditorKeys;
import noppes.npcs.constants.ScriptContext;
import noppes.npcs.util.ValueUtil;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
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

    private GuiScriptInterface parent;
    
    // ==================== DIMENSIONS & POSITION ====================
    public int x;
    public int y;
    
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
    public String highlightedWord;
    private ScriptTextContainer container = null;
    private boolean enableCodeHighlighting = false;
    // Extra empty lines to allow padding at the bottom of the editor viewport
    private int bottomPaddingLines = 6;

    // Search bar layout tracking to allow idempotent/resilient resizing
    private int searchBaseY = 0;
    private int searchBaseHeight = 0;
    private int searchAppliedOffset = 0;
    private boolean searchBaseInitialized = false;

    private int getPaddedLineCount() {
        if (container == null) return 0;
        // Only add bottom padding when the content is already scrollable. This avoids
        // introducing a scrollbar when there's nothing to scroll for.
        if (container.linesCount > container.visibleLines - bottomPaddingLines) {
            return Math.max(0, container.linesCount + bottomPaddingLines);
        } else {
            return container.linesCount;
        }
    }

    // ==================== HELPER CLASS INSTANCES ====================
    private final ScrollState scroll = new ScrollState();
    private final SelectionState selection = new SelectionState();
    private final HoverState hoverState = new HoverState();
    /** When true, clicking a token will pin its hover tooltip until dismissed. */
    public boolean clickToPinEnabled = false;
    
    // ==================== UI COMPONENTS ====================
    private int cursorCounter;
    private ITextChangeListener listener;
    private static int LINE_NUMBER_GUTTER_WIDTH = 25;
    
    // ==================== GUTTER ICONS ====================
    /** Hover state for gutter icons - tracks which icon the mouse is over */
    private MethodInfo hoveredGutterMethod = null;
    
    // ==================== UNDO/REDO ====================
    public List<UndoData> undoList = new ArrayList<>();
    public List<UndoData> redoList = new ArrayList<>();
    public boolean undoing = false;
    
    // Atomic undo: group typing into word-based undo steps
    private long lastTypingTime = 0;
    private int lastTypingPos = -1;

    // Clipboard tracking for line-copy paste behavior
    private boolean lastCopyWasLine = false;
    private String lastCopiedLineText = null;

    // ==================== KEYS ====================
    public static final ScriptEditorKeys KEYS = new ScriptEditorKeys();
    public OverlayKeyPresetViewer KEYS_OVERLAY = new OverlayKeyPresetViewer(KEYS);

    // ==================== SEARCH/REPLACE ====================
    public static final SearchReplaceBar searchBar = new SearchReplaceBar();
    
    // ==================== GO TO LINE ====================
    private final GoToLineDialog goToLineDialog = new GoToLineDialog();

    // ==================== RENAME REFACTOR ====================
    private final RenameRefactorHandler renameHandler = new RenameRefactorHandler();

    // ==================== AUTOCOMPLETE ====================
    private final AutocompleteManager autocompleteManager = new AutocompleteManager();

    // ==================== CONSTRUCTOR ====================

    public GuiScriptTextArea(GuiScreen guiScreen, int id, int x, int y, int width, int height, String text) {
        super(id, guiScreen, x, y, width, height, null);
        init(x, y, width, height, text);

        if (guiScreen instanceof GuiScriptInterface)
            this.parent = (GuiScriptInterface) guiScreen;
    }

    public void init(int x, int y, int width, int height, String text) {
        this.x = xPosition = x;
        this.y = yPosition = y;
        this.width = width;
        this.height = height;
        this.undoing = true;
        this.setText(text);
        this.undoing = false;
        setCallbacks();
        // Reset search-base tracking whenever the editor is (re)initialized
        this.searchBaseY = 0;
        this.searchBaseHeight = 0;
        this.searchAppliedOffset = 0;
        this.searchBaseInitialized = false;
        
        KEYS_OVERLAY.openOnClick = true;
        initGui();
        initializeKeyBindings();
        // Propagate click-to-pin option into hover state
        hoverState.setClickToPinEnabled(clickToPinEnabled);
    }
    public void initGui() {
        int endX = x + width, endY = y + height;
        int xOffset = hasVerticalScrollbar() ? -8 : -2;
        KEYS_OVERLAY.scale = 0.75f;
        KEYS_OVERLAY.borderCol1 = KEYS_OVERLAY.borderCol2 = 0xFF3c3c3c;
        int overlayWidth = 160;
        KEYS_OVERLAY.initGui(x + (width - overlayWidth) / 2 + 5, y + height / 10, overlayWidth,
                height - height / 5 - 10);

        KEYS_OVERLAY.viewButton.scale = 0.45f;
        KEYS_OVERLAY.viewButton.initGui(endX + xOffset, endY - 26);
        
        // Dismiss autocomplete on resize to avoid positioning issues
        if (autocompleteManager != null) {
            autocompleteManager.dismiss();
        }
        
        // Initialize search bar (preserves state across initGui calls)
        searchBar.initGui(x, y, width);
        if (searchBar.isVisible()) { // If open
            // Shift viewport down again using the bar's current height
            searchBar.callback.resizeEditor(true, searchBar.getTotalHeight());
            if (!active) // Focus search if opening another script tab & bar is open
                searchBar.focus(false);
        }
        
        // Initialize Go To Line dialog
        goToLineDialog.initGui(x, y, width);
    }

    public void setCallbacks() {
        searchBar.setCallback(new SearchReplaceBar.SearchCallback() {
            @Override
            public String getText() {
                return GuiScriptTextArea.this.text;
            }

            public String getHighlightedWord() {
                return GuiScriptTextArea.this.highlightedWord;
            }

            @Override
            public int getSelectionStart() {
                return GuiScriptTextArea.this.selection.getStartSelection();
            }

            @Override
            public int getSelectionEnd() {
                return GuiScriptTextArea.this.selection.getEndSelection();
            }

            @Override
            public void setText(String newText) {
                GuiScriptTextArea.this.setText(newText);
            }

            @Override
            public void scrollToPosition(int position) {
                // Find line containing position and scroll to it
                if (container == null || container.lines == null)
                    return;

                // Calculate offset to account for search bar height
                int searchBarOffset = searchBar.getTotalHeight();
                int effectiveHeight = GuiScriptTextArea.this.height - searchBarOffset;
                int visibleLines = effectiveHeight / container.lineHeight;
                // Calculate how many lines the search bar covers
                int linesHiddenBySRB = searchBarOffset > 0 ? (int) Math.ceil(
                        (double) searchBarOffset / container.lineHeight) : 0;

                for (int i = 0; i < container.lines.size(); i++) {
                    LineData ld = container.lines.get(i);
                    if (position >= ld.start && position < ld.end) {
                        int visible = Math.max(1, visibleLines);
                        int effectiveVisible = Math.max(1, visible - bottomPaddingLines);
                        int maxScroll = Math.max(0, getPaddedLineCount() - visible);
                        int targetLine = i;

                        // If search bar is visible and would hide this line, scroll down so it's visible
                        // The target line should appear below the search bar, not under it
                        int currentScroll = scroll.getScrolledLine();
                        int firstVisibleLine = currentScroll + linesHiddenBySRB;

                        if (searchBarOffset > 0 && targetLine < firstVisibleLine) {
                            // Force scroll so target line appears just below the search bar
                            scroll.setTargetScroll(Math.max(0, targetLine - linesHiddenBySRB), maxScroll);
                        } else {
                            scroll.scrollToLine(targetLine, effectiveVisible, maxScroll);
                        }
                        break;
                    }
                }
            }

            @Override
            public void setSelection(int start, int end) {
                selection.setSelection(start, end);
                selection.setCursorPositionDirect(end);
            }

            @Override
            public int getGutterWidth() {
                return LINE_NUMBER_GUTTER_WIDTH;
            }

            @Override
            public void unfocusMainEditor() {
                // Save position but unfocus
                active = false;
            }

            @Override
            public void focusMainEditor() {
                active = true;
                searchBar.resetSelection();
            }

            @Override
            public void onMatchesUpdated() {
                // Called when matches change - could be used for UI updates
            }

            // Robust resize: use base editor bounds and apply the requested offset
            public void resizeEditor(boolean open, int barHeight) {
                int desiredOffset = Math.max(0, barHeight);
                // Initialize base values if not set
                if (!searchBaseInitialized) {
                    searchBaseY = GuiScriptTextArea.this.y;
                    searchBaseHeight = GuiScriptTextArea.this.height;
                    searchAppliedOffset = 0;
                    searchBaseInitialized = true;
                }

                int targetOffset = open ? desiredOffset : 0;
                if (targetOffset == searchAppliedOffset)
                    return; // already in desired state

                // Compute new bounds from base values (idempotent)
                int newY = searchBaseY + targetOffset;
                int newHeight = Math.max(12, searchBaseHeight - targetOffset);

                GuiScriptTextArea.this.y = newY;
                GuiScriptTextArea.this.height = newHeight;
                searchAppliedOffset = targetOffset;

                if (container != null)
                    container.visibleLines = Math.max(GuiScriptTextArea.this.height / container.lineHeight - 1, 1);
            }
        });
        // Initialize Go To Line dialog with callback
        goToLineDialog.setCallback(new GoToLineDialog.GoToLineCallback() {
            @Override
            public int getLineCount() {
                return container != null ? container.linesCount : 0;
            }

            @Override
            public int getColumnCount(int lineIndex) {
                if (container == null || container.lines == null || lineIndex < 0 || lineIndex >= container.lines.size()) {
                    return 0;
                }
                LineData ld = container.lines.get(lineIndex);
                return ld.end - ld.start;
            }

            @Override
            public void goToLineColumn(int line, int column) {
                if (container == null || container.lines == null)
                    return;

                // Convert 1-indexed line to 0-indexed
                int lineIdx = line - 1;
                if (lineIdx < 0 || lineIdx >= container.lines.size())
                    return;

                LineData ld = container.lines.get(lineIdx);
                int lineLength = ld.end - ld.start;

                // Convert 1-indexed column to 0-indexed, clamp to line length
                // lineLength - 1 because the line's end is the start of the next line
                int col = Math.max(0, Math.min(column - 1, lineLength - 1));
                int position = ld.start + col;

                // Set cursor position
                selection.reset(position);

                // Scroll to make the line visible
                int visible = GuiScriptTextArea.this.height / (container != null ? container.lineHeight : 12);
                int effectiveVisible = Math.max(1, visible - bottomPaddingLines);
                int maxScroll = Math.max(0, getPaddedLineCount() - visible);
                scroll.scrollToLine(lineIdx, effectiveVisible, maxScroll);
            }

            @Override
            public void unfocusMainEditor() {
                // Save position but unfocus
                active = false;
            }

            @Override
            public void focusMainEditor() {
                active = true;
                selection.markActivity();
            }

            @Override
            public void onDialogClose() {
                active = true;
                selection.markActivity();
            }
        });

        // Initialize Rename Refactor handler with callback
        renameHandler.setCallback(new RenameRefactorHandler.RenameCallback() {
            @Override
            public String getText() {
                return GuiScriptTextArea.this.text;
            }

            @Override
            public void setText(String newText) {
                GuiScriptTextArea.this.setText(newText);
            }

            @Override
            public List<LineData> getLines() {
                return container != null ? container.lines : new ArrayList<>();
            }

            @Override
            public int getCursorPosition() {
                return selection.getCursorPosition();
            }

            public SelectionState getSelectionState() {
                return selection;
            }

            @Override
            public void setCursorPosition(int pos) {
                selection.reset(pos);
            }

            @Override
            public void unfocusMainEditor() {
                active = false;
            }

            @Override
            public void focusMainEditor() {
                active = true;
                selection.markActivity();
            }

            @Override
            public int getGutterWidth() {
                return LINE_NUMBER_GUTTER_WIDTH;
            }

            @Override
            public int getLineHeight() {
                return container != null ? container.lineHeight : 12;
            }

            @Override
            public int getScrolledLine() {
                return scroll.getScrolledLine();
            }

            @Override
            public double getFractionalOffset() {
                return scroll.getFractionalOffset();
            }

            @Override
            public void scrollToPosition(int pos) {
                if (container == null || container.lines == null)
                    return;
                for (int i = 0; i < container.lines.size(); i++) {
                    LineData ld = container.lines.get(i);
                    if (pos >= ld.start && pos < ld.end) {
                        int visible = Math.max(1, container.visibleLines);
                        int effectiveVisible = Math.max(1, visible - bottomPaddingLines);
                        int maxScroll = Math.max(0, getPaddedLineCount() - visible);
                        scroll.scrollToLine(i, effectiveVisible, maxScroll);
                        break;
                    }
                }
            }

            @Override
            public ScriptTextContainer getContainer() {
                return container;
            }

            @Override
            public void setTextWithoutUndo(String newText) {
                // Set text without creating an undo entry (for live rename preview)
                boolean wasUndoing = undoing;
                undoing = true;
                setText(newText);
                undoing = wasUndoing;
            }

            @Override
            public void pushUndoState(String textState, int cursor) {
                // Push a specific text state to the undo list
                if (!undoing) {
                    undoList.add(new UndoData(textState, cursor));
                    redoList.clear();
                }
            }

            @Override
            public int getViewportWidth() {
                return width - LINE_NUMBER_GUTTER_WIDTH - 8; // Account for gutter and scrollbar
            }
        });
        
        // Initialize Autocomplete Manager with callback
        autocompleteManager.setInsertCallback(new AutocompleteManager.InsertCallback() {
            @Override
            public void insertText(String text, int startPosition) {
                // Replace text from startPosition to current cursor
                String fullText = GuiScriptTextArea.this.text;
                int cursorPos = selection.getCursorPosition();
                
                // Bounds check to prevent StringIndexOutOfBoundsException
                int start = Math.max(0, Math.min(startPosition, fullText.length()));
                int cursor = Math.max(start, Math.min(cursorPos, fullText.length()));
                
                String before = fullText.substring(0, start);
                String after = fullText.substring(cursor);
                setText(before + text + after);
                selection.reset(start + text.length());
                scrollToCursor();
            }
            
            @Override
            public void replaceTextRange(String text, int startPosition, int endPosition) {
                // Replace text from startPosition to endPosition
                String fullText = GuiScriptTextArea.this.text;
                
                // Bounds check to prevent StringIndexOutOfBoundsException
                int start = Math.max(0, Math.min(startPosition, fullText.length()));
                int end = Math.max(start, Math.min(endPosition, fullText.length()));
                
                String before = fullText.substring(0, start);
                String after = fullText.substring(end);
                setText(before + text + after);
                selection.reset(start + text.length());
                scrollToCursor();
            }
            
            @Override
            public void addImport(String importPath) {
                // Add import statement and sort all imports
                addAndSortImport(importPath);
            }
            
            @Override
            public int getCursorPosition() {
                return selection.getCursorPosition();
            }
            
            @Override
            public void setCursorPosition(int position) {
                selection.reset(Math.max(0, Math.min(position, GuiScriptTextArea.this.text.length())));
                scrollToCursor();
            }
            
            @Override
            public String getText() {
                return GuiScriptTextArea.this.text;
            }
            
            @Override
            public int[] getCursorScreenPosition() {
                // Calculate screen position of cursor for menu placement
                int cursorLine = getCursorLineIndex();
                int cursorCol = 0;
                if (container != null && container.lines != null && cursorLine < container.lines.size()) {
                    LineData ld = container.lines.get(cursorLine);
                    String lineText = ld.text;
                    int cursorOffset = selection.getCursorPosition() - ld.start;
                    cursorCol = ClientProxy.Font.width(lineText.substring(0, Math.min(cursorOffset, lineText.length())));
                }
                
                int screenX = GuiScriptTextArea.this.x + LINE_NUMBER_GUTTER_WIDTH + 1 + cursorCol;
                int lineY = cursorLine - scroll.getScrolledLine();
                int screenY = GuiScriptTextArea.this.y + lineY * (container != null ? container.lineHeight : 12);
                
                return new int[] { screenX, screenY };
            }
            
            @Override
            public int[] getViewportDimensions() {
                Minecraft mc = Minecraft.getMinecraft();
                ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
                return new int[] { sr.getScaledWidth(), sr.getScaledHeight() };
            }
        });
    }

    public boolean fullscreen() {
        return GuiScriptInterface.isFullscreen;
    }
    
    // ==================== RENDERING ====================
    public void drawTextBox(int xMouse, int yMouse) {
        if (!visible)
            return;
        clampSelectionBounds();
        
        // Dynamically calculate gutter width based on line count digits + icon space
        if (container != null && container.linesCount > 0) {
            int maxLineNum = container.linesCount;
            String maxLineStr = String.valueOf(maxLineNum);
            int digitWidth = ClientProxy.Font.width(maxLineStr);
            LINE_NUMBER_GUTTER_WIDTH = digitWidth + 10 + GutterIconRenderer.ICON_GUTTER_WIDTH; // 10px padding + icon space
        }
        // Draw outer border around entire area
        int offset = fullscreen() ? 2 : 1;
        drawRect(x - offset, y - offset - searchBar.getTotalHeight(), x + width + offset, y + height + offset,
                0xffa0a0a0);

        int searchHeight = searchBar.getTotalHeight();


        // Draw line number gutter background
        int viewportX = x + LINE_NUMBER_GUTTER_WIDTH;
        drawRect(x, y, x + LINE_NUMBER_GUTTER_WIDTH, y + height, 0xff000000);
        // Draw text viewport background (starts after gutter)
        drawRect(x + LINE_NUMBER_GUTTER_WIDTH, y, x + width, y + height, 0xff000000);
        // Draw separator line between gutter and text area
        drawRect(x + LINE_NUMBER_GUTTER_WIDTH-1, y, x + LINE_NUMBER_GUTTER_WIDTH, y + height, 0xff3c3f41);

        // Enable scissor test to clip drawing to the TEXT viewport rectangle (excludes gutter)
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        scissorViewport();

        container.visibleLines = (height / container.lineHeight);

        int maxScroll = Math.max(0, getPaddedLineCount() - container.visibleLines);

        // Handle mouse wheel scroll
        int wheelDelta = ((GuiNPCInterface) listener).mouseScroll = Mouse.getDWheel();
        if (listener instanceof GuiNPCInterface) {
            ((GuiNPCInterface) listener).mouseScroll = wheelDelta;

            // Let autocomplete menu consume scroll first if visible
            if (wheelDelta != 0 && autocompleteManager.isVisible() && autocompleteManager.mouseScrolled(xMouse, yMouse, wheelDelta)) {
                // Autocomplete consumed the scroll
            } else {
                boolean canScroll = !KEYS_OVERLAY.isVisible() || KEYS_OVERLAY.isVisible() && !KEYS_OVERLAY.aboveOverlay;
                if (wheelDelta != 0 && canScroll)
                    scroll.applyWheelScroll(wheelDelta, maxScroll);
            }
        }

        // Handle scrollbar dragging (delegated to ScrollState)
        if (scroll.isClickScrolling())
            scroll.handleClickScrolling(yMouse, x, y, height, container.visibleLines, getPaddedLineCount(), maxScroll);
        
        // Update scroll animation
        scroll.initializeIfNeeded(scroll.getScrolledLine());
        scroll.update(maxScroll);

        // Update hover state for token tooltips
        updateHoverState(xMouse, yMouse);

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
        // y += searchHeight;
        // height -= searchHeight;
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
        // Always highlight unmatched braces (positions in text)
        List<Integer> unmatchedBraces = BracketMatcher.findUnmatchedBracePositions(text);
        
        // Determine which exact brace span (openLine/closeLine) to highlight indent guides for
        int highlightedOpenLine = -1;
        int highlightedCloseLine = -1;
        if (startBracket != endBracket && startBracket >= 0) {
            int bracketLineIdx = -1;
            // Only consider curly braces for highlighting the indent guides
            boolean isCurlyBracket = false;
            char bc = text.charAt(startBracket);
            if (text != null && startBracket >= 0 && startBracket < text.length()) {
                if (bc == '{' || bc == '}') isCurlyBracket = true;
            }
            for (int li = 0; li < list.size(); li++) {
                LineData ld = list.get(li);
                if (startBracket >= ld.start && startBracket < ld.end) {
                    bracketLineIdx = li;
                    break;
                }
            }
            if (bracketLineIdx >= 0 && isCurlyBracket) {
                // Prefer a span that directly matches the bracket character's line:
                // - if the bracket is an opening '{', prefer spans where openLine == bracketLineIdx
                // - if the bracket is a closing '}', prefer spans where closeLine == bracketLineIdx
                // If no exact match is found, fall back to the smallest enclosing span (innermost).
                int bestSize = Integer.MAX_VALUE;
                boolean foundExact = false;
                char bracketChar = bc; // from earlier
                for (int[] span : braceSpans) {
                    int openLine = span[1];
                    int closeLine = span[2];
                    if (bracketLineIdx >= openLine && bracketLineIdx <= closeLine) {
                        int size = closeLine - openLine;
                        boolean exactMatch = (bracketChar == '{' && openLine == bracketLineIdx) || (bracketChar == '}' && closeLine == bracketLineIdx);
                        if (exactMatch) {
                            // Prefer exact matches immediately (still choose smallest exact span)
                            if (!foundExact || size < bestSize) {
                                foundExact = true;
                                bestSize = size;
                                highlightedOpenLine = openLine;
                                highlightedCloseLine = closeLine;
                            }
                        } else if (!foundExact) {
                            // keep the smallest enclosing span as a fallback
                            if (size < bestSize) {
                                bestSize = size;
                                highlightedOpenLine = openLine;
                                highlightedCloseLine = closeLine;
                            }
                        }
                    }
                }
            }
        }

        highlightedWord = null;
        if (selection.hasSelection()) {
            Matcher m = container.regexWord.matcher(text);
            while (m.find()) {
                if (m.start() == selection.getStartSelection() && m.end() == selection.getEndSelection()) {
                    highlightedWord = text.substring(selection.getStartSelection(), selection.getEndSelection());
                }
            }
        }

        // Apply fractional GL translate for sub-pixel smooth scrolling
        double fracOffset = scroll.getFractionalOffset();
        float fracPixels = (float) (fracOffset * container.lineHeight);
        GL11.glPushMatrix();
        GL11.glTranslatef(0.0f, -fracPixels, 0.0f);
        
        
        // Expand render range by one line above/below so partially-visible lines are drawn
        int renderStart = Math.max(0, scroll.getScrolledLine() - 1);
        // Compute the last line index to render, including the last partially-visible line if any.
        // Adds the fractional scroll offset (fracOffset * lineHeight) to ensure the bottom line is drawn
        // when only part of it is visible in the viewport.
        int renderEnd = (int) Math.min(list.size() - 1,
                scroll.getScrolledLine() + container.visibleLines + fracPixels + 1);

        // Strings start drawing vertically this much into the line.
        int stringYOffset = 2;
        
        // Render LINE GUTTER numbers
        for (int i = renderStart; i <= renderEnd; i++) {
            int posY = y + (i - scroll.getScrolledLine()) * container.lineHeight + stringYOffset;
            String lineNum = String.valueOf(i + 1);
            int lineNumWidth = ClientProxy.Font.width(lineNum);
            int lineNumX = x + LINE_NUMBER_GUTTER_WIDTH - lineNumWidth - 5 - GutterIconRenderer.ICON_GUTTER_WIDTH; // right-align before icon space
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
            ScriptLine scriptLine = container.getDocument().getLine(i);
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

                // Highlight unmatched braces in this line (always red)
                if (unmatchedBraces != null && !unmatchedBraces.isEmpty()) {
                    for (int ubPos : unmatchedBraces) {
                        if (ubPos >= data.start && ubPos < data.end) {
                            int rel = ubPos - data.start;
                            int s = ClientProxy.Font.width(line.substring(0, rel));
                            int e = ClientProxy.Font.width(line.substring(0, rel + 1)) + 1;
                            drawRect(x + LINE_NUMBER_GUTTER_WIDTH + 1 + s, posY, x + LINE_NUMBER_GUTTER_WIDTH + 1 + e, posY + container.lineHeight, 0xffcc0000);
                        }
                    }
                }
                //Highlight words
                if (highlightedWord != null) {
                    Matcher m = container.regexWord.matcher(line);
                    while (m.find()) {
                        if (line.substring(m.start(), m.end()).equals(highlightedWord)) {
                            int s = ClientProxy.Font.width(line.substring(0, m.start()));
                            int e = ClientProxy.Font.width(line.substring(0, m.end())) + 1;
                            drawRect(x + LINE_NUMBER_GUTTER_WIDTH + 1 + s, posY, x + LINE_NUMBER_GUTTER_WIDTH + 1 + e, posY + container.lineHeight, 0x99004c00);
                        }
                    }
                }
                
                // Highlight search matches
                if (searchBar.isVisible()) {
                    List<int[]> searchMatches = searchBar.getMatches();
                    int currentMatchIdx = searchBar.getCurrentMatchIndex();
                    for (int mi = 0; mi < searchMatches.size(); mi++) {
                        int[] match = searchMatches.get(mi);
                        // Check if match overlaps with this line
                        if (match[1] > data.start && match[0] < data.end) {
                            int matchStart = Math.max(match[0] - data.start, 0);
                            int matchEnd = Math.min(match[1] - data.start, line.length());
                            if (matchStart < matchEnd) {
                                int s = ClientProxy.Font.width(line.substring(0, matchStart));
                                int e = ClientProxy.Font.width(line.substring(0, matchEnd)) + 1;
                                boolean isExcluded = searchBar.isMatchExcluded(mi);
                                // Current match gets brighter highlight, others get dimmer
                                int highlightColor = (mi == currentMatchIdx) ? 0xBB4488ff : 0x662266aa;
                                if (isExcluded) {
                                    highlightColor = 0x33666666; // Dimmer for excluded matches
                                }
                                int highlightX = x + LINE_NUMBER_GUTTER_WIDTH + 1 + s;
                                int highlightEndX = x + LINE_NUMBER_GUTTER_WIDTH + 1 + e;
                                drawRect(highlightX, posY, highlightEndX, posY + container.lineHeight, highlightColor);

                                // Draw strikethrough line for excluded matches
                                if (isExcluded) {
                                    int strikeY = posY + container.lineHeight / 2;
                                    drawRect(highlightX, strikeY, highlightEndX, strikeY + 1, 0xFFaa4444);
                                }
                            }
                        }
                    }
                }

                // Highlight rename refactor occurrences
                if (renameHandler.isActive()) {
                    List<int[]> renameOccurrences = renameHandler.getOccurrences();
                    for (int[] occ : renameOccurrences) {
                        // Check if occurrence overlaps with this line
                        if (occ[1] >= data.start && occ[0] <= data.end) {
                            int occStart = Math.max(occ[0] - data.start, 0);
                            int occEnd = Math.min(occ[1] - data.start, line.length());

                            // Handle empty word case - draw 1 pixel wide box
                            boolean isEmpty = (occ[0] == occ[1]);

                            if (occStart <= occEnd) {  // Changed from < to <= to handle empty case
                                int s = ClientProxy.Font.width(line.substring(0, occStart));
                                int e = isEmpty ? s + 2 : ClientProxy.Font.width(
                                        line.substring(0, occEnd)) + 1; // 2px wide for empty
                                int occX = x + LINE_NUMBER_GUTTER_WIDTH  + s;
                                int occEndX = x + LINE_NUMBER_GUTTER_WIDTH + 2 + e;
                                boolean isPrimary = renameHandler.isPrimaryOccurrence(occ[0]);

                                // Draw background highlight
                                int bgColor = isPrimary ? 0x55335577 : 0x33224466;
                                drawRect(occX, posY, occEndX, posY + container.lineHeight, bgColor);

                                // Draw white border for primary occurrence (IntelliJ-like)
                                if (isPrimary) {
                                    int borderColor = 0xDDFFFFFF;
                                    //RED BG
                                    drawRect(occX, posY, occEndX, posY + container.lineHeight, 0x33ff0000);
                                    
                                    // Top border
                                    drawRect(occX, posY, occEndX, posY + 1, borderColor);
                                    // Bottom border  
                                    drawRect(occX, posY + container.lineHeight - 1, occEndX,
                                            posY + container.lineHeight, borderColor);
                                    // Left border
                                    drawRect(occX, posY, occX + 1, posY + container.lineHeight, borderColor);
                                    // Right border
                                    drawRect(occEndX - 1, posY, occEndX, posY + container.lineHeight, borderColor);

                                    // Draw cursor inside the primary occurrence
                                    if (renameHandler.shouldShowCursor()) {
                                        int cursorInWord = renameHandler.getCursorInWord();
                                        String currentWord = renameHandler.getCurrentWord();
                                        if (currentWord != null && cursorInWord >= 0 && cursorInWord <= currentWord.length()) {
                                            String beforeCursor = currentWord.substring(0,
                                                    Math.min(cursorInWord, currentWord.length()));
                                            int cursorX = occX + ClientProxy.Font.width(beforeCursor);
                                            // drawRect(cursorX, posY + 1, cursorX + 1, posY + container.lineHeight - 1,
                                            //    0xFFFFFFFF);
                                        }
                                    }
                                }
                            }
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

                        boolean highlighted = (openLine == highlightedOpenLine && closeLine == highlightedCloseLine);
                        int guideColor = highlighted ? 0x9933cc00 : 0x33FFFFFF;
                        
                        int topY = y + (drawStart - scroll.getScrolledLine()) * container.lineHeight;
                        int bottomY = y + (endLine - scroll.getScrolledLine() + 1) * container.lineHeight;
                        if(highlighted)
                            bottomY-=2;
                        drawRect(gx, topY, gx + 1, bottomY, guideColor);
                    }
                }
                int yPos = posY + stringYOffset;

                //data.drawString(x + LINE_NUMBER_GUTTER_WIDTH + 1, yPos, 0xFFe0e0e0);

                //scriptLine.drawString(x+LINE_NUMBER_GUTTER_WIDTH + 1, yPos, 0xFFe0e0e0);
                scriptLine.drawStringHex(x + LINE_NUMBER_GUTTER_WIDTH + 1, yPos);

                // Draw cursor: pause blinking while user is active recently
                boolean recentInput = selection.hadRecentInput();
                if (active && isEnabled() && (recentInput || (cursorCounter / 10) % 2 == 0) && (selection.getCursorPosition() >= data.start && selection.getCursorPosition() < data.end || (i == list.size() - 1 && selection.getCursorPosition() == text.length()))) {
                    int posX = x + LINE_NUMBER_GUTTER_WIDTH + ClientProxy.Font.width(
                            line.substring(0, Math.min(selection.getCursorPosition() - data.start, line.length())));
                    drawRect(posX + 1, posY, posX + 2, posY  + container.lineHeight, 0xffffffff);
                }
            }
        }

        // Render gutter icons for method override/implements
        if (container != null && container.getDocument() != null) {
            hoveredGutterMethod = GutterIconRenderer.renderIcons(container.lineHeight,
                    x + LINE_NUMBER_GUTTER_WIDTH - GutterIconRenderer.ICON_GUTTER_WIDTH + 1, y, renderStart, renderEnd,
                    scroll.getScrolledLine(), stringYOffset, container.getDocument().getAllMethods(), container.lines,
                    xMouse, yMouse, fracPixels);
        }
        
        GL11.glPopMatrix();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        
        if (hasVerticalScrollbar()) {
            Minecraft.getMinecraft().renderEngine.bindTexture(GuiCustomScroll.resource);
            int effLines = Math.max(1, getPaddedLineCount());
            int sbSize = Math.max((int) (1f * (container.visibleLines) / effLines * height), 2);

            int posX = x + width - 6;
            double linesCount = (double) effLines;
            int posY = (int) (y + 1f * scroll.getScrollPos() / linesCount * (height - 4)) + 1;

            drawRect(posX, posY, posX + 5, posY + sbSize + 2, 0xFFe0e0e0);
        }

        if (parent != null)
            parent.fullscreenButton.draw(xMouse, yMouse);
        
        // Draw search/replace bar (overlays viewport)
        searchBar.draw(xMouse, yMouse);
        
        // Draw go to line dialog (overlays everything)
        goToLineDialog.draw(xMouse, yMouse);
        
        KEYS_OVERLAY.draw(xMouse, yMouse, wheelDelta);

        // Draw autocomplete menu (overlays code area)
        autocompleteManager.draw(xMouse, yMouse);

        // Draw hover tooltips (on top of everything)
        if (hoverState.isTooltipVisible()) {
            int xOffset = hasVerticalScrollbar() ? -8 : -2;
            int viewportWidth = width - LINE_NUMBER_GUTTER_WIDTH;
            int viewportY = y;
            int viewportHeight = height;
            TokenHoverRenderer.render(hoverState, viewportX, viewportWidth+xOffset, viewportY, viewportHeight);
        }

        // Draw gutter icon tooltip
        if (hoveredGutterMethod != null) {
            GutterIconRenderer.renderTooltip(hoveredGutterMethod, xMouse, yMouse, x, width, y, height);
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
        // Adjust yMouse to account for fractional GL translation (negative offset applied in rendering).
        // Use a double here (no integer rounding) so clicks that land on partially
        // visible lines (fractional positions) correctly hit that line.
        double fracPixels = scroll.getFractionalOffset() * container.lineHeight;
        double yMouseD = yMouse + fracPixels;
        
        ArrayList list = new ArrayList(this.container.lines);

        for (int i = 0; i < list.size(); ++i) {
            LineData data = (LineData) list.get(i);
            //+1 to account for the fractional line
            if (i >= scroll.getScrolledLine() && i <= scroll.getScrolledLine() + this.container.visibleLines +1) {
                double yPos = (i - scroll.getScrolledLine()) * this.container.lineHeight;
                if (yMouseD >= yPos && yMouseD < yPos + this.container.lineHeight) {
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
    
    /**
     * Get the token at a specific screen position (mouse coordinates).
     * Also returns the token's screen position and dimensions for tooltip placement.
     * 
     * @param xMouse Screen X coordinate
     * @param yMouse Screen Y coordinate
     * @return Array of [Token, tokenScreenX, tokenScreenY, tokenWidth] or null if no token
     */
    private Object[] getTokenAtScreenPosition(int xMouse, int yMouse) {
        if (container == null || !(container instanceof ScriptTextContainer)) {
            return null;
        }
        
        ScriptTextContainer scriptContainer = (ScriptTextContainer) container;
        
        // Check if mouse is within the text viewport
        int viewportX = x + LINE_NUMBER_GUTTER_WIDTH + 1;
        if (xMouse < viewportX || xMouse > x + width || yMouse < y || yMouse > y + height) {
            return null;
        }
        
        // Adjust mouse position relative to text area
        int relativeY = yMouse - y;

        // Account for fractional scrolling
        double fracOffset = scroll.getFractionalOffset();
        double fracPixels = fracOffset * container.lineHeight;
        double adjustedY = relativeY + fracPixels;
        
        // Find which line the mouse is over
        int lineIdx = scroll.getScrolledLine() + (int)(adjustedY / container.lineHeight);
        if (lineIdx < 0 || lineIdx >= container.lines.size()) {
            return null;
        }
        
        ScriptLine lineData = container.getDocument().getLine(lineIdx);
        String lineText = lineData.getText();
        int lineStart = lineData.getGlobalStart();
        
        // Get the token at this position
        int globalMouseX = getSelectionPos(xMouse,yMouse);
        Token token = lineData.getTokenAt(globalMouseX, (t) -> t.getType() != TokenType.DEFAULT); // Ignore default tokens i.e. whitespaces
        if (token == null) 
            return null;
        
        // Calculate token's screen position
        int tokenLocalStart = token.getGlobalStart() - lineStart;
        int tokenLocalEnd = token.getGlobalEnd() - lineStart;
        tokenLocalStart = Math.max(0, Math.min(tokenLocalStart, lineText.length()));
        tokenLocalEnd = Math.max(0, Math.min(tokenLocalEnd, lineText.length()));
        
        int tokenScreenX = viewportX + ClientProxy.Font.width(lineText.substring(0, tokenLocalStart));
        int tokenScreenY = y + (lineIdx - scroll.getScrolledLine()) * container.lineHeight - (int)fracPixels;
        int tokenWidth = ClientProxy.Font.width(lineText.substring(tokenLocalStart, tokenLocalEnd));
        
        return new Object[] { token, tokenScreenX, tokenScreenY, tokenWidth };
    }
    
    /**
     * Update hover state based on current mouse position.
     * Called every frame from drawTextBox.
     */
    private void updateHoverState(int xMouse, int yMouse) {
        // Don't show tooltips when not active, clicking, or when overlays are visible
        if (!isEnabled() || clicked || searchBar.isVisible() ||
                goToLineDialog.isVisible() || KEYS_OVERLAY.isVisible() || renameHandler.isActive() || autocompleteManager.isVisible()) {
            hoverState.clearHover();
            return;
        }
        
        // Get token at current mouse position
        Object[] tokenInfo = getTokenAtScreenPosition(xMouse, yMouse);
        
        if (tokenInfo != null) {
            Token token = (Token) tokenInfo[0];
            int tokenScreenX = (Integer) tokenInfo[1];
            int tokenScreenY = (Integer) tokenInfo[2];
            int tokenWidth = (Integer) tokenInfo[3];
            
            hoverState.update(xMouse, yMouse, token, tokenScreenX, tokenScreenY, tokenWidth);
        } else {
            hoverState.update(xMouse, yMouse, null, 0, 0, 0);
        }
    }
    
    
    // Scroll viewport to keep cursor visible (minimal adjustment, like IntelliJ)
    // Only scrolls if cursor is outside the visible area
    private void scrollToCursor() {
        if (container == null || container.lines == null || container.lines.isEmpty()) return;
        
        int lineIdx = getCursorLineIndex();
        int visible = Math.max(1, container.visibleLines);
        int effectiveVisible = Math.max(1, visible - bottomPaddingLines);
        int maxScroll = Math.max(0, getPaddedLineCount() - visible);

        scroll.scrollToLine(lineIdx, effectiveVisible, maxScroll);
    }

    // ==================== KEY BINDINGS INITIALIZATION ====================

    /**
     * Initialize key bindings for editor shortcuts using ScriptEditorKeys.
     * Centralized active/enabled checking ensures shortcuts only fire when appropriate.
     */
    private void initializeKeyBindings() {
        // Helper: execute action only if text area is active and enabled
        Supplier<Boolean> isActive = () -> active && isEnabled() && !KEYS_OVERLAY.isVisible();
        Supplier<Boolean> openBoxes = () -> !KEYS_OVERLAY.isVisible();

        // CUT: Copy selection to clipboard and delete it. If no selection, cut the current sentence.
        KEYS.CUT.setTask(e -> {
            if (!e.isPress() || !isActive.get())
                return;

            if (selection.hasSelection()) {
                NoppesStringUtils.setClipboardContents(selection.getSelectedText(text));
                String s = getSelectionBeforeText();
                setText(s + getSelectionAfterText());
                selection.reset(s.length());
                scrollToCursor();
                return;
            }

            // No selection: cut the current sentence (heuristic based on .!? or newline)
            if (text == null || text.isEmpty())
                return;
            int cursor = selection.getCursorPosition();
            int start = cursor;
            int pos = cursor - 1;
            while (pos >= 0) {
                char ch = text.charAt(pos);
                if (ch == '.' || ch == '!' || ch == '?' || ch == '\n') {
                    start = pos + 1;
                    break;
                }
                pos--;
            }
            while (start < cursor && Character.isWhitespace(text.charAt(start))) start++;

            int end = cursor;
            pos = cursor;
            while (pos < text.length()) {
                char ch = text.charAt(pos);
                if (ch == '.' || ch == '!' || ch == '?' || ch == '\n') {
                    end = pos + 1;
                    break;
                }
                pos++;
            }
            if (end == cursor) end = text.length();
            while (end > start && Character.isWhitespace(text.charAt(end - 1))) end--;

            if (start >= end) {
                // fallback: cut whole current line
                int ls = text.lastIndexOf('\n', Math.max(0, cursor - 1));
                start = ls == -1 ? 0 : ls + 1;
                int le = text.indexOf('\n', cursor);
                end = le == -1 ? text.length() : le;
            }

            if (start < end) {
                String cut = text.substring(start, end);
                NoppesStringUtils.setClipboardContents(cut);
                setText(text.substring(0, start) + text.substring(end));
                selection.reset(start);
                scrollToCursor();
            }
        });

        // COPY: Copy selection to clipboard (or entire line if no selection)
        KEYS.COPY.setTask(e -> {
            if (!e.isPress() || !isActive.get())
                return;

            if (selection.hasSelection()) {
                NoppesStringUtils.setClipboardContents(selection.getSelectedText(text));
                lastCopyWasLine = false;
                lastCopiedLineText = null;
            } else {
                // Copy entire current line (including newline)
                int cursor = selection.getCursorPosition();
                LineData targetLine = null;
                for (LineData line : container.lines) {
                    if (cursor >= line.start && cursor <= line.end) {
                        targetLine = line;
                        break;
                    }
                }
                
                if (targetLine != null) {
                    int safeStart = Math.max(0, Math.min(targetLine.start, text.length()));
                    int safeEnd = Math.max(safeStart, Math.min(targetLine.end, text.length()));
                    String lineText = text.substring(safeStart, safeEnd);
                    NoppesStringUtils.setClipboardContents(lineText);
                    lastCopyWasLine = true;
                    lastCopiedLineText = lineText;
                }
            }
        });

        // PASTE: Insert clipboard contents at caret
        KEYS.PASTE.setTask(e -> {
            if (!e.isPress() || !isActive.get())
                return;

            String clipboard = NoppesStringUtils.getClipboardContents();
            if (clipboard == null)
                clipboard = "";

            if (selection.hasSelection()) {
                addText(clipboard);
                lastCopyWasLine = false;
                lastCopiedLineText = null;
                scrollToCursor();
                return;
            }

            boolean isLinePaste = lastCopyWasLine && lastCopiedLineText != null && clipboard.equals(lastCopiedLineText);
            if (isLinePaste && container != null && container.lines != null) {
                LineData currentLine = selection.findCurrentLine(container.lines);
                if (currentLine != null) {
                    int insertPos = Math.max(0, Math.min(currentLine.end, text.length()));
                    String insertText = clipboard;

                    // Ensure insertion happens on the line below
                    if (insertPos > 0 && text.charAt(insertPos - 1) != '\n') {
                        insertText = "\n" + insertText;
                    }

                    // Ensure the inserted line doesn't merge with the following line
                    if (insertPos < text.length() && !insertText.endsWith("\n")) {
                        insertText = insertText + "\n";
                    }

                    String newText = text.substring(0, insertPos) + insertText + text.substring(insertPos);
                    setText(newText);

                    int newCursor = insertPos + (insertText.startsWith("\n") ? 1 : 0);
                    selection.reset(Math.min(newCursor, newText.length()));
                    scrollToCursor();
                    return;
                }
            }

            addText(clipboard);
            scrollToCursor();
        });

        // UNDO: Restore last edit from undo list
        // Works in search bar.
        KEYS.UNDO.setTask(e -> {
            if ((!e.isPress() && !e.isHold()) || !openBoxes.get())
                return;

            if (searchBar.hasFocus()) {
                searchBar.undo();
            } else {
                if (undoList.isEmpty())
                    return;
                undoing = true;
                redoList.add(new UndoData(this.text, selection.getCursorPosition()));
                UndoData data = undoList.remove(undoList.size() - 1);
                setText(data.text);
                selection.reset(data.cursorPosition);
                undoing = false;
                scrollToCursor();
                searchBar.updateMatches();
                if (!active)
                    active = true;
            }
        });

        // REDO: Restore last undone edit from redo list
        // Works in search bar.
        KEYS.REDO.setTask(e -> {
            if ((!e.isPress() && !e.isHold()) || !openBoxes.get())
                return;

            if (searchBar.hasFocus()) {
                searchBar.redo();
            } else {
                if (redoList.isEmpty())
                    return;
                undoing = true;
                undoList.add(new UndoData(this.text, selection.getCursorPosition()));
                UndoData data = redoList.remove(redoList.size() - 1);
                setText(data.text);
                selection.reset(data.cursorPosition);
                undoing = false;
                scrollToCursor();
                searchBar.updateMatches();
                if (!active)
                    active = true;
            }
        });

        // FORMAT: Format/indent code
        KEYS.FORMAT.setTask(e -> {
            if (!e.isPress() || !isActive.get())
                return;
            formatText();
        });

        // TOGGLE_COMMENT: Toggle comment for selection or current line
        KEYS.TOGGLE_COMMENT.setTask(e -> {
            if (!e.isPress() || !isActive.get())
                return;

            if (selection.hasSelection())
                toggleCommentSelection();
            else
                toggleCommentLineAtCursor();
        });

        // DUPLICATE: Duplicate selection or current line
        KEYS.DUPLICATE.setTask(e -> {
            if (!e.isPress() || !isActive.get())
                return;

            if (selection.hasSelection()) {
                // Multi-line selection duplication
                LineData firstLine = null, lastLine = null;
                for (LineData line : container.lines) {
                    if (line.end > selection.getStartSelection() && line.start < selection.getEndSelection()) {
                        if (firstLine == null)
                            firstLine = line;
                        lastLine = line;
                    }
                }
                if (firstLine != null && lastLine != null) {
                    String selectedText = text.substring(firstLine.start, lastLine.end);
                    int savedStart = selection.getStartSelection();
                    int savedEnd = selection.getEndSelection();
                    int insertAt = lastLine.end;
                    setText(text.substring(0, insertAt) + selectedText + text.substring(insertAt));
                    selection.setStartSelection(savedStart);
                    selection.setEndSelection(savedEnd);
                    selection.setCursorPositionDirect(savedEnd);
                }
            } else {
                // Duplicate current line
                for (LineData line : container.lines) {
                    if (selection.getCursorPosition() >= line.start && selection.getCursorPosition() <= line.end) {
                        int safeStart = Math.max(0, Math.min(line.start, text.length()));
                        int safeEnd = Math.max(safeStart, Math.min(line.end, text.length()));
                        String lineText = text.substring(safeStart, safeEnd);
                        String insertText = lineText.endsWith("\n") ? lineText : "\n" + lineText;
                        int insertionPoint = Math.min(line.end, text.length());
                        setText(text.substring(0, insertionPoint) + insertText + text.substring(insertionPoint));
                        int newCursor = insertionPoint + insertText.length() - (insertText.endsWith("\n") ? 1 : 0);
                        selection.reset(Math.max(0, Math.min(newCursor, this.text.length())));
                        break;
                    }
                }
            }
        });

        // DELETE_LINE: Delete the current line
        KEYS.DELETE_LINE.setTask(e -> {
            if (!e.isPress() || !isActive.get())
                return;

            if (text == null || text.isEmpty())
                return;

            int cursor = selection.getCursorPosition();
            
            LineData targetLine = null;
            int targetIndex = -1;
            for (LineData line : container.lines) {
                if (cursor >= line.start && cursor <= line.end) {
                    targetLine = line;
                    targetIndex = container.lines.indexOf(line);
                    break;
                }
            }
            
            if (targetLine == null)
                return;
            
            int start = Math.max(0, Math.min(targetLine.start, text.length()));
            int end = Math.max(start, Math.min(targetLine.end, text.length()));
            
            String newText = text.substring(0, start) + text.substring(end);
            setText(newText);

            int newCursor = 0;
            if (targetIndex > 0 && targetIndex <= container.lines.size() - 1) {
                LineData previousLine = container.lines.get(targetIndex - 1);
                int prevStart = Math.max(0, Math.min(previousLine.start, newText.length()));
                int prevEnd = Math.max(prevStart, Math.min(previousLine.end, newText.length()));
                int prevLen = Math.max(0, prevEnd - prevStart);
                int offsetInLine = Math.max(0, Math.min(cursor - targetLine.start, prevLen));
                newCursor = Math.min(prevStart + offsetInLine, newText.length());
            }

            selection.reset(Math.min(newCursor, newText.length()));
            scrollToCursor();
        });

        // MOVE_LINE_UP: Move current line up
        KEYS.MOVE_LINE_UP.setThrottleInterval(50).setTask(e -> {
            if ((!e.isPress() && !e.isHold()) || !isActive.get())
                return;

            if (text == null || text.isEmpty())
                return;

            int tLen = text.length();
            int cursor = Math.max(0, Math.min(selection.getCursorPosition(), tLen));

            // Work on real newline-delimited lines, not wrapped LineData.
            int currStart = text.lastIndexOf('\n', Math.max(0, cursor - 1));
            currStart = currStart == -1 ? 0 : (currStart + 1);
            if (currStart == 0)
                return; // Can't move first line up

            int currEnd = text.indexOf('\n', cursor);
            currEnd = currEnd == -1 ? tLen : (currEnd + 1);

            int prevEnd = currStart;
            int prevStart = text.lastIndexOf('\n', Math.max(0, prevEnd - 2));
            prevStart = prevStart == -1 ? 0 : (prevStart + 1);

            String previousText = text.substring(prevStart, prevEnd);
            String currentText = text.substring(currStart, currEnd);

            // If current line is the last line without a trailing newline, but it is being moved
            // into the middle, ensure it ends with '\n' by transferring the '\n' from the previous line.
            if (!currentText.endsWith("\n") && previousText.endsWith("\n") && !previousText.isEmpty()) {
                currentText = currentText + "\n";
                previousText = previousText.substring(0, previousText.length() - 1);
            }

            String before = text.substring(0, prevStart);
            String after = text.substring(currEnd);
            String newText = before + currentText + previousText + after;
            setText(newText);

            int currentContentLen = currentText.endsWith("\n") ? Math.max(0, currentText.length() - 1) : currentText.length();
            int offsetInLine = Math.max(0, Math.min(cursor - currStart, currentContentLen));
            int newCursor = Math.min(prevStart + offsetInLine, newText.length());
            selection.reset(newCursor);
            scrollToCursor();
        });

        // MOVE_LINE_DOWN: Move current line down
        KEYS.MOVE_LINE_DOWN.setThrottleInterval(50).setTask(e -> {
            if ((!e.isPress() && !e.isHold()) || !isActive.get())
                return;

            if (text == null || text.isEmpty())
                return;

            int tLen = text.length();
            int cursor = Math.max(0, Math.min(selection.getCursorPosition(), tLen));

            // Work on real newline-delimited lines, not wrapped LineData.
            int currStart = text.lastIndexOf('\n', Math.max(0, cursor - 1));
            currStart = currStart == -1 ? 0 : (currStart + 1);

            int currEnd = text.indexOf('\n', cursor);
            currEnd = currEnd == -1 ? tLen : (currEnd + 1);
            if (currEnd >= tLen)
                return; // Can't move last line down

            int nextStart = currEnd;
            int nextEnd = text.indexOf('\n', nextStart);
            nextEnd = nextEnd == -1 ? tLen : (nextEnd + 1);

            String currentText = text.substring(currStart, currEnd);
            String nextText = text.substring(nextStart, nextEnd);

            // If the next line is the last line and doesn't end with '\n' (common when it contains
            // only indentation spaces), swapping can merge whitespace onto the moved line.
            // Fix by transferring the '\n' from currentText to nextText.
            if (!nextText.endsWith("\n") && currentText.endsWith("\n") && !currentText.isEmpty()) {
                currentText = currentText.substring(0, currentText.length() - 1);
                nextText = nextText + "\n";
            }

            String before = text.substring(0, currStart);
            String after = text.substring(nextEnd);
            String newText = before + nextText + currentText + after;
            setText(newText);

            int currentContentLen = currentText.endsWith("\n") ? Math.max(0, currentText.length() - 1) : currentText.length();
            int offsetInLine = Math.max(0, Math.min(cursor - currStart, currentContentLen));
            int newCursor = Math.min(currStart + nextText.length() + offsetInLine, newText.length());
            selection.reset(newCursor);
            scrollToCursor();
        });


        // Check if can open just for SearchReplaceBar and GoToLine

        // SEARCH: Open search bar (Ctrl+R)
        // Works in search bar.
        KEYS.SEARCH.setTask(e -> {
            if (!e.isPress() || !openBoxes.get())
                return;
            
            unfocusAll();
            searchBar.openSearch();
        });
        
        // SEARCH_REPLACE: Open search+replace bar (Ctrl+Shift+R)
        // Works in search bar.
        KEYS.SEARCH_REPLACE.setTask(e -> {
            if (!e.isPress() || !openBoxes.get())
                return;
            
            unfocusAll();
            searchBar.openSearchReplace();
        });
        
        // GO_TO_LINE: Open go to line dialog (Ctrl+G)
        // Works in search bar.
        KEYS.GO_TO_LINE.setTask(e -> {
            if (!e.isPress() || !openBoxes.get())
                return;
            
            unfocusAll();
            goToLineDialog.toggle();
        });

        // RENAME: Start rename refactoring (Shift+F6)
        KEYS.RENAME.setTask(e -> {
            if (!e.isPress() || !openBoxes.get())
                return;


            if (!renameHandler.isActive()) {
                unfocusAll();
                active = true;
                renameHandler.startRename();
            }
        });
        
        // AUTOCOMPLETE: Trigger autocomplete (Ctrl+Space)
        KEYS.AUTOCOMPLETE.setTask(e -> {
            if (!e.isPress() || !isActive.get())
                return;
            
            autocompleteManager.triggerExplicit();
        });
    }

    public void unfocusAll() {
        if (searchBar.hasFocus()) searchBar.unfocus();
        if (goToLineDialog.hasFocus()) goToLineDialog.unfocus();
        if (renameHandler.isActive())
            renameHandler.cancel();
        if (autocompleteManager.isVisible())
            autocompleteManager.dismiss();
    }
    // ==================== KEYBOARD INPUT HANDLING ====================

    /**
     * Handles keyboard input for the text area, delegating to specialized handlers
     * for different types of input: navigation, deletion, shortcuts, and character input.
     */
    @Override
    public boolean textboxKeyTyped(char c, int i) {
        if (KEYS_OVERLAY.keyTyped(c, i))
            return true;

        // Handle rename refactor input first if active
        if (renameHandler.isActive() && renameHandler.keyTyped(c, i))
            return true;
        
        // Handle Go To Line dialog input first if it has focus
        if (goToLineDialog.isVisible() &&goToLineDialog.keyTyped(c, i)) 
                return true;
        
        // Handle search bar input first if it has focus
        if (searchBar.isVisible() && searchBar.keyTyped(c, i)) 
            return true;
        
        // Handle autocomplete navigation keys first when visible
        if (autocompleteManager.isVisible()) {
            if (autocompleteManager.keyPressed(i)) {
                return true;
            }
        }

        // Ignore if any global keys bound to this code are currently pressed
        if (KEYS.hasMatchingKeyPressed(i))
            return false;
        

        if (!active)
            return false;

        if (this.isKeyComboCtrlA(i)) {
            selection.selectAll(text.length());
            return true;
        }

        if (!isEnabled()) return false;

        if (handleNavigationKeys(i)) return true;
        if (handleInsertionKeys(i)) return true;
        if (handleDeletionKeys(i)) return true;
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
                int pos = selection.getCursorPosition();
                int g = pos;
                java.util.function.IntPredicate isWordChar = ch -> Character.isLetterOrDigit(ch) || ch == '_';

                if (pos > 0) {
                    char left = text.charAt(pos - 1);
                    if (Character.isWhitespace(left)) {
                        while (g - 1 >= 0 && Character.isWhitespace(text.charAt(g - 1))) g--;
                    } else if (isWordChar.test(left)) {
                        while (g - 1 >= 0 && isWordChar.test(text.charAt(g - 1))) g--;
                    } else {
                        while (g - 1 >= 0 && !Character.isWhitespace(text.charAt(g - 1)) && !isWordChar.test(text.charAt(g - 1))) g--;
                    }
                }
                j = Math.max(1, pos - g);
            }
            int newPos = Math.max(selection.getCursorPosition() - j, 0);
            // If Shift is held, extend selection; otherwise place caret.
            setCursor(newPos, GuiScreen.isShiftKeyDown());
            
            // Notify autocomplete of cursor movement
            if (autocompleteManager.isVisible()) {
                autocompleteManager.onCursorMove(text, newPos);
            }
            
            return true;
        }

        // RIGHT ARROW: move cursor right; with Ctrl -> jump to next word start
        if (i == Keyboard.KEY_RIGHT) {
            int j = 1; // default: move one character
            if (isCtrlKeyDown()) {
                int pos = selection.getCursorPosition();
                int end = pos;
                java.util.function.IntPredicate isWordChar = ch -> Character.isLetterOrDigit(ch) || ch == '_';

                if (pos < text.length()) {
                    char first = text.charAt(pos);
                    if (Character.isWhitespace(first)) {
                        while (end < text.length() && Character.isWhitespace(text.charAt(end))) end++;
                    } else if (isWordChar.test(first)) {
                        while (end < text.length() && isWordChar.test(text.charAt(end))) end++;
                    } else {
                        while (end < text.length() && !Character.isWhitespace(text.charAt(end)) && !isWordChar.test(text.charAt(end))) end++;
                    }
                }
                j = Math.max(1, end - pos);
            }
            int newPos = Math.min(selection.getCursorPosition() + j, text.length());
            setCursor(newPos, GuiScreen.isShiftKeyDown());
            
            // Notify autocomplete of cursor movement
            if (autocompleteManager.isVisible()) {
                autocompleteManager.onCursorMove(text, newPos);
            }
            
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
     * Handles insertion-related keys such as Tab indentation and Enter behavior.
     */
    private boolean handleInsertionKeys(int i) {
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

        // RETURN/ENTER: special handling for /** javadoc stub and opening brace '{'
        if (i == Keyboard.KEY_RETURN) {
            int cursorPos = selection.getCursorPosition();
            
            // Check for /** javadoc stub auto-generation
            String before = getSelectionBeforeText();
            if (before.endsWith("/**")) {
                // Find current line to get indent level
                String indent = "";
                for (LineData ld : this.container.lines) {
                    if (cursorPos >= ld.start && cursorPos <= ld.end) {
                        indent = ld.text.substring(0, IndentHelper.getLineIndent(ld.text));
                        break;
                    }
                }
                
                // Look ahead for a function declaration to generate smart JSDoc
                String after = getSelectionAfterText();
                String javadocStub = generateJSDocStub(after, indent);
                addText(javadocStub);
                
                // Position cursor after " * " on the description line
                int newCursorPos = before.length() + 1 + indent.length() + 3; // +1 for \n, +3 for " * "
                selection.reset(newCursorPos);
                scrollToCursor();
                return true;
            }
            
            int prevNonWs = cursorPos - 1;
            while (prevNonWs >= 0 && prevNonWs < (text != null ? text.length() : 0) && Character.isWhitespace(
                    text.charAt(prevNonWs))) {
                prevNonWs--;
            }

            if (prevNonWs >= 0 && cursorPos <= (text != null ? text.length() : 0) && text.charAt(prevNonWs) == '{') {
                String indent = "";
                for (LineData ld : this.container.lines) {
                    if (prevNonWs >= ld.start && prevNonWs < ld.end) {
                        indent = ld.text.substring(0, IndentHelper.getLineIndent(ld.text));
                        break;
                    }
                }
                if (indent == null)
                    indent = "";
                String childIndent = indent + "    ";
                String after = getSelectionAfterText();

                int firstNewline = after.indexOf('\n');
                String leadingSegment = firstNewline == -1 ? after : after.substring(0, firstNewline);
                if (leadingSegment.trim().length() > 0) {
                    addText("\n" + childIndent);
                    scrollToCursor();
                    return true;
                }

                boolean hasMatchingCloseSameIndent = false;
                try {
                    int openLineIdx = -1;
                    int bracePos = prevNonWs;
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
                                int closeIndent = IndentHelper.getLineIndent(this.container.lines.get(spanClose).text);
                                if (closeIndent == indent.length()) {
                                    hasMatchingCloseSameIndent = true;
                                }
                                break;
                            }
                        }
                    }
                } catch (Exception ex) {
                    hasMatchingCloseSameIndent = false;
                }

                if (hasMatchingCloseSameIndent) {
                    addText("\n" + childIndent);
                    scrollToCursor();
                } else {
                    String insert = "\n" + childIndent + "\n" + indent + "}";
                    setText(before + insert + after);
                    int newCursor = before.length() + 1 + childIndent.length();
                    selection.reset(newCursor);
                    scrollToCursor();
                }
            } else {
                addText(Character.toString('\n') + getAutoIndentForEnter());
                scrollToCursor();
            }
            return true;
        }

        return false;
    }
    
    /**
     * Generates a JSDoc stub for a function declaration.
     * Looks at the text after the cursor to find function signature and generates
     * appropriate @param and @return tags.
     * 
     * @param after Text after the /** to search for function
     * @param indent Current line indentation
     * @return The JSDoc stub string including newlines
     */
    private String generateJSDocStub(String after, String indent) {
        StringBuilder stub = new StringBuilder();
        stub.append("\n").append(indent).append(" * ");  // Description line
        
        // Try to find a function/method declaration or field declaration following the JSDoc
        // Skip whitespace and newlines
        String trimmed = after.replaceFirst("^[\\s\\n\\r]*", "");
        
        // First, try to match function/method pattern
        java.util.regex.Pattern funcPattern = java.util.regex.Pattern.compile(
            // Group 1: Optional return type (Java) or 'function' keyword (JS)
            "^(?:(\\w+(?:<[^>]+>)?|function)\\s+)?" +
            // Group 2: Method/function name  
            "(\\w+)\\s*" +
            // Group 3: Parameters inside parentheses
            "\\(([^)]*)\\)"
        );
        
        java.util.regex.Matcher funcMatcher = funcPattern.matcher(trimmed);
        if (funcMatcher.find()) {
            // Handle function/method
            String returnOrKeyword = funcMatcher.group(1);
            String funcName = funcMatcher.group(2);
            String paramsStr = funcMatcher.group(3) != null ? funcMatcher.group(3).trim() : "";
            
            // Determine if this is JavaScript (function keyword or no return type)
            boolean isJS = "function".equals(returnOrKeyword) || returnOrKeyword == null ||
                (container != null && container.getDocument() != null && container.getDocument().isJavaScript());
            
            // Parse parameters
            if (!paramsStr.isEmpty()) {
                String[] params = paramsStr.split(",");
                for (String param : params) {
                    param = param.trim();
                    if (param.isEmpty()) continue;
                    
                    String paramName;
                    String paramType = "any";
                    
                    // Split on whitespace to get type and name
                    String[] parts = param.split("\\s+");
                    if (parts.length >= 2) {
                        // Java style: Type name
                        paramType = parts[parts.length - 2];
                        paramName = parts[parts.length - 1];
                    } else {
                        // JS style: just name
                        paramName = parts[0];
                    }
                    
                    // Remove any trailing array brackets or varargs
                    paramName = paramName.replaceAll("[\\[\\]\\.]", "");
                    
                    if (isJS) {
                        stub.append("\n").append(indent).append(" * @param {").append(paramType).append("} ").append(paramName);
                    } else {
                        stub.append("\n").append(indent).append(" * @param ").append(paramName);
                    }
                }
            }
            
            // Add @return/@returns tag if applicable
            if (returnOrKeyword != null && !"function".equals(returnOrKeyword) && !"void".equals(returnOrKeyword)) {
                if (isJS) {
                    stub.append("\n").append(indent).append(" * @returns {").append(returnOrKeyword).append("}");
                } else {
                    stub.append("\n").append(indent).append(" * @return");
                }
            } else if (isJS) {
                // For JS functions without explicit return type, add empty @returns
                stub.append("\n").append(indent).append(" * @returns {any}");
            }
        } else {
            // Try to match field declaration: [modifiers] Type name or var/let/const name
            java.util.regex.Pattern fieldPattern = java.util.regex.Pattern.compile(
                "^(?:(?:public|private|protected|static|final|var|let|const)\\s+)*" + // Optional modifiers
                "(\\w+(?:<[^>]+>)?)\\s+" +  // Type (Group 1)
                "(\\w+)" +                     // Field name (Group 2)
                "(?:\\s*=|\\s*;)"              // Followed by = or ;
            );
            
            java.util.regex.Matcher fieldMatcher = fieldPattern.matcher(trimmed);
            if (fieldMatcher.find()) {
                String fieldType = fieldMatcher.group(1);
                
                // Check if this is JavaScript
                boolean isJS = container != null && container.getDocument() != null && container.getDocument().isJavaScript();
                
                if (isJS && fieldType != null) {
                    // For JS fields, add @type annotation
                    stub.append("\n").append(indent).append(" * @type {").append("any").append("}");
                }
            }
        }
        
        // Close the JSDoc block
        stub.append("\n").append(indent).append(" */");
        
        return stub.toString();
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
            setText(getSelectionBeforeText() + s, true); // Use atomic undo
            // Keep caret at same start selection
            selection.reset(selection.getStartSelection());
            return true;
        }

        // CTRL+BACKSPACE: delete to previous word or whitespace boundary.
        if (isKeyComboCtrlBackspace(i)) {
            String before = getSelectionBeforeText();
            if (selection.getStartSelection() > 0 && !selection.hasSelection()) {
                int pos = selection.getCursorPosition();
                int g = pos;

                // Helper: treat letters, digits and underscore as word characters
                java.util.function.IntPredicate isWordChar = ch -> Character.isLetterOrDigit(ch) || ch == '_';

                // If caret is after whitespace, delete contiguous whitespace first
                char left = before.charAt(pos - 1);
                if (Character.isWhitespace(left)) {
                    while (g - 1 >= 0 && Character.isWhitespace(before.charAt(g - 1)))
                        g--;
                } else if (isWordChar.test(left)) {
                    // Delete contiguous word characters (letters/digits/_)
                    while (g - 1 >= 0 && isWordChar.test(before.charAt(g - 1)))
                        g--;
                } else {
                    // Delete contiguous non-word, non-whitespace characters (punctuation)
                    while (g - 1 >= 0 && !Character.isWhitespace(before.charAt(g - 1)) && !isWordChar.test(before.charAt(g - 1)))
                        g--;
                }

                before = before.substring(0, g);
                selection.setStartSelection(selection.getStartSelection() - (pos - g));
            }
            setText(before + getSelectionAfterText(), true); // Use atomic undo
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
                setText(s + getSelectionAfterText(), true); // Use atomic undo
                selection.reset(selection.getStartSelection());
                scrollToCursor();
                return true;
            }

            // 2) nothing to delete
            if (selection.getStartSelection() <= 0) {
                return true;
            }

            // If the current line is whitespace-only, delete the whole line
            // (including the trailing newline if present). This makes Backspace
            // intuitive on blank/indented lines outside any recognized scope.
            LineData currCheck = selection.findCurrentLine(container.lines);
            if (currCheck != null && currCheck.text.trim().length() == 0) {
                int removeEnd = text.indexOf('\n', currCheck.start - 1);
                if (removeEnd == -1) {
                    removeEnd = text.length();
                } else {
                    removeEnd = removeEnd + 1; // include the newline
                }
                String before = text.substring(0, ValueUtil.clamp(currCheck.start - 1, 0, text.length()));
                String after = removeEnd <= text.length() ? text.substring(removeEnd) : "";
                setText(before + after, true); // Use atomic undo
                int newCursor = Math.max(0, currCheck.start - 1);
                selection.reset(newCursor);
                scrollToCursor();
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
                        setText(before + after, true); // Use atomic undo
                        // Place caret at end of previous line
                        int newCursor = Math.max(0, curr.start - 1);
                        selection.reset(newCursor);
                        scrollToCursor();
                        return true;
                    } else {
                        // Merge current line content with the previous line preserving spacing
                        int contentStart = curr.start + actualIndent;
                        String before = newlinePos >= 0 ? text.substring(0, newlinePos) : "";
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

                        setText(before + spacer + content, true); // Use atomic undo
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
                    setText(before + after, true); // Use atomic undo
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
            setText(s + getSelectionAfterText(), true); // Use atomic undo
            selection.reset(selection.getStartSelection());
            scrollToCursor();
            // Notify autocomplete of deletion
            autocompleteManager.onDeleteKey(text, selection.getCursorPosition());
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

        // CTRL+F: format the text according to IndentHelper rules
        if (i == Keyboard.KEY_F && isCtrlKeyDown()) {
            formatText();
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
                // Notify autocomplete of the character
                autocompleteManager.onCharTyped(c, text, selection.getCursorPosition());
                return true;
            }

            // Auto-pair insertion: when opening a quote/brace/bracket is typed,
            // insert a matching closer and place caret between the pair.
            if (c == '"') {
                setText(before + "\"\"" + after, true);
                selection.reset(before.length() + 1);
                scrollToCursor();
                // Notify autocomplete of the character
                autocompleteManager.onCharTyped(c, text, selection.getCursorPosition());
                return true;
            }
            if (c == '\'') {
                setText(before + "''" + after, true);
                selection.reset(before.length() + 1);
                scrollToCursor();
                // Notify autocomplete of the character
                autocompleteManager.onCharTyped(c, text, selection.getCursorPosition());
                return true;
            }
            if (c == '[') {
                setText(before + "[]" + after, true);
                selection.reset(before.length() + 1);
                scrollToCursor();
                // Notify autocomplete of the character
                autocompleteManager.onCharTyped(c, text, selection.getCursorPosition());
                return true;
            }
            if (c == '(') {
                setText(before + "()" + after, true);
                selection.reset(before.length() + 1);
                scrollToCursor();
                // Notify autocomplete of the character
                autocompleteManager.onCharTyped(c, text, selection.getCursorPosition());
                return true;
            }

            // Default insertion for printable characters: insert at caret (replacing selection)
            addText(Character.toString(c));
            scrollToCursor();
            // Notify autocomplete of the character
            autocompleteManager.onCharTyped(c, text, selection.getCursorPosition());
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
    }

    private void toggleCommentLineAtCursor() {
        CommentHandler.SingleLineToggleResult result = CommentHandler.toggleCommentAtCursor(
                text, container.lines, selection.getCursorPosition());
        setText(result.newText);
        setCursor(result.newCursorPosition, false);
    }
    
    public boolean closeOnEsc(){
        return !KEYS_OVERLAY.isVisible() && !searchBar.isVisible() && !goToLineDialog.isVisible() && !renameHandler.isActive() && !autocompleteManager.isVisible(); 
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
        // Calculate viewport width for line wrapping (account for gutter and scrollbar)
        int viewportWidth = this.width - LINE_NUMBER_GUTTER_WIDTH - 10;
        IndentHelper.FormatResult result = IndentHelper.formatText(text, selection.getCursorPosition(), viewportWidth);
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
        this.setText(this.getSelectionBeforeText() + s + this.getSelectionAfterText(), true); // Use atomic undo for typing
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
        // Check autocomplete menu clicks first
        if (autocompleteManager.isVisible() && autocompleteManager.mouseClicked(xMouse, yMouse, mouseButton)) {
            return;
        }
        
        // Dismiss autocomplete if clicking elsewhere
        if (autocompleteManager.isVisible()) {
            autocompleteManager.dismiss();
        }

        if (parent != null && parent.fullscreenButton.mouseClicked(xMouse, yMouse, mouseButton))
            return;
        
        
        // Check go to line dialog clicks first
        if (goToLineDialog.isVisible() && goToLineDialog.mouseClicked(xMouse, yMouse, mouseButton)) {
            return;
        }
        
        // Check search bar clicks first
        if (searchBar.isVisible() && searchBar.mouseClicked(xMouse, yMouse, mouseButton)) {
            return;
        }
        
        // If search bar is visible but click was outside it, unfocus the search bar
        if (searchBar.isVisible()) {
            searchBar.unfocus();
        }
        
        // Let the overlay consume clicks (it returns true when it handled the event)
        if (KEYS_OVERLAY.mouseClicked(xMouse, yMouse, mouseButton))
            return;
            
        // Determine whether click occurred inside the text area bounds
        this.active = xMouse >= this.x && xMouse < this.x + this.width && yMouse >= this.y && yMouse < this.y + this.height;
        if (this.active) {
            // Ctrl+Click: Go to definition
            if (mouseButton == 0 && isCtrlKeyDown()) {
                Object[] tokenInfo = getTokenAtScreenPosition(xMouse, yMouse);
                if (tokenInfo != null) {
                    Token token = (Token) tokenInfo[0];
                    int targetOffset = -1;
                    
                    // Check if token has method info with declaration
                    if (token.getMethodInfo() != null) {
                        MethodInfo methodInfo = token.getMethodInfo();
                        if (methodInfo.getNameOffset() >= 0) {
                            targetOffset = methodInfo.getNameOffset();
                        }
                    }
                    // Check if token has method call info with resolved declaration
                    else if (token.getMethodCallInfo() != null) {
                        MethodCallInfo callInfo = token.getMethodCallInfo();
                        MethodInfo resolvedMethod = callInfo.getResolvedMethod();
                        if (resolvedMethod != null && resolvedMethod.getNameOffset() >= 0) {
                            targetOffset = resolvedMethod.getNameOffset();
                        }
                    }
                    // Check if token has field info with declaration
                    else if (token.getFieldInfo() != null) {
                        FieldInfo fieldInfo = token.getFieldInfo();
                        if (fieldInfo.getDeclarationOffset() >= 0) {
                            targetOffset = fieldInfo.getDeclarationOffset();
                        }
                    }
                    // Check if token has field access info with resolved declaration
                    else if (token.getFieldAccessInfo() != null) {
                        FieldAccessInfo accessInfo = token.getFieldAccessInfo();
                        FieldInfo resolvedField = accessInfo.getResolvedField();
                        if (resolvedField != null && resolvedField.getDeclarationOffset() >= 0) {
                            targetOffset = resolvedField.getDeclarationOffset();
                        }
                    }
                    // Check if token is a script-defined type
                    else if (token.getTypeInfo() != null && token.getTypeInfo() instanceof ScriptTypeInfo) {
                       ScriptTypeInfo scriptType = 
                            (ScriptTypeInfo) token.getTypeInfo();
                        if (scriptType.getDeclarationOffset() >= 0) {
                            targetOffset = scriptType.getDeclarationOffset();
                        }
                    }
                    
                    // Jump to definition if found
                    if (targetOffset >= 0) {
                        selection.reset(targetOffset);
                        scrollToCursor();
                        this.clicked = false;
                        activeTextfield = this;
                        return; // Consume the event
                    }
                }
            }
            
            // Compute logical click position in text
            int clickPos = this.getSelectionPos(xMouse, yMouse);

            // Check if rename refactoring is active and click is in the rename box
            if (renameHandler.isActive() && renameHandler.handleClick(clickPos)) {
                // Click was handled by rename handler - don't reset selection or do other click handling
                this.clicked = false;
                activeTextfield = this;
                return;
            }

            // Normal click handling - reset selection/caret
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
            if (this.clicked && getPaddedLineCount() * this.container.lineHeight > this.height && xMouse > this.x + this.width - 8) {
                // We consumed the mouse-down as a scrollbar drag start
                this.clicked = false;
                scroll.startScrollbarDrag(yMouse,this.y,this.height, getPaddedLineCount());
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
                    // Prevent subsequent mouse-drag handling in the render loop from
                    // treating this double-click as a normal click-drag which would
                    // immediately reset and extend the selection. Clearing `clicked`
                    // keeps the double-click selection stable.
                    this.clicked = false;
                } else if (this.clickCount >= 3) {
                    // Triple-click: select the entire logical line that contains the caret
                    this.tripleClicked = true;
                    selection.selectLineAtCursor(container.lines);
                    // Same as double-click: clear `clicked` to avoid accidental drag-extension
                    this.clicked = false;
                    this.clickCount = 0;
                }
            }

            this.lastClicked = time;
            activeTextfield = this;

            // Click-to-pin handling: if enabled, clicking a token will pin/unpin its tooltip
            if (mouseButton == 0 && hoverState.isClickToPinEnabled()) {
                Object[] tokenInfo = getTokenAtScreenPosition(xMouse, yMouse);
                if (tokenInfo != null) {
                    Token clickedToken = (Token) tokenInfo[0];
                    int tokenScreenX = (Integer) tokenInfo[1];
                    int tokenScreenY = (Integer) tokenInfo[2];
                    int tokenWidth = (Integer) tokenInfo[3];

                    // If already pinned on same token, unpin; otherwise pin this token
                    if (hoverState.isPinned() && hoverState.getHoveredToken() == clickedToken) {
                        hoverState.unpin();
                    } else {
                        hoverState.pinToken(clickedToken, tokenScreenX, tokenScreenY, tokenWidth);
                    }
                } else {
                    // Clicked outside any token -> unpin any pinned tooltip
                    if (hoverState.isPinned()) {
                        hoverState.unpin();
                    }
                }
            }
        }
    }

    // Called from GuiScreen.updateScreen()
    public void updateCursorCounter() {
        // Only process KeyPresets if search bar and go-to-line dialog don't have focus
        // This prevents COPY, PASTE, UNDO, etc. from firing when typing in dialogs
        KEYS.tick();

        searchBar.updateCursor();
        goToLineDialog.updateCursor();
        renameHandler.updateCursor();
        ++this.cursorCounter;
    }
    
    // ==================== TEXT MANAGEMENT ====================

    public void setText(String text) {
        setText(text, false);
    }
    
    /**
     * Set text with optional atomic undo support.
     * 
     * @param text The new text content
     * @param allowAtomic If true, allows grouping consecutive typing into single undo steps
     */
    private void setText(String text, boolean allowAtomic) {
        if (text == null) {
            return;
        }

        text = text.replace("\r", "");
        text = text.replace("\t", "    ");
        // preserve trailing newlines here — JavaTextContainer.init will
        // ignore an extra empty final split when rendering lines.
        if (this.text == null || !this.text.equals(text)) {
            if (this.listener != null) {
                this.listener.textUpdate(text);
            }

            // Atomic undo logic: group consecutive typing into word-based undo steps
            if (!this.undoing && allowAtomic && !undoList.isEmpty()) {
                long now = System.currentTimeMillis();
                int cursorPos = selection.getCursorPosition();
                
                // Check if we should merge with the previous undo entry
                boolean shouldMerge = 
                    (now - lastTypingTime < 2000) &&  // Within 2 seconds
                    Math.abs(cursorPos - lastTypingPos) <= 1; // Contiguous edit
                
                // If typing a space or newline, break the undo group
                if (shouldMerge && text.length() > 0 && cursorPos > 0 && cursorPos <= text.length()) {
                    char lastChar = text.charAt(cursorPos - 1);
                    if (Character.isWhitespace(lastChar)) {
                        shouldMerge = false;
                    }
                }
                
                if (shouldMerge) {
                    // Don't add a new undo entry - just update the text
                    // The existing undo entry will remain unchanged
                } else {
                    // Add new undo entry
                    this.undoList.add(new GuiScriptTextArea.UndoData(this.text, selection.getCursorPosition()));
                    this.redoList.clear();
                }
                
                lastTypingTime = now;
                lastTypingPos = cursorPos;
            } else if (!this.undoing) {
                // Normal undo entry (non-atomic)
                this.undoList.add(new GuiScriptTextArea.UndoData(this.text, selection.getCursorPosition()));
                this.redoList.clear();
                
                // Reset atomic undo tracking
                lastTypingTime = System.currentTimeMillis();
                lastTypingPos = selection.getCursorPosition();
            }

            this.text = text;
            //this.container = new TextContainer(text);
            if (this.container == null)
                this.container = new ScriptTextContainer(text);

            this.container.init(text, this.width, this.height);

            if (this.enableCodeHighlighting) 
                this.container.formatCodeText();

            // Ensure scroll state stays in bounds after text change
            int maxScroll = Math.max(0, getPaddedLineCount() - this.container.visibleLines);
            scroll.clampToBounds(maxScroll);

            selection.clamp(this.text.length());

            // Consider text changes user activity to pause caret blinking briefly
            selection.markActivity();
            searchBar.updateMatches();
            
            // Update autocomplete manager with current container
            autocompleteManager.setContainer(this.container);

        }
    }

    public String getText() {
        return this.text;
    }

    public boolean isEnabled() {
        return this.enabled && this.visible;
    }

    public boolean hasVerticalScrollbar() {
        return this.container != null && this.container.visibleLines < getPaddedLineCount();
    }

    public void enableCodeHighlighting() {
        this.enableCodeHighlighting = true;
        this.container.formatCodeText();
    }
    
    /**
     * Set the scripting language for syntax highlighting and type inference.
     * @param language The language name (e.g., "ECMAScript", "Groovy")
     */
    public void setLanguage(String language) {
        if (this.container != null) {
            this.container.setLanguage(language);
            if (this.enableCodeHighlighting) {
                this.container.formatCodeText();
            }
        }
    }
    
    /**
     * Get the current scripting language.
     */
    public String getLanguage() {
        return this.container != null ? this.container.getLanguage() : "ECMAScript";
    }

    /**
     * Set the script context (NPC, PLAYER, BLOCK, ITEM, etc.).
     * This determines which hooks and event types are available for autocomplete.
     *
     * @param context The script context
     */
    public void setScriptContext(ScriptContext context) {
        if (this.container != null) {
            this.container.setScriptContext(context);
        }
    }

    /**
     * Get the current script context.
     *
     * @return The script context (NPC, PLAYER, BLOCK, ITEM, etc.)
     */
    public ScriptContext getScriptContext() {
        return this.container != null ? this.container.getScriptContext() : ScriptContext.GLOBAL;
    }

    public ScriptTextContainer getContainer() {
        return this.container;
    }

    /**
     * Add implicit imports that should be resolved without explicit import statements.
     * Used for JaninoScript default imports and hook parameter types.
     *
     * @param patterns Array of import patterns to add (wildcard packages like "noppes.npcs.api.*" or FQ class names)
     */
    public void addImplicitImports(String... patterns) {
        if (this.container != null) {
            this.container.addImplicitImports(patterns);
        }
    }

    public void setListener(ITextChangeListener listener) {
        this.listener = listener;
    }

    private void clampSelectionBounds() {
        selection.clamp(text != null ? text.length() : 0);
    }
    
    // ==================== AUTOCOMPLETE VISIBILITY ====================
    
    /**
     * Check if a click position is within the bounds of the autocomplete menu.
     * Returns false if autocomplete is not visible.
     */
    public boolean isPointOnAutocompleteMenu(int mouseX, int mouseY) {
        if (autocompleteManager == null || !autocompleteManager.isVisible()) {
            return false;
        }
        
        
        AutocompleteMenu menu = autocompleteManager.getMenu();
        if (menu == null) {
            return false;
        }
        
        int menuX = menu.getX();
        int menuY = menu.getY();
        int menuWidth = menu.getWidth();
        int menuHeight = menu.getHeight();
        
        return mouseX >= menuX && mouseX <= menuX + menuWidth &&
               mouseY >= menuY && mouseY <= menuY + menuHeight;
    }
    
    // ==================== AUTO-IMPORT ====================
    
    /**
     * Add an import statement and sort all imports.
     */
    private void addAndSortImport(String importPath) {
        String currentText = this.text;
        int savedCursorPos = selection.getCursorPosition();
        
        // Find all existing imports
        java.util.regex.Pattern importPattern = java.util.regex.Pattern.compile(
            "(?m)^\\s*import\\s+(?:static\\s+)?([A-Za-z_][A-Za-z0-9_]*(?:\\s*\\.\\s*[A-Za-z_*][A-Za-z0-9_]*)*)\\s*;\\s*$"
        );
        java.util.regex.Matcher matcher = importPattern.matcher(currentText);
        
        java.util.List<ImportEntry> imports = new java.util.ArrayList<>();
        int firstImportStart = -1;
        int lastImportEnd = -1;
        
        while (matcher.find()) {
            String importStatement = matcher.group(0);
            String importPathFound = matcher.group(1).replaceAll("\\s+", "");
            
            if (firstImportStart == -1) {
                firstImportStart = matcher.start();
            }
            lastImportEnd = matcher.end();
            
            // Skip if this is the import we're trying to add
            if (!importPathFound.equals(importPath)) {
                imports.add(new ImportEntry(importPathFound, importStatement.trim()));
            }
        }
        
        // Add the new import
        imports.add(new ImportEntry(importPath, "import " + importPath + ";"));
        
        // Sort imports
        java.util.Collections.sort(imports, new java.util.Comparator<ImportEntry>() {
            @Override
            public int compare(ImportEntry a, ImportEntry b) {
                // Sort order: java.*, javax.*, then others alphabetically
                boolean aIsJava = a.path.startsWith("java.");
                boolean aIsJavax = a.path.startsWith("javax.");
                boolean bIsJava = b.path.startsWith("java.");
                boolean bIsJavax = b.path.startsWith("javax.");
                
                if (aIsJava && !bIsJava) return -1;
                if (!aIsJava && bIsJava) return 1;
                if (aIsJavax && !bIsJavax && !bIsJava) return -1;
                if (!aIsJavax && bIsJavax && !aIsJava) return 1;
                
                return a.path.compareTo(b.path);
            }
        });
        
        // Build the new import block
        StringBuilder importBlock = new StringBuilder();
        String prevPackage = "";
        for (ImportEntry entry : imports) {
            // Add blank line between different top-level packages
            String topPackage = entry.path.contains(".") ? 
                entry.path.substring(0, entry.path.indexOf('.')) : entry.path;
            if (!prevPackage.isEmpty() && !topPackage.equals(prevPackage)) {
                importBlock.append("\n");
            }
            importBlock.append(entry.statement).append("\n");
            prevPackage = topPackage;
        }
        
        // Determine where to insert/replace imports
        String newText;
        int cursorAdjustment = 0;
        
        if (firstImportStart != -1) {
            // Replace existing import block
            String before = currentText.substring(0, firstImportStart);
            String after = currentText.substring(lastImportEnd);
            newText = before + importBlock.toString() + after;
            
            // Adjust cursor if it's after the import block
            int newImportEnd = firstImportStart + importBlock.length();
            if (savedCursorPos >= lastImportEnd) {
                cursorAdjustment = newImportEnd - lastImportEnd;
            }
        } else {
            // No existing imports - add at top after package statement (if any)
            java.util.regex.Pattern packagePattern = java.util.regex.Pattern.compile(
                "(?m)^\\s*package\\s+[A-Za-z_][A-Za-z0-9_.]*\\s*;\\s*$"
            );
            java.util.regex.Matcher pkgMatcher = packagePattern.matcher(currentText);
            
            int insertPos = 0;
            if (pkgMatcher.find()) {
                insertPos = pkgMatcher.end();
                // Add blank line after package
                newText = currentText.substring(0, insertPos) + "\n" + 
                         importBlock.toString() + "\n" + currentText.substring(insertPos);
                cursorAdjustment = importBlock.length() + 2; // +2 for the newlines
            } else {
                // Insert at very beginning
                newText = importBlock.toString() + "\n" + currentText;
                cursorAdjustment = importBlock.length() + 1;
            }
        }
        
        // Apply the changes
        setText(newText);
        selection.reset(savedCursorPos + cursorAdjustment);
        scrollToCursor();
    }

    public void formatCodeText() {
        if (this.enableCodeHighlighting && this.container != null) {
            this.container.formatCodeText();
        }
    }

    /**
     * Helper class for tracking imports during sorting.
     */
    private static class ImportEntry {
        String path;
        String statement;
        
        ImportEntry(String path, String statement) {
            this.path = path;
            this.statement = statement;
        }
    }
    
    // ==================== INNER CLASSES ====================

    public static class UndoData {
        public String text;
        public int cursorPosition;

        public UndoData(String text, int cursorPosition) {
            this.text = text;
            this.cursorPosition = cursorPosition;
        }
    }
}
