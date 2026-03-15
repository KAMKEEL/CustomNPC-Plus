/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler
 */

/**
 * Handles registration and retrieval of linked items.
 * Linked items are custom item definitions that can be updated globally.
  * @javaFqn noppes.npcs.api.handler.ILinkedItemHandler
*/
export interface ILinkedItemHandler {
    /**
     * Creates a new linked item with the given name.
     *
     * @param name the item name.
     * @return the created linked item.
     */
    createItem(name: String): import('./data/ILinkedItem').ILinkedItem;
    /**
     * Creates an ItemStack from a linked item by its ID.
     *
     * @param id the linked item ID.
     * @return the item stack, or null if the ID is invalid.
     */
    createItemStack(id: import('./int').int): import('../item/IItemStack').IItemStack;
    /**
     * Registers a linked item with this handler.
     *
     * @param linkedItem the linked item to add.
     */
    add(linkedItem: import('./data/ILinkedItem').ILinkedItem): import('./void').void;
    /**
     * Removes and returns the linked item with the given ID.
     *
     * @param id the linked item ID.
     * @return the removed linked item, or null if not found.
     */
    remove(id: import('./int').int): import('./data/ILinkedItem').ILinkedItem;
    /**
     * Returns the linked item with the given ID.
     *
     * @param id the linked item ID.
     * @return the linked item, or null if not found.
     */
    get(id: import('./int').int): import('./data/ILinkedItem').ILinkedItem;
    /**
     * Checks whether a linked item with the given ID exists.
     *
     * @param id the linked item ID.
     * @return true if it exists; false otherwise.
     */
    contains(id: import('./int').int): import('./boolean').boolean;
    /**
     * Checks whether the given linked item is registered.
     *
     * @param linkedItem the linked item to check.
     * @return true if it exists; false otherwise.
     */
    contains(linkedItem: import('./data/ILinkedItem').ILinkedItem): import('./boolean').boolean;
}
