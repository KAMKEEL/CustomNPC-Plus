package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import noppes.npcs.util.ValueUtil;

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

    private String floatingPrecision = "%.4f"; //4 decimal places

    private final int[] allowedSpecialChars = {0, 1, 3, 14, 8, 22, 24, 199, 211, 203, 205, 207, 211};

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

    protected boolean charAllowed(char c, int i) {
        for (int j : allowedSpecialChars)
            if (j == c)
                return true;

        String s = getText();
        boolean isNumbersOnly = integersOnly || floatsOnly || doublesOnly;
        boolean isDecimal = !integersOnly && c == '.' && !s.contains(".");
        boolean isNegative = isNumbersOnly && c == '-' && (getSelectionEnd() == 0 || !s.contains("-") && getCursorPosition() == 0);

        if (!isNumbersOnly || isNegative || isDecimal || Character.isDigit(c))
            return true;

        return false;
    }

    @Override
    public boolean textboxKeyTyped(char c, int i) {
        if (!charAllowed(c, i) || !canEdit)
            return false;
        return super.textboxKeyTyped(c, i);
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

    public void toDefaults() {
        if (integersOnly && !doublesOnly && !floatsOnly) {
            if (isEmpty() || !isInteger())
                setText(def + "");
            else if (getInteger() < min)
                setText(min + "");
            else if (getInteger() > max)
                setText(max + "");
        } else if (doublesOnly && !floatsOnly) {
            if (floatingPrecision != null)
                setText(ValueUtil.format(floatingPrecision, getDouble()));

            if (isEmpty() || !isDouble())
                setText(defDouble + "");
            else if (getDouble() < minDouble)
                setText(minDouble + "");
            else if (getDouble() > maxDouble)
                setText(maxDouble + "");
        } else if (floatsOnly) {
            if (floatingPrecision != null)
                setText(ValueUtil.format(floatingPrecision, getFloat()));

            if (isEmpty() || !isFloat())
                setText(defFloat + "");
            else if (getFloat() < minFloat)
                setText(minFloat + "");
            else if (getFloat() > maxFloat)
                setText(maxFloat + "");
        }
    }

    public void unFocused() {
        toDefaults();
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

    public GuiNpcTextField setMinMaxDefault(int min, int max, int def) {
        this.min = min;
        this.max = max;
        this.def = def;
        return this;
    }

    public GuiNpcTextField setMinMaxDefaultDouble(double min, double max, double def) {
        minDouble = min;
        maxDouble = max;
        defDouble = def;
        return this;
    }

    public GuiNpcTextField setMinMaxDefaultFloat(float min, float max, float def) {
        minFloat = min;
        maxFloat = max;
        defFloat = def;
        return this;
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

    public void setFloatingPrecision(int decimalPlaces) {
        floatingPrecision = "%." + decimalPlaces + "f";
    }

    public void setInt(int value) {
        setText(ValueUtil.clamp(value, min, max) + "");
    }

    public void setFloat(float value) {
        value = ValueUtil.clamp(value, minFloat, maxFloat);
        setText(ValueUtil.format(floatingPrecision, value));
    }

    public void setDouble(double value) {
        value = ValueUtil.clamp(value, minDouble, maxDouble);
        setText(ValueUtil.format(floatingPrecision, value));
    }

    public boolean isNumbersOnly() {
        return integersOnly || floatsOnly || doublesOnly;
    }

}
