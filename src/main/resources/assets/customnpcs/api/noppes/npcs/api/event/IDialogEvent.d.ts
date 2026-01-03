/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.event
 */

export interface IDialogEvent extends IPlayerEvent {

    // Methods
    getDialog(): import('../handler/data/IDialog').IDialog;

    // Nested interfaces
    interface DialogOpen extends IDialogEvent {
    }
    interface DialogOption extends IDialogEvent {
    }
    interface DialogClosed extends IDialogEvent {
    }

}
