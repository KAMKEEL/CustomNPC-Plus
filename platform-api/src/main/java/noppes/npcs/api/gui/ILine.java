package noppes.npcs.api.gui;

/**
 * Represents a line component in a custom GUI.
 * Provides methods to get or set endpoints and thickness.
 */
public interface ILine extends ICustomGuiComponent {

    /**
     * Returns the starting x coordinate of the line.
     *
     * @return the x1 value.
     */
    int getX1();

    /**
     * Returns the starting y coordinate of the line.
     *
     * @return the y1 value.
     */
    int getY1();

    /**
     * Returns the ending x coordinate of the line.
     *
     * @return the x2 value.
     */
    int getX2();

    /**
     * Returns the ending y coordinate of the line.
     *
     * @return the y2 value.
     */
    int getY2();

    /**
     * Returns the thickness of the line.
     *
     * @return the thickness in pixels.
     */
    int getThickness();

    /**
     * Sets the starting x coordinate of the line.
     *
     * @param x1 the new x1 value.
     */
    void setX1(int x1);

    /**
     * Sets the starting y coordinate of the line.
     *
     * @param y1 the new y1 value.
     */
    void setY1(int y1);

    /**
     * Sets the ending x coordinate of the line.
     *
     * @param x2 the new x2 value.
     */
    void setX2(int x2);

    /**
     * Sets the ending y coordinate of the line.
     *
     * @param y2 the new y2 value.
     */
    void setY2(int y2);

    /**
     * Sets the thickness of the line.
     *
     * @param thickness the new thickness.
     */
    void setThickness(int thickness);
}
