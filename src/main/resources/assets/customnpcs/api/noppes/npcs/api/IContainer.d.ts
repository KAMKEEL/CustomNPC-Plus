/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api
 */

export interface IContainer {

    // Methods
    getSize(): number;
    getSlot(slot: number): import('./item/IItemStack').IItemStack;
    setSlot(slot: number, item: import('./item/IItemStack').IItemStack): void;
    getMCInventory(): IInventory;
    getMCContainer(): Container;
    count(itemStack: import('./item/IItemStack').IItemStack, ignoreDamage: boolean, ignoreNBT: boolean): number;
    getItems(): import('./item/IItemStack').IItemStack[];
    isCustomGUI(): boolean;
    detectAndSendChanges(): void;
    isPlayerNotUsingContainer(player: import('./entity/IPlayer').IPlayer): boolean;

}
