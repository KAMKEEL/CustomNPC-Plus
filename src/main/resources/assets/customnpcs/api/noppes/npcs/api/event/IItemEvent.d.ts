/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.event
 */

export interface IItemEvent extends ICustomNPCsEvent {
    getItem(): import('../item/IItemCustomizable').IItemCustomizable;
}

export namespace IItemEvent {
    export type InitEvent = IItemEvent
    export interface UpdateEvent extends IItemEvent {
        getEntity(): import('../entity/IEntity').IEntity;
    }
    export interface TossedEvent extends IItemEvent {
        getEntity(): import('../entity/IEntity').IEntity;
        getPlayer(): import('../entity/IPlayer').IPlayer;
    }
    export interface PickedUpEvent extends IItemEvent {
        getPlayer(): import('../entity/IPlayer').IPlayer;
    }
    export interface SpawnEvent extends IItemEvent {
        getEntity(): import('../entity/IEntity').IEntity;
    }
    export interface InteractEvent extends IItemEvent {
        getType(): number;
        getTarget(): import('../entity/IEntity').IEntity;
        getPlayer(): import('../entity/IPlayer').IPlayer;
    }
    export interface RightClickEvent extends IItemEvent {
        getType(): number;
        getTarget(): any;
        getPlayer(): import('../entity/IPlayer').IPlayer;
    }
    export interface AttackEvent extends IItemEvent {
        getType(): number;
        getTarget(): import('../entity/IEntity').IEntity;
        getSwingingEntity(): import('../entity/IEntity').IEntity;
    }
    export interface StartUsingItem extends IItemEvent {
        getPlayer(): import('../entity/IPlayer').IPlayer;
        getDuration(): number;
    }
    export interface UsingItem extends IItemEvent {
        getPlayer(): import('../entity/IPlayer').IPlayer;
        getDuration(): number;
    }
    export interface StopUsingItem extends IItemEvent {
        getPlayer(): import('../entity/IPlayer').IPlayer;
        getDuration(): number;
    }
    export interface FinishUsingItem extends IItemEvent {
        getPlayer(): import('../entity/IPlayer').IPlayer;
        getDuration(): number;
    }
    export interface BreakItem extends IItemEvent {
        getBrokenStack(): import('../item/IItemStack').IItemStack;
        getPlayer(): import('../entity/IPlayer').IPlayer;
    }
    export interface RepairItem extends IItemEvent {
        getLeft(): import('../item/IItemStack').IItemStack;
        getRight(): import('../item/IItemStack').IItemStack;
        getOutput(): import('../item/IItemStack').IItemStack;
        getAnvilBreakChance(): number;
    }
}
