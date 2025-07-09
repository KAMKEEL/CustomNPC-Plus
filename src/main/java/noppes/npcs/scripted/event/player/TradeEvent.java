package noppes.npcs.scripted.event.player;

import cpw.mods.fml.common.eventhandler.Cancelable;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.event.ITradeEvent;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.constants.EnumScriptType;

@Cancelable
public class TradeEvent extends PlayerEvent implements ITradeEvent {
    public final ICustomNpc npc;
    public final IItemStack currency1;
    public final IItemStack currency2;
    public final IItemStack soldItem;

    public TradeEvent(IPlayer player, ICustomNpc npc, IItemStack currency1, IItemStack currency2, IItemStack soldItem) {
        super(player);
        this.npc = npc;
        this.currency1 = currency1;
        this.currency2 = currency2;
        this.soldItem = soldItem;
    }

    @Override
    public String getHookName() {
        return EnumScriptType.TRADE.function;
    }

    @Override
    public ICustomNpc getNpc() {
        return npc;
    }

    @Override
    public IItemStack getCurrency1() {
        return currency1;
    }

    @Override
    public IItemStack getCurrency2() {
        return currency2;
    }

    @Override
    public IItemStack getSoldItem() {
        return soldItem;
    }
}
