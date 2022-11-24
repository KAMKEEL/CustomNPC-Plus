package noppes.npcs.api.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.IDialog;

public interface IDialogEvent extends ICustomNPCsEvent {

    IPlayer getPlayer();

    IDialog getDialog();

    @Cancelable
    interface DialogOpen {
    }

    @Cancelable
    interface DialogOption {
    }

    interface DialogClosed {
    }
}
