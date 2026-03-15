/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity
 */

/**
 * A continuous laser beam that extends outward from its origin along a direction vector.
 * <p>
 * Unlike other projectiles, lasers don't move through the world — they extend from their
 * origin point along a direction, hitting everything in their path up to {@link #getMaxLength()}.
 * The direction can be set explicitly via {@link #setDirection(double, double, double)}.
 * <p>
 * The coordinate-based {@code fireAt(x, y, z)} calculates and sets the direction vector
 * automatically, then uses the laser's internal launch sequence.
  * @javaFqn noppes.npcs.api.entity.IEnergyLaser
*/
export interface IEnergyLaser<T extends Entity /* net.minecraft.entity.Entity */> extends import('./IEnergyProjectile').IEnergyProjectile {
    /**
     * Width of the laser beam.
     * @return the laser width in blocks
     */
    getLaserWidth(): import('./float').float;
    setLaserWidth(width: import('./float').float): import('./void').void;
    getExpansionSpeed(): import('./float').float;
    setExpansionSpeed(speed: import('./float').float): import('./void').void;
    getMaxLength(): import('./float').float;
    setMaxLength(maxLength: import('./float').float): import('./void').void;
    getCurrentLength(): import('./float').float;
    isFullyExtended(): import('./boolean').boolean;
    getDirX(): import('./double').double;
    getDirY(): import('./double').double;
    getDirZ(): import('./double').double;
    setDirection(x: import('./double').double, y: import('./double').double, z: import('./double').double): import('./void').void;
    getEndX(): import('./double').double;
    getEndY(): import('./double').double;
    getEndZ(): import('./double').double;
}
