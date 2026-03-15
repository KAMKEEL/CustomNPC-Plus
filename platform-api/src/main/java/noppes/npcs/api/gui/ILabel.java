package noppes.npcs.api.gui;

/**
 * Represents a text label component in a custom GUI.
 * Provides methods to get or set text, size, scale, and shadow properties.
 */
public interface ILabel extends ICustomGuiComponent {

    /**
     * Returns the text of the label.
     *
     * @return the label text.
     */
    String getText();

    /**
     * Sets the label text.
     *
     * @param text the new text.
     * @return this label instance.
     */
    ILabel setText(String text);

    /**
     * Returns the label's width.
     *
     * @return the width in pixels.
     */
    int getWidth();

    /**
     * Returns the label's height.
     *
     * @return the height in pixels.
     */
    int getHeight();

    /**
     * Sets the size of the label.
     *
     * @param width  the new width.
     * @param height the new height.
     * @return this label instance.
     */
    ILabel setSize(int width, int height);

    /**
     * Returns the scale factor of the label.
     *
     * @return the scale.
     */
    float getScale();

    /**
     * Sets the scale factor of the label.
     *
     * @param scale the new scale.
     * @return this label instance.
     */
    ILabel setScale(float scale);

    /**
     * Returns whether the label text is rendered with a shadow.
     *
     * @return true if shadow is enabled; false otherwise.
     */
    boolean getShadow();

    /**
     * Sets whether the label text should be rendered with a shadow.
     *
     * @param shadow true to enable shadow.
     */
    void setShadow(boolean shadow);
}
