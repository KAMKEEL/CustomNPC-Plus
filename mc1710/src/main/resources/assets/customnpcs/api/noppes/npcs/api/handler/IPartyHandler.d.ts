/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler
 */

/**
 * @javaFqn noppes.npcs.api.handler.IPartyHandler
 */
export interface IPartyHandler {
    /**
     * @param player - The Leader of the Party
     * @return IParty Object
     */
    createParty(player: import('../entity/IPlayer').IPlayer): import('./data/IParty').IParty;
    /**
     * @param player - Gets the party of current player and disbands it
     */
    disbandParty(player: import('../entity/IPlayer').IPlayer): import('./void').void;
}
