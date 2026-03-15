package noppes.npcs.api.entity;


/**
 * Represents a throwable entity with additional methods.
 *
 */
public interface IThrowable extends IEntity {

    /**
     * Returns the entity that threw this throwable.
     *
     * @return the thrower, or null if unknown.
     */
    IEntityLivingBase getThrower();

    /**
     * Removes this throwable entity from the world.
     */
    void kill();
}
