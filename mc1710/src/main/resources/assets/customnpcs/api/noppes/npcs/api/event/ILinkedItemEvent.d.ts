/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.event
 */

/**
 * Events fired for linked items when their version changes or when they are being built.
  * @javaFqn noppes.npcs.api.event.ILinkedItemEvent
*/
export interface ILinkedItemEvent extends import('./IItemEvent').IItemEvent {
}

export namespace ILinkedItemEvent {
    /**
     * Fired when the linked item's version changes.
     * @hookName versionChanged
          * @javaFqn noppes.npcs.api.event.ILinkedItemEvent.VersionChangeEvent
*/
    export interface VersionChangeEvent extends import('./IItemEvent').IItemEvent {
        /** @return the new version number. */
        getVersion(): import('./int').int;
        /** @return the previous version number. */
        getPreviousVersion(): import('./int').int;
        readonly prevVersion: import('./int version,').int version,;
    }
    /**
     * Fired when the linked item is being built/constructed.
     * @hookName buildingItem
          * @javaFqn noppes.npcs.api.event.ILinkedItemEvent.BuildEvent
*/
    export interface BuildEvent extends import('./IItemEvent').IItemEvent {
    }
}
