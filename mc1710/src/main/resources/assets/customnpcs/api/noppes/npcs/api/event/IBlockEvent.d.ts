/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.event
 */

/**
 * Events fired for scripted blocks, including interactions, redstone, and entity collisions.
  * @javaFqn noppes.npcs.api.event.IBlockEvent
*/
export interface IBlockEvent extends import('./ICustomNPCsEvent').ICustomNPCsEvent {
    /** @return the scripted block associated with this event. */
    getBlock(): import('../IBlock').IBlock;
    block: import('../IBlock').IBlock;
}

export namespace IBlockEvent {
    
    /**
     * @hookName fallenUpon
          * @javaFqn noppes.npcs.api.event.IBlockEvent.EntityFallenUponEvent
*/
    export interface EntityFallenUponEvent extends IBlockEvent {
        getEntity(): import('../entity/IEntity').IEntity;
        getDistanceFallen(): import('./float').float;
        readonly entity: import('../entity/IEntity').IEntity;
        distanceFallen: import('./float').float;
    }
    /**
     * @javaFqn noppes.npcs.api.event.IBlockEvent.InteractEvent
     */
    export interface InteractEvent extends IBlockEvent {
        getPlayer(): import('../entity/IPlayer').IPlayer;
        getHitX(): import('./float').float;
        getHitY(): import('./float').float;
        getHitZ(): import('./float').float;
        getSide(): import('./int').int;
        readonly player: import('../entity/IPlayer').IPlayer;
        readonly hitZ: import('./float hitX, hitY,').float hitX, hitY,;
        readonly side: import('./int').int;
    }
    /**
     * @javaFqn noppes.npcs.api.event.IBlockEvent.RedstoneEvent
     */
    export interface RedstoneEvent extends IBlockEvent {
        getPrevPower(): import('./int').int;
        getPower(): import('./int').int;
        readonly power: import('./int prevPower,').int prevPower,;
    }
    /**
     * @hookName broken
          * @javaFqn noppes.npcs.api.event.IBlockEvent.BreakEvent
*/
    export interface BreakEvent extends IBlockEvent {
    }
    /**
     * @javaFqn noppes.npcs.api.event.IBlockEvent.ExplodedEvent
     */
    export interface ExplodedEvent extends IBlockEvent {
    }
    /**
     * @hookName rainFilled
          * @javaFqn noppes.npcs.api.event.IBlockEvent.RainFillEvent
*/
    export interface RainFillEvent extends IBlockEvent {
    }
    /**
     * @javaFqn noppes.npcs.api.event.IBlockEvent.NeighborChangedEvent
     */
    export interface NeighborChangedEvent extends IBlockEvent {
        getChangedPos(): import('../IPos').IPos;
        readonly changedPos: import('../IPos').IPos;
    }
    /**
     * @javaFqn noppes.npcs.api.event.IBlockEvent.InitEvent
     */
    export interface InitEvent extends IBlockEvent {
    }
    /**
     * @hookName tick
          * @javaFqn noppes.npcs.api.event.IBlockEvent.UpdateEvent
*/
    export interface UpdateEvent extends IBlockEvent {
    }
    /**
     * @javaFqn noppes.npcs.api.event.IBlockEvent.ClickedEvent
     */
    export interface ClickedEvent extends IBlockEvent {
        getPlayer(): import('../entity/IPlayer').IPlayer;
        readonly player: import('../entity/IPlayer').IPlayer;
    }
    /**
     * @javaFqn noppes.npcs.api.event.IBlockEvent.HarvestedEvent
     */
    export interface HarvestedEvent extends IBlockEvent {
        getPlayer(): import('../entity/IPlayer').IPlayer;
        readonly player: import('../entity/IPlayer').IPlayer;
    }
    /**
     * @javaFqn noppes.npcs.api.event.IBlockEvent.CollidedEvent
     */
    export interface CollidedEvent extends IBlockEvent {
        getEntity(): import('../entity/IEntity').IEntity;
        readonly entity: import('../entity/IEntity').IEntity;
    }
    /**
     * @javaFqn noppes.npcs.api.event.IBlockEvent.TimerEvent
     */
    export interface TimerEvent extends IBlockEvent {
        getId(): import('./int').int;
        readonly id: import('./int').int;
    }
}
