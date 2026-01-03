/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler
 */

export interface ITransportHandler {

    // Methods
    categories(): import('./data/ITransportCategory').ITransportCategory[];
    createCategory(title: string): void;
    getCategory(title: string): import('./data/ITransportCategory').ITransportCategory;
    removeCategory(title: string): void;

}
