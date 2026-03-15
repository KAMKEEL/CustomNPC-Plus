package noppes.npcs.api.handler.data;

/**
 * Represents a magic element that can have interactions with other magics.
 * Each magic has unique properties such as an ID, name, color, and display name,
 * and can hold interaction values with other magics.
 */
public interface IMagic {

    /**
     * Gets the unique identifier of this magic.
     *
     * @return the magic ID
     */
    int getId();

    /**
     * Gets the internal name of the magic.
     *
     * @return the name of the magic
     */
    String getName();

    /**
     * Sets the internal name of the magic.
     *
     * @param name the new name of the magic
     */
    void setName(String name);

    /**
     * Sets the color for this magic.
     *
     * @param c the color represented as an integer (typically in hexadecimal)
     */
    void setColor(int c);

    /**
     * Gets the color of this magic.
     *
     * @return the color as an integer
     */
    int getColor();

    /**
     * Gets the display name of the magic.
     *
     * @return the display name of the magic
     */
    String getDisplayName();

    /**
     * Sets the display name of the magic.
     *
     * @param displayName the new display name
     */
    void setDisplayName(String displayName);

    /**
     * Saves the current state of the magic.
     * Typically persists changes to a controller or storage system.
     */
    void save();

    /**
     * Checks if this magic has an interaction value set for another magic.
     *
     * @param magicID the unique identifier of the other magic
     * @return true if an interaction is present, false otherwise
     */
    boolean hasInteraction(int magicID);

    /**
     * Sets an interaction value for a specific other magic.
     *
     * @param magicID the unique identifier of the other magic
     * @param value   the interaction value (usually a percentage or multiplier)
     */
    void setInteraction(int magicID, float value);

    /**
     * Retrieves the interaction value for a specific other magic.
     *
     * @param magicID the unique identifier of the other magic
     * @param value   the default value to return if no interaction is found
     * @return the interaction value if present; otherwise, the default value
     */
    float getInteraction(int magicID, float value);
}
