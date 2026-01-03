/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.event
 */

export interface IProjectileEvent extends ICustomNPCsEvent {

    // Methods
    getProjectile(): import('../entity/IProjectile').IProjectile;
    getSource(): import('../entity/IEntity').IEntity;

    // Nested interfaces
    interface UpdateEvent extends IProjectileEvent {
    }
    interface ImpactEvent extends IProjectileEvent {
        getType(): number;
        getEntity(): import('../entity/IEntity').IEntity;
        getBlock(): import('../IBlock').IBlock;
    }

}
