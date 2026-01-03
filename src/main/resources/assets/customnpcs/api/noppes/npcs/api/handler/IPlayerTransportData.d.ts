/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler
 */

export interface IPlayerTransportData {

    // Methods
    hasTransport(id: number): boolean;
    addTransport(id: number): void;
    addTransport(location: import('./data/ITransportLocation').ITransportLocation): void;
    getTransport(id: number): import('./data/ITransportLocation').ITransportLocation;
    getTransports(): import('./data/ITransportLocation').ITransportLocation[];
    removeTransport(id: number): void;

}
