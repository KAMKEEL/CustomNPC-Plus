package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ChatAllowedCharacters;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.gui.util.script.JavaTextContainer;
import noppes.npcs.client.gui.util.script.JavaTextContainer.LineData;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;

import static net.minecraft.client.gui.GuiScreen.isCtrlKeyDown;

public class GuiScriptTextArea extends GuiNpcTextField {
    public int id;
    public int x;
    public int y;
    public int width;
    public int height;
    private int cursorCounter;
    private long lastInputTime = 0L;
    private ITextChangeListener listener;
    public String text = null;
    private JavaTextContainer container = null;
    public boolean active = false;
    public boolean enabled = true;
    public boolean visible = true;
    public boolean clicked = false;
    public boolean doubleClicked = false;
    public boolean tripleClicked = false;
    public boolean clickScrolling = false;
    private int clickCount = 0;
    private int startSelection;
    private int endSelection;
    private int cursorPosition;
    private int scrolledLine = 0;
    // Smooth scrolling state: fractional line position and target position
    private double scrollPos = 0.0;
    private double targetScroll = 0.0;
    // velocity used for spring-based easing
    private double scrollVelocity = 0.0;
    // last timestamp used for scroll integration (ms)
    private long lastScrollTime = 0L;
    private boolean enableCodeHighlighting = false;
    public List<GuiScriptTextArea.UndoData> undoList = new ArrayList();
    public List<GuiScriptTextArea.UndoData> redoList = new ArrayList();
    public boolean undoing = false;
    private long lastClicked = 0L;
    // private static TrueTypeFont font = new TrueTypeFont(new Font("Arial Unicode MS", Font.PLAIN, ConfigClient.FontSize), 1);

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

