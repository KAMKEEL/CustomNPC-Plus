package noppes.npcs.api.handler;

import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.IParty;

public interface IPartyHandler {


    /**
     * @param player - The Leader of the Party
     * @return IParty Object
     */
    IParty createParty(IPlayer player);

    /**
     * @param player - Gets the party of current player and disbands it
     */
    void disbandParty(IPlayer player);
}
