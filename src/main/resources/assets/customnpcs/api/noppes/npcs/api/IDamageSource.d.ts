/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api
 */

export interface IDamageSource {

    // Methods
    getType(): string;
    isUnblockable(): boolean;
    isProjectile(): boolean;
    getTrueSource(): import('./entity/IEntity').IEntity;
    getImmediateSource(): import('./entity/IEntity').IEntity;
    getMCDamageSource(): DamageSource;

}
