package noppes.npcs.api.entity;

import noppes.npcs.api.IEnergyHandler;

/**
 * Represents a standalone energy explosion entity.
 * Created via {@link IEnergyHandler#createExplosion}.
 * Configure visual/damage properties, then call {@link #spawn()} to place in the world.
 *
 * When damage is set (&gt; 0), the explosion will deal area damage on the first tick
 * with distance-based falloff, routing through energy damage handlers for addon integration.
 */
public interface IEnergyExplosion extends IEnergyAbility {

    // ==================== RADIUS & DURATION ====================

    /** @return The maximum explosion radius in blocks. */
    float getRadius();

    /** @param radius Maximum explosion radius in blocks (clamped 0.5-50). Duration auto-scales. */
    void setRadius(float radius);

    /** @return Duration of the explosion visual in ticks (auto-calculated from radius). */
    int getDuration();

    // ==================== DAMAGE ====================

    /** @return Base damage dealt at the center. 0 means visual-only. */
    float getDamage();

    /** @param damage Base damage at the center. Setting &gt; 0 enables damage. */
    void setDamage(float damage);

    /** @return Base knockback strength. */
    float getKnockback();

    /** @param knockback Base knockback strength. */
    void setKnockback(float knockback);

    /** @return Upward knockback component. */
    float getKnockbackUp();

    /** @param knockbackUp Upward knockback component. */
    void setKnockbackUp(float knockbackUp);

    /** @return Damage falloff factor (0=no falloff, 1=full falloff at edge). */
    float getDamageFalloff();

    /** @param falloff Damage falloff factor (0-1). At 0.5, edge damage = 50% of center. */
    void setDamageFalloff(float falloff);

    // ==================== SPAWNING ====================

    /**
     * Spawn this explosion entity into the world.
     * Sends the visual to nearby clients and, if damage &gt; 0, applies area damage on the first tick.
     */
    void spawn();
}
