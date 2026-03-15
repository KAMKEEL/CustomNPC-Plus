/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.jobs
 */

/**
 * @javaFqn noppes.npcs.api.jobs.IJob
 */
export interface IJob {
    getType(): import('./int').int;
    getNpc(): import('../entity/ICustomNpc').ICustomNpc;
    readonly npc: import('./EntityNPCInterface').EntityNPCInterface;
    readonly jobInterface: import('./JobInterface').JobInterface;
}
