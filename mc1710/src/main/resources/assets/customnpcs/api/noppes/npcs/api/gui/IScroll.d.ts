/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.gui
 */

/**
 * Represents a scrollable list component in a custom GUI.
 * Provides methods to control size, selection, and the displayed list.
  * @javaFqn noppes.npcs.api.gui.IScroll
*/
export interface IScroll extends import('./ICustomGuiComponent').ICustomGuiComponent {
    /**
     * Returns the width of the scroll component.
     *
     * @return the width in pixels.
     */
    getWidth(): import('./int').int;
    /**
     * Returns the height of the scroll component.
     *
     * @return the height in pixels.
     */
    getHeight(): import('./int').int;
    /**
     * Sets the size of the scroll component.
     *
     * @param width  the new width.
     * @param height the new height.
     * @return this scroll instance.
     */
    setSize(width: import('./int').int, height: import('./int').int): import('./IScroll').IScroll;
    /**
     * Returns the list of text entries displayed in the scroll.
     *
     * @return an array of strings.
     */
    getList(): String[];
    /**
     * Sets the list of text entries to display.
     *
     * @param textList an array of strings.
     * @return this scroll instance.
     */
    setList(textList: String[]): import('./IScroll').IScroll;
    /**
     * Returns the default selection index.
     *
     * @return the default selection.
     */
    getDefaultSelection(): import('./int').int;
    /**
     * Sets the default selection index.
     *
     * @param defaultSelection the selection index.
     * @return this scroll instance.
     */
    setDefaultSelection(defaultSelection: import('./int').int): import('./IScroll').IScroll;
    /**
     * Checks whether multiple selections are allowed.
     *
     * @return true if multi-select is enabled.
     */
    isMultiSelect(): import('./boolean').boolean;
    /**
     * Sets whether multiple selections are allowed.
     *
     * @param selectMultiple true to allow multi-select.
     * @return this scroll instance.
     */
    setMultiSelect(selectMultiple: import('./boolean').boolean): import('./IScroll').IScroll;
}
