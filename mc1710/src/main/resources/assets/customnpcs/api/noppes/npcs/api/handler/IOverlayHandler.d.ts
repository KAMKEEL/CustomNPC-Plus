/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler
 */

/**
 * Manages skin overlays applied to an entity or player.
 * Overlays are indexed by ID and rendered on top of the base skin.
  * @javaFqn noppes.npcs.api.handler.IOverlayHandler
*/
export interface IOverlayHandler {
    /**
     * Adds or replaces an overlay at the given ID.
     *
     * @param id      the overlay ID.
     * @param overlay the skin overlay to add.
     */
    add(id: import('./int').int, overlay: import('../ISkinOverlay').ISkinOverlay): import('./void').void;
    /**
     * Returns the overlay at the given ID.
     *
     * @param id the overlay ID.
     * @return the skin overlay, or null if not found.
     */
    get(id: import('./int').int): import('../ISkinOverlay').ISkinOverlay;
    /**
     * Checks whether an overlay exists at the given ID.
     *
     * @param id the overlay ID.
     * @return true if an overlay exists; false otherwise.
     */
    has(id: import('./int').int): import('./boolean').boolean;
    /**
     * Removes the overlay at the given ID.
     *
     * @param id the overlay ID.
     * @return true if an overlay was removed; false otherwise.
     */
    remove(id: import('./int').int): import('./boolean').boolean;
    /**
     * Returns the number of active overlays.
     *
     * @return the overlay count.
     */
    size(): import('./int').int;
    /**
     * Removes all overlays.
     */
    clear(): import('./void').void;
    readonly parent: Object;
    overlayList: Java.java.util.HashMap<Integer, import('../ISkinOverlay').ISkinOverlay>;
}
