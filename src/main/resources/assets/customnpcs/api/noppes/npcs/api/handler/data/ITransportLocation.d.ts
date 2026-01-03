/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

export interface ITransportLocation {

    // Methods
    getId(): number;
    setName(name: string): void;
    getName(): string;
    setDimension(dimension: number): void;
    getDimension(): number;
    setType(type: number): void;
    getType(): number;
    setPosition(x: number, y: number, z: number): void;
    setPosition(pos: import('../../IPos').IPos): void;
    getX(): number;
    getY(): number;
    getZ(): number;
    save(): void;

}
