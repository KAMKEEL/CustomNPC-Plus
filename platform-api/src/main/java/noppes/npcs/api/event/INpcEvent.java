package noppes.npcs.api.event;

import noppes.npcs.api.Cancelable;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.IDialog;
import noppes.npcs.api.item.IItemStack;

/**
 * Events fired for CustomNPC entities during their lifecycle and interactions.
 */
public interface INpcEvent extends ICustomNPCsEvent {

    /** @return the NPC associated with this event. */
    ICustomNpc getNpc();

    /**
     * Fired when an NPC timer triggers.
     * @hookName timer
     */
    interface TimerEvent extends INpcEvent {
        /** @return the timer ID. */
        int getId();
    }

    /**
     * Fired when an entity collides with the NPC.
     * @hookName collide
     */
    interface CollideEvent extends INpcEvent {
        /** @return the colliding entity. */
        IEntity getEntity();
    }

    /**
     * Fired when the NPC takes damage. Cancelable.
     * @hookName damaged
     */
    @Cancelable
    interface DamagedEvent extends INpcEvent {
        /** @return the entity that caused the damage, or null. */
        IEntity getSource();

        /** @return the damage source details. */
        IDamageSource getDamageSource();

        /** @return the damage amount. */
        float getDamage();

        /** @param damage the new damage amount. */
        void setDamage(float damage);

        /** @param bo whether to clear the NPC's current target after damage. */
        void setClearTarget(boolean bo);

        /** @return whether the NPC's target will be cleared after damage. */
        boolean getClearTarget();

        /** @return the damage type string. */
        String getType();
    }

    /**
     * Fired when the NPC launches a ranged attack. Cancelable.
     * @hookName rangedLaunched
     */
    @Cancelable
    interface RangedLaunchedEvent extends INpcEvent {
        /** @return the target entity. */
        IEntityLivingBase getTarget();

        /** @param damage the new ranged damage. */
        void setDamage(float damage);

        /** @return the ranged damage. */
        float getDamage();
    }

    /**
     * Fired when the NPC performs a melee attack. Cancelable.
     * @hookName meleeAttack
     */
    @Cancelable
    interface MeleeAttackEvent extends INpcEvent {
        /** @return the target entity. */
        IEntityLivingBase getTarget();

        /** @param damage the new melee damage. */
        void setDamage(float damage);

        /** @return the melee damage. */
        float getDamage();
    }

    /**
     * Fired when the NPC swings its weapon.
     * @hookName meleeSwing
     */
    interface SwingEvent extends INpcEvent {

        /**
         * Can be null
         *
         * @return Returns the swung item
         */
        IItemStack getItemStack();
    }

    /**
     * Fired when the NPC kills an entity.
     * @hookName kills
     */
    interface KilledEntityEvent {
        /** @return the entity that was killed. */
        IEntityLivingBase getEntity();
    }

    /**
     * Fired when the NPC dies. Cancelable.
     * @hookName killed
     */
    @Cancelable
    interface DiedEvent extends INpcEvent {
        /** @return the entity that killed this NPC, or null. */
        IEntity getSource();

        /** @return the damage source details. */
        IDamageSource getDamageSource();

        /** @return the damage type string. */
        String getType();

        /** @param droppedItems the items to drop on death. */
        void setDroppedItems(IItemStack[] droppedItems);

        /** @return the items dropped on death. */
        IItemStack[] getDroppedItems();

        /** @param expDropped the experience to drop on death. */
        void setExpDropped(int expDropped);

        /** @return the experience dropped on death. */
        int getExpDropped();
    }

    /**
     * Fired when a player interacts with the NPC. Cancelable.
     * @hookName interact
     */
    @Cancelable
    interface InteractEvent extends INpcEvent {
        /** @return the interacting player. */
        IPlayer getPlayer();
    }

    /**
     * Fired when a dialog opens with the NPC. Cancelable.
     * @hookName dialog
     */
    @Cancelable
    interface DialogEvent extends INpcEvent {
        /** @return the interacting player. */
        IPlayer getPlayer();

        /** @return the dialog. */
        IDialog getDialog();

        /** @return the dialog ID. */
        int getDialogId();

        /** @return the selected option ID. */
        int getOptionId();
    }

    /**
     * Fired when a dialog with the NPC is closed.
     * @hookName dialogClosed
     */
    interface DialogClosedEvent extends INpcEvent {
        /** @return the player who closed the dialog. */
        IPlayer getPlayer();

        /** @return the dialog. */
        IDialog getDialog();

        /** @return the dialog ID. */
        int getDialogId();

        /** @return the selected option ID. */
        int getOptionId();
    }

    /**
     * Fired when the NPC loses its current target. Cancelable.
     * @hookName targetLost
     */
    @Cancelable
    interface TargetLostEvent extends INpcEvent {
        /** @return the target being lost. */
        IEntityLivingBase getTarget();

        /** @return the new target, or null. */
        IEntityLivingBase getNewTarget();
    }

    /**
     * Fired when the NPC acquires a new target. Cancelable.
     * @hookName target
     */
    @Cancelable
    interface TargetEvent extends INpcEvent {
        /** @param entity the new target to set. */
        void setTarget(IEntityLivingBase entity);

        /** @return the current target. */
        IEntityLivingBase getTarget();
    }

    /**
     * Fired each tick for the NPC.
     * @hookName tick
     */
    interface UpdateEvent extends INpcEvent {
    }

    /**
     * Fired when the NPC is initialized.
     * @hookName init
     */
    interface InitEvent extends INpcEvent {
    }
}
