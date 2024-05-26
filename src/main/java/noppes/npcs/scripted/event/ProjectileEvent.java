package noppes.npcs.scripted.event;

import noppes.npcs.api.IBlock;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IProjectile;
import noppes.npcs.api.event.IProjectileEvent;

public class ProjectileEvent extends CustomNPCsEvent implements IProjectileEvent {
    public IProjectile projectile;
    public IEntity source;

    public ProjectileEvent(IProjectile projectile) {
        this.projectile = projectile;
        this.source = projectile.getThrower();
    }

    public IProjectile getProjectile() {
        return projectile;
    }

    public IEntity getSource() {
        return source;
    }

    /**
     * projectileTick
     */
    public static class UpdateEvent extends ProjectileEvent implements IProjectileEvent.UpdateEvent {
        public UpdateEvent(IProjectile projectile) {
            super(projectile);
        }
    }

    /**
     * projectileImpact
     */
    public static class ImpactEvent extends ProjectileEvent implements  IProjectileEvent.ImpactEvent {
        /**
         * 0:entity, 1:block
         */
        public final int type;
        public final Object target;

        public ImpactEvent(IProjectile projectile, int type, Object target) {
            super(projectile);
            this.type = type;
            this.target = target;
        }

        public int getType() {
            return type;
        }

        public IEntity getEntity() {
            if(type == 0)
                return (IEntity) target;
            return null;
        }

        public IBlock getBlock() {
            if(type == 1)
                return (IBlock) target;
            return null;
        }
    }
}
