/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.event
 */

export interface IPlayerEvent extends ICustomNPCsEvent {
    getPlayer(): import('../entity/IPlayer').IPlayer;
}

export namespace IPlayerEvent {
    export interface ChatEvent extends IPlayerEvent {
        setMessage(message: string): void;
        getMessage(): string;
    }
    export interface KeyPressedEvent extends IPlayerEvent {
        getKey(): number;
        isCtrlPressed(): boolean;
        isAltPressed(): boolean;
        isShiftPressed(): boolean;
        isMetaPressed(): boolean;
        keyDown(): boolean;
        getKeysDown(): number[];
    }
    export interface MouseClickedEvent extends IPlayerEvent {
        getButton(): number;
        getMouseWheel(): number;
        buttonDown(): boolean;
        isCtrlPressed(): boolean;
        isAltPressed(): boolean;
        isShiftPressed(): boolean;
        isMetaPressed(): boolean;
        getKeysDown(): number[];
    }
    export interface PickupXPEvent extends IPlayerEvent {
        getAmount(): number;
    }
    export interface LevelUpEvent extends IPlayerEvent {
        getChange(): number;
    }
    export type LogoutEvent = IPlayerEvent
    export type LoginEvent = IPlayerEvent
    export type RespawnEvent = IPlayerEvent
    export interface ChangedDimension extends IPlayerEvent {
        getFromDim(): number;
        getToDim(): number;
    }
    export interface TimerEvent extends IPlayerEvent {
        getId(): number;
    }
    export interface AttackedEvent extends IPlayerEvent {
        getDamageSource(): import('../IDamageSource').IDamageSource;
        getSource(): import('../entity/IEntity').IEntity;
        getDamage(): number;
    }
    export interface DamagedEvent extends IPlayerEvent {
        getDamageSource(): import('../IDamageSource').IDamageSource;
        getSource(): import('../entity/IEntity').IEntity;
        getDamage(): number;
    }
    export type LightningEvent = IPlayerEvent
    export interface SoundEvent extends IPlayerEvent {
        getName(): string;
        getPitch(): number;
        getVolume(): number;
    }
    export interface FallEvent extends IPlayerEvent {
        getDistance(): number;
    }
    export type JumpEvent = IPlayerEvent
    export interface KilledEntityEvent extends IPlayerEvent {
        getEntity(): IEntityLivingBase;
    }
    export interface DiedEvent extends IPlayerEvent {
        getDamageSource(): import('../IDamageSource').IDamageSource;
        getType(): string;
        getSource(): import('../entity/IEntity').IEntity;
    }
    export interface RangedLaunchedEvent extends IPlayerEvent {
        getBow(): import('../item/IItemStack').IItemStack;
        getCharge(): number;
    }
    export interface AttackEvent extends IPlayerEvent {
        getDamageSource(): import('../IDamageSource').IDamageSource;
        getTarget(): import('../entity/IEntity').IEntity;
        getDamage(): number;
    }
    export interface DamagedEntityEvent extends IPlayerEvent {
        getDamageSource(): import('../IDamageSource').IDamageSource;
        getTarget(): import('../entity/IEntity').IEntity;
        getDamage(): number;
    }
    export interface ContainerClosed extends IPlayerEvent {
        getContainer(): import('../IContainer').IContainer;
    }
    export interface ContainerOpen extends IPlayerEvent {
        getContainer(): import('../IContainer').IContainer;
    }
    export interface PickUpEvent extends IPlayerEvent {
        getItem(): import('../item/IItemStack').IItemStack;
    }
    export interface DropEvent extends IPlayerEvent {
        getItems(): import('../item/IItemStack').IItemStack[];
    }
    export interface TossEvent extends IPlayerEvent {
        getItem(): import('../item/IItemStack').IItemStack;
    }
    export interface InteractEvent extends IPlayerEvent {
        getType(): number;
        getTarget(): import('../entity/IEntity').IEntity;
    }
    export interface RightClickEvent extends IPlayerEvent {
        getType(): number;
        getTarget(): any;

    }
    export type UpdateEvent = IPlayerEvent
    export type InitEvent = IPlayerEvent
    export interface StartUsingItem extends IPlayerEvent {
        getItem(): import('../item/IItemStack').IItemStack;
        getDuration(): number;
    }
    export interface UsingItem extends IPlayerEvent {
        getItem(): import('../item/IItemStack').IItemStack;
        getDuration(): number;
    }
    export interface StopUsingItem extends IPlayerEvent {
        getItem(): import('../item/IItemStack').IItemStack;
        getDuration(): number;
    }
    export interface FinishUsingItem extends IPlayerEvent {
        getItem(): import('../item/IItemStack').IItemStack;
        getDuration(): number;
    }
    export interface BreakEvent extends IPlayerEvent {
        getBlock(): import('../IBlock').IBlock;
        getExp(): number;
    }
    export interface UseHoeEvent extends IPlayerEvent {
        getHoe(): import('../item/IItemStack').IItemStack;
        getX(): number;
        getY(): number;
        getZ(): number;
    }
    export interface WakeUpEvent extends IPlayerEvent {
        setSpawn(): boolean;
    }
    export interface SleepEvent extends IPlayerEvent {
        getX(): number;
        getY(): number;
        getZ(): number;
    }
    export interface AchievementEvent extends IPlayerEvent {
        getDescription(): string;
    }
    export interface FillBucketEvent extends IPlayerEvent {
        getCurrent(): import('../item/IItemStack').IItemStack;
        getFilled(): import('../item/IItemStack').IItemStack;
    }
    export interface BonemealEvent extends IPlayerEvent {
        getBlock(): import('../IBlock').IBlock;
        getX(): number;
        getY(): number;
        getZ(): number;
    }
    export type RangedChargeEvent = IPlayerEvent
    export interface EffectEvent extends IPlayerEvent {
        getEffect(): import('../handler/data/IPlayerEffect').IPlayerEffect;
    }
    export namespace EffectEvent {
        export type Added = EffectEvent
        export type Ticked = EffectEvent
        export interface Removed extends EffectEvent {
            hasTimerRunOut(): boolean;
            causedByDeath(): boolean;
        }
    }

    export interface ProfileEvent extends IPlayerEvent {
        getProfile(): import('../handler/data/IProfile').IProfile;
        getSlot(): number;
        isPost(): boolean;
    }
    export namespace ProfileEvent {
        export interface Changed extends ProfileEvent {
            getPrevSlot(): number;
        }
        export type Create = ProfileEvent
        export type Removed = ProfileEvent
    }

}
