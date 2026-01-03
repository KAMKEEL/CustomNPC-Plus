/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api
 */

export interface ITimers {

    // Methods
    timerIds(): number[];
    start(id: number, ticks: number, repeat: boolean): void;
    forceStart(id: number, ticks: number, repeat: boolean): void;
    has(id: number): boolean;
    stop(id: number): boolean;
    reset(id: number): void;
    clear(): void;
    ticks(id: number): number;
    setTicks(id: number, ticks: number): void;
    maxTicks(id: number): number;
    setMaxTicks(id: number, maxTicks: number): void;
    repeats(id: number): boolean;
    setRepeats(id: number, repeat: boolean): void;
    size(): number;

}
