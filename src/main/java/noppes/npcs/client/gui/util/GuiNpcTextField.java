package noppes.npcs.client.gui.util;

import kamkeel.npcs.util.TextSplitter;
import kamkeel.npcs.util.FileNameHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.ArrayList;
import java.util.List;

public class GuiNpcTextField extends GuiTextField {
    public boolean enabled = true;
    public boolean inMenu = true;
    public boolean integersOnly = false;
    public boolean doublesOnly = false;
    public boolean floatsOnly = false;
    private ITextfieldListener listener;
    public int id;
    public int min = 0, max = Integer.MAX_VALUE, def = 0;
    public double minDouble = 0, maxDouble = Double.MAX_VALUE, defDouble = 0;
    public float minFloat = 0, maxFloat = Float.MAX_VALUE, defFloat = 0;
    protected static GuiNpcTextField activeTextfield = null;
    public boolean canEdit = true;
    private boolean fileNameSafe = false;
    public String hoverableText = "";
    private boolean wasHovered = false;
    private int hoverCount = 0;

    private final int[] allowedSpecialChars = {14, 211, 203, 205, 199, 207};

    public GuiNpcTextField(int id, GuiScreen parent, FontRenderer fontRenderer, int i, int j, int k, int l, String s) {
        super(fontRenderer, i, j, k, l);
        setMaxStringLength(500);
        this.setText(s);
        setCursorPositionZero();
        this.id = id;
        if (parent instanceof ITextfieldListener)
            listener = (ITextfieldListener) parent;
    }

    public static boolean isFieldActive() {
        return activeTextfield != null;
    }

    public GuiNpcTextField(int id, GuiScreen parent, int i, int j, int k, int l, String s) {
        this(id, parent, Minecraft.getMinecraft().fontRenderer, i, j, k, l, s);
    }

    public void initGui() {
    }

    protected boolean charAllowed(char c, int i) {
        if (!integersOnly || Character.isDigit(c))
            return true;

        if (integersOnly && getText().isEmpty() && c == '-')
            return true;

        for (int j : allowedSpecialChars)
            if (j == i)
                return true;

        return false;
    }

    @Override
    public boolean textboxKeyTyped(char c, int i) {
        if (!canEdit || !isFocused())
            return false;

        if (fileNameSafe && !GuiScreen.isCtrlKeyDown() && !isFileNameCharAllowed(c, i)) {
            return false;
        }

        // Allow Ctrl shortcuts (select all, copy, paste, cut) to bypass char filter
        if (GuiScreen.isCtrlKeyDown()) {
            // For Ctrl+V (paste) on numeric fields, filter clipboard to valid chars
            if (i == 47 && isNumericField()) {
                String clipboard = GuiScreen.getClipboardString();
                if (clipboard != null) {
                    StringBuilder filtered = new StringBuilder();
                    for (char ch : clipboard.toCharArray()) {
                        if (Character.isDigit(ch) || ch == '-' || ((doublesOnly || floatsOnly) && ch == '.'))
                            filtered.append(ch);
                    }
                    if (filtered.length() > 0)
                        writeText(filtered.toString());
                }
                return true;
            }
            if (i == 47 && fileNameSafe) {
                String clipboard = GuiScreen.getClipboardString();
                if (clipboard != null) {
                    String filtered = filterFileNameClipboard(clipboard);
                    if (!filtered.isEmpty())
                        writeText(filtered);
                }
                return true;
            }
            boolean handled = super.textboxKeyTyped(c, i);
            return handled;
        }
        if (!charAllowed(c, i))
            return false;
        return super.textboxKeyTyped(c, i);
    }

    private boolean isNumericField() {
        return integersOnly || doublesOnly || floatsOnly;
    }

    public boolean isEmpty() {
        return getText().trim().length() == 0;
    }

    public int getInteger() {
        return Integer.parseInt(getText());
    }

