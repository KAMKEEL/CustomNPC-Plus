package noppes.npcs.api.handler;

import noppes.npcs.api.handler.data.ITransportLocation;

/**
 * Tracks which transport locations a player has unlocked.
 */
public interface IPlayerTransportData {

    /**
     * @param id the transport location ID.
     * @return true if the player has unlocked this transport.
     */
    boolean hasTransport(int id);

    /**
     * Unlocks a transport location by ID.
     *
     * @param id the transport location ID.
     */
    void addTransport(int id);

    /**
     * Unlocks the given transport location.
     *
     * @param location the transport location.
     */
    void addTransport(ITransportLocation location);

    /**
     * Returns the transport location with the given ID.
     *
     * @param id the transport location ID.
     * @return the transport location, or null if not found.
     */
    ITransportLocation getTransport(int id);

    /**
     * Returns all unlocked transport locations for the player.
     *
     * @return an array of transport locations.
     */
    ITransportLocation[] getTransports();

    /**
     * Removes an unlocked transport location by ID.
     *
     * @param id the transport location ID.
     */
    void removeTransport(int id);
}
