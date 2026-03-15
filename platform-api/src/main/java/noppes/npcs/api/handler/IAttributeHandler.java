package noppes.npcs.api.handler;

import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.IAttributeDefinition;
import noppes.npcs.api.handler.data.IPlayerAttributes;

/**
 * Handles registration and lookup of attribute definitions as well as
 * retrieval of player attributes.
 * <p>
 * Maintain a unique registry of attribute definitions and provide methods to
 * retrieve a player's aggregated attributes.
 * </p>
 */
public interface IAttributeHandler {

    /**
     * Returns the aggregated attributes for a given player.
     *
     * @param player the player whose attributes are requested
     * @return an {@link IPlayerAttributes} instance containing all custom attributes for the player,
     * or null if the player is invalid
     */
    IPlayerAttributes getPlayerAttributes(IPlayer player);

    /**
     * Returns the attribute definition corresponding to the given key.
     *
     * @param key the unique key identifying the attribute (e.g., "health", "movement_speed")
     * @return the {@link IAttributeDefinition} instance for the specified key, or null if not found
     */
    IAttributeDefinition getAttributeDefinition(String key);

    /**
     * Returns an array of all registered attribute definitions.
     *
     * @return an array of {@link IAttributeDefinition} objects representing all attributes
     */
    IAttributeDefinition[] getAllAttributesArray();
}
