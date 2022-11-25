package noppes.npcs.api.event;

import cpw.mods.fml.common.eventhandler.Event;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntity;

public interface IForgeEvent extends ICustomNPCsEvent {

    Event getEvent();

    interface WorldEvent extends IForgeEvent {
        IWorld getWorld();
    }

    interface EntityEvent extends IForgeEvent {
        IEntity getEntity();
    }

    interface InitEvent extends IForgeEvent {
    }
}
