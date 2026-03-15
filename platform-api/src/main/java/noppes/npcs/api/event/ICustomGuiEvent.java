package noppes.npcs.api.event;

import noppes.npcs.api.Cancelable;
import noppes.npcs.api.gui.ICustomGui;
import noppes.npcs.api.item.IItemStack;

/**
 * Events fired when a player interacts with a custom GUI.
 */
public interface ICustomGuiEvent extends IPlayerEvent {

    /** @return the custom GUI associated with this event. */
    ICustomGui getGui();

    /** @return the component ID that triggered this event. */
    int getId();

    /**
     * @hookName customGuiButton
     */
    interface ButtonEvent extends ICustomGuiEvent {
    }

    /**
     * @hookName customGuiTextfield
     */
    interface UnfocusedEvent extends ICustomGuiEvent {
    }

    /**
     * @hookName customGuiClosed
     */
    interface CloseEvent extends ICustomGuiEvent {
    }

    /**
     * @hookName customGuiScroll
     */
    interface ScrollEvent extends ICustomGuiEvent {
        String[] getSelection();

        boolean doubleClick();

        int getScrollIndex();
    }

    /**
     * @hookName customGuiSlot
     */
    interface SlotEvent extends ICustomGuiEvent {
        IItemStack getStack();
    }

    /**
     * @hookName customGuiSlotClicked
     */
    @Cancelable
    interface SlotClickEvent extends ICustomGuiEvent {
        IItemStack getStack();

        int getDragType();
    }
}
