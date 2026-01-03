/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler
 */

export interface IPartyHandler {

    // Methods
    createParty(player: import('../entity/IPlayer').IPlayer): import('./data/IParty').IParty;
    disbandParty(player: import('../entity/IPlayer').IPlayer): void;

}
