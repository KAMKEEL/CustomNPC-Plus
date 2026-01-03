/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

export interface ITransportCategory {

    // Methods
    getId(): number;
    setTitle(title: string): void;
    getTitle(): string;
    addLocation(name: string): void;
    getLocation(name: string): import('./ITransportLocation').ITransportLocation;
    removeLocation(name: string): void;

}
