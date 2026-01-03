/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.jobs
 */

export interface IJobItemGiver extends IJob {

    // Methods
    setCooldown(cooldown: number): void;
    setCooldownType(type: number): void;
    getCooldownType(): number;
    setGivingMethod(method: number): void;
    getGivingMethod(): number;
    setLines(lines: string[]): void;
    getLines(): string[];
    setAvailability(availability: import('../handler/data/IAvailability').IAvailability): void;
    getAvailability(): import('../handler/data/IAvailability').IAvailability;
    setItem(slot: number, item: import('../item/IItemStack').IItemStack): void;
    getItems(): import('../item/IItemStack').IItemStack[];
    giveItems(player: import('../entity/IPlayer').IPlayer): boolean;
    canPlayerInteract(player: import('../entity/IPlayer').IPlayer): boolean;

}
