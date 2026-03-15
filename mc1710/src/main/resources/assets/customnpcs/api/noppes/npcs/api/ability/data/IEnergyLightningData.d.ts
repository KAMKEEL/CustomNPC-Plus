/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.ability.data
 */

/**
 * Lightning visual effect properties for energy ability projectiles.
 * Controls the electric arc effect rendered around the projectile.
  * @javaFqn noppes.npcs.api.ability.data.IEnergyLightningData
*/
export interface IEnergyLightningData {
    /** @return Whether the lightning visual effect is enabled. */
    isLightningEffect(): import('./boolean').boolean;
    /** @param lightningEffect Whether to enable the lightning effect. */
    setLightningEffect(lightningEffect: import('./boolean').boolean): import('./void').void;
    /** @return Density of lightning arcs (higher = more arcs). */
    getLightningDensity(): import('./float').float;
    /** @param lightningDensity Lightning arc density. */
    setLightningDensity(lightningDensity: import('./float').float): import('./void').void;
    /** @return Radius of the lightning effect around the projectile, in blocks. */
    getLightningRadius(): import('./float').float;
    /** @param lightningRadius Lightning effect radius in blocks. */
    setLightningRadius(lightningRadius: import('./float').float): import('./void').void;
    /** @return Fade-out time for lightning arcs in ticks. */
    getLightningFadeTime(): import('./int').int;
    /** @param lightningFadeTime Fade-out time in ticks. */
    setLightningFadeTime(lightningFadeTime: import('./int').int): import('./void').void;
}
