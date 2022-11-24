package noppes.npcs.api.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import noppes.npcs.api.IBlock;
import noppes.npcs.api.IContainer;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.item.IItemStack;

public interface IPlayerEvent extends ICustomNPCsEvent {

    IPlayer getPlayer();

    @Cancelable
    interface ChatEvent {
        void setMessage(String message);

        String getMessage();
    }

    interface KeyPressedEvent {
        int getKey();

        boolean isCtrlPressed();

        boolean isAltPressed();

        boolean isShiftPressed();

        boolean isMetaPressed();

        boolean keyDown();

        int[] getKeysDown();
    }

    interface MouseClickedEvent {
        int getButton();

        int getMouseWheel();

        boolean buttonDown();

        boolean isCtrlPressed();

        boolean isAltPressed();

        boolean isShiftPressed();

        boolean isMetaPressed();

        int[] getKeysDown();
    }

    interface PickupXPEvent {
        int getAmount();
    }

    interface LevelUpEvent {
        int getChange();
    }

    interface LogoutEvent {
    }

    interface LoginEvent {
    }

    interface RespawnEvent {
    }

    interface ChangedDimension {
        int getFromDim();

        int getToDim();
    }

    interface TimerEvent {
        int getId();
    }

    @Cancelable
    interface AttackedEvent {
        IDamageSource getDamageSource();

        IEntity getSource();

        float getDamage();
    }

    @Cancelable
    interface DamagedEvent {
        IDamageSource getDamageSource();

        IEntity getSource();

        float getDamage();
    }

    @Cancelable
    interface LightningEvent {
    }

    @Cancelable
    interface SoundEvent {
        String getName();

        float getPitch();

        float getVolume();
    }

    @Cancelable
    interface FallEvent {
        float getDistance();
    }

    interface JumpEvent {
    }

    interface KilledEntityEvent {
        IEntityLivingBase getEntity();
    }

    interface DiedEvent {
        IDamageSource getDamageSource();

        String getType();

        IEntity getSource();
    }

    @Cancelable
    interface RangedLaunchedEvent {
        IItemStack getBow();

        int getCharge();
    }

    @Cancelable
    interface AttackEvent {
        IDamageSource getDamageSource();

        IEntity getTarget();

        float getDamage();
    }

    @Cancelable
    interface DamagedEntityEvent {
        IDamageSource getDamageSource();

        IEntity getTarget();

        float getDamage();
    }

    interface ContainerClosed {
        IContainer getContainer();
    }

    interface ContainerOpen {
        IContainer getContainer();
    }

    @Cancelable
    interface PickUpEvent {
        IItemStack getItem();
    }

    @Cancelable
    interface DropEvent {
        IItemStack[] getItems();
    }

    @Cancelable
    interface TossEvent {
        IItemStack getItem();
    }

    @Cancelable
    interface InteractEvent {
        int getType();

        IEntity getTarget();
    }

    interface UpdateEvent {
    }

    interface InitEvent {
    }

    interface StartUsingItem {
        IItemStack getItem();

        int getDuration();
    }

    interface UsingItem {
        IItemStack getItem();

        int getDuration();
    }

    interface StopUsingItem {
        IItemStack getItem();

        int getDuration();
    }

    interface FinishUsingItem {
        IItemStack getItem();

        int getDuration();
    }

    @Cancelable
    interface BreakEvent {
        IBlock getBlock();

        int getExp();
    }

    interface UseHoeEvent {
        IItemStack getHoe();

        int getX();

        int getY();

        int getZ();
    }

    interface WakeUpEvent {
        boolean setSpawn();
    }

    interface SleepEvent {
        int getX();

        int getY();

        int getZ();
    }

    interface AchievementEvent {
        String getDescription();
    }

    interface FillBucketEvent {
        IItemStack getCurrent() ;

        IItemStack getFilled();
    }

    interface BonemealEvent {
        IBlock getBlock();

        int getX();

        int getY();

        int getZ();
    }

    interface RangedChargeEvent {
    }
}
