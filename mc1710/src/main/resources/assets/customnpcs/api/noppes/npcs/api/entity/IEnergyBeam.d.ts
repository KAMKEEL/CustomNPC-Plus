/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity
 */

/**
 * A directional beam projectile with a trailing path and optional head orb.
 * <p>
 * Beams travel forward from their origin, dealing damage along their length.
 * They can optionally stay attached to their owner and follow the owner's movement.
 * <p>
 * Beams use {@code startFiring} internally rather than {@code startMoving} —
 * this is handled automatically by the fire methods.
  * @javaFqn noppes.npcs.api.entity.IEnergyBeam
*/
export interface IEnergyBeam<T extends Entity /* net.minecraft.entity.Entity */> extends import('./IEnergyProjectile').IEnergyProjectile {
    /**
     * Width of the beam trail.
     * @return the beam width in blocks
     */
    getBeamWidth(): import('./float').float;
    setBeamWidth(width: import('./float').float): import('./void').void;
    getHeadSize(): import('./float').float;
    setHeadSize(size: import('./float').float): import('./void').void;
    isAttachedToOwner(): import('./boolean').boolean;
    setAttachedToOwner(attached: import('./boolean').boolean): import('./void').void;
    shouldRenderTailOrb(): import('./boolean').boolean;
}
