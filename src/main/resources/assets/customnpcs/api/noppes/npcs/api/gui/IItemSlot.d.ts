/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.gui
 */

export interface IItemSlot extends ICustomGuiComponent {

    // Methods
    hasStack(): boolean;
    getStack(): import('../item/IItemStack').IItemStack;
    setStack(itemStack: import('../item/IItemStack').IItemStack): import('./IItemSlot').IItemSlot;
    getMCSlot(): Slot;

}
