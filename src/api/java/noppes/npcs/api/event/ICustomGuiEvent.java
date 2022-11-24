package noppes.npcs.api.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.gui.ICustomGui;
import noppes.npcs.api.item.IItemStack;

public interface ICustomGuiEvent extends ICustomNPCsEvent {

    IPlayer getPlayer();

    ICustomGui getGui();

    int getId();

    interface ButtonEvent {
    }

    interface UnfocusedEvent {
    }

    interface CloseEvent {
    }

    interface ScrollEvent {
        String[] getSelection();

        boolean doubleClick();

        int getScrollIndex();
    }

    interface SlotEvent {
        IItemStack getStack();
    }

    @Cancelable
    interface SlotClickEvent {
        IItemStack getStack();

        int getDragType();
    }
}
