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

/**
 * Go To Line:Column dialog for the script editor.
 * Provides IntelliJ-like "Go to Line" functionality (Ctrl+G).
 *
 * Input format: "line" or "line:column"
 * Example: "42" goes to line 42, column 1
 * Example: "42:15" goes to line 42, column 15
 */
public class GoToLineDialog {

    // ==================== TEXTURE ====================
    public static final ResourceLocation TEXTURE = OverlayKeyPresetViewer.TEXTURE;

    // ==================== DIMENSIONS ====================
    private int x, y, width;
    private int dialogWidth = 200;
    private int dialogHeight = 51;
    private int textFieldWidth = 180;
    private int textFieldHeight = 16;
    private int padding = 8;

    // ==================== STATE ====================
    private boolean visible = false;
    private boolean focused = true;

    // ==================== TEXT FIELD ====================
    private String inputText = "";
    private int cursor = 0;
    private int selectionStart = 0;
    private int selectionEnd = 0;
    private int scrollOffset = 0;

    // ==================== CURSOR BLINK ====================
    private int cursorCounter = 0;
    private long lastInputTime = 0;

    // ==================== CALLBACK ====================
    private GoToLineCallback callback;

    private final FontRenderer font = Minecraft.getMinecraft().fontRenderer;

    /**
     * Callback interface for go-to-line operations
     */
    public interface GoToLineCallback {
        /** Get total line count */
        int getLineCount();

        /** Get the column count for a specific line (0-indexed) */
        int getColumnCount(int lineIndex);

        /** Navigate to specific line and column (both 1-indexed for user display) */
        void goToLineColumn(int line, int column);

        /** Called when dialog closes - should restore focus to editor */
        void onDialogClose();

        void unfocusMainEditor();
        
        void focusMainEditor();
    }

    /**
     * Initialize the dialog with callback
     */
    public GoToLineDialog(GoToLineCallback callback) {
        this.callback = callback;
    }

    /**
     * Initialize/update position for the dialog
     */
    public void initGui(int guiX, int guiY, int guiWidth) {
        this.width = guiWidth;
        // Center the dialog horizontally
        this.x = guiX + (guiWidth - dialogWidth) / 2;
        // Position near top of editor
        this.y = guiY + 40;
    }

    // ==================== VISIBILITY ====================

    public boolean isVisible() {
        return visible;
    }

    public void show() {
        visible = true;
        focused = true;
        inputText = "";
        cursor = 0;
        selectionStart = 0;
        selectionEnd = 0;
        scrollOffset = 0;
        markActivity();
        if (callback != null) callback.unfocusMainEditor();
    }

    public void close() {
        visible = false;
        focused = false;
        if (callback != null) {
            callback.onDialogClose();
            callback.focusMainEditor();
        }
    }

    public void toggle() {
        if (visible) {
            close();
        } else {
            show();
        }
    }

    public boolean hasFocus() {
        return visible && focused;
    }

    // ==================== RENDERING ====================

