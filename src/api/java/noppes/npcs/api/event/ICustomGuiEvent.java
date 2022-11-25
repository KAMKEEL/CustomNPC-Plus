package noppes.npcs.api.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.gui.ICustomGui;
import noppes.npcs.api.item.IItemStack;

public interface ICustomGuiEvent extends ICustomNPCsEvent {

    IPlayer getPlayer();

    ICustomGui getGui();

    int getId();

    interface ButtonEvent extends ICustomGuiEvent {
    }

    interface UnfocusedEvent extends ICustomGuiEvent {
    }

    interface CloseEvent extends ICustomGuiEvent {
    }

    interface ScrollEvent extends ICustomGuiEvent {
        String[] getSelection();

        boolean doubleClick();

        int getScrollIndex();
    }

    interface SlotEvent extends ICustomGuiEvent {
        IItemStack getStack();
    }

    @Cancelable
    interface SlotClickEvent extends ICustomGuiEvent {
        IItemStack getStack();

        int getDragType();
    }
}
