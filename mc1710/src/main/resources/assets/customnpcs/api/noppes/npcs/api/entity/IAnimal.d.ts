/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity
 */

/**
 * Represents an animal entity with additional behavior such as breeding,
 * following, and mating.
 *
 * @param <T> the underlying Minecraft EntityAnimal type.
  * @javaFqn noppes.npcs.api.entity.IAnimal
*/
export interface IAnimal<T extends EntityAnimal /* net.minecraft.entity.passive.EntityAnimal */> extends import('./IEntityLiving').IEntityLiving {
    /**
     * Checks whether the specified item can be used to breed this animal.
     * (e.g. wheat, carrots or seeds depending on the animal type)
     *
     * @param itemStack the item to test.
     * @return true if the item is a breeding item; false otherwise.
     */
    isBreedingItem(itemStack: import('../item/IItemStack').IItemStack): import('./boolean').boolean;
    /**
     * Called when a player interacts with the animal.
     * For example, this might return true if the player milks a cow or saddles a pig.
     *
     * @param player the interacting player.
     * @return true if the interaction was handled; false otherwise.
     */
    interact(player: import('./IPlayer').IPlayer): import('./boolean').boolean;
    /**
     * Sets the specified player as the one to follow.
     *
     * @param player the player to follow.
     */
    setFollowPlayer(player: import('./IPlayer').IPlayer): import('./void').void;
    /**
     * Returns the player that this animal is currently following.
     *
     * @return the following player, or null if not following anyone.
     */
    followingPlayer(): import('./IPlayer').IPlayer;
    /**
     * Checks if the animal is currently in a "love" state,
     * indicating readiness to mate.
     *
     * @return true if the animal is in love; false otherwise.
     */
    isInLove(): import('./boolean').boolean;
    /**
     * Resets the animal's love state.
     */
    resetInLove(): import('./void').void;
    /**
     * Checks if this animal can mate with another animal.
     *
     * @param animal the other animal.
     * @return true if mating is possible; false otherwise.
     */
    canMateWith(animal: import('./IAnimal').IAnimal): import('./boolean').boolean;
}
