//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.client.gui.util;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraftforge.client.GuiIngameForge;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.client.gui.util.TextContainer.LineData;
import noppes.npcs.config.TrueTypeFont;
import org.lwjgl.input.Mouse;

public class GuiTextArea extends Gui implements IGui, IKeyListener, IMouseListener {
    public int id;
    public int x;
    public int y;
    public int width;
    public int height;
    private int cursorCounter;
    private ITextChangeListener listener;
    private static TrueTypeFont font;
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
    private static final char colorChar = '\uffff';
    public List<GuiTextArea.UndoData> undoList = new ArrayList();
    public List<GuiTextArea.UndoData> redoList = new ArrayList();
    public boolean undoing = false;
    private long lastClicked = 0L;

    public GuiTextArea(int id, int x, int y, int width, int height, String text) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.undoing = true;
        this.setText(text);
        this.undoing = false;
        font.setSpecial('\uffff');
    }

    public void drawScreen(int xMouse, int yMouse) {
        if(this.visible) {
            drawRect(this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, -6250336);
            drawRect(this.x, this.y, this.x + this.width, this.y + this.height, -16777216);
            this.container.visibleLines = this.height / this.container.lineHeight;
            int startBracket;
            if(this.clicked) {
                this.clicked = Mouse.isButtonDown(0);
                startBracket = this.getSelectionPos(xMouse, yMouse);
                if(startBracket != this.cursorPosition) {
                    if(this.doubleClicked) {
                        this.startSelection = this.endSelection = this.cursorPosition;
                        this.doubleClicked = false;
                    }

                    this.setCursor(startBracket, true);
                }
            } else if(this.doubleClicked) {
                this.doubleClicked = false;
            }

            if(this.clickScrolling) {
                this.clickScrolling = Mouse.isButtonDown(0);
                startBracket = this.container.linesCount - this.container.visibleLines;
                this.scrolledLine = Math.min(Math.max((int)(1.0F * (float)startBracket * (float)(yMouse - this.y) / (float)this.height), 0), startBracket);
            }

            startBracket = 0;
            int endBracket = 0;
            if(this.endSelection - this.startSelection == 1 || this.startSelection == this.endSelection && this.startSelection < this.text.length()) {
                char list = this.text.charAt(this.startSelection);
                int wordHightLight = 0;
                if(list == 123) {
                    wordHightLight = this.findClosingBracket(this.text.substring(this.startSelection), '{', '}');
                } else if(list == 91) {
                    wordHightLight = this.findClosingBracket(this.text.substring(this.startSelection), '[', ']');
                } else if(list == 40) {
                    wordHightLight = this.findClosingBracket(this.text.substring(this.startSelection), '(', ')');
                } else if(list == 125) {
                    wordHightLight = this.findOpeningBracket(this.text.substring(0, this.startSelection + 1), '{', '}');
                } else if(list == 93) {
                    wordHightLight = this.findOpeningBracket(this.text.substring(0, this.startSelection + 1), '[', ']');
                } else if(list == 41) {
                    wordHightLight = this.findOpeningBracket(this.text.substring(0, this.startSelection + 1), '(', ')');
                }

                if(wordHightLight != 0) {
                    startBracket = this.startSelection;
                    endBracket = this.startSelection + wordHightLight;
                }
            }

            ArrayList var15 = new ArrayList(this.container.lines);
            String var18 = null;
            if(this.startSelection != this.endSelection) {
                Matcher sbSize = this.container.regexWord.matcher(this.text);

                while(sbSize.find()) {
                    if(sbSize.start() == this.startSelection && sbSize.end() == this.endSelection) {
                        var18 = this.text.substring(this.startSelection, this.endSelection);
                    }
                }
            }

            int var16;
            for(var16 = 0; var16 < var15.size(); ++var16) {
                LineData posX = (LineData)var15.get(var16);
                String posY = posX.text;
                int w = posY.length();
                int yPos;
                int posX1;
                int posY1;
                if(startBracket != endBracket) {
                    if(startBracket >= posX.start && startBracket < posX.end) {
                        yPos = font.width(posY.substring(0, startBracket - posX.start));
                        posX1 = font.width(posY.substring(0, startBracket - posX.start + 1)) + 1;
                        posY1 = this.y + 1 + (var16 - this.scrolledLine) * this.container.lineHeight;
                        drawRect(this.x + 1 + yPos, posY1, this.x + 1 + posX1, posY1 + this.container.lineHeight + 1, -1728001024);
                    }

                    if(endBracket >= posX.start && endBracket < posX.end) {
                        yPos = font.width(posY.substring(0, endBracket - posX.start));
                        posX1 = font.width(posY.substring(0, endBracket - posX.start + 1)) + 1;
                        posY1 = this.y + 1 + (var16 - this.scrolledLine) * this.container.lineHeight;
                        drawRect(this.x + 1 + yPos, posY1, this.x + 1 + posX1, posY1 + this.container.lineHeight + 1, -1728001024);
                    }
                }

                if(var16 >= this.scrolledLine && var16 < this.scrolledLine + this.container.visibleLines) {
                    if(var18 != null) {
                        Matcher var20 = this.container.regexWord.matcher(posY);

                        while(var20.find()) {
                            if(posY.substring(var20.start(), var20.end()).equals(var18)) {
                                posX1 = font.width(posY.substring(0, var20.start()));
                                posY1 = font.width(posY.substring(0, var20.end())) + 1;
                                int posY2 = this.y + 1 + (var16 - this.scrolledLine) * this.container.lineHeight;
                                drawRect(this.x + 1 + posX1, posY2, this.x + 1 + posY1, posY2 + this.container.lineHeight + 1, -1728033792);
                            }
                        }
                    }

                    if(this.startSelection != this.endSelection && this.endSelection > posX.start && this.startSelection <= posX.end && this.startSelection < posX.end) {
                        yPos = font.width(posY.substring(0, Math.max(this.startSelection - posX.start, 0)));
                        posX1 = font.width(posY.substring(0, Math.min(this.endSelection - posX.start, w))) + 1;
                        posY1 = this.y + 1 + (var16 - this.scrolledLine) * this.container.lineHeight;
                        drawRect(this.x + 1 + yPos, posY1, this.x + 1 + posX1, posY1 + this.container.lineHeight + 1, -1728052993);
                    }

                    yPos = this.y + (var16 - this.scrolledLine) * this.container.lineHeight + 1;
                    font.draw(posX.getFormattedString(), (float)(this.x + 1), (float)yPos, -2039584);
                    if(this.active && this.isEnabled() && this.cursorCounter / 6 % 2 == 0 && this.cursorPosition >= posX.start && this.cursorPosition < posX.end) {
                        posX1 = this.x + font.width(posY.substring(0, this.cursorPosition - posX.start));
                        drawRect(posX1 + 1, yPos, posX1 + 2, yPos + 1 + this.container.lineHeight, -3092272);
                    }
                }
            }

            if(this.hasVerticalScrollbar()) {
                Minecraft.getMinecraft().renderEngine.bindTexture(GuiCustomScroll.resource);
                
                var16 = Math.max((int)(1.0F * (float)this.container.visibleLines / (float)this.container.linesCount * (float)this.height), 2);
                int var17 = this.x + this.width - 6;
                int var19 = (int)((float)this.y + 1.0F * (float)this.scrolledLine / (float)this.container.linesCount * (float)(this.height - 4)) + 1;
                drawRect(var17, var19, var17 + 5, var19 + var16, -2039584);
            }

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
                        int w = font.width(data.text.substring(0, j));
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

    public int getID() {
        return this.id;
    }


    public void keyTyped(char c, int i) {
        if(this.active && this.isEnabled()) {
            if(i == 15) {
                this.addText("    ");
            }

            if(i == 28) {
                this.addText(Character.toString('\n') + this.getIndentCurrentLine());
            }

            if(ChatAllowedCharacters.isAllowedCharacter(c)) {
                this.addText(Character.toString(c));
            }
        }
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
                return this.getSelectionPos(this.x + 1 + font.width(data.text.substring(0, this.cursorPosition - data.start)), this.y + 1 + (i - 1 - this.scrolledLine) * this.container.lineHeight);
            }
        }

        return 0;
    }

    private int cursorDown() {
        for(int i = 0; i < this.container.lines.size(); ++i) {
            LineData data = (LineData)this.container.lines.get(i);
            if(this.cursorPosition >= data.start && this.cursorPosition < data.end) {
                int var10000 = this.cursorPosition - data.start;
                return this.getSelectionPos(this.x + 1 + font.width(data.text.substring(0, this.cursorPosition - data.start)), this.y + 1 + (i + 1 - this.scrolledLine) * this.container.lineHeight);
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

    public boolean mouseClicked(int xMouse, int yMouse, int mouseButton) {
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
        }

        return this.active;
    }

    public void updateScreen() {
        ++this.cursorCounter;
        int k2 = Mouse.getDWheel();
        if(k2 != 0) {
            this.scrolledLine += k2 > 0?-1:1;
            this.scrolledLine = Math.max(Math.min(this.scrolledLine, this.container.linesCount - this.height / this.container.lineHeight), 0);
        }

    }

    public void setText(String text) {
        text = text.replace("\r", "");
        if(this.text == null || !this.text.equals(text)) {
            if(this.listener != null) {
                this.listener.textUpdate(text);
            }

            if(!this.undoing) {
                this.undoList.add(new GuiTextArea.UndoData(this.text, this.cursorPosition));
                this.redoList.clear();
            }

            this.text = text;
            this.container = new TextContainer(text);
            this.container.init(font, this.width, this.height);
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

    public boolean isActive() {
        return this.active;
    }

    static {
        font = new TrueTypeFont(new Font("Arial Unicode MS", 0, CustomNpcs.FontSize), 1.0F);
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
