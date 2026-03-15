/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.ability.type
 */

/**
 * API interface for Disc abilities.
 * Flat spinning disc projectile with optional boomerang.
  * @javaFqn noppes.npcs.api.ability.type.IAbilityDisc
*/
export interface IAbilityDisc extends import('./IAbilityEnergyProjectile').IAbilityEnergyProjectile {
    /** @return Travel speed of the disc in blocks per tick. */
    getSpeed(): import('./float').float;
    /** @param speed Travel speed in blocks per tick. */
    setSpeed(speed: import('./float').float): import('./void').void;
    /** @return Visual radius of the disc in blocks. */
    getDiscRadius(): import('./float').float;
    /** @param radius Disc radius in blocks. */
    setDiscRadius(radius: import('./float').float): import('./void').void;
    /** @return Thickness of the disc in blocks. */
    getDiscThickness(): import('./float').float;
    /** @param thickness Disc thickness in blocks. */
    setDiscThickness(thickness: import('./float').float): import('./void').void;
    /** @return Whether the disc spins on a vertical axis (blade-like). */
    isVertical(): import('./boolean').boolean;
    /** @param vertical Whether the disc spins vertically. */
    setVertical(vertical: import('./boolean').boolean): import('./void').void;
    /** @return Whether the disc returns to the caster after reaching max distance. */
    isBoomerang(): import('./boolean').boolean;
    /** @param boomerang Whether the disc returns to the caster. */
    setBoomerang(boomerang: import('./boolean').boolean): import('./void').void;
    /** @return Delay in ticks before the disc begins returning. */
    getBoomerangDelay(): import('./int').int;
    /** @param ticks Boomerang return delay in ticks. */
    setBoomerangDelay(ticks: import('./int').int): import('./void').void;
    /** @return Whether the disc tracks its target. */
    isHoming(): import('./boolean').boolean;
    /** @param homing Whether to track the target. */
    setHoming(homing: import('./boolean').boolean): import('./void').void;
    /** @return Homing turn strength (higher = tighter turns). */
    getHomingStrength(): import('./float').float;
    /** @param strength Homing turn strength. */
    setHomingStrength(strength: import('./float').float): import('./void').void;
    /** @return Maximum distance at which homing activates, in blocks. */
    getHomingRange(): import('./float').float;
    /** @param range Maximum homing activation range in blocks. */
    setHomingRange(range: import('./float').float): import('./void').void;
    /** @return Visual rotation speed of the disc in degrees per tick. */
    getRotationSpeed(): import('./float').float;
    /** @param speed Rotation speed in degrees per tick. */
    setRotationSpeed(speed: import('./float').float): import('./void').void;
    /** @return Number of disc projectiles fired per use. */
    getProjectileCount(): import('./int').int;
    /** @param count Number of projectiles per use. */
    setProjectileCount(count: import('./int').int): import('./void').void;
    /** @return Delay in ticks between each projectile in a multi-projectile volley. */
    getFireDelay(): import('./int').int;
    /** @param delay Delay in ticks between projectiles. */
    setFireDelay(delay: import('./int').int): import('./void').void;
    /**
     * @param projectileIndex Projectile index.
     * @return Inner color as a packed RGB integer.
     */
    getInnerColor(projectileIndex: import('./int').int): import('./int').int;
    /**
     * @param projectileIndex Projectile index.
     * @param color Inner color as a packed RGB integer.
     */
    setInnerColor(projectileIndex: import('./int').int, color: import('./int').int): import('./void').void;
    /**
     * @param projectileIndex Projectile index.
     * @return Outer color as a packed RGB integer.
     */
    getOuterColor(projectileIndex: import('./int').int): import('./int').int;
    /**
     * @param projectileIndex Projectile index.
     * @param color Outer color as a packed RGB integer.
     */
    setOuterColor(projectileIndex: import('./int').int, color: import('./int').int): import('./void').void;
    /**
     * @param projectileIndex Projectile index.
     * @return Anchor point ordinal.
     */
    getAnchorPoint(projectileIndex: import('./int').int): import('./int').int;
    /**
     * @param projectileIndex Projectile index.
     * @param point Anchor point ordinal.
     */
    setAnchorPoint(projectileIndex: import('./int').int, point: import('./int').int): import('./void').void;
}
