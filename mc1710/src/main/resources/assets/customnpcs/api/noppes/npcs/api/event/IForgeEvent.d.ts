/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.event
 */

/**
 * Wrapper events for Forge events, allowing scripts to listen for native Forge events.
  * @javaFqn noppes.npcs.api.event.IForgeEvent
*/
export interface IForgeEvent extends import('./ICustomNPCsEvent').ICustomNPCsEvent {
    /** @return the underlying Forge event. */
    getEvent(): Event;
    readonly event: Event;
}

export namespace IForgeEvent {
    
    /**
     * Fired for Forge world events.
     * @hookName forgeWorld
          * @javaFqn noppes.npcs.api.event.IForgeEvent.WorldEvent
*/
    export interface WorldEvent extends IForgeEvent {
        /** @return the world associated with this event. */
        getWorld(): import('../IWorld').IWorld;
        readonly world: import('../IWorld').IWorld;
    }
    /**
     * Fired for Forge entity events.
     * @hookName forgeEntity
          * @javaFqn noppes.npcs.api.event.IForgeEvent.EntityEvent
*/
    export interface EntityEvent extends IForgeEvent {
        /** @return the entity associated with this event. */
        getEntity(): import('../entity/IEntity').IEntity;
        readonly entity: import('../entity/IEntity').IEntity;
    }
    /**
     * Fired during Forge initialization.
     * @hookName forgeInit
          * @javaFqn noppes.npcs.api.event.IForgeEvent.InitEvent
*/
    export interface InitEvent extends IForgeEvent {
    }
}
