/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.jobs
 */

/**
 * @javaFqn noppes.npcs.api.jobs.IJobItemGiver
 */
export interface IJobItemGiver extends import('./IJob').IJob {
    setCooldown(cooldown: import('./int').int): import('./void').void;
    setCooldownType(type: import('./int').int): import('./void').void;
    getCooldownType(): import('./int').int;
    setGivingMethod(method: import('./int').int): import('./void').void;
    getGivingMethod(): import('./int').int;
    setLines(lines: String[]): import('./void').void;
    getLines(): String[];
    setAvailability(availability: import('../handler/data/IAvailability').IAvailability): import('./void').void;
    getAvailability(): import('../handler/data/IAvailability').IAvailability;
    setItem(slot: import('./int').int, item: import('../item/IItemStack').IItemStack): import('./void').void;
    getItems(): import('../item/IItemStack').IItemStack[];
    giveItems(player: import('../entity/IPlayer').IPlayer): import('./boolean').boolean;
    canPlayerInteract(player: import('../entity/IPlayer').IPlayer): import('./boolean').boolean;
}
