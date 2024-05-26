package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatAllowedCharacters;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.gui.util.TextContainer.LineData;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

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
    private ITextChangeListener listener;
    public String text = null;
    private TextContainer container = null;
    public boolean active = false;
    public boolean enabled = true;
    public boolean visible = true;
    public boolean clicked = false;
    public boolean doubleClicked = false;
    public boolean clickScrolling = false;
    private int startSelection;
    private int endSelection;
    private int cursorPosition;
    private int scrolledLine = 0;
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

    public void drawTextBox(int xMouse, int yMouse){
        if(!visible)
            return;
        drawRect(x - 1, y - 1, x + width + 1, y + height + 1, 0xffa0a0a0);
        drawRect(x, y, x + width, y + height, 0xff000000);

        container.visibleLines = height / container.lineHeight;

        if(listener instanceof GuiNPCInterface){
            int k2 = ((GuiNPCInterface) listener).mouseScroll = Mouse.getDWheel();
            if(k2 != 0) {
                this.scrolledLine -= k2/40;
                this.scrolledLine = Math.max(Math.min(this.scrolledLine, this.container.linesCount - this.height / this.container.lineHeight), 0);
            }
        }

        if(clicked){
            clicked = Mouse.isButtonDown(0);
            int i = getSelectionPos(xMouse, yMouse);
            if(i != cursorPosition){
                if(doubleClicked){
                    startSelection = endSelection = cursorPosition;
                    doubleClicked = false;
                }
                setCursor(i, true);
            }
        }
        else if(doubleClicked){
            doubleClicked = false;
        }

        if(clickScrolling){
            clickScrolling = Mouse.isButtonDown(0);
            int diff = container.linesCount - container.visibleLines;
            scrolledLine = Math.min(Math.max((int)(1f * diff * (yMouse - y) / height), 0), diff);
        }
        int startBracket = 0, endBracket = 0;
        if(endSelection - startSelection == 1 || startSelection == endSelection && startSelection < text.length()){
            char c = text.charAt(startSelection);
            int found = 0;
            if(c == '{'){
                found = findClosingBracket(text.substring(startSelection), '{', '}');
            }
            else if(c == '['){
                found = findClosingBracket(text.substring(startSelection), '[', ']');
            }
            else if(c == '('){
                found = findClosingBracket(text.substring(startSelection), '(', ')');
            }
            else if(c == '}'){
                found = findOpeningBracket(text.substring(0, startSelection + 1), '{', '}');
            }
            else if(c == ']'){
                found = findOpeningBracket(text.substring(0, startSelection + 1), '[', ']');
            }
            else if(c == ')'){
                found = findOpeningBracket(text.substring(0, startSelection + 1), '(', ')');
            }
            if(found != 0){
                startBracket = startSelection;
                endBracket = startSelection + found;
            }
        }

        List<LineData> list = new ArrayList<LineData>(container.lines);

        String wordHightLight = null;
        if(startSelection != endSelection){
            Matcher m = container.regexWord.matcher(text);
            while(m.find()){
                if(m.start() == startSelection && m.end() == endSelection){
                    wordHightLight = text.substring(startSelection, endSelection);
                }
            }
        }
        for(int i = 0; i < list.size(); i++){
            LineData data = list.get(i);
            String line = data.text;
            int w = line.length();
            if(startBracket != endBracket){
                if(startBracket >= data.start && startBracket < data.end){
                    int s = ClientProxy.Font.width(line.substring(0, startBracket - data.start));
                    int e = ClientProxy.Font.width(line.substring(0, startBracket - data.start + 1)) + 1;
                    int posY = y + 1 + (i - scrolledLine) * container.lineHeight;
                    drawRect(x + 1 + s, posY, x + 1 + e, posY + container.lineHeight + 1, 0x9900cc00);
                }
                if(endBracket >= data.start && endBracket < data.end){
                    int s = ClientProxy.Font.width(line.substring(0, endBracket - data.start));
                    int e = ClientProxy.Font.width(line.substring(0, endBracket - data.start + 1)) + 1;
                    int posY = y + 1 + (i - scrolledLine) * container.lineHeight;
                    drawRect(x + 1 + s, posY, x + 1 + e, posY + container.lineHeight + 1, 0x9900cc00);
                }
            }
            if(i >= scrolledLine && i < scrolledLine + container.visibleLines){
                if(wordHightLight != null){
                    Matcher m = container.regexWord.matcher(line);
                    while(m.find()){
                        if(line.substring(m.start(), m.end()).equals(wordHightLight)){
                            int s = ClientProxy.Font.width(line.substring(0, m.start()));
                            int e = ClientProxy.Font.width(line.substring(0, m.end())) + 1;
                            int posY = y + 1 + (i - scrolledLine) * container.lineHeight;
                            drawRect(x + 1 + s, posY, x + 1 + e, posY + container.lineHeight + 1, 0x99004c00);
                        }
                    }
                }
                if(startSelection != endSelection && endSelection > data.start && startSelection <= data.end){
                    if(startSelection < data.end){
                        int s = ClientProxy.Font.width(line.substring(0, Math.max(startSelection - data.start, 0)));
                        int e = ClientProxy.Font.width(line.substring(0, Math.min(endSelection - data.start, w))) + 1;
                        int posY = y + 1 + (i - scrolledLine) * container.lineHeight;
                        drawRect(x + 1 + s, posY, x + 1 + e, posY + container.lineHeight + 1, 0x990000ff);
                    }
                }
                int yPos = y + (i - scrolledLine) * container.lineHeight + 1;
                data.drawString(x + 1, yPos, 0xFFe0e0e0);

                if(active && isEnabled() && (cursorCounter / 6) % 2 == 0 && cursorPosition >= data.start && cursorPosition < data.end){
                    int posX = x + ClientProxy.Font.width(line.substring(0, cursorPosition - data.start));
                    drawRect(posX + 1, yPos, posX + 2, yPos + 1 + container.lineHeight, -3092272);
                }
            }
        }

        if(hasVerticalScrollbar()){
            Minecraft.getMinecraft().renderEngine.bindTexture(GuiCustomScroll.resource);
            int sbSize = Math.max((int) (1f * container.visibleLines / container.linesCount * height), 2);

            int posX = x + width - 6;
            int posY = (int) (y +  1f * scrolledLine / container.linesCount * (height - 4)) + 1;

            drawRect(posX, posY, posX + 5, posY + sbSize, 0xFFe0e0e0);
        }
    }

    private int findClosingBracket(String str, char s, char e) {
        int found = 0;
        char[] chars = str.toCharArray();

        for(int i = 0; i < chars.length; ++i) {
            char c = chars[i];
            if(c == s) {
                ++found;
            } else if(c == e) {
                --found;
                if(found == 0) {
                    return i;
                }
            }
        }

        return 0;
    }

    private int findOpeningBracket(String str, char s, char e) {
        int found = 0;
        char[] chars = str.toCharArray();

        for(int i = chars.length - 1; i >= 0; --i) {
            char c = chars[i];
            if(c == e) {
                ++found;
            } else if(c == s) {
                --found;
                if(found == 0) {
                    return i - chars.length + 1;
                }
            }
        }

        return 0;
    }

    private int getSelectionPos(int xMouse, int yMouse) {
        xMouse -= this.x + 1;
        yMouse -= this.y + 1;
        ArrayList list = new ArrayList(this.container.lines);

        for(int i = 0; i < list.size(); ++i) {
            LineData data = (LineData)list.get(i);
            if(i >= this.scrolledLine && i < this.scrolledLine + this.container.visibleLines) {
                int yPos = (i - this.scrolledLine) * this.container.lineHeight;
                if(yMouse >= yPos && yMouse < yPos + this.container.lineHeight) {
                    int lineWidth = 0;
                    char[] chars = data.text.toCharArray();

                    for(int j = 1; j <= chars.length; ++j) {
                        int w = ClientProxy.Font.width(data.text.substring(0, j));
                        if(xMouse < lineWidth + (w - lineWidth) / 2) {
                            return data.start + j - 1;
                        }

                        lineWidth = w;
                    }

                    return data.end - 1;
                }
            }
        }

        return this.container.text.length();
    }

    @Override
    public boolean textboxKeyTyped(char c, int i) {
        if(!active)
            return false;

        if(this.isKeyComboCtrlA(i)){
            startSelection = cursorPosition = 0;
            endSelection = text.length();
            return true;
        }

        if(!isEnabled())
            return false;

        String original = text;
        if(i == Keyboard.KEY_LEFT){
            int j = 1;
            if(isCtrlKeyDown()){
                Matcher m = container.regexWord.matcher(text.substring(0, cursorPosition));
                while(m.find()){
                    if(m.start() == m.end())
                        continue;
                    j = cursorPosition - m.start();
                }
            }
            setCursor(cursorPosition - j, GuiScreen.isShiftKeyDown());
            return true;
        }
        if(i == Keyboard.KEY_RIGHT){
            int j = 1;
            if(isCtrlKeyDown()){
                Matcher m = container.regexWord.matcher(text.substring(cursorPosition));
                if(m.find() && m.start() > 0 || m.find()){
                    j = m.start();
                }
            }
            setCursor(cursorPosition + j, GuiScreen.isShiftKeyDown());
            return true;
        }
        if(i == Keyboard.KEY_UP){
            setCursor(cursorUp(), GuiScreen.isShiftKeyDown());
            return true;
        }
        if(i == Keyboard.KEY_DOWN){
            setCursor(cursorDown(), GuiScreen.isShiftKeyDown());
            return true;
        }
        if(i == Keyboard.KEY_DELETE){
            String s = getSelectionAfterText();
            if(!s.isEmpty() && startSelection == endSelection)
                s = s.substring(1);
            setText(getSelectionBeforeText() + s);
            endSelection = cursorPosition = startSelection;
            return true;
        }
        if(isKeyComboCtrlBackspace(i)){
            String s = getSelectionBeforeText();
            if(startSelection > 0 && startSelection == endSelection){
                int nearestCondition = cursorPosition;
                int g;
                boolean cursorInWhitespace = Character.isWhitespace(s.charAt(cursorPosition - 1));
                if(cursorInWhitespace){
                    // Find the nearest word if we are starting in whitespace
                    for (g = cursorPosition - 1; g >= 0; g--) {
                        char currentChar = s.charAt(g);
                        if (!Character.isWhitespace(currentChar)) {
                            nearestCondition = g;
                            break;
                        }
                        if(g == 0){
                            nearestCondition = 0;
                        }
                    }
                }
                else {
                    // Find the nearest blank space or new line
                    for (g = cursorPosition - 1; g >= 0; g--) {
                        char currentChar = s.charAt(g);
                        if (Character.isWhitespace(currentChar)  || currentChar == '\n') {
                            nearestCondition = g;
                            break;
                        }
                        if(g == 0){
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
        if(i == Keyboard.KEY_BACK){
            String s = getSelectionBeforeText();
            if(startSelection > 0 && startSelection == endSelection){
                s = s.substring(0, s.length() - 1);
                startSelection--;
            }
            setText(s + getSelectionAfterText());
            endSelection = cursorPosition = startSelection;
            return true;
        }
        if(this.isKeyComboCtrlX(i)){
            if(startSelection != endSelection){
                NoppesStringUtils.setClipboardContents(text.substring(startSelection, endSelection));
                String s = getSelectionBeforeText();
                setText(s + getSelectionAfterText());
                endSelection = startSelection = cursorPosition = s.length();

            }
            return true;
        }
        if(this.isKeyComboCtrlC(i)){
            if(startSelection != endSelection){
                NoppesStringUtils.setClipboardContents(text.substring(startSelection, endSelection));
            }
            return true;
        }
        if(this.isKeyComboCtrlV(i)){
            addText(NoppesStringUtils.getClipboardContents());
            return true;
        }
        if(i == Keyboard.KEY_Z && isCtrlKeyDown()){
            if(undoList.isEmpty())
                return false;
            undoing = true;
            redoList.add(new UndoData(this.text, this.cursorPosition));
            UndoData data = undoList.remove(undoList.size() - 1);
            setText(data.text);
            endSelection = startSelection = cursorPosition = data.cursorPosition;
            undoing = false;
            return true;
        }
        if(i == Keyboard.KEY_Y && isCtrlKeyDown()){
            if(redoList.isEmpty())
                return false;
            undoing = true;
            undoList.add(new UndoData(this.text, this.cursorPosition));
            UndoData data = redoList.remove(redoList.size() - 1);
            setText(data.text);
            endSelection = startSelection = cursorPosition = data.cursorPosition;
            undoing = false;
            return true;
        }
        if(i == Keyboard.KEY_TAB){
            addText("    ");
        }
        if(i == Keyboard.KEY_RETURN){
            addText(Character.toString('\n') + getIndentCurrentLine());
        }
        if(ChatAllowedCharacters.isAllowedCharacter(c)){
            addText(Character.toString(c));
        }

        return true;
    }

    private boolean isShiftKeyDown()
    {
        return Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54);
    }

    private boolean isAltKeyDown()
    {
        return Keyboard.isKeyDown(56) || Keyboard.isKeyDown(184);
    }

    private boolean isKeyComboCtrlX(int keyID)
    {
        return keyID == 45 && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
    }

    private boolean isKeyComboCtrlBackspace(int keyID)
    {
        return keyID == Keyboard.KEY_BACK && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
    }

    private boolean isKeyComboCtrlV(int keyID)
    {
        return keyID == 47 && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
    }

    private boolean isKeyComboCtrlC(int keyID)
    {
        return keyID == 46 && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
    }

    private boolean isKeyComboCtrlA(int keyID)
    {
        return keyID == 30 && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
    }

    private String getIndentCurrentLine() {
        Iterator var1 = this.container.lines.iterator();

        LineData data;
        do {
            if(!var1.hasNext()) {
                return "";
            }

            data = (LineData)var1.next();
        } while(this.cursorPosition <= data.start || this.cursorPosition > data.end);

        int i;
        for(i = 0; i < data.text.length() && data.text.charAt(i) == 32; ++i) {
            ;
        }

        return data.text.substring(0, i);
    }

    private void setCursor(int i, boolean select) {
        i = Math.min(Math.max(i, 0), this.text.length());
        if(i != this.cursorPosition) {
            if(!select) {
                this.endSelection = this.startSelection = this.cursorPosition = i;
            } else {
                int diff = this.cursorPosition - i;
                if(this.cursorPosition == this.startSelection) {
                    this.startSelection -= diff;
                } else if(this.cursorPosition == this.endSelection) {
                    this.endSelection -= diff;
                }

                if(this.startSelection > this.endSelection) {
                    int j = this.endSelection;
                    this.endSelection = this.startSelection;
                    this.startSelection = j;
                }

                this.cursorPosition = i;
            }
        }
    }

    private void addText(String s) {
        this.setText(this.getSelectionBeforeText() + s + this.getSelectionAfterText());
        this.endSelection = this.startSelection = this.cursorPosition = this.startSelection + s.length();
    }

    private int cursorUp() {
        for(int i = 0; i < this.container.lines.size(); ++i) {
            LineData data = (LineData)this.container.lines.get(i);
            if(this.cursorPosition >= data.start && this.cursorPosition < data.end) {
                if(i == 0) {
                    return 0;
                }

                int var10000 = this.cursorPosition - data.start;
                return this.getSelectionPos(this.x + 1 + ClientProxy.Font.width(data.text.substring(0, this.cursorPosition - data.start)), this.y + 1 + (i - 1 - this.scrolledLine) * this.container.lineHeight);
            }
        }

        return 0;
    }

    private int cursorDown() {
        for(int i = 0; i < this.container.lines.size(); ++i) {
            LineData data = (LineData)this.container.lines.get(i);
            if(this.cursorPosition >= data.start && this.cursorPosition < data.end) {
                int var10000 = this.cursorPosition - data.start;
                return this.getSelectionPos(this.x + 1 + ClientProxy.Font.width(data.text.substring(0, this.cursorPosition - data.start)), this.y + 1 + (i + 1 - this.scrolledLine) * this.container.lineHeight);
            }
        }

        return this.text.length();
    }

    public String getSelectionBeforeText() {
        return this.startSelection == 0?"":this.text.substring(0, this.startSelection);
    }

    public String getSelectionAfterText() {
        return this.text.substring(this.endSelection);
    }

    public void mouseClicked(int xMouse, int yMouse, int mouseButton) {
        this.active = xMouse >= this.x && xMouse < this.x + this.width && yMouse >= this.y && yMouse < this.y + this.height;
        if(this.active) {
            this.startSelection = this.endSelection = this.cursorPosition = this.getSelectionPos(xMouse, yMouse);
            this.clicked = mouseButton == 0;
            this.doubleClicked = false;
            long time = System.currentTimeMillis();
            if(this.clicked && this.container.linesCount * this.container.lineHeight > this.height && xMouse > this.x + this.width - 8) {
                this.clicked = false;
                this.clickScrolling = true;
            } else if(time - this.lastClicked < 500L) {
                this.doubleClicked = true;
                Matcher m = this.container.regexWord.matcher(this.text);

                while(m.find()) {
                    if(this.cursorPosition > m.start() && this.cursorPosition < m.end()) {
                        this.startSelection = m.start();
                        this.endSelection = m.end();
                        break;
                    }
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
        if(this.text == null || !this.text.equals(text)) {
            if(this.listener != null) {
                this.listener.textUpdate(text);
            }

            if(!this.undoing) {
                this.undoList.add(new GuiScriptTextArea.UndoData(this.text, this.cursorPosition));
                this.redoList.clear();
            }

            this.text = text;
            this.container = new TextContainer(text);
            this.container.init(this.width, this.height);
            if(this.enableCodeHighlighting) {
                this.container.formatCodeText();
            }

            if(this.scrolledLine > this.container.linesCount - this.container.visibleLines) {
                this.scrolledLine = Math.max(0, this.container.linesCount - this.container.visibleLines);
            }

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

    class UndoData {
        public String text;
        public int cursorPosition;

        public UndoData(String text, int cursorPosition) {
            this.text = text;
            this.cursorPosition = cursorPosition;
        }
    }
}
