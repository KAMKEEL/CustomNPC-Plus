package noppes.npcs.api.event;

import noppes.npcs.api.IBlock;
import noppes.npcs.api.entity.IEnergyProjectile;
import noppes.npcs.api.entity.IEntity;

/**
 * Events fired during energy projectile lifecycle and impacts.
 */
public interface IEnergyProjectileEvent extends ICustomNPCsEvent {

    /** @return the energy projectile entity. */
    IEnergyProjectile getProjectile();

    /** @return the entity that fired this projectile. */
    IEntity getOwner();

    /**
     * Fired when an energy projectile is launched.
     * @hookName energyProjectileFired
     */
    interface FiredEvent extends IEnergyProjectileEvent {
    }

    /**
     * Fired each tick while the projectile exists.
     * @hookName energyProjectileTick
     */
    interface UpdateEvent extends IEnergyProjectileEvent {
        int getTick();
    }

    /**
     * Fired when the projectile hits an entity.
     * @hookName energyProjectileEntityImpact
     */
    interface EntityImpactEvent extends IEnergyProjectileEvent {
        /** @return the entity that was hit. */
        IEntity getTarget();

        /** @return the damage dealt. */
        float getDamage();

        /** @param damage the new damage value. */
        void setDamage(float damage);
    }

    /**
     * Fired when the projectile hits a block.
     * @hookName energyProjectileBlockImpact
     */
    interface BlockImpactEvent extends IEnergyProjectileEvent {
        /** @return the block that was hit. */
        IBlock getBlock();
    }

    /**
     * Fired when the projectile expires without hitting anything.
     * @hookName energyProjectileExpired
     */
    interface ExpiredEvent extends IEnergyProjectileEvent {
    }
}
