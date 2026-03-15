/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity.data
 */

/**
 * Represents the rotation configuration for a model.
 * It controls how different parts of a model rotate under various conditions.
  * @javaFqn noppes.npcs.api.entity.data.IModelRotate
*/
export interface IModelRotate {
    /**
     * Checks if rotation is applied while the entity is standing.
     *
     * @return true if rotation while standing is enabled; false otherwise.
     */
    whileStanding(): import('./boolean').boolean;
    /**
     * Sets whether rotation should be applied while the entity is standing.
     *
     * @param whileStanding true to enable; false to disable.
     */
    whileStanding(whileStanding: import('./boolean').boolean): import('./void').void;
    /**
     * Checks if rotation is applied while the entity is attacking.
     *
     * @return true if rotation while attacking is enabled; false otherwise.
     */
    whileAttacking(): import('./boolean').boolean;
    /**
     * Sets whether rotation should be applied while the entity is attacking.
     *
     * @param whileAttacking true to enable; false to disable.
     */
    whileAttacking(whileAttacking: import('./boolean').boolean): import('./void').void;
    /**
     * Checks if rotation is applied while the entity is moving.
     *
     * @return true if rotation while moving is enabled; false otherwise.
     */
    whileMoving(): import('./boolean').boolean;
    /**
     * Sets whether rotation should be applied while the entity is moving.
     *
     * @param whileMoving true to enable; false to disable.
     */
    whileMoving(whileMoving: import('./boolean').boolean): import('./void').void;
    /**
     * Retrieves the rotation settings for a specific part of the model.
     * Parts are indexed as follows: 0 - head, 1 - body, 2 - left arm, 3 - right arm, 4 - left leg, 5 - right leg.
     *
     * @param part the index of the model part.
     * @return the rotation settings for the specified part.
     */
    getPart(part: import('./int').int): import('./IModelRotatePart').IModelRotatePart;
    whileStanding: import('./boolean').boolean;
    whileAttacking: import('./boolean').boolean;
    whileMoving: import('./boolean').boolean;
    head: import('./ModelRotatePart').ModelRotatePart;
    body: import('./ModelRotatePart').ModelRotatePart;
    larm: import('./ModelRotatePart').ModelRotatePart;
    rarm: import('./ModelRotatePart').ModelRotatePart;
    lleg: import('./ModelRotatePart').ModelRotatePart;
    rleg: import('./ModelRotatePart').ModelRotatePart;
}
