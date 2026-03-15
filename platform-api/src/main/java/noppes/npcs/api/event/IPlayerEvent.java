package noppes.npcs.api.event;

import noppes.npcs.api.Cancelable;
import noppes.npcs.api.IBlock;
import noppes.npcs.api.IContainer;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.IPlayerEffect;
import noppes.npcs.api.handler.data.IProfile;
import noppes.npcs.api.item.IItemStack;

/**
 * Events fired for player actions including combat, items, movement, and interactions.
 */
public interface IPlayerEvent extends ICustomNPCsEvent {

    /** @return the player associated with this event. */
    IPlayer getPlayer();

    /**
     * Fired when the player sends a chat message. Cancelable.
     * @hookName chat
     */
    @Cancelable
    interface ChatEvent extends IPlayerEvent {
        /** @param message the new chat message. */
        void setMessage(String message);

        /** @return the chat message. */
        String getMessage();
    }

    /**
     * Fired when the player presses or releases a key.
     * @hookName keyPressed
     */
    interface KeyPressedEvent extends IPlayerEvent {
        /** @return the key code. */
        int getKey();

        /** @return true if Ctrl is held. */
        boolean isCtrlPressed();

        /** @return true if Alt is held. */
        boolean isAltPressed();

        /** @return true if Shift is held. */
        boolean isShiftPressed();

        /** @return true if Meta is held. */
        boolean isMetaPressed();

        /** @return true if the key is being pressed down, false if released. */
        boolean keyDown();

        /** @return array of currently held key codes. */
        int[] getKeysDown();
    }

    /**
     * Fired when the player clicks a mouse button.
     * @hookName mouseClicked
     */
    interface MouseClickedEvent extends IPlayerEvent {
        /** @return the mouse button index. */
        int getButton();

        /** @return the mouse wheel scroll delta. */
        int getMouseWheel();

        /** @return true if the button is being pressed down. */
        boolean buttonDown();

        /** @return true if Ctrl is held. */
        boolean isCtrlPressed();

        /** @return true if Alt is held. */
        boolean isAltPressed();

        /** @return true if Shift is held. */
        boolean isShiftPressed();

        /** @return true if Meta is held. */
        boolean isMetaPressed();

        /** @return array of currently held key codes. */
        int[] getKeysDown();
    }

    /**
     * Fired when the player picks up experience orbs.
     * @hookName pickupXP
     */
    interface PickupXPEvent extends IPlayerEvent {
        /** @return the amount of XP picked up. */
        int getAmount();
    }

    /**
     * Fired when the player levels up.
     * @hookName levelUp
     */
    interface LevelUpEvent extends IPlayerEvent {
        /** @return the number of levels gained. */
        int getChange();
    }

    /**
     * Fired when the player logs out.
     * @hookName logout
     */
    interface LogoutEvent extends IPlayerEvent {
    }

    /**
     * Fired when the player logs in.
     * @hookName login
     */
    interface LoginEvent extends IPlayerEvent {
    }

    /**
     * Fired when the player respawns after death.
     * @hookName respawn
     */
    interface RespawnEvent extends IPlayerEvent {
    }

    /**
     * Fired when the player changes dimensions.
     * @hookName changedDim
     */
    interface ChangedDimension extends IPlayerEvent {
        /** @return the dimension ID the player came from. */
        int getFromDim();

        /** @return the dimension ID the player traveled to. */
        int getToDim();
    }

    /**
     * Fired when a player timer triggers.
     * @hookName timer
     */
    interface TimerEvent extends IPlayerEvent {
        /** @return the timer ID. */
        int getId();
    }

    /**
     * Fired when the player is attacked (before armor reduction). Cancelable.
     * @hookName attacked
     */
    @Cancelable
    interface AttackedEvent extends IPlayerEvent {
        /** @return the damage source details. */
        IDamageSource getDamageSource();

        /** @return the attacking entity, or null. */
        IEntity getSource();

        /** @return the raw damage amount. */
        float getDamage();
    }

    /**
     * Fired when the player takes damage (after armor reduction). Cancelable.
     * @hookName damaged
     */
    @Cancelable
    interface DamagedEvent extends IPlayerEvent {
        /** @return the damage source details. */
        IDamageSource getDamageSource();

        /** @return the damaging entity, or null. */
        IEntity getSource();

        /** @return the damage amount after reduction. */
        float getDamage();
    }

    /**
     * Fired when the player is about to be struck by lightning. Cancelable.
     * @hookName lightning
     */
    @Cancelable
    interface LightningEvent extends IPlayerEvent {
    }

    /**
     * Fired when a sound is played for the player. Cancelable.
     * @hookName playSound
     */
    @Cancelable
    interface SoundEvent extends IPlayerEvent {
        /** @return the sound resource name. */
        String getName();

