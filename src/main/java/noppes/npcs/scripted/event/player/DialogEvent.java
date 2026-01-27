package noppes.npcs.scripted.event.player;

import cpw.mods.fml.common.eventhandler.Cancelable;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.event.IDialogEvent;
import noppes.npcs.api.handler.data.IDialog;
import noppes.npcs.constants.EnumScriptType;

public class DialogEvent extends PlayerEvent implements IDialogEvent {
    public final IDialog dialog;
    private final int optionId;

    public DialogEvent(IPlayer player, IDialog dialog) {
        this(player, dialog, -1);
    }

    public DialogEvent(IPlayer player, IDialog dialog, int optionId) {
        super(player);
        this.dialog = dialog;
        this.optionId = optionId;
    }

    public IDialog getDialog() {
        return dialog;
    }

    @Override
    public int getDialogId() {
        return dialog != null ? dialog.getId() : -1;
    }

    @Override
    public int getOptionId() {
        return optionId;
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

        public DialogOption(IPlayer player, IDialog dialog, int optionId) {
            super(player, dialog, optionId);
        }

        public String getHookName() {
            return EnumScriptType.DIALOG_OPTION.function;
        }

    }

    public static class DialogClosed extends DialogEvent implements IDialogEvent.DialogClosed {
        public DialogClosed(IPlayer player, IDialog dialog) {
            super(player, dialog);
        }

        public String getHookName() {
            return EnumScriptType.DIALOG_CLOSE.function;
        }

    }
}
