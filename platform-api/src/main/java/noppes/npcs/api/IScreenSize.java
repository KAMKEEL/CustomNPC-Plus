package noppes.npcs.api;

/**
 * Represents the scaled screen dimensions of the client display.
 */
public interface IScreenSize {

    /**
     * Gets the scaled screen width in pixels.
     * @return the screen width
     */
    public int getWidth();

    /**
     * Gets the scaled screen height in pixels.
     * @return the screen height
     */
    public int getHeight();

    /**
     * Gets a pixel value representing the given percentage of the screen width.
     * @param percent the percentage of the width (0.0 to 100.0)
     * @return the corresponding pixel value
     */
    public int getWidthPercent(double percent);

    /**
     * Gets a pixel value representing the given percentage of the screen height.
     * @param percent the percentage of the height (0.0 to 100.0)
     * @return the corresponding pixel value
     */
    public int getHeightPercent(double percent);
}
