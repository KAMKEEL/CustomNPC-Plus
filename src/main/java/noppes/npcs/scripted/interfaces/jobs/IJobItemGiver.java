package noppes.npcs.scripted.interfaces.jobs;

import noppes.npcs.scripted.interfaces.entity.IPlayer;
import noppes.npcs.scripted.interfaces.handler.data.IAvailability;
import noppes.npcs.scripted.interfaces.item.IItemStack;

public interface IJobItemGiver extends IJob {

    void setCooldown(int cooldown);

    void setCooldownType(int type);

    int getCooldownType();

    void setGivingMethod(int method);

    int getGivingMethod();

    void setLines(String[] lines);

    String[] getLines();

    void setAvailability(IAvailability availability);

    IAvailability getAvailability();

    void setItem(int slot, IItemStack item);

    IItemStack[] getItems();

    boolean giveItems(IPlayer player);

    boolean canPlayerInteract(IPlayer player);
}
