package noppes.npcs.api.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.IFaction;

public interface IFactionEvent extends ICustomNPCsEvent {

    IPlayer getPlayer();

    IFaction getFaction();

    @Cancelable
    interface FactionPoints extends IFactionEvent {
        boolean decreased();

        int getPoints();
    }
}
