package noppes.npcs.api.handler;

import noppes.npcs.api.handler.data.IFaction;

import java.util.List;

/**
 * Handles creation, deletion, and retrieval of factions.
 */
public interface IFactionHandler {

    /**
     * Returns all registered factions.
     *
     * @return a list of factions.
     */
    List<IFaction> list();

    /**
     * Deletes the faction with the given ID.
     *
     * @param id the faction ID.
     * @return the deleted faction, or null if not found.
     */
    IFaction delete(int id);

    /**
     * Creates a new faction with the given name and default points.
     *
     * @param name          the faction name.
     * @param defaultPoints the default standing points for new players.
     * @return the created faction.
     */
    IFaction create(String name, int defaultPoints);

    /**
     * Returns the faction with the given ID.
     *
     * @param id the faction ID.
     * @return the faction, or null if not found.
     */
    IFaction get(int id);
}
