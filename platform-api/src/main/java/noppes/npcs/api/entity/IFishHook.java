package noppes.npcs.api.entity;


/**
 * Represents a fish hook entity with additional methods.
 *
 */
public interface IFishHook extends IEntity {

    /**
     * Returns the player who cast this fish hook.
     *
     * @return the casting player, or null if none.
     */
    IPlayer getCaster();

    /**
     * Removes this fish hook entity from the world.
     */
    void kill();
}
