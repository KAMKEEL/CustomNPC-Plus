package noppes.npcs.api.handler.data;

public interface IDialogImage {
    /**
     * @return The unique ID of this dialog image
     */
    int getId();

    /**
     * Sets the texture resource location for this image.
     * @param texture The resource location string
     */
    void setTexture(String texture);

    /**
     * @return The texture resource location for this image
     */
    String getTexture();

    /**
     * Sets the screen position of this image.
     * @param x The X position in pixels
     * @param y The Y position in pixels
     */
    void setPosition(int x, int y);

    /**
     * @return The X position of this image in pixels
     */
    int getX();

    /**
     * @return The Y position of this image in pixels
     */
    int getY();

    /**
     * Sets the rendered width and height of this image.
     * @param width The width in pixels
     * @param height The height in pixels
     */
    void setWidthHeight(int width, int height);

    /**
     * @return The rendered width of this image in pixels
     */
    int getWidth();

    /**
     * @return The rendered height of this image in pixels
     */
    int getHeight();

    /**
     * Sets the texture UV offset for this image.
     * @param offsetX The horizontal texture offset in pixels
     * @param offsetY The vertical texture offset in pixels
     */
    void setTextureOffset(int offsetX, int offsetY);

    /**
     * @return The horizontal texture UV offset in pixels
     */
    int getTextureX();

    /**
     * @return The vertical texture UV offset in pixels
     */
    int getTextureY();

    /**
     * Sets the tint color of this image.
     * @param color The color as an integer (e.g. 0xFFFFFF)
     */
    void setColor(int color);

    /**
     * @return The tint color of this image
     */
    int getColor();

    /**
     * Sets the tint color used when this image is selected/hovered.
     * @param color The selected color as an integer (e.g. 0xFFFFFF)
     */
    void setSelectedColor(int color);

    /**
     * @return The tint color used when this image is selected/hovered
     */
    int getSelectedColor();

    /**
     * Sets the render scale of this image.
     * @param scale The scale factor (default 1.0)
     */
    void setScale(float scale);

    /**
     * @return The render scale of this image
     */
    float getScale();

    /**
     * Sets the alpha transparency of this image.
     * @param alpha The alpha value from 0.0 (transparent) to 1.0 (opaque)
     */
    void setAlpha(float alpha);

    /**
     * @return The alpha transparency of this image (0.0 to 1.0)
     */
    float getAlpha();

    /**
     * Sets the rotation angle of this image.
     * @param rotation The rotation in degrees
     */
    void setRotation(float rotation);

    /**
     * @return The rotation angle of this image in degrees
     */
    float getRotation();

    /**
     * Sets the image type which determines where the image is rendered.
     * @param imageType The image type: 0 = Default, 1 = Text, 2 = Option
     */
    void setImageType(int imageType);

    /**
     * @return The image type: 0 = Default, 1 = Text, 2 = Option
     */
    int getImageType();

    /**
     * Sets the alignment of this image.
     * @param alignment The alignment value
     */
    void setAlignment(int alignment);

    /**
     * @return The alignment value of this image
     */
    int getAlignment();
}
