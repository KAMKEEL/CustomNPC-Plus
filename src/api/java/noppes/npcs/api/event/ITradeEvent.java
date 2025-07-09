package noppes.npcs.api.event;

import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.item.IItemStack;

/**
 * Fired when a player trades with an NPC.
 */
public interface ITradeEvent extends IPlayerEvent {
    /**
     * @return The NPC being traded with
     */
    ICustomNpc getNpc();

    /**
     * @return The first currency item for the trade or null
     */
    IItemStack getCurrency1();

    /**
     * @return The second currency item for the trade or null
     */
    IItemStack getCurrency2();

    /**
     * @return The item being bought from the NPC
     */
    IItemStack getSoldItem();
}
