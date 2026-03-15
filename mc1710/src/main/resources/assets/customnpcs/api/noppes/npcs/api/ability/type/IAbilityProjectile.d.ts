/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.ability.type
 */

/**
 * API interface for Projectile abilities.
 * Ranged projectile attacks.
  * @javaFqn noppes.npcs.api.ability.type.IAbilityProjectile
*/
export interface IAbilityProjectile extends import('../IAbility').IAbility {
    /** @return Base damage dealt on hit. */
    getDamage(): import('./float').float;
    /** @param damage Base damage on hit. */
    setDamage(damage: import('./float').float): import('./void').void;
    /** @return Travel speed of the projectile in blocks per tick. */
    getSpeed(): import('./float').float;
    /** @param speed Travel speed in blocks per tick. */
    setSpeed(speed: import('./float').float): import('./void').void;
    /** @return Knockback strength applied on hit. */
    getKnockback(): import('./float').float;
    /** @param knockback Knockback strength. */
    setKnockback(knockback: import('./float').float): import('./void').void;
    /** @return The projectile type identifier (e.g. arrow, snowball). */
    getProjectileType(): String;
    /** @param type Projectile type identifier. */
    setProjectileType(type: String): import('./void').void;
    /** @return Whether the projectile explodes on impact. */
    isExplosive(): import('./boolean').boolean;
    /** @param explosive Whether to explode on impact. */
    setExplosive(explosive: import('./boolean').boolean): import('./void').void;
    /** @return Explosion radius in blocks. */
    getExplosionRadius(): import('./float').float;
    /** @param radius Explosion radius in blocks. */
    setExplosionRadius(radius: import('./float').float): import('./void').void;
    /** @return Whether the projectile tracks its target. */
    isHoming(): import('./boolean').boolean;
    /** @param homing Whether to track the target. */
    setHoming(homing: import('./boolean').boolean): import('./void').void;
    /** @return Homing turn strength (higher = tighter turns). */
    getHomingStrength(): import('./float').float;
    /** @param strength Homing turn strength. */
    setHomingStrength(strength: import('./float').float): import('./void').void;
    getDisplayDamage: import('./float').float;
}
