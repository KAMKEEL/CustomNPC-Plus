/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.event
 */

/**
 * Events fired for player actions including combat, items, movement, and interactions.
  * @javaFqn noppes.npcs.api.event.IPlayerEvent
*/
export interface IPlayerEvent extends import('./ICustomNPCsEvent').ICustomNPCsEvent {
    /** @return the player associated with this event. */
    getPlayer(): import('../entity/IPlayer').IPlayer;
    readonly player: import('../entity/IPlayer').IPlayer;
}

export namespace IPlayerEvent {
    
    /**
     * Fired when the player sends a chat message. Cancelable.
     * @hookName chat
          * @javaFqn noppes.npcs.api.event.IPlayerEvent.ChatEvent
*/
    export interface ChatEvent extends IPlayerEvent {
        /** @param message the new chat message. */
        setMessage(message: String): import('./void').void;
        /** @return the chat message. */
        getMessage(): String;
        message: String;
    }
    /**
     * Fired when the player presses or releases a key.
     * @hookName keyPressed
          * @javaFqn noppes.npcs.api.event.IPlayerEvent.KeyPressedEvent
*/
    export interface KeyPressedEvent extends IPlayerEvent {
        /** @return the key code. */
        getKey(): import('./int').int;
        /** @return true if Ctrl is held. */
        isCtrlPressed(): import('./boolean').boolean;
        /** @return true if Alt is held. */
        isAltPressed(): import('./boolean').boolean;
        /** @return true if Shift is held. */
        isShiftPressed(): import('./boolean').boolean;
        /** @return true if Meta is held. */
        isMetaPressed(): import('./boolean').boolean;
        /** @return true if the key is being pressed down, false if released. */
        keyDown(): import('./boolean').boolean;
        /** @return array of currently held key codes. */
        getKeysDown(): import('./int').int[];
        readonly key: import('./int').int;
        readonly isCtrlPressed: import('./boolean').boolean;
        readonly isAltPressed: import('./boolean').boolean;
        readonly isShiftPressed: import('./boolean').boolean;
        readonly isMetaPressed: import('./boolean').boolean;
        readonly keyDown: import('./boolean').boolean;
        readonly keysDown: import('./int').int[];
    }
    /**
     * Fired when the player clicks a mouse button.
     * @hookName mouseClicked
          * @javaFqn noppes.npcs.api.event.IPlayerEvent.MouseClickedEvent
*/
    export interface MouseClickedEvent extends IPlayerEvent {
        /** @return the mouse button index. */
        getButton(): import('./int').int;
        /** @return the mouse wheel scroll delta. */
        getMouseWheel(): import('./int').int;
        /** @return true if the button is being pressed down. */
        buttonDown(): import('./boolean').boolean;
        /** @return true if Ctrl is held. */
        isCtrlPressed(): import('./boolean').boolean;
        /** @return true if Alt is held. */
        isAltPressed(): import('./boolean').boolean;
        /** @return true if Shift is held. */
        isShiftPressed(): import('./boolean').boolean;
        /** @return true if Meta is held. */
        isMetaPressed(): import('./boolean').boolean;
        /** @return array of currently held key codes. */
        getKeysDown(): import('./int').int[];
        readonly isCtrlPressed: import('./boolean').boolean;
        readonly isAltPressed: import('./boolean').boolean;
        readonly isShiftPressed: import('./boolean').boolean;
        readonly isMetaPressed: import('./boolean').boolean;
        readonly keysDown: import('./int').int[];
        readonly button: import('./int').int;
        readonly mouseWheel: import('./int').int;
        readonly buttonDown: import('./boolean').boolean;
    }
    /**
     * Fired when the player picks up experience orbs.
     * @hookName pickupXP
          * @javaFqn noppes.npcs.api.event.IPlayerEvent.PickupXPEvent
*/
    export interface PickupXPEvent extends IPlayerEvent {
        /** @return the amount of XP picked up. */
        getAmount(): import('./int').int;
        readonly amount: import('./int').int;
    }
    /**
     * Fired when the player levels up.
     * @hookName levelUp
          * @javaFqn noppes.npcs.api.event.IPlayerEvent.LevelUpEvent
*/
    export interface LevelUpEvent extends IPlayerEvent {
        /** @return the number of levels gained. */
        getChange(): import('./int').int;
        readonly change: import('./int').int;
    }
    /**
     * Fired when the player logs out.
     * @hookName logout
          * @javaFqn noppes.npcs.api.event.IPlayerEvent.LogoutEvent
*/
    export interface LogoutEvent extends IPlayerEvent {
    }
    /**
     * Fired when the player logs in.
     * @hookName login
          * @javaFqn noppes.npcs.api.event.IPlayerEvent.LoginEvent
*/
    export interface LoginEvent extends IPlayerEvent {
    }
    /**
     * Fired when the player respawns after death.
     * @hookName respawn
          * @javaFqn noppes.npcs.api.event.IPlayerEvent.RespawnEvent
*/
    export interface RespawnEvent extends IPlayerEvent {
    }
    /**
     * Fired when the player changes dimensions.
     * @hookName changedDim
          * @javaFqn noppes.npcs.api.event.IPlayerEvent.ChangedDimension
*/
    export interface ChangedDimension extends IPlayerEvent {
        /** @return the dimension ID the player came from. */
        getFromDim(): import('./int').int;
        /** @return the dimension ID the player traveled to. */
        getToDim(): import('./int').int;
        readonly fromDim: import('./int').int;
        readonly toDim: import('./int').int;
    }
    /**
     * Fired when a player timer triggers.
     * @hookName timer
          * @javaFqn noppes.npcs.api.event.IPlayerEvent.TimerEvent
*/
    export interface TimerEvent extends IPlayerEvent {
        /** @return the timer ID. */
        getId(): import('./int').int;
        readonly id: import('./int').int;
    }
    /**
     * Fired when the player is attacked (before armor reduction). Cancelable.
     * @hookName attacked
          * @javaFqn noppes.npcs.api.event.IPlayerEvent.AttackedEvent
*/
    export interface AttackedEvent extends IPlayerEvent {
        /** @return the damage source details. */
        getDamageSource(): import('../IDamageSource').IDamageSource;
        /** @return the attacking entity, or null. */
        getSource(): import('../entity/IEntity').IEntity;
        /** @return the raw damage amount. */
        getDamage(): import('./float').float;
        readonly damageSource: import('../IDamageSource').IDamageSource;
        readonly source: import('../entity/IEntity').IEntity;
        damage: import('./float').float;
    }
    /**
     * Fired when the player takes damage (after armor reduction). Cancelable.
     * @hookName damaged
          * @javaFqn noppes.npcs.api.event.IPlayerEvent.DamagedEvent
*/
    export interface DamagedEvent extends IPlayerEvent {
        /** @return the damage source details. */
        getDamageSource(): import('../IDamageSource').IDamageSource;
        /** @return the damaging entity, or null. */
        getSource(): import('../entity/IEntity').IEntity;
        /** @return the damage amount after reduction. */
        getDamage(): import('./float').float;
        readonly damageSource: import('../IDamageSource').IDamageSource;
        readonly source: import('../entity/IEntity').IEntity;
        damage: import('./float').float;
        clearTarget: import('./boolean').boolean;
    }
    /**
     * Fired when the player is about to be struck by lightning. Cancelable.
     * @hookName lightning
          * @javaFqn noppes.npcs.api.event.IPlayerEvent.LightningEvent
*/
    export interface LightningEvent extends IPlayerEvent {
    }
    /**
     * Fired when a sound is played for the player. Cancelable.
     * @hookName playSound
          * @javaFqn noppes.npcs.api.event.IPlayerEvent.SoundEvent
*/
    export interface SoundEvent extends IPlayerEvent {
        /** @return the sound resource name. */
        getName(): String;
        /** @return the sound pitch. */
        getPitch(): import('./float').float;
        /** @return the sound volume. */
        getVolume(): import('./float').float;
        readonly name: String;
        readonly pitch: import('./float').float;
        readonly volume: import('./float').float;
    }
    /**
     * Fired when the player falls. Cancelable.
     * @hookName fall
          * @javaFqn noppes.npcs.api.event.IPlayerEvent.FallEvent
*/
    export interface FallEvent extends IPlayerEvent {
        /** @return the fall distance in blocks. */
        getDistance(): import('./float').float;
        readonly distance: import('./float').float;
    }
    /**
     * Fired when the player jumps.
     * @hookName jump
          * @javaFqn noppes.npcs.api.event.IPlayerEvent.JumpEvent
*/
    export interface JumpEvent extends IPlayerEvent {
    }
    /**
     * Fired when the player kills an entity.
     * @hookName kills
          * @javaFqn noppes.npcs.api.event.IPlayerEvent.KilledEntityEvent
*/
    export interface KilledEntityEvent extends IPlayerEvent {
        /** @return the killed entity. */
        getEntity(): import('../entity/IEntityLivingBase').IEntityLivingBase;
        readonly entity: import('../entity/IEntityLivingBase').IEntityLivingBase;
    }
    /**
     * Fired when the player dies.
     * @hookName killed
          * @javaFqn noppes.npcs.api.event.IPlayerEvent.DiedEvent
*/
    export interface DiedEvent extends IPlayerEvent {
        /** @return the damage source that killed the player. */
        getDamageSource(): import('../IDamageSource').IDamageSource;
        /** @return the damage type string. */
        getType(): String;
        /** @return the entity that killed the player, or null. */
        getSource(): import('../entity/IEntity').IEntity;
        readonly damageSource: import('../IDamageSource').IDamageSource;
        readonly type: String;
        readonly source: import('../entity/IEntity').IEntity;
    }
    /**
     * Fired when the player launches a ranged weapon (e.g., bow). Cancelable.
     * @hookName rangedLaunched
          * @javaFqn noppes.npcs.api.event.IPlayerEvent.RangedLaunchedEvent
*/
    export interface RangedLaunchedEvent extends IPlayerEvent {
        /** @return the bow item. */
        getBow(): import('../item/IItemStack').IItemStack;
        /** @return the charge level of the bow. */
        getCharge(): import('./int').int;
        readonly bow: import('../item/IItemStack').IItemStack;
        charge: import('./int').int;
    }
    /**
     * Fired when the player attacks an entity. Cancelable.
     * @hookName attack
          * @javaFqn noppes.npcs.api.event.IPlayerEvent.AttackEvent
*/
    export interface AttackEvent extends IPlayerEvent {
        /** @return the damage source details. */
        getDamageSource(): import('../IDamageSource').IDamageSource;
        /** @return the target entity. */
        getTarget(): import('../entity/IEntity').IEntity;
        /** @return the attack damage. */
        getDamage(): import('./float').float;
        readonly damageSource: import('../IDamageSource').IDamageSource;
        readonly target: import('../entity/IEntity').IEntity;
        damage: import('./float').float;
    }
    /**
     * Fired when the player deals damage to an entity (after hit). Cancelable.
     * @hookName damagedEntity
          * @javaFqn noppes.npcs.api.event.IPlayerEvent.DamagedEntityEvent
*/
    export interface DamagedEntityEvent extends IPlayerEvent {
        /** @return the damage source details. */
        getDamageSource(): import('../IDamageSource').IDamageSource;
        /** @return the damaged entity. */
        getTarget(): import('../entity/IEntity').IEntity;
        /** @return the damage dealt. */
        getDamage(): import('./float').float;
        readonly damageSource: import('../IDamageSource').IDamageSource;
        readonly target: import('../entity/IEntity').IEntity;
        damage: import('./float').float;
    }
    /**
     * Fired when the player closes a container.
     * @hookName containerClosed
          * @javaFqn noppes.npcs.api.event.IPlayerEvent.ContainerClosed
*/
    export interface ContainerClosed extends IPlayerEvent {
        /** @return the closed container. */
        getContainer(): import('../IContainer').IContainer;
        readonly container: import('../IContainer').IContainer;
    }
    /**
     * Fired when the player opens a container.
     * @hookName containerOpen
          * @javaFqn noppes.npcs.api.event.IPlayerEvent.ContainerOpen
*/
    export interface ContainerOpen extends IPlayerEvent {
        /** @return the opened container. */
        getContainer(): import('../IContainer').IContainer;
        readonly container: import('../IContainer').IContainer;
    }
    /**
     * Fired when the player picks up an item. Cancelable.
     * @hookName pickUp
          * @javaFqn noppes.npcs.api.event.IPlayerEvent.PickUpEvent
*/
    export interface PickUpEvent extends IPlayerEvent {
        /** @return the picked up item. */
        getItem(): import('../item/IItemStack').IItemStack;
        readonly item: import('../item/IItemStack').IItemStack;
    }
    /**
     * Fired when items are dropped from the player's inventory (e.g., on death). Cancelable.
     * @hookName drop
          * @javaFqn noppes.npcs.api.event.IPlayerEvent.DropEvent
*/
    export interface DropEvent extends IPlayerEvent {
        /** @return the dropped items. */
        getItems(): import('../item/IItemStack').IItemStack[];
        readonly items: import('../item/IItemStack').IItemStack[];
    }
    /**
     * Fired when the player tosses a single item. Cancelable.
     * @hookName toss
          * @javaFqn noppes.npcs.api.event.IPlayerEvent.TossEvent
*/
    export interface TossEvent extends IPlayerEvent {
        /** @return the tossed item. */
        getItem(): import('../item/IItemStack').IItemStack;
        readonly item: import('../item/IItemStack').IItemStack;
    }
    /**
     * Fired when the player interacts with an entity. Cancelable.
     * @hookName interact
          * @javaFqn noppes.npcs.api.event.IPlayerEvent.InteractEvent
*/
    export interface InteractEvent extends IPlayerEvent {
        /** @return the interaction type. */
        getType(): import('./int').int;
        /** @return the target entity. */
        getTarget(): import('../entity/IEntity').IEntity;
        readonly type: import('./int').int;
        readonly target: import('../entity/IEntity').IEntity;
    }
    /**
     * Fired when the player right-clicks. Cancelable.
     * @hookName rightClick
          * @javaFqn noppes.npcs.api.event.IPlayerEvent.RightClickEvent
*/
    export interface RightClickEvent extends IPlayerEvent {
        /** @return the click type. */
        getType(): import('./int').int;
        /** @return the target (entity, block, or null). */
        getTarget(): Object;
        /** @return the player. */
        getPlayer(): import('../entity/IPlayer').IPlayer;
        readonly type: import('./int').int;
        readonly target: Object;
    }
    /**
     * Fired each tick for the player.
     * @hookName tick
          * @javaFqn noppes.npcs.api.event.IPlayerEvent.UpdateEvent
*/
    export interface UpdateEvent extends IPlayerEvent {
    }
    /**
     * Fired when the player script is initialized.
     * @hookName init
          * @javaFqn noppes.npcs.api.event.IPlayerEvent.InitEvent
*/
    export interface InitEvent extends IPlayerEvent {
    }
    /**
     * Fired when the player starts using an item (e.g., drawing a bow).
     * @hookName startItem
          * @javaFqn noppes.npcs.api.event.IPlayerEvent.StartUsingItem
*/
    export interface StartUsingItem extends IPlayerEvent {
        /** @return the item being used. */
        getItem(): import('../item/IItemStack').IItemStack;
        /** @return the use duration in ticks. */
        getDuration(): import('./int').int;
        readonly item: import('../item/IItemStack').IItemStack;
        readonly duration: import('./int').int;
    }
    /**
     * Fired each tick while the player is using an item.
     * @hookName usingItem
          * @javaFqn noppes.npcs.api.event.IPlayerEvent.UsingItem
*/
    export interface UsingItem extends IPlayerEvent {
        /** @return the item being used. */
        getItem(): import('../item/IItemStack').IItemStack;
        /** @return the remaining use duration in ticks. */
        getDuration(): import('./int').int;
        readonly item: import('../item/IItemStack').IItemStack;
        readonly duration: import('./int').int;
    }
    /**
     * Fired when the player stops using an item before completion.
     * @hookName stopItem
          * @javaFqn noppes.npcs.api.event.IPlayerEvent.StopUsingItem
*/
    export interface StopUsingItem extends IPlayerEvent {
        /** @return the item that was being used. */
        getItem(): import('../item/IItemStack').IItemStack;
        /** @return the remaining use duration in ticks. */
        getDuration(): import('./int').int;
        readonly item: import('../item/IItemStack').IItemStack;
        readonly duration: import('./int').int;
    }
    /**
     * Fired when the player finishes using an item (full duration).
     * @hookName finishItem
          * @javaFqn noppes.npcs.api.event.IPlayerEvent.FinishUsingItem
*/
    export interface FinishUsingItem extends IPlayerEvent {
        /** @return the item that was used. */
        getItem(): import('../item/IItemStack').IItemStack;
        /** @return the total use duration in ticks. */
        getDuration(): import('./int').int;
        readonly item: import('../item/IItemStack').IItemStack;
        readonly duration: import('./int').int;
    }
    /**
     * Fired when the player breaks a block. Cancelable.
     * @hookName breakBlock
          * @javaFqn noppes.npcs.api.event.IPlayerEvent.BreakEvent
*/
    export interface BreakEvent extends IPlayerEvent {
        /** @return the block being broken. */
        getBlock(): import('../IBlock').IBlock;
        /** @return the experience dropped from breaking the block. */
        getExp(): import('./int').int;
        readonly block: import('../IBlock').IBlock;
        exp: import('./int').int;
    }
    /**
     * Fired when the player uses a hoe on a block.
     * @hookName useHoe
          * @javaFqn noppes.npcs.api.event.IPlayerEvent.UseHoeEvent
*/
    export interface UseHoeEvent extends IPlayerEvent {
        /** @return the hoe item. */
        getHoe(): import('../item/IItemStack').IItemStack;
        /** @return the X coordinate of the hoed block. */
        getX(): import('./int').int;
        /** @return the Y coordinate of the hoed block. */
        getY(): import('./int').int;
        /** @return the Z coordinate of the hoed block. */
        getZ(): import('./int').int;
    }
    /**
     * Fired when the player wakes up from a bed.
     * @hookName wakeUp
          * @javaFqn noppes.npcs.api.event.IPlayerEvent.WakeUpEvent
*/
    export interface WakeUpEvent extends IPlayerEvent {
        /** @return true if the bed position was set as spawn. */
        setSpawn(): import('./boolean').boolean;
    }
    /**
     * Fired when the player sleeps in a bed.
     * @hookName sleep
          * @javaFqn noppes.npcs.api.event.IPlayerEvent.SleepEvent
*/
    export interface SleepEvent extends IPlayerEvent {
        /** @return the X coordinate of the bed. */
        getX(): import('./int').int;
        /** @return the Y coordinate of the bed. */
        getY(): import('./int').int;
        /** @return the Z coordinate of the bed. */
        getZ(): import('./int').int;
    }
    /**
     * Fired when the player earns an achievement.
     * @hookName achievement
          * @javaFqn noppes.npcs.api.event.IPlayerEvent.AchievementEvent
*/
    export interface AchievementEvent extends IPlayerEvent {
        /** @return the achievement description. */
        getDescription(): String;
    }
    /**
     * Fired when the player fills a bucket.
     * @hookName fillBucket
          * @javaFqn noppes.npcs.api.event.IPlayerEvent.FillBucketEvent
*/
    export interface FillBucketEvent extends IPlayerEvent {
        /** @return the empty bucket item. */
        getCurrent(): import('../item/IItemStack').IItemStack;
        /** @return the filled bucket item. */
        getFilled(): import('../item/IItemStack').IItemStack;
    }
    /**
     * Fired when the player uses bonemeal on a block.
     * @hookName bonemeal
          * @javaFqn noppes.npcs.api.event.IPlayerEvent.BonemealEvent
*/
    export interface BonemealEvent extends IPlayerEvent {
        /** @return the block that was bonemealed. */
        getBlock(): import('../IBlock').IBlock;
        /** @return the X coordinate. */
        getX(): import('./int').int;
        /** @return the Y coordinate. */
        getY(): import('./int').int;
        /** @return the Z coordinate. */
        getZ(): import('./int').int;
    }
    /**
     * Fired when the player begins charging a ranged weapon.
     * @hookName rangedCharge
          * @javaFqn noppes.npcs.api.event.IPlayerEvent.RangedChargeEvent
*/
    export interface RangedChargeEvent extends IPlayerEvent {
    }
    /**
     * Events fired for player custom effects (add, tick, remove).
     * @hookName onEffect
          * @javaFqn noppes.npcs.api.event.IPlayerEvent.EffectEvent
*/
    export interface EffectEvent extends IPlayerEvent {
        /** @return the player effect. */
        getEffect(): import('../handler/data/IPlayerEffect').IPlayerEffect;
        readonly effect: import('../handler/data/IPlayerEffect').IPlayerEffect;
    }
    export namespace EffectEvent {
        
