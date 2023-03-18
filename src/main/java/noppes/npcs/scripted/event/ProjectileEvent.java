package noppes.npcs.scripted.event;

import noppes.npcs.api.entity.IProjectile;

public class ProjectileEvent extends CustomNPCsEvent {
    public IProjectile projectile;

    public ProjectileEvent(IProjectile projectile) {
        this.projectile = projectile;
    }

    /**
     * projectileTick
     */
    public static class UpdateEvent extends ProjectileEvent {
        public UpdateEvent(IProjectile projectile) {
            super(projectile);
        }
    }

    /**
     * projectileImpact
     */
    public static class ImpactEvent extends ProjectileEvent {
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
    }
}