    public void draw(int mouseX, int mouseY) {
        if (!visible)
            return;

        ParseResult result = null;
        if (!inputText.isEmpty())
            result = parseInput(inputText);

        GL11.glPushMatrix();
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Draw dialog background with shadow
        drawDialogBackground(result != null && !result.valid);

        // Draw title
        String title = "Go to Line";
        font.drawString(title, x + padding + 1, y + padding + 4, 0xFFe0e0e0);

        // Draw hint text showing valid range
        int lineCount = callback != null ? callback.getLineCount() : 0;
        String hint = "[" + lineCount + " lines]";
        int hintWidth = font.getStringWidth(hint);
        dialogWidth = 200;
        font.drawString(hint, x + dialogWidth - padding - hintWidth, y + padding + 4, 0xFF888888);


        // Draw text field
        int fieldX = x + padding + 0;
        int fieldY = y + padding + 14 + 5;
        textFieldWidth = dialogWidth - padding * 2;
        drawTextField(fieldX, fieldY, textFieldWidth, textFieldHeight, inputText, cursor,
                selectionStart, selectionEnd, scrollOffset, true);

        if (inputText.isEmpty())
            font.drawString("[Line][:Column]", x + padding + 6, y + padding + 23, 0xFF888888);

        // Draw error/validation hint if input is invalid
        if (result != null && !result.valid)
            font.drawString(result.error, x + padding + 2, y + dialogHeight - 3, 0xFFff6666);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    private void drawDialogBackground(boolean resultInvalid) {
        int dialogHeight = this.dialogHeight;
        if (resultInvalid)
            dialogHeight += 5; // Pad by 6

        int y = this.y + 5;
        // Draw shadow
        Gui.drawRect(x + 2, y + 2, x + dialogWidth + 2, y + dialogHeight + 2, 0x80000000);

        // Draw main background
        Gui.drawRect(x, y, x + dialogWidth, y + dialogHeight, 0xFF2d2d30);

        // Draw border
        Gui.drawRect(x, y, x + dialogWidth, y + 1, 0xFF3c3c3c);
        Gui.drawRect(x, y + dialogHeight - 1, x + dialogWidth, y + dialogHeight, 0xFF3c3c3c);
        Gui.drawRect(x, y, x + 1, y + dialogHeight, 0xFF3c3c3c);
        Gui.drawRect(x + dialogWidth - 1, y, x + dialogWidth, y + dialogHeight, 0xFF3c3c3c);
    }

    private void drawTextField(int fx, int fy, int fw, int fh, String text, int cursor,
                               int selStart, int selEnd, int scrollOff, boolean isFocused) {
        // Draw text field background
        Gui.drawRect(fx, fy, fx + fw, fy + fh, 0xFF1e1e1e);
        Gui.drawRect(fx, fy, fx + fw, fy + 1, isFocused ? 0xFF007acc : 0xFF3c3c3c);
        Gui.drawRect(fx, fy + fh - 1, fx + fw, fy + fh, isFocused ? 0xFF007acc : 0xFF3c3c3c);
        Gui.drawRect(fx, fy, fx + 1, fy + fh, isFocused ? 0xFF007acc : 0xFF3c3c3c);
        Gui.drawRect(fx + fw - 1, fy, fx + fw, fy + fh, isFocused ? 0xFF007acc : 0xFF3c3c3c);

        // Calculate visible area
        int textX = fx + 4;
        int textY = fy + (fh - 8) / 2;
        int visibleWidth = fw - 8;

        // Apply scroll offset
        String visibleText = text;
        int adjustedCursor = cursor;
        int adjustedSelStart = selStart;
        int adjustedSelEnd = selEnd;

        if (scrollOff > 0 && scrollOff < text.length()) {
            visibleText = text.substring(scrollOff);
            adjustedCursor = cursor - scrollOff;
            adjustedSelStart = Math.max(0, selStart - scrollOff);
            adjustedSelEnd = Math.max(0, selEnd - scrollOff);
        }

        // Draw selection highlight
        if (adjustedSelStart != adjustedSelEnd) {
            int minSel = Math.min(adjustedSelStart, adjustedSelEnd);
            int maxSel = Math.max(adjustedSelStart, adjustedSelEnd);
            String beforeSel = visibleText.substring(0, Math.min(minSel, visibleText.length()));
            String inSel = visibleText.substring(Math.min(minSel, visibleText.length()),
                    Math.min(maxSel, visibleText.length()));
            int selX = textX + font.getStringWidth(beforeSel);
            int selW = font.getStringWidth(inSel);
            Gui.drawRect(selX, textY - 1, selX + selW, textY + 9, 0xFF264f78);
        }

        // Draw text
        font.drawString(visibleText, textX, textY, 0xFFe0e0e0);

        // Draw cursor
        if (isFocused && shouldShowCursor()) {
            String beforeCursor = adjustedCursor > 0 && adjustedCursor <= visibleText.length()
                    ? visibleText.substring(0, adjustedCursor) : "";
            int cursorX = textX + font.getStringWidth(beforeCursor);
            Gui.drawRect(cursorX, textY - 1, cursorX + 1, textY + 9, 0xFFe0e0e0);
        }
    }

    // ==================== INPUT HANDLING ====================

    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (!visible)
            return false;

        // Check if click is inside dialog
        if (mouseX >= x && mouseX < x + dialogWidth && mouseY >= y && mouseY < y + dialogHeight) {
            // Check if click is in text field
            int fieldX = x + padding;
            int fieldY = y + padding + 14;
            if (mouseX >= fieldX && mouseX < fieldX + textFieldWidth &&
                    mouseY >= fieldY && mouseY < fieldY + textFieldHeight) {
                focused = true;
                if (callback != null) callback.unfocusMainEditor();
                // Position cursor based on click
                int clickX = mouseX - fieldX - 4;
                String visibleText = scrollOffset > 0 && scrollOffset < inputText.length()
                        ? inputText.substring(scrollOffset) : inputText;
                int newCursor = getCharIndexAtX(visibleText, clickX) + scrollOffset;
                cursor = Math.max(0, Math.min(newCursor, inputText.length()));
                selectionStart = cursor;
                selectionEnd = cursor;
                markActivity();
            }
            return true;
        }

        // Click outside dialog - close it
        close();
        return false;
    }

