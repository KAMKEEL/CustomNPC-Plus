/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity
 */

/**
 * Represents an energy dome barrier - a spherical shield centered on the caster.
  * @javaFqn noppes.npcs.api.entity.IEnergyDome
*/
export interface IEnergyDome<T extends Entity /* net.minecraft.entity.Entity */> extends import('./IEnergyBarrier').IEnergyBarrier {
    /** @return The radius of the dome in blocks. */
    getDomeRadius(): import('./float').float;
    /** @param radius The radius of the dome in blocks. */
    setDomeRadius(radius: import('./float').float): import('./void').void;
}