        /** @return the sound pitch. */
        float getPitch();

        /** @return the sound volume. */
        float getVolume();
    }

    /**
     * Fired when the player falls. Cancelable.
     * @hookName fall
     */
    @Cancelable
    interface FallEvent extends IPlayerEvent {
        /** @return the fall distance in blocks. */
        float getDistance();
    }

    /**
     * Fired when the player jumps.
     * @hookName jump
     */
    interface JumpEvent extends IPlayerEvent {
    }

    /**
     * Fired when the player kills an entity.
     * @hookName kills
     */
    interface KilledEntityEvent extends IPlayerEvent {
        /** @return the killed entity. */
        IEntityLivingBase getEntity();
    }

    /**
     * Fired when the player dies.
     * @hookName killed
     */
    interface DiedEvent extends IPlayerEvent {
        /** @return the damage source that killed the player. */
        IDamageSource getDamageSource();

        /** @return the damage type string. */
        String getType();

        /** @return the entity that killed the player, or null. */
        IEntity getSource();
    }

    /**
     * Fired when the player launches a ranged weapon (e.g., bow). Cancelable.
     * @hookName rangedLaunched
     */
    @Cancelable
    interface RangedLaunchedEvent extends IPlayerEvent {
        /** @return the bow item. */
        IItemStack getBow();

        /** @return the charge level of the bow. */
        int getCharge();
    }

    /**
     * Fired when the player attacks an entity. Cancelable.
     * @hookName attack
     */
    @Cancelable
    interface AttackEvent extends IPlayerEvent {
        /** @return the damage source details. */
        IDamageSource getDamageSource();

        /** @return the target entity. */
        IEntity getTarget();

        /** @return the attack damage. */
        float getDamage();
    }

    /**
     * Fired when the player deals damage to an entity (after hit). Cancelable.
     * @hookName damagedEntity
     */
    @Cancelable
    interface DamagedEntityEvent extends IPlayerEvent {
        /** @return the damage source details. */
        IDamageSource getDamageSource();

        /** @return the damaged entity. */
        IEntity getTarget();

        /** @return the damage dealt. */
        float getDamage();
    }

    /**
     * Fired when the player closes a container.
     * @hookName containerClosed
     */
    interface ContainerClosed extends IPlayerEvent {
        /** @return the closed container. */
        IContainer getContainer();
    }

    /**
     * Fired when the player opens a container.
     * @hookName containerOpen
     */
    interface ContainerOpen extends IPlayerEvent {
        /** @return the opened container. */
        IContainer getContainer();
    }

    /**
     * Fired when the player picks up an item. Cancelable.
     * @hookName pickUp
     */
    @Cancelable
    interface PickUpEvent extends IPlayerEvent {
        /** @return the picked up item. */
        IItemStack getItem();
    }

    /**
     * Fired when items are dropped from the player's inventory (e.g., on death). Cancelable.
     * @hookName drop
     */
    @Cancelable
    interface DropEvent extends IPlayerEvent {
        /** @return the dropped items. */
        IItemStack[] getItems();
    }

    /**
     * Fired when the player tosses a single item. Cancelable.
     * @hookName toss
     */
    @Cancelable
    interface TossEvent extends IPlayerEvent {
        /** @return the tossed item. */
        IItemStack getItem();
    }

    /**
     * Fired when the player interacts with an entity. Cancelable.
     * @hookName interact
     */
    @Cancelable
    interface InteractEvent extends IPlayerEvent {
        /** @return the interaction type. */
        int getType();

        /** @return the target entity. */
        IEntity getTarget();
    }

    /**
     * Fired when the player right-clicks. Cancelable.
     * @hookName rightClick
     */
    @Cancelable
    interface RightClickEvent extends IPlayerEvent {
        /** @return the click type. */
        int getType();

        /** @return the target (entity, block, or null). */
        Object getTarget();

        /** @return the player. */
        IPlayer getPlayer();
    }

    /**
     * Fired each tick for the player.
     * @hookName tick
     */
    interface UpdateEvent extends IPlayerEvent {
    }

    /**
     * Fired when the player script is initialized.
     * @hookName init
     */
    interface InitEvent extends IPlayerEvent {
    }

    /**
     * Fired when the player starts using an item (e.g., drawing a bow).
     * @hookName startItem
     */
    interface StartUsingItem extends IPlayerEvent {
        /** @return the item being used. */
        IItemStack getItem();

        /** @return the use duration in ticks. */
        int getDuration();
    }

    /**
     * Fired each tick while the player is using an item.
     * @hookName usingItem
     */
    interface UsingItem extends IPlayerEvent {
        /** @return the item being used. */
        IItemStack getItem();

        /** @return the remaining use duration in ticks. */
        int getDuration();
    }

