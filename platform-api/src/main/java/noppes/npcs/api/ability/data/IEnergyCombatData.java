package noppes.npcs.api.ability.data;

/**
 * Combat properties for energy ability projectiles.
 * Controls damage, knockback, and explosion behavior.
 */
public interface IEnergyCombatData {
    /** @return Base damage dealt on hit. */
    float getDamage();

    /** @param damage Base damage on hit. */
    void setDamage(float damage);

    /** @return Horizontal knockback strength applied on hit. */
    float getKnockback();

    /** @param knockback Horizontal knockback strength. */
    void setKnockback(float knockback);

    /** @return Vertical knockback strength applied on hit. */
    float getKnockbackUp();

    /** @param knockbackUp Vertical knockback strength. */
    void setKnockbackUp(float knockbackUp);

    /** @return Whether the projectile explodes on impact. */
    boolean isExplosive();

    /** @param explosive Whether to explode on impact. */
    void setExplosive(boolean explosive);

    /** @return Explosion radius in blocks. */
    float getExplosionRadius();

    /** @param explosionRadius Explosion radius in blocks. */
    void setExplosionRadius(float explosionRadius);

    /** @return Damage falloff multiplier over distance (0.0-1.0). */
    float getExplosionDamageFalloff();

    /** @param explosionDamageFalloff Damage falloff multiplier (0.0-1.0). */
    void setExplosionDamageFalloff(float explosionDamageFalloff);
}
