package noppes.npcs.api.handler.data;

import java.util.List;

public interface IDialogCategory {
    /**
     * @return A list of all dialogs in this category
     */
    List<IDialog> dialogs();

    /**
     * @return The name of this dialog category
     */
    String getName();

    /**
     * Creates a new empty dialog in this category.
     * @return The newly created {@link IDialog}
     */
    IDialog create();

    /**
     * @return The unique ID of this dialog category
     */
    int getId();
}
