package noppes.npcs.api.gui;

/**
 * Represents a clickable button component in a custom GUI.
 * Provides methods to control size, label, texture, and state.
 */
public interface IButton extends ICustomGuiComponent {

    /**
     * Returns the button's width.
     *
     * @return the width in pixels.
     */
    int getWidth();

    /**
     * Returns the button's height.
     *
     * @return the height in pixels.
     */
    int getHeight();

    /**
     * Sets the size of the button.
     *
     * @param width  the new width in pixels.
     * @param height the new height in pixels.
     * @return this button instance.
     */
    IButton setSize(int width, int height);

    /**
     * Returns the button's label text.
     *
     * @return the label.
     */
    String getLabel();

    /**
     * Sets the button's label text.
     *
     * @param text the new label.
     * @return this button instance.
     */
    IButton setLabel(String text);

    /**
     * Returns the texture resource location for this button.
     *
     * @return the texture location as a string.
     */
    String getTexture();

    /**
     * Checks if the button has an assigned texture.
     *
     * @return true if a texture is set; false otherwise.
     */
    boolean hasTexture();

    /**
     * Sets the texture resource location for this button.
     *
     * @param texture the texture resource location.
     * @return this button instance.
     */
    IButton setTexture(String texture);

    /**
     * Returns the X offset within the texture.
     *
     * @return the texture X offset.
     */
    int getTextureX();

    /**
     * Returns the Y offset within the texture.
     *
     * @return the texture Y offset.
     */
    int getTextureY();

    /**
     * Sets the texture offset (X and Y) for this button.
     *
     * @param textureX the new X offset.
     * @param textureY the new Y offset.
     * @return this button instance.
     */
    IButton setTextureOffset(int textureX, int textureY);

    /**
     * Sets the scale factor of the button.
     *
     * @param scale the scale factor.
     */
    void setScale(float scale);

    /**
     * Returns the scale factor of the button.
     *
     * @return the scale.
     */
    float getScale();

    /**
     * Enables or disables the button.
     *
     * @param enabled true to enable; false to disable.
     */
    void setEnabled(boolean enabled);

    /**
     * Checks if the button is enabled.
     *
     * @return true if enabled; false otherwise.
     */
    boolean isEnabled();
}
