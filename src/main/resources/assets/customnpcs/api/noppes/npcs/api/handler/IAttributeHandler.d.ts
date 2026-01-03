/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler
 */

export interface IAttributeHandler {

    // Methods
    getPlayerAttributes(player: import('../entity/IPlayer').IPlayer): import('./data/IPlayerAttributes').IPlayerAttributes;
    getAttributeDefinition(key: string): import('./data/IAttributeDefinition').IAttributeDefinition;
    getAllAttributesArray(): import('./data/IAttributeDefinition').IAttributeDefinition[];

}
