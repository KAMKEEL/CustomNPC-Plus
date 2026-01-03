/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.jobs
 */

export interface IJobBard extends IJob {

    // Methods
    getSong(): string;
    setSong(song: string): void;
    setInstrument(i: number): void;
    getInstrumentId(): number;
    setMinRange(range: number): void;
    getMinRange(): number;
    setMaxRange(range: number): void;
    getMaxRange(): number;
    setStreaming(streaming: boolean): void;
    getStreaming(): boolean;
    hasOffRange(value: boolean): void;
    hasOffRange(): boolean;

}
