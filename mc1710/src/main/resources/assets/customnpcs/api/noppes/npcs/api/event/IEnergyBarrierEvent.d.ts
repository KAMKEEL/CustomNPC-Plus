/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.event
 */

/**
 * Events fired during energy barrier lifecycle and interactions.
  * @javaFqn noppes.npcs.api.event.IEnergyBarrierEvent
*/
export interface IEnergyBarrierEvent extends import('./ICustomNPCsEvent').ICustomNPCsEvent {
    /** @return the energy barrier entity. */
    getBarrier(): import('../entity/IEnergyBarrier').IEnergyBarrier;
    /** @return the entity that owns this barrier. */
    getOwner(): import('../entity/IEntity').IEntity;
    readonly barrier: import('../entity/IEnergyBarrier').IEnergyBarrier;
    readonly owner: import('../entity/IEntity').IEntity;
}

export namespace IEnergyBarrierEvent {
    
    
    /**
     * Fired when an energy barrier is spawned.
     * @hookName energyBarrierSpawned
          * @javaFqn noppes.npcs.api.event.IEnergyBarrierEvent.SpawnedEvent
*/
    export interface SpawnedEvent extends IEnergyBarrierEvent {
    }
    /**
     * Fired each tick while the barrier exists.
     * @hookName energyBarrierTick
          * @javaFqn noppes.npcs.api.event.IEnergyBarrierEvent.UpdateEvent
*/
    export interface UpdateEvent extends IEnergyBarrierEvent {
    }
    /**
     * Fired when the barrier is hit by an energy projectile.
     * @hookName energyBarrierHit
          * @javaFqn noppes.npcs.api.event.IEnergyBarrierEvent.HitEvent
*/
    export interface HitEvent extends IEnergyBarrierEvent {
        /** @return the projectile that hit the barrier. */
        getProjectile(): import('../entity/IEnergyProjectile').IEnergyProjectile;
        /** @return the damage dealt. */
        getDamage(): import('./float').float;
        /** @param damage the new damage value. */
        setDamage(damage: import('./float').float): import('./void').void;
    }
    /**
     * Fired when the barrier is destroyed.
     * @hookName energyBarrierDestroyed
          * @javaFqn noppes.npcs.api.event.IEnergyBarrierEvent.DestroyedEvent
*/
    export interface DestroyedEvent extends IEnergyBarrierEvent {
    }
}
