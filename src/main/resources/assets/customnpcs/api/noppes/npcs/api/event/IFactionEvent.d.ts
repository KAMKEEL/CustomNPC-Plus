/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.event
 */

export interface IFactionEvent extends IPlayerEvent {

    // Methods
    getFaction(): import('../handler/data/IFaction').IFaction;

    // Nested interfaces
    interface FactionPoints extends IFactionEvent {
        decreased(): boolean;
        getPoints(): number;
    }

}
