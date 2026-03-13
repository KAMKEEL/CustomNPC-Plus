package noppes.npcs.api.handler.data;

/**
 * A collection of dialog lines, supporting creation, indexed access, and random retrieval.
 */
public interface ILines {

    /**
     * Creates and adds a new line with the given text.
     *
     * @param text the line text.
     * @return the created line.
     */
    ILine createLine(String text);

    /**
     * Returns a line, either randomly or sequentially.
     *
     * @param isRandom true to return a random line; false for sequential.
     * @return the selected line, or null if empty.
     */
    ILine getLine(boolean isRandom);

    /**
     * Returns the line at the given index.
     *
     * @param lineIndex the line index.
     * @return the line, or null if the index is invalid.
     */
    ILine getLine(int lineIndex);

    /**
     * Sets the line at the given index.
     *
     * @param lineIndex the line index.
     * @param line      the line to set.
     */
    void setLine(int lineIndex, ILine line);

    /**
     * Removes the line at the given index.
     *
     * @param lineIndex the line index.
     */
    void removeLine(int lineIndex);

    /**
     * Removes all lines.
     */
    void clear();

    /** @return true if there are no lines. */
    boolean isEmpty();

    /** @return the indices of all stored lines. */
    Integer[] getKeys();
}
