/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.ability.type
 */

/**
 * API interface for Sweeper abilities.
 * Low sweeping beam that rotates around the NPC.
  * @javaFqn noppes.npcs.api.ability.type.IAbilitySweeper
*/
export interface IAbilitySweeper extends import('../IAbility').IAbility {
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
    /** @return Damage dealt per hit to entities caught in the beam. */
    getDamage(): import('./float').float;
    /** @param damage Damage per hit. */
    setDamage(damage: import('./float').float): import('./void').void;
    /** @return Interval in ticks between damage applications to the same target. */
    getDamageInterval(): import('./int').int;
    /** @param interval Damage interval in ticks. */
    setDamageInterval(interval: import('./int').int): import('./void').void;
    /** @return Whether the beam can hit through multiple targets. */
    isPiercing(): import('./boolean').boolean;
    /** @param piercing Whether the beam pierces through targets. */
    setPiercing(piercing: import('./boolean').boolean): import('./void').void;
    /** @return Rotation speed of the sweep in degrees per tick. */
    getSweepSpeed(): import('./float').float;
    /** @param speed Sweep rotation speed in degrees per tick. */
    setSweepSpeed(speed: import('./float').float): import('./void').void;
    /** @return Number of full rotations the beam completes before stopping. */
    getNumberOfRotations(): import('./int').int;
    /** @param rotations Number of full rotations. */
    setNumberOfRotations(rotations: import('./int').int): import('./void').void;
    /** @return Whether the beam pivot tracks the target instead of rotating freely. */
    isLockOnTarget(): import('./boolean').boolean;
    /** @param lock Whether the beam locks on to the target. */
    setLockOnTarget(lock: import('./boolean').boolean): import('./void').void;
    /** @return Inner (core) color of the beam as a packed RGB integer. */
    getInnerColor(): import('./int').int;
    /** @param color Inner color as a packed RGB integer. */
    setInnerColor(color: import('./int').int): import('./void').void;
    /** @return Outer (glow) color of the beam as a packed RGB integer. */
    getOuterColor(): import('./int').int;
    /** @param color Outer color as a packed RGB integer. */
    setOuterColor(color: import('./int').int): import('./void').void;
    /** @return Width of the outer color layer relative to the beam width. */
    getOuterColorWidth(): import('./float').float;
    /** @param width Outer color layer width. */
    setOuterColorWidth(width: import('./float').float): import('./void').void;
    /** @return Whether the outer color layer is rendered. */
    isOuterColorEnabled(): import('./boolean').boolean;
    /** @param enabled Whether to render the outer color layer. */
    setOuterColorEnabled(enabled: import('./boolean').boolean): import('./void').void;
    getDisplayDamage: import('./float').float;
}