    public boolean isInteger() {
        try {
            Integer.parseInt(getText());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public double getDouble() {
        return Double.parseDouble(getText());
    }

    public boolean isDouble() {
        try {
            Double.parseDouble(getText());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public float getFloat() {
        return Float.parseFloat(getText());
    }

    public boolean isFloat() {
        try {
            Float.parseFloat(getText());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void mouseClicked(int i, int j, int k) {
        if (!canEdit)
            return;
        boolean wasFocused = this.isFocused();
        super.mouseClicked(i, j, k);
        if (wasFocused != isFocused()) {
            if (wasFocused) {
                unFocused();
            }
        }
        if (isFocused())
            activeTextfield = this;
    }

    public void unFocused() {
        if (integersOnly && !doublesOnly && !floatsOnly) {
            if (isEmpty() || !isInteger())
                setText(def + "");
            else if (getInteger() < min)
                setText(min + "");
            else if (getInteger() > max)
                setText(max + "");
        } else if (doublesOnly && !floatsOnly) {
            if (isEmpty() || !isDouble())
                setText(defDouble + "");
            else if (getDouble() < minDouble)
                setText(minDouble + "");
            else if (getDouble() > maxDouble)
                setText(maxDouble + "");
        } else if (floatsOnly) {
            if (isEmpty() || !isFloat())
                setText(defFloat + "");
            else if (getFloat() < minFloat)
                setText(minFloat + "");
            else if (getFloat() > maxFloat)
                setText(maxFloat + "");
        }
        if (fileNameSafe) {
            normalizeFileNameText();
        }
        setCursorPositionZero();
        if (listener != null)
            listener.unFocused(this);

        if (this == activeTextfield)
            activeTextfield = null;
    }

    @Override
    public void drawTextBox() {
        if (enabled)
            super.drawTextBox();
    }

    public void setMinMaxDefault(int min, int max, int def) {
        this.min = min;
        this.max = max;
        this.def = def;
    }

    public void setMinMaxDefaultDouble(double min, double max, double def) {
        minDouble = min;
        maxDouble = max;
        defDouble = def;
    }

    public void setMinMaxDefaultFloat(float min, float max, float def) {
        minFloat = min;
        maxFloat = max;
        defFloat = def;
    }

    public static void unfocus() {
        GuiNpcTextField prev = activeTextfield;
        activeTextfield = null;
        if (prev != null) {
            prev.unFocused();
        }
    }

    public void drawTextBox(int mousX, int mousY) {
        drawTextBox();
    }

    public GuiNpcTextField setIntegersOnly() {
        integersOnly = true;
        return this;
    }

    public GuiNpcTextField setDoublesOnly() {
        doublesOnly = true;
        return this;
    }

    public GuiNpcTextField setFloatsOnly() {
        floatsOnly = true;
        return this;
    }

    public GuiNpcTextField setFileNameSafe() {
        fileNameSafe = true;
        normalizeFileNameText();
        return this;
    }

    private boolean isSpecialNavigationKey(int keyCode) {
        for (int allowed : allowedSpecialChars) {
            if (allowed == keyCode) {
                return true;
            }
        }
        return false;
    }

    private boolean isFileNameCharAllowed(char c, int keyCode) {
        if (isSpecialNavigationKey(keyCode) || c < 32) {
            return true;
        }
        return Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == ' ';
    }

    private String filterFileNameClipboard(String clipboard) {
        StringBuilder filtered = new StringBuilder(clipboard.length());
        for (int idx = 0; idx < clipboard.length(); idx++) {
            char c = clipboard.charAt(idx);
            if (Character.isLetterOrDigit(c) || c == '_' || c == '-') {
                filtered.append(c);
            } else if (Character.isWhitespace(c)) {
                filtered.append(' ');
            }
        }
        return filtered.toString();
    }

    private void normalizeFileNameText() {
        String current = getText();
        String sanitized = FileNameHelper.sanitizeTextInput(current);
        if (sanitized.equals(current)) return;

        int cursor = getCursorPosition();
        setText(sanitized);
        setCursorPosition(Math.min(cursor, sanitized.length()));
    }

    public void setHoverText(String text) {
        String translated = StatCollector.translateToLocal(text);
        this.hoverableText = translated == null ? "" : translated.replace("\\n", "\n");
    }

    public boolean hasHoverText() {
        return hoverableText != null && !hoverableText.isEmpty();
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= xPosition && mouseX < xPosition + width &&
            mouseY >= yPosition && mouseY < yPosition + height;
    }

    public void drawHover(int mouseX, int mouseY, boolean hasSubGui) {
        drawHover(mouseX, mouseY, mouseX, mouseY, hasSubGui);
    }

    public void drawHover(int hitMouseX, int hitMouseY, int tooltipMouseX, int tooltipMouseY, boolean hasSubGui) {
        if (hasSubGui || !enabled || hoverableText.isEmpty())
            return;

        boolean isHovered = isMouseOver(hitMouseX, hitMouseY);
        if (!isHovered) {
            wasHovered = false;
            hoverCount = 0;
            return;
        }

        if (!wasHovered) {
            wasHovered = true;
            hoverCount = 0;
        } else if (hoverCount < 65) {
            hoverCount++;
        }

        if (hoverCount > 60) {
            GL11.glPushMatrix();
            Minecraft mc = Minecraft.getMinecraft();
            GL11.glColor4f(1, 1, 1, 1);
            List<String> lines = splitHoverText(hoverableText, 30);
            drawHoveringText(lines, tooltipMouseX, tooltipMouseY, mc);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glPopMatrix();
        }
    }

    private List<String> splitHoverText(String text, int maxLineLength) {
        List<String> lines = new ArrayList<String>();
        if (text == null || text.isEmpty())
            return lines;

        String normalized = text.replace("\\n", "\n");
        String[] explicitLines = normalized.split("\\r?\\n", -1);
        for (String line : explicitLines) {
            if (line.isEmpty()) {
                lines.add("");
                continue;
            }
            lines.addAll(TextSplitter.splitText(line, maxLineLength));
        }
        return lines;
    }

    protected void drawHoveringText(List<String> textLines, int x, int y, Minecraft mc) {
        if (mc.fontRenderer == null || textLines.isEmpty())
            return;
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        int maxWidth = 0;
        for (String s : textLines) {
            int lineWidth = mc.fontRenderer.getStringWidth(s);
            if (lineWidth > maxWidth) {
                maxWidth = lineWidth;
            }
        }

        int j2 = x + 12;
        int k2 = y - 12;
        int maxHeight = 8;

        if (textLines.size() > 1) {
            maxHeight += 2 + (textLines.size() - 1) * 10;
        }

        // Get scaled screen dimensions
        ScaledResolution scaledRes = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int screenWidth = scaledRes.getScaledWidth();
        int screenHeight = scaledRes.getScaledHeight();

        // If tooltip would go off right edge, draw on left side of mouse
        if (j2 + maxWidth + 6 > screenWidth) {
            j2 = x - maxWidth - 16;
        }

        // Keep tooltip on screen horizontally
        if (j2 < 4) {
            j2 = 4;
        }

        // Keep tooltip on screen vertically
        if (k2 + maxHeight + 6 > screenHeight) {
            k2 = screenHeight - maxHeight - 6;
        }
        if (k2 < 4) {
            k2 = 4;
        }

        int zLevel = 300;
        int bgColor = 0xF0100010;
        int borderColor1 = 0x505000FF;
        int borderColor2 = (borderColor1 & 0xFEFEFE) >> 1 | borderColor1 & 0xFF000000;

        drawGradientRect(j2 - 3, k2 - 4, j2 + maxWidth + 3, k2 - 3, bgColor, bgColor, zLevel);
        drawGradientRect(j2 - 3, k2 + maxHeight + 3, j2 + maxWidth + 3, k2 + maxHeight + 4, bgColor, bgColor, zLevel);
        drawGradientRect(j2 - 3, k2 - 3, j2 + maxWidth + 3, k2 + maxHeight + 3, bgColor, bgColor, zLevel);
        drawGradientRect(j2 - 4, k2 - 3, j2 - 3, k2 + maxHeight + 3, bgColor, bgColor, zLevel);
        drawGradientRect(j2 + maxWidth + 3, k2 - 3, j2 + maxWidth + 4, k2 + maxHeight + 3, bgColor, bgColor, zLevel);
        drawGradientRect(j2 - 3, k2 - 3 + 1, j2 - 3 + 1, k2 + maxHeight + 3 - 1, borderColor1, borderColor2, zLevel);
        drawGradientRect(j2 + maxWidth + 2, k2 - 3 + 1, j2 + maxWidth + 3, k2 + maxHeight + 3 - 1, borderColor1, borderColor2, zLevel);
        drawGradientRect(j2 - 3, k2 - 3, j2 + maxWidth + 3, k2 - 3 + 1, borderColor1, borderColor1, zLevel);
        drawGradientRect(j2 - 3, k2 + maxHeight + 2, j2 + maxWidth + 3, k2 + maxHeight + 3, borderColor2, borderColor2, zLevel);

        for (int l = 0; l < textLines.size(); ++l) {
            String s1 = textLines.get(l);
            mc.fontRenderer.drawStringWithShadow(s1, j2, k2, -1);
            if (l == 0) {
                k2 += 2;
            }
            k2 += 10;
        }

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        RenderHelper.enableStandardItemLighting();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
    }

    protected void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor, int zLevel) {
        float f = (float) (startColor >> 24 & 255) / 255.0F;
        float f1 = (float) (startColor >> 16 & 255) / 255.0F;
        float f2 = (float) (startColor >> 8 & 255) / 255.0F;
        float f3 = (float) (startColor & 255) / 255.0F;
        float f4 = (float) (endColor >> 24 & 255) / 255.0F;
        float f5 = (float) (endColor >> 16 & 255) / 255.0F;
        float f6 = (float) (endColor >> 8 & 255) / 255.0F;
        float f7 = (float) (endColor & 255) / 255.0F;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        net.minecraft.client.renderer.Tessellator tessellator = net.minecraft.client.renderer.Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_F(f1, f2, f3, f);
        tessellator.addVertex((double) right, (double) top, (double) zLevel);
        tessellator.addVertex((double) left, (double) top, (double) zLevel);
        tessellator.setColorRGBA_F(f5, f6, f7, f4);
        tessellator.addVertex((double) left, (double) bottom, (double) zLevel);
        tessellator.addVertex((double) right, (double) bottom, (double) zLevel);
        tessellator.draw();
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }
}
