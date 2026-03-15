package noppes.npcs.api.entity;


/**
 * Represents a arrow entity with additional methods.
 *
 */
public interface IArrow extends IEntity {

    /**
     * Gets the entity that shot this arrow.
     * @return the shooter entity, or null if none
     */
    IEntity getShooter();

    /**
     * Gets the base damage of this arrow.
     * @return the damage value
     */
    double getDamage();

    /**
     * Removes this arrow from the world.
     */
    void kill();
}
