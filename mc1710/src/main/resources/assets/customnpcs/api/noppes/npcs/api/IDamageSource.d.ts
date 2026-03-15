/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api
 */

/**
 * @javaFqn noppes.npcs.api.IDamageSource
 */
export interface IDamageSource {
    /**
     *
     * @return The damage type of the damage source as a string. Ex: "lava", "explosion", "magic", "outOfWorld", etc.
     */
    getType(): String;
    isUnblockable(): import('./boolean').boolean;
    isProjectile(): import('./boolean').boolean;
    /**
     *
     * @return The entity source of where the damage source originated. If a player was shot by an arrow from a skeleton, this would return an IEntity object of the skeleton.
     */
    getTrueSource(): import('./entity/IEntity').IEntity;
    /**
     *
     * @return The entity source of where the damage source originated. If a player was shot by an arrow from a skeleton, this would return an IEntity object of the arrow.
     */
    getImmediateSource(): import('./entity/IEntity').IEntity;
    /**
     *
     * @return An obfuscated MC damage source object.
     */
    getMCDamageSource(): import('../../../net/minecraft/util/DamageSource').DamageSource;
}
