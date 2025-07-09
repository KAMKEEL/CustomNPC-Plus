package noppes.npcs.api.event;

import noppes.npcs.api.entity.IPlayer;

public interface IPlayerEvent extends ICustomNPCsEvent {
    /**
     * @return player involved in the event
     */
    IPlayer getPlayer();
}
