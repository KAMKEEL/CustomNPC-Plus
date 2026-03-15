/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.event
 */

/**
 * Events fired when a player interacts with a custom GUI.
  * @javaFqn noppes.npcs.api.event.ICustomGuiEvent
*/
export interface ICustomGuiEvent extends import('./IPlayerEvent').IPlayerEvent {
    /** @return the custom GUI associated with this event. */
    getGui(): import('../gui/ICustomGui').ICustomGui;
    /** @return the component ID that triggered this event. */
    getId(): import('./int').int;
    readonly gui: import('../gui/ICustomGui').ICustomGui;
}

export namespace ICustomGuiEvent {
    
    
    /**
     * @hookName customGuiButton
          * @javaFqn noppes.npcs.api.event.ICustomGuiEvent.ButtonEvent
*/
    export interface ButtonEvent extends ICustomGuiEvent {
        readonly buttonId: import('./int').int;
    }
    /**
     * @hookName customGuiTextfield
          * @javaFqn noppes.npcs.api.event.ICustomGuiEvent.UnfocusedEvent
*/
    export interface UnfocusedEvent extends ICustomGuiEvent {
        readonly textfieldId: import('./int').int;
    }
    /**
     * @hookName customGuiClosed
          * @javaFqn noppes.npcs.api.event.ICustomGuiEvent.CloseEvent
*/
    export interface CloseEvent extends ICustomGuiEvent {
    }
    /**
     * @hookName customGuiScroll
          * @javaFqn noppes.npcs.api.event.ICustomGuiEvent.ScrollEvent
*/
    export interface ScrollEvent extends ICustomGuiEvent {
        getSelection(): String[];
        doubleClick(): import('./boolean').boolean;
        getScrollIndex(): import('./int').int;
        readonly scrollId: import('./int').int;
        readonly selection: String[];
        readonly doubleClick: import('./boolean').boolean;
        readonly scrollIndex: import('./int').int;
    }
    /**
     * @hookName customGuiSlot
          * @javaFqn noppes.npcs.api.event.ICustomGuiEvent.SlotEvent
*/
    export interface SlotEvent extends ICustomGuiEvent {
        getStack(): import('../item/IItemStack').IItemStack;
        readonly slotId: import('./int').int;
        readonly stack: import('../item/IItemStack').IItemStack;
        readonly slot: import('./IItemSlot').IItemSlot;
    }
    /**
     * @hookName customGuiSlotClicked
          * @javaFqn noppes.npcs.api.event.ICustomGuiEvent.SlotClickEvent
*/
    export interface SlotClickEvent extends ICustomGuiEvent {
        getStack(): import('../item/IItemStack').IItemStack;
        getDragType(): import('./int').int;
        readonly slotId: import('./int').int;
        readonly stack: import('../item/IItemStack').IItemStack;
        readonly dragType: import('./int').int;
        readonly slot: import('./IItemSlot').IItemSlot;
        readonly clickType: import('./int').int;
    }
}
