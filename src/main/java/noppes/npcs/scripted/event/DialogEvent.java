package noppes.npcs.scripted.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import noppes.npcs.api.event.IDialogEvent;
import noppes.npcs.api.handler.data.IDialog;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.constants.EnumScriptType;

public class DialogEvent extends CustomNPCsEvent implements IDialogEvent {
    public final IDialog dialog;
    public final IPlayer player;

    public DialogEvent(IPlayer player, IDialog dialog){
        this.dialog = dialog;
        this.player = player;
    }

    public IPlayer getPlayer() {
        return player;
    }

    public IDialog getDialog() {
        return dialog;
    }

    public String getHookName() {
        return EnumScriptType.DIALOG_EVENT.function;
    }

    @Cancelable
    public static class DialogOpen extends DialogEvent implements IDialogEvent.DialogOpen {
        public DialogOpen(IPlayer player, IDialog dialog) {
            super(player, dialog);
        }

        public String getHookName() {
            return EnumScriptType.DIALOG_OPEN.function;
        }
    }

    @Cancelable
    public static class DialogOption extends DialogEvent implements IDialogEvent.DialogOption {
        public DialogOption(IPlayer player, IDialog dialog) {
            super(player, dialog);
        }

        public String getHookName() {
            return EnumScriptType.DIALOG_OPTION.function;
        }

    }

    public static class DialogClosed extends DialogEvent implements IDialogEvent.DialogClosed {
        public DialogClosed(IPlayer player, IDialog dialog){
            super(player, dialog);
        }

        public String getHookName() {
            return EnumScriptType.DIALOG_CLOSE.function;
        }

    }
}
