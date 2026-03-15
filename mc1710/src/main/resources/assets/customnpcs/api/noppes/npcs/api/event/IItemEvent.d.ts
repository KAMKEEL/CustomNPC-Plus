/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.event
 */

/**
 * Events fired for scripted/customizable items during their lifecycle and interactions.
  * @javaFqn noppes.npcs.api.event.IItemEvent
*/
export interface IItemEvent extends import('./ICustomNPCsEvent').ICustomNPCsEvent {
    /** @return the customizable item associated with this event. */
    getItem(): import('../item/IItemCustomizable').IItemCustomizable;
    readonly item: import('../item/IItemCustomizable').IItemCustomizable;
}

export namespace IItemEvent {
    
    /**
     * Fired when the item is initialized.
     * @hookName init
          * @javaFqn noppes.npcs.api.event.IItemEvent.InitEvent
*/
    export interface InitEvent extends IItemEvent {
    }
    /**
     * Fired each tick while the item exists in an entity's inventory.
     * @hookName tick
          * @javaFqn noppes.npcs.api.event.IItemEvent.UpdateEvent
*/
    export interface UpdateEvent extends IItemEvent {
        /** @return the entity holding the item. */
        getEntity(): import('../entity/IEntity').IEntity;
        readonly entity: import('../entity/IEntity').IEntity;
    }
    /**
     * Fired when the item is tossed/dropped. Cancelable.
     * @hookName tossed
          * @javaFqn noppes.npcs.api.event.IItemEvent.TossedEvent
*/
    export interface TossedEvent extends IItemEvent {
        /** @return the dropped item entity. */
        getEntity(): import('../entity/IEntity').IEntity;
        /** @return the player who dropped the item. */
        getPlayer(): import('../entity/IPlayer').IPlayer;
        readonly entity: import('../entity/IEntity').IEntity;
        readonly player: import('../entity/IPlayer').IPlayer;
    }
    /**
     * Fired when the item is picked up.
     * @hookName pickedUp
          * @javaFqn noppes.npcs.api.event.IItemEvent.PickedUpEvent
*/
    export interface PickedUpEvent extends IItemEvent {
        /** @return the player who picked up the item. */
        getPlayer(): import('../entity/IPlayer').IPlayer;
        readonly player: import('../entity/IPlayer').IPlayer;
    }
    /**
     * Fired when the item entity spawns in the world. Cancelable.
     * @hookName spawn
          * @javaFqn noppes.npcs.api.event.IItemEvent.SpawnEvent
*/
    export interface SpawnEvent extends IItemEvent {
        /** @return the spawned item entity. */
        getEntity(): import('../entity/IEntity').IEntity;
        readonly entity: import('../entity/IEntity').IEntity;
    }
    /**
     * Fired when the item is used to interact with an entity. Cancelable.
     * @hookName interact
          * @javaFqn noppes.npcs.api.event.IItemEvent.InteractEvent
*/
    export interface InteractEvent extends IItemEvent {
        /** @return the interaction type. */
        getType(): import('./int').int;
        /** @return the target entity. */
        getTarget(): import('../entity/IEntity').IEntity;
        /** @return the player performing the interaction. */
        getPlayer(): import('../entity/IPlayer').IPlayer;
        readonly type: import('./int').int;
        readonly target: import('../entity/IEntity').IEntity;
        readonly player: import('../entity/IPlayer').IPlayer;
    }
    /**
     * Fired when the item is right-clicked. Cancelable.
     * @hookName rightClick
          * @javaFqn noppes.npcs.api.event.IItemEvent.RightClickEvent
*/
    export interface RightClickEvent extends IItemEvent {
        /** @return the click type. */
        getType(): import('./int').int;
        /** @return the target (entity or block). */
        getTarget(): Object;
        /** @return the player who right-clicked. */
        getPlayer(): import('../entity/IPlayer').IPlayer;
        readonly type: import('./int').int;
        readonly target: Object;
        readonly player: import('../entity/IPlayer').IPlayer;
    }
    /**
     * Fired when the item is used to attack an entity. Cancelable.
     * @hookName attack
          * @javaFqn noppes.npcs.api.event.IItemEvent.AttackEvent
*/
    export interface AttackEvent extends IItemEvent {
        /** @return the attack type. */
        getType(): import('./int').int;
        /** @return the entity being attacked. */
        getTarget(): import('../entity/IEntity').IEntity;
        /** @return the entity swinging the item. */
        getSwingingEntity(): import('../entity/IEntity').IEntity;
        readonly type: import('./int').int;
        readonly target: import('../entity/IEntity').IEntity;
        readonly swingingEntity: import('../entity/IEntity').IEntity;
    }
    /**
     * Fired when the player starts using the item (e.g., drawing a bow).
     * @hookName startItem
          * @javaFqn noppes.npcs.api.event.IItemEvent.StartUsingItem
*/
    export interface StartUsingItem extends IItemEvent {
        /** @return the player using the item. */
        getPlayer(): import('../entity/IPlayer').IPlayer;
        /** @return the use duration in ticks. */
        getDuration(): import('./int').int;
        readonly player: import('../entity/IPlayer').IPlayer;
        readonly duration: import('./int').int;
    }
    /**
     * Fired each tick while the player is using the item.
     * @hookName usingItem
          * @javaFqn noppes.npcs.api.event.IItemEvent.UsingItem
*/
    export interface UsingItem extends IItemEvent {
        /** @return the player using the item. */
        getPlayer(): import('../entity/IPlayer').IPlayer;
        /** @return the remaining use duration in ticks. */
        getDuration(): import('./int').int;
        readonly player: import('../entity/IPlayer').IPlayer;
        readonly duration: import('./int').int;
    }
    /**
     * Fired when the player stops using the item before completion.
     * @hookName stopItem
          * @javaFqn noppes.npcs.api.event.IItemEvent.StopUsingItem
*/
    export interface StopUsingItem extends IItemEvent {
        /** @return the player who stopped using the item. */
        getPlayer(): import('../entity/IPlayer').IPlayer;
        /** @return the remaining use duration in ticks. */
        getDuration(): import('./int').int;
        readonly player: import('../entity/IPlayer').IPlayer;
        readonly duration: import('./int').int;
    }
    /**
     * Fired when the player finishes using the item (full duration).
     * @hookName finishItem
          * @javaFqn noppes.npcs.api.event.IItemEvent.FinishUsingItem
*/
    export interface FinishUsingItem extends IItemEvent {
        /** @return the player who finished using the item. */
        getPlayer(): import('../entity/IPlayer').IPlayer;
        /** @return the total use duration in ticks. */
        getDuration(): import('./int').int;
        readonly player: import('../entity/IPlayer').IPlayer;
        readonly duration: import('./int').int;
    }
    /**
     * Fired when the item breaks due to durability loss or consumption.
     * @hookName breakItem
          * @javaFqn noppes.npcs.api.event.IItemEvent.BreakItem
*/
    export interface BreakItem extends IItemEvent {
        /** @return the item stack that broke. */
        getBrokenStack(): import('../item/IItemStack').IItemStack;
        /** @return the player whose item broke. */
        getPlayer(): import('../entity/IPlayer').IPlayer;
        readonly player: import('../entity/IPlayer').IPlayer;
    }
    /**
     * Fired when the item is repaired on an anvil.
     * @hookName repairItem
          * @javaFqn noppes.npcs.api.event.IItemEvent.RepairItem
*/
    export interface RepairItem extends IItemEvent {
        /** @return the left input item. */
        getLeft(): import('../item/IItemStack').IItemStack;
        /** @return the right input item (repair material). */
        getRight(): import('../item/IItemStack').IItemStack;
        /** @return the resulting output item. */
        getOutput(): import('../item/IItemStack').IItemStack;
        /** @return the chance the anvil breaks after this repair. */
        getAnvilBreakChance(): import('./float').float;
        readonly player: import('../entity/IPlayer').IPlayer;
        readonly right: import('./IItemStack left,').IItemStack left,;
        readonly anvilBreakChance: import('./float').float;
    }
}
