package noppes.npcs.api.ability.type;

import noppes.npcs.api.ability.IAbility;

/**
 * API interface for Projectile abilities.
 * Ranged projectile attacks.
 */
public interface IAbilityProjectile extends IAbility {

    /** @return Base damage dealt on hit. */
    float getDamage();

    /** @param damage Base damage on hit. */
    void setDamage(float damage);

    /** @return Travel speed of the projectile in blocks per tick. */
    float getSpeed();

    /** @param speed Travel speed in blocks per tick. */
    void setSpeed(float speed);

    /** @return Knockback strength applied on hit. */
    float getKnockback();

    /** @param knockback Knockback strength. */
    void setKnockback(float knockback);

    /** @return The projectile type identifier (e.g. arrow, snowball). */
    String getProjectileType();

    /** @param type Projectile type identifier. */
    void setProjectileType(String type);

    /** @return Whether the projectile explodes on impact. */
    boolean isExplosive();

    /** @param explosive Whether to explode on impact. */
    void setExplosive(boolean explosive);

    /** @return Explosion radius in blocks. */
    float getExplosionRadius();

    /** @param radius Explosion radius in blocks. */
    void setExplosionRadius(float radius);

    /** @return Whether the projectile tracks its target. */
    boolean isHoming();

    /** @param homing Whether to track the target. */
    void setHoming(boolean homing);

    /** @return Homing turn strength (higher = tighter turns). */
    float getHomingStrength();

    /** @param strength Homing turn strength. */
    void setHomingStrength(float strength);
}
