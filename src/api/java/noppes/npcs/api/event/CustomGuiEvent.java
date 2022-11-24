package noppes.npcs.api.event;

import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.gui.ICustomGui;

public interface CustomGuiEvent {

    IPlayer getPlayer();

    ICustomGui getGui();

    interface ButtonEvent {
    }

    interface UnfocusedEvent {
    }

    interface CloseEvent {
    }

    interface ScrollEvent {
    }

    interface SlotEvent {
    }

    interface SlotClickEvent {
    }
}
