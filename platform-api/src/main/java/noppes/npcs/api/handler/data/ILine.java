package noppes.npcs.api.handler.data;

import noppes.npcs.api.entity.IEntityLivingBase;

/**
 * Represents a single NPC dialog line with text, an optional sound, and target formatting.
 */
public interface ILine {

    /**
     * Returns a copy of this line with target-specific placeholders resolved.
     *
     * @param entityLivingBase the target entity used for placeholder substitution.
     * @return the formatted line.
     */
    default ILine formatTarget(IEntityLivingBase entityLivingBase) {
        return this;
    }

    /** @return the text content of this line. */
    String getText();

    /**
     * Sets the text content of this line.
     *
     * @param text the new text.
     */
    void setText(String text);

    /** @return the sound resource to play with this line, or null if none. */
    String getSound();

    /**
     * Sets the sound resource to play with this line.
     *
     * @param sound the sound resource path, or null for none.
     */
    void setSound(String sound);

    /**
     * Sets whether the text of this line should be hidden when spoken.
     *
     * @param hide true to hide the text; false to show.
     */
    void hideText(boolean hide);

    /** @return true if the text is hidden when this line is spoken. */
    boolean hideText();
}
