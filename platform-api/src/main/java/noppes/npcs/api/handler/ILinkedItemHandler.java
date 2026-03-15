package noppes.npcs.api.handler;

import noppes.npcs.api.handler.data.ILinkedItem;
import noppes.npcs.api.item.IItemStack;

/**
 * Handles registration and retrieval of linked items.
 * Linked items are custom item definitions that can be updated globally.
 */
public interface ILinkedItemHandler {

    /**
     * Creates a new linked item with the given name.
     *
     * @param name the item name.
     * @return the created linked item.
     */
    ILinkedItem createItem(String name);

    /**
     * Creates an ItemStack from a linked item by its ID.
     *
     * @param id the linked item ID.
     * @return the item stack, or null if the ID is invalid.
     */
    IItemStack createItemStack(int id);

    /**
     * Registers a linked item with this handler.
     *
     * @param linkedItem the linked item to add.
     */
    void add(ILinkedItem linkedItem);

    /**
     * Removes and returns the linked item with the given ID.
     *
     * @param id the linked item ID.
     * @return the removed linked item, or null if not found.
     */
    ILinkedItem remove(int id);

    /**
     * Returns the linked item with the given ID.
     *
     * @param id the linked item ID.
     * @return the linked item, or null if not found.
     */
    ILinkedItem get(int id);

    /**
     * Checks whether a linked item with the given ID exists.
     *
     * @param id the linked item ID.
     * @return true if it exists; false otherwise.
     */
    boolean contains(int id);

    /**
     * Checks whether the given linked item is registered.
     *
     * @param linkedItem the linked item to check.
     * @return true if it exists; false otherwise.
     */
    boolean contains(ILinkedItem linkedItem);
}
