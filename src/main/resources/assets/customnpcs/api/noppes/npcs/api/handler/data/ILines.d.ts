/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

export interface ILines {

    // Methods
    createLine(text: string): import('./ILine').ILine;
    getLine(isRandom: boolean): import('./ILine').ILine;
    getLine(lineIndex: number): import('./ILine').ILine;
    setLine(lineIndex: number, line: import('./ILine').ILine): void;
    removeLine(lineIndex: number): void;
    clear(): void;
    isEmpty(): boolean;
    getKeys(): Integer[];

}
