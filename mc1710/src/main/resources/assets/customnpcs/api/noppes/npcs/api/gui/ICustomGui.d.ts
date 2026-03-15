/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.gui
 */

/**
 * Represents a custom graphical user interface (GUI) that can contain various components
 * such as buttons, labels, text fields, item slots, etc.
  * @javaFqn noppes.npcs.api.gui.ICustomGui
*/
export interface ICustomGui {
    /**
     * Returns the unique ID of the GUI.
     *
     * @return the GUI ID.
     */
    getID(): import('./int').int;
    /**
     * Returns the width of the GUI.
     *
     * @return the width in pixels.
     */
    getWidth(): import('./int').int;
    /**
     * Returns the height of the GUI.
     *
     * @return the height in pixels.
     */
    getHeight(): import('./int').int;
    /**
     * Returns a list of all components contained in this GUI.
     *
     * @return the list of components.
     */
    getComponents(): import('./ICustomGuiComponent').ICustomGuiComponent[];
    /**
     * Clears all components from the GUI.
     */
    clear(): import('./void').void;
    /**
     * Returns a list of item slot components in this GUI.
     *
     * @return the list of item slots.
     */
    getSlots(): import('./IItemSlot').IItemSlot[];
    /**
     * Sets the size of the GUI.
     *
     * @param width  the new width.
     * @param height the new height.
     */
    setSize(width: import('./int').int, height: import('./int').int): import('./void').void;
    /**
     * Specifies whether the GUI pauses the game.
     *
     * @param pauseGame true to pause the game.
     */
    setDoesPauseGame(pauseGame: import('./boolean').boolean): import('./void').void;
    /**
     * Checks if the GUI pauses the game.
     *
     * @return true if it pauses the game; false otherwise.
     */
    doesPauseGame(): import('./boolean').boolean;
    /**
     * Sets the background texture resource location for the GUI.
     *
     * @param resourceLocation the texture resource location.
     */
    setBackgroundTexture(resourceLocation: String): import('./void').void;
    /**
     * Returns the background texture resource location.
     *
     * @return the texture as a string.
     */
    getBackgroundTexture(): String;
    /**
     * Adds a button to the GUI.
     *
     * @param id   the component ID.
     * @param text the button label.
     * @param x    the x position.
     * @param y    the y position.
     * @return the created button.
     */
    addButton(id: import('./int').int, text: String, x: import('./int').int, y: import('./int').int): import('./IButton').IButton;
    /**
     * Adds a button to the GUI with a specified size.
     *
     * @param id     the component ID.
     * @param text   the button label.
     * @param x      the x position.
     * @param y      the y position.
     * @param width  the button width.
     * @param height the button height.
     * @return the created button.
     */
    addButton(id: import('./int').int, text: String, x: import('./int').int, y: import('./int').int, width: import('./int').int, height: import('./int').int): import('./IButton').IButton;
    /**
     * Adds a textured button to the GUI.
     *
     * @param id      the component ID.
     * @param text    the button label.
     * @param x       the x position.
     * @param y       the y position.
     * @param width   the button width.
     * @param height  the button height.
     * @param texture the texture resource location.
     * @return the created button.
     */
    addTexturedButton(id: import('./int').int, text: String, x: import('./int').int, y: import('./int').int, width: import('./int').int, height: import('./int').int, texture: String): import('./IButton').IButton;
    /**
     * Adds a textured button with texture offset.
     *
     * @param id       the component ID.
     * @param text     the button label.
     * @param x        the x position.
     * @param y        the y position.
     * @param width    the button width.
     * @param height   the button height.
     * @param texture  the texture resource location.
     * @param textureX the x offset in the texture.
     * @param textureY the y offset in the texture.
     * @return the created button.
     */
    addTexturedButton(id: import('./int').int, text: String, x: import('./int').int, y: import('./int').int, width: import('./int').int, height: import('./int').int, texture: String, textureX: import('./int').int, textureY: import('./int').int): import('./IButton').IButton;
    /**
     * Adds a label component to the GUI.
     *
     * @param id     the component ID.
     * @param text   the label text.
     * @param x      the x position.
     * @param y      the y position.
     * @param width  the width.
     * @param height the height.
     * @return the created label.
     */
    addLabel(id: import('./int').int, text: String, x: import('./int').int, y: import('./int').int, width: import('./int').int, height: import('./int').int): import('./ILabel').ILabel;
    /**
     * Adds a label component with a specified text color.
     *
     * @param id     the component ID.
     * @param text   the label text.
     * @param x      the x position.
     * @param y      the y position.
     * @param width  the width.
     * @param height the height.
     * @param color  the text color.
     * @return the created label.
     */
    addLabel(id: import('./int').int, text: String, x: import('./int').int, y: import('./int').int, width: import('./int').int, height: import('./int').int, color: import('./int').int): import('./ILabel').ILabel;
    /**
     * Adds a text field component to the GUI.
     *
     * @param id     the component ID.
     * @param x      the x position.
     * @param y      the y position.
     * @param width  the width.
     * @param height the height.
     * @return the created text field.
     */
    addTextField(id: import('./int').int, x: import('./int').int, y: import('./int').int, width: import('./int').int, height: import('./int').int): import('./ITextField').ITextField;
    /**
     * Adds a textured rectangle component to the GUI.
     *
     * @param id      the component ID.
     * @param texture the texture resource location.
     * @param x       the x position.
     * @param y       the y position.
     * @param width   the width.
     * @param height  the height.
     * @return the created textured rectangle.
     */
    addTexturedRect(id: import('./int').int, texture: String, x: import('./int').int, y: import('./int').int, width: import('./int').int, height: import('./int').int): import('./ITexturedRect').ITexturedRect;
    /**
     * Adds a textured rectangle component with texture offset.
     *
     * @param id       the component ID.
     * @param texture  the texture resource location.
     * @param x        the x position.
     * @param y        the y position.
     * @param width    the width.
     * @param height   the height.
     * @param textureX the x offset within the texture.
     * @param textureY the y offset within the texture.
     * @return the created textured rectangle.
     */
    addTexturedRect(id: import('./int').int, texture: String, x: import('./int').int, y: import('./int').int, width: import('./int').int, height: import('./int').int, textureX: import('./int').int, textureY: import('./int').int): import('./ITexturedRect').ITexturedRect;
    /**
     * Adds an item slot component to the GUI.
     *
     * @param id the component ID.
     * @param x  the x position.
     * @param y  the y position.
     * @return the created item slot.
     */
    addItemSlot(id: import('./int').int, x: import('./int').int, y: import('./int').int): import('./IItemSlot').IItemSlot;
    /**
     * Adds an item slot component with an initial item stack.
     *
     * @param id        the component ID.
     * @param x         the x position.
     * @param y         the y position.
     * @param itemStack the initial item.
     * @return the created item slot.
     */
    addItemSlot(id: import('./int').int, x: import('./int').int, y: import('./int').int, itemStack: import('../item/IItemStack').IItemStack): import('./IItemSlot').IItemSlot;
    /**
     * @param x the X position in the GUI
     * @param y the Y position in the GUI
     * @return the created item slot
     * @deprecated Use addItemSlot(int, int, IItemStack) with an ID instead.
     */
    addItemSlot(x: import('./int').int, y: import('./int').int): import('./IItemSlot').IItemSlot;
    /**
     * @param x the X position in the GUI
     * @param y the Y position in the GUI
     * @param itemStack the item to display in the slot
     * @return the created item slot
     * @deprecated Use addItemSlot(int, int, IItemStack) with an ID instead.
     */
    addItemSlot(x: import('./int').int, y: import('./int').int, itemStack: import('../item/IItemStack').IItemStack): import('./IItemSlot').IItemSlot;
    /**
     * Adds a scroll component to the GUI.
     *
     * @param id     the component ID.
     * @param x      the x position.
     * @param y      the y position.
     * @param width  the width.
     * @param height the height.
     * @param list   the list of strings to display.
     * @return the created scroll component.
     */
    addScroll(id: import('./int').int, x: import('./int').int, y: import('./int').int, width: import('./int').int, height: import('./int').int, list: String[]): import('./IScroll').IScroll;
    /**
     * Adds a line component to the GUI.
     *
     * @param id        the component ID.
     * @param x1        the start x position.
     * @param y1        the start y position.
     * @param x2        the end x position.
     * @param y2        the end y position.
     * @param color     the line color.
     * @param thickness the line thickness.
     * @return the created line component.
     */
    addLine(id: import('./int').int, x1: import('./int').int, y1: import('./int').int, x2: import('./int').int, y2: import('./int').int, color: import('./int').int, thickness: import('./int').int): import('./ILine').ILine;
    /**
     * Adds a line component with default thickness.
     *
     * @param id the component ID.
     * @param x1 the start x position.
     * @param y1 the start y position.
     * @param x2 the end x position.
     * @param y2 the end y position.
     * @return the created line component.
     */
    addLine(id: import('./int').int, x1: import('./int').int, y1: import('./int').int, x2: import('./int').int, y2: import('./int').int): import('./ILine').ILine;
    /**
     * Displays the player's inventory at the specified position.
     *
     * @param x the x position.
     * @param y the y position.
     */
    showPlayerInventory(x: import('./int').int, y: import('./int').int): import('./void').void;
    /**
     * Retrieves the GUI component with the given ID.
     *
     * @param id the component ID.
     * @return the component.
     */
    getComponent(id: import('./int').int): import('./ICustomGuiComponent').ICustomGuiComponent;
    /**
     * Removes the GUI component with the specified ID.
     *
     * @param id the component ID.
     */
    removeComponent(id: import('./int').int): import('./void').void;
    /**
     * Updates the specified GUI component.
     *
     * @param component the component to update.
     */
    updateComponent(component: import('./ICustomGuiComponent').ICustomGuiComponent): import('./void').void;
    /**
     * Sends an update of the GUI to the given player.
     *
     * @param player the player to update.
     */
    update(player: import('../entity/IPlayer').IPlayer): import('./void').void;
    /**
     * @return true if the player inventory is shown.
     */
    getShowPlayerInv(): import('./boolean').boolean;
    /**
     * @return the x position where the player inventory is displayed.
     */
    getPlayerInvX(): import('./int').int;
    /**
     * @return the y position where the player inventory is displayed.
     */
    getPlayerInvY(): import('./int').int;
    /**
     * Recreates the GUI from NBT data.
     *
     * @param tag the NBT tag.
     * @return this GUI.
     */
    fromNBT(tag: import('../../../../net/minecraft/nbt/NBTTagCompound').NBTTagCompound): import('./ICustomGui').ICustomGui;
    /**
     * Serializes the GUI to an NBT tag.
     *
     * @return the NBT data.
     */
    toNBT(): import('../../../../net/minecraft/nbt/NBTTagCompound').NBTTagCompound;
}
