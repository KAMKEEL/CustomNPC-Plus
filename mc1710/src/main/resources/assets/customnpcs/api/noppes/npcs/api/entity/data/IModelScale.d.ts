/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity.data
 */

/**
 * Represents the scaling configuration for a model.
 * Provides access to scaling data for individual parts of a model.
  * @javaFqn noppes.npcs.api.entity.data.IModelScale
*/
export interface IModelScale {
    /**
     * Retrieves the scaling settings for a specified model part.
     * Parts are indexed as follows: 0 - head, 1 - body, 2 - arms, 3 - legs.
     *
     * @param part the index of the model part.
     * @return the scale configuration for that part.
     */
    getPart(part: import('./int').int): import('./IModelScalePart').IModelScalePart;
    head: import('./ModelScalePart').ModelScalePart;
    body: import('./ModelScalePart').ModelScalePart;
    arms: import('./ModelScalePart').ModelScalePart;
    legs: import('./ModelScalePart').ModelScalePart;
}