        /**
         * Fired when an effect is added to the player.
         * @hookName onEffectAdd
                  * @javaFqn noppes.npcs.api.event.IPlayerEvent.EffectEvent.Added
*/
        export interface Added extends EffectEvent {
        }
        /**
         * Fired each tick while the effect is active.
         * @hookName onEffectTick
                  * @javaFqn noppes.npcs.api.event.IPlayerEvent.EffectEvent.Ticked
*/
        export interface Ticked extends EffectEvent {
        }
        /**
         * Fired when an effect is removed from the player.
         * @hookName onEffectRemove
                  * @javaFqn noppes.npcs.api.event.IPlayerEvent.EffectEvent.Removed
*/
        export interface Removed extends EffectEvent {
            /**
             * @return If the effect timer has ticked down to 0.
             */
            hasTimerRunOut(): import('./boolean').boolean;
            /**
             * @return If the effect was removed on death.
             */
            causedByDeath(): import('./boolean').boolean;
        }
    }
    /**
     * Events fired for profile operations (create, change, remove). Cancelable.
     * @hookName profile
          * @javaFqn noppes.npcs.api.event.IPlayerEvent.ProfileEvent
*/
    export interface ProfileEvent extends IPlayerEvent {
        /**
         * @return IProfile Object of the Operation
         */
        getProfile(): import('../handler/data/IProfile').IProfile;
        /**
         * @return Slot ID in question
         */
        getSlot(): import('./int').int;
        /**
         * @return returns true if it occurs after the operation (not cancellable)
         */
        isPost(): import('./boolean').boolean;
        readonly profile: import('../handler/data/IProfile').IProfile;
        readonly slot: import('./int').int;
        readonly post: import('./boolean').boolean;
    }
    export namespace ProfileEvent {
        
        
        
        /**
         * Fired when the player switches profile slots. Cancelable.
         * @hookName profileChange
                  * @javaFqn noppes.npcs.api.event.IPlayerEvent.ProfileEvent.Changed
*/
        export interface Changed extends ProfileEvent {
            /**
             * @return The previous slot before the switch
             */
            getPrevSlot(): import('./int').int;
            readonly prevSlot: import('./int').int;
        }
        /**
         * Fired when a new profile is created. Cancelable.
         * @hookName profileCreate
                  * @javaFqn noppes.npcs.api.event.IPlayerEvent.ProfileEvent.Create
*/
        export interface Create extends ProfileEvent {
        }
        /**
         * Fired when a profile is removed. Cancelable.
         * @hookName profileRemove
                  * @javaFqn noppes.npcs.api.event.IPlayerEvent.ProfileEvent.Removed
*/
        export interface Removed extends ProfileEvent {
        }
    }
}
