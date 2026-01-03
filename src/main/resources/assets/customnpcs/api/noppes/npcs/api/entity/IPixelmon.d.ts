/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity
 */

export interface IPixelmon extends IAnimal<T> {

    // Methods
    getIsShiny(): boolean;
    setIsShiny(bo: boolean): void;
    getLevel(): number;
    setLevel(level: number): void;
    getIV(type: number): number;
    setIV(type: number, value: number): void;
    getEV(type: number): number;
    setEV(type: number, value: number): void;
    getStat(type: number): number;
    setStat(type: number, value: number): void;
    getSize(): number;
    setSize(type: number): void;
    getHapiness(): number;
    setHapiness(value: number): void;
    getNature(): number;
    setNature(type: number): void;
    getPokeball(): number;
    setPokeball(type: number): void;
    getNickname(): string;
    hasNickname(): boolean;
    setNickname(name: string): void;
    getMove(slot: number): string;
    setMove(slot: number, move: string): void;

}