    private int getCharIndexAtX(String text, int targetX) {
        int x = 0;
        for (int i = 0; i < text.length(); i++) {
            int charWidth = font.getCharWidth(text.charAt(i));
            if (x + charWidth / 2 > targetX) {
                return i;
            }
            x += charWidth;
        }
        return text.length();
    }

    public boolean keyTyped(char c, int keyCode) {
        if (!visible || !focused)
            return false;

        boolean ctrl = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
        boolean shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);

        // Escape - close dialog
        if (keyCode == Keyboard.KEY_ESCAPE) {
            close();
            return true;
        }

        // Enter - execute go to line
        if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
            executeGoToLine();
            return true;
        }

        // Select all (Ctrl+A)
        if (ctrl && keyCode == Keyboard.KEY_A) {
            selectionStart = 0;
            selectionEnd = inputText.length();
            cursor = inputText.length();
            markActivity();
            return true;
        }

        // Copy (Ctrl+C)
        if (ctrl && keyCode == Keyboard.KEY_C) {
            if (selectionStart != selectionEnd) {
                int minSel = Math.min(selectionStart, selectionEnd);
                int maxSel = Math.max(selectionStart, selectionEnd);
                noppes.npcs.NoppesStringUtils.setClipboardContents(inputText.substring(minSel, maxSel));
            }
            return true;
        }

        // Cut (Ctrl+X)
        if (ctrl && keyCode == Keyboard.KEY_X) {
            if (selectionStart != selectionEnd) {
                int minSel = Math.min(selectionStart, selectionEnd);
                int maxSel = Math.max(selectionStart, selectionEnd);
                noppes.npcs.NoppesStringUtils.setClipboardContents(inputText.substring(minSel, maxSel));
                inputText = inputText.substring(0, minSel) + inputText.substring(maxSel);
                cursor = minSel;
                selectionStart = cursor;
                selectionEnd = cursor;
                updateScrollOffset();
                markActivity();
            }
            return true;
        }

        // Paste (Ctrl+V)
        if (ctrl && keyCode == Keyboard.KEY_V) {
            String clipboard = noppes.npcs.NoppesStringUtils.getClipboardContents();
            if (clipboard != null) {
                // Only allow digits and colon
                StringBuilder filtered = new StringBuilder();
                for (char ch : clipboard.toCharArray()) {
                    if (Character.isDigit(ch) || ch == ':') {
                        filtered.append(ch);
                    }
                }
                insertText(filtered.toString());
            }
            return true;
        }

        // Backspace
        if (keyCode == Keyboard.KEY_BACK) {
            if (selectionStart != selectionEnd) {
                deleteSelection();
            } else if (cursor > 0) {
                inputText = inputText.substring(0, cursor - 1) + inputText.substring(cursor);
                cursor--;
                selectionStart = cursor;
                selectionEnd = cursor;
                updateScrollOffset();
            }
            markActivity();
            return true;
        }

        // Delete
        if (keyCode == Keyboard.KEY_DELETE) {
            if (selectionStart != selectionEnd) {
                deleteSelection();
            } else if (cursor < inputText.length()) {
                inputText = inputText.substring(0, cursor) + inputText.substring(cursor + 1);
            }
            markActivity();
            return true;
        }

        // Left arrow
        if (keyCode == Keyboard.KEY_LEFT) {
            if (shift) {
                if (cursor > 0) {
                    cursor--;
                    selectionEnd = cursor;
                }
            } else {
                if (selectionStart != selectionEnd) {
                    cursor = Math.min(selectionStart, selectionEnd);
                } else if (cursor > 0) {
                    cursor--;
                }
                selectionStart = cursor;
                selectionEnd = cursor;
            }
            updateScrollOffset();
            markActivity();
            return true;
        }

        // Right arrow
        if (keyCode == Keyboard.KEY_RIGHT) {
            if (shift) {
                if (cursor < inputText.length()) {
                    cursor++;
                    selectionEnd = cursor;
                }
            } else {
                if (selectionStart != selectionEnd) {
                    cursor = Math.max(selectionStart, selectionEnd);
                } else if (cursor < inputText.length()) {
                    cursor++;
                }
                selectionStart = cursor;
                selectionEnd = cursor;
            }
            updateScrollOffset();
            markActivity();
            return true;
        }

        // Home
        if (keyCode == Keyboard.KEY_HOME) {
            if (shift) {
                selectionEnd = 0;
                cursor = 0;
            } else {
                cursor = 0;
                selectionStart = cursor;
                selectionEnd = cursor;
            }
            updateScrollOffset();
            markActivity();
            return true;
        }

        // End
        if (keyCode == Keyboard.KEY_END) {
            if (shift) {
                selectionEnd = inputText.length();
                cursor = inputText.length();
            } else {
                cursor = inputText.length();
                selectionStart = cursor;
                selectionEnd = cursor;
            }
            updateScrollOffset();
            markActivity();
            return true;
        }

        // Type character - only allow digits and colon
        if (ChatAllowedCharacters.isAllowedCharacter(c)) {
            if (Character.isDigit(c) || c == ':') {
                insertText(String.valueOf(c));
                markActivity();
                return true;
            }
        }

        return true; // Consume all other keys when dialog is focused
    }

    private void insertText(String text) {
        if (selectionStart != selectionEnd) {
            deleteSelection();
        }
        inputText = inputText.substring(0, cursor) + text + inputText.substring(cursor);
        cursor += text.length();
        selectionStart = cursor;
        selectionEnd = cursor;
        updateScrollOffset();
    }

    private void deleteSelection() {
        int minSel = Math.min(selectionStart, selectionEnd);
        int maxSel = Math.max(selectionStart, selectionEnd);
        inputText = inputText.substring(0, minSel) + inputText.substring(maxSel);
        cursor = minSel;
        selectionStart = cursor;
        selectionEnd = cursor;
        updateScrollOffset();
    }

    private void updateScrollOffset() {
        int fieldWidth = textFieldWidth - 8;
        String beforeCursor = cursor > 0 ? inputText.substring(0, cursor) : "";
        int cursorX = font.getStringWidth(beforeCursor);

        // Scroll to keep cursor visible
        if (cursorX - scrollOffset * 6 > fieldWidth) {
            scrollOffset = Math.max(0, cursor - fieldWidth / 6);
        } else if (cursor < scrollOffset) {
            scrollOffset = cursor;
        }
    }

    private void executeGoToLine() {
        ParseResult result = parseInput(inputText);
        if (result.valid && callback != null) {
            callback.goToLineColumn(result.line, result.column);
            close();
        }
    }

    // ==================== PARSING ====================

    private static class ParseResult {
        boolean valid;
        int line;
        int column;
        String error;

        static ParseResult invalid(String error) {
            ParseResult r = new ParseResult();
            r.valid = false;
            r.error = error;
            return r;
        }

        static ParseResult of(int line, int column) {
            ParseResult r = new ParseResult();
            r.valid = true;
            r.line = line;
            r.column = column;
            return r;
        }
    }

    private ParseResult parseInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            return ParseResult.invalid("Enter line number");
        }

        input = input.trim();
        int line, column = 1;

        int colonIndex = input.indexOf(':');
        if (colonIndex >= 0) {
            // Format: line:column
            String linePart = input.substring(0, colonIndex);
            String colPart = input.substring(colonIndex + 1);

            if (linePart.isEmpty()) {
                return ParseResult.invalid("Invalid line number");
            }

            try {
                line = Integer.parseInt(linePart);
            } catch (NumberFormatException e) {
                return ParseResult.invalid("Invalid line number");
            }

            if (!colPart.isEmpty()) {
                try {
                    column = Integer.parseInt(colPart);
                } catch (NumberFormatException e) {
                    return ParseResult.invalid("Invalid column number");
                }
            }
        } else {
            // Format: just line
            try {
                line = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                return ParseResult.invalid("Invalid line number");
            }
        }

        // Validate ranges
        int lineCount = callback != null ? callback.getLineCount() : 0;
        if (line < 1) {
            return ParseResult.invalid("Line must be >= 1");
        }
        if (line > lineCount) {
            return ParseResult.invalid("Line must be <= " + lineCount);
        }
        if (column < 1) {
            return ParseResult.invalid("Column must be >= 1");
        }
        // Column validation happens at callback - depends on actual line length

        return ParseResult.of(line, column);
    }

    // ==================== CURSOR BLINK ====================

    public void updateCursor() {
        cursorCounter++;
    }

    private void markActivity() {
        lastInputTime = System.currentTimeMillis();
    }

    private boolean shouldShowCursor() {
        // Keep cursor visible for 500ms after any input
        if (System.currentTimeMillis() - lastInputTime < 500) {
            return true;
        }
        // Then blink every 500ms
        return (cursorCounter / 10) % 2 == 0;
    }

    // ==================== GETTERS ====================

    public int getTotalHeight() {
        return visible ? dialogHeight + 10 : 0;
    }
}