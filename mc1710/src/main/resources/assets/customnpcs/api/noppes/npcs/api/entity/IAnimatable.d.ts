/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity
 */

/**
 * Represents an entity capable of playing animations.
 * Provides access to its animation data.
  * @javaFqn noppes.npcs.api.entity.IAnimatable
*/
export interface IAnimatable {
    /**
     * Returns the animation data associated with this entity.
     *
     * @return the animation data.
     */
    getAnimationData(): import('../handler/data/IAnimationData').IAnimationData;
}
