/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.ability.data
 */

/**
 * Homing and speed properties for energy ability projectiles.
 * Controls projectile velocity and target-seeking behavior.
  * @javaFqn noppes.npcs.api.ability.data.IEnergyHomingData
*/
export interface IEnergyHomingData {
    /** @return Projectile travel speed in blocks per tick. */
    getSpeed(): import('./float').float;
    /** @param speed Travel speed in blocks per tick. */
    setSpeed(speed: import('./float').float): import('./void').void;
    /** @return Whether the projectile tracks its target. */
    isHoming(): import('./boolean').boolean;
    /** @param homing Whether to track the target. */
    setHoming(homing: import('./boolean').boolean): import('./void').void;
    /** @return Homing turn strength (higher = tighter turns). */
    getHomingStrength(): import('./float').float;
    /** @param homingStrength Homing turn strength. */
    setHomingStrength(homingStrength: import('./float').float): import('./void').void;
    /** @return Maximum distance at which homing activates, in blocks. */
    getHomingRange(): import('./float').float;
    /** @param homingRange Maximum homing activation range in blocks. */
    setHomingRange(homingRange: import('./float').float): import('./void').void;
}
