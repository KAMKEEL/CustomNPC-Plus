package noppes.npcs.api.event;

import cpw.mods.fml.common.eventhandler.Event;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntity;

public interface IForgeEvent extends ICustomNPCsEvent {

    Event getEvent();

    interface WorldEvent {
        IWorld getWorld();
    }

    interface EntityEvent {
        IEntity getEntity();
    }

    interface InitEvent {
    }
}
