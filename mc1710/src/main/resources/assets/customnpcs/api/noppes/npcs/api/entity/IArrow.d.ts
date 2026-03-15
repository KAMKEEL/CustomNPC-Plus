/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity
 */

/**
 * Represents a arrow entity with additional methods.
 *
 * @param <T> The underlying Minecraft EntityArrow type.
  * @javaFqn noppes.npcs.api.entity.IArrow
*/
export interface IArrow<T extends EntityArrow /* net.minecraft.entity.projectile.EntityArrow */> extends import('./IEntity').IEntity {
    /**
     * Gets the entity that shot this arrow.
     * @return the shooter entity, or null if none
     */
    getShooter(): import('./IEntity').IEntity;
    /**
     * Gets the base damage of this arrow.
     * @return the damage value
     */
    getDamage(): import('./double').double;
    /**
     * Removes this arrow from the world.
     */
    kill(): import('./void').void;
}
