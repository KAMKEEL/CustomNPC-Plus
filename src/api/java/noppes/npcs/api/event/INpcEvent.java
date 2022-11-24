package noppes.npcs.api.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.IDialog;
import noppes.npcs.api.item.IItemStack;

public interface INpcEvent extends ICustomNPCsEvent {

    ICustomNpc getNpc();

    interface TimerEvent {
        int getId();
    }

    interface CollideEvent {
        IEntity getEntity();
    }

    @Cancelable
    interface DamagedEvent {

        IEntity getSource();

        IDamageSource getDamageSource();

        float getDamage();

        void setDamage(float damage);

        void setClearTarget(boolean bo);

        boolean getClearTarget();

        String getType();
    }

    @Cancelable
    interface RangedLaunchedEvent {
        IEntityLivingBase getTarget();

        void setDamage(float damage);

        float getDamage();
    }

    @Cancelable
    interface MeleeAttackEvent {
        IEntityLivingBase getTarget();

        void setDamage(float damage);

        float getDamage();
    }

    interface KilledEntityEvent {
        IEntityLivingBase getEntity();
    }

    @Cancelable
    interface DiedEvent {
        IEntity getSource();

        IDamageSource getDamageSource();

        String getType();

        void setDroppedItems(IItemStack[] droppedItems);

        IItemStack[] getDroppedItems();

        void setExpDropped(int expDropped);

        int getExpDropped();
    }

    @Cancelable
    interface InteractEvent {
        IPlayer getPlayer();
    }

    @Cancelable
    interface DialogEvent {
        IPlayer getPlayer();

        IDialog getDialog();

        int getDialogId();

        int getOptionId();
    }

    interface DialogClosedEvent {
        IPlayer getPlayer();

        IDialog getDialog();

        int getDialogId();

        int getOptionId();
    }

    @Cancelable
    interface TargetLostEvent {
        IEntityLivingBase getTarget();
    }

    @Cancelable
    interface TargetEvent {
        void setTarget(IEntityLivingBase entity);

        IEntityLivingBase getTarget();
    }

    interface UpdateEvent {
    }

    interface InitEvent {
    }
}
