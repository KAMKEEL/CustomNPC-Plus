package noppes.npcs.api.ability.type;

import noppes.npcs.api.ability.IAbility;

/**
 * Shared API interface for energy projectile abilities (Orb, Disc, Beam, LaserShot).
 * Contains methods common to all energy projectile types.
 */
public interface IAbilityEnergyProjectile extends IAbility {

    // ==================== COMBAT ====================

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

    /** @param knockback Vertical knockback strength. */
    void setKnockbackUp(float knockback);

    /** @return Whether the projectile explodes on impact. */
    boolean isExplosive();

    /** @param explosive Whether to explode on impact. */
    void setExplosive(boolean explosive);

    /** @return Explosion radius in blocks. */
    float getExplosionRadius();

    /** @param radius Explosion radius in blocks. */
    void setExplosionRadius(float radius);

    /** @return Damage falloff multiplier over explosion distance (0.0-1.0). */
    float getExplosionDamageFalloff();

    /** @param falloff Damage falloff multiplier (0.0-1.0). */
    void setExplosionDamageFalloff(float falloff);

    /** @return Hit type ordinal (determines how repeated hits are handled). */
    int getHitType();

    /** @param hitType Hit type ordinal. */
    void setHitType(int hitType);

    /** @return Delay in ticks between repeated hits on the same target. */
    int getMultiHitDelayTicks();

    /** @param delayTicks Delay in ticks between repeated hits. */
    void setMultiHitDelayTicks(int delayTicks);

    /** @return Maximum number of times this projectile can hit the same target. */
    int getMaxHits();

    /** @param maxHits Maximum hit count per target. */
    void setMaxHits(int maxHits);

    // ==================== LIFESPAN ====================

    /** @return Maximum travel distance in blocks before the projectile expires. */
    float getMaxDistance();

    /** @param distance Maximum travel distance in blocks. */
    void setMaxDistance(float distance);

    /** @return Maximum lifetime in ticks before the projectile expires. */
    int getMaxLifetime();

    /** @param ticks Maximum lifetime in ticks. */
    void setMaxLifetime(int ticks);

    // ==================== COLORS ====================

    /** @return Inner (core) color as a packed RGB integer. */
    int getInnerColor();

    /** @param color Inner color as a packed RGB integer. */
    void setInnerColor(int color);

    /** @return Outer (glow) color as a packed RGB integer. */
    int getOuterColor();

    /** @param color Outer color as a packed RGB integer. */
    void setOuterColor(int color);

    /** @return Whether the outer color layer is rendered. */
    boolean isOuterColorEnabled();

    /** @param enabled Whether to render the outer color layer. */
    void setOuterColorEnabled(boolean enabled);

    /** @return Width of the outer color layer relative to the projectile size. */
    float getOuterColorWidth();

    /** @param width Outer color layer width. */
    void setOuterColorWidth(float width);

    /** @return Alpha (opacity) of the outer color layer (0.0-1.0). */
    float getOuterColorAlpha();

    /** @param alpha Outer color opacity (0.0-1.0). */
    void setOuterColorAlpha(float alpha);

    // ==================== LIGHTNING ====================

    /** @return Whether the lightning visual effect is enabled. */
    boolean hasLightningEffect();

    /** @param enabled Whether to enable the lightning effect. */
    void setLightningEffect(boolean enabled);

    /** @return Density of lightning arcs (higher = more arcs). */
    float getLightningDensity();

    /** @param density Lightning arc density. */
    void setLightningDensity(float density);

    /** @return Radius of the lightning effect around the projectile, in blocks. */
    float getLightningRadius();

    /** @param radius Lightning effect radius in blocks. */
    void setLightningRadius(float radius);

    // ==================== ANCHOR ====================

    /** @return Anchor point ordinal (0=FEET, 1=CENTER, 2=RIGHT_HAND, 3=LEFT_HAND, 4=HEAD, 5=FRONT, 6=ABOVE_HEAD). */
    int getAnchorPoint();

    /** @param point Anchor point ordinal (0=FEET, 1=CENTER, 2=RIGHT_HAND, 3=LEFT_HAND, 4=HEAD, 5=FRONT, 6=ABOVE_HEAD). */
    void setAnchorPoint(int point);

    /** @return Whether the projectile launches from its anchor position instead of the default eye/look-vector position. */
    boolean getLaunchFromAnchor();

    /** @param launchFromAnchor When true, the projectile launches from its configured anchor position. */
    void setLaunchFromAnchor(boolean launchFromAnchor);
}
