/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.event
 */

export interface IBlockEvent extends ICustomNPCsEvent {
    getBlock(): import('../IBlock').IBlock;
}

export namespace IBlockEvent {
    export interface EntityFallenUponEvent extends IBlockEvent {
        getEntity(): import('../entity/IEntity').IEntity;
        getDistanceFallen(): number;
    }
    export interface InteractEvent extends IBlockEvent {
        getPlayer(): import('../entity/IPlayer').IPlayer;
        getHitX(): number;
        getHitY(): number;
        getHitZ(): number;
        getSide(): number;
    }
    export interface RedstoneEvent extends IBlockEvent {
        getPrevPower(): number;
        getPower(): number;
    }
    export interface BreakEvent extends IBlockEvent {
    }
    export interface ExplodedEvent extends IBlockEvent {
    }
    export interface RainFillEvent extends IBlockEvent {
    }
    export interface NeighborChangedEvent extends IBlockEvent {
        getChangedPos(): import('../IPos').IPos;
    }
    export interface InitEvent extends IBlockEvent {
    }
    export interface UpdateEvent extends IBlockEvent {
    }
    export interface ClickedEvent extends IBlockEvent {
        getPlayer(): import('../entity/IPlayer').IPlayer;
    }
    export interface HarvestedEvent extends IBlockEvent {
        getPlayer(): import('../entity/IPlayer').IPlayer;
    }
    export interface CollidedEvent extends IBlockEvent {
        getEntity(): import('../entity/IEntity').IEntity;
    }
    export interface TimerEvent extends IBlockEvent {
        getId(): number;
    }
}
