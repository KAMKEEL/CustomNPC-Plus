package noppes.npcs.scripted.event;

import noppes.npcs.scripted.handler.data.IDialog;
import noppes.npcs.scripted.interfaces.IPlayer;

public class DialogEvent extends CustomNPCsEvent {
    public final IDialog dialog;
    public final IPlayer player;

    public DialogEvent(IPlayer player, IDialog dialog){
        this.dialog = dialog;
        this.player = player;
    }

    public static class DialogOpen extends DialogEvent {
        public DialogOpen(IPlayer player, IDialog dialog) {
            super(player, dialog);
        }
    }

    public static class DialogOption extends DialogEvent {
        public DialogOption(IPlayer player, IDialog dialog) {
            super(player, dialog);
        }
    }

    public static class DialogClosed extends DialogEvent {
        public DialogClosed(IPlayer player, IDialog dialog){
            super(player, dialog);
        }
    }
}
