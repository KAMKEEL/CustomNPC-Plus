package noppes.npcs.api.event;

import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.item.IItemStack;

public interface INpcEvent extends ICustomNPCsEvent {
    ICustomNpc getNpc();

    interface TradeEvent extends INpcEvent {
        IPlayer getPlayer();
        IItemStack getCurrency1();
        IItemStack getCurrency2();
        IItemStack getSoldItem();
    }
}
