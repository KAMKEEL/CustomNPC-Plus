/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity
 */

/**
 * Entity-level interface for sweeper beam entities.
 * Created via IEnergyHandler.createSweeper().
 * Configure properties, then call spawn() to place in the world.
  * @javaFqn noppes.npcs.api.entity.IEnergySweeper
*/
export interface IEnergySweeper<T extends Entity /* net.minecraft.entity.Entity */> extends import('./IEnergyAbility').IEnergyAbility {
    /** @return Length of the sweeping beam in blocks. */
    getBeamLength(): import('./float').float;
    /** @param length Beam length in blocks. */
    setBeamLength(length: import('./float').float): import('./void').void;
    /** @return Width of the sweeping beam in blocks. */
    getBeamWidth(): import('./float').float;
    /** @param width Beam width in blocks. */
    setBeamWidth(width: import('./float').float): import('./void').void;
    /** @return Height of the sweeping beam in blocks. */
    getBeamHeight(): import('./float').float;
    /** @param height Beam height in blocks. */
    setBeamHeight(height: import('./float').float): import('./void').void;
    /** @return Rotation speed of the sweep in degrees per tick. */
    getSweepSpeed(): import('./float').float;
    /** @param degreesPerTick Sweep rotation speed in degrees per tick. */
    setSweepSpeed(degreesPerTick: import('./float').float): import('./void').void;
    /** @return Number of full rotations the beam completes before stopping. */
    getNumberOfRotations(): import('./int').int;
    /** @param rotations Number of full rotations. */
    setNumberOfRotations(rotations: import('./int').int): import('./void').void;
    /** @return Whether the beam pivot tracks the target instead of rotating freely. */
    isLockOnTarget(): import('./boolean').boolean;
    /** @param lock Whether the beam locks on to the target. */
    setLockOnTarget(lock: import('./boolean').boolean): import('./void').void;
    /** @return Damage dealt per hit to entities caught in the beam. */
    getDamage(): import('./float').float;
    /** @param damage Damage per hit. */
    setDamage(damage: import('./float').float): import('./void').void;
    /** @return Interval in ticks between damage applications to the same target. */
    getDamageInterval(): import('./int').int;
    /** @param ticks Damage interval in ticks. */
    setDamageInterval(ticks: import('./int').int): import('./void').void;
    /** @return Whether the beam can hit through multiple targets. */
    isPiercing(): import('./boolean').boolean;
    /** @param piercing Whether the beam pierces through targets. */
    setPiercing(piercing: import('./boolean').boolean): import('./void').void;
    /** Spawn this sweeper entity into the world. */
    spawn(): import('./void').void;
}
