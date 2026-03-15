/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity
 */

/**
 * Represents a fish hook entity with additional methods.
 *
 * @param <T> The underlying Minecraft EntityFishHook type.
  * @javaFqn noppes.npcs.api.entity.IFishHook
*/
export interface IFishHook<T extends EntityFishHook /* net.minecraft.entity.projectile.EntityFishHook */> extends import('./IEntity').IEntity {
    /**
     * Returns the player who cast this fish hook.
     *
     * @return the casting player, or null if none.
     */
    getCaster(): import('./IPlayer').IPlayer;
    /**
     * Removes this fish hook entity from the world.
     */
    kill(): import('./void').void;
}
