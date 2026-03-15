/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.event
 */

/**
 * Events fired during energy projectile lifecycle and impacts.
  * @javaFqn noppes.npcs.api.event.IEnergyProjectileEvent
*/
export interface IEnergyProjectileEvent extends import('./ICustomNPCsEvent').ICustomNPCsEvent {
    /** @return the energy projectile entity. */
    getProjectile(): import('../entity/IEnergyProjectile').IEnergyProjectile;
    /** @return the entity that fired this projectile. */
    getOwner(): import('../entity/IEntity').IEntity;
    readonly projectile: import('../entity/IEnergyProjectile').IEnergyProjectile;
    readonly owner: import('../entity/IEntity').IEntity;
}

export namespace IEnergyProjectileEvent {
    
    
    /**
     * Fired when an energy projectile is launched.
     * @hookName energyProjectileFired
          * @javaFqn noppes.npcs.api.event.IEnergyProjectileEvent.FiredEvent
*/
    export interface FiredEvent extends IEnergyProjectileEvent {
    }
    /**
     * Fired each tick while the projectile exists.
     * @hookName energyProjectileTick
          * @javaFqn noppes.npcs.api.event.IEnergyProjectileEvent.UpdateEvent
*/
    export interface UpdateEvent extends IEnergyProjectileEvent {
        getTick(): import('./int').int;
        getTick: import('./int').int;
    }
    /**
     * Fired when the projectile hits an entity.
     * @hookName energyProjectileEntityImpact
          * @javaFqn noppes.npcs.api.event.IEnergyProjectileEvent.EntityImpactEvent
*/
    export interface EntityImpactEvent extends IEnergyProjectileEvent {
        /** @return the entity that was hit. */
        getTarget(): import('../entity/IEntity').IEntity;
        /** @return the damage dealt. */
        getDamage(): import('./float').float;
        /** @param damage the new damage value. */
        setDamage(damage: import('./float').float): import('./void').void;
    }
    /**
     * Fired when the projectile hits a block.
     * @hookName energyProjectileBlockImpact
          * @javaFqn noppes.npcs.api.event.IEnergyProjectileEvent.BlockImpactEvent
*/
    export interface BlockImpactEvent extends IEnergyProjectileEvent {
        /** @return the block that was hit. */
        getBlock(): import('../IBlock').IBlock;
    }
    /**
     * Fired when the projectile expires without hitting anything.
     * @hookName energyProjectileExpired
          * @javaFqn noppes.npcs.api.event.IEnergyProjectileEvent.ExpiredEvent
*/
    export interface ExpiredEvent extends IEnergyProjectileEvent {
    }
}
