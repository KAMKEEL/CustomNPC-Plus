/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity
 */

/**
 * A spherical homing energy ball.
 * <p>
 * Orbs travel in a straight line toward their target and can optionally home in on living entities.
 * Use the base {@link IEnergyProjectile} methods for size, speed, damage, homing, and launch control.
 * <p>
 * Example usage:
 * <pre>
 * // Fire from a specific position toward a target
 * var orb = API.getEnergyController().createOrb(world, player, x, y, z, 1.0);
 * orb.setDamage(10);
 * orb.setHoming(true);
 * orb.fireAt(target);           // launches from (x,y,z) toward target
 *
 * // Fire from the caster's eye position using look-vector snap
 * var orb2 = API.getEnergyController().createOrb(world, player, 0, 0, 0, 1.0);
 * orb2.fireFrom(player, target); // repositions to player's eye, then fires toward target
 * </pre>
  * @javaFqn noppes.npcs.api.entity.IEnergyOrb
*/
export interface IEnergyOrb<T extends Entity /* net.minecraft.entity.Entity */> extends import('./IEnergyProjectile').IEnergyProjectile {
}
