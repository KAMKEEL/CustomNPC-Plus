package noppes.npcs.api.ability.type;

import noppes.npcs.api.ability.IAbility;

/**
 * API interface for zone-based abilities (Trap, Hazard).
 * Shared zone properties: duration, shape, spawn, visual layers, colors.
 */
public interface IAbilityZone extends IAbility {

    /** @return Duration of the zone in ticks. */
    int getDurationTicks();

    /** @param ticks Zone duration in ticks. */
    void setDurationTicks(int ticks);

    /** @return Zone shape ordinal (0=CIRCLE, 1=SQUARE). */
    int getZoneShapeOrdinal();

    /** @param shape Zone shape ordinal (0=CIRCLE, 1=SQUARE). */
    void setZoneShapeOrdinal(int shape);

    /** @return Spawn offset radius from the caster in blocks. */
    float getSpawnRadius();

    /** @param radius Spawn offset radius in blocks. */
    void setSpawnRadius(float radius);

    /** @return Number of zones spawned per use. */
    int getZoneCount();

    /** @param count Number of zones to spawn. */
    void setZoneCount(int count);

    /** @return Visual height of the zone in blocks. */
    float getZoneHeight();

    /** @param height Zone height in blocks. */
    void setZoneHeight(float height);

    /** @return Density of particles in the zone effect. */
    float getParticleDensity();

    /** @param density Particle density. */
    void setParticleDensity(float density);

    /** @return Scale of individual particles. */
    float getParticleScale();

    /** @param scale Particle scale. */
    void setParticleScale(float scale);

    /** @return Animation speed multiplier for zone visual effects. */
    float getAnimSpeed();

    /** @param speed Animation speed multiplier. */
    void setAnimSpeed(float speed);

    /** @return Density of lightning arcs in the zone. */
    float getLightningDensity();

    /** @param density Lightning arc density. */
    void setLightningDensity(float density);

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

    // ==================== VISUAL LAYERS ====================

    /** @return Whether the ground fill layer is rendered. */
    boolean isGroundFill();

    /** @param enabled Whether to render the ground fill. */
    void setGroundFill(boolean enabled);

    /** @return Alpha (opacity) of the ground fill layer (0.0-1.0). */
    float getGroundAlpha();

    /** @param alpha Ground fill opacity (0.0-1.0). */
    void setGroundAlpha(float alpha);

    /** @return Whether ring decorations are rendered. */
    boolean isRings();

    /** @param enabled Whether to render rings. */
    void setRings(boolean enabled);

    /** @return Number of concentric rings in the zone. */
    int getRingCount();

    /** @param count Number of rings. */
    void setRingCount(int count);

    /** @return Whether the border outline is rendered. */
    boolean isBorder();

    /** @param enabled Whether to render the border. */
    void setBorder(boolean enabled);

    /** @return Rotation speed of the border in degrees per tick. */
    float getBorderSpeed();

    /** @param speed Border rotation speed. */
    void setBorderSpeed(float speed);

    /** @return Whether accent decorations are rendered. */
    boolean isAccents();

    /** @param enabled Whether to render accents. */
    void setAccents(boolean enabled);

    /** @return Accent style ordinal (0=STATIC, 1=SWAYING, 2=FLICKERING). */
    int getAccentStyle();

    /** @param style Accent style ordinal (0=STATIC, 1=SWAYING, 2=FLICKERING). */
    void setAccentStyle(int style);

    /** @return Whether lightning arcs are rendered in the zone. */
    boolean isLightning();

    /** @param enabled Whether to render lightning. */
    void setLightning(boolean enabled);

    /** @return Whether particles are rendered in the zone. */
    boolean isParticles();

    /** @param enabled Whether to render particles. */
    void setParticles(boolean enabled);

    /** @return Particle motion style ordinal (0=RISING, 1=DRIFTING, 2=SPARKS). */
    int getParticleMotion();

    /** @param motion Particle motion style ordinal (0=RISING, 1=DRIFTING, 2=SPARKS). */
    void setParticleMotion(int motion);

    /** @return Resource directory path for custom particle textures. */
    String getParticleDir();

    /** @param directory Resource directory path for particle textures. */
    void setParticleDir(String directory);

    /** @return Size of individual particles in pixels. */
    int getParticleSize();

    /** @param size Particle size in pixels. */
    void setParticleSize(int size);

    /** @return Whether particles use the glow (fullbright) render mode. */
    boolean isParticleGlow();

    /** @param glow Whether particles glow. */
    void setParticleGlow(boolean glow);
}
