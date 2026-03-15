/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity
 */

/**
 * A thin, wide blade projectile that slices through targets.
 * <p>
 * Slicers travel in a straight line, cutting through entities in their path.
  * @javaFqn noppes.npcs.api.entity.IEnergySlicer
*/
export interface IEnergySlicer<T extends Entity /* net.minecraft.entity.Entity */> extends import('./IEnergyProjectile').IEnergyProjectile {
    /**
     * Width of the slicer blade.
     * @return the width of each slice in blocks
     */
    getSliceWidth(): import('./float').float;
    setSliceWidth(width: import('./float').float): import('./void').void;
    getSliceThickness(): import('./float').float;
    setSliceThickness(thickness: import('./float').float): import('./void').void;
}
