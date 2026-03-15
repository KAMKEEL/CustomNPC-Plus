/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.item
 */

/**
 * @javaFqn noppes.npcs.api.item.IItemLinked
 */
export interface IItemLinked extends import('./IItemCustomizable').IItemCustomizable {
    getLinkedItem(): import('../handler/data/ILinkedItem').ILinkedItem;
    /**
     * Sets the current durability value for this linked item stack.
     * This is a per-stack override, not changing the linked item template.
     *
     * @param durabilityValue The durability value
     */
    setDurabilityValue(durabilityValue: import('./float').float): import('./void').void;
    linkedItem: import('./LinkedItem').LinkedItem;
    durabilityValue: import('./double').double;
    linkedVersion: import('./int').int;
}
