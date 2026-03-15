/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

/**
 * A collection of dialog lines, supporting creation, indexed access, and random retrieval.
  * @javaFqn noppes.npcs.api.handler.data.ILines
*/
export interface ILines {
    /**
     * Creates and adds a new line with the given text.
     *
     * @param text the line text.
     * @return the created line.
     */
    createLine(text: String): import('./ILine').ILine;
    /**
     * Returns a line, either randomly or sequentially.
     *
     * @param isRandom true to return a random line; false for sequential.
     * @return the selected line, or null if empty.
     */
    getLine(isRandom: import('./boolean').boolean): import('./ILine').ILine;
    /**
     * Returns the line at the given index.
     *
     * @param lineIndex the line index.
     * @return the line, or null if the index is invalid.
     */
    getLine(lineIndex: import('./int').int): import('./ILine').ILine;
    /**
     * Sets the line at the given index.
     *
     * @param lineIndex the line index.
     * @param line      the line to set.
     */
    setLine(lineIndex: import('./int').int, line: import('./ILine').ILine): import('./void').void;
    /**
     * Removes the line at the given index.
     *
     * @param lineIndex the line index.
     */
    removeLine(lineIndex: import('./int').int): import('./void').void;
    /**
     * Removes all lines.
     */
    clear(): import('./void').void;
    /** @return true if there are no lines. */
    isEmpty(): import('./boolean').boolean;
    /** @return the indices of all stored lines. */
    getKeys(): Integer[];
}
