/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

/**
 * @javaFqn noppes.npcs.api.handler.data.IDialogImage
 */
export interface IDialogImage {
    /**
     * @return The unique ID of this dialog image
     */
    getId(): import('./int').int;
    /**
     * Sets the texture resource location for this image.
     * @param texture The resource location string
     */
    setTexture(texture: String): import('./void').void;
    /**
     * @return The texture resource location for this image
     */
    getTexture(): String;
    /**
     * Sets the screen position of this image.
     * @param x The X position in pixels
     * @param y The Y position in pixels
     */
    setPosition(x: import('./int').int, y: import('./int').int): import('./void').void;
    /**
     * @return The X position of this image in pixels
     */
    getX(): import('./int').int;
    /**
     * @return The Y position of this image in pixels
     */
    getY(): import('./int').int;
    /**
     * Sets the rendered width and height of this image.
     * @param width The width in pixels
     * @param height The height in pixels
     */
    setWidthHeight(width: import('./int').int, height: import('./int').int): import('./void').void;
    /**
     * @return The rendered width of this image in pixels
     */
    getWidth(): import('./int').int;
    /**
     * @return The rendered height of this image in pixels
     */
    getHeight(): import('./int').int;
    /**
     * Sets the texture UV offset for this image.
     * @param offsetX The horizontal texture offset in pixels
     * @param offsetY The vertical texture offset in pixels
     */
    setTextureOffset(offsetX: import('./int').int, offsetY: import('./int').int): import('./void').void;
    /**
     * @return The horizontal texture UV offset in pixels
     */
    getTextureX(): import('./int').int;
    /**
     * @return The vertical texture UV offset in pixels
     */
    getTextureY(): import('./int').int;
    /**
     * Sets the tint color of this image.
     * @param color The color as an integer (e.g. 0xFFFFFF)
     */
    setColor(color: import('./int').int): import('./void').void;
    /**
     * @return The tint color of this image
     */
    getColor(): import('./int').int;
    /**
     * Sets the tint color used when this image is selected/hovered.
     * @param color The selected color as an integer (e.g. 0xFFFFFF)
     */
    setSelectedColor(color: import('./int').int): import('./void').void;
    /**
     * @return The tint color used when this image is selected/hovered
     */
    getSelectedColor(): import('./int').int;
    /**
     * Sets the render scale of this image.
     * @param scale The scale factor (default 1.0)
     */
    setScale(scale: import('./float').float): import('./void').void;
    /**
     * @return The render scale of this image
     */
    getScale(): import('./float').float;
    /**
     * Sets the alpha transparency of this image.
     * @param alpha The alpha value from 0.0 (transparent) to 1.0 (opaque)
     */
    setAlpha(alpha: import('./float').float): import('./void').void;
    /**
     * @return The alpha transparency of this image (0.0 to 1.0)
     */
    getAlpha(): import('./float').float;
    /**
     * Sets the rotation angle of this image.
     * @param rotation The rotation in degrees
     */
    setRotation(rotation: import('./float').float): import('./void').void;
    /**
     * @return The rotation angle of this image in degrees
     */
    getRotation(): import('./float').float;
    /**
     * Sets the image type which determines where the image is rendered.
     * @param imageType The image type: 0 = Default, 1 = Text, 2 = Option
     */
    setImageType(imageType: import('./int').int): import('./void').void;
    /**
     * @return The image type: 0 = Default, 1 = Text, 2 = Option
     */
    getImageType(): import('./int').int;
    /**
     * Sets the alignment of this image.
     * @param alignment The alignment value
     */
    setAlignment(alignment: import('./int').int): import('./void').void;
    /**
     * @return The alignment value of this image
     */
    getAlignment(): import('./int').int;
}