    /**
     * Fired when the player stops using an item before completion.
     * @hookName stopItem
     */
    interface StopUsingItem extends IPlayerEvent {
        /** @return the item that was being used. */
        IItemStack getItem();

        /** @return the remaining use duration in ticks. */
        int getDuration();
    }

    /**
     * Fired when the player finishes using an item (full duration).
     * @hookName finishItem
     */
    interface FinishUsingItem extends IPlayerEvent {
        /** @return the item that was used. */
        IItemStack getItem();

        /** @return the total use duration in ticks. */
        int getDuration();
    }

    /**
     * Fired when the player breaks a block. Cancelable.
     * @hookName breakBlock
     */
    @Cancelable
    interface BreakEvent extends IPlayerEvent {
        /** @return the block being broken. */
        IBlock getBlock();

        /** @return the experience dropped from breaking the block. */
        int getExp();
    }

    /**
     * Fired when the player uses a hoe on a block.
     * @hookName useHoe
     */
    interface UseHoeEvent extends IPlayerEvent {
        /** @return the hoe item. */
        IItemStack getHoe();

        /** @return the X coordinate of the hoed block. */
        int getX();

        /** @return the Y coordinate of the hoed block. */
        int getY();

        /** @return the Z coordinate of the hoed block. */
        int getZ();
    }

    /**
     * Fired when the player wakes up from a bed.
     * @hookName wakeUp
     */
    interface WakeUpEvent extends IPlayerEvent {
        /** @return true if the bed position was set as spawn. */
        boolean setSpawn();
    }

    /**
     * Fired when the player sleeps in a bed.
     * @hookName sleep
     */
    interface SleepEvent extends IPlayerEvent {
        /** @return the X coordinate of the bed. */
        int getX();

        /** @return the Y coordinate of the bed. */
        int getY();

        /** @return the Z coordinate of the bed. */
        int getZ();
    }

    /**
     * Fired when the player earns an achievement.
     * @hookName achievement
     */
    interface AchievementEvent extends IPlayerEvent {
        /** @return the achievement description. */
        String getDescription();
    }

    /**
     * Fired when the player fills a bucket.
     * @hookName fillBucket
     */
    interface FillBucketEvent extends IPlayerEvent {
        /** @return the empty bucket item. */
        IItemStack getCurrent();

        /** @return the filled bucket item. */
        IItemStack getFilled();
    }

    /**
     * Fired when the player uses bonemeal on a block.
     * @hookName bonemeal
     */
    interface BonemealEvent extends IPlayerEvent {
        /** @return the block that was bonemealed. */
        IBlock getBlock();

        /** @return the X coordinate. */
        int getX();

        /** @return the Y coordinate. */
        int getY();

        /** @return the Z coordinate. */
        int getZ();
    }

    /**
     * Fired when the player begins charging a ranged weapon.
     * @hookName rangedCharge
     */
    interface RangedChargeEvent extends IPlayerEvent {
    }

    /**
     * Events fired for player custom effects (add, tick, remove).
     * @hookName onEffect
     */
    interface EffectEvent extends IPlayerEvent {

        /** @return the player effect. */
        IPlayerEffect getEffect();

        /**
         * Fired when an effect is added to the player.
         * @hookName onEffectAdd
         */
        interface Added extends EffectEvent {

        }

        /**
         * Fired each tick while the effect is active.
         * @hookName onEffectTick
         */
        interface Ticked extends EffectEvent {

        }

        /**
         * Fired when an effect is removed from the player.
         * @hookName onEffectRemove
         */
        interface Removed extends EffectEvent {

            /**
             * @return If the effect timer has ticked down to 0.
             */
            boolean hasTimerRunOut();

            /**
             * @return If the effect was removed on death.
             */
            boolean causedByDeath();
        }

    }

    /**
     * Events fired for profile operations (create, change, remove). Cancelable.
     * @hookName profile
     */
    @Cancelable
    interface ProfileEvent extends IPlayerEvent {

        /**
         * @return IProfile Object of the Operation
         */
        IProfile getProfile();

        /**
         * @return Slot ID in question
         */
        int getSlot();

        /**
         * @return returns true if it occurs after the operation (not cancellable)
         */
        boolean isPost();

        /**
         * Fired when the player switches profile slots. Cancelable.
         * @hookName profileChange
         */
        @Cancelable
        interface Changed extends ProfileEvent {
            /**
             * @return The previous slot before the switch
             */
            int getPrevSlot();
        }

        /**
         * Fired when a new profile is created. Cancelable.
         * @hookName profileCreate
         */
        @Cancelable
        interface Create extends ProfileEvent {
        }

        /**
         * Fired when a profile is removed. Cancelable.
         * @hookName profileRemove
         */
        @Cancelable
        interface Removed extends ProfileEvent {
        }
    }
}
