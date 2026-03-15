package noppes.npcs.api.event;

import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntity;

/**
 * Wrapper events for Forge events, allowing scripts to listen for native Forge events.
 */
public interface IForgeEvent extends ICustomNPCsEvent {

    /** @return the underlying Forge event. */
    Object getEvent();

    /**
     * Fired for Forge world events.
     * @hookName forgeWorld
     */
    interface WorldEvent extends IForgeEvent {
        /** @return the world associated with this event. */
        IWorld getWorld();
    }

    /**
     * Fired for Forge entity events.
     * @hookName forgeEntity
     */
    interface EntityEvent extends IForgeEvent {
        /** @return the entity associated with this event. */
        IEntity getEntity();
    }

    /**
     * Fired during Forge initialization.
     * @hookName forgeInit
     */
    interface InitEvent extends IForgeEvent {
    }
}
