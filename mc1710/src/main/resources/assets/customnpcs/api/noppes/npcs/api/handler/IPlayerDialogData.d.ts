/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler
 */

/**
 * Tracks which dialogs a player has read.
  * @javaFqn noppes.npcs.api.handler.IPlayerDialogData
*/
export interface IPlayerDialogData {
    /**
     * Checks if the player has read the dialog with the given ID.
     *
     * @param id the dialog ID.
     * @return true if the dialog has been read; false otherwise.
     */
    hasReadDialog(id: import('./int').int): import('./boolean').boolean;
    /**
     * Marks the dialog with the given ID as read.
     *
     * @param id the dialog ID.
     */
    readDialog(id: import('./int').int): import('./void').void;
    /**
     * Marks the dialog with the given ID as unread.
     *
     * @param id the dialog ID.
     */
    unreadDialog(id: import('./int').int): import('./void').void;
}
