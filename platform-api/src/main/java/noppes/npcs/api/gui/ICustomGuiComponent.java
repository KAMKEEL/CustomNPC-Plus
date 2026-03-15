package noppes.npcs.api.gui;


/**
 * Base interface for all custom GUI components.
 * Defines common methods for position, appearance, and NBT serialization.
 */
public interface ICustomGuiComponent {

    /**
     * Returns the unique ID of this component.
     *
     * @return the component ID.
     */
    int getID();

    /**
     * Sets the unique ID for this component.
     *
     * @param id the new ID.
     * @return this component instance.
     */
    ICustomGuiComponent setID(int id);

    /**
     * Returns the x position of this component.
     *
     * @return the x position.
     */
    int getPosX();

    /**
     * Returns the y position of this component.
     *
     * @return the y position.
     */
    int getPosY();

    /**
     * Sets the position of the component.
     *
     * @param x the new x position.
     * @param y the new y position.
     * @return this component instance.
     */
    ICustomGuiComponent setPos(int x, int y);

    /**
     * Checks if the component has hover text.
     *
     * @return true if hover text is set.
     */
    boolean hasHoverText();

    /**
     * Returns the hover text as an array of strings.
     *
     * @return the hover text.
     */
    String[] getHoverText();

    /**
     * Sets the hover text with a single line.
     *
     * @param hoverText the hover text.
     * @return this component instance.
     */
    ICustomGuiComponent setHoverText(String hoverText);

    /**
     * Sets the hover text with multiple lines.
     *
     * @param hoverTextLines the hover text lines.
     * @return this component instance.
     */
    ICustomGuiComponent setHoverText(String[] hoverTextLines);

    /**
     * Returns the text color.
     *
     * @return the color as an integer.
     */
    int getColor();

    /**
     * Sets the text color.
     *
     * @param color the color.
     * @return this component instance.
     */
    ICustomGuiComponent setColor(int color);

    /**
     * Returns the component's transparency (alpha).
     *
     * @return the alpha value.
     */
    float getAlpha();

    /**
     * Sets the component's transparency (alpha).
     *
     * @param alpha the alpha value.
     */
    void setAlpha(float alpha);

    /**
     * Returns the component's rotation.
     *
     * @return the rotation angle.
     */
    float getRotation();

    /**
     * Sets the component's rotation.
     *
     * @param rotation the rotation angle.
     */
    void setRotation(float rotation);

    /**
     * Serializes the component to an NBT compound.
     *
     * @param nbt the NBT compound to populate.
     * @return the NBT compound.
     */
    Object toNBT(Object nbt);

    /**
     * Deserializes the component from an NBT compound.
     *
     * @param nbt the NBT compound.
     * @return this component instance.
     */
    ICustomGuiComponent fromNBT(Object nbt);
}
