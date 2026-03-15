package noppes.npcs.api.handler;

import noppes.npcs.api.handler.data.ITag;

import java.util.List;

/**
 * Handles creation, deletion, and retrieval of tags.
 * Tags are labels that can be applied to entities for categorization.
 */
public interface ITagHandler {

    /**
     * Returns all registered tags.
     *
     * @return a list of tags.
     */
    List<ITag> list();

    /**
     * Deletes the tag with the given ID.
     *
     * @param id the tag ID.
     * @return the deleted tag, or null if not found.
     */
    ITag delete(int id);

    /**
     * Creates a new tag with the given name and color.
     *
     * @param name  the tag name.
     * @param color the tag color as a packed RGB integer.
     * @return the created tag.
     */
    ITag create(String name, int color);

    /**
     * Returns the tag with the given ID.
     *
     * @param id the tag ID.
     * @return the tag, or null if not found.
     */
    ITag get(int id);
}
