package noppes.npcs.api.event;

import noppes.npcs.api.Cancelable;
import noppes.npcs.api.handler.data.IFaction;

/**
 * Events fired when a player's faction standing changes.
 */
public interface IFactionEvent extends IPlayerEvent {

    /** @return the faction involved in this event. */
    IFaction getFaction();

    /**
     * Fired when a player's faction points change. Cancelable.
     * @hookName factionPoints
     */
    @Cancelable
    interface FactionPoints extends IFactionEvent {
        /** @return true if the points decreased, false if increased. */
        boolean decreased();

        /** @return the amount of points changed. */
        int getPoints();
    }
}
