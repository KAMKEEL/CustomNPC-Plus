package noppes.npcs.api.event;

import noppes.npcs.api.entity.IEnergyBarrier;
import noppes.npcs.api.entity.IEnergyProjectile;
import noppes.npcs.api.entity.IEntity;

/**
 * Events fired during energy barrier lifecycle and interactions.
 */
public interface IEnergyBarrierEvent extends ICustomNPCsEvent {

    /** @return the energy barrier entity. */
    IEnergyBarrier getBarrier();

    /** @return the entity that owns this barrier. */
    IEntity getOwner();

    /**
     * Fired when an energy barrier is spawned.
     * @hookName energyBarrierSpawned
     */
    interface SpawnedEvent extends IEnergyBarrierEvent {
    }

    /**
     * Fired each tick while the barrier exists.
     * @hookName energyBarrierTick
     */
    interface UpdateEvent extends IEnergyBarrierEvent {
    }

    /**
     * Fired when the barrier is hit by an energy projectile.
     * @hookName energyBarrierHit
     */
    interface HitEvent extends IEnergyBarrierEvent {
        /** @return the projectile that hit the barrier. */
        IEnergyProjectile getProjectile();

        /** @return the damage dealt. */
        float getDamage();

        /** @param damage the new damage value. */
        void setDamage(float damage);
    }

    /**
     * Fired when the barrier is destroyed.
     * @hookName energyBarrierDestroyed
     */
    interface DestroyedEvent extends IEnergyBarrierEvent {
    }
}
