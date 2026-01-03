/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.roles
 */

export interface IRoleTransporter extends IRole {

    // Methods
    getName(): string;
    getTransportId(): number;
    unlock(player: IPlayer<EntityPlayerMP, location: import('../handler/data/ITransportLocation').ITransportLocation): void;
    getTransport(): import('../handler/data/ITransportLocation').ITransportLocation;
    hasTransport(): boolean;
    setTransport(location: import('../handler/data/ITransportLocation').ITransportLocation): void;

}
