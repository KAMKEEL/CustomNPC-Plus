/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

export interface IMagic {

    // Methods
    getId(): number;
    getName(): string;
    setName(name: string): void;
    setColor(c: number): void;
    getColor(): number;
    getDisplayName(): string;
    setDisplayName(displayName: string): void;
    save(): void;
    hasInteraction(magicID: number): boolean;
    setInteraction(magicID: number, value: number): void;
    getInteraction(magicID: number, value: number): number;

}
