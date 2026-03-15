/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.ability.data
 */

/**
 * Combat properties for energy ability projectiles.
 * Controls damage, knockback, and explosion behavior.
  * @javaFqn noppes.npcs.api.ability.data.IEnergyCombatData
*/
export interface IEnergyCombatData {
    /** @return Base damage dealt on hit. */
    getDamage(): import('./float').float;
    /** @param damage Base damage on hit. */
    setDamage(damage: import('./float').float): import('./void').void;
    /** @return Horizontal knockback strength applied on hit. */
    getKnockback(): import('./float').float;
    /** @param knockback Horizontal knockback strength. */
    setKnockback(knockback: import('./float').float): import('./void').void;
    /** @return Vertical knockback strength applied on hit. */
    getKnockbackUp(): import('./float').float;
    /** @param knockbackUp Vertical knockback strength. */
    setKnockbackUp(knockbackUp: import('./float').float): import('./void').void;
    /** @return Whether the projectile explodes on impact. */
    isExplosive(): import('./boolean').boolean;
    /** @param explosive Whether to explode on impact. */
    setExplosive(explosive: import('./boolean').boolean): import('./void').void;
    /** @return Explosion radius in blocks. */
    getExplosionRadius(): import('./float').float;
    /** @param explosionRadius Explosion radius in blocks. */
    setExplosionRadius(explosionRadius: import('./float').float): import('./void').void;
    /** @return Damage falloff multiplier over distance (0.0-1.0). */
    getExplosionDamageFalloff(): import('./float').float;
    /** @param explosionDamageFalloff Damage falloff multiplier (0.0-1.0). */
    setExplosionDamageFalloff(explosionDamageFalloff: import('./float').float): import('./void').void;
}
