package noppes.npcs.api.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.item.IItemCustom;

public interface IItemEvent extends ICustomNPCsEvent {

    IItemCustom getItem();

    interface InitEvent {
    }

    interface UpdateEvent {
        IEntity getEntity();
    }

    @Cancelable
    interface TossedEvent {
        IEntity getEntity();

        IPlayer getPlayer();
    }

    interface PickedUpEvent {
        IPlayer getPlayer();
    }

    @Cancelable
    interface SpawnEvent {
        IEntity getEntity();
    }

    @Cancelable
    interface InteractEvent {
        int getType();

        IEntity getTarget();

        IPlayer getPlayer();
    }

    @Cancelable
    interface AttackEvent {
        int getType();

        IEntity getTarget();

        IEntity getSwingingEntity();
    }

    interface StartUsingItem {
        IPlayer getPlayer();

        int getDuration();
    }

    interface UsingItem {
        IPlayer getPlayer();

        int getDuration();
    }

    interface StopUsingItem {
        IPlayer getPlayer();

        int getDuration();
    }

    interface FinishUsingItem {
        IPlayer getPlayer();

        int getDuration();
    }
}
