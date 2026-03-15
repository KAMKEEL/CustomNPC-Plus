/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.ability.type
 */

/**
 * API interface for Dash abilities.
 * Quick evasive sidestep movement.
  * @javaFqn noppes.npcs.api.ability.type.IAbilityDash
*/
export interface IAbilityDash extends import('../IAbility').IAbility {
    /** @return Dash mode ordinal (0=AGGRESSIVE, 1=DEFENSIVE, 2=DIRECTIONAL). */
    getDashMode(): import('./int').int;
    /** @param mode Dash mode ordinal (0=AGGRESSIVE, 1=DEFENSIVE, 2=DIRECTIONAL). */
    setDashMode(mode: import('./int').int): import('./void').void;
    /** @return Distance covered by the dash in blocks. */
    getDashDistance(): import('./float').float;
    /** @param distance Dash distance in blocks. */
    setDashDistance(distance: import('./float').float): import('./void').void;
    /** @return Speed of the dash movement. */
    getDashSpeed(): import('./float').float;
    /** @param speed Dash movement speed. */
    setDashSpeed(speed: import('./float').float): import('./void').void;
    /** @return Angle offset of the dash direction in degrees. */
    getDashAngle(): import('./float').float;
    /** @param dashAngle Direction angle offset in degrees. */
    setDashAngle(dashAngle: import('./float').float): import('./void').void;
    /** @return Dash direction ordinal (determines lateral movement direction). */
    getDashDirection(): import('./int').int;
    /** @param mode Dash direction ordinal. */
    setDashDirection(mode: import('./int').int): import('./void').void;
    RANDOM: import('./enum DashDirection private static final Random').enum DashDirection private static final Random;
}
