package noppes.npcs.api.handler;

/**
 * Tracks which dialogs a player has read.
 */
public interface IPlayerDialogData {

    /**
     * Checks if the player has read the dialog with the given ID.
     *
     * @param id the dialog ID.
     * @return true if the dialog has been read; false otherwise.
     */
    boolean hasReadDialog(int id);

    /**
     * Marks the dialog with the given ID as read.
     *
     * @param id the dialog ID.
     */
    void readDialog(int id);

    /**
     * Marks the dialog with the given ID as unread.
     *
     * @param id the dialog ID.
     */
    void unreadDialog(int id);
}
