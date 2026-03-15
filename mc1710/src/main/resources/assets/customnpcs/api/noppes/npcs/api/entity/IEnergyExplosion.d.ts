/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity
 */

/**
 * Represents a standalone energy explosion entity.
 * Created via {@link IEnergyHandler#createExplosion}.
 * Configure visual/damage properties, then call {@link #spawn()} to place in the world.
 *
 * When damage is set (&gt; 0), the explosion will deal area damage on the first tick
 * with distance-based falloff, routing through energy damage handlers for addon integration.
  * @javaFqn noppes.npcs.api.entity.IEnergyExplosion
*/
export interface IEnergyExplosion<T extends Entity /* net.minecraft.entity.Entity */> extends import('./IEnergyAbility').IEnergyAbility {
    /** @return The maximum explosion radius in blocks. */
    getRadius(): import('./float').float;
    /** @param radius Maximum explosion radius in blocks (clamped 0.5-50). Duration auto-scales. */
    setRadius(radius: import('./float').float): import('./void').void;
    /** @return Duration of the explosion visual in ticks (auto-calculated from radius). */
    getDuration(): import('./int').int;
    /** @return Base damage dealt at the center. 0 means visual-only. */
    getDamage(): import('./float').float;
    /** @param damage Base damage at the center. Setting &gt; 0 enables damage. */
    setDamage(damage: import('./float').float): import('./void').void;
    /** @return Base knockback strength. */
    getKnockback(): import('./float').float;
    /** @param knockback Base knockback strength. */
    setKnockback(knockback: import('./float').float): import('./void').void;
    /** @return Upward knockback component. */
    getKnockbackUp(): import('./float').float;
    /** @param knockbackUp Upward knockback component. */
    setKnockbackUp(knockbackUp: import('./float').float): import('./void').void;
    /** @return Damage falloff factor (0=no falloff, 1=full falloff at edge). */
    getDamageFalloff(): import('./float').float;
    /** @param falloff Damage falloff factor (0-1). At 0.5, edge damage = 50% of center. */
    setDamageFalloff(falloff: import('./float').float): import('./void').void;
    /**
     * Spawn this explosion entity into the world.
     * Sends the visual to nearby clients and, if damage &gt; 0, applies area damage on the first tick.
     */
    spawn(): import('./void').void;
}
