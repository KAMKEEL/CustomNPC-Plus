/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.event
 */

/**
 * Events fired during dialog interactions between a player and an NPC.
  * @javaFqn noppes.npcs.api.event.IDialogEvent
*/
export interface IDialogEvent extends import('./IPlayerEvent').IPlayerEvent {
    /** @return the dialog associated with this event. */
    getDialog(): import('../handler/data/IDialog').IDialog;
    /** @return the dialog's unique ID. */
    getDialogId(): import('./int').int;
    /** @return the selected dialog option ID. */
    getOptionId(): import('./int').int;
    readonly dialog: import('../handler/data/IDialog').IDialog;
    readonly dialogId: import('./int').int;
    readonly optionId: import('./int').int;
}

export namespace IDialogEvent {
    
    
    
    /**
     * Fired when a dialog is opened. Cancelable.
     * @hookName dialogOpen
          * @javaFqn noppes.npcs.api.event.IDialogEvent.DialogOpen
*/
    export interface DialogOpen extends IDialogEvent {
    }
    /**
     * Fired when a dialog option is selected. Cancelable.
     * @hookName dialogOption
          * @javaFqn noppes.npcs.api.event.IDialogEvent.DialogOption
*/
    export interface DialogOption extends IDialogEvent {
    }
    /**
     * Fired when a dialog is closed.
     * @hookName dialogClose
          * @javaFqn noppes.npcs.api.event.IDialogEvent.DialogClosed
*/
    export interface DialogClosed extends IDialogEvent {
    }
}
