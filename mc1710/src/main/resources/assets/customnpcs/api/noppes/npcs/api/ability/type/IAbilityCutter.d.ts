/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.ability.type
 */

/**
 * API interface for Cutter abilities.
 * Sweeping fan attack in an arc.
  * @javaFqn noppes.npcs.api.ability.type.IAbilityCutter
*/
export interface IAbilityCutter extends import('../IAbility').IAbility {
    /** @return Arc angle of the sweep in degrees. */
    getArcAngle(): import('./float').float;
    /** @param angle Arc angle in degrees. */
    setArcAngle(angle: import('./float').float): import('./void').void;
    /** @return Range of the sweep attack in blocks. */
    getRange(): import('./float').float;
    /** @param range Sweep range in blocks. */
    setRange(range: import('./float').float): import('./void').void;
    /** @return Damage dealt to entities hit by the sweep. */
    getDamage(): import('./float').float;
    /** @param damage Sweep damage amount. */
    setDamage(damage: import('./float').float): import('./void').void;
    /** @return Knockback strength applied to entities hit. */
    getKnockback(): import('./float').float;
    /** @param knockback Knockback strength. */
    setKnockback(knockback: import('./float').float): import('./void').void;
    /** @return Sweep mode ordinal (0=SWIPE, 1=SPIN). */
    getSweepMode(): import('./int').int;
    /** @param mode Sweep mode ordinal (0=SWIPE, 1=SPIN). */
    setSweepMode(mode: import('./int').int): import('./void').void;
    /** @return Speed of the sweep animation. */
    getSweepSpeed(): import('./float').float;
    /** @param speed Sweep animation speed. */
    setSweepSpeed(speed: import('./float').float): import('./void').void;
    /** @return Duration of the spin in ticks (only used in SPIN mode). */
    getSpinDurationTicks(): import('./int').int;
    /** @param ticks Spin duration in ticks. */
    setSpinDurationTicks(ticks: import('./int').int): import('./void').void;
    /** @return Whether the sweep hits through multiple targets. */
    isPiercing(): import('./boolean').boolean;
    /** @param piercing Whether the sweep pierces through targets. */
    setPiercing(piercing: import('./boolean').boolean): import('./void').void;
    /** @return Inner radius of the sweep arc in blocks (creates a donut-shaped hitbox). */
    getInnerRadius(): import('./float').float;
    /** @param radius Inner radius in blocks. */
    setInnerRadius(radius: import('./float').float): import('./void').void;
    arcAngle: import('./enum SweepMode private float').enum SweepMode private float;
    getDisplayDamage: import('./float').float;
}
