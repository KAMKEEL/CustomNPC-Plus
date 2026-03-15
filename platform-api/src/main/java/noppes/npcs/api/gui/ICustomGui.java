package noppes.npcs.api.gui;

import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.item.IItemStack;

import java.util.List;

/**
 * Represents a custom graphical user interface (GUI) that can contain various components
 * such as buttons, labels, text fields, item slots, etc.
 */
public interface ICustomGui {

    /**
     * Returns the unique ID of the GUI.
     *
     * @return the GUI ID.
     */
    int getID();

    /**
     * Returns the width of the GUI.
     *
     * @return the width in pixels.
     */
    int getWidth();

    /**
     * Returns the height of the GUI.
     *
     * @return the height in pixels.
     */
    int getHeight();

    /**
     * Returns a list of all components contained in this GUI.
     *
     * @return the list of components.
     */
    List<ICustomGuiComponent> getComponents();

    /**
     * Clears all components from the GUI.
     */
    void clear();

    /**
     * Returns a list of item slot components in this GUI.
     *
     * @return the list of item slots.
     */
    List<IItemSlot> getSlots();

    /**
     * Sets the size of the GUI.
     *
     * @param width  the new width.
     * @param height the new height.
     */
    void setSize(int width, int height);

    /**
     * Specifies whether the GUI pauses the game.
     *
     * @param pauseGame true to pause the game.
     */
    void setDoesPauseGame(boolean pauseGame);

    /**
     * Checks if the GUI pauses the game.
     *
     * @return true if it pauses the game; false otherwise.
     */
    boolean doesPauseGame();

    /**
     * Sets the background texture resource location for the GUI.
     *
     * @param resourceLocation the texture resource location.
     */
    void setBackgroundTexture(String resourceLocation);

    /**
     * Returns the background texture resource location.
     *
     * @return the texture as a string.
     */
    String getBackgroundTexture();

    /**
     * Adds a button to the GUI.
     *
     * @param id   the component ID.
     * @param text the button label.
     * @param x    the x position.
     * @param y    the y position.
     * @return the created button.
     */
    IButton addButton(int id, String text, int x, int y);

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
    IButton addButton(int id, String text, int x, int y, int width, int height);

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
    IButton addTexturedButton(int id, String text, int x, int y, int width, int height, String texture);

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
    IButton addTexturedButton(int id, String text, int x, int y, int width, int height, String texture, int textureX, int textureY);

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
    ILabel addLabel(int id, String text, int x, int y, int width, int height);

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
    ILabel addLabel(int id, String text, int x, int y, int width, int height, int color);

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
    ITextField addTextField(int id, int x, int y, int width, int height);

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
    ITexturedRect addTexturedRect(int id, String texture, int x, int y, int width, int height);

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
    ITexturedRect addTexturedRect(int id, String texture, int x, int y, int width, int height, int textureX, int textureY);

    /**
     * Adds an item slot component to the GUI.
     *
     * @param id the component ID.
     * @param x  the x position.
     * @param y  the y position.
     * @return the created item slot.
     */
    IItemSlot addItemSlot(int id, int x, int y);

    /**
     * Adds an item slot component with an initial item stack.
     *
     * @param id        the component ID.
     * @param x         the x position.
     * @param y         the y position.
     * @param itemStack the initial item.
     * @return the created item slot.
     */
    IItemSlot addItemSlot(int id, int x, int y, IItemStack itemStack);

    /**
     * @param x the X position in the GUI
     * @param y the Y position in the GUI
     * @return the created item slot
     * @deprecated Use addItemSlot(int, int, IItemStack) with an ID instead.
     */
    @Deprecated
    IItemSlot addItemSlot(int x, int y);

    /**
     * @param x the X position in the GUI
     * @param y the Y position in the GUI
     * @param itemStack the item to display in the slot
     * @return the created item slot
     * @deprecated Use addItemSlot(int, int, IItemStack) with an ID instead.
     */
    @Deprecated
    IItemSlot addItemSlot(int x, int y, IItemStack itemStack);

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
    IScroll addScroll(int id, int x, int y, int width, int height, String[] list);

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
    ILine addLine(int id, int x1, int y1, int x2, int y2, int color, int thickness);

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
    ILine addLine(int id, int x1, int y1, int x2, int y2);

    /**
     * Displays the player's inventory at the specified position.
     *
     * @param x the x position.
     * @param y the y position.
     */
    void showPlayerInventory(int x, int y);

    /**
     * Retrieves the GUI component with the given ID.
     *
     * @param id the component ID.
     * @return the component.
     */
    ICustomGuiComponent getComponent(int id);

    /**
     * Removes the GUI component with the specified ID.
     *
     * @param id the component ID.
     */
    void removeComponent(int id);

    /**
     * Updates the specified GUI component.
     *
     * @param component the component to update.
     */
    void updateComponent(ICustomGuiComponent component);

    /**
     * Sends an update of the GUI to the given player.
     *
     * @param player the player to update.
     */
    void update(IPlayer player);

    /**
     * @return true if the player inventory is shown.
     */
    boolean getShowPlayerInv();

    /**
     * @return the x position where the player inventory is displayed.
     */
    int getPlayerInvX();

    /**
     * @return the y position where the player inventory is displayed.
     */
    int getPlayerInvY();

    /**
     * Recreates the GUI from NBT data.
     *
     * @param tag the NBT tag.
     * @return this GUI.
     */
    ICustomGui fromNBT(Object tag);

    /**
     * Serializes the GUI to an NBT tag.
     *
     * @return the NBT data.
     */
    Object toNBT();
}
