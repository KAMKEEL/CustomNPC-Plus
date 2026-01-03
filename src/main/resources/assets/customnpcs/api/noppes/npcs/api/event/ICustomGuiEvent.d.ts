/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.event
 */

export interface ICustomGuiEvent extends IPlayerEvent {

    // Methods
    getGui(): import('../gui/ICustomGui').ICustomGui;
    getId(): number;

    // Nested interfaces
    interface ButtonEvent extends ICustomGuiEvent {
    }
    interface UnfocusedEvent extends ICustomGuiEvent {
    }
    interface CloseEvent extends ICustomGuiEvent {
    }
    interface ScrollEvent extends ICustomGuiEvent {
        getSelection(): string[];
        doubleClick(): boolean;
        getScrollIndex(): number;
    }
    interface SlotEvent extends ICustomGuiEvent {
        getStack(): import('../item/IItemStack').IItemStack;
    }
    interface SlotClickEvent extends ICustomGuiEvent {
        getStack(): import('../item/IItemStack').IItemStack;
        getDragType(): number;
    }

}
