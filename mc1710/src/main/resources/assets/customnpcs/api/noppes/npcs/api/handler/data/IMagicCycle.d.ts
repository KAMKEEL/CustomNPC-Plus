/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

/**
 * Represents a cycle (or category) grouping for magics.
 * A magic cycle holds ordering and display information as well as associations
 * to multiple magics.
  * @javaFqn noppes.npcs.api.handler.data.IMagicCycle
*/
export interface IMagicCycle {
    /**
     * Gets the unique identifier of this magic cycle.
     *
     * @return the cycle ID
     */
    getId(): import('./int').int;
    /**
     * Gets the internal name of the magic cycle.
     *
     * @return the name of the cycle
     */
    getName(): String;
    /**
     * Sets the internal name of the magic cycle.
     *
     * @param name the new name of the cycle
     */
    setName(name: String): import('./void').void;
    /**
     * Gets the display name of the magic cycle.
     *
     * @return the display name for the cycle
     */
    getDisplayName(): String;
    /**
     * Sets the display name of the magic cycle.
     *
     * @param displayName the new display name of the cycle
     */
    setDisplayName(displayName: String): import('./void').void;
    /**
     * Gets the layout type for the cycle.
     * This is typically an integer representation of the layout enum.
     * <p>
     * 0: CIRCULAR
     * 1: SQUARE
     * 2: TREE
     * 3: GENERATED
     * 4: CIRCULAR_MANUAL
     * 5: SQUARE_MANUAL
     * 6: TREE_MANUAL
     * 7: CHART
     *
     * @return the layout type as an integer
     */
    getLayoutType(): import('./int').int;
    /**
     * Sets the layout type for the cycle.
     * The provided integer should correspond to a valid layout type.
     * <p>
     * 0: CIRCULAR
     * 1: SQUARE
     * 2: TREE
     * 3: GENERATED
     * 4: CIRCULAR_MANUAL
     * 5: SQUARE_MANUAL
     * 6: TREE_MANUAL
     * 7: CHART
     *
     * @param layout the layout type as an integer
     */
    setLayoutType(layout: import('./int').int): import('./void').void;
}
