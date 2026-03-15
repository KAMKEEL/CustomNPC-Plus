package noppes.npcs.api.event;

import noppes.npcs.api.Cancelable;
import noppes.npcs.api.handler.data.IDialog;

/**
 * Events fired during dialog interactions between a player and an NPC.
 */
public interface IDialogEvent extends IPlayerEvent {

    /** @return the dialog associated with this event. */
    IDialog getDialog();

    /** @return the dialog's unique ID. */
    int getDialogId();

    /** @return the selected dialog option ID. */
    int getOptionId();

    /**
     * Fired when a dialog is opened. Cancelable.
     * @hookName dialogOpen
     */
    @Cancelable
    interface DialogOpen extends IDialogEvent {
    }

    /**
     * Fired when a dialog option is selected. Cancelable.
     * @hookName dialogOption
     */
    @Cancelable
    interface DialogOption extends IDialogEvent {
    }

    /**
     * Fired when a dialog is closed.
     * @hookName dialogClose
     */
    interface DialogClosed extends IDialogEvent {
    }
}
