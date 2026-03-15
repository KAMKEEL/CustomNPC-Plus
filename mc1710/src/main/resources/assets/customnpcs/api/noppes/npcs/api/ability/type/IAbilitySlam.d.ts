/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.ability.type
 */

/**
 * API interface for Slam abilities.
 * Ground-pound AOE attack with leap.
  * @javaFqn noppes.npcs.api.ability.type.IAbilitySlam
*/
export interface IAbilitySlam extends import('../IAbility').IAbility {
    /** @return Damage dealt on ground impact. */
    getDamage(): import('./float').float;
    /** @param damage Impact damage amount. */
    setDamage(damage: import('./float').float): import('./void').void;
    /** @return Radius of the impact area in blocks. */
    getRadius(): import('./float').float;
    /** @param radius Impact radius in blocks. */
    setRadius(radius: import('./float').float): import('./void').void;
    /** @return Knockback strength applied to entities in the impact area. */
    getKnockbackStrength(): import('./float').float;
    /** @param knockback Knockback strength. */
    setKnockbackStrength(knockback: import('./float').float): import('./void').void;
    /** @return Horizontal speed of the leap toward the target. */
    getLeapSpeed(): import('./float').float;
    /** @param speed Leap speed. */
    setLeapSpeed(speed: import('./float').float): import('./void').void;
    /** @return Vertical height of the leap in blocks. */
    getLeapHeight(): import('./float').float;
    /** @param height Leap height in blocks. */
    setLeapHeight(height: import('./float').float): import('./void').void;
    getDisplayDamage: import('./float').float;
}
