//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.scripted.entity;

import net.minecraft.entity.EntityCreature;
import noppes.npcs.scripted.ITimers;
import noppes.npcs.scripted.entity.IEntityLiving;
import noppes.npcs.scripted.entity.IEntityLivingBase;
import noppes.npcs.scripted.entity.IPlayer;
import noppes.npcs.scripted.entity.IProjectile;
import noppes.npcs.scripted.entity.data.INPCAdvanced;
import noppes.npcs.scripted.entity.data.INPCAi;
import noppes.npcs.scripted.entity.data.INPCDisplay;
import noppes.npcs.scripted.entity.data.INPCInventory;
import noppes.npcs.scripted.entity.data.INPCJob;
import noppes.npcs.scripted.entity.data.INPCRole;
import noppes.npcs.scripted.entity.data.INPCStats;
import noppes.npcs.scripted.handler.data.IDialog;
import noppes.npcs.scripted.handler.data.IFaction;
import noppes.npcs.scripted.item.IItemStack;

public interface ICustomNpc<T extends EntityCreature> extends IEntityLiving<T> {
    INPCDisplay getDisplay();

    INPCInventory getInventory();

    INPCStats getStats();

    INPCAi getAi();

    INPCAdvanced getAdvanced();

    IFaction getFaction();

    void setFaction(int var1);

    INPCRole getRole();

    INPCJob getJob();

    ITimers getTimers();

    int getHomeX();

    int getHomeY();

    int getHomeZ();

    IEntityLivingBase getOwner();

    void setHome(int var1, int var2, int var3);

    void reset();

    void say(String var1);

    void sayTo(IPlayer var1, String var2);

    IProjectile shootItem(IEntityLivingBase var1, IItemStack var2, int var3);

    IProjectile shootItem(double var1, double var3, double var5, IItemStack var7, int var8);

    void giveItem(IPlayer var1, IItemStack var2);

    void setDialog(int var1, IDialog var2);

    IDialog getDialog(int var1);

    void updateClient();

    String executeCommand(String var1);
}
