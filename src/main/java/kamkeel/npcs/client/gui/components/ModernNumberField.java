package kamkeel.npcs.client.gui.components;

/**
 * A text field specialized for numeric input.
 * Supports integers and floats with min/max validation.
 * Leverages GuiNpcTextField's existing numeric support.
 */
public class ModernNumberField extends ModernTextField {

    public ModernNumberField(int id, int x, int y, int width, int height) {
        super(id, x, y, width, height);
        setText("0");
        integersOnly = true;
    }

    public ModernNumberField(int id, int x, int y, int width, int height, int value) {
        super(id, x, y, width, height);
        integersOnly = true;
        setText(String.valueOf(value));
    }

    public ModernNumberField(int id, int x, int y, int width, int height, float value) {
        super(id, x, y, width, height);
        floatsOnly = true;
        setText(String.valueOf(value));
    }

    /**
     * Configure for integer input with min/max bounds.
     */
    public ModernNumberField setIntegerBounds(int min, int max, int defaultVal) {
        integersOnly = true;
        floatsOnly = false;
        doublesOnly = false;
        setMinMaxDefault(min, max, defaultVal);
        return this;
    }

    /**
     * Configure for float input with min/max bounds.
     */
    public ModernNumberField setFloatBounds(float min, float max, float defaultVal) {
        floatsOnly = true;
        integersOnly = false;
        doublesOnly = false;
        setMinMaxDefaultFloat(min, max, defaultVal);
        return this;
    }

    /**
     * Set the value as an integer.
     */
    public void setInteger(int value) {
        value = Math.max(min, Math.min(max, value));
        setText(String.valueOf(value));
    }

    /**
     * Set the value as a float.
     */
    public void setFloat(float value) {
        value = Math.max(minFloat, Math.min(maxFloat, value));
        setText(String.valueOf(value));
    }

    public void setIntegersOnly(boolean integersOnly) {
        this.integersOnly = integersOnly;
    }
}
