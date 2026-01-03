/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

export interface IPlayerAttributes {

    // Methods
    recalculate(player: import('../../entity/IPlayer').IPlayer): void;
    getAttributes(): import('./ICustomAttribute').ICustomAttribute[];
    getAttributeValue(key: string): number;
    hasAttribute(key: string): boolean;
    getAttribute(key: string): import('./ICustomAttribute').ICustomAttribute;

}
