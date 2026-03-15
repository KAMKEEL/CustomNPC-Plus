/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.event
 */

/**
 * Events fired when a player's faction standing changes.
  * @javaFqn noppes.npcs.api.event.IFactionEvent
*/
export interface IFactionEvent extends import('./IPlayerEvent').IPlayerEvent {
    /** @return the faction involved in this event. */
    getFaction(): import('../handler/data/IFaction').IFaction;
    readonly faction: import('../handler/data/IFaction').IFaction;
}

export namespace IFactionEvent {
    
    /**
     * Fired when a player's faction points change. Cancelable.
     * @hookName factionPoints
          * @javaFqn noppes.npcs.api.event.IFactionEvent.FactionPoints
*/
    export interface FactionPoints extends IFactionEvent {
        /** @return true if the points decreased, false if increased. */
        decreased(): import('./boolean').boolean;
        /** @return the amount of points changed. */
        getPoints(): import('./int').int;
        decrease: import('./boolean').boolean;
        points: import('./int').int;
    }
}
