/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.ability.type
 */

/**
 * Shared API interface for energy projectile abilities (Orb, Disc, Beam, LaserShot).
 * Contains methods common to all energy projectile types.
  * @javaFqn noppes.npcs.api.ability.type.IAbilityEnergyProjectile
*/
export interface IAbilityEnergyProjectile extends import('../IAbility').IAbility {
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
    /** @param knockback Vertical knockback strength. */
    setKnockbackUp(knockback: import('./float').float): import('./void').void;
    /** @return Whether the projectile explodes on impact. */
    isExplosive(): import('./boolean').boolean;
    /** @param explosive Whether to explode on impact. */
    setExplosive(explosive: import('./boolean').boolean): import('./void').void;
    /** @return Explosion radius in blocks. */
    getExplosionRadius(): import('./float').float;
    /** @param radius Explosion radius in blocks. */
    setExplosionRadius(radius: import('./float').float): import('./void').void;
    /** @return Damage falloff multiplier over explosion distance (0.0-1.0). */
    getExplosionDamageFalloff(): import('./float').float;
    /** @param falloff Damage falloff multiplier (0.0-1.0). */
    setExplosionDamageFalloff(falloff: import('./float').float): import('./void').void;
    /** @return Hit type ordinal (determines how repeated hits are handled). */
    getHitType(): import('./int').int;
    /** @param hitType Hit type ordinal. */
    setHitType(hitType: import('./int').int): import('./void').void;
    /** @return Delay in ticks between repeated hits on the same target. */
    getMultiHitDelayTicks(): import('./int').int;
    /** @param delayTicks Delay in ticks between repeated hits. */
    setMultiHitDelayTicks(delayTicks: import('./int').int): import('./void').void;
    /** @return Maximum number of times this projectile can hit the same target. */
    getMaxHits(): import('./int').int;
    /** @param maxHits Maximum hit count per target. */
    setMaxHits(maxHits: import('./int').int): import('./void').void;
    /** @return Maximum travel distance in blocks before the projectile expires. */
    getMaxDistance(): import('./float').float;
    /** @param distance Maximum travel distance in blocks. */
    setMaxDistance(distance: import('./float').float): import('./void').void;
    /** @return Maximum lifetime in ticks before the projectile expires. */
    getMaxLifetime(): import('./int').int;
    /** @param ticks Maximum lifetime in ticks. */
    setMaxLifetime(ticks: import('./int').int): import('./void').void;
    /** @return Inner (core) color as a packed RGB integer. */
    getInnerColor(): import('./int').int;
    /** @param color Inner color as a packed RGB integer. */
    setInnerColor(color: import('./int').int): import('./void').void;
    /** @return Outer (glow) color as a packed RGB integer. */
    getOuterColor(): import('./int').int;
    /** @param color Outer color as a packed RGB integer. */
    setOuterColor(color: import('./int').int): import('./void').void;
    /** @return Whether the outer color layer is rendered. */
    isOuterColorEnabled(): import('./boolean').boolean;
    /** @param enabled Whether to render the outer color layer. */
    setOuterColorEnabled(enabled: import('./boolean').boolean): import('./void').void;
    /** @return Width of the outer color layer relative to the projectile size. */
    getOuterColorWidth(): import('./float').float;
    /** @param width Outer color layer width. */
    setOuterColorWidth(width: import('./float').float): import('./void').void;
    /** @return Alpha (opacity) of the outer color layer (0.0-1.0). */
    getOuterColorAlpha(): import('./float').float;
    /** @param alpha Outer color opacity (0.0-1.0). */
    setOuterColorAlpha(alpha: import('./float').float): import('./void').void;
    /** @return Whether the lightning visual effect is enabled. */
    hasLightningEffect(): import('./boolean').boolean;
    /** @param enabled Whether to enable the lightning effect. */
    setLightningEffect(enabled: import('./boolean').boolean): import('./void').void;
    /** @return Density of lightning arcs (higher = more arcs). */
    getLightningDensity(): import('./float').float;
    /** @param density Lightning arc density. */
    setLightningDensity(density: import('./float').float): import('./void').void;
    /** @return Radius of the lightning effect around the projectile, in blocks. */
    getLightningRadius(): import('./float').float;
    /** @param radius Lightning effect radius in blocks. */
    setLightningRadius(radius: import('./float').float): import('./void').void;
    /** @return Anchor point ordinal (0=FEET, 1=CENTER, 2=RIGHT_HAND, 3=LEFT_HAND, 4=HEAD, 5=FRONT, 6=ABOVE_HEAD). */
    getAnchorPoint(): import('./int').int;
    /** @param point Anchor point ordinal (0=FEET, 1=CENTER, 2=RIGHT_HAND, 3=LEFT_HAND, 4=HEAD, 5=FRONT, 6=ABOVE_HEAD). */
    setAnchorPoint(point: import('./int').int): import('./void').void;
    /** @return Whether the projectile launches from its anchor position instead of the default eye/look-vector position. */
    getLaunchFromAnchor(): import('./boolean').boolean;
    /** @param launchFromAnchor When true, the projectile launches from its configured anchor position. */
    setLaunchFromAnchor(launchFromAnchor: import('./boolean').boolean): import('./void').void;
    getDisplayDamage: import('./float').float;
}
