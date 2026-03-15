/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.jobs
 */

/**
 * @javaFqn noppes.npcs.api.jobs.IJobHealer
 */
export interface IJobHealer extends import('./IJob').IJob {
    heal(entity: import('../entity/IEntityLivingBase').IEntityLivingBase, amount: import('./float').float): import('./void').void;
    setRange(range: import('./int').int): import('./void').void;
    getRange(): import('./int').int;
    setSpeed(speed: import('./int').int): import('./void').void;
    getSpeed(): import('./int').int;
}
