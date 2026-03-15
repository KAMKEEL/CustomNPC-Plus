/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity
 */

/**
 * Represents a living entity (mob, NPC, etc.) that extends the base living functionality.
 * Provides additional methods for navigation, sound, custom name, leashing, and more.
 *
 * @param <T> The underlying Minecraft EntityLiving type.
  * @javaFqn noppes.npcs.api.entity.IEntityLiving
*/
export interface IEntityLiving<T extends EntityLiving /* net.minecraft.entity.EntityLiving */> extends import('./IEntityLivingBase').IEntityLivingBase {
    /**
     * Checks if the entity is currently navigating (pathfinding).
     *
     * @return true if navigating; false otherwise.
     */
    isNavigating(): import('./boolean').boolean;
    /**
     * Clears the current navigation path.
     */
    clearNavigation(): import('./void').void;
    /**
     * Commands the entity to navigate toward the specified destination.
     *
     * @param x     Destination x coordinate.
     * @param y     Destination y coordinate.
     * @param z     Destination z coordinate.
     * @param speed Movement speed (0.7 is default).
     */
    navigateTo(x: import('./double').double, y: import('./double').double, z: import('./double').double, speed: import('./double').double): import('./void').void;
    /**
     * Returns the underlying Minecraft entity.
     *
     * @return the Minecraft entity.
     */
    getMCEntity(): T;
    /**
     * Plays the entity's living sound.
     */
    playLivingSound(): import('./void').void;
    /**
     * Spawns explosion particles for this entity.
     */
    spawnExplosionParticle(): import('./void').void;
    /**
     * Sets the forward movement speed for the entity.
     *
     * @param speed the forward speed.
     */
    setMoveForward(speed: import('./float').float): import('./void').void;
    /**
     * Rotates the entity to face the given entity.
     *
     * @param entity the target entity.
     * @param pitch  the pitch angle.
     * @param yaw    the yaw angle.
     */
    faceEntity(entity: import('./IEntity').IEntity, pitch: import('./float').float, yaw: import('./float').float): import('./void').void;
    /**
     * @return Whether the entity can pick up loot.
     */
    canPickUpLoot(): import('./boolean').boolean;
    /**
     * Sets whether the entity can pick up loot.
     *
     * @param pickUp true to allow picking up loot.
     */
    setCanPickUpLoot(pickUp: import('./boolean').boolean): import('./void').void;
    /**
     * @return Whether the entity is persistent (won't despawn).
     */
    isPersistent(): import('./boolean').boolean;
    /**
     * Enables persistence so that the entity does not despawn.
     */
    enablePersistence(): import('./void').void;
    /**
     * Sets a custom name tag for the entity.
     *
     * @param text the custom name.
     */
    setCustomNameTag(text: String): import('./void').void;
    /**
     * Returns the entity's custom name tag.
     *
     * @return the custom name.
     */
    getCustomNameTag(): String;
    /**
     * @return Whether the entity has a custom name tag.
     */
    hasCustomNameTag(): import('./boolean').boolean;
    /**
     * Sets whether the entity's name tag is always rendered.
     *
     * @param alwaysRender true to always render the name tag.
     */
    setAlwaysRenderNameTag(alwaysRender: import('./boolean').boolean): import('./void').void;
    /**
     * @return Whether the name tag is always rendered.
     */
    getAlwaysRenderNameTag(): import('./boolean').boolean;
    /**
     * Clears the entity's leash.
     *
     * @param sendPacket whether to send a packet update.
     * @param dropLeash  whether to drop the leash item.
     */
    clearLeashed(sendPacket: import('./boolean').boolean, dropLeash: import('./boolean').boolean): import('./void').void;
    /**
     * @return Whether the entity allows leashing.
     */
    allowLeashing(): import('./boolean').boolean;
    /**
     * @return Whether the entity is currently leashed.
     */
    getLeashed(): import('./boolean').boolean;
    /**
     * @return The entity to which this entity is leashed.
     */
    getLeashedTo(): import('./IEntity').IEntity;
    /**
     * Leashes this entity to the specified entity.
     *
     * @param entity     the entity to leash to.
     * @param sendPacket whether to send a packet update.
     */
    setLeashedTo(entity: import('./IEntity').IEntity, sendPacket: import('./boolean').boolean): import('./void').void;
    /**
     * @return Whether the entity can be steered.
     */
    canBeSteered(): import('./boolean').boolean;
}
