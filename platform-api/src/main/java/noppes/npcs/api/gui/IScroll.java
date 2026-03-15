package noppes.npcs.api.gui;

/**
 * Represents a scrollable list component in a custom GUI.
 * Provides methods to control size, selection, and the displayed list.
 */
public interface IScroll extends ICustomGuiComponent {

    /**
     * Returns the width of the scroll component.
     *
     * @return the width in pixels.
     */
    int getWidth();

    /**
     * Returns the height of the scroll component.
     *
     * @return the height in pixels.
     */
    int getHeight();

    /**
     * Sets the size of the scroll component.
     *
     * @param width  the new width.
     * @param height the new height.
     * @return this scroll instance.
     */
    IScroll setSize(int width, int height);

    /**
     * Returns the list of text entries displayed in the scroll.
     *
     * @return an array of strings.
     */
    String[] getList();

    /**
     * Sets the list of text entries to display.
     *
     * @param textList an array of strings.
     * @return this scroll instance.
     */
    IScroll setList(String[] textList);

    /**
     * Returns the default selection index.
     *
     * @return the default selection.
     */
    int getDefaultSelection();

    /**
     * Sets the default selection index.
     *
     * @param defaultSelection the selection index.
     * @return this scroll instance.
     */
    IScroll setDefaultSelection(int defaultSelection);

    /**
     * Checks whether multiple selections are allowed.
     *
     * @return true if multi-select is enabled.
     */
    boolean isMultiSelect();

    /**
     * Sets whether multiple selections are allowed.
     *
     * @param selectMultiple true to allow multi-select.
     * @return this scroll instance.
     */
    IScroll setMultiSelect(boolean selectMultiple);
}
