/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

export interface IPlayerEffect {

    // Methods
    kill(): void;
    getId(): number;
    getDuration(): number;
    setDuration(duration: number): void;
    getLevel(): byte;
    setLevel(level: byte): void;
    getName(): string;
    performEffect(player: import('../../entity/IPlayer').IPlayer): void;
    getIndex(): number;
    setIndex(index: number): void;

}