    public void drawTextBox(int xMouse, int yMouse) {
        if (!visible)
            return;
        clampSelectionBounds();
        drawRect(x - 1, y - 1, x + width + 1, y + height + 1, 0xffa0a0a0);
        drawRect(x, y, x + width, y + height, 0xff000000); //THIS IS THE VIEWPORT

        // Enable scissor test to clip drawing to the viewport rectangle
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int scaleFactor = sr.getScaleFactor();
        int scissorX = this.x * scaleFactor;
        int scissorY = (sr.getScaledHeight() - (this.y + this.height)) * scaleFactor;
        int scissorW = this.width * scaleFactor;
        int scissorH = this.height * scaleFactor;
        GL11.glScissor(scissorX, scissorY, scissorW, scissorH);
        
        container.visibleLines = height / container.lineHeight;

        int maxScroll = Math.max(0, this.container.linesCount - container.visibleLines);
        if (listener instanceof GuiNPCInterface) {
            int k2 = ((GuiNPCInterface) listener).mouseScroll = Mouse.getDWheel();
            if (k2 != 0) {
                // update target scroll smoothly instead of jumping
                double sign = Math.copySign(1, k2);
                targetScroll -= sign * 1.0; // one line per wheel tick
                if (targetScroll < 0) targetScroll = 0;
                if (targetScroll > maxScroll) targetScroll = maxScroll;
            }
        }

        if (clicked) {
            clicked = Mouse.isButtonDown(0);
            int i = getSelectionPos(xMouse, yMouse);
            if (i != cursorPosition) {
                if (doubleClicked || tripleClicked) {
                    startSelection = endSelection = cursorPosition;
                    doubleClicked = false;
                    tripleClicked = false;
                }
                setCursor(i, true);
            }
        } else if (doubleClicked || tripleClicked) {
            doubleClicked = false;
            tripleClicked = false;
        }

        if (clickScrolling) {
            clickScrolling = Mouse.isButtonDown(0);
            int diff = container.linesCount - container.visibleLines;
            double target = Math.min(Math.max(1f * diff * (yMouse - y) / height, 0f), (float) diff);
            targetScroll = target;
        }

        // Animate scrollPos towards targetScroll using exponential smoothing
        long nowMs = System.currentTimeMillis();
        if (scrollPos < 0) {
            scrollPos = scrolledLine; // initialize
            lastScrollTime = nowMs;
        }
        double dt = Math.min(0.05, (nowMs - lastScrollTime) / 1000.0); // clamp dt for stability
        lastScrollTime = nowMs;

        double dist = targetScroll - scrollPos;
        // If close enough, snap to target to avoid long slow tail
        if (Math.abs(dist) < 0.01) {
            scrollPos = targetScroll;
            scrollVelocity = 0.0;
        } else {
            // Time constant controlling speed (seconds). Smaller -> faster snap.
            final double tau = 0.025; // ~55ms time constant feels snappy
            double alpha = 1.0 - Math.exp(-dt / Math.max(1e-6, tau));
            double prev = scrollPos;
            scrollPos += dist * alpha;
            scrollVelocity = (scrollPos - prev) / (dt > 0 ? dt : 1e-6);
            // Clamp overshoot
            if ((dist > 0 && scrollPos > targetScroll) || (dist < 0 && scrollPos < targetScroll)) {
                scrollPos = targetScroll;
                scrollVelocity = 0.0;
            }
        }
        // Update integer scrolledLine for compatibility
        scrolledLine = Math.max(0, Math.min((int) Math.floor(scrollPos), maxScroll));
        double fracOffset = scrollPos - scrolledLine;
        int startBracket = 0, endBracket = 0;
        if (startSelection >= 0 && text != null && text.length() > 0 &&
                (endSelection - startSelection == 1 || startSelection == endSelection)) {
            int[] span = findBracketSpanAt(startSelection);
            if (span != null) {
                startBracket = span[0];
                endBracket = span[1];
            }
        }

        List<JavaTextContainer.LineData> list = new ArrayList<>(container.lines);

        // Build brace spans: {origDepth, open line, close line, adjustedDepth}
        List<int[]> braceSpans = computeBraceSpans(text, list);

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
        if (startSelection != endSelection) {
            Matcher m = container.regexWord.matcher(text);
            while (m.find()) {
                if (m.start() == startSelection && m.end() == endSelection) {
                    wordHightLight = text.substring(startSelection, endSelection);
                }
            }
        }
        // Expand render range by one line above/below so partially-visible lines are drawn
        int renderStart = Math.max(0, (int) Math.floor(scrollPos) - 1);
        int renderEnd = Math.min(list.size() - 1, (int) Math.ceil(scrollPos + container.visibleLines) + 1);
        for (int i = renderStart; i <= renderEnd; i++) {
            LineData data = list.get(i);
            String line = data.text;
            int w = line.length();
            // compute Y using fractional scrollPos so animation is smooth
            int posY = y + (int) Math.round((i - scrollPos) * container.lineHeight);
            if (i >= renderStart && i <= renderEnd) {
                //Highlight braces the cursor position is on
                if (startBracket != endBracket) {
                    if (startBracket >= data.start && startBracket < data.end) {
                        int s = ClientProxy.Font.width(line.substring(0, startBracket - data.start));
                        int e = ClientProxy.Font.width(line.substring(0, startBracket - data.start + 1)) + 1;
                        drawRect(x + 1 + s, posY, x + 1 + e, posY + container.lineHeight + 0, 0x9900cc00);
                    }
                    if (endBracket >= data.start && endBracket < data.end) {
                        int s = ClientProxy.Font.width(line.substring(0, endBracket - data.start));
                        int e = ClientProxy.Font.width(line.substring(0, endBracket - data.start + 1)) + 1;
                        drawRect(x + 1 + s, posY, x + 1 + e, posY + container.lineHeight + 0, 0x9900cc00);
                    }
                }
                //Highlight words
                if (wordHightLight != null) {
                    Matcher m = container.regexWord.matcher(line);
                    while (m.find()) {
                        if (line.substring(m.start(), m.end()).equals(wordHightLight)) {
                            int s = ClientProxy.Font.width(line.substring(0, m.start()));
                            int e = ClientProxy.Font.width(line.substring(0, m.end())) + 1;
                            drawRect(x + 1 + s, posY, x + 1 + e, posY + container.lineHeight, 0x99004c00);
                        }
                    }
                }
                // Highlight the current line (light gray) under any selection
                if (active && isEnabled() && (cursorPosition >= data.start && cursorPosition < data.end || (i == list.size() - 1 && cursorPosition == text.length()))) {
                    drawRect(x + 0, posY, x + width - 1, posY + container.lineHeight, 0x22e0e0e0);
                }
                // Highlight selection
                if (startSelection != endSelection && endSelection > data.start && startSelection <= data.end) {
                    if (startSelection < data.end) {
                        int s = ClientProxy.Font.width(line.substring(0, Math.max(startSelection - data.start, 0)));
                        int e = ClientProxy.Font.width(line.substring(0, Math.min(endSelection - data.start, w))) + 1;
                        drawRect(x + 1 + s, posY, x + 1 + e, posY + container.lineHeight, 0x992172ff);
                    }
                }
                int yPos = posY + 1;

                // Draw indent guides once per visible block based on brace spans
                if (i == Math.max(0, (int) Math.floor(scrollPos)) && !braceSpans.isEmpty()) {
                    int visStart = Math.max(0, (int) Math.floor(scrollPos));
                    int visEnd = Math.min(list.size() - 1, visStart + container.visibleLines - 1);
                    for (int[] span : braceSpans) {
                        int originalDepth = span[0];
                        int openLine = span[1];
                        int closeLine = span[2];
                        int depth = span.length > 3 ? span[3] : originalDepth;
                        // Skip top-level (depth 1) using the original depth to avoid hiding nested guides when adjusted
                        //if (originalDepth <= 1)
                        //continue;
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
                        int gx = x + 4 + px - 2; // shift left ~2px for the IntelliJ feel

                        int topY = y + (int) Math.round((drawStart - scrollPos) * container.lineHeight);
                        int bottomY = y + (int) Math.round((drawEnd - scrollPos + 1) * container.lineHeight) - 2;
                            int guideColor = (openLine == highlightedOpenLine && closeLine == highlightedCloseLine) ? 0x9933cc00 : 0x33FFFFFF;
                            drawRect(gx, topY, gx + 1, bottomY, guideColor);
                    }
                }
                data.drawString(x + 1, yPos, 0xFFe0e0e0);

                // Draw cursor: pause blinking while user is active recently
                boolean recentInput = System.currentTimeMillis() - this.lastInputTime < 500;
                if (active && isEnabled() && (recentInput || (cursorCounter / 10) % 2 == 0) && (cursorPosition >= data.start && cursorPosition < data.end || (i == list.size() - 1 && cursorPosition == text.length()))) {
                    int posX = x + ClientProxy.Font.width(
                            line.substring(0, Math.min(cursorPosition - data.start, line.length())));
                    drawRect(posX + 1, yPos -1, posX + 2, yPos - 1 + container.lineHeight, 0xffffffff);
                }
            }
        }

        if (hasVerticalScrollbar()) {
            Minecraft.getMinecraft().renderEngine.bindTexture(GuiCustomScroll.resource);
            int sbSize = Math.max((int) (1f * container.visibleLines / container.linesCount * height), 2);

            int posX = x + width - 6;
            double linesCount = Math.max(1, (double) container.linesCount);
            int posY = (int) (y + 1f * scrollPos / linesCount * (height - 4)) + 1;

            drawRect(posX, posY, posX + 5, posY + sbSize, 0xFFe0e0e0);
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }
    // Find a bracket span for a given cursor position. Returns {start, end} or null.
    private int[] findBracketSpanAt(int pos) {
        if (text == null || text.isEmpty()) return null;
        // First try at pos (caret before the char)
        if (pos >= 0 && pos < text.length()) {
            char c = text.charAt(pos);
            int found = 0;
            if (c == '{') found = findClosingBracket(text.substring(pos), '{', '}');
            else if (c == '[') found = findClosingBracket(text.substring(pos), '[', ']');
            else if (c == '(') found = findClosingBracket(text.substring(pos), '(', ')');
            else if (c == '}') found = findOpeningBracket(text.substring(0, pos + 1), '{', '}');
            else if (c == ']') found = findOpeningBracket(text.substring(0, pos + 1), '[', ']');
            else if (c == ')') found = findOpeningBracket(text.substring(0, pos + 1), '(', ')');
            if (found != 0) return new int[]{pos, pos + found};
        }

        // Scan backwards from just before the caret, skipping spaces/tabs on the same line,
        // to find a nearby bracket that should be treated as "immediately before" the caret.
        if (pos > 0) {
            int scan = pos - 1;
            // Move left over spaces/tabs but do not cross a newline
            while (scan >= 0) {
                char sc = text.charAt(scan);
                if (sc == ' ' || sc == '\t') {scan--;continue;}
                if (sc == '\n') {scan = -1; // stop, do not cross line boundary
                     break;
                }

                int found2 = 0;
                if (sc == '{') found2 = findClosingBracket(text.substring(scan), '{', '}');
                else if (sc == '[') found2 = findClosingBracket(text.substring(scan), '[', ']');
                else if (sc == '(') found2 = findClosingBracket(text.substring(scan), '(', ')');
                else if (sc == '}') found2 = findOpeningBracket(text.substring(0, scan + 1), '{', '}');
                else if (sc == ']') found2 = findOpeningBracket(text.substring(0, scan + 1), '[', ']');
                else if (sc == ')') found2 = findOpeningBracket(text.substring(0, scan + 1), '(', ')');

                if (found2 != 0) return new int[]{scan, scan + found2};
                // Not a bracket; stop scanning further
                break;
            }
        }

        return null;
    }
    private int findClosingBracket(String str, char s, char e) {
        int found = 0;
        char[] chars = str.toCharArray();

        for (int i = 0; i < chars.length; ++i) {
            char c = chars[i];
            if (c == s) {
                ++found;
            } else if (c == e) {
                --found;
                if (found == 0) {
                    return i;
                }
            }
        }

        return 0;
    }

    private int findOpeningBracket(String str, char s, char e) {
        int found = 0;
        char[] chars = str.toCharArray();

        for (int i = chars.length - 1; i >= 0; --i) {
            char c = chars[i];
            if (c == e) {
                ++found;
            } else if (c == s) {
                --found;
                if (found == 0) {
                    return i - chars.length + 1;
                }
            }
        }

        return 0;
    }

    // Computes brace spans as {originalDepth, openLineIndex, closeLineIndex, adjustedDepth}, inclusive of brace lines
    private List<int[]> computeBraceSpans(String fullText, List<LineData> lines) {
        List<int[]> spans = new ArrayList<>();
        if (fullText == null || fullText.isEmpty() || lines == null || lines.isEmpty())
            return spans;

        int lineIdx = 0;
        int lineEnd = lines.get(0).end;
        List<Integer> openStack = new ArrayList<>(); // line indices of unmatched opens

        boolean inLineComment = false;
        boolean inBlockComment = false;
        boolean inString = false;
        boolean escape = false;
        char stringDelimiter = 0;

        for (int pos = 0; pos < fullText.length(); pos++) {
            while (lineIdx < lines.size() - 1 && pos >= lineEnd) {
                lineIdx++;
                lineEnd = lines.get(lineIdx).end;
            }

            char c = fullText.charAt(pos);
            char next = pos + 1 < fullText.length() ? fullText.charAt(pos + 1) : 0;

            if (inString) {
                if (escape) {
                    escape = false;
                } else if (c == '\\') {
                    escape = true;
                } else if (c == stringDelimiter) {
                    inString = false;
                } else if (c == '\n') {
                    // Treat unterminated strings as ending at EOL to avoid breaking later brace parsing
                    inString = false;
                }
                continue;
            }

            if (inBlockComment) {
                if (c == '*' && next == '/') {
                    inBlockComment = false;
                    pos++;
                }
                continue;
            }

            if (inLineComment) {
                if (c == '\n') {
                    inLineComment = false;
                }
                continue;
            }

            if (c == '/' && next == '/') {
                inLineComment = true;
                pos++;
                continue;
            }
            if (c == '/' && next == '*') {
                inBlockComment = true;
                pos++;
                continue;
            }
            if (c == '"' || c == '\'') {
                inString = true;
                stringDelimiter = c;
                escape = false;
                continue;
            }

            if (c == '{') {
                openStack.add(lineIdx);
            } else if (c == '}') {
                if (!openStack.isEmpty()) {
                    int openLine = openStack.remove(openStack.size() - 1);
                    int spanDepth = openStack.size() + 1;
                    int closeLine = lineIdx;
                    spans.add(new int[]{spanDepth, openLine, closeLine});
                }
            }
        }

        // Normalize depths to reduce impact of unmatched opens without hiding nested guides
        if (!openStack.isEmpty()) {
            int baseline = openStack.size();
            List<int[]> adjusted = new ArrayList<>(spans.size());
            for (int[] span : spans) {
                int adjustedDepth = Math.max(1, span[0] - baseline);
                adjusted.add(new int[]{span[0], span[1], span[2], adjustedDepth});
            }
            spans = adjusted;
        } else {
            for (int idx = 0; idx < spans.size(); idx++) {
                int[] span = spans.get(idx);
                spans.set(idx, new int[]{span[0], span[1], span[2], span[0]});
            }
        }

        return spans;
    }

    private int getSelectionPos(int xMouse, int yMouse) {
        xMouse -= this.x + 1;
        yMouse -= this.y + 1;
        ArrayList list = new ArrayList(this.container.lines);

        for (int i = 0; i < list.size(); ++i) {
            LineData data = (LineData) list.get(i);
            if (i >= this.scrolledLine && i < this.scrolledLine + this.container.visibleLines) {
                int yPos = (i - this.scrolledLine) * this.container.lineHeight;
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

    @Override
    public boolean textboxKeyTyped(char c, int i) {
        if (!active)
            return false;

        if (this.isKeyComboCtrlA(i)) {
            startSelection = cursorPosition = 0;
            endSelection = text.length();
            return true;
        }

        if (!isEnabled())
            return false;

        LineData currentLine = null;
        for (LineData line : this.container.lines) {
            if (startSelection >= line.start && startSelection < line.end) {
                currentLine = line;
                break;
            }
        }

        String original = text;
        if (i == Keyboard.KEY_LEFT) {
            int j = 1;
            if (isCtrlKeyDown()) {
                Matcher m = container.regexWord.matcher(text.substring(0, cursorPosition));
                while (m.find()) {
                    if (m.start() == m.end())
                        continue;
                    j = cursorPosition - m.start();
                }
            }
            int newPos = Math.max(cursorPosition - j, 0);
            setCursor(newPos, GuiScreen.isShiftKeyDown());
            return true;
        }
        if (i == Keyboard.KEY_RIGHT) {
            int j = 1;
            if (isCtrlKeyDown()) {
                Matcher m = container.regexWord.matcher(text.substring(cursorPosition));
                if (m.find() && m.start() > 0 || m.find()) {
                    j = m.start();
                }
            }
            int newPos = Math.min(cursorPosition + j, text.length());
            setCursor(newPos, GuiScreen.isShiftKeyDown());
            return true;
        }
        if (i == Keyboard.KEY_UP) {
            setCursor(cursorUp(), GuiScreen.isShiftKeyDown());
            return true;
        }
        if (i == Keyboard.KEY_DOWN) {
            setCursor(cursorDown(), GuiScreen.isShiftKeyDown());
            return true;
        }
        if (i == Keyboard.KEY_DELETE) {
            String s = getSelectionAfterText();
            if (!s.isEmpty() && startSelection == endSelection)
                s = s.substring(1);
            setText(getSelectionBeforeText() + s);
            endSelection = cursorPosition = startSelection;
            return true;
        }
        if (isKeyComboCtrlBackspace(i)) {
            String s = getSelectionBeforeText();
            if (startSelection > 0 && startSelection == endSelection) {
                int nearestCondition = cursorPosition;
                int g;
                boolean cursorInWhitespace = Character.isWhitespace(s.charAt(cursorPosition - 1));
                if (cursorInWhitespace) {
                    // Find the nearest word if we are starting in whitespace
                    for (g = cursorPosition - 1; g >= 0; g--) {
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
                    // Find the nearest blank space or new line
                    for (g = cursorPosition - 1; g >= 0; g--) {
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

                // Remove all text to the left up to the nearest boundary
                s = s.substring(0, nearestCondition);
                startSelection -= (cursorPosition - nearestCondition);
            }
            setText(s + getSelectionAfterText());
            endSelection = cursorPosition = startSelection;
            return true;
        }
        if (i == Keyboard.KEY_BACK) {
            // Handle selection deletion first
            if (startSelection != endSelection) {
                String s = getSelectionBeforeText();
                setText(s + getSelectionAfterText());
                endSelection = cursorPosition = startSelection;
                return true;
            }
            
            // Nothing to delete if at start
            if (startSelection <= 0) {
                return true;
            }
            
            // Find current line for indent-aware backspace
            LineData curr = null;
            for (LineData line : container.lines) {
                if (cursorPosition >= line.start && cursorPosition < line.end) {
                    curr = line;
                    break;
                }
            }
            
            // Indent-aware backspace: if cursor is at or before the EXPECTED indent, merge with previous line
            // If cursor is after expected indent (even in extra whitespace), do normal backspace
            if (curr != null && curr.start > 0) {
                int col = cursorPosition - curr.start;
                int actualIndent = getLineIndent(curr.text);
                int expectedIndent = getExpectedIndent(curr);
                
                // Only trigger smart merge if cursor is at or before the expected indent position
                if (col <= expectedIndent) {
                    boolean lineHasContent = curr.text.trim().length() > 0;
                    int newlinePos = curr.start - 1;  // Position of newline before this line
                    
                    if (!lineHasContent) {
                        // Empty/whitespace-only line: remove the entire line including its trailing newline
                        int removeEnd = text.indexOf('\n', curr.start);
                        if (removeEnd == -1) {
                            removeEnd = text.length();
                        } else {
                            removeEnd = removeEnd + 1;  // Include the newline
                        }
                        String before = text.substring(0, curr.start);
                        String after = removeEnd <= text.length() ? text.substring(removeEnd) : "";
                        setText(before + after);
                        // Cursor goes to end of previous line (at the newline position, which is now end of prev line content)
                        int newCursor = Math.max(0, curr.start - 1);
                        endSelection = cursorPosition = startSelection = newCursor;
                        return true;
                    } else {
                        // Line has content: merge with previous line
                        // Remove newline + indent, keep content
                        int contentStart = curr.start + actualIndent;
                        
                        String before = text.substring(0, newlinePos);
                        String content = contentStart <= text.length() ? text.substring(contentStart) : "";
                        
                        // Determine if we need spacing between merged content
                        String spacer = "";
                        if (before.length() > 0 && content.length() > 0) {
                            char lastChar = before.charAt(before.length() - 1);
                            char firstChar = content.charAt(0);
                            // Add space unless previous ends with whitespace/open bracket or current starts with close bracket/punctuation
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
                        endSelection = cursorPosition = startSelection = newCursor;
                        return true;
                    }
                }
            }
            
            // Auto-pair deletion: if deleting an opening bracket/quote and next char is the matching closer
            if (startSelection > 0 && startSelection < text.length()) {
                char prev = text.charAt(startSelection - 1);
                char nextc = text.charAt(startSelection);
                if ((prev == '(' && nextc == ')') || 
                    (prev == '[' && nextc == ']') || 
                    (prev == '{' && nextc == '}') || 
                    (prev == '\'' && nextc == '\'') || 
                    (prev == '"' && nextc == '"')) {
                    String before = text.substring(0, startSelection - 1);
                    String after = startSelection + 1 < text.length() ? text.substring(startSelection + 1) : "";
                    setText(before + after);
                    startSelection -= 1;
                    endSelection = cursorPosition = startSelection;
                    return true;
                }
            }

            // Normal backspace: delete one character
            String s = getSelectionBeforeText();
            s = s.substring(0, s.length() - 1);
            startSelection--;
            setText(s + getSelectionAfterText());
            endSelection = cursorPosition = startSelection;
            return true;
        }
        if (this.isKeyComboCtrlX(i)) {
            if (startSelection != endSelection) {
                NoppesStringUtils.setClipboardContents(text.substring(startSelection, endSelection));
                String s = getSelectionBeforeText();
                setText(s + getSelectionAfterText());
                endSelection = startSelection = cursorPosition = s.length();

            }
            return true;
        }
        if (this.isKeyComboCtrlC(i)) {
            if (startSelection != endSelection) {
                NoppesStringUtils.setClipboardContents(text.substring(startSelection, endSelection));
            }
            return true;
        }
        if (this.isKeyComboCtrlV(i)) {
            addText(NoppesStringUtils.getClipboardContents());
            return true;
        }
        if (i == Keyboard.KEY_Z && isCtrlKeyDown()) {
            if (undoList.isEmpty())
                return false;
            undoing = true;
            redoList.add(new UndoData(this.text, this.cursorPosition));
            UndoData data = undoList.remove(undoList.size() - 1);
            setText(data.text);
            endSelection = startSelection = cursorPosition = data.cursorPosition;
            undoing = false;
            return true;
        }
        if (i == Keyboard.KEY_Y && isCtrlKeyDown()) {
            if (redoList.isEmpty())
                return false;
            undoing = true;
            undoList.add(new UndoData(this.text, this.cursorPosition));
            UndoData data = redoList.remove(redoList.size() - 1);
            setText(data.text);
            endSelection = startSelection = cursorPosition = data.cursorPosition;
            undoing = false;
            return true;
        }
        if (i == Keyboard.KEY_TAB) {
            boolean shift = isShiftKeyDown();
            if (shift) {
                handleShiftTab();
            } else {
                handleTab();
            }
        }
        if (i == Keyboard.KEY_F && isCtrlKeyDown()) {
            formatText();
            return true;
        }
        if (i == Keyboard.KEY_RETURN) {
            if (cursorPosition > 0 && cursorPosition <= text.length() && text.charAt(cursorPosition - 1) == '{') {
                String indent = getIndentCurrentLine();
                String childIndent = indent + "    ";
                String before = getSelectionBeforeText();
                String after = getSelectionAfterText();

                // If there is non-empty code on the same line after the brace (before the next newline),
                // treat it as code that should be moved into the newly created inner line. In that case
                // just insert the child indent and return — do not insert the closing brace before that code.
                int firstNewline = after.indexOf('\n');
                String leadingSegment = firstNewline == -1 ? after : after.substring(0, firstNewline);
                if (leadingSegment.trim().length() > 0) {
                    addText("\n" + childIndent);
                    return true;
                }

                // Determine whether this opening brace already has a matching closing
                // brace at the same scope (and indent). Prefer using the brace-span
                // computation which skips strings/comments and handles nesting.
                boolean hasMatchingCloseSameIndent = false;
                try {
                    // Find the line index containing the opening brace (cursorPosition-1)
                    int openLineIdx = -1;
                    int bracePos = cursorPosition - 1;
                    for (int li = 0; li < this.container.lines.size(); li++) {
                        LineData ld = this.container.lines.get(li);
                        if (bracePos >= ld.start && bracePos < ld.end) {
                            openLineIdx = li;
                            break;
                        }
                    }

                    if (openLineIdx >= 0) {
                        List<int[]> spans = computeBraceSpans(text, this.container.lines);
                        for (int[] span : spans) {
                            int spanOpen = span[1];
                            int spanClose = span[2];
                            if (spanOpen == openLineIdx) {
                                // Found a matching close; check its indent equals current indent
                                int closeIndent = getLineIndent(this.container.lines.get(spanClose).text);
                                if (closeIndent == indent.length()) {
                                    hasMatchingCloseSameIndent = true;
                                }
                                break;
                            }
                        }
                    }
                } catch (Exception ex) {
                    // Fallback: if anything goes wrong, conservatively behave as before
                    hasMatchingCloseSameIndent = false;
                }


                if (hasMatchingCloseSameIndent) {
                    addText("\n" + childIndent);
                } else {
                    String insert = "\n" + childIndent + "\n" + indent + "}";
                    setText(before + insert + after);
                    int newCursor = before.length() + 1 + childIndent.length();
                    startSelection = endSelection = cursorPosition = newCursor;
                }
            } else {
                addText(Character.toString('\n') + getAutoIndentForEnter());
            }
        }
        if (ChatAllowedCharacters.isAllowedCharacter(c)) {
            String before = getSelectionBeforeText();
            String after = getSelectionAfterText();

            // If typing a closing char and the next char is already that closer, skip over it
            if ((c == ')' || c == ']' || c == '"' || c == '\'' ) && after.length() > 0 && after.charAt(0) == c) {
                // move cursor forward over existing closer
                startSelection = endSelection = cursorPosition = before.length() + 1;
                return true;
            }

            if (c == '"') {
                setText(before + "\"\"" + after);
                startSelection = endSelection = cursorPosition = before.length() + 1;
                return true;
            }
            if (c == '\'') {
                setText(before + "''" + after);
                startSelection = endSelection = cursorPosition = before.length() + 1;
                return true;
            }
            if (c == '[') {
                setText(before + "[]" + after);
                startSelection = endSelection = cursorPosition = before.length() + 1;
                return true;
            }
            if (c == '(') {
                setText(before + "()" + after);
                startSelection = endSelection = cursorPosition = before.length() + 1;
                return true;
            }

            addText(Character.toString(c));
        }

        if (i == Keyboard.KEY_SLASH && isCtrlKeyDown()) {
            if (startSelection != endSelection) {
                toggleCommentSelection();
            } else {
                toggleCommentLineAtCursor();
            }
            return true;
        }

        if (i == Keyboard.KEY_D && isCtrlKeyDown()) {
            if (startSelection != endSelection) {
                // Handle multi-line selection duplication
                LineData firstLine = null, lastLine = null;
                for (LineData line : container.lines) {
                    if (line.end > startSelection && line.start < endSelection) {
                        if (firstLine == null) firstLine = line;
                        lastLine = line;
                    }
                }
                if (firstLine != null && lastLine != null) {
                    String selectedText = text.substring(firstLine.start, lastLine.end);
                    // Insert the selected block immediately after the last line without adding an extra newline
                    String insertText = selectedText;
                    // Save selection before setText
                    int savedStart = startSelection;
                    int savedEnd = endSelection;
                    int insertAt = lastLine.end;
                    setText(text.substring(0, insertAt) + insertText + text.substring(insertAt));
                    // Restore cursor and selection
                    startSelection = savedStart;
                    endSelection = this.cursorPosition =  savedEnd;
                    return true;
                }
            } else {
                // Single line duplication (existing logic)
                for (LineData line : container.lines) {
                    if (cursorPosition >= line.start && cursorPosition <= line.end) {
                        int lineStart = line.start, lineEnd = line.end;
                        String lineText = text.substring(lineStart, lineEnd);
                        // Avoid inserting an extra blank line: if lineText already ends with a newline,
                        // reuse it; otherwise prepend a newline so the duplicate appears immediately after.
                        String insertText;
                        if (lineText.endsWith("\n")) {
                            insertText = lineText;
                        } else {
                            insertText = "\n" + lineText;
                        }
                        int insertionPoint = lineEnd;
                        setText(text.substring(0, insertionPoint) + insertText + text.substring(insertionPoint));
                        // Place cursor at end of duplicated line (just before any trailing newline)
                        int newCursor = insertionPoint + insertText.length() - (insertText.endsWith("\n") ? 1 : 0);
                        startSelection = endSelection = cursorPosition = Math.max(0, Math.min(newCursor, this.text.length()));
                        return true;
                    }
                }
            }
        }

        return true;
    }

    private boolean isShiftKeyDown() {
        return Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54);
    }

    private void toggleCommentSelection() {
        StringBuilder newText = new StringBuilder(text.length() + 100);
        int prevEnd = 0;

        int newStart = -1; //calculates expanded startSelection
        int newEnd = -1; //calculates expanded endSelection
        int newIndex = 0;

        for (LineData line : container.lines) {
            int safeLineStart = Math.max(0, Math.min(line.start, text.length()));
            int safeLineEnd = Math.max(0, Math.min(line.end, text.length()));

            if (safeLineStart > prevEnd) {
                if (newStart == -1 && startSelection >= prevEnd && startSelection <= safeLineStart) {
                    newStart = newIndex + (startSelection - prevEnd);
                }
                if (newEnd == -1 && endSelection >= prevEnd && endSelection <= safeLineStart) {
                    newEnd = newIndex + (endSelection - prevEnd);
                }
                newText.append(text, prevEnd, safeLineStart);
                newIndex += safeLineStart - prevEnd;
            }

            if (safeLineEnd > startSelection && safeLineStart < endSelection) {
                String lineText = text.substring(safeLineStart, safeLineEnd);
                CommentToggleInfo ti = toggleLine(lineText);
                String newLineText = ti.text;
                int nonWs = ti.nonWs;
                int delta = ti.delta;

                if (newStart == -1 && startSelection >= safeLineStart && startSelection <= safeLineEnd) {
                    int offsetInOldStart = startSelection - safeLineStart;
                    int newOffsetStart = offsetInOldStart;
                    if (offsetInOldStart >= nonWs) {
                        newOffsetStart = offsetInOldStart + delta;
                    }
                    newStart = newIndex + Math.max(0, newOffsetStart);
                }
                if (newEnd == -1 && endSelection >= safeLineStart && endSelection <= safeLineEnd) {
                    int offsetInOld = endSelection - safeLineStart;
                    int newOffset = offsetInOld;
                    if (offsetInOld >= nonWs) {
                        newOffset = offsetInOld + delta;
                    }
                    newEnd = newIndex + Math.max(0, newOffset);
                }

                newText.append(newLineText);
                newIndex += newLineText.length();
            } else {
                if (newStart == -1 && startSelection >= safeLineStart && startSelection <= safeLineEnd) {
                    newStart = newIndex + (startSelection - safeLineStart);
                }
                if (newEnd == -1 && endSelection >= safeLineStart && endSelection <= safeLineEnd) {
                    newEnd = newIndex + (endSelection - safeLineStart);
                }
                newText.append(text, safeLineStart, safeLineEnd);
                newIndex += safeLineEnd - safeLineStart;
            }
            prevEnd = safeLineEnd;
        }

        if (prevEnd < text.length()) {
            if (newStart == -1 && startSelection >= prevEnd && startSelection <= text.length()) {
                newStart = newIndex + (startSelection - prevEnd);
            }
            if (newEnd == -1 && endSelection >= prevEnd && endSelection <= text.length()) {
                newEnd = newIndex + (endSelection - prevEnd);
            }
            newText.append(text, prevEnd, text.length());
            newIndex += text.length() - prevEnd;
        }

        if (newStart == -1) newStart = Math.max(0, Math.min(startSelection, newText.length()));
        if (newEnd == -1) newEnd = Math.max(0, Math.min(endSelection, newText.length()));

        setText(newText.toString());
        startSelection = newStart;
        endSelection = newEnd;
        cursorPosition = endSelection;
    }

    private void toggleCommentLineAtCursor() {
        for (LineData line : container.lines) {
            int lineStart = Math.max(0, Math.min(line.start, text.length()));
            int lineEnd = Math.max(0, Math.min(line.end, text.length()));
            if (cursorPosition >= lineStart && cursorPosition <= lineEnd) {
                String lineText = text.substring(lineStart, lineEnd);
                CommentToggleInfo ti = toggleLine(lineText);
                String newLineText = ti.text;
                int nonWs = ti.nonWs;
                boolean hasComment = ti.delta < 0;
                String before = text.substring(0, lineStart);
                String after = lineEnd <= text.length() ? text.substring(lineEnd) : "";
                setText(before + newLineText + after);
                int cursorDelta = hasComment ? -2 : 2;
                int newCursor = cursorPosition + cursorDelta;
                if (cursorPosition < lineStart + nonWs + (hasComment ? 2 : 0)) newCursor = cursorPosition;
                setCursor(Math.max(lineStart, newCursor), false);
                return;
            }
        }
    }

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
        Iterator var1 = this.container.lines.iterator();

        LineData data;
        do {
            if (!var1.hasNext()) {
                return "";
            }

            data = (LineData) var1.next();
        } while (this.cursorPosition <= data.start || this.cursorPosition > data.end);

        int i;
        for (i = 0; i < data.text.length() && data.text.charAt(i) == 32; ++i) {
        }

        return data.text.substring(0, i);
    }

    private String getAutoIndentForEnter() {
        LineData currentLine = null;
        for (LineData line : this.container.lines) {
            if (this.cursorPosition >= line.start && this.cursorPosition < line.end) {
                currentLine = line;
                break;
            }
        }

        if (currentLine == null) {
            return "";
        }

        String lineText = currentLine.text;
        int leading = 0;
        while (leading < lineText.length() && (lineText.charAt(leading) == ' ' || lineText.charAt(leading) == '\t')) {
            leading++;
        }

        String baseIndent = lineText.substring(0, leading);
        int relativeCursor = Math.max(0, Math.min(this.cursorPosition - currentLine.start, lineText.length()));
        String beforeCursor = lineText.substring(0, relativeCursor);

        // Find last non-whitespace before cursor
        int lastNonWs = -1;
        for (int idx = beforeCursor.length() - 1; idx >= 0; idx--) {
            if (!Character.isWhitespace(beforeCursor.charAt(idx))) {
                lastNonWs = idx;
                break;
            }
        }

        // Only add extra indent if cursor is actually after the opening brace (not just in trailing whitespace)
        // Check that the last non-whitespace is at or after the line's indent position
        boolean opensBlock = lastNonWs >= 0 && lastNonWs >= leading && beforeCursor.charAt(lastNonWs) == '{';
        if (opensBlock) {
            return baseIndent + "    ";
        }

        return baseIndent;
    }

    private int getLineIndent(String lineText) {
        int leading = 0;
        while (leading < lineText.length() && (lineText.charAt(leading) == ' ' || lineText.charAt(
                leading) == '\t')) {
            leading++;
        }
        return leading;
    }

    // Calculate the expected/proper indent for the current line based on surrounding context
    private int getExpectedIndent(LineData currentLine) {
        if (currentLine == null) {
            return 0;
        }

        int idx = container.lines.indexOf(currentLine);
        if (idx <= 0) {
            return 0; // First line has no expected indent
        }

        // Find the previous non-empty line
        LineData prevLine = null;
        for (int i = idx - 1; i >= 0; i--) {
            LineData line = container.lines.get(i);
            if (line.text.trim().length() > 0) {
                prevLine = line;
                break;
            }
        }

        if (prevLine == null) {
            return 0;
        }

        int prevIndent = getLineIndent(prevLine.text);
        String prevTrimmed = prevLine.text.trim();
        String currTrimmed = currentLine.text.trim();

        // If current line starts with }, expected indent is one level back
        if (currTrimmed.startsWith("}")) {
            return Math.max(0, prevIndent - getTabSize());
        }

        // If previous line ends with {, expected indent is one level forward
        if (prevTrimmed.endsWith("{")) {
            return prevIndent + getTabSize();
        }

        // Otherwise, expected indent matches previous line
        return prevIndent;
    }

    private int getTabSize() {
        return 4;
    }

    private String repeatSpace(int count) {
        if (count <= 0)
            return "";
        char[] arr = new char[count];
        java.util.Arrays.fill(arr, ' ');
        return new String(arr);
    }

    private void formatText() {
        // Find which line the cursor is on and calculate position within that line (after stripping indent)
        int cursorLine = -1;
        int cursorColInContent = 0;  // position in line after removing leading whitespace
        int lineStartPos = 0;
        String[] linesArr = text.split("\n", -1);
        
        for (int li = 0; li < linesArr.length; li++) {
            String line = linesArr[li];
            int lineEndPos = lineStartPos + line.length();
            
            if (cursorPosition >= lineStartPos && cursorPosition <= lineEndPos) {
                cursorLine = li;
                int leadingSpaces = line.length() - line.replaceAll("^[ \t]+", "").length();
                cursorColInContent = Math.max(0, cursorPosition - lineStartPos - leadingSpaces);
                break;
            }
            lineStartPos = lineEndPos + 1; // +1 for newline
        }
        
        StringBuilder out = new StringBuilder(text.length() + 32);

        boolean inString = false;
        boolean escape = false;
        boolean inBlockComment = false;
        int depth = 0;
        int tab = getTabSize();
        
        int newCursorPos = 0;

        for (int li = 0; li < linesArr.length; li++) {
            String line = linesArr[li];
            String trimmedLeading = line.replaceAll("^[ \t]+", "");

            int lineIndentDec = 0;
            int opens = 0, closes = 0;
            boolean startsWithClose = false;

            // First pass to determine startsWithClose and brace counts ignoring strings/comments
            for (int idx = 0; idx < line.length(); idx++) {
                char c = line.charAt(idx);
                char next = idx + 1 < line.length() ? line.charAt(idx + 1) : 0;

                if (inString) {
                    if (escape) {
                        escape = false;
                    } else if (c == '\\') {
                        escape = true;
                    } else if (c == '"') {
                        inString = false;
                    }
                    continue;
                }

                if (inBlockComment) {
                    if (c == '*' && next == '/') {
                        inBlockComment = false;
                        idx++;
                    }
                    continue;
                }

                if (c == '/' && next == '/') {
                    break; // rest of line is comment
                }
                if (c == '/' && next == '*') {
                    inBlockComment = true;
                    idx++;
                    continue;
                }
                if (c == '"') {
                    inString = true;
                    escape = false;
                    continue;
                }

                if (c == '{') {
                    opens++;
                } else if (c == '}') {
                    closes++;
                    if (!startsWithClose) {
                        String prefix = line.substring(0, idx);
                        if (prefix.trim().isEmpty()) {
                            startsWithClose = true;
                        }
                    }
                }
            }

            int indentLevel = depth;
            if (startsWithClose)
                indentLevel = Math.max(0, indentLevel - 1);

            int targetIndent = indentLevel * tab;
            
            // Track cursor position in the formatted output
            if (li == cursorLine) {
                newCursorPos = out.length() + targetIndent + Math.min(cursorColInContent, trimmedLeading.length());
            }
            
            out.append(repeatSpace(targetIndent)).append(trimmedLeading);
            if (li < linesArr.length - 1)
                out.append('\n');

            depth = Math.max(0, depth + opens - closes);
        }

        setText(out.toString());
        
        // Restore cursor position
        if (cursorLine >= 0) {
            newCursorPos = Math.max(0, Math.min(newCursorPos, this.text.length()));
            startSelection = endSelection = cursorPosition = newCursorPos;
        }
    }

    private void handleTab() {
        LineData currentLine = null;
        for (LineData line : this.container.lines) {
            if (this.cursorPosition >= line.start && this.cursorPosition < line.end) {
                currentLine = line;
                break;
            }
        }
        if (currentLine == null) {
            addText("    ");
            return;
        }
        int tab = getTabSize();
        int indentLen = getLineIndent(currentLine.text);
        int textStartPos = currentLine.start + indentLen;

        if (this.cursorPosition <= textStartPos) {
            // Cursor before any text: if cursor is exactly at text start, move forward to next tab stop.
            // If cursor is inside leading whitespace (before text start), choose nearest tab stop (tie -> forward).
            int targetIndent;
            if (this.cursorPosition == textStartPos) {
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
            startSelection = endSelection = cursorPosition = Math.min(newCursor, this.text.length());
        } else {
            // Cursor is after start of text: insert spaces at cursor to move following text to next tab stop
            int column = this.cursorPosition - currentLine.start;
            int targetColumn = ((column / tab) + 1) * tab;
            int toInsert = Math.max(0, targetColumn - column);
            if (toInsert > 0) {
                String spaces = repeatSpace(toInsert);
                addText(spaces);
            }
        }
    }

    private void handleShiftTab() {
        LineData currentLine = null;
        for (LineData line : this.container.lines) {
            if (this.cursorPosition >= line.start && this.cursorPosition < line.end) {
                currentLine = line;
                break;
            }
        }
        if (currentLine == null)
            return;
        int tab = getTabSize();
        int indentLen = getLineIndent(currentLine.text);
        int textStartPos = currentLine.start + indentLen;

        if (this.cursorPosition <= textStartPos) {
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
            startSelection = endSelection = cursorPosition = Math.min(newCursor, this.text.length());
        } else {
            // Cursor after start of text: remove up to previous tab stop worth of spaces immediately before cursor
            int column = this.cursorPosition - currentLine.start;
            int mod = column % tab;
            int toRemove = mod == 0 ? tab : mod;
            int removed = 0;
            int pos = this.cursorPosition - 1;
            while (pos >= currentLine.start && removed < toRemove && text.charAt(pos) == ' ') {
                pos--;
                removed++;
            }
            if (removed > 0) {
                int removeStart = pos + 1;
                String before = text.substring(0, removeStart);
                String after = text.substring(this.cursorPosition);
                setText(before + after);
                int newCursor = removeStart;
                startSelection = endSelection = cursorPosition = Math.min(newCursor, this.text.length());
            }
        }
    }

    private void setCursor(int i, boolean select) {
        i = Math.min(Math.max(i, 0), this.text.length());
        if (i != this.cursorPosition) {
            if (!select) {
                this.endSelection = this.startSelection = this.cursorPosition = i;
            } else {
                int diff = this.cursorPosition - i;
                if (this.cursorPosition == this.startSelection) {
                    this.startSelection -= diff;
                } else if (this.cursorPosition == this.endSelection) {
                    this.endSelection -= diff;
                }

                if (this.startSelection > this.endSelection) {
                    int j = this.endSelection;
                    this.endSelection = this.startSelection;
                    this.startSelection = j;
                }

                this.cursorPosition = i;
            }

            clampSelectionBounds();
            // Moving the cursor is user activity — pause blinking briefly
            this.lastInputTime = System.currentTimeMillis();
        }
    }

    private void addText(String s) {
        this.setText(this.getSelectionBeforeText() + s + this.getSelectionAfterText());
        this.endSelection = this.startSelection = this.cursorPosition = this.startSelection + s.length();
        this.lastInputTime = System.currentTimeMillis();
    }

    private int cursorUp() {
        for (int i = 0; i < this.container.lines.size(); ++i) {
            LineData data = (LineData) this.container.lines.get(i);
            if (this.cursorPosition >= data.start && this.cursorPosition < data.end) {
                if (i == 0) {
                    return 0;
                }

                int column = this.cursorPosition - data.start;
                LineData target = this.container.lines.get(i - 1);
                int targetPos = this.getSelectionPos(this.x + 1 + ClientProxy.Font.width(data.text.substring(0, column)), this.y + 1 + (i - 1 - this.scrolledLine) * this.container.lineHeight);
                int targetIndent = getLineIndent(target.text);
                int minPos = target.start + Math.min(targetIndent, Math.max(0, target.text.length()));
                return Math.max(minPos, targetPos);
            }
        }

        return 0;
    }

    private int cursorDown() {
        if (cursorPosition == text.length())
            return cursorPosition;
        for (int i = 0; i < this.container.lines.size(); ++i) {
            LineData data = (LineData) this.container.lines.get(i);
            if (this.cursorPosition >= data.start && this.cursorPosition < data.end) {
                int column = this.cursorPosition - data.start;
                LineData target = this.container.lines.get(Math.min(i + 1, this.container.lines.size() - 1));
                int targetPos = this.getSelectionPos(this.x + 1 + ClientProxy.Font.width(data.text.substring(0, column)), this.y + 1 + (i + 1 - this.scrolledLine) * this.container.lineHeight);
                int targetIndent = getLineIndent(target.text);
                int minPos = target.start + Math.min(targetIndent, Math.max(0, target.text.length()));
                return Math.max(minPos, targetPos);
            }
        }

        return this.text.length();
    }

    public String getSelectionBeforeText() {
        return this.startSelection == 0 ? "" : this.text.substring(0, this.startSelection);
    }

    public String getSelectionAfterText() {
        return this.text.substring(this.endSelection);
    }

    public void mouseClicked(int xMouse, int yMouse, int mouseButton) {
        this.active = xMouse >= this.x && xMouse < this.x + this.width && yMouse >= this.y && yMouse < this.y + this.height;
        if (this.active) {
            this.startSelection = this.endSelection = this.cursorPosition = this.getSelectionPos(xMouse, yMouse);
            this.lastInputTime = System.currentTimeMillis();
            this.clicked = mouseButton == 0;
            this.doubleClicked = false;
            this.tripleClicked = false;
            long time = System.currentTimeMillis();
            if (this.clicked && this.container.linesCount * this.container.lineHeight > this.height && xMouse > this.x + this.width - 8) {
                this.clicked = false;
                this.clickScrolling = true;
            } else {
                if (time - this.lastClicked < 300L) {
                    this.clickCount++;
                } else {
                    this.clickCount = 1;
                }

                if (this.clickCount == 2) {
                    // Double-click: select word under cursor (restore prior behavior)
                    this.doubleClicked = true;
                    Matcher m = this.container.regexWord.matcher(this.text);
                    while (m.find()) {
                        if (this.cursorPosition >= m.start() && this.cursorPosition <= m.end()) {     
                            this.startSelection = m.start();
                            this.endSelection = m.end();
                            break;
                        }
                    }
                } else if (this.clickCount >= 3) {
                    // Triple-click: select entire line
                    this.tripleClicked = true;
                    for (LineData line : container.lines) {
                        if (this.cursorPosition >= line.start && this.cursorPosition <= line.end) {
                            this.startSelection = line.start;
                            this.endSelection = line.end;
                            break;
                        }
                    }
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
                this.undoList.add(new GuiScriptTextArea.UndoData(this.text, this.cursorPosition));
                this.redoList.clear();
            }

            this.text = text;
            //this.container = new TextContainer(text);
            this.container = new JavaTextContainer(text);
            this.container.init(this.width, this.height);
            if (this.enableCodeHighlighting) {
                this.container.formatCodeText();
            }

            // Ensure scrolledLine stays in bounds and snap smooth scroll to avoid perceived lag
            int maxScroll = Math.max(0, this.container.linesCount - this.container.visibleLines);
            if (this.scrolledLine > maxScroll) {
                this.scrolledLine = maxScroll;
            }
            // Immediately update fractional scroll targets so new content appears without delay
            this.targetScroll = this.scrolledLine;
            this.scrollPos = this.scrolledLine;

            clampSelectionBounds();

            // Consider text changes user activity to pause caret blinking briefly
            this.lastInputTime = System.currentTimeMillis();

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
        if (this.text == null || this.text.isEmpty()) {
            this.startSelection = this.endSelection = this.cursorPosition = 0;
            return;
        }

        int length = this.text.length();
        this.startSelection = Math.max(0, Math.min(this.startSelection, length));
        this.endSelection = Math.max(0, Math.min(this.endSelection, length));
        this.cursorPosition = Math.max(0, Math.min(this.cursorPosition, length));
    }

    private static class CommentToggleInfo {
        public final String text;
        public final int delta; // newText.length() - oldText.length()
        public final int nonWs;

        public CommentToggleInfo(String text, int delta, int nonWs) {
            this.text = text;
            this.delta = delta;
            this.nonWs = nonWs;
        }
    }

    private CommentToggleInfo toggleLine(String lineText) {
        int nonWs = 0;
        while (nonWs < lineText.length() && Character.isWhitespace(lineText.charAt(nonWs))) nonWs++;
        boolean hasContent = nonWs < lineText.length();
        boolean hasComment = hasContent && lineText.startsWith("//", nonWs);
        String newLineText;
        if (hasComment) {
            int cut = Math.min(nonWs + 2, lineText.length());
            newLineText = lineText.substring(0, nonWs) + (cut <= lineText.length() ? lineText.substring(cut) : "");
        } else if (hasContent) {
            newLineText = lineText.substring(0, nonWs) + "//" + lineText.substring(nonWs);
        } else {
            newLineText = lineText;
        }
        return new CommentToggleInfo(newLineText, newLineText.length() - lineText.length(), nonWs);
    }

    class UndoData {
        public String text;
        public int cursorPosition;

        public UndoData(String text, int cursorPosition) {
            this.text = text;
            this.cursorPosition = cursorPosition;
        }
    }
}
