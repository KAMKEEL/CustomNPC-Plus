/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.event
 */

export interface INpcEvent extends ICustomNPCsEvent {
    getNpc(): import('../entity/ICustomNpc').ICustomNpc;
}

export namespace INpcEvent {
    export interface TimerEvent extends INpcEvent {
        getId(): number;
    }
    export interface CollideEvent extends INpcEvent {
        getEntity(): import('../entity/IEntity').IEntity;
    }
    export interface DamagedEvent extends INpcEvent {
        getSource(): import('../entity/IEntity').IEntity;
        getDamageSource(): import('../IDamageSource').IDamageSource;
        getDamage(): number;
        setDamage(damage: number): void;
        setClearTarget(bo: boolean): void;
        getClearTarget(): boolean;
        getType(): string;
    }
    export interface RangedLaunchedEvent extends INpcEvent {
        getTarget(): IEntityLivingBase;
        setDamage(damage: number): void;
        getDamage(): number;
    }
    export interface MeleeAttackEvent extends INpcEvent {
        getTarget(): IEntityLivingBase;
        setDamage(damage: number): void;
        getDamage(): number;
    }
    export interface SwingEvent extends INpcEvent {
        getItemStack(): import('../item/IItemStack').IItemStack;
    }
    export interface KilledEntityEvent extends INpcEvent {
        getEntity(): IEntityLivingBase;
    }
    export interface DiedEvent extends INpcEvent {
        getSource(): import('../entity/IEntity').IEntity;
        getDamageSource(): import('../IDamageSource').IDamageSource;
        getType(): string;
        setDroppedItems(droppedItems: import('../item/IItemStack').IItemStack[]): void;
        getDroppedItems(): import('../item/IItemStack').IItemStack[];
        setExpDropped(expDropped: number): void;
        getExpDropped(): number;
    }
    export interface InteractEvent extends INpcEvent {
        getPlayer(): import('../entity/IPlayer').IPlayer;
    }
    export interface DialogEvent extends INpcEvent {
        getPlayer(): import('../entity/IPlayer').IPlayer;
        getDialog(): import('../handler/data/IDialog').IDialog;
        getDialogId(): number;
        getOptionId(): number;
    }
    export interface DialogClosedEvent extends INpcEvent {
        getPlayer(): import('../entity/IPlayer').IPlayer;
        getDialog(): import('../handler/data/IDialog').IDialog;
        getDialogId(): number;
        getOptionId(): number;
    }
    export interface TargetLostEvent extends INpcEvent {
        getTarget(): IEntityLivingBase;
        getNewTarget(): IEntityLivingBase;
    }
    export interface TargetEvent extends INpcEvent {
        setTarget(entity: IEntityLivingBase): void;
        getTarget(): IEntityLivingBase;
    }
    export interface UpdateEvent extends INpcEvent {
    }
    export interface InitEvent extends INpcEvent {
    }

}
