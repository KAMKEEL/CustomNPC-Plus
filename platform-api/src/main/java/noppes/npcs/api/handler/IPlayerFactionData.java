package noppes.npcs.api.handler;

/**
 * Tracks a player's faction standing points.
 */
public interface IPlayerFactionData {

    /**
     * Returns the player's points for the given faction.
     *
     * @param id the faction ID.
     * @return the faction points.
     */
    int getPoints(int id);

    /**
     * Adds points to the player's standing with the given faction.
     * Use negative values to decrease.
     *
     * @param id     the faction ID.
     * @param points the points to add.
     */
    void addPoints(int id, int points);

    /**
     * Sets the player's points for the given faction.
     *
     * @param id     the faction ID.
     * @param points the new point value.
     */
    void setPoints(int id, int points);
}
