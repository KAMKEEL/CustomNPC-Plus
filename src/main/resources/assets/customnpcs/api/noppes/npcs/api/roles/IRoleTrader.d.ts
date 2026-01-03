/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.roles
 */

export interface IRoleTrader extends IRole {

    // Methods
    setSellOption(slot: number, currency: import('../item/IItemStack').IItemStack, currency2: import('../item/IItemStack').IItemStack, sold: import('../item/IItemStack').IItemStack): void;
    setSellOption(slot: number, currency: import('../item/IItemStack').IItemStack, sold: import('../item/IItemStack').IItemStack): void;
    getSellOption(slot: number): import('../item/IItemStack').IItemStack;
    getCurrency(slot: number): import('../item/IItemStack').IItemStack[];
    removeSellOption(slot: number): void;
    setMarket(name: string): void;
    getMarket(): string;
    getPurchaseNum(slot: number): number;
    getPurchaseNum(slot: number, player: import('../entity/IPlayer').IPlayer): number;
    resetPurchaseNum(): void;
    resetPurchaseNum(slot: number): void;
    resetPurchaseNum(slot: number, player: import('../entity/IPlayer').IPlayer): void;
    isSlotEnabled(slot: number): boolean;
    isSlotEnabled(slot: number, player: import('../entity/IPlayer').IPlayer): boolean;
    disableSlot(slot: number): void;
    disableSlot(slot: number, player: import('../entity/IPlayer').IPlayer): void;
    enableSlot(slot: number): void;
    enableSlot(slot: number, player: import('../entity/IPlayer').IPlayer): void;

}
