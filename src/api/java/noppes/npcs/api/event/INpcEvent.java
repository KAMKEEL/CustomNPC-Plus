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

    interface TimerEvent extends INpcEvent {
        int getId();
    }

    interface CollideEvent extends INpcEvent{
        IEntity getEntity();
    }

    @Cancelable
    interface DamagedEvent extends INpcEvent {

        IEntity getSource();

        IDamageSource getDamageSource();

        float getDamage();

        void setDamage(float damage);

        void setClearTarget(boolean bo);

        boolean getClearTarget();

        String getType();
    }

    @Cancelable
    interface RangedLaunchedEvent extends INpcEvent {
        IEntityLivingBase getTarget();

        void setDamage(float damage);

        float getDamage();
    }

    @Cancelable
    interface MeleeAttackEvent extends INpcEvent {
        IEntityLivingBase getTarget();

        void setDamage(float damage);

        float getDamage();
    }

    interface KilledEntityEvent {
        IEntityLivingBase getEntity();
    }

    @Cancelable
    interface DiedEvent extends INpcEvent {
        IEntity getSource();

        IDamageSource getDamageSource();

        String getType();

        void setDroppedItems(IItemStack[] droppedItems);

        IItemStack[] getDroppedItems();

        void setExpDropped(int expDropped);

        int getExpDropped();
    }

    @Cancelable
    interface InteractEvent extends INpcEvent {
        IPlayer getPlayer();
    }

    @Cancelable
    interface DialogEvent extends INpcEvent {
        IPlayer getPlayer();

        IDialog getDialog();

        int getDialogId();

        int getOptionId();
    }

    interface DialogClosedEvent extends INpcEvent {
        IPlayer getPlayer();

        IDialog getDialog();

        int getDialogId();

        int getOptionId();
    }

    @Cancelable
    interface TargetLostEvent extends INpcEvent {
        IEntityLivingBase getTarget();
    }

    @Cancelable
    interface TargetEvent extends INpcEvent {
        void setTarget(IEntityLivingBase entity);

        IEntityLivingBase getTarget();
    }

    interface UpdateEvent extends INpcEvent {
    }

    interface InitEvent extends INpcEvent {
    }
}
