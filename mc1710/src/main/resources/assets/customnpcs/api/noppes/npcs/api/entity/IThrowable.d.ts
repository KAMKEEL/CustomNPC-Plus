/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity
 */

/**
 * Represents a throwable entity with additional methods.
 *
 * @param <T> The underlying Minecraft EntityThrowable type.
  * @javaFqn noppes.npcs.api.entity.IThrowable
*/
export interface IThrowable<T extends EntityThrowable /* net.minecraft.entity.projectile.EntityThrowable */> extends import('./IEntity').IEntity {
    /**
     * Returns the entity that threw this throwable.
     *
     * @return the thrower, or null if unknown.
     */
    getThrower(): import('./IEntityLivingBase').IEntityLivingBase;
    /**
     * Removes this throwable entity from the world.
     */
    kill(): import('./void').void;
}
