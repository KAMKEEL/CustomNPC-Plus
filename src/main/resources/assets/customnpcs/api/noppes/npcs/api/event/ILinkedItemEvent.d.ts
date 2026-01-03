/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.event
 */

export interface ILinkedItemEvent extends IItemEvent {

    // Nested interfaces
    interface VersionChangeEvent extends IItemEvent {
        getVersion(): number;
        getPreviousVersion(): number;
    }
    interface BuildEvent extends IItemEvent {
    }

}
