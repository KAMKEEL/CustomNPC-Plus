package noppes.npcs.api.entity.data;

/**
 * Represents the rotation configuration for a model.
 * It controls how different parts of a model rotate under various conditions.
 */
public interface IModelRotate {

    /**
     * Checks if rotation is applied while the entity is standing.
     *
     * @return true if rotation while standing is enabled; false otherwise.
     */
    boolean whileStanding();

    /**
     * Sets whether rotation should be applied while the entity is standing.
     *
     * @param whileStanding true to enable; false to disable.
     */
    void whileStanding(boolean whileStanding);

    /**
     * Checks if rotation is applied while the entity is attacking.
     *
     * @return true if rotation while attacking is enabled; false otherwise.
     */
    boolean whileAttacking();

    /**
     * Sets whether rotation should be applied while the entity is attacking.
     *
     * @param whileAttacking true to enable; false to disable.
     */
    void whileAttacking(boolean whileAttacking);

    /**
     * Checks if rotation is applied while the entity is moving.
     *
     * @return true if rotation while moving is enabled; false otherwise.
     */
    boolean whileMoving();

    /**
     * Sets whether rotation should be applied while the entity is moving.
     *
     * @param whileMoving true to enable; false to disable.
     */
    void whileMoving(boolean whileMoving);

    /**
     * Retrieves the rotation settings for a specific part of the model.
     * Parts are indexed as follows: 0 - head, 1 - body, 2 - left arm, 3 - right arm, 4 - left leg, 5 - right leg.
     *
     * @param part the index of the model part.
     * @return the rotation settings for the specified part.
     */
    IModelRotatePart getPart(int part);
}
