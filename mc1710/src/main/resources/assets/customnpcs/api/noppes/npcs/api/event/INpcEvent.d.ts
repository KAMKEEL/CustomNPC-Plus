/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.event
 */

/**
 * Events fired for CustomNPC entities during their lifecycle and interactions.
  * @javaFqn noppes.npcs.api.event.INpcEvent
*/
export interface INpcEvent extends import('./ICustomNPCsEvent').ICustomNPCsEvent {
    /** @return the NPC associated with this event. */
    getNpc(): import('../entity/ICustomNpc').ICustomNpc;
    readonly npc: import('../entity/ICustomNpc').ICustomNpc;
}

export namespace INpcEvent {
    
    /**
     * Fired when an NPC timer triggers.
     * @hookName timer
          * @javaFqn noppes.npcs.api.event.INpcEvent.TimerEvent
*/
    export interface TimerEvent extends INpcEvent {
        /** @return the timer ID. */
        getId(): import('./int').int;
        readonly id: import('./int').int;
    }
    /**
     * Fired when an entity collides with the NPC.
     * @hookName collide
          * @javaFqn noppes.npcs.api.event.INpcEvent.CollideEvent
*/
    export interface CollideEvent extends INpcEvent {
        /** @return the colliding entity. */
        getEntity(): import('../entity/IEntity').IEntity;
        readonly entity: import('../entity/IEntity').IEntity;
    }
    /**
     * Fired when the NPC takes damage. Cancelable.
     * @hookName damaged
          * @javaFqn noppes.npcs.api.event.INpcEvent.DamagedEvent
*/
    export interface DamagedEvent extends INpcEvent {
        /** @return the entity that caused the damage, or null. */
        getSource(): import('../entity/IEntity').IEntity;
        /** @return the damage source details. */
        getDamageSource(): import('../IDamageSource').IDamageSource;
        /** @return the damage amount. */
        getDamage(): import('./float').float;
        /** @param damage the new damage amount. */
        setDamage(damage: import('./float').float): import('./void').void;
        /** @param bo whether to clear the NPC's current target after damage. */
        setClearTarget(bo: import('./boolean').boolean): import('./void').void;
        /** @return whether the NPC's target will be cleared after damage. */
        getClearTarget(): import('./boolean').boolean;
        /** @return the damage type string. */
        getType(): String;
        readonly damageSource: import('../IDamageSource').IDamageSource;
        readonly source: import('../entity/IEntity').IEntity;
        damage: import('./float').float;
        clear: import('./boolean').boolean;
        readonly damagesource: import('./DamageSource').DamageSource;
    }
    /**
     * Fired when the NPC launches a ranged attack. Cancelable.
     * @hookName rangedLaunched
          * @javaFqn noppes.npcs.api.event.INpcEvent.RangedLaunchedEvent
*/
    export interface RangedLaunchedEvent extends INpcEvent {
        /** @return the target entity. */
        getTarget(): import('../entity/IEntityLivingBase').IEntityLivingBase;
        /** @param damage the new ranged damage. */
        setDamage(damage: import('./float').float): import('./void').void;
        /** @return the ranged damage. */
        getDamage(): import('./float').float;
        readonly target: import('../entity/IEntityLivingBase').IEntityLivingBase;
        damage: import('./float').float;
    }
    /**
     * Fired when the NPC performs a melee attack. Cancelable.
     * @hookName meleeAttack
          * @javaFqn noppes.npcs.api.event.INpcEvent.MeleeAttackEvent
*/
    export interface MeleeAttackEvent extends INpcEvent {
        /** @return the target entity. */
        getTarget(): import('../entity/IEntityLivingBase').IEntityLivingBase;
        /** @param damage the new melee damage. */
        setDamage(damage: import('./float').float): import('./void').void;
        /** @return the melee damage. */
        getDamage(): import('./float').float;
        readonly target: import('../entity/IEntityLivingBase').IEntityLivingBase;
        damage: import('./float').float;
    }
    /**
     * Fired when the NPC swings its weapon.
     * @hookName meleeSwing
          * @javaFqn noppes.npcs.api.event.INpcEvent.SwingEvent
*/
    export interface SwingEvent extends INpcEvent {
        /**
         * Can be null
         *
         * @return Returns the swung item
         */
        getItemStack(): import('../item/IItemStack').IItemStack;
        readonly itemStack: import('../item/IItemStack').IItemStack;
    }
    /**
     * Fired when the NPC kills an entity.
     * @hookName kills
          * @javaFqn noppes.npcs.api.event.INpcEvent.KilledEntityEvent
*/
    export interface KilledEntityEvent {
        /** @return the entity that was killed. */
        getEntity(): import('../entity/IEntityLivingBase').IEntityLivingBase;
        readonly entity: import('../entity/IEntityLivingBase').IEntityLivingBase;
    }
    /**
     * Fired when the NPC dies. Cancelable.
     * @hookName killed
          * @javaFqn noppes.npcs.api.event.INpcEvent.DiedEvent
*/
    export interface DiedEvent extends INpcEvent {
        /** @return the entity that killed this NPC, or null. */
        getSource(): import('../entity/IEntity').IEntity;
        /** @return the damage source details. */
        getDamageSource(): import('../IDamageSource').IDamageSource;
        /** @return the damage type string. */
        getType(): String;
        /** @param droppedItems the items to drop on death. */
        setDroppedItems(droppedItems: import('../item/IItemStack').IItemStack[]): import('./void').void;
        /** @return the items dropped on death. */
        getDroppedItems(): import('../item/IItemStack').IItemStack[];
        /** @param expDropped the experience to drop on death. */
        setExpDropped(expDropped: import('./int').int): import('./void').void;
        /** @return the experience dropped on death. */
        getExpDropped(): import('./int').int;
        readonly damageSource: import('../IDamageSource').IDamageSource;
        readonly type: String;
        readonly source: import('../entity/IEntity').IEntity;
        droppedItems: import('../item/IItemStack').IItemStack[];
        expDropped: import('./int').int;
    }
    /**
     * Fired when a player interacts with the NPC. Cancelable.
     * @hookName interact
          * @javaFqn noppes.npcs.api.event.INpcEvent.InteractEvent
*/
    export interface InteractEvent extends INpcEvent {
        /** @return the interacting player. */
        getPlayer(): import('../entity/IPlayer').IPlayer;
        readonly player: import('../entity/IPlayer').IPlayer;
    }
    /**
     * Fired when a dialog opens with the NPC. Cancelable.
     * @hookName dialog
          * @javaFqn noppes.npcs.api.event.INpcEvent.DialogEvent
*/
    export interface DialogEvent extends INpcEvent {
        /** @return the interacting player. */
        getPlayer(): import('../entity/IPlayer').IPlayer;
        /** @return the dialog. */
        getDialog(): import('../handler/data/IDialog').IDialog;
        /** @return the dialog ID. */
        getDialogId(): import('./int').int;
        /** @return the selected option ID. */
        getOptionId(): import('./int').int;
        readonly player: import('../entity/IPlayer').IPlayer;
        readonly id: import('./int').int;
        readonly optionId: import('./int').int;
        readonly dialogObj: import('../handler/data/IDialog').IDialog;
    }
    /**
     * Fired when a dialog with the NPC is closed.
     * @hookName dialogClosed
          * @javaFqn noppes.npcs.api.event.INpcEvent.DialogClosedEvent
*/
    export interface DialogClosedEvent extends INpcEvent {
        /** @return the player who closed the dialog. */
        getPlayer(): import('../entity/IPlayer').IPlayer;
        /** @return the dialog. */
        getDialog(): import('../handler/data/IDialog').IDialog;
        /** @return the dialog ID. */
        getDialogId(): import('./int').int;
        /** @return the selected option ID. */
        getOptionId(): import('./int').int;
        readonly player: import('../entity/IPlayer').IPlayer;
        readonly id: import('./int').int;
        readonly optionId: import('./int').int;
        readonly dialogObj: import('./Dialog').Dialog;
    }
    /**
     * Fired when the NPC loses its current target. Cancelable.
     * @hookName targetLost
          * @javaFqn noppes.npcs.api.event.INpcEvent.TargetLostEvent
*/
    export interface TargetLostEvent extends INpcEvent {
        /** @return the target being lost. */
        getTarget(): import('../entity/IEntityLivingBase').IEntityLivingBase;
        /** @return the new target, or null. */
        getNewTarget(): import('../entity/IEntityLivingBase').IEntityLivingBase;
        readonly oldTarget: import('../entity/IEntityLivingBase').IEntityLivingBase;
        readonly newTarget: import('../entity/IEntityLivingBase').IEntityLivingBase;
    }
    /**
     * Fired when the NPC acquires a new target. Cancelable.
     * @hookName target
          * @javaFqn noppes.npcs.api.event.INpcEvent.TargetEvent
*/
    export interface TargetEvent extends INpcEvent {
        /** @param entity the new target to set. */
        setTarget(entity: import('../entity/IEntityLivingBase').IEntityLivingBase): import('./void').void;
        /** @return the current target. */
        getTarget(): import('../entity/IEntityLivingBase').IEntityLivingBase;
        entity: import('../entity/IEntityLivingBase').IEntityLivingBase;
    }
    /**
     * Fired each tick for the NPC.
     * @hookName tick
          * @javaFqn noppes.npcs.api.event.INpcEvent.UpdateEvent
*/
    export interface UpdateEvent extends INpcEvent {
    }
    /**
     * Fired when the NPC is initialized.
     * @hookName init
          * @javaFqn noppes.npcs.api.event.INpcEvent.InitEvent
*/
    export interface InitEvent extends INpcEvent {
    }
}
