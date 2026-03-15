/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.ability.type
 */

/**
 * API interface for Laser abilities.
 * Sweeping beam that follows the caster's look vector.
  * @javaFqn noppes.npcs.api.ability.type.IAbilityLaser
*/
export interface IAbilityLaser extends import('./IAbilityEnergyProjectile').IAbilityEnergyProjectile {
    /** @return Width of the laser beam in blocks. */
    getLaserWidth(): import('./float').float;
    /** @param width Laser beam width in blocks. */
    setLaserWidth(width: import('./float').float): import('./void').void;
    /** @return Speed at which the laser extends to its max length, in blocks per tick. */
    getExpansionSpeed(): import('./float').float;
    /** @param speed Expansion speed in blocks per tick. */
    setExpansionSpeed(speed: import('./float').float): import('./void').void;
    /** @return Maximum length the laser can reach in blocks. */
    getMaxLength(): import('./float').float;
    /** @param maxLength Maximum laser length in blocks. */
    setMaxLength(maxLength: import('./float').float): import('./void').void;
}
