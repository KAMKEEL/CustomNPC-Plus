/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.gui
 */

/**
 * Represents an item slot component in a custom GUI.
 * Provides methods to get or set the contained item.
  * @javaFqn noppes.npcs.api.gui.IItemSlot
*/
export interface IItemSlot extends import('./ICustomGuiComponent').ICustomGuiComponent {
    /**
     * Checks if the slot currently contains an item stack.
     *
     * @return true if a stack exists; false otherwise.
     */
    hasStack(): import('./boolean').boolean;
    /**
     * Returns the item stack contained in this slot.
     *
     * @return the item stack.
     */
    getStack(): import('../item/IItemStack').IItemStack;
    /**
     * Sets the item stack for this slot.
     *
     * @param itemStack the item stack to set.
     * @return this item slot instance.
     */
    setStack(itemStack: import('../item/IItemStack').IItemStack): import('./IItemSlot').IItemSlot;
    /**
     * Returns the underlying Minecraft slot object.
     *
     * @return the Minecraft Slot.
     */
    getMCSlot(): import('../../../../net/minecraft/inventory/Slot').Slot;
}
