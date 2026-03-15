/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.jobs
 */

/**
 * @javaFqn noppes.npcs.api.jobs.IJobSpawner
 */
export interface IJobSpawner extends import('./IJob').IJob {
    spawnEntity(number: import('./int').int): import('../entity/IEntityLivingBase').IEntityLivingBase;
    getEntity(number: import('./int').int, x: import('./int').int, y: import('./int').int, z: import('./int').int, world: import('../IWorld').IWorld): import('../entity/IEntityLivingBase').IEntityLivingBase;
    getEntity(number: import('./int').int, pos: import('../IPos').IPos, world: import('../IWorld').IWorld): import('../entity/IEntityLivingBase').IEntityLivingBase;
    setEntity(number: import('./int').int, entityLivingBase: import('../entity/IEntityLivingBase').IEntityLivingBase): import('./void').void;
    /**
     * Removes all spawned entities
     */
    removeAllSpawned(): import('./void').void;
    getNearbySpawned(): import('../entity/IEntityLivingBase').IEntityLivingBase[];
    hasPixelmon(): import('./boolean').boolean;
    isEmpty(): import('./boolean').boolean;
    isOnCooldown(player: import('../entity/IPlayer').IPlayer): import('./boolean').boolean;
}
