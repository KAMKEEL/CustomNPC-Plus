package noppes.npcs.api.gui;

/**
 * Represents a text field component in a custom GUI.
 * Provides methods to get or set its size and text content.
 */
public interface ITextField extends ICustomGuiComponent {

    /**
     * Returns the width of the text field.
     *
     * @return the width in pixels.
     */
    int getWidth();

    /**
     * Returns the height of the text field.
     *
     * @return the height in pixels.
     */
    int getHeight();

    /**
     * Sets the size of the text field.
     *
     * @param width  the new width.
     * @param height the new height.
     * @return this text field instance.
     */
    ITextField setSize(int width, int height);

    /**
     * Returns the current text content of the text field.
     *
     * @return the text.
     */
    String getText();

    /**
     * Sets the text content of the text field.
     *
     * @param text the new text.
     * @return this text field instance.
     */
    ITextField setText(String text);
}
