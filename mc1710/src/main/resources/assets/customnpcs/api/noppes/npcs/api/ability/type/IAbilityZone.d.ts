/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.ability.type
 */

/**
 * API interface for zone-based abilities (Trap, Hazard).
 * Shared zone properties: duration, shape, spawn, visual layers, colors.
  * @javaFqn noppes.npcs.api.ability.type.IAbilityZone
*/
export interface IAbilityZone extends import('../IAbility').IAbility {
    /** @return Duration of the zone in ticks. */
    getDurationTicks(): import('./int').int;
    /** @param ticks Zone duration in ticks. */
    setDurationTicks(ticks: import('./int').int): import('./void').void;
    /** @return Zone shape ordinal (0=CIRCLE, 1=SQUARE). */
    getZoneShapeOrdinal(): import('./int').int;
    /** @param shape Zone shape ordinal (0=CIRCLE, 1=SQUARE). */
    setZoneShapeOrdinal(shape: import('./int').int): import('./void').void;
    /** @return Spawn offset radius from the caster in blocks. */
    getSpawnRadius(): import('./float').float;
    /** @param radius Spawn offset radius in blocks. */
    setSpawnRadius(radius: import('./float').float): import('./void').void;
    /** @return Number of zones spawned per use. */
    getZoneCount(): import('./int').int;
    /** @param count Number of zones to spawn. */
    setZoneCount(count: import('./int').int): import('./void').void;
    /** @return Visual height of the zone in blocks. */
    getZoneHeight(): import('./float').float;
    /** @param height Zone height in blocks. */
    setZoneHeight(height: import('./float').float): import('./void').void;
    /** @return Density of particles in the zone effect. */
    getParticleDensity(): import('./float').float;
    /** @param density Particle density. */
    setParticleDensity(density: import('./float').float): import('./void').void;
    /** @return Scale of individual particles. */
    getParticleScale(): import('./float').float;
    /** @param scale Particle scale. */
    setParticleScale(scale: import('./float').float): import('./void').void;
    /** @return Animation speed multiplier for zone visual effects. */
    getAnimSpeed(): import('./float').float;
    /** @param speed Animation speed multiplier. */
    setAnimSpeed(speed: import('./float').float): import('./void').void;
    /** @return Density of lightning arcs in the zone. */
    getLightningDensity(): import('./float').float;
    /** @param density Lightning arc density. */
    setLightningDensity(density: import('./float').float): import('./void').void;
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
    /** @return Whether the ground fill layer is rendered. */
    isGroundFill(): import('./boolean').boolean;
    /** @param enabled Whether to render the ground fill. */
    setGroundFill(enabled: import('./boolean').boolean): import('./void').void;
    /** @return Alpha (opacity) of the ground fill layer (0.0-1.0). */
    getGroundAlpha(): import('./float').float;
    /** @param alpha Ground fill opacity (0.0-1.0). */
    setGroundAlpha(alpha: import('./float').float): import('./void').void;
    /** @return Whether ring decorations are rendered. */
    isRings(): import('./boolean').boolean;
    /** @param enabled Whether to render rings. */
    setRings(enabled: import('./boolean').boolean): import('./void').void;
    /** @return Number of concentric rings in the zone. */
    getRingCount(): import('./int').int;
    /** @param count Number of rings. */
    setRingCount(count: import('./int').int): import('./void').void;
    /** @return Whether the border outline is rendered. */
    isBorder(): import('./boolean').boolean;
    /** @param enabled Whether to render the border. */
    setBorder(enabled: import('./boolean').boolean): import('./void').void;
    /** @return Rotation speed of the border in degrees per tick. */
    getBorderSpeed(): import('./float').float;
    /** @param speed Border rotation speed. */
    setBorderSpeed(speed: import('./float').float): import('./void').void;
    /** @return Whether accent decorations are rendered. */
    isAccents(): import('./boolean').boolean;
    /** @param enabled Whether to render accents. */
    setAccents(enabled: import('./boolean').boolean): import('./void').void;
    /** @return Accent style ordinal (0=STATIC, 1=SWAYING, 2=FLICKERING). */
    getAccentStyle(): import('./int').int;
    /** @param style Accent style ordinal (0=STATIC, 1=SWAYING, 2=FLICKERING). */
    setAccentStyle(style: import('./int').int): import('./void').void;
    /** @return Whether lightning arcs are rendered in the zone. */
    isLightning(): import('./boolean').boolean;
    /** @param enabled Whether to render lightning. */
    setLightning(enabled: import('./boolean').boolean): import('./void').void;
    /** @return Whether particles are rendered in the zone. */
    isParticles(): import('./boolean').boolean;
    /** @param enabled Whether to render particles. */
    setParticles(enabled: import('./boolean').boolean): import('./void').void;
    /** @return Particle motion style ordinal (0=RISING, 1=DRIFTING, 2=SPARKS). */
    getParticleMotion(): import('./int').int;
    /** @param motion Particle motion style ordinal (0=RISING, 1=DRIFTING, 2=SPARKS). */
    setParticleMotion(motion: import('./int').int): import('./void').void;
    /** @return Resource directory path for custom particle textures. */
    getParticleDir(): String;
    /** @param directory Resource directory path for particle textures. */
    setParticleDir(directory: String): import('./void').void;
    /** @return Size of individual particles in pixels. */
    getParticleSize(): import('./int').int;
    /** @param size Particle size in pixels. */
    setParticleSize(size: import('./int').int): import('./void').void;
    /** @return Whether particles use the glow (fullbright) render mode. */
    isParticleGlow(): import('./boolean').boolean;
    /** @param glow Whether particles glow. */
    setParticleGlow(glow: import('./boolean').boolean): import('./void').void;
}
