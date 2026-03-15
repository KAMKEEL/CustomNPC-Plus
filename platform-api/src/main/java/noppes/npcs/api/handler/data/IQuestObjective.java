package noppes.npcs.api.handler.data;

public interface IQuestObjective {

    /**
     * If this function is used on a player within a party and the
     * quest objective type is set to ALL. The return progress will be
     * set to 0 for handling multiple players.
     *
     * @return progress of objective
     */
    int getProgress();

    /**
     * If this function is used on a player within a party and the
     * quest objective type is set to ALL. It will set everyone to have
     * the same progress within the party.
     *
     * @param progress Progress Amount
     */
    void setProgress(int progress);

    /**
     * This function is typically used for setting individual
     * progress for a player in a party for a Quest Objective
     * that is set for ALL Players. This function will work
     * like the normal setProgress if the player does not
     * have a party
     *
     * @param playerName Name of Player
     * @param progress   Progress Amount
     */
    void setPlayerProgress(String playerName, int progress);

    /** @return the target progress value needed to complete this objective. */
    int getMaxProgress();

    /** @return true if this objective has been completed. */
    boolean isCompleted();

    /** @return the objective description text. */
    String getText();

    /** @return additional display text for this objective, or null. */
    String getAdditionalText();
}
