package noppes.npcs.api.handler.data;

/**
 * Defines the blueprint for an attribute.
 * <p>
 * An attribute definition contains identifying information such as a unique key,
 * a human-readable display name, a translation key for localization, and a color code.
 * </p>
 */
public interface IAttributeDefinition {

    /**
     * Returns the unique key that identifies this attribute.
     *
     * @return a String representing the attribute key
     */
    String getKey();

    /**
     * Returns the human-readable display name for this attribute.
     *
     * @return a String representing the display name
     */
    String getDisplayName();

    /**
     * Returns the translation key used for localizing this attribute.
     *
     * @return a String representing the translation key
     */
    String getTranslationKey();

    /**
     * Returns the color code associated with this attribute.
     *
     * @return a char representing the color code
     */
    char getColorCode();
}
