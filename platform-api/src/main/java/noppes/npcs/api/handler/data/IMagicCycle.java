package noppes.npcs.api.handler.data;

/**
 * Represents a cycle (or category) grouping for magics.
 * A magic cycle holds ordering and display information as well as associations
 * to multiple magics.
 */
public interface IMagicCycle {

    /**
     * Gets the unique identifier of this magic cycle.
     *
     * @return the cycle ID
     */
    int getId();

    /**
     * Gets the internal name of the magic cycle.
     *
     * @return the name of the cycle
     */
    String getName();

    /**
     * Sets the internal name of the magic cycle.
     *
     * @param name the new name of the cycle
     */
    void setName(String name);

    /**
     * Gets the display name of the magic cycle.
     *
     * @return the display name for the cycle
     */
    String getDisplayName();

    /**
     * Sets the display name of the magic cycle.
     *
     * @param displayName the new display name of the cycle
     */
    void setDisplayName(String displayName);

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
    int getLayoutType();

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
    void setLayoutType(int layout);
}
