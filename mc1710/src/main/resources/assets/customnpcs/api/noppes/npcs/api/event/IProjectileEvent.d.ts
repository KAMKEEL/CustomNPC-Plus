/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.event
 */

/**
 * Events fired during NPC projectile lifecycle and impacts.
  * @javaFqn noppes.npcs.api.event.IProjectileEvent
*/
export interface IProjectileEvent extends import('./ICustomNPCsEvent').ICustomNPCsEvent {
    /** @return the projectile entity. */
    getProjectile(): import('../entity/IProjectile').IProjectile;
    /** @return the entity that launched this projectile. */
    getSource(): import('../entity/IEntity').IEntity;
    projectile: import('../entity/IProjectile').IProjectile;
    source: import('../entity/IEntity').IEntity;
}

export namespace IProjectileEvent {
    
    
    /**
     * Fired each tick while the projectile exists.
     * @hookName projectileTick
          * @javaFqn noppes.npcs.api.event.IProjectileEvent.UpdateEvent
*/
    export interface UpdateEvent extends IProjectileEvent {
    }
    /**
     * Fired when the projectile hits something.
     * @hookName projectileImpact
          * @javaFqn noppes.npcs.api.event.IProjectileEvent.ImpactEvent
*/
    export interface ImpactEvent extends IProjectileEvent {
        /** @return the impact type. */
        getType(): import('./int').int;
        /** @return the entity hit, or null if a block was hit. */
        getEntity(): import('../entity/IEntity').IEntity;
        /** @return the block hit, or null if an entity was hit. */
        getBlock(): import('../IBlock').IBlock;
        readonly type: import('./int').int;
        readonly target: Object;
    }
}
