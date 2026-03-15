/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api
 */

/**
 * @javaFqn noppes.npcs.api.IContainer
 */
export interface IContainer {
    getSize(): import('./int').int;
    getSlot(slot: import('./int').int): import('./item/IItemStack').IItemStack;
    /**
     *
     * @param slot The slot to be replaced
     * @param item The item replacing the previous item in that slot, as an IItemStack object
     */
    setSlot(slot: import('./int').int, item: import('./item/IItemStack').IItemStack): import('./void').void;
    /**
     *
     * @return An obfuscated MC inventory object.
     */
    getMCInventory(): import('../../../net/minecraft/inventory/IInventory').IInventory;
    /**
     *
     * @return An obfuscated MC container object.
     */
    getMCContainer(): import('../../../net/minecraft/inventory/Container').Container;
    /**
     *
     * @param itemStack    The item stack to be searched in the container
     * @param ignoreDamage Whether damage should be ignored when searching
     * @param ignoreNBT    Whether NBT values should be ignored when searching
     * @return The amount of the item stack found, based on the flags given above.
     */
    count(itemStack: import('./item/IItemStack').IItemStack, ignoreDamage: import('./boolean').boolean, ignoreNBT: import('./boolean').boolean): import('./int').int;
    /**
     *
     * @return A list of all item stacks in the container as a list of IItemStack objects.
     */
    getItems(): import('./item/IItemStack').IItemStack[];
    /**
     *
     * @return True if this container belongs to a Custom GUI.
     */
    isCustomGUI(): import('./boolean').boolean;
    /**
     * Sends changes to be reflected on the player's client.
     */
    detectAndSendChanges(): import('./void').void;
    isPlayerNotUsingContainer(player: import('./entity/IPlayer').IPlayer): import('./boolean').boolean;
}
