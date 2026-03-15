package noppes.npcs.api.event;

import noppes.npcs.api.Cancelable;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.item.IItemCustomizable;
import noppes.npcs.api.item.IItemStack;

/**
 * Events fired for scripted/customizable items during their lifecycle and interactions.
 */
public interface IItemEvent extends ICustomNPCsEvent {

    /** @return the customizable item associated with this event. */
    IItemCustomizable getItem();

    /**
     * Fired when the item is initialized.
     * @hookName init
     */
    interface InitEvent extends IItemEvent {
    }

    /**
     * Fired each tick while the item exists in an entity's inventory.
     * @hookName tick
     */
    interface UpdateEvent extends IItemEvent {
        /** @return the entity holding the item. */
        IEntity getEntity();
    }

    /**
     * Fired when the item is tossed/dropped. Cancelable.
     * @hookName tossed
     */
    @Cancelable
    interface TossedEvent extends IItemEvent {
        /** @return the dropped item entity. */
        IEntity getEntity();

        /** @return the player who dropped the item. */
        IPlayer getPlayer();
    }

    /**
     * Fired when the item is picked up.
     * @hookName pickedUp
     */
    interface PickedUpEvent extends IItemEvent {
        /** @return the player who picked up the item. */
        IPlayer getPlayer();
    }

    /**
     * Fired when the item entity spawns in the world. Cancelable.
     * @hookName spawn
     */
    @Cancelable
    interface SpawnEvent extends IItemEvent {
        /** @return the spawned item entity. */
        IEntity getEntity();
    }

    /**
     * Fired when the item is used to interact with an entity. Cancelable.
     * @hookName interact
     */
    @Cancelable
    interface InteractEvent extends IItemEvent {
        /** @return the interaction type. */
        int getType();

        /** @return the target entity. */
        IEntity getTarget();

        /** @return the player performing the interaction. */
        IPlayer getPlayer();
    }

    /**
     * Fired when the item is right-clicked. Cancelable.
     * @hookName rightClick
     */
    @Cancelable
    interface RightClickEvent extends IItemEvent {
        /** @return the click type. */
        int getType();

        /** @return the target (entity or block). */
        Object getTarget();

        /** @return the player who right-clicked. */
        IPlayer getPlayer();
    }

    /**
     * Fired when the item is used to attack an entity. Cancelable.
     * @hookName attack
     */
    @Cancelable
    interface AttackEvent extends IItemEvent {
        /** @return the attack type. */
        int getType();

        /** @return the entity being attacked. */
        IEntity getTarget();

        /** @return the entity swinging the item. */
        IEntity getSwingingEntity();
    }

    /**
     * Fired when the player starts using the item (e.g., drawing a bow).
     * @hookName startItem
     */
    interface StartUsingItem extends IItemEvent {
        /** @return the player using the item. */
        IPlayer getPlayer();

        /** @return the use duration in ticks. */
        int getDuration();
    }

    /**
     * Fired each tick while the player is using the item.
     * @hookName usingItem
     */
    interface UsingItem extends IItemEvent {
        /** @return the player using the item. */
        IPlayer getPlayer();

        /** @return the remaining use duration in ticks. */
        int getDuration();
    }

    /**
     * Fired when the player stops using the item before completion.
     * @hookName stopItem
     */
    interface StopUsingItem extends IItemEvent {
        /** @return the player who stopped using the item. */
        IPlayer getPlayer();

        /** @return the remaining use duration in ticks. */
        int getDuration();
    }

    /**
     * Fired when the player finishes using the item (full duration).
     * @hookName finishItem
     */
    interface FinishUsingItem extends IItemEvent {
        /** @return the player who finished using the item. */
        IPlayer getPlayer();

        /** @return the total use duration in ticks. */
        int getDuration();
    }

    /**
     * Fired when the item breaks due to durability loss or consumption.
     * @hookName breakItem
     */
    interface BreakItem extends IItemEvent {
        /** @return the item stack that broke. */
        IItemStack getBrokenStack();

        /** @return the player whose item broke. */
        IPlayer getPlayer();
    }

    /**
     * Fired when the item is repaired on an anvil.
     * @hookName repairItem
     */
    interface RepairItem extends IItemEvent {
        /** @return the left input item. */
        IItemStack getLeft();

        /** @return the right input item (repair material). */
        IItemStack getRight();

        /** @return the resulting output item. */
        IItemStack getOutput();

        /** @return the chance the anvil breaks after this repair. */
        float getAnvilBreakChance();
    }
}
