/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler
 */

/**
 * Handles creation, deletion, and retrieval of tags.
 * Tags are labels that can be applied to entities for categorization.
  * @javaFqn noppes.npcs.api.handler.ITagHandler
*/
export interface ITagHandler {
    /**
     * Returns all registered tags.
     *
     * @return a list of tags.
     */
    list(): import('./data/ITag').ITag[];
    /**
     * Deletes the tag with the given ID.
     *
     * @param id the tag ID.
     * @return the deleted tag, or null if not found.
     */
    delete(id: import('./int').int): import('./data/ITag').ITag;
    /**
     * Creates a new tag with the given name and color.
     *
     * @param name  the tag name.
     * @param color the tag color as a packed RGB integer.
     * @return the created tag.
     */
    create(name: String, color: import('./int').int): import('./data/ITag').ITag;
    /**
     * Returns the tag with the given ID.
     *
     * @param id the tag ID.
     * @return the tag, or null if not found.
     */
    get(id: import('./int').int): import('./data/ITag').ITag;
}
