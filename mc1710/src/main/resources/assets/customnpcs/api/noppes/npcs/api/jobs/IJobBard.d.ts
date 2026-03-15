/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.jobs
 */

/**
 * @javaFqn noppes.npcs.api.jobs.IJobBard
 */
export interface IJobBard extends import('./IJob').IJob {
    getSong(): String;
    setSong(song: String): import('./void').void;
    setInstrument(i: import('./int').int): import('./void').void;
    getInstrumentId(): import('./int').int;
    setMinRange(range: import('./int').int): import('./void').void;
    getMinRange(): import('./int').int;
    setMaxRange(range: import('./int').int): import('./void').void;
    getMaxRange(): import('./int').int;
    setStreaming(streaming: import('./boolean').boolean): import('./void').void;
    getStreaming(): import('./boolean').boolean;
    hasOffRange(value: import('./boolean').boolean): import('./void').void;
    hasOffRange(): import('./boolean').boolean;
}
