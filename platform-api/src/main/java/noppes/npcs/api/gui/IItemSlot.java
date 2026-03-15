package noppes.npcs.api.gui;

import noppes.npcs.api.item.IItemStack;

/**
 * Represents an item slot component in a custom GUI.
 * Provides methods to get or set the contained item.
 */
public interface IItemSlot extends ICustomGuiComponent {

    /**
     * Checks if the slot currently contains an item stack.
     *
     * @return true if a stack exists; false otherwise.
     */
    boolean hasStack();

    /**
     * Returns the item stack contained in this slot.
     *
     * @return the item stack.
     */
    IItemStack getStack();

    /**
     * Sets the item stack for this slot.
     *
     * @param itemStack the item stack to set.
     * @return this item slot instance.
     */
    IItemSlot setStack(IItemStack itemStack);

    /**
     * Returns the underlying Minecraft slot object.
     *
     * @return the Minecraft Slot.
     */
    Object getMCSlot();
}
