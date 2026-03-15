/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler
 */

/**
 * Handles retrieval of dialogs and dialog categories.
  * @javaFqn noppes.npcs.api.handler.IDialogHandler
*/
export interface IDialogHandler {
    /**
     * Returns all dialog categories.
     *
     * @return a list of dialog categories.
     */
    categories(): import('./data/IDialogCategory').IDialogCategory[];
    /**
     * Returns the dialog with the specified ID.
     *
     * @param id the dialog ID.
     * @return the dialog, or null if not found.
     */
    get(id: import('./int').int): import('./data/IDialog').IDialog;
}
