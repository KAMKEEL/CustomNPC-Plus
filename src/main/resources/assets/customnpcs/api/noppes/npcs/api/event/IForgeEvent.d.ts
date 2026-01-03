/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.event
 */

export interface IForgeEvent extends ICustomNPCsEvent {
    getEvent(): any;
}

export namespace IForgeEvent {
    export interface WorldEvent extends IForgeEvent {
        getWorld(): import('../IWorld').IWorld;
    }
    export interface EntityEvent extends IForgeEvent {
        getEntity(): import('../entity/IEntity').IEntity;
    }
    export interface InitEvent extends IForgeEvent {
    }
}
