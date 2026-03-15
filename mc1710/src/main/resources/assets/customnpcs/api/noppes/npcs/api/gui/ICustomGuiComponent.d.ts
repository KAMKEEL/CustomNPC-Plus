/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.gui
 */

/**
 * Base interface for all custom GUI components.
 * Defines common methods for position, appearance, and NBT serialization.
  * @javaFqn noppes.npcs.api.gui.ICustomGuiComponent
*/
export interface ICustomGuiComponent {
    /**
     * Returns the unique ID of this component.
     *
     * @return the component ID.
     */
    getID(): import('./int').int;
    /**
     * Sets the unique ID for this component.
     *
     * @param id the new ID.
     * @return this component instance.
     */
    setID(id: import('./int').int): import('./ICustomGuiComponent').ICustomGuiComponent;
    /**
     * Returns the x position of this component.
     *
     * @return the x position.
     */
    getPosX(): import('./int').int;
    /**
     * Returns the y position of this component.
     *
     * @return the y position.
     */
    getPosY(): import('./int').int;
    /**
     * Sets the position of the component.
     *
     * @param x the new x position.
     * @param y the new y position.
     * @return this component instance.
     */
    setPos(x: import('./int').int, y: import('./int').int): import('./ICustomGuiComponent').ICustomGuiComponent;
    /**
     * Checks if the component has hover text.
     *
     * @return true if hover text is set.
     */
    hasHoverText(): import('./boolean').boolean;
    /**
     * Returns the hover text as an array of strings.
     *
     * @return the hover text.
     */
    getHoverText(): String[];
    /**
     * Sets the hover text with a single line.
     *
     * @param hoverText the hover text.
     * @return this component instance.
     */
    setHoverText(hoverText: String): import('./ICustomGuiComponent').ICustomGuiComponent;
    /**
     * Sets the hover text with multiple lines.
     *
     * @param hoverTextLines the hover text lines.
     * @return this component instance.
     */
    setHoverText(hoverTextLines: String[]): import('./ICustomGuiComponent').ICustomGuiComponent;
    /**
     * Returns the text color.
     *
     * @return the color as an integer.
     */
    getColor(): import('./int').int;
    /**
     * Sets the text color.
     *
     * @param color the color.
     * @return this component instance.
     */
    setColor(color: import('./int').int): import('./ICustomGuiComponent').ICustomGuiComponent;
    /**
     * Returns the component's transparency (alpha).
     *
     * @return the alpha value.
     */
    getAlpha(): import('./float').float;
    /**
     * Sets the component's transparency (alpha).
     *
     * @param alpha the alpha value.
     */
    setAlpha(alpha: import('./float').float): import('./void').void;
    /**
     * Returns the component's rotation.
     *
     * @return the rotation angle.
     */
    getRotation(): import('./float').float;
    /**
     * Sets the component's rotation.
     *
     * @param rotation the rotation angle.
     */
    setRotation(rotation: import('./float').float): import('./void').void;
    /**
     * Serializes the component to an NBT compound.
     *
     * @param nbt the NBT compound to populate.
     * @return the NBT compound.
     */
    toNBT(nbt: import('../../../../net/minecraft/nbt/NBTTagCompound').NBTTagCompound): import('../../../../net/minecraft/nbt/NBTTagCompound').NBTTagCompound;
    /**
     * Deserializes the component from an NBT compound.
     *
     * @param nbt the NBT compound.
     * @return this component instance.
     */
    fromNBT(nbt: import('../../../../net/minecraft/nbt/NBTTagCompound').NBTTagCompound): import('./ICustomGuiComponent').ICustomGuiComponent;
    getType: import('./abstract int').abstract int;
}
