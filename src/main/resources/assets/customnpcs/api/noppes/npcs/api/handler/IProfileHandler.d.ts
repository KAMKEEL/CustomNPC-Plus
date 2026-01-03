/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler
 */

export interface IProfileHandler {

    // Methods
    getProfile(player: import('../entity/IPlayer').IPlayer): import('./data/IProfile').IProfile;
    changeSlot(player: import('../entity/IPlayer').IPlayer, slotID: number): boolean;
    hasSlot(player: import('../entity/IPlayer').IPlayer, slotID: number): boolean;
    removeSlot(player: import('../entity/IPlayer').IPlayer, slotID: number): boolean;
    getSlotPlayerData(player: import('../entity/IPlayer').IPlayer, slotID: number): import('./IPlayerData').IPlayerData;
    saveSlotData(player: import('../entity/IPlayer').IPlayer): void;

}
