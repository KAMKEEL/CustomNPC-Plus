package noppes.npcs.api.handler;

import noppes.npcs.api.handler.data.IDialog;
import noppes.npcs.api.handler.data.IDialogCategory;

import java.util.List;

/**
 * Handles retrieval of dialogs and dialog categories.
 */
public interface IDialogHandler {

    /**
     * Returns all dialog categories.
     *
     * @return a list of dialog categories.
     */
    List<IDialogCategory> categories();

    /**
     * Returns the dialog with the specified ID.
     *
     * @param id the dialog ID.
     * @return the dialog, or null if not found.
     */
    IDialog get(int id);
}
