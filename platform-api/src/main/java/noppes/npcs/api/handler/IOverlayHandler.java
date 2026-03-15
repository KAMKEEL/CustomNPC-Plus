package noppes.npcs.api.handler;

import noppes.npcs.api.ISkinOverlay;

/**
 * Manages skin overlays applied to an entity or player.
 * Overlays are indexed by ID and rendered on top of the base skin.
 */
public interface IOverlayHandler {

    /**
     * Adds or replaces an overlay at the given ID.
     *
     * @param id      the overlay ID.
     * @param overlay the skin overlay to add.
     */
    void add(int id, ISkinOverlay overlay);

    /**
     * Returns the overlay at the given ID.
     *
     * @param id the overlay ID.
     * @return the skin overlay, or null if not found.
     */
    ISkinOverlay get(int id);

    /**
     * Checks whether an overlay exists at the given ID.
     *
     * @param id the overlay ID.
     * @return true if an overlay exists; false otherwise.
     */
    boolean has(int id);

    /**
     * Removes the overlay at the given ID.
     *
     * @param id the overlay ID.
     * @return true if an overlay was removed; false otherwise.
     */
    boolean remove(int id);

    /**
     * Returns the number of active overlays.
     *
     * @return the overlay count.
     */
    int size();

    /**
     * Removes all overlays.
     */
    void clear();
}
