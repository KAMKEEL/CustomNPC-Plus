package noppes.npcs.api.handler.data;

/**
 * Represents a tag that can be applied to NPCs for categorization and filtering.
 */
public interface ITag {

    /** @return the unique UUID string for this tag. */
    String getUuid();

    /** @return the tag display name. */
    String getName();

    /** @param name the tag display name. */
    void setName(String name);

    /** @param c the tag color as a packed RGB integer. */
    void setColor(int c);

    /** @return the unique tag ID. */
    int getId();

    /** @return the tag color as a packed RGB integer. */
    int getColor();

    /** @return true if this tag is hidden from display. */
    boolean getIsHidden();

    /** @param hidden true to hide this tag from display. */
    void setIsHidden(boolean hidden);

    /** Saves this tag to disk. */
    void save();
}
