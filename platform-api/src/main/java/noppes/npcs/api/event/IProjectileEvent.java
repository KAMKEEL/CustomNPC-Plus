package noppes.npcs.api.event;

import noppes.npcs.api.IBlock;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IProjectile;

/**
 * Events fired during NPC projectile lifecycle and impacts.
 */
public interface IProjectileEvent extends ICustomNPCsEvent {

    /** @return the projectile entity. */
    public IProjectile getProjectile();

    /** @return the entity that launched this projectile. */
    public IEntity getSource();

    /**
     * Fired each tick while the projectile exists.
     * @hookName projectileTick
     */
    interface UpdateEvent extends IProjectileEvent {
    }

    /**
     * Fired when the projectile hits something.
     * @hookName projectileImpact
     */
    interface ImpactEvent extends IProjectileEvent {
        /** @return the impact type. */
        public int getType();

        /** @return the entity hit, or null if a block was hit. */
        public IEntity getEntity();

        /** @return the block hit, or null if an entity was hit. */
        public IBlock getBlock();
    }

}
